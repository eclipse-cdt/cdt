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
public interface IASTFunctionCallExpression extends IASTExpression  {
    
    public static final ASTNodeProperty FUNCTION_NAME = new ASTNodeProperty( "Function Name"); //$NON-NLS-1$
    public void setFunctionNameExpression( IASTExpression expression );
    public IASTExpression getFunctionNameExpression();
    
    public static final ASTNodeProperty PARAMETERS = new ASTNodeProperty( "Parameters"); //$NON-NLS-1$
    public void setParameterExpression( IASTExpression expression );
    public IASTExpression getParameterExpression();
    
}
