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
public interface IASTArraySubscriptExpression extends IASTExpression {

    public static final ASTNodeProperty ARRAY = new ASTNodeProperty( "Array"); //$NON-NLS-1$
    public IASTExpression getArrayExpression();
    public void setArrayExpression( IASTExpression expression );
    public static final ASTNodeProperty SUBSCRIPT = new ASTNodeProperty( "Subscript"); //$NON-NLS-1$
    public IASTExpression getSubscriptExpression();
    public void setSubscriptExpression( IASTExpression expression );
    
}
