/*************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 */
/*
 * Created on Apr 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 *
 */
public class CPPClassSpecialization extends CPPSpecialization implements
		ICPPClassType, ICPPInternalClassType {

	/**
	 * @param specialized
	 * @param scope
	 */
	public CPPClassSpecialization(IBinding specialized, ICPPScope scope, ObjectMap argumentMap) {
		super(specialized, scope, argumentMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase[] getBases() throws DOMException {
		ICPPClassType cls = (ICPPClassType) getSpecializedBinding();
		if( cls != null ){
			ICPPBase [] bases = cls.getBases();
			for (int i = 0; i < bases.length; i++) {
				ICPPClassType T = bases[i].getBaseClass();
				if( T instanceof ICPPTemplateTypeParameter && argumentMap.containsKey( T ) ){
					IType t = (IType) argumentMap.get( T );
					if( t instanceof ICPPClassType )
						((CPPBaseClause)bases[i]).setBaseClass( (ICPPClassType) argumentMap.get(T) );
				}
			}
			return bases;
		}
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
	public ICPPConstructor[] getConstructors() throws DOMException {
		// TODO Auto-generated method stub
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	public IBinding[] getFriends() throws DOMException {
		// TODO Auto-generated method stub
		return IBinding.EMPTY_BINDING_ARRAY;
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
	public IScope getCompositeScope() throws DOMException {
		IASTNode definition = getDefinition();
		if( definition != null ){
			IASTNode node = definition.getParent();
	        while( node instanceof IASTName )
	            node = node.getParent();
	        if( node instanceof ICPPASTCompositeTypeSpecifier )
	            return ((ICPPASTCompositeTypeSpecifier)node).getScope();
		}
		return ((ICPPClassType)getSpecializedBinding()).getCompositeScope(); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
	public boolean isSameType(IType type) {
		return type == this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public ICPPDelegate createDelegate(IASTName name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object clone() {
		// TODO Auto-generated method stub
		return this;
	}

	public ICPPMethod[] getConversionOperators() {
		// TODO Auto-generated method stub
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

}
