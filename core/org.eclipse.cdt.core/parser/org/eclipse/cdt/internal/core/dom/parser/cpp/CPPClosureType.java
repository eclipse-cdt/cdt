/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Thomas Corbat (IFS)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.UNSPECIFIED_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression.CaptureDefault;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPlaceholderType.PlaceholderKind;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a closure type.
 */
public class CPPClosureType extends PlatformObject implements ICPPClassType, ICPPInternalBinding {
	private final ICPPASTLambdaExpression fLambdaExpression;
	private IType[] fParameterTypes;
	private ICPPParameter[] fParameters;
	protected ICPPMethod[] fMethods;
	private ClassScope fScope;
	// Used for generic lambdas; null otherwise.
	private ICPPTemplateParameter[] fInventedTemplateParameters;

	public CPPClosureType(ICPPASTLambdaExpression lambdaExpr) {
		fLambdaExpression = lambdaExpr;
	}

	private ICPPMethod[] createMethods() {
		boolean needConversionOperator = fLambdaExpression.getCaptureDefault() == CaptureDefault.UNSPECIFIED
				&& fLambdaExpression.getCaptures().length == 0;

		final ICPPClassScope scope = getCompositeScope();
		ICPPMethod[] result = new ICPPMethod[needConversionOperator ? 6 : 5];

		// Deleted default constructor: A()
		CPPImplicitConstructor ctor = new CPPImplicitConstructor(scope, CharArrayUtils.EMPTY,
				ICPPParameter.EMPTY_CPPPARAMETER_ARRAY, fLambdaExpression);
		ctor.setDeleted(true);
		result[0] = ctor;

		// Copy constructor: A(const A &)
		IType pType = new CPPReferenceType(SemanticUtil.constQualify(this), false);
		ICPPParameter[] ps = new ICPPParameter[] { new CPPParameter(pType, 0) };
		ctor = new CPPImplicitConstructor(scope, CharArrayUtils.EMPTY, ps, fLambdaExpression);
		result[1] = ctor;

		// Deleted copy assignment operator: A& operator = (const A &)
		IType refType = new CPPReferenceType(this, false);
		ICPPFunctionType ft = CPPVisitor.createImplicitFunctionType(refType, ps, false, false);
		ICPPMethod m = new CPPImplicitMethod(scope, OverloadableOperator.ASSIGN.toCharArray(), ft, ps, false);
		result[2] = m;

		// Destructor: ~A()
		ft = CPPVisitor.createImplicitFunctionType(UNSPECIFIED_TYPE, ICPPParameter.EMPTY_CPPPARAMETER_ARRAY, false,
				false);
		m = new CPPImplicitMethod(scope, new char[] { '~' }, ft, ICPPParameter.EMPTY_CPPPARAMETER_ARRAY, false);
		result[3] = m;

		// Function call operator
		final IType returnType = getReturnType();
		final IType[] parameterTypes = getParameterTypes();
		ft = new CPPFunctionType(returnType, parameterTypes, getNoexceptEvaluation(), !isMutable(), false, false, false,
				false);

		ICPPParameter[] params = getParameters();
		char[] operatorParensName = OverloadableOperator.PAREN.toCharArray();
		if (isGeneric()) {
			m = new CPPImplicitMethodTemplate(getInventedTemplateParameterList(), scope, operatorParensName, ft, params,
					false) {
				@Override
				public boolean isImplicit() {
					return false;
				}
			};
		} else {
			m = new CPPImplicitMethod(scope, operatorParensName, ft, params, false) {
				@Override
				public boolean isImplicit() {
					return false;
				}
			};
		}
		result[4] = m;

		// Conversion operator
		if (needConversionOperator) {
			final CPPFunctionType conversionTarget = new CPPFunctionType(returnType, parameterTypes, null);
			ft = new CPPFunctionType(conversionTarget, IType.EMPTY_TYPE_ARRAY,
					CPPASTFunctionDeclarator.NOEXCEPT_TRUE /* (CWG DR 1722) */, true, false, false, false, false);
			// Calling CPPASTConversionName.createName(IType) would try to stringize the type to
			// construct a name, which is unnecessary work (not to mention prone to recursion with
			// dependent types). Since the name doesn't matter anyways, just make one up.
			char[] conversionOperatorName = CPPASTConversionName.createName("__fptr"); //$NON-NLS-1$
			if (isGeneric()) {
				ICPPTemplateParameter[] templateParams = getInventedTemplateParameterList();
				// Clone the template parameters, since they are used by the function call operator,
				// and the same parameters cannot participate in two different templates.
				ICPPTemplateParameter[] templateParamClones = new ICPPTemplateParameter[templateParams.length];
				for (int i = 0; i < templateParams.length; ++i) {
					templateParamClones[i] = (ICPPTemplateParameter) ((IType) templateParams[i]).clone();
				}
				m = new CPPImplicitMethodTemplate(templateParamClones, scope, conversionOperatorName, ft, params,
						false) {
					@Override
					public boolean isImplicit() {
						return false;
					}
				};
			} else {
				m = new CPPImplicitMethod(scope, conversionOperatorName, ft, params, false) {
					@Override
					public boolean isImplicit() {
						return false;
					}
				};
			}
			result[5] = m;
		}
		return result;
	}

	public ICPPMethod getFunctionCallOperator() {
		return getMethods()[4];
	}

	public ICPPMethod getConversionOperator() {
		ICPPMethod[] methods = getMethods();
		return methods.length >= 6 ? methods[5] : null;
	}

	private boolean isMutable() {
		ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
		return lambdaDtor != null && lambdaDtor.isMutable();
	}

