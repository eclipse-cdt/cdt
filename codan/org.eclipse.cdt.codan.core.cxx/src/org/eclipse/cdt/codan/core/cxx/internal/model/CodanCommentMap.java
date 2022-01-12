/*******************************************************************************
 * Copyright (c) 2009,2016 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public List<IASTComment> getTrailingCommentsForNode(IASTNode node) {
		return commentedNodeMap.getTrailingCommentsForNode(node);
	}

	@Override
	public List<IASTComment> getLeadingCommentsForNode(IASTNode node) {
		return commentedNodeMap.getLeadingCommentsForNode(node);
	}

	@Override
	public List<IASTComment> getFreestandingForNode(IASTNode node) {
		return commentedNodeMap.getFreestandingCommentsForNode(node);
	}

	/**
	 * @param node
	 * @return
	 */
	@Override
	public IASTComment getLastLeadingCommentForNode(IASTNode node) {
		IASTComment comment = null;
		List<IASTComment> comms = getLeadingCommentsForNode(node);
		if (comms.size() > 0) {
			comment = comms.get(comms.size() - 1);
		}
		return comment;
	}

	/**
	 * @param node
	 * @return
	 */
	@Override
	public IASTComment getFirstTrailingCommentForNode(IASTNode node) {
		IASTComment comment = null;
		List<IASTComment> comms = getTrailingCommentsForNode(node);
		if (comms.size() > 0) {
			comment = comms.get(0);
		}
		return comment;
	}

	/**
	 * @param node
	 * @return
	 */
	@Override
	public IASTComment getLastFreestandingCommentForNode(IASTNode node) {
		IASTComment comment = null;
		List<IASTComment> comms = getFreestandingForNode(node);
		if (comms.size() > 0) {
			comment = comms.get(comms.size() - 1);
		}
		return comment;
	}
}
