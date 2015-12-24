/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Models macro expansion found in the source code that is not nested inside another expansion. 
 * @since 5.0
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorMacroExpansion extends IASTNode {
	public static final IASTPreprocessorMacroExpansion[] EMPTY_ARRAY = {};
    public static final ASTNodeProperty EXPANSION_NAME= 
    		new ASTNodeProperty("IASTPreprocessorMacroExpansion.EXPANSION_NAME - macro name"); //$NON-NLS-1$
    public static final ASTNodeProperty NESTED_EXPANSION_NAME= 
    		new ASTNodeProperty("IASTPreprocessorMacroExpansion.NESTED_EXPANSION_NAME - nested macro name"); //$NON-NLS-1$

	/**
	 * Returns the macro definition used for the expansion.
	 */
	public IASTPreprocessorMacroDefinition getMacroDefinition();

	/**
	 * Returns the reference to the macro that causes this expansion.
	 */
	public IASTName getMacroReference();
	
	/**
	 * Returns an array of nested macro expansions.
	 */
	public IASTName[] getNestedMacroReferences();
}
