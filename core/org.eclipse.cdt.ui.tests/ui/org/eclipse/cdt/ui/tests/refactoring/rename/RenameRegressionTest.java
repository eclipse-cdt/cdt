/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Wind River Systems Inc. - ported for new rename implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import java.io.StringWriter;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.tests.FailingTest;
import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameRegressionTests extends RenameTestBase {
	public RenameRegressionTests() {
		super();
	}

	public RenameRegressionTests(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(true);
	}

	public static Test suite(boolean cleanup) {
		TestSuite innerSuite = new TestSuite(RenameRegressionTests.class);
		innerSuite.addTest(new FailingTest(new RenameRegressionTests("_testMethod_35_72726"), 72726));

		TestSuite suite = new TestSuite("RenameRegressionTests");
		suite.addTest(innerSuite);
		suite.addTest(RenameVariableTests.suite(false));
		suite.addTest(RenameFunctionTests.suite(false));
		suite.addTest(RenameTypeTests.suite(false));
		suite.addTest(RenameMacroTests.suite(false));
		suite.addTest(RenameTemplatesTests.suite(false));

		if (cleanup)
			suite.addTest(new RenameRegressionTests("cleanupProject"));

		return suite;
	}

	public void testSimpleRename() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("int boo;    // boo  \n");
		writer.write("#if 0               \n");
		writer.write("boo                 \n");
		writer.write("#endif              \n");
		writer.write("void f() {          \n");
		writer.write("   boo++;           \n");
		writer.write("}                   \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		Change changes = getRefactorChanges(file, contents.indexOf("boo"), "ooga");

		assertTotalChanges(2, 1, 1, changes);
		assertChange(changes, file, contents.indexOf("boo"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("boo++"), 3, "ooga");
	}

	public void testLocalVar() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("void f() {          \n");
		writer.write("   int boo;         \n");
		writer.write("   boo++;           \n");
		writer.write("   {                \n");
		writer.write("     int boo;       \n");
		writer.write("     boo++;         \n");
		writer.write("   }                \n");
		writer.write("   boo++;           \n");
		writer.write("}                   \n");

		String contents = writer.toString();
		int offset = contents.indexOf("boo");
		IFile file = importFile("t.cpp", contents);
		Change changes = getRefactorChanges(file, offset, "ooga");

		assertTotalChanges(3, changes);
		assertChange(changes, file, offset, 3, "ooga");
		offset = contents.indexOf("boo", offset + 1);
		assertChange(changes, file, offset, 3, "ooga");
		offset = contents.lastIndexOf("boo");
		assertChange(changes, file, offset, 3, "ooga");
	}

	public void testParameter() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("void f(int boo) {   \n");
		writer.write("   boo++;           \n");
		writer.write("   {                \n");
		writer.write("     int boo;       \n");
		writer.write("     boo++;         \n");
		writer.write("   }                \n");
		writer.write("   boo++;           \n");
		writer.write("}                   \n");

		String contents = writer.toString();
		int offset = contents.indexOf("boo");
		IFile file = importFile("t.cpp", contents);
		Change changes = getRefactorChanges(file, offset, "ooga");

		assertTotalChanges(3, changes);
		assertChange(changes, file, offset, 3, "ooga");
		offset = contents.indexOf("boo", offset + 1);
		assertChange(changes, file, offset, 3, "ooga");
		offset = contents.lastIndexOf("boo");
		assertChange(changes, file, offset, 3, "ooga");
	}

	public void testFileStaticVar() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("static int boo;     \n");
		writer.write("void f() {          \n");
		writer.write("   boo++;           \n");
		writer.write("}                   \n");
		writer.write("void g(int boo) {   \n");
		writer.write("   boo++;           \n");
		writer.write("}                   \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		importFile("t2.cpp", contents);

		int offset = contents.indexOf("boo");
		Change changes = getRefactorChanges(file, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, file, offset, 3, "ooga");
		offset = contents.indexOf("boo", offset + 1);
		assertChange(changes, file, offset, 3, "ooga");
	}

	public void testClass_1() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo/*vp1*/{}; \n");
		writer.write("void f() {          \n");
		writer.write("   Boo a;           \n");
		writer.write("}                   \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("Boo/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("Boo/*vp1*/"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Boo a"), 3, "Ooga");
	}

	public void testAttribute_2() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo{          \n");
		writer.write("  int att1;//vp1,res1   \n");
		writer.write("};                  \n");
		writer.write("void f() {          \n");
		writer.write("   Boo a;           \n");
		writer.write("   a.att1;//res2     \n");
		writer.write("}                   \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("att1;//vp1");
		Change changes = getRefactorChanges(file, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("att1;//vp1,res1"), 4, "ooga");
		assertChange(changes, file, contents.indexOf("att1;//res2"), 4, "ooga");
	}

	public void testMethod_1() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{                                \n");
		writer.write("public:                                   \n");
		writer.write("  const void* method1(const char*);       \n");
		writer.write("};                                        \n");
		writer.write("const void* Foo::method1(const char* x) { \n");
		writer.write("   return (void*) x;                      \n");
		writer.write("}                                         \n");
		writer.write("void test() {                             \n");
		writer.write("     Foo d;                               \n");
		writer.write("     d.method1(\"hello\");                \n");
		writer.write("}                                         \n");
		String source = writer.toString();
		IFile cpp = importFile("t.cpp", source);
		//vp1 const
		int offset = source.indexOf("method1");
		Change changes = getRefactorChanges(cpp, offset, "m1");
		assertTotalChanges(3, changes);
		assertChange(changes, cpp, source.indexOf("method1"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(const"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(\"hello"), 7, "m1");
	}

	public void testMethod_3() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo{                   \n");
		writer.write("  int method1(){}//vp1,res1  \n");
		writer.write("};                           \n");
		writer.write("void f() {                   \n");
		writer.write("   Boo a;                    \n");
		writer.write("   a.method1();//res2        \n");
		writer.write("}                            \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("method1(){}//vp1");
		Change changes = getRefactorChanges(file, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("method1(){}//vp1,res1"), 7, "ooga");
		assertChange(changes, file, contents.indexOf("method1();//res2"), 7, "ooga");
	}

	// The constructor name is accepted, but the refactoring doesn't remove the return
	// type and there is a compile error. Renaming to a constructor should be disabled.
	// However, the UI does display the error in the preview panel. Defect 78769 states
	// the error should be shown on the first page. The parser passes, but the UI could be
	// better.
	public void testConstructor_27() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo{           \n");
		writer.write("  int foo(){}//vp1   \n");
		writer.write("};                   \n");
		writer.write("void f() {           \n");
		writer.write("   Boo a;            \n");
		writer.write("   a.foo();          \n");
		writer.write("}                    \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("foo(){}");
		try {
			getRefactorChanges(file, offset, "Boo");
		} catch (AssertionFailedError e) {
			//test passes
			assertTrue(e.getMessage().startsWith("Input check on Boo failed."));
			return;
		}
		fail("An error should have occurred in the input check.");
	}

	public void testDestructor_29_72612() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo{           \n");
		writer.write("  int foo(){}//vp1   \n");
		writer.write("};                   \n");
		writer.write("void f() {           \n");
		writer.write("   Boo a;            \n");
		writer.write("   a.foo();          \n");
		writer.write("}                    \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("foo(){}");
		try {
			getRefactorChanges(file, offset, "~Boo");
		} catch (AssertionFailedError e) {
			// test passes
			assertTrue(e.getMessage().startsWith("Input check on ~Boo failed."));
			return;
		}
		fail("An error should have occurred in the input check.");
	}

	public void testFunction_31() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("void foo(){}             \n");
		writer.write("void foo/*vp1*/(int i){} \n");
		writer.write("class Foo{               \n");
		writer.write("   int method1(){        \n");
		writer.write("    foo(3);              \n");
		writer.write("    foo();               \n");
		writer.write("   }                     \n");
		writer.write("};                       \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("foo/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("foo/*vp1*/"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("foo(3)"), 3, "ooga");
	}

	public void testMethod_32_72717() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Base {                 \n");
		writer.write(" virtual void foo()=0;       \n");
		writer.write("};                           \n");
		writer.write("class Derived: public Base { \n");
		writer.write(" virtual void foo();         \n");
		writer.write(" void foo(char i);           \n");
		writer.write(" void moon/*vp1*/(int i);    \n");
		writer.write("};                           \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("moon/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "foo");
		assertTotalChanges(1, changes);
		assertChange(changes, file, contents.indexOf("moon/*vp1*/"), 4, "foo");
		RefactoringStatus status = checkConditions(file, offset, "foo");
		assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Overloading  \n" + "New element: foo  \n" + "Conflicting element type: Method");
	}

	public void testMethod_33_72605() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo {                  \n");
		writer.write(" void aMethod/*vp1*/(int x=0);       \n");
		writer.write("};                           \n");
		writer.write("void Foo::aMethod(int x){}   \n");
		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("aMethod/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("aMethod/*vp1*/"), 7, "ooga");
		assertChange(changes, file, contents.indexOf("aMethod(int x)"), 7, "ooga");
	}

	public void testMethod_33b_72605() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo {                  \n");
		writer.write(" void aMethod/*vp1*/(int x=0);       \n");
		writer.write("};                           \n");
		String header = writer.toString();
		IFile hFile = importFile("t.hh", header);
		writer = new StringWriter();
		writer.write("#include \"t.hh\"            \n");
		writer.write("void Foo::aMethod(int x){}   \n");
		String source = writer.toString();
		IFile cppfile = importFile("t.cpp", source);
		waitForIndexer();

		int hoffset = header.indexOf("aMethod");
		int cppoffset = source.indexOf("aMethod");

		Change changes = getRefactorChanges(hFile, hoffset, "ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, hFile, hoffset, 7, "ooga");
		assertChange(changes, cppfile, cppoffset, 7, "ooga");

		changes = getRefactorChanges(cppfile, cppoffset, "ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, hFile, hoffset, 7, "ooga");
		assertChange(changes, cppfile, cppoffset, 7, "ooga");
	}

	public void testMethod_34() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Base{              \n");
		writer.write("  virtual void v/*vp1*/()=0;     \n");
		writer.write("};                       \n");
		writer.write("class Derived: Base {    \n");
		writer.write("  void v(){};            \n");
		writer.write("};                       \n");
		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("v/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("v/*vp1*/"), 1, "ooga");
		assertChange(changes, file, contents.indexOf("v(){}"), 1, "ooga");
	}

	// defect is input for new name is not allowed
	public void _testMethod_35_72726() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{                               \n");
		writer.write("  Foo& operator *=/*vp1*/(const Foo &rhs);\n");
		writer.write("  Foo& operator==/*vp2*/(const Foo &rhs);\n");
		writer.write("};                                       \n");
		writer.write("Foo& Foo::operator *=(const Foo &rhs){   \n");
		writer.write("  return *this;                          \n");
		writer.write("};                                       \n");
		writer.write("Foo& Foo::operator==(const Foo &rhs){    \n");
		writer.write("  return *this;                          \n");
		writer.write("};                                       \n");
		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// vp1 with space
		int offset = contents.indexOf("operator *=/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "operator +=");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("operator *=/*vp1*/"), 11, "operator +=");
		assertChange(changes, file, contents.indexOf("operator *=(const"), 11, "operator +=");
		// vp2 without space
		offset = contents.indexOf("operator==/*vp2*/");
		changes = getRefactorChanges(file, offset, "operator=");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("operator==/*vp2*/"), 11, "operator=");
		assertChange(changes, file, contents.indexOf("operator==(const"), 11, "operator=");

	}

	public void testMethod_39() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{                              \n");
		writer.write("  const void*   method1(const char*);   \n");
		writer.write("  const int   method2(int j);           \n");
		writer.write("};                                      \n");
		String header = writer.toString();
		IFile h = importFile("t.hh", header);

		writer = new StringWriter();
		writer.write("#include \"t.hh\"                                             \n");
		writer.write("const void* Foo::method1(const char* x){return (void*) x;}    \n");
		writer.write("const int Foo::method2(int){return 5;}             \n");
		writer.write("void test() {                                      \n");
		writer.write("     Foo d;                                        \n");
		writer.write("     d.method1(\"hello\");                         \n");
		writer.write("     int i =d.method2(3);                          \n");
		writer.write("}                                                  \n");
		String source = writer.toString();
		IFile cpp = importFile("t.cpp", source);
		waitForIndexer();

		// vp1 const
		int offset = header.indexOf("method1");
		Change changes = getRefactorChanges(h, offset, "m1");
		assertTotalChanges(3, changes);
		assertChange(changes, h, header.indexOf("method1"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(const"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(\"hello"), 7, "m1");
		// vp2 const in definition with ::
		offset = source.indexOf("method2(int");
		changes = getRefactorChanges(cpp, offset, "m2");
		assertTotalChanges(3, changes);
		assertChange(changes, h, header.indexOf("method2"), 7, "m2");
		assertChange(changes, cpp, source.indexOf("method2(int"), 7, "m2");
		assertChange(changes, cpp, source.indexOf("method2(3"), 7, "m2");
	}

	public void testMethod_40() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{                                   \n");
		writer.write("  static int method1/*vp1*/(const char* x);  \n");
		writer.write("  static int method2/*vp2*/(int);            \n");
		writer.write("};                                           \n");
		String header = writer.toString();
		IFile h = importFile("t.hh", header);

		writer = new StringWriter();
		writer.write("#include \"t.hh\"                             \n");
		writer.write("static int Foo::method1(const char* x){return 5;}  \n");
		writer.write("static int Foo::method2(int x){return (2);}; \n");
		writer.write("void test() {                                \n");
		writer.write("     Foo::method1(\"hello\");                \n");
		writer.write("     int i =Foo::method2(3);                 \n");
		writer.write("}                                            \n");
		String source = writer.toString();
		IFile cpp = importFile("t.cpp", source);
		waitForIndexer();

		// vp1 static method declaration
		int offset = header.indexOf("method1/*vp1*/");
		Change changes = getRefactorChanges(h, offset, "m1");
		assertTotalChanges(3, changes);
		assertChange(changes, h, header.indexOf("method1/*vp1*/"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(const"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(\"hello"), 7, "m1");
		// vp2 static method definition
		offset = source.indexOf("Foo::method2") + 5;
		changes = getRefactorChanges(cpp, offset, "m2");
		assertTotalChanges(3, changes);
		assertChange(changes, h, header.indexOf("method2/*vp2*/"), 7, "m2");
		assertChange(changes, cpp, source.indexOf("method2(int x"), 7, "m2");
		assertChange(changes, cpp, source.indexOf("method2(3"), 7, "m2");
	}

	public void testMethod_41() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{                                   \n");
		writer.write("public:                                      \n");
		writer.write("  volatile int  method1/*vp1*/(int);         \n");
		writer.write("private:                                     \n");
		writer.write("  int b;                                     \n");
		writer.write("};                                           \n");
		String header = writer.toString();
		IFile h = importFile("t.hh", header);

		writer = new StringWriter();
		writer.write("#include \"t.hh\"                            \n");
		writer.write("volatile int Foo::method1(int x){return (2);};               \n");
		writer.write("void test() {                                \n");
		writer.write("  Foo d;                             \n");
		writer.write("  int i =d.method1(1);                       \n");
		writer.write("}                                            \n");
		String source = writer.toString();
		IFile cpp = importFile("t.cpp", source);
		waitForIndexer();

		// vp1 volatile
		int offset = header.indexOf("method1/*vp1*/");
		Change changes = getRefactorChanges(h, offset, "m1");
		assertTotalChanges(3, changes);
		assertChange(changes, h, header.indexOf("method1/*vp1*/"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(int x"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(1"), 7, "m1");
	}

	public void testMethod_43() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{                                   \n");
		writer.write("public:                                      \n");
		writer.write("  inline void  method1/*vp1*/(int i) {b=i;}  \n");
		writer.write("private:                                     \n");
		writer.write("  int b;                                     \n");
		writer.write("};                                           \n");
		String header = writer.toString();
		IFile h = importFile("t.hh", header);

		writer = new StringWriter();
		writer.write("#include \"t.hh\"                            \n");
		writer.write("void test() {                                \n");
		writer.write("  Foo* d;                                    \n");
		writer.write("  d->method1(1);                             \n");
		writer.write("}                                            \n");
		String source = writer.toString();
		IFile cpp = importFile("t.cpp", source);
		waitForIndexer();

		// vp1 inline
		int offset = header.indexOf("method1/*vp1*/");
		Change changes = getRefactorChanges(h, offset, "m1");
		assertTotalChanges(2, changes);
		assertChange(changes, h, header.indexOf("method1/*vp1*/"), 7, "m1");
		assertChange(changes, cpp, source.indexOf("method1(1"), 7, "m1");
	}

	public void testMethod_44() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Base{              \n");
		writer.write("  virtual void v();      \n");
		writer.write("  int i;                 \n");
		writer.write("};                       \n");
		writer.write("void Base::v(){}         \n");
		writer.write("class Derived: Base {    \n");
		writer.write("  virtual void v/*vp1*/(){}//explicitly virtual          \n");
		writer.write("};                       \n");
		writer.write("class Derived2: Derived {\n");
		writer.write("  void v(){i++;}         \n");
		writer.write("};                       \n");
		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// vp1 implicit virtual method
		int offset = contents.indexOf("v/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "v1");
		assertTotalChanges(4, changes);
		assertChange(changes, file, contents.indexOf("v();"), 1, "v1");
		assertChange(changes, file, contents.indexOf("v(){}"), 1, "v1");
		assertChange(changes, file, contents.indexOf("v/*vp1*/"), 1, "v1");
		assertChange(changes, file, contents.indexOf("v(){i++;}"), 1, "v1");
	}

	public void testMethod_45() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Base{              \n");
		writer.write("  virtual void v();      \n");
		writer.write("  int i;                 \n");
		writer.write("};                       \n");
		writer.write("void Base::v(){}         \n");
		writer.write("class Derived: Base {    \n");
		writer.write("  void v/*vp1*/(){}//implicitly virtual          \n");
		writer.write("};                       \n");
		writer.write("class Derived2: Derived {\n");
		writer.write("  void v(){i++;}         \n");
		writer.write("};                       \n");
		String contents = writer.toString();
		IFile file = importFile("t45.cpp", contents);
		waitForIndexer();
		// vp1 implicit virtual method
		int offset = contents.indexOf("v/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "v1");
		assertTotalChanges(4, changes);
		assertChange(changes, file, contents.indexOf("v()"), 1, "v1");
		assertChange(changes, file, contents.indexOf("v(){}"), 1, "v1");
		assertChange(changes, file, contents.indexOf("v/*vp1*/"), 1, "v1");
		assertChange(changes, file, contents.indexOf("v(){i"), 1, "v1");
	}

	public void testStruct_46() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("struct st1/*vp1*/{};             \n");
		writer.write("class c1/*vp1*/{                 \n");
		writer.write("  public: struct st2/*vp2*/{} s; \n");
		writer.write("};                               \n");
		writer.write("namespace N{                     \n");
		writer.write(" struct st3/*vp3*/{};            \n");
		writer.write(" class c2/*vp1*/{                \n");
		writer.write("   st1 s;                        \n");
		writer.write("   st3 ss;                       \n");
		writer.write("   c2() {                        \n");
		writer.write("     c1::st2 s;                  \n");
		writer.write("   }                             \n");
		writer.write(" };                              \n");
		writer.write("}                                \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// vp1 global declaration
		int offset = contents.indexOf("st1/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga1");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("st1/*vp1*/"), 3, "Ooga1");
		assertChange(changes, file, contents.indexOf("st1 s"), 3, "Ooga1");
		// vp2 Declared in class
		offset = contents.indexOf("st2/*vp2*/");
		changes = getRefactorChanges(file, offset, "Ooga2");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("st2/*vp2*/"), 3, "Ooga2");
		assertChange(changes, file, contents.indexOf("st2 s"), 3, "Ooga2");
		// vp3 Declared in namespace
		offset = contents.indexOf("st3/*vp3*/");
		changes = getRefactorChanges(file, offset, "Ooga3");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("st3/*vp3*/"), 3, "Ooga3");
		assertChange(changes, file, contents.indexOf("st3 ss"), 3, "Ooga3");
	}

	public void testUnion_47() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("union st1/*vp1*/{};              \n");
		writer.write("class c1/*vp1*/{                 \n");
		writer.write("  public: union st2/*vp2*/{} s;  \n");
		writer.write("};                               \n");
		writer.write("namespace N{                     \n");
		writer.write(" union st3/*vp3*/{};             \n");
		writer.write(" class c2/*vp1*/{                \n");
		writer.write("   st1 s;                        \n");
		writer.write("   st3 ss;                       \n");
		writer.write("   c2() {                        \n");
		writer.write("     c1::st2 s;                  \n");
		writer.write("   }                             \n");
		writer.write(" };                              \n");
		writer.write("}                                \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// vp1 global declaration
		int offset = contents.indexOf("st1/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga1");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("st1/*vp1*/"), 3, "Ooga1");
		assertChange(changes, file, contents.indexOf("st1 s"), 3, "Ooga1");
		// vp2 Declared in class
		offset = contents.indexOf("st2/*vp2*/");
		changes = getRefactorChanges(file, offset, "Ooga2");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("st2/*vp2*/"), 3, "Ooga2");
		assertChange(changes, file, contents.indexOf("st2 s"), 3, "Ooga2");
		// vp3 Declared in namespace
		offset = contents.indexOf("st3/*vp3*/");
		changes = getRefactorChanges(file, offset, "Ooga3");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("st3/*vp3*/"), 3, "Ooga3");
		assertChange(changes, file, contents.indexOf("st3 ss"), 3, "Ooga3");
	}

	public void testEnumeration_48() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("enum e1/*vp1*/{E0};              \n");
		writer.write("class c1 {                       \n");
		writer.write("  public: enum e2/*vp2*/{E1} s;  \n");
		writer.write("};                               \n");
		writer.write("namespace N{                     \n");
		writer.write(" enum e3/*vp3*/{};               \n");
		writer.write(" class c2/*vp1*/{                \n");
		writer.write("   e1 s;                         \n");
		writer.write("   e3 ss;                        \n");
		writer.write("   c2() {                        \n");
		writer.write("     c1::e2 s;                   \n");
		writer.write("   }                             \n");
		writer.write(" };                              \n");
		writer.write("}                                \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// vp1 global declaration
		int offset = contents.indexOf("e1/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga1");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("e1/*vp1*/"), 2, "Ooga1");
		assertChange(changes, file, contents.indexOf("e1 s"), 2, "Ooga1");
		// vp2 Declared in class
		offset = contents.indexOf("e2/*vp2*/");
		changes = getRefactorChanges(file, offset, "Ooga2");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("e2/*vp2*/"), 2, "Ooga2");
		assertChange(changes, file, contents.indexOf("e2 s"), 2, "Ooga2");
		// vp3 Declared in namespace
		offset = contents.indexOf("e3/*vp3*/");
		changes = getRefactorChanges(file, offset, "Ooga3");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("e3/*vp3*/"), 2, "Ooga3");
		assertChange(changes, file, contents.indexOf("e3 ss"), 2, "Ooga3");
	}

	public void testTemplate_49_72626() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("template <class Type>            \n");
		writer.write("class Array/*vp1*/   {           \n");
		writer.write("  public:    Array(){            \n");
		writer.write("   a=new Type[10];               \n");
		writer.write("  }                              \n");
		writer.write("  virtual Type& operator[](int i){return a[i];}  \n");
		writer.write("  protected: Type *a;            \n");
		writer.write("};                               \n");
		writer.write("void f(){                        \n");
		writer.write("   Array <int> a;                 \n");
		writer.write("}                                \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("Array");
		Change changes = getRefactorChanges(file, offset, "Arr2");
		assertTotalChanges(3, changes);
		assertChange(changes, file, offset, 5, "Arr2");
		assertChange(changes, file, offset = contents.indexOf("Array", offset + 1), 5, "Arr2");
		assertChange(changes, file, offset = contents.indexOf("Array", offset + 1), 5, "Arr2");
	}

	public void testClass_52() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("namespace N1 {           \n");
		writer.write("class Boo{};             \n");
		writer.write("}                        \n");
		writer.write("namespace N2  {          \n");
		writer.write("class Boo/*vp1*/{};      \n");
		writer.write("}                        \n");
		writer.write("void f() {               \n");
		writer.write("   N1::Boo c1;           \n");
		writer.write("   N2::Boo c2;           \n");
		writer.write("}                        \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("Boo/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("Boo/*vp1*/"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Boo c2"), 3, "Ooga");
	}

	public void testClass_53() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo/*vp1*/ {//ren1     \n");
		writer.write("  Foo();//ren2               \n");
		writer.write("  virtual ~Foo();//ren3      \n");
		writer.write("};                           \n");
		writer.write("Foo::Foo() {}//ren4,5        \n");
		writer.write("Foo::~Foo() {}//ren6,7       \n");
		writer.write("void f() {                   \n");
		writer.write("   Foo *f=new Foo();//ren8,9 \n");
		writer.write("   f->~Foo();//ren10         \n");
		writer.write("}                            \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("Foo/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga");

		assertTotalChanges(10, changes);
		assertChange(changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo();//ren2"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo();//ren3"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo::Foo() {}//ren4,5"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo() {}//ren4,5"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo::~Foo() {}//ren6,7"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo() {}//ren6,7"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo *f=new Foo();//ren8,9"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo();//ren8,9"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo();//ren10"), 3, "Ooga");
	}

	public void testAttribute_54() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo{                   \n");
		writer.write("  static int att;//vp1,rn1   \n");
		writer.write("};                           \n");
		writer.write("void f() {                   \n");
		writer.write("   Boo a;                    \n");
		writer.write("   a.att;//rn2               \n");
		writer.write("   Boo::att;//rn3            \n");
		writer.write("}                            \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("att;//vp1");
		Change changes = getRefactorChanges(file, offset, "ooga");

		assertTotalChanges(3, changes);
		assertChange(changes, file, contents.indexOf("att;//vp1"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("att;//rn2"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("att;//rn3"), 3, "ooga");
	}

	public void testClass_55() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{           \n");
		writer.write("  class Hoo{//vp1    \n");
		writer.write("     public: Hoo();  \n");
		writer.write("  };                 \n");
		writer.write("  Foo(){             \n");
		writer.write("     Foo::Hoo h;     \n");
		writer.write("  }                  \n");
		writer.write("};                   \n");
		writer.write("Foo::Hoo::Hoo(){}    \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("Hoo{");
		Change changes = getRefactorChanges(file, offset, "ooga");

		assertTotalChanges(5, changes);
		assertChange(changes, file, contents.indexOf("Hoo{//vp1"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("Hoo();"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("Hoo h;"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("Hoo::Hoo(){}"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("Hoo(){}"), 3, "ooga");
	}

	public void testClass_55_79231() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo{};//vp1            \n");
		writer.write("class Foo{               \n");
		writer.write("   Foo() {               \n");
		writer.write("     class Boo{};        \n");
		writer.write("     Boo t;              \n");
		writer.write("     }                   \n");
		writer.write("};                       \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// defect is that the inner class defined in a method is also renamed, when it
		// shouldn't be.
		int offset = contents.indexOf("Boo{};//vp1");
		Change changes = getRefactorChanges(file, offset, "Ooga");

		assertTotalChanges(1, changes);
		assertChange(changes, file, contents.indexOf("Boo{};//vp1"), 3, "Ooga");
	}

	public void testClass_55_72748() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{};//vp1            \n");
		writer.write("void f(){                \n");
		writer.write("  Foo *somePtr;                  \n");
		writer.write("  if (somePtr == reinterpret_cast<Foo*>(0)){}                \n");
		writer.write("}                        \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// defect is that the Foo in <> is not renamed
		int offset = contents.indexOf("Foo{};//vp1");
		Change changes = getRefactorChanges(file, offset, "Ooga");

		assertTotalChanges(3, changes);
		assertChange(changes, file, contents.indexOf("Foo{};//vp1"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo *somePtr"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo*>(0)"), 3, "Ooga");
	}

	public void testClass_56() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{};//vp1,rn1            \n");
		writer.write("class Derived: public Foo{//rn2  \n");
		writer.write("  Derived():Foo(){}//rn3         \n");
		writer.write("};                               \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// defect is that the inner class defined in a method is also renamed, when it
		// shouldn't be.
		int offset = contents.indexOf("Foo{};//vp1");
		Change changes = getRefactorChanges(file, offset, "Ooga");

		assertTotalChanges(3, changes);
		assertChange(changes, file, contents.indexOf("Foo{};//vp1"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo{//rn2"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo(){}//rn3"), 3, "Ooga");
	}

	public void testAttribute_61() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{       \n");
		writer.write(" private: static int count;//vp1     \n");
		writer.write("};       \n");
		String header = writer.toString();
		IFile h = importFile("Foo.hh", header);
		writer = new StringWriter();
		writer.write("#include \"Foo.hh\"                   \n");
		writer.write("int Foo::count=10;                           \n");

		String source = writer.toString();
		IFile cpp = importFile("Foo.cpp", source);
		int offset = header.indexOf("count");
		Change changes = getRefactorChanges(h, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, h, header.indexOf("count"), 5, "ooga");
		assertChange(changes, cpp, source.indexOf("count"), 5, "ooga");
	}

	public void testEnumerator_62() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("enum Foo{E0, E1};//vp1       \n");
		String header = writer.toString();
		IFile h = importFile("Foo.hh", header);
		writer = new StringWriter();
		writer.write("#include \"Foo.hh\"                   \n");
		writer.write("void f() {                           \n");
		writer.write(" int i=E1;                   \n");
		writer.write("}                            \n");

		String source = writer.toString();
		IFile cpp = importFile("Foo.cpp", source);
		waitForIndexer();

		int offset = header.indexOf("E1");
		getRefactorChanges(h, offset, "Ooga");
		Change changes = getRefactorChanges(h, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, h, header.indexOf("E1"), 2, "ooga");
		assertChange(changes, cpp, source.indexOf("E1"), 2, "ooga");
	}

	public void testAttribute_63() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{       \n");
		writer.write(" int att;        \n");
		writer.write(" Foo(int i);     \n");
		writer.write("};       \n");
		String header = writer.toString();
		IFile h = importFile("Foo.hh", header);
		writer = new StringWriter();
		writer.write("#include \"Foo.hh\"                   \n");
		writer.write("Foo::Foo(int i): att(i) {}                           \n");

		String source = writer.toString();
		IFile cpp = importFile("Foo.cpp", source);
		waitForIndexer();

		int offset = header.indexOf("att");
		Change changes = getRefactorChanges(h, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, h, header.indexOf("att"), 3, "ooga");
		assertChange(changes, cpp, source.indexOf("att"), 3, "ooga");
	}

	public void testAttribute_64() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Foo{               \n");
		writer.write("   private:              \n");
		writer.write("   int b;//vp1,rn1       \n");
		writer.write("   int m(int b) {        \n");
		writer.write("         return b;       \n");
		writer.write("   }                     \n");
		writer.write("   int n() {             \n");
		writer.write("         return b;//rn2  \n");
		writer.write("   }                     \n");
		writer.write("   int o() {             \n");
		writer.write("         int b=2;        \n");
		writer.write("   return b;             \n");
		writer.write(" }                       \n");
		writer.write("};                       \n");
		String header = writer.toString();
		IFile h = importFile("Foo.hh", header);
		int offset = header.indexOf("b;//vp1");
		Change changes = getRefactorChanges(h, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, h, header.indexOf("b;//vp1"), 1, "ooga");
		assertChange(changes, h, header.indexOf("b;//rn2"), 1, "ooga");
	}

	public void testAttribute_65() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class A{             \n");
		writer.write("    int x();         \n");
		writer.write("};                   \n");
		writer.write("class B{             \n");
		writer.write("    friend class A;  \n");
		writer.write("    private:         \n");
		writer.write("    int att;         \n");
		writer.write("};                   \n");
		String header = writer.toString();
		IFile h = importFile("Foo.hh", header);
		writer = new StringWriter();
		writer.write("#include \"Foo.hh\"   \n");
		writer.write("int A::x() {         \n");
		writer.write(" B b;                \n");
		writer.write(" int att=b.att;      \n");
		writer.write("}                    \n");
		String source = writer.toString();
		IFile cpp = importFile("Foo.cpp", source);
		int offset = header.indexOf("att");
		Change changes = getRefactorChanges(h, offset, "ooga");

		assertTotalChanges(2, changes);
		assertChange(changes, h, header.indexOf("att"), 3, "ooga");
		assertChange(changes, cpp, source.indexOf("att;"), 3, "ooga");
	}

	public void testNamespace_66() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("namespace Foo/*vp1*/{            \n");
		writer.write(" namespace Baz/*vp2*/ {          \n");
		writer.write("   int i;                \n");
		writer.write(" }                       \n");
		writer.write(" using namespace Baz;    \n");
		writer.write("}                        \n");
		writer.write("void f() {               \n");
		writer.write("  Foo::i = 1;            \n");
		writer.write("}                        \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// vp1 Foo with ref in function
		int offset = contents.indexOf("Foo/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo::"), 3, "Ooga");
		// vp2 nested Baz with ref in using
		offset = contents.indexOf("Baz/*vp2*/");
		changes = getRefactorChanges(file, offset, "Wooga");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("Baz/*vp2*/"), 3, "Wooga");
		assertChange(changes, file, contents.indexOf("Baz;"), 3, "Wooga");
	}

	public void testNamespace_66_79281() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("namespace Foo{           \n");
		writer.write(" int i;                  \n");
		writer.write("}                        \n");
		writer.write("namespace Bar/*vp1*/ = Foo;      \n");
		writer.write("void f() {               \n");
		writer.write("  Bar::i = 1;            \n");
		writer.write("}                        \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("Bar/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("Bar/*vp1*/"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Bar::"), 3, "Ooga");
	}

	public void testNamespace_66_79282() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("namespace Foo/*vp1*/{}           \n");
		writer.write("namespace Bar = Foo;     \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		// defect is Foo on line 2 is not renamed
		int offset = contents.indexOf("Foo/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "Ooga");
		assertTotalChanges(2, changes);
		assertChange(changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga");
		assertChange(changes, file, contents.indexOf("Foo;"), 3, "Ooga");
	}

	public void testFunction_67() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("void foo/*vp1*/(){}//rn1     \n");
		writer.write("void bar(){                  \n");
		writer.write("  foo();//rn2                \n");
		writer.write("}                            \n");
		writer.write("namespace N{                 \n");
		writer.write("  class A{                   \n");
		writer.write("  A() {foo();}//rn3          \n");
		writer.write("  };                         \n");
		writer.write("}                            \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("foo/*vp1*/");
		Change changes = getRefactorChanges(file, offset, "ooga");
		assertTotalChanges(3, changes);
		assertChange(changes, file, contents.indexOf("foo/*vp1*/"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("foo();//rn2"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("foo();}//rn3"), 3, "ooga");
	}

	public void testVariable_68() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class A{                 \n");
		writer.write("  public: int i;         \n");
		writer.write("};                       \n");
		writer.write("A var;//vp1,rn1          \n");
		writer.write("void f(){                \n");
		writer.write("  int j = ::var.i;//rn2  \n");
		writer.write("}                        \n");
		writer.write("class B{                 \n");
		writer.write("  void g(){              \n");
		writer.write("    var.i=3;//rn3        \n");
		writer.write("  }                      \n");
		writer.write("};                       \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("var;//vp1");
		Change changes = getRefactorChanges(file, offset, "ooga");
		assertTotalChanges(3, changes);
		assertChange(changes, file, contents.indexOf("var;//vp1"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("var.i;//rn2"), 3, "ooga");
		assertChange(changes, file, contents.indexOf("var.i=3;//rn3"), 3, "ooga");
	}

	public void testVariable_68_79295() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("int var;//vp1            \n");
		writer.write("void f(int var){         \n");
		writer.write("  int i = var;           \n");
		writer.write("}                        \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		//defect is the argument and local variable var are incorrectly renamed
		int offset = contents.indexOf("var;//vp1");
		Change changes = getRefactorChanges(file, offset, "ooga");
		assertTotalChanges(1, changes);
		assertChange(changes, file, contents.indexOf("var;//vp1"), 3, "ooga");
	}

	// similar to test 92, except this one will continue with warning, or error status
	// while case in 92 must stop refactor with fatal status
	public void testClass_81_72620() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("union u_haul{};      \n");
		writer.write("struct s_haul{};     \n");
		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("s_haul");
		try {
			getRefactorChanges(file, offset, "u_haul");
		} catch (AssertionFailedError e) {
			assertTrue(e.getMessage().startsWith("Input check on u_haul failed."));
			return;
		}
		fail("An error should have occurred in the input check.");
	}

	public void testVariable_88_72617() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class A{};               \n");
		writer.write("A a;//vp1                \n");
		String header = writer.toString();
		IFile h = importFile("Foo.hh", header);
		writer = new StringWriter();
		writer.write("#include \"Foo.hh\"   \n");
		writer.write("void f() {           \n");
		writer.write(" A a;                \n");
		writer.write("}                    \n");
		String source = writer.toString();
		importFile("Foo.cpp", source);
		int offset = header.indexOf("a;//vp1");
		Change changes = getRefactorChanges(h, offset, "ooga");

		assertTotalChanges(1, changes);
		assertChange(changes, h, header.indexOf("a;//vp1"), 1, "ooga");
	}

	// Two ways to test name collision on same type:
	// if you don't know the error message, catch on getRefactorChanges
	// or if you want to verify a message or severity, use getRefactorMessages
	// and getRefactorSeverity
	public void testClass_92A() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class Boo{};         \n");
		writer.write("  void f() {}        \n");
		writer.write("};                   \n");
		writer.write("class Foo/*vp1*/{};  \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("Foo/*vp1*/");
		try {
			getRefactorChanges(file, offset, "Boo");
		} catch (AssertionFailedError e) {
			assertTrue(e.getMessage().startsWith("Input check on Boo failed."));
			return;
		}
		fail("An error or warning should have occurred in the input check.");
	}

	public void testClass_92B() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class A{};           \n");
		writer.write("class B{};//vp1      \n");

		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("B{};//vp1");

		String[] messages = getRefactorMessages(file, offset, "A");
		assertEquals(1, messages.length);
		assertEquals("A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
				+ "New element: A  \n" + "Conflicting element type: Type", messages[0]);
		// assert that you cannot refactor because severity is FATAL (4)
		int s = getRefactorSeverity(file, offset, "A");
		assertEquals(RefactoringStatus.ERROR, s);
	}

	public void testRenameParticipant() throws Exception {
		TestRenameParticipant.reset();
		StringWriter writer = new StringWriter();
		writer.write("class A{}; ");
		String contents = writer.toString();
		IFile file = importFile("t.cpp", contents);
		int offset = contents.indexOf("A");
		getRefactorChanges(file, offset, "B");
		assertEquals(1, TestRenameParticipant.getConditionCheckCount());
		assertEquals(1, TestRenameParticipant.getCreateChangeCount());
		Object element = TestRenameParticipant.getElement();

		assertNotNull(element);
		assertTrue(element instanceof IBinding);
		IBinding binding = (IBinding) element;
		assertEquals(binding.getName(), "A");

		RenameArguments args = TestRenameParticipant.staticGetArguments();
		assertNotNull(args);
		assertEquals("B", args.getNewName());
	}
}
