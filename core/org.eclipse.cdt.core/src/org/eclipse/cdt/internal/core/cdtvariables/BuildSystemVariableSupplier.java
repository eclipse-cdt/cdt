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
package org.eclipse.cdt.internal.core.cdtvariables;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableManager;

public class BuildSystemVariableSupplier extends CoreMacroSupplierBase {
	private static BuildSystemVariableSupplier fInstance;
	private BuildSystemVariableSupplier(){
	}
	
	public static BuildSystemVariableSupplier getInstance(){
		if(fInstance == null){
			fInstance = new BuildSystemVariableSupplier();
		}
		return fInstance;
	}
	private class ExtensionMacroProvider extends CdtVariableManager{
		private IVariableContextInfo fStartInfo;
		private int fContextType;
		private Object fContextData;
		private boolean fStartInitialized;
		
		public ExtensionMacroProvider(int contextType, Object contextData){
			fContextType = contextType;
			fContextData = contextData;
		}

		@Override
		public IVariableContextInfo getMacroContextInfo(int contextType,Object contextData){
			IVariableContextInfo startInfo = getStartInfo();
			if(contextType == fContextType &&
					contextData == fContextData)
				return startInfo;
			
			IVariableContextInfo info = super.getMacroContextInfo(contextType, contextData);
			if(info == null)
				return null;
			
			if(SupplierBasedCdtVariableManager.checkParentContextRelation(startInfo,info))
				return info;
			return null;
		}
		
		protected IVariableContextInfo getStartInfo(){
			if(fStartInfo == null && !fStartInitialized){
				IVariableContextInfo info = super.getMacroContextInfo(fContextType,fContextData);
				if(info != null){
					ICdtVariableSupplier suppliers[] = info.getSuppliers();
					suppliers = filterValidSuppliers(suppliers);
					if(suppliers != null)
						fStartInfo = new DefaultVariableContextInfo(fContextType,fContextData,suppliers);
					else
						fStartInfo = info.getNext();
					fStartInitialized = true;
				}
				fStartInitialized = true;
			}
			return fStartInfo;
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

	@Override
	protected ICdtVariable getMacro(String name, int type, Object data) {
		ICConfigurationDescription des = (ICConfigurationDescription)data;
		ICdtVariablesContributor cr = des.getBuildVariablesContributor();
		if(cr != null)
			return cr.getVariable(name, new ExtensionMacroProvider(type, data));
		return null;
	}

	@Override
	protected ICdtVariable[] getMacros(int type, Object data) {
		ICConfigurationDescription des = (ICConfigurationDescription)data;
		ICdtVariablesContributor cr = des.getBuildVariablesContributor();
		if(cr != null)
			return cr.getVariables(new ExtensionMacroProvider(type, data));
		return new ICdtVariable[0];
	}
}
