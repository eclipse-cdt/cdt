/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableManager;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;

/**
 * This supplier is used to suply macros provided by the tool-integrator
 *
 * @since 3.0
 */
public class ExternalExtensionMacroSupplier implements ICdtVariableSupplier{
	private static final String fNonOverloadableMacros[] = new String[]{
		//tool-integrators not allowed currently to override the "CWD" and "PWD" macros
		"CWD",   //$NON-NLS-1$
		"PWD"	  //$NON-NLS-1$
	};

	private ICdtVariableManager fMngr;
	private ICConfigurationDescription fCfgDes;

//	private static ExternalExtensionMacroSupplier fInstance;

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
		@Override
		public ICdtVariable getVariable(String macroName,
				int contextType,
				Object contextData,
				boolean includeParent) {
			if(getValidName(macroName) == null)
				return null;
			return fMngr.getVariable(macroName, fCfgDes);
//			return super.getMacro(macroName,contextType,contextData,includeParent);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getMacros(int, java.lang.Object, boolean)
		 */
		@Override
		public ICdtVariable[] getVariables(int contextType,Object contextData, boolean includeParent) {
//			return filterVariables(super.getMacros(contextType, contextData,  includeParent));
			return filterVariables(fMngr.getVariables(fCfgDes));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider#getContextInfo(java.lang.Object)
		 */
		@Override
		public IMacroContextInfo getMacroContextInfo(int contextType,Object contextData){
			IMacroContextInfo startInfo = getStartInfo();
			if(contextType == fContextType &&
					contextData == fContextData)
				return startInfo;

			IMacroContextInfo info = super.getMacroContextInfo(contextType, contextData);
			if(info == null)
				return null;

			if(SupplierBasedCdtVariableManager.checkParentContextRelation(startInfo,info))
				return info;
			return null;
		}

		protected IMacroContextInfo getStartInfo(){
			if(fStartInfo == null && !fStartInitialized){
				IMacroContextInfo info = super.getMacroContextInfo(fContextType,fContextData);
				if(info != null){
					ICdtVariableSupplier suppliers[] = info.getSuppliers();
					suppliers = filterValidSuppliers(suppliers);
					if(suppliers != null)
						fStartInfo = new DefaultMacroContextInfo(fContextType,fContextData,suppliers);
					else
						fStartInfo = (IMacroContextInfo)info.getNext();
					fStartInitialized = true;
				}
				fStartInitialized = true;
			}
			return fStartInfo;
		}
	}


	public ExternalExtensionMacroSupplier(ICdtVariableManager mngr, ICConfigurationDescription cfgDes){
		fMngr = mngr;
		fCfgDes = cfgDes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	@Override
	public ICdtVariable getVariable(String macroName, IVariableContextInfo context) {
		if((macroName = getValidName(macroName)) == null)
			return null;

		IMacroContextInfo info = (IMacroContextInfo)context;
		int contextType = info.getContextType();
		Object contextData = info.getContextData();
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			IConfiguration cfg = null;
			IBuilder builder = null;
			if(contextData instanceof IBuilder){
				builder = (IBuilder)contextData;
				cfg = builder.getParent().getParent();
			} else if(contextData instanceof IConfiguration){
				cfg = (IConfiguration)contextData;
				builder = cfg.getBuilder();
			}
			if(cfg != null){
				IConfigurationBuildMacroSupplier supplier = cfg.getBuildMacroSupplier();
				if(supplier == null)
					return null;
				return supplier.getMacro(macroName,cfg,new ExtensionMacroProvider(contextType, contextData));
			}
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if (contextData instanceof IManagedProject) {
				IManagedProject project = (IManagedProject)contextData;
				IProjectBuildMacroSupplier supplier = project.getProjectType() != null ? project.getProjectType().getBuildMacroSupplier() : null;
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
	@Override
	public ICdtVariable[] getVariables(IVariableContextInfo context) {
		IBuildMacro macros[] = null;
		IMacroContextInfo info = (IMacroContextInfo)context;
		int contextType = info.getContextType();
		Object contextData = info.getContextData();

		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			IConfiguration cfg = null;
			IBuilder builder = null;
			if(contextData instanceof IBuilder){
				builder = (IBuilder)contextData;
				cfg = builder.getParent().getParent();
			}else if(contextData instanceof IConfiguration){
				cfg = (IConfiguration)contextData;
				builder = cfg.getBuilder();
			}
			if(cfg != null){
				IConfigurationBuildMacroSupplier supplier = cfg.getBuildMacroSupplier();
				if(supplier != null)
					macros = supplier.getMacros(cfg,new ExtensionMacroProvider(contextType, contextData));
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if (contextData instanceof IManagedProject) {
				IManagedProject project = (IManagedProject)contextData;
				IProjectBuildMacroSupplier supplier = project.getProjectType() != null ? project.getProjectType().getBuildMacroSupplier() : null;
				if(supplier != null)
					macros = supplier.getMacros(project,new ExtensionMacroProvider(contextType, contextData));
			}
		}
		return filterVariables(macros);
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

	protected ICdtVariable[] filterVariables(ICdtVariable macros[]){
		return filterVariables(macros,fNonOverloadableMacros);
	}

	private ICdtVariable[] filterVariables(ICdtVariable macros[], String remove[]){
		if(macros == null || macros.length == 0)
			return macros;

		ICdtVariable filtered[] = new ICdtVariable[macros.length];
		int filteredNum = 0;
		for(int i = 0; i < macros.length; i++){
			ICdtVariable var = macros[i];
			String name = null;
			if(var != null && (name = var.getName().trim()).length() != 0){
				boolean skip = false;
				if(remove != null && remove.length > 0){
					for(int j = 0; j < remove.length; j++){
						if(remove[j] != null && remove[j].equals(name)){
							skip = true;
							break;
						}
					}
				}
				if(!skip)
					filtered[filteredNum++] = var;
			}
		}

		if(filteredNum != filtered.length){
			ICdtVariable m[] = new ICdtVariable[filteredNum];
			for(int i = 0; i < filteredNum; i++)
				m[i] = filtered[i];
			filtered = m;
		}
		return filtered;

	}

	protected ICdtVariableSupplier[] filterValidSuppliers(ICdtVariableSupplier suppliers[]){
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

		ICdtVariableSupplier validSuppliers[] =
			new ICdtVariableSupplier[suppliers.length - startNum];

		for(i = startNum, j = 0; i < suppliers.length; i++, j++)
			validSuppliers[j] = suppliers[i];

		return validSuppliers;
	}

}
