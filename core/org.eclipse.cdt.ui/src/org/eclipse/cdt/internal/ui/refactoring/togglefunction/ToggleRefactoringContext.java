/*******************************************************************************
 * Copyright (c) 2011, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.IndexToASTNameHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

public class ToggleRefactoringContext {
	private IASTFunctionDefinition targetDefinition;
	private IASTFunctionDeclarator targetDeclaration;
	private IASTTranslationUnit targetDefinitionAST;
	private IASTTranslationUnit targetDeclarationAST;
	private final CRefactoringContext refactoringContext;
	private final IIndex index;
	private final ITranslationUnit selectionTU;
	private IASTTranslationUnit selectionAST;
	private IBinding binding;
	private IASTName selectionName;
	private boolean defaultAnswer;
	private boolean settedDefaultAnswer;

	public ToggleRefactoringContext(CRefactoringContext refactoringContext, IIndex index,
			ITranslationUnit translationUnit, ITextSelection selection)
			throws OperationCanceledException, CoreException {
		this.refactoringContext = refactoringContext;
		this.index = index;
		this.selectionTU = translationUnit;
		findSelectionAST();
		findSelectedFunctionDeclarator(selection);
		findBinding();
		findDeclaration();
		findDefinition();
	}

	public void findSelectedFunctionDeclarator(ITextSelection selection) {
		selectionName = new DeclaratorFinder(selection, selectionAST).getName();
	}

	public void findBinding() {
		try {
			binding = index.findBinding(selectionName);
			if (binding == null) {
				binding = selectionName.resolveBinding();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	// Declaration may still be null afterwards, but thats ok.
	public void findDeclaration() {
		try {
			IIndexName[] decnames = index.findNames(binding, IIndex.FIND_DECLARATIONS);
			if (decnames.length > 1)
				throw new NotSupportedException(Messages.ToggleRefactoringContext_MultipleDeclarations);
			for (IIndexName iname : decnames) {
				selectionAST = getASTForIndexName(iname);
				IASTName astname = IndexToASTNameHelper.findMatchingASTName(selectionAST, iname, index);
				if (astname != null) {
					targetDeclaration = findFunctionDeclarator(astname);
					targetDeclarationAST = selectionAST;
					break;
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	public void findDefinition() {
		try {
			IIndexName[] defnames = index.findNames(binding, IIndex.FIND_DEFINITIONS);
			if (defnames.length > 1) {
				throw new NotSupportedException(Messages.ToggleRefactoringContext_MultipleDefinitions);
			}
			for (IIndexName iname : defnames) {
				IASTTranslationUnit unit = getASTForIndexName(iname);
				IASTName astname = IndexToASTNameHelper.findMatchingASTName(unit, iname, index);
				if (astname != null) {
					targetDefinition = findFunctionDefinition(astname);
					targetDefinitionAST = unit;
					break;
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		if (targetDefinition == null)
			throw new NotSupportedException(Messages.ToggleRefactoringContext_NoDefinitionFound);
	}

	public IASTFunctionDeclarator getDeclaration() {
		return targetDeclaration;
	}

	public IASTFunctionDefinition getDefinition() {
		return targetDefinition;
	}

	public IASTTranslationUnit getDeclarationAST() {
		return targetDeclarationAST;
	}

	public IASTTranslationUnit getDefinitionAST() {
		return targetDefinitionAST;
	}

	public ITranslationUnit getSelectionTU() {
		return selectionTU;
	}

	public IFile getSelectionFile() {
		return (IFile) selectionTU.getResource();
	}

	public IASTTranslationUnit getASTForPartnerFile() throws CoreException {
		ITranslationUnit tu = SourceHeaderPartnerFinder.getPartnerTranslationUnit(selectionTU, refactoringContext);
		if (tu == null)
			return null;
		return refactoringContext.getAST(tu, null);
	}

	private void findSelectionAST() throws OperationCanceledException, CoreException {
		selectionAST = refactoringContext.getAST(selectionTU, null);
		if (selectionAST == null)
			throw new NotSupportedException(Messages.ToggleRefactoringContext_NoTuFound);
	}

	private IASTTranslationUnit getASTForIndexName(IIndexName indexName) throws CModelException, CoreException {
		if (isSameFileAsInTU(indexName)) {
			return selectionAST;
		}
		ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(indexName.getFile().getLocation(), null);
		if (tu == null)
			return null;
		return refactoringContext.getAST(tu, null);
	}

	private boolean isSameFileAsInTU(IIndexName indexName) {
		return indexName.getFileLocation().getFileName().equals(selectionAST.getFileLocation().getFileName());
	}

	private IASTFunctionDeclarator findFunctionDeclarator(IASTNode node) {
		if (node instanceof IASTSimpleDeclaration) {
			return (IASTFunctionDeclarator) ((IASTSimpleDeclaration) node).getDeclarators()[0];
		}
		return ASTQueries.findAncestorWithType(node, IASTFunctionDeclarator.class);
	}

	private IASTFunctionDefinition findFunctionDefinition(IASTNode node) {
		return ASTQueries.findAncestorWithType(node, IASTFunctionDefinition.class);
	}

	public void setDefaultAnswer(boolean defaultAnswer) {
		this.defaultAnswer = defaultAnswer;
	}

	public boolean getDefaultAnswer() {
		return defaultAnswer;
	}

	public void setSettedDefaultAnswer(boolean settedDefaultAnswer) {
		this.settedDefaultAnswer = settedDefaultAnswer;
	}

	public boolean isSettedDefaultAnswer() {
		return settedDefaultAnswer;
	}

	public IASTTranslationUnit getAST(IFile file, IProgressMonitor pm)
			throws OperationCanceledException, CoreException {
		ITranslationUnit tu = CoreModelUtil.findTranslationUnit(file);
		if (tu == null)
			return null;
		tu = CModelUtil.toWorkingCopy(tu);
		return refactoringContext.getAST(tu, pm);
	}
}
