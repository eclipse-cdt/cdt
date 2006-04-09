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
import org.eclipse.cdt.core.dom.ast.IType;
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

    public boolean isGlobal() {
        return global;
    }

    public void setIsGlobal(boolean value) {
        global = value;
    }

    public IASTExpression getNewPlacement() {
        return placement;
    }

    public void setNewPlacement(IASTExpression expression) {
        placement = expression;
    }

    public IASTExpression getNewInitializer() {
        return initializer;
    }

    public void setNewInitializer(IASTExpression expression) {
        initializer = expression;
    }

    public IASTTypeId getTypeId() {
        return typeId;
    }

    public void setTypeId(IASTTypeId typeId) {
        this.typeId = typeId;
    }

    public boolean isNewTypeId() {
        return isNewTypeId;
    }

    public void setIsNewTypeId(boolean value) {
        isNewTypeId = value;
    }

    public IASTExpression [] getNewTypeIdArrayExpressions() {
        if( arrayExpressions == null ) return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        return (IASTExpression[]) ArrayUtil.removeNulls( IASTExpression.class, arrayExpressions );
    }

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
    
    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
