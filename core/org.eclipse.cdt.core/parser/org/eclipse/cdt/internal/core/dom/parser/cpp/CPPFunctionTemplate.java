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
 * Created on Mar 31, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPFunctionTemplate extends CPPTemplateDefinition implements ICPPFunctionTemplate, ICPPFunction {
	IFunctionType type = null;
	/**
	 * @param decl
	 */
	public CPPFunctionTemplate(IASTName name) {
		super(name);
	}

	public void addDefinition(IASTNode node) {
		if( !(node instanceof IASTName) )
			return;
		updateFunctionParameterBindings( (IASTName) node );
		super.addDefinition( node );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
		if( !(node instanceof IASTName) )
			return;
		updateFunctionParameterBindings( (IASTName) node );
		super.addDeclaration( node );
	}	
	/**
	 * @param name
	 */
	private void updateFunctionParameterBindings(IASTName paramName) {
		IASTName defName = definition != null ? definition : declarations[0];
		ICPPASTFunctionDeclarator orig = (ICPPASTFunctionDeclarator) defName.getParent();
    	IASTParameterDeclaration [] ops = orig.getParameters();
    	IASTParameterDeclaration [] nps = ((ICPPASTFunctionDeclarator)paramName.getParent()).getParameters();
    	CPPParameter temp = null;
    	for( int i = 0; i < nps.length; i++ ){
    		temp = (CPPParameter) ops[i].getDeclarator().getName().getBinding();
    		if( temp != null ){
    		    IASTName name = nps[i].getDeclarator().getName();
    			name.setBinding( temp );
    			temp.addDeclaration( name );
    		}
    	}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplate#instantiate(org.eclipse.cdt.core.dom.ast.IASTNode[])
	 */
	public IBinding instantiate(ICPPASTTemplateId templateId ) {//IASTNode[] arguments) {
		ICPPTemplateParameter [] params = getTemplateParameters();
		IASTNode [] arguments = templateId.getTemplateArguments();
		
		ObjectMap map = new ObjectMap(params.length);
		if( arguments.length == params.length ){
			for( int i = 0; i < arguments.length; i++ ){
				IType t = CPPVisitor.createType( arguments[i] );
				map.put( params[i], t );
			}
		}
		
		return CPPTemplates.createInstance( templateId, (ICPPScope) getScope(), this, map );
	}

	/**
	 * @param templateParameter
	 * @return
	 */
//	public IBinding resolveParameter(ICPPASTTemplateParameter templateParameter) {
//		return null;
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter[] getParameters() {
		IASTName name = getTemplateName();
		IASTNode parent = name.getParent();
		if( parent instanceof ICPPASTQualifiedName )
			parent = parent.getParent();
		if( parent instanceof ICPPASTFunctionDeclarator ){
			ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
			IASTParameterDeclaration[] params = dtor.getParameters();
			int size = params.length;
			IParameter [] result = new IParameter[ size ];
			if( size > 0 ){
				for( int i = 0; i < size; i++ ){
					IASTParameterDeclaration p = params[i];
					result[i] = (IParameter) p.getDeclarator().getName().resolveBinding();
				}
			}
			return result;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
	 */
	public IFunctionType getType() {
		if( type == null ) {
			IASTName name = getTemplateName();
			IASTNode parent = name.getParent();
			while( parent.getParent() instanceof IASTDeclarator )
				parent = parent.getParent();
			
			IType temp = CPPVisitor.createType( (IASTDeclarator)parent );
			if( temp instanceof IFunctionType )
				type = (IFunctionType) temp;
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
	 */
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param param
	 * @return
	 */
	public IBinding resolveFunctionParameter(ICPPASTParameterDeclaration param) {
	   	IASTName name = param.getDeclarator().getName();
    	IBinding binding = name.getBinding();
    	if( binding != null )
    		return binding;
		
    	ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) param.getParent();
    	IASTParameterDeclaration [] ps = fdtor.getParameters();
    	int i = 0;
    	for( ; i < ps.length; i++ ){
    		if( param == ps[i] )
    			break;
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known defns and decls
    	binding = new CPPParameter( name );
    	IASTParameterDeclaration temp = null;
    	if( definition != null ){
    		IASTNode node = definition.getParent();
    		if( node instanceof ICPPASTQualifiedName )
    			node = node.getParent();
    		temp = ((ICPPASTFunctionDeclarator)node).getParameters()[i];
    		IASTName n = temp.getDeclarator().getName();
    		if( n != name ) {
    		    n.setBinding( binding );
    		    ((CPPParameter)binding).addDeclaration( n );
    		}
    	}
    	if( declarations != null ){
    		for( int j = 0; j < declarations.length && declarations[j] != null; j++ ){
    			temp = ((ICPPASTFunctionDeclarator)declarations[j].getParent()).getParameters()[i];
        		IASTName n = temp.getDeclarator().getName();
        		if( n != name ) {
        		    n.setBinding( binding );
        		    ((CPPParameter)binding).addDeclaration( n );
        		}

    		}
    	}
    	return binding;	}

}
