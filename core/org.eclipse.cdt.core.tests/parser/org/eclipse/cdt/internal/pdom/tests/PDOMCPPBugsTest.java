/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.cdt.internal.core.pdom.IPDOM;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Tests bugs found in the PDOM
 */
public class PDOMCPPBugsTest extends BaseTestCase {
	ICProject cproject;
	
	public static Test suite() {
		return suite(PDOMCPPBugsTest.class);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		cproject= CProjectHelper.createCCProject("PDOMBugsTest"+System.currentTimeMillis(), "bin", IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(8000, NPM));
	}

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

	// #define ONE 1
	// #define TWO() 2
	// #define THREE(X) 3
	// #define FOUR(...) 4
	public void testBug211986() throws Exception {
		IProject project= cproject.getProject();
		String content= getContentsForTest(1)[0].toString();
		IFile cHeader= TestSourceReader.createFile(project, "macros.cpp", content);

		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		CCorePlugin.getIndexManager().joinIndexer(10000, NPM);

		Field MACRO_STYLE= PDOMMacro.class.getDeclaredField("MACRO_STYLE");
		MACRO_STYLE.setAccessible(true);
		int msOffset= ((Integer) MACRO_STYLE.get(null)).intValue();
		
		{
			PDOM pdom= (PDOM) ((PDOMManager) CCorePlugin.getIndexManager()).getPDOM(cproject);
			pdom.acquireWriteLock();
			try {
				pdom.getDB().setVersion(38); // the version before MACROSTYLE bytes were introduced
				pdom.getDB().putByte(PDOM.HAS_READABLE_MACRO_STYLE_BYTES, (byte) 0);
				
				PDOMMacro ONE= getMacro(pdom, "ONE"), TWO= getMacro(pdom, "TWO");
				PDOMMacro THREE= getMacro(pdom, "THREE"), FOUR= getMacro(pdom, "FOUR"); 
				assertEquals("1", new String(ONE.getExpansion()));
				assertEquals("2", new String(TWO.getExpansion()));
				assertEquals("3", new String(THREE.getExpansion()));
				assertEquals("4", new String(FOUR.getExpansion()));
				pdom.getDB().putByte(ONE.getRecord() + msOffset, (byte) 0xFF); // emulate non-zeroed data from incremental indexing
				pdom.getDB().putByte(TWO.getRecord() + msOffset, (byte) 0xFF); // emulate non-zeroed data from incremental indexing
				pdom.getDB().putByte(THREE.getRecord() + msOffset, (byte) 0xFF); // emulate non-zeroed data from incremental indexing
				pdom.getDB().putByte(FOUR.getRecord() + msOffset, (byte) 0xFF); // emulate non-zeroed data from incremental indexing
			} finally {
				pdom.releaseWriteLock();
			}
		}
		cproject.close();
		project.close(NPM);
		
		CCorePlugin.getIndexManager().joinIndexer(5000, NPM);
		
		project.open(NPM);
		cproject.open(NPM);
		
		CCorePlugin.getIndexManager().joinIndexer(5000, NPM);
		
		IPDOM pdom= ((PDOMManager) CCorePlugin.getIndexManager()).getPDOM(cproject);
		pdom.acquireReadLock();
		try {
			PDOMMacro ONE= getMacro(pdom, "ONE"), TWO= getMacro(pdom, "TWO");
			PDOMMacro THREE= getMacro(pdom, "THREE"), FOUR= getMacro(pdom, "FOUR"); 
			assertEquals("1", new String(ONE.getExpansion()));
			assertTrue(ONE.getMacro() instanceof ObjectStyleMacro);
			assertEquals("2", new String(TWO.getExpansion()));
			
			 // we are testing for the old (incorrect) behaviour here
			assertTrue(TWO.getMacro() instanceof ObjectStyleMacro);
			
			assertEquals("3", new String(THREE.getExpansion()));
			assertTrue(THREE.getMacro() instanceof FunctionStyleMacro);
			assertEquals("4", new String(FOUR.getExpansion()));
			assertTrue(FOUR.getMacro() instanceof FunctionStyleMacro);
		} finally {
			pdom.releaseReadLock();
		}
	}
	
	/**
	 * Assumes locks handled externally. 
	 * @param pdom
	 * @param name
	 * @return single macro with specified name
	 */
	private PDOMMacro getMacro(IPDOM pdom, String name) throws CoreException {
		IIndexMacro[] ms= pdom.findMacros(name.toCharArray(), false, true, IndexFilter.ALL, NPM);
		assertEquals(1, ms.length);
		return (PDOMMacro) ms[0];
	}

	public void _test191679() throws Exception {
		IProject project= cproject.getProject();
		IFolder cHeaders= cproject.getProject().getFolder("cHeaders");
		cHeaders.create(true, true, NPM);
		LanguageManager lm= LanguageManager.getInstance();
		
		IFile cHeader= TestSourceReader.createFile(cHeaders, "cHeader.h", "extern \"C\" void foo(int i) {}\n");
		ICProjectDescription pd= CCorePlugin.getDefault().getProjectDescription(project);
		ICConfigurationDescription cfgd= pd.getDefaultSettingConfiguration();
		ProjectLanguageConfiguration plc= LanguageManager.getInstance().getLanguageConfiguration(project);
		plc.addFileMapping(cfgd, cHeader, GCCLanguage.ID);
		IContentType ct= Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_CHEADER);
		lm.storeLanguageMappingConfiguration(project, new IContentType[] {ct});
		
		IFile cppSource= TestSourceReader.createFile(cHeaders, "cppSource.cpp", "void ref() {foo(1);}");
		
		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		CCorePlugin.getIndexManager().joinIndexer(10000, NPM);
		
		final PDOM pdom= (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireReadLock();
		try {
			{ // test reference to 'foo' was resolved correctly
				IIndexBinding[] ib= pdom.findBindings(new char[][]{"foo".toCharArray()}, IndexFilter.ALL, NPM);
				assertEquals(1, ib.length);
				
				assertTrue(ib[0] instanceof IFunction);
				assertTrue(!(ib[0] instanceof ICPPBinding));
				
				IName[] nms= pdom.findNames(ib[0], IIndexFragment.FIND_REFERENCES);
				assertEquals(1, nms.length);
				assertTrue(nms[0].getFileLocation().getFileName().endsWith(".cpp"));
			}
		} finally {
			pdom.releaseReadLock();
		}
	}
	
	protected StringBuffer[] getContentsForTest(int blocks) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), blocks);
	}
}

