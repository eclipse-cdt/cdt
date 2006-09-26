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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;

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
	 * Reads a section in comments form the source of the given class. The section
	 * is started with '// {tag}' and ends with the first line not started by '//' 
	 * @since 4.0
	 */
    protected String readTaggedComment(Class clazz, final String tag) throws IOException {
        IPath filePath= new Path("ui/" + clazz.getName().replace('.', '/') + ".java");
        
        InputStream in= FileLocator.openStream(CTestPlugin.getDefault().getBundle(), filePath, false);
        LineNumberReader reader= new LineNumberReader(new InputStreamReader(in));
        boolean found= false;
        final StringBuffer content= new StringBuffer();
        try {
            String line= reader.readLine();
            while (line != null) {
            	line= line.trim();
                if (line.startsWith("//")) {
                    line= line.substring(2);
                    if (found) {
                        content.append(line);
                        content.append('\n');
                    }
                    else {
                        line= line.trim();
                        if (line.startsWith("{" + tag)) {
                            if (line.length() == tag.length()+1 ||
                                    !Character.isJavaIdentifierPart(line.charAt(tag.length()+1))) {
                                found= true;
                            }
                        }
                    }
                }
                else if (found) {
                    break;
                }
                line= reader.readLine();
            }
        }
        finally {
            reader.close();
        }
        assertTrue("Tag '" + tag + "' is not defined inside of '" + filePath + "'.", found);
        return content.toString();
    }

	/**
	 * Reads a section in comments form the source of the given class. Fully 
	 * equivalent to <code>readTaggedComment(getClass(), tag)</code>
	 * @since 4.0
	 */
    protected String readTaggedComment(final String tag) throws IOException {
    	return readTaggedComment(getClass(), tag);
    }
    
    /**
     * Creates a file with content at the given path inside the given container. 
     * If the file exists its content is replaced.
     * @param container a container to create the file in
     * @param filePath the path relative to the container to create the file at
     * @param contents the content for the file
     * @return a file object.
     * @throws Exception
     * @since 4.0
     */    
    protected IFile createFile(IContainer container, IPath filePath, String contents) throws Exception {
		//Obtain file handle
		IFile file = container.getFile(filePath);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		//Create file input stream
		if (file.exists()) {
			file.setContents(stream, false, false, new NullProgressMonitor());
		} 
		else {
			file.create(stream, false, new NullProgressMonitor());
		}
		return file;
	}

    protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
    	return createFile(container, new Path(fileName), contents);
    }
    
    protected IASTTranslationUnit createPDOMBasedAST(ICProject project, IFile file) throws CModelException, CoreException {
    	ICElement elem= project.findElement(file.getFullPath());
    	if (elem instanceof ITranslationUnit) {
    		ITranslationUnit tu= (ITranslationUnit) elem;
    		if (tu != null) {
    			return tu.getLanguage().getASTTranslationUnit(tu, ILanguage.AST_SKIP_INDEXED_HEADERS |ILanguage.AST_USE_INDEX);
    		}
    	}
    	fail("Could not create ast for " + file.getFullPath());
    	return null;
    }

	protected void waitForIndexer(PDOM pdom, IFile file, int maxmillis) throws Exception {
		long endTime= System.currentTimeMillis() + maxmillis;
		do {
			pdom.acquireReadLock();
			try {
				PDOMFile pfile= pdom.getFile(file.getLocation());
				// mstodo check timestamp
				if (pfile != null) {
					return;
				}
			}
			finally {
				pdom.releaseReadLock();
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
