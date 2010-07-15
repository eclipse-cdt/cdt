package org.eclipse.cdt.msw.build;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author DSchaefer
 *
 */
public class WinEnvironmentVariableSupplier
	implements IConfigurationEnvironmentVariableSupplier, IProjectEnvironmentVariableSupplier {
	
	private static Map<String, IBuildEnvironmentVariable> envvars;
	private static String sdkDir;
	private static String vcDir;
	
	private static class WindowsBuildEnvironmentVariable implements IBuildEnvironmentVariable {
		
		private final String name;
		private final String value;
		private final int operation;
		
		public WindowsBuildEnvironmentVariable(String name, String value, int operation) {
			this.name = name;
			this.value = value;
			this.operation = operation;
		}
		
		public String getDelimiter() {
			return ";";
		}
		
		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}

		public int getOperation() {
			return operation;
		}

	}

	public WinEnvironmentVariableSupplier() {
		initvars();
	}
	
	public IBuildEnvironmentVariable getVariable(String variableName,
			IManagedProject project, IEnvironmentVariableProvider provider) {
		return envvars.get(variableName);
	}
	
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return envvars.get(variableName);
	}

	public IBuildEnvironmentVariable[] getVariables(IManagedProject project,
			IEnvironmentVariableProvider provider) {
		return envvars.values().toArray(new IBuildEnvironmentVariable[envvars.size()]);
	}
	
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return envvars.values().toArray(new IBuildEnvironmentVariable[envvars.size()]);
	}
	
	private static String getSDKDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		return reg.getLocalMachineValue("SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.0", "InstallationFolder");
	}
	
	private static String getVCDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		return reg.getLocalMachineValue("SOFTWARE\\Microsoft\\VisualStudio\\SxS\\VC7", "9.0");
	}
	
	public static IPath[] getIncludePath() {
		// Include paths
		if (sdkDir != null) {
			return new IPath[] {
				new Path(vcDir.concat("Include")),
				new Path(sdkDir.concat("Include")),
				new Path(sdkDir.concat("Include\\gl"))
			};
		} else
			return new IPath[0];
	}
	
	private static void addvar(IBuildEnvironmentVariable var) {
		envvars.put(var.getName(), var);
	}
	
	private static synchronized void initvars() {
		if (envvars != null)
			return;
		envvars = new HashMap<String, IBuildEnvironmentVariable>();
		
		// The SDK Location
		sdkDir = getSDKDir();
		if (sdkDir == null)
			return;
		
		vcDir = getVCDir();
		
		// INCLUDE
		StringBuffer buff = new StringBuffer();
		IPath includePaths[] = getIncludePath();
		for (IPath path : includePaths) {
			buff.append(path.toOSString()).append(';');
		}
		addvar(new WindowsBuildEnvironmentVariable("INCLUDE", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));

		// LIB
		buff = new StringBuffer();
		buff.append(vcDir).append("Lib;");
		buff.append(sdkDir).append("Lib;");
		addvar(new WindowsBuildEnvironmentVariable("LIB", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));
		
		// PATH
		buff = new StringBuffer();
		buff.append(vcDir).append("Bin;");
		buff.append(vcDir).append("vcpackages;");
		buff.append(vcDir).append("..\\Common7\\IDE;");
		buff.append(sdkDir).append("Bin;");
		addvar(new WindowsBuildEnvironmentVariable("PATH", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));
	}

}
