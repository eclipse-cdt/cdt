/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Binding for a non-type template parameter.
 */
public class CPPTemplateNonTypeParameter extends CPPTemplateParameter implements
		ICPPTemplateNonTypeParameter {

	private IType type = null;
	
	public CPPTemplateNonTypeParameter(IASTName name) {
		super(name);
	}

	public IASTExpression getDefault() {
		IASTInitializerClause def= getDefaultClause();
		if (def instanceof IASTExpression) {
			return (IASTExpression) def;
		}
		
		return null;
	}
	
	public IASTInitializerClause getDefaultClause() {
		IASTName[] nds = getDeclarations();
		if (nds == null || nds.length == 0)
		    return null;
		
		for (IASTName name : nds) {
			if (name != null) {
				IASTNode parent = name.getParent();
				assert parent instanceof IASTDeclarator;
				if (parent instanceof IASTDeclarator) {
					IASTDeclarator dtor = (IASTDeclarator) parent;
					IASTInitializer initializer = dtor.getInitializer();
					if (initializer instanceof IASTEqualsInitializer) {
						return ((IASTEqualsInitializer) initializer).getInitializerClause();
					}
				}
			}
		}
		return null;
	}
	
	public ICPPTemplateArgument getDefaultValue() {
		IASTInitializerClause dc= getDefault();
		IASTExpression d= null;
		if (dc instanceof IASTExpression) {
			d= (IASTExpression) dc;
		} else if (dc instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList list= (ICPPASTInitializerList) dc;
			switch(list.getSize()) {
			case 0:
				return new CPPTemplateArgument(Value.create(0), getType());
			case 1:
				dc= list.getClauses()[0];
				if (dc instanceof IASTExpression) {
					d= (IASTExpression) dc;
				}
			}
		}
		
		if (d == null)
			return null;
		
		IValue val= Value.create(d, Value.MAX_RECURSION_DEPTH);
		IType t= getType();
		return new CPPTemplateArgument(val, t);
	}

	public IType getType() {
		if (type == null) {
			IASTNode parent= getPrimaryDeclaration().getParent();
			while (parent != null) {
				if (parent instanceof ICPPASTParameterDeclaration) {
					type= CPPVisitor.createParameterType((ICPPASTParameterDeclaration) parent, true);
					break;
				}
				parent= parent.getParent();
			}
		}
		return type;
	}

	public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
	}

	public boolean isStatic() {
		return false;
	}
	public boolean isExtern() {
		return false;
	}
	public boolean isAuto() {
		return false;
	}
	public boolean isRegister() {
		return false;
	}
	public IValue getInitialValue() {
		return null;
	}
	public boolean isExternC() {
		return false;
	}
	public boolean isMutable() {
		return false;
	}
}
