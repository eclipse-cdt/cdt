/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model;

import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.ICodanCommentMap;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Implementation of ICodanCommentMap. 
 */
public class CodanCommentMap implements ICodanCommentMap {
	private NodeCommentMap commentedNodeMap;

	/**
	 * @param commentedNodeMap
	 */
	public CodanCommentMap(NodeCommentMap commentedNodeMap) {
		this.commentedNodeMap = commentedNodeMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.cxx.model.ICodanCommentMap#
	 * getTrailingCommentsForNode(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public List<IASTComment> getTrailingCommentsForNode(IASTNode node) {
		return commentedNodeMap.getTrailingCommentsForNode(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.cxx.model.ICodanCommentMap#
	 * getLeadingCommentsForNode(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public List<IASTComment> getLeadingCommentsForNode(IASTNode node) {
		return commentedNodeMap.getLeadingCommentsForNode(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.codan.core.cxx.model.ICodanCommentMap#getFreestandingForNode(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	public List<IASTComment> getFreestandingForNode(IASTNode node) {
		return commentedNodeMap.getFreestandingCommentsForNode(node);
	}
}
