/********************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186997] No deferred queries in Local Files
 * Kevin Doyle (IBM) - [199871] LocalFileService needs to implement getMessage()
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 * David Dykstal (IBM) - [222270] clean up interfaces in org.eclipse.rse.core.filters
 * David McKnight (IBM) - [280605] SystemTextEditor.isLocal() returns false for LocalFileSubSystemConfiguration
 * David McKnight   (IBM)        - [420798] Slow performances in RDz 9.0 with opening 7000 files located on a network driver.
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.local;

import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.connectorservice.local.LocalConnectorService;
import org.eclipse.rse.internal.connectorservice.local.LocalConnectorServiceManager;
import org.eclipse.rse.internal.services.local.ILocalService;
import org.eclipse.rse.internal.services.local.files.LocalFileService;
import org.eclipse.rse.internal.services.local.search.LocalSearchService;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.internal.subsystems.files.local.LocalLanguageUtilityFactory;
import org.eclipse.rse.internal.subsystems.files.local.LocalSearchResultConfiguration;
import org.eclipse.rse.internal.subsystems.files.local.model.LocalFileAdapter;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;


/**
 * Provides a factory for generating instances of the class
 * SampleFileSubSystem.
 */
public class LocalFileSubSystemConfiguration extends FileServiceSubSystemConfiguration 
{
	protected boolean _isWindows;
	protected IHostFileToRemoteFileAdapter _hostFileAdapter;
	public LocalFileSubSystemConfiguration() 
	{
		super();
		String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
	
		_isWindows = (osName.startsWith("windows")); //$NON-NLS-1$
		setIsUnixStyle(!_isWindows);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isFactoryFor(java.lang.Class)
	 */
	public boolean isFactoryFor(Class subSystemType) {
		boolean isFor = FileServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsArchiveManagement()
	 */
	public boolean supportsArchiveManagement() {
		return true;
	}
	
	/**
	 * Override from parent.
	 * Create the default filter pool when this factory is first created, and populate it with
	 *  default filters.
	 * <p>
	 * For local, what default filters we create we depends on the local operating system:
	 * <ul>
	 *  <li>_isWindows: "Drives" listing all the local drives
	 *  <li>Others: "Root files" listing all the contents of the root drive
	 * </ul>
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"in createDefaultFilterPool for remote file subsystem factory");
		ISystemFilterPool pool = null;
		try {
		  // -----------------------------------------------------
		  // create a pool named filters
		  // -----------------------------------------------------      			  
		  pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user
		  if (pool == null) // hmmm, why would this happen?
		  {
			SystemBasePlugin.logError("Creating default filter pool "+getDefaultFilterPoolName(mgr.getName(), getId())+" for mgr "+mgr.getName()+" failed.",null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		  }
		  //System.out.println("Pool created");
		  // ---------------------------------------------------------------------------------------------
		  // create default filters in that pool iff this is the user's private profile we are creating...
		  // ---------------------------------------------------------------------------------------------
		  if (isUserPrivateProfile(mgr))
		  {
		      // Create 'My Home' filter for local (should apply to both _isWindows and linux clients)
	    	  String myHomeFilterName = SystemFileResources.RESID_FILTER_MYHOME;
	    	  RemoteFileFilterString myDocsFilterString = new RemoteFileFilterString(this);
	    	  myDocsFilterString.setPath(System.getProperty("user.home") + getSeparator()); //$NON-NLS-1$
	    	  String[] myDocsFilterStrings = new String[] {myDocsFilterString.toString()};
	    	  mgr.createSystemFilter(pool, myHomeFilterName, myDocsFilterStrings);
			  
			  
		      RemoteFileFilterString defaultFilterString = new RemoteFileFilterString(this);
		      if (!_isWindows)
		        defaultFilterString.setPath(getSeparator());
		      String[] filterStrings = new String[] {defaultFilterString.toString()};
		      //System.out.println("creating filter...");	
		      String filterName = null;
		      if (_isWindows)
		        filterName = SystemFileResources.RESID_FILTER_DRIVES;
		      else
		        filterName = SystemFileResources.RESID_FILTER_ROOTFILES;
		      mgr.createSystemFilter(pool, filterName, filterStrings);
		  }
		  else
		  {
		      RemoteFileFilterString defaultFilterString = new RemoteFileFilterString(this);
		      if (!_isWindows)
		        defaultFilterString.setPath(getSeparator());
			  String[] filterStrings = new String[] {defaultFilterString.toString()};
		      //System.out.println("creating filter...");	
		      String filterName = null;
		      if (_isWindows)
		        filterName = SystemFileResources.RESID_FILTER_DRIVES;
		      else
		        filterName = SystemFileResources.RESID_FILTER_ROOTFILES;
		      mgr.createSystemFilter(pool, filterName, filterStrings);
		  }
		} catch (Exception exc)
		{
			SystemBasePlugin.logError("Error creating default filter pool",exc); //$NON-NLS-1$
		}
		return pool;
	}
   
	/**
	 * Instantiate and return an instance of OUR subystem. 
	 * Do not populate it yet though!
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost host) 
	{
		LocalConnectorService connectorService = (LocalConnectorService)getConnectorService(host);
		ISubSystem subsys = new FileServiceSubSystem(host, connectorService, getFileService(host), getHostFileAdapter(), getSearchService(host));
		return subsys;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFileTypes()
	 */
	public boolean supportsFileTypes() {
		return false;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsSearch()
	 */
	public boolean supportsSearch() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsEnvironmentVariablesPropertyPage()
	 */
	public boolean supportsEnvironmentVariablesPropertyPage() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()
	 */
	public boolean supportsFilters() {
		return true;
	}
	
	/**
	 * Returns <code>false</code>.
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsSubSystemConnect()
	 */
	public boolean supportsSubSystemConnect() {
		return false;
	}

	public boolean supportsDeferredQueries() {
		//No need for deferred queries in Local, since these are always fast
		//return false;
		return true;
	}

	public IConnectorService getConnectorService(IHost host)
	{
		return LocalConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
	}
	
	public void setConnectorService(IHost host, IConnectorService connectorService)
	{
		LocalConnectorServiceManager.getInstance().setConnectorService(host, getServiceImplType(), connectorService);
	}
	
	public IFileService createFileService(IHost host)
	{
		return new LocalFileService(RemoteFileUtility.getSystemFileTransferModeRegistry());
	}
	
	public ISearchService createSearchService(IHost host)
	{
		return new LocalSearchService();
	}
	
	/**
	 * Creates a config and adds it to the result set.
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfigurationFactory#createSearchConfiguration(org.eclipse.rse.services.search.IHostSearchResultSet, java.lang.Object, org.eclipse.rse.services.clientserver.SystemSearchString)
	 */
	public IHostSearchResultConfiguration createSearchConfiguration(IHost host, IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString) 
	{
		IHostSearchResultConfiguration config = new LocalSearchResultConfiguration(resultSet, searchTarget, searchString, getSearchService(host));
		resultSet.addSearchConfiguration(config);
		return config;
	}
	
	public IHostFileToRemoteFileAdapter getHostFileAdapter()
	{
		if (_hostFileAdapter == null)
		{
			_hostFileAdapter =  new LocalFileAdapter();
		}
		return _hostFileAdapter;
	}

	public ILanguageUtilityFactory getLanguageUtilityFactory(IRemoteFileSubSystem ss)
	{
		return LocalLanguageUtilityFactory.getInstance(ss);
	}
	
	public Class getServiceImplType()
	{
		return ILocalService.class;
	}
	
    public String getEditorProfileID()
    {
    	return "universallocal"; //$NON-NLS-1$
    }

}