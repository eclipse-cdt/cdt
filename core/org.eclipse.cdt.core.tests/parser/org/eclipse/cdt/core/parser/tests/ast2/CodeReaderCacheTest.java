/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.CodeReaderCache;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author dsteffle
 */
public class CodeReaderCacheTest extends CDOMBaseTest {
	
	public CodeReaderCacheTest(String name) {
		super(name, CodeReaderCacheTest.class);
	}
	
    public static Test suite() {
        TestSuite suite = new TestSuite( CodeReaderCacheTest.class );
        suite.addTest( new CodeReaderCacheTest("cleanupProject") );    //$NON-NLS-1$
	    return suite;
    }

	private class UpdateFileJob extends Job {
		private IFile file = null;
		private String fileName = null;
		private String code = null;
		
		public UpdateFileJob(String name, IFile file, String fileName, String code) {
			super(name);
			this.file = file;
			this.fileName = fileName;
			this.code = code;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			while(!monitor.isCanceled()) {
				try {
					file = importFile(fileName, code);
				} catch (Exception e) {
				}
			}
			
			return Status.OK_STATUS;
		}
		
		public IFile getFile() {
			return file;
		}
	}

	// THIS MUST BE RUN FIRST IN THIS TEST
	public void testSetCacheSize() {
		ICodeReaderCache cache = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
		// update the size of the cache... must be done for the first test since other test suites use 0MB cache size
		assertTrue(cache instanceof CodeReaderCache);
		((CodeReaderCache)cache).setCacheSize(CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
	}
	
	public void testSimpleCacheFunctionality() {
		StringBuffer code = new StringBuffer();
		code.append("int x;"); //$NON-NLS-1$
		
		IFile file = null;
		try {
			file = importFile("test.c", code.toString()); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		parse(file);
		
		ICodeReaderCache cache = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
		CodeReader reader = cache.get(file.getLocation().toOSString());
		assertNotNull(reader);
		assertEquals(cache.getCurrentSpace(), 1);
		assertEquals(String.valueOf(reader.filename), file.getLocation().toOSString());
		cache.remove(String.valueOf(reader.filename));
		assertEquals(cache.getCurrentSpace(), 0);
	}
	
	public void testResourceChangedUpdate() {
		boolean hasPassed = false;
		StringBuffer code = new StringBuffer();
		code.append("int x;"); //$NON-NLS-1$
		ICodeReaderCache cache = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
		
		IFile file = null;
		
		try {
			file = importFile("test.c", code.toString()); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// start a new job that repeatedly updates the file...
		UpdateFileJob job = new UpdateFileJob("updater", file, "test.c", code.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		job.schedule();
		
		while(!hasPassed) {
			if (file != null) {
				parse(file);
			}
			
			try {
				Thread.sleep(1000); // give the updater thread some time to update the resource
				file = job.getFile();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (cache.getCurrentSpace() == 0) // item was properly removed by the updater thread 
				hasPassed = true;
		}
		
		job.cancel();
	}
    
    public void testResourceChangedNestedPathUpdate() {
        boolean hasPassed = false;
        StringBuffer code = new StringBuffer();
        code.append("int x;"); //$NON-NLS-1$
        ICodeReaderCache cache = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
        
        IFile file = null;
        
        try {
            importFolder("test");
            file = importFile("test/test.c", code.toString()); //$NON-NLS-1$
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // start a new job that repeatedly updates the file...
        UpdateFileJob job = new UpdateFileJob("updater", file, "test/test.c", code.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        job.schedule();
        
        while(!hasPassed) {
            if (file != null) {
                parse(file);
            }
            
            try {
                Thread.sleep(1000); // give the updater thread some time to update the resource
                file = job.getFile();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if (cache.getCurrentSpace() == 0) // item was properly removed by the updater thread 
                hasPassed = true;
        }
        
        job.cancel();
    }
	
	// THIS MUST BE RUN LAST IN THIS TEST
	public void testClearCache() {
		ICodeReaderCache cache = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
		// now that the 
		assertTrue(cache instanceof CodeReaderCache);
		((CodeReaderCache)cache).setCacheSize(0);
	}
}
