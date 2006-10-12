/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

public class BaseTestCase extends TestCase {
	private boolean fExpectFailure= false;
	private int fBugnumber= 0;
	
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
		if (Test.class.isAssignableFrom(clazz)) {
			Method[] methods= clazz.getDeclaredMethods();
			for (int i= 0; i < methods.length; i++) {
				addFailingMethod(suite, methods[i], clazz, prefix);
			}
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
	 * Reads a section in comments form the source of the given class. Fully 
	 * equivalent to <code>readTaggedComment(getClass(), tag)</code>
	 * @since 4.0
	 */
    protected String readTaggedComment(final String tag) throws IOException {
    	return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), getClass(), tag);
    }
    
    protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
    	return TestSourceReader.createFile(container, new Path(fileName), contents);
    }
    
    protected IASTTranslationUnit createIndexBasedAST(IIndex index, ICProject project, IFile file) throws CModelException, CoreException {
    	ICElement elem= project.findElement(file.getFullPath());
    	if (elem instanceof ITranslationUnit) {
    		ITranslationUnit tu= (ITranslationUnit) elem;
    		if (tu != null) {
    			return tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
    		}
    	}
    	fail("Could not create ast for " + file.getFullPath());
    	return null;
    }

	protected void waitForIndexer(IIndex index, IFile file, int maxmillis) throws Exception {
		long endTime= System.currentTimeMillis() + maxmillis;
		do {
			index.acquireReadLock();
			try {
				IIndexFile pfile= index.getFile(file.getLocation());
				// mstodo check timestamp
				if (pfile != null) {
					return;
				}
			}
			finally {
				index.releaseReadLock();
			}
			
			Thread.sleep(50);
		} while (System.currentTimeMillis() < endTime);
		throw new Exception("Indexer did not complete in time!");
	}
	
	protected void runEventQueue(int time) {
		long endTime= System.currentTimeMillis()+time;
		do {
			while (Display.getCurrent().readAndDispatch());
		}
		while(System.currentTimeMillis() < endTime);
	}
}
