/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
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
import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.MbsMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

public class BuildVariablesContributor implements ICdtVariablesContributor {
	private BuildConfigurationData fCfgData;
	
	private class ContributorMacroContextInfo extends DefaultMacroContextInfo {
		ICdtVariableManager fMngr;
		private ICConfigurationDescription fCfgDes;

		public ContributorMacroContextInfo(ICdtVariableManager mngr, ICConfigurationDescription cfgDes,
				int type, Object data) {
			super(type, data);
			fMngr = mngr;
			fCfgDes = cfgDes;
		}

		
		protected ICdtVariableSupplier[] getSuppliers(int type, Object data) {
			switch(type){
			case IBuildMacroProvider.CONTEXT_CONFIGURATION:
				return new ICdtVariableSupplier[]{
					new ExternalExtensionMacroSupplier(fMngr, fCfgDes),
					MbsMacroSupplier.getInstance()
				};
			case IBuildMacroProvider.CONTEXT_PROJECT:
				return new ICdtVariableSupplier[]{
						new ExternalExtensionMacroSupplier(fMngr, fCfgDes),
						MbsMacroSupplier.getInstance()
					};
			case IBuildMacroProvider.CONTEXT_WORKSPACE:
				return new ICdtVariableSupplier[]{
						MbsMacroSupplier.getInstance()
					};
			}
			return null;
		}


		public IVariableContextInfo getNext() {
			switch(getContextType()){
			case IBuildMacroProvider.CONTEXT_CONFIGURATION:{
					Object data = getContextData();
					IConfiguration configuration = null;
					if(data instanceof IBuilder)
						configuration = ((IBuilder)data).getParent().getParent();
					else if(data instanceof IConfiguration)
						configuration  = (IConfiguration)data;
					
					if(configuration != null){
						IManagedProject managedProject = configuration.getManagedProject();
							if(managedProject != null)
								return new ContributorMacroContextInfo(
										fMngr,
										fCfgDes,
										IBuildMacroProvider.CONTEXT_PROJECT,
										managedProject);
					}
				}
				break;
			case IBuildMacroProvider.CONTEXT_PROJECT:{
					Object data = getContextData();
					if(data instanceof IManagedProject){
						IWorkspace wsp = ResourcesPlugin.getWorkspace();
						if(wsp != null)
							return new ContributorMacroContextInfo(
									fMngr,
									fCfgDes,
									IBuildMacroProvider.CONTEXT_WORKSPACE,
									wsp);
					}
				}
				break;
			case IBuildMacroProvider.CONTEXT_WORKSPACE:
				if(getContextData() instanceof IWorkspace){
					return new ContributorMacroContextInfo(
							fMngr,
							fCfgDes,
							IBuildMacroProvider.CONTEXT_INSTALLATIONS,
							null);
				}
				break;
			}
			return null;
		}
	}

	BuildVariablesContributor(BuildConfigurationData data){
		fCfgData = data;
	}

	public ICdtVariable getVariable(String name, ICdtVariableManager provider) {
		ContributorMacroContextInfo info = createContextInfo(provider);
		if(info != null)
			return SupplierBasedCdtVariableManager.getVariable(name, info, true);
		return null;
	}
	
	private ContributorMacroContextInfo createContextInfo(ICdtVariableManager mngr){
		IConfiguration cfg = fCfgData.getConfiguration();
		if(((Configuration)cfg).isPreference())
			return null;
		ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(cfg);
		if(cfgDes != null){
			return new ContributorMacroContextInfo(mngr,
					cfgDes, 
					BuildMacroProvider.CONTEXT_CONFIGURATION,
					cfg);
		}
		return null;
	}
	
	public ICdtVariable[] getVariables(ICdtVariableManager provider) {
		ContributorMacroContextInfo info = createContextInfo(provider);
		if(info != null)
			return SupplierBasedCdtVariableManager.getVariables(info, true);
		return null;
	}
	
}
