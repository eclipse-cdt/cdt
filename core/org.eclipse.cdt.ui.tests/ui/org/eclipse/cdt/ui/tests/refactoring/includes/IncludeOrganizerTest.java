/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.refactoring.includes.IHeaderChooser;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeOrganizer;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences.UnusedStatementDisposition;
import org.eclipse.cdt.internal.ui.refactoring.includes.SymbolExportMap;

/**
 * Tests for {@link IncludeOrganizer}.
 */
public class IncludeOrganizerTest extends IncludesTestBase {

	public IncludeOrganizerTest() {
		super();
	}

	public IncludeOrganizerTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(IncludeOrganizerTest.class);
	}

	@Override
	protected void resetPreferences() {
		super.resetPreferences();
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setToDefault(PreferenceConstants.INCLUDES_UNUSED_STATEMENTS_DISPOSITION);
		preferenceStore.setToDefault(PreferenceConstants.FORWARD_DECLARE_COMPOSITE_TYPES);
		preferenceStore.setToDefault(PreferenceConstants.FORWARD_DECLARE_ENUMS);
		preferenceStore.setToDefault(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS);
		preferenceStore.setToDefault(PreferenceConstants.FORWARD_DECLARE_TEMPLATES);
		preferenceStore.setToDefault(PreferenceConstants.FORWARD_DECLARE_NAMESPACE_ELEMENTS);
		preferenceStore.setToDefault(PreferenceConstants.INCLUDES_ALLOW_REORDERING);
	}

	private void assertExpectedResults() throws Exception {
		String actual = organizeIncludes(ast.getOriginatingTranslationUnit());
		assertEquals(selectedFile.getExpectedSource(), actual);
	}

	/**
	 * Invokes include organizer and returns the new contents of the translation unit.
	 */
	private String organizeIncludes(ITranslationUnit tu) throws Exception {
		IHeaderChooser headerChooser = new FirstHeaderChooser();
		IncludeOrganizer organizer = new IncludeOrganizer(tu, index, LINE_DELIMITER, headerChooser);
		List<TextEdit> edits = organizer.organizeIncludes(ast);
		IDocument document = new Document(new String(tu.getContents()));
		if (!edits.isEmpty()) {
			// Apply text edits.
			MultiTextEdit edit = new MultiTextEdit();
			edit.addChildren(edits.toArray(new TextEdit[edits.size()]));
			edit.apply(document);
		}
		return document.get();
	}

	//h1.h
	//typedef int my_type;

	//A.h
	//class A {
	//  my_type m1();
	//};

	//A.cpp
	//// Comment line 1
	//// Comment line 2
	//
	//// Comment for m1
	//my_type A::m1() {
	//  return 0;
	//}
	//====================
	//// Comment line 1
	//// Comment line 2
	//
	//#include "A.h"
	//
	//#include "h1.h"
	//
	//// Comment for m1
	//my_type A::m1() {
	//  return 0;
	//}
	public void testNoExistingIncludes() throws Exception {
		assertExpectedResults();
	}

	//B.h
	//class B {};

	//C.h
	//class C {};

	//A.h
	//#if !defined(INCLUDE_GUARD)
	//#define INCLUDE_GUARD
	//// Comment line 1
	//// Comment line 2
	//
	//// Comment for A
	//class A {
	//  B f;
	//  C m();
	//};
	//#endif  // INCLUDE_GUARD
	//====================
	//#if !defined(INCLUDE_GUARD)
	//#define INCLUDE_GUARD
	//// Comment line 1
	//// Comment line 2
	//
	//#include "B.h"
	//
	//class C;
	//
	//// Comment for A
	//class A {
	//  B f;
	//  C m();
	//};
	//#endif  // INCLUDE_GUARD
	public void testIncludeGuards() throws Exception {
		assertExpectedResults();
	}

	//B.h
	//template <typename T> class B {};

	//C.h
	//class C {};

	//A.h
	//#pragma once
	//namespace ns {
	//// Comment line 1
	//// Comment line 2
	//
	//// Comment for A
	//class A : public B<C> {};
	//}  // namespace ns
	//====================
	//#pragma once
	//
	//#include "B.h"
	//
	//class C;
	//
	//namespace ns {
	//// Comment line 1
	//// Comment line 2
	//
	//// Comment for A
	//class A : public B<C> {};
	//}  // namespace ns
	public void testPragmaOnce() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//typedef int Type1;

	//h2.h
	//class Type2 {};

	//h3.h
	//enum Type3 { ONE, TWO };

	//h4.h
	//class Unrelated {};

	//A.h
	//#include "h1.h"
	//class Type2;
	//enum class Type3;
	//extern Type1 f1();
	//extern Type2 f2();
	//extern Type3 f3();

	//A.cpp
	//// Comment
	//
	//#include "h2.h" /* Required */  // another comment
	//#include "h1.h"  // Unused
	//#include "h3.h"
	//#include "h5.h"  // Unresolved includes are preserved
	//#ifdef SOME_OTHER_TIME
	//#include "h4.h"  // Unused but unsafe to remove
	//#endif
	//
	//void test() {
	//  f1();
	//  f2();
	//  f3();
	//}
	//====================
	//// Comment
	//
	//#include "A.h"
	//
	////#include "h1.h"  // Unused
	//#include "h2.h" /* Required */  // another comment
	//#include "h3.h"
	//#include "h5.h"  // Unresolved includes are preserved
	//
	//#ifdef SOME_OTHER_TIME
	//#include "h4.h"  // Unused but unsafe to remove
	//#endif
	//
	//void test() {
	//  f1();
	//  f2();
	//  f3();
	//}
	public void testExistingIncludes() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//typedef int Type1;

	//h2.h
	//class Type2 {};

	//h3.h
	//enum class Type3 { ONE, TWO };

	//h4.h
	//class Unrelated {};

	//A.h
	//#include "h1.h"
	//class Type2;
	//enum class Type3;
	//extern Type1 f1();
	//extern Type2 f2();
	//extern Type3 f3();

	//A.cpp
	//// Comment
	//
	//#include "h2.h" /* Required */  // another comment
	//#include "h1.h"  // Unused
	//#include "h3.h"
	//#include "h5.h"  // Unresolved includes are preserved
	//#ifdef SOME_OTHER_TIME
	//#include "h4.h"  // Unused but unsafe to remove
	//#endif
	//
	//void test() {
	//  f1();
	//  f2();
	//  f3();
	//}
	//====================
	//// Comment
	//
	//#include "h2.h" /* Required */  // another comment
	////#include "h1.h"  // Unused
	//#include "h3.h"
	//#include "h5.h"  // Unresolved includes are preserved
	//#include "A.h"
	//
	//#ifdef SOME_OTHER_TIME
	//#include "h4.h"  // Unused but unsafe to remove
	//#endif
	//
	//void test() {
	//  f1();
	//  f2();
	//  f3();
	//}
	public void testExistingIncludesNoReordering() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.INCLUDES_ALLOW_REORDERING, false);
		assertExpectedResults();
	}

	//h1.h
	//class A {};

	//h2.h
	//class B {};

	//h3.h
	//#include "h2.h"	// IWYU pragma: export
	//class C {};

	//h4.h
	//#include "h1.h"
	///* IWYU pragma: begin_exports */
	//#include "h3.h"
	///* IWYU pragma: end_exports */
	//class D {};

	//source.cpp
	//A a;
	//B b;
	//C c;
	//D d;
	//====================
	//#include "h1.h"
	//#include "h4.h"
	//
	//A a;
	//B b;
	//C c;
	//D d;
	public void testHeaderExport() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//#define M2(t, p) t p

	//h2.h
	//#include "h1.h"
	//#define M1(x, y) M2(int, x) = y

	//h3.h
	//#include "h2.h"

	//source.cpp
	//#include "h3.h"
	//M1(a, 1);
	//====================
	//#include "h2.h"
	//
	//M1(a, 1);
	public void testMacro() throws Exception {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.INCLUDES_UNUSED_STATEMENTS_DISPOSITION,
				UnusedStatementDisposition.REMOVE.toString());
		assertExpectedResults();
	}

	//string.h
	//#include "stddef.h"
	//extern char* strchr(char* s, int c);

	//stddef.h
	//#define NULL 0

	//source.cpp
	//#include "stddef.h"
	//char* test() {
	//  int* p = NULL;
	//  return strchr("aaa", '*');
	//}
	//====================
	//#include "string.h"
	//
	//char* test() {
	//  int* p = NULL;
	//  return strchr("aaa", '*');
	//}
	public void testExportedSymbol() throws Exception {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.INCLUDES_UNUSED_STATEMENTS_DISPOSITION,
				UnusedStatementDisposition.REMOVE.toString());
		SymbolExportMap symbolExportMap = new SymbolExportMap(new String[] { "NULL", "string.h" });
		preferenceStore.setValue(PreferenceConstants.INCLUDES_SYMBOL_EXPORTING_HEADERS,
				SymbolExportMap.serializeMaps(Collections.singletonList(symbolExportMap)));
		assertExpectedResults();
	}
}