/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author jcamelon
 */
public class CASTIdExpression extends CASTNode implements IASTIdExpression {

    private IASTName name;

    public IASTName getName() {
        return name;
    }

    public void setName(IASTName name) {
        this.name = name;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( name != null ) if( !name.accept( action ) ) return false;
        return true;
    }

	public int getRoleForName(IASTName n) {
		if( n == name )	return r_reference;
		return r_unclear;
	}
	
	public IType getExpressionType() {
		return CVisitor.getExpressionType(this);
	}
	
}
