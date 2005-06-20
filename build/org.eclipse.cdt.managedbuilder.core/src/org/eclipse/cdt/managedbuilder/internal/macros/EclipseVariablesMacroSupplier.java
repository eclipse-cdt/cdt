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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * This supplier suplies the macros that represent the Eclipse variables
 * 
 * @since 3.0
 */
public class EclipseVariablesMacroSupplier implements IBuildMacroSupplier {
	private static final String VAR_PREFIX = "${";  //$NON-NLS-1$
	private static final char VAR_SUFFIX = '}';  //$NON-NLS-1$
	private static final char COLON = ':';  //$NON-NLS-1$
	
	private static EclipseVariablesMacroSupplier fInstance;
	
	private EclipseVariablesMacroSupplier(){
		
	}

	public static EclipseVariablesMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new EclipseVariablesMacroSupplier();
		return fInstance;
	}
	
	public class EclipseVarMacro extends BuildMacro {
		private IStringVariable fVariable;
		private String fArgument;
		private boolean fInitialized;
		
		private EclipseVarMacro(IStringVariable var){
			this(var,null);
		}

		private EclipseVarMacro(IStringVariable var, String argument){
			fVariable = var;
			fType = VALUE_TEXT;
			fName = var.getName();
			if(argument != null)
				fName += COLON + argument;
			fArgument = argument;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringValue()
		 */
		public String getStringValue() throws BuildMacroException {
			if(!fInitialized){
				loadValue(fVariable);
				fInitialized = true;
			}
			return fStringValue;
		}
		
		private void loadValue(IStringVariable var){
			if(var instanceof IDynamicVariable){
				IDynamicVariable dynamicVar = (IDynamicVariable)var;
				if(fArgument == null || dynamicVar.supportsArgument()){
					try{
						fStringValue = dynamicVar.getValue(fArgument);
					}catch(CoreException e){
						fStringValue = null;
					}
				}else
					fStringValue = null;
					
			}else if(var instanceof IValueVariable){
				if(fArgument == null)
					fStringValue = ((IValueVariable)var).getValue();
				else
					fStringValue = null;
			}
			
		}
		
		public IStringVariable getVariable(){
			return fVariable;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		if(contextType != IBuildMacroProvider.CONTEXT_WORKSPACE)
			return null;
		if(macroName == null || "".equals(macroName))	//$NON-NLS-1$
			return null;
		
		String varName = null;
		String param = null;
		IStringVariable var = null;
		int index = macroName.indexOf(COLON);
		if(index == -1)
			varName = macroName;
		else if(index > 0){
			varName = macroName.substring(0,index);
			param = macroName.substring(index+1);
		}
		
		if(varName != null){
			IStringVariableManager mngr = VariablesPlugin.getDefault().getStringVariableManager();
			var = mngr.getValueVariable(varName);
			if(var == null)
				var = mngr.getDynamicVariable(varName);
		}
		
		if(var != null)
			return new EclipseVarMacro(var,param);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		if(contextType != IBuildMacroProvider.CONTEXT_WORKSPACE)
			return null;

		IStringVariableManager mngr = VariablesPlugin.getDefault().getStringVariableManager();
		IDynamicVariable vars[] = mngr.getDynamicVariables();
		Map map = new HashMap();
		for(int i = 0; i < vars.length; i++)
			map.put(vars[i].getName(),vars[i]);

		IValueVariable valVars[] = mngr.getValueVariables();
		for(int i = 0; i < valVars.length; i++)
			map.put(valVars[i].getName(),valVars[i]);

		Collection collection = map.values();
		EclipseVarMacro macros[] = new EclipseVarMacro[collection.size()];
		Iterator iter = collection.iterator();
		for(int i = 0; i < macros.length ; i++)
			macros[i] = new EclipseVarMacro((IStringVariable)iter.next());
		
		return macros;
	}
	
	private String getMacroValue(String name){
		IStringVariableManager mngr = VariablesPlugin.getDefault().getStringVariableManager();
		try{
			return mngr.performStringSubstitution(VAR_PREFIX + name + VAR_SUFFIX);
		}catch (CoreException e){
		}

		return null;
	}
}
