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
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the root node in the physical AST. A physical node represents
 * a chunk of text in the source program.
 * 
 * @author Doug Schaefer
 */
public interface IASTNode {

	/**
	 * Each physical node comes from a source string somewhere. This could
	 * be a file, a macro expansion, or simply an in memory buffer, or
	 * whatever.
	 * 
	 * TODO: there will be cases where nodes may span locations, e.g. a macro
	 * expansion in the middle of a declaration. This would probably be best
	 * represented by a composite location that map offset to a "sublocation"
	 * or something...
	 * 
	 * @return the location of the node
	 */
	public IASTNodeLocation getLocation();

	/**
	 * This is the offset into the location of the beginning of the text
	 * that this node represents.
	 * 
	 * @return the offset of the node
	 */
	public int getOffset();

	/**
	 * This is the length text that this node represents.
	 * 
	 * @return
	 */
	public int getLength();

	/**
	 * Get the parent node of this node in the tree.
	 * 
	 * @return the parent node of this node
	 */
	public IASTNode getParent();

	/**
	 * In order to properly understand the relationship between this child
	 * node and it's parent, a node property object is used.
	 * 
	 * @return
	 */
	public IASTNodeProperty getPropertyInParent();
	

}
