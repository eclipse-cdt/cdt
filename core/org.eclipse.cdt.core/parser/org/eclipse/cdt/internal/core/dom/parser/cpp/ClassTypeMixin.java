/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType.CPPClassTypeProblem;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Holds common implementation of methods for ICPPClassType implementations that have
 * a corresponding textual definition in the source code. This functionality is then
 * accessed via a delegate.
 * 
 *  @see CPPClassType
 *  @see CPPClassTemplate
 */
class ClassTypeMixin {
	private ICPPInternalClassTypeMixinHost host;

	public ClassTypeMixin(ICPPInternalClassTypeMixinHost host) {
		this.host= host;
	}
	
	public IBinding[] getFriends() {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if(  host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new IBinding [] { new ProblemBinding( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}
		ObjectSet resultSet = new ObjectSet(2);
		IASTDeclaration [] members = host.getCompositeTypeSpecifier().getMembers();
		for( int i = 0; i < members.length; i++ ){
			IASTDeclaration decl = members[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();

			if( decl instanceof IASTSimpleDeclaration ){
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)decl).getDeclSpecifier();
				if( declSpec.isFriend() ){
					IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
					if( declSpec instanceof ICPPASTElaboratedTypeSpecifier && dtors.length == 0 ){
						resultSet.put( ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding() );
					} else {
						for( int j = 0; j < dtors.length; j++ ){
							if( dtors[j] == null ) break;
							resultSet.put( dtors[j].getName().resolveBinding() );
						}    
					}
				}
			} else if( decl instanceof IASTFunctionDefinition ){
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)decl).getDeclSpecifier();
				if( declSpec.isFriend() ){
					IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
					resultSet.put( dtor.getName().resolveBinding() );
				}

			}
		}

		return (IBinding[]) resultSet.keyArray( IBinding.class );
	}

	public ICPPMethod[] getConversionOperators() throws DOMException {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if(  host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new ICPPMethod[] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}
		IBinding binding = null;
		ICPPMethod [] result = null;

		IASTDeclaration [] decls = host.getCompositeTypeSpecifier().getMembers();
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
	}

	public ICPPBase [] getBases() {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if(  host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new ICPPBase [] { new CPPBaseClause.CPPBaseProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}
		ICPPASTBaseSpecifier [] bases = host.getCompositeTypeSpecifier().getBaseSpecifiers();
		if( bases.length == 0 )
			return ICPPBase.EMPTY_BASE_ARRAY;

		ICPPBase [] bindings = new ICPPBase[ bases.length ];
		for( int i = 0; i < bases.length; i++ ){
			bindings[i] = new CPPBaseClause( bases[i] );
		}

		return bindings; 
	}

	public ICPPMethod[] getMethods() throws DOMException {
		ObjectSet set = new ObjectSet(4);
		set.addAll(getDeclaredMethods());
		ICPPClassScope scope = (ICPPClassScope) host.getCompositeScope();
		set.addAll( scope.getImplicitMethods() );
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			IBinding b = bases[i].getBaseClass();
			if( b instanceof ICPPClassType )
				set.addAll( ((ICPPClassType)b).getMethods() );
		}
		return (ICPPMethod[]) set.keyArray( ICPPMethod.class );
	}


	public ICPPField[] getDeclaredFields() throws DOMException {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if(  host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new ICPPField[] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}
		IBinding binding = null;
		ICPPField [] result = null;

		IASTDeclaration [] decls = host.getCompositeTypeSpecifier().getMembers();
		for ( int i = 0; i < decls.length; i++ ) {
			if( decls[i] instanceof IASTSimpleDeclaration ){
				IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
				for ( int j = 0; j < dtors.length; j++ ) {
					binding = dtors[j].getName().resolveBinding();
					if( binding instanceof ICPPField )
						result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
				}
			} else if( decls[i] instanceof ICPPASTUsingDeclaration ){
				IASTName n = ((ICPPASTUsingDeclaration)decls[i]).getName();
				binding = n.resolveBinding();
				if( binding instanceof ICPPUsingDeclaration ){
					IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
					for ( int j = 0; j < bs.length; j++ ) {
						if( bs[j] instanceof ICPPField )
							result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, bs[j] );
					}
				} else if( binding instanceof ICPPField ) {
					result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
				}
			}
		}
		return (ICPPField[]) ArrayUtil.trim( ICPPField.class, result );
	}

	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if( host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new ICPPMethod [] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}

		ICPPMethod[] methods = getDeclaredMethods();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			IBinding b = bases[i].getBaseClass();
			if( b instanceof ICPPClassType )
				methods = (ICPPMethod[]) ArrayUtil.addAll( ICPPMethod.class, methods, ((ICPPClassType)b).getAllDeclaredMethods() );
		}
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, methods );
	}

	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if( host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new ICPPMethod[] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}
		IBinding binding = null;
		ICPPMethod [] result = null;

		IASTDeclaration [] decls = host.getCompositeTypeSpecifier().getMembers();
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
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if( host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new ICPPConstructor [] { new CPPConstructor.CPPConstructorProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}

		ICPPClassScope scope = (ICPPClassScope) host.getCompositeScope();
		if(ASTInternal.isFullyCached(scope))
			return ((CPPClassScope)scope).getConstructors( true );

		IASTDeclaration [] members = host.getCompositeTypeSpecifier().getMembers();
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

	public ICPPClassType[] getNestedClasses() {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if( host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new ICPPClassType[] { new CPPClassTypeProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}

		ICPPClassType [] result = null;

		IASTDeclaration [] decls = host.getCompositeTypeSpecifier().getMembers();
		for ( int i = 0; i < decls.length; i++ ) {
			IASTDeclaration decl = decls[i];
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			if( decl instanceof IASTSimpleDeclaration ){
				IBinding binding = null;
				IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
				if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
					binding = ((ICPPASTCompositeTypeSpecifier)declSpec).getName().resolveBinding();
				} else if( declSpec instanceof ICPPASTElaboratedTypeSpecifier &&
						((IASTSimpleDeclaration)decl).getDeclarators().length == 0 )
				{
					binding = ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding();
				}
				if( binding instanceof ICPPClassType )
					result = (ICPPClassType[])ArrayUtil.append( ICPPClassType.class, result, binding );
			} 
		}
		return (ICPPClassType[]) ArrayUtil.trim( ICPPClassType.class, result );
	}

	public IField[] getFields() throws DOMException {
		if( host.getDefinition() == null ){
			host.checkForDefinition();
			if( host.getDefinition() == null ){
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new IField [] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray() ) };
			}
		}

		IField[] fields = getDeclaredFields();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			IBinding b = bases[i].getBaseClass();
			if( b instanceof ICPPClassType )
				fields = (IField[]) ArrayUtil.addAll( IField.class, fields, ((ICPPClassType)b).getFields() );
		}
		return (IField[]) ArrayUtil.trim( IField.class, fields );
	}

	public IField findField(String name) throws DOMException {
		IBinding [] bindings = CPPSemantics.findBindings( host.getCompositeScope(), name, true );
		IField field = null;
		for ( int i = 0; i < bindings.length; i++ ) {
			if( bindings[i] instanceof IField ){
				if( field == null )
					field = (IField) bindings[i];
				else {
					IASTNode[] declarations= host.getDeclarations();
					IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
					return new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray() );
				}
			}
		}
		return field;
	}
}
