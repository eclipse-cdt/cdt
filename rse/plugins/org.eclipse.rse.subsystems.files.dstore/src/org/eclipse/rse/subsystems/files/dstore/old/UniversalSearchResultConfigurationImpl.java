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

package org.eclipse.rse.subsystems.files.dstore.old;

import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResultConfiguration;

import org.eclipse.dstore.core.model.DataElement;

public class UniversalSearchResultConfigurationImpl extends RemoteSearchResultConfiguration {
	
	protected DataElement statusObject;

	/**
	 * Constructor to create a universal search result configuration.
	 * @param resultSet the parent result set.
	 * @param searchObject the search target.
	 * @param string the search string.
	 */
	public UniversalSearchResultConfigurationImpl(IHostSearchResultSet resultSet, Object searchObject, SystemSearchString string) {
		super(resultSet, searchObject, string);
	}
	
	public void setStatusObject(DataElement statusObject) {
		this.statusObject = statusObject;
	}
	
	public DataElement getStatusObject() {
		return statusObject;
	}
	
	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#cancel()
	 */
	public void cancel() {
		
		if (getStatus() != RUNNING) {
			super.cancel();
			return;
		}
		
		IRemoteFile remoteFile = (IRemoteFile)getSearchTarget();
		IRemoteFileSubSystem subsys = remoteFile.getParentRemoteFileSubSystem();
		subsys.cancelSearch(this);
	}
}