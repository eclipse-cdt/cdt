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
 * Created on Mar 28, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPClassInstance extends CPPInstance implements ICPPClassType, ICPPInternalBinding {
	private CPPClassInstanceScope instanceScope;
	
	/**
	 * @param decl
	 * @param args
	 * @param arguments
	 */
	public CPPClassInstance( ICPPScope scope, IBinding decl, ObjectMap argMap, IType[] args ) {
		super( scope, decl, argMap, args );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase[] getBases() throws DOMException {
		ICPPClassType cls = (ICPPClassType) getSpecializedBinding();
		if( cls != null ){
			ICPPBase [] bases = cls.getBases();
			for (int i = 0; i < bases.length; i++) {
				IBinding T = bases[i].getBaseClass();
				if( T instanceof ICPPTemplateTypeParameter && argumentMap.containsKey( T ) ){
					IType t = (IType) argumentMap.get( T );
					if( t instanceof ICPPClassType )
						((CPPBaseClause)bases[i]).setBaseClass( (ICPPClassType) argumentMap.get(T) );
				}
			}
			return bases;
		}
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
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
	public ICPPConstructor[] getConstructors() throws DOMException {
		CPPClassInstanceScope scope = (CPPClassInstanceScope) getCompositeScope();
		if( scope.isFullyCached() )
			return scope.getConstructors();
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
		return ((ICPPClassType)getSpecializedBinding()).getKey();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		if( instanceScope == null ){
			instanceScope = new CPPClassInstanceScope( this );
		}
		return instanceScope;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone(){
		// TODO Auto-generated method stub
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPClassType.CPPClassTypeDelegate( name, this );
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef )
            return ((ITypedef)type).isSameType( this );
        if( type instanceof CPPDeferredClassInstance )
        	return type.isSameType( this );  //the CPPDeferredClassInstance has some fuzziness
        
        if( type instanceof ICPPTemplateInstance ){
        	if( getSpecializedBinding() != ((ICPPTemplateInstance)type).getTemplateDefinition() )
        		return false;
        	
        	ObjectMap m1 = getArgumentMap(), m2 = ((ICPPTemplateInstance)type).getArgumentMap();
        	if( m1 == null || m2 == null || m1.size() != m2.size())
        		return false;
        	for( int i = 0; i < m1.size(); i++ ){
        		IType t1 = (IType) m1.getAt( i );
        		IType t2 = (IType) m2.getAt( i );
        		if( t1 == null || ! t1.isSameType( t2 ) )
        			return false;
        	}
        	return true;
        }

        return false;
    }

	public ICPPClassType[] getNestedClasses() throws DOMException {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

}
