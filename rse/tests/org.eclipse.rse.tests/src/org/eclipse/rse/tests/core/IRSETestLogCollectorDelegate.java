/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.core;

import org.eclipse.core.runtime.IPath;

/**
 * Test log collector delegate interface contract. The test
 * collector delegates will be called from the <code>RSECoreTestCase</code>
 * in case the last test which had been run failed.  
 */
public interface IRSETestLogCollectorDelegate {

	public IPath[] getAbsoluteLogFileLocations();
	
	public void dispose();
}
