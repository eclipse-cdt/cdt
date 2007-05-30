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

/**
 * Common interface for names in the index and the AST.
 * <p> This interface is not intended to be implemented by clients. </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 * @since 4.0
 */
public interface IName {
	/**
	 * Return a char array representation of the name.
	 * 
	 * @return ~ toString().toCharArray()
	 */
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
