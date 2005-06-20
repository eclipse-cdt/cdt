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
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvVarOperationProcessor;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier;

/**
 * This supplier is used to suply macros provided by the tool-integrator
 * 
 * @since 3.0
 */
public class ExternalExtensionMacroSupplier implements IBuildMacroSupplier {
	private static final String fNonOverloadableMacros[] = new String[]{
		//tool-integrators not allowed currently to override the "CWD" and "PWD" macros
		"CWD",   //$NON-NLS-1$
		"PWD"	  //$NON-NLS-1$
	};

	private static ExternalExtensionMacroSupplier fInstance;
	
	private class ExtensionMacroProvider extends BuildMacroProvider{
		private IMacroContextInfo fStartInfo;
		private int fContextType;
		private Object fContextData;
		private boolean fStartInitialized;
		
		public ExtensionMacroProvider(int contextType, Object contextData){
			fContextType = contextType;
			fContextData = contextData;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariable(java.lang.String, java.lang.Object, boolean)
		 */
		public IBuildMacro getMacro(String macroName,
				int contextType,
				Object contextData, 
				boolean includeParent) {
			if(getValidName(macroName) == null)
				return null;
			return super.getMacro(macroName,contextType,contextData,includeParent);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getMacros(int, java.lang.Object, boolean)
		 */
		public IBuildMacro[] getMacros(int contextType,Object contextData, boolean includeParent) {
			return filterMacros(super.getMacros(contextType, contextData,  includeParent));
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getContextInfo(java.lang.Object)
		 */
		public IMacroContextInfo getMacroContextInfo(int contextType,Object contextData){
			IMacroContextInfo startInfo = getStartInfo();
			if(contextType == fContextType &&
					contextData == fContextData)
				return startInfo;
			
			IMacroContextInfo info = super.getMacroContextInfo(contextType, contextData);
			if(info == null)
				return null;
			
			if(checkParentContextRelation(startInfo,info))
				return info;
			return null;
		}
		
		protected IMacroContextInfo getStartInfo(){
			if(fStartInfo == null && !fStartInitialized){
				IMacroContextInfo info = super.getMacroContextInfo(fContextType,fContextData);
				if(info != null){
					IBuildMacroSupplier suppliers[] = info.getSuppliers();
					suppliers = filterValidSuppliers(suppliers);
					if(suppliers != null)
						fStartInfo = new DefaultMacroContextInfo(fContextType,fContextData,suppliers);
					else
						fStartInfo = info.getNext();
					fStartInitialized = true;
				}
				fStartInitialized = true;
			}
			return fStartInfo;
		}
	}

	
	private ExternalExtensionMacroSupplier(){
		
	}
	
	public static ExternalExtensionMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new ExternalExtensionMacroSupplier();
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		if((macroName = getValidName(macroName)) == null)
			return null;
			
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				IConfiguration cfg = (IConfiguration)contextData;
				IConfigurationBuildMacroSupplier supplier = cfg.getBuildMacroSupplier();
				if(supplier == null)
					return null;
				return supplier.getMacro(macroName,cfg,new ExtensionMacroProvider(contextType, contextData));
			}
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if (contextData instanceof IManagedProject) {
				IManagedProject project = (IManagedProject)contextData; 
				IProjectBuildMacroSupplier supplier = project.getProjectType().getBuildMacroSupplier();
				if(supplier == null)
					return null;
				return supplier.getMacro(macroName,project,new ExtensionMacroProvider(contextType, contextData));
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		IBuildMacro macros[] = null;
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				IConfiguration cfg = (IConfiguration)contextData;
				IConfigurationBuildMacroSupplier supplier = cfg.getBuildMacroSupplier();
				if(supplier != null)
					macros = supplier.getMacros(cfg,new ExtensionMacroProvider(contextType, contextData));
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if (contextData instanceof IManagedProject) {
				IManagedProject project = (IManagedProject)contextData; 
				IProjectBuildMacroSupplier supplier = project.getProjectType().getBuildMacroSupplier();
				if(supplier != null)
					macros = supplier.getMacros(project,new ExtensionMacroProvider(contextType, contextData));
			}
		}
		return filterMacros(macros);
	}
	
	protected String getValidName(String name){
		if(name == null || (name = name.trim()).length() == 0)
			return null;
		if(fNonOverloadableMacros != null){
			for(int i = 0; i < fNonOverloadableMacros.length; i++){
				if(fNonOverloadableMacros[i].equals(EnvVarOperationProcessor.normalizeName(name)))
					return null;
			}
		}
		return name;
	}
	
	protected IBuildMacro[] filterMacros(IBuildMacro macros[]){
		return MacroResolver.filterMacros(macros,fNonOverloadableMacros);
	}

	protected IBuildMacroSupplier[] filterValidSuppliers(IBuildMacroSupplier suppliers[]){
		if(suppliers == null)
			return null;

		int i = 0, j = 0;
		for(i = 0; i < suppliers.length; i++){
			if(suppliers[i] == this)
				break;
		}
		
	
		if(i >= suppliers.length)
			return null;
		
		int startNum = i + 1;

		IBuildMacroSupplier validSuppliers[] = 
			new IBuildMacroSupplier[suppliers.length - startNum];
		
		for(i = startNum, j = 0; i < suppliers.length; i++, j++)
			validSuppliers[j] = suppliers[i];
		
		return validSuppliers;
	}

}
