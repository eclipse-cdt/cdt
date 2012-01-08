/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression.CaptureDefault;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a class type.
 */
public class CPPClosureType extends PlatformObject implements ICPPClassType, ICPPInternalBinding {
	private static final CPPBasicType NO_RETURN_TYPE = new CPPBasicType(Kind.eUnspecified, 0);

	private final ICPPASTLambdaExpression fLambdaExpression;
	private ICPPMethod[] fMethods;
	private ClassScope fScope;

	public CPPClosureType(ICPPASTLambdaExpression lambdaExpr) {
		fLambdaExpression= lambdaExpr;
	}

	private ICPPMethod[] createMethods() {
		boolean needConversionOperator= 
			fLambdaExpression.getCaptureDefault() == CaptureDefault.UNSPECIFIED &&
			fLambdaExpression.getCaptures().length == 0;
		
		final ICPPClassScope scope= getCompositeScope();
		ICPPMethod[] result= new ICPPMethod[needConversionOperator ? 6 : 5];

		// Deleted default constructor: A()
		CPPImplicitConstructor ctor= new CPPImplicitConstructor(scope, CharArrayUtils.EMPTY, ICPPParameter.EMPTY_CPPPARAMETER_ARRAY);
		ctor.setDeleted(true);
		result[0]= ctor;
		
		// Copy constructor: A(const A &)
		IType pType = new CPPReferenceType(SemanticUtil.constQualify(this), false);
		ICPPParameter[] ps = new ICPPParameter[] { new CPPParameter(pType, 0) };
		ctor = new CPPImplicitConstructor(scope, CharArrayUtils.EMPTY, ps);
		result[1]= ctor;
		
		// Deleted copy assignment operator: A& operator = (const A &)
		IType refType = new CPPReferenceType(this, false);
		ICPPFunctionType ft= CPPVisitor.createImplicitFunctionType(refType, ps, false, false);
		ICPPMethod m = new CPPImplicitMethod(scope, OverloadableOperator.ASSIGN.toCharArray(), ft, ps);
		result[2]= m;

		// Destructor: ~A()
		ft= CPPVisitor.createImplicitFunctionType(NO_RETURN_TYPE, ICPPParameter.EMPTY_CPPPARAMETER_ARRAY, false, false);
		m = new CPPImplicitMethod(scope, new char[] {'~'}, ft, ICPPParameter.EMPTY_CPPPARAMETER_ARRAY);
		result[3]= m;
		
		// Function call operator
		final IType returnType= getReturnType();
		final IType[] parameterTypes= getParameterTypes();
		ft= new CPPFunctionType(returnType, parameterTypes, isMutable(), false, false);

		ICPPParameter[] params = new ICPPParameter[parameterTypes.length];
		for (int i = 0; i < params.length; i++) {
			params[i]= new CPPParameter(parameterTypes[i], 0);
		}
		m= new CPPImplicitMethod(scope, OverloadableOperator.PAREN.toCharArray(), ft, params) {
			@Override
			public boolean isImplicit() {return false;}
		};
		result[4]= m;
		
		// Conversion operator
		if (needConversionOperator) {
			final CPPFunctionType conversionTarget = new CPPFunctionType(returnType, parameterTypes);
			ft= new CPPFunctionType(conversionTarget, IType.EMPTY_TYPE_ARRAY, true, false, false);
			m= new CPPImplicitMethod(scope, CPPASTConversionName.createName(conversionTarget, null), ft, params);
			result[5]= m;
		}
		return result;
	}

	public ICPPMethod getFunctionCallOperator() {
		return getMethods()[4];
	}

	private boolean isMutable() {
		ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
		return lambdaDtor != null && lambdaDtor.isMutable();
	}

	private IType getReturnType() {
		ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
		if (lambdaDtor != null) {
			IASTTypeId trailingReturnType = lambdaDtor.getTrailingReturnType();
			if (trailingReturnType != null) {
				return CPPVisitor.createType(trailingReturnType);
			}
		}
		IASTCompoundStatement body = fLambdaExpression.getBody();
		if (body != null) {
			IASTStatement[] stmts = body.getStatements();
			if (stmts.length > 0) {
				// Gnu extension allows to deduce return type in complex compound statements
				IASTStatement stmt= stmts[stmts.length-1];
				if (stmt instanceof IASTReturnStatement) {
					IASTReturnStatement rtstmt= (IASTReturnStatement) stmt;
					IASTExpression expr= rtstmt.getReturnValue();
					if (expr != null) {
						IType type= expr.getExpressionType();
						type= Conversions.lvalue_to_rvalue(type);
						if (type != null) {
							return type;
						}
					}
				}
			}
		}
		return CPPSemantics.VOID_TYPE;		
	}

