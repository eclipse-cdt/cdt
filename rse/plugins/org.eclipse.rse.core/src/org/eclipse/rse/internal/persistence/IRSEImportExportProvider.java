/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * David Dykstal (IBM) - [189274] provide import and export operations for profiles
 *********************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.persistence.dom.RSEDOM;

/**
 * An import export provider is an add-in interface for a persistence provider. If the provider
 * implements this interface then it may be used for import/export operations.
 */
public interface IRSEImportExportProvider {
	
	/**
	 * Exports an RSEDOM using this provider to a location named by the file parameter.
	 * The export is synchronous.
	 * @param folder the File object into which to export
	 * @param dom the DOM to export
	 * @param monitor the monitor to use for progress reporting and cancellation
	 * @return true if the export completed.
	 */
	public boolean exportRSEDOM(File folder, RSEDOM dom, IProgressMonitor monitor);
	
	/**
	 * Imports an RSEDOM using this provider from a location named by the file parameter.
	 * The import is synchronous.
	 * @param folder the File object from which to import. The DOM is located within this
	 * folder and the persistence provider must be able to find it. There must be only
	 * one DOM in this folder.
	 * @param monitor the monitor to use for progress reporting and cancellation
	 * @return the DOM that was imported or null if the import did not succeed.
	 */
	public RSEDOM importRSEDOM(File folder, IProgressMonitor monitor);
	
	/**
	 * Sets the provider identifier for this provider. This id is the one specified
	 * in the extension point that defined the provider, or the id that was specified for
	 * the provider when it was added to the manager.
	 * @param providerId The id for this provider.
	 */
	public void setId(String providerId);

	/**
	 * Gets the provider identifier for this provider. This id is the one specified
	 * in the extension point that defined the provider, or the id that was specified for
	 * the provider when it was added to the manager.
	 * @return The id for this provider.
	 */
	public String getId();

}
