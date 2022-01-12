/*******************************************************************************
 * Copyright (c) 2011, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * Helper class for finding definitions and class member declarations
 */
public class DefinitionFinder {
	/**
	 * Finds the definition for the given name. The definition and the original name may belong
	 * to a different ASTs. The search is done in the index and in the ASTs of dirty editors.
	 *
	 * @param name the name to find the definition for
	 * @param context the refactoring context
	 * @param pm the progress monitor
	 * @return the definition name, or {@code null} if there is no definition or if it is
	 *     not unique.
	 * @throws CoreException thrown in case of errors
	 */
	public static IASTName getDefinition(IASTName name, CRefactoringContext context, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return null;
		}
		return getDefinition(binding, name.getTranslationUnit(), context, pm);
	}

	/**
	 * Finds the definition for the given binding. The search is done in the index and in the ASTs
	 * of dirty editors.
	 *
	 * @param binding the binding to find the definition for
	 * @param contextTu the translation unit that determines the set of files to search for
	 *     the definition. Only the files directly or indirectly included by the translation unit
	 *     are considered.
	 * @param context the refactoring context
	 * @param pm the progress monitor
	 * @return the definition name, or {@code null} if there is no definition or if it is
	 *     not unique.
	 * @throws CoreException thrown in case of errors
	 */
	public static IASTName getDefinition(IBinding binding, IASTTranslationUnit contextTu, CRefactoringContext context,
			IProgressMonitor pm) throws CoreException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		IIndex index = context.getIndex();
		if (index == null) {
			return null;
		}
		IIndexBinding indexBinding = index.adaptBinding(binding);
		if (binding == null)
			return null;
		Set<String> searchedFiles = new HashSet<>();
		List<IASTName> definitions = new ArrayList<>();
		IIndexName[] definitionsFromIndex = index.findNames(indexBinding,
				IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		int remainingCount = definitionsFromIndex.length;
		SubMonitor loopProgress = sm.newChild(6).setWorkRemaining(remainingCount);
		for (IIndexName name : definitionsFromIndex) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			IIndexFile indexFile = name.getFile();
			if (contextTu.getASTFileSet().contains(indexFile) || contextTu.getIndexFileSet().contains(indexFile)) {
				ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(indexFile.getLocation(), null);
				if (tu == null)
					return null;
				if (searchedFiles.add(tu.getLocation().toOSString())) {
					findDefinitionsInTranslationUnit(indexBinding, tu, context, definitions, pm);
					if (definitions.size() > 1)
						return null;
				}
			}
			loopProgress.setWorkRemaining(--remainingCount);
		}
		if (definitions.isEmpty()) {
			// Check dirty editors in case definition has just been introduced but not saved yet.
			IEditorPart[] dirtyEditors = EditorUtility.getDirtyEditors(true);
			loopProgress = sm.newChild(3).setWorkRemaining(dirtyEditors.length);
			for (IEditorPart editor : dirtyEditors) {
				if (sm.isCanceled()) {
					throw new OperationCanceledException();
				}
				IEditorInput editorInput = editor.getEditorInput();
				if (editorInput instanceof ITranslationUnitEditorInput) {
					ITranslationUnit tu = ((ITranslationUnitEditorInput) editorInput).getTranslationUnit();
					if (searchedFiles.add(tu.getLocation().toOSString())) {
						findDefinitionsInTranslationUnit(indexBinding, tu, context, definitions,
								loopProgress.newChild(1));
						if (definitions.size() > 1)
							return null;
					}
				}
			}
		}

		return definitions.size() == 1 ? definitions.get(0) : null;
	}

	/**
	 * Checks if the given binding has a definition. The search is done in the index and in the ASTs
	 * of dirty editors.
	 *
	 * @param binding the binding to find the definition for
	 * @param context the refactoring context
	 * @param pm the progress monitor
	 * @return <code>true</code> if the binding has a definition.
	 * @throws CoreException thrown in case of errors
	 */
	public static boolean hasDefinition(IBinding binding, CRefactoringContext context, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		IIndex index = context.getIndex();
		if (index == null) {
			return false;
		}
		IIndexBinding indexBinding = index.adaptBinding(binding);
		if (binding == null)
			return false;
		Set<String> dirtyFiles = new HashSet<>();
		IEditorPart[] dirtyEditors = EditorUtility.getDirtyEditors(true);
		for (IEditorPart editor : dirtyEditors) {
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof ITranslationUnitEditorInput) {
				ITranslationUnit tu = ((ITranslationUnitEditorInput) editorInput).getTranslationUnit();
				dirtyFiles.add(tu.getLocation().toOSString());
			}
		}

		Set<String> searchedFiles = new HashSet<>();
		IIndexName[] definitionsFromIndex = index.findNames(indexBinding,
				IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		int remainingCount = definitionsFromIndex.length;
		SubMonitor loopProgress = sm.newChild(6).setWorkRemaining(remainingCount);
		for (IIndexName name : definitionsFromIndex) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(name.getFile().getLocation(), null);
			String filename = tu.getLocation().toOSString();
			if (searchedFiles.add(filename) && (!dirtyFiles.contains(filename)
					|| hasDefinitionsInTranslationUnit(indexBinding, tu, context, loopProgress.newChild(1)))) {
				return true;
			}
			loopProgress.setWorkRemaining(--remainingCount);
		}

		// Check dirty editors in case definition has just been introduced but not saved yet.
		loopProgress = sm.newChild(3).setWorkRemaining(dirtyEditors.length);
		for (IEditorPart editor : dirtyEditors) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof ITranslationUnitEditorInput) {
				ITranslationUnit tu = ((ITranslationUnitEditorInput) editorInput).getTranslationUnit();
				String filename = tu.getLocation().toOSString();
				if (searchedFiles.add(filename)
						&& hasDefinitionsInTranslationUnit(indexBinding, tu, context, loopProgress.newChild(1))) {
					return true;
				}
			}
		}

		return false;
	}

	private static void findDefinitionsInTranslationUnit(IIndexBinding binding, ITranslationUnit tu,
			CRefactoringContext context, List<IASTName> definitions, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		IASTTranslationUnit ast = context.getAST(tu, pm);
		ArrayUtil.addAll(definitions, ast.getDefinitionsInAST(binding));
	}

	private static boolean hasDefinitionsInTranslationUnit(IIndexBinding binding, ITranslationUnit tu,
			CRefactoringContext context, IProgressMonitor pm) throws CoreException, OperationCanceledException {
		IASTTranslationUnit ast = context.getAST(tu, pm);
		return ast.getDefinitionsInAST(binding).length != 0;
	}

	/**
	 * Finds the declaration for the given class member. The declaration and the original member
	 * name may belong to a different ASTs. The search is done in the index and in the ASTs of dirty
	 * editors.
	 *
	 * @param memberName the name of the class member to find the declaration for
	 * @param context the refactoring context
	 * @param pm the progress monitor
	 * @return the declaration name, or {@code null} if there is no declaration or if it is
	 *     not unique.
	 * @throws CoreException thrown in case of errors
	 */
	public static IASTName getMemberDeclaration(IASTName memberName, CRefactoringContext context, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		IBinding binding = memberName.resolveBinding();
		if (!(binding instanceof ICPPMember))
			return null;
		return getMemberDeclaration((ICPPMember) binding, memberName.getTranslationUnit(), context, pm);
	}

	/**
	 * Finds the declaration for the given class member. The declaration and the original member
	 * name may belong to a different ASTs. The search is done in the index and in the ASTs of dirty
	 * editors.
	 *
	 * @param member the class member binding to find the declaration for
	 * @param contextTu the translation unit that determines the set of files to search for
	 *     the declaration. Only the files directly or indirectly included by the translation unit
	 *     are considered.
	 * @param context the refactoring context
	 * @param pm the progress monitor
	 * @return the declaration name, or {@code null} if there is no declaration or if it is
	 *     not unique.
	 * @throws CoreException thrown in case of errors
	 */
	public static IASTName getMemberDeclaration(ICPPMember member, IASTTranslationUnit contextTu,
			CRefactoringContext context, IProgressMonitor pm) throws CoreException, OperationCanceledException {
		IBinding classBinding = member.getClassOwner();
		IBinding memberBinding = member;
		if (member instanceof ICPPSpecialization)
			memberBinding = ((ICPPSpecialization) member).getSpecializedBinding();
		IASTName classDefintionName = getDefinition(classBinding, contextTu, context, pm);
		if (classDefintionName == null) {
			/*
			 * We didn't find the class definition, check again the template definition then if
			 * it was a template instance.
			 */
			if (classBinding instanceof ICPPTemplateInstance)
				classBinding = ((ICPPTemplateInstance) classBinding).getTemplateDefinition();
			else
				return null;
			classDefintionName = getDefinition(classBinding, contextTu, context, pm);
			if (classDefintionName == null)
				return null;
		}
		IASTCompositeTypeSpecifier compositeTypeSpecifier = ASTQueries.findAncestorWithType(classDefintionName,
				IASTCompositeTypeSpecifier.class);
		IASTTranslationUnit ast = classDefintionName.getTranslationUnit();
		IIndex index = context.getIndex();
		if (index == null) {
			return null;
		}
		IASTName[] memberDeclarationNames = ast.getDeclarationsInAST(index.adaptBinding(memberBinding));
		for (IASTName name : memberDeclarationNames) {
			if (name.getPropertyInParent() == IASTDeclarator.DECLARATOR_NAME) {
				IASTDeclaration declaration = ASTQueries.findAncestorWithType(name, IASTDeclaration.class);
				if (declaration.getParent() == compositeTypeSpecifier) {
					return name;
				}
			}
		}
		return null;
	}
}
