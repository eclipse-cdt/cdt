/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined by the tool-integrator
 * 
 * @since 3.0
 */
public class ExternalExtensionEnvironmentSupplier implements
		IEnvironmentVariableSupplier {
	private static final String fNonOverloadableVariables[] = new String[]{
			//tool-integrators not allowed currently to override the "CWD" and "PWD" variables
			EnvVarOperationProcessor.normalizeName("CWD"),   //$NON-NLS-1$
			EnvVarOperationProcessor.normalizeName("PWD")	  //$NON-NLS-1$
		};

	/**
	 * EnvironmentVariableProvider passed to the tool-integrator provided
	 * suppliers.
	 * Accepts only contexts lower than the one passed to a suppler  
	 * 
	 * @since 3.0
	 */
	private class ExtensionEnvVarProvider extends EnvironmentVariableProvider{
		private IContextInfo fStartInfo;
		private Object fStartLevel;
		private boolean fStartInitialized;
		
		public ExtensionEnvVarProvider(Object level){
			fStartLevel = level;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariable(java.lang.String, java.lang.Object, boolean)
		 */
		public IBuildEnvironmentVariable getVariable(String variableName,
				Object level, boolean includeParentLevels) {
			if(getValidName(variableName) == null)
				return null;
			return super.getVariable(variableName,level,includeParentLevels);
		}

		public IBuildEnvironmentVariable[] getVariables(Object level, boolean includeParentLevels) {
			return filterVariables(super.getVariables(level,includeParentLevels));
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getContextInfo(java.lang.Object)
		 */
		protected IContextInfo getContextInfo(Object level){
			IContextInfo startInfo = getStartInfo();
			if(level == fStartLevel)
				return startInfo;
			
			IContextInfo info = super.getContextInfo(level);
			if(info == null)
				return null;
			
			if(checkParentContextRelation(startInfo,info))
				return info;
			return null;
		}
		
		protected IContextInfo getStartInfo(){
			if(fStartInfo == null && !fStartInitialized){
				IContextInfo info = super.getContextInfo(fStartLevel);
				if(info != null){
					IEnvironmentVariableSupplier suppliers[] = info.getSuppliers();
					suppliers = filterValidSuppliers(suppliers);
					if(suppliers != null)
						fStartInfo = new DefaultContextInfo(fStartLevel,suppliers);
					else
						fStartInfo = info.getNext();
					fStartInitialized = true;
				}
				fStartInitialized = true;
			}
			return fStartInfo;
		}
		
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getStoredBuildPathVariables(int)
		 */
		protected StoredBuildPathEnvironmentContainer getStoredBuildPathVariables(int buildPathType){
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getStoredIncludeBuildPathVariables()
		 */
		protected StoredBuildPathEnvironmentContainer getStoredIncludeBuildPathVariables(){
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getStoredLibraryBuildPathVariables()
		 */
		protected StoredBuildPathEnvironmentContainer getStoredLibraryBuildPathVariables(){
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	public IBuildEnvironmentVariable getVariable(String name, Object context) {
		if(context == null)
			return null;
		if((name = getValidName(name)) == null)
			return null;
			
		else if(context instanceof IConfiguration){
			IConfiguration cfg = (IConfiguration)context;
			IConfigurationEnvironmentVariableSupplier supplier = cfg.getEnvironmentVariableSupplier();
			if(supplier == null)
				return null;
			return supplier.getVariable(name,cfg,new ExtensionEnvVarProvider(context));
		}
		else if (context instanceof IManagedProject) {
			IManagedProject project = (IManagedProject)context; 
			IProjectEnvironmentVariableSupplier supplier = project.getProjectType().getEnvironmentVariableSupplier();
			if(supplier == null)
				return null;
			return supplier.getVariable(name,project,new ExtensionEnvVarProvider(context));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	public IBuildEnvironmentVariable[] getVariables(Object context) {
		if(context == null)
			return null;
		IBuildEnvironmentVariable variables[] = null;
		if(context instanceof IConfiguration){
			IConfiguration cfg = (IConfiguration)context;
			IConfigurationEnvironmentVariableSupplier supplier = cfg.getEnvironmentVariableSupplier();
			if(supplier == null)
				return null;
			variables = supplier.getVariables(cfg,new ExtensionEnvVarProvider(context));
		}
		else if (context instanceof IManagedProject) {
			IManagedProject project = (IManagedProject)context; 
			IProjectEnvironmentVariableSupplier supplier = project.getProjectType().getEnvironmentVariableSupplier();
			if(supplier == null)
				return null;
			variables = supplier.getVariables(project,new ExtensionEnvVarProvider(context));
		}
		
		return filterVariables(variables);
	}

	protected IEnvironmentVariableSupplier[] filterValidSuppliers(IEnvironmentVariableSupplier suppliers[]){
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


		IEnvironmentVariableSupplier validSuppliers[] = 
			new IEnvironmentVariableSupplier[suppliers.length - startNum];
		
		for(i = startNum, j = 0; i < suppliers.length; i++, j++)
			validSuppliers[j] = suppliers[i];
		
		return validSuppliers;
	}
	
	protected String getValidName(String name){
		name = EnvVarOperationProcessor.normalizeName(name);
		if(name == null)
			return null;
		if(fNonOverloadableVariables != null){
			for(int i = 0; i < fNonOverloadableVariables.length; i++){
				if(name.equals(fNonOverloadableVariables[i]))
					return null;
			}
		}
		return name;
	}
	
	protected IBuildEnvironmentVariable[] filterVariables(IBuildEnvironmentVariable variables[]){
		if(variables == null || variables.length == 0)
			return variables;
		
		IBuildEnvironmentVariable filtered[] = new IBuildEnvironmentVariable[variables.length];
		int filteredNum = 0;
		for(int i = 0; i < variables.length; i++){
			if(getValidName(variables[i].getName()) != null)
				filtered[filteredNum++] = variables[i];
		}

		if(filteredNum != filtered.length){
			IBuildEnvironmentVariable vars[] = new IBuildEnvironmentVariable[filteredNum];
			for(int i = 0; i < filteredNum; i++)
				vars[i] = filtered[i];
			filtered = vars;
		}
		return filtered;
	}
}
