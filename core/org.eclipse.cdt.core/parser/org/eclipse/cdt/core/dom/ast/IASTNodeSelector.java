/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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
 * Interface for searching nodes in a translation unit. An instance of this interface, responsible
 * for one file contained in a translation-unit, can be obtained using 
 * {@link IASTTranslationUnit#getNodeSelector(String)}.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.0
 */
public interface IASTNodeSelector {

	/**
	 * Returns the name for the exact given range, or <code>null</code> if there is no such node.
     * Will not return an implicit name.
	 */
	IASTName findName(int offset, int length);

	/**
	 * Returns the smallest name enclosing the given range, or <code>null</code> if there is no such node.
     * Will not return an implicit name.
	 */
	IASTName findEnclosingName(int offset, int length);

	/**
	 * Returns the first name contained in the given range, or <code>null</code> if there is no such node.
     * Will not return an implicit name.
	 */
	IASTName findFirstContainedName(int offset, int length);

	/**
	 * Returns the implicit name for the exact given range, or <code>null</code> if there is no such node.
	 * 
	 * Note that there can be more than one implicit name in the same location.
	 * The implicit name's parent can be used to get all the names at the location.
	 * 
	 * @since 5.1
	 */
	IASTImplicitName findImplicitName(int offset, int length);
	
	/**
	 * Returns the first implicit name enclosing the given range.
	 * 
	 * Note that there can be more than one implicit name in the same location.
	 * The implicit name's parent can be used to get all the names at the location.
	 * 
	 * @since 5.1
	 */
	IASTImplicitName findEnclosingImplicitName(int offset, int length);
	
	/**
	 * Returns the node for the exact given range, or <code>null</code> if there is no such node.
	 * <p>
	 * For nodes with the same location, macro-expansions ({@link IASTPreprocessorMacroExpansion}) are preferred
	 * over c/c++-nodes and children are preferred over their parents.
	 */
	IASTNode findNode(int offset, int length);

	/**
	 * Returns the smallest node enclosing the given range, or <code>null</code> if there is no such node.
	 * <p>
	 * For nodes with the same location, macro-expansions ({@link IASTPreprocessorMacroExpansion}) are preferred
	 * over c/c++-nodes nodes and children are preferred over their parents.
	 */
	IASTNode findEnclosingNode(int offset, int length);
	
	/**
	 * Returns the smallest node strictly enclosing the given range, or <code>null</code> if there is no such node.
	 * <p>
	 * For nodes with the same location, macro-expansions ({@link IASTPreprocessorMacroExpansion}) are preferred
	 * over c/c++-nodes nodes and children are preferred over their parents.
	 * @since 5.3
	 */
	IASTNode findStrictlyEnclosingNode(int offset, int length);

	/**
	 * Returns the first node contained in the given range, or <code>null</code> if there is no such node.
	 * <p>
	 * For nodes with the same location, macro-expansions ({@link IASTPreprocessorMacroExpansion}) are preferred
	 * over c/c++-nodes nodes and children are preferred over their parents.
	 */
	IASTNode findFirstContainedNode(int offset, int length);
	
	/**
	 * Returns the node for the exact given range, or <code>null</code> if there is no such node.
	 * <p>
	 * The method never returns a macro expansion ({@link IASTPreprocessorMacroExpansion}) or the name for
	 * an expansion. Rather than that the expansion itself is searched for a matching node.
	 * @since 5.1
	 */
	IASTNode findNodeInExpansion(int offset, int length);

	/**
	 * Returns the smallest node enclosing the range, or <code>null</code> if there is no such node.
	 * <p>
	 * The method never returns a macro expansion ({@link IASTPreprocessorMacroExpansion}) or the name for
	 * an expansion. Rather than that the expansion itself is searched for a matching node.
	 * @since 5.1
	 */
	IASTNode findEnclosingNodeInExpansion(int offset, int length);

	/**
	 * Returns the first node contained in the given expansion, or <code>null</code> if there is no such node.
	 * <p>
	 * The method never returns a macro expansion ({@link IASTPreprocessorMacroExpansion}) or the name for
	 * an expansion. Rather than that the expansion itself is searched for a matching node.
	 * @since 5.1
	 */
	IASTNode findFirstContainedNodeInExpansion(int offset, int length);

	/**
	 * Returns a macro expansion enclosing the given range, or <code>null</code>.
	 */
	IASTPreprocessorMacroExpansion findEnclosingMacroExpansion(int offset, int length);
}
