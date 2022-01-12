/*******************************************************************************
 * Copyright (c) 2013, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import java.util.Collections;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.includes.HeaderSubstitutionMap;
import org.eclipse.cdt.internal.ui.refactoring.includes.IHeaderChooser;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeMap;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeOrganizer;
import org.eclipse.cdt.internal.ui.refactoring.includes.SymbolExportMap;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

import junit.framework.Test;

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
		preferenceStore.setToDefault(PreferenceConstants.INCLUDES_HEADER_SUBSTITUTION);
		preferenceStore.setToDefault(PreferenceConstants.INCLUDES_SYMBOL_EXPORTING_HEADERS);
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
		IncludeOrganizer organizer = new IncludeOrganizer(tu, index, headerChooser);
		MultiTextEdit edit = organizer.organizeIncludes(ast);
		IDocument document = new Document(new String(tu.getContents()));
		edit.apply(document);
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
	//#include "C.h"
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

	//f.h
	//void f(int p);

	//f.cpp
	//#include "f.h"
	//void f(int p) {
	//}
	//====================
	//#include "f.h"
	//
	//void f(int p) {
	//}
	public void testExistingPartnerIncludeIsNotRemoved() throws Exception {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertExpectedResults();
	}

	//a.impl
	//class A {};

	//a.h
	//#include "a.impl"  // Non-header file extension

	//b.inc
	//class B {};

	//b.h
	//#include "b.inc"   // Auto-exported header file extension

	//c.inc
	//class C {};

	//test.cpp
	//A a;
	//B b;
	//C c;
	//====================
	//#include "a.h"
	//#include "b.h"
	//#include "c.inc"
	//
	//A a;
	//B b;
	//C c;
	public void testNonStandardFileExtensions() throws Exception {
		assertExpectedResults();
	}

	//a.h
	//class A {};

	//f.h
	//#include "a.h"

	//f.cpp
	//#include "f.h"
	//A a;
	//====================
	//#include "f.h"
	//
	//A a;
	public void testPartnerInclusion() throws Exception {
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

	//dir1/private1.h
	///** @file dir1/private1.h
	// *  This is an internal header file, included by other library headers.
	// *  Do not attempt to use it directly. @headername{dir1/public1.h}
	// */
	//class A {};

	//dir1/public1.h
	//#include "private1.h"

	//dir1/private2.h
	//// IWYU pragma: private,
	//// include "dir1/public2.h"
	//class B {};

	//dir1/public2.h
	//#include "private2.h"

	//dir1/private3.h
	//// IWYU pragma: private
	//class C {};

	//dir1/public3.h
	//#include "private3.h"

	//dir2/private4.h
	//// IWYU pragma: private, include "dir2/public4.h"
	//class D {};

	//dir2/public4.h
	//#include "private4.h"

	//dir2/source.cpp
	//A a;
	//B b;
	//C c;
	//D d;
	//====================
	//#include "dir1/public1.h"
	//#include "dir1/public2.h"
	//#include "dir1/public3.h"
	//#include "dir2/private4.h"
	//
	//A a;
	//B b;
	//C c;
	//D d;
	public void testPrivateHeaders() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//class A {};

	//h2.h
	//#include "h1.h"	// IWYU pragma: export
	//class B {};

	//h3.h
	//#include "h2.h"

	//source.cpp
	//A a;
	//B b;
	//====================
	//#include "h3.h"
	//
	//A a;
	//B b;
	public void testIndirectHeaderExport() throws Exception {
		HeaderSubstitutionMap headerMap = new HeaderSubstitutionMap("Test", false,
				new IncludeMap(true, new String[] { "h2.h", "h3.h" }), new IncludeMap(false));
		getPreferenceStore().setValue(PreferenceConstants.INCLUDES_HEADER_SUBSTITUTION,
				HeaderSubstitutionMap.serializeMaps(Collections.singletonList(headerMap)));
		assertExpectedResults();
	}

	//h1.h

	//h2.h

	//h3.h

	//source.cpp
	//#include "h1.h"
	//#include "h2.h"  //  IWYU pragma: keep
	//#include "h3.h"  /*   IWYU pragma: keep   */
	//====================
	//#include "h2.h"  //  IWYU pragma: keep
	//#include "h3.h"  /*   IWYU pragma: keep   */
	public void testPragmaKeep() throws Exception {
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
		SymbolExportMap symbolExportMap = new SymbolExportMap(new String[] { "NULL", "string.h" });
		preferenceStore.setValue(PreferenceConstants.INCLUDES_SYMBOL_EXPORTING_HEADERS,
				SymbolExportMap.serializeMaps(Collections.singletonList(symbolExportMap)));
		assertExpectedResults();
	}

	//h1.h
	//class A {};
	//class B;

	//h2.h
	//class C {};

	//source.cpp
	//#include "h2.h"
	//A a;
	//B* b;
	//C* c;
	//====================
	//#include "h1.h"
	//
	//class C;
	//
	//A a;
	//B* b;
	//C* c;
	public void testSymbolToDeclareIsDefinedInIncludedHeader() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//typedef int int32;

	//h2.h
	//#include "h1.h"
	//extern int32 var;

	//source.cpp
	//int a = var;
	//====================
	//#include "h2.h"
	//
	//int a = var;
	public void testVariableReference() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//struct A {
	//  template <typename T>
	//  void m(const T& p);
	//};

	//h2.h
	//#include "h1.h"
	//template<typename T>
	//void A::m(const T& p) {
	//}

	//h3.h
	//#include "h1.h"
	//typedef A B;

	//source.cpp
	//void test(B& b) {
	//  b.m(1);
	//}
	//====================
	//#include "h2.h"
	//#include "h3.h"
	//
	//void test(B& b) {
	//  b.m(1);
	//}
	public void testMethodDefinedInHeader() throws Exception {
		assertExpectedResults();
	}

	//a.h
	//struct A {
	//  void a() const;
	//};

	//b.h
	//#include "a.h"
	//struct B : public A {
	//};

	//c.h
	//class B;
	//
	//struct C {
	//  const B& c() const;
	//};

	//source.cpp
	//void test(const C& x) {
	//  x.c().a();
	//}
	//====================
	//#include "b.h"
	//#include "c.h"
	//
	//void test(const C& x) {
	//  x.c().a();
	//}
	public void testMethodCall_488349() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//struct A {
	//  void operator()();
	//};
	//struct B : public A {
	//};

	//h2.h
	//#include "h1.h"
	//
	//struct C {
	//  B b;
	//};

	//source.cpp
	//void test(C* c) {
	//  c->b();
	//}
	//====================
	//#include "h2.h"
	//
	//void test(C* c) {
	//  c->b();
	//}
	public void testFieldAccess_442841() throws Exception {
		assertExpectedResults();
	}

	//h1.h
	//namespace ns3 {
	//class C {};
	//namespace ns2 {
	//class A {};
	//class B {};
	//namespace ns1 {
	//C* f(const A& a, B* b);
	//} // ns1
	//} // ns2
	//} // ns3

	//source.cpp
	//#include "h1.h"
	//void test(ns3::ns2::A& a) {
	//  ns3::C* c = ns3::ns2::ns1::f(a, nullptr);
	//}
	//====================
	//namespace ns3 {
	//class C;
	//namespace ns2 {
	//class A;
	//class B;
	//} /* namespace ns2 */
	//} /* namespace ns3 */
	//namespace ns3 {
	//namespace ns2 {
	//namespace ns1 {
	//C* f(const A &a, B *b);
	//} /* namespace ns1 */
	//} /* namespace ns2 */
	//} /* namespace ns3 */
	//
	//void test(ns3::ns2::A& a) {
	//  ns3::C* c = ns3::ns2::ns1::f(a, nullptr);
	//}
	public void testForwardDeclarations() throws Exception {
		// TODO(sprigogin): Move ns1 outside of other namespaces after IncludeOrganizer starts using ASTWriter.
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertExpectedResults();
	}

	//h1.h
	//class A;
	//
	//A& f();

	//h2.h
	//struct A {
	//  void m();
	//};

	//source.cpp
	//#include "h1.h"
	//#include "h2.h"
	//
	//template <typename T>
	//void g(T& p) {
	//  p.m();
	//}
	//
	//void test() {
	//  g(f());
	//}
	//====================
	//#include "h1.h"
	//#include "h2.h"
	//
	//template <typename T>
	//void g(T& p) {
	//  p.m();
	//}
	//
	//void test() {
	//  g(f());
	//}
	public void testTemplateParameter_514197() throws Exception {
		assertExpectedResults();
	}
}
