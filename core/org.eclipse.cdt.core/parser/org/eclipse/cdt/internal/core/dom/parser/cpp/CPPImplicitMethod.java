/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Binding for implicit methods, base class for implicit constructors.
 */
public class CPPImplicitMethod extends CPPImplicitFunction implements ICPPMethod {
    
    public CPPImplicitMethod(ICPPClassScope scope, char[] name, ICPPFunctionType type, ICPPParameter[] params) {
		super(name, scope, type, params, false);
	}
   
	@Override
	public int getVisibility() {
		IASTDeclaration decl= getPrimaryDeclaration();
		if (decl == null) {
			// 12.1-5, 12.8-10 Implicit constructors and assignment operators are public
			return ICPPASTVisibilityLabel.v_public;
		}
		
		IASTNode parent= decl.getParent();
		while (parent instanceof ICPPASTTemplateDeclaration) {
			decl= (ICPPASTTemplateDeclaration) parent;
			parent= parent.getParent();
		}
		if (parent instanceof IASTCompositeTypeSpecifier) {
			IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
			IASTDeclaration [] members = cls.getMembers();
			ICPPASTVisibilityLabel vis = null;
			for (IASTDeclaration member : members) {
				if( member instanceof ICPPASTVisibilityLabel )
					vis = (ICPPASTVisibilityLabel) member;
				else if( member == decl )
					break;
			}
			if( vis != null ){
				return vis.getVisibility();
			} else if( cls.getKey() == ICPPASTCompositeTypeSpecifier.k_class ){
				return ICPPASTVisibilityLabel.v_private;
			} 
		}
        return ICPPASTVisibilityLabel.v_public;
    }
	
	@Override
	public ICPPClassType getClassOwner() {
		ICPPClassScope scope = (ICPPClassScope)getScope();
		return scope.getClassType();
	}
	
	public IASTDeclaration getPrimaryDeclaration() {
		// first check if we already know it
		if (declarations != null) {
			for (IASTDeclarator dtor : declarations) {
				if (dtor == null)
					break;

				IASTDeclaration decl = (IASTDeclaration) ASTQueries.findOutermostDeclarator(dtor).getParent();
				IASTNode parent= decl.getParent();
				while (parent instanceof ICPPASTTemplateDeclaration)
					parent= parent.getParent();
				if (parent instanceof ICPPASTCompositeTypeSpecifier)
					return decl;
			}
		}

		IFunctionType ftype = getType();
		IType[] params = ftype.getParameterTypes();

		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) ASTInternal
				.getPhysicalNodeOfScope(getScope());
		if (compSpec == null) {
			return null;
		}
		IASTDeclaration[] members = compSpec.getMembers();
		for (IASTDeclaration member : members) {
			IASTDeclarator[] ds = null;
			while (member instanceof ICPPASTTemplateDeclaration)
				member = ((ICPPASTTemplateDeclaration) member).getDeclaration();
			
			if (member instanceof IASTSimpleDeclaration) {
				ds = ((IASTSimpleDeclaration) member).getDeclarators();
			} else if (member instanceof IASTFunctionDefinition) {
				ds = new IASTDeclarator[] {((IASTFunctionDefinition) member).getDeclarator()};
			} else {
				continue;
			}
			
			for (IASTDeclarator dtor : ds) {
				IASTName name = ASTQueries.findInnermostDeclarator(dtor).getName();
				if (ASTQueries.findTypeRelevantDeclarator(dtor) instanceof ICPPASTFunctionDeclarator
						&& CharArrayUtils.equals(name.getLookupKey(), getNameCharArray())) {
					IType t0 = CPPVisitor.createType(dtor);
					boolean ok = false;
					if (t0 instanceof IFunctionType) {
						IFunctionType t = (IFunctionType) t0;
						IType[] ps = t.getParameterTypes();
						if (ps.length == params.length) {
							int idx = 0;
							for (; idx < ps.length && ps[idx] != null; idx++) {
								if (!ps[idx].isSameType(params[idx]))
									break;
							}
							ok = idx == ps.length;
						} else if (ps.length == 0) {
							if (params.length == 1) {
								ok = SemanticUtil.isVoidType(params[0]);
							}
						}
					} else {
						ok = false;
					}
					if (ok) {
						name.setBinding(this);
						if (member instanceof IASTSimpleDeclaration)
							ASTInternal.addDeclaration(this, dtor);
						else if (member instanceof IASTFunctionDefinition)
							ASTInternal.addDefinition(this, dtor);
						return member;
					}
				}
			}
		}
		return null;
	}

    @Override
	public boolean isVirtual() {
        return false;
    }

	@Override
	public boolean isDestructor() {
		char [] n = getNameCharArray();
		if( n != null && n.length > 0 )
			return n[0] == '~';
		return false;
	}

	@Override
	public boolean isImplicit() {
		return getPrimaryDeclaration() == null;
	}
	
    @Override
	public boolean isExplicit() {
        return false;
    }
	
	@Override
	public boolean isPureVirtual() {
		return false;
	}

	@Override
	public IBinding getOwner() {
		return getClassOwner();
	}

	@Override
	public IType[] getExceptionSpecification() {
		return ClassTypeHelper.getInheritedExceptionSpecification(this);
	}
}
