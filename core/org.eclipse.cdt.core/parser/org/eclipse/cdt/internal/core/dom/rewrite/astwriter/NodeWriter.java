/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
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

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Base class for node writers. This class contains methods and string constants
 * used by multiple node writers.
 * 
 * @author Emanuel Graf IFS
 */
public class NodeWriter {
	protected Scribe scribe;
	protected ASTWriterVisitor visitor;
	protected NodeCommentMap commentMap;
	protected static final String COMMA_SPACE = ", "; //$NON-NLS-1$
	protected static final String EQUALS = " = "; //$NON-NLS-1$
	protected static final String SPACE_COLON_SPACE = " : "; //$NON-NLS-1$
	protected static final String VAR_ARGS = "..."; //$NON-NLS-1$
	protected static final String COLON_COLON = "::"; //$NON-NLS-1$
	protected static final String COLON_SPACE = ": "; //$NON-NLS-1$

	public NodeWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super();
		this.scribe = scribe;
		this.visitor = visitor;
		this.commentMap = commentMap;
	}

	protected void writeNodeList(IASTNode[] nodes) {
		for (int i = 0; i < nodes.length; ++i) {
			nodes[i].accept(visitor);
			if (i + 1 < nodes.length) {
				scribe.print(COMMA_SPACE);
			}
		}
	}
	
	protected void visitNodeIfNotNull(IASTNode node) {
		if (node != null) {
			node.accept(visitor);
		}
	}

	protected void writeTrailingComments(IASTNode node) {
		// Default is to write a new line after the trailing comments.
		writeTrailingComments(node, true);
	}

	protected void writeTrailingComments(IASTNode node, boolean newLine) {
		boolean first = true;
		for (IASTComment comment : getTrailingComments(node)) {
			if (!first) {
				scribe.newLine();
			}
			scribe.printSpace();
			scribe.print(comment.getComment());
			first = false;
		}
		if (newLine) {
			scribe.newLine();
		}
	}

	protected boolean hasTrailingComments(IASTNode node) {
		return !getTrailingComments(node).isEmpty();
	}

	private List<IASTComment> getTrailingComments(IASTNode node) {
		List<IASTComment> trailingComments = commentMap.getTrailingCommentsForNode(node);
		IASTNode originalNode = node.getOriginalNode();
		if (originalNode != node)
			trailingComments.addAll(commentMap.getTrailingCommentsForNode(originalNode));
		return trailingComments;
	}

	protected boolean hasFreestandingComments(IASTNode node) {
		return !getFreestandingComments(node).isEmpty();
	}

	private List<IASTComment> getFreestandingComments(IASTNode node) {
		List<IASTComment> freestandingComments = commentMap.getFreestandingCommentsForNode(node);
		IASTNode originalNode = node.getOriginalNode();
		if (originalNode != node)
			freestandingComments.addAll(commentMap.getFreestandingCommentsForNode(originalNode));
		return freestandingComments;
	}

	protected void writeFreestandingComments(IASTNode node) {
		for (IASTComment comment : getFreestandingComments(node)) {
			scribe.print(comment.getComment());
			scribe.newLine();
		}
	}
}
