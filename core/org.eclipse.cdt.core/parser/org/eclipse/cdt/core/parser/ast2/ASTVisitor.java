/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2;

/**
 * The general method for navigating the AST is to provide a visitor. This
 * allows implementations of the AST to hide the internal structure of the
 * tree.
 * 
 * @author Doug Schaefer
 */
public class ASTVisitor {

	/**
	 * This method is called back with the current node in the tree.
	 * The return value of this method determines whether children of this
	 * node should be visited as well.
	 * 
	 * @param node the current node
	 * @return whether to visit the children of this node
	 */
	public boolean visit(IASTNode node) {
		// By default, do a full traversal
		return true;
	}

	/**
	 * Called when the children, if any, have been fully visited.
	 * 
	 * @param node the current node
	 */
	public void endVisit(IASTNode node) {
	}
	
}
