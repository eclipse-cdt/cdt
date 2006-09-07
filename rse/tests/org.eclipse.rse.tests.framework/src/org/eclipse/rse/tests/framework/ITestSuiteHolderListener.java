/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework;

/**
 * Implementers of this interface can register with a test suite holder to listen for the 
 * events regarding the test suite in the holder.
 * @see ITestSuiteHolder
 */
public interface ITestSuiteHolderListener {
    
    /**
     * A test in the suite held by a test holder has ended.  The holder may be queried for 
     * the result.
     * @param holder
     */
    public void testEnded(ITestSuiteHolder holder);
    
    /**
     * A test holder has been reset.
     * @param holder
     */
    public void testHolderReset(ITestSuiteHolder holder);
    
}


