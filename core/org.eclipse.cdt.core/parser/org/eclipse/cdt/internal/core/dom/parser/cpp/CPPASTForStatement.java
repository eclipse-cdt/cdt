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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTForStatement extends CPPASTNode implements IASTForStatement, IASTAmbiguityParent {
    private IScope scope = null;
    
    private IASTExpression initialExpression;
    private IASTDeclaration initDeclaration;
    private IASTExpression condition;
    private IASTExpression iterationExpression;
    private IASTStatement body;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getInitExpression()
     */
    public IASTExpression getInitExpression() {
        return initialExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setInit(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setInit(IASTExpression expression) {
        this.initialExpression = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getInitDeclaration()
     */
    public IASTDeclaration getInitDeclaration() {
        return initDeclaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setInit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void setInit(IASTDeclaration declaration) {
        this.initDeclaration = declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getCondition()
     */
    public IASTExpression getCondition() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setCondition(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setCondition(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getIterationExpression()
     */
    public IASTExpression getIterationExpression() {
        return iterationExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setIterationExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setIterationExpression(IASTExpression iterator) {
        this.iterationExpression = iterator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement statement) {
        body = statement;

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getScope()
     */
    public IScope getScope() {
        if( scope == null )
            scope = new CPPBlockScope( this );
        return scope;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( initDeclaration != null ) if( !initDeclaration.accept( action ) ) return false;
        if( initialExpression != null ) if( !initialExpression.accept( action ) ) return false;
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( iterationExpression != null ) if( !iterationExpression.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( body == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            body = (IASTStatement) other;
        }
        if( child == condition )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition  = (IASTExpression) other;
        }
        if( child == iterationExpression )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            iterationExpression  = (IASTExpression) other;
        }
        if( child == initialExpression )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            initialExpression  = (IASTExpression) other;
        }
        if( initDeclaration == child )
        {
            other.setParent( child.getParent() );
            other.setPropertyInParent( child.getPropertyInParent() );
            initDeclaration = (IASTDeclaration) other;
        }

    }
}
