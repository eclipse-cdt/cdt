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
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.EnvironmentMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;

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
		private int fStartType;
		private Object fStartData;
		private IMacroContextInfo fStartMacroContextInfo;
		private boolean fStartMacroInfoInitialized;
		
		public ExtensionEnvVarProvider(Object level){
			fStartLevel = level;
			fStartType = getMacroContextTypeFromContext(level);
			fStartData = level;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariable(java.lang.String, java.lang.Object, boolean)
		 */
		public IBuildEnvironmentVariable getVariable(String variableName,
				Object level, boolean includeParentLevels, boolean resolveMacros) {
			if(getValidName(variableName) == null)
				return null;
			return super.getVariable(variableName,level,includeParentLevels,resolveMacros);
		}

		public IBuildEnvironmentVariable[] getVariables(Object level, boolean includeParentLevels, boolean resolveMacros) {
			return filterVariables(super.getVariables(level,includeParentLevels,resolveMacros));
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getContextInfo(java.lang.Object)
		 */
		public IContextInfo getContextInfo(Object level){
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
		
		public IMacroSubstitutor getMacroSubstitutor(IMacroContextInfo info, String inexistentMacroValue, String listDelimiter){
			return super.getMacroSubstitutor(getSubstitutorMacroContextInfo(info),inexistentMacroValue,listDelimiter);
		}
		
		protected IMacroContextInfo getSubstitutorMacroContextInfo(IMacroContextInfo info){
			IMacroContextInfo startInfo = getStartMacroContextInfo();
			if(info == null)
				return null;

			if(info.getContextType() == fStartType &&
					info.getContextData() == fStartData)
				return startInfo;
			
			
			if(BuildMacroProvider.getDefault().checkParentContextRelation(startInfo,info))
				return info;
			return null;
		}
		
		protected IMacroContextInfo getStartMacroContextInfo(){
			if(fStartMacroContextInfo == null && !fStartMacroInfoInitialized){
				final IMacroContextInfo info = getMacroContextInfoForContext(fStartLevel);
				if(info != null){
					fStartMacroContextInfo = new DefaultMacroContextInfo(fStartType,fStartData){
						protected IBuildMacroSupplier[] getSuppliers(int type, Object data){
							IBuildMacroSupplier suppliers[] = info.getSuppliers();
							return filterValidMacroSuppliers(suppliers);
						}
						
						public IMacroContextInfo getNext() {
							return info.getNext();
						}
					};
					fStartInitialized = true;
				}
				fStartInitialized = true;
			}
			return fStartMacroContextInfo;
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
		return EnvVarOperationProcessor.filterVariables(variables,fNonOverloadableVariables);
	}
	
	protected IBuildMacroSupplier[] filterValidMacroSuppliers(IBuildMacroSupplier suppliers[]){
		if(suppliers == null)
			return null;

		int i = 0, j = 0;
		for(i = 0; i < suppliers.length; i++){
			if(suppliers[i] instanceof EnvironmentMacroSupplier)
				break;
		}
		
	
		if(i >= suppliers.length)
			return suppliers;
		
		int startNum = i + 1;

		IBuildMacroSupplier validSuppliers[] = 
			new IBuildMacroSupplier[suppliers.length - startNum];
		
		for(i = startNum, j = 0; i < suppliers.length; i++, j++)
			validSuppliers[j] = suppliers[i];
		
		return validSuppliers;
	}

}
