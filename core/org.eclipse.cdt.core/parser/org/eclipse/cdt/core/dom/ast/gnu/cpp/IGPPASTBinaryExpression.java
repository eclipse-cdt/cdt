/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;

/**
 * @author jcamelon
 */
public interface IGPPASTBinaryExpression extends ICPPASTBinaryExpression {
    
    public static final int op_max = ICPPASTBinaryExpression.op_last + 1;
    public static final int op_min = ICPPASTBinaryExpression.op_last + 2;
    public static final int op_last = op_min;

}
