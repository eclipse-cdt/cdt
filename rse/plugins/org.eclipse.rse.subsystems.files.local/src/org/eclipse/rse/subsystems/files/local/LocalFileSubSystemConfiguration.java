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

package org.eclipse.rse.subsystems.files.local;

import java.util.Vector;

import org.eclipse.rse.connectorservice.local.LocalConnectorService;
import org.eclipse.rse.connectorservice.local.LocalConnectorServiceManager;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.internal.model.Host;
import org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.local.ILocalService;
import org.eclipse.rse.services.local.files.LocalFileService;
import org.eclipse.rse.services.local.search.LocalSearchService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.local.model.LocalFileAdapter;


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
		String osName = System.getProperty("os.name").toLowerCase();
	
		_isWindows = (osName.startsWith("windows"));
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
			SystemBasePlugin.logError("Creating default filter pool "+getDefaultFilterPoolName(mgr.getName(), getId())+" for mgr "+mgr.getName()+" failed.",null);
			return null;
		  }
		  //System.out.println("Pool created");
		  // ---------------------------------------------------------------------------------------------
		  // create default filters in that pool iff this is the user's private profile we are creating...
		  // ---------------------------------------------------------------------------------------------
		  if (isUserPrivateProfile(mgr))
		  {
		      Vector filterStrings = new Vector();
		      RemoteFileFilterString defaultFilterString = new RemoteFileFilterString(this);
		      if (!_isWindows)
		        defaultFilterString.setPath(getSeparator());
		      filterStrings.add(defaultFilterString.toString());
		      //System.out.println("creating filter...");	
		      String filterName = null;
		      if (_isWindows)
		        filterName = SystemFileResources.RESID_FILTER_DRIVES;
		      else
		        filterName = SystemFileResources.RESID_FILTER_ROOTFILES;
		      mgr.createSystemFilter(pool, filterName, filterStrings);
		      
		      // Create 'My Home' filter for local (should apply to both _isWindows and linux clients)
	    	  filterName = SystemFileResources.RESID_FILTER_MYHOME;
	    	  RemoteFileFilterString myDocsFilterString = new RemoteFileFilterString(this);
	    	  myDocsFilterString.setPath(System.getProperty("user.home") + getSeparator());
	    	  Vector myDocsFilterStrings = new Vector();
	    	  myDocsFilterStrings.add(myDocsFilterString.toString());
	    	  mgr.createSystemFilter(pool, filterName, myDocsFilterStrings);

		      //System.out.println("filter created");		  
		      // -----------------------------------------------------
		      // add a default named filter for integrated file system    	
		      // -----------------------------------------------------      	
		      //filterStrings = new Vector();
		      //filterStrings.add(new AS400IFSFilterString().toString());
		      //mgr.createSystemFilter(pool,rb.getString(IAS400Constants.RESID_IFS_LIST),filterStrings);
		  }
		} catch (Exception exc)
		{
			SystemBasePlugin.logError("Error creating default filter pool",exc);
		}
		return pool;
	}
   
	/**
	 * Instantiate and return an instance of OUR subystem. 
	 * Do not populate it yet though!
	 * @see org.eclipse.rse.core.subsystems.impl.SubSystemFactoryImpl#createSubSystemInternal(Host)
	 */
	public ISubSystem createSubSystemInternal(IHost host) 
	{
		LocalConnectorService connectorService = (LocalConnectorService)getConnectorService(host);
		ISubSystem subsys = new FileServiceSubSystem(host, connectorService, getFileService(host), getHostFileAdapter(), getSearchService(host));
		return subsys;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.subsystems.SubSystemFactory#supportsFileTypes()
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

	public IConnectorService getConnectorService(IHost host)
	{
		return LocalConnectorServiceManager.getTheLocalSystemManager().getConnectorService(host, getServiceImplType());
	}
	
	public void setConnectorService(IHost host, IConnectorService connectorService)
	{
		LocalConnectorServiceManager.getTheLocalSystemManager().setConnectorService(host, getServiceImplType(), connectorService);
	}
	
	public IFileService createFileService(IHost host)
	{
		return new LocalFileService(SystemFileTransferModeRegistry.getDefault());
	}
	
	public ISearchService createSearchService(IHost host)
	{
		return new LocalSearchService();
	}
	
	/**
	 * Creates a config and adds it to the result set.
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfigurationFactory#createSearchConfiguration(org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultSet, java.lang.Object, org.eclipse.rse.services.clientserver.SystemSearchString)
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
}