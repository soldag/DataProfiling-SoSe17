package de.metanome.algorithms.taney;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;

public class TaneyAlgorithm {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected FunctionalDependencyResultReceiver resultReceiver = null;
	
	protected String relationName;
	protected List<String> columnNames;
	protected Map<ColumnCombinationBitset, PositionListIndex> plis;
	
	public void execute() throws AlgorithmExecutionException {
		
		this.initialize();
		RelationalInput input = this.inputGenerator.generateNewCopy();
		List<FunctionalDependency> results = this.generateResults(input);
		System.out.println(results);
		this.emit(results);		
	}
	
	protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {

		RelationalInput input = this.inputGenerator.generateNewCopy();
		this.relationName = input.relationName();
		this.columnNames = input.columnNames();
		this.plis = new HashMap<>();
	}
	
	protected List<FunctionalDependency> generateResults(RelationalInput input) throws InputIterationException {

		Map<ColumnCombinationBitset, List<PseudoFunctionalDependency>> results = new HashMap<>();
		
		// Build PLIs for single columns and add them to our PLI-map
		PLIBuilder builder = new PLIBuilder(input);
		List<PositionListIndex> singletonPlis = builder.getPLIList();
		for(int i = 0; i < singletonPlis.size(); i++) {
			this.plis.put(new ColumnCombinationBitset(i), singletonPlis.get(i));
		}
		
		// Initialize our working queue with the column combinations of size 2 (second layer of the lattice)
		ArrayDeque<ColumnCombinationBitset> combinationQueue = new ArrayDeque<>();
		for(int i = 0; i < this.columnNames.size(); i++) {
			for(int j = i + 1; j < this.columnNames.size(); j++) {
				combinationQueue.add(new ColumnCombinationBitset(i, j));
			}
		}
		
		// Iterate over column combinations in the working queue
		while(combinationQueue.size() > 0) {
			ColumnCombinationBitset columnCombination = combinationQueue.pop();
			
			// Generate possible FDs
			for(int rhsIndex : columnCombination.getSetBits()) {
				ColumnCombinationBitset rhs = new ColumnCombinationBitset(rhsIndex);
				ColumnCombinationBitset lhs = columnCombination.minus(rhs);
				
				// TODO: Candidate pruning
				
				// Check for functional dependency
				if(this.isFd(lhs, rhs)) {
					PseudoFunctionalDependency newFd = new PseudoFunctionalDependency(lhs, rhs);
					if(results.containsKey(rhs)) {
						results.get(rhs).add(newFd);
					}
					else {
						// Only Array.asList() creates array of fixed size
						results.put(rhs, new ArrayList<PseudoFunctionalDependency>(Arrays.asList(newFd)));
					}
				}
			}
			
			this.generatePostCombinations(columnCombination).stream().forEach(combinationQueue::add);
		}
		
		// Convert map of LHS and RHS of FDs to functional dependencies
		return results.values().stream()
				.flatMap(pseudoFds -> pseudoFds.stream())
				.map(pseudoFd -> pseudoFd.materialize(this.relationName, this.columnNames))
				.collect(Collectors.toList());
	}
	
	private boolean isFd(ColumnCombinationBitset lhs, ColumnCombinationBitset rhs) {		
		PositionListIndex lhsPli = this.plis.get(lhs);
		PositionListIndex rhsPli = this.plis.get(rhs);
		PositionListIndex combinedPli = rhsPli.intersect(lhsPli);
		
		// Store combined PLI, we will probably need it later
		ColumnCombinationBitset combinedColumn = lhs.union(rhs);
		this.plis.put(combinedColumn, combinedPli);
		
		// Partitioning (it's magic!)
		return lhsPli.getRawKeyError() == combinedPli.getRawKeyError();
	}
	
	private List<ColumnCombinationBitset> generatePostCombinations(ColumnCombinationBitset priorColumnCombination) {
		// Generate the next nodes in lattice
		int maxColumnIndex = Collections.max(priorColumnCombination.getSetBits());		
		return IntStream.range(maxColumnIndex + 1, this.columnNames.size())
			.boxed()
			.map(columnIndex -> {
				List<Integer> bits = priorColumnCombination.getSetBits();
				bits.add(columnIndex);
				return new ColumnCombinationBitset(bits);
			})
			.collect(Collectors.toList());
	}
	
	protected void emit(List<FunctionalDependency> results) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		for (FunctionalDependency fd : results)
			this.resultReceiver.receiveResult(fd);
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}

	private class PseudoFunctionalDependency {
		ColumnCombinationBitset lhs;
		ColumnCombinationBitset rhs;
		
		public PseudoFunctionalDependency(ColumnCombinationBitset lhs, ColumnCombinationBitset rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public FunctionalDependency materialize(String relationName, List<String> columnNames) {		
			ColumnCombination lhsCombination = lhs.createColumnCombination(relationName, columnNames);
			// RHS should only have one bit set
			int rhsIndex = this.rhs.getSetBits().get(0);
			ColumnIdentifier rhsIdentifier = new ColumnIdentifier(relationName, columnNames.get(rhsIndex));
			
			return new FunctionalDependency(lhsCombination, rhsIdentifier);
		}
	}
}
