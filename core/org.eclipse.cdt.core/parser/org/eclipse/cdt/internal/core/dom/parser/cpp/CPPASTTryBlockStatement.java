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
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;

/**
 * @author jcamelon
 */
public class CPPASTTryBlockStatement extends CPPASTNode implements
        ICPPASTTryBlockStatement {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator#addCatchHandler(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void addCatchHandler(ICPPASTCatchHandler statement) {
        if( catchHandlers == null )
        {
            catchHandlers = new ICPPASTCatchHandler[ DEFAULT_CATCH_HANDLER_LIST_SIZE ];
            currentIndex = 0;
        }
        if( catchHandlers.length == currentIndex )
        {
        	ICPPASTCatchHandler [] old = catchHandlers;
            catchHandlers = new ICPPASTCatchHandler[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                catchHandlers[i] = old[i];
        }
        catchHandlers[ currentIndex++ ] = statement;    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator#getCatchHandlers()
     */
    public ICPPASTCatchHandler[] getCatchHandlers() {
        if( catchHandlers == null ) return ICPPASTCatchHandler.EMPTY_CATCHHANDLER_ARRAY;
        removeNullCatchHandlers();
        return catchHandlers;
    }

    private void removeNullCatchHandlers() {
        int nullCount = 0; 
        for( int i = 0; i < catchHandlers.length; ++i )
            if( catchHandlers[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        ICPPASTCatchHandler [] old = catchHandlers;
        int newSize = old.length - nullCount;
        catchHandlers = new ICPPASTCatchHandler[ newSize ];
        for( int i = 0; i < newSize; ++i )
            catchHandlers[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private ICPPASTCatchHandler [] catchHandlers = null;
    private static final int DEFAULT_CATCH_HANDLER_LIST_SIZE = 4;
    private IASTStatement tryBody;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement#setTryBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setTryBody(IASTStatement tryBlock) {
        tryBody = tryBlock;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement#getTryBody()
     */
    public IASTStatement getTryBody() {
        return tryBody;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( tryBody != null ) if( !tryBody.accept( action ) ) return false;
        
        ICPPASTCatchHandler [] handlers = getCatchHandlers();
        for ( int i = 0; i < handlers.length; i++ ) {
            if( !handlers[i].accept( action ) ) return false;
        }
        return true;
    }

}
