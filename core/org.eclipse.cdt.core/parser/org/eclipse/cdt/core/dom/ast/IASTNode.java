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
	 * This is the offset into the location of the beginning of the text
	 * that this node represents.
	 * 
	 * @return the offset of the node
	 */
	public int getOffset();
	
	public void setOffset( int offset );

	/**
	 * This is the length text that this node represents.
	 * 
	 * @return
	 */
	public int getLength();
	
	public void setLength( int length );

	/**
	 * Get the parent node of this node in the tree.
	 * 
	 * @return the parent node of this node
	 */
	public IASTNode getParent();
	
	public void setParent( IASTNode node );

	/**
	 * In order to properly understand the relationship between this child
	 * node and it's parent, a node property object is used.
	 * 
	 * @return
	 */
	public ASTNodeProperty getPropertyInParent();
	
	public void setPropertyInParent( ASTNodeProperty property );
	

}
