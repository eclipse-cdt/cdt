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
            catchHandlers = new IASTStatement[ DEFAULT_CATCH_HANDLER_LIST_SIZE ];
            currentIndex = 0;
        }
        if( catchHandlers.length == currentIndex )
        {
            IASTStatement [] old = catchHandlers;
            catchHandlers = new IASTStatement[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                catchHandlers[i] = old[i];
        }
        catchHandlers[ currentIndex++ ] = statement;    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator#getCatchHandlers()
     */
    public List getCatchHandlers() {
        if( catchHandlers == null ) return Collections.EMPTY_LIST;
        removeNullCatchHandlers();
        return Arrays.asList( catchHandlers );
    }

    private void removeNullCatchHandlers() {
        int nullCount = 0; 
        for( int i = 0; i < catchHandlers.length; ++i )
            if( catchHandlers[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTStatement [] old = catchHandlers;
        int newSize = old.length - nullCount;
        catchHandlers = new IASTStatement[ newSize ];
        for( int i = 0; i < newSize; ++i )
            catchHandlers[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTStatement [] catchHandlers = null;
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement#addCatchHandler(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler)
     */

}
