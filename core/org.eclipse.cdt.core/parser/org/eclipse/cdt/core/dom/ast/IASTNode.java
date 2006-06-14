/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the root node in the physical AST. A physical node represents a chunk
 * of text in the source program.
 * 
 * @author Doug Schaefer
 */
public interface IASTNode {
	
	public static final IASTNode [] EMPTY_NODE_ARRAY = new IASTNode[0];
	
	/**
	 * Get the translation unit (master) node that is the ancestor of all nodes
	 * in this AST.
	 * 
	 * @return <code>IASTTranslationUnit</code>
	 */
	public IASTTranslationUnit getTranslationUnit();

	/**
	 * Get the location of this node. In cases not involving macro expansions,
	 * the IASTNodeLocation [] result will only have one element in it, and it
	 * will be an IASTFileLocation or subinterface.
	 * 
	 * Where the node is completely generated within a macro expansion,
	 * IASTNodeLocation [] result will have one element in it, and it will be an
	 * IASTMacroExpansion.
	 * 
	 * Nodes that span file context into a macro expansion (and potentially out
	 * of the macro expansion again) result in an IASTNodeLocation [] result
	 * that is of length > 1.
	 * 
	 * @return <code>IASTNodeLocation []</code>
	 */
	public IASTNodeLocation[] getNodeLocations();
	
    /**
     * Get the location of the node as a file.
     * 
     * @return <code>IASTFileLocation</code>
     */
    public IASTFileLocation getFileLocation();
    
	/**
	 * Lightweight check for understanding what file we are in.  
	 * 
	 * @return <code>String</code> absolute path
	 */
	public String getContainingFilename();
	
	/**
	 * Get the parent node of this node in the tree.
	 * 
	 * @return the parent node of this node
	 */
	public IASTNode getParent();

	/**
	 * Set the parent node of this node in the tree.
	 * 
	 * @param node
	 *            <code>IASTNode</code>
	 */
	public void setParent(IASTNode node);

	/**
	 * In order to properly understand the relationship between this child node
	 * and it's parent, a node property object is used.
	 * 
	 * @return <code>ASTNodeProperty</code>
	 */
	public ASTNodeProperty getPropertyInParent();

	/**
	 * Set the parent property of the node.
	 * 
	 * @param property
	 */
	public void setPropertyInParent(ASTNodeProperty property);

	/**
	 * Abstract method to be overriden by all subclasses. Necessary for
	 * visitation of the tree using an <code>ASTVisitor</code>.
	 * 
	 * @param visitor
	 * @return continue on (true) or quit( false )
	 */
	public boolean accept(ASTVisitor visitor);
    
    /**
     * Returns the raw signature of the IASTNode before it is processed by the preprocessor.
     * 
     * Example:
     * #define ONE 1
     * int x=ONE; // getRawSignature() for this declaration would return "int x=ONE;"
     * @return the raw signature of the IASTNode before it is processed by the preprocessor
     */
    public String getRawSignature();

}
