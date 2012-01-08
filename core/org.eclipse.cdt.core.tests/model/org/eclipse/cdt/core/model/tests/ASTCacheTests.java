/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * Tests for the {@link ASTCache}.
 */
public class ASTCacheTests extends BaseTestCase {
	private final static boolean DEBUG= false;
	
	private static int fgReconcilerCount;

	public class MockReconciler extends Thread {
		private final ITranslationUnit fTU;
		private final ASTCache fCache;
		public volatile boolean fStopped;
		public IASTTranslationUnit fAST;

		public MockReconciler(ITranslationUnit tu, ASTCache cache) {
			super("MockReconciler-"+fgReconcilerCount++);
			fTU= tu;
			fCache= cache;
			setDaemon(true);
		}
		@Override
		public void run() {
			while (!fStopped) {
				try {
					synchronized (this) {
						fCache.aboutToBeReconciled(fTU);
						if (DEBUG) System.out.println("about ot reconcile "+fTU.getElementName());
						fAST= null;
						notify();
					}
					Thread.sleep(50);
					IASTTranslationUnit ast= fCache.createAST(fTU, fIndex, null);
					synchronized (this) {
						fAST= ast;
						if (DEBUG) System.out.println("reconciled "+fTU.getElementName());
						fCache.reconciled(fAST, fTU);
					}
					Thread.sleep(50);
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

	public static Test suite() {
		TestSuite suite= new TestSuite(ASTCacheTests.class);
		return suite;
	}

	private final String SOURCE1= "void foo1() {}"; //$NON-NLS-1$
	private final String SOURCE2= "void foo2() {}"; //$NON-NLS-1$

	@Override
	public void setUp() throws Exception {
		super.setUp();
		IProgressMonitor npm= new NullProgressMonitor();
		fProject= createProject("ASTCacheTest");
		assertNotNull(fProject);
		IFile file1= createFile(fProject.getProject(), "source1.cpp", SOURCE1);
		assertNotNull(file1);
		IFile file2= createFile(fProject.getProject(), "source2.cpp", SOURCE2);
		assertNotNull(file2);
		fTU1= (ITranslationUnit) CoreModel.getDefault().create(file1);
		assertNotNull(fTU1);
		fTU2= (ITranslationUnit) CoreModel.getDefault().create(file2);
		assertNotNull(fTU2);
		CCorePlugin.getIndexManager().joinIndexer(5000, npm);
		fIndex= CCorePlugin.getIndexManager().getIndex(fProject);
		fIndex.acquireReadLock();
	}

	@Override
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

	protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
		return TestSourceReader.createFile(container, new Path(fileName), contents);
	}

	public void testASTCache() throws Exception {
		checkActiveElement();
		checkSingleThreadAccess();
		checkAccessWithSequentialReconciler();
		checkAccessWithConcurrentReconciler();
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
		final ASTCache cache= new ASTCache();
		final int[] counter= {0};
		cache.setActiveElement(fTU1);
		IStatus status= cache.runOnAST(fTU1, false, null, new ASTRunnable() {
			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
				assertNull(ast);
				counter[0]++;
				return Status.OK_STATUS;
			}
		});
		assertEquals(1, counter[0]);

		IProgressMonitor npm= new NullProgressMonitor();
		npm.setCanceled(true);
		status= cache.runOnAST(fTU1, true, npm, new ASTRunnable() {
			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
				assertNull(ast);
				counter[0]++;
				return Status.OK_STATUS;
			}
		});
		assertEquals(2, counter[0]);

		npm.setCanceled(false);
		status= cache.runOnAST(fTU1, true, npm, new ASTRunnable() {
			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
				assertNotNull(ast);
				counter[0]++;
				return Status.OK_STATUS;
			}
		});
		assertEquals(3, counter[0]);
	}

