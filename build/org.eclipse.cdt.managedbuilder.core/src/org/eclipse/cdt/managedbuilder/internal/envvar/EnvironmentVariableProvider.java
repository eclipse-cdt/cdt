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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentBuildPathsChangeListener;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.EnvironmentMacroSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.QualifiedName;

/**
 * This class implements the IEnvironmentVariableProvider interface and provides all
 * build environment funvtionality to the MBS
 * 
 * @since 3.0
 *
 */
public class EnvironmentVariableProvider implements
		IEnvironmentVariableProvider {
	private static final QualifiedName fBuildPathVarProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "buildPathVar");	//$NON-NLS-1$

	private static final String DELIMITER_WIN32 = ";";  //$NON-NLS-1$
	private static final String DELIMITER_UNIX = ":";  //$NON-NLS-1$
	
	private static EnvironmentVariableProvider fInstance = null;
	private List fListeners = null;

	private EnvVarMacroSubstitutor fMacroSubstitutor;
	
	private StoredBuildPathEnvironmentContainer fIncludeStoredBuildPathVariables;
	private StoredBuildPathEnvironmentContainer fLibraryStoredBuildPathVariables;
	
	public static final UserDefinedEnvironmentSupplier fUserSupplier = new UserDefinedEnvironmentSupplier();
	public static final ExternalExtensionEnvironmentSupplier fExternalSupplier = new ExternalExtensionEnvironmentSupplier();
	public static final MbsEnvironmentSupplier fMbsSupplier = new MbsEnvironmentSupplier();
	public static final EclipseEnvironmentSupplier fEclipseSupplier = new EclipseEnvironmentSupplier();

	/**
	 * This class is used by the EnvironmentVariableProvider to calculate the build paths
	 * in case a tool-integrator did not provide the special logic for obtaining the build 
	 * paths from environment variable values
	 * 
	 * @since 3.0
	 *
	 */
	static public class DefaultBuildPathResolver implements IBuildPathResolver {
		private String fDelimiter;
		
		public DefaultBuildPathResolver(String delimiter){
			fDelimiter = delimiter;
		}

		public String[] resolveBuildPaths(int pathType, String variableName,
				String variableValue, IConfiguration configuration) {
			
			if(fDelimiter == null || "".equals(fDelimiter)) //$NON-NLS-1$
				return new String[]{variableValue};
			
			List list = EnvVarOperationProcessor.convertToList(variableValue,fDelimiter);
			return (String[]) list.toArray(new String[list.size()]);
		}

	}

	public class EnvVarMacroSubstitutor extends DefaultMacroSubstitutor {
		private String fDefaultDelimiter;
		public EnvVarMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
			super(contextType,contextData,inexistentMacroValue,listDelimiter);
			fDefaultDelimiter = listDelimiter;
		}

		public EnvVarMacroSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter){
			super(contextInfo, inexistentMacroValue, listDelimiter, null ,inexistentMacroValue);
			fDefaultDelimiter = listDelimiter;
		}
		
		public IBuildEnvironmentVariable resolveVariable(EnvVarDescriptor var) throws BuildMacroException {
			String value;
			if(var == null || (value = var.getValue()) == null || value.length() == 0 || var.getOperation() == IBuildEnvironmentVariable.ENVVAR_REMOVE)
				return var;

			String listDelimiter = var.getDelimiter();
			if(listDelimiter == null)
				listDelimiter = fDefaultDelimiter;
			setListDelimiter(listDelimiter);
			IBuildMacro macro = EnvironmentMacroSupplier.getInstance().createBuildMacro(var);
			IMacroContextInfo varMacroInfo = getVarMacroContextInfo(var);
			int varSupplierNum = getVarMacroSupplierNum(var,varMacroInfo);
			value = resolveToString(new MacroDescriptor(macro,varMacroInfo,varSupplierNum));
			removeResolvedMacro(var.getName());
			return new BuildEnvVar(var.getName(),value,var.getOperation(),var.getDelimiter());
		}
		
		protected IMacroContextInfo getVarMacroContextInfo(EnvVarDescriptor var){
			IContextInfo info = var.getContextInfo();
			if(info != null)
				return getMacroContextInfoForContext(info.getContext());
			return null;
		}
		
		protected int getVarMacroSupplierNum(EnvVarDescriptor var, IMacroContextInfo varMacroInfo){
			int varSupplierNum = -1;
			IBuildMacroSupplier macroSuppliers[] = varMacroInfo.getSuppliers();
			for(int i = 0; i < macroSuppliers.length; i++){
				if(macroSuppliers[i] instanceof EnvironmentMacroSupplier){
					varSupplierNum = i;
					break;
				}
			}
			return varSupplierNum;
		}
	}

	protected EnvironmentVariableProvider(){
		
	}

	public static EnvironmentVariableProvider getDefault(){
		if(fInstance == null)
			fInstance = new EnvironmentVariableProvider();
		return fInstance;
	}
	
	/*
	 * returns a variable of a given name or null
	 * the context information is taken from the contextInfo passed
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo
	 */
	public EnvVarDescriptor getVariable(String variableName,
			IContextInfo contextInfo, boolean includeParentLevels){

		if(contextInfo == null)
			return null;
		if((variableName = EnvVarOperationProcessor.normalizeName(variableName)) == null) //$NON-NLS-1$
			return null;


		IContextInfo infos[] = getAllContextInfos(contextInfo);
		
		if(!includeParentLevels){
			IEnvironmentVariableSupplier suppliers[] = infos[0].getSuppliers();
			boolean bVarFound = false;
			for(int i = 0; i < suppliers.length; i++){
				if(suppliers[i].getVariable(variableName,infos[0].getContext()) != null){
					bVarFound = true;
					break;
				}
			}
			if(!bVarFound)
				return null;
		}

		IBuildEnvironmentVariable variable = null;
		IContextInfo varContextInfo = null;
		int varSupplierNum = -1;
		
		for(int i = infos.length-1 ; i >=0 ; i-- ) {
			IContextInfo info = infos[i];
			IEnvironmentVariableSupplier suppliers[] = info.getSuppliers();
			
			for(int j = suppliers.length-1 ; j >= 0 ; j-- ) {
				IEnvironmentVariableSupplier supplier = suppliers[j];
				IBuildEnvironmentVariable var = supplier.getVariable(variableName,info.getContext());
				
				if(var == null)
					continue;
				
				varContextInfo = info;
				varSupplierNum = j;
					
				if(variable == null)
					variable = var;
				else
					variable = EnvVarOperationProcessor.performOperation(variable,var);
			}
		}
	
		if(variable != null){
			if(variable.getOperation() == IBuildEnvironmentVariable.ENVVAR_REMOVE)
				return null;
			return new EnvVarDescriptor(variable,varContextInfo,varSupplierNum);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariable()
	 */
	public IBuildEnvironmentVariable getVariable(String variableName,
			Object level, boolean includeParentLevels, boolean resolveMacros) {

		if(variableName == null || "".equals(variableName)) //$NON-NLS-1$
			return null;
		
		IContextInfo info = getContextInfo(level);
		EnvVarDescriptor var = getVariable(variableName,info,includeParentLevels);
		if(level instanceof IConfiguration && includeParentLevels)
			checkBuildPathVariable((IConfiguration)level,variableName,var);
		
		return resolveMacros ? calculateResolvedVariable(var,info) : var;
	}
	
	/*
	 * returns the context info that should be used for the given level
	 * or null if the the given level is not supported
	 */
	public IContextInfo getContextInfo(Object level){
		DefaultContextInfo info = new DefaultContextInfo(level);
		if(info.getSuppliers() == null)
			return null;
		return info;
	}
	
	/*
	 * returns a list of defined variables.
	 * the context information is taken from the contextInfo passed
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo
	 */
	public EnvVarCollector getVariables(IContextInfo contextInfo,
			boolean includeParentLevels) {
		if(contextInfo == null)
			return null;
		
		IContextInfo infos[] = getAllContextInfos(contextInfo);
		HashSet set = null;
		
		if(!includeParentLevels){
			IEnvironmentVariableSupplier suppliers[] = infos[0].getSuppliers();
			set = new HashSet();
			for(int i = 0; i < suppliers.length; i++){
				IBuildEnvironmentVariable vars[] = suppliers[i].getVariables(infos[0].getContext());
				if(vars != null){
					for(int j = 0; j < vars.length; j++){
						String name = EnvVarOperationProcessor.normalizeName(vars[j].
								getName());
						if(name != null)
							set.add(name);
					}
				}
			}
			if(set.size() == 0)
				return new EnvVarCollector();
		}
		
		EnvVarCollector envVarSet = new EnvVarCollector();
		
		for(int i = infos.length-1 ; i >=0 ; i-- ) {
			IContextInfo info = infos[i];
			IEnvironmentVariableSupplier suppliers[] = info.getSuppliers();
			
			for(int j = suppliers.length-1 ; j >= 0 ; j-- ) {
				IEnvironmentVariableSupplier supplier = suppliers[j];
				IBuildEnvironmentVariable vars[] = null;
				if(set != null){
					List varList = new ArrayList();
					Iterator iter = set.iterator();
					
					while(iter.hasNext()){
						IBuildEnvironmentVariable var = supplier.getVariable((String)iter.next(),info.getContext());
						if(var != null)
							varList.add(var);
					}
					vars = (IBuildEnvironmentVariable[])varList.toArray(new IBuildEnvironmentVariable[varList.size()]);
				}
				else{
					 vars = supplier.getVariables(info.getContext());
				}
				envVarSet.add(vars,info,j);
			}
		}

		return envVarSet;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getVariables()
	 */
	public IBuildEnvironmentVariable[] getVariables(Object level,
			boolean includeParentLevels, boolean resolveMacros) {
		
		IContextInfo info = getContextInfo(level);
		EnvVarCollector varSet = getVariables(info,includeParentLevels);
		
		EnvVarDescriptor vars[] = varSet != null ? varSet.toArray(false) : null;

		if(level instanceof IConfiguration)
			if(includeParentLevels)
				checkBuildPathVariables((IConfiguration)level,varSet);
			else if (vars != null){
				for(int i = 0; i < vars.length; i++)
					checkBuildPathVariable((IConfiguration)level,vars[i].getName(),vars[i]);
			}

		if(vars != null){
			if(!resolveMacros)
				return vars;
			
			IBuildEnvironmentVariable resolved[] = new IBuildEnvironmentVariable[vars.length];
			for(int i = 0; i < vars.length; i++)
				resolved[i] = calculateResolvedVariable(vars[i], info);
			return resolved;
		}
		return null;
	}
	
	/*
	 * returns an array of the IContextInfo that holds the context informations
	 * starting from the one passed to this method and including all subsequent parents
	 */
	private IContextInfo[] getAllContextInfos(IContextInfo contextInfo){
		if(contextInfo == null)
			return null;
			
		List list = new ArrayList();
	
		list.add(contextInfo);
			
		while((contextInfo = contextInfo.getNext()) != null)
			list.add(contextInfo);
		
		return (IContextInfo[])list.toArray(new IContextInfo[list.size()]);
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
	public IEnvironmentVariableSupplier[] getSuppliers(Object level) {
		IContextInfo info = getContextInfo(level);
		if(info != null)
			return info.getSuppliers();
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#getBuildPaths()
	 */
	public String[] getBuildPaths(IConfiguration configuration,
			int buildPathType) {
		ITool tools[] = configuration.getFilteredTools();
		List list = new ArrayList();
		
		for(int i = 0; i < tools.length; i++){
			IEnvVarBuildPath pathDescriptors[] = tools[i].getEnvVarBuildPaths();
			
			if(pathDescriptors == null || pathDescriptors.length == 0)
				continue;
			
			for(int j = 0; j < pathDescriptors.length; j++){
				IEnvVarBuildPath curPathDes = pathDescriptors[j];
				if(curPathDes.getType() != buildPathType)
					continue;
				
				String vars[] = curPathDes.getVariableNames();
				if(vars == null || vars.length == 0)
					continue;
				
				IBuildPathResolver pathResolver = curPathDes.getBuildPathResolver();
				if(pathResolver == null){
					String delimiter = curPathDes.getPathDelimiter();
					if(delimiter == null)
						delimiter = getDefaultDelimiter();
					pathResolver = new DefaultBuildPathResolver(delimiter);
				}
				
				for(int k = 0; k < vars.length; k++){
					String varName = vars[k];
					
					EnvVarDescriptor var = getVariable(varName,getContextInfo(configuration),true);
					if(var == null)
						continue;
					
					String varValue = calculateResolvedVariable(var, getContextInfo(configuration)).getValue();
					String paths[] = pathResolver.resolveBuildPaths(buildPathType,varName,varValue,configuration);
					if(paths != null && paths.length != 0)
						list.addAll(Arrays.asList(paths));
				}
			}
		}
		
		return (String[])list.toArray(new String[list.size()]);
	}
	
	/*
	 * returns a list of registered listeners
	 */
	private List getListeners(){
		if(fListeners == null)
			fListeners = new ArrayList();
		return fListeners;
	}
	
	/*
	 * notifies registered listeners
	 */
	private void notifyListeners(IConfiguration configuration, int buildPathType){
		List listeners = getListeners();
		Iterator iterator = listeners.iterator();
		while(iterator.hasNext())
			((IEnvironmentBuildPathsChangeListener)iterator.next()).buildPathsChanged(configuration,buildPathType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#subscribe()
	 */
	public synchronized void subscribe(IEnvironmentBuildPathsChangeListener listener) {
		if(listener == null)
			return;
		
		List listeners = getListeners();
		
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider#unsubscribe()
	 */
	public synchronized void unsubscribe(IEnvironmentBuildPathsChangeListener listener) {
		if(listener == null)
			return;
		
		List listeners = getListeners();
		
		listeners.remove(listener);
	}
	
	/*
	 * returns true if the first passed contextInfo is the child of the second one
	 */
	public boolean checkParentContextRelation(IContextInfo child, IContextInfo parent){
		if(child == null || parent == null)
			return false;

		IContextInfo enumInfo = child;
		do{
			if(parent.getContext() == enumInfo.getContext())
				return true;
		}while((enumInfo = enumInfo.getNext()) != null);
		return false;
	}
	
	/*
	 * performs a check of the build path variables for the given configuration
	 * If the build variables are changed, the notification is sent
	 */
	public void checkBuildPathVariables(IConfiguration configuration){
		checkBuildPathVariables(configuration,getVariables(getContextInfo(configuration),true));
	}

	/*
	 * performs a check of the build path variables of the specified type
	 * for the given configuration
	 * If the build variables are changed, the notification is sent
	 */
	public void checkBuildPathVariables(IConfiguration configuration,int buildPathType){
		checkBuildPathVariables(configuration,buildPathType,getVariables(getContextInfo(configuration),true));
	}

	/*
	 * performs a check of the build path variables
	 * for the given configuration given the set of the variables 
	 * defined for this configuration
	 * If the build variables are changed, the notification is sent
	 */
	protected void checkBuildPathVariables(IConfiguration configuration, EnvVarCollector varSet){
		checkBuildPathVariables(configuration,IEnvVarBuildPath.BUILDPATH_INCLUDE,varSet);
		checkBuildPathVariables(configuration,IEnvVarBuildPath.BUILDPATH_LIBRARY,varSet);
	}

	/*
	 * performs a check of whether the given variable is the build path variable
	 * and if true checks whether it is changed.
	 * In the case of it is changed all other build path variables are checked
	 * and notification is sent.
	 * If it is not changed, other build path variables are not checked
	 * In the case of the given variable is not the build path one, this method does nothing
	 */
	protected void checkBuildPathVariable(IConfiguration configuration, String varName, EnvVarDescriptor var){
		checkBuildPathVariable(configuration, IEnvVarBuildPath.BUILDPATH_INCLUDE, varName, var);
		checkBuildPathVariable(configuration, IEnvVarBuildPath.BUILDPATH_LIBRARY, varName, var);
	}

	/*
	 * performs a check of whether the given variable is the build path variable
	 * of the specified type and if true checks whether it is changed.
	 * In the case of it is changed all other build path variables of that type are checked
	 * and notification is sent.
	 * If it is not changed, other build path variables are not checked
	 * In the case of the given variable is not the build path one, this method does nothing
	 */
	protected void checkBuildPathVariable(IConfiguration configuration, int buildPathType, String varName, EnvVarDescriptor var){
		StoredBuildPathEnvironmentContainer buildPathVars = getStoredBuildPathVariables(buildPathType);
		if(buildPathVars == null)
			return;
		if(buildPathVars.isVariableChanged(varName,var,configuration)){
			buildPathVars.synchronize(getVariables(getContextInfo(configuration),true),configuration);
			notifyListeners(configuration, buildPathType);
		}
	}

	/*
	 * performs a check of the build path variables of the specified type
	 * for the given configuration given the set of the variables 
	 * defined for this configuration. 
	 * If the build variables are changed, the notification is sent
	 */
	protected void checkBuildPathVariables(IConfiguration configuration, int buildPathType, EnvVarCollector varSet){
		StoredBuildPathEnvironmentContainer buildPathVars = getStoredBuildPathVariables(buildPathType);
		if(buildPathVars == null)
			return;
		if(buildPathVars.checkBuildPathChange(varSet,configuration)){
			notifyListeners(configuration, buildPathType);
		}
	}
	
	/*
	 * returns the container of the build variables of the specified type
	 */
	protected StoredBuildPathEnvironmentContainer getStoredBuildPathVariables(int buildPathType){
		return buildPathType == IEnvVarBuildPath.BUILDPATH_LIBRARY ?
				getStoredLibraryBuildPathVariables() :
				getStoredIncludeBuildPathVariables();
	}
	
	/*
	 * returns the container of the Include path variables
	 */
	protected StoredBuildPathEnvironmentContainer getStoredIncludeBuildPathVariables(){
		if(fIncludeStoredBuildPathVariables == null)
			fIncludeStoredBuildPathVariables = new StoredBuildPathEnvironmentContainer(IEnvVarBuildPath.BUILDPATH_INCLUDE);
		return fIncludeStoredBuildPathVariables;
	}

	/*
	 * returns the container of the Library path variables
	 */
	protected StoredBuildPathEnvironmentContainer getStoredLibraryBuildPathVariables(){
		if(fLibraryStoredBuildPathVariables == null)
			fLibraryStoredBuildPathVariables = new StoredBuildPathEnvironmentContainer(IEnvVarBuildPath.BUILDPATH_LIBRARY);
		return fLibraryStoredBuildPathVariables;
	}	

	public IBuildEnvironmentVariable calculateResolvedVariable(EnvVarDescriptor des, IContextInfo info){
		if(des == null || info == null)
			return null;

		return calculateResolvedVariable(des,getMacroSubstitutor(getMacroContextInfoForContext(info.getContext()),""," ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public IBuildEnvironmentVariable calculateResolvedVariable(EnvVarDescriptor des, IMacroSubstitutor sub){
		if(des == null)
			return null;
		IBuildEnvironmentVariable var = des;

		try{
			if(sub instanceof EnvVarMacroSubstitutor)
				var = ((EnvVarMacroSubstitutor)sub).resolveVariable(des);
			else if(des.getOperation() != IBuildEnvironmentVariable.ENVVAR_REMOVE){
				String name = des.getName();
				var = new BuildEnvVar(name,sub.resolveToString(name),des.getOperation(),des.getDelimiter());
			}
		} catch (BuildMacroException e){
		}
		return var;
		
	}
	
	protected int getMacroContextTypeFromContext(Object context){
		if(context instanceof IConfiguration)
			return IBuildMacroProvider.CONTEXT_CONFIGURATION;
		else if(context instanceof IManagedProject)
			return IBuildMacroProvider.CONTEXT_PROJECT;
		else if(context instanceof IWorkspace)
			return IBuildMacroProvider.CONTEXT_WORKSPACE;
		else if(context == null)
			return IBuildMacroProvider.CONTEXT_ECLIPSEENV;
		return 0;
	}
	
	public IMacroContextInfo getMacroContextInfoForContext(Object context){
		return new DefaultMacroContextInfo(getMacroContextTypeFromContext(context),context);
	}
	
	public IMacroSubstitutor getMacroSubstitutor(IMacroContextInfo info, String inexistentMacroValue, String listDelimiter){
		if(fMacroSubstitutor == null)
			fMacroSubstitutor = new EnvVarMacroSubstitutor(info,inexistentMacroValue,listDelimiter);
		else {
			try {
				fMacroSubstitutor.setMacroContextInfo(info);
				fMacroSubstitutor.setInexistentMacroValue(inexistentMacroValue);
				fMacroSubstitutor.setListDelimiter(listDelimiter);
			} catch (BuildMacroException e){
				fMacroSubstitutor = new EnvVarMacroSubstitutor(info,inexistentMacroValue,listDelimiter);
			}
		}
		return fMacroSubstitutor;
	}
	
}
