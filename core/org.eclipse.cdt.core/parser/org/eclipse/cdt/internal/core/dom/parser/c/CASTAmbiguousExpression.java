/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;

public class CASTAmbiguousExpression extends CASTAmbiguity implements IASTAmbiguousExpression {

    private IASTExpression [] expressions = new IASTExpression[2];
    private int expressionsPos=-1;

    
    public CASTAmbiguousExpression(IASTExpression... expressions) {
		for(IASTExpression e : expressions)
			addExpression(e);
	}

    @Override
	protected IScope getAffectedScope() {
    	// an expression cannot pollute a parent scope
    	return null;
    }
    
	public void addExpression(IASTExpression e) {
        assertNotFrozen();
    	if (e != null) {
    		expressions = (IASTExpression[]) ArrayUtil.append( IASTExpression.class, expressions, ++expressionsPos, e );
    		e.setParent(this);
			e.setPropertyInParent(SUBEXPRESSION);
    	}
    }

    public IASTExpression[] getExpressions() {
    	expressions = (IASTExpression[]) ArrayUtil.removeNullsAfter( IASTExpression.class, expressions, expressionsPos ); 
        return expressions;
    }

    @Override
	public IASTNode[] getNodes() {
        return getExpressions();
    }

    public IType getExpressionType() {
    	return CVisitor.getExpressionType(this);
    }

	public IASTExpression copy() {
		throw new UnsupportedOperationException();
	}
    
}
