/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.cdtvariables.DefaultVariableContextInfo;
import org.eclipse.cdt.internal.core.cdtvariables.EnvironmentVariableSupplier;
import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableManager;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined by the tool-integrator
 * 
 * @since 3.0
 */
public class BuildSystemEnvironmentSupplier implements
		ICoreEnvironmentVariableSupplier {

	/**
	 * EnvironmentVariableProvider passed to the tool-integrator provided
	 * suppliers.
	 * Accepts only contexts lower than the one passed to a suppler  
	 * 
	 * @since 3.0
	 */
	private class ExtensionEnvVarProvider extends EnvironmentVariableManager{
		private IEnvironmentContextInfo fStartInfo;
		private Object fStartLevel;
		private boolean fStartInitialized;
		private int fStartType;
		private Object fStartData;
		private IVariableContextInfo fStartMacroContextInfo;
		private boolean fStartMacroInfoInitialized;
		
		public ExtensionEnvVarProvider(Object level){
			fStartLevel = level;
			fStartType = getMacroContextTypeFromContext(level);
			fStartData = level;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariable(java.lang.String, java.lang.Object, boolean)
		 */
		@Override
		public IEnvironmentVariable getVariable(String variableName,
				ICConfigurationDescription cfg, boolean resolveMacros) {
			if((variableName = getValidName(variableName)) == null)
				return null;
			return super.getVariable(variableName,cfg,resolveMacros);
		}

		@Override
		public IEnvironmentVariable[] getVariables(ICConfigurationDescription cfg, boolean resolveMacros) {
			return filterVariables(super.getVariables(cfg,resolveMacros));
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getContextInfo(java.lang.Object)
		 */
		@Override
		public IEnvironmentContextInfo getContextInfo(Object level){
			IEnvironmentContextInfo startInfo = getStartInfo();
			if(level == fStartLevel)
				return startInfo;
			
			IEnvironmentContextInfo info = super.getContextInfo(level);
			if(info == null)
				return null;
			
			if(checkParentContextRelation(startInfo,info))
				return info;
			return null;
		}
		
		protected IEnvironmentContextInfo getStartInfo(){
			if(fStartInfo == null && !fStartInitialized){
				IEnvironmentContextInfo info = super.getContextInfo(fStartLevel);
				if(info != null){
					ICoreEnvironmentVariableSupplier suppliers[] = info.getSuppliers();
					suppliers = filterValidSuppliers(suppliers);
					if(suppliers != null)
						fStartInfo = new DefaultEnvironmentContextInfo(fStartLevel,suppliers);
					else
						fStartInfo = info.getNext();
					fStartInitialized = true;
				}
				fStartInitialized = true;
			}
			return fStartInfo;
		}
		
		@Override
		public IVariableSubstitutor getVariableSubstitutor(IVariableContextInfo info, String inexistentMacroValue, String listDelimiter){
			return super.getVariableSubstitutor(getSubstitutorMacroContextInfo(info),inexistentMacroValue,listDelimiter);
		}
		
		protected IVariableContextInfo getSubstitutorMacroContextInfo(IVariableContextInfo info){
			IVariableContextInfo startInfo = getStartMacroContextInfo();
			if(info == null)
				return null;

			if(info instanceof ICoreVariableContextInfo){
				ICoreVariableContextInfo coreInfo = (ICoreVariableContextInfo)info;
				if(coreInfo.getContextType() == fStartType &&
						coreInfo.getContextData() == fStartData)
					return startInfo;
			}
			
			if(SupplierBasedCdtVariableManager.checkParentContextRelation(startInfo,info))
				return info;
			return null;
		}
		
		protected IVariableContextInfo getStartMacroContextInfo(){
			if(fStartMacroContextInfo == null && !fStartMacroInfoInitialized){
				final IVariableContextInfo info = getMacroContextInfoForContext(fStartLevel);
				if(info != null){
					fStartMacroContextInfo = new DefaultVariableContextInfo(fStartType,fStartData){
						@Override
						protected ICdtVariableSupplier[] getSuppliers(int type, Object data){
							ICdtVariableSupplier suppliers[] = info.getSuppliers();
							return filterValidMacroSuppliers(suppliers);
						}
						
						@Override
						public IVariableContextInfo getNext() {
							return info.getNext();
						}
					};
					fStartInitialized = true;
				}
				fStartInitialized = true;
			}
			return fStartMacroContextInfo;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	public IEnvironmentVariable getVariable(String name, Object context) {
		if(context == null)
			return null;
		if((name = getValidName(name)) == null)
			return null;
			
		if(context instanceof ICConfigurationDescription){
			ICConfigurationDescription cfg = (ICConfigurationDescription)context;
			if (cfg.getBuildSetting() == null)
				return null;
			IEnvironmentContributor supplier = cfg.getBuildSetting().getBuildEnvironmentContributor();
			if(supplier == null)
				return null;
			return supplier.getVariable(name, new ExtensionEnvVarProvider(context));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	public IEnvironmentVariable[] getVariables(Object context) {
		if(context == null)
			return new IEnvironmentVariable[0];
		IEnvironmentVariable variables[] = null;
		if(context instanceof ICConfigurationDescription){
			ICConfigurationDescription cfg = (ICConfigurationDescription)context;
			if (cfg.getBuildSetting() == null)
				return new IEnvironmentVariable[0];
			IEnvironmentContributor supplier = cfg.getBuildSetting().getBuildEnvironmentContributor();
			if(supplier == null)
				return new IEnvironmentVariable[0];
			variables = supplier.getVariables(new ExtensionEnvVarProvider(context));
		}
		
		return filterVariables(variables);
	}

	protected ICoreEnvironmentVariableSupplier[] filterValidSuppliers(ICoreEnvironmentVariableSupplier suppliers[]){
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


		ICoreEnvironmentVariableSupplier validSuppliers[] = 
			new ICoreEnvironmentVariableSupplier[suppliers.length - startNum];
		
		for(i = startNum, j = 0; i < suppliers.length; i++, j++)
			validSuppliers[j] = suppliers[i];
		
		return validSuppliers;
	}
	
	protected String getValidName(String name){
		name = EnvVarOperationProcessor.normalizeName(name);
		if(name == null)
			return null;
		return name;
	}
	
	protected IEnvironmentVariable[] filterVariables(IEnvironmentVariable variables[]){
		return EnvVarOperationProcessor.filterVariables(variables,null);
	}
	
	protected ICdtVariableSupplier[] filterValidMacroSuppliers(ICdtVariableSupplier suppliers[]){
		if(suppliers == null)
			return null;

		int i = 0, j = 0;
		for(i = 0; i < suppliers.length; i++){
			if(suppliers[i] instanceof EnvironmentVariableSupplier)
				break;
		}
		
	
		if(i >= suppliers.length)
			return suppliers;
		
		int startNum = i + 1;

		ICdtVariableSupplier validSuppliers[] = 
			new ICdtVariableSupplier[suppliers.length - startNum];
		
		for(i = startNum, j = 0; i < suppliers.length; i++, j++)
			validSuppliers[j] = suppliers[i];
		
		return validSuppliers;
	}

	public boolean appendEnvironment(Object context) {
		// TODO 
		return true;
	}

}
