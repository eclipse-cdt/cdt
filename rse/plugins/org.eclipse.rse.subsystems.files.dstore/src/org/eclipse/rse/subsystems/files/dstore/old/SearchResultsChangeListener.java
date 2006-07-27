/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.files.dstore.old;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.dstore.extra.internal.extra.IDomainListener;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteSearchConstants;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResult;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResultsContentsType;
import org.eclipse.rse.subsystems.files.dstore.model.DStoreFile;
import org.eclipse.rse.subsystems.files.dstore.model.DStoreSearchResult;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class SearchResultsChangeListener implements IDomainListener, ICommunicationsListener {


	private Shell _shell;
	private UniversalSearchResultConfigurationImpl _searchConfig;
	private DataElement _status;
	private IRemoteFileSubSystem _subsys;
	private DataStore _dataStore;
	private IRemoteFileContext _defaultContext;
	private IRemoteFileContext _currentContext;
	//private IRemoteFileFactory _factory;
	private long _lastUpdateTime;
	private boolean _disconnected = false;
	private boolean _cancelled = false;

	private static int MAX_RESULTS = 10000;

	public SearchResultsChangeListener(Shell shell, UniversalSearchResultConfigurationImpl searchConfig) {
		_shell = shell;
		_searchConfig = searchConfig;
		
		IRemoteFile rmtFile = (IRemoteFile)_searchConfig.getSearchTarget();
		_subsys = rmtFile.getParentRemoteFileSubSystem();
		
		_status = _searchConfig.getStatusObject();
		_dataStore = _status.getDataStore();
		
		_dataStore.getDomainNotifier().addDomainListener(this);
		_subsys.getConnectorService().addCommunicationsListener(this);
		
		_lastUpdateTime = 0;

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();

		//_factory = ((FileServiceSubSystem)_subsys).getRemoteFileFactory();

		if (_status.getValue().equals("done")) {
			setDone();
		}
		else if (_status.getValue().equals("cancelled")) {
			_cancelled = true;
			setDone();
		}
	}

	public IHostSearchResultConfiguration getSearchResults() {
		return _searchConfig;
	}

	public DataElement getStatus() {
		return _status;
	}

	/**
	 * @see IDomainListener#listeningTo(DomainEvent)
	 */
	public boolean listeningTo(DomainEvent event) {
		
		
		DataElement parent = (DataElement)event.getParent();
		if (_status == null) {
			return false;
		}
		else if (_status == parent) {
			return true;
		}
		else if (_status.getParent() == parent)
		{
			return true;
		}

		return false;
	}

	/**
	 * @see IDomainListener#domainChanged(DomainEvent)
	 */
	public void domainChanged(DomainEvent event) {

		if (!_subsys.isConnected())
		{
			_disconnected = true;
			setDone();
			return;
		}
		
		if (_status.getValue().equals("done")) {
			setDone();
		}
		else if (_status.getValue().equals("cancelled")) {
			_cancelled = true;
			setDone();
		}
		else {
			long currentTime = System.currentTimeMillis();
			long deltaTime = currentTime - _lastUpdateTime;
			
			// update frequency is inversely proportional to the number of search results
			if (deltaTime > _searchConfig.getResultsSize() && _searchConfig.getResultsSize() < MAX_RESULTS) {
				_lastUpdateTime = currentTime;
				handleSearchResults();
			}
		}
	}

	private void handleSearchResults() {
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();

		// append new results to existing results
		List results = _status.getNestedData();
		if (results != null)
		{
			int totalSize = results.size();
			int currentSize = _searchConfig.getResultsSize();
	
			for (int loop = currentSize; loop < totalSize && loop < MAX_RESULTS; loop++) {
				
				DataElement result = (DataElement)results.get(loop);
	
				if (_defaultContext == null) {
					_defaultContext = new RemoteFileContext(_subsys, null, null);
					_currentContext = _defaultContext;
				}
	
				if (result.getName().length() > 0) {
					
					// for defect 47414, this code changes the context used for creating IRemoteFiles
					char slash = '/';
					
					if (_subsys.getHost().getSystemType().equals("Windows")) {
						slash = '\\';
					}
					
					String parentPath = result.getValue();
					
					// check if parent is an archive
					boolean isParentArchive = ArchiveHandlerManager.getInstance().isRegisteredArchive(parentPath);
	
					int lastSlash = parentPath.lastIndexOf(slash);
					
					if (lastSlash > 0) {
						
						String parentName = parentPath.substring(lastSlash + 1, parentPath.length());
						String parentRoot = parentPath.substring(0, lastSlash);
	
						if ((_currentContext.getParentRemoteFile() == null) || !_currentContext.getParentRemoteFile().getAbsolutePath().equals(parentPath)) {
							
							// change the current context
							DataElement parentElement = null;
							
							if (ArchiveHandlerManager.isVirtual(parentRoot)) {
								parentElement = result.getDataStore().createObject(null, IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR, parentName);
							}
							else {
								
								if (!isParentArchive) {
									parentElement = result.getDataStore().createObject(null, IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR, parentName);
								}
								else {
									parentElement = result.getDataStore().createObject(null, IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR, parentName);
								}
							}
	
							parentElement.setAttribute(DE.A_VALUE, parentRoot);
	
							try
							{
								IRemoteFile remoteFileParent = _subsys.getRemoteFileObject(parentPath);
									//_factory.createRemoteFile(_defaultContext, parentElement, false, true);
								// DKM - need to see why we're not checking the cache first...
								//((RemoteFileSubSystemImpl)_subsys).cacheRemoteFile(remoteFileParent, parentPath);
								_currentContext = new RemoteFileContext(_subsys, remoteFileParent, null);
							}
							catch (Exception e)
							{
								
							}
						}
					}
					else {
						_currentContext = _defaultContext;
					}
	
					if (!_subsys.isConnected())
					{
						_disconnected = true;
						setDone();
						return;
					}
					
					IRemoteFile remoteFile = null;
					String remotePath = parentPath + slash + result.getName();
					if (_subsys instanceof FileServiceSubSystem)
					{
						remoteFile = (IRemoteFile)((FileServiceSubSystem)_subsys).getCachedRemoteFile(remotePath);
					}
					
					if (remoteFile == null)
					{
						try
						{
							remoteFile = _subsys.getRemoteFileObject(remotePath);
								//_factory.createRemoteFile(_currentContext, result, false, true);
							((RemoteFileSubSystem)_subsys).cacheRemoteFile(remoteFile, remotePath);
							((RemoteFile)remoteFile).setParentRemoteFile(_currentContext.getParentRemoteFile());
						}
						catch (Exception e)
						{
							
						}
					}
					
					if (remoteFile != null) {
						
						if (result.getNestedSize() > 0) {
						    
							boolean isWindows = _subsys.getHost().getSystemType().equals("Windows");
							char separator = isWindows ? '\\' : '/';
							List contents = new ArrayList();
							
							// go through search results
							for (int i = 0; i < result.getNestedSize(); i++) {
							    
								DataElement resultElement = result.get(i);
								
								// search result object
								DStoreSearchResult searchResult = new DStoreSearchResult(_searchConfig, remoteFile, resultElement, _searchConfig.getSearchString());
								
								// set the name which should be the line
								searchResult.setText(resultElement.getName());
								
								String source = resultElement.getSource().replace('\\', separator).replace('/', separator);
								String path = source;
								String lineNumStr = null;
								int colonIndex = source.indexOf(":");
								if (colonIndex > 2)
								{
									path = source.substring(0, colonIndex);
									lineNumStr = source.substring(colonIndex);
								}
	
								int lineNum = 0;
								
								if (lineNumStr != null && lineNumStr.length() > 0) {
									Integer lineLocation = new Integer(lineNumStr);
									lineNum = lineLocation.intValue();
								}
								
								// set the line number
								searchResult.setLine(lineNum);
								
								// set the index
								searchResult.setIndex(i);
	
								// add the search result
								contents.add(searchResult);
							}
							
							// set search results to contents of remote file
							((DStoreFile)remoteFile).setContents(RemoteSearchResultsContentsType.getInstance(), _searchConfig.getSearchString().toString(), contents.toArray());
						}
						
						// add remote file to search result set
						_searchConfig.addResult(remoteFile);
					}
				}
			}
			
			if (totalSize >= MAX_RESULTS) {
				RemoteSearchResult warning = new RemoteSearchResult(_searchConfig, _searchConfig, _searchConfig.getSearchString());
				warning.setLine(0);
				warning.setText(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_LIST_CANCELLED).getLevelOneText());
				_searchConfig.addResult(warning);
			}
	
			registry.fireEvent(new SystemResourceChangeEvent(_searchConfig, ISystemResourceChangeEvents.EVENT_REFRESH, null));
		}
	}

	/**
	 * Indicates search is done.
	 */
	public void setDone() {
		
		_status.getDataStore().getDomainNotifier().removeDomainListener(this);
		
		_subsys.getConnectorService().removeCommunicationsListener(this);

		if (!_disconnected) {
			handleSearchResults();
			
			if (_cancelled) {
				_searchConfig.setStatus(IRemoteSearchConstants.CANCELLED);
			}
			else {
				_searchConfig.setStatus(IRemoteSearchConstants.FINISHED);
			}
		}
		else {
			_searchConfig.setStatusObject(null);
			_searchConfig.setStatus(IRemoteSearchConstants.DISCONNECTED);
		}

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		registry.fireEvent(new SystemResourceChangeEvent(_searchConfig, ISystemResourceChangeEvents.EVENT_SEARCH_FINISHED, null));
	}

	/**
	 * @see IDomainListener#getShell()
	 */
	public Shell getShell() {
		return _shell;
	}

	/**
	 * 
	 */
	public boolean wasCancelled() {
		return _cancelled;
	}

	/**
	 * @see ICommunicationsListener#communicationsStateChange(CommunicationsEvent)
	 */
	public void communicationsStateChange(CommunicationsEvent e) {
		
		int connState = e.getState();
		
		if (connState == CommunicationsEvent.CONNECTION_ERROR || connState == CommunicationsEvent.AFTER_DISCONNECT) {
			_disconnected = true;

			Display.getDefault().asyncExec(new Runnable() {
				
				public void run() {
					setDone();
				}
			});
		}
	}

	/**
	 * @see ICommunicationsListener#isPassiveCommunicationsListener()
	 */
	public boolean isPassiveCommunicationsListener() {
		return true;
	}
}