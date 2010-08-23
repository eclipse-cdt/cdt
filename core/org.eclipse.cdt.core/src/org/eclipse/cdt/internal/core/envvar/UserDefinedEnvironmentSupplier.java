/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.cdt.utils.envvar.StorableEnvironmentLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This is the Environment Variable Supplier used to supply and persist user 
 * defined variables.  Variables are stored in the context of a CDT {@link ICConfigurationDescription},
 * or, globally at the {@link IWorkspace} level.
 *
 * <p>
 * This class is Singleton held by {@link EnvironmentVariableManager}.
 *
 * <p>
 * It also allows temporary 'overriding' of variables. These are not persisted, but override
 * the values of any existing user-defined variable. This functionality is used by HeadlessBuilder
 * to temporarily override environment variables on the command line.
 *
 * @since 3.0
 */
public class UserDefinedEnvironmentSupplier extends 
			StorableEnvironmentLoader 
			implements ICoreEnvironmentVariableSupplier{

	public static final String NODENAME = "environment";  //$NON-NLS-1$
	public static final String PREFNAME_WORKSPACE = "workspace";  //$NON-NLS-1$
	public static final String PREFNAME_PROJECT = "project";  //$NON-NLS-1$
	public static final String NODENAME_CFG = "project";  //$NON-NLS-1$

/*	private static final String fNonOverloadableVariables[] = new String[]{
			//users not allowed currently to override the "CWD" and "PWD" variables
			EnvVarOperationProcessor.normalizeName("CWD"),   //$NON-NLS-1$
			EnvVarOperationProcessor.normalizeName("PWD")	  //$NON-NLS-1$
		};
*/
	private StorableEnvironment fWorkspaceVariables;
	private StorableEnvironment fOverrideVariables = new StorableEnvironment(false);

	static class VarKey {
		private IEnvironmentVariable fVar;
		private boolean fNameOnly;
		private int fCode;
		
		VarKey(IEnvironmentVariable var, boolean nameOnly){
			fVar = var;
			fNameOnly = nameOnly;
		}
		
		public IEnvironmentVariable getVariable(){
			return fVar; 
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(!(obj instanceof VarKey))
				return false;
			
			VarKey other = (VarKey)obj;
			
			IEnvironmentVariable otherVar = other.fVar;
			
			if(fVar == otherVar)
				return true;
			
			if(!CDataUtil.objectsEqual(fVar.getName(), otherVar.getName()))
				return false;

			if(fNameOnly)
				return true;
			
			if(fVar.getOperation() != otherVar.getOperation())
				return false;

			if(!CDataUtil.objectsEqual(fVar.getValue(), otherVar.getValue()))
				return false;
			
			if(!CDataUtil.objectsEqual(fVar.getDelimiter(),otherVar.getDelimiter()))
				return false;
				
			return true;
		}

		@Override
		public int hashCode() {
			int code = fCode;
			if(code == 0){
				code = 47;
				
				String tmp = fVar.getName();
				if(tmp != null)
					code += tmp.hashCode();
	
				if(fNameOnly)
					return code;
				
				code += fVar.getOperation();
				
				tmp = fVar.getValue();
				if(tmp != null)
					code += tmp.hashCode();
				
				tmp = fVar.getDelimiter();
				if(tmp != null)
					code += tmp.hashCode();
				
				fCode = code;
			}
			return code;
		}
		
	}
	protected StorableEnvironment getEnvironment(Object context){
		return getEnvironment(context,true);
	}
	
	protected StorableEnvironment getEnvironment(Object context, boolean forceLoad){
//		if(context == null)
//			return null;
		
		StorableEnvironment env = null;
		if(context instanceof IInternalCCfgInfo){
			try {
				CConfigurationSpecSettings settings = ((IInternalCCfgInfo)context).getSpecSettings();
				env = settings.getEnvironment();
				if(env == null && forceLoad){
					env = loadEnvironment(context, settings.isReadOnly());
					settings.setEnvironment(env);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		else if(context instanceof IWorkspace || context == null){
			if(fWorkspaceVariables == null && forceLoad)
				fWorkspaceVariables = loadEnvironment(context, false);
			env = fWorkspaceVariables;
		}
		
		return env;
	}

	@Override
	protected ISerializeInfo getSerializeInfo(Object context){
		ISerializeInfo serializeInfo = null;

		if(context instanceof ICConfigurationDescription){
			final ICConfigurationDescription cfg = (ICConfigurationDescription)context;
			final String name = cfg.getId();
			if(name != null)
				serializeInfo = new ISerializeInfo(){
				public Preferences getNode(){
					return getConfigurationNode(cfg.getProjectDescription());
				}

				public String getPrefName(){
					return name;
				}
			};
		}
		else if(context == null || context instanceof IWorkspace){
			final Preferences prefs = getWorkspaceNode();
			final String name = PREFNAME_WORKSPACE;
			if (prefs != null)
				serializeInfo = new ISerializeInfo(){
				public Preferences getNode(){
					return prefs;
				}
				
				public String getPrefName(){
					return name;
				}
			};
		}
		return serializeInfo;
	}
	
	private Preferences getConfigurationNode(ICProjectDescription projDes){
		Preferences prefNode = getProjectNode(projDes);
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME_CFG);
	}
	
	private Preferences getProjectNode(ICProjectDescription projDes){
		if(projDes == null)
			return null;
		IProject project = projDes.getProject();
		if(!project.exists())
			return null;
		
		Preferences prefNode = new ProjectScope(project).getNode(CCorePlugin.PLUGIN_ID);
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME);
	}
	
	private Preferences getWorkspaceNode(){
		Preferences prefNode = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME);
	}
	
	public void checkInexistentConfigurations(ICProjectDescription projDes){
		Preferences prefNode = getConfigurationNode(projDes);
		if(prefNode == null)
			return;
		
		try{
			String ids[] = prefNode.keys();
			boolean found = false;
			for (String id : ids) {
				if(projDes.getConfigurationById(id) == null){
					prefNode.remove(id);
					found = true;
				}
			}
			
			if(found)
				prefNode.flush();
		}
		catch(BackingStoreException e){
		}
	}
	
	public void storeWorkspaceEnvironment(boolean force){
		if(fWorkspaceVariables != null){
			try{
				storeEnvironment(fWorkspaceVariables,ResourcesPlugin.getWorkspace(),force, true);
			} catch(CoreException e){
				
			}
		}
	}
	
	public StorableEnvironment getWorkspaceEnvironmentCopy(){
		StorableEnvironment envVar = getEnvironment(null); 
		return new StorableEnvironment(envVar, false);
	}
	
	public boolean setWorkspaceEnvironment(StorableEnvironment env){
		StorableEnvironment oldEnv = getEnvironment(null);
		
		fWorkspaceVariables = new StorableEnvironment(env, false);
		
		EnvironmentChangeEvent event = createEnvironmentChangeEvent(fWorkspaceVariables.getVariables(), oldEnv.getVariables());
		
		storeWorkspaceEnvironment(true);
		
//		updateProjectInfo(null);
		
		return event != null;
	}
	
	static EnvironmentChangeEvent createEnvironmentChangeEvent(IEnvironmentVariable[] newVars, IEnvironmentVariable[] oldVars){
		IEnvironmentVariable[] addedVars = null, removedVars = null, changedVars = null;
		
		if(oldVars == null || oldVars.length == 0){
			if(newVars != null && newVars.length != 0)
				addedVars = newVars.clone(); 
		} else if(newVars == null || newVars.length == 0){
			removedVars = oldVars.clone();
		} else {
			HashSet<VarKey> newSet = new HashSet<VarKey>(newVars.length);
			HashSet<VarKey> oldSet = new HashSet<VarKey>(oldVars.length);
			
			for (IEnvironmentVariable newVar : newVars) {
				newSet.add(new VarKey(newVar, true));
			}
	
			for (IEnvironmentVariable oldVar : oldVars) {
				oldSet.add(new VarKey(oldVar, true));
			}
	
			@SuppressWarnings("unchecked")
			HashSet<VarKey> newSetCopy = (HashSet<VarKey>)newSet.clone();
	
			newSet.removeAll(oldSet);
			oldSet.removeAll(newSetCopy);
			
			if(newSet.size() != 0){
				addedVars = varsFromKeySet(newSet);
			}
			
			if(oldSet.size() != 0){
				removedVars = varsFromKeySet(oldSet);
			}
			
			newSetCopy.removeAll(newSet);
			
			HashSet<VarKey> modifiedSet = new HashSet<VarKey>(newSetCopy.size());
			for (VarKey key : newSetCopy) {
				modifiedSet.add(new VarKey(key.getVariable(), false));
			}
			
			for (IEnvironmentVariable oldVar : oldVars) {
				modifiedSet.remove(new VarKey(oldVar, false));
			}
			
			if(modifiedSet.size() != 0)
				changedVars = varsFromKeySet(modifiedSet); 
		}
		
		if(addedVars != null || removedVars != null || changedVars != null)
			return new EnvironmentChangeEvent(addedVars, removedVars, changedVars);
		return null;
	}
	
	static IEnvironmentVariable[] varsFromKeySet(Set<VarKey> set){
		IEnvironmentVariable vars[] = new IEnvironmentVariable[set.size()];
		int i = 0;
		for(Iterator<VarKey> iter = set.iterator(); iter.hasNext(); i++){
			VarKey key = iter.next();
			vars[i] = key.getVariable();
		}
		
		return vars;
	}

	
	public void storeProjectEnvironment(ICProjectDescription des, boolean force){
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs) {
			storeEnvironment(cfg, force, false);
		}
		
		Preferences node = getProjectNode(des);
		try {
			node.flush();
		} catch (BackingStoreException e) {
		}
	}
	
	private void storeEnvironment(Object context, boolean force, boolean flush){
		StorableEnvironment env = getEnvironment(context, false);
		if(env != null){
			try {
				storeEnvironment(env, context, force, flush);
			} catch (CoreException e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	public IEnvironmentVariable getVariable(String name, Object context) {
		if(getValidName(name) == null)
			return null;
		IEnvironmentVariable var = fOverrideVariables.getVariable(name);
		StorableEnvironment env = getEnvironment(context);
		if (env == null)
			return var;
		return EnvVarOperationProcessor.performOperation(env.getVariable(name), var);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	public IEnvironmentVariable[] getVariables(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return null;
		IEnvironmentVariable[] override = filterVariables(fOverrideVariables.getVariables());
		IEnvironmentVariable[] normal = filterVariables(env.getVariables());
		return combineVariables(normal, override);
	}

	private IEnvironmentVariable[] combineVariables(IEnvironmentVariable[] oldVariables, IEnvironmentVariable[] newVariables) {
		Map<String, IEnvironmentVariable> vars = new HashMap<String, IEnvironmentVariable>(oldVariables.length + newVariables.length);
		for (IEnvironmentVariable variable : oldVariables)
			vars.put(variable.getName(), variable);
		for (IEnvironmentVariable variable : newVariables) {
			if (!vars.containsKey(variable.getName()))
				vars.put(variable.getName(), variable);
			else
				vars.put(variable.getName(), EnvVarOperationProcessor.performOperation(vars.get(variable.getName()), variable));
		}
		return vars.values().toArray(new IEnvironmentVariable[vars.size()]);
	}

	/**
	 * Add an environment variable 'override'. This variable won't be persisted but will instead 
	 * replace / remove / prepend / append any existing environment variable with the same name.
	 * This change is not persisted and remains for the current eclipse session.
	 *
	 * @param name Environment variable name
	 * @param value Environment variable value
	 * @param op one of the IBuildEnvironmentVariable.ENVVAR_* operation types
	 * @param delimiter delimiter to use or null for default
	 * @return Overriding IEnvironmentVariable or null if name is not valid 
	 */
	public IEnvironmentVariable createOverrideVariable(String name, String value, int op, String delimiter) {
		if (getValidName(name) == null)
			return null;
		return fOverrideVariables.createVariable(name,value,op,delimiter);
	}

	public IEnvironmentVariable createVariable(String name, String value, int op, String delimiter, Object context){
		if(getValidName(name) == null)
			return null;
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return null;
		IEnvironmentVariable var =  env.createVariable(name,value,op,delimiter);
		if(env.isChanged()){
//			updateProjectInfo(context);
			env.setChanged(false);
		}
		return var;
	}

	public IEnvironmentVariable deleteVariable(String name, Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return null;
		IEnvironmentVariable var = env.deleteVariable(name);
		if(var != null){
//			updateProjectInfo(context);
		}
		return var;
	}
	
	public void deleteAll(Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return;

		if(env.deleteAll()){
//			updateProjectInfo(context);
		}
	}
	
	public void setVariables(IEnvironmentVariable vars[], Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return;
		
		env.setVariales(vars);
		if(env.isChanged()){
//			updateProjectInfo(context);
			env.setChanged(false);
		}
	}
	
//	protected void updateProjectInfo(Object context){
//	}
	
//	protected void cfgVarsModified(ICConfigurationDescription cfg){
//		cfg.setRebuildState(true);
//		EnvironmentVariableProvider.getDefault().checkBuildPathVariables(cfg);
//	}

	protected String getValidName(String name){
		if(name == null || (name = name.trim()).length() == 0)
			return null;
//		if(fNonOverloadableVariables != null){
//			for(int i = 0; i < fNonOverloadableVariables.length; i++){
//				if(fNonOverloadableVariables[i].equals(EnvVarOperationProcessor.normalizeName(name)))
//					return null;
//			}
//		}
		return name;
	}
	
	protected IEnvironmentVariable[] filterVariables(IEnvironmentVariable variables[]){
		return EnvVarOperationProcessor.filterVariables(variables,null);
	}

	public boolean appendEnvironment(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return true;
		return env.appendEnvironment();
	}
	
	public boolean appendContributedEnvironment(Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return true;
		return env.appendContributedEnvironment();
	}

	
	public void setAppendEnvironment(boolean append, Object context) {
		StorableEnvironment env = getEnvironment(context);
		if(env != null){
			env.setAppendEnvironment(append);
		}
	}
	
	public void setAppendContributedEnvironment(boolean append, Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env != null){
			env.setAppendContributedEnvironment(append);
		}
	}
	
	public void restoreDefaults(Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env != null){
			env.restoreDefaults();
		}
	}

}
