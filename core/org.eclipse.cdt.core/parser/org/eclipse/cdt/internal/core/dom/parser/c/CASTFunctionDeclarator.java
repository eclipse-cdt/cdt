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
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTFunctionDeclarator extends CASTDeclarator implements
        IASTStandardFunctionDeclarator {

    private IASTParameterDeclaration [] parameters = null;
    private boolean varArgs;
    

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#getParameters()
     */
    public IASTParameterDeclaration[] getParameters() {
        if( parameters == null ) return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        return (IASTParameterDeclaration[]) ArrayUtil.removeNulls( IASTParameterDeclaration.class, parameters );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#addParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
     */
    public void addParameterDeclaration(IASTParameterDeclaration parameter) {
        parameters = (IASTParameterDeclaration[]) ArrayUtil.append( IASTParameterDeclaration.class, parameters, parameter );
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
