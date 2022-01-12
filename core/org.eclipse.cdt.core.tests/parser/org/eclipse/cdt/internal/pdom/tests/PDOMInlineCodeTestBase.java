/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

/**
 * @author Thomas Corbat
 *
 * Base class for PDOM tests relying on code placed in comments in front
 * of the test.
 */
public class PDOMInlineCodeTestBase extends PDOMTestBase {
	protected PDOM pdom;
	protected ICProject cproject;

	@Override
	public void setUp() throws Exception {
		cproject = CProjectHelper.createCCProject("classTemplateTests" + System.currentTimeMillis(), "bin",
				IPDOMManager.ID_NO_INDEXER);
	}

	protected void setUpSections(int sections) throws Exception {
		CharSequence[] contents = TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "parser",
				getClass(), getName(), sections);
		for (CharSequence content : contents) {
			IFile file = TestSourceReader.createFile(cproject.getProject(), new Path("refs.cpp"), content.toString());
		}
		IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(cproject);
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		if (pdom != null) {
			pdom.releaseReadLock();
		}
		pdom = null;
		cproject.getProject().delete(true, npm());
	}
}