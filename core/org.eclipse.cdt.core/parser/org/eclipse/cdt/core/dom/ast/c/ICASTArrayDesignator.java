/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public interface ICASTArrayDesignator extends ICASTDesignator {

    public static final ASTNodeProperty SUBSCRIPT_EXPRESSION = new ASTNodeProperty( "Subscript Expression" ); //$NON-NLS-1$
    
    public IASTExpression getSubscriptExpression();
    public void setSubscriptExpression( IASTExpression value );
    
}
