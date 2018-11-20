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
package org.eclipse.cdt.codan.core.cxx.model;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Comment map allows to get comments before of after the specific ast node
 */
public interface ICodanCommentMap {
	/**
	 * Returns a List of comments for the given node (following the node).
	 * If no comments are available an empty list is returned.
	 *
	 * @param node The key to fetch the associated comments.
	 * @return list of comments
	 */
	public List<IASTComment> getTrailingCommentsForNode(IASTNode node);

	/**
	 * Returns a List of all comments for the given node (preceding the node).
	 * If no comments are available an empty list is returned.
	 *
	 * @param node The key to fetch the associated comments.
	 * @return list of comments
	 */
	public List<IASTComment> getLeadingCommentsForNode(IASTNode node);

	/**
	 * Returns a List of comments associated with the given node.
	 * If no comments are available an empty list is returned.
	 *
	 *
	 * @param node The key to fetch the associated comments.
	 * @return list of comments
	 */
	public List<IASTComment> getFreestandingForNode(IASTNode node);

	/**
	 * Gets last comment from {@link #getLeadingCommentsForNode(IASTNode)}, or
	 * null if list is empty
	 *
	 * @param node - The key to fetch the associated comments.
	 * @return - A comment node or null if not found.
	 */
	public IASTComment getLastLeadingCommentForNode(IASTNode node);

	/**
	 * Gets first comment from {@link #getTrailingCommentsForNode(IASTNode)} or
	 * null if list is empty.
	 *
	 * @param node - The key to fetch the associated comments.
	 * @return - A comment node or null if not found.
	 */
	public IASTComment getFirstTrailingCommentForNode(IASTNode node);

	/**
	 * Gets last comment from {@link #getFreestandingForNode(IASTNode)} or
	 * null if list is empty.
	 *
	 * @param node - The key to fetch the associated comments.
	 * @return - A comment node or null if not found.
	 */
	public IASTComment getLastFreestandingCommentForNode(IASTNode node);
}
