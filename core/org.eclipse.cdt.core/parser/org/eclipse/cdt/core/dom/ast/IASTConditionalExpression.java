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
public interface IASTConditionalExpression extends IASTExpression {

    public static final ASTNodeProperty LOGICAL_CONDITION = new ASTNodeProperty( "Logical Condition"); //$NON-NLS-1$
    public static final ASTNodeProperty POSITIVE_RESULT   = new ASTNodeProperty( "Positive Result" ); //$NON-NLS-1$
    public static final ASTNodeProperty NEGATIVE_RESULT   = new ASTNodeProperty( "Negative Result" ); //$NON-NLS-1$
    
    public IASTExpression getLogicalConditionExpression();
    public void setLogicalConditionExpression( IASTExpression expression );
    
    public IASTExpression getPositiveResultExpression();
    public void setPositiveResultExpression(IASTExpression expression);
    
    public IASTExpression getNegativeResultExpression();
    public void setNegativeResultExpression(IASTExpression expression);
    
}
