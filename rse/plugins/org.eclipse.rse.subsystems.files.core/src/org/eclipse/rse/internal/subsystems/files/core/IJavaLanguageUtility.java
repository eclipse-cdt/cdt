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

package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * Interface for Java language utility.
 */
public interface IJavaLanguageUtility extends ILanguageUtility {
	
	/**
	 * Returns the qualified class name for the remote file.
	 * @param remoteFile the remote file.
	 * @return the qualified class name.
	 */
	public String getQualifiedClassName(IRemoteFile remoteFile);
}