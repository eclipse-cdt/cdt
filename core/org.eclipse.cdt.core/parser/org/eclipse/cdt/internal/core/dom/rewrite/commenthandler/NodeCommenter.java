/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.io.InputStream;
import java.util.Vector;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
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
import org.eclipse.cdt.internal.core.dom.rewrite.util.OffsetHelper;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * The NodeCommenter contains all the logic that is needed for the ASTCommentVisitor to assign the comments
 * to the suitable node. Together with the ASTCommenterVisitor it fills all the comments with the correspondent
 * node into the NodeCommentMap.
 * 
 * Following, a little explanation of the assignment logic. It is only a loose illustration a detailed description 
 * would include a combined explanation of ASTCommenterVisitor and NodeCommenter.<br>
 * To understand the logic we define the three types of comments:<br>
 * leading comments - Comments before a statement, declaration, or definition.<br>
 * trailing comments - Comments right after the AST node on the same line.<br>
 * freestanding comments - Comments before a closing brace such as they occur in 
 * namespace-, class- and method-definitions or at the end of a file.<br>
 * 
 * The first comment is fetched and the position of it is compared to the position of the actual node. If 
 * the position of the comment is smaller than the comment is added to the node as leading. If it is behind the node
 * but on the same line it is added as trailing. If one of these possibilities match the next comment is fetched for
 * the same check. If it doesn't match the same procedure is done for all the child nodes. After checking the sub nodes
 * the actual node is checked again if the comment is trailing. Then there is also the possibility that this comment is 
 * freestanding. This is the case when the comment is not added to any child node but the position is smaller den
 * the end position of the node. 
 * 
 * @author Guido Zgraggen IFS
 */
public class NodeCommenter {

	protected CPPASTVisitor visitor;
	protected CommentHandler commHandler;
	protected NodeCommentMap commentMap;
	protected Vector<IASTNode> children;

	public NodeCommenter(CPPASTVisitor visitor, CommentHandler commHandler, NodeCommentMap commentMap) {
		this.visitor = visitor;
		this.commHandler = commHandler;
		this.commentMap = commentMap;
		this.children = new Vector<IASTNode>();
	}

	protected void writeNodeList(IASTNode[] nodes) {
		for(int i = 0; i < nodes.length; ++i) {
			nodes[i].accept(visitor);
		}
	}
	
	protected void visitNodeIfNotNull(IASTNode node){
		if(node != null){
			node.accept(visitor);
		}
	}

	protected boolean appendComment(ASTNode node, IASTComment comment) {
		ASTNode com = (ASTNode) comment;

		if(node.getFileLocation() == null) {
			//MacroExpansions have no Filelocation
			return false;
		}
				
		int nodeLineNumber = OffsetHelper.getEndingLineNumber(node);
		int commentLineNumber= OffsetHelper.getStartingLineNumber(comment);
	
		if(OffsetHelper.getNodeEndPoint(com) <= OffsetHelper.getNodeOffset(node)) {
			addLeadingCommentToMap(node, comment);
			return true;
		}
		else if(isTrailing(node, com, nodeLineNumber, commentLineNumber)) {
			addTrailingCommentToMap(node, comment);
			return true;
		}
		return false;
	}
	
