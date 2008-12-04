/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Represents a function declarator.
 */
public class CPPASTFunctionDeclarator extends CPPASTDeclarator implements ICPPASTFunctionDeclarator {
    private IASTParameterDeclaration[] parameters = null;
    private int parametersPos = -1;
    private IASTTypeId[] typeIds = NO_EXCEPTION_SPECIFICATION;
    private int typeIdsPos = -1;
    
    private boolean varArgs;
    private boolean pureVirtual;
    private boolean isVolatile;
    private boolean isConst;
    
    private ICPPFunctionScope scope = null;
    
    public CPPASTFunctionDeclarator() {
	}

	public CPPASTFunctionDeclarator(IASTName name) {
		super(name);
	}

	public IASTParameterDeclaration[] getParameters() {
        if (parameters == null) 
        	return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        
        return parameters= ArrayUtil.trimAt(IASTParameterDeclaration.class, parameters, parametersPos);
    }

    public void addParameterDeclaration(IASTParameterDeclaration parameter) {
        assertNotFrozen();
    	if (parameter != null) {
    		parameter.setParent(this);
			parameter.setPropertyInParent(FUNCTION_PARAMETER);
    		parameters = (IASTParameterDeclaration[]) ArrayUtil.append(IASTParameterDeclaration.class, parameters, ++parametersPos, parameter);
    	}
    }

    public boolean takesVarArgs() {
        return varArgs;
    }

    public void setVarArgs(boolean value) {
        assertNotFrozen();
        varArgs = value;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean value) {
        assertNotFrozen();
        this.isConst = value;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setVolatile(boolean value) {
        assertNotFrozen();
        this.isVolatile = value;
    }

    public IASTTypeId[] getExceptionSpecification() {
        return typeIds= ArrayUtil.trimAt(IASTTypeId.class, typeIds, typeIdsPos);
    }
    
    public void setEmptyExceptionSpecification() {
        assertNotFrozen();
    	typeIds= IASTTypeId.EMPTY_TYPEID_ARRAY;
    }

    public void addExceptionSpecificationTypeId(IASTTypeId typeId) {
        assertNotFrozen();
    	if (typeId != null) {
    		typeIds = (IASTTypeId[]) ArrayUtil.append(IASTTypeId.class, typeIds, ++typeIdsPos, typeId);
    		typeId.setParent(this);
			typeId.setPropertyInParent(EXCEPTION_TYPEID);
    	}
    }

    public boolean isPureVirtual() {
        return pureVirtual;
    }

    public void setPureVirtual(boolean isPureVirtual) {
        assertNotFrozen();
        this.pureVirtual = isPureVirtual;
    }

    @Deprecated
    public org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer[] getConstructorChain() {
    	if (CPPVisitor.findTypeRelevantDeclarator(this) == this) {
    		IASTNode parent= getParent();
    		while(!(parent instanceof IASTDeclaration)) {
    			if (parent == null)
    				break;
    			parent= parent.getParent();
    		}
    		if (parent instanceof ICPPASTFunctionDefinition) {
    			return ((ICPPASTFunctionDefinition) parent).getMemberInitializers();
    		}
    	}
    	return org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer.EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY;
    }

    @Deprecated
    public void addConstructorToChain(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer initializer) {
        assertNotFrozen();
    }

    public ICPPFunctionScope getFunctionScope() {
        if (scope != null)
            return scope;
        
        // introduce a scope for function declarations and definitions, only.
        IASTNode node= getParent();
        while(!(node instanceof IASTDeclaration)) {
        	if (node==null)
        		return null;
        	node= node.getParent();
        }
        if (node instanceof IASTParameterDeclaration)
        	return null;
        
        if (CPPVisitor.findTypeRelevantDeclarator(this) == this) {
            scope = new CPPFunctionScope(this);
        }
        return scope;
    }
    
    @Override
	protected boolean postAccept(ASTVisitor action) {
		IASTParameterDeclaration[] params = getParameters();
		for (int i = 0; i < params.length; i++) {
			if (!params[i].accept(action))
				return false;
		}

		IASTTypeId[] ids = getExceptionSpecification();
		for (int i = 0; i < ids.length; i++) {
			if (!ids[i].accept(action))
				return false;
		}

		return super.postAccept(action);
	}
}
