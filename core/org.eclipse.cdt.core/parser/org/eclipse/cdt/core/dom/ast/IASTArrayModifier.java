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
public interface IASTArrayModifier extends IASTNode {
    
    public static final ASTNodeProperty CONSTANT_EXPRESSION = new ASTNodeProperty( "Constant Expression");  //$NON-NLS-1$
    public static final IASTArrayModifier[] EMPTY_ARRAY = new IASTArrayModifier[0];
    public IASTExpression getConstantExpression();
    public void setConstantExpression( IASTExpression expression );

}
