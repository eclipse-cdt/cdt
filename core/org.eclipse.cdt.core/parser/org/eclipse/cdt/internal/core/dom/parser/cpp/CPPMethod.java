/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 1, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CPPMethod extends CPPFunction implements ICPPMethod {

    public static class CPPMethodProblem extends CPPFunctionProblem implements ICPPMethod {
        /**
         * @param id
         * @param arg
         */
        public CPPMethodProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public int getVisibility() throws DOMException {
            throw new DOMException( this );
        }
    }
    
	public CPPMethod( ICPPASTFunctionDeclarator declarator ){
		super( declarator );
	}
	
	public IASTDeclaration getPrimaryDeclaration() throws DOMException{
		//first check if we already know it
		if( declarations != null ){
			for( int i = 0; i < declarations.length; i++ ){
				IASTDeclaration decl = (IASTDeclaration) declarations[i].getParent();
				if( decl.getParent() instanceof ICPPASTCompositeTypeSpecifier )
					return decl;
			}
		}
		
		char [] myName = ((IASTDeclarator)getPhysicalNode()).getName().toCharArray();
		
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
			} else if( members[i] instanceof IASTFunctionDefinition ){
				IASTName name = ((IASTFunctionDefinition) members[i]).getDeclarator().getName();
				if( CharArrayUtils.equals( name.toCharArray(), myName ) &&
					name.resolveBinding() == this )
				{
					return members[i];
				}
			}
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMember#getVisibility()
	 */
	public int getVisibility() throws DOMException {
		IASTDeclaration decl = getPrimaryDeclaration();
		IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
		IASTDeclaration [] members = cls.getMembers();
		ICPPASTVisiblityLabel vis = null;
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
		return ICPPASTVisiblityLabel.v_public;
	}

	public IScope getScope() {
		return CPPVisitor.getContainingScope( declarations != null ? declarations[0] : definition  );
	}
	
	public String getName() {
	    if( definition != null ){
	        IASTName n = definition.getName();
	        if( n instanceof ICPPASTQualifiedName ){
	            IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
	            return ns[ ns.length - 1 ].toString();
	        }
	        return n.toString();
	    }
		return declarations[0].getName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    if( definition != null ){
	        IASTName n = definition.getName();
	        if( n instanceof ICPPASTQualifiedName ){
	            IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
	            return ns[ ns.length - 1 ].toCharArray();
	        }
	        return n.toCharArray();
	    }
		return declarations[0].getName().toCharArray();
	}
}
