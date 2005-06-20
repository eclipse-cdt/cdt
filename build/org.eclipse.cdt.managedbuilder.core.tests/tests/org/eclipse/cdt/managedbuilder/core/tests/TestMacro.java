/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

 package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedIsToolChainSupported;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IReservedMacroNameSupplier;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
/**
 * 
 */

/**
 * 
 *
 */
public class TestMacro implements 
	IConfigurationBuildMacroSupplier, 
	IProjectBuildMacroSupplier,
	IReservedMacroNameSupplier,
	IConfigurationEnvironmentVariableSupplier,
	IProjectEnvironmentVariableSupplier,
	IManagedIsToolChainSupported,
	IManagedBuilderMakefileGenerator
	{

	public static boolean supported[] = {false, false, false, false, false};
	public static IPath topBuildDir = null;
	
	public static String CFG_VAR = "CFG_PROVIDER_VAR"; //$NON-NLS-1$
	public static String PRJ_VAR = "PRJ_PROVIDER_VAR"; //$NON-NLS-1$
	
//	IConfigurationBuildMacroSupplier
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier#getMacro(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider)
	 */
	public IBuildMacro getMacro(String macroName, IConfiguration configuration,
			IBuildMacroProvider provider) {
		ManagedBuildMacrosTests.functionCalled |= ManagedBuildMacrosTests.GET_ONE_CONFIG;
		
		IBuildMacro ms = null;
		if (!(provider instanceof TestMacro)) {
			ms = provider.getMacro(macroName, 
					IBuildMacroProvider.CONTEXT_CONFIGURATION,
					configuration, false);
		}
		return ms;
	}

	public IBuildMacro getMacro(String macroName, IManagedProject mproj,
			IBuildMacroProvider provider) {
		ManagedBuildMacrosTests.functionCalled |= ManagedBuildMacrosTests.GET_ONE_PROJECT;

		IBuildMacro ms = null;
		if (!(provider instanceof TestMacro)) {
			ms = provider.getMacro(macroName, 
					IBuildMacroProvider.CONTEXT_PROJECT,
					mproj, false);
		}
		return ms;
	}

//	IProjectBuildMacroSupplier
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier#getMacros(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider)
	 */
	public IBuildMacro[] getMacros(IConfiguration configuration,
			IBuildMacroProvider provider) {
		ManagedBuildMacrosTests.functionCalled |= ManagedBuildMacrosTests.GET_MANY_CONFIG;
		IBuildMacro[] ms = null;
		if (!(provider instanceof TestMacro)) {
			ms = provider.getMacros(IBuildMacroProvider.CONTEXT_CONFIGURATION, configuration, false);
			IBuildMacro[] newms = null;
			if (ms != null && ms.length > 0) {
				newms = new BuildMacro[ms.length + 1];
				System.arraycopy(ms, 0, newms, 0, ms.length);
			} else {
				newms = new BuildMacro[1];
			}
			newms[ms.length] = new BuildMacro("NEW_FOR_CFG", 1, "NewMacrosForConfigContext");  //$NON-NLS-1$ //$NON-NLS-2$
			return newms;
		}
		return null;
	}

	/**
	 * 
	 */
	public IBuildMacro[] getMacros(IManagedProject mproj,
			IBuildMacroProvider provider) {
		ManagedBuildMacrosTests.functionCalled |= ManagedBuildMacrosTests.GET_MANY_PROJECT;
		IBuildMacro[] ms = null;
		if (!(provider instanceof TestMacro)) {
			ms = provider.getMacros(IBuildMacroProvider.CONTEXT_PROJECT, mproj, false);
			IBuildMacro[] newms = null;
			if (ms != null && ms.length > 0) {
				newms = new BuildMacro[ms.length + 1];
				System.arraycopy(ms, 0, newms, 0, ms.length);
			} else {
				newms = new BuildMacro[1];
			}
			newms[newms.length - 1] = new BuildMacro("NEW_FOR_PRJ", 1, "NewMacrosForProjectContext");  //$NON-NLS-1$ //$NON-NLS-2$
			return newms;
		}
		return null;
	}

//	IReservedMacroNameSupplier
	
	/**
	 * 
	 */
	public boolean isReservedName(String macroName, IConfiguration configuration) {
		ManagedBuildMacrosTests.functionCalled |= ManagedBuildMacrosTests.RESERVED_NAME;
		if (macroName.equalsIgnoreCase("USERNAME")) return true;  //$NON-NLS-1$
		return false;
	}
	
//	IConfigurationEnvironmentVariableSupplier
	
	/**
	 * 
	 */
	public IBuildEnvironmentVariable getVariable(String variableName, 
			IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (CFG_VAR.equals(variableName)) {
			return new BuildEnvVar(CFG_VAR, CFG_VAR + configuration.getName());
		} else
			return null;
	}

	/**
	 * 
	 */
	public IBuildEnvironmentVariable[] getVariables (IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable v = getVariable(CFG_VAR, configuration, provider);
		if (v != null) {
			IBuildEnvironmentVariable[] vs = new IBuildEnvironmentVariable[1];
			vs[0] = v;
			return(vs);
		} else	
			return null;
	}
	
//	IProjectEnvironmentVariableSupplier
	
	/**
	 * 
	 */
	public IBuildEnvironmentVariable getVariable(String variableName, 
			IManagedProject project,
			IEnvironmentVariableProvider provider) {
		if (PRJ_VAR.equals(variableName)) {
			return new BuildEnvVar(PRJ_VAR, PRJ_VAR + project.getName());
		} else
			return null;
	}
	
	/**
	 * 
	 */
	public IBuildEnvironmentVariable[] getVariables (IManagedProject project,
			IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable v = getVariable(PRJ_VAR, project, provider);
		if (v != null) {
			IBuildEnvironmentVariable[] vs = new IBuildEnvironmentVariable[1];
			vs[0] = v;
			return(vs);
		} else	
			return null;
	}

	
//	IManagedIsToolChainSupported
	
	/**
	 * 
	 */
	public boolean isSupported(IToolChain toolChain, 
			PluginVersionIdentifier version, 
			String instance) {
		if ("One".equals(toolChain.getParent().getName()))   return supported[0]; //$NON-NLS-1$
		if ("Two".equals(toolChain.getParent().getName()))   return supported[1]; //$NON-NLS-1$
		if ("Three".equals(toolChain.getParent().getName())) return supported[2]; //$NON-NLS-1$
		if ("Four".equals(toolChain.getParent().getName()))  return supported[3]; //$NON-NLS-1$
		return false;
	}

	
//	IManagedBuilderMakefileGenerator
	
	/**
	 */
	public IPath getBuildWorkingDir() {
//		System.out.println("---- getBuildWorkingDir: " + topBuildDir); 
		return topBuildDir; 
		}
	public void generateDependencies() {}
	public MultiStatus generateMakefiles(IResourceDelta delta) { return null; }
	public String getMakefileName() {return "test_instead_make"; } //$NON-NLS-1$
	public void initialize(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
//		System.out.println("---- init: " + topBuildDir); 
	}
	public boolean isGeneratedResource(IResource resource) { return false; }
	public void regenerateDependencies(boolean force) {}
	public MultiStatus regenerateMakefiles() { return null; }
}
