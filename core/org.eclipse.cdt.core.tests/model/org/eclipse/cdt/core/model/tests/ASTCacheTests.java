/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Tests for the {@link ASTCache}.
 */
public class ASTCacheTests extends BaseTestCase {
	private static int fgReconcilerCount;

	public class MockReconciler extends Thread {
		private ITranslationUnit fTU;
		private ASTCache fCache;
		public volatile boolean fStopped;
		private IASTTranslationUnit fAST;

		public MockReconciler(ITranslationUnit tu, ASTCache cache) {
			super("MockReconciler-"+fgReconcilerCount++);
			fTU= tu;
			fCache= cache;
		}
		public void run() {
			while (!fStopped) {
				try {
					Thread.sleep(500);
					fCache.aboutToBeReconciled(fTU);
					IASTTranslationUnit ast;
					synchronized (this) {
						notifyAll();
					}
					fAST= fCache.createAST(fTU, fIndex, null);
					fCache.reconciled(fAST, fTU);
				} catch (InterruptedException exc) {
					fStopped= true;
					break;
				}
			}
		}
	}

	private ICProject fProject;
	private ITranslationUnit fTU1;
	private ITranslationUnit fTU2;
	private IIndex fIndex;

	public ASTCacheTests(String name) {
		super(name);
	}

	// {source1.cpp}
	// void foo1() {}
	// void bar1() {}

	// {source2.cpp}
	// void foo2() {}
	// void bar2() {}

	public static Test suite() {
		TestSuite suite= new TestSuite(ASTCacheTests.class);
		return suite;
	}

	public void setUp() throws Exception {
		super.setUp();
		fProject= createProject("ASTCacheTest");
		assertNotNull(fProject);
		IFile file1= createFile(fProject.getProject(), "source1.cpp", readTaggedComment("source1.cpp"));
		assertNotNull(file1);
		fTU1= CProjectHelper.findTranslationUnit(fProject, file1.getName());
		assertNotNull(fTU1);
		IFile file2= createFile(fProject.getProject(), "source2.cpp", readTaggedComment("source2.cpp"));
		assertNotNull(file2);
		fTU2= CProjectHelper.findTranslationUnit(fProject, file2.getName());
		assertNotNull(fTU2);
		CCorePlugin.getIndexManager().joinIndexer(5000, new NullProgressMonitor());
		fIndex= CCorePlugin.getIndexManager().getIndex(fProject);
		fIndex.acquireReadLock();
	}

	public void tearDown() throws Exception {
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
		if (fProject != null) {
			CProjectHelper.delete(fProject);
		}
		super.tearDown();
	}

	protected ICProject createProject(final String name) throws CoreException {
		return CProjectHelper.createCProject(name, null, IPDOMManager.ID_FAST_INDEXER);
	}

	protected String readTaggedComment(String tag) throws Exception {
		return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "model", getClass(), tag);
	}

	protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
		return TestSourceReader.createFile(container, new Path(fileName), contents);
	}

	public void testASTCache() throws Exception {
		checkActiveElement();
		checkSingleThreadAccess();
		checkAccessWithBackgroundReconciler();
	}
	
	private void checkActiveElement() throws Exception {
		ASTCache cache= new ASTCache();
		assertFalse(cache.isActiveElement(fTU1));
		assertFalse(cache.isActiveElement(fTU2));
		cache.setActiveElement(fTU1);
		assertTrue(cache.isActiveElement(fTU1));
		assertFalse(cache.isActiveElement(fTU2));
		cache.setActiveElement(fTU2);
		assertFalse(cache.isActiveElement(fTU1));
		assertTrue(cache.isActiveElement(fTU2));
	}
	
	private void checkSingleThreadAccess() throws Exception {
		ASTCache cache= new ASTCache();
		cache.setActiveElement(fTU1);
		IASTTranslationUnit ast;
		ast= cache.getAST(fTU1, fIndex, false, null);
		assertNull(ast);
		IProgressMonitor npm= new NullProgressMonitor();
		npm.setCanceled(true);
		ast= cache.getAST(fTU1, fIndex, true, npm);
		assertNull(ast);
		npm.setCanceled(false);
		ast= cache.getAST(fTU1, fIndex, true, npm);
		assertNotNull(ast);
	}

	private void checkAccessWithBackgroundReconciler() throws Exception {
		ASTCache cache= new ASTCache();
		cache.setActiveElement(fTU1);
		MockReconciler reconciler1= new MockReconciler(fTU1, cache);
		MockReconciler reconciler2= null;
		try {
			assertFalse(cache.isReconciling(fTU1));
			reconciler1.start();
			synchronized (reconciler1) {
				reconciler1.wait();
			}
			IASTTranslationUnit ast;
			ast= cache.getAST(fTU1, fIndex, true, null);
			assertNotNull(ast);
			assertSame(ast, reconciler1.fAST);
			
			// change active element
			cache.setActiveElement(fTU2);
			reconciler2= new MockReconciler(fTU2, cache);
			reconciler2.start();
			synchronized (reconciler2) {
				reconciler2.wait();
			}
			ast= cache.getAST(fTU2, fIndex, true, null);
			assertNotNull(ast);
			assertSame(ast, reconciler2.fAST);
		} finally {
			reconciler1.fStopped= true;
			reconciler1.join(1000);
			if (reconciler2 != null) {
				reconciler2.fStopped= true;
				reconciler2.join(1000);
			}
		}
	}

}
