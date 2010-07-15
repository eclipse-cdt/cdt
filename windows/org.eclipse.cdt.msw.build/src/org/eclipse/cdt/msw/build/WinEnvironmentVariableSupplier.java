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

/**
 * @author DSchaefer
 *
 */
public class WinEnvironmentVariableSupplier
	implements IConfigurationEnvironmentVariableSupplier, IProjectEnvironmentVariableSupplier {
	
	private Map<String, IBuildEnvironmentVariable> envvars;
	
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

	public IBuildEnvironmentVariable getVariable(String variableName,
			IManagedProject project, IEnvironmentVariableProvider provider) {
		if (envvars == null)
			initvars();
		return envvars.get(variableName);
	}
	
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (envvars == null)
			initvars();
		return envvars.get(variableName);
	}

	public IBuildEnvironmentVariable[] getVariables(IManagedProject project,
			IEnvironmentVariableProvider provider) {
		if (envvars == null)
			initvars();
		return envvars.values().toArray(new IBuildEnvironmentVariable[envvars.size()]);
	}
	
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (envvars == null)
			initvars();
		return envvars.values().toArray(new IBuildEnvironmentVariable[envvars.size()]);
	}
	
	private void addvar(IBuildEnvironmentVariable var) {
		envvars.put(var.getName(), var);
	}
	
	public static String getSDKDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		return reg.getLocalMachineValue("SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v7.0", "InstallationFolder");
	}
	
	public static String getVCDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		return reg.getLocalMachineValue("SOFTWARE\\Microsoft\\VisualStudio\\SxS\\VC7", "9.0");
	}
	
	private void initvars() {
		envvars = new HashMap<String, IBuildEnvironmentVariable>();
		
		// The SDK Location
		String sdkDir = getSDKDir();
		if (sdkDir == null)
			return;
		
		String vcDir = getVCDir();
		
		// INCLUDE
		StringBuffer buff = new StringBuffer();
		buff.append(vcDir).append("Include;");
		buff.append(sdkDir).append("Include;");
		buff.append(sdkDir).append("Include\\gl;");
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
