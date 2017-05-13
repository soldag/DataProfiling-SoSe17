package de.metanome.algorithms.success;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;

public class SUCCESSAlgorithm {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected UniqueColumnCombinationResultReceiver resultReceiver = null;
	
	protected String relationName;
	protected List<String> columnNames;
	
	public void execute() throws AlgorithmExecutionException {
		
		this.initialize();
		List<List<String>> records = this.readInput();
		List<UniqueColumnCombination> results = this.generateResults(records);
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
	
	protected List<UniqueColumnCombination> generateResults(List<List<String>> records) {
		List<List<Integer>> results = new ArrayList<>();
		
		// Initialize our working queue with the column names (first layer of the lattice)
		ArrayDeque<List<Integer>> combinationQueue = new ArrayDeque<>(
				IntStream.range(0, this.columnNames.size())
				.boxed()
				.map(Arrays::asList)
				.collect(Collectors.toList()));
		
		// Iterate over column combinations in the working queue
		while(combinationQueue.size() > 0) {
			List<Integer> columnCombination = combinationQueue.pop();
			
			// Discard in case of superset of result
			if (results.stream().anyMatch(columnCombination::containsAll)) {
				continue;
			}
			
			// If the current combination is unique, we add it to the result.
			// From here, we don't need to generate posterior nodes of the lattice because we want to have the minimal UCCs.
			if (this.isUnique(columnCombination, records)) {
				results.add(columnCombination);
			
			// The current combination is not unique, so we generate the posterior nodes of the lattice and add them to the working queue.
			} else {
				generatePostCombinations(columnCombination).stream()
					.forEach(combinationQueue::add);
			}
		}
		
		// Convert list of sets of column names to unique column combinations
		return results.stream()
			.map(columnCombination -> {
				ColumnIdentifier[] columnIdentifiers = columnCombination
						.stream()
						.map(columnIndex -> new ColumnIdentifier(this.relationName, this.columnNames.get(columnIndex)))
						.toArray(ColumnIdentifier[]::new);
				UniqueColumnCombination ucc = new UniqueColumnCombination(new ColumnCombination(columnIdentifiers));
				return ucc;
			})
			.collect(Collectors.toList());
	}
	
	private boolean isUnique(List<Integer> columnIndices, List<List<String>> records) {		
		// Put the regarding lines of our records together in one string.
		// Filter for rows containing NULL values and ignore them since NULL != NULL.
		List<String> projectedRecords = records.stream()
				.map(row -> this.projectRow(row, columnIndices))
				.filter(row -> !row.contains(null))
				.map(row -> row.stream().collect(Collectors.joining("|")))
				.collect(Collectors.toList());
		
		// Store the rows in a hash set. If two values collide, break.
		Set<String> uniqueRows = new HashSet<>(projectedRecords.size());
		for (String row : projectedRecords) {
			if (!uniqueRows.add(row)) {
				return false;
			}
		}
		
		return true;
	}
	
	private List<String> projectRow(List<String> row, List<Integer> columnIndices) {
		return columnIndices
			.stream()
			.map(row::get)
			.collect(Collectors.toList());
	}
	
	private List<List<Integer>> generatePostCombinations(List<Integer> priorColumnIndices) {
		// Generate new column combinations from given node.
		// Combinations already in the result are handled directly in generateResult().
		int maxColumnIndex = priorColumnIndices.get(priorColumnIndices.size() - 1);
		
		return IntStream.range(maxColumnIndex + 1, this.columnNames.size())
			.boxed()
			.map(columnIndex -> {
				List<Integer> newColumnIndices = new ArrayList<>(priorColumnIndices);
				newColumnIndices.add(columnIndex);
				return newColumnIndices;
			})
			.collect(Collectors.toList());
	}
	
	protected void emit(List<UniqueColumnCombination> results) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		for (UniqueColumnCombination ucc : results)
			this.resultReceiver.receiveResult(ucc);
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
