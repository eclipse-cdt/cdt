/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * /
 *******************************************************************************/
/*
 * Created on Apr 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 *
 */
public class CPPMethodSpecialization extends CPPFunctionSpecialization
		implements ICPPMethod {

	public CPPMethodSpecialization(IBinding orig, ICPPScope scope, ObjectMap argMap ) {
		super(orig, scope, argMap );
	}

	public boolean isVirtual() throws DOMException {
		ICPPMethod f = (ICPPMethod) getSpecializedBinding();
		if( f != null )
			return f.isVirtual();
		IASTNode definition = getDefinition();
		if( definition != null ){
			IASTNode node = definition.getParent();
			while( node instanceof IASTDeclarator )
				node = node.getParent();
			
			ICPPASTDeclSpecifier declSpec = null;
			if( node instanceof IASTSimpleDeclaration )
				declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)node).getDeclSpecifier();
			else if( node instanceof IASTFunctionDefinition )
				declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)node).getDeclSpecifier();
			
			if( declSpec != null ){
				return declSpec.isVirtual();
			}
		}
		return false;
	}

	public int getVisibility() throws DOMException {
		ICPPMethod f = (ICPPMethod) getSpecializedBinding();
		if( f != null )
			return f.getVisibility();
		return 0;
	}

	public ICPPClassType getClassOwner() throws DOMException {
		ICPPMethod f = (ICPPMethod) getSpecializedBinding();
		if( f != null )
			return f.getClassOwner();
		return null;
	}
	
	public boolean isDestructor() {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;
		
		return false;
	}

}
