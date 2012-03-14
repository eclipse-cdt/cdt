/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.util;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Collection of helper methods for common operations on AST nodes.
 */
public class ASTNodes {

	// Not instantiatable.
	private ASTNodes() {
	}

	/**
	 * Returns the offset of an AST node.
	 */
	public static int offset(IASTNode node) {
		return node.getFileLocation().getNodeOffset();
	}

	/**
	 * Returns the exclusive end offset of an AST node.
	 */
	public static int endOffset(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		return location.getNodeOffset() + location.getNodeLength();
	}
}
