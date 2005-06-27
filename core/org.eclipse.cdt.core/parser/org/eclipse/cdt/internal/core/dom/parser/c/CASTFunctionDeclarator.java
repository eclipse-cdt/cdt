/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
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
    private int parametersPos=-1;
    private boolean varArgs;
    

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#getParameters()
     */
    public IASTParameterDeclaration[] getParameters() {
        if( parameters == null ) return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        parameters = (IASTParameterDeclaration[]) ArrayUtil.removeNullsAfter( IASTParameterDeclaration.class, parameters, parametersPos );
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#addParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
     */
    public void addParameterDeclaration(IASTParameterDeclaration parameter) {
    	if (parameter != null) {
    		parametersPos++;
    		parameters = (IASTParameterDeclaration[]) ArrayUtil.append( IASTParameterDeclaration.class, parameters, parameter );
    	}        
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
