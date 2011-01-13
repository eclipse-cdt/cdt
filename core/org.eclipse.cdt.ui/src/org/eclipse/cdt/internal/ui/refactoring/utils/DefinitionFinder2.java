/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;

/**
 * Helper class to find definitions. This class is intended as a replacement for DefinitionFinder.
 */
public class DefinitionFinder2 {

	public static ASTNameInContext getDefinition(IASTSimpleDeclaration simpleDeclaration,
			RefactoringASTCache astCache) throws CoreException {
		IASTDeclarator declarator = simpleDeclaration.getDeclarators()[0];
		IIndex index = astCache.getIndex();
		if (index == null) {
			return null;
		}
		IIndexBinding binding = index.adaptBinding(declarator.getName().resolveBinding());
		if (binding == null) {
			return null;
		}
		return getDefinition(binding, astCache, index);
	}

	private static ASTNameInContext getDefinition(IIndexBinding binding,
			RefactoringASTCache astCache, IIndex index) throws CoreException {
		Set<String> searchedFiles = new HashSet<String>();
		ITranslationUnit[] workingCopies = CUIPlugin.getSharedWorkingCopies();
		List<ASTNameInContext> definitions = new ArrayList<ASTNameInContext>();
		for (ITranslationUnit tu : workingCopies) {
			findDefinitionsInTranslationUnit(binding, tu, astCache, definitions, null);
			searchedFiles.add(tu.getLocation().toOSString());
		}

		IIndexName[] definitionsFromIndex = index.findDefinitions(binding);

		if (definitionsFromIndex.length > 0) {
			for (IIndexName name : definitionsFromIndex) {
				ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(
						name.getFile().getLocation(), null);
				if (searchedFiles.add(tu.getLocation().toOSString())) {
					findDefinitionsInTranslationUnit(binding, tu, astCache, definitions, null);
				}
			}
		}
		if (definitions.size() != 1) {
			return null;
		}
		return definitions.get(0);
	}

	private static void findDefinitionsInTranslationUnit(IIndexBinding binding, ITranslationUnit tu,
			RefactoringASTCache astCache, List<ASTNameInContext> definitions, IProgressMonitor pm)
			throws OperationCanceledException, CoreException {
		IASTTranslationUnit ast = astCache.getAST(tu, pm);
		findDefinitionsInAST(binding, ast, tu, definitions);
	}

	private static void findDefinitionsInAST(IIndexBinding binding, IASTTranslationUnit ast,
			ITranslationUnit tu, List<ASTNameInContext> definitions) {
		for (IName definition : ast.getDefinitions(binding)) {
			if (definition instanceof IASTName) {
				definitions.add(new ASTNameInContext((IASTName) definition, tu));
			}
		}
	}
}
