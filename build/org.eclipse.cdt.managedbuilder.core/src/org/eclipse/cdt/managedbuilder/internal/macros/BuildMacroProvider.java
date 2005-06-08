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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;

/**
 * The default IBuildMacroProvider implementation
 * @since 3.0
 */
public class BuildMacroProvider implements IBuildMacroProvider {
	static private BuildMacroProvider fDefault;
	
	public static UserDefinedMacroSupplier fUserDefinedMacroSupplier = UserDefinedMacroSupplier.getInstance();
	public static ExternalExtensionMacroSupplier fExternalExtensionMacroSupplier = ExternalExtensionMacroSupplier.getInstance();
	public static EnvironmentMacroSupplier fEnvironmentMacroSupplier = EnvironmentMacroSupplier.getInstance();
	public static MbsMacroSupplier fMbsMacroSupplier = MbsMacroSupplier.getInstance();
	public static CdtPathEntryMacroSupplier fCdtPathEntryMacroSupplier = CdtPathEntryMacroSupplier.getInstance();
	public static EclipseVariablesMacroSupplier fEclipseVariablesMacroSupplier = EclipseVariablesMacroSupplier.getInstance();

	protected BuildMacroProvider(){
		
	}
	
	public static BuildMacroProvider getDefault(){
		if(fDefault == null)
			fDefault = new BuildMacroProvider();
		return fDefault; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getMacro(java.lang.String, int, java.lang.Object, boolean)
	 */
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData, boolean includeParentContexts) {
		return getMacro(macroName,
				getMacroContextInfo(contextType,contextData),includeParentContexts);
	}

