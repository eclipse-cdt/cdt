/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.togglefunction;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleRefactoring;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

/**
 * Tests for ToggleRefactoring for C projects.
 */
public class ToggleRefactoringCTest extends RefactoringTestBase {
	private ToggleRefactoring refactoring;

	@Override
	public void setUp() throws Exception {
		createEmptyFiles = false;
		setCpp(false);
		super.setUp();
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new ToggleRefactoring(getSelectedTranslationUnit(), getSelection(), getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		refactoring.getContext().setSettedDefaultAnswer(true);
		refactoring.getContext().setDefaultAnswer(true);
	}

	//A.h
	//void /*$*/freefunction/*$$*/() {
	//	return;
	//}
	//====================
	//void freefunction();

	//A.c
	//====================
	//#include "A.h"
	//
	//void freefunction() {
	//	return;
	//}
	public void testFileFromHeaderToImpl() throws Exception {
		assertRefactoringSuccess();
	}

	//A.c
	//#include "A.h"
	//
	//void /*$*/test/*$$*/() {
	//}
	//====================
	//#include "A.h"

	//A.h
	//void test();
	//====================
	//void test() {
	//}
	public void testToggleCFunction() throws Exception {
		assertRefactoringSuccess();
	}
}