	private IType getReturnType() {
		IASTDeclSpecifier declSpecForDeduction = null;
		IASTDeclarator declaratorForDeduction = null;
		ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
		PlaceholderKind placeholder = null;
		if (lambdaDtor != null) {
			IASTTypeId trailingReturnType = lambdaDtor.getTrailingReturnType();
			if (trailingReturnType != null) {
				IASTDeclSpecifier declSpec = trailingReturnType.getDeclSpecifier();
				placeholder = CPPVisitor.usesAuto(declSpec);
				if (placeholder != null) {
					declSpecForDeduction = declSpec;
					declaratorForDeduction = trailingReturnType.getAbstractDeclarator();
				} else {
					return CPPVisitor.createType(trailingReturnType);
				}
			}
		}
		IASTCompoundStatement body = fLambdaExpression.getBody();
		if (body != null) {
			return CPPVisitor.deduceReturnType(body, declSpecForDeduction, declaratorForDeduction, placeholder);
		}
		return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
	}

	public boolean isGeneric() {
		return getInventedTemplateParameterList().length > 0;
	}

	public ICPPTemplateParameter[] getInventedTemplateParameterList() {
		if (fInventedTemplateParameters == null) {
			fInventedTemplateParameters = computeInventedTemplateParameterList();
		}
		return fInventedTemplateParameters;
	}

	public ICPPTemplateParameter[] computeInventedTemplateParameterList() {
		ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
		ICPPTemplateParameter[] result = ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
		if (lambdaDtor != null) {
			// Create an invented template parameter for every "auto" in the lambda's
			// function parameter list.
			int position = 0;
			ICPPASTParameterDeclaration[] params = lambdaDtor.getParameters();
			for (ICPPASTParameterDeclaration param : params) {
				IASTDeclSpecifier declSpec = param.getDeclSpecifier();
				if (declSpec instanceof IASTSimpleDeclSpecifier) {
					if (((IASTSimpleDeclSpecifier) declSpec).getType() == IASTSimpleDeclSpecifier.t_auto) {
						boolean isPack = param.getDeclarator().declaresParameterPack();
						result = ArrayUtil.append(result,
								new CPPImplicitTemplateTypeParameter(fLambdaExpression, position, isPack));
						position++;
					}
				}
			}
		}
		return ArrayUtil.trim(result);
	}

	private IType[] getParameterTypes() {
		if (fParameterTypes == null) {
			ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
			if (lambdaDtor != null) {
				fParameterTypes = CPPVisitor.createParameterTypes(lambdaDtor);
			} else {
				fParameterTypes = IType.EMPTY_TYPE_ARRAY;
			}
		}
		return fParameterTypes;
	}

	private ICPPEvaluation getNoexceptEvaluation() {
		ICPPEvaluation eval = null;
		if (fLambdaExpression.getDeclarator() != null)
			eval = fLambdaExpression.getDeclarator().getNoexceptEvaluation();
		return eval;
	}

	public ICPPParameter[] getParameters() {
		if (fParameters == null) {
			final IType[] parameterTypes = getParameterTypes();
			fParameters = new ICPPParameter[parameterTypes.length];
			ICPPASTFunctionDeclarator lambdaDtor = fLambdaExpression.getDeclarator();
			if (lambdaDtor != null) {
				ICPPASTParameterDeclaration[] paramDecls = lambdaDtor.getParameters();
				for (int i = 0; i < fParameters.length; i++) {
					CPPParameter param = new CPPParameter(parameterTypes[i], i);
					param.addDeclaration(paramDecls[i].getDeclarator().getName());
					fParameters[i] = param;
				}
			}
		}
		return fParameters;
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
			fScope = new ClassScope();
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
		if (type == null)
			return false;
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexBinding)
			return type.isSameType(this);
		if (!getClass().equals(type.getClass()))
			return false;
		return fLambdaExpression.getFileLocation().equals(((CPPClosureType) type).fLambdaExpression.getFileLocation());
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
			fMethods = createMethods();
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
		ICPPMethod[] methods = getMethods();
		int i = 0;
		for (; i < methods.length; i++) {
			if (!(methods[i] instanceof ICPPConstructor)) {
				break;
			}
		}
		ICPPConstructor[] result = new ICPPConstructor[i];
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
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		return ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
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
		return fLambdaExpression.getRawSignature();
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

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public int getVisibility(IBinding member) {
		for (ICPPMethod method : fMethods) {
			if (method.equals(member))
				return v_public;
		}
		throw new IllegalArgumentException(member.getName() + " is not a member of closure type '" //$NON-NLS-1$
				+ fLambdaExpression.getRawSignature() + "'"); //$NON-NLS-1$
	}

	// A lambda expression can appear in a dependent context, such as in the value of
	// a variable template, so it needs to be instantiable.
	public CPPClosureType instantiate(InstantiationContext context) {
		return new CPPClosureSpecialization(fLambdaExpression, this, context);
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
			IBinding m = getBinding(name);
			if (m != null) {
				return new IBinding[] { m };
			}
			return IBinding.EMPTY_BINDING_ARRAY;
		}

		private IBinding[] getPrefixBindings(char[] name) {
			List<IBinding> result = new ArrayList<>();
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
		public IBinding[] find(String name, IASTTranslationUnit tu) {
			return find(name);
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
			return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
		}

		/**
		 * @deprecated Use {@link #getBindings(ScopeLookupData)} instead
		 */
		@Deprecated
		@Override
		public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
				IIndexFileSet acceptLocalBindings) {
			return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
		}

		@Override
		public IBinding[] getBindings(ScopeLookupData lookup) {
			if (lookup.getLookupName() instanceof ICPPASTTemplateId)
				return IBinding.EMPTY_BINDING_ARRAY;

			if (lookup.isPrefixLookup())
				return getPrefixBindings(lookup.getLookupKey());
			return getBindings(lookup.getLookupKey());
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
