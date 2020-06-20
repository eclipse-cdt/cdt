/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author markus.schorn@windriver.com
 */
public class RenameVariableTests extends RenameTestBase {

	public RenameVariableTests(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(true);
	}

	public static Test suite(boolean cleanup) {
		TestSuite suite = new TestSuite(RenameVariableTests.class);

		if (cleanup) {
			suite.addTest(new RefactoringTests("cleanupProject"));
		}
		return suite;
	}

	public void testLocalNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("void f(int par1) {         \n");
		writer.write("  int v1, x1;              \n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1++;        \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  int v2;                  \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2++;         \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  int v3;                  \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3++;         \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("w1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: v1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("x1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: v1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset2, "w2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w2  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset2, "par2");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par2  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset2, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset3, "w3");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w3  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset3, "par3");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par3  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset3, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings depending on scope
		status = checkConditions(cpp, offset1, "member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "member");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "method");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: method  \n" + "Conflicting element type: Method");
		status = checkConditions(cpp, offset2, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");
		//        lookup inside a static method also returns non-static members
		//        we may want to have a check whether a binding is accessible or not.

		//        status= checkConditions(cpp, offset3, "member");
		//        assertRefactoringOk(status);
		//        status= checkConditions(cpp, offset3, "method");
		//        assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset3, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");

		// renamings that are ok.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_def_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_def_ov");
		assertRefactoringOk(status);

		// renamings that are ok.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);
	}

	public void testLocalNameConflictsPlainC() throws Exception {
		createCFwdDecls("c_fwd.h");
		createCDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("void f(int par1) {     \n");
		writer.write("  int v1, x1, w1;      \n");
		writer.write("  w1++; v1++;          \n");
		writer.write("}                      \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.c", contents);

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("w1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: v1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("x1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: v1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings that are ok.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

	}

	public void testParameterNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("void f(int par1, int v1) {\n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1++;        \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2, int v2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2++;         \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3, int v3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3++;         \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("w1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Redeclaration  \n" + "New element: v1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset2, "w2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w2  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset2, "par2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: par2  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset2, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset3, "w3");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w3  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset3, "par3");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: par3  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset3, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings depending on scope
		status = checkConditions(cpp, offset1, "member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "member");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "method");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: method  \n" + "Conflicting element type: Method");
		status = checkConditions(cpp, offset2, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");
		//        lookup inside a static method also returns non-static members
		//        we may want to have a check whether a binding is accessible or not.

		//        status= checkConditions(cpp, offset3, "member");
		//        assertRefactoringOk(status);
		//        status= checkConditions(cpp, offset3, "method");
		//        assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset3, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");

		// renamings that are ok.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_def_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_def_ov");
		assertRefactoringOk(status);

		// renamings that are ok.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);
	}

	public void testParameterNameConflictsPlainC() throws Exception {
		createCFwdDecls("c_fwd.h");
		createCDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("void f(int par1, int v1) {\n");
		writer.write("     int w1; v1++;        \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile c = importFile("test.c", contents);

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(c, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(c, contents.indexOf("w1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(c, contents.indexOf("par1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Redeclaration  \n" + "New element: v1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(c, offset1, "par1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(c, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings that are ok.
		status = checkConditions(c, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "func_def_ov");
		assertRefactoringOk(status);

		// renamings that are ok.
		status = checkConditions(c, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "un_member");
		assertRefactoringOk(status);
	}

	public void testVaribleNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("int v1, v2, v3;           \n");
		writer.write("static int s1;            \n");
		writer.write("static int s2;            \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1++;        \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2++;         \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3++;         \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		writer = new StringWriter();
		writer.write("static int static_other_file;     \n");
		importFile("other.cpp", writer.toString());

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("w1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: v1  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: v1  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset2, "w2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w2  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset2, "par2");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par2  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset2, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset3, "w3");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w3  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset3, "par3");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par3  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset3, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings depending on scope
		status = checkConditions(cpp, offset1, "member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "member");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "method");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: method  \n" + "Conflicting element type: Method");
		status = checkConditions(cpp, offset2, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");
		//        lookup inside a static method also returns non-static members
		//        we may want to have a check whether a binding is accessible or not.

		//        status= checkConditions(cpp, offset3, "member");
		//        assertRefactoringOk(status);
		//        status= checkConditions(cpp, offset3, "method");
		//        assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset3, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");

		// renamings conflicting with global stuff.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset1, "func_proto_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");
		// would be good to see an error here
		status = checkConditions(cpp, offset1, "func_def_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset2, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset2, "func_proto_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset2, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");
		// would be good to see an error here
		status = checkConditions(cpp, offset2, "func_def_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_proto_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_def_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def_ov  \n" + "Conflicting element type: Global function");

		// renamings that are ok.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);

		// file static stuff
		status = checkConditions(cpp, contents.indexOf("s1"), "s2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: s2  \n" + "Conflicting element type: File static variable");

		status = checkConditions(cpp, contents.indexOf("s1"), "static_other_file");
		assertRefactoringOk(status);
	}

	public void testVaribleNameConflictsPlainC() throws Exception {
		createCFwdDecls("c_fwd.h");
		createCDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("int v1;                \n");
		writer.write("static int s1;            \n");
		writer.write("static int s2;            \n");
		writer.write("void f(int par1){         \n");
		writer.write("     int w1; v1++;        \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile c = importFile("test.c", contents);

		writer = new StringWriter();
		writer.write("static int static_other_file;     \n");
		importFile("other.c", writer.toString());

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(c, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(c, contents.indexOf("w1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: v1  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, contents.indexOf("par1"), "v1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: v1  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, offset1, "par1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(c, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings conflicting with global stuff.
		status = checkConditions(c, offset1, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(c, offset1, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");

		// renamings that are ok.
		status = checkConditions(c, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "un_member");
		assertRefactoringOk(status);

		// file static stuff
		status = checkConditions(c, contents.indexOf("s1"), "s2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: s2  \n" + "Conflicting element type: File static variable");

		status = checkConditions(c, contents.indexOf("s1"), "static_other_file");
		assertRefactoringOk(status);
	}

	public void testEnumeratorNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("enum E {v1, v2, v3};      \n");
		writer.write("static int s1;            \n");
		writer.write("static int s2;            \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1=v1;           \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2=v2;            \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3=v3;            \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		writer = new StringWriter();
		writer.write("static int static_other_file;     \n");
		importFile("other.cpp", writer.toString());

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("w1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Enumerator");
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Enumerator");
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset2, "w2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w2  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset2, "par2");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par2  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset2, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset3, "w3");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w3  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset3, "par3");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par3  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset3, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset3, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings depending on scope
		status = checkConditions(cpp, offset1, "member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "member");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "method");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: method  \n" + "Conflicting element type: Method");
		status = checkConditions(cpp, offset2, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");
		status = checkConditions(cpp, offset3, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset3, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");

		// renamings conflicting with global stuff.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset1, "func_proto_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");
		// would be good to see an error here
		status = checkConditions(cpp, offset1, "func_def_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset2, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset2, "func_proto_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset2, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");
		// would be good to see an error here
		status = checkConditions(cpp, offset2, "func_def_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_proto_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto_ov  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");
		status = checkConditions(cpp, offset3, "func_def_ov");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def_ov  \n" + "Conflicting element type: Global function");

		// renamings that are ok.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Constructor");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);

		// file static stuff
		status = checkConditions(cpp, contents.indexOf("s1"), "s2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: s2  \n" + "Conflicting element type: File static variable");

		status = checkConditions(cpp, contents.indexOf("s1"), "static_other_file");
		assertRefactoringOk(status);
	}

	public void testEnumeratorNameConflictsPlainC() throws Exception {
		createCFwdDecls("c_fwd.h");
		createCDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("enum E {v1, v2, v3};      \n");
		writer.write("static int s1;            \n");
		writer.write("static int s2;            \n");
		writer.write("void f(int par1){         \n");
		writer.write("     int w1=v1;           \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile c = importFile("test.c", contents);

		writer = new StringWriter();
		writer.write("static int static_other_file;     \n");
		importFile("other.cpp", writer.toString());

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(c, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(c, contents.indexOf("w1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Enumerator");
		status = checkConditions(c, contents.indexOf("par1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Enumerator");
		status = checkConditions(c, offset1, "par1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(c, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(c, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		// renamings conflicting with global stuff.
		status = checkConditions(c, offset1, "func_proto");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_proto  \n" + "Conflicting element type: Global function");
		status = checkConditions(c, offset1, "func_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: func_def  \n" + "Conflicting element type: Global function");

		status = checkConditions(c, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(c, offset1, "un_member");
		assertRefactoringOk(status);
	}

	public void testMemberNameConflicts1() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("class Dummy {             \n");
		writer.write("  int v1, v2, v3;         \n");
		writer.write("  int member;         \n");
		writer.write("  int method(int);           \n");
		writer.write("  static int static_method(int);           \n");
		writer.write("  static int static_member;         \n");
		writer.write("};                        \n");
		writer.write("void Dummy::method(int par1) { \n");
		writer.write("  {                        \n");
		writer.write("     int w1; v1++;       \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void Dummy::static_method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2++;       \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("w1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset2, "w2");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: w2  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, offset2, "par2");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: par2  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset2, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset2, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Shadowing  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");

		status = checkConditions(cpp, offset2, "member");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Redeclaration  \n" + "New element: member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: method  \n" + "Conflicting element type: Method");
		status = checkConditions(cpp, offset2, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset2, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");
		status = checkConditions(cpp, offset3, "static_member");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: static_member  \n" + "Conflicting element type: Field");
		status = checkConditions(cpp, offset3, "static_method");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: static_method  \n" + "Conflicting element type: Method");
	}

	public void testMemberNameConflicts2() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("class Dummy {             \n");
		writer.write("  int v1, v2, v3;         \n");
		writer.write("};                        \n");
		writer.write("Dummy d;                  \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1; d.v1++;        \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; d.v2++;       \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; d.v3++;       \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "w2");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "par2");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "enum_item");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "w3");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "par3");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "enum_item");
		assertRefactoringOk(status);

		// renamings depending on scope
		status = checkConditions(cpp, offset1, "member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "static_method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "static_method");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "func_def_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "func_def_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);
	}

	public void testReferenceViaMacro() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define PASSON(x) (x)         \n");
		writer.write("#define INC(x) PASSON(/*pc*/x)++         \n");
		writer.write("void f() {                 \n");
		writer.write("   int v1;                  \n");
		writer.write("   INC(/*comment*/ v1);                 \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("v1");
		int offset2 = contents.indexOf("v1", offset + 1);
		Change changes = getRefactorChanges(cpp, offset2, "z");
		assertTotalChanges(2, changes);
		assertChange(changes, cpp, offset, 2, "z");
		assertChange(changes, cpp, offset2, 2, "z");
	}

	public void testReferenceViaMacro2() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define INC(x,y) x+=y      \n");
		writer.write("void f() {                 \n");
		writer.write("   int v1,v2;              \n");
		writer.write("   INC(v2,v1);             \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("v1");
		int offset2 = contents.indexOf("v1", offset + 1);
		Change changes = getRefactorChanges(cpp, offset2, "z");
		assertTotalChanges(2, changes);
		assertChange(changes, cpp, offset, 2, "z");
		assertChange(changes, cpp, offset2, 2, "z");
	}

	public void testReferenceViaMacro3() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define INC(x,y) x+=y      \n");
		writer.write("void f() {                 \n");
		writer.write("   int v1,v2;              \n");
		writer.write("   INC(v1,v1);             \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("v1");
		Change changes = getRefactorChanges(cpp, offset, "z");
		assertTotalChanges(3, changes);
		assertChange(changes, cpp, offset, 2, "z");
		offset = contents.indexOf("v1", offset + 1);
		assertChange(changes, cpp, offset, 2, "z");
		offset = contents.indexOf("v1", offset + 1);
		assertChange(changes, cpp, offset, 2, "z");
	}

	public void testReferenceViaMacro4() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define INC(x)   v2++      \n");
		writer.write("void f() {                 \n");
		writer.write("   int v1;                 \n");
		writer.write("   INC(v1);                \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("v1");
		Change changes = getRefactorChanges(cpp, offset, "z");
		assertTotalChanges(1, 1, 0, changes);
		assertChange(changes, cpp, offset, 2, "z");
	}

	public void testReferenceViaMacro5() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define INC(x)   v1++      \n");
		writer.write("void f() {                 \n");
		writer.write("   int v1,v2;              \n");
		writer.write("   INC(v2);                \n");
		writer.write("}                          \n");
		writer.write("void f2() {                 \n");
		writer.write("   int v12;                  \n");
		writer.write("   INC(v12);                  \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v1", offset1 + 1);
		Change changes = getRefactorChanges(cpp, offset2, "z");
		assertTotalChanges(2, changes);
		assertChange(changes, cpp, offset1, 2, "z");
		assertChange(changes, cpp, offset2, 2, "z");
	}

	public void testBug72646() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class C2: public C1 {     \n");
		writer.write("  C2(int x, int y);       \n");
		writer.write("  int y;                  \n");
		writer.write("};                        \n");
		writer.write("C2::C2(int x, int y)      \n");
		writer.write("   :C1(x), y(y) {}        \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("y");
		offset = contents.indexOf("y", offset + 1);
		Change changes = getRefactorChanges(cpp, offset, "z");
		assertTotalChanges(2, changes);
		assertChange(changes, cpp, offset, 1, "z");
		offset = contents.indexOf("y", offset + 1);
		offset = contents.indexOf("y", offset + 1);
		assertChange(changes, cpp, offset, 1, "z");
	}
}
