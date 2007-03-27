/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.core.testplugin.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

public class BaseTestCase extends TestCase {
	protected static final IProgressMonitor NPM= new NullProgressMonitor();

	private boolean fExpectFailure= false;
	private int fBugnumber= 0;
	private int fExpectedLoggedNonOK= 0;
	
	public BaseTestCase() {
		super();
	}
	
	public BaseTestCase(String name) {
		super(name);
	}

	protected static TestSuite suite(Class clazz) {
		return suite(clazz, null);
	}
	
	protected static TestSuite suite(Class clazz, String failingTestPrefix) {
		TestSuite suite= new TestSuite(clazz);
		Test failing= getFailingTests(clazz, failingTestPrefix);
		if (failing != null) {
			suite.addTest(failing);
		}
		return suite;
	}

	private static Test getFailingTests(Class clazz, String prefix) {
		TestSuite suite= new TestSuite("Failing Tests");
		Vector names= new Vector();
		Class superClass= clazz;
		while (Test.class.isAssignableFrom(superClass) && !TestCase.class.equals(superClass)) {
			Method[] methods= superClass.getDeclaredMethods();
			for (int i= 0; i < methods.length; i++) {
				addFailingMethod(suite, methods[i], clazz, prefix);
			}
			superClass= superClass.getSuperclass();
		}
		if (suite.countTestCases() == 0) {
			return null;
		}
		return suite;
	}

	private static void addFailingMethod(TestSuite suite, Method m, Class clazz, String prefix) {
		String name= m.getName();
		if (name.startsWith("test") || (prefix != null && !name.startsWith(prefix))) {
			return;
		}
		if (name.equals("tearDown") || name.equals("setUp") || name.equals("runBare")) {
			return;
		}
		if (Modifier.isPublic(m.getModifiers())) {
			Class[] parameters= m.getParameterTypes();
			Class returnType= m.getReturnType();
			if (parameters.length == 0 && returnType.equals(Void.TYPE)) {
				Test test= TestSuite.createTest(clazz, name);
				((BaseTestCase) test).setExpectFailure(0);
				suite.addTest(test);
			}
		}
	}

	public void runBare() throws Throwable {
		final List statusLog= Collections.synchronizedList(new ArrayList());
		ILogListener logListener= new ILogListener() {
			public void logging(IStatus status, String plugin) {
				if(!status.isOK()) {
					statusLog.add(status);
				}
			}
		};
		CCorePlugin.getDefault().getLog().addLogListener(logListener);
		
		try {
			super.runBare();
			
			if(statusLog.size()!=fExpectedLoggedNonOK) {
				StringBuffer msg= new StringBuffer("Expected number ("+fExpectedLoggedNonOK+") of ");
				msg.append("non-OK status objects differs from actual ("+statusLog.size()+").\n");
				Throwable cause= null;
				if(!statusLog.isEmpty()) {
					for(Iterator i= statusLog.iterator(); i.hasNext(); ) {
						IStatus status= (IStatus) i.next();
						if(cause==null) {
							cause= status.getException();
						}
						Throwable t= status.getException();
						msg.append("\t"+status.getMessage()+" "+(t!=null?t.getMessage():"")+"\n");
					}
				}
				AssertionFailedError afe= new AssertionFailedError(msg.toString());
				afe.initCause(cause);
				throw afe;
			}
		} finally {
			CCorePlugin.getDefault().getLog().removeLogListener(logListener);
		}
	}

    public void run( TestResult result ) {
    	if (!fExpectFailure) {
    		super.run(result);
    		return;
    	}
    	
        result.startTest( this );
        
        TestResult r = new TestResult();
        super.run( r );
        if (r.failureCount() == 1) {
        	TestFailure failure= (TestFailure) r.failures().nextElement();
        	String msg= failure.exceptionMessage();
        	if (msg != null && msg.startsWith("Method \"" + getName() + "\"")) {
        		result.addFailure(this, new AssertionFailedError(msg));
        	}
        }
        else if( r.errorCount() == 0 && r.failureCount() == 0 )
        {
            String err = "Unexpected success of " + getName();
            if( fBugnumber > 0 ) {
                err += ", bug #" + fBugnumber; 
            }
            result.addFailure( this, new AssertionFailedError( err ) );
        }
        
        result.endTest( this );
    }
    
    public void setExpectFailure(int bugnumber) {
    	fExpectFailure= true;
    	fBugnumber= bugnumber;
    }
    
    /**
     * The last value passed to this method in the body of a testXXX method
     * will be used to determine whether or not the presence of non-OK status objects
     * in the log should fail the test. If the logged number of non-OK status objects
     * differs from the last value passed, the test is failed. If this method is not called
     * at all, the expected number defaults to zero.
     * @param value
     */
    public void setExpectedNumberOfLoggedNonOKStatusObjects(int count) {
    	fExpectedLoggedNonOK= count;
    }
}