/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Nov 1, 2004
 */
package org.eclipse.cdt.core.tests;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;


/**
 * Wraps a test case to check for its failure.
 * @author aniefer
 */
public class FailingTest extends TestCase {
    private TestCase test = null;
    private int bugNum = -1;
    
    public FailingTest( TestCase test, int bugNumber ){
        this.test = test;
        this.bugNum = bugNumber;
        String name= "Failing " + test.getName();
        if (bugNum > 0) {
        	name += " [bug " + bugNum + "]";
        }
        setName(name);
    }
    
    public FailingTest( TestCase test ){
        this(test, -1);
    }
    /* (non-Javadoc)
     * @see junit.framework.Test#run(junit.framework.TestResult)
     */
    public void run( TestResult result ) {
        result.startTest( this );
        
        TestResult r = new TestResult();
        test.run( r );
        if (r.failureCount() == 1) {
        	TestFailure failure= r.failures().nextElement();
        	String msg= failure.exceptionMessage();
        	if (msg != null && msg.startsWith("Method \"" + test.getName() + "\"")) {
        		result.addFailure(this, new AssertionFailedError(msg));
        	}
        }
        else if( r.errorCount() == 0 && r.failureCount() == 0 )
        {
            String err = "Unexpected success"; //$NON-NLS-1$
            if( bugNum != -1 )
                err += ", bug #" + bugNum; //$NON-NLS-1$
            result.addFailure( this, new AssertionFailedError( err ) );
        }
        
        result.endTest( this );
    }
}
