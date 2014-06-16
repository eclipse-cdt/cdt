/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google) 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RenameMacroTests extends RenameTestBase {

    public RenameMacroTests(String name) {
        super(name);
    }

    public static Test suite(){
        return suite(true);
    }

    public static Test suite(boolean cleanup) {
        TestSuite suite = new TestSuite(RenameMacroTests.class); 
        if (cleanup) {
            suite.addTest(new RefactoringTests("cleanupProject"));    //$NON-NLS-1$
        }
        return suite;
    }

    public void testMacroRename() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("#define HALLO x   \n"); //$NON-NLS-1$
        buf.append("class v1 {                \n"); //$NON-NLS-1$
        buf.append(" int HALLO;                   \n"); //$NON-NLS-1$
        buf.append("};                        \n"); //$NON-NLS-1$
        buf.append("class HALLO {                \n"); //$NON-NLS-1$
        buf.append(" int v;                   \n"); //$NON-NLS-1$
        buf.append("};                        \n"); //$NON-NLS-1$
        buf.append("class v3 {                \n"); //$NON-NLS-1$
        buf.append(" int v;                   \n"); //$NON-NLS-1$
        buf.append("};                        \n"); //$NON-NLS-1$
        buf.append("class v4 {                \n"); //$NON-NLS-1$
        buf.append(" int HALLO();          \n"); //$NON-NLS-1$
        buf.append("};                        \n"); //$NON-NLS-1$
        buf.append("int v4::HALLO(){}      \n"); //$NON-NLS-1$
        buf.append("void f(int par1){         \n"); //$NON-NLS-1$
        buf.append("  {                       \n"); //$NON-NLS-1$
        buf.append("     int HALLO; v1::v++;    \n"); //$NON-NLS-1$
        buf.append("  }                       \n"); //$NON-NLS-1$
        buf.append("}                         \n"); //$NON-NLS-1$
        String contents = buf.toString();
        IFile cpp= importFile("test.cpp", contents); //$NON-NLS-1$
        
        int offset1= contents.indexOf("HALLO"); //$NON-NLS-1$
        int offset2= contents.indexOf("HALLO", offset1 + 1); //$NON-NLS-1$
        
        Change ch= getRefactorChanges(cpp, offset1, "WELT");  //$NON-NLS-1$
        assertTotalChanges(6, ch);
        int off= offset1;
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$

        ch= getRefactorChanges(cpp, offset2, "WELT");  //$NON-NLS-1$
        assertTotalChanges(6, ch);
        off= offset1;
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 5, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("HALLO", off + 1); //$NON-NLS-1$
    }
    
    public void testMacroNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringBuilder buf = new StringBuilder();
        buf.append("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        buf.append("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        buf.append("#define MACRO 1           \n"); //$NON-NLS-1$
        buf.append("int v1(); int v2(); int v3();  \n"); //$NON-NLS-1$
        buf.append("static int s1();          \n"); //$NON-NLS-1$
        buf.append("static int s2();          \n"); //$NON-NLS-1$
        buf.append("void f(int par1){         \n"); //$NON-NLS-1$
        buf.append("     int w1; v1();        \n"); //$NON-NLS-1$
        buf.append("     extern_var;          \n"); //$NON-NLS-1$
        buf.append("     var_def;             \n"); //$NON-NLS-1$
        buf.append("     enum_item;           \n"); //$NON-NLS-1$
        buf.append("}                         \n"); //$NON-NLS-1$
        String contents = buf.toString();
        IFile cpp= importFile("test.cpp", contents); //$NON-NLS-1$
        
        buf = new StringBuilder();
        buf.append("static int static_other_file();     \n"); //$NON-NLS-1$        
        importFile("other.cpp", buf.toString()); //$NON-NLS-1$
        waitForIndexer();

        int offset1= contents.indexOf("MACRO"); //$NON-NLS-1$
        
        // conflicts after renaming
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Name conflict  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("par1"), "MACRO");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "'MACRO' conflicts with the name of an existing macro."); //$NON-NLS-1$
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
        StringBuilder buf = new StringBuilder();
        buf.append("class CC {int a;};         \n"); //$NON-NLS-1$
        String contents = buf.toString();
        IFile cpp= importFile("test.cpp", contents); //$NON-NLS-1$

        buf = new StringBuilder();
        buf.append("#define CC mm              \n"); //$NON-NLS-1$
        buf.append("int CC;                   \n"); //$NON-NLS-1$
        String contents2 = buf.toString();
        IFile cpp2= importFile("test2.cpp", contents2); //$NON-NLS-1$

        int offset1= contents.indexOf("CC"); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset1, "CCC");  //$NON-NLS-1$
        assertTotalChanges(1, ch);

        int offset2= contents2.indexOf("CC"); //$NON-NLS-1$
        ch= getRefactorChanges(cpp2, offset2, "CCC");  //$NON-NLS-1$
        assertTotalChanges(2, ch);
    }
    
    public void testMacroRename_434917() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("#define CC mm\n"); //$NON-NLS-1$
        String contents = buf.toString();
        IFile header= importFile("test.h", contents); //$NON-NLS-1$

        buf = new StringBuilder();
        buf.append("#include \"test.h\"\n");
        buf.append("int CC;\n"); //$NON-NLS-1$
        String contents2 = buf.toString();
        IFile source= importFile("test.cpp", contents2); //$NON-NLS-1$

        int offset= contents.indexOf("CC"); //$NON-NLS-1$
        Change ch= getRefactorChanges(header, offset, "CCC");  //$NON-NLS-1$
        assertTotalChanges(2, ch);
    }

    public void testIncludeGuard() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("#ifndef _guard            \n"); //$NON-NLS-1$
        buf.append("#define _guard            \n"); //$NON-NLS-1$
        buf.append(" int HALLO                \n"); //$NON-NLS-1$
        buf.append("#endif /* _guard */       \n"); //$NON-NLS-1$
        String contents = buf.toString();
        IFile cpp= importFile("test.cpp", contents); //$NON-NLS-1$
        
        int offset1= contents.indexOf("_guard"); //$NON-NLS-1$
        int offset2= contents.indexOf("_guard", offset1 + 1); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset2, "WELT");  //$NON-NLS-1$
        assertTotalChanges(2, 0, 1, ch);
        int off= offset1;
        assertChange(ch, cpp, off, 6, "WELT"); //$NON-NLS-1$
        off= contents.indexOf("_guard", off + 1); //$NON-NLS-1$
        assertChange(ch, cpp, off, 6, "WELT"); //$NON-NLS-1$
    }
    
    public void testMacroParameters() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("int var;                  \n"); //$NON-NLS-1$
        buf.append("#define M1(var) var       \n"); //$NON-NLS-1$
        buf.append("#define M2(var, x) (var+x)*var  \n"); //$NON-NLS-1$
        buf.append("#define M3 var            \n"); //$NON-NLS-1$
        String contents = buf.toString();
        IFile cpp= importFile("test.cpp", contents); //$NON-NLS-1$
        
        int offset1= contents.indexOf("var"); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset1, "xxx");  //$NON-NLS-1$
        assertTotalChanges(1, 1, 0, ch);
    }

    public void testRenameMacroAsMacroArgument() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("#define M1(var) var       \n"); //$NON-NLS-1$
        buf.append("#define M2 1              \n"); //$NON-NLS-1$
        buf.append("int b= M2;                \n"); //$NON-NLS-1$        
        buf.append("int a= M1(M2);            \n"); //$NON-NLS-1$
        String contents = buf.toString();
        IFile cpp= importFile("test.cpp", contents); //$NON-NLS-1$
        
        int offset1= contents.indexOf("M2"); //$NON-NLS-1$
        Change ch= getRefactorChanges(cpp, offset1, "xxx");  //$NON-NLS-1$
        assertTotalChanges(countOccurrences(contents, "M2"), ch); //$NON-NLS-1$
    }        
}
