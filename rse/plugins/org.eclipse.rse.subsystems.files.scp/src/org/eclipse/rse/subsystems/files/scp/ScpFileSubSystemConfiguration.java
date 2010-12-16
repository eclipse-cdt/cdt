/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 * Nikita Shulga (Mentor Graphics) - adapted from SftpFileSubSystemConfiguration.
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.scp;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.connectorservice.ssh.SshConnectorService;
import org.eclipse.rse.internal.connectorservice.ssh.SshConnectorServiceManager;
import org.eclipse.rse.internal.services.ssh.ISshService;
import org.eclipse.rse.internal.services.ssh.files.scp.ScpFileService;
import org.eclipse.rse.internal.subsystems.files.scp.ScpFileAdapter;

import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

@SuppressWarnings("restriction")
public class ScpFileSubSystemConfiguration extends
		FileServiceSubSystemConfiguration {

	protected IHostFileToRemoteFileAdapter _hostFileAdapter;

	public ScpFileSubSystemConfiguration() {
		super();
		setIsUnixStyle(true);
	}

	@Override
	public ISubSystem createSubSystemInternal(IHost host) {
		IConnectorService connectorService = getConnectorService(host);
		IFileService fileService = getFileService(host);
		ISubSystem subsys = new FileServiceSubSystem(host, connectorService,
				fileService, getHostFileAdapter(), getSearchService(host));
		return subsys;
	}

	public IFileService createFileService(IHost host) {
		SshConnectorService connectorService = (SshConnectorService) getConnectorService(host);
		return new ScpFileService(connectorService);
	}

	public IHostFileToRemoteFileAdapter getHostFileAdapter() {
		if (_hostFileAdapter == null) {
			_hostFileAdapter = new ScpFileAdapter();
		}
		return _hostFileAdapter;
	}

	public ILanguageUtilityFactory getLanguageUtilityFactory(
			IRemoteFileSubSystem arg0) {
		return null;
	}

	public IConnectorService getConnectorService(IHost host) {
		return SshConnectorServiceManager.getInstance().getConnectorService(
				host, getServiceImplType());
	}

	public Class<?> getServiceImplType() {
		return ISshService.class;
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

	public ISearchService createSearchService(IHost arg0) {
		// no search service supported for ssh/scp at moment
		return null;
	}

	public IHostSearchResultConfiguration createSearchConfiguration(IHost arg0,
			IHostSearchResultSet arg1, Object arg2, SystemSearchString arg3) {
		return null;
	}

}
