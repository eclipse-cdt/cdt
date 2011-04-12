/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

/**
 * Given a selection and a translation unit, this class finds a
 * ICPPASTFunctionDeclarator if possible. Special case: Nested local functions
 * are skipped during search.
 */
public class DeclaratorFinder {

	private IASTFunctionDeclarator foundDeclarator;

	public DeclaratorFinder(ITextSelection selection, IASTTranslationUnit unit) {
		foundDeclarator = findDeclaratorInSelection(selection, unit);

		if (foundDeclarator == null) {
			throw new NotSupportedException(Messages.DeclaratorFinder_NoDeclarator);
		}
		if (isPartOfAStatement(foundDeclarator)) {
			throw new NotSupportedException(Messages.DeclaratorFinder_NestedFunction);
		}
	}

	public IASTName getName() {
		return foundDeclarator.getName();
	}

	private IASTFunctionDeclarator findDeclaratorInSelection(
ITextSelection selection,
			IASTTranslationUnit unit) {
		IASTNode firstNodeInsideSelection = unit.getNodeSelector(null)
				.findFirstContainedNode(selection.getOffset(),
						selection.getLength());
		IASTFunctionDeclarator declarator = findDeclaratorInAncestors(firstNodeInsideSelection);
		
		if (declarator == null) {
			firstNodeInsideSelection = unit.getNodeSelector(null).findEnclosingNode(
					selection.getOffset(), selection.getLength());
			declarator = findDeclaratorInAncestors(firstNodeInsideSelection);
		}
		return declarator;
	}

	private IASTFunctionDeclarator findDeclaratorInAncestors(IASTNode node) {
		while (node != null) {
			IASTFunctionDeclarator declarator = extractDeclarator(node);
			if (node instanceof ICPPASTTemplateDeclaration) {
				declarator = extractDeclarator(((ICPPASTTemplateDeclaration) node).getDeclaration());
			}
			if (declarator != null) {
				return declarator;
			}
			node = node.getParent();
		}
		return null;
	}

	private IASTFunctionDeclarator extractDeclarator(IASTNode node) {
		if (node instanceof ICPPASTTemplateDeclaration) {
			node = ((ICPPASTTemplateDeclaration) node).getDeclaration();
		}
		if (node instanceof IASTFunctionDeclarator) {
			return (IASTFunctionDeclarator) node;
		}
		if (node instanceof IASTFunctionDefinition) {
			return ((IASTFunctionDefinition) node).getDeclarator();
		}
		if (node instanceof IASTSimpleDeclaration) {
			IASTDeclarator[] declarators = ((IASTSimpleDeclaration) node).getDeclarators();
			if (declarators.length > 1) {
				throw new NotSupportedException(Messages.DeclaratorFinder_MultipleDeclarators);
			}
			
			if (declarators.length == 1 && 
					declarators[0] instanceof IASTFunctionDeclarator)
				return (IASTFunctionDeclarator) declarators[0];
		}
		return null;
	}

	private boolean isPartOfAStatement(IASTNode node) {
		return ToggleNodeHelper.getAncestorOfType(node, IASTStatement.class) != null;
	}
}