	private IType[] getParameterTypes() {
		ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
		if (lambdaDtor != null) {
			return CPPVisitor.createParameterTypes(lambdaDtor);
		}
		return IType.EMPTY_TYPE_ARRAY;
	}
	
	@Override
	public final String getName() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public char[] getNameCharArray() {
		return CharArrayUtils.EMPTY;
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(fLambdaExpression);
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		if (fScope == null) {
			fScope= new ClassScope();
		}
		return fScope;
	}

	@Override
	public int getKey() {
		return k_class;
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() {
		return getOwner() == null;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexBinding)
			return type.isSameType(this);
		return false;
	}
	
	@Override
	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	@Override
	public ICPPField[] getFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods() {
		if (fMethods == null) {
			fMethods= createMethods();
		}
		return fMethods;
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		ICPPMethod[] methods= getMethods();
		int i= 0;
		for (; i < methods.length; i++) {
			if (!(methods[i] instanceof ICPPConstructor)) {
				break;
			}
		}
		ICPPConstructor[] result= new ICPPConstructor[i];
		System.arraycopy(methods, 0, result, 0, i);
		return result;
	}

	@Override
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}
	
	@Override
	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public IField findField(String name) {
		return null;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
	/**
	 * For debugging purposes, only.
	 */
	@Override
	public String toString() {
		char[] name= ASTTypeUtil.createNameForAnonymous(this);
		if (name != null)
			return new String(name);
		return null;
	}

	@Override
	public IBinding getOwner() {
		return CPPVisitor.findDeclarationOwner(fLambdaExpression, true);
	}
	
	@Override
	public boolean isAnonymous() {
		return false;
	}
	
	@Override
	public IASTNode getDefinition() {
		return fLambdaExpression;
	}

	@Override
	public IASTNode[] getDeclarations() {
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	@Override
	public void addDefinition(IASTNode node) {
	}

	@Override
	public void addDeclaration(IASTNode node) {
	}


	private final class ClassScope implements ICPPClassScope {
		@Override
		public EScopeKind getKind() {
			return EScopeKind.eClassType;
		}

		@Override
		public IName getScopeName() {
			return null;
		}

		@Override
		public IScope getParent() {
			return getScope();
		}

		private IBinding getBinding(char[] name) {
			for (ICPPMethod m : getMethods()) {
				if (!(m instanceof ICPPConstructor) && CharArrayUtils.equals(name, m.getNameCharArray())) {
					return m;
				}
			}
			return null;
		}

		private IBinding[] getBindings(char[] name) {
			IBinding m= getBinding(name);
			if (m != null) {
				return new IBinding[] {m};
			}
			return IBinding.EMPTY_BINDING_ARRAY;
		}

		private IBinding[] getPrefixBindings(char[] name) {
			List<IBinding> result= new ArrayList<IBinding>();
			IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(name);
			for (ICPPMethod m : getMethods()) {
				if (!(m instanceof ICPPConstructor)) {
					if (matcher.match(m.getNameCharArray())) {
						result.add(m);
					}
				}
			}
			return result.toArray(new IBinding[result.size()]);
		}

		@Override
		public IBinding[] find(String name) {
			return getBindings(name.toCharArray());
		}

		@Override
		public IBinding getBinding(IASTName name, boolean resolve) {
			if (name instanceof ICPPASTTemplateId)
				return null;
			return getBinding(name.getSimpleID());
		}

		@Override
		public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
			return getBinding(name, resolve);
		}

		@Override
		public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
			if (name instanceof ICPPASTTemplateId)
				return IBinding.EMPTY_BINDING_ARRAY;
			
			if (prefixLookup)
				return getPrefixBindings(name.getSimpleID());
			return getBindings(name.getSimpleID());
		}

		@Override
		public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
				IIndexFileSet acceptLocalBindings) {
			return getBindings(name, resolve, prefixLookup);
		}

		@Override
		public ICPPClassType getClassType() {
			return CPPClosureType.this;
		}

		@Override
		public ICPPMethod[] getImplicitMethods() {
			return getMethods();
		}

		@Override
		public ICPPConstructor[] getConstructors() {
			return CPPClosureType.this.getConstructors();
		}
	}
}