	protected boolean appendFreestandingComment(ASTNode node, IASTComment comment) {
		ASTNode com = (ASTNode) comment;

		if(node.getFileLocation() == null) {
			//MacroExpansions have no Filelocation
			return false;
		}
		if(OffsetHelper.getNodeEndPoint(com) <= OffsetHelper.getNodeEndPoint(node)) {
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
		if(nodeLineNumber == commentLineNumber 
				&& OffsetHelper.getNodeOffset(com) >= OffsetHelper.getNodeEndPoint(node) 
				&& canNotBeAddedToParent(node,com)
				&& !mustBeAddToSubnodes(node)) {
			
			if(OffsetHelper.getNodeOffset(com) < OffsetHelper.getNodeEndPoint(node) + 2) {
				return true;
			}
			IPath path = new Path(node.getContainingFilename());
			IFile file = ResourceLookup.selectFileForLocation(path, null); // NPE thrown below, like original behavior
			
			//XXX HSR Guido: Possible Performance Issue (File access)
			try {
				InputStream is = file.getContents();
				
				int length = OffsetHelper.getNodeOffset(com)-OffsetHelper.getNodeEndPoint(node);
				byte[] b = new byte[length];

				long count = is.skip(OffsetHelper.getEndOffsetWithoutComments(node));
				if(count < OffsetHelper.getEndOffsetWithoutComments(node)) {
					return false;
				}
				if(is.read(b, 0, length) == -1) {
					return false;
				}

				for(byte bb : b) {
					if(!Character.isWhitespace(bb)) {
						is.close();
						return false;
					}		
				}
				is.close();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
			
		return false;
	}
	
	private boolean canNotBeAddedToParent(ASTNode node, ASTNode com) {
		ASTNode parent = (ASTNode) node.getParent();
		
		if(hasNodeSameEndingAsSubnode(parent)) {
			return true;
		}else if(parent instanceof IASTTranslationUnit) {
			return true;
		}else if(parent instanceof ICPPASTTemplateDeclaration) {
			return true;
		}else if(parent instanceof CPPASTIfStatement) {
			return true;
		}else if(parent instanceof ICPPASTBaseSpecifier) {
			parent = (ASTNode) parent.getParent();
		}
		return !(OffsetHelper.getNodeOffset(com) >= OffsetHelper.getNodeEndPoint(parent));
	}

	private boolean mustBeAddToSubnodes(ASTNode node) {
		return hasNodeSameEndingAsSubnode(node);
	}
	
	private boolean hasNodeSameEndingAsSubnode(ASTNode node) {
		if(node instanceof CPPASTFunctionDefinition) {
			return true;
		}else if(node instanceof CPPASTDeclarationStatement) {
			return true;
		}else if(node instanceof CPPASTForStatement) {
			return true;
		}else if(node instanceof CPPASTLabelStatement) {
			return true;
		}else if(node instanceof CPPASTIfStatement) {
			return true;
		}else if(node instanceof CPPASTSwitchStatement) {
			return true;
		}else if(node instanceof CPPASTWhileStatement) {
			return true;
		}else if(node instanceof CPPASTTemplateDeclaration) {
			return true;
		}else if(node instanceof CPPASTLinkageSpecification) {
			return true;
		}else if(node instanceof CPPASTExplicitTemplateInstantiation) {
			return true;
		}
		return false;
	}
	
	protected int appendComments(ASTNode node) {
		while(commHandler.hasMore()) {
			IASTComment comment = commHandler.getFirst();
			
			if(isNotSameFile(node, comment)) {
				return ASTVisitor.PROCESS_SKIP;
			}
			
			if(!appendComment(node, comment)) {
				return ASTVisitor.PROCESS_CONTINUE;
			}
		}
		return ASTVisitor.PROCESS_ABORT;
	}
	
	protected int appendFreestandingComments(ASTNode node) {
		while(commHandler.hasMore()) {
			IASTComment comment = commHandler.getFirst();
			
			if(isNotSameFile(node, comment)) {
				return ASTVisitor.PROCESS_SKIP;
			}
			
			if(appendComment(node, comment)) {
				return ASTVisitor.PROCESS_CONTINUE;
			}
			
			if(!appendFreestandingComment(node, comment)) {
				return ASTVisitor.PROCESS_CONTINUE;
			}
		}
		return ASTVisitor.PROCESS_ABORT;
	}
	
	public void appendRemainingComments(IASTDeclaration declaration) {
		while(commHandler.hasMore()) {
			IASTComment comment = commHandler.getFirst();
			if(appendComment((ASTNode)declaration, comment)) {
				continue;
			}
			addFreestandingCommentToMap((ASTNode) declaration, comment);
		}
	}
	
	private boolean isNotSameFile(IASTNode node, IASTComment comment) {
		if(node.getFileLocation()==null) {
			return true;
		}
		return !node.getFileLocation().getFileName().equals(comment.getFileLocation().getFileName());
	}
}
