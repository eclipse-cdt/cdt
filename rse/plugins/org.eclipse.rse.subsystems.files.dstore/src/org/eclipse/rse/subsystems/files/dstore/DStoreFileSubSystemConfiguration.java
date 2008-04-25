/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Kushal Munir (IBM) - [189352] Set whether file service is Unix-style system or not
 * David McKnight   (IBM)        - [206755] upload and download buffer should be in kbytes, not bytes
 * David McKnight   (IBM)        - [216252] use SimpleSystemMessage instead of getMessage()
 * David McKnight     (IBM)    - [227406][api][dstore] need apis for getting buffer size in IDataStoreProvider
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.dstore;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorServiceManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.internal.services.dstore.files.DStoreFileService;
import org.eclipse.rse.internal.services.dstore.search.DStoreSearchService;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.internal.subsystems.files.dstore.DStoreFileAdapter;
import org.eclipse.rse.internal.subsystems.files.dstore.DStoreFileSubSystemSearchResultConfiguration;
import org.eclipse.rse.internal.subsystems.files.dstore.DStoreLanguageUtilityFactory;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.dstore.IDStoreService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;

public class DStoreFileSubSystemConfiguration extends FileServiceSubSystemConfiguration
{
	protected boolean _isWindows;
	protected IHostFileToRemoteFileAdapter _hostFileAdapter;
	public DStoreFileSubSystemConfiguration()
	{
		super();
		_isWindows = false;
		setIsUnixStyle(!_isWindows);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#isFactoryFor(java.lang.Class)
	 */
	public boolean isFactoryFor(Class subSystemType)
	{
		boolean isFor = FileServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration#supportsArchiveManagement()
	 */
	public boolean supportsArchiveManagement()
	{
		return true;
	}

	/**
	 * Instantiate and return an instance of OUR subsystem. Do not populate it
	 * yet though!
	 *
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost host)
	{
		DStoreConnectorService connectorService = (DStoreConnectorService)getConnectorService(host);
		ISubSystem subsys = new FileServiceSubSystem(host, connectorService, getFileService(host), getHostFileAdapter(), getSearchService(host));
		return subsys;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsFileTypes()
	 */
	public boolean supportsFileTypes()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsSearch()
	 */
	public boolean supportsSearch()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsEnvironmentVariablesPropertyPage()
	 */
	public boolean supportsEnvironmentVariablesPropertyPage()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsFilters()
	 */
	public boolean supportsFilters()
	{
		return true;
	}

	public void setConnectorService(IHost host, IConnectorService connectorService)
	{
		DStoreConnectorServiceManager.getInstance().setConnectorService(host, getServiceImplType(), connectorService);
	}

	public IConnectorService getConnectorService(IHost host)
	{
		return DStoreConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
	}

	public IFileService createFileService(IHost host)
	{
		final IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		DStoreConnectorService connectorService = (DStoreConnectorService)getConnectorService(host);
		final DStoreFileService service = new DStoreFileService(connectorService, RemoteFileUtility.getSystemFileTransferModeRegistry());
		service.setIsUnixStyle(isUnixStyle());

		int dvalue = store.getInt(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE) *  IUniversalDataStoreConstants.KB_IN_BYTES;
		if (dvalue == 0)
			dvalue = IUniversalDataStoreConstants.BUFFER_SIZE;
		service.setBufferDownloadSize(dvalue);


		int uvalue = store.getInt(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE) *  IUniversalDataStoreConstants.KB_IN_BYTES;
		if (uvalue == 0)
			uvalue = IUniversalDataStoreConstants.BUFFER_SIZE;
		service.setBufferUploadSize(uvalue);


 		// Listen to preference changes
		IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event){
				String propertyName = event.getProperty();
				if (propertyName.equals(ISystemFilePreferencesConstants.DOWNLOAD_BUFFER_SIZE)){
					int value = Integer.parseInt((String)event.getNewValue());
					service.setBufferDownloadSize(value);
				}
				else if (propertyName.equals(ISystemFilePreferencesConstants.UPLOAD_BUFFER_SIZE)){
					int value = Integer.parseInt((String)event.getNewValue());
					service.setBufferDownloadSize(value);
				}
			}
		};

		Preferences pstore = RSEUIPlugin.getDefault().getPluginPreferences();
		pstore.addPropertyChangeListener(preferenceListener);

		return service;
	}

	public ISearchService createSearchService(IHost host)
	{
		DStoreConnectorService connectorService = (DStoreConnectorService)getConnectorService(host);
		return new DStoreSearchService(connectorService);
	}

	public IHostFileToRemoteFileAdapter getHostFileAdapter()
	{
		if (_hostFileAdapter == null)
		{
			_hostFileAdapter =  new DStoreFileAdapter();
		}
		return _hostFileAdapter;
	}

	public IHostSearchResultConfiguration createSearchConfiguration(IHost host, IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString)
	{
		DStoreFileSubSystemSearchResultConfiguration config = new DStoreFileSubSystemSearchResultConfiguration(resultSet, searchTarget, searchString, getSearchService(host), getHostFileAdapter());
		resultSet.addSearchConfiguration(config);
		return config;
	}

	public ILanguageUtilityFactory getLanguageUtilityFactory(IRemoteFileSubSystem ss)
	{
		return DStoreLanguageUtilityFactory.getInstance(ss);
	}

	public Class getServiceImplType()
	{
		return IDStoreService.class;
	}
}
