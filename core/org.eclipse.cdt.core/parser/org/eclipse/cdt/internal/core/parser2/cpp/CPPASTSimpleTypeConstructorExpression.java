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
package org.eclipse.cdt.internal.core.parser2.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;

/**
 * @author jcamelon
 */
public class CPPASTSimpleTypeConstructorExpression extends CPPASTNode implements
        ICPPASTSimpleTypeConstructorExpression {

    private int st;
    private IASTExpression init;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression#getSimpleType()
     */
    public int getSimpleType() {
        return st;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression#setSimpleType(int)
     */
    public void setSimpleType(int value) {
        st = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression#getInitialValue()
     */
    public IASTExpression getInitialValue() {
        return init;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression#setInitialValue(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setInitialValue(IASTExpression expression) {
        init = expression;
    }

}
