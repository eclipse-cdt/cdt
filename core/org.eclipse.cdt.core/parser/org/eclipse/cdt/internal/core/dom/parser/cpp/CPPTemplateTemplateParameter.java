/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 13, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPTemplateTemplateParameter extends CPPTemplateParameter implements
		ICPPTemplateTemplateParameter, ICPPClassType, ICPPInternalTemplate, ICPPInternalUnknown {

	private ICPPTemplateParameter [] templateParameters = null;
	private ObjectMap instances = null;
	private ICPPScope unknownScope = null;
	
	/**
	 * @param name
	 */
	public CPPTemplateTemplateParameter(IASTName name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ICPPScope getUnknownScope() {
	    if( unknownScope == null ) {
	        unknownScope = new CPPUnknownScope( this, null );
	    }
	    return unknownScope;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter#getTemplateParameters()
	 */
	public ICPPTemplateParameter[] getTemplateParameters() {
		if( templateParameters == null ){
			ICPPASTTemplatedTypeTemplateParameter template = (ICPPASTTemplatedTypeTemplateParameter) getPrimaryDeclaration().getParent();
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

	/**
	 * @param templateParameter
	 * @return
	 */
	public IBinding resolveTemplateParameter(ICPPASTTemplateParameter templateParameter) {
		IASTName name = CPPTemplates.getTemplateParameterName( templateParameter );
		
		IBinding binding = name.getBinding();
		if( binding == null ){
			//create a new binding and set it for the corresponding parameter in all known decls
	    	if( templateParameter instanceof ICPPASTSimpleTypeTemplateParameter )
	    		binding = new CPPTemplateTypeParameter( name );
	    	else if( templateParameter instanceof ICPPASTParameterDeclaration )
	    		binding = new CPPTemplateNonTypeParameter( name );
	    	else 
	    		binding = new CPPTemplateTemplateParameter( name );
	    	name.setBinding( binding );
		}
		return binding;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition#getTemplateSpecializations()
	 */
	public ICPPClassTemplatePartialSpecialization[] getTemplateSpecializations() throws DOMException {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter#getDefault()
	 */
	public IType getDefault() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase[] getBases() {
		// TODO Auto-generated method stub
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFields()
	 */
	public IField[] getFields() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#findField(java.lang.String)
	 */
	public IField findField(String name) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public ICPPField[] getDeclaredFields() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	public IBinding[] getFriends() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() throws DOMException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public ICPPDelegate createDelegate(IASTName name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef )
            return ((ITypedef)type).isSameType( this );
        return false;
    }

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	public IBinding instantiate(IType[] arguments) {
		return deferredInstance( arguments );
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		ICPPSpecialization instance = getInstance( arguments );
		if( instance == null ){
			instance = new CPPDeferredClassInstance( this, arguments );
			addSpecialization( arguments, instance );
		}
		return instance;
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
		if( instances == null )
			instances = new ObjectMap( 2 );
		instances.put( types, spec );
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#resolveUnknown(org.eclipse.cdt.core.parser.util.ObjectMap)
     */
    public IBinding resolveUnknown( ObjectMap argMap ) {
        // TODO Auto-generated method stub
        return null;
    }
}
