/*******************************************************************************
 * Copyright (c) 2005, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.refactoring.rename;

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author markus.schorn@windriver.com
 */
public class RenameVariableTests extends RenameTests {

    public RenameVariableTests(String name) {
        super(name);
    }
    public static Test suite(){
        return suite(true);
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite(RenameVariableTests.class); 

        if (cleanup) {
            suite.addTest( new RefactoringTests("cleanupProject") );    //$NON-NLS-1$
        }
        return suite;
    }
    
    public void testLocalNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("void f(int par1) {         \n"); //$NON-NLS-1$
        writer.write("  int v1, x1;              \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1++;        \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  int v2;                  \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2++;         \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  int v3;                  \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3++;         \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$

        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("x1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset2, "w2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w2  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "par2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par2  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset3, "w3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w3  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "par3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par3  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings depending on scope
        status= checkConditions(cpp, offset1, "member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
//        lookup inside a static method also returns non-static members
//        we may want to have a check whether a binding is accessible or not.
        
//        status= checkConditions(cpp, offset3, "member");  //$NON-NLS-1$
//        assertRefactoringOk(status);
//        status= checkConditions(cpp, offset3, "method");  //$NON-NLS-1$
//        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$

        // renamings that are ok.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings that are ok.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }
    
    public void testLocalNameConflictsPlainC() throws Exception {
        createCFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("void f(int par1) {     \n"); //$NON-NLS-1$
        writer.write("  int v1, x1, w1;      \n"); //$NON-NLS-1$
        writer.write("  w1++; v1++;          \n"); //$NON-NLS-1$
        writer.write("}                      \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.c", contents ); //$NON-NLS-1$

        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("x1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings that are ok.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

    }


    public void testParameterNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("void f(int par1, int v1) {\n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1++;        \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2, int v2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2++;         \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3, int v3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3++;         \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$

        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset2, "w2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w2  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "par2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: par2  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset3, "w3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w3  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "par3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: par3  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings depending on scope
        status= checkConditions(cpp, offset1, "member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
//        lookup inside a static method also returns non-static members
//        we may want to have a check whether a binding is accessible or not.
        
//        status= checkConditions(cpp, offset3, "member");  //$NON-NLS-1$
//        assertRefactoringOk(status);
//        status= checkConditions(cpp, offset3, "method");  //$NON-NLS-1$
//        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$

        // renamings that are ok.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings that are ok.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }
    
    public void testParameterNameConflictsPlainC() throws Exception {
        createCFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("void f(int par1, int v1) {\n"); //$NON-NLS-1$
        writer.write("     int w1; v1++;        \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile c= importFile("test.c", contents ); //$NON-NLS-1$

        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(c, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(c, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(c, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings that are ok.
        status= checkConditions(c, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings that are ok.
        status= checkConditions(c, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testVaribleNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("int v1, v2, v3;           \n"); //$NON-NLS-1$
        writer.write("static int s1;            \n"); //$NON-NLS-1$
        writer.write("static int s2;            \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1++;        \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2++;         \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3++;         \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write( "static int static_other_file;     \n" ); //$NON-NLS-1$        
        importFile( "other.cpp", writer.toString() ); //$NON-NLS-1$


        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset2, "w2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w2  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "par2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par2  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset3, "w3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w3  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "par3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par3  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings depending on scope
        status= checkConditions(cpp, offset1, "member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
//        lookup inside a static method also returns non-static members
//        we may want to have a check whether a binding is accessible or not.
        
//        status= checkConditions(cpp, offset3, "member");  //$NON-NLS-1$
//        assertRefactoringOk(status);
//        status= checkConditions(cpp, offset3, "method");  //$NON-NLS-1$
//        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$

        // renamings conflicting with global stuff.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        // would be good to see an error here
        status= checkConditions(cpp, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        // would be good to see an error here
        status= checkConditions(cpp, offset2, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$

        // renamings that are ok.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        // file static stuff
        status= checkConditions(cpp, contents.indexOf("s1"), "s2"); //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: s2  \n" +
        		"Conflicting element type: File static variable"); //$NON-NLS-1$

        status= checkConditions(cpp, contents.indexOf("s1"), "static_other_file"); //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringOk(status); 
    }

    public void testVaribleNameConflictsPlainC() throws Exception {
        createCFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("int v1;                \n"); //$NON-NLS-1$
        writer.write("static int s1;            \n"); //$NON-NLS-1$
        writer.write("static int s2;            \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1; v1++;        \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile c= importFile("test.c", contents ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write( "static int static_other_file;     \n" ); //$NON-NLS-1$        
        importFile( "other.c", writer.toString() ); //$NON-NLS-1$


        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(c, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(c, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings conflicting with global stuff.
        status= checkConditions(c, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$

        // renamings that are ok.
        status= checkConditions(c, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        // file static stuff
        status= checkConditions(c, contents.indexOf("s1"), "s2"); //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: s2  \n" +
        		"Conflicting element type: File static variable"); //$NON-NLS-1$

        status= checkConditions(c, contents.indexOf("s1"), "static_other_file"); //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringOk(status); 
    }

    public void testEnumeratorNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("enum E {v1, v2, v3};      \n"); //$NON-NLS-1$
        writer.write("static int s1;            \n"); //$NON-NLS-1$
        writer.write("static int s2;            \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1=v1;           \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2=v2;            \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3=v3;            \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write( "static int static_other_file;     \n" ); //$NON-NLS-1$        
        importFile( "other.cpp", writer.toString() ); //$NON-NLS-1$


        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset2, "w2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w2  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "par2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par2  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset3, "w3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w3  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "par3");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par3  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings depending on scope
        status= checkConditions(cpp, offset1, "member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$

        // renamings conflicting with global stuff.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        // would be good to see an error here
        status= checkConditions(cpp, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        // would be good to see an error here
        status= checkConditions(cpp, offset2, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def_ov  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$

        // renamings that are ok.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        // file static stuff
        status= checkConditions(cpp, contents.indexOf("s1"), "s2"); //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: s2  \n" +
        		"Conflicting element type: File static variable"); //$NON-NLS-1$

        status= checkConditions(cpp, contents.indexOf("s1"), "static_other_file"); //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringOk(status); 
    }

    public void testEnumeratorNameConflictsPlainC() throws Exception {
        createCFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("enum E {v1, v2, v3};      \n"); //$NON-NLS-1$
        writer.write("static int s1;            \n"); //$NON-NLS-1$
        writer.write("static int s2;            \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1=v1;           \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile c= importFile("test.c", contents ); //$NON-NLS-1$
        
        writer = new StringWriter();
        writer.write( "static int static_other_file;     \n" ); //$NON-NLS-1$        
        importFile( "other.cpp", writer.toString() ); //$NON-NLS-1$


        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(c, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(c, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$
        status= checkConditions(c, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        // renamings conflicting with global stuff.
        status= checkConditions(c, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_proto  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$
        status= checkConditions(c, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: func_def  \n" +
        		"Conflicting element type: Global function"); //$NON-NLS-1$

        status= checkConditions(c, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(c, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testMemberNameConflicts1() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("class Dummy {             \n"); //$NON-NLS-1$
        writer.write("  int v1, v2, v3;         \n"); //$NON-NLS-1$        
        writer.write("  int member;         \n"); //$NON-NLS-1$
        writer.write("  int method(int);           \n"); //$NON-NLS-1$
        writer.write("  static int static_method(int);           \n"); //$NON-NLS-1$
        writer.write("  static int static_member;         \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$        
        writer.write("void Dummy::method(int par1) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w1; v1++;       \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void Dummy::static_method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2++;       \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$

        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w1  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("w1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par1  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$

        status= checkConditions(cpp, offset2, "w2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: w2  \n" +
        		"Conflicting element type: Local variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "par2");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: par2  \n" +
        		"Conflicting element type: Parameter"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "extern_var");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: extern_var  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "var_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: var_def  \n" +
        		"Conflicting element type: Global variable"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_item");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: enum_item  \n" +
        		"Conflicting element type: Enumerator"); //$NON-NLS-1$


        status= checkConditions(cpp, offset2, "member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "static_member");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: static_member  \n" +
        		"Conflicting element type: Field"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "static_method");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: static_method  \n" +
        		"Conflicting element type: Method"); //$NON-NLS-1$
    }

    public void testMemberNameConflicts2() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("class Dummy {             \n"); //$NON-NLS-1$
        writer.write("  int v1, v2, v3;         \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$        
        writer.write("Dummy d;                  \n"); //$NON-NLS-1$        
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; d.v1++;        \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; d.v2++;       \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; d.v3++;       \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status); 

        status= checkConditions(cpp, offset2, "w2");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "par2");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status); 

        status= checkConditions(cpp, offset3, "w3");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "par3");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status); 

        // renamings depending on scope
        status= checkConditions(cpp, offset1, "member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "member");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "method");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status); 

        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringOk(status); 
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }
    
    public void testReferenceViaMacro() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#define PASSON(x) (x)         \n"); //$NON-NLS-1$
        writer.write("#define INC(x) PASSON(/*pc*/x)++         \n"); //$NON-NLS-1$
        writer.write("void f() {                 \n"); //$NON-NLS-1$
        writer.write("   int v1;                  \n"); //$NON-NLS-1$
        writer.write("   INC(/*comment*/ v1);                 \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset =  contents.indexOf("v1") ; //$NON-NLS-1$
        int offset2=  contents.indexOf("v1", offset+1) ; //$NON-NLS-1$        
        Change changes = getRefactorChanges(cpp, offset2, "z"); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, cpp, offset, 2, "z" );  //$NON-NLS-1$
        assertChange( changes, cpp, offset2, 2, "z" );  //$NON-NLS-1$
    }

    public void testReferenceViaMacro2() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#define INC(x,y) x+=y      \n"); //$NON-NLS-1$
        writer.write("void f() {                 \n"); //$NON-NLS-1$
        writer.write("   int v1,v2;              \n"); //$NON-NLS-1$
        writer.write("   INC(v2,v1);             \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset =  contents.indexOf("v1") ; //$NON-NLS-1$
        int offset2= contents.indexOf("v1", offset+1) ; //$NON-NLS-1$  
        Change changes = getRefactorChanges(cpp, offset2, "z"); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, cpp, offset, 2, "z" );  //$NON-NLS-1$
        assertChange( changes, cpp, offset2, 2, "z" );  //$NON-NLS-1$
    }

    public void testReferenceViaMacro3() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#define INC(x,y) x+=y      \n"); //$NON-NLS-1$
        writer.write("void f() {                 \n"); //$NON-NLS-1$
        writer.write("   int v1,v2;              \n"); //$NON-NLS-1$
        writer.write("   INC(v1,v1);             \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset =  contents.indexOf("v1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(cpp, offset, "z"); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
        assertChange( changes, cpp, offset, 2, "z" );  //$NON-NLS-1$
        offset=  contents.indexOf("v1", offset+1) ; //$NON-NLS-1$        
        assertChange( changes, cpp, offset, 2, "z" );  //$NON-NLS-1$
        offset=  contents.indexOf("v1", offset+1) ; //$NON-NLS-1$        
        assertChange( changes, cpp, offset, 2, "z" );  //$NON-NLS-1$
    }

    public void testReferenceViaMacro4() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#define INC(x)   v2++      \n"); //$NON-NLS-1$
        writer.write("void f() {                 \n"); //$NON-NLS-1$
        writer.write("   int v1;                 \n"); //$NON-NLS-1$
        writer.write("   INC(v1);                \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset =  contents.indexOf("v1") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(cpp, offset, "z"); //$NON-NLS-1$
        assertTotalChanges( 1, 1, 0, changes );
        assertChange( changes, cpp, offset, 2, "z" );  //$NON-NLS-1$
    }

    public void testReferenceViaMacro5() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("#define INC(x)   v1++      \n"); //$NON-NLS-1$
        writer.write("void f() {                 \n"); //$NON-NLS-1$
        writer.write("   int v1,v2;              \n"); //$NON-NLS-1$
        writer.write("   INC(v2);                \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("void f2() {                 \n"); //$NON-NLS-1$
        writer.write("   int v12;                  \n"); //$NON-NLS-1$
        writer.write("   INC(v12);                  \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1=  contents.indexOf("v1") ; //$NON-NLS-1$
        int offset2=  contents.indexOf("v1", offset1+1) ; //$NON-NLS-1$        
        Change changes = getRefactorChanges(cpp, offset2, "z"); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, cpp, offset1, 2, "z" );  //$NON-NLS-1$
        assertChange( changes, cpp, offset2, 2, "z" );  //$NON-NLS-1$
    }
    
    public void testBug72646() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class C2: public C1 {     \n"); //$NON-NLS-1$
        writer.write("  C2(int x, int y);       \n"); //$NON-NLS-1$
        writer.write("  int y;                  \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("C2::C2(int x, int y)      \n"); //$NON-NLS-1$
        writer.write("   :C1(x), y(y) {}        \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset =  contents.indexOf("y") ; //$NON-NLS-1$
        offset=  contents.indexOf("y", offset+1) ; //$NON-NLS-1$        
        Change changes = getRefactorChanges(cpp, offset, "z"); //$NON-NLS-1$
        assertTotalChanges( 2, changes );
        assertChange( changes, cpp, offset, 1, "z" );  //$NON-NLS-1$
        offset=  contents.indexOf("y", offset+1) ; //$NON-NLS-1$        
        offset=  contents.indexOf("y", offset+1) ; //$NON-NLS-1$        
        assertChange( changes, cpp, offset, 1, "z" );  //$NON-NLS-1$
    }
}
