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

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author jcamelon
 */
public class CASTCompoundStatement extends CASTNode implements
        IASTCompoundStatement {

    private int currentIndex = 0;
    private void removeNullStatements() {
        int nullCount = 0; 
        for( int i = 0; i < statements.length; ++i )
            if( statements[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTStatement [] old = statements;
        int newSize = old.length - nullCount;
        statements = new IASTStatement[ newSize ];
        for( int i = 0; i < newSize; ++i )
            statements[i] = old[i];
        currentIndex = newSize;
    }

    
    private IASTStatement [] statements = null;
    private IScope scope = null;
    private static final int DEFAULT_STATEMENT_LIST_SIZE = 8;


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompoundStatement#getStatements()
     */
    public IASTStatement[] getStatements() {
        if( statements == null ) return IASTStatement.EMPTY_STATEMENT_ARRAY;
        removeNullStatements();
        return statements;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompoundStatement#addStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void addStatement(IASTStatement statement) {
        if( statements == null )
        {
            statements = new IASTStatement[ DEFAULT_STATEMENT_LIST_SIZE ];
            currentIndex = 0;
        }
        if( statements.length == currentIndex )
        {
            IASTStatement [] old = statements;
            statements = new IASTStatement[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                statements[i] = old[i];
        }
        statements[ currentIndex++ ] = statement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompoundStatement#resolveBinding()
     */
    public IScope getScope() {
        if( scope == null )
            scope = new CScope( this );
        return scope;
    }

}
