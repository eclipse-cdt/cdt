package org.eclipse.cdt.core.build;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * Toolchains are a collection of tools that take the source code and converts
 * it into an executable system.
 * 
 * @since 6.0
 */
public interface IToolChain extends IAdaptable {

	IToolChainType getType();

	String getName();

	IEnvironmentVariable getVariable(String name);

	IEnvironmentVariable[] getVariables();

	boolean supports(ILaunchTarget target);

	IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, Path command, List<String> args,
			List<String> includePaths, IResource resource, Path buildDirectory);

	String[] getErrorParserIds();

	Path getCommandPath(String command);

}
