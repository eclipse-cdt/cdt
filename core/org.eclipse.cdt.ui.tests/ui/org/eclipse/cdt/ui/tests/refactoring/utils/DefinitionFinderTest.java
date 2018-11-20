/*******************************************************************************
 * Copyright (c) 2011, 2013 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

public class DefinitionFinderTest extends RefactoringTestBase {
	private static class DummyRefactoring extends CRefactoring {
		public DummyRefactoring(ICElement element, ISelection selection, ICProject project) {
			super(element, selection, project);
		}

		@Override
		protected RefactoringStatus checkFinalConditions(IProgressMonitor progressMonitor,
				CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
			return null;
		}

		@Override
		protected RefactoringDescriptor getRefactoringDescriptor() {
			return null;
		}

		@Override
		protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
				throws CoreException, OperationCanceledException {
		}
	}

	public DefinitionFinderTest() {
		super();
	}

	public DefinitionFinderTest(String name) {
		super(name);
	}

	@Override
	protected CRefactoring createRefactoring() {
		return new DummyRefactoring(getSelectedTranslationUnit(), getSelection(), getCProject());
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//void foo();
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//void foo() {
	//}
	public void testFindFunctionDefinition() throws Exception {
		CRefactoringContext refactoringContext = new CRefactoringContext(createRefactoring());
		try {
			IASTTranslationUnit ast = refactoringContext.getAST(getSelectedTranslationUnit(), null);
			for (IASTDeclaration declaration : ast.getDeclarations()) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTName name = ((IASTSimpleDeclaration) declaration).getDeclarators()[0].getName();
					assertNotNull(DefinitionFinder.getDefinition(name, refactoringContext, npm()));
				}
			}
		} finally {
			refactoringContext.dispose();
		}
	}
}
