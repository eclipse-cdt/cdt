/*******************************************************************************
 * Copyright (c) 2011, 2012 Google, Inc and others.
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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Helper class for finding definitions and class member declarations
 */
public class DefinitionFinder {

	public static IASTName getDefinition(IASTName name,	CRefactoringContext context,
			IProgressMonitor pm) throws CoreException {
		IIndex index = context.getIndex();
		if (index == null) {
			return null;
		}
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return null;
		}
		return getDefinition(binding, context, index, pm);
	}

	private static IASTName getDefinition(IBinding binding, CRefactoringContext context,
			IIndex index, IProgressMonitor pm) throws CoreException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		IIndexBinding indexBinding = index.adaptBinding(binding);
		if (binding == null)
			return null;
		Set<String> searchedFiles = new HashSet<String>();
		List<IASTName> definitions = new ArrayList<IASTName>();
		// TODO(sprigogin): Check index before dirty editors.
		IEditorPart[] dirtyEditors = EditorUtility.getDirtyEditors(true);
		SubMonitor loopProgress = sm.newChild(3).setWorkRemaining(dirtyEditors.length);
		for (IEditorPart editor : dirtyEditors) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof ITranslationUnitEditorInput) {
				ITranslationUnit tu =
						CModelUtil.toWorkingCopy(((ITranslationUnitEditorInput) editorInput).getTranslationUnit());
				findDefinitionsInTranslationUnit(indexBinding, tu, context, definitions, loopProgress.newChild(1));
				searchedFiles.add(tu.getLocation().toOSString());
			}
		}
		
		IIndexName[] definitionsFromIndex = index.findDefinitions(indexBinding);
		int remainingCount = definitionsFromIndex.length;
		loopProgress = sm.newChild(6).setWorkRemaining(remainingCount);
		for (IIndexName name : definitionsFromIndex) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(
					name.getFile().getLocation(), null);
			if (searchedFiles.add(tu.getLocation().toOSString())) {
				findDefinitionsInTranslationUnit(indexBinding, tu, context, definitions, pm);
			}
			loopProgress.setWorkRemaining(--remainingCount);
		}

		return definitions.size() == 1 ? definitions.get(0) : null;
	}

	private static void findDefinitionsInTranslationUnit(IIndexBinding binding, ITranslationUnit tu,
			CRefactoringContext context, List<IASTName> definitions, IProgressMonitor pm)
			throws OperationCanceledException, CoreException {
		IASTTranslationUnit ast = context.getAST(tu, pm);
		ArrayUtil.addAll(definitions, ast.getDefinitionsInAST(binding));
	}

	public static IASTName getMemberDeclaration(IASTName memberName, CRefactoringContext context,
			IProgressMonitor pm) throws CoreException {
		IIndex index = context.getIndex();
		if (index == null)
			return null;
		IBinding binding = memberName.resolveBinding();
		if (!(binding instanceof ICPPMember))
			return null;
		return getMemberDeclaration((ICPPMember) binding, context, index, pm);
	}

	private static IASTName getMemberDeclaration(ICPPMember member, CRefactoringContext context,
			IIndex index, IProgressMonitor pm) throws CoreException {
		IASTName classDefintionName = getDefinition(member.getClassOwner(), context, index, pm);
		if (classDefintionName == null)
			return null;
		IASTCompositeTypeSpecifier compositeTypeSpecifier =
				CPPVisitor.findAncestorWithType(classDefintionName, IASTCompositeTypeSpecifier.class);
		IASTTranslationUnit ast = classDefintionName.getTranslationUnit();
		IASTName[] memberDeclarationNames = ast.getDeclarationsInAST(index.adaptBinding(member));
		for (IASTName name : memberDeclarationNames) {
			if (name.getPropertyInParent() == IASTDeclarator.DECLARATOR_NAME) {
				IASTDeclaration declaration = CPPVisitor.findAncestorWithType(name, IASTDeclaration.class);
				if (declaration.getParent() == compositeTypeSpecifier) {
					return name;
				}
			}
		}
		return null;
	}
}
