/*******************************************************************************
 * Copyright (c) 2011, 2012 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;

public class DefinitionFinderTest extends RefactoringTest {

	public DefinitionFinderTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
	}

	@Override
	protected void runTest() throws Throwable {
		IFile file = project.getFile(fileName);
		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
		CRefactoring2 refactoring = new CRefactoring2(tu, null, tu.getCProject()) {
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
		};

		CRefactoringContext refactoringContext = new CRefactoringContext(refactoring);
		try {
			IASTTranslationUnit ast = refactoringContext.getAST(tu, null);
			for (IASTDeclaration declaration : ast.getDeclarations()) {
				if (declaration instanceof IASTSimpleDeclaration) {
					assertNotNull(DefinitionFinder.getDefinition((IASTSimpleDeclaration) declaration,
							refactoringContext, NULL_PROGRESS_MONITOR));	
				}
			}
		} finally {
			refactoringContext.dispose();
		}
	}
}
