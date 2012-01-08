/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a c++ function parameter
 */
public class CPPLambdaExpressionParameter extends PlatformObject implements ICPPParameter {
	private IType fType = null;
	private IASTName fDeclaration = null;
	
	public CPPLambdaExpressionParameter(IASTName name) {
		fDeclaration = name;
	}
		
    @Override
	public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return fDeclaration.getSimpleID();
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(fDeclaration);
	}

	@Override
	public IType getType() {
		if (fType == null) {
			IASTNode parent= fDeclaration.getParent();
			while (parent != null) {
				if (parent instanceof ICPPASTParameterDeclaration) {
					fType= CPPVisitor.createType((ICPPASTParameterDeclaration) parent, false);
					break;
				}
				parent= parent.getParent();
			}
		}
		return fType;
	}

    @Override
	public boolean isStatic() {
        return false;
    }
    @Override
	public String[] getQualifiedName() {
        return new String[] { getName() };
    }
    @Override
	public char[][] getQualifiedNameCharArray() {
        return new char[][] { getNameCharArray() };
    }
    @Override
	public boolean isGloballyQualified() {
        return false;
    }

    @Override
	public boolean isExtern() {
        //7.1.1-5 extern can not be used in the declaration of a parameter
        return false;
    }

    @Override
	public boolean isMutable() {
        //7.1.1-8 mutable can only apply to class members
        return false;
    }

    @Override
	public boolean isAuto() {
        return hasStorageClass(IASTDeclSpecifier.sc_auto);
    }

    @Override
	public boolean isRegister() {
        return hasStorageClass(IASTDeclSpecifier.sc_register);
    }
    
    private boolean hasStorageClass(int storage) {
    	IASTNode parent = fDeclaration.getParent();
    	while (parent != null && !(parent instanceof IASTParameterDeclaration))
    		parent = parent.getParent();
    	if (parent != null) {
    		IASTDeclSpecifier declSpec = ((IASTParameterDeclaration) parent).getDeclSpecifier();
    		if (declSpec.getStorageClass() == storage)
    			return true;
    	}
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}
	
	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public boolean isExternC() {
		return false;
	}
	
	@Override
	public String toString() {
		String name = getName();
		return name.length() != 0 ? name : "<unnamed>"; //$NON-NLS-1$
	}
	
	@Override
	public IBinding getOwner() {
		IASTNode node= fDeclaration;
		while (node != null && !(node instanceof ICPPASTLambdaExpression))
			node= node.getParent();
		
		if (node instanceof ICPPASTLambdaExpression) {
			IType type= ((ICPPASTLambdaExpression) node).getExpressionType();
			if (type instanceof IBinding) {
				return (IBinding) type;
			}
		}
		return null;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
}
