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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

/**
 * @author jcamelon
 */
public class CPPASTTemplateId extends CPPASTNode implements ICPPASTTemplateId {

    private IASTName templateName;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#getTemplateName()
     */
    public IASTName getTemplateName() {
        return templateName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#setTemplateName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setTemplateName(IASTName name) {
        templateName = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#addTemplateArgument(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void addTemplateArgument(IASTTypeId typeId) {
        if( templateArguments == null )
        {
            templateArguments = new IASTNode[ DEFAULT_ARGS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( templateArguments.length == currentIndex )
        {
            IASTNode [] old = templateArguments;
            templateArguments = new IASTNode[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                templateArguments[i] = old[i];
        }
        templateArguments[ currentIndex++ ] = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#addTemplateArgument(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void addTemplateArgument(IASTExpression expression) {
        if( templateArguments == null )
        {
            templateArguments = new IASTNode[ DEFAULT_ARGS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( templateArguments.length == currentIndex )
        {
            IASTNode [] old = templateArguments;
            templateArguments = new IASTNode[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                templateArguments[i] = old[i];
        }
        templateArguments[ currentIndex++ ] = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId#getTemplateArguments()
     */
    public List getTemplateArguments() {
        if( templateArguments == null ) return Collections.EMPTY_LIST;
        removeNullArguments();
        return Arrays.asList( templateArguments );
    }
    
    private void removeNullArguments() {
        int nullCount = 0; 
        for( int i = 0; i < templateArguments.length; ++i )
            if( templateArguments[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTNode [] old = templateArguments;
        int newSize = old.length - nullCount;
        templateArguments = new IASTNode[ newSize ];
        for( int i = 0; i < newSize; ++i )
            templateArguments[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTNode [] templateArguments = null;
    private static final int DEFAULT_ARGS_LIST_SIZE = 4;


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
        // TODO Auto-generated method stub
        return null;
    }

}
