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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

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

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Helper class to find definitions. This class is intended as a replacement for DefinitionFinder.
 */
public class DefinitionFinder {

	public static IASTName getDefinition(IASTSimpleDeclaration simpleDeclaration,
			RefactoringASTCache astCache, IProgressMonitor pm) throws CoreException {
		IIndex index = astCache.getIndex();
		IASTDeclarator declarator = simpleDeclaration.getDeclarators()[0];
		if (index == null) {
			return null;
		}
		IIndexBinding binding = index.adaptBinding(declarator.getName().resolveBinding());
		if (binding == null) {
			return null;
		}
		return getDefinition(binding, astCache, index, pm);
	}

	private static IASTName getDefinition(IIndexBinding binding,
			RefactoringASTCache astCache, IIndex index, IProgressMonitor pm) throws CoreException {
		Set<String> searchedFiles = new HashSet<String>();
		List<IASTName> definitions = new ArrayList<IASTName>();
		IEditorPart[] dirtyEditors = EditorUtility.getDirtyEditors(true);
		for (IEditorPart editor : dirtyEditors) {
			if (pm != null && pm.isCanceled()) {
				throw new OperationCanceledException();
			}
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof ITranslationUnitEditorInput) {
				ITranslationUnit tu =
						CModelUtil.toWorkingCopy(((ITranslationUnitEditorInput) editorInput).getTranslationUnit());
				findDefinitionsInTranslationUnit(binding, tu, astCache, definitions, null);
				searchedFiles.add(tu.getLocation().toOSString());
			}
		}
		
		IIndexName[] definitionsFromIndex = index.findDefinitions(binding);
		for (IIndexName name : definitionsFromIndex) {
			if (pm != null && pm.isCanceled()) {
				throw new OperationCanceledException();
			}
			ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(
					name.getFile().getLocation(), null);
			if (searchedFiles.add(tu.getLocation().toOSString())) {
				findDefinitionsInTranslationUnit(binding, tu, astCache, definitions, pm);
			}
		}

		return definitions.size() == 1 ? definitions.get(0) : null;
	}

	private static void findDefinitionsInTranslationUnit(IIndexBinding binding, ITranslationUnit tu,
			RefactoringASTCache astCache, List<IASTName> definitions, IProgressMonitor pm)
			throws OperationCanceledException, CoreException {
		IASTTranslationUnit ast = astCache.getAST(tu, pm);
		findDefinitionsInAST(binding, ast, tu, definitions);
	}

	private static void findDefinitionsInAST(IIndexBinding binding, IASTTranslationUnit ast,
			ITranslationUnit tu, List<IASTName> definitions) {
		for (IName definition : ast.getDefinitions(binding)) {
			if (definition instanceof IASTName) {
				definitions.add((IASTName) definition);
			}
		}
	}
}
