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
 * This represents the definition of a macro.
 * 
 * @author Doug Schaefer
 */
public interface IASTPreprocessorMacroDefinition extends IASTPreprocessorStatement {

    public static final ASTNodeProperty MACRO_NAME = new ASTNodeProperty( "Macro Name"); //$NON-NLS-1$
    public IASTName getName();
    public void setName( IASTName name );
    
    public String getExpansion();
    public void setExpansion( String exp );
}
