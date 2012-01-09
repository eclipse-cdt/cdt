/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
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
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLabelStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLinkageSpecification;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;

/**
 * The NodeCommenter contains all the logic that is needed for the ASTCommentVisitor to assign
 * the comments to the suitable node. Together with the ASTCommenterVisitor it fills all
 * the comments with the correspondent node into the NodeCommentMap.
 * 
 * Following, a little explanation of the assignment logic. It is only a loose illustration
 * a detailed description would include a combined explanation of ASTCommenterVisitor and
 * NodeCommenter.<br>
 * To understand the logic we define the three types of comments:<br>
 * leading comments - Comments before a statement, declaration, or definition.<br>
 * trailing comments - Comments right after the AST node on the same line.<br>
 * freestanding comments - Comments before a closing brace such as they occur in 
 * namespace-, class- and method-definitions or at the end of a file.<br>
 * 
 * The first comment is fetched and the position of it is compared to the position of the actual
 * node. If the position of the comment is smaller than the comment is added to the node as leading.
 * If it is behind the node but on the same line it is added as trailing. If one of these
 * possibilities match the next comment is fetched for the same check. If it doesn't match the same
 * procedure is done for all the child nodes. After checking the sub nodes the actual node is
 * checked again if the comment is trailing. Then there is also the possibility that this comment is 
 * freestanding. This is the case when the comment is not added to any child node but the position
 * is smaller than the end position of the node. 
 * 
 * @author Guido Zgraggen IFS
 */
public class NodeCommenter {
	protected ASTVisitor visitor;
	protected CommentHandler commHandler;
	protected NodeCommentMap commentMap;
	protected List<IASTNode> children;

	public NodeCommenter(ASTVisitor visitor, CommentHandler commHandler, NodeCommentMap commentMap) {
		this.visitor = visitor;
		this.commHandler = commHandler;
		this.commentMap = commentMap;
		this.children = new ArrayList<IASTNode>();
	}

	protected void writeNodeList(IASTNode[] nodes) {
		for (int i = 0; i < nodes.length; ++i) {
			nodes[i].accept(visitor);
		}
	}
	
	protected void visitNodeIfNotNull(IASTNode node){
		if (node != null){
			node.accept(visitor);
		}
	}

	protected boolean appendComment(ASTNode node, IASTComment comment) {
		ASTNode com = (ASTNode) comment;

		if (node.getFileLocation() == null) {
			// MacroExpansions have no FileLocation
			return false;
		}
				
		int nodeLineNumber = getEndingLineNumber(node);
		int commentLineNumber= getStartingLineNumber(comment);
	
		if (getNodeEndPoint(com) <= getNodeOffset(node)) {
			addLeadingCommentToMap(node, comment);
			return true;
		} else if (isTrailing(node, com, nodeLineNumber, commentLineNumber)) {
			addTrailingCommentToMap(node, comment);
			return true;
		}
		return false;
	}
	
	protected boolean appendFreestandingComment(ASTNode node, IASTComment comment) {
		ASTNode com = (ASTNode) comment;

		if (node.getFileLocation() == null) {
			// MacroExpansions have no FileLocation
			return false;
		}
		if (getNodeEndPoint(com) <= getNodeEndPoint(node)) {
			addFreestandingCommentToMap(node, comment);
			return true;
		}
		return false;
	}

	private void addLeadingCommentToMap(ASTNode node, IASTComment comment) {
		commentMap.addLeadingCommentToNode(node, comment);
		commHandler.allreadyAdded(comment);
	}

	private void addTrailingCommentToMap(ASTNode node, IASTComment comment) {
		commentMap.addTrailingCommentToNode(node, comment);
		commHandler.allreadyAdded(comment);
	}

	private void addFreestandingCommentToMap(ASTNode node, IASTComment comment) {
		commentMap.addFreestandingCommentToNode(node, comment);
		commHandler.allreadyAdded(comment);
	}

