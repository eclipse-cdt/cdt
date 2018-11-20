/*******************************************************************************
 * Copyright (c) 2007, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import junit.framework.Test;

public class ParameterHintTests extends AbstractContentAssistTest {
	private static final String HEADER_FILE_NAME = "PHTest.h";
	private static final String SOURCE_FILE_NAME = "PHTest.cpp";

	//	{PHTest.h}
	//	class aClass {
	//  public:
	//	    int aField;
	//	    void aMethod(char c);
	//	    void aMethod(char c, int x);
	//	};
	//	class bClass {
	//	public:
	//	    bClass(int x);
	//	    bClass(int x, int y);
	//	};
	//	void aFunc(int i);
	//	int anotherFunc(int i, int j);
	//	int pi(aClass a);
	//	int pie(aClass a);
	//	int pies(aClass a);
	//	template<class T>class tClass {public:tClass(T t);};
	//	template<class T>void tFunc(T x, T y);

	public ParameterHintTests(String name) {
		super(name, true);
	}

	public static Test suite() {
		return BaseTestCase.suite(ParameterHintTests.class, "_");
	}

	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		String headerContent = readTaggedComment(HEADER_FILE_NAME);
		StringBuilder sourceContent = getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \"" + HEADER_FILE_NAME + "\"\n");
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	protected void assertParameterHints(String[] expected) throws Exception {
		assertContentAssistResults(getBuffer().length() - 1, expected, DEFAULT_FLAGS, CompareType.CONTEXT);
	}

	protected void assertDisplayedParameterHints(String[] expected) throws Exception {
		assertContentAssistResults(getBuffer().length() - 1, expected, DEFAULT_FLAGS, CompareType.INFORMATION);
	}

	//void foo(){aFunc(
	public void testFunction() throws Exception {
		assertParameterHints(new String[] { "aFunc(int i) : void" });
	}

	//void foo(){tFunc(
	public void testTemplateFunction() throws Exception {
		assertParameterHints(new String[] { "tFunc(T x, T y) : void" });
	}

	//void foo(){tFunc<int>(
	public void testTemplateFunction2() throws Exception {
		assertParameterHints(new String[] { "tFunc(T x, T y) : void" });
	}

	//void foo(){int a=5;aFunc  ( anotherFunc   (  a ,  (in
	public void testOffsetCalculation() throws Exception {
		assertParameterHints(new String[] { "anotherFunc(int i, int j) : int" });
	}

	//void foo(){int a=pie(
	public void testAccurateName() throws Exception {
		assertParameterHints(new String[] { "pie(aClass a) : int" });
	}

	//void foo(){int a=pi
	public void testInvalidInvocation() throws Exception {
		assertParameterHints(new String[] {});
	}

	//void aClass::aMethod(
	public void testMethodDefinition() throws Exception {
		assertParameterHints(new String[] { "aMethod(char c) : void", "aMethod(char c, int x) : void" });
	}

	//void aClass::aMethod(char c){aMethod(c,aFi
	public void testMethodScope() throws Exception {
		assertParameterHints(new String[] { "aMethod(char c) : void", "aMethod(char c, int x) : void" });
	}

	//void foo(){aClass a=new aClass(
	public void testConstructor() throws Exception {
		assertParameterHints(new String[] { "aClass(const aClass &)" });
	}

	//void foo(){bClass b(
	public void testConstructor2_Bug223660() throws Exception {
		// http://bugs.eclipse.org/223660
		assertParameterHints(new String[] { "bClass(int x)", "bClass(int x, int y)", "bClass(const bClass &)" });
	}

	//	struct D {
	//	  bClass b;
	//	  D() : b(
	public void testConstructor3_Bug327064() throws Exception {
		// http://bugs.eclipse.org/327064
		assertParameterHints(new String[] { "bClass(int x)", "bClass(int x, int y)", "bClass(const bClass &)" });
	}

	//void foo(){tClass<int> t=new tClass<int>(
	public void testTemplateConstructor() throws Exception {
		assertParameterHints(new String[] { "tClass(T t)", "tClass(const tClass<T> &)" });
	}

	//void foo(){tClass<int> t(
	public void testTemplateConstructor2_Bug223660() throws Exception {
		// http://bugs.eclipse.org/223660
		assertParameterHints(new String[] { "tClass(int t)", "tClass(const tClass<int> &)" });
	}

	//int pi = 3;void foo(){pi(
	public void testFunctionsOnly() throws Exception {
		assertParameterHints(new String[] { "pi(aClass a) : int" });
	}

	// class OtherClass {
	// public:
	// char getChar(int a, int b);
	// };
	//
	// void foo() {
	//    OtherClass* oc;
	//    int i= (int) oc->getChar(
	public void testMethodWithCast() throws Exception {
		assertParameterHints(new String[] { "getChar(int a, int b) : char" });
	}

	// void foo(int i, int j) {
	// 	foo(
	public void testFormatterConfiguredWithSpaceAfterComma() throws Exception {
		setCommaAfterFunctionParameter(CCorePlugin.INSERT);
		assertParameterHints(new String[] { "foo(int i, int j) : void" });
	}

	//	void foo(int x, int y);
	//	int main() { foo(
	public void testFunction_461680() throws Exception {
		assertDisplayedParameterHints(new String[] { "void foo(int x, int y)" });
	}

	//	struct C {
	//		void foo(int arg);
	//	};
	//	void caller(C c) { c.foo(
	public void testMethod_461680() throws Exception {
		assertDisplayedParameterHints(new String[] { "void C::foo(int arg)" });
	}

	//	struct Base {
	//		void foo(int arg);
	//	};
	//	struct Derived : Base {};
	//	void caller(Derived* d) { d->foo(
	public void testNonOverriddenMethod_461680() throws Exception {
		assertDisplayedParameterHints(new String[] { "void Base::foo(int arg)" });
	}

	//	struct Base {
	//		virtual void foo(int arg);
	//	};
	//	void caller(Base* b) { b->foo(
	public void testVirtualMethod_461680() throws Exception {
		assertDisplayedParameterHints(new String[] { "virtual void Base::foo(int arg)" });
	}

	//	struct Base {
	//		virtual void foo(int arg);
	//	};
	//	struct Derived : Base {};
	//	void caller(Derived* d) { d->foo(
	public void testNonOverriddenVirtualMethod_461680() throws Exception {
		assertDisplayedParameterHints(new String[] { "virtual void Base::foo(int arg)" });
	}

	//	struct Base {
	//		virtual void foo(int arg);
	//	};
	//	struct Derived : Base {
	//		void foo(int arg) override;
	//	};
	//	void caller(Derived* d) { d->foo(
	public void testOverriddenVirtualMethod_461680() throws Exception {
		assertDisplayedParameterHints(new String[] { "virtual void Derived::foo(int arg)" });
	}
}
