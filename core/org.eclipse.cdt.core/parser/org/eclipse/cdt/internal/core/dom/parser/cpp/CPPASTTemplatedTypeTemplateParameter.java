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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;

/**
 * @author jcamelon
 */
public class CPPASTTemplatedTypeTemplateParameter extends CPPASTNode implements
        ICPPASTTemplatedTypeTemplateParameter {

    public ICPPASTTemplateParameter[] getTemplateParameters() {
        if( parameters == null ) return ICPPASTTemplateParameter.EMPTY_TEMPLATEPARAMETER_ARRAY;
        removeNullParameters();
        return parameters;
    }

    public void addTemplateParamter(ICPPASTTemplateParameter parm) {
        if( parameters == null )
        {
            parameters = new ICPPASTTemplateParameter[ DEFAULT_PARMS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( parameters.length == currentIndex )
        {
            ICPPASTTemplateParameter [] old = parameters;
            parameters = new ICPPASTTemplateParameter[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                parameters[i] = old[i];
        }
        parameters[ currentIndex++ ] = parm;
    }
    private void removeNullParameters() {
        int nullCount = 0; 
        for( int i = 0; i < parameters.length; ++i )
            if( parameters[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        ICPPASTTemplateParameter[] old = parameters;
        int newSize = old.length - nullCount;
        parameters = new ICPPASTTemplateParameter[ newSize ];
        for( int i = 0; i < newSize; ++i )
            parameters[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private ICPPASTTemplateParameter [] parameters = null;
    private static final int DEFAULT_PARMS_LIST_SIZE = 4;
    private IASTName name;
    private IASTExpression defaultValue;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name =name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter#getDefaultValue()
     */
    public IASTExpression getDefaultValue() {
        return defaultValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter#setDefaultValue(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setDefaultValue(IASTExpression expression) {
        this.defaultValue = expression;
    }

}
