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
 * Created on Mar 14, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public abstract class CPPTemplateDefinition extends PlatformObject implements ICPPTemplateDefinition, ICPPInternalTemplate {
	public static final class CPPTemplateProblem extends ProblemBinding implements ICPPTemplateDefinition {
		public CPPTemplateProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
		public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPClassTemplatePartialSpecialization[] getTemplateSpecializations() throws DOMException {
			throw new DOMException( this );
		}
		public String[] getQualifiedName() throws DOMException {
			throw new DOMException( this );
		}
		public char[][] getQualifiedNameCharArray() throws DOMException {
			throw new DOMException( this );
		}
		public boolean isGloballyQualified() throws DOMException {
			throw new DOMException( this );
		}
	}
	//private IASTName templateName;
	protected IASTName [] declarations = null;
	protected IASTName definition = null;
	
	private ICPPTemplateParameter [] templateParameters = null;
	private ObjectMap instances = null;
	
	public CPPTemplateDefinition( IASTName name ) {
		if( name != null ){
			ASTNodeProperty prop = name.getPropertyInParent();
			if( prop == ICPPASTQualifiedName.SEGMENT_NAME ){
				prop = name.getParent().getPropertyInParent();
			}
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
	}

	public abstract ICPPSpecialization deferredInstance( IType [] arguments );
	
	public IBinding instantiate(ICPPASTTemplateId templateId ) {//IASTNode[] arguments) {
		IASTNode [] args = templateId.getTemplateArguments();
		IType [] types = CPPTemplates.createTypeArray( args );
		return instantiate( types );
	}
	
	public IBinding instantiate( IType [] arguments ){
		ICPPTemplateDefinition template = null;
		if( this instanceof ICPPClassTemplate ){
			try {
				template = CPPTemplates.matchTemplatePartialSpecialization( (ICPPClassTemplate) this, arguments );
			} catch (DOMException e) {
				return e.getProblem();
			}
		}
		
		if( template instanceof IProblemBinding )
			return template;
		if( template != null && template instanceof ICPPClassTemplatePartialSpecialization ){
			return ((CPPTemplateDefinition)template).instantiate( arguments );	
		}
		
		return CPPTemplates.instantiateTemplate( this, arguments, null );
	}
	
	public ICPPSpecialization getInstance( IType [] arguments ) {
		if( instances == null )
			return null;
		
		int found = -1;
		for( int i = 0; i < instances.size(); i++ ){
			IType [] args = (IType[]) instances.keyAt( i );
			if( args.length == arguments.length ){
				int j = 0;
				for(; j < args.length; j++) {
					if( !( args[j].isSameType( arguments[j] ) ) )
						break;
				}
				if( j == args.length ){
					found = i;
					break;
				}
			}
		}
		if( found != -1 ){
			return (ICPPSpecialization) instances.getAt(found);
		}
		return null;
	}
	
	public void addSpecialization( IType [] types, ICPPSpecialization spec ){
		if( types == null )
			return;
		for( int i = 0; i < types.length; i++ )
			if( types[i] == null )
				return;
		if( instances == null )
			instances = new ObjectMap( 2 );
		instances.put( types, spec );
	}
	
	public IBinding resolveTemplateParameter(ICPPASTTemplateParameter templateParameter) {
	   	IASTName name = CPPTemplates.getTemplateParameterName( templateParameter );
    	IBinding binding = name.getBinding();
    	if( binding != null )
    		return binding;
			
    	ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) templateParameter.getParent();
    	ICPPASTTemplateParameter [] ps = templateDecl.getTemplateParameters();

    	int i = 0;
    	for( ; i < ps.length; i++ ){
    		if( templateParameter == ps[i] )
    			break;
    	}
    	
    	if( definition != null || (declarations != null && declarations.length > 0 ) ){
    	    IASTName templateName = ( definition != null ) ? definition : declarations[0];
    	    ICPPASTTemplateDeclaration temp = CPPTemplates.getTemplateDeclaration( templateName );
    	    ICPPASTTemplateParameter [] params = temp.getTemplateParameters();
    	    if( params.length > i ) {
    	        IASTName paramName = CPPTemplates.getTemplateParameterName( params[i] );
    	        if( paramName.getBinding() != null ){
    	            binding = paramName.getBinding();
    	            name.setBinding( binding );
    	            if( binding instanceof ICPPInternalBinding )
    	                ((ICPPInternalBinding)binding).addDeclaration( name );
    	            return binding;
    	        }
    	    }
    	}
    	//create a new binding and set it for the corresponding parameter in all known decls
    	if( templateParameter instanceof ICPPASTSimpleTypeTemplateParameter )
    		binding = new CPPTemplateTypeParameter( name );
    	else if( templateParameter instanceof ICPPASTParameterDeclaration )
    		binding = new CPPTemplateNonTypeParameter( name );
    	else 
    		binding = new CPPTemplateTemplateParameter( name );
    	
    	ICPPASTTemplateParameter temp = null;
    	ICPPASTTemplateDeclaration template = null;
    	int length = ( declarations != null ) ? declarations.length : 0;
		int j = ( definition != null ) ? -1 : 0;
		for( ; j < length; j++ ){
			template = ( j == -1 ) ? CPPTemplates.getTemplateDeclaration( definition )
								   : CPPTemplates.getTemplateDeclaration( declarations[j] );
			if( template == null )
				continue;
			temp = template.getTemplateParameters()[i];

    		IASTName n = CPPTemplates.getTemplateParameterName( temp );
    		if( n != null && n != name && n.getBinding() == null ) {
    		    n.setBinding( binding );
    		    if( binding instanceof ICPPInternalBinding )
	                ((ICPPInternalBinding)binding).addDeclaration( n );
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
		return CPPVisitor.getContainingScope( getTemplateName() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
	 */
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
	 */
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	public boolean isGloballyQualified() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition#getParameters()
	 */
	public ICPPTemplateParameter[] getTemplateParameters() {
		if( templateParameters == null ){
			ICPPASTTemplateDeclaration template = CPPTemplates.getTemplateDeclaration( getTemplateName() );
			if( template == null )
				return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
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
	    if( node instanceof ICPPASTCompositeTypeSpecifier ){
	        node = ((ICPPASTCompositeTypeSpecifier)node).getName();
	        if( node instanceof ICPPASTQualifiedName ){
	            IASTName [] ns = ((ICPPASTQualifiedName)node).getNames();
	            node = ns[ ns.length - 1];
	        }
	    }
		if( !(node instanceof IASTName) )
			return;
		updateTemplateParameterBindings( (IASTName) node );
		definition = (IASTName) node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
	    if( node instanceof ICPPASTElaboratedTypeSpecifier ){
	        node = ((ICPPASTElaboratedTypeSpecifier)node).getName();
	        if( node instanceof ICPPASTQualifiedName ){
	            IASTName [] ns = ((ICPPASTQualifiedName)node).getNames();
	            node = ns[ ns.length - 1];
	        }
	    }
		if( !(node instanceof IASTName) )
			return;
		IASTName declName = (IASTName) node;
		updateTemplateParameterBindings( declName );
		if( declarations == null )
	        declarations = new IASTName[] { declName };
	    else {
	        //keep the lowest offset declaration in [0]
			if( declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset() ){
				declarations = (IASTName[]) ArrayUtil.prepend( IASTName.class, declarations, declName );
			} else {
				declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, declName );
			}
	    }
	}	
	
	public void removeDeclaration(IASTNode node) {
		if( definition == node ){
			definition = null;
			return;
		}
		if( declarations != null ) {
			for (int i = 0; i < declarations.length; i++) {
				if( node == declarations[i] ) {
					if( i == declarations.length - 1 )
						declarations[i] = null;
					else
						System.arraycopy( declarations, i + 1, declarations, i, declarations.length - 1 - i );
					return;
				}
			}
		}
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
    			temp.addDeclaration( n );
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
}
