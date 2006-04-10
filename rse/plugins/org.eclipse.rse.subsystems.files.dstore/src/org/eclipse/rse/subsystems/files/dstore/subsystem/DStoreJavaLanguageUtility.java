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

package org.eclipse.rse.subsystems.files.dstore.subsystem;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.internal.subsystems.files.core.AbstractJavaLanguageUtility;
import org.eclipse.rse.services.dstore.files.DStoreHostFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.dstore.model.DStoreFile;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;

/**
 * This class is the Java language utility for universal.
 */
public class DStoreJavaLanguageUtility extends AbstractJavaLanguageUtility implements IUniversalDataStoreConstants {

	/**
	 * Constructor.
	 * @param subsystem the subsystem with which the utility is associated.
	 * @param language the language.
	 */
	public DStoreJavaLanguageUtility(IRemoteFileSubSystem subsystem, String language) {
		super(subsystem, language);
	}

	/**
	 * The given remote file must be an instance of <code>DStoreFileImpl</code>.
	 * @see org.eclipse.rse.internal.subsystems.files.core.IJavaLanguageUtility#getQualifiedClassName(org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile)
	 */
	public String getQualifiedClassName(IRemoteFile remoteFile) {
		
		DStoreFile univFile = null;
		
		if (remoteFile instanceof DStoreFile) {
			univFile = (DStoreFile)remoteFile;
		}
		else {
			return null;
		}
		
		String remotePath = remoteFile.getAbsolutePath();
		DataElement deObj = ((DStoreHostFile)univFile.getHostFile()).getDataElement();	
		
		DataStore ds = deObj.getDataStore();
		DataElement queryCmd = ds.localDescriptorQuery(deObj.getDescriptor(), C_QUERY_QUALIFIED_CLASSNAME);

		if (queryCmd != null) {
			DataElement status = ds.synchronizedCommand(queryCmd, deObj, true);
			DataElement className = ds.find(status, DE.A_TYPE, TYPE_QUALIFIED_CLASSNAME, 1);
				
			if (className != null && !className.equals("null")) {
				return className.getName();
			}
			else {
				SystemBasePlugin.logWarning("Qualified class name for " + remoteFile.getAbsolutePath() + " is null");
			}
		}
		
		return null;
	}
}