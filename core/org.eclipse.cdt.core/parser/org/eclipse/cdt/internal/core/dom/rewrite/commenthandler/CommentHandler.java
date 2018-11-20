/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;

/**
 * The CommentHandler is initialized with all the comment which should be processed.
 * During the process of comment assignment this comment collection is work through one
 * after another until no more comments are left.
 *
 * @author Guido Zgraggen IFS
 */
public class CommentHandler {
	private final List<IASTComment> comments;

	public CommentHandler(List<IASTComment> comments) {
		super();
		this.comments = comments;
	}

	public void allreadyAdded(IASTComment com) {
		comments.remove(com);
	}

	public boolean hasMore() {
		return !comments.isEmpty();
	}

	public IASTComment getFirst() {
		return comments.get(0);
	}
}
