/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author jcamelon
 */
public class CASTLiteralExpression extends CASTNode implements
        IASTLiteralExpression {

    private int kind;
    private String value = ""; //$NON-NLS-1$

    public int getKind() {
        return kind;
    }

    public void setKind(int value) {
        kind = value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String toString() {
        return value;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
    public IType getExpressionType() {
    	return CVisitor.getExpressionType(this);
    }
    
}
