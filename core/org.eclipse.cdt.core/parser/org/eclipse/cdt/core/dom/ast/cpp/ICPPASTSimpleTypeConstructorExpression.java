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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public interface ICPPASTSimpleTypeConstructorExpression extends IASTExpression {

	public static final int t_unspecified = 0;
	public static final int t_void = 1;
	public static final int t_char = 2;
	public static final int t_int = 3;
	public static final int t_float = 4;
	public static final int t_double = 5;
	public static final int t_bool = 6;
	public static final int t_wchar_t = 7;
	public static final int t_short = 8;
	public static final int t_long = 9;
	public static final int t_signed = 10;
	public static final int t_unsigned = 11;
	public static final int t_last = t_unsigned;

	public int getSimpleType();
	public void setSimpleType( int value );
	
	public static final ASTNodeProperty INITIALIZER_VALUE = new ASTNodeProperty( "Initializer Value"); //$NON-NLS-1$
    
	public IASTExpression getInitialValue();
	public void setInitialValue( IASTExpression expression );
	
}
