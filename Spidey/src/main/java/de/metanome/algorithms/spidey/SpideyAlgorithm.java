package de.metanome.algorithms.spidey;

import java.util.ArrayList;
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
import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;

public class SpideyAlgorithm {

	protected RelationalInputGenerator inputGenerator = null;
	protected InclusionDependencyResultReceiver resultReceiver = null;

	protected String relationName;
	protected List<String> columnNames;
	protected Map<ColumnCombinationBitset, PositionListIndex> plis;

	public void execute() throws AlgorithmExecutionException {

		this.initialize();
		List<List<String>> records = this.readInput();
		List<InclusionDependency> results = this.generateResults(records);
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
		System.out.println(results);
		this.emit(results);
	}

	protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {
		RelationalInput input = this.inputGenerator.generateNewCopy();
		this.relationName = input.relationName();
		this.columnNames = input.columnNames();
	}
	
	protected List<List<String>> readInput() throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
		List<List<String>> records = new ArrayList<>();
		RelationalInput input = this.inputGenerator.generateNewCopy();
		while (input.hasNext())
			records.add(input.next());
		return records;
	}
	
	protected void print(List<List<String>> records) {		
		// Print schema
		System.out.print(this.relationName + "( ");
		for (String columnName : this.columnNames)
			System.out.print(columnName + " ");
		System.out.println(")");
		
		// Print records
		for (List<String> record : records) {
			System.out.print("| ");
			for (String value : record)
				System.out.print(value + " | ");
			System.out.println();
		}
	}

	protected List<InclusionDependency> generateResults(List<List<String>> input) throws InputIterationException {
		// TODO: Check what happens for multiple input files
		// Initialize priority queues and refs for columns
		List<PriorityQueue<String>> sortedColumns = new ArrayList<PriorityQueue<String>>();
		List<Set<Integer>> refList = new ArrayList<Set<Integer>>();
		Set<Integer> allColumns = IntStream.range(0, this.columnNames.size()).boxed().collect(Collectors.toSet());
		for (int i = 0; i < this.columnNames.size(); i++) {
			sortedColumns.add(new PriorityQueue<String>());
			int columnIndex = i; // otherwise there will be scope problems
			refList.add(allColumns.stream().filter(item -> item != columnIndex).collect(Collectors.toSet()));
		}

		// Fill priority queues
		for (List<String> row : input) {
			for (int i = 0; i < row.size(); i++) {
				PriorityQueue<String> currentColumn = sortedColumns.get(i);
				String currentValue = row.get(i);
				// TODO: Is "" correct representations of null?
				if (!currentColumn.contains(currentValue) && currentValue != "") {
					currentColumn.add(currentValue);
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
			for (Integer columnIndex : allColumns) {
				PriorityQueue<String> current = sortedColumns.get(columnIndex);
				if (String.valueOf(current.peek()).equals(minValue)) {
					attributesToProcess.add(columnIndex);
					current.remove();
					if (current.isEmpty()) {
						remainingQueues--;
					};
				}
			}
			for (Set<Integer> refs : refList) {
				// TODO: Remove from refs
			}
		}
		
		// Convert refs to list of inclusion dependencies
		List<InclusionDependency> results = new ArrayList<InclusionDependency>();
		for (int i = 0; i < refList.size(); i++) {
			Set<Integer> ref = refList.get(i);
			if (!ref.isEmpty()) {
				ColumnPermutation dependant = this.createColumnPermutation(i);
				ColumnPermutation referenced = this.createColumnPermutation(ref.toArray(new Integer[ref.size()]));
				results.add(new InclusionDependency(dependant, referenced));
			}
		}
		return results;
	}
	
	private ColumnPermutation createColumnPermutation(Integer... columnIndices) {
		ColumnIdentifier[] columnIdentifiers = new ColumnIdentifier[columnIndices.length];
		for (int i = 0; i < columnIndices.length; i++) {
			// TODO: Does columnNames break with multiple input files?
			columnIdentifiers[i] = new ColumnIdentifier(this.relationName, this.columnNames.get(columnIndices[i]));
		}
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
