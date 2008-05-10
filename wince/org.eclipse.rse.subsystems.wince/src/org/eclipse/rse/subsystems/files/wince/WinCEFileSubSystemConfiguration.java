/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SftpFileSubSystemConfiguration
 *******************************************************************************/
package org.eclipse.rse.subsystems.files.wince;

import java.util.Vector;

import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.connectorservice.wince.WinCEConnectorService;
import org.eclipse.rse.internal.connectorservice.wince.WinCEConnectorServiceManager;
import org.eclipse.rse.internal.services.wince.IWinCEService;
import org.eclipse.rse.internal.services.wince.files.WinCEFileService;
import org.eclipse.rse.internal.subsystems.files.wince.WinCEFileAdapter;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;

public class WinCEFileSubSystemConfiguration extends FileServiceSubSystemConfiguration {

  IHostFileToRemoteFileAdapter hostFileAdapter;
  
  public IFileService createFileService(IHost host) {
    WinCEConnectorService connectorService = (WinCEConnectorService) getConnectorService(host);
    return new WinCEFileService(connectorService);
  }

  public ISubSystem createSubSystemInternal(IHost host) {
    WinCEConnectorService connectorService = (WinCEConnectorService) getConnectorService(host);
    return new WinCEFileServiceSubSystem(host, connectorService, getFileService(host), getHostFileAdapter(), getSearchService(host));
  }

  public IHostSearchResultConfiguration createSearchConfiguration(IHost host,
      IHostSearchResultSet resultSet, Object searchTarget,
      SystemSearchString searchString) {
    return null;
  }

  public ISearchService createSearchService(IHost host) {
    return null;
  }

  protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr) {
    ISystemFilterPool pool = null;
    try {
      pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true);
      
      // "My Home" filter
      Vector filterStrings = new Vector();
      RemoteFileFilterString myHomeFilterString = new RemoteFileFilterString(this);
      myHomeFilterString.setPath("\\My Documents\\"); //$NON-NLS-1$
      filterStrings.add(myHomeFilterString.toString());
      mgr.createSystemFilter(pool, "My Home", filterStrings); //$NON-NLS-1$
      //filter.setNonChangable(true);
      //filter.setSingleFilterStringOnly(true);
      
      // "Root Files" filter
      filterStrings = new Vector();
      RemoteFileFilterString rootFilesFilterString = new RemoteFileFilterString(this);
      filterStrings.add(rootFilesFilterString.toString());          
      mgr.createSystemFilter(pool, "Root", filterStrings); //$NON-NLS-1$
    } catch (Exception exc) {
      SystemBasePlugin.logError("Error creating default filter pool",exc); //$NON-NLS-1$
    }
    return pool;
  }

  public IHostFileToRemoteFileAdapter getHostFileAdapter() {
    if (hostFileAdapter == null) {
      hostFileAdapter = new WinCEFileAdapter();
    }
    return hostFileAdapter;
  }

  public ILanguageUtilityFactory getLanguageUtilityFactory(
      IRemoteFileSubSystem ss) {
    return null;
  }

  public boolean supportsSearch() {
    return false;
  }

  public boolean supportsArchiveManagement() {
    return false;
  }

  public IConnectorService getConnectorService(IHost host) {
    return WinCEConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
  }

  public Class getServiceImplType() {
    return IWinCEService.class;
  }

  public void setConnectorService(IHost host, IConnectorService connectorService) {
    WinCEConnectorServiceManager.getInstance().setConnectorService(host, getServiceImplType(), connectorService);
  }

}
