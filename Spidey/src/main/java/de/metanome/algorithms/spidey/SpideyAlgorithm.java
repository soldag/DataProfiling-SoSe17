package de.metanome.algorithms.spidey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;

public class SpideyAlgorithm {

	protected RelationalInputGenerator[] inputGenerators = null;
	protected InclusionDependencyResultReceiver resultReceiver = null;

	protected List<RelationalInput> inputs;
	protected List<String> relationNames;
	protected List<List<String>> columnNames;

	public void execute() throws AlgorithmExecutionException {
		System.out.println("Starting your friendly neighborhood IND algorithm.");
		System.out.println("       :");
		System.out.println("       ;");
		System.out.println("      :");
		System.out.println("      ;");
		System.out.println("     /");
		System.out.println("   o/");
		System.out.println(" ._/\\___,");
		System.out.println("     \\");
		System.out.println("     /");
		System.out.println("     `");
		
		this.initialize();
		List<InclusionDependency> results = this.generateResults();
		this.emit(results);
		System.out.println(results);
	}

	protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {		
		this.inputs = new ArrayList<>(this.inputGenerators.length);
		this.relationNames = new ArrayList<>(this.inputGenerators.length);
		this.columnNames = new ArrayList<>(this.inputGenerators.length);
		
		for (RelationalInputGenerator inputGenerator : this.inputGenerators) {
			RelationalInput input = inputGenerator.generateNewCopy();
			this.inputs.add(input);
			this.relationNames.add(input.relationName());
			this.columnNames.add(input.columnNames());
		}
	}

	protected List<InclusionDependency> generateResults() throws InputIterationException {
		// Initialize priority queues and refs for columns
		List<PriorityQueue<String>> sortedColumns = new ArrayList<PriorityQueue<String>>();
		List<Set<Integer>> refList = new ArrayList<Set<Integer>>();		
		int globalColumnsCount = IntStream.range(0, this.relationNames.size()).map(i -> this.columnNames.get(i).size()).sum();
		Set<Integer> columnIndices = IntStream.range(0, globalColumnsCount).boxed().collect(Collectors.toSet());
		for (int i = 0; i < globalColumnsCount; i++) {
			sortedColumns.add(new PriorityQueue<String>());
			int columnIndex = i; // otherwise there will be scope problems
			refList.add(columnIndices.stream().filter(item -> item != columnIndex).collect(Collectors.toSet()));
		}

		// Fill priority queues
		for(int i = 0; i < this.inputs.size(); i++) {
			RelationalInput input = this.inputs.get(i);
			while (input.hasNext()) {
				List<String> row = input.next();
				for (int j = 0; j < row.size(); j++) {
					String currentValue = row.get(j);
					PriorityQueue<String> currentSortedColumn = sortedColumns.get(this.getGlobalColumnIndex(i, j));
					if (!currentSortedColumn.contains(currentValue) && currentValue != null) {
						currentSortedColumn.add(currentValue);
					}
				}
			}
		}
		
		// While there are values left, get the next smallest value
		// All columns containing this value are added to the set attributesToProcess
		// After that, all refs are intersected with the attributes to process and the priority queues are updated
		int remainingQueues = sortedColumns.size();
		while (remainingQueues > 1) {
			String minValue = sortedColumns.stream()
				.map(column -> column.peek())
				.filter(value -> value != null)
				.min(String.CASE_INSENSITIVE_ORDER)
				.get();
			Set<Integer> attributesToProcess = new HashSet<Integer>();
			for (Integer columnIndex : columnIndices) {
				PriorityQueue<String> current = sortedColumns.get(columnIndex);
				if (!current.isEmpty() && current.peek().equals(minValue)) { // TODO String.valueof() needed?
					attributesToProcess.add(columnIndex);
					current.remove();
					if (current.isEmpty()) {
						remainingQueues--;
					};
				}
			}
			
			for(int columnIndex: attributesToProcess) {
				refList.get(columnIndex).retainAll(attributesToProcess);
			}
		}
		
		// Convert refs to list of inclusion dependencies
		List<InclusionDependency> results = new ArrayList<InclusionDependency>();
		for (int dependantIndex: columnIndices) {
			for(int referencedIndex: refList.get(dependantIndex)) {
				ColumnPermutation dependant = this.createColumnPermutation(dependantIndex);
				ColumnPermutation referenced = this.createColumnPermutation(referencedIndex);
				results.add(new InclusionDependency(dependant, referenced));
			}
		}
		
		return results;
	}
	
	private int getGlobalColumnIndex(int inputIndex, int columnIndex) {
		return IntStream.range(0, inputIndex).map(i -> this.inputs.get(i).columnNames().size()).sum() + columnIndex;
	}
	
	private String[] getRelationColumnName(int globalColumnIndex) {
		// Get relation name
		int startIndex = 0;
		int relationIndex = -1;
		for(int i = 0; i< this.relationNames.size(); i++) {
			relationIndex = i;
			
			int newStartIndex = startIndex + this.columnNames.get(i).size();
			if(newStartIndex > globalColumnIndex) {
				break;
			}
			else {
				startIndex = newStartIndex;
			}
		}
		
		// Get column name
		if(relationIndex >= 0) {
			int columnIndex = globalColumnIndex - startIndex;
			String relationName = this.relationNames.get(relationIndex);
			String columnName = this.columnNames.get(relationIndex).get(columnIndex);
			
			return new String[] {relationName, columnName};
		}
		
		return null;
	}
	
	private ColumnPermutation createColumnPermutation(Integer... columnIndices) {
		ColumnIdentifier[] columnIdentifiers = Arrays.stream(columnIndices)
				.map(globalIndex -> this.getRelationColumnName(globalIndex))
				.map(relationColumnNames -> new ColumnIdentifier(relationColumnNames[0], relationColumnNames[1]))
				.toArray(ColumnIdentifier[]::new);
		return new ColumnPermutation(columnIdentifiers);
	}

	protected void emit(List<InclusionDependency> results) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		for (InclusionDependency ind : results)
			this.resultReceiver.receiveResult(ind);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}