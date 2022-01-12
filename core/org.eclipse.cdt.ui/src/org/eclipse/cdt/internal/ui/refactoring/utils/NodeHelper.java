/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * General class for common Node operations.
 *
 * @author Lukas Felber & Guido Zgraggen
 */
public class NodeHelper {

	public static IASTDeclaration[] getDeclarations(IASTNode parent) {
		if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			return ((ICPPASTCompositeTypeSpecifier) parent).getMembers();
		} else if (parent instanceof CPPASTTranslationUnit) {
			return ((CPPASTTranslationUnit) parent).getDeclarations();
		} else if (parent instanceof CPPASTNamespaceDefinition) {
			return ((CPPASTNamespaceDefinition) parent).getDeclarations();
		}
		return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
	}

	public static IASTNode findFollowingNode(IASTNode currentNode) {
		if (currentNode == null || currentNode.getParent() == null) {
			return null;
		}
		boolean match = false;
		for (IASTNode actNode : getDeclarations(currentNode.getParent())) {
			if (match) {
				return actNode;
			}
			if (actNode.equals(currentNode)) {
				match = true;
			}
		}
		return null;
	}

	public static IASTNode findTopLevelParent(IASTNode currentNode) {
		while (currentNode != null && currentNode.getParent() != null && currentNode.getParent().getParent() != null) {
			return findTopLevelParent(currentNode.getParent());
		}
		return currentNode;
	}

	public static boolean isSameNode(IASTNode node1, IASTNode node2) {
		if (node1 == null || node2 == null) {
			return false;
		}
		return node1.getNodeLocations()[0].getNodeOffset() == node2.getNodeLocations()[0].getNodeOffset()
				&& node1.getNodeLocations()[0].getNodeLength() == node2.getNodeLocations()[0].getNodeLength()
				&& new Path(node1.getFileLocation().getFileName())
						.equals(new Path(node2.getFileLocation().getFileName()));
	}

	public static MethodContext findMethodContext(IASTNode node, CRefactoringContext refactoringContext,
			IProgressMonitor pm) throws CoreException {
		IASTTranslationUnit translationUnit = node.getTranslationUnit();
		boolean found = false;
		MethodContext context = new MethodContext();
		IASTName name = null;
		while (node != null && !found) {
			node = node.getParent();
			if (node instanceof IASTFunctionDeclarator) {
				name = ((IASTFunctionDeclarator) node).getName();
				found = true;
				context.setType(MethodContext.ContextType.FUNCTION);
			} else if (node instanceof IASTFunctionDefinition) {
				name = CPPVisitor.findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator()).getName();
				found = true;
				context.setType(MethodContext.ContextType.FUNCTION);
			}
		}
		if (name != null) {
			getMethodContexWithIndex(refactoringContext, translationUnit, name, context, pm);
		}
		return context;
	}

	private static void getMethodContexWithIndex(CRefactoringContext refactoringContext, IASTTranslationUnit ast,
			IASTName name, MethodContext context, IProgressMonitor pm) throws CoreException {
		if (name instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
			context.setMethodQName(qname);
		}
		IBinding binding = name.resolveBinding();
		if (binding instanceof ICPPMethod) {
			context.setType(MethodContext.ContextType.METHOD);
			IASTName declName = DefinitionFinder.getMemberDeclaration(name, refactoringContext, pm);
			context.setMethodDeclarationName(declName);
		}
	}

	public static boolean isMethodDeclaration(IASTSimpleDeclaration simpleDeclaration) {
		if (simpleDeclaration == null) {
			return false;
		}
		final IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		final IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
		if ((declSpecifier instanceof ICPPASTDeclSpecifier) && ((ICPPASTDeclSpecifier) declSpecifier).isFriend()) {
			return false;
		}
		return declarators.length == 1 && declarators[0] instanceof ICPPASTFunctionDeclarator;
	}

	public static boolean isContainedInTemplateDeclaration(IASTNode node) {
		return ASTQueries.findAncestorWithType(node, ICPPASTTemplateDeclaration.class) != null;
	}
}
