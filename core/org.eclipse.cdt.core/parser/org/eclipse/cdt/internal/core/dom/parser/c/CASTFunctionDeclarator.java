/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;

/**
 * @author jcamelon
 */
public class CASTFunctionDeclarator extends CASTDeclarator implements
        IASTStandardFunctionDeclarator {

    private IASTParameterDeclaration [] parameters = null;
    private static final int DEFAULT_PARAMETERS_LIST_SIZE = 2;
    
    private int currentIndex = 0;
    private boolean varArgs;
    
    /**
     * @param decls2
     */
    private void removeNullParameters() {
        int nullCount = 0; 
        for( int i = 0; i < parameters.length; ++i )
            if( parameters[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTParameterDeclaration [] old = parameters;
        int newSize = old.length - nullCount;
        parameters = new IASTParameterDeclaration[ newSize ];
        for( int i = 0; i < newSize; ++i )
            parameters[i] = old[i];
        currentIndex = newSize;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#getParameters()
     */
    public IASTParameterDeclaration[] getParameters() {
        if( parameters == null ) return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        removeNullParameters();
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#addParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
     */
    public void addParameterDeclaration(IASTParameterDeclaration parameter) {
        if( parameters == null )
        {
            parameters = new IASTParameterDeclaration[ DEFAULT_PARAMETERS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( parameters.length == currentIndex )
        {
            IASTParameterDeclaration [] old = parameters;
            parameters = new IASTParameterDeclaration[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                parameters[i] = old[i];
        }
        parameters[ currentIndex++ ] = parameter;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#takesVarArgs()
     */
    public boolean takesVarArgs() {
        return varArgs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#setVarArgs(boolean)
     */
    public void setVarArgs(boolean value) {
        varArgs = value;
    }

    protected boolean postAccept( ASTVisitor action ){
        IASTParameterDeclaration [] params = getParameters();
        for ( int i = 0; i < params.length; i++ ) {
            if( !params[i].accept( action ) ) return false;
        }
        return true;
    }
}
