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
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;

/**
 * @author jcamelon
 */
public class CPPASTNewExpression extends CPPASTNode implements
        ICPPASTNewExpression {

    private boolean global;
    private IASTExpression placement;
    private IASTExpression initializer;
    private IASTTypeId typeId;
    private boolean isNewTypeId;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#isGlobal()
     */
    public boolean isGlobal() {
        return global;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#setIsGlobal(boolean)
     */
    public void setIsGlobal(boolean value) {
        global = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#getNewPlacement()
     */
    public IASTExpression getNewPlacement() {
        return placement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#setNewPlacement(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setNewPlacement(IASTExpression expression) {
        placement = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#getNewInitializer()
     */
    public IASTExpression getNewInitializer() {
        return initializer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#setNewInitializer(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setNewInitializer(IASTExpression expression) {
        initializer = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#getTypeId()
     */
    public IASTTypeId getTypeId() {
        return typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#setTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void setTypeId(IASTTypeId typeId) {
        this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#isNewTypeId()
     */
    public boolean isNewTypeId() {
        return isNewTypeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#setIsNewTypeId(boolean)
     */
    public void setIsNewTypeId(boolean value) {
        isNewTypeId = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#getNewTypeIdArrayExpressions()
     */
    public List getNewTypeIdArrayExpressions() {
        if( arrayExpressions == null ) return Collections.EMPTY_LIST;
        removeNullExpressions();
        return Arrays.asList( arrayExpressions );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#addNewTypeIdArrayExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void addNewTypeIdArrayExpression(IASTExpression expression) {
        if( arrayExpressions == null )
        {
            arrayExpressions = new IASTExpression[ DEFAULT_ARRAY_EXPRESSIONS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( arrayExpressions.length == currentIndex )
        {
            IASTExpression [] old = arrayExpressions;
            arrayExpressions = new IASTExpression[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                arrayExpressions[i] = old[i];
        }
        arrayExpressions[ currentIndex++ ] = expression;
    }
    
    private void removeNullExpressions() {
        int nullCount = 0; 
        for( int i = 0; i < arrayExpressions.length; ++i )
            if( arrayExpressions[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTExpression [] old = arrayExpressions;
        int newSize = old.length - nullCount;
        arrayExpressions = new IASTExpression[ newSize ];
        for( int i = 0; i < newSize; ++i )
            arrayExpressions[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTExpression [] arrayExpressions = null;
    private static final int DEFAULT_ARRAY_EXPRESSIONS_LIST_SIZE = 4;

}
