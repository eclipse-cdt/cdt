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
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
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

	private StorableEnvironment fConfigurationVariables;
	private StorableEnvironment fProjectVariables;
	private StorableEnvironment fWorkspaceVariables;
	
	private IConfiguration fCurrentCfg = null;
	private IManagedProject fCurrentProj = null;

	protected StorableEnvironment getEnvironment(Object context){
		if(context == null)
			return null;
		
		StorableEnvironment env = null;
		if(context instanceof IConfiguration){
			IConfiguration newCfg = (IConfiguration)context;
			if(fCurrentCfg == newCfg && fConfigurationVariables != null){
				env = fConfigurationVariables;
			}
			else{
				env = loadEnvironment(newCfg);
				if(env != null){
					if(fConfigurationVariables != null)
						try{
							storeEnvironment(fConfigurationVariables,fCurrentCfg,false);
						} catch(CoreException e){
						}
					fConfigurationVariables = env;
					fCurrentCfg = newCfg;
				}
			}
		}
		else if(context instanceof IManagedProject){
			IManagedProject newProj = (IManagedProject)context;
			if(fCurrentProj == newProj && fProjectVariables != null){
				env = fProjectVariables;
			}
			else{
				env = loadEnvironment(newProj);
				if(env != null){
					if(fProjectVariables != null)
						try{
							storeEnvironment(fProjectVariables,fCurrentProj,false);
						} catch(CoreException e){
						}
						fProjectVariables = env; 
						fCurrentProj = newProj;
				}
			}
		}
		else if(context instanceof IWorkspace){
			if(fWorkspaceVariables == null)
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
			String ids[] = prefNode.childrenNames();
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
		if(fConfigurationVariables != null && fCurrentCfg != null ){
			try{
				storeEnvironment(fConfigurationVariables,fCurrentCfg,force);
			} catch(CoreException e){
				
			}
		}

		if(fProjectVariables != null && fCurrentProj != null ){
			try{
				storeEnvironment(fProjectVariables,fCurrentProj,force);
			} catch(CoreException e){
				
			}
		}
		
		if(fWorkspaceVariables != null){
			try{
				storeEnvironment(fWorkspaceVariables,ResourcesPlugin.getWorkspace(),force);
			} catch(CoreException e){
				
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
	
	protected void setRebuildStateForContext(Object context){
		if(context == null)
			return;
		if(context instanceof IConfiguration){
			((IConfiguration)context).setRebuildState(true);
		}
		else if(context instanceof IManagedProject){
			IConfiguration cfgs[] = ((IManagedProject)context).getConfigurations();
			for(int i = 0; i < cfgs.length; i++){
				cfgs[i].setRebuildState(true);
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
							cfgs[j].setRebuildState(true);
						}
					}
				}
			}
		}
			
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
