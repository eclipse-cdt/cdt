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

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;

/**
 * @author jcamelon
 */
public interface ICPPASTCastExpression extends IASTCastExpression {

    public static final int op_dynamic_cast = IASTCastExpression.op_last + 1;
    public static final int op_static_cast = IASTCastExpression.op_last + 2;
    public static final int op_reinterpret_cast = IASTCastExpression.op_last + 3;
    public static final int op_const_cast = IASTCastExpression.op_last + 4;
    public static final int op_last = op_const_cast;
}
