/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * The binding for a method.
 */
public class CPPMethod extends CPPFunction implements ICPPMethod {
	public CPPMethod(IASTDeclarator declarator) {
		super(declarator);
	}

	public IASTDeclaration getPrimaryDeclaration() {
		//first check if we already know it
		if (declarations != null) {
			for (IASTDeclarator dtor : declarations) {
				if (dtor == null) {
					break;
				}
				dtor = ASTQueries.findOutermostDeclarator(dtor);
				IASTDeclaration decl = (IASTDeclaration) dtor.getParent();
				if (decl.getParent() instanceof ICPPASTCompositeTypeSpecifier)
					return decl;
			}
		}
		if (definition != null) {
			IASTDeclarator dtor = ASTQueries.findOutermostDeclarator(definition);
			IASTDeclaration decl = (IASTDeclaration) dtor.getParent();
			if (decl.getParent() instanceof ICPPASTCompositeTypeSpecifier)
				return decl;
		}

		final char[] myName = getASTName().getLookupKey();
		ICPPClassScope scope = (ICPPClassScope) getScope();
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) ASTInternal.getPhysicalNodeOfScope(scope);
		if (compSpec != null) {
			IASTDeclaration [] members = compSpec.getMembers();
			for (IASTDeclaration member : members) {
				if (member instanceof IASTSimpleDeclaration) {
					IASTDeclarator[] dtors = ((IASTSimpleDeclaration) member).getDeclarators();
					for (IASTDeclarator dtor : dtors) {
						IASTName name = ASTQueries.findInnermostDeclarator(dtor).getName();
						if (CharArrayUtils.equals(name.getLookupKey(), myName) && name.resolveBinding() == this) {
							return member;
						}
					}
				} else if (member instanceof IASTFunctionDefinition) {
					final IASTFunctionDeclarator declarator = ((IASTFunctionDefinition) member).getDeclarator();
					IASTName name = ASTQueries.findInnermostDeclarator(declarator).getName();
					if (CharArrayUtils.equals(name.getLookupKey(), myName) && name.resolveBinding() == this) {
						return member;
					}
				}
			}
		}
		return null;
	}

	@Override
	public int getVisibility() {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl == null) {
			IScope scope = getScope();
			if (scope instanceof ICPPClassScope) {
				ICPPClassType cls = ((ICPPClassScope)scope).getClassType();
				if (cls != null)
					return (cls.getKey() == ICPPClassType.k_class) ? ICPPASTVisibilityLabel.v_private : ICPPASTVisibilityLabel.v_public;
			}
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

	@Override
	public ICPPClassType getClassOwner() {
		ICPPClassScope scope = (ICPPClassScope)getScope();
		return scope.getClassType();
	}

	@Override
	protected IASTName getASTName() {
		IASTDeclarator dtor= (declarations != null && declarations.length > 0) ? declarations[0] : definition;
		dtor= ASTQueries.findInnermostDeclarator(dtor);
	    IASTName name= dtor.getName();
	    if (name instanceof ICPPASTQualifiedName) {
	        name = name.getLastName();
	    }
	    return name;
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(getASTName());
	}

    @Override
	public boolean isVirtual() {
    	IASTDeclaration decl = getPrimaryDeclaration();
		if (decl != null) {
			ICPPASTDeclSpecifier declSpec = getDeclSpec(decl);
			if (declSpec != null) {
				return declSpec.isVirtual();
			}
		}
        return false;
    }

	protected ICPPASTDeclSpecifier getDeclSpec(IASTDeclaration decl) {
		ICPPASTDeclSpecifier declSpec = null;
		if (decl instanceof IASTSimpleDeclaration) {
			declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)decl).getDeclSpecifier();
		} else if (decl instanceof IASTFunctionDefinition) {
			declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)decl).getDeclSpecifier();
		}
		return declSpec;
	}

    @Override
	public boolean isInline() {
        IASTDeclaration decl = getPrimaryDeclaration();
        if (decl instanceof IASTFunctionDefinition)
            return true;
		if (decl == null)
			return false;

        IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)decl).getDeclSpecifier();
        return declSpec.isInline();
    }

    @Override
	public boolean isMutable() {
        return hasStorageClass(this, IASTDeclSpecifier.sc_mutable);
    }

	@Override
	public boolean isStatic(boolean resolveAll) {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl != null) {
			ICPPASTDeclSpecifier declSpec = getDeclSpec(decl);
			if (declSpec != null) {
				return declSpec.getStorageClass() == IASTDeclSpecifier.sc_static;
			}
		}
		return false;
	}

	@Override
	public boolean isDestructor() {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;

		return false;
	}

	@Override
	public boolean isImplicit() {
		return false;
	}

    @Override
	public boolean isPureVirtual() {
		ICPPASTFunctionDeclarator declarator = findFunctionDeclarator();
    	if(declarator != null){
    		return declarator.isPureVirtual();
    	}
    	return false;
    }

    @Override
    public boolean isFinal() {
    	ICPPASTFunctionDeclarator declarator = findFunctionDeclarator();
    	if(declarator != null){
    		return declarator.isFinal();
    	}
    	return false;
    }

    @Override
    public boolean isOverride() {
    	ICPPASTFunctionDeclarator declarator = findFunctionDeclarator();
    	if(declarator != null){
    		return declarator.isOverride();
    	}
    	return false;
    }

    private ICPPASTFunctionDeclarator findFunctionDeclarator(){
    	if (declarations != null) {
			for (IASTDeclarator dtor : declarations) {
				if (dtor == null)
					break;

				dtor = ASTQueries.findOutermostDeclarator(dtor);
				IASTDeclaration decl = (IASTDeclaration) dtor.getParent();
				if (decl.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
					dtor= ASTQueries.findTypeRelevantDeclarator(dtor);
					if (dtor instanceof ICPPASTFunctionDeclarator) {
						return (ICPPASTFunctionDeclarator) dtor;
					}
				}
			}
		}
    	return definition;
    }

    @Override
	public boolean isExplicit() {
    	IASTDeclaration decl= getPrimaryDeclaration();
    	if (decl != null) {
    		ICPPASTDeclSpecifier declspec= getDeclSpec(decl);
    		if (declspec != null) {
    			return declspec.isExplicit();
    		}
    	}
        return false;
    }
}
