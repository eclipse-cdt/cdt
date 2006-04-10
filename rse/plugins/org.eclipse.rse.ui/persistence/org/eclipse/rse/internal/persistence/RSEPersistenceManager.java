/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.persistence;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.internal.filters.SystemFilterPoolManager;
import org.eclipse.rse.internal.persistence.dom.RSEDOMExporter;
import org.eclipse.rse.internal.persistence.dom.RSEDOMImporter;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemHostPool;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.persistence.DefaultRSEPersistenceProvider;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.RSEDOM;


public class RSEPersistenceManager implements IRSEPersistenceManager
{
	public static final int STATE_NONE = 0;
	public static final int STATE_IMPORTING = 1;
	public static final int STATE_EXPORTING = 2;
	
	private int _currentState = STATE_NONE;
	
	private RSEDOMExporter _exporter;
	private RSEDOMImporter _importer;
	private IRSEPersistenceProvider _persistenceProvider;
	
	public RSEPersistenceManager()
	{
		_exporter = RSEDOMExporter.getInstance();
		_importer = RSEDOMImporter.getInstance();
	}
	
	public void registerRSEPersistenceProvider(IRSEPersistenceProvider provider)
	{
		_persistenceProvider = provider;
	}
	
	public IRSEPersistenceProvider getRSEPersistanceProvider()
	{
		if (_persistenceProvider == null)
		{
			_persistenceProvider = new DefaultRSEPersistenceProvider();
		}
		return _persistenceProvider;
	}
	
	
	public boolean restore(ISystemProfileManager profileManager)
	{
		return load(profileManager);
	}

	/**
	 * Restore a profile of a given name from disk...
	 */
	protected ISystemProfile restoreProfile(ISystemProfileManager mgr, String name)
			throws Exception
	{
		/*
		 * FIXME String fileName = mgr.getRootSaveFileName(name); java.util.List
		 * ext = null;//FIXME
		 * getMOFHelpers().restore(SystemResourceManager.getProfileFolder(name),fileName);
		 *  // should be exactly one profile... Iterator iList = ext.iterator();
		 * SystemProfile profile = (SystemProfile)iList.next();
		 * mgr.initialize(profile, name); return profile;
		 */
		return null;
	}

	/**
	 * Save all profiles to disk
	 */
	public boolean commit(ISystemProfileManager profileManager)
	{
		
		ISystemProfile[] profiles = profileManager.getSystemProfiles();
		for (int idx = 0; idx < profiles.length; idx++)
		{
			try
			{
				commit(profiles[idx]);
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				System.out.println("Error saving profile " + profiles[idx]
						+ ": " + exc.getClass().getName() + " "
						+ exc.getMessage());
				return false;
			}
		}
		
		return true;
	}

	

	public boolean restore(ISystemHostPool connectionPool)
	{
	return false;
	}

	/**
	 * Restore a connection of a given name from disk...
	 */
	protected IHost restoreHost(ISystemHostPool hostPool, String connectionName)
			throws Exception
	{
		/*
		 * FIXME //System.out.println("in SystemConnectionPoolImpl#restore for
		 * connection " + connectionName); String fileName =
		 * getRootSaveFileName(connectionName);
		 * //System.out.println(".......fileName = " + fileName);
		 * //System.out.println(".......folderName = " +
		 * getConnectionFolder(connectionName).getName()); java.util.List ext =
		 * getMOFHelpers().restore(getConnectionFolder(connectionName),fileName);
		 *  // should be exactly one profile... Iterator iList = ext.iterator();
		 * SystemConnection connection = (SystemConnection)iList.next(); if
		 * (connection != null) { if
		 * (!connection.getAliasName().equalsIgnoreCase(connectionName)) {
		 * SystemPlugin.logDebugMessage(this.getClass().getName(),"Incorrect
		 * alias name found in connections.xmi file for " + connectionName+".
		 * Name was reset"); connection.setAliasName(connectionName); // just in
		 * case! } internalAddConnection(connection); } return connection;
		 */
		return null;
	}

	public boolean commit(ISystemHostPool connectionPool)
	{
		if (connectionPool.isDirty())
		{
			commit(connectionPool.getSystemProfile());
			connectionPool.setDirty(false);
		}
		/*
		Host[] connections = connectionPool.getHosts();
		for (int idx = 0; idx < connections.length; idx++)
		{
			if (!saveHost(connectionPool, connections[idx]))
			{
				return false;
			}
		}
		return true;
		*/
		return false; // all persistance should be at profile level
	}

	public boolean commit(ISystemFilterPoolManager filterPoolManager)
	{
		if (filterPoolManager.isDirty())
		{
			commit(filterPoolManager.getSystemProfile());
			filterPoolManager.setDirty(false);
		}
		return false;
	}

	public boolean commit(ISystemFilterPool filterPool)
	{
		if (filterPool.isDirty())
		{
			commit(filterPool.getSystemFilterPoolManager().getSystemProfile());
			filterPool.setDirty(false);
		}
		return false;
	}

	public boolean restore(ISystemFilterPool filterPool)
	{
		System.out.println("restore filterpool");
		// TODO Auto-generated method stub
		return false;
	}


