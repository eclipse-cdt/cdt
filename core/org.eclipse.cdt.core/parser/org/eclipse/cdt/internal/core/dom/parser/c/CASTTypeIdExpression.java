/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;

/**
 * @author jcamelon
 */
public class CASTTypeIdExpression extends CASTNode implements
        IASTTypeIdExpression {

    private int op;
    private IASTTypeId typeId;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression#getOperator()
     */
    public int getOperator() {
        return op;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression#setOperator(int)
     */
    public void setOperator(int value) {
        this.op = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression#setTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void setTypeId(IASTTypeId typeId) {
       this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression#getTypeId()
     */
    public IASTTypeId getTypeId() {
        return typeId;
    }

}
