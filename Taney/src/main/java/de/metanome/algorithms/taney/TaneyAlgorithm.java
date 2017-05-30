package de.metanome.algorithms.taney;

import java.util.ArrayDeque;
import java.util.Arrays;
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
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;

public class TaneyAlgorithm {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected FunctionalDependencyResultReceiver resultReceiver = null;
	
	protected String relationName;
	protected List<String> columnNames;
	protected Map<String, PositionListIndex> plis;
	
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
		Map<String, PseudoFunctionalDependency> results = new HashMap<>();
		
		// Build PLIs for single columns
		PLIBuilder builder = new PLIBuilder(input);
		List<PositionListIndex> singletonPlis = builder.getPLIList();
		for(int i = 0; i < singletonPlis.size(); i++) {
			this.plis.put(Arrays.toString(new int[] { i }), singletonPlis.get(i));
		}
		
		// Initialize our working queue with the column combinations of size 2 (second layer of the lattice)
		ArrayDeque<int[]> combinationQueue = new ArrayDeque<>();
		for(int i = 0; i < this.columnNames.size(); i++) {
			for(int j = i + 1; j < this.columnNames.size(); j++) {
				combinationQueue.add(new int[] {i, j});
			}
		}
		
		// Iterate over column combinations in the working queue
		while(combinationQueue.size() > 0) {
			int[] columnCombination = combinationQueue.pop();
			
			// Generate possible fds
			for(int rhs: columnCombination) {
				int[] lhs = Arrays.stream(columnCombination)
					.filter(column -> column != rhs)
					.toArray();
				
				if(this.isFD(lhs, rhs)) {
					results.put(Arrays.toString(lhs), new PseudoFunctionalDependency(lhs, rhs));
				}
				
				this.generatePostCombinations(columnCombination).stream()
					.forEach(combinationQueue::add);
			}
		}
		
		// Convert map of lhs and rhs of fds to functional dependencies
		return results.values().stream()
				.map(pseudoFd -> pseudoFd.materialize(this.relationName, this.columnNames))
				.collect(Collectors.toList());
	}
	
	private boolean isFD(int[] lhs, int rhs) {		
		PositionListIndex lhsPli = this.plis.get(Arrays.toString(lhs));
		PositionListIndex rhsPli = this.plis.get(Arrays.toString(new int[] { rhs }));
		PositionListIndex combinedPli = rhsPli.intersect(lhsPli);
		
		int[] combination = IntStream.concat(Arrays.stream(lhs), IntStream.of(rhs)).toArray();
		this.plis.put(Arrays.toString(combination), combinedPli);
		
		return lhsPli.getRawKeyError() == combinedPli.getRawKeyError();
	}
	
	private List<int[]> generatePostCombinations(int[] priorColumnIndices) {
		// Generate new column combinations from given node.
		// Combinations already in the result are handled directly in generateResult().
		int maxColumnIndex = priorColumnIndices[priorColumnIndices.length - 1];
		
		return IntStream.range(maxColumnIndex + 1, this.columnNames.size())
			.boxed()
			.map(columnIndex -> IntStream.concat(Arrays.stream(priorColumnIndices), IntStream.of(columnIndex)).toArray())
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
		private int[] lhs;
		private int rhs;
		
		public PseudoFunctionalDependency(int[] lhs, int rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public FunctionalDependency materialize(String relationName, List<String> columnNames) {
			ColumnIdentifier[] lhsIdentifiers = Arrays.stream(this.lhs)
					.boxed()
					.map(columnIndex -> new ColumnIdentifier(relationName, columnNames.get(columnIndex)))
					.toArray(ColumnIdentifier[]::new);		
			ColumnCombination lhsCombination = new ColumnCombination(lhsIdentifiers);
			
			ColumnIdentifier rhsIdentifier = new ColumnIdentifier(relationName, columnNames.get(this.rhs));
			
			return new FunctionalDependency(lhsCombination, rhsIdentifier);
		}
	}
}
