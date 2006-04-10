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

import java.io.File;
import java.io.IOException;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.internal.subsystems.files.core.AbstractJavaLanguageUtility;
import org.eclipse.rse.services.clientserver.java.ClassFileUtil;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.local.model.LocalFile;

/**
 * This class is the Java language utility for local.
 */
public class LocalJavaLanguageUtility extends AbstractJavaLanguageUtility {

	/**
	 * Constructor.
	 * @param subsystem the subsystem with which the utility is associated.
	 * @param language the language.
	 */
	public LocalJavaLanguageUtility(IRemoteFileSubSystem subsystem, String language) {
		super(subsystem, language);
	}

	/**
	 * The given remote file must be an instance of <code>LocalFileImpl</code>.
	 * @see com.ibm.etools.systems.subsystems.util.IJavaLanguageUtility#getQualifiedClassName(com.ibm.etools.systems.subsystems.IRemoteFile)
	 */
	public String getQualifiedClassName(IRemoteFile remoteFile) {
		
		try {
			if (remoteFile instanceof LocalFile) {
				File file = (File)(remoteFile.getFile());
				return ClassFileUtil.getInstance().getQualifiedClassName(file);
			}
			else {
				return null;
			}
		}
		catch (IOException e) {
			SystemBasePlugin.logError("Error occurred trying to get qualified class name", e);
			return null;
		}
	}
}