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
public class RenameTemplatesTests extends RenameTestBase {

	public RenameTemplatesTests(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(true);
	}

	public static Test suite(boolean cleanup) {
		TestSuite suite = new TestSuite(RenameTemplatesTests.class);
		if (cleanup) {
			suite.addTest(new RefactoringTests("cleanupProject"));
		}
		return suite;
	}

	public void testClassTemplate() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("template <class Type>   \n");
		writer.write("class Array {                \n");
		writer.write("public:                   \n");
		writer.write("   Array(unsigned sz) {}  \n");
		writer.write("   ~Array(){}             \n");
		writer.write("   Type& operator[] (unsigned idx); \n");
		writer.write("};                        \n");
		writer.write("template <class Type>     \n");
		writer.write("inline Type& Array<Type>::operator[] (unsigned index) {\n");
		writer.write("   return 1;              \n");
		writer.write("};                        \n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("Array");

		RefactoringStatus stat = checkConditions(cpp, offset1, "WELT");
		assertRefactoringOk(stat);

		Change ch = getRefactorChanges(cpp, offset1, "WELT");
		assertTotalChanges(4, ch);
	}

	public void _testRenameSpecializations_bug240692() throws Exception {
		StringWriter writer = new StringWriter();

		writer.write("template <class T>\n");
		writer.write("class CSome {\n");
		writer.write("public:\n");
		writer.write("    void Foo() {};\n");
		writer.write("};\n");

		writer.write("int main ()\n");
		writer.write("{\n");
		writer.write("    CSome <int> A;\n");
		writer.write("    A.Foo();\n");
		writer.write("    return 0;\n");
		writer.write("}\n");
		String contents = writer.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("Foo");

		RefactoringStatus stat = checkConditions(cpp, offset1, "Baz");
		assertRefactoringOk(stat);

		assertTotalChanges(2, getRefactorChanges(cpp, offset1, "Baz"));
	}
}
