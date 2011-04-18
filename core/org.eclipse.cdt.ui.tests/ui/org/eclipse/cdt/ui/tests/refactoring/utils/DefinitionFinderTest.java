/*******************************************************************************
 * Copyright (c) 2011 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder2;

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
		ICElement element = CCorePlugin.getDefault().getCoreModel().create(file);
		if (element instanceof ITranslationUnit) {
			IASTTranslationUnit ast = astCache.getAST((ITranslationUnit) element, null);
			for (IASTDeclaration declaration : ast.getDeclarations()) {
				if (declaration instanceof IASTSimpleDeclaration) {
					assertNotNull(DefinitionFinder2.getDefinition((IASTSimpleDeclaration) declaration, astCache, NULL_PROGRESS_MONITOR));	
				}
			}
		}
	}
}
