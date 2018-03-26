/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.extractfunction;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring;

/**
 * Tests for Extract Function refactoring preview.
 */
public class ExtractFunctionRefactoringPreviewTest extends RefactoringTestBase {
	private ExtractFunctionRefactoring refactoring;
	private String extractedFunctionName = "extracted";
	private String expectedSignature;

	public ExtractFunctionRefactoringPreviewTest() {
		super();
	}

	public ExtractFunctionRefactoringPreviewTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		setIncludeFolder("resources/includes/");
		super.setUp();

	}

	public static Test suite() {
		return suite(ExtractFunctionRefactoringPreviewTest.class);
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new ExtractFunctionRefactoring(getSelectedTranslationUnit(),
				getSelection(),	getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		ExtractFunctionInformation refactoringInfo = refactoring.getRefactoringInfo();
		refactoringInfo.setMethodName(extractedFunctionName);
	}

	private void assertPreview() {
		CRefactoringContext context = new CRefactoringContext(createRefactoring());
		try {
			refactoring.checkInitialConditions(new NullProgressMonitor());
		} catch (OperationCanceledException | CoreException e) {
			e.printStackTrace();
		}
		assertEquals(expectedSignature, refactoring.getSignature(extractedFunctionName));
		context.dispose();
	}

	//main.cpp
	//#include <iostream>
	//int main() {
	//	/*$*/std::cout << "hello\n";/*$$*/
	//}
	public void testPreviewVoid_() {
		expectedSignature = "void extracted()";
		assertPreview();
	}

	//main.cpp
	//int main() {
	//	int a { 2 };
	//	/*$*/a++;/*$$*/
	//	a++;
	//}
	public void testPreviewInt_Int() {
		expectedSignature = "int extracted(int a)";
		assertPreview();
	}

	//main.cpp
	//int main() {
	//	int a {2};
	//	/*$*/a/*$$*/ = 15;
	//}
	public void testPreviewIntRef_IntRef() {
		expectedSignature = "int& extracted(int& a)";
		assertPreview();
	}

	//main.cpp
	//int main() {
	//	int a[2];
	//	/*$*/a[0]/*$$*/ = 8;
	//}
	public void testPreviewAutoRefRef_IntArray() {
		expectedSignature = "int& extracted(int a[2])";
		assertPreview();
	}

	//main.cpp
	//#include <Banana>
	//
	//int main() {
	//	Banana b;
	//	/*$*/b.c/*$$*/ = 2;
	//}
	public void testPreviewStruct() {
		expectedSignature = "int& extracted(Banana& b)";
		assertPreview();
	}

	//main.cpp
	//
	//int main() {
	//	int a = /*$*/2/*$$*/;
	//}
	public void testPreviewConstexpr() {
		expectedSignature = "constexpr int extracted()";
		assertPreview();
	}

	//main.cpp
	//
	//int main() {
	//	int x;
	//	int *px;
	//	px = /*$*/&x/*$$*/;
	//}
	public void testPreviewPointer() {
		expectedSignature = "int* extracted(int& x)";
		assertPreview();
	}
}