	/**
	 * @param macroName
	 * @param contextInfo
	 * @param includeParentContexts
	 * @return
	 */
	static public IBuildMacro getMacro(String macroName, IMacroContextInfo contextInfo, boolean includeParentContexts) {
		if(contextInfo == null || macroName == null)
			return null;
		
		do{
			IBuildMacroSupplier suppliers[] = contextInfo.getSuppliers();
			if(suppliers != null){
				for(int i = 0; i < suppliers.length; i++){
					IBuildMacro macro = suppliers[i].getMacro(macroName,contextInfo.getContextType(),contextInfo.getContextData());
					if(macro != null)
						return macro;
				}
			}
		}while(includeParentContexts && (contextInfo = contextInfo.getNext()) != null);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getMacros(int, java.lang.Object, boolean)
	 */
	public IBuildMacro[] getMacros(int contextType, Object contextData,
			boolean includeParentContexts) {
		return getMacros(getMacroContextInfo(contextType,contextData),
				includeParentContexts);
	}

	/**
	 * @param contextInfo
	 * @param includeParentContexts
	 * @return
	 */
	static public IBuildMacro[] getMacros(IMacroContextInfo contextInfo,
			boolean includeParentContexts) {
		if(contextInfo == null)
			return null;
		
		Map map = new HashMap();
		IMacroContextInfo infos[] = includeParentContexts ? 
				getAllMacroContextInfos(contextInfo) :
					new IMacroContextInfo[]{contextInfo};
		
		for(int k = infos.length - 1; k >= 0; k--){
			contextInfo = infos[k];
			IBuildMacroSupplier suppliers[] = contextInfo.getSuppliers();
			if(suppliers != null){
				for(int i = suppliers.length - 1; i >= 0; i--){
					IBuildMacro macros[] = suppliers[i].getMacros(contextInfo.getContextType(),contextInfo.getContextData());
					if(macros != null){
						for(int j = 0; j < macros.length; j++){
							map.put(macros[j].getName(),macros[j]);
						}
					}
				}
			}
		}
		
		Collection values = map.values();
		return (IBuildMacro[])values.toArray(new IBuildMacro[values.size()]);
	}
	
	/*
	 * returns an array of the IMacroContextInfo that holds the context informations
	 * starting from the one passed to this method and including all subsequent parents
	 */
	private static IMacroContextInfo[] getAllMacroContextInfos(IMacroContextInfo contextInfo){
		if(contextInfo == null)
			return null;
			
		List list = new ArrayList();
	
		list.add(contextInfo);
			
		while((contextInfo = contextInfo.getNext()) != null)
			list.add(contextInfo);
		
		return (IMacroContextInfo[])list.toArray(new IMacroContextInfo[list.size()]);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#getSuppliers(int, java.lang.Object)
	 */
	public IBuildMacroSupplier[] getSuppliers(int contextType,
			Object contextData) {
		IMacroContextInfo info = getMacroContextInfo(contextType,contextData);
		if(info != null)
			return info.getSuppliers();
		return null;
	}
	
	/**
	 * @param contextType
	 * @param contextData
	 * @return
	 */
	public IMacroContextInfo getMacroContextInfo(
			int contextType,
			Object contextData){
		DefaultMacroContextInfo info = new DefaultMacroContextInfo(contextType,contextData);
		if(info.getSuppliers() != null)
			return info;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#convertStringListToString(java.lang.String[], java.lang.String)
	 */
	public String convertStringListToString(String[] value, String listDelimiter) {
		return MacroResolver.convertStringListToString(value,listDelimiter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#resolveValue(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object)
	 */
	public String resolveValue(String value, String nonexistentMacrosValue,
			String listDelimiter, int contextType, Object contextData)
			throws BuildMacroException {
		
		IMacroContextInfo info = getMacroContextInfo(contextType,contextData);
		if(info != null)
			return MacroResolver.resolveToString(value,
					getMacroSubstitutor(info,nonexistentMacrosValue, listDelimiter));
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#resolveStringListValue(java.lang.String, java.lang.String, int, java.lang.Object)
	 */
	public String[] resolveStringListValue(String value,
			String nonexistentMacrosValue, String listDelimiter,
			int contextType, Object contextData) throws BuildMacroException {
		
		IMacroContextInfo info = getMacroContextInfo(contextType,contextData);
		if(info != null)
			return MacroResolver.resolveToStringList(value,getMacroSubstitutor(info,nonexistentMacrosValue, listDelimiter)); 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#resolveValueToMakefileFormat(java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object)
	 */
	public String resolveValueToMakefileFormat(String value,
			String nonexistentMacrosValue, String listDelimiter,
			int contextType, Object contextData) throws BuildMacroException {

		IMacroContextInfo info = getMacroContextInfo(contextType,contextData);
		if(info != null)
			return MacroResolver.resolveToString(value,
					getBuildfileMacroSubstitutor(info,nonexistentMacrosValue, listDelimiter));
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#resolveStringListValueToMakefileFormat(java.lang.String, java.lang.String, int, java.lang.Object)
	 */
	public String[] resolveStringListValueToMakefileFormat(String value,
			String nonexistentMacrosValue, String listDelimiter, int contextType, Object contextData)
			throws BuildMacroException {

		IMacroContextInfo info = getMacroContextInfo(contextType,contextData);
		if(info != null)
			MacroResolver.resolveToStringList(value,getBuildfileMacroSubstitutor(info,nonexistentMacrosValue, listDelimiter));
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#isStringListValue(java.lang.String)
	 */
	public boolean isStringListValue(String value, int contextType, Object contextData) throws BuildMacroException {
		try {
			MacroResolver.resolveToStringList(value,getMacroSubstitutor(getMacroContextInfo(contextType,contextData)," ",null));	//$NON-NLS-1$
		}catch(BuildMacroException e){
			return false;
		}
		return true;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider#checkIntegrity(int, java.lang.Object)
	 */
	public void checkIntegrity(int contextType, Object contextData)
			throws BuildMacroException {

		IMacroContextInfo info = getMacroContextInfo(contextType,contextData);
		IMacroSubstitutor subst = new DefaultMacroSubstitutor(info,null,""){ //$NON-NLS-1$
			protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException {
				if(macro instanceof EclipseVariablesMacroSupplier.EclipseVarMacro)
					return new ResolvedMacro(macro.getName(),""); //$NON-NLS-1$
				return super.resolveMacro(macro);
			}
		};
		if(info != null)
			MacroResolver.checkIntegrity(info,subst);
	}

	public IMacroSubstitutor getMacroSubstitutor(IMacroContextInfo info, String inexistentMacroValue, String listDelimiter){
		return new DefaultMacroSubstitutor(info, inexistentMacroValue, listDelimiter);
	}

	public IMacroSubstitutor getBuildfileMacroSubstitutor(IMacroContextInfo info, String inexistentMacroValue, String listDelimiter){
		return new BuildfileMacroSubstitutor(info, inexistentMacroValue, listDelimiter);
	}

	/*
	 * returns true if the first passed contextInfo is the child of the second one
	 */
	public boolean checkParentContextRelation(IMacroContextInfo child, IMacroContextInfo parent){
		if(child == null || parent == null)
			return false;

		IMacroContextInfo enumInfo = child;
		do{
			if(parent.getContextType() == enumInfo.getContextType() &&
					parent.getContextData() == enumInfo.getContextData())
				return true;
		}while((enumInfo = enumInfo.getNext()) != null);
		return false;
	}
	
	/**
	 * answers whether the environment macros are to be expanded in the buildfile
	 * 
	 * @param cfg
	 * @return
	 */
	public boolean areMacrosExpandedInBuildfile(IConfiguration cfg){
		boolean expanded = fUserDefinedMacroSupplier.areMacrosExpanded(cfg);
		if(expanded || canKeepMacrosInBuildfile(cfg))
			return expanded;
		return true;
	}

	/**
	 * sets whether the environment macros are to be expanded in the buildfile or not
	 * If a bulder does not support treating environment variables as its own ones this method
	 * has no effect
	 * Returns the result of this set operation. That is whether the environment macros are to be expanded in the buildfile
	 * 
	 * @param cfg
	 * @param expand
	 * @return
	 */
	public boolean expandMacrosInBuildfile(IConfiguration cfg, boolean expand){
		if(expand || canKeepMacrosInBuildfile(cfg)){
			fUserDefinedMacroSupplier.setMacrosExpanded(cfg,expand);
			return expand;	
		}
		return true;
	}
	
	/**
	 * answers whether the builder used for the given configuration is capable
	 * of handling macros in the buildfile
	 * 
	 * @param cfg
	 * @return
	 */
	public boolean canKeepMacrosInBuildfile(IConfiguration cfg){
		return MacroResolver.canKeepMacrosInBuildfile(cfg);
	}

	/**
	 * answers whether the given builder is capable
	 * of handling macros in the buildfile
	 * 
	 * @param builder
	 * @return
	 */
	public boolean canKeepMacrosInBuildfile(IBuilder builder){
		return MacroResolver.canKeepMacrosInBuildfile(builder);
	}
}
