/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 14, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPTemplateDefinition implements ICPPTemplateDefinition {
	private IASTDeclaration primaryDecl;
	private IASTName name;
	
	public CPPTemplateDefinition( IASTDeclaration decl ) {
		primaryDecl = decl;
		name = getTemplateName( decl );
	}
	
	private IASTName getTemplateName( IASTDeclaration decl ){
		if( decl instanceof IASTSimpleDeclaration ){
			IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
			if( dtors.length > 0 )
				return dtors[0].getName();
			
			IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)decl).getDeclSpecifier();
			if( declSpec instanceof ICPPASTCompositeTypeSpecifier )
				return ((ICPPASTCompositeTypeSpecifier)declSpec).getName();
		} else if( decl instanceof IASTFunctionDefinition ){
			return ((IASTFunctionDefinition)decl).getDeclarator().getName();
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope( primaryDecl );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplate#instantiate(org.eclipse.cdt.core.dom.ast.IASTNode[])
	 */
	public IBinding instantiate(ICPPASTTemplateId templateId ) {//IASTNode[] arguments) {
		IBinding decl = getTemplatedDeclaration();
		ICPPTemplateParameter [] params = getParameters();
		IASTNode [] arguments = templateId.getTemplateArguments();
		
		ObjectMap map = new ObjectMap(params.length);
		if( arguments.length == params.length ){
			for( int i = 0; i < arguments.length; i++ ){
				IType t = CPPVisitor.createType( arguments[i] );
				map.put( params[i], t );
			}
		}
		
		return CPPTemplates.createInstance( templateId, (ICPPScope) getScope(), decl, map );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplate#getTemplatedDeclaration()
	 */
	public IBinding getTemplatedDeclaration() {
		if( primaryDecl instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simple = (IASTSimpleDeclaration) primaryDecl;
			if( simple.getDeclarators().length == 0 && simple.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier ){
				IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) simple.getDeclSpecifier();
				return compSpec.getName().resolveBinding();
			}
		} else if( primaryDecl instanceof IASTFunctionDefinition ){
			
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
	 */
	public String[] getQualifiedName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
	 */
	public char[][] getQualifiedNameCharArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	public boolean isGloballyQualified() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition#getParameters()
	 */
	public ICPPTemplateParameter[] getParameters() {
		ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) primaryDecl.getParent();
		ICPPASTTemplateParameter [] params = template.getTemplateParameters();
		ICPPTemplateParameter p = null;
		ICPPTemplateParameter [] result = null;
		for (int i = 0; i < params.length; i++) {
			if( params[i] instanceof ICPPASTSimpleTypeTemplateParameter ){
				p = (ICPPTemplateParameter) ((ICPPASTSimpleTypeTemplateParameter)params[i]).getName().resolveBinding();
			} else if( params[i] instanceof ICPPASTParameterDeclaration ) {
				p = (ICPPTemplateParameter) ((ICPPASTParameterDeclaration)params[i]).getDeclarator().getName().resolveBinding();
			} else if( params[i] instanceof ICPPASTTemplatedTypeTemplateParameter ){
				p = (ICPPTemplateParameter) ((ICPPASTTemplatedTypeTemplateParameter)params[i]).getName().resolveBinding();
			}
			
			if( p != null ){
				result = (ICPPTemplateParameter[]) ArrayUtil.append( ICPPTemplateParameter.class, result, p );
			}
		}
		return (ICPPTemplateParameter[]) ArrayUtil.trim( ICPPTemplateParameter.class, result );
	}

}
