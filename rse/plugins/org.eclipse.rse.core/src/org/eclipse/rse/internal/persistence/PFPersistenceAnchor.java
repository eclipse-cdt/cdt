/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.persistence;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

interface PFPersistenceAnchor {

	/**
	 * @return the names of all valid profiles. Can be used to get a profile
	 * location from getProfileLocation().
	 */
	String[] getProfileLocationNames();

	/**
	 * @param profileName The name of a profile
	 * @param monitor a progress monitor for progress and canceling
	 * @return an IStatus indicating if the delete operation succeeded
	 */
	IStatus deleteProfileLocation(String profileName, IProgressMonitor monitor);

	/**
	 * @param profileName the name of the profile to get the location for
	 * @return the location of the profile
	 */
	PFPersistenceLocation getProfileLocation(String profileName);

}
