package de.metanome.algorithms.spidey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
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
		RelationalInput input = this.inputGenerator.generateNewCopy();
		List<InclusionDependency> results = this.generateResults(input);
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
		this.plis = new HashMap<>();
	}

	protected List<InclusionDependency> generateResults(RelationalInput input) throws InputIterationException {

		return null;
	}

	protected void emit(List<InclusionDependency> results) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		for (InclusionDependency fd : results)
			this.resultReceiver.receiveResult(fd);
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
