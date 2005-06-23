/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTNewExpression extends CPPASTNode implements
        ICPPASTNewExpression, IASTAmbiguityParent {

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
    public IASTExpression [] getNewTypeIdArrayExpressions() {
        if( arrayExpressions == null ) return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        return (IASTExpression[]) ArrayUtil.removeNulls( IASTExpression.class, arrayExpressions );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression#addNewTypeIdArrayExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void addNewTypeIdArrayExpression(IASTExpression expression) {
        arrayExpressions = (IASTExpression[]) ArrayUtil.append( IASTExpression.class, arrayExpressions, expression );
    }
    
    private IASTExpression [] arrayExpressions = null;

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( placement != null ) if( !placement.accept( action ) ) return false;
        if( typeId != null ) if( !typeId.accept( action ) ) return false;

        IASTExpression [] exps = getNewTypeIdArrayExpressions();
        for( int i = 0; i < exps.length; i++ )
            if( !exps[i].accept( action ) ) return false;
            
        if( initializer != null ) if( !initializer.accept( action ) ) return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == placement )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            placement  = (IASTExpression) other;
        }
        if( child == initializer )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            initializer  = (IASTExpression) other;
        }
        if( arrayExpressions == null ) return;
        for( int i = 0; i < arrayExpressions.length; ++i )
            if( arrayExpressions[i] == child )
            {
                other.setPropertyInParent( child.getPropertyInParent() );
                other.setParent( child.getParent() );
                arrayExpressions[i] = (IASTExpression) other;
            }   
    }
}
