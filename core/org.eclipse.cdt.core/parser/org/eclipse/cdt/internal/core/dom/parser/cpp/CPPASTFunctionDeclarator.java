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

/**
 * @author jcamelon
 */
public class CPPASTFunctionDeclarator extends CPPASTDeclarator implements
        ICPPASTFunctionDeclarator {
    
    private IASTParameterDeclaration [] parameters = null;
    private static final int DEFAULT_PARAMETERS_LIST_SIZE = 2;
    private ICPPFunctionScope scope = null;
    private int currentIndex = 0;
    private boolean varArgs;
    private boolean pureVirtual;
    private boolean isVolatile;
    private boolean isConst;
    
    /**
     * @param decls2
     */
    private void removeNullParameters() {
        int nullCount = 0; 
        for( int i = 0; i < parameters.length; ++i )
            if( parameters[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTParameterDeclaration [] old = parameters;
        int newSize = old.length - nullCount;
        parameters = new IASTParameterDeclaration[ newSize ];
        for( int i = 0; i < newSize; ++i )
            parameters[i] = old[i];
        currentIndex = newSize;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#getParameters()
     */
    public IASTParameterDeclaration [] getParameters() {
        if( parameters == null ) return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        removeNullParameters();
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator#addParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
     */
    public void addParameterDeclaration(IASTParameterDeclaration parameter) {
        if( parameters == null )
        {
            parameters = new IASTParameterDeclaration[ DEFAULT_PARAMETERS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( parameters.length == currentIndex )
        {
            IASTParameterDeclaration [] old = parameters;
            parameters = new IASTParameterDeclaration[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                parameters[i] = old[i];
        }
        parameters[ currentIndex++ ] = parameter;
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

    private int currentTypeIdIndex = 0;    
    private IASTTypeId [] typeIds = null;
    private static final int DEFAULT_TYPEID_LIST_SIZE = 4;
    private void removeNullTypeIds() {
        int nullCount = 0; 
        for( int i = 0; i < typeIds.length; ++i )
            if( typeIds[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTTypeId [] old = typeIds;
        int newSize = old.length - nullCount;
        typeIds = new IASTTypeId[ newSize ];
        for( int i = 0; i < newSize; ++i )
            typeIds[i] = old[i];
        currentTypeIdIndex = newSize;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#getExceptionSpecification()
     */
    public IASTTypeId[] getExceptionSpecification() {
        if( typeIds == null ) return IASTTypeId.EMPTY_TYPEID_ARRAY;
        removeNullTypeIds();
        return typeIds;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#addExceptionSpecificationTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void addExceptionSpecificationTypeId(IASTTypeId typeId) {
        if( typeIds == null )
        {
            typeIds = new IASTTypeId[ DEFAULT_TYPEID_LIST_SIZE ];
            currentTypeIdIndex = 0;
        }
        if( typeIds.length == currentTypeIdIndex )
        {
            IASTTypeId [] old = typeIds;
            typeIds = new IASTTypeId[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                typeIds[i] = old[i];
        }
        typeIds[ currentTypeIdIndex++ ] = typeId;
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


    private int currentConstructorChainIndex = 0;    
    private ICPPASTConstructorChainInitializer [] constructorChain = null;
    private static final int DEFAULT_CONS_LIST_SIZE = 4;

    private void removeNullConstructors() {
        int nullCount = 0; 
        for( int i = 0; i < constructorChain.length; ++i )
            if( constructorChain[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        ICPPASTConstructorChainInitializer [] old = constructorChain;
        int newSize = old.length - nullCount;
        constructorChain = new ICPPASTConstructorChainInitializer[ newSize ];
        for( int i = 0; i < newSize; ++i )
            constructorChain[i] = old[i];
        currentConstructorChainIndex = newSize;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#getConstructorChain()
     */
    public ICPPASTConstructorChainInitializer[] getConstructorChain() {
        if( constructorChain == null ) return ICPPASTConstructorChainInitializer.EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY;
        removeNullConstructors();
        return constructorChain;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator#addConstructorToChain(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer)
     */
    public void addConstructorToChain(ICPPASTConstructorChainInitializer initializer) {
        if( constructorChain == null )
        {
            constructorChain = new ICPPASTConstructorChainInitializer[ DEFAULT_CONS_LIST_SIZE ];
            currentConstructorChainIndex = 0;
        }
        if( constructorChain.length == currentConstructorChainIndex )
        {
            ICPPASTConstructorChainInitializer [] old = constructorChain;
            constructorChain = new ICPPASTConstructorChainInitializer[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                constructorChain[i] = old[i];
        }
        constructorChain[ currentConstructorChainIndex++ ] = initializer;
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
