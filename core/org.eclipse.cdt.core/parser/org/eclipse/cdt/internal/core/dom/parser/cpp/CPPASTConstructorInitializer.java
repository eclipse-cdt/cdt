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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;

/**
 * @author jcamelon
 */
public class CPPASTConstructorInitializer extends CPPASTNode implements
        ICPPASTConstructorInitializer {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer#getExpression()
     */
    public IASTExpression getExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer#setExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setExpression(IASTExpression expression) {
        this.exp = expression;
    }

}
