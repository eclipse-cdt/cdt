/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTTryBlockStatement extends ASTNode implements ICPPASTTryBlockStatement, IASTAmbiguityParent {


    public CPPASTTryBlockStatement() {
	}

	public CPPASTTryBlockStatement(IASTStatement tryBody) {
		setTryBody(tryBody);
	}

	@Override
	public CPPASTTryBlockStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTTryBlockStatement copy(CopyStyle style) {
		CPPASTTryBlockStatement copy = new CPPASTTryBlockStatement(tryBody == null ? null
				: tryBody.copy(style));
		for (ICPPASTCatchHandler handler : getCatchHandlers())
			copy.addCatchHandler(handler == null ? null : handler.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public void addCatchHandler(ICPPASTCatchHandler statement) {
        assertNotFrozen();
    	if (statement != null) {
    		catchHandlers = ArrayUtil.appendAt( ICPPASTCatchHandler.class, catchHandlers, ++catchHandlersPos, statement );
    		statement.setParent(this);
			statement.setPropertyInParent(CATCH_HANDLER);
    	}
    }


    @Override
	public ICPPASTCatchHandler[] getCatchHandlers() {
        if( catchHandlers == null ) return ICPPASTCatchHandler.EMPTY_CATCHHANDLER_ARRAY;
        catchHandlers = ArrayUtil.trimAt( ICPPASTCatchHandler.class, catchHandlers, catchHandlersPos );
        return catchHandlers;
    }


    private ICPPASTCatchHandler [] catchHandlers = null;
    private int catchHandlersPos=-1;
    private IASTStatement tryBody;

    @Override
	public void setTryBody(IASTStatement tryBlock) {
        assertNotFrozen();
        tryBody = tryBlock;
        if (tryBlock != null) {
			tryBlock.setParent(this);
			tryBlock.setPropertyInParent(BODY);
		}
    }

 
    @Override
	public IASTStatement getTryBody() {
        return tryBody;
    }

    @Override
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
        
        if( action.shouldVisitStatements ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if( tryBody == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            tryBody = (IASTStatement) other;
        }
    }
}
