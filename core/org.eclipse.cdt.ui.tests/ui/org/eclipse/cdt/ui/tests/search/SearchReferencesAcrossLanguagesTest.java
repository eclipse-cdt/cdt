/*******************************************************************************
 * Copyright (c) 2015 Wei Li
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wei Li - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.tests.BaseUITestCase;


import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.CSearchTextSelectionQuery;

import junit.framework.TestSuite;

public class SearchReferencesAcrossLanguagesTest extends BaseUITestCase {

	protected ICProject fCProject;
	protected IIndex fIndex;

	public SearchReferencesAcrossLanguagesTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(SearchReferencesAcrossLanguagesTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject = CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(fCProject);
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}

	@Override
	protected void tearDown() throws Exception {
		closeAllEditors();
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}

	protected IProject getProject() {
		return fCProject.getProject();
	}

	// typedef struct foo_s{
	//     int m1;
	// } foo_t;

	// #include "405678.h"
	// void bar_c() {
	//     foo_t foo;
	//     foo.m1 = 2;
	// }

	// #include "405678.h"
	// void bar_cpp() {
	//     foo_t foo;
	//     foo.m1 = 1;
	// }	 
	public void testSearchReferencesAcrossLangs_405678() throws Exception {
		final StringBuilder[] contents = getContentsForTest(3);
		final String hcontent = contents[0].toString();
		final String ccontent = contents[1].toString();
		final String cppcontent = contents[2].toString();
		IFile f_h= createFile(getProject(), "405678.h", hcontent);
		IFile f_c= createFile(getProject(), "405678.c", ccontent);
		IFile f_cpp= createFile(getProject(), "405678.cpp", cppcontent);
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.update(new ICElement[] {fCProject}, IIndexManager.UPDATE_ALL);
		waitForIndexer(fCProject);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		assertNotNull(page);
		IEditorPart editor = IDE.openEditor(page, f_h, CUIPlugin.EDITOR_ID);
		assertNotNull(editor);
		CEditor ceditor = (CEditor) editor.getAdapter(CEditor.class);
		assertNotNull(ceditor);

		ceditor.selectAndReveal(hcontent.indexOf("m1"), 2);
		ISelection sel = ceditor.getSelectionProvider().getSelection();

		// Now a query is created and executed.
		CSearchTextSelectionQuery query = new CSearchTextSelectionQuery(null, ceditor.getInputCElement(), (ITextSelection) sel, IIndex.FIND_REFERENCES);

		IStatus status = null;
		long end_ms = System.currentTimeMillis() + 1000;
		do {
			status = query.run(npm());
			if (status == Status.CANCEL_STATUS) {
				Thread.sleep(100);
			}
		} while(!status.isOK() && System.currentTimeMillis() < end_ms);
		assertTrue("query failed: " + status.getMessage(), status.isOK());

		ISearchResult result = query.getSearchResult();
		assertNotNull(result);
		assertTrue(result instanceof CSearchResult);

		// The query should have found two references, one in the c source file and another
		// in the cpp source file
		CSearchResult searchResult = (CSearchResult) result;
		assertEquals(2, searchResult.getMatchCount());
	}
}