	 public boolean commit(ISystemFilter filter)
   {
		 System.out.println("commit filter");
		 /*
		 if (filter.isDirty())
		 {
			 System.out.println("saving filter: "+filter.getName());
			 filter.setDirty(false);
		 }
		 */
	   	/* FIXME
	       //initMOF();  assume caller did this!
	       String fileName = getRootSaveFileName(this);
	       IFolder folder = getFolder(this);
	   	getMOFHelpers().save(folder,fileName, this);        
	   	*/
	   	return false;
   }

	public ISystemFilterPool restoreFilterPool(String name)
	{
		System.out.println("restore filter pool "+name);
		// FIXME
		return null;
	}

	public boolean commit(ISubSystem subSystem)
	{
		
		if (subSystem.isDirty())
		{
		//	System.out.println("updated " + subSystem.getName());
			try
			{
				// commit everything for now
				ISystemProfileManager mgr = SystemPlugin.getTheSystemRegistry().getSystemProfileManager();
				commit(mgr);
				subSystem.setDirty(false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return true;
		}
		
		/*
		// FIXME
		if (subSystem.isDirty())
		{
			System.out.println("saving subsystem: "+subSystem.getName());
		}
		*/
		return false;
	}
	
	
	/**
     * Restore the filter pools from disk.
     * @param logger The logging object to log errors to
     * @param mgrFolder folder containing filter pool folders, or single file for that save policy
     * @param name the name of the manager to restore. File name is derived from it when saving to one file.
     * @param savePolicy How to persist filters. See SystemFilterConstants.
     * @param namingPolicy Naming prefix information for persisted data file names.
     * @return the restored manager, or null if it does not exist. If anything else went
     *  wrong, an exception is thrown.
     */
    public ISystemFilterPoolManager restoreFilterPoolManager(ISystemProfile profile, Logger logger, ISystemFilterPoolManagerProvider caller,  String name)
    {
    
    	ISystemFilterPoolManager mgr = SystemFilterPoolManager.createManager(profile);
    	 ((SystemFilterPoolManager)mgr).initialize(logger,caller,name); // core data
    	 
        return mgr;	
    }
    
    /**
	 * Attempt to save single profile to disk.
	 */
	public boolean commit(ISystemProfile profile)
	{
		if (profile != null)
		{
			String name = profile.getName();
			return save(profile, false);			
		}
		return false;
	}

	/**
	 * Loads and restores RSE artifacts from the last session
	 * @param profileManager
	 * @return
	 */
    public boolean load(ISystemProfileManager profileManager)
    {
    	boolean successful = true;
    	if (isExporting() || isImporting())
    	{
    		successful = false;
    	}
    	else
    	{
    		_currentState = STATE_IMPORTING;
			IProject project = SystemResourceManager.getRemoteSystemsProject();
	
			try
			{
				project.refreshLocal(IResource.DEPTH_ONE, null);
				IResource[] folders = project.members();
				for (int f = 0; f < folders.length; f++)
				{
					
					if (folders[f] instanceof IFolder)
					{
						IFolder folder = (IFolder)folders[f];
						IResource[] members = folder.members();
						for (int i = 0; i < members.length; i++)
						{
							IResource member = members[i];
							
							if (member instanceof IFile && member.getFileExtension().equals("rsedom"))
							{
								String name = member.getName();
								String domName = member.getName().substring(0, name.length() - 7);
								// read and restore dom
								RSEDOM dom = importRSEDOM(profileManager, domName);
								if (dom != null)
								{
									ISystemProfile restoredProfile = _importer.restoreProfile(profileManager, dom);
									if (restoredProfile == null)
									{
										successful = false;
									}
								}
								else
								{
									successful = false;
								}
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			_currentState = STATE_NONE;
    	}
    	return successful;
    }
    
    /**
     * Saves the RSE artifacts from this session
     */
	public boolean save(ISystemProfile profile, boolean force)
	{
		boolean result = false;
		if (!isImporting() && !isExporting())
		{
			
			RSEDOM dom = exportRSEDOM(profile, force);
			if (dom.needsSave() && !dom.saveScheduled())
			{
		
				IProject project = SystemResourceManager.getRemoteSystemsProject();
					
				SaveRSEDOMJob job = new SaveRSEDOMJob(this, dom, getRSEPersistanceProvider());
				//job.setRule(project);
				job.schedule();
				dom.markSaveScheduled();
			}
			else
			{
				System.out.println("no save required");
				result = true;
			}
		}
		return result;
	}    
	
	public boolean isExporting()
	{
		return _currentState == STATE_EXPORTING;
	}
	
	public boolean isImporting()
	{
		return _currentState == STATE_IMPORTING;
	}
	
	public void setState(int state)
	{
		_currentState = state;
	}

	public RSEDOM exportRSEDOM(ISystemProfile profile, boolean force)
	{
		RSEDOM dom = null;
		_currentState = STATE_EXPORTING;
		dom = _exporter.createRSEDOM(profile, force);

		return dom;
	}
	
	public RSEDOM importRSEDOM(ISystemProfileManager profileManager, String domName)
	{
		RSEDOM dom = getRSEPersistanceProvider().loadRSEDOM(profileManager, domName, null);
		return dom;
	}


	public boolean commit(IHost host) 
	{
		return commit(host.getSystemProfile());
	}
	
 

}