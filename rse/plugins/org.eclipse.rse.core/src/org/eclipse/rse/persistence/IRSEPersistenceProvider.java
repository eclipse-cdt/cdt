/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 177329: added getSaveJob so that the persistence provider 
 * determines the job characteristics.
 ********************************************************************************/

package org.eclipse.rse.persistence;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.persistence.dom.RSEDOM;

/**
 * This is the interface that needs to be implemented when providing an extension
 * using the RSE persistence provider extension point. 
 * 
 * Implement this class to provide a specialized means of 
 * saving and restoring the RSE DOM.
 * 
 * This interface is used by the persistence manager to schedule loads and saves
 * of the DOM and should only be used by the persistence manager.
 */
public interface IRSEPersistenceProvider {

	/**
	 * Restores an RSE DOM given a profileName. 
	 * 
	 * @param profileName name of the Profile to load
	 * @param monitor The monitor to use for progress monitoring and cancellation.
	 * Must not be null.
	 * @return the RSE DOM for the specified profile
	 */
	public RSEDOM loadRSEDOM(String profileName, IProgressMonitor monitor);

	/**
	 * Persists an RSE DOM. Writes the DOM to some form of external storage and
	 * then marks the DOM as clean using {@link RSEDOM#markUpdated()}.
	 * @param dom the RSE DOM to persist.
	 * @param monitor The monitor to use for progress monitoring and cancellation.
	 * Must not be null.
	 * @return true if succcessful
	 */
	public boolean saveRSEDOM(RSEDOM dom, IProgressMonitor monitor);

	/**
	 * Returns a job suitable for saving a DOM.
	 * The result can be null if the persistence provider determines that 
	 * a job would not be the best way of saving a particular DOM.
	 * @param dom the DOM for which to construct the job.
	 * @return The job that can be scheduled to perform the save operation.
	 */
	public Job getSaveJob(RSEDOM dom);
	
	/**
	 * @return The names of the profiles that have been saved by this persistence provider.
	 */
	public String[] getSavedProfileNames();

	/**
	 * Removes a profile. Does nothing if the profile is not found.
	 * @param profileName the name of the profile to remove
	 * @param monitor The monitor to use for progress monitoring and cancellation.
	 * Must not be null.
	 * @return the IStatus indicating the operations success.
	 */
	public IStatus deleteProfile(String profileName, IProgressMonitor monitor);

}