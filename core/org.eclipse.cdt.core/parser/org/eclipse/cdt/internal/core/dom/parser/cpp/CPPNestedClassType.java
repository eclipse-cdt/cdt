/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNestedClassType;

/**
 * Binding for a nested class type.
 */
public class CPPNestedClassType extends CPPClassType implements ICPPNestedClassType {

	public CPPNestedClassType(IASTName name, IBinding indexBinding) {
		super(name, indexBinding);
	}

	@Override
	public int getVisibility() {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl == null) {
			ICPPClassType cls = getClassOwner();
			if (cls != null)
				return getDefaultVisibility(cls);
			return ICPPASTVisibilityLabel.v_private;
		}

		IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
		IASTDeclaration [] members = cls.getMembers();
		ICPPASTVisibilityLabel vis = null;
		for (IASTDeclaration member : members) {
			if (member instanceof ICPPASTVisibilityLabel) {
				vis = (ICPPASTVisibilityLabel) member;
			} else if (member == decl) {
				break;
			}
		}
		if (vis != null) {
			return vis.getVisibility();
		} else if (cls.getKey() == ICPPASTCompositeTypeSpecifier.k_class) {
			return ICPPASTVisibilityLabel.v_private;
		}
		return ICPPASTVisibilityLabel.v_public;
	}
	
	public IASTDeclaration getPrimaryDeclaration() {
		IASTNode[] declarators = getDeclarations();
		if (declarators != null) {
			for (IASTNode dtor : declarators) {
				IASTDeclaration typeDeclaration = getDeclarationInCompositeSpecifier(dtor);
				if (typeDeclaration != null) {
					return typeDeclaration;
				}
			}
		}
		
		IASTNode declarator = getDefinition();
		if (declarator != null) {
			IASTDeclaration typeDeclaration = getDeclarationInCompositeSpecifier(declarator);
			if(typeDeclaration != null){
				return typeDeclaration;
			}
		}
		return null;
	}
	
	private IASTDeclaration getDeclarationInCompositeSpecifier(IASTNode dtor) {
		if (dtor != null) {
			IASTNode declarationSpecifier = dtor.getParent();
			if (declarationSpecifier != null) {
				IASTNode simpleDeclaration = declarationSpecifier.getParent();
				if (simpleDeclaration instanceof IASTDeclaration) {
					if (simpleDeclaration.getParent() instanceof ICPPASTCompositeTypeSpecifier)
						return (IASTDeclaration) simpleDeclaration;
				}
			}
		}
		return null;
	}

	@Override
	public ICPPClassType getClassOwner() {
		ICPPClassScope scope = (ICPPClassScope) getScope();
		return scope.getClassType();
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public IType getType() throws DOMException {
		return this;
	}

	
	private int getDefaultVisibility(ICompositeType compositeType){
		if(compositeType.getKey() == ICPPClassType.k_class){
			return ICPPMember.v_private;
		}
		return ICPPMember.v_public;
	}
}
