/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CPPField extends CPPVariable implements ICPPField, ICPPInternalBinding {
    public static class CPPFieldDelegate extends CPPVariable.CPPVariableDelegate implements ICPPField {
        public CPPFieldDelegate( IASTName name, ICPPField binding ) {
            super( name, binding );
        }
        public int getVisibility() throws DOMException {
            return ((ICPPField)getBinding()).getVisibility();
        }
        public ICPPClassType getClassOwner() throws DOMException {
        	return ((ICPPField)getBinding()).getClassOwner();
        }
    }
    public static class CPPFieldProblem extends CPPVariable.CPPVariableProblem implements ICPPField {
        /**
         * @param id
         * @param arg
         */
        public CPPFieldProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

        public int getVisibility() throws DOMException {
            throw new DOMException( this );
        }
        public ICPPClassType getClassOwner() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
    }
    
	public CPPField( IASTName name ){
		super( name );
	}

	public IASTDeclaration getPrimaryDeclaration() throws DOMException{
		//first check if we already know it
	    IASTName [] declarations = (IASTName[]) getDeclarations();
		if( declarations != null || getDefinition() != null ){
			int len = ( declarations != null ) ? declarations.length : 0;
			for( int i = -1; i < len; i++ ){
				IASTNode node = ( i == -1 ) ? getDefinition() : declarations[i];
				if( node != null ){
					while( !(node instanceof IASTDeclaration ) )
						node = node.getParent();
					if( node.getParent() instanceof ICPPASTCompositeTypeSpecifier )
						return (IASTDeclaration) node;
				}
			}
		}
		
		char [] myName = getNameCharArray();
		
		ICPPClassScope scope = (ICPPClassScope) getScope();
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) scope.getPhysicalNode();
		IASTDeclaration [] members = compSpec.getMembers();
		for( int i = 0; i < members.length; i++ ){
			if( members[i] instanceof IASTSimpleDeclaration ){
				IASTDeclarator [] dtors = ((IASTSimpleDeclaration)members[i]).getDeclarators();
				for( int j = 0; j < dtors.length; j++ ){
					IASTName name = dtors[j].getName();
					if( CharArrayUtils.equals( name.toCharArray(), myName ) &&
						name.resolveBinding() == this )
					{
						return members[i];
					}
				}
			}
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMember#getVisibility()
	 */
	public int getVisibility() throws DOMException {
		ICPPASTVisiblityLabel vis = null;
		IASTDeclaration decl = getPrimaryDeclaration();
		if( decl != null ) {
			IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
			IASTDeclaration [] members = cls.getMembers();
			
			for( int i = 0; i < members.length; i++ ){
				if( members[i] instanceof ICPPASTVisiblityLabel )
					vis = (ICPPASTVisiblityLabel) members[i];
				else if( members[i] == decl )
					break;
			}
		
			if( vis != null ){
				return vis.getVisibility();
			} else if( cls.getKey() == ICPPASTCompositeTypeSpecifier.k_class ){
				return ICPPASTVisiblityLabel.v_private;
			}
		}
		return ICPPASTVisiblityLabel.v_public;
	}
	
	public ICPPClassType getClassOwner() throws DOMException {
		ICPPClassScope scope = (ICPPClassScope) getScope();
		return scope.getClassType();
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable#isMutable()
     */
    public boolean isMutable() {
        return hasStorageClass( ICPPASTDeclSpecifier.sc_mutable );
    }
    
    public boolean isExtern() {
        //7.1.1-5 The extern specifier can not be used in the declaration of class members
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPFieldDelegate( name, this );
    }
}
