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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;

/**
 * @author jcamelon
 */
public class CPPASTTemplateDeclaration extends CPPASTNode implements
        ICPPASTTemplateDeclaration {

    private boolean exported;
    private IASTDeclaration declaration;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#isExported()
     */
    public boolean isExported() {
        return exported;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#setExported(boolean)
     */
    public void setExported(boolean value) {
        exported = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#getDeclaration()
     */
    public IASTDeclaration getDeclaration() {
        return declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#setDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void setDeclaration(IASTDeclaration declaration) {
        this.declaration = declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#getTemplateParameters()
     */
    public List getTemplateParameters() {
        if( parameters == null ) return Collections.EMPTY_LIST;
        removeNullParameters();
        return Arrays.asList( parameters );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#addTemplateParamter(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter)
     */
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

}
