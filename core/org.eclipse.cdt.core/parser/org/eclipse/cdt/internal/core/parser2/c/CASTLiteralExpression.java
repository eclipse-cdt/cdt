/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.c;

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

/**
 * @author jcamelon
 */
public class CASTLiteralExpression extends CASTNode implements
        IASTLiteralExpression {

    private int kind;
    private String value = ""; //$NON-NLS-1$

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTLiteralExpression#getKind()
     */
    public int getKind() {
        return kind;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTLiteralExpression#setKind(int)
     */
    public void setKind(int value) {
        kind = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTLiteralExpression#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return value;
    }

}
