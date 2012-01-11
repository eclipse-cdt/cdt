/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Represents a function declarator.
 */
public class CPPASTFunctionDeclarator extends CPPASTDeclarator implements ICPPASTFunctionDeclarator,
		IASTAmbiguityParent {
    private ICPPASTParameterDeclaration[] parameters = null;
    private IASTTypeId[] typeIds = NO_EXCEPTION_SPECIFICATION;
    private IASTTypeId trailingReturnType= null;
    
    private boolean varArgs;
    private boolean pureVirtual;
    private boolean isVolatile;
    private boolean isConst;
    private boolean isMutable;
    
    private ICPPFunctionScope scope = null;
    
    public CPPASTFunctionDeclarator() {
	}

	public CPPASTFunctionDeclarator(IASTName name) {
		super(name);
	}
	
	@Override
	public CPPASTFunctionDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFunctionDeclarator copy(CopyStyle style) {
		CPPASTFunctionDeclarator copy = new CPPASTFunctionDeclarator();
		copyBaseDeclarator(copy, style);
		copy.varArgs = varArgs;
		copy.pureVirtual = pureVirtual;
		copy.isVolatile = isVolatile;
		copy.isConst = isConst;
		copy.isMutable = isMutable;

		for (IASTParameterDeclaration param : getParameters())
			copy.addParameterDeclaration(param == null ? null : param.copy(style));
		for (IASTTypeId typeId : getExceptionSpecification())
			copy.addExceptionSpecificationTypeId(typeId == null ? null : typeId.copy(style));
		if (trailingReturnType != null) {
			copy.setTrailingReturnType(trailingReturnType.copy(style));
		}
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public ICPPASTParameterDeclaration[] getParameters() {
        if (parameters == null) 
        	return ICPPASTParameterDeclaration.EMPTY_CPPPARAMETERDECLARATION_ARRAY;
        
        return parameters= ArrayUtil.trim(parameters);
    }

    @Override
	public void addParameterDeclaration(IASTParameterDeclaration parameter) {
        assertNotFrozen();
    	if (parameter != null) {
    		parameter.setParent(this);
			parameter.setPropertyInParent(FUNCTION_PARAMETER);
    		parameters = ArrayUtil.append(ICPPASTParameterDeclaration.class, parameters, (ICPPASTParameterDeclaration) parameter);
    	}
    }

    @Override
	public boolean takesVarArgs() {
        return varArgs;
    }

    @Override
	public void setVarArgs(boolean value) {
        assertNotFrozen();
        varArgs = value;
    }

    @Override
	public boolean isConst() {
        return isConst;
    }

    @Override
	public void setConst(boolean value) {
        assertNotFrozen();
        this.isConst = value;
    }

    @Override
	public boolean isVolatile() {
        return isVolatile;
    }

    @Override
	public void setVolatile(boolean value) {
        assertNotFrozen();
        this.isVolatile = value;
    }

    @Override
	public boolean isMutable() {
        return isMutable;
    }

    @Override
	public void setMutable(boolean value) {
        assertNotFrozen();
        this.isMutable = value;
    }

    @Override
	public IASTTypeId[] getExceptionSpecification() {
        return typeIds= ArrayUtil.trim(typeIds);
    }
    
    @Override
	public void setEmptyExceptionSpecification() {
        assertNotFrozen();
    	typeIds= IASTTypeId.EMPTY_TYPEID_ARRAY;
    }

    @Override
	public void addExceptionSpecificationTypeId(IASTTypeId typeId) {
        assertNotFrozen();
    	if (typeId != null) {
    		assert typeIds != null;
    		typeIds = ArrayUtil.append(typeIds, typeId);
    		typeId.setParent(this);
			typeId.setPropertyInParent(EXCEPTION_TYPEID);
    	}
    }
    
    @Override
	public IASTTypeId getTrailingReturnType() {
		return trailingReturnType;
	}

	@Override
	public void setTrailingReturnType(IASTTypeId typeId) {
		assertNotFrozen();
		trailingReturnType= typeId;
		if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TRAILING_RETURN_TYPE);
		}
	}

	@Override
	public boolean isPureVirtual() {
        return pureVirtual;
    }

    @Override
	public void setPureVirtual(boolean isPureVirtual) {
        assertNotFrozen();
        this.pureVirtual = isPureVirtual;
    }

    @Override
	@Deprecated
    public org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer[] getConstructorChain() {
    	if (ASTQueries.findTypeRelevantDeclarator(this) == this) {
    		IASTNode parent= getParent();
    		while (!(parent instanceof IASTDeclaration)) {
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

    @Override
	@Deprecated
    public void addConstructorToChain(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer initializer) {
        assertNotFrozen();
    }

    @Override
	public ICPPFunctionScope getFunctionScope() {
        if (scope != null)
            return scope;
        
        // introduce a scope for function declarations and definitions, only.
        IASTNode node= getParent();
        while (!(node instanceof IASTDeclaration)) {
        	if (node==null)
        		return null;
        	node= node.getParent();
        }
        if (node instanceof IASTParameterDeclaration)
        	return null;
        
        if (ASTQueries.findTypeRelevantDeclarator(this) == this) {
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
		
		if (trailingReturnType != null && !trailingReturnType.accept(action)) 
			return false;

		return super.postAccept(action);
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (parameters != null) {
			for (int i = 0; i < parameters.length; ++i) {
				if (child == parameters[i]) {
					other.setPropertyInParent(child.getPropertyInParent());
					other.setParent(child.getParent());
					parameters[i] = (ICPPASTParameterDeclaration) other;
					return;
				}
			}
		}
		assert false;
	}
}
