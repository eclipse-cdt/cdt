/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 31, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CPPMethodTemplate extends CPPFunctionTemplate implements
		ICPPMethod {

	/**
	 * @param name
	 */
	public CPPMethodTemplate(IASTName name) {
		super(name);
	}

	public IASTDeclaration getPrimaryDeclaration() throws DOMException{
		//first check if we already know it
		if( declarations != null ){
			for( int i = 0; i < declarations.length; i++ ){
			    IASTNode parent = declarations[i].getParent();
			    while( !(parent instanceof IASTDeclaration) )
			        parent = parent.getParent();

			    IASTDeclaration decl = (IASTDeclaration) parent.getParent();
				if( decl instanceof ICPPASTCompositeTypeSpecifier )
					return decl;
			}
		}
		
		char [] myName = getNameCharArray();
		
		IScope scope = getScope();
		if( scope instanceof ICPPTemplateScope )
		    scope = scope.getParent();
		ICPPClassScope clsScope = (ICPPClassScope) scope;
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) clsScope.getPhysicalNode();
		IASTDeclaration [] members = compSpec.getMembers();
		for( int i = 0; i < members.length; i++ ){
		    if( members[i] instanceof ICPPASTTemplateDeclaration ){
		        IASTDeclaration decl = ((ICPPASTTemplateDeclaration)members[i]).getDeclaration();
		        if( decl instanceof IASTSimpleDeclaration ){
					IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
					for( int j = 0; j < dtors.length; j++ ){
						IASTName name = CPPVisitor.getMostNestedDeclarator( dtors[j] ).getName();
						if( CharArrayUtils.equals( name.toCharArray(), myName ) &&
							name.resolveBinding() == this )
						{
							return members[i];
						}
					}
				} else if( decl instanceof IASTFunctionDefinition ){
					IASTName name = CPPVisitor.getMostNestedDeclarator( ((IASTFunctionDefinition) decl).getDeclarator() ).getName();
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod#isVirtual()
     */
    public boolean isVirtual() throws DOMException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction#isInline()
     */
    public boolean isInline() throws DOMException {
        IASTDeclaration decl = getPrimaryDeclaration();
        if( decl instanceof ICPPASTTemplateDeclaration && ((ICPPASTTemplateDeclaration)decl).getDeclaration() instanceof IASTFunctionDefinition )
            return true;

        return super.isInline();
    }

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod#isDestructor()
     */
	public boolean isDestructor() throws DOMException {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;
		
		return false;
	}

}
