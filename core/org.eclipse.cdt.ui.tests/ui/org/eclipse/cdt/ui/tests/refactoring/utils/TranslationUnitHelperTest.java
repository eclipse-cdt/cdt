/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * 	   Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper;

public class TranslationUnitHelperTest extends RefactoringTestBase {
	private static class DummyRefactoring extends CRefactoring {

		public DummyRefactoring(IFile file, ISelection selection, ICElement element, ICProject proj) {
			super(file, selection, element, proj);
		}

		@Override
		public RefactoringStatus checkFinalConditions(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
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

	public TranslationUnitHelperTest() {
		super();
	}

	public TranslationUnitHelperTest(String name) {
		super(name);
	}

	@Override
	protected Refactoring createRefactoring() {
		return new DummyRefactoring(getSelectedFile(), getSelection(), null, getCProject());
	}

	private void assertFirstNodeIsAtOffset(int offset) throws Exception {
		IASTTranslationUnit ast = TranslationUnitHelper.loadTranslationUnit(getSelectedFile(), false);
		IASTNode firstNode = TranslationUnitHelper.getFirstNode(ast);
		assertEquals(offset, firstNode.getNodeLocations()[0].getNodeOffset());
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	void foo();
	//};
	//
	//#endif /*A_H_*/
	public void testBeforeClass() throws Exception {
		assertFirstNodeIsAtOffset(27);
	}

	//A.h
	//typedef int nummere;
	//
	//class A {
	//public:
	//	A();
	//};
	public void testBeforeTypedef() throws Exception {
		assertFirstNodeIsAtOffset(0);
	}
}
