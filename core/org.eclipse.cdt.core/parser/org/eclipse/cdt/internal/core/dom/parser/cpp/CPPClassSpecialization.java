/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
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
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author aniefer
 *
 */
public class CPPClassSpecialization extends CPPSpecialization implements
		ICPPClassType {

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
			ICPPBase[] result = null;
			ICPPBase[] bindings = ((ICPPClassType)getSpecializedBinding()).getBases();
			for (int i = 0; i < bindings.length; i++) {
				ICPPBase specBinding = (ICPPBase) ((ICPPInternalBase)bindings[i]).clone();
    		    IBinding base = bindings[i].getBaseClass();
    		    if (base instanceof IType) {
    		    	IType specBase = CPPTemplates.instantiateType((IType) base, argumentMap);
    		    	specBase = SemanticUtil.getUltimateType(specBase, false);
    		    	if (specBase instanceof IBinding) {
    		    		((ICPPInternalBase)specBinding).setBaseClass((IBinding)specBase);
    		    	}
    		    	result = (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, specBinding);
    		    }
			}
			return (ICPPBase[]) ArrayUtil.trim(ICPPBase.class, result);
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
		IScope scope= getCompositeScope();
		if (scope instanceof CPPClassSpecializationScope) {
			CPPClassSpecializationScope sscope= (CPPClassSpecializationScope) scope;
			if (sscope.isFullyCached())
				return sscope.getDeclaredMethods();
		}
		
		IBinding binding = null;
		ICPPMethod [] result = null;

		IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
		for ( int i = 0; i < decls.length; i++ ) {
			IASTDeclaration decl = decls[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			if( decl instanceof IASTSimpleDeclaration ){
				IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
				for ( int j = 0; j < dtors.length; j++ ) {
					binding = dtors[j].getName().resolveBinding();
					if( binding instanceof ICPPMethod)
						result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
				}
			} else if( decl instanceof IASTFunctionDefinition ){
				IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
				dtor = CPPVisitor.getMostNestedDeclarator( dtor );
				binding = dtor.getName().resolveBinding();
				if( binding instanceof ICPPMethod ){
					result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
				}
			} else if( decl instanceof ICPPASTUsingDeclaration ){
				IASTName n = ((ICPPASTUsingDeclaration)decl).getName();
				binding = n.resolveBinding();
				if( binding instanceof ICPPUsingDeclaration ){
					IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
					for ( int j = 0; j < bs.length; j++ ) {
						if( bs[j] instanceof ICPPMethod )
							result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, bs[j] );
					}
				} else if( binding instanceof ICPPMethod ) {
					result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
				}
			}
		}
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, result );
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
			} else {
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
		if( type == this )
			return true;
		if( type instanceof ITypedef || type instanceof IIndexType )
			return type.isSameType( this );
		return false;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return this;
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}
}
