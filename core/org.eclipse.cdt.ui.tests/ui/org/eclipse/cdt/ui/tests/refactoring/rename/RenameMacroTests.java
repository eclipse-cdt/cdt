/*******************************************************************************
 * Copyright (c) 2005, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RenameMacroTests extends RenameTests {

    public RenameMacroTests(String name) {
        super(name);
    }

    public static Test suite(){
        return suite(true);
    }

    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite(RenameMacroTests.class); 
        if (cleanup) {
            suite.addTest( new RefactoringTests("cleanupProject") );    //$NON-NLS-1$
        }
        return suite;
    }

    public void testMacroRename() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#define HALLO x   \n"); //$NON-NLS-1$
        writer.write("class v1 {                \n"); //$NON-NLS-1$
        writer.write(" int HALLO;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("class HALLO {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("class v3 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("class v4 {                \n"); //$NON-NLS-1$
        writer.write(" int HALLO();          \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("int v4::HALLO(){}      \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int HALLO; v1::v++;    \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("HALLO"); //$NON-NLS-1$
        int offset2= contents.indexOf("HALLO", offset1+1); //$NON-NLS-1$
        
        Change ch= getRefactorChanges(cpp, offset1, "WELT");  //$NON-NLS-1$
        assertTotalChanges(6, ch);
        int off= offset1;
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$

        ch= getRefactorChanges(cpp, offset2, "WELT");  //$NON-NLS-1$
        assertTotalChanges(6, ch);
        off= offset1;
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off+1); //$NON-NLS-1$
    }
    
    public void testMacroNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("#define MACRO 1           \n"); //$NON-NLS-1$
        writer.write("int v1(); int v2(); int v3();  \n"); //$NON-NLS-1$
        writer.write("static int s1();          \n"); //$NON-NLS-1$
        writer.write("static int s2();          \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1; v1();        \n"); //$NON-NLS-1$
        writer.write("     extern_var;          \n"); //$NON-NLS-1$
        writer.write("     var_def;             \n"); //$NON-NLS-1$
        writer.write("     enum_item;           \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write( "static int static_other_file();     \n" ); //$NON-NLS-1$        
        importFile( "other.cpp", writer.toString() ); //$NON-NLS-1$
        waitForIndexer();


        int offset1= contents.indexOf("MACRO"); //$NON-NLS-1$
        
        // conflicts after renaming
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Name conflict  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("par1"), "MACRO");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "'MACRO' conflicts with the name of an existing macro!"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Name conflict  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Name conflict  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Name conflict  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Name conflict  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$
   }

    public void testClassMacroClash() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class CC {int a;};         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$

        writer = new StringWriter();
        writer.write("#define CC mm              \n"); //$NON-NLS-1$
        writer.write("int CC;                   \n"); //$NON-NLS-1$
        String contents2 = writer.toString();
        IFile cpp2= importFile("test2.cpp", contents2 ); //$NON-NLS-1$

        int offset1= contents.indexOf("CC"); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset1, "CCC");  //$NON-NLS-1$
        assertTotalChanges(1, ch);

        int offset2= contents2.indexOf("CC"); //$NON-NLS-1$
        ch= getRefactorChanges(cpp2, offset2, "CCC");  //$NON-NLS-1$
        assertTotalChanges(2, ch);
    }
    
    public void testIncludeGuard() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#ifndef _guard            \n"); //$NON-NLS-1$
        writer.write("#define _guard            \n"); //$NON-NLS-1$
        writer.write(" int HALLO                \n"); //$NON-NLS-1$
        writer.write("#endif /* _guard */       \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("_guard"); //$NON-NLS-1$
        int offset2= contents.indexOf("_guard", offset1+1); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset2, "WELT");  //$NON-NLS-1$
        assertTotalChanges(2, 0, 1, ch);
        int off= offset1;
        assertChange(ch, cpp, off, 6, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("_guard", off+1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 6, "WELT"); //$NON-NLS-1$
    }
    
    public void testMacroParameters() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("int var;                  \n"); //$NON-NLS-1$
        writer.write("#define M1(var) var       \n"); //$NON-NLS-1$
        writer.write("#define M2(var, x) (var+x)*var  \n"); //$NON-NLS-1$
        writer.write("#define M3 var            \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("var"); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset1, "xxx");  //$NON-NLS-1$
        assertTotalChanges(1, 1, 0, ch);
    }

    public void testRenameMacroAsMacroArgument() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#define M1(var) var       \n"); //$NON-NLS-1$
        writer.write("#define M2 1              \n"); //$NON-NLS-1$
        writer.write("int b= M2;                \n"); //$NON-NLS-1$        
        writer.write("int a= M1(M2);            \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("M2"); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset1, "xxx");  //$NON-NLS-1$
        assertTotalChanges(countOccurrences(contents, "M2"), ch); //$NON-NLS-1$
    }        
}
