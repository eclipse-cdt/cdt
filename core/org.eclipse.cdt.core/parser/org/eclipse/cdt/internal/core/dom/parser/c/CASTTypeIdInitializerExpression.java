/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;

/**
 * @author jcamelon
 */
public class CASTTypeIdInitializerExpression extends CASTNode implements
        ICASTTypeIdInitializerExpression {

    private IASTTypeId t;
    private IASTInitializer i;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#getTypeId()
     */
    public IASTTypeId getTypeId() {
        return t;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#setTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void setTypeId(IASTTypeId typeId) {
        t = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#getInitializer()
     */
    public IASTInitializer getInitializer() {
        return i;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#setInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
     */
    public void setInitializer(IASTInitializer initializer) {
        i = initializer;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( t != null ) if( !t.accept( action ) ) return false;
        if( i != null ) if( !i.accept( action ) ) return false;
        return true;
    }
}
