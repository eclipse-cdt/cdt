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
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;

/**
 * @author jcamelon
 */
public class CPPASTReturnStatement extends CPPASTNode implements
        IASTReturnStatement {
    private IASTExpression retValue;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTReturnStatement#getReturnValue()
     */
    public IASTExpression getReturnValue() {
        return retValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTReturnStatement#setReturnValue(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setReturnValue(IASTExpression returnValue) {
        retValue = returnValue;
    }

}
