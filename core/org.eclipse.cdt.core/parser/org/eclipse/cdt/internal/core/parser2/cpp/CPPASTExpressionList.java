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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;

/**
 * @author jcamelon
 */
public class CPPASTExpressionList extends CPPASTNode implements
        IASTExpressionList {
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTExpressionList#getExpressions()
     */
    public List getExpressions() {
        if( expressions == null ) return Collections.EMPTY_LIST;
        removeNullExpressions();
        return Arrays.asList( expressions );
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
