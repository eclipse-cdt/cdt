/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
 * This interface represents a preprocessor #undef statement.
 * 
 * @author jcamelon
 */
public interface IASTPreprocessorUndefStatement extends
		IASTPreprocessorStatement {

    public static final ASTNodeProperty MACRO_NAME = new ASTNodeProperty( "IASTPreprocessorUndefStatement.MACRO_NAME - the name of the macro being undefined"); //$NON-NLS-1$
    public IASTName getMacroName();
    
}
