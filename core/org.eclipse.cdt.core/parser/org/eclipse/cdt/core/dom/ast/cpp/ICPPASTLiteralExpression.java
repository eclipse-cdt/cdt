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

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

/**
 * @author jcamelon
 */
public interface ICPPASTLiteralExpression extends IASTLiteralExpression {

    public static final int lk_this = IASTLiteralExpression.lk_last + 1;
    public static final int lk_true = IASTLiteralExpression.lk_last + 2;
    public static final int lk_false = IASTLiteralExpression.lk_last + 3;
    public static final int lk_last = lk_false;
}
