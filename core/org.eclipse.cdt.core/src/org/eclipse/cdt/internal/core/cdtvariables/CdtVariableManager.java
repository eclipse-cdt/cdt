/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

import java.util.Arrays;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.cdtvariables.EclipseVariablesVariableSupplier.EclipseVarMacro;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableManager;
import org.eclipse.core.variables.IStringVariable;

/**
 * The default IBuildMacroProvider implementation
 * @since 3.0
 */
public class CdtVariableManager implements ICdtVariableManager {
	static private CdtVariableManager fDefault;
	
	public static final UserDefinedVariableSupplier fUserDefinedMacroSupplier = UserDefinedVariableSupplier.getInstance();
	public static final BuildSystemVariableSupplier fBuildSystemVariableSupplier = BuildSystemVariableSupplier.getInstance();
	public static final EnvironmentVariableSupplier fEnvironmentMacroSupplier = EnvironmentVariableSupplier.getInstance();
	public static final CdtMacroSupplier fCdtMacroSupplier = CdtMacroSupplier.getInstance();
	public static final EclipseVariablesVariableSupplier fEclipseVariablesMacroSupplier = EclipseVariablesVariableSupplier.getInstance();
	
	protected CdtVariableManager(){
		
	}
	
	public static CdtVariableManager getDefault(){
		if(fDefault == null)
			fDefault = new CdtVariableManager();
		return fDefault; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getMacro(java.lang.String, int, java.lang.Object, boolean)
	 */
	public ICdtVariable getVariable(String macroName, ICConfigurationDescription cfg) {
		if(cfg instanceof CConfigurationDescriptionCache){
			StorableCdtVariables macros = ((CConfigurationDescriptionCache)cfg).getCachedVariables();
			if(macros != null)
				return macros.getMacro(macroName);
		}
		int type = getContextType(cfg);
		return SupplierBasedCdtVariableManager.getVariable(macroName,
				getMacroContextInfo(type,cfg),true);
	}
	
	private IVariableContextInfo getVariableContextInfo(ICConfigurationDescription cfg){
		int type = getContextType(cfg);
		return getMacroContextInfo(type,cfg);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getMacros(int, java.lang.Object, boolean)
	 */
	public ICdtVariable[] getVariables(ICConfigurationDescription cfg) {
		if(cfg instanceof CConfigurationDescriptionCache){
			StorableCdtVariables macros = ((CConfigurationDescriptionCache)cfg).getCachedVariables();
			if(macros != null)
				return macros.getMacros();
		}
		int type = getContextType(cfg);
		return SupplierBasedCdtVariableManager.getVariables(getMacroContextInfo(type,cfg),
				true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getSuppliers(int, java.lang.Object)
	 */
	public ICdtVariableSupplier[] getSuppliers(int contextType,
			Object contextData) {
		IVariableContextInfo info = getMacroContextInfo(contextType,contextData);
		if(info != null)
			return info.getSuppliers();
		return null;
	}
	
	public IVariableContextInfo getMacroContextInfo(
			int contextType,
			Object contextData){
		DefaultVariableContextInfo info = new DefaultVariableContextInfo(contextType,contextData);
		if(info.getSuppliers() != null)
			return info;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#convertStringListToString(java.lang.String[], java.lang.String)
	 */
	public String convertStringListToString(String[] value, String listDelimiter) {
		return CdtVariableResolver.convertStringListToString(value,listDelimiter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#resolveValue(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object)
	 */
	public String resolveValue(String value, String nonexistentMacrosValue,
			String listDelimiter, ICConfigurationDescription cfg)
			throws CdtVariableException {
		
		IVariableContextInfo info = getMacroContextInfo(getContextType(cfg),cfg);
		if(info != null)
			return CdtVariableResolver.resolveToString(value,
					getMacroSubstitutor(info,nonexistentMacrosValue, listDelimiter));
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#resolveStringListValue(java.lang.String, java.lang.String, int, java.lang.Object)
	 */
	public String[] resolveStringListValue(String value,
			String nonexistentMacrosValue, String listDelimiter,
			ICConfigurationDescription cfg) throws CdtVariableException {
		
		IVariableContextInfo info = getMacroContextInfo(getContextType(cfg),cfg);
		if(info != null)
			return CdtVariableResolver.resolveToStringList(value,getMacroSubstitutor(info,nonexistentMacrosValue, listDelimiter)); 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#isStringListValue(java.lang.String)
	 */
	public boolean isStringListValue(String value, ICConfigurationDescription cfg) throws CdtVariableException {
		try {
			CdtVariableResolver.resolveToStringList(value,getMacroSubstitutor(getMacroContextInfo(getContextType(cfg), cfg)," ",null));	//$NON-NLS-1$
		}catch(CdtVariableException e){
			return false;
		}
		return true;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#checkIntegrity(int, java.lang.Object)
	 */
	public void checkVariableIntegrity(ICConfigurationDescription cfg)
			throws CdtVariableException {

		int type = getContextType(cfg);
		IVariableContextInfo info = getMacroContextInfo(type,cfg);
		IVariableSubstitutor subst = new CoreVariableSubstitutor(info,null,""){ //$NON-NLS-1$
			@Override
			protected ResolvedMacro resolveMacro(ICdtVariable macro) throws CdtVariableException {
				if(macro instanceof EclipseVariablesVariableSupplier.EclipseVarMacro)
					return new ResolvedMacro(macro.getName(),""); //$NON-NLS-1$
				return super.resolveMacro(macro);
			}
		};
		if(info != null)
			CdtVariableResolver.checkIntegrity(info,subst);
	}
	
	private int getContextType(ICConfigurationDescription des){
		if(des != null)
			return ICoreVariableContextInfo.CONTEXT_CONFIGURATION;
		return ICoreVariableContextInfo.CONTEXT_WORKSPACE;
	}

	public IVariableSubstitutor getMacroSubstitutor(IVariableContextInfo info, String inexistentMacroValue, String listDelimiter){
		return new CoreVariableSubstitutor(info, inexistentMacroValue, listDelimiter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#resolveStringListValues(java.lang.String[], java.lang.String, java.lang.String, int, java.lang.Object)
	 */
	public String[] resolveStringListValues(String[] value, String nonexistentMacrosValue, String listDelimiter, ICConfigurationDescription cfg) throws CdtVariableException {
		IVariableContextInfo info = getMacroContextInfo(getContextType(cfg),cfg);
		if(info != null)
			return CdtVariableResolver.resolveStringListValues(value,
					getMacroSubstitutor(info,nonexistentMacrosValue, listDelimiter), true);
		return null;
	}

	public boolean isEnvironmentVariable(ICdtVariable variable,
			ICConfigurationDescription cfg) {
		if(variable instanceof EnvironmentVariableSupplier.EnvVarMacro)
			return true;
		
		IVariableContextInfo info = getVariableContextInfo(cfg);
		ICdtVariable var = fEnvironmentMacroSupplier.getVariable(variable.getName(), info);
		if(var != null && variablesEqual(var, variable))
			return true;
		
		return false;
	}
	
	private static boolean variablesEqual(ICdtVariable var1, ICdtVariable var2){
		if(CDataUtil.objectsEqual(var1, var2))
			return true;
		
		if(var1 == null || var2 == null)
			return false;
		
		if(var1.getValueType() != var2.getValueType())
			return false;
		
		if(!var1.getName().equals(var2.getName()))
			return false;
		
		try {
			if(CdtVariableResolver.isStringListVariable(var1.getValueType())){
				String[] v1 = var1.getStringListValue();
				String[] v2 = var2.getStringListValue();
				if(!Arrays.equals(v1, v2))
					return false;
			} else {
				if(!CDataUtil.objectsEqual(var1.getStringValue(), var2.getStringValue()))
					return false;
			}
		} catch (CdtVariableException e){
			return false;
		}
		
		return true;
	}

	public IStringVariable toEclipseVariable(ICdtVariable variable,
			ICConfigurationDescription cfg) {
		if(variable instanceof EclipseVariablesVariableSupplier.EclipseVarMacro){
			return ((EclipseVarMacro)variable).getVariable();
		}
		return null;
	}

	public boolean isUserVariable(ICdtVariable variable,
			ICConfigurationDescription cfg) {
		if(!(variable instanceof StorableCdtVariable))
			return false;
		
		if(cfg != null)
			return UserDefinedVariableSupplier.getInstance().containsVariable(ICoreVariableContextInfo.CONTEXT_CONFIGURATION, cfg, variable);
		
		return UserDefinedVariableSupplier.getInstance().containsVariable(ICoreVariableContextInfo.CONTEXT_WORKSPACE, null, variable);
	}
}
