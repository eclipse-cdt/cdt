/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

/**
 * Models function declarators for plain c.
 */
public class CASTFunctionDeclarator extends CASTDeclarator implements IASTStandardFunctionDeclarator {

    private IASTParameterDeclaration [] parameters = null;
    private int parametersPos=-1;
    private boolean varArgs;
    private IScope scope;
    
    public CASTFunctionDeclarator() {
	}

	public CASTFunctionDeclarator(IASTName name) {
		super(name);
	}

	@Override
	public CASTFunctionDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTFunctionDeclarator copy(CopyStyle style) {
		CASTFunctionDeclarator copy = new CASTFunctionDeclarator();
		copyBaseDeclarator(copy, style);
		copy.varArgs = varArgs;
		
		for(IASTParameterDeclaration param : getParameters())
			copy.addParameterDeclaration(param == null ? null : param.copy(style));
		
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
	@Override
	public IASTParameterDeclaration[] getParameters() {
        if( parameters == null ) return IASTParameterDeclaration.EMPTY_PARAMETERDECLARATION_ARRAY;
        parameters = ArrayUtil.trimAt( IASTParameterDeclaration.class, parameters, parametersPos );
        return parameters;
    }

    @Override
	public void addParameterDeclaration(IASTParameterDeclaration parameter) {
        assertNotFrozen();
    	if (parameter != null) {
    		parameter.setParent(this);
			parameter.setPropertyInParent(FUNCTION_PARAMETER);
    		parameters = ArrayUtil.appendAt( IASTParameterDeclaration.class, parameters, ++parametersPos, parameter );
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
	protected boolean postAccept(ASTVisitor action) {
		IASTParameterDeclaration[] params = getParameters();
		for (int i = 0; i < params.length; i++) {
			if (!params[i].accept(action))
				return false;
		}
		return super.postAccept(action);
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
        if( parameters != null ) {
        	for (int i = 0; i < parameters.length; ++i) {
        		if (child == parameters[i]) {
        			other.setPropertyInParent(child.getPropertyInParent());
        			other.setParent(child.getParent());
        			parameters[i]= (IASTParameterDeclaration) other;
        			return;
        		}
        	}
        }
        super.replace(child, other);
	}
	
	@Override
	public IScope getFunctionScope() {
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
        
        if (node instanceof IASTFunctionDefinition) {
        	scope= ((IASTFunctionDefinition) node).getScope();
        } else if (ASTQueries.findTypeRelevantDeclarator(this) == this) {
            scope = new CScope(this, EScopeKind.eLocal);
        }
        return scope;
	}
}
