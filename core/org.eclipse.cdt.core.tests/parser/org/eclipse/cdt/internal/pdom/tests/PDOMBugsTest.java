/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests bugs found in the PDOM
 */
public class PDOMBugsTest extends BaseTestCase {
	ICProject cproject;
	
	public static Test suite() {
		return suite(PDOMBugsTest.class);
	}
	
	protected void setUp() throws Exception {
		cproject= CProjectHelper.createCCProject("PDOMBugsTest"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);		
		super.setUp();
	}

	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}

	public void testPDOMProperties() throws Exception {
		IWritableIndexFragment pdom = (IWritableIndexFragment) CCoreInternals.getPDOMManager().getPDOM(cproject);
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
			pdom.releaseWriteLock(0);
		}
	}
	
	public void testProjectPDOMProperties() throws Exception {
		IWritableIndexFragment pdom = (IWritableIndexFragment) CCoreInternals.getPDOMManager().getPDOM(cproject);
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
		File tmp= new File(System.getProperty("java.io.tmpdir")+"/temp"+System.currentTimeMillis()+".pdom");
		IIndexLocationConverter cvr= new ResourceContainerRelativeLocationConverter(cproject.getProject());
		CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, tmp, cvr);
		
		IWritableIndexFragment pdom = new WritablePDOM(tmp, cvr);
		pdom.acquireReadLock();
		try {
			String id= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull(id);
			
			String id2= ((PDOM)CCoreInternals.getPDOMManager().getPDOM(cproject)).getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull(id2);
			assertFalse(id2.equals(id));
			
			CCoreInternals.getPDOMManager().reindex(cproject);
			
			String id3= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull(id3);
			assertEquals(id,id3);
			
			String id4= ((PDOM)CCoreInternals.getPDOMManager().getPDOM(cproject)).getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			assertNotNull(id4);
			assertFalse(id4.equals(id));
		} finally {
			pdom.releaseReadLock();
		}
	}
}
