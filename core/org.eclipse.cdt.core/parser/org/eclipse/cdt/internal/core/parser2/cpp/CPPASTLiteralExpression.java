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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;


/**
 * @author jcamelon
 */
public class CPPASTLiteralExpression extends CPPASTNode implements
        ICPPASTLiteralExpression {

    private int kind;
    private String value = "";  //$NON-NLS-1$

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
        this.kind = value;
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