	private boolean isTrailing(ASTNode node, ASTNode com, int nodeLineNumber, int commentLineNumber) {
		if (nodeLineNumber != commentLineNumber || 
				getNodeOffset(com) < getNodeEndPoint(node) || 
				!canNotBeAddedToParent(node, com) ||
				mustBeAddedToSubnodes(node)) {
			return false;
		}
		if (getNodeOffset(com) < getNodeEndPoint(node) + 2) {
			return true;
		}
		String code = node.getTranslationUnit().getRawSignature();
		int commentOffset = getNodeOffset(com) - getNodeEndPoint(node) + getNodeEndOffset(node);
		for (int offset = getNodeEndOffset(node); offset < commentOffset; offset++) {
			if (!Character.isWhitespace(code.charAt(offset)))
				return false;
		}
		return true;
	}
	private boolean canNotBeAddedToParent(ASTNode node, ASTNode com) {
		ASTNode parent = (ASTNode) node.getParent();
		
		if (hasNodeSameEndingAsSubnode(parent)) {
			return true;
		} else if (parent instanceof IASTTranslationUnit) {
			return true;
		} else if (parent instanceof ICPPASTTemplateDeclaration) {
			return true;
		} else if (parent instanceof CPPASTIfStatement) {
			return true;
		} else if (parent instanceof ICPPASTBaseSpecifier) {
			parent = (ASTNode) parent.getParent();
		}
		return getNodeOffset(com) < getNodeEndPoint(parent);
	}

	private boolean mustBeAddedToSubnodes(ASTNode node) {
		return hasNodeSameEndingAsSubnode(node);
	}
	
	private boolean hasNodeSameEndingAsSubnode(ASTNode node) {
		if (node instanceof CPPASTFunctionDefinition) {
			return true;
		} else if (node instanceof CPPASTDeclarationStatement) {
			return true;
		} else if (node instanceof CPPASTForStatement) {
			return true;
		} else if (node instanceof CPPASTLabelStatement) {
			return true;
		} else if (node instanceof CPPASTIfStatement) {
			return true;
		} else if (node instanceof CPPASTSwitchStatement) {
			return true;
		} else if (node instanceof CPPASTWhileStatement) {
			return true;
		} else if (node instanceof CPPASTTemplateDeclaration) {
			return true;
		} else if (node instanceof CPPASTLinkageSpecification) {
			return true;
		} else if (node instanceof CPPASTExplicitTemplateInstantiation) {
			return true;
		}
		return false;
	}
	
	protected int appendComments(ASTNode node) {
		while (commHandler.hasMore()) {
			IASTComment comment = commHandler.getFirst();
			
			if (isNotSameFile(node, comment)) {
				return ASTVisitor.PROCESS_SKIP;
			}
			
			if (!appendComment(node, comment)) {
				return ASTVisitor.PROCESS_CONTINUE;
			}
		}
		return ASTVisitor.PROCESS_ABORT;
	}
	
	protected int appendFreestandingComments(ASTNode node) {
		while (commHandler.hasMore()) {
			IASTComment comment = commHandler.getFirst();
			
			if (isNotSameFile(node, comment)) {
				return ASTVisitor.PROCESS_SKIP;
			}
			
			if (appendComment(node, comment)) {
				return ASTVisitor.PROCESS_CONTINUE;
			}
			
			if (!appendFreestandingComment(node, comment)) {
				return ASTVisitor.PROCESS_CONTINUE;
			}
		}
		return ASTVisitor.PROCESS_ABORT;
	}
	
	public void appendRemainingComments(IASTDeclaration declaration) {
		while (commHandler.hasMore()) {
			IASTComment comment = commHandler.getFirst();
			if (appendComment((ASTNode)declaration, comment)) {
				continue;
			}
			addFreestandingCommentToMap((ASTNode) declaration, comment);
		}
	}
	
	private boolean isNotSameFile(IASTNode node, IASTComment comment) {
		if (node.getFileLocation() == null) {
			return true;
		}
		return !node.getFileLocation().getFileName().equals(comment.getFileLocation().getFileName());
	}

	private static int getNodeEndOffset(IASTNode node) {
		IASTFileLocation fileLocation = node.getFileLocation();
		return fileLocation.getNodeOffset() + fileLocation.getNodeLength();
	}
	
	private static int getNodeOffset(ASTNode node) {
		return node.getOffset();
	}

	private static int getNodeEndPoint(ASTNode node) {
		return node.getOffset() + node.getLength();
	}

	private static int getStartingLineNumber(IASTNode node) {
		return node.getFileLocation().getStartingLineNumber();
	}

	private static int getEndingLineNumber(IASTNode node) {
		return node.getFileLocation().getEndingLineNumber();
	}
}
