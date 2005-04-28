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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTTryBlockStatement extends CPPASTNode implements
        ICPPASTTryBlockStatement, IASTAmbiguityParent {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator#addCatchHandler(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void addCatchHandler(ICPPASTCatchHandler statement) {
        catchHandlers = (ICPPASTCatchHandler[]) ArrayUtil.append( ICPPASTCatchHandler.class, catchHandlers, statement );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator#getCatchHandlers()
     */
    public ICPPASTCatchHandler[] getCatchHandlers() {
        if( catchHandlers == null ) return ICPPASTCatchHandler.EMPTY_CATCHHANDLER_ARRAY;
        return (ICPPASTCatchHandler[]) ArrayUtil.removeNulls( ICPPASTCatchHandler.class, catchHandlers );
    }


    private ICPPASTCatchHandler [] catchHandlers = null;
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

    public void replace(IASTNode child, IASTNode other) {
        if( tryBody == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            tryBody = (IASTStatement) other;
        }
    }
}
