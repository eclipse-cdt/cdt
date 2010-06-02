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
public class RenameTypeTests extends RenameTests {

    public RenameTypeTests(String name) {
        super(name);
    }
    public static Test suite(){
        return suite(true);
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite(RenameTypeTests.class); 
        if (cleanup) {
            suite.addTest( new RefactoringTests("cleanupProject") );    //$NON-NLS-1$
        }
        return suite;
    }
    
    
    public void testClassNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("class v1 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("class v2 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("class v3 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("class v4 {                \n"); //$NON-NLS-1$
        writer.write(" int function();          \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("int v4::function(){}      \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1::v++;    \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        int offset4= contents.indexOf("v4"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Shadowing  \n" +
        		"New element: v1  \n" +
        		"Conflicting element type: Constructor"); //$NON-NLS-1$
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
        status= checkConditions(cpp, offset4, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);
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

        // renamings conflicting with global stuff.
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
        status= checkConditions(cpp, offset4, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
     // renamings colliding with types.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset4, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testNamespaceNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("namespace v4 {            \n"); //$NON-NLS-1$
        writer.write(" int function();          \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("namespace v1 {            \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("namespace v2 {            \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("namespace v3 {            \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("int v4::function(){}      \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1::v++;    \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        int offset4= contents.indexOf("v4"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
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
        status= checkConditions(cpp, offset4, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);
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

        // renamings conflicting with global stuff.
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
        status= checkConditions(cpp, offset4, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset4, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testStructNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("struct v4 {               \n"); //$NON-NLS-1$
        writer.write(" int function();          \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("struct v1 {               \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("struct v2 {               \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("struct v3 {               \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("int v4::function(){}      \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1::v++;    \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$

        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        int offset4= contents.indexOf("v4"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
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
        status= checkConditions(cpp, offset4, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);
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

        // renamings conflicting with global stuff.
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
        status= checkConditions(cpp, offset4, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset4, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testStructNameConflictsPlainC() throws Exception {
        createCFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("struct v1 {               \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("struct v2 {               \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("struct v3 {               \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1::v++;    \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$

        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings conflicting with global stuff.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

    }

    public void testUnionNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("union v4 {                \n"); //$NON-NLS-1$
        writer.write(" int function();          \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("union v1 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("union v2 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("union v3 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("int v4::function(){}      \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("  {                       \n"); //$NON-NLS-1$
        writer.write("     int w1; v1::v++;    \n"); //$NON-NLS-1$
        writer.write("  }                       \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3::v++;     \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        int offset4= contents.indexOf("v4"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
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
        status= checkConditions(cpp, offset4, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);
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

        // renamings conflicting with global stuff.
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
        status= checkConditions(cpp, offset4, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset4, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset4, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset4, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testUnionNameConflictsPlainC() throws Exception {
        createCFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("union v1 {                \n"); //$NON-NLS-1$
        writer.write(" int v;                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("union v1 vv1;             \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1; vv1.v++;    \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.c", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings conflicting with global stuff.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

    }

    public void testEnumNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("enum v1 {                 \n"); //$NON-NLS-1$
        writer.write("    v11                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("enum v2 {                 \n"); //$NON-NLS-1$
        writer.write("    v22                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("enum v3 {                 \n"); //$NON-NLS-1$
        writer.write("     v33                  \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1; v1 v;        \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("     int w2; v2 v;         \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("     int w3; v3 v;         \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        int offset2= contents.indexOf("v2"); //$NON-NLS-1$
        int offset3= contents.indexOf("v3"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
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

        // renamings conflicting with global stuff.
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

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testEnumNameConflictsPlainC() throws Exception {
        createCppFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCppDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("enum v1 {                 \n"); //$NON-NLS-1$
        writer.write("    v11                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("enum v2 {                 \n"); //$NON-NLS-1$
        writer.write("    v22                   \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("enum v3 {                 \n"); //$NON-NLS-1$
        writer.write("     v33                  \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1; enum v1 v;   \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.c", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings conflicting with global stuff.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }

    public void testTypedefNameConflicts() throws Exception {
        createCppFwdDecls("cpp_fwd.hh"); //$NON-NLS-1$
        createCppDefs("cpp_def.hh"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"cpp_fwd.hh\"   \n"); //$NON-NLS-1$
        writer.write("#include \"cpp_def.hh\"   \n"); //$NON-NLS-1$
        writer.write("typedef int v1;           \n"); //$NON-NLS-1$
        writer.write("typedef long v2;          \n"); //$NON-NLS-1$
        writer.write("typedef struct {int a;} v3; \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1; v1 v;        \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        writer.write("void class_def::method(int par2) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w2; v2 v;         \n"); //$NON-NLS-1$
        writer.write("  }                        \n"); //$NON-NLS-1$
        writer.write("}                          \n"); //$NON-NLS-1$
        writer.write("static void class_def::static_method(int par3) { \n"); //$NON-NLS-1$
        writer.write("  {                        \n"); //$NON-NLS-1$
        writer.write("     int w3; v3 v;         \n"); //$NON-NLS-1$
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
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
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

        // renamings conflicting with global stuff.
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

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);

        status= checkConditions(cpp, offset2, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset2, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset2, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        
        status= checkConditions(cpp, offset3, "class_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "class_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: class_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "namespace_def");  //$NON-NLS-1$
        assertRefactoringWarning(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: namespace_def  \n" +
        		"Conflicting element type: Namespace"); //$NON-NLS-1$
        status= checkConditions(cpp, offset3, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset3, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }
    
    public void testTypedefNameConflictsPlainC() throws Exception {
        createCFwdDecls("c_fwd.h"); //$NON-NLS-1$
        createCDefs("c_def.h"); //$NON-NLS-1$
        StringWriter writer = new StringWriter();
        writer.write("#include \"c_fwd.h\"   \n"); //$NON-NLS-1$
        writer.write("#include \"c_def.h\"   \n"); //$NON-NLS-1$
        writer.write("typedef int v1;           \n"); //$NON-NLS-1$
        writer.write("typedef long v2;          \n"); //$NON-NLS-1$
        writer.write("typedef struct {int a;} v3; \n"); //$NON-NLS-1$
        writer.write("void f(int par1){         \n"); //$NON-NLS-1$
        writer.write("     int w1; v1 v;        \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.c", contents ); //$NON-NLS-1$
        
        int offset1= contents.indexOf("v1"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "w1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, contents.indexOf("par1"), "v1");  //$NON-NLS-1$ //$NON-NLS-2$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "par1");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "extern_var");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "var_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "enum_item");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "method");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "static_method");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings conflicting with global stuff.
        status= checkConditions(cpp, offset1, "func_proto");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_proto_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "func_def_ov");  //$NON-NLS-1$
        assertRefactoringOk(status);

        // renamings colliding with types.
        status= checkConditions(cpp, offset1, "struct_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_fwd");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_fwd  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "struct_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: struct_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "union_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: union_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "enum_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: enum_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "typedef_def");  //$NON-NLS-1$
        assertRefactoringError(status, "A conflict was encountered during refactoring.  \n" +
        		"Type of problem: Redeclaration  \n" +
        		"New element: typedef_def  \n" +
        		"Conflicting element type: Type"); //$NON-NLS-1$
        status= checkConditions(cpp, offset1, "st_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
        status= checkConditions(cpp, offset1, "un_member");  //$NON-NLS-1$
        assertRefactoringOk(status);
    }
    
    public void testRenameClass() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class String              \n"); //$NON-NLS-1$
        writer.write("{                         \n"); //$NON-NLS-1$
        writer.write("public:                   \n"); //$NON-NLS-1$
        writer.write("  String();               \n"); //$NON-NLS-1$
        writer.write("  String(const String &other); \n"); //$NON-NLS-1$
        writer.write("  ~String();                   \n"); //$NON-NLS-1$
        writer.write("  String &operator=( const String &other ); \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("  String::String(){}      \n"); //$NON-NLS-1$
        writer.write("  String::String(const String &other){}; \n"); //$NON-NLS-1$
        writer.write("  String::~String(){};                   \n"); //$NON-NLS-1$
        writer.write("  String& String::operator=( const String &other ) \n"); //$NON-NLS-1$
        writer.write("     {return *this;}                        \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$

        int offset1= contents.indexOf("String"); //$NON-NLS-1$
        
        // conflicting renamings
        RefactoringStatus status= checkConditions(cpp, offset1, "CString");  //$NON-NLS-1$
        assertRefactoringOk(status);
        Change ch= getRefactorChanges(cpp, offset1, "CString"); //$NON-NLS-1$
        assertTotalChanges(countOccurrences(contents, "String"), ch); //$NON-NLS-1$
    }
    
    public void testBug72888() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class MyEx {};            \n"); //$NON-NLS-1$
        writer.write("void someFunc() {         \n"); //$NON-NLS-1$
        writer.write("  throw MyEx();          \n"); //$NON-NLS-1$
        writer.write("};                        \n"); //$NON-NLS-1$
        writer.write("int main(){               \n"); //$NON-NLS-1$
        writer.write("   try{                   \n"); //$NON-NLS-1$
        writer.write("      someFunc();         \n"); //$NON-NLS-1$
        writer.write("   } catch(MyEx &e) {}    \n"); //$NON-NLS-1$
        writer.write("   return 0;              \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$
        String contents = writer.toString();
        IFile cpp= importFile("test.cpp", contents ); //$NON-NLS-1$
        
        int offset =  contents.indexOf("MyEx") ; //$NON-NLS-1$
        Change changes = getRefactorChanges(cpp, offset, "xx"); //$NON-NLS-1$
        assertTotalChanges( 3, changes );
    }
}
