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
 *     Sergey Prigogin (Google)
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
public class RenameTypeTests extends RenameTestBase {

	public RenameTypeTests(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(true);
	}

	public static Test suite(boolean cleanup) {
		TestSuite suite = new TestSuite(RenameTypeTests.class);
		if (cleanup) {
			suite.addTest(new RefactoringTests("cleanupProject"));
		}
		return suite;
	}

	public void testClassNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("class v1 {                \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("class v2 {                \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("class v3 {                \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("class v4 {                \n");
		writer.write(" int function();          \n");
		writer.write("};                        \n");
		writer.write("int v4::function(){}      \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1::v++;    \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");
		int offset4 = contents.indexOf("v4");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringError(status, "A conflict was encountered during refactoring.  \n"
				+ "Type of problem: Shadowing  \n" + "New element: v1  \n" + "Conflicting element type: Constructor");
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
		status = checkConditions(cpp, offset4, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "enum_item");
		assertRefactoringOk(status);
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

		// renamings conflicting with global stuff.
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
		status = checkConditions(cpp, offset4, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def_ov");
		assertRefactoringOk(status);

		// renamings colliding with types.
		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset4, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset4, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "un_member");
		assertRefactoringOk(status);
	}

	public void testNamespaceNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("namespace v4 {            \n");
		writer.write(" int function();          \n");
		writer.write("};                        \n");
		writer.write("namespace v1 {            \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("namespace v2 {            \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("namespace v3 {            \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("int v4::function(){}      \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1::v++;    \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");
		int offset4 = contents.indexOf("v4");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
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
		status = checkConditions(cpp, offset4, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "enum_item");
		assertRefactoringOk(status);
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

		// renamings conflicting with global stuff.
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
		status = checkConditions(cpp, offset4, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def_ov");
		assertRefactoringOk(status);

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset4, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset4, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "un_member");
		assertRefactoringOk(status);
	}

	public void testStructNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("struct v4 {               \n");
		writer.write(" int function();          \n");
		writer.write("};                        \n");
		writer.write("struct v1 {               \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("struct v2 {               \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("struct v3 {               \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("int v4::function(){}      \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1::v++;    \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");
		int offset4 = contents.indexOf("v4");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
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
		status = checkConditions(cpp, offset4, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "enum_item");
		assertRefactoringOk(status);
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

		// renamings conflicting with global stuff.
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
		status = checkConditions(cpp, offset4, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def_ov");
		assertRefactoringOk(status);

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset4, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset4, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "un_member");
		assertRefactoringOk(status);
	}

	public void testStructNameConflictsPlainC() throws Exception {
		createCFwdDecls("c_fwd.h");
		createCDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("struct v1 {               \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("struct v2 {               \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("struct v3 {               \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1::v++;    \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringOk(status);

		// renamings conflicting with global stuff.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

	}

	public void testUnionNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("union v4 {                \n");
		writer.write(" int function();          \n");
		writer.write("};                        \n");
		writer.write("union v1 {                \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("union v2 {                \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("union v3 {                \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("int v4::function(){}      \n");
		writer.write("void f(int par1){         \n");
		writer.write("  {                       \n");
		writer.write("     int w1; v1::v++;    \n");
		writer.write("  }                       \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3::v++;     \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");
		int offset4 = contents.indexOf("v4");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
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
		status = checkConditions(cpp, offset4, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "enum_item");
		assertRefactoringOk(status);
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

		// renamings conflicting with global stuff.
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
		status = checkConditions(cpp, offset4, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "func_def_ov");
		assertRefactoringOk(status);

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset4, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset4, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset4, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset4, "un_member");
		assertRefactoringOk(status);
	}

	public void testUnionNameConflictsPlainC() throws Exception {
		createCFwdDecls("c_fwd.h");
		createCDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("union v1 {                \n");
		writer.write(" int v;                   \n");
		writer.write("};                        \n");
		writer.write("union v1 vv1;             \n");
		writer.write("void f(int par1){         \n");
		writer.write("     int w1; vv1.v++;    \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.c", contents);

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringOk(status);

		// renamings conflicting with global stuff.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

	}

	public void testEnumNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("enum v1 {                 \n");
		writer.write("    v11                   \n");
		writer.write("};                        \n");
		writer.write("enum v2 {                 \n");
		writer.write("    v22                   \n");
		writer.write("};                        \n");
		writer.write("enum v3 {                 \n");
		writer.write("     v33                  \n");
		writer.write("};                        \n");
		writer.write("void f(int par1){         \n");
		writer.write("     int w1; v1 v;        \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("     int w2; v2 v;         \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("     int w3; v3 v;         \n");
		writer.write("}                          \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("v1");
		int offset2 = contents.indexOf("v2");
		int offset3 = contents.indexOf("v3");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
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

		// renamings conflicting with global stuff.
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

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);
	}

	public void testEnumNameConflictsPlainC() throws Exception {
		createCppFwdDecls("c_fwd.h");
		createCppDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("enum v1 {                 \n");
		writer.write("    v11                   \n");
		writer.write("};                        \n");
		writer.write("enum v2 {                 \n");
		writer.write("    v22                   \n");
		writer.write("};                        \n");
		writer.write("enum v3 {                 \n");
		writer.write("     v33                  \n");
		writer.write("};                        \n");
		writer.write("void f(int par1){         \n");
		writer.write("     int w1; enum v1 v;   \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.c", contents);

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringOk(status);

		// renamings conflicting with global stuff.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def_ov");
		assertRefactoringOk(status);

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);
	}

	public void testTypedefNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringWriter writer = new StringWriter();
		writer.write("#include \"cpp_fwd.hh\"   \n");
		writer.write("#include \"cpp_def.hh\"   \n");
		writer.write("typedef int v1;           \n");
		writer.write("typedef long v2;          \n");
		writer.write("typedef struct {int a;} v3; \n");
		writer.write("void f(int par1){         \n");
		writer.write("     int w1; v1 v;        \n");
		writer.write("}                         \n");
		writer.write("void class_def::method(int par2) { \n");
		writer.write("  {                        \n");
		writer.write("     int w2; v2 v;         \n");
		writer.write("  }                        \n");
		writer.write("}                          \n");
		writer.write("static void class_def::static_method(int par3) { \n");
		writer.write("  {                        \n");
		writer.write("     int w3; v3 v;         \n");
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
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
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

		// renamings conflicting with global stuff.
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

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset2, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset2, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset2, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset2, "un_member");
		assertRefactoringOk(status);

		status = checkConditions(cpp, offset3, "class_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "class_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: class_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset3, "namespace_def");
		assertRefactoringWarning(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: namespace_def  \n" + "Conflicting element type: Namespace");
		status = checkConditions(cpp, offset3, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset3, "un_member");
		assertRefactoringOk(status);
	}

	public void testTypedefNameConflictsPlainC() throws Exception {
		createCFwdDecls("c_fwd.h");
		createCDefs("c_def.h");
		StringWriter writer = new StringWriter();
		writer.write("#include \"c_fwd.h\"   \n");
		writer.write("#include \"c_def.h\"   \n");
		writer.write("typedef int v1;           \n");
		writer.write("typedef long v2;          \n");
		writer.write("typedef struct {int a;} v3; \n");
		writer.write("void f(int par1){         \n");
		writer.write("     int w1; v1 v;        \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.c", contents);

		int offset1 = contents.indexOf("v1");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, contents.indexOf("par1"), "v1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "method");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "static_method");
		assertRefactoringOk(status);

		// renamings conflicting with global stuff.
		status = checkConditions(cpp, offset1, "func_proto");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_proto_ov");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "func_def_ov");
		assertRefactoringOk(status);

		// renamings colliding with types.
		status = checkConditions(cpp, offset1, "struct_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_fwd");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_fwd  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "struct_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: struct_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "union_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: union_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "enum_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: enum_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "typedef_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Redeclaration  \n"
						+ "New element: typedef_def  \n" + "Conflicting element type: Type");
		status = checkConditions(cpp, offset1, "st_member");
		assertRefactoringOk(status);
		status = checkConditions(cpp, offset1, "un_member");
		assertRefactoringOk(status);
	}

	public void testRenameClass() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class String              \n");
		writer.write("{                         \n");
		writer.write("public:                   \n");
		writer.write("  String();               \n");
		writer.write("  String(const String &other); \n");
		writer.write("  ~String();                   \n");
		writer.write("  String &operator=(const String &other); \n");
		writer.write("};                        \n");
		writer.write("  String::String() {}     \n");
		writer.write("  String::String(const String &other) {}; \n");
		writer.write("  String::~String() {};                   \n");
		writer.write("  String& String::operator=(const String &other) \n");
		writer.write("     {return *this;}                      \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("String");

		// conflicting renamings
		RefactoringStatus status = checkConditions(cpp, offset, "CString");
		assertRefactoringOk(status);
		Change ch = getRefactorChanges(cpp, offset, "CString");
		assertTotalChanges(countOccurrences(contents, "String"), ch);
	}

	public void testRenameClassFromCtor() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class String              \n"); //$NON-NLS-1$
		writer.write("{                         \n"); //$NON-NLS-1$
		writer.write("public:                   \n"); //$NON-NLS-1$
		writer.write("  String();               \n"); //$NON-NLS-1$
		writer.write("  String(const String &other); \n"); //$NON-NLS-1$
		writer.write("  ~String();                   \n"); //$NON-NLS-1$
		writer.write("  String &operator=(const String &other); \n"); //$NON-NLS-1$
		writer.write("};                        \n"); //$NON-NLS-1$
		writer.write("  String::String() {}     \n"); //$NON-NLS-1$
		writer.write("  String::String(const String &other) {}; \n"); //$NON-NLS-1$
		writer.write("  String::~String() {};                   \n"); //$NON-NLS-1$
		writer.write("  String& String::operator=(const String &other) \n"); //$NON-NLS-1$
		writer.write("     {return *this;}                      \n"); //$NON-NLS-1$
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents); //$NON-NLS-1$

		int offset = contents.indexOf("String()"); //$NON-NLS-1$

		RefactoringStatus status = checkConditions(cpp, offset, "CString"); //$NON-NLS-1$
		assertRefactoringOk(status);
		Change ch = getRefactorChanges(cpp, offset, "CString"); //$NON-NLS-1$
		assertTotalChanges(countOccurrences(contents, "String"), ch); //$NON-NLS-1$
	}

	public void testRenameClassFromDtor() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class String              \n"); //$NON-NLS-1$
		writer.write("{                         \n"); //$NON-NLS-1$
		writer.write("public:                   \n"); //$NON-NLS-1$
		writer.write("  String();               \n"); //$NON-NLS-1$
		writer.write("  String(const String &other); \n"); //$NON-NLS-1$
		writer.write("  ~String();                   \n"); //$NON-NLS-1$
		writer.write("  String &operator=(const String &other); \n"); //$NON-NLS-1$
		writer.write("};                        \n"); //$NON-NLS-1$
		writer.write("  String::String() {}     \n"); //$NON-NLS-1$
		writer.write("  String::String(const String &other) {}; \n"); //$NON-NLS-1$
		writer.write("  String::~String() {};                   \n"); //$NON-NLS-1$
		writer.write("  String& String::operator=(const String &other) \n"); //$NON-NLS-1$
		writer.write("     {return *this;}                      \n"); //$NON-NLS-1$
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents); //$NON-NLS-1$

		int offset = contents.indexOf("~String") + 1; //$NON-NLS-1$

		RefactoringStatus status = checkConditions(cpp, offset, "CString"); //$NON-NLS-1$
		assertRefactoringOk(status);
		Change ch = getRefactorChanges(cpp, offset, "CString"); //$NON-NLS-1$
		assertTotalChanges(countOccurrences(contents, "String"), ch); //$NON-NLS-1$
	}

	public void testUsingDeclaration_332895() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("namespace ns {            \n");
		writer.write("typedef int MyType;       \n");
		writer.write("}                         \n");
		writer.write("                          \n");
		writer.write("using ns::MyType;         \n");
		writer.write("MyType a;                 \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("MyType");

		RefactoringStatus status = checkConditions(cpp, offset, "YourType");
		assertRefactoringOk(status);
		Change ch = getRefactorChanges(cpp, offset, "YourType");
		assertTotalChanges(countOccurrences(contents, "MyType"), ch);
	}

	public void testBug72888() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("class MyEx {};            \n");
		writer.write("void someFunc() {         \n");
		writer.write("  throw MyEx();           \n");
		writer.write("};                        \n");
		writer.write("int main(){               \n");
		writer.write("   try {                  \n");
		writer.write("      someFunc();         \n");
		writer.write("   } catch(MyEx &e) {}    \n");
		writer.write("   return 0;              \n");
		writer.write("}                         \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset = contents.indexOf("MyEx");
		Change changes = getRefactorChanges(cpp, offset, "xx");
		assertTotalChanges(3, changes);
	}
}
