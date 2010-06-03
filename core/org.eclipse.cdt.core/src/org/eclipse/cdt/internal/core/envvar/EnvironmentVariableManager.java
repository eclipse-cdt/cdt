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
package org.eclipse.cdt.internal.core.envvar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.cdtvariables.DefaultVariableContextInfo;
import org.eclipse.cdt.internal.core.cdtvariables.EnvironmentVariableSupplier;
import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;


/**
 * This class implements the IEnvironmentVariableProvider interface and provides all
 * build environment funvtionality to the MBS
 * 
 * @since 3.0
 *
 */
public class EnvironmentVariableManager implements
		IEnvironmentVariableManager {

	private static final String DELIMITER_WIN32 = ";";  //$NON-NLS-1$
	private static final String DELIMITER_UNIX = ":";  //$NON-NLS-1$
	
	private static EnvironmentVariableManager fInstance = null;

//	private EnvVarVariableSubstitutor fVariableSubstitutor;
	
	public static final UserDefinedEnvironmentSupplier fUserSupplier = new UserDefinedEnvironmentSupplier();
	public static final BuildSystemEnvironmentSupplier fExternalSupplier = new BuildSystemEnvironmentSupplier();
	public static final EclipseEnvironmentSupplier fEclipseSupplier = new EclipseEnvironmentSupplier();
	
	private ContributedEnvironment fContributedEnvironment;

	public class EnvVarVariableSubstitutor extends SupplierBasedCdtVariableSubstitutor {
		private String fDefaultDelimiter;
/*		public EnvVarMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
			super(contextType,contextData,inexistentMacroValue,listDelimiter);
			fDefaultDelimiter = listDelimiter;
		}
*/
		public EnvVarVariableSubstitutor(IVariableContextInfo contextInfo, String inexistentMacroValue, String listDelimiter){
			super(contextInfo, inexistentMacroValue, listDelimiter, null ,inexistentMacroValue);
			fDefaultDelimiter = listDelimiter;
		}
		
		public IEnvironmentVariable resolveVariable(EnvVarDescriptor var) throws CdtVariableException {
			String value;
			if(var == null || (value = var.getValue()) == null || value.length() == 0 || var.getOperation() == IEnvironmentVariable.ENVVAR_REMOVE)
				return var;

			String listDelimiter = var.getDelimiter();
			if(listDelimiter == null)
				listDelimiter = fDefaultDelimiter;
			setListDelimiter(listDelimiter);
			ICdtVariable macro = EnvironmentVariableSupplier.getInstance().createBuildMacro(var);
			IVariableContextInfo varMacroInfo = getVarMacroContextInfo(var);
			int varSupplierNum = getVarMacroSupplierNum(var,varMacroInfo);
			value = resolveToString(new MacroDescriptor(macro,varMacroInfo,varSupplierNum));
			removeResolvedMacro(var.getName());
			return new EnvironmentVariable(var.getName(),value,var.getOperation(),var.getDelimiter());
		}
		
		protected IVariableContextInfo getVarMacroContextInfo(EnvVarDescriptor var){
			IEnvironmentContextInfo info = var.getContextInfo();
			if(info != null)
				return getMacroContextInfoForContext(info.getContext());
			return null;
		}
		
		protected int getVarMacroSupplierNum(EnvVarDescriptor var, IVariableContextInfo varMacroInfo){
			int varSupplierNum = -1;
			ICdtVariableSupplier macroSuppliers[] = varMacroInfo.getSuppliers();
			for(int i = 0; i < macroSuppliers.length; i++){
				if(macroSuppliers[i] instanceof EnvironmentVariableSupplier){
					varSupplierNum = i;
					break;
				}
			}
			return varSupplierNum;
		}
	}

	protected EnvironmentVariableManager(){
		fContributedEnvironment = new ContributedEnvironment(this);
	}

	public static EnvironmentVariableManager getDefault(){
		if(fInstance == null)
			fInstance = new EnvironmentVariableManager();
		return fInstance;
	}
	
	/*
	 * returns a variable of a given name or null
	 * the context information is taken from the contextInfo passed
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo
	 */
	public static EnvVarDescriptor getVariable(String variableName,
			IEnvironmentContextInfo contextInfo, boolean includeParentLevels){

		if(contextInfo == null)
			return null;
		if((variableName = EnvVarOperationProcessor.normalizeName(variableName)) == null) 
			return null;


		IEnvironmentContextInfo infos[] = getAllContextInfos(contextInfo);
		
		if(!includeParentLevels){
			ICoreEnvironmentVariableSupplier suppliers[] = infos[0].getSuppliers();
			boolean bVarFound = false;
			for (ICoreEnvironmentVariableSupplier supplier : suppliers) {
				if(supplier.getVariable(variableName,infos[0].getContext()) != null){
					bVarFound = true;
					break;
				}
			}
			if(!bVarFound)
				return null;
		}

		IEnvironmentVariable variable = null;
		IEnvironmentContextInfo varContextInfo = null;
		int varSupplierNum = -1;
		ICoreEnvironmentVariableSupplier varSupplier = null;
		
		for(int i = infos.length-1 ; i >=0 ; i-- ) {
			IEnvironmentContextInfo info = infos[i];
			ICoreEnvironmentVariableSupplier suppliers[] = info.getSuppliers();
			
			for(int j = suppliers.length-1 ; j >= 0 ; j-- ) {
				ICoreEnvironmentVariableSupplier supplier = suppliers[j];
				IEnvironmentVariable var = supplier.getVariable(variableName,info.getContext());
				
				if(var == null)
					continue;
				
				varContextInfo = info;
				varSupplierNum = j;
				varSupplier = supplier;
					
				if(variable == null)
					variable = var;
				else
					variable = EnvVarOperationProcessor.performOperation(variable,var);
			}
		}
	
		if(variable != null){
//			if(variable.getOperation() == IEnvironmentVariable.ENVVAR_REMOVE)
//				return null;
			return new EnvVarDescriptor(variable,varContextInfo,varSupplierNum,varSupplier);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariable()
	 */
	public IEnvironmentVariable getVariable(String variableName,
			ICConfigurationDescription cfg, boolean resolveMacros) {
		if(variableName == null || "".equals(variableName)) //$NON-NLS-1$
			return null;
		
		IEnvironmentContextInfo info = getContextInfo(cfg);
		EnvVarDescriptor var = getVariable(variableName,info,true);
		
		if(var != null && var.getOperation() != IEnvironmentVariable.ENVVAR_REMOVE){
			return resolveMacros ? calculateResolvedVariable(var,info) : var;
		}
		return null;
	}
	
	IEnvironmentContextInfo getDefaultContextInfo(Object level){
		DefaultEnvironmentContextInfo info = new DefaultEnvironmentContextInfo(level);
		if(info.getSuppliers() == null)
			return null;
		return info;
	}
	
	/*
	 * returns the context info that should be used for the given level
	 * or null if the the given level is not supported
	 */
	public IEnvironmentContextInfo getContextInfo(Object level){
		if(level instanceof ICConfigurationDescription)
			return fContributedEnvironment.appendEnvironment((ICConfigurationDescription)level) ?
					getDefaultContextInfo(level) : fContributedEnvironment.getContextInfo(level);
		return getDefaultContextInfo(level);
	}
	
	/*
	 * returns a list of defined variables.
	 * the context information is taken from the contextInfo passed
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo
	 */
	public static EnvVarCollector getVariables(IEnvironmentContextInfo contextInfo,
			boolean includeParentLevels) {
		if(contextInfo == null)
			return null;
		
		IEnvironmentContextInfo infos[] = getAllContextInfos(contextInfo);
		HashSet<String> set = null;
		
		if(!includeParentLevels){
			ICoreEnvironmentVariableSupplier suppliers[] = infos[0].getSuppliers();
			set = new HashSet<String>();
			for(int i = 0; i < suppliers.length; i++){
				IEnvironmentVariable vars[] = suppliers[i].getVariables(infos[0].getContext());
				if(vars != null){
					for (IEnvironmentVariable var : vars) {
						String name = EnvVarOperationProcessor.normalizeName(var.
								getName());
						if(name != null)
							set.add(name);
					}
				}
				if(!suppliers[i].appendEnvironment(infos[0].getContext()))
					break;
			}
			if(set.size() == 0)
				return new EnvVarCollector();
		}
		
		EnvVarCollector envVarSet = new EnvVarCollector();
		
		for(int i = infos.length-1 ; i >=0 ; i-- ) {
			IEnvironmentContextInfo info = infos[i];
			ICoreEnvironmentVariableSupplier suppliers[] = info.getSuppliers();
			
			for(int j = suppliers.length-1 ; j >= 0 ; j-- ) {
				ICoreEnvironmentVariableSupplier supplier = suppliers[j];
				if(!supplier.appendEnvironment(info.getContext())){
					envVarSet.clear();
				}

				IEnvironmentVariable vars[] = null;
				if(set != null){
					List<IEnvironmentVariable> varList = new ArrayList<IEnvironmentVariable>();
					Iterator<String> iter = set.iterator();
					
					while(iter.hasNext()){
						IEnvironmentVariable var = supplier.getVariable(iter.next(),info.getContext());
						if(var != null)
							varList.add(var);
					}
					vars = varList.toArray(new IEnvironmentVariable[varList.size()]);
				}
				else{
					 vars = supplier.getVariables(info.getContext());
				}
				envVarSet.add(vars,info,j, supplier);
			}
		}

		return envVarSet;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariables()
	 */
	public IEnvironmentVariable[] getVariables(ICConfigurationDescription cfg, boolean resolveMacros) {
		
		
		IEnvironmentContextInfo info = getContextInfo(cfg);
		EnvVarCollector varSet = getVariables(info,true);
		
		EnvVarDescriptor vars[] = varSet != null ? varSet.toArray(false) : null;

		if(vars != null){
			if(!resolveMacros)
				return vars;
			
			IEnvironmentVariable resolved[] = new IEnvironmentVariable[vars.length];
			for(int i = 0; i < vars.length; i++)
				resolved[i] = calculateResolvedVariable(vars[i], info);
			return resolved;
		}
		return new EnvVarDescriptor[0];
	}
	
	/*
	 * returns an array of the IContextInfo that holds the context informations
	 * starting from the one passed to this method and including all subsequent parents
	 */
	public static IEnvironmentContextInfo[] getAllContextInfos(IEnvironmentContextInfo contextInfo){
		if(contextInfo == null)
			return null;
			
		List<IEnvironmentContextInfo> list = new ArrayList<IEnvironmentContextInfo>();
	
		list.add(contextInfo);
			
		while((contextInfo = contextInfo.getNext()) != null)
			list.add(contextInfo);
		
		return list.toArray(new IEnvironmentContextInfo[list.size()]);
	}
	
	private boolean isWin32(){
		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (os.startsWith("windows ")) //$NON-NLS-1$
			return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getDefaultDelimiter()
	 */
	public String getDefaultDelimiter() {
		return isWin32() ? DELIMITER_WIN32 : DELIMITER_UNIX;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#isVariableCaseSensitive()
	 */
	public boolean isVariableCaseSensitive() {
		return !isWin32();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getSuppliers()
	 */
	public ICoreEnvironmentVariableSupplier[] getSuppliers(Object level) {
		IEnvironmentContextInfo info = getContextInfo(level);
		if(info != null)
			return info.getSuppliers();
		return null;
	}
	
	/*
	 * returns true if the first passed contextInfo is the child of the second one
	 */
	public boolean checkParentContextRelation(IEnvironmentContextInfo child, IEnvironmentContextInfo parent){
		if(child == null || parent == null)
			return false;

		IEnvironmentContextInfo enumInfo = child;
		do{
			if(parent.getContext() == enumInfo.getContext())
				return true;
		}while((enumInfo = enumInfo.getNext()) != null);
		return false;
	}
	
	public IEnvironmentVariable calculateResolvedVariable(EnvVarDescriptor des, IEnvironmentContextInfo info){
		if(des == null || info == null)
			return null;

		return calculateResolvedVariable(des,getVariableSubstitutor(getMacroContextInfoForContext(info.getContext()),""," ")); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	public IEnvironmentVariable calculateResolvedVariable(EnvVarDescriptor des, IVariableSubstitutor sub){
		if(des == null)
			return null;
		IEnvironmentVariable var = des;

		try{
			if(sub instanceof EnvVarVariableSubstitutor)
				var = ((EnvVarVariableSubstitutor)sub).resolveVariable(des);
			else if(des.getOperation() != IEnvironmentVariable.ENVVAR_REMOVE){
				String name = des.getName();
				var = new EnvironmentVariable(name,sub.resolveToString(name),des.getOperation(),des.getDelimiter());
			}
		} catch (CdtVariableException e){
		}
		return var;
		
	}
	
	protected int getMacroContextTypeFromContext(Object context){
		if(context instanceof ICConfigurationDescription)
			return ICoreVariableContextInfo.CONTEXT_CONFIGURATION;
		else
			return ICoreVariableContextInfo.CONTEXT_WORKSPACE;
	}
	
	public ICoreVariableContextInfo getMacroContextInfoForContext(Object context){
		return new DefaultVariableContextInfo(getMacroContextTypeFromContext(context),context);
	}
	
	public IVariableSubstitutor getVariableSubstitutor(IVariableContextInfo info, String inexistentMacroValue, String listDelimiter){
		return new EnvVarVariableSubstitutor(info,inexistentMacroValue,listDelimiter);
//		if(fVariableSubstitutor == null)
//			fVariableSubstitutor = new EnvVarVariableSubstitutor(info,inexistentMacroValue,listDelimiter);
//		else {
//			try {
//				fVariableSubstitutor.setMacroContextInfo(info);
//				fVariableSubstitutor.setInexistentMacroValue(inexistentMacroValue);
//				fVariableSubstitutor.setListDelimiter(listDelimiter);
//			} catch (CdtVariableException e){
//				fVariableSubstitutor = new EnvVarVariableSubstitutor(info,inexistentMacroValue,listDelimiter);
//			}
//		}
//		return fVariableSubstitutor;
	}

	public IContributedEnvironment getContributedEnvironment() {
		return fContributedEnvironment;
	}
	
	
}
