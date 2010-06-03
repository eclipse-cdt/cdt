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
package org.eclipse.cdt.internal.core.cdtvariables;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.cdtvariables.CdtVariable;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.core.resources.IWorkspace;

/**
 * This supplier suplies the macros that represent the Managed Build environment variables
 * 
 * @since 3.0
 */
public class EnvironmentVariableSupplier extends CoreMacroSupplierBase {
	private static EnvironmentVariableSupplier fInstance;
	private EnvironmentVariableManager fEnvironmentProvider;
	
	public class EnvVarMacro extends CdtVariable{
		private IEnvironmentVariable fVariable;
		private EnvVarMacro(IEnvironmentVariable var){
			fName = var.getName();
			fVariable = var;
		}
		
		private void loadValue(IEnvironmentVariable var){
			String delimiter = var.getDelimiter();
			String value = var.getOperation() != IEnvironmentVariable.ENVVAR_REMOVE ?
					var.getValue() : null;
			
			if(isTextList(value,delimiter)){
				fType = VALUE_TEXT_LIST;
				if(value != null){
					List<String> list = EnvVarOperationProcessor.convertToList(value,delimiter);
					fStringListValue = list.toArray(new String[list.size()]);
				} else {
					fStringListValue = null;
				}
			} else {
				fType = VALUE_TEXT;
				fStringValue = value;
			}
		}
		
		
		@Override
		public int getValueType() {
			if(fVariable != null){
				loadValue(fVariable);

				//we do not need it any more, release clean the reference
				fVariable = null;
			}
			return super.getValueType();
		}

		@Override
		public String getStringValue() throws CdtVariableException {
			if(fVariable != null){
				loadValue(fVariable);

				//we do not need it any more, release clean the reference
				fVariable = null;
			}
			return super.getStringValue();
		}

		@Override
		public String[] getStringListValue() throws CdtVariableException {
			if(fVariable != null){
				loadValue(fVariable);

				//we do not need it any more, release clean the reference
				fVariable = null;
			}
			return super.getStringListValue();
		}
	}

	protected EnvironmentVariableSupplier(){
		this(EnvironmentVariableManager.getDefault());
	}
	
	public EnvironmentVariableSupplier(EnvironmentVariableManager varProvider){
		fEnvironmentProvider = varProvider;
	}
	
	private static boolean isTextList(String str, String delimiter) {
		if (delimiter == null || "".equals(delimiter)) //$NON-NLS-1$
			return false;
		
		// Regex: ([^:]+:)+[^:]*
		String patternStr = "([^" + delimiter + "]+" + delimiter + ")+[^" + delimiter + "]*"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
		return Pattern.matches(patternStr, str);
	}
	
	public ICdtVariable createBuildMacro(IEnvironmentVariable var){
		if(var != null)
			return new EnvVarMacro(var);
		return null; 
	}

	public static EnvironmentVariableSupplier getInstance(){
		if(fInstance == null)
			fInstance = new EnvironmentVariableSupplier();
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	@Override
	public ICdtVariable getMacro(String macroName, int contextType,
			Object contextData) {
		if(macroName == null || "".equals(macroName))	//$NON-NLS-1$
		return null;

		IEnvironmentVariable var = null;
		switch(contextType){
		case ICoreVariableContextInfo.CONTEXT_CONFIGURATION:
			if(contextData instanceof ICConfigurationDescription){
				var = fEnvironmentProvider.getVariable(macroName, (ICConfigurationDescription)contextData, false);
			}
			break;
		case ICoreVariableContextInfo.CONTEXT_WORKSPACE:
			if(contextData == null || contextData instanceof IWorkspace){
				var = fEnvironmentProvider.getVariable(macroName, (ICConfigurationDescription)null, false);
			}
			break;
//		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
//			if(contextData == null){
//				var = fEnvironmentProvider.getVariable(macroName,fEnvironmentProvider.getContextInfo(contextData),false);
//			}
//			break;
		}
		if(var != null && var.getOperation() != IEnvironmentVariable.ENVVAR_REMOVE)
			return new EnvVarMacro(var);

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	@Override
	public ICdtVariable[] getMacros(int contextType, Object contextData) {
		IEnvironmentVariable vars[] = null;

		switch(contextType){
		case ICoreVariableContextInfo.CONTEXT_CONFIGURATION:
			if(contextData instanceof ICConfigurationDescription){
				vars = fEnvironmentProvider.getVariables((ICConfigurationDescription)contextData, false);
			}
			break;
		case ICoreVariableContextInfo.CONTEXT_WORKSPACE:
			if(contextData == null || contextData instanceof IWorkspace){
				vars = fEnvironmentProvider.getVariables((ICConfigurationDescription)null, false);
			}
			break;
//		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
//			if(contextData == null){
//				vars = fEnvironmentProvider.getVariables(fEnvironmentProvider.getContextInfo(contextData),false).toArray(false);
//			}
//			break;
		}
		
		if(vars != null){
			EnvVarMacro macros[] = new EnvVarMacro[vars.length];
			for(int i = 0; i < macros.length; i++)
				macros[i] = new EnvVarMacro(vars[i]);
			
			return macros;
		}

		return null;
	}
/*
	public EnvironmentVariableProvider getEnvironmentVariableProvider(){
		return fEnvironmentProvider;
	}
*/
}
