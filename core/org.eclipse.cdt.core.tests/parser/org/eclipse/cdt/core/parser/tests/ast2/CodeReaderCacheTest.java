/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
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
public class CodeReaderCacheTest extends CDOMTestBase {

	public CodeReaderCacheTest() {
	}

	public CodeReaderCacheTest(String name) {
		super(name, CodeReaderCacheTest.class);
	}

    public static Test suite() {
        TestSuite suite = new TestSuite(CodeReaderCacheTest.class);
        suite.addTest(new CodeReaderCacheTest("cleanupProject"));
	    return suite;
    }

	private class UpdateFileJob extends Job {
		private IFile file;
		private final String fileName;
		private final String code;

		public UpdateFileJob(String name, IFile file, String fileName, String code) {
			super(name);
			this.file = file;
			this.fileName = fileName;
			this.code = code;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (!monitor.isCanceled()) {
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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ICodeReaderCache cache = getCodeReaderCache();
		assertTrue(cache instanceof CodeReaderCache);
		((CodeReaderCache) cache).setCacheSize(CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
	}

	@Override
	public void tearDown() throws Exception {
		ICodeReaderCache cache = getCodeReaderCache();
		assertTrue(cache instanceof CodeReaderCache);
		((CodeReaderCache) cache).setCacheSize(0);
		super.tearDown();
	}

	private ICodeReaderCache getCodeReaderCache() {
		return CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES).getCodeReaderCache();
	}

	public void testSimpleCacheFunctionality() throws Exception {
		StringBuilder code = new StringBuilder();
		code.append("int x;");

		IFile file = importFile("test.c", code.toString());
		parse(file);

		ICodeReaderCache cache = getCodeReaderCache();
		cache.flush();
		assertEquals(0, cache.getCurrentSpace());
		CodeReader reader = cache.get(file.getLocation().toOSString());
		assertNotNull(reader);
		assertEquals(1, cache.getCurrentSpace());
		assertEquals(String.valueOf(reader.filename), file.getLocation().toOSString());
		cache.remove(String.valueOf(reader.filename));
		assertEquals(0, cache.getCurrentSpace());
	}

	public void testResourceChangedUpdate() throws Exception {
		boolean hasPassed = false;
		StringBuilder code = new StringBuilder();
		code.append("int x;");
		ICodeReaderCache cache = getCodeReaderCache();

		IFile file = importFile("test.c", code.toString());
		parse(file);

		// start a new job that repeatedly updates the file...
		UpdateFileJob job = new UpdateFileJob("updater", file, "test.c", code.toString());  //$NON-NLS-2$
		job.schedule();

		while (!hasPassed) {
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

	// This is broken.
	// I have a mind to delete any test that has a Thread.sleep() in it.
    public void testResourceChangedNestedPathUpdate(int off) throws Exception {
        boolean hasPassed = false;
        StringBuilder code = new StringBuilder();
        code.append("int x;");
        ICodeReaderCache cache = getCodeReaderCache();

        importFolder("test");
        IFile file = importFile("test/test.c", code.toString());

        // start a new job that repeatedly updates the file...
        UpdateFileJob job = new UpdateFileJob("updater", file, "test/test.c", code.toString());
        job.schedule();

        while (!hasPassed) {
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
}
