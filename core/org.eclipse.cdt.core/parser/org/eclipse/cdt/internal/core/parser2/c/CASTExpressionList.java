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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;

/**
 * @author jcamelon
 */
public class CASTExpressionList extends CASTNode implements IASTExpressionList {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTExpressionList#getExpressions()
     */
    public IASTExpression[] getExpressions() {
        if( expressions == null ) return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        removeNullExpressions();
        return expressions;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTExpressionList#addExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void addExpression(IASTExpression expression) {
        if( expressions == null )
        {
            expressions = new IASTExpression[ DEFAULT_EXPRESSIONLIST_SIZE ];
            currentIndex = 0;
        }
        if( expressions.length == currentIndex )
        {
            IASTExpression [] old = expressions;
            expressions = new IASTExpression[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                expressions[i] = old[i];
        }
        expressions[ currentIndex++ ] = expression;
    }

    /**
     * @param decls2
     */
    private void removeNullExpressions() {
        int nullCount = 0; 
        for( int i = 0; i < expressions.length; ++i )
            if( expressions[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTExpression [] old = expressions;
        int newSize = old.length - nullCount;
        expressions = new IASTExpression[ newSize ];
        for( int i = 0; i < newSize; ++i )
            expressions[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTExpression [] expressions = null;
    private static final int DEFAULT_EXPRESSIONLIST_SIZE = 4;

}
