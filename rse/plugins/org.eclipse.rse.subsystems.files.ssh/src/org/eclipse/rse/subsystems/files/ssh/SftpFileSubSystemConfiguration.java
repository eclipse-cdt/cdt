/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.ssh;

import org.eclipse.rse.connectorservice.ssh.SshConnectorService;
import org.eclipse.rse.connectorservice.ssh.SshConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.services.ssh.ISshService;
import org.eclipse.rse.services.ssh.files.SftpFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

public class SftpFileSubSystemConfiguration extends FileServiceSubSystemConfiguration {

	protected IHostFileToRemoteFileAdapter _hostFileAdapter;

	public SftpFileSubSystemConfiguration() {
		super();
		setIsUnixStyle(true);
	}

	public boolean isFactoryFor(Class subSystemType) {
		boolean isFor = FileServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}
	
	public ISubSystem createSubSystemInternal(IHost host) {
		SshConnectorService connectorService = (SshConnectorService)getConnectorService(host);
		ISubSystem subsys = new FileServiceSubSystem(host, connectorService, getFileService(host), getHostFileAdapter(), getSearchService(host));
		return subsys;
	}

	public IConnectorService getConnectorService(IHost host) {
		return SshConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
	}
	
	public void setConnectorService(IHost host, IConnectorService connectorService) {
		SshConnectorServiceManager.getInstance().setConnectorService(host, getServiceImplType(), connectorService);
	}
	
	public IFileService createFileService(IHost host) {
		SshConnectorService connectorService = (SshConnectorService)getConnectorService(host);
		return new SftpFileService(connectorService);
	}
	
	public ISearchService createSearchService(IHost host) {
		// no search service supported for ssh/sftp at moment
		return null;
	}
	
	public IHostFileToRemoteFileAdapter getHostFileAdapter() {
		if (_hostFileAdapter == null) {
			_hostFileAdapter =  new SftpFileAdapter();
		}
		return _hostFileAdapter;
	}

	public Class getServiceImplType() {
		return ISshService.class;
	}

	public IHostSearchResultConfiguration createSearchConfiguration(IHost host, IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString) {
		return null;
	}

	public ILanguageUtilityFactory getLanguageUtilityFactory(IRemoteFileSubSystem ss) {
		return null;
	}
	
	public boolean supportsArchiveManagement() {
		return false;
	}

	public boolean supportsFileTypes() {
		return false;
	}

	public boolean supportsSearch() {
		return false;
	}

	public boolean supportsEnvironmentVariablesPropertyPage() {
		return false;
	}

	public boolean supportsFilters() {
		return true;
	}

}
