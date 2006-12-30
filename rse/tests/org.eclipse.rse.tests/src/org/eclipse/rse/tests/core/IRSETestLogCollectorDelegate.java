/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.core;

import org.eclipse.core.runtime.IPath;

/**
 * Test log collector delegate interface contract. The test
 * collector delegates will be called from the <code>RSECoreTestCase</code>
 * in case the last test which had been run failed.  
 */
public interface IRSETestLogCollectorDelegate {

	/**
	 * Returns the list of absolute file locations to included
	 * within the collected logs archive file. The returned
	 * absolute file locations must denote real existing files.
	 * Possible dynamic content to collect can be written to
	 * temporary files. The log collector delegate is responsible
	 * for removing these temporary files if the <code>dispose()</code>
	 * method is called.
	 * 
	 * @return An array of absolute file locations.
	 */
	public IPath[] getAbsoluteLogFileLocations();
	
	/**
	 * Signal the RSE test log collector delegate to dispose
	 * any resource created. Possibly created temporary files
	 * should be deleted. Open streams or handles should be
	 * closed.
	 */
	public void dispose();
}
