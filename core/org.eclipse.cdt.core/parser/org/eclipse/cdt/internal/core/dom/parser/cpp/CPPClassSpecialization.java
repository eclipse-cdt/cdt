/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
/*
 * Created on Apr 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;

/**
 * @author aniefer
 *
 */
public class CPPClassSpecialization extends CPPSpecialization implements
		ICPPClassType, ICPPInternalClassType {

	private IScope specScope;
	
	/**
	 * @param specialized
	 * @param scope
	 */
	public CPPClassSpecialization(IBinding specialized, ICPPScope scope, ObjectMap argumentMap) {
		super(specialized, scope, argumentMap);
	}

	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(){
	    IASTNode definition = getDefinition();
		if( definition != null ){
	        IASTNode node = definition;
	        while( node instanceof IASTName )
	            node = node.getParent();
	        if( node instanceof ICPPASTCompositeTypeSpecifier )
	            return (ICPPASTCompositeTypeSpecifier)node;
	    }
	    return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase[] getBases() throws DOMException {
		if( getDefinition() == null ){
			ICPPBase[] bindings = ((ICPPClassType)getSpecializedBinding()).getBases();
			for (int i = 0; i < bindings.length; i++) {
    		    IBinding base = bindings[i].getBaseClass();
    		    if (bindings[i] instanceof CPPBaseClause && base instanceof IType) {
    		    	IType specBase = CPPTemplates.instantiateType((IType) base, argumentMap);
    		    	((CPPBaseClause)bindings[i]).setBaseClass((ICPPClassType)specBase);
    		    }
			}
			return bindings;
        }
		
		ICPPASTBaseSpecifier[] bases = getCompositeTypeSpecifier().getBaseSpecifiers();
		if (bases.length == 0)
			return ICPPBase.EMPTY_BASE_ARRAY;

		ICPPBase[] bindings = new ICPPBase[bases.length];
		for (int i = 0; i < bases.length; i++) {
			bindings[i] = new CPPBaseClause(bases[i]);
			IBinding base = bindings[i].getBaseClass();
			if (base instanceof IType) {
				IType specBase = CPPTemplates.instantiateType((IType) base, argumentMap);
				if (specBase instanceof ICPPClassType) {
					((CPPBaseClause) bindings[i]).setBaseClass((ICPPClassType) specBase);
				}
			}
		}
		return bindings;

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
		IScope scope = getCompositeScope();
		if (scope instanceof CPPClassSpecializationScope) {
			if (ASTInternal.isFullyCached(scope))
				return ((CPPClassSpecializationScope)scope).getConstructors();
		}
        
        if( ASTInternal.isFullyCached(scope))
        	return ((CPPClassScope)scope).getConstructors( true );
        	
        IASTDeclaration [] members = getCompositeTypeSpecifier().getMembers();
        for( int i = 0; i < members.length; i++ ){
        	IASTDeclaration decl = members[i];
        	if( decl instanceof ICPPASTTemplateDeclaration )
        		decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			if( decl instanceof IASTSimpleDeclaration ){
			    IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
			    for( int j = 0; j < dtors.length; j++ ){
			        if( dtors[j] == null ) break;
		            ASTInternal.addName(scope,  dtors[j].getName() );
			    }
			} else if( decl instanceof IASTFunctionDefinition ){
			    IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
			    ASTInternal.addName(scope,  dtor.getName() );
			}
        }
        
        return ((CPPClassScope)scope).getConstructors( true );
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
		if (getDefinition() != null)
			return getCompositeTypeSpecifier().getKey();
		
		return ((ICPPClassType)getSpecializedBinding()).getKey();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() throws DOMException {
		if (specScope == null) {
			ICPPClassScope scope = null;
			if( getDefinition() != null ){
				scope = (ICPPClassScope) getCompositeTypeSpecifier().getScope();
			}
			
			if (scope != null && scope.getClassType() == this) {
				//explicit specialization: can use composite type specifier scope
				specScope = scope;
			} else if (scope != null) {
				//implicit specialization: must specialize bindings in scope
				specScope = new CPPClassSpecializationScope(this);
			}	
		}
		return specScope;
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
		return new CPPClassType.CPPClassTypeDelegate( name, this );
	}

	public Object clone() {
		// TODO Auto-generated method stub
		return this;
	}

	public ICPPMethod[] getConversionOperators() {
		try {
			ICPPMethod [] result = null;
			
			IScope scope = getCompositeScope();
			if (scope instanceof CPPClassSpecializationScope) {
				if (ASTInternal.isFullyCached(scope))
					result = ((CPPClassSpecializationScope)scope).getConversionOperators();
			} else {
				IBinding binding = null;
				 
				IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
				IASTName name = null;
				for ( int i = 0; i < decls.length; i++ ) {
					if( decls[i] instanceof IASTSimpleDeclaration ){
						IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
						for ( int j = 0; j < dtors.length; j++ ) {
							name = CPPVisitor.getMostNestedDeclarator( dtors[j] ).getName();
							if( name instanceof ICPPASTConversionName ){
								binding = name.resolveBinding();
								if( binding instanceof ICPPMethod)
									result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );	
							}
						}
					} else if( decls[i] instanceof IASTFunctionDefinition ){
						IASTDeclarator dtor = ((IASTFunctionDefinition)decls[i]).getDeclarator();
						name = CPPVisitor.getMostNestedDeclarator( dtor ).getName();
						if( name instanceof ICPPASTConversionName ){
							binding = name.resolveBinding();
							if( binding instanceof ICPPMethod ){
								result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
							}
						}
					} 
				}	
			}
	   
			ICPPBase [] bases = getBases();
			for ( int i = 0; i < bases.length; i++ ) {
				ICPPClassType cls = null;
				try {
					IBinding b = bases[i].getBaseClass();
					if( b instanceof ICPPClassType )
						cls = (ICPPClassType) b;
				} catch (DOMException e) {
					continue;
				}
				if( cls instanceof ICPPInternalClassType )
					result = (ICPPMethod[]) ArrayUtil.addAll( ICPPMethod.class, result, ((ICPPInternalClassType)cls).getConversionOperators() );
			}
			
			return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, result );
		} catch (DOMException e) {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}
}
