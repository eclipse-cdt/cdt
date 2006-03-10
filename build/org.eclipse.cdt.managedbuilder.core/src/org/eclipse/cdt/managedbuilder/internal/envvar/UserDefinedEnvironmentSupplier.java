/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined by a user
 * 
 * @since 3.0
 */
public class UserDefinedEnvironmentSupplier extends 
			StorableEnvironmentLoader 
			implements IEnvironmentVariableSupplier{

	public static final String NODENAME = "environment";  //$NON-NLS-1$
	public static final String PREFNAME_WORKSPACE = "workspace";  //$NON-NLS-1$
	public static final String PREFNAME_PROJECT = "project";  //$NON-NLS-1$
	public static final String NODENAME_CFG = "project";  //$NON-NLS-1$

	private static final String fNonOverloadableVariables[] = new String[]{
			//users not allowed currently to override the "CWD" and "PWD" variables
			EnvVarOperationProcessor.normalizeName("CWD"),   //$NON-NLS-1$
			EnvVarOperationProcessor.normalizeName("PWD")	  //$NON-NLS-1$
		};

	private StorableEnvironment fWorkspaceVariables;
	
	protected StorableEnvironment getEnvironment(Object context){
		return getEnvironment(context,true);
	}
	
	protected StorableEnvironment getEnvironment(Object context, boolean forceLoad){
		if(context == null)
			return null;
		
		StorableEnvironment env = null;
		if(context instanceof IConfiguration){
			IConfiguration cfg = (IConfiguration)context;
			env = ((ToolChain)cfg.getToolChain()).getUserDefinedEnvironment();
			if(env == null && forceLoad){
				env = loadEnvironment(cfg);
				((ToolChain)cfg.getToolChain()).setUserDefinedEnvironment(env);
			}
		}
		else if(context instanceof ManagedProject){
			ManagedProject proj = (ManagedProject)context;
			env = proj.getUserDefinedEnvironmet();
			if(env == null && forceLoad){
				env = loadEnvironment(proj);
				proj.setUserDefinedEnvironmet(env);
			}
		}
		else if(context instanceof IWorkspace){
			if(fWorkspaceVariables == null && forceLoad)
				fWorkspaceVariables = loadEnvironment(context);
			env = fWorkspaceVariables;
		}
		
		return env;
	}
	
	protected ISerializeInfo getSerializeInfo(Object context){
		ISerializeInfo serializeInfo = null;
		
		if(context instanceof IConfiguration){
			IConfiguration cfg = (IConfiguration)context;
			IManagedProject project = cfg.getManagedProject();
			
			final Preferences prefs = project != null ? getConfigurationNode(project) : null;
			final String name = cfg.getId();
			if(prefs != null && name != null)
				serializeInfo = new ISerializeInfo(){
				public Preferences getNode(){
					return prefs;
				}
				
				public String getPrefName(){
					return name;
				}
			};
		}
		else if(context instanceof IManagedProject){
			IManagedProject proj = (IManagedProject)context;
			final Preferences prefs = getProjectNode(proj);
			final String name = PREFNAME_PROJECT;
			if(prefs != null && name != null)
				serializeInfo = new ISerializeInfo(){
				public Preferences getNode(){
					return prefs;
				}
				
				public String getPrefName(){
					return name;
				}
			};
		}
		else if(context instanceof IWorkspace){
			final Preferences prefs = getWorkspaceNode();
			final String name = PREFNAME_WORKSPACE;
			if(prefs != null && name != null)
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
	private Preferences getConfigurationNode(IManagedProject managedProject){
		Preferences prefNode = getProjectNode(managedProject);
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME_CFG);
	}
	
	private Preferences getProjectNode(IManagedProject managedProject){
		IProject project = (IProject)managedProject.getOwner();
		if(!project.exists())
			return null;
		
		Preferences prefNode = new ProjectScope(project).getNode(ManagedBuilderCorePlugin.getUniqueIdentifier());
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME);
	}
	
	private Preferences getWorkspaceNode(){
		Preferences prefNode = new InstanceScope().getNode(ManagedBuilderCorePlugin.getUniqueIdentifier());
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME);
	}
	
	public void checkInexistentConfigurations(IManagedProject managedProject){
		Preferences prefNode = getConfigurationNode(managedProject);
		if(prefNode == null)
			return;
		
		try{
			String ids[] = prefNode.keys();
			boolean found = false;
			for( int i = 0; i < ids.length; i++){
				if(managedProject.getConfiguration(ids[i]) == null){
					prefNode.remove(ids[i]);
					found = true;
				}
			}
			
			if(found)
				prefNode.flush();
		}
		catch(BackingStoreException e){
		}
	}
	
	public void serialize(boolean force){
		if(fWorkspaceVariables != null){
			try{
				storeEnvironment(fWorkspaceVariables,ResourcesPlugin.getWorkspace(),force);
			} catch(CoreException e){
				
			}
		}
	}
	
	public void storeEnvironment(Object context, boolean force){
		StorableEnvironment env = getEnvironment(context, false);
		if(env != null){
			try {
				storeEnvironment(env, context, force);
			} catch (CoreException e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	public IBuildEnvironmentVariable getVariable(String name, Object context) {
		if(getValidName(name) == null)
			return null;
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return null;
		return env.getVariable(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	public IBuildEnvironmentVariable[] getVariables(Object context) {
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return null;
		return filterVariables(env.getVariables());
	}
	
	public IBuildEnvironmentVariable createVariable(String name, String value, int op, String delimiter, Object context){
		if(getValidName(name) == null)
			return null;
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return null;
		IBuildEnvironmentVariable var =  env.createVariable(name,value,op,delimiter);
		if(env.isChanged())
			setRebuildStateForContext(context);
		return var;
	}

	public IBuildEnvironmentVariable deleteVariable(String name, Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return null;
		IBuildEnvironmentVariable var = env.deleteVariable(name);
		if(var != null)
			setRebuildStateForContext(context);
		return var;
	}
	
	public void deleteAll(Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return;

		if(env.deleteAll())
		setRebuildStateForContext(context);
	}
	
	public void setVariables(IBuildEnvironmentVariable vars[], Object context){
		StorableEnvironment env = getEnvironment(context);
		if(env == null)
			return;
		
		env.setVariales(vars);
		if(env.isChanged())
			setRebuildStateForContext(context);
	}
	
	protected void setRebuildStateForContext(Object context){
		if(context == null)
			return;
		if(context instanceof IConfiguration){
			cfgVarsModified((IConfiguration)context);
		}
		else if(context instanceof IManagedProject){
			IConfiguration cfgs[] = ((IManagedProject)context).getConfigurations();
			for(int i = 0; i < cfgs.length; i++){
				cfgVarsModified(cfgs[i]);
			}
		}
		else if(context instanceof IWorkspace){
			IProject projects[] = ((IWorkspace)context).getRoot().getProjects();
			for(int i = 0; i < projects.length; i++){
				if(ManagedBuildManager.manages(projects[i])){
					IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(projects[i]);
					if(info != null){
						IConfiguration cfgs[] = info.getManagedProject().getConfigurations();
						for(int j = 0; j < cfgs.length; j++){
							cfgVarsModified(cfgs[j]);
						}
					}
				}
			}
		}
			
	}
	
	protected void cfgVarsModified(IConfiguration cfg){
		cfg.setRebuildState(true);
		EnvironmentVariableProvider.getDefault().checkBuildPathVariables(cfg);
	}

	protected String getValidName(String name){
		if(name == null || (name = name.trim()).length() == 0)
			return null;
		if(fNonOverloadableVariables != null){
			for(int i = 0; i < fNonOverloadableVariables.length; i++){
				if(fNonOverloadableVariables[i].equals(EnvVarOperationProcessor.normalizeName(name)))
					return null;
			}
		}
		return name;
	}
	
	protected IBuildEnvironmentVariable[] filterVariables(IBuildEnvironmentVariable variables[]){
		return EnvVarOperationProcessor.filterVariables(variables,fNonOverloadableVariables);
	}

}
