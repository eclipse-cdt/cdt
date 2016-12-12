/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.CSearchTextSelectionQuery;
import org.eclipse.cdt.internal.ui.search.LineSearchElement;
import org.eclipse.cdt.internal.ui.search.LineSearchElement.Match;

import junit.framework.TestSuite;

/**
 * Test the "Find References" source navigation feature. The code implementing
 * this feature is org.eclipse.cdt.internal.ui.search.actions.FindRefsAction,
 * although this test operates one level lower, creating a
 * CSearchTextSelectionQuery directly.
 */
public class FindReferencesTest extends SearchTestBase {
	public static TestSuite suite() {
		return suite(FindReferencesTest.class);
	}

	private CSearchQuery makeProjectQuery(int offset, int length) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = null;
		try {
			part = page.openEditor(new FileEditorInput(fHeaderFile), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
		} catch (PartInitException e) {
			assertFalse(true);
		}
		assertInstance(part, CEditor.class);
		CEditor editor = (CEditor) part;
		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 5000, 10);
		ITranslationUnit tu = editor.getInputCElement();
		return new CSearchTextSelectionQuery(new ICElement[] { fCProject }, tu, new TextSelection(offset, length),
				CSearchQuery.FIND_REFERENCES);
	}

	//	struct A {
	//		virtual void waldo();
	//	};
	//
	//	struct B : public A {
	//		virtual void waldo() override;
	//	};
	//
	//	int main() {
	//		A* a = new B();
	//		a->waldo();
	//	}

	// // empty file
	public void testOnlyPolymorphicMatches_bug491343() throws Exception {
		int offset = fHeaderContents.indexOf("waldo() override");
		CSearchQuery query = makeProjectQuery(offset, 5);
		assertOccurrences(query, 1);
	}
	

	//	#define waldo()
	//
	//	struct S {
	//		void foo() {
	//			waldo();
	//		}
	//	};
	
	//	// empty file
	public void testEnclosingDefinitionOfMacroReference_508216() throws Exception {
		int offset = fHeaderContents.indexOf("define waldo") + 7;
		CSearchQuery query = makeProjectQuery(offset, 5);
		CSearchResult result = getSearchResult(query);
		Object[] elements = result.getElements();
		assertEquals(1, elements.length);
		assertInstance(elements[0], LineSearchElement.class);
		Match[] matches = ((LineSearchElement) elements[0]).getMatches();
		assertEquals(1, matches.length);
		assertNotNull(matches[0].getEnclosingElement());
	}
	
	//	namespace N {
	//		void foo();
	//	}
	//	using N::foo;
	
	//	// empty file
	public void testUsingDeclaration_399147() throws Exception {
		int offset = fHeaderContents.indexOf("void foo") + 5;
		CSearchQuery query = makeProjectQuery(offset, 3);
		assertOccurrences(query, 1);
	}
}
