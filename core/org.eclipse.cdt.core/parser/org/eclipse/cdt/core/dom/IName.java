/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Common interface for names in the index and the AST
 * @since 4.0
 */
public interface IName {

	public static final IName[] EMPTY_NAME_ARRAY = new IName[0];

	/**
	 * Return a char array representation of the name.
	 * 
	 * @return ~ toString().toCharArray()
	 */
	public char[] toCharArray();
	
	/**
	 * Resolve the semantic object this name is referring to.
	 * 
	 * @return <code>IBinding</code> binding
	 */
	public IBinding resolveBinding();

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
