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

import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;

/*
 * this class represents the environment variable-relaed information.
 * That is the context for which the variable is defined and the supplier
 * that supplies the variable
 * 
 */
public class EnvVarDescriptor implements IBuildEnvironmentVariable{
	private IBuildEnvironmentVariable fVariable;
	private IContextInfo fContextInfo;
	private int fSupplierNum;
	
	public EnvVarDescriptor(IBuildEnvironmentVariable variable, IContextInfo contextInfo, int supplierNum){
		fVariable = variable;
		fContextInfo = contextInfo;
		fSupplierNum = supplierNum;
	}

	public IContextInfo getContextInfo() {
		return fContextInfo;
	}

	public int getSupplierNum() {
		return fSupplierNum;
	}

	public IBuildEnvironmentVariable getOriginalVariable() {
		return fVariable;
	}
	
	public String getName() {
		return fVariable.getName();
	}

	public String getValue() {
		return fVariable.getValue();
	}
	
	public int getOperation() {
		return fVariable.getOperation();
	}

	public String getDelimiter() {
		return fVariable.getDelimiter();
	}

	public void setContextInfo(IContextInfo contextInfo) {
		fContextInfo = contextInfo;
	}

	public void setSupplierNum(int supplierNum) {
		fSupplierNum = supplierNum;
	}

	public void setVariable(IBuildEnvironmentVariable variable) {
		fVariable = variable;
	}
	
/*	public String getResolvedValue(int contextType, Object contextData){
		String value = null;
		if(getOperation() != IBuildEnvironmentVariable.ENVVAR_REMOVE){
			String name = getName();
			value = getValue();
			if(value != null && value.length() > 0){
				int supplierNum = -1;
				IMacroContextInfo macroInfo = getMacroContextInfo(fContextInfo);
				IBuildMacroSupplier macroSuppliers[] = macroInfo.getSuppliers();
				for(int i = 0; i < macroSuppliers.length; i++){
					if(macroSuppliers[i] instanceof EnvironmentMacroSupplier){
						supplierNum = i;
						break;
					}
				}

				DefaultMacroSubstitutor sub = new DefaultMacroSubstitutor(new DefaultMacroContextInfo(contextType,contextData),""," ");//,delimiters,""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				try{
					value = sub.resolveToString(new BuildMacro(name,IBuildMacro.VALUE_TEXT,value),macroInfo,supplierNum);
				} catch (BuildMacroException e){
				}
			}
		}
		return value;
	}

	protected IMacroContextInfo getMacroContextInfo(IContextInfo info){
		Object context = info.getContext();
		if(context instanceof IConfiguration)
			return new DefaultMacroContextInfo(IBuildMacroProvider.CONTEXT_CONFIGURATION,context);
		else if(context instanceof IManagedProject)
			return new DefaultMacroContextInfo(IBuildMacroProvider.CONTEXT_PROJECT,context);
		else if(context instanceof IWorkspace)
			return new DefaultMacroContextInfo(IBuildMacroProvider.CONTEXT_WORKSPACE,context);
		else if(context == null)
			return new DefaultMacroContextInfo(IBuildMacroProvider.CONTEXT_ECLIPSEENV,context);
		return null;
	}
*/
}
