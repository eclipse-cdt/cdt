/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLabelStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.rewrite.util.OffsetHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Guido Zgraggen IFS
 * 
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
			else {
				IPath path = new Path(node.getContainingFilename());
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
				
				//XXX HSR Guido: Possible Performance Issue (File access)
				try {
					InputStream is = file.getContents();
					
					int length = OffsetHelper.getNodeOffset(com)-OffsetHelper.getNodeEndPoint(node);
					byte[] b = new byte[length];

					is.skip(OffsetHelper.getNodeEndPoint(node));
					is.read(b, 0, length);

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
		}
			
		return false;
	}
	
	private boolean canNotBeAddedToParent(ASTNode node, ASTNode com) {
		ASTNode parent = (ASTNode) node.getParent();
		
		if(hasNodeSameEndingAsSubnode(parent)) {
			return true;
		}else if(parent instanceof IASTTranslationUnit) {
			return true;
		}else if(parent instanceof ICPPASTBaseSpecifier) {
			parent = (ASTNode) parent.getParent();
		}else if(parent instanceof ICPPASTTemplateDeclaration) {
			return true;
		}else if(parent instanceof CPPASTIfStatement) {
			return true;
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