	private void checkAccessWithSequentialReconciler() throws Exception {
		final ASTCache cache= new ASTCache();
		final MockReconciler reconciler1= new MockReconciler(fTU1, cache);
		final MockReconciler reconciler2= new MockReconciler(fTU2, cache);
		try {
			cache.setActiveElement(fTU1);
			assertFalse(cache.isReconciling(fTU1));
			synchronized (reconciler1) {
				reconciler1.start();
				reconciler1.wait();
				assertNull(reconciler1.fAST);
				assertTrue(cache.isActiveElement(fTU1));
				assertTrue(cache.isReconciling(fTU1));
			}
			reconciler1.fStopped= true;
			IStatus status= cache.runOnAST(fTU1, true, null, new ASTRunnable() {
				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
					assertNotNull(ast);
					assertTrue(cache.isActiveElement(fTU1));
					assertFalse(cache.isReconciling(fTU1));
					assertSame(ast, reconciler1.fAST);
					return Status.OK_STATUS;
				}
			});
			
			// change active element
			cache.setActiveElement(fTU2);
			assertFalse(cache.isReconciling(fTU2));
			synchronized (reconciler2) {
				reconciler2.start();
				reconciler2.wait();
				assertNull(reconciler2.fAST);
				assertTrue(cache.isActiveElement(fTU2));
				assertTrue(cache.isReconciling(fTU2));
			}
			reconciler2.fStopped= true;
			
			status= cache.runOnAST(fTU2, true, null, new ASTRunnable() {
				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
					assertNotNull(ast);
					assertTrue(cache.isActiveElement(fTU2));
					assertFalse(cache.isReconciling(fTU2));
					assertSame(ast, reconciler2.fAST);
					return Status.OK_STATUS;
				}
			});
		} finally {
			reconciler1.fStopped= true;
			reconciler1.join(1000);
			reconciler2.fStopped= true;
			reconciler2.join(1000);
		}
	}

	private void checkAccessWithConcurrentReconciler() throws Exception {
		ASTCache cache= new ASTCache();
		MockReconciler reconciler1= new MockReconciler(fTU1, cache);
		MockReconciler reconciler2= new MockReconciler(fTU2, cache);
		reconciler1.start();
		Thread.sleep(50);
		reconciler2.start();
		try {
			int iterations= 0;
			while (iterations < 10) {
				++iterations;
				if (DEBUG) System.out.println("iteration="+iterations);
				cache.setActiveElement(fTU1);
				Thread.sleep(50);
				waitForAST(cache, fTU1, new ASTRunnable() {
					@Override
					public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
						assertNotNull(ast);
						assertEquals("void foo1() {}", ast.getDeclarations()[0].getRawSignature());
						return Status.OK_STATUS;
					}
				});

				waitForAST(cache, fTU2, new ASTRunnable() {
					@Override
					public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
						assertNotNull(ast);
						assertEquals("void foo2() {}", ast.getDeclarations()[0].getRawSignature());
						return Status.OK_STATUS;
					}
				});

				// change active element
				cache.setActiveElement(fTU2);
				Thread.sleep(50);
				waitForAST(cache, fTU2, new ASTRunnable() {
					@Override
					public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
						assertNotNull(ast);
						assertEquals("void foo2() {}", ast.getDeclarations()[0].getRawSignature());
						return Status.OK_STATUS;
					}
				});

				waitForAST(cache, fTU1, new ASTRunnable() {
					@Override
					public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
						assertNotNull(ast);
						assertEquals("void foo1() {}", ast.getDeclarations()[0].getRawSignature());
						return Status.OK_STATUS;
					}
				});
}
		} finally {
			reconciler1.fStopped= true;
			reconciler1.join(1000);
			reconciler2.fStopped= true;
			reconciler2.join(1000);
		}
	}

	private void waitForAST(ASTCache cache, ITranslationUnit tUnit, ASTRunnable runnable) {
		if (DEBUG) System.out.println("waiting for "+tUnit.getElementName());
		long start= System.currentTimeMillis();
		try {
			cache.runOnAST(tUnit, true, null, runnable);
		}
		finally {
			if (DEBUG) System.out.println("wait time= " + (System.currentTimeMillis() - start));
		}
	}
}
