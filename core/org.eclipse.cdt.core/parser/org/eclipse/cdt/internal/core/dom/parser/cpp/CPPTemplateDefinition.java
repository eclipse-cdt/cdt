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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author aniefer
 */
public abstract class CPPTemplateDefinition implements ICPPTemplateDefinition, ICPPInternalBinding {
	//private IASTName templateName;
	protected IASTName [] declarations = null;
	protected IASTName definition = null;
	
	private ICPPTemplateParameter [] templateParameters = null;
	private ICPPTemplateSpecialization [] specializations = null;
	
	public CPPTemplateDefinition( IASTName name ) {
		ASTNodeProperty prop = name.getPropertyInParent();
		if( prop == IASTCompositeTypeSpecifier.TYPE_NAME ){
			definition = name;
		} else if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME ) {
			declarations = new IASTName [] { name };
		} else {
			IASTNode parent = name.getParent();
			while( !(parent instanceof IASTDeclaration) )
				parent = parent.getParent();
			if( parent instanceof IASTFunctionDefinition )
				definition = name;
			else
				declarations = new IASTName [] { name };
		}
	}

	public abstract IBinding instantiate( ICPPASTTemplateId templateId  );
	
	public ICPPTemplateSpecialization [] getSpecializations() {
		return (ICPPTemplateSpecialization[]) ArrayUtil.trim( ICPPTemplateSpecialization.class, specializations );
	}
	public void addSpecialization( ICPPTemplateSpecialization spec ){
		specializations = (ICPPTemplateSpecialization[]) ArrayUtil.append( ICPPTemplateSpecialization.class, specializations, spec );
		
	}
	
	public IBinding resolveTemplateParameter(ICPPASTTemplateParameter templateParameter) {
	   	IASTName name = CPPTemplates.getTemplateParameterName( templateParameter );
    	IBinding binding = name.getBinding();
    	if( binding != null )
    		return binding;
		
    	if( templateParameter.getParent() instanceof ICPPASTTemplatedTypeTemplateParameter ){
    		
    	}
    	
    	ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) templateParameter.getParent();
    	ICPPASTTemplateParameter [] ps = templateDecl.getTemplateParameters();

    	int i = 0;
    	for( ; i < ps.length; i++ ){
    		if( templateParameter == ps[i] )
    			break;
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known decls
    	binding = new CPPTemplateParameter( name );
    	ICPPASTTemplateParameter temp = null;
    	ICPPASTTemplateDeclaration template = null;
    	int length = ( declarations != null ) ? declarations.length : 0;
		int j = ( definition != null ) ? -1 : 0;
		for( ; j < length; j++ ){
			template = ( j == -1 ) ? CPPTemplates.getTemplateDeclaration( definition )
								   : CPPTemplates.getTemplateDeclaration( declarations[j] );
			temp = template.getTemplateParameters()[i];

    		IASTName n = CPPTemplates.getTemplateParameterName( temp );
    		if( n != name ) {
    		    n.setBinding( binding );
    		}

		}
    	return binding;
	}
	
	public IASTName getTemplateName(){
		return definition != null ? definition : declarations[0];
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return getTemplateName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return getTemplateName().toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope( getTemplateName().getParent() );
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
	public ICPPTemplateParameter[] getTemplateParameters() {
		if( templateParameters == null ){
			ICPPASTTemplateDeclaration template = CPPTemplates.getTemplateDeclaration( getTemplateName() );
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
			templateParameters = (ICPPTemplateParameter[]) ArrayUtil.trim( ICPPTemplateParameter.class, result );
		}
		return templateParameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		if( !(node instanceof IASTName) )
			return;
		updateTemplateParameterBindings( (IASTName) node );
		definition = (IASTName) node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
		if( !(node instanceof IASTName) )
			return;
		IASTName declName = (IASTName) node;
		updateTemplateParameterBindings( declName );
		if( declarations == null ){
			declarations = new IASTName [] { declName };
			return;
		}
		declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, declName );
	}	
	
	protected void updateTemplateParameterBindings( IASTName name ){
    	IASTName orig = definition != null ? definition : declarations[0];
    	ICPPASTTemplateDeclaration origTemplate = CPPTemplates.getTemplateDeclaration( orig );
    	ICPPASTTemplateDeclaration newTemplate = CPPTemplates.getTemplateDeclaration( name );
    	ICPPASTTemplateParameter [] ops = origTemplate.getTemplateParameters();
    	ICPPASTTemplateParameter [] nps = newTemplate.getTemplateParameters();
    	ICPPInternalBinding temp = null;
    	for( int i = 0; i < nps.length; i++ ){
    		temp = (ICPPInternalBinding) CPPTemplates.getTemplateParameterName( ops[i] ).getBinding();
    		if( temp != null ){
    		    IASTName n = CPPTemplates.getTemplateParameterName( nps[i] );
    			n.setBinding( temp );
    			temp.addDeclaration( name );
    		}
    	}
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
	 */
	public IASTNode[] getDeclarations() {
		return declarations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
	 */
	public IASTNode getDefinition() {
		return definition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public ICPPDelegate createDelegate(IASTName name) {
		return null;
	}
}
