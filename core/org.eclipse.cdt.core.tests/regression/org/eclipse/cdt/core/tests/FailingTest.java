/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 1, 2004
 */
package org.eclipse.cdt.core.tests;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;


/**
 * @author aniefer
 */
public class FailingTest implements Test {
    private Test test = null;
    private int bugNum = -1;
    public FailingTest( Test test, int bugNumber ){
        this.test = test;
        this.bugNum = bugNumber;
    }
    public FailingTest( Test test ){
        this.test = test;
    }
    /* (non-Javadoc)
     * @see junit.framework.Test#countTestCases()
     */
    public int countTestCases() {
        return 1;
    }
    /* (non-Javadoc)
     * @see junit.framework.Test#run(junit.framework.TestResult)
     */
    public void run( TestResult result ) {
        result.startTest( test );
        
        TestResult r = new TestResult();
        test.run( r );
        if( r.errorCount() == 0 && r.failureCount() == 0 )
        {
            String err = "Unexpected success"; //$NON-NLS-1$
            if( bugNum != -1 )
                err += ", bug #" + bugNum; //$NON-NLS-1$
            result.addFailure( test, new AssertionFailedError( err ) );
        }
        
        result.endTest( test );
    }
}
