/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * The specialization of a method in the context of a class-specialization.
 */
public class CPPMethodSpecialization extends CPPFunctionSpecialization
		implements ICPPMethod {

	public CPPMethodSpecialization(ICPPMethod orig, ICPPClassType owner, ICPPTemplateParameterMap argMap ) {
		super(orig, owner, argMap );
	}

	public boolean isVirtual() {
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

	public int getVisibility() {
		ICPPMethod f = (ICPPMethod) getSpecializedBinding();
		if( f != null )
			return f.getVisibility();
		return 0;
	}

	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}
	
	public boolean isDestructor() {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;
		
		return false;
	}

	public boolean isExplicit() {
		return ((ICPPMethod)getSpecializedBinding()).isExplicit();
	}

	public boolean isImplicit() {
		return ((ICPPMethod) getSpecializedBinding()).isImplicit();
	}

	public boolean isPureVirtual() {
		ICPPMethod f = (ICPPMethod) getSpecializedBinding();
		if (f != null)
			return f.isPureVirtual();

		return false;
	}

	@Override
	public IType[] getExceptionSpecification() {
		if (isImplicit()) {
			return ClassTypeHelper.getInheritedExceptionSpecification(this);
		}
		return super.getExceptionSpecification();
	}
}
