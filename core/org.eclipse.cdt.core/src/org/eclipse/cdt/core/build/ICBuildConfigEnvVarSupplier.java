package org.eclipse.cdt.core.build;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;

/**
 * @since 6.0
 */
public interface ICBuildConfigEnvVarSupplier {

	IEnvironmentVariable getVariable(String name);
	
	IEnvironmentVariable[] getVariables();
	
}
