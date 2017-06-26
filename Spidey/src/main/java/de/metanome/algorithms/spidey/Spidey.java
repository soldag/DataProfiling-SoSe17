package de.metanome.algorithms.spidey;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;

public class Spidey extends SpideyAlgorithm 							// Separating the algorithm implementation and the Metanome interface implementation is good practice
						  implements InclusionDependencyAlgorithm, 		// Defines the type of the algorithm, i.e., the result type, for instance, FunctionalDependencyAlgorithm or InclusionDependencyAlgorithm; implementing multiple types is possible
						  			 RelationalInputParameterAlgorithm {	// Defines the input type of the algorithm; relational input is any relational input from files or databases; more specific input specifications are possible

	public enum Identifier {
		INPUT_GENERATOR
	};

	@Override
	public String getAuthors() {
		return "Soeren Oldag, Tamara Slosarek"; // A string listing the author(s) of this algorithm
	}

	@Override
	public String getDescription() {
		return "An IND algorithm returning unary inclusion dependencies"; // A string briefly describing what this algorithm does
	}
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() { // Tells Metanome which and how many parameters the algorithm needs
		ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
		conf.add(new ConfigurationRequirementRelationalInput(Spidey.Identifier.INPUT_GENERATOR.name(),
			ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
		
		return conf;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (!Spidey.Identifier.INPUT_GENERATOR.name().equals(identifier))
			this.handleUnknownConfiguration(identifier, values);
		this.inputGenerators = values;
	}

	@Override
	public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void execute() throws AlgorithmExecutionException {
		super.execute();
	}

	private void handleUnknownConfiguration(String identifier, Object[] values) throws AlgorithmConfigurationException {
		throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> [" + concat(values, ",") + "]");
	}
	
	private static String concat(Object[] objects, String separator) {
		if (objects == null)
			return "";
		
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < objects.length; i++) {
			buffer.append(objects[i].toString());
			if ((i + 1) < objects.length)
				buffer.append(separator);
		}
		return buffer.toString();
	}
}
