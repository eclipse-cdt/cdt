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
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvVarOperationProcessor;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.core.resources.IWorkspace;

/**
 * This supplier suplies the macros that represent the Managed Build environment variables
 * 
 * @since 3.0
 */
public class EnvironmentMacroSupplier implements IBuildMacroSupplier {
	private static EnvironmentMacroSupplier fInstance;
	private EnvironmentVariableProvider fEnvironmentProvider;
	
	public class EnvVarMacro extends BuildMacro{
		private IBuildEnvironmentVariable fVariable;
		private EnvVarMacro(IBuildEnvironmentVariable var){
			fName = var.getName();
			fVariable = var;
		}
		
		private void loadValue(IBuildEnvironmentVariable var){
			String delimiter = var.getDelimiter();
			String value = var.getOperation() != IBuildEnvironmentVariable.ENVVAR_REMOVE ?
					var.getValue() : null;
			
			if(delimiter != null && !"".equals(delimiter)){	//$NON-NLS-1$
				fType = VALUE_TEXT_LIST;
				if(value != null){
					List list = EnvVarOperationProcessor.convertToList(value,delimiter);
					fStringListValue = (String[])list.toArray(new String[list.size()]);
				} else {
					fStringListValue = null;
				}
			} else {
				fType = VALUE_TEXT;
				fStringValue = value;
			}
		}
		
		
		public int getMacroValueType() {
			if(fVariable != null){
				loadValue(fVariable);

				//we do not need it any more, release clean the reference
				fVariable = null;
			}
			return super.getMacroValueType();
		}

		public String getStringValue() throws BuildMacroException {
			if(fVariable != null){
				loadValue(fVariable);

				//we do not need it any more, release clean the reference
				fVariable = null;
			}
			return super.getStringValue();
		}

		public String[] getStringListValue() throws BuildMacroException {
			if(fVariable != null){
				loadValue(fVariable);

				//we do not need it any more, release clean the reference
				fVariable = null;
			}
			return super.getStringListValue();
		}
	}

	protected EnvironmentMacroSupplier(){
		this((EnvironmentVariableProvider)ManagedBuildManager.getEnvironmentVariableProvider());
	}
	
	public EnvironmentMacroSupplier(EnvironmentVariableProvider varProvider){
		fEnvironmentProvider = varProvider;
	}
	
	public IBuildMacro createBuildMacro(IBuildEnvironmentVariable var){
		if(var != null)
			return new EnvVarMacro(var);
		return null; 
	}

	public static EnvironmentMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new EnvironmentMacroSupplier();
		return fInstance;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		if(macroName == null || "".equals(macroName))	//$NON-NLS-1$
		return null;

		IBuildEnvironmentVariable var = null;
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				var = fEnvironmentProvider.getVariable(macroName,fEnvironmentProvider.getContextInfo(contextData),false);
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(contextData instanceof IManagedProject){
				var = fEnvironmentProvider.getVariable(macroName,fEnvironmentProvider.getContextInfo(contextData),false);
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(contextData instanceof IWorkspace){
				var = fEnvironmentProvider.getVariable(macroName,fEnvironmentProvider.getContextInfo(contextData),false);
			}
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			if(contextData == null){
				var = fEnvironmentProvider.getVariable(macroName,fEnvironmentProvider.getContextInfo(contextData),false);
			}
			break;
		}
		if(var != null && var.getOperation() != IBuildEnvironmentVariable.ENVVAR_REMOVE)
			return new EnvVarMacro(var);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		IBuildEnvironmentVariable vars[] = null;

		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				vars = fEnvironmentProvider.getVariables(fEnvironmentProvider.getContextInfo(contextData),false).toArray(false);
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(contextData instanceof IManagedProject){
				vars = fEnvironmentProvider.getVariables(fEnvironmentProvider.getContextInfo(contextData),false).toArray(false);
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(contextData instanceof IWorkspace){
				vars = fEnvironmentProvider.getVariables(fEnvironmentProvider.getContextInfo(contextData),false).toArray(false);
			}
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			if(contextData == null){
				vars = fEnvironmentProvider.getVariables(fEnvironmentProvider.getContextInfo(contextData),false).toArray(false);
			}
			break;
		}
		
		if(vars != null){
			EnvVarMacro macros[] = new EnvVarMacro[vars.length];
			for(int i = 0; i < macros.length; i++)
				macros[i] = new EnvVarMacro(vars[i]);
			
			return macros;
		}
		return null;
	}

	public EnvironmentVariableProvider getEnvironmentVariableProvider(){
		return fEnvironmentProvider;
	}
}
