/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
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
public class CPPASTFunctionDeclarator extends CPPASTDeclarator implements ICPPASTFunctionDeclarator {
    
    private IASTParameterDeclaration [] parameters = null;
    private int parametersPos=-1;
    private ICPPFunctionScope scope = null;
    private boolean varArgs;
    private boolean pureVirtual;
    private boolean isVolatile;
    private boolean isConst;
  
    
    public CPPASTFunctionDeclarator() {
	}

	public CPPASTFunctionDeclarator(IASTName name) {
		super(name);
	}

	public IASTParameterDeclaration [] getParameters() {
        if( parameters == null ) return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        parameters = (IASTParameterDeclaration[]) ArrayUtil.removeNullsAfter( IASTParameterDeclaration.class, parameters, parametersPos );
        return parameters;
    }

    public void addParameterDeclaration(IASTParameterDeclaration parameter) {
    	if (parameter != null) {
    		parameter.setParent(this);
			parameter.setPropertyInParent(FUNCTION_PARAMETER);
    		parameters = (IASTParameterDeclaration []) ArrayUtil.append( IASTParameterDeclaration.class, parameters, ++parametersPos, parameter );
    	}
    }

    public boolean takesVarArgs() {
        return varArgs;
    }

    public void setVarArgs(boolean value) {
        varArgs = value;
    }


    public boolean isConst() {
        return isConst;
    }


    public void setConst(boolean value) {
        this.isConst = value;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setVolatile(boolean value) {
        this.isVolatile = value;
    }

    private IASTTypeId [] typeIds = null;
    private int typeIdsPos=-1;

    public IASTTypeId[] getExceptionSpecification() {
        if( typeIds == null ) return IASTTypeId.EMPTY_TYPEID_ARRAY;
        typeIds = (IASTTypeId[]) ArrayUtil.removeNullsAfter( IASTTypeId.class, typeIds, typeIdsPos );
        return typeIds;
    }


    public void addExceptionSpecificationTypeId(IASTTypeId typeId) {
    	if (typeId != null) {
    		typeIds = (IASTTypeId[]) ArrayUtil.append( IASTTypeId.class, typeIds, ++typeIdsPos, typeId );
    		typeId.setParent(this);
			typeId.setPropertyInParent(EXCEPTION_TYPEID);
    	}
    }


    public boolean isPureVirtual() {
        return pureVirtual;
    }


    public void setPureVirtual(boolean isPureVirtual) {
        this.pureVirtual = isPureVirtual;
    }


    private ICPPASTConstructorChainInitializer [] constructorChain = null;
    private int constructorChainPos=-1;

 
    public ICPPASTConstructorChainInitializer[] getConstructorChain() {
        if( constructorChain == null ) return ICPPASTConstructorChainInitializer.EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY;
        constructorChain = (ICPPASTConstructorChainInitializer[]) ArrayUtil.removeNullsAfter( ICPPASTConstructorChainInitializer.class, constructorChain, constructorChainPos );
        return constructorChain;
    }


    public void addConstructorToChain(ICPPASTConstructorChainInitializer initializer) {
    	if (initializer != null) {
    		constructorChain = (ICPPASTConstructorChainInitializer[]) ArrayUtil.append(ICPPASTConstructorChainInitializer.class, constructorChain, ++constructorChainPos, initializer );
    		initializer.setParent(this);
			initializer.setPropertyInParent(CONSTRUCTOR_CHAIN_MEMBER);
    	}
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
