/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestSuite;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.util.OneSourceMultipleHeadersTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.ui.refactoring.includes.BindingClassifier;
import org.eclipse.cdt.internal.ui.refactoring.includes.InclusionContext;

/**
 * Tests for {@link BindingClassifier}.
 */
public class BindingClassifierTest extends OneSourceMultipleHeadersTestCase {
	private IIndex fIndex;
	private InclusionContext fContext;
	private BindingClassifier fBindingClassifier;

	public BindingClassifierTest() {
		super(new TestSourceReader(CTestPlugin.getDefault().getBundle(), "ui", BindingClassifierTest.class), true);
	}

	public static TestSuite suite() {
		return suite(BindingClassifierTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp(true);
		IASTTranslationUnit ast = getAst();
		fIndex = CCorePlugin.getIndexManager().getIndex(getCProject(),
				IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
		fIndex.acquireReadLock();
		ITranslationUnit tu = ast.getOriginatingTranslationUnit();
		fContext = new InclusionContext(tu, fIndex);
		fBindingClassifier = new BindingClassifier(fContext);
		fBindingClassifier.classifyNodeContents(ast);
	}

	@Override
	protected void tearDown() throws Exception {
		fIndex.releaseReadLock();
		super.tearDown();
	}

	private void assertDefined(String... names) {
		assertExpectedBindings(names, fBindingClassifier.getBindingsToDefine(), "defined");
	}

	private void assertDeclared(String... names) {
		assertExpectedBindings(names, fBindingClassifier.getBindingsToDeclare(), "declared");
	}

	private void assertExpectedBindings(String[] expectedNames, Set<IBinding> bindings, String verb) {
		Set<String> expected = new TreeSet<String>(Arrays.asList(expectedNames));
		Set<String> extra = new TreeSet<String>();
		for (IBinding binding : bindings) {
			extra.add(binding.getName());
		}
		Set<String> missing = new TreeSet<String>(expected);
		missing.removeAll(extra);
		extra.removeAll(expected);
		if (extra.isEmpty() && missing.isEmpty())
			return;
		List<String> errors = new ArrayList<String>(2);
		if (!missing.isEmpty()) {
			errors.add(MessageFormat.format("{0,choice,1#Binding|1<Bindings} \"{1}\" {0,choice,1#is|1<are} not {2}.",
					missing.size(), join(missing, "\", \""), verb));
		}
		if (!extra.isEmpty()) {
			errors.add(MessageFormat.format("{0,choice,1#Binding|1<Bindings} \"{1}\" should not be {2}.",
					extra.size(), join(extra, "\", \""), verb));
		}
		fail(join(errors, " "));
	}

	private String join(Iterable<String> strings, String delimiter) {
		StringBuilder buf = new StringBuilder();
		for (String str : strings) {
			if (buf.length() != 0)
				buf.append(delimiter);
			buf.append(str);
		}
		return buf.toString();
	}

	//	class A;
	//	typedef A* td1;
	//	typedef td1* td2;
	//	td2 f();

	//	A* a = *f();
	public void testTypedef_1() throws Exception {
		assertDefined("f");
		assertDeclared("A");
	}

	//	class A;
	//	typedef A* td1;
	//	typedef td1* td2;
	//	td2 f();

	//	td1 a = *f();
	public void testTypedef_2() throws Exception {
		assertDefined("f", "td1");
		assertDeclared();
	}

	//	template<typename T> struct allocator {};
	//	template<typename T, typename U = allocator<T>> class basic_string {};
	//	typedef basic_string<char> string;
	//	template<typename T, typename A>
	//	basic_string<T, A> f(const T* a, const basic_string<T, A>& b);

	//	void test() {
	//	  string a;
	//	  f("*", a);
	//	}
	public void testTypedef_3() throws Exception {
		assertDefined("f", "string"); // "basic_string" and "allocator" should not be defined.
		assertDeclared();
	}

	//	struct A { int x; };
	//	typedef A* td;
	//	td f();

	//	int a = f()->x;
	public void testClassMember() throws Exception {
		assertDefined("f", "A");
		assertDeclared();
	}

	//	struct A { void m(); };
	//	class B : public A {};
	//	B b;

	//	void test() {
	//	  b.m();
	//	}
	public void testClassHierarchy() throws Exception {
		assertDefined("b", "B");
		assertDeclared();
	}

	//	class A { void m(); };

	//	void test(A* a) {
	//	  a->m();
	//	}
	public void testMethodCall() throws Exception {
		assertDefined("A");
		assertDeclared();
	}

	//	struct A {};
	//	struct B {};

	//	struct C {
	//	  A a;
	//	  static B b;
	//	};
	public void testFieldReference() throws Exception {
		assertDefined("A");
		assertDeclared("B");
	}

	//	int a;

	//	void test() {
	//	  void* x = &a;
	//	}
	public void testVariableReference() throws Exception {
		assertDefined("a");  // Forward declaration of variables is not allowed by default.
		assertDeclared();
	}

	//	struct A {
	//	  void operator()(int p);
	//	};
	//	const A& a;

	//	void test() {
	//	  a(1);
	//	}
	public void testCallOperator() throws Exception {
		assertDefined("A", "a");
		assertDeclared();
	}

	//	struct A {};
	//	template<typename T> struct B {};
	//	template<typename T, typename U = B<T>> struct C {};

	//	struct D : public C<A> {};
	public void testTemplate_1() throws Exception {
		assertDefined("C");
		assertDeclared("A");
	}

	//	struct A {};
	//	template<typename T> struct B {};
	//	template<typename T, typename U = B<T>> struct C {};
	//	struct D : public C<A> {};

	//	void test() {
	//	  D d;
	//	}
	public void testTemplate_2() throws Exception {
		assertDefined("D");
		assertDeclared();
	}

	//	struct A {};
	//	struct B {};
	//	struct C {};
	//	struct prefixD {};
	//	#define MACRO(t1, v1, t2, v3, t4, v4) t1 v1; t2 b; C v3; prefix##t4 v4  
	
	//	MACRO(A, a, B, c, D, d);
	public void testMacro() throws Exception {
		assertDefined("A", "B", "MACRO");
		assertDeclared();
	}
}
