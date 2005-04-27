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
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public interface IASTAmbiguousStatement extends IASTStatement {

    public static final ASTNodeProperty STATEMENT = new ASTNodeProperty( "IASTAmbiguousStatement.STATEMENT - Ambiguous statement." ); //$NON-NLS-1$
    public void addStatement( IASTStatement s );
    public IASTStatement [] getStatements();
    
}
