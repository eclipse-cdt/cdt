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
package org.eclipse.cdt.codan.core.cxx.model;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Comment map allows to get comments before of after the specific ast node
 */
public interface ICodanCommentMap {
	/**
	 * Returns an Collection of comments for the given node (after the node).
	 * This list contains all the comments
	 * which are assigned to this specific node. If no comments are available an
	 * empty
	 * collection is returned.
	 * 
	 * @param node The key to fetch the associated comments.
	 * @return list of comments
	 */
	public List<IASTComment> getTrailingCommentsForNode(IASTNode node);

	/**
	 * Returns an Collection of comments for the given node (before the node).
	 * This list contains all the comments
	 * which are assigned to this specific node. If no comments are available an
	 * empty
	 * collection is returned.
	 * 
	 * @param node The key to fetch the associated comments.
	 * @return list of comments
	 */
	public List<IASTComment> getLeadingCommentsForNode(IASTNode node);

	
	/**
	 * Returns an ArrayList for the given node. This ArrayList contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * ArrayList is returned.
	 * @param node The key to fetch the associated comments.
	 * @return ArrayList
	 */
	public List<IASTComment> getFreestandingForNode(IASTNode node);

	/**
	 * @param node
	 * @return
	 */
	public IASTComment getLastLeadingCommentForNode(IASTNode node);

	/**
	 * @param node
	 * @return
	 */
	public IASTComment getFirstTrailingCommentForNode(IASTNode node);

	/**
	 * @param node
	 * @return
	 */
	public IASTComment getLastFreestandingCommentForNode(IASTNode node);
}
