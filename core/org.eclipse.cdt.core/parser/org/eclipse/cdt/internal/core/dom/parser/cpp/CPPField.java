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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * Binding for a field.
 */
public class CPPField extends CPPVariable implements ICPPField {
    public static class CPPFieldProblem extends ProblemBinding implements ICPPField {
        private ICPPClassType fOwner;

		public CPPFieldProblem(ICPPClassType owner, IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
            fOwner= owner;
        }
        public int getVisibility() {
            return v_private;
        }
        public ICPPClassType getClassOwner() {
            return fOwner;
        }
		public ICompositeType getCompositeTypeOwner() {
			return getClassOwner();
		}
    }
    
	public CPPField( IASTName name ){
		super( name );
	}

	public IASTDeclaration getPrimaryDeclaration() {
		//first check if we already know it
		IASTDeclaration decl= findDeclaration(getDefinition());
		if (decl != null) {
			return decl;
		}
		
	    IASTName [] declarations = (IASTName[]) getDeclarations();
		if (declarations != null) {
			for (IASTName name : declarations) {
				decl= findDeclaration(name);
				if (decl != null) {
					return decl;
				}
			}
		}
		
		char [] myName = getNameCharArray();
		
		ICPPClassScope scope = (ICPPClassScope) getScope();
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) ASTInternal.getPhysicalNodeOfScope(scope);
		IASTDeclaration [] members = compSpec.getMembers();
		for (IASTDeclaration member : members) {
			if (member instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration) member).getDeclarators();
				for (IASTDeclarator dtor : dtors) {
					IASTName name = dtor.getName();
					if (CharArrayUtils.equals(name.getLookupKey(), myName) && name.resolveBinding() == this) {
						return member;
					}
				}
			}
		}
		return null;
	}

	private IASTDeclaration findDeclaration(IASTNode node) {
		while(node != null && node instanceof IASTDeclaration == false) {
			node = node.getParent();
		}
		if (node != null && node.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
			return (IASTDeclaration) node;
    	}
		return null;
	}

	public int getVisibility() {
		ICPPASTVisibilityLabel vis = null;
		IASTDeclaration decl = getPrimaryDeclaration();
		if( decl != null ) {
			IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
			IASTDeclaration [] members = cls.getMembers();
			
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
	
	public ICPPClassType getClassOwner() {
		ICPPClassScope scope = (ICPPClassScope) getScope();
		return scope.getClassType();
	}
	
    @Override
	public boolean isStatic() {
        // definition of a static field doesn't necessarily say static
		if (getDeclarations() == null) {
			IASTNode def= getDefinition();
			if (def instanceof ICPPASTQualifiedName) {
				return true;
			}
		}
		return super.isStatic();
	}
	

    @Override
	public boolean isMutable() {
        return hasStorageClass( IASTDeclSpecifier.sc_mutable);
    }
    
    @Override
	public boolean isExtern() {
        //7.1.1-5 The extern specifier can not be used in the declaration of class members
        return false;
    }
    
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}
}
