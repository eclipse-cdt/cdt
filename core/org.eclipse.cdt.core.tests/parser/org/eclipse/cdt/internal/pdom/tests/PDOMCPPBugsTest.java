/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests bugs found in the PDOM
 */
public class PDOMCPPBugsTest extends BaseTestCase {
	ICProject cproject;
	
	public static Test suite() {
		return suite(PDOMCPPBugsTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cproject= CProjectHelper.createCCProject("PDOMBugsTest"+System.currentTimeMillis(), "bin", IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(8000, npm()));
	}

	@Override
	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}

	public void testPDOMProperties() throws Exception {
		PDOM pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireWriteLock(0);
		try {
			WritablePDOM wpdom = (WritablePDOM) pdom;
			wpdom.setProperty("a", "b");
			assertEquals("b", wpdom.getProperty("a"));
			wpdom.setProperty("c", "d");
			assertEquals("d", wpdom.getProperty("c"));
			wpdom.setProperty("c", "e");
			assertEquals("e", wpdom.getProperty("c"));
		} finally {
			pdom.releaseWriteLock(0, true);
		}
	}
	
	public void testProjectPDOMProperties() throws Exception {
		PDOM pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireReadLock();
		try {
			String id= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull(id);
			
			CCoreInternals.getPDOMManager().reindex(cproject);
			
			String id2= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull(id2);
			assertEquals(id, id2);
		} finally {
			pdom.releaseReadLock();
		}
	}
	
	public void testProjectPDOMPropertiesOnExport() throws Exception {
		// this test is currently failing on the cdt test build machine, but
		// not on my local linux or windows boxes.
		
		File tmp= new File(System.getProperty("java.io.tmpdir")+"/temp"+System.currentTimeMillis()+".pdom");
		IIndexLocationConverter cvr= new ResourceContainerRelativeLocationConverter(cproject.getProject());
		final PDOMManager pdomManager = CCoreInternals.getPDOMManager();
		pdomManager.exportProjectPDOM(cproject, tmp, cvr);
		
		IWritableIndexFragment pdom = new WritablePDOM(tmp, cvr, new ChunkCache(), LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
		pdom.acquireReadLock();
		try {
			String id= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull("Exported pdom ID is null", id);
			
			String id2 = getFragmentID(cproject);
			assertNotNull("Project pdom ID is null", id2);
			assertFalse("Project pdom ID equals export PDOM id", id2.equals(id));
			
			pdomManager.reindex(cproject);
			assertTrue(pdomManager.joinIndexer(4000, new NullProgressMonitor()));
			
			String id3= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull("Exported pdom ID is null after project reindex", id3);
			assertEquals("Exported pdom ID hasChanged during reindex", id, id3);
			
			String id4= getFragmentID(cproject);
			assertNotNull("Reindexed project pdom ID is null", id4);
			assertFalse("Reindexex project pdom ID equals exported pdom ID", id4.equals(id));
		} finally {
			pdom.releaseReadLock();
		}
	}

	private String getFragmentID(final ICProject cproject) throws CoreException, InterruptedException {
		PDOMManager pdomManager= CCoreInternals.getPDOMManager();
		final PDOM projectPDOM = (PDOM)pdomManager.getPDOM(cproject);
		String id2;
		projectPDOM.acquireReadLock();
		try {
			id2= (projectPDOM).getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
		}
		finally {
			projectPDOM.releaseReadLock();
		}
		return id2;
	}
	
	public void testInterruptingAcquireReadLock() throws Exception {
		final PDOM pdom= (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		final boolean[] ok= {false};
		pdom.acquireWriteLock();
		try {
			Thread other= new Thread() {
				@Override
				public void run() {
					try {
						pdom.acquireReadLock();
					} catch (InterruptedException e) {
						ok[0]= true;
					} 
				}
			};
			other.start();
			other.interrupt();
			other.join();
			assertTrue("thread was not interrupted", ok[0]);
		}
		finally {
			pdom.releaseWriteLock();
		}
		pdom.acquireWriteLock();
		pdom.releaseWriteLock();
	}
	
	public void testInterruptingAcquireWriteLock() throws Exception {
		final WritablePDOM pdom= (WritablePDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		final boolean[] ok= {false};
		pdom.acquireReadLock();
		try {
			Thread other= new Thread() {
				@Override
				public void run() {
					try {
						pdom.acquireReadLock();
						pdom.acquireWriteLock(1);
					} catch (InterruptedException e) {
						ok[0]= true;
						pdom.releaseReadLock();
					} 
				}
			};
			other.start();
			other.interrupt();
			other.join();
			assertTrue("thread was not interrupted", ok[0]);
		}
		finally {
			pdom.releaseReadLock();
		}
		pdom.acquireWriteLock();
		pdom.releaseWriteLock();
	}
	
	public void test191679() throws Exception {
		IProject project= cproject.getProject();
		IFolder cHeaders= cproject.getProject().getFolder("cHeaders");
		cHeaders.create(true, true, npm());
		LanguageManager lm= LanguageManager.getInstance();
		
		IFile cHeader= TestSourceReader.createFile(cHeaders, "cSource.c", "void foo(int i){}");		
		IFile cppSource= TestSourceReader.createFile(cHeaders, "cppSource.cpp", "extern \"C\" void foo(int i); void ref() {foo(1);}");
		
		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		CCorePlugin.getIndexManager().joinIndexer(10000, npm());
		
		final PDOM pdom= (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireReadLock();
		try {
			{ // test reference to 'foo' was resolved correctly
				IIndexBinding[] ib= pdom.findBindings(new char[][]{"foo".toCharArray()}, IndexFilter.ALL, npm());
				assertEquals(2, ib.length);
				if (ib[0] instanceof ICPPBinding) {
					IIndexBinding h= ib[0]; ib[0]= ib[1]; ib[1]= h;
				}
				assertTrue(ib[0] instanceof IFunction);
				assertFalse(ib[0] instanceof ICPPBinding);
				
				assertTrue(ib[1] instanceof IFunction);
				assertTrue(ib[1] instanceof ICPPBinding);
				
				IName[] nms= pdom.findNames(ib[0], IIndexFragment.FIND_REFERENCES | IIndexFragment.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
				assertEquals(1, nms.length);
				assertTrue(nms[0].getFileLocation().getFileName().endsWith(".cpp"));

				nms= pdom.findNames(ib[0], IIndexFragment.FIND_REFERENCES);
				assertEquals(0, nms.length);
				
				nms= pdom.findNames(ib[1], IIndexFragment.FIND_DEFINITIONS | IIndexFragment.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
				assertEquals(1, nms.length);
				assertTrue(nms[0].getFileLocation().getFileName().endsWith(".c"));

				nms= pdom.findNames(ib[1], IIndexFragment.FIND_DEFINITIONS);
				assertEquals(0, nms.length);
			}
		} finally {
			pdom.releaseReadLock();
		}
	}
}

