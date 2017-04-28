package de.metanome.algorithms.success;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import de.metanome.algorithm_integration.result_receiver.OrderDependencyResultReceiver;
import de.metanome.algorithm_integration.results.OrderDependency;

public class SUCCESSAlgorithm {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected OrderDependencyResultReceiver resultReceiver = null;
	
	protected String relationName;
	protected List<String> columnNames;
	
	protected String someStringParameter;
	protected Integer someIntegerParameter;
	protected Boolean someBooleanParameter;
	
	public void execute() throws AlgorithmExecutionException {
		
		////////////////////////////////////////////
		// THE DISCOVERY ALGORITHM LIVES HERE :-) //
		////////////////////////////////////////////
		// Example: Initialize
		this.initialize();
		// Example: Read input data
		List<List<String>> records = this.readInput();
		// Example: Print what the algorithm read (to test that everything works)
		this.print(records);
		// Example: Generate some results (usually, the algorithm should really calculate them on the data)
		List<OrderDependency> results = this.generateResults();
		// Example: To test if the algorithm outputs results
		this.emit(results);
		/////////////////////////////////////////////
		
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
		// Print parameter
		System.out.println("Some String: " + this.someStringParameter);
		System.out.println("Some Integer: " + this.someIntegerParameter);
		System.out.println("Some Boolean: " + this.someBooleanParameter);
		System.out.println();
		
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
	
	protected List<OrderDependency> generateResults() {
		List<OrderDependency> results = new ArrayList<>();
		ColumnPermutation lhs = new ColumnPermutation(this.getRandomColumn(), this.getRandomColumn());
		ColumnPermutation rhs = new ColumnPermutation(this.getRandomColumn(), this.getRandomColumn());
		OrderDependency od = new OrderDependency(lhs, rhs, OrderDependency.OrderType.LEXICOGRAPHICAL, OrderDependency.ComparisonOperator.STRICTLY_SMALLER);
		results.add(od);
		return results;
	}
	
	protected ColumnIdentifier getRandomColumn() {
		Random random = new Random(System.currentTimeMillis());
		return new ColumnIdentifier(this.relationName, this.columnNames.get(random.nextInt(this.columnNames.size())));
	}
	
	protected void emit(List<OrderDependency> results) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		for (OrderDependency od : results)
			this.resultReceiver.receiveResult(od);
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
