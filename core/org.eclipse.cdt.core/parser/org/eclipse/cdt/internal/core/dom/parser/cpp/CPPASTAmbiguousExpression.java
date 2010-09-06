/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;

public class CPPASTAmbiguousExpression extends ASTAmbiguousNode implements
        IASTAmbiguousExpression {

    private IASTExpression [] exp = new IASTExpression[2];
    private int expPos=-1;
    
    public CPPASTAmbiguousExpression(IASTExpression... expressions) {
		for(IASTExpression e : expressions)
			addExpression(e);
	}

    
    public IASTExpression copy() {
		throw new UnsupportedOperationException();
	}
    
	public void addExpression(IASTExpression e) {
        assertNotFrozen();
    	if (e != null) {
    		exp = (IASTExpression[]) ArrayUtil.append( IASTExpression.class, exp, ++expPos, e );
    		e.setParent(this);
			e.setPropertyInParent(SUBEXPRESSION);
    	}
    }

    public IASTExpression[] getExpressions() {
        exp = (IASTExpression[]) ArrayUtil.removeNullsAfter( IASTExpression.class, exp, expPos );
    	return exp;
    }

    @Override
	public IASTNode[] getNodes() {
        return getExpressions();
    }
}
