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

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMProjectIndexLocationConverter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.framework.Bundle;

/**
 * Tests behaviour related to location representation in the PDOM
 */
public class PDOMLocationTests extends BaseTestCase {
	ICProject cproject;
	
	protected void setUp() throws Exception {
		cproject= CProjectHelper.createCCProject("PDOMLocationTests"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
		
		Bundle b = CTestPlugin.getDefault().getBundle();
		StringBuffer[] testData = TestSourceReader.getContentsForTest(b, "parser", getClass(), getName(), 3);
		
		super.setUp();
	}

	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		super.tearDown();
	}

	public void testLocationConverter() {
		PDOMProjectIndexLocationConverter converter = new PDOMProjectIndexLocationConverter(cproject.getProject());
		String[] externals = new String[] {
				"c:/a/b/c/d.foo",
				"c:\\a\\b\\c\\d\\e.foo",
				"d:/foo.bar",
				"d:\\Documents and Settings\\JDoe\\Eclipse Workspaces\\ProjectX\\foo.bar",
				"/home/jdoe/eclipse workspaces/projectx/foo.bar"
		};
		for(int i=0; i<externals.length; i++) {
			IIndexFileLocation loc = IndexLocationFactory.getExternalIFL(externals[i]);
			String raw = converter.toInternalFormat(loc);
			IIndexFileLocation roundtrip = converter.fromInternalFormat(raw);
			assertTrue(roundtrip!=null);
			assertEquals(roundtrip.getFullPath(), loc.getFullPath());
			assertEquals(roundtrip.getURI(), loc.getURI());
		}
	}
}
