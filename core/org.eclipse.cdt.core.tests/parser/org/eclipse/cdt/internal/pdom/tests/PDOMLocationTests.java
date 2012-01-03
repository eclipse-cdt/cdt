/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMProjectIndexLocationConverter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * Tests behavior related to location representation in the PDOM
 */
public class PDOMLocationTests extends BaseTestCase {
	ICProject cproject;
	
	public static Test suite() {
		return suite(PDOMLocationTests.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		cproject= CProjectHelper.createCCProject("PDOMLocationTests"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}

	public void testLocationConverter() {
		PDOMProjectIndexLocationConverter converter = new PDOMProjectIndexLocationConverter(cproject.getProject());
		String[] winExternals= new String[] {
				"c:/a/b/c/d.foo",
				"c:\\a\\b\\c\\d\\e.foo",
				"d:/foo.bar",
				"d:\\Documents and Settings\\JDoe\\Eclipse Workspaces\\ProjectX\\foo.bar"
		};
		String[] linuxExternals = new String[] {
				"/home/jdoe/workspaces/projectx/foo",
				"/home/jdoe/eclipse workspaces/projectx/foo.bar"
		};
		
		Set<String> externals= new HashSet();
		externals.addAll(Arrays.asList(linuxExternals));
		if(Platform.getOS().equals("win32")) {
			externals.addAll(Arrays.asList(winExternals));
		}
		
		for(String ext : externals) {
			IIndexFileLocation loc = IndexLocationFactory.getExternalIFL(ext);
			String raw = converter.toInternalFormat(loc);
			IIndexFileLocation roundtrip = converter.fromInternalFormat(raw);
			assertTrue(roundtrip!=null);
			assertEquals(roundtrip.getFullPath(), loc.getFullPath());
			assertEquals(roundtrip.getURI(), loc.getURI());
		}
	}
}
