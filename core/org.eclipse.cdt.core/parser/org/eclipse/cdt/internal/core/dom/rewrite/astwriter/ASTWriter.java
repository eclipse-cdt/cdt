/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGeneratorWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * ASTWriter main class. Generates source code from <code>IASTNode</code>.
 * Uses a {@link ChangeGeneratorWriterVisitor} to generate the code for the given nodes.
 *
 * @author Emanuel Graf
 */
public class ASTWriter {
	private final ASTModificationStore modificationStore = new ASTModificationStore();

	/**
	 * Creates a <code>ASTWriter</code>.
	 */
	public ASTWriter() {
		super();
	}

	/**
	 * Generates the source code representing this node.
	 *
	 * @param rootNode Node to write.
	 * @return A <code>String</code> representing the source code for the node.
	 * @throws ProblemRuntimeException if the node or one of it's children is a <code>IASTProblemNode</code>.
	 */
	public String write(IASTNode rootNode) throws ProblemRuntimeException {
		return write(rootNode, new NodeCommentMap());
	}

	/**
	 * Generates the source code representing this node including comments.
	 *
	 * @param rootNode Node to write.
	 * @param commentMap comments for the translation unit
	 * @return A <code>String</code> representing the source code for the node.
	 * @throws ProblemRuntimeException if the node or one of it's children is
	 *     an <code>IASTProblemNode</code>.
	 *
	 * @see ASTCommenter#getCommentedNodeMap(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
	 */
	public String write(IASTNode rootNode, NodeCommentMap commentMap) throws ProblemRuntimeException {
		ChangeGeneratorWriterVisitor writer = new ChangeGeneratorWriterVisitor(
				modificationStore, null, commentMap);
		if (rootNode != null) {
			rootNode.accept(writer);
		}
		return writer.toString();
	}

	/**
	 * Returns <code>true</code> if the node should be separated by a blank line from the node
	 * before it.
	 * 
	 * @param node The node.
	 * @return <code>true</code> if the node should be separated by a blank line from the node
	 * 	   before it.
	 */
	public static boolean requiresLeadingBlankLine(IASTNode node) {
		if (node instanceof ICPPASTTemplateDeclaration) {
			node = ((ICPPASTTemplateDeclaration) node).getDeclaration();
		}
		return node instanceof IASTASMDeclaration ||
				node instanceof IASTFunctionDefinition ||
				node instanceof ICPPASTVisibilityLabel;
	}

	/**
	 * Returns <code>true</code> if the node should be separated by a blank line from the node
	 * after it.
	 * 
	 * @param node The node.
	 * @return <code>true</code> if the node should be separated by a blank line from the node
	 *     after it.
	 */
	public static boolean requiresTrailingBlankLine(IASTNode node) {
		if (node instanceof ICPPASTNamespaceDefinition)
			return true;
		if (node instanceof IASTFunctionDefinition)
			return true;
		if (node instanceof IASTIfStatement) {
			IASTIfStatement statement = ((IASTIfStatement) node);
			IASTStatement lastClause = statement.getElseClause();
			if (lastClause == null)
				lastClause = statement.getThenClause();

			if (!(lastClause instanceof IASTCompoundStatement) &&
					!doNodesHaveSameOffset(lastClause, statement)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if there should be no blank line after this node even if a blank
	 * line is normally required before the subsequent node.
	 * 
	 * @param node The node.
	 * @return <code>true</code> if there should be no blank line after this node.
	 */
	public static boolean suppressesTrailingBlankLine(IASTNode node) {
		return node instanceof ICPPASTVisibilityLabel;
	}

	/**
	 * Returns <code>true</code> if the two given nodes should be separated by a blank line.
	 * 
	 * @param node1 The first node.
	 * @param node2 The second node.
	 * @return <code>true</code> if the blank line between the nodes is needed.
	 */
	public static boolean requireBlankLineInBetween(IASTNode node1, IASTNode node2) {
		if (node1 instanceof ContainerNode) {
			List<IASTNode> nodes = ((ContainerNode) node1).getNodes();
			if (!nodes.isEmpty()) {
				node1 = nodes.get(nodes.size() - 1);
			}
		}
		if (node2 instanceof ContainerNode) {
			List<IASTNode> nodes = ((ContainerNode) node2).getNodes();
			if (!nodes.isEmpty()) {
				node2 = nodes.get(0);
			}
		}
		while (node1 instanceof ICPPASTTemplateDeclaration) {
			node1 = ((ICPPASTTemplateDeclaration) node1).getDeclaration();
		}
		while (node2 instanceof ICPPASTTemplateDeclaration) {
			node2 = ((ICPPASTTemplateDeclaration) node2).getDeclaration();
		}
		if (node1 instanceof ICPPASTVisibilityLabel && node2 instanceof ICPPASTVisibilityLabel) {
			return true;
		}
		if (suppressesTrailingBlankLine(node1)) {
			return false;
		}
		if (node1 instanceof IASTPreprocessorIncludeStatement !=
				node2 instanceof IASTPreprocessorIncludeStatement) {
			return true;
		}
		if (isFunctionDeclaration(node1) != isFunctionDeclaration(node2)) {
			return true;
		}
		if (requiresTrailingBlankLine(node1)) {
			return true;
		}

		return requiresLeadingBlankLine(node2);
	}

	private static boolean isFunctionDeclaration(IASTNode node) {
		if (!(node instanceof IASTSimpleDeclaration)) {
			return false;
		}
		for (IASTDeclarator declarator : ((IASTSimpleDeclaration) node).getDeclarators()) {
			if (declarator instanceof IASTFunctionDeclarator)
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the two given nodes have the same offset. For nodes that are normally
	 * separated by other tokens this is an indication that they were produced by the same macro
	 * expansion.
	 */
	private static boolean doNodesHaveSameOffset(IASTNode node1, IASTNode node2) {
		return node1.getFileLocation().getNodeOffset() == node2.getFileLocation().getNodeOffset();
	}
}
