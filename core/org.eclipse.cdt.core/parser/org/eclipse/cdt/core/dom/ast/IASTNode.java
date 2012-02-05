/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.parser.IToken;

/**
 * This is the root node in the physical AST. A physical node represents a chunk
 * of text in the source program.
 *
 * Classes implementing this interface are not thread safe.
 * Even 'get' methods may cause changes to the underlying object.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IASTNode {
	/**
	 * @since 5.3
	 */
	public enum CopyStyle {
		/**
		 * Copy without location, this copy is independent of the index and can be shared.
		 */
		withoutLocations,
		/**
		 * The copied node has a {@link IASTCopyLocation} linking the copy to the original node.
		 * If the index was supplied creating the original AST, the caller has to hold a read lock
		 * on it. The returned copy is valid only while the read lock is being held and should
		 * not be accessed after releasing the lock.
		 */
		withLocations
	}
	
	public static final IASTNode[] EMPTY_NODE_ARRAY = {};
	
	/**
	 * Get the translation unit (master) node that is the ancestor of all nodes
	 * in this AST.
	 * 
	 * @return <code>IASTTranslationUnit</code>
	 */
	public IASTTranslationUnit getTranslationUnit();

	/**
	 * Get the location of this node. In cases not involving macro expansions,
	 * the IASTNodeLocation[] result will only have one element in it, and it
	 * will be an IASTFileLocation.
	 * 
	 * Where the node is completely generated within a macro expansion,
	 * IASTNodeLocation[] result will have one element in it, and it will be an
	 * {@link IASTMacroExpansionLocation}.
	 * 
	 * Nodes that span file context into a macro expansion (and potentially out
	 * of the macro expansion again) result in an IASTNodeLocation[] result
	 * that is of length > 1.
	 * 
	 * We do not provide meaningful node locations for nested macro references
	 * (see {@link IASTPreprocessorMacroExpansion#getNestedMacroReferences()}).
	 * For those, the file location of the enclosing explicit macro reference is 
	 * returned. You can however compute their image-location using
	 * {@link IASTName#getImageLocation()}
	 */
	public IASTNodeLocation[] getNodeLocations();
	
    /**
     * Computes a file location for the node. When the node actually resides in a macro-expansion
     * the location of the expansion is returned. In case the node spans multiple files the location
     * will be in a common root file and will contain the appropriate include directives.
     * <p>
     * The method may return <code>null</code> in case the node does not have a file-location. This
     * is for instance the case for built-in macro names or empty names for anonymous type
     * declarations.
     * 
     * @return the mapped file location or <code>null</code>.
     */
    public IASTFileLocation getFileLocation();
    
	/**
	 * Lightweight check for understanding what file we are in.  
	 * 
	 * @return <code>String</code> absolute path
	 */
	public String getContainingFilename();
	
	/**
	 * Lightweight check to see whether this node is part of the root file. 
	 * @since 5.0 
	 */
	public boolean isPartOfTranslationUnitFile();
	
	/**
	 * Get the parent node of this node in the tree.
	 * 
	 * @return the parent node of this node
	 */
	public IASTNode getParent();
	
	/**
	 * Returns the children of this node.
	 * @since 5.1
	 */
	IASTNode[] getChildren();

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
	 * Abstract method to be overridden by all subclasses. Necessary for
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

    /**
     * Returns whether this node contains the given one. The decision is made
     * purely on location information and therefore the method is fast.
     * @param node the node to check
     * @return whether this node contains the given one.
     * @since 4.0
     */
	public boolean contains(IASTNode node);
	
	/**
	 * Returns the tokens that can be found between this node and its left sibling (or the
	 * beginning of the parent, if there is no left sibling). The tokens are obtained 
	 * from the lexer, no preprocessing is performed.
	 * The offsets of the tokens are relative to the file-offset of this node.
	 * <p> <b>Examples</b> looking at the condition of if-statements:
	 * <pre>
	 * #define IF      if
     * #define IF_P    if (
     * #define IF_P_T  if (true
     * #define SEMI_IF ; if 
     * #define IF_COND if (true)
     * void test() {
     *    if (true) {}       // leading syntax: 'if ('
     *    IF (true) {}       // leading syntax: 'IF ('
     *    IF_P true) {}      // leading syntax: 'IF_P'
     *    IF_P_T ) {}        // throws ExpansionOverlapsBoundaryException
     *    SEMI_IF (true) {}  // throws ExpansionOverlapsBoundaryException
     *    IF_COND            // throws ExpansionOverlapsBoundaryException
     * </pre>
	 * @return a chain of tokens or <code>null</code>, if there are none.
	 * @throws ExpansionOverlapsBoundaryException if one of the boundaries of the leading syntax is
	 * overlapped by a macro-expansion. 
	 * @throws UnsupportedOperationException if invoked on preprocessor nodes, or nodes that are not
	 * part of a translation unit.
	 * @since 5.1
	 */
	public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException,
			UnsupportedOperationException;

	/**
	 * Returns the tokens that can be found between this node and its right sibling (or the
	 * end of the parent, if there is no right sibling). The tokens are obtained from the lexer,
	 * no preprocessing is performed.
	 * The offsets of the tokens are relative to the file-offset of the end of this node.
	 * <p> For examples see {@link #getLeadingSyntax()}.
	 * @return a chain of tokens or <code>null</code>, if there are none.
	 * @throws ExpansionOverlapsBoundaryException if one of the boundaries of the trailing syntax is
	 * overlapped by a macro-expansion.
	 * @throws UnsupportedOperationException if invoked on preprocessor nodes, or nodes that are not
	 * part of a translation unit.
	 * @since 5.1
	 */
	public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException, UnsupportedOperationException;
	
	/**
	 * Returns the tokens that make up this node. The tokens are obtained from the lexer,
	 * no preprocessing is performed.
	 * The offsets of the tokens are relative to the file-offset of the beginning of this node.
	 * <p> For examples see {@link #getLeadingSyntax()}.
	 * @return a chain of tokens or <code>null</code>, if there are none.
	 * @throws ExpansionOverlapsBoundaryException if one of the boundaries of the node is
	 * overlapped by a macro-expansion.
	 * @throws UnsupportedOperationException if invoked on preprocessor nodes, or nodes that are not
	 * part of a translation unit.
	 * @since 5.1
	 */
	public IToken getSyntax() throws ExpansionOverlapsBoundaryException;
	
	/**
	 * Returns true if this node is frozen, false otherwise.
	 * If the node is frozen then any attempt to call a method that changes
	 * the node's state will result in an IllegalStateException.
	 * @since 5.1
	 */
	public boolean isFrozen();
	
	/**
	 * Returns false if this node was parsed in an inactive code branch.
	 * @since 5.1
	 */
	public boolean isActive();
	
	/**
	 * Returns a mutable copy of the tree rooted at this node. The following postconditions hold:
	 * 
	 * <code>
	 * copy.getParent() == null
	 * copy.getPropertyInParent() == null
	 * copy.isFrozen() == false
	 * </code>
	 * 
	 * Preprocessor nodes do not currently support being copied.
	 * 
	 * Implicit name nodes are not copied, instead they can be regenerated if required.
	 * 
	 * Calling this method is equivalent
	 * 
	 * @since 5.1
	 * @throws UnsupportedOperationException
	 *             if this node or one of its descendants does not support copying
	 */
	public IASTNode copy();

	/**
	 * Returns a mutable copy of the tree rooted at this node. The following postconditions hold:
	 * 
	 * <code>
	 * copy.getParent() == null
	 * copy.getPropertyInParent() == null
	 * copy.isFrozen() == false
	 * </code>
	 * 
	 * Preprocessor nodes do not currently support being copied.
	 * 
	 * Implicit name nodes are not copied, instead they can be regenerated if required.
	 * 
	 * @param style
	 *            {@link CopyStyle} create a copy with or without locations. Please see
	 *            {@link CopyStyle} for restrictions on copies with Locations.
	 * @since 5.3
	 * @throws UnsupportedOperationException
	 *             if this node or one of its descendants does not support copying
	 */
	public IASTNode copy(CopyStyle style);
}
