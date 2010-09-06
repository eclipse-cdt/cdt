/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IType;

public class CASTProblemExpression extends CASTProblemOwner implements IASTProblemExpression {

    public CASTProblemExpression() {
		super();
	}

	public CASTProblemExpression(IASTProblem problem) {
		super(problem);
	}

	public CASTProblemExpression copy() {
		CASTProblemExpression copy = new CASTProblemExpression();
		copyBaseProblem(copy);
		return copy;
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
        super.accept(action);	// visits the problem
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
    	return null;
    }

	public boolean isLValue() {
		return false;
	}
	
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
}
