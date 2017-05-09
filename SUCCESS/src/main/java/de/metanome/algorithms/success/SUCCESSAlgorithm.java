package de.metanome.algorithms.success;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		List<Set<String>> results = new ArrayList<>();
		
		// Initialize our working queue with the column names (first layer of the lattice)
		ArrayDeque<Set<String>> combinationQueue = new ArrayDeque<>(this.columnNames
				.stream()
				.map(columnName -> new HashSet<String>(Arrays.asList(columnName)))
				.collect(Collectors.toList()));
		
		// Iterate over column combinations in the working queue
		while(combinationQueue.size() > 0) {
			Set<String> currentColumns = combinationQueue.pop();
			
			// Discard in case of superset of result
			if (results.stream().anyMatch(result -> currentColumns.containsAll(result))) {
				continue;
			}
			
			// If the current combination is unique, we add it to the result.
			// From here, we don't need to generate posterior nodes of the lattice because we want to have the minimal UCCs.
			if (this.isUnique(currentColumns, records)) {
				results.add(currentColumns);
			
			// The current combination is not unique, so we generate the posterior nodes of the lattice and add them to the working queue.
			} else {
				generatePostCombinations(currentColumns).stream()
					.forEach(columnCombination -> combinationQueue.add(columnCombination));
			}
		}
		
		// Convert list of sets of column names to unique column combinations
		return results.stream()
			.map(columnCombination -> {
				ColumnIdentifier[] columnIdentifiers = columnCombination
						.stream()
						.map(columnName -> new ColumnIdentifier(this.relationName, columnName))
						.toArray(ColumnIdentifier[]::new);
				UniqueColumnCombination ucc = new UniqueColumnCombination(new ColumnCombination(columnIdentifiers));
				return ucc;
			})
			.collect(Collectors.toList());
	}
	
	private boolean isUnique(Set<String> columns, List<List<String>> records) {
		// First, map the column names to indices to access the right columns in the records
		List<Integer> columnIndices = columns.stream()
			.map(columnName -> this.columnNames.indexOf(columnName))
			.collect(Collectors.toList());
		
		// Put the regarding lines of our records together in one string.
		// Filter for rows containing NULL values and ignore them since NULL != NULL.
		List<String> projectedRecords = records.stream()
				.map(row -> this.projectRow(row, columnIndices))
				.filter(row -> !row.contains(null))
				.map(row -> row.stream().collect(Collectors.joining("|")))
				.collect(Collectors.toList());
		
		// Store the rows in a hash set. If two values collide, break.
		HashSet<String> uniqueRows = new HashSet<>(projectedRecords.size());
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
			.map(columnIndex -> row.get(columnIndex))
			.collect(Collectors.toList());
	}
	
	private List<Set<String>> generatePostCombinations(Set<String> priorColumns) {
		// Generate new column combinations from given node.
		// Combinations already in the result are handled directly in generateResult().
		Set<String> remainingColumns = new HashSet<>(this.columnNames);
		remainingColumns.removeAll(priorColumns);
		
		return remainingColumns
			.stream()
			.map(columnName -> {
				Set<String> newColumns = new HashSet<>(priorColumns);
				newColumns.add(columnName);
				return newColumns;
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
