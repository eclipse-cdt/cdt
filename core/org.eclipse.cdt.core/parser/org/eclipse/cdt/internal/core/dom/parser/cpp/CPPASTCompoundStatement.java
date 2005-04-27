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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTCompoundStatement extends CPPASTNode implements
        IASTCompoundStatement, IASTAmbiguityParent {

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
    private ICPPScope scope = null;
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
     * @see org.eclipse.cdt.core.dom.ast.IASTCompoundStatement#resolveScope()
     */
    public IScope getScope() {
    	if( scope == null )
    		scope = new CPPBlockScope( this );
        return scope;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        IASTStatement [] s = getStatements();
        for ( int i = 0; i < s.length; i++ ) {
            if( !s[i].accept( action ) ) return false;
        }
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        IASTStatement [] s = getStatements();
        for( int i = 0; i < s.length; ++i )
        {
            if( s[i] == child )
            {
                other.setParent( s[i].getParent() );
                other.setPropertyInParent( s[i].getPropertyInParent() );
                s[i] = (IASTStatement) other;
                break;
            }
        }
    }
}
