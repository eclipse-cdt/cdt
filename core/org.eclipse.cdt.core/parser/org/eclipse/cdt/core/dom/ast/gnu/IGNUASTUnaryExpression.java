/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * @author jcamelon
 */
public interface IGNUASTUnaryExpression extends IASTUnaryExpression {

    public static final int op_typeof = IASTUnaryExpression.op_last + 1;
    public static final int op_alignOf = IASTUnaryExpression.op_last + 2;
    public static final int op_last = op_alignOf;
}
