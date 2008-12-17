/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Represents a literal
 */
public class CASTLiteralExpression extends ASTNode implements IASTLiteralExpression {

    private int kind;
    private char[] value = CharArrayUtils.EMPTY;

    public CASTLiteralExpression() {
	}

	public CASTLiteralExpression(int kind, char[] value) {
		this.kind = kind;
		this.value = value;
	}
	
	public CASTLiteralExpression copy() {
		CASTLiteralExpression copy = new CASTLiteralExpression(kind, value == null ? null : value.clone());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public int getKind() {
        return kind;
    }

    public void setKind(int value) {
        assertNotFrozen();
        kind = value;
    }

    public char[] getValue() {
    	return value;
    }

    public void setValue(char[] value) {
        assertNotFrozen();
    	this.value= value;
    }
    
    @Override
	public String toString() {
        return new String(value);
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
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
    
    /**
     * @deprecated, use {@link #setValue(char[])}, instead.
     */
    @Deprecated
	public void setValue(String value) {
        assertNotFrozen();
        this.value = value.toCharArray();
    }
    
    /**
     * @deprecated use {@link #CASTLiteralExpression(int, char[])}, instead.
     */
	@Deprecated
	public CASTLiteralExpression(int kind, String value) {
		this(kind, value.toCharArray());
	}
}
