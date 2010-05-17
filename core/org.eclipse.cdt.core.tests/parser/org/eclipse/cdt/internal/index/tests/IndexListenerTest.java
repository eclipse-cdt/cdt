/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.index.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexChangeEvent;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexerStateEvent;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;

public class IndexListenerTest extends BaseTestCase {
	private ICProject fProject1;
	private ICProject fProject2;

	public static Test suite() {
		return suite(IndexListenerTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		fProject1 = CProjectHelper.createCCProject("testIndexListener1", null, IPDOMManager.ID_FAST_INDEXER);
		fProject2 = CProjectHelper.createCCProject("testIndexListener2", null, IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(2000, new NullProgressMonitor()));
	}

	@Override
	protected void tearDown() throws Exception {
		CProjectHelper.delete(fProject1);
		CProjectHelper.delete(fProject2);
	}

	public void testIdleListener() throws Exception {
		final Object mutex= new Object();
		final int[] state= new int[] {0, 0, 0};
		IIndexManager im= CCorePlugin.getIndexManager();
		assertTrue(im.joinIndexer(10000, npm()));
		
		IIndexerStateListener listener = new IIndexerStateListener() {
			public void indexChanged(IIndexerStateEvent event) {
				synchronized (mutex) {
					if (event.indexerIsIdle()) {
						state[0]++;
						state[2]= 0;
					}
					else {
						state[1]++;
						state[2]= 1;
					}
					mutex.notify();
				}
			}
		};
		
		im.addIndexerStateListener(listener);
		try {
			IFile file= TestSourceReader.createFile(fProject1.getProject(), "test.cpp", "int a;");
			synchronized (mutex) {
				if (state[0]+state[1] < 2) {
					mutex.wait(8000);
					if (state[0]+state[1] < 2) {
						mutex.wait(5000);
					}
				}
				assertEquals(1, state[0]);
				assertEquals(1, state[1]);
				assertEquals(0, state[2]);
			}
		}
		finally {
			im.removeIndexerStateListener(listener);
		}
	}
		
	public void testChangeListener() throws Exception {
		final Object mutex= new Object();
		final List projects= new ArrayList();
		IIndexManager im= CCorePlugin.getIndexManager();
		
		assertTrue(im.joinIndexer(10000, npm()));
		IIndexChangeListener listener = new IIndexChangeListener() {
			public void indexChanged(IIndexChangeEvent event) {
				synchronized (mutex) {
					projects.add(event.getAffectedProject());
					mutex.notify();
				}
			}
		};
				
		im.addIndexChangeListener(listener);
		try {
			IFile file= TestSourceReader.createFile(fProject1.getProject(), "test.cpp", "int a;");
			
			synchronized (mutex) {
				mutex.wait(8000);
			}
			assertEquals(1, projects.size());
			assertTrue(projects.contains(fProject1));
			projects.clear();


			IFile file1= TestSourceReader.createFile(fProject1.getProject(), "test.cpp", "int b;");
			IFile file2= TestSourceReader.createFile(fProject2.getProject(), "test.cpp", "int c;");
			synchronized (mutex) {
				mutex.wait(1000);
				if (projects.size() < 2) {
					mutex.wait(1000);
				}
			}
			assertEquals(2, projects.size());
			assertTrue(projects.contains(fProject1));
			assertTrue(projects.contains(fProject2));
			projects.clear();
		}
		finally {
			im.removeIndexChangeListener(listener);
		}
	}
}
