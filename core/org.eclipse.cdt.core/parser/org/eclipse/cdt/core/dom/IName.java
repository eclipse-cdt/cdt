/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Common interface for names in the index and the AST.
 * @since 4.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IName {
	/**
	 * @since 5.2
	 */
	public static final IName[] EMPTY_ARRAY= {};

	/**
	 * Returns the name without qualification and without template arguments.
	 * @since 5.1
	 */
	public char[] getSimpleID();

	/**
	 * @deprecated Using this method is problematic, because for names from the
	 * index never contain qualification or template arguments, which is different
	 * for names from the AST.
	 * Use {@link #getSimpleID()}, instead.
	 */
	@Deprecated
	public char[] toCharArray();
	
	/**
	 * Is this name being used in the AST as the introduction of a declaration?
	 * @return boolean
	 */
	public boolean isDeclaration();
	
	/**
	 * Is this name being used in the AST as a reference rather than a declaration?
	 * @return boolean
	 */
    
	public boolean isReference();
    
    /**
     * Is this name being used in the AST as a reference rather than a declaration?
     * @return boolean
     */
    public boolean isDefinition();

    /**
     * Same as {@link IASTNode#getFileLocation()}
     * @return the file location of this name.
     */
	public IASTFileLocation getFileLocation();
}
