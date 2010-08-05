/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Wind River Systems Inc. - ported for new rename implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import java.io.StringWriter;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.tests.FailingTest;

public class RenameRegressionTests extends RenameTests {
    public RenameRegressionTests() {
        super();
    }

    public RenameRegressionTests(String name) {
        super(name);
    }

    public static Test suite(){
        return suite(true);
    }

    public static Test suite(boolean cleanup) {
        TestSuite innerSuite= new TestSuite(RenameRegressionTests.class);
        innerSuite.addTest(new FailingTest(new RenameRegressionTests("_testMethod_35_72726"),72726)); //$NON-NLS-1$
        
        TestSuite suite = new TestSuite("RenameRegressionTests"); //$NON-NLS-1$
        suite.addTest(innerSuite);
        suite.addTest(RenameVariableTests.suite(false));
        suite.addTest(RenameFunctionTests.suite(false));
        suite.addTest(RenameTypeTests.suite(false));
        suite.addTest(RenameMacroTests.suite(false));
        suite.addTest(RenameTemplatesTests.suite(false));
        
        if (cleanup)
            suite.addTest(new RenameRegressionTests("cleanupProject"));    //$NON-NLS-1$
        
        return suite;
    }
    
    public void testSimpleRename() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("int boo;    // boo  \n"); //$NON-NLS-1$
        writer.write("#if 0               \n"); //$NON-NLS-1$
        writer.write("boo                 \n"); //$NON-NLS-1$
        writer.write("#endif              \n"); //$NON-NLS-1$
        writer.write("void f() {          \n"); //$NON-NLS-1$
        writer.write("   boo++;           \n"); //$NON-NLS-1$
        writer.write("}                   \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        Change changes = getRefactorChanges(file, contents.indexOf("boo"), "ooga"); //$NON-NLS-1$ //$NON-NLS-2$
        
        assertTotalChanges(2, 1, 1, changes);
        assertChange(changes, file, contents.indexOf("boo"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("boo++"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testLocalVar() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("void f() {          \n"); //$NON-NLS-1$
        writer.write("   int boo;         \n"); //$NON-NLS-1$
        writer.write("   boo++;           \n"); //$NON-NLS-1$
        writer.write("   {                \n"); //$NON-NLS-1$
        writer.write("     int boo;       \n"); //$NON-NLS-1$
        writer.write("     boo++;         \n"); //$NON-NLS-1$
        writer.write("   }                \n"); //$NON-NLS-1$
        writer.write("   boo++;           \n"); //$NON-NLS-1$
        writer.write("}                   \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        int offset= contents.indexOf("boo"); //$NON-NLS-1$
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$ 
        
        assertTotalChanges(3, changes);
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
        offset= contents.indexOf("boo", offset+1); //$NON-NLS-1$
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
        offset= contents.lastIndexOf("boo"); //$NON-NLS-1$
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
    }

    public void testParameter() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("void f(int boo) {   \n"); //$NON-NLS-1$
        writer.write("   boo++;           \n"); //$NON-NLS-1$
        writer.write("   {                \n"); //$NON-NLS-1$
        writer.write("     int boo;       \n"); //$NON-NLS-1$
        writer.write("     boo++;         \n"); //$NON-NLS-1$
        writer.write("   }                \n"); //$NON-NLS-1$
        writer.write("   boo++;           \n"); //$NON-NLS-1$
        writer.write("}                   \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        int offset= contents.indexOf("boo"); //$NON-NLS-1$
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$ 
        
        assertTotalChanges(3, changes);
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
        offset= contents.indexOf("boo", offset+1); //$NON-NLS-1$
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
        offset= contents.lastIndexOf("boo"); //$NON-NLS-1$
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
    }

    public void testFileStaticVar() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("static int boo;     \n"); //$NON-NLS-1$
        writer.write("void f() {          \n"); //$NON-NLS-1$
        writer.write("   boo++;           \n"); //$NON-NLS-1$
        writer.write("}                   \n"); //$NON-NLS-1$
        writer.write("void g(int boo) {   \n"); //$NON-NLS-1$
        writer.write("   boo++;           \n"); //$NON-NLS-1$
        writer.write("}                   \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        importFile("t2.cpp", contents); //$NON-NLS-1$
        
        int offset= contents.indexOf("boo"); //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$ 
        
        assertTotalChanges(2, changes);
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
        offset= contents.indexOf("boo", offset+1); //$NON-NLS-1$
        assertChange(changes, file, offset, 3, "ooga");  //$NON-NLS-1$
    }

    public void testClass_1() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo/*vp1*/{}; \n"); //$NON-NLS-1$
        writer.write("void f() {          \n"); //$NON-NLS-1$
        writer.write("   Boo a;           \n"); //$NON-NLS-1$
        writer.write("}                   \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset= contents.indexOf("Boo/*vp1*/"); //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("Boo/*vp1*/"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Boo a"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testAttribute_2() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{          \n"); //$NON-NLS-1$
        writer.write("  int att1;//vp1,res1   \n"); //$NON-NLS-1$
        writer.write("};                  \n"); //$NON-NLS-1$
        writer.write("void f() {          \n"); //$NON-NLS-1$
        writer.write("   Boo a;           \n"); //$NON-NLS-1$
        writer.write("   a.att1;//res2     \n"); //$NON-NLS-1$
        writer.write("}                   \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("att1;//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("att1;//vp1,res1"), 4, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("att1;//res2"), 4, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testMethod_1() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{                                \n"); //$NON-NLS-1$
        writer.write("public:                                   \n"); //$NON-NLS-1$
        writer.write("  const void* method1(const char*);       \n"); //$NON-NLS-1$
        writer.write("};                                        \n"); //$NON-NLS-1$
        writer.write("const void* Foo::method1(const char* x) { \n"); //$NON-NLS-1$
        writer.write("   return (void*) x;                      \n"); //$NON-NLS-1$
        writer.write("}                                         \n"); //$NON-NLS-1$
        writer.write("void test() {                             \n"); //$NON-NLS-1$
        writer.write("     Foo d;                               \n"); //$NON-NLS-1$
        writer.write("     d.method1(\"hello\");                \n"); //$NON-NLS-1$
        writer.write("}                                         \n"); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile("t.cpp", source); //$NON-NLS-1$
        //vp1 const
        int offset = source.indexOf("method1"); //$NON-NLS-1$
        Change changes = getRefactorChanges(cpp, offset, "m1"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, cpp, source.indexOf("method1"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(const"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(\"hello"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testMethod_3() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{                   \n"); //$NON-NLS-1$
        writer.write("  int method1(){}//vp1,res1  \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
        writer.write("void f() {                   \n"); //$NON-NLS-1$
        writer.write("   Boo a;                    \n"); //$NON-NLS-1$
        writer.write("   a.method1();//res2        \n"); //$NON-NLS-1$
        writer.write("}                            \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("method1(){}//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("method1(){}//vp1,res1"), 7, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("method1();//res2"), 7, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }
  
    public void testConstructor_26() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{                   \n"); //$NON-NLS-1$
        writer.write("  Boo(){}//vp1,res1  \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
        writer.write("void f() {                   \n"); //$NON-NLS-1$
        writer.write("   Boo a = new Boo();                    \n"); //$NON-NLS-1$
        writer.write("}                            \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("Boo(){}") ; //$NON-NLS-1$
        try {
            getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().startsWith("Input check on ooga failed.")); //$NON-NLS-1$
            return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }

    // The constructor name is accepted, but the refactoring doesn't remove the return
    // type and there is a compile error. Renaming to a constructor should be disabled.
    // However, the UI does display the error in the preview panel. Defect 78769 states
    // the error should be shown on the first page. The parser passes, but the UI could be
    // better.
    public void testConstructor_27() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{           \n"); //$NON-NLS-1$
        writer.write("  int foo(){}//vp1   \n"); //$NON-NLS-1$
        writer.write("};                   \n"); //$NON-NLS-1$
        writer.write("void f() {           \n"); //$NON-NLS-1$
        writer.write("   Boo a;            \n"); //$NON-NLS-1$
        writer.write("   a.foo();          \n"); //$NON-NLS-1$
        writer.write("}                    \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("foo(){}") ; //$NON-NLS-1$
        try {
            getRefactorChanges(file, offset, "Boo"); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
            //test passes
            assertTrue(e.getMessage().startsWith("Input check on Boo failed.")); //$NON-NLS-1$
            return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }

    public void testDestructor_28() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{           \n"); //$NON-NLS-1$
        writer.write("  ~Boo(){}//vp1      \n"); //$NON-NLS-1$
        writer.write("};                   \n"); //$NON-NLS-1$
        writer.write("void f() {           \n"); //$NON-NLS-1$
        writer.write("   Boo a ;           \n"); //$NON-NLS-1$
        writer.write("   a.~Boo();         \n"); //$NON-NLS-1$
        writer.write("}                    \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("~Boo(){}") ; //$NON-NLS-1$
        try {
            getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().startsWith("Input check on ooga failed.")); //$NON-NLS-1$
            return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }

    public void testDestructor_29_72612() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{           \n"); //$NON-NLS-1$
        writer.write("  int foo(){}//vp1   \n"); //$NON-NLS-1$
        writer.write("};                   \n"); //$NON-NLS-1$
        writer.write("void f() {           \n"); //$NON-NLS-1$
        writer.write("   Boo a;            \n"); //$NON-NLS-1$
        writer.write("   a.foo();          \n"); //$NON-NLS-1$
        writer.write("}                    \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("foo(){}") ; //$NON-NLS-1$
        try {
            getRefactorChanges(file, offset, "~Boo"); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
            // test passes
            assertTrue(e.getMessage().startsWith("Input check on ~Boo failed.")); //$NON-NLS-1$
            return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$
    }

    public void testFunction_31() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("void foo(){}             \n"); //$NON-NLS-1$
        writer.write("void foo/*vp1*/(int i){} \n"); //$NON-NLS-1$
        writer.write("class Foo{               \n"); //$NON-NLS-1$
        writer.write("   int method1(){        \n"); //$NON-NLS-1$
        writer.write("    foo(3);              \n"); //$NON-NLS-1$
        writer.write("    foo();               \n"); //$NON-NLS-1$
        writer.write("   }                     \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("foo/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("foo/*vp1*/"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("foo(3)"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testMethod_32_72717() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Base {                 \n"); //$NON-NLS-1$
        writer.write(" virtual void foo()=0;       \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
        writer.write("class Derived: public Base { \n"); //$NON-NLS-1$
        writer.write(" virtual void foo();         \n"); //$NON-NLS-1$
        writer.write(" void foo(char i);           \n"); //$NON-NLS-1$
        writer.write(" void moon/*vp1*/(int i);    \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
         
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("moon/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "foo"); //$NON-NLS-1$
        assertTotalChanges(1, changes);
        assertChange(changes, file, contents.indexOf("moon/*vp1*/"), 4, "foo");  //$NON-NLS-1$//$NON-NLS-2$
        RefactoringStatus status= checkConditions(file, offset, "foo"); //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Overloading  \n" +
        		"New element: foo  \n" + 
        		"Conflicting element type: Method"); //$NON-NLS-1$
    }
    
    public void testMethod_33_72605() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo {                  \n"); //$NON-NLS-1$
        writer.write(" void aMethod/*vp1*/(int x=0);       \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
        writer.write("void Foo::aMethod(int x){}   \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("aMethod/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("aMethod/*vp1*/"), 7, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("aMethod(int x)"), 7, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testMethod_33b_72605() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo {                  \n"); //$NON-NLS-1$
        writer.write(" void aMethod/*vp1*/(int x=0);       \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile hFile= importFile("t.hh", header); //$NON-NLS-1$
        writer= new StringWriter();
        writer.write("#include \"t.hh\"            \n"); //$NON-NLS-1$
        writer.write("void Foo::aMethod(int x){}   \n"); //$NON-NLS-1$
        String source = writer.toString();
        IFile cppfile = importFile("t.cpp", source); //$NON-NLS-1$
        waitForIndexer();

        int hoffset =  header.indexOf("aMethod") ; //$NON-NLS-1$
        int cppoffset =  source.indexOf("aMethod") ; //$NON-NLS-1$
        
        Change changes = getRefactorChanges(hFile, hoffset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, hFile, hoffset, 7, "ooga");  //$NON-NLS-1$
        assertChange(changes, cppfile, cppoffset, 7, "ooga");  //$NON-NLS-1$

        changes = getRefactorChanges(cppfile, cppoffset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, hFile, hoffset, 7, "ooga");  //$NON-NLS-1$
        assertChange(changes, cppfile, cppoffset, 7, "ooga");  //$NON-NLS-1$
    }

    public void testMethod_34() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Base{              \n"); //$NON-NLS-1$
        writer.write("  virtual void v/*vp1*/()=0;     \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        writer.write("class Derived: Base {    \n"); //$NON-NLS-1$
        writer.write("  void v(){};            \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("v/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("v/*vp1*/"), 1, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("v(){}"), 1, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    // defect is input for new name is not allowed
    public void _testMethod_35_72726() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{                               \n"); //$NON-NLS-1$
        writer.write("  Foo& operator *=/*vp1*/(const Foo &rhs);\n"); //$NON-NLS-1$
        writer.write("  Foo& operator==/*vp2*/(const Foo &rhs);\n"); //$NON-NLS-1$
        writer.write("};                                       \n"); //$NON-NLS-1$
        writer.write("Foo& Foo::operator *=(const Foo &rhs){   \n"); //$NON-NLS-1$
        writer.write("  return *this;                          \n"); //$NON-NLS-1$
        writer.write("};                                       \n"); //$NON-NLS-1$
        writer.write("Foo& Foo::operator==(const Foo &rhs){    \n"); //$NON-NLS-1$
        writer.write("  return *this;                          \n"); //$NON-NLS-1$
        writer.write("};                                       \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // vp1 with space
        int offset =  contents.indexOf("operator *=/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "operator +="); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("operator *=/*vp1*/"), 11, "operator +=");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("operator *=(const"), 11, "operator +=");  //$NON-NLS-1$//$NON-NLS-2$
        // vp2 without space
        offset =  contents.indexOf("operator==/*vp2*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "operator="); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("operator==/*vp2*/"), 11, "operator=");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("operator==(const"), 11, "operator=");  //$NON-NLS-1$//$NON-NLS-2$
       
    }

    public void testMethod_39() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{                              \n"); //$NON-NLS-1$
        writer.write("  const void*   method1(const char*);   \n"); //$NON-NLS-1$
        writer.write("  const int   method2(int j);           \n"); //$NON-NLS-1$
        writer.write("};                                      \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("t.hh", header); //$NON-NLS-1$
       
        writer = new StringWriter();
        writer.write("#include \"t.hh\"                                             \n"); //$NON-NLS-1$
        writer.write("const void* Foo::method1(const char* x){return (void*) x;}    \n"); //$NON-NLS-1$
        writer.write("const int Foo::method2(int){return 5;}             \n"); //$NON-NLS-1$
        writer.write("void test() {                                      \n"); //$NON-NLS-1$
        writer.write("     Foo d;                                        \n"); //$NON-NLS-1$
        writer.write("     d.method1(\"hello\");                         \n"); //$NON-NLS-1$
        writer.write("     int i =d.method2(3);                          \n"); //$NON-NLS-1$
        writer.write("}                                                  \n"); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile("t.cpp", source); //$NON-NLS-1$
        waitForIndexer();
        
        // vp1 const
        int offset = header.indexOf("method1"); //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "m1"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, h, header.indexOf("method1"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(const"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(\"hello"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        // vp2 const in definition with ::
        offset = source.indexOf("method2(int"); //$NON-NLS-1$
        changes = getRefactorChanges(cpp, offset, "m2"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, h, header.indexOf("method2"), 7, "m2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method2(int"), 7, "m2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method2(3"), 7, "m2");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testMethod_40() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{                                   \n"); //$NON-NLS-1$
        writer.write("  static int method1/*vp1*/(const char* x);  \n"); //$NON-NLS-1$
        writer.write("  static int method2/*vp2*/(int);            \n"); //$NON-NLS-1$
        writer.write("};                                           \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("t.hh", header); //$NON-NLS-1$
       
        writer = new StringWriter();
        writer.write("#include \"t.hh\"                             \n"); //$NON-NLS-1$
        writer.write("static int Foo::method1(const char* x){return 5;}  \n"); //$NON-NLS-1$
        writer.write("static int Foo::method2(int x){return (2);}; \n"); //$NON-NLS-1$
        writer.write("void test() {                                \n"); //$NON-NLS-1$
        writer.write("     Foo::method1(\"hello\");                \n"); //$NON-NLS-1$
        writer.write("     int i =Foo::method2(3);                 \n"); //$NON-NLS-1$
        writer.write("}                                            \n"); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile("t.cpp", source); //$NON-NLS-1$
        waitForIndexer();

        // vp1 static method declaration
        int offset = header.indexOf("method1/*vp1*/"); //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "m1"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, h, header.indexOf("method1/*vp1*/"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(const"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(\"hello"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        // vp2 static method definition
        offset = source.indexOf("Foo::method2")+5; //$NON-NLS-1$
        changes = getRefactorChanges(cpp, offset, "m2"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, h, header.indexOf("method2/*vp2*/"), 7, "m2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method2(int x"), 7, "m2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method2(3"), 7, "m2");  //$NON-NLS-1$//$NON-NLS-2$
    }
   
    public void testMethod_41() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{                                   \n"); //$NON-NLS-1$
        writer.write("public:                                      \n"); //$NON-NLS-1$
        writer.write("  volatile int  method1/*vp1*/(int);         \n"); //$NON-NLS-1$
        writer.write("private:                                     \n"); //$NON-NLS-1$
        writer.write("  int b;                                     \n"); //$NON-NLS-1$
        writer.write("};                                           \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("t.hh", header); //$NON-NLS-1$
       
        writer = new StringWriter();
        writer.write("#include \"t.hh\"                            \n"); //$NON-NLS-1$
        writer.write("volatile int Foo::method1(int x){return (2);};               \n"); //$NON-NLS-1$
        writer.write("void test() {                                \n"); //$NON-NLS-1$
        writer.write("  Foo d;                             \n"); //$NON-NLS-1$
        writer.write("  int i =d.method1(1);                       \n"); //$NON-NLS-1$
        writer.write("}                                            \n"); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile("t.cpp", source); //$NON-NLS-1$
        waitForIndexer();

        // vp1 volatile
        int offset =  header.indexOf("method1/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "m1"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, h, header.indexOf("method1/*vp1*/"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(int x"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("method1(1"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testMethod_43() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write("class Foo{                                   \n"); //$NON-NLS-1$
	    writer.write("public:                                      \n"); //$NON-NLS-1$
	    writer.write("  inline void  method1/*vp1*/(int i) {b=i;}  \n"); //$NON-NLS-1$
	    writer.write("private:                                     \n"); //$NON-NLS-1$
	    writer.write("  int b;                                     \n"); //$NON-NLS-1$
	    writer.write("};                                           \n"); //$NON-NLS-1$
	    String header = writer.toString();
	    IFile h = importFile("t.hh", header); //$NON-NLS-1$
	   
	    writer = new StringWriter();
	    writer.write("#include \"t.hh\"                            \n"); //$NON-NLS-1$
	    writer.write("void test() {                                \n"); //$NON-NLS-1$
	    writer.write("  Foo* d;                                    \n"); //$NON-NLS-1$
	    writer.write("  d->method1(1);                             \n"); //$NON-NLS-1$
	    writer.write("}                                            \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile("t.cpp", source); //$NON-NLS-1$
	    waitForIndexer();
	
	    // vp1 inline
	    int offset =  header.indexOf("method1/*vp1*/") ; //$NON-NLS-1$
	    Change changes = getRefactorChanges(h, offset, "m1"); //$NON-NLS-1$
	    assertTotalChanges(2, changes);
	    assertChange(changes, h, header.indexOf("method1/*vp1*/"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange(changes, cpp, source.indexOf("method1(1"), 7, "m1");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testMethod_44() throws Exception {
	    StringWriter writer = new StringWriter();
	    writer.write("class Base{              \n"); //$NON-NLS-1$
	    writer.write("  virtual void v();      \n"); //$NON-NLS-1$
	    writer.write("  int i;                 \n"); //$NON-NLS-1$
	    writer.write("};                       \n"); //$NON-NLS-1$
	    writer.write("void Base::v(){}         \n"); //$NON-NLS-1$
	    writer.write("class Derived: Base {    \n"); //$NON-NLS-1$
	    writer.write("  virtual void v/*vp1*/(){}//explicitly virtual          \n"); //$NON-NLS-1$
	    writer.write("};                       \n"); //$NON-NLS-1$
	    writer.write("class Derived2: Derived {\n"); //$NON-NLS-1$
	    writer.write("  void v(){i++;}         \n"); //$NON-NLS-1$
	    writer.write("};                       \n"); //$NON-NLS-1$
	    String contents = writer.toString();
	    IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
	    // vp1 implicit virtual method
	    int offset =  contents.indexOf("v/*vp1*/") ; //$NON-NLS-1$
	    Change changes = getRefactorChanges(file, offset, "v1"); //$NON-NLS-1$
	    assertTotalChanges(4, changes);
	    assertChange(changes, file, contents.indexOf("v();"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange(changes, file, contents.indexOf("v(){}"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange(changes, file, contents.indexOf("v/*vp1*/"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$
	    assertChange(changes, file, contents.indexOf("v(){i++;}"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$       
    }
   
    public void testMethod_45() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Base{              \n"); //$NON-NLS-1$
        writer.write("  virtual void v();      \n"); //$NON-NLS-1$
        writer.write("  int i;                 \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        writer.write("void Base::v(){}         \n"); //$NON-NLS-1$
        writer.write("class Derived: Base {    \n"); //$NON-NLS-1$
        writer.write("  void v/*vp1*/(){}//implicitly virtual          \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        writer.write("class Derived2: Derived {\n"); //$NON-NLS-1$
        writer.write("  void v(){i++;}         \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile("t45.cpp", contents); //$NON-NLS-1$
        waitForIndexer();
        // vp1 implicit virtual method
        int offset =  contents.indexOf("v/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "v1"); //$NON-NLS-1$
        assertTotalChanges(4, changes);
        assertChange(changes, file, contents.indexOf("v()"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("v(){}"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("v/*vp1*/"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("v(){i"), 1, "v1");  //$NON-NLS-1$//$NON-NLS-2$       
    }

    public void testStruct_46() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("struct st1/*vp1*/{};             \n"); //$NON-NLS-1$
        writer.write("class c1/*vp1*/{                 \n"); //$NON-NLS-1$
        writer.write("  public: struct st2/*vp2*/{} s; \n"); //$NON-NLS-1$
        writer.write("};                               \n"); //$NON-NLS-1$
        writer.write("namespace N{                     \n"); //$NON-NLS-1$
        writer.write(" struct st3/*vp3*/{};            \n"); //$NON-NLS-1$
        writer.write(" class c2/*vp1*/{                \n"); //$NON-NLS-1$
        writer.write("   st1 s;                        \n"); //$NON-NLS-1$
        writer.write("   st3 ss;                       \n"); //$NON-NLS-1$
        writer.write("   c2() {                        \n"); //$NON-NLS-1$
        writer.write("     c1::st2 s;                  \n"); //$NON-NLS-1$
        writer.write("   }                             \n"); //$NON-NLS-1$
        writer.write(" };                              \n"); //$NON-NLS-1$
        writer.write("}                                \n"); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // vp1 global declaration
        int offset =  contents.indexOf("st1/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga1"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("st1/*vp1*/"), 3, "Ooga1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("st1 s"), 3, "Ooga1");  //$NON-NLS-1$//$NON-NLS-2$
        // vp2 Declared in class
        offset =  contents.indexOf("st2/*vp2*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "Ooga2"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("st2/*vp2*/"), 3, "Ooga2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("st2 s"), 3, "Ooga2");  //$NON-NLS-1$//$NON-NLS-2$
        // vp3 Declared in namespace
        offset =  contents.indexOf("st3/*vp3*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "Ooga3"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("st3/*vp3*/"), 3, "Ooga3");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("st3 ss"), 3, "Ooga3");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testUnion_47() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("union st1/*vp1*/{};              \n"); //$NON-NLS-1$
        writer.write("class c1/*vp1*/{                 \n"); //$NON-NLS-1$
        writer.write("  public: union st2/*vp2*/{} s;  \n"); //$NON-NLS-1$
        writer.write("};                               \n"); //$NON-NLS-1$
        writer.write("namespace N{                     \n"); //$NON-NLS-1$
        writer.write(" union st3/*vp3*/{};             \n"); //$NON-NLS-1$
        writer.write(" class c2/*vp1*/{                \n"); //$NON-NLS-1$
        writer.write("   st1 s;                        \n"); //$NON-NLS-1$
        writer.write("   st3 ss;                       \n"); //$NON-NLS-1$
        writer.write("   c2() {                        \n"); //$NON-NLS-1$
        writer.write("     c1::st2 s;                  \n"); //$NON-NLS-1$
        writer.write("   }                             \n"); //$NON-NLS-1$
        writer.write(" };                              \n"); //$NON-NLS-1$
        writer.write("}                                \n"); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // vp1 global declaration
        int offset =  contents.indexOf("st1/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga1"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("st1/*vp1*/"), 3, "Ooga1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("st1 s"), 3, "Ooga1");  //$NON-NLS-1$//$NON-NLS-2$
        // vp2 Declared in class
        offset =  contents.indexOf("st2/*vp2*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "Ooga2"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("st2/*vp2*/"), 3, "Ooga2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("st2 s"), 3, "Ooga2");  //$NON-NLS-1$//$NON-NLS-2$
        // vp3 Declared in namespace
        offset =  contents.indexOf("st3/*vp3*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "Ooga3"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("st3/*vp3*/"), 3, "Ooga3");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("st3 ss"), 3, "Ooga3");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testEnumeration_48() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("enum e1/*vp1*/{E0};              \n"); //$NON-NLS-1$
        writer.write("class c1 {                       \n"); //$NON-NLS-1$
        writer.write("  public: enum e2/*vp2*/{E1} s;  \n"); //$NON-NLS-1$
        writer.write("};                               \n"); //$NON-NLS-1$
        writer.write("namespace N{                     \n"); //$NON-NLS-1$
        writer.write(" enum e3/*vp3*/{};               \n"); //$NON-NLS-1$
        writer.write(" class c2/*vp1*/{                \n"); //$NON-NLS-1$
        writer.write("   e1 s;                         \n"); //$NON-NLS-1$
        writer.write("   e3 ss;                        \n"); //$NON-NLS-1$
        writer.write("   c2() {                        \n"); //$NON-NLS-1$
        writer.write("     c1::e2 s;                   \n"); //$NON-NLS-1$
        writer.write("   }                             \n"); //$NON-NLS-1$
        writer.write(" };                              \n"); //$NON-NLS-1$
        writer.write("}                                \n"); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // vp1 global declaration
        int offset =  contents.indexOf("e1/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga1"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("e1/*vp1*/"), 2, "Ooga1");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("e1 s"), 2, "Ooga1");  //$NON-NLS-1$//$NON-NLS-2$
        // vp2 Declared in class
        offset =  contents.indexOf("e2/*vp2*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "Ooga2"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("e2/*vp2*/"), 2, "Ooga2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("e2 s"), 2, "Ooga2");  //$NON-NLS-1$//$NON-NLS-2$
        // vp3 Declared in namespace
        offset =  contents.indexOf("e3/*vp3*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "Ooga3"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("e3/*vp3*/"), 2, "Ooga3");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("e3 ss"), 2, "Ooga3");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testTemplate_49_72626() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("template <class Type>            \n"); //$NON-NLS-1$
        writer.write("class Array/*vp1*/   {           \n"); //$NON-NLS-1$
        writer.write("  public:    Array(){            \n"); //$NON-NLS-1$
        writer.write("   a=new Type[10];               \n"); //$NON-NLS-1$
        writer.write("  }                              \n"); //$NON-NLS-1$
        writer.write("  virtual Type& operator[](int i){return a[i];}  \n"); //$NON-NLS-1$
        writer.write("  protected: Type *a;            \n"); //$NON-NLS-1$
        writer.write("};                               \n"); //$NON-NLS-1$
        writer.write("void f(){                        \n"); //$NON-NLS-1$
        writer.write("   Array <int> a;                 \n"); //$NON-NLS-1$
        writer.write("}                                \n"); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("Array") ; //$NON-NLS-1$
        Change changes= getRefactorChanges(file, offset, "Arr2"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, file, offset, 5, "Arr2");  //$NON-NLS-1$
        assertChange(changes, file, offset=contents.indexOf("Array", offset+1), 5, "Arr2");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, offset=contents.indexOf("Array", offset+1), 5, "Arr2");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testClass_52() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("namespace N1 {           \n"); //$NON-NLS-1$
        writer.write("class Boo{};             \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        writer.write("namespace N2  {          \n"); //$NON-NLS-1$
        writer.write("class Boo/*vp1*/{};      \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        writer.write("void f() {               \n"); //$NON-NLS-1$
        writer.write("   N1::Boo c1;           \n"); //$NON-NLS-1$
        writer.write("   N2::Boo c2;           \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("Boo/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("Boo/*vp1*/"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Boo c2"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testClass_53() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo/*vp1*/ {//ren1     \n"); //$NON-NLS-1$
        writer.write("  Foo();//ren2               \n"); //$NON-NLS-1$
        writer.write("  virtual ~Foo();//ren3      \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
        writer.write("Foo::Foo() {}//ren4,5        \n"); //$NON-NLS-1$
        writer.write("Foo::~Foo() {}//ren6,7       \n"); //$NON-NLS-1$
        writer.write("void f() {                   \n"); //$NON-NLS-1$
        writer.write("   Foo *f=new Foo();//ren8,9 \n"); //$NON-NLS-1$
        writer.write("   f->~Foo();//ren10         \n"); //$NON-NLS-1$
        writer.write("}                            \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("Foo/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        
        assertTotalChanges(10, changes);
        assertChange(changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo();//ren2"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo();//ren3"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo::Foo() {}//ren4,5"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo() {}//ren4,5"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo::~Foo() {}//ren6,7"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo() {}//ren6,7"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo *f=new Foo();//ren8,9"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo();//ren8,9"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo();//ren10"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testAttribute_54() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{                   \n"); //$NON-NLS-1$
        writer.write("  static int att;//vp1,rn1   \n"); //$NON-NLS-1$
        writer.write("};                           \n"); //$NON-NLS-1$
        writer.write("void f() {                   \n"); //$NON-NLS-1$
        writer.write("   Boo a;                    \n"); //$NON-NLS-1$
        writer.write("   a.att;//rn2               \n"); //$NON-NLS-1$
        writer.write("   Boo::att;//rn3            \n"); //$NON-NLS-1$
        writer.write("}                            \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("att;//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(3, changes);
        assertChange(changes, file, contents.indexOf("att;//vp1"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("att;//rn2"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("att;//rn3"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testClass_55() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{           \n"); //$NON-NLS-1$
        writer.write("  class Hoo{//vp1    \n"); //$NON-NLS-1$
        writer.write("     public: Hoo();  \n"); //$NON-NLS-1$
        writer.write("  };                 \n"); //$NON-NLS-1$
        writer.write("  Foo(){             \n"); //$NON-NLS-1$
        writer.write("     Foo::Hoo h;     \n"); //$NON-NLS-1$
        writer.write("  }                  \n"); //$NON-NLS-1$
        writer.write("};                   \n"); //$NON-NLS-1$
        writer.write("Foo::Hoo::Hoo(){}    \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("Hoo{") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(5, changes);
        assertChange(changes, file, contents.indexOf("Hoo{//vp1"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Hoo();"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Hoo h;"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Hoo::Hoo(){}"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Hoo(){}"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testClass_55_79231() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{};//vp1            \n"); //$NON-NLS-1$
        writer.write("class Foo{               \n"); //$NON-NLS-1$
        writer.write("   Foo() {               \n"); //$NON-NLS-1$
        writer.write("     class Boo{};        \n"); //$NON-NLS-1$
        writer.write("     Boo t;              \n"); //$NON-NLS-1$
        writer.write("     }                   \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // defect is that the inner class defined in a method is also renamed, when it 
        // shouldn't be.
        int offset =  contents.indexOf("Boo{};//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        
        assertTotalChanges(1, changes);
        assertChange(changes, file, contents.indexOf("Boo{};//vp1"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testClass_55_72748() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{};//vp1            \n"); //$NON-NLS-1$
        writer.write("void f(){                \n"); //$NON-NLS-1$
        writer.write("  Foo *somePtr;                  \n"); //$NON-NLS-1$
        writer.write("  if (somePtr == reinterpret_cast<Foo*>(0)){}                \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // defect is that the Foo in <> is not renamed 
        int offset =  contents.indexOf("Foo{};//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        
        assertTotalChanges(3, changes);
        assertChange(changes, file, contents.indexOf("Foo{};//vp1"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo *somePtr"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo*>(0)"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testClass_56() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{};//vp1,rn1            \n"); //$NON-NLS-1$
        writer.write("class Derived: public Foo{//rn2  \n"); //$NON-NLS-1$
        writer.write("  Derived():Foo(){}//rn3         \n"); //$NON-NLS-1$
        writer.write("};                               \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // defect is that the inner class defined in a method is also renamed, when it 
        // shouldn't be.
        int offset =  contents.indexOf("Foo{};//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        
        assertTotalChanges(3, changes);
        assertChange(changes, file, contents.indexOf("Foo{};//vp1"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo{//rn2"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo(){}//rn3"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testAttribute_61() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{       \n"); //$NON-NLS-1$
        writer.write(" private: static int count;//vp1     \n"); //$NON-NLS-1$
        writer.write("};       \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("Foo.hh", header); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"Foo.hh\"                   \n"); //$NON-NLS-1$
        writer.write("int Foo::count=10;                           \n"); //$NON-NLS-1$
          
        String source = writer.toString();
        IFile cpp = importFile("Foo.cpp", source); //$NON-NLS-1$
        int offset =  header.indexOf("count") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, h, header.indexOf("count"), 5, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("count"), 5, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testEnumerator_62() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("enum Foo{E0, E1};//vp1       \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("Foo.hh", header); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"Foo.hh\"                   \n"); //$NON-NLS-1$
        writer.write("void f() {                           \n"); //$NON-NLS-1$
        writer.write(" int i=E1;                   \n"); //$NON-NLS-1$
        writer.write("}                            \n"); //$NON-NLS-1$
          
        String source = writer.toString();
        IFile cpp=importFile("Foo.cpp", source); //$NON-NLS-1$
        waitForIndexer();
        
        int offset =  header.indexOf("E1") ; //$NON-NLS-1$
        getRefactorChanges(h, offset, "Ooga"); //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, h, header.indexOf("E1"), 2, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("E1"), 2, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testAttribute_63() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{       \n"); //$NON-NLS-1$
        writer.write(" int att;        \n"); //$NON-NLS-1$
        writer.write(" Foo(int i);     \n"); //$NON-NLS-1$
        writer.write("};       \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("Foo.hh", header); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"Foo.hh\"                   \n"); //$NON-NLS-1$
        writer.write("Foo::Foo(int i): att(i) {}                           \n"); //$NON-NLS-1$
          
        String source = writer.toString();
        IFile cpp = importFile("Foo.cpp", source); //$NON-NLS-1$
        waitForIndexer();
        
        int offset =  header.indexOf("att") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, h, header.indexOf("att"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("att"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testAttribute_64() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Foo{               \n"); //$NON-NLS-1$
        writer.write("   private:              \n"); //$NON-NLS-1$
        writer.write("   int b;//vp1,rn1       \n"); //$NON-NLS-1$
        writer.write("   int m(int b) {        \n"); //$NON-NLS-1$
        writer.write("         return b;       \n"); //$NON-NLS-1$
        writer.write("   }                     \n"); //$NON-NLS-1$
        writer.write("   int n() {             \n"); //$NON-NLS-1$
        writer.write("         return b;//rn2  \n"); //$NON-NLS-1$
        writer.write("   }                     \n"); //$NON-NLS-1$
        writer.write("   int o() {             \n"); //$NON-NLS-1$
        writer.write("         int b=2;        \n"); //$NON-NLS-1$
        writer.write("   return b;             \n"); //$NON-NLS-1$
        writer.write(" }                       \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("Foo.hh", header); //$NON-NLS-1$
        int offset =  header.indexOf("b;//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, h, header.indexOf("b;//vp1"), 1, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, h, header.indexOf("b;//rn2"), 1, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testAttribute_65() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class A{             \n"); //$NON-NLS-1$
        writer.write("    int x();         \n"); //$NON-NLS-1$
        writer.write("};                   \n"); //$NON-NLS-1$
        writer.write("class B{             \n"); //$NON-NLS-1$
        writer.write("    friend class A;  \n"); //$NON-NLS-1$
        writer.write("    private:         \n"); //$NON-NLS-1$
        writer.write("    int att;         \n"); //$NON-NLS-1$
        writer.write("};                   \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("Foo.hh", header); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"Foo.hh\"   \n"); //$NON-NLS-1$
        writer.write("int A::x() {         \n"); //$NON-NLS-1$
        writer.write(" B b;                \n"); //$NON-NLS-1$
        writer.write(" int att=b.att;      \n"); //$NON-NLS-1$
        writer.write("}                    \n"); //$NON-NLS-1$
        String source = writer.toString();
        IFile cpp = importFile("Foo.cpp", source); //$NON-NLS-1$
        int offset =  header.indexOf("att") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(2, changes);
        assertChange(changes, h, header.indexOf("att"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, cpp, source.indexOf("att;"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testNamespace_66() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("namespace Foo/*vp1*/{            \n"); //$NON-NLS-1$
        writer.write(" namespace Baz/*vp2*/ {          \n"); //$NON-NLS-1$
        writer.write("   int i;                \n"); //$NON-NLS-1$
        writer.write(" }                       \n"); //$NON-NLS-1$
        writer.write(" using namespace Baz;    \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        writer.write("void f() {               \n"); //$NON-NLS-1$
        writer.write("  Foo::i = 1;            \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
       
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // vp1 Foo with ref in function
        int offset =  contents.indexOf("Foo/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo::"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        // vp2 nested Baz with ref in using
        offset =  contents.indexOf("Baz/*vp2*/") ; //$NON-NLS-1$
        changes = getRefactorChanges(file, offset, "Wooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("Baz/*vp2*/"), 3, "Wooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Baz;"), 3, "Wooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testNamespace_66_79281() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("namespace Foo{           \n"); //$NON-NLS-1$
        writer.write(" int i;                  \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        writer.write("namespace Bar/*vp1*/ = Foo;      \n"); //$NON-NLS-1$
        writer.write("void f() {               \n"); //$NON-NLS-1$
        writer.write("  Bar::i = 1;            \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("Bar/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("Bar/*vp1*/"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Bar::"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    } 

    public void testNamespace_66_79282() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("namespace Foo/*vp1*/{}           \n"); //$NON-NLS-1$
        writer.write("namespace Bar = Foo;     \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        // defect is Foo on line 2 is not renamed
        int offset =  contents.indexOf("Foo/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "Ooga"); //$NON-NLS-1$
        assertTotalChanges(2, changes);
        assertChange(changes, file, contents.indexOf("Foo/*vp1*/"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("Foo;"), 3, "Ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    public void testFunction_67() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("void foo/*vp1*/(){}//rn1     \n"); //$NON-NLS-1$
        writer.write("void bar(){                  \n"); //$NON-NLS-1$
        writer.write("  foo();//rn2                \n"); //$NON-NLS-1$
        writer.write("}                            \n"); //$NON-NLS-1$
        writer.write("namespace N{                 \n"); //$NON-NLS-1$
        writer.write("  class A{                   \n"); //$NON-NLS-1$
        writer.write("  A() {foo();}//rn3          \n"); //$NON-NLS-1$
        writer.write("  };                         \n"); //$NON-NLS-1$
        writer.write("}                            \n"); //$NON-NLS-1$
         
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("foo/*vp1*/") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, file, contents.indexOf("foo/*vp1*/"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("foo();//rn2"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("foo();}//rn3"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    } 

    public void testVariable_68() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class A{                 \n"); //$NON-NLS-1$
        writer.write("  public: int i;         \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
        writer.write("A var;//vp1,rn1          \n"); //$NON-NLS-1$
        writer.write("void f(){                \n"); //$NON-NLS-1$
        writer.write("  int j = ::var.i;//rn2  \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
        writer.write("class B{                 \n"); //$NON-NLS-1$
        writer.write("  void g(){              \n"); //$NON-NLS-1$
        writer.write("    var.i=3;//rn3        \n"); //$NON-NLS-1$
        writer.write("  }                      \n"); //$NON-NLS-1$
        writer.write("};                       \n"); //$NON-NLS-1$
         
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("var;//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(3, changes);
        assertChange(changes, file, contents.indexOf("var;//vp1"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("var.i;//rn2"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
        assertChange(changes, file, contents.indexOf("var.i=3;//rn3"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    } 

    public void testVariable_68_79295() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("int var;//vp1            \n"); //$NON-NLS-1$
        writer.write("void f(int var){         \n"); //$NON-NLS-1$
        writer.write("  int i = var;           \n"); //$NON-NLS-1$
        writer.write("}                        \n"); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        //defect is the argument and local variable var are incorrectly renamed
        int offset =  contents.indexOf("var;//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(file, offset, "ooga"); //$NON-NLS-1$
        assertTotalChanges(1, changes);
        assertChange(changes, file, contents.indexOf("var;//vp1"), 3, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    } 

    // similar to test 92, except this one will continue with warning, or error status
    // while case in 92 must stop refactor with fatal status
    public void testClass_81_72620() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("union u_haul{};      \n"); //$NON-NLS-1$
        writer.write("struct s_haul{};     \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("s_haul") ; //$NON-NLS-1$
        try {
            getRefactorChanges(file, offset, "u_haul"); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().startsWith("Input check on u_haul failed.")); //$NON-NLS-1$
            return;
        }
        fail ("An error should have occurred in the input check."); //$NON-NLS-1$ 
    }

    public void testVariable_88_72617() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class A{};               \n"); //$NON-NLS-1$
        writer.write("A a;//vp1                \n"); //$NON-NLS-1$
        String header = writer.toString();
        IFile h = importFile("Foo.hh", header); //$NON-NLS-1$
        writer = new StringWriter();
        writer.write("#include \"Foo.hh\"   \n"); //$NON-NLS-1$
        writer.write("void f() {           \n"); //$NON-NLS-1$
        writer.write(" A a;                \n"); //$NON-NLS-1$
        writer.write("}                    \n"); //$NON-NLS-1$
        String source = writer.toString();
        importFile("Foo.cpp", source); //$NON-NLS-1$
        int offset =  header.indexOf("a;//vp1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(h, offset, "ooga"); //$NON-NLS-1$
        
        assertTotalChanges(1, changes);
        assertChange(changes, h, header.indexOf("a;//vp1"), 1, "ooga");  //$NON-NLS-1$//$NON-NLS-2$
    }

    // Two ways to test name collision on same type:
    // if you don't know the error message, catch on getRefactorChanges
    // or if you want to verify a message or severity, use getRefactorMessages
    // and getRefactorSeverity
    public void testClass_92A() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class Boo{};         \n"); //$NON-NLS-1$
        writer.write("  void f() {}        \n"); //$NON-NLS-1$
        writer.write("};                   \n"); //$NON-NLS-1$
        writer.write("class Foo/*vp1*/{};  \n"); //$NON-NLS-1$
        
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("Foo/*vp1*/") ; //$NON-NLS-1$
        try {
            getRefactorChanges(file, offset, "Boo"); //$NON-NLS-1$
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().startsWith("Input check on Boo failed.")); //$NON-NLS-1$
            return;
        }
        fail ("An error or warning should have occurred in the input check."); //$NON-NLS-1$ 
    }

    public void testClass_92B() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class A{};           \n"); //$NON-NLS-1$
        writer.write("class B{};//vp1      \n"); //$NON-NLS-1$
           
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("B{};//vp1") ; //$NON-NLS-1$
        
        String[] messages = getRefactorMessages(file, offset, "A"); //$NON-NLS-1$
        assertEquals(1, messages.length);
        assertEquals("A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: A  \n" +
        		"Conflicting element type: Type", messages[0]);   //$NON-NLS-1$
        // assert that you cannot refactor because severity is FATAL (4)
        int s = getRefactorSeverity(file, offset, "A"); //$NON-NLS-1$
        assertEquals(RefactoringStatus.ERROR,s);
    }
    
    public void testRenameParticipant() throws Exception {
        TestRenameParticipant.reset();
        StringWriter writer = new StringWriter();
        writer.write("class A{}; "); //$NON-NLS-1$
        String contents = writer.toString();
        IFile file = importFile("t.cpp", contents); //$NON-NLS-1$
        int offset =  contents.indexOf("A") ; //$NON-NLS-1$
        getRefactorChanges(file, offset, "B"); //$NON-NLS-1$
        assertEquals(1, TestRenameParticipant.getConditionCheckCount());
        assertEquals(1, TestRenameParticipant.getCreateChangeCount());
        Object element= TestRenameParticipant.getElement();

        assertNotNull(element);
        assertTrue(element instanceof IBinding);
        IBinding binding= (IBinding) element;
        assertEquals(binding.getName(), "A");  //$NON-NLS-1$
        
        RenameArguments args= TestRenameParticipant.staticGetArguments();
        assertNotNull(args);
        assertEquals("B", args.getNewName()); //$NON-NLS-1$
    }
}
