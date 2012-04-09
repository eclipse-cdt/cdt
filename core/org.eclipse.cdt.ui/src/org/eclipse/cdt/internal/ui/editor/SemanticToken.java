/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Semantic token.
 * Cloned from JDT.
 * 
 * @since 4.0
 */
public final class SemanticToken {
	/** AST node */
	private IASTNode fNode;

	/** Binding */
	private IBinding fBinding;
	/** Is the binding resolved? */
	private boolean fIsBindingResolved= false;

	/** AST root */
	private IASTTranslationUnit fRoot;
	private boolean fIsRootResolved;

	/**
	 * @return Returns the binding, can be <code>null</code>.
	 */
	public IBinding getBinding() {
		if (!fIsBindingResolved) {
			fIsBindingResolved= true;
			if (fNode instanceof IASTName)
				fBinding= ((IASTName) fNode).resolveBinding();
		}
		
		return fBinding;
	}

	/**
	 * @return the AST node
	 */
	public IASTNode getNode() {
		return fNode;
	}
	
	/**
	 * @return the AST root
	 */
	public IASTTranslationUnit getRoot() {
		if (!fIsRootResolved) {
			fIsRootResolved= true;
			if (fNode != null) {
				fRoot= fNode.getTranslationUnit();
			}
		}
		return fRoot;
	}

	/**
	 * Update this token with the given AST node.
	 * <p>
	 * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
	 * </p>
	 *
	 * @param node the AST node
	 */
	void update(IASTNode node) {
		clear();
		fNode= node;
	}

	/**
	 * Clears this token.
	 * <p>
	 * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
	 * </p>
	 */
	void clear() {
		fNode= null;
		fBinding= null;
		fIsBindingResolved= false;
		fRoot= null;
		fIsRootResolved= false;
	}
}
