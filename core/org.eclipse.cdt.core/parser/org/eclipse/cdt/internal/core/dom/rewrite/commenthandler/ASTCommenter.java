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
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.util.Vector;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @author Guido Zgraggen IFS
 *
 */
public class ASTCommenter {
	
	public static NodeCommentMap getCommentedNodeMap(IASTTranslationUnit transUnit){
		if(transUnit== null) {
			return new NodeCommentMap();
		}
		Vector<IASTComment> comments = getCommentsInWorkspace(transUnit);
		if(comments == null || comments.size() == 0) {
			return new NodeCommentMap();
		}
		return addCommentsToCommentMap(transUnit, comments);
	}

	private static Vector<IASTComment> getCommentsInWorkspace(IASTTranslationUnit tu) {
		IASTComment[] comments = tu.getComments();
		Vector<IASTComment> commentsInWorksapce = new Vector<IASTComment>();

		if (comments == null || comments.length == 0) {
			return null;
		}

		for (IASTComment comment : comments) {
			if (isInWorkspace(comment)) {
				commentsInWorksapce.add(comment);
			}
		}
		return commentsInWorksapce;
	}

	private static boolean isInWorkspace(IASTNode node) {
		IPath workspacePath = Platform.getLocation();
		IPath nodePath = new Path(node.getContainingFilename());
		return workspacePath.isPrefixOf(nodePath);
	}

	
	private static NodeCommentMap addCommentsToCommentMap(IASTTranslationUnit rootNode,	Vector<IASTComment> comments){
		NodeCommentMap commentMap = new NodeCommentMap();
		CommentHandler commHandler = new CommentHandler(comments);

		IASTDeclaration[] declarations = rootNode.getDeclarations();
		for (int i = 0; i < declarations.length; i++) {

			if (isInWorkspace(declarations[i])) {

				ASTCommenterVisitor commenter = new ASTCommenterVisitor(commHandler, commentMap);
				declarations[i].accept(commenter);
				
				//add remaining comments to the last declaration => Comments won't get lost
				if (i + 1 == declarations.length) {
					commenter.addRemainingComments(declarations[i]);
				}
			}
		}
		return commentMap;
	}	
}
