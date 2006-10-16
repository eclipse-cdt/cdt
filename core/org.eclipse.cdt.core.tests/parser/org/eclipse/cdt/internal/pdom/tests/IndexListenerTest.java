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

package org.eclipse.cdt.internal.pdom.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexChangeEvent;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexerStateEvent;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.runtime.NullProgressMonitor;

public class IndexListenerTest extends BaseTestCase {
	protected IIndex fIndex;
	private ICProject fProject1;
	private ICProject fProject2;

	public static Test suite() {
		return suite(IndexListenerTest.class);
	}

	protected void setUp() throws Exception {
		fProject1 = CProjectHelper.createCCProject("testIndexListener1", null);
		fProject2 = CProjectHelper.createCCProject("testIndexListener2", null);
		CCorePlugin.getPDOMManager().setIndexerId(fProject1, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getPDOMManager().setIndexerId(fProject2, IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(2000, new NullProgressMonitor()));
	}

	protected void tearDown() throws Exception {
		CProjectHelper.delete(fProject1);
		CProjectHelper.delete(fProject2);
	}

	public void testIdleListener() throws Exception {
		final int[] state= new int[] {0, 0, 0};
		IIndexManager im= CCorePlugin.getIndexManager();
		
		im.addIndexerStateListener(new IIndexerStateListener() {
			public void indexChanged(IIndexerStateEvent event) {
				if (event.indexerIsIdle()) {
					state[0]++;
					state[2]= 0;
				}
				else {
					state[1]++;
					state[2]= 1;
				}
			}
		});
		
		TestSourceReader.createFile(fProject1.getProject(), "test.cpp", "int a;");
		Thread.sleep(200);
		assertTrue(im.joinIndexer(2000, new NullProgressMonitor()));
		Thread.sleep(200);
		assertEquals(1, state[0]);
		assertEquals(1, state[1]);
		assertEquals(0, state[2]);
	}

	public void testChangeListener() throws Exception {
		final List projects= new ArrayList();
		IIndexManager im= CCorePlugin.getIndexManager();
		
		im.addIndexChangeListener(new IIndexChangeListener() {
			public void indexChanged(IIndexChangeEvent event) {
				projects.add(event.getAffectedProject());
			}
		});
		
		TestSourceReader.createFile(fProject1.getProject(), "test.cpp", "int a;");
		Thread.sleep(200);
		assertEquals(1, projects.size());
		assertTrue(projects.contains(fProject1));
		projects.clear();
		

		TestSourceReader.createFile(fProject1.getProject(), "test.cpp", "int a;");
		TestSourceReader.createFile(fProject2.getProject(), "test.cpp", "int b;");
		Thread.sleep(200);
		assertEquals(2, projects.size());
		assertTrue(projects.contains(fProject1));
		assertTrue(projects.contains(fProject2));
		projects.clear();

	}
}
