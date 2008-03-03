/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for searching nodes of a file in a translation unit. An instance of this interface can
 * be obtained via {@link IASTTranslationUnit#getNodeSelector(String)}.
 * @since 5.0
 */
public interface IASTNodeSelector {

	/**
	 * Returns the name for the exact given range, or <code>null</code> if there is no such node.
	 */
	IASTName findName(int offset, int length);

	/**
	 * Returns the smallest name surrounding the given range, or <code>null</code> if there is no such node.
	 */
	IASTName findSurroundingName(int offset, int length);

	/**
	 * Returns the first name contained in the given range, or <code>null</code> if there is no such node.
	 */
	IASTName findFirstContainedName(int offset, int length);

	/**
	 * Returns the node for the exact given range, or <code>null</code> if there is no such node.
	 */
	IASTNode findNode(int offset, int length);

	/**
	 * Returns the smallest node surrounding the given range, or <code>null</code> if there is no such node.
	 */
	IASTNode findSurroundingNode(int offset, int length);

	/**
	 * Returns the first node contained in the given range, or <code>null</code> if there is no such node.
	 */
	IASTNode findFirstContainedNode(int offset, int length);
}
