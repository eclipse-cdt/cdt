/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author jcamelon
 */
public interface IASTTypeIdExpression extends IASTExpression {

    public static final int op_sizeof = 0;
    public static final int op_last   = op_sizeof;
    
    public int getOperator();
    public void setOperator( int value );
        
    public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty( "Type Id"); //$NON-NLS-1$
    public void setTypeId( IASTTypeId typeId );
    public IASTTypeId getTypeId();

}
