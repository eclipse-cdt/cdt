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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CPPASTFunctionDeclarator extends CPPASTDeclarator implements
        ICPPASTFunctionDeclarator {
    
    private IASTParameterDeclaration [] parameters = null;
    private ICPPFunctionScope scope = null;
    private boolean varArgs;
    private boolean pureVirtual;
    private boolean isVolatile;
    private boolean isConst;
    
  
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#getParameters()
     */
    public IASTParameterDeclaration [] getParameters() {
        if( parameters == null ) return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        return (IASTParameterDeclaration[]) ArrayUtil.removeNulls( IASTParameterDeclaration.class, parameters );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#addParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
     */
    public void addParameterDeclaration(IASTParameterDeclaration parameter) {
        parameters = (IASTParameterDeclaration []) ArrayUtil.append( IASTParameterDeclaration.class, parameters, parameter );
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


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#isConst()
     */
    public boolean isConst() {
        return isConst;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#setConst(boolean)
     */
    public void setConst(boolean value) {
        this.isConst = value;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#isVolatile()
     */
    public boolean isVolatile() {
        return isVolatile;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#setVolatile(boolean)
     */
    public void setVolatile(boolean value) {
        this.isVolatile = value;
    }

    private IASTTypeId [] typeIds = null;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#getExceptionSpecification()
     */
    public IASTTypeId[] getExceptionSpecification() {
        if( typeIds == null ) return IASTTypeId.EMPTY_TYPEID_ARRAY;
        return (IASTTypeId[]) ArrayUtil.removeNulls( IASTTypeId.class, typeIds );
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#addExceptionSpecificationTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void addExceptionSpecificationTypeId(IASTTypeId typeId) {
        typeIds = (IASTTypeId[]) ArrayUtil.append( IASTTypeId.class, typeIds, typeId );
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#isPureVirtual()
     */
    public boolean isPureVirtual() {
        return pureVirtual;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#setPureVirtual(boolean)
     */
    public void setPureVirtual(boolean isPureVirtual) {
        this.pureVirtual = isPureVirtual;
    }


    private ICPPASTConstructorChainInitializer [] constructorChain = null;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#getConstructorChain()
     */
    public ICPPASTConstructorChainInitializer[] getConstructorChain() {
        if( constructorChain == null ) return ICPPASTConstructorChainInitializer.EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY;
        return (ICPPASTConstructorChainInitializer[]) ArrayUtil.removeNulls( ICPPASTConstructorChainInitializer.class, constructorChain );
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#addConstructorToChain(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer)
     */
    public void addConstructorToChain(ICPPASTConstructorChainInitializer initializer) {
        constructorChain = (ICPPASTConstructorChainInitializer[]) ArrayUtil.append( ICPPASTConstructorChainInitializer.class, constructorChain, initializer );
    }

    public ICPPFunctionScope getFunctionScope(){
        if( scope != null )
            return scope;
        
        ASTNodeProperty prop = getPropertyInParent();
        if( prop == IASTSimpleDeclaration.DECLARATOR || prop == IASTFunctionDefinition.DECLARATOR )
            scope = new CPPFunctionScope( this );
        return scope;
    }
    
    protected boolean postAccept( ASTVisitor action ){
        IASTParameterDeclaration [] params = getParameters();
        for ( int i = 0; i < params.length; i++ ) {
            if( !params[i].accept( action ) ) return false;
        }
        
        ICPPASTConstructorChainInitializer [] chain = getConstructorChain();
        for ( int i = 0; i < chain.length; i++ ) {
            if( !chain[i].accept( action ) ) return false;
        }
        
        IASTInitializer initializer = getInitializer();
        if( initializer != null ) if( !initializer.accept( action ) ) return false;
        
        IASTTypeId[] ids = getExceptionSpecification();
        for ( int i = 0; i < ids.length; i++ ) {
            if( !ids[i].accept( action ) ) return false;
        }
        return true;
    }
}
