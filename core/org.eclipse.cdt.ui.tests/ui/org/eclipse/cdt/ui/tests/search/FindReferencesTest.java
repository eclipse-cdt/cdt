/*******************************************************************************
 * Copyright (c) 2016, 2017 Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial implementation
 *     Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.CSearchTextSelectionQuery;
import org.eclipse.cdt.internal.ui.search.LineSearchElement;
import org.eclipse.cdt.internal.ui.search.LineSearchElement.Match;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import junit.framework.TestSuite;

/**
 * Test the "Find References" source navigation feature. The code implementing
 * this feature is org.eclipse.cdt.internal.ui.search.actions.FindRefsAction,
 * although this test operates one level lower, creating a
 * CSearchTextSelectionQuery directly.
 */
public class FindReferencesTest extends SearchTestBase {
	public static class SingleProject extends FindReferencesTest {
		public SingleProject() {
			setStrategy(new SingleProjectStrategy());
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	public static class ReferencedProject extends FindReferencesTest {
		public ReferencedProject() {
			setStrategy(new ReferencedProjectStrategy());
		}

		public static TestSuite suite() {
			return suite(ReferencedProject.class);
		}
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(SingleProject.class);
		suite.addTestSuite(ReferencedProject.class);
		return suite;
	}

	public FindReferencesTest() {
		// For convenience, to be able to run tests via right click -> Run As -> JUnit Plugin Test.
		// Will use the SingleProjectStrategy when run this way.
		setStrategy(new SingleProjectStrategy());
	}

	private CSearchQuery makeSearchQuery(IFile file, TextSelection selection) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = null;
		try {
			part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
		} catch (PartInitException e) {
			assertFalse(true);
		}
		assertInstance(part, CEditor.class);
		CEditor editor = (CEditor) part;
		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 5000, 10);
		ITranslationUnit tu = editor.getInputCElement();
		return new CSearchTextSelectionQuery(fStrategy.getScope(), tu, selection, CSearchQuery.FIND_REFERENCES);
	}

	private TextSelection selectSection(String section, String context, String code) {
		int contextOffset;
		if (context == null) {
			context = code;
			contextOffset = 0;
		} else {
			contextOffset = code.indexOf(context);
			if (contextOffset < 0)
				fail("Didn't find \"" + context + "\" in \"" + code + "\"");
		}
		int offset = context.indexOf(section);
		if (offset < 0)
			fail("Didn't find \"" + section + "\" in \"" + context + "\"");
		return new TextSelection(contextOffset + offset, section.length());
	}

	//	struct A {
	//	  virtual void waldo();
	//	};
	//
	//	struct B : public A {
	//	  virtual void waldo() override;
	//	};
	//
	//	int main() {
	//	  A* a = new B();
	//	  a->waldo();
	//	}

	// // empty file
	public void testOnlyPolymorphicMatches_bug491343() throws Exception {
		CSearchQuery query = makeSearchQuery(fHeaderFile, selectSection("waldo", "waldo() override", fHeaderContents));
		assertOccurrences(query, 1);
	}

	//	#define waldo()
	//
	//	struct S {
	//	  void foo() {
	//	    waldo();
	//	  }
	//	};

	//	// empty file
	public void testEnclosingDefinitionOfMacroReference_508216() throws Exception {
		CSearchQuery query = makeSearchQuery(fHeaderFile, selectSection("waldo", "#define waldo", fHeaderContents));
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
		CSearchQuery query = makeSearchQuery(fHeaderFile, selectSection("foo", "void foo", fHeaderContents));
		assertOccurrences(query, 1);
	}

	//	// empty file

	//	namespace { struct A {}; }
	//
	//	template <typename T>
	//	class B {};
	//
	//	void findMe(B<A> b) {}
	//
	//	void test(B<A> b) {
	//	  findMe(b);
	//	}
	public void testAnonymousNamespace_509749() throws Exception {
		CSearchQuery query = makeSearchQuery(fSourceFile, selectSection("findMe", "findMe(b)", fSourceContents));
		assertOccurrences(query, 1);
	}

	//	template <typename T>
	//	class Waldo {
	//		void find();
	//	};

	//	#include "header.h"
	//	void foo() {
	//		Waldo<int> waldo;
	//		waldo.find();
	//	}
	public void testMethodOfClassTemplate_509734() throws Exception {
		CSearchQuery query = makeSearchQuery(fHeaderFile, selectSection("find", "void find()", fHeaderContents));
		assertOccurrences(query, 1);
	}
}
