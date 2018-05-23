/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *     Marc-Andre Laperle
 *     Anders Dahlberg (Ericsson) - bug 84144
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeUptoPointers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeTransformationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.util.ReturnStatementVisitor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.AttributeUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPAliasTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumeration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespaceAlias;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPlaceholderType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPlaceholderType.PlaceholderKind;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnaryTypeTransformation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownTypeScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariableTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

/**
 * Collection of methods to extract information from a C++ translation unit.
 */
public class CPPVisitor extends ASTQueries {
	public static final String BEGIN_STR = "begin"; //$NON-NLS-1$
	public static final char[] BEGIN = BEGIN_STR.toCharArray();
	public static final char[] END = "end".toCharArray(); //$NON-NLS-1$
	public static final String STD = "std"; //$NON-NLS-1$
	private static final char[] SIZE_T = "size_t".toCharArray(); //$NON-NLS-1$
	private static final char[] PTRDIFF_T = "ptrdiff_t".toCharArray(); //$NON-NLS-1$
	private static final char[] TYPE_INFO = "type_info".toCharArray(); //$NON-NLS-1$
	private static final char[] INITIALIZER_LIST = "initializer_list".toCharArray(); //$NON-NLS-1$
	private static final char[][] EMPTY_CHAR_ARRAY_ARRAY = {};
	private static final String STD_TUPLE_SIZE_STR = STD + "::tuple_size"; //$NON-NLS-1$
	private static final String STD_TUPLE_ELEMENT_STR = STD + "::tuple_element"; //$NON-NLS-1$
	private static final String VALUE_STR = "value"; //$NON-NLS-1$
	private static final String TYPE_STR = "type"; //$NON-NLS-1$
	public static final IASTInitializerClause[] NO_ARGS = {};

	// Flags for createType().

	// Attempt to resolve placeholders ('auto' and 'decltype(auto)').
	public static final int RESOLVE_PLACEHOLDERS = 0x1;

	// Given a function declarator, compute only the return type rather than
	// the entire function type.
	public static final int ONLY_RETURN_TYPE = 0x2;

	// Common combinations of flags.
	public static final int DO_NOT_RESOLVE_PLACEHOLDERS = 0;

	// Thread-local set of declarators for which auto types are being created.
	// Used to prevent infinite recursion while processing invalid self-referencing
	// auto-type declarations.
	private static final ThreadLocal<Set<IASTDeclarator>> autoTypeDeclarators = new ThreadLocal<Set<IASTDeclarator>>() {
		@Override
		protected Set<IASTDeclarator> initialValue() {
			return new HashSet<>();
		}
	};

	public static final Predicate<IVariable> IS_STATIC_VARIABLE = IVariable::isStatic;
	private static final Predicate<IVariable> HAS_INTEGRAL_TYPE = field -> BuiltinOperators
			.isIntegral(SemanticUtil.getUltimateTypeUptoPointers(field.getType()));
	private static final Predicate<ICPPField> IS_CONSTEXPR_FIELD = field -> field.isConstexpr();
	private static final Predicate<IBinding> IS_NAMED_VALUE = binding -> binding.getName().equals(VALUE_STR);

	/**
	 * Required until Java 9 Optional.stream()
	 */
	private static final <E> Stream<E> toStream(Optional<E> optional) {
		return optional.map(Stream::of).orElse(Stream.empty());
	}

	public static IBinding createBinding(IASTName name) {
		IASTNode parent = name.getParent();
		IBinding binding = null;
		if (parent instanceof IASTNamedTypeSpecifier || parent instanceof ICPPASTBaseSpecifier
				|| parent instanceof ICPPASTConstructorChainInitializer
				|| (parent instanceof ICPPASTCapture && !(parent instanceof ICPPASTInitCapture))
				|| name.getPropertyInParent() == ICPPASTNamespaceAlias.MAPPING_NAME) {
			if (name.getLookupKey().length == 0)
				return null;

			return CPPSemantics.resolveBinding(name);
		} else if (parent instanceof ICPPASTQualifiedName) {
			if (name.getLookupKey().length == 0)
				return null;

			final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) parent;
			if (name != qname.getLastName())
				return CPPSemantics.resolveBinding(name);

			parent = parent.getParent();
			if (!declaresMemberInClassOrNamespace(qname)) {
				binding = CPPSemantics.resolveBinding(name);
				if (parent instanceof IASTCompositeTypeSpecifier) {
					if (binding instanceof IIndexBinding) {
						// Need to create an AST binding.
					} else {
						ASTInternal.addDefinition(binding, parent);
						return binding;
					}
				} else {
					return binding;
				}
			}
		} else if (parent instanceof ICPPASTTemplateId) {
			final ICPPASTTemplateId id = (ICPPASTTemplateId) parent;
			if (CPPTemplates.isClassTemplate(id))
				return CPPSemantics.resolveBinding(name);

			// Function templates/instances/specializations must be resolved via the id.
			id.resolveBinding();
			return name.getBinding();
		}

		if (parent instanceof IASTIdExpression) {
			return resolveBinding(parent);
		} else if (parent instanceof ICPPASTFieldReference) {
			return resolveBinding(parent);
		} else if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			return createBinding((ICPPASTCompositeTypeSpecifier) parent);
		} else if (parent instanceof IASTDeclarator) {
			return createBinding((IASTDeclarator) parent);
		} else if (parent instanceof ICPPASTElaboratedTypeSpecifier) {
			return createBinding((ICPPASTElaboratedTypeSpecifier) parent);
		} else if (parent instanceof ICPPASTStructuredBindingDeclaration) {
			return new CPPVariable(name);
		} else if (parent instanceof IASTDeclaration) {
			return createBinding((IASTDeclaration) parent);
		} else if (parent instanceof ICPPASTEnumerationSpecifier) {
			return createBinding((ICPPASTEnumerationSpecifier) parent);
		} else if (parent instanceof IASTEnumerator) {
			return createBinding((IASTEnumerator) parent);
		} else if (parent instanceof IASTGotoStatement) {
			return resolveBinding((IASTGotoStatement) parent);
		} else if (parent instanceof IASTLabelStatement) {
			return createBinding((IASTLabelStatement) parent);
		} else if (parent instanceof ICPPASTTemplateParameter) {
			return CPPTemplates.createBinding((ICPPASTTemplateParameter) parent);
		} else if (parent instanceof ICPPASTFieldDesignator) {
			binding = resolveBinding(parent);
		}

		if (name.getLookupKey().length > 0)
			return binding;
		return null;
	}

	private static boolean declaresMemberInClassOrNamespace(ICPPASTQualifiedName qname) {
		ICPPASTNameSpecifier[] qualifier = qname.getQualifier();
		if (qualifier.length == 0)
			return false;

		IASTNode parent = qname.getParent();
		IASTNode decl = null;
		if (parent instanceof IASTCompositeTypeSpecifier) {
			decl = parent.getParent();
		} else if (parent instanceof IASTDeclarator) {
			decl = ASTQueries.findOutermostDeclarator((IASTDeclarator) parent).getParent();
		}
		IScope inScope = null;
		while (decl != null) {
			ASTNodeProperty prop = decl.getPropertyInParent();
			if (prop == IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
				inScope = ((ICPPASTCompositeTypeSpecifier) decl.getParent()).getScope();
				break;
			} else if (prop == ICPPASTNamespaceDefinition.OWNED_DECLARATION) {
				inScope = ((ICPPASTNamespaceDefinition) decl.getParent()).getScope();
				break;
			} else if (prop == ICPPASTTemplateDeclaration.OWNED_DECLARATION) {
				decl = decl.getParent();
			} else {
				return false;
			}
		}

		if (inScope == null)
			return false;

		IBinding pb = qualifier[qualifier.length - 1].resolvePreBinding();
		if (pb instanceof IProblemBinding)
			return false;

		IScope scope = null;
		if (pb instanceof IType) {
			IType t = SemanticUtil.getNestedType((IType) pb, TDEF);
			if (t instanceof ICPPClassType) {
				scope = ((ICPPClassType) t).getCompositeScope();
			}
		} else if (pb instanceof ICPPNamespace) {
			scope = ((ICPPNamespace) pb).getNamespaceScope();
		}

		return scope == inScope;
	}

	private static IBinding resolveBinding(IASTGotoStatement gotoStatement) {
		return resolveLabel(gotoStatement.getName());
	}

	private static IBinding createBinding(IASTLabelStatement labelStatement) {
		IASTName name = labelStatement.getName();
		ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope(name);
		IBinding binding = functionScope.getBinding(name, false);
		if (binding == null || !(binding instanceof ILabel)) {
			binding = new CPPLabel(name);
			ASTInternal.addName(functionScope, name);
		} else {
			((CPPLabel) binding).setLabelStatement(name);
		}

		return binding;
	}

	private static IBinding createBinding(IASTEnumerator enumerator) {
		ICPPScope scope = (ICPPScope) getContainingScope(enumerator);
		IBinding enumtor;
		enumtor = scope.getBinding(enumerator.getName(), false);
		if (enumtor == null || !(enumtor instanceof IEnumerator)) {
			enumtor = new CPPEnumerator(enumerator.getName());
		}

		return enumtor;
	}

	private static IBinding createBinding(ICPPASTEnumerationSpecifier specifier) {
		ICPPScope scope = (ICPPScope) getContainingScope(specifier);
		final IASTName name = specifier.getName();
		IType fixedType = createEnumBaseType(specifier);
		IBinding binding = scope.getBinding(name, false);
		if (binding instanceof CPPEnumeration) {
			CPPEnumeration e = (CPPEnumeration) binding;
			if (name.equals(e.getDefinition())) {
				return e;
			}
			if (e.isScoped() == specifier.isScoped()) {
				IType ft2 = e.getFixedType();
				if (fixedType == ft2 || (fixedType != null && fixedType.isSameType(ft2))) {
					if (specifier.isOpaque()) {
						e.addDeclaration(name);
					} else if (e.getDefinition() == null) {
						e.addDefinition(name);
					} else {
						return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
					}
					return e;
				}
			}
			return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
		}
		// [dcl.enum] 7.2-5
		// "The underlying type can be explicitly specified using enum-base;
		// if not explicitly specified, the underlying type of a scoped
		// enumeration type is int."
		if (fixedType == null && specifier.isScoped()) {
			fixedType = CPPBasicType.INT;
		}
		return new CPPEnumeration(specifier, fixedType);
	}

	private static IType createEnumBaseType(ICPPASTEnumerationSpecifier specifier) {
		ICPPASTDeclSpecifier declspec = specifier.getBaseType();
		if (declspec != null) {
			IType type = createType(declspec);
			return SemanticUtil.getNestedType(type, ALLCVQ);
		}
		if (specifier.isScoped()) {
			return CPPSemantics.INT_TYPE;
		}
		return null;
	}

	private static IBinding createBinding(final ICPPASTElaboratedTypeSpecifier elabType) {
		final IASTNode parent = elabType.getParent();
		IBinding binding = null;
		boolean mustBeSimple = true;
		boolean isFriend = false;
		boolean qualified = false;
		IASTName name = elabType.getName();
		if (name instanceof ICPPASTQualifiedName) {
			qualified = true;
			name = name.getLastName();
		}
		if (parent instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) parent;
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) simpleDeclaration.getDeclSpecifier();
			IASTDeclarator[] dtors = simpleDeclaration.getDeclarators();
			isFriend = declSpec.isFriend() && dtors.length == 0;
			if (dtors.length != 0 || isFriend) {
				binding = CPPSemantics.resolveBinding(name);
				mustBeSimple = !isFriend;
			} else {
				mustBeSimple = false;
			}
		} else if (parent instanceof IASTParameterDeclaration || parent instanceof IASTDeclaration
				|| parent instanceof IASTTypeId) {
			binding = CPPSemantics.resolveBinding(elabType.getName());
		}
		if (binding instanceof IIndexBinding && binding instanceof ICPPClassType) {
			binding = (ICPPClassType) SemanticUtil.mapToAST((ICPPClassType) binding);
			ASTInternal.addDeclaration(binding, elabType);
		}

		if (binding != null && (!(binding instanceof IProblemBinding)
				|| ((IProblemBinding) binding).getID() != IProblemBinding.SEMANTIC_NAME_NOT_FOUND)) {
			return binding;
		}

		// 7.1.5.3-2 ... If name lookup does not find a declaration for the name, the elaborated-type-specifier is ill-formed
		// unless it is of the simple form class-key identifier
		if (mustBeSimple && (elabType.getName() instanceof ICPPASTQualifiedName
				|| elabType.getKind() == IASTElaboratedTypeSpecifier.k_enum)) {
			return binding;
		}

		try {
			ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration(name);
			ICPPScope scope = (ICPPScope) getContainingScope(name);
			while (scope instanceof ICPPTemplateScope) {
				scope = (ICPPScope) scope.getParent();
			}

			if (mustBeSimple) {
				// 3.3.1-5 ... the identifier is declared in the smallest non-class non-function-prototype
				// scope that contains the declaration.
				while (scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope) {
					scope = CPPSemantics.getParentScope(scope, elabType.getTranslationUnit());
				}
			}
			if (scope instanceof ICPPClassScope && isFriend && !qualified) {
				while (scope instanceof ICPPClassScope) {
					scope = CPPSemantics.getParentScope(scope, elabType.getTranslationUnit());
				}
			}
			if (scope != null) {
				binding = scope.getBinding(elabType.getName(), false);
				if (binding instanceof ICPPUsingDeclaration) {
					IBinding[] expanded = ((ICPPUsingDeclaration) binding).getDelegates();
					if (expanded.length == 1 && expanded[0] instanceof IType) {
						binding = expanded[0];
					}
				}
			}
			if (binding instanceof ICPPInternalBinding) {
				if (!name.isActive())
					return binding;

				if (binding instanceof ICPPClassType) {
					ICPPInternalBinding internalBinding = (ICPPInternalBinding) binding;
					if (templateParametersMatch((ICPPClassType) binding, templateDecl)) {
						internalBinding.addDeclaration(elabType);
						return binding;
					}

					if (CPPSemantics.declaredBefore(internalBinding, name, false)) {
						return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
					}
					markRedeclaration(internalBinding);
				}
			}

			// Create a binding.
			if (elabType.getKind() != IASTElaboratedTypeSpecifier.k_enum) {
				if (templateDecl != null) {
					binding = new CPPClassTemplate(name);
				} else {
					binding = new CPPClassType(name, binding);
				}
				// Name may live in a different scope, so make sure to add it to the owner scope as well.
				// [namespace.memdef] p3:
				// 	"If a friend declaration in a non-local class first declares a
				//	class, function, class template or function template the friend
				//	is a member of the innermost enclosing namespace. The friend
				//	declaration does not by itself make the name visible to
				//	unqualified lookup or qualified lookup."
				boolean visibleToAdlOnly = isFriend;
				ASTInternal.addName(scope, elabType.getName(), visibleToAdlOnly);
			}
		} catch (DOMException e) {
			binding = e.getProblem();
		}

		return binding;
	}

	/**
	 * Checks if the given name is the name of a friend declaration.
	 *
	 * @param name the name to check
	 * @return {@code true} if {@code name} is the name of a friend declaration
	 */
	public static boolean isNameOfFriendDeclaration(IASTNode name) {
		if (name.getPropertyInParent() == ICPPASTQualifiedName.SEGMENT_NAME) {
			ICPPASTQualifiedName qName = (ICPPASTQualifiedName) name.getParent();
			if (name != qName.getLastName())
				return false;
			name = qName;
		}
		if (name.getPropertyInParent() != ICPPASTElaboratedTypeSpecifier.TYPE_NAME)
			return false;
		ICPPASTElaboratedTypeSpecifier typeSpec = (ICPPASTElaboratedTypeSpecifier) name.getParent();
		if (typeSpec.getPropertyInParent() != IASTSimpleDeclaration.DECL_SPECIFIER)
			return false;
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) typeSpec.getParent();
		ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) declaration.getDeclSpecifier();
		return declSpec.isFriend() && declaration.getDeclarators().length == 0;
	}

	public static void markRedeclaration(final ICPPInternalBinding ib) {
		// Mark the other declarations as problem and create the binding
		final IASTNode[] decls = ib.getDeclarations();
		if (decls != null) {
			for (IASTNode decl : decls) {
				if (decl instanceof IASTName) {
					final IASTName n = (IASTName) decl;
					n.setBinding(new ProblemBinding(n, IProblemBinding.SEMANTIC_INVALID_REDECLARATION));
				}
			}
		}
		IASTNode decl = ib.getDefinition();
		if (decl instanceof IASTName) {
			final IASTName n = (IASTName) decl;
			n.setBinding(new ProblemBinding(n, IProblemBinding.SEMANTIC_INVALID_REDEFINITION));
		}
	}

	/**
	 * Tests whether a class binding matches the template parameters of another declaration
	 */
	private static boolean templateParametersMatch(ICPPClassType binding, ICPPASTTemplateDeclaration templateDecl) {
		final boolean isTemplate = binding instanceof ICPPClassTemplate;
		if (templateDecl == null)
			return !isTemplate;
		if (!isTemplate)
			return false;

		ICPPTemplateParameter[] pars1 = ((ICPPClassTemplate) binding).getTemplateParameters();
		ICPPASTTemplateParameter[] pars2 = templateDecl.getTemplateParameters();

		int i = 0;
		for (ICPPASTTemplateParameter p2 : pars2) {
			if (i >= pars1.length)
				return true;

			if (!CPPSemantics.isSameTemplateParameter(pars1[i++], p2))
				return false;
		}
		return true;
	}

	private static IBinding createBinding(ICPPASTCompositeTypeSpecifier compType) {
		IASTName name = compType.getName().getLastName();
		if (name instanceof ICPPASTTemplateId)
			return CPPTemplates.createBinding((ICPPASTTemplateId) name);

		ICPPScope scope = (ICPPScope) getContainingScope(name);
		try {
			while (scope instanceof ICPPTemplateScope) {
				scope = (ICPPScope) scope.getParent();
			}
		} catch (DOMException e) {
			return e.getProblem();
		}

		// Can't lookup anonymous names.
		IBinding binding = null;
		ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration(name);
		if (name.getLookupKey().length > 0 && scope != null) {
			binding = scope.getBinding(name, false);

			if (binding instanceof ICPPInternalBinding && binding instanceof ICPPClassType && name.isActive()) {
				ICPPInternalBinding internalBinding = (ICPPInternalBinding) binding;
				if (internalBinding.getDefinition() == null
						&& templateParametersMatch((ICPPClassType) binding, templateDecl)) {
					ASTInternal.addDefinition(internalBinding, compType);
					return binding;
				}
				if (CPPSemantics.declaredBefore(internalBinding, name, false)) {
					return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
				}
				markRedeclaration(internalBinding);
			}
		}
		if (templateDecl != null)
			return new CPPClassTemplate(name);

		return new CPPClassType(name, binding);
	}

	private static IBinding createBinding(IASTDeclaration declaration) {
		if (declaration instanceof ICPPASTNamespaceDefinition) {
			ICPPASTNamespaceDefinition namespaceDef = (ICPPASTNamespaceDefinition) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope(namespaceDef);
			IBinding binding;
			binding = scope.getBinding(namespaceDef.getName(), false);
			if (!(binding instanceof ICPPInternalBinding) || binding instanceof IProblemBinding
					|| !(binding instanceof ICPPNamespace)) {
				binding = new CPPNamespace(namespaceDef);
			}
			return binding;
		} else if (declaration instanceof ICPPASTUsingDirective) {
			return CPPSemantics.resolveBinding(((ICPPASTUsingDirective) declaration).getQualifiedName());
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
			ICPPASTNamespaceAlias alias = (ICPPASTNamespaceAlias) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope(declaration);
			IBinding binding;
			binding = scope.getBinding(alias.getAlias(), false);
			if (!(binding instanceof ICPPInternalBinding)) {
				IBinding namespace = alias.getMappingName().resolveBinding();
				if (namespace instanceof IProblemBinding) {
					IProblemBinding problem = (IProblemBinding) namespace;
					namespace = new CPPNamespace.CPPNamespaceProblem(problem.getASTNode(), problem.getID(),
							alias.getMappingName().toCharArray());
				}
				if (namespace instanceof ICPPNamespace) {
					binding = new CPPNamespaceAlias(alias.getAlias(), (ICPPNamespace) namespace);
				} else {
					binding = new ProblemBinding(alias.getAlias(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND);
				}
			}
			return binding;
		} else if (declaration instanceof ICPPASTAliasDeclaration) {
			ICPPASTAliasDeclaration alias = (ICPPASTAliasDeclaration) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope(declaration);
			IBinding binding = scope.getBinding(alias.getAlias(), false);
			if (!(binding instanceof ICPPInternalBinding)) {
				IType type = createType(alias.getMappingTypeId());
				if (type instanceof IProblemBinding) {
					IProblemBinding problem = (IProblemBinding) type;
					type = new CPPClassType.CPPClassTypeProblem(problem.getASTNode(), problem.getID(),
							alias.getMappingTypeId().getAbstractDeclarator().getName().toCharArray());
				}
				if (type != null) {
					if (alias.getParent() instanceof ICPPASTTemplateDeclaration) {
						binding = new CPPAliasTemplate(alias.getAlias(), type);
					} else {
						CPPTypedef typedef = new CPPTypedef(alias.getAlias());
						typedef.setType(type);
						binding = typedef;
					}
				} else {
					binding = new ProblemBinding(alias.getAlias(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND);
				}
			}
			return binding;
		}
		return null;
	}

	private static int findParameterIndex(IASTParameterDeclaration param, IASTParameterDeclaration[] params) {
		int i = 0;
		for (; i < params.length; i++) {
			if (params[i] == param)
				return i;
		}
		return -1;
	}

	private static IBinding createBinding(IASTDeclarator declarator) {
		IASTNode parent = findOutermostDeclarator(declarator).getParent();

		if (parent instanceof ICPPASTInitCapture) {
			return new CPPVariable(declarator.getName());
		}

		declarator = findInnermostDeclarator(declarator);

		final IASTDeclarator typeRelevantDtor = findTypeRelevantDeclarator(declarator);

		IASTName name = declarator.getName().getLastName();

		// In case the binding was created starting from another name within the declarator.
		IBinding candidate = name.getBinding();
		if (candidate != null) {
			return candidate;
		}

		// Function type.
		if (parent instanceof IASTTypeId)
			return CPPSemantics.resolveBinding(name);

		// Function type for non-type template parameter.
		ASTNodeProperty prop = parent.getPropertyInParent();
		if (prop == ICPPASTTemplateDeclaration.PARAMETER || prop == ICPPASTTemplatedTypeTemplateParameter.PARAMETER) {
			return CPPTemplates.createBinding((ICPPASTTemplateParameter) parent);
		}

		// Explicit instantiations.
		if (prop == ICPPASTExplicitTemplateInstantiation.OWNED_DECLARATION)
			return CPPSemantics.resolveBinding(name);

		// Explicit specializations.
		ICPPASTTemplateDeclaration tmplDecl = CPPTemplates.getTemplateDeclaration(name);
		if (tmplDecl instanceof ICPPASTTemplateSpecialization) {
			IBinding b = CPPSemantics.resolveBinding(name);
			if (parent instanceof ICPPASTFunctionDefinition) {
				ASTInternal.addDefinition(b, name);
			} else {
				ASTInternal.addDeclaration(b, name);
			}
			return b;
		}

		// Parameter declarations.
		if (parent instanceof ICPPASTParameterDeclaration) {
			ICPPASTParameterDeclaration param = (ICPPASTParameterDeclaration) parent;
			parent = param.getParent();
			if (parent instanceof IASTStandardFunctionDeclarator) {
				IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) param.getParent();
				// Create parameter bindings only if the declarator declares a function.
				if (findTypeRelevantDeclarator(fdtor) != fdtor)
					return null;

				final IASTNode dtorParent = findOutermostDeclarator(fdtor).getParent();
				if (dtorParent instanceof ICPPASTLambdaExpression) {
					CPPClosureType closure = (CPPClosureType) ((ICPPASTLambdaExpression) dtorParent)
							.getExpressionType();
					ICPPParameter[] paramBindings = closure.getParameters();
					int index = findParameterIndex(param, fdtor.getParameters());
					if (index >= 0 && index < paramBindings.length) {
						return paramBindings[index];
					}
				}

				if (dtorParent instanceof IASTDeclaration) {
					int index = findParameterIndex(param, fdtor.getParameters());
					return new CPPParameter(name, index);
				}
				return null;
			} else if (parent instanceof ICPPASTTemplateDeclaration) {
				return CPPTemplates.createBinding(param);
			}
			return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_TYPE);
		}

		// Function declaration/definition.
		IBinding binding = null;
		final boolean template = tmplDecl != null;
		boolean isFriendDecl = false;
		ICPPScope scope = (ICPPScope) getContainingNonTemplateScope(name);
		if (scope instanceof ICPPClassScope) {
			isFriendDecl = isFriendDeclaration(parent);
			if (isFriendDecl) {
				try {
					while (scope.getKind() == EScopeKind.eClassType) {
						scope = CPPSemantics.getParentScope(scope, name.getTranslationUnit());
					}
				} catch (DOMException e1) {
				}
			}
		}
		boolean forceResolve = isFriendDecl && name instanceof ICPPASTTemplateId;
		if (name.getLookupKey().length != 0 && scope != null) {
			binding = scope.getBinding(name, forceResolve);
		}

		boolean isFunction = false;
		if (parent instanceof ICPPASTFunctionDefinition) {
			isFunction = true;
		} else if (parent instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;
			if (simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				// Typedef declaration.
				if (binding instanceof ICPPInternalBinding && binding instanceof ITypedef && name.isActive()) {
					IType t1 = ((ITypedef) binding).getType();
					IType t2 = createType(declarator);
					if (t1 != null && t2 != null && t1.isSameType(t2)) {
						ASTInternal.addDeclaration(binding, name);
						return binding;
					}
					return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
				}
				// If we don't resolve the target type first, we get a problem binding in case
				// the typedef redeclares the target type, otherwise it is safer to defer
				// the resolution of the target type.
				IType targetType = createType(declarator);
				CPPTypedef td = new CPPTypedef(name);
				td.setType(targetType);
				binding = td;
			} else if (typeRelevantDtor instanceof IASTFunctionDeclarator) {
				// Function declaration via function declarator.
				isFunction = true;
			} else {
				// Looks like a variable declaration.
				IType t1 = createType(declarator);
				if (SemanticUtil.getNestedType(t1, TDEF) instanceof IFunctionType) {
					// Function declaration via a typedef for a function type
					isFunction = true;
				} else if (binding instanceof IParameter) {
					// Variable declaration redeclaring a parameter.
					binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
				} else {
					// Variable declaration.
					IType t2 = null;
					if (binding != null && binding instanceof IVariable && !(binding instanceof IIndexBinding)) {
						t2 = ((IVariable) binding).getType();
					}
					if (t1 != null && t2 != null) {
						if (areArraysOfTheSameElementType(t1, t2) || t1.isSameType(t2)) {
							ASTInternal.addDeclaration(binding, name);
						} else {
							binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
						}
					} else if (simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
						binding = new CPPField(name);
					} else if (template) {
						if (simpleDecl.getParent().getParent() instanceof ICPPASTCompositeTypeSpecifier) {
							binding = new CPPFieldTemplate(name);
						} else {
							binding = new CPPVariableTemplate(name);
						}
					} else {
						binding = new CPPVariable(name);
					}
				}
			}
		}

		if (isFunction) {
			if (binding instanceof ICPPInternalBinding && binding instanceof ICPPFunction && name.isActive()) {
				ICPPFunction function = (ICPPFunction) binding;
				boolean sameFunction = CPPSemantics.isSameFunction(function, typeRelevantDtor)
						|| function instanceof ICPPDeferredFunction;
				if (function.getOwner() instanceof ICPPClassType) {
					// Don't consider a function brought into scope from a base class scope
					// to be the same as a function declared in a derived class scope.
					IScope bindingScope = ((ICPPClassType) function.getOwner()).getCompositeScope();
					if (bindingScope == null || !bindingScope.equals(scope)) {
						sameFunction = false;
					}
				}
				if (sameFunction) {
					binding = CPPSemantics.checkDeclSpecifier(binding, name, parent);
					if (binding instanceof IProblemBinding)
						return binding;

					ICPPInternalBinding internal = (ICPPInternalBinding) function;
					if (parent instanceof IASTSimpleDeclaration) {
						ASTInternal.addDeclaration(internal, name);
					} else if (internal.getDefinition() == null) {
						ASTInternal.addDefinition(internal, name);
					} else {
						IASTNode def = internal.getDefinition();
						if (def instanceof IASTDeclarator)
							def = ((IASTDeclarator) def).getName();
						if (def != name) {
							return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
						}
					}

					return function;
				}
			}

			if (scope instanceof ICPPClassScope) {
				if (isConstructor(scope, typeRelevantDtor)) {
					binding = template ? (ICPPConstructor) new CPPConstructorTemplate(name)
							: new CPPConstructor((ICPPASTFunctionDeclarator) typeRelevantDtor);
				} else {
					binding = template ? (ICPPMethod) new CPPMethodTemplate(name) : new CPPMethod(typeRelevantDtor);
				}
			} else {
				binding = template ? (ICPPFunction) new CPPFunctionTemplate(name) : new CPPFunction(typeRelevantDtor);
			}
			binding = CPPSemantics.checkDeclSpecifier(binding, name, parent);
			if (isFriendDecl && scope instanceof IASTInternalScope) {
				((IASTInternalScope) scope).addBinding(binding);
			}
		}

		return binding;
	}

	public static boolean isFriendDeclaration(IASTNode decl) {
		IASTDeclSpecifier declSpec;
		if (decl instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
		} else if (decl instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition) decl).getDeclSpecifier();
		} else {
			return false;
		}
		return declSpec instanceof ICPPASTDeclSpecifier && ((ICPPASTDeclSpecifier) declSpec).isFriend();
	}

	public static boolean isConstructor(IScope containingScope, IASTDeclarator declarator) {
		if (containingScope instanceof ICPPClassScope && isConstructorDtor(declarator)) {
			ICPPClassType classType = ((ICPPClassScope) containingScope).getClassType();
			final char[] dtorName = findInnermostDeclarator(declarator).getName().getLookupKey();
			return CharArrayUtils.equals(dtorName, classType.getNameCharArray());
		}
		return false;
	}

	public static boolean isConstructorDeclaration(IASTName name) {
		if (name == null)
			return false;
		final ASTNodeProperty propertyInParent = name.getPropertyInParent();
		if (propertyInParent == null)
			return false;
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTTemplateId) {
			name = (IASTName) parent;
			parent = name.getParent();
		}
		if (parent instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) parent).getLastName() != name)
				return false;
			name = (IASTName) parent;
			parent = name.getParent();
		}
		if (parent instanceof IASTDeclarator) {
			if (isConstructorDtor((IASTDeclarator) parent)) {
				if (name instanceof ICPPASTQualifiedName) {
					ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) name).getQualifier();
					if (qualifier.length >= 1) {
						IBinding b = qualifier[qualifier.length - 1].resolvePreBinding();
						if (b instanceof IType) {
							IType classType = getNestedType((IType) b, TDEF);
							if (classType instanceof ICPPClassType) {
								final char[] dtorName = name.getLastName().getLookupKey();
								final char[] className = ((ICPPClassType) classType).getNameCharArray();
								return CharArrayUtils.equals(dtorName, className);
							}
						}
					}
					return false;
				}
				while (parent != null) {
					if (parent instanceof ICPPASTCompositeTypeSpecifier) {
						final char[] className = ((ICPPASTCompositeTypeSpecifier) parent).getName().getLastName()
								.getLookupKey();
						final char[] dtorName = name.getLookupKey();
						return CharArrayUtils.equals(dtorName, className);
					}
					parent = parent.getParent();
				}
			}
		}
		return false;
	}

	private static boolean isConstructorDtor(IASTDeclarator declarator) {
		if (declarator == null || !(declarator instanceof IASTFunctionDeclarator))
			return false;

		IASTDeclSpecifier declSpec = null;
		IASTNode parent = findOutermostDeclarator(declarator).getParent();
		if (parent instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
		} else if (parent instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition) parent).getDeclSpecifier();
		}
		if (declSpec != null && declSpec instanceof IASTSimpleDeclSpecifier) {
			return (((IASTSimpleDeclSpecifier) declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified);
		}

		return false;
	}

	public static boolean isLastNameInUsingDeclaration(IASTName name) {
		IASTNode parent = name.getParent();
		return parent instanceof ICPPASTQualifiedName && ((ICPPASTQualifiedName) parent).getLastName() == name
				&& parent.getParent() instanceof ICPPASTUsingDeclaration;
	}

	public static IScope getContainingNonTemplateScope(final IASTNode inputNode) {
		IScope scope = getContainingScope(inputNode);
		while (scope instanceof ICPPTemplateScope) {
			scope = getContainingScope(((ICPPTemplateScope) scope).getTemplateDeclaration());
		}
		return scope;
	}

	public static IScope getContainingScope(final IASTNode inputNode) {
		if (inputNode == null || inputNode instanceof IASTTranslationUnit)
			return null;
		IASTNode node = inputNode;
		while (node != null) {
			if (node instanceof IASTName && !(node instanceof ICPPASTQualifiedName)) {
				return getContainingScope((IASTName) node);
			}
			if (node instanceof IASTDeclaration) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTTranslationUnit) {
					return ((IASTTranslationUnit) parent).getScope();
				} else if (parent instanceof IASTDeclarationStatement) {
					return getContainingScope((IASTStatement) parent);
				} else if (parent instanceof IASTForStatement) {
					return ((IASTForStatement) parent).getScope();
				} else if (parent instanceof ICPPASTRangeBasedForStatement) {
					return ((ICPPASTRangeBasedForStatement) parent).getScope();
				} else if (parent instanceof IASTCompositeTypeSpecifier) {
					return ((IASTCompositeTypeSpecifier) parent).getScope();
				} else if (parent instanceof ICPPASTNamespaceDefinition) {
					return ((ICPPASTNamespaceDefinition) parent).getScope();
				} else if (parent instanceof ICPPASTSwitchStatement) {
					return ((ICPPASTSwitchStatement) parent).getScope();
				} else if (parent instanceof ICPPASTIfStatement) {
					return ((ICPPASTIfStatement) parent).getScope();
				} else if (parent instanceof ICPPASTWhileStatement) {
					return ((ICPPASTWhileStatement) parent).getScope();
				} else if (parent instanceof ICPPASTTemplateDeclaration) {
					return ((ICPPASTTemplateDeclaration) parent).getScope();
				} else if (parent instanceof ICPPASTCatchHandler) {
					return ((ICPPASTCatchHandler) parent).getScope();
				}
			} else if (node instanceof IASTStatement) {
				return getContainingScope((IASTStatement) node);
			} else if (node instanceof IASTTypeId) {
				ASTNodeProperty prop = node.getPropertyInParent();
				if (prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT || prop == ICPPASTConversionName.TYPE_ID) {
					node = node.getParent(); // template-id or conversion name
					while (node instanceof IASTName) {
						node = node.getParent();
					}
					continue;
				} else if (prop == ICPPASTFunctionDeclarator.TRAILING_RETURN_TYPE
						|| prop == ICPPASTFunctionDeclarator.EXCEPTION_TYPEID) {
					IScope result = scopeViaFunctionDtor((ICPPASTFunctionDeclarator) node.getParent());
					if (result != null)
						return result;

				}
			} else if (node instanceof IASTParameterDeclaration
					|| node.getPropertyInParent() == ICPPASTFunctionDeclarator.NOEXCEPT_EXPRESSION) {
				IASTNode parent = node.getParent();
				if (parent instanceof ICPPASTFunctionDeclarator) {
					IScope result = scopeViaFunctionDtor((ICPPASTFunctionDeclarator) parent);
					if (result != null)
						return result;
				} else if (parent instanceof ICPPASTTemplateDeclaration) {
					return ((ICPPASTTemplateDeclaration) parent).getScope();
				}
			} else if (node instanceof IASTInitializer) {
				if (node instanceof ICPPASTConstructorChainInitializer) {
					// The name of the member initializer is resolved in the scope of
					// the owner of the ctor.
					ICPPASTConstructorChainInitializer initializer = (ICPPASTConstructorChainInitializer) node;
					IASTFunctionDefinition fdef = (IASTFunctionDefinition) initializer.getParent();
					IBinding binding = fdef.getDeclarator().getName().resolveBinding();
					try {
						return binding.getScope();
					} catch (DOMException e) {
					}
				} else {
					IASTNode parent = node.getParent();
					if (parent instanceof IASTDeclarator) {
						IASTDeclarator dtor = (IASTDeclarator) parent;
						IASTName name = dtor.getName();
						if (name instanceof ICPPASTQualifiedName) {
							return getContainingScope(name.getLastName());
						}
					} else if (parent instanceof ICPPASTConstructorChainInitializer) {
						// The initializer for the member initializer is resolved in
						// the body of the ctor.
						IASTNode temp = getContainingBlockItem(node);
						if (temp instanceof IASTFunctionDefinition) {
							IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition) temp)
									.getBody();
							return body.getScope();
						}
						node = parent;
					}
				}
			} else if (node instanceof IASTExpression) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTForStatement) {
					return ((IASTForStatement) parent).getScope();
				} else if (parent instanceof ICPPASTRangeBasedForStatement) {
					return ((ICPPASTRangeBasedForStatement) parent).getScope();
				} else if (parent instanceof ICPPASTIfStatement) {
					return ((ICPPASTIfStatement) parent).getScope();
				} else if (parent instanceof ICPPASTSwitchStatement) {
					return ((ICPPASTSwitchStatement) parent).getScope();
				} else if (parent instanceof ICPPASTWhileStatement) {
					return ((ICPPASTWhileStatement) parent).getScope();
				} else if (parent instanceof IASTCompoundStatement) {
					return ((IASTCompoundStatement) parent).getScope();
				} else if (parent instanceof IASTArrayModifier) {
					IASTNode d = parent.getParent();
					while (!(d instanceof IASTDeclarator || d instanceof IASTExpression)) {
						d = d.getParent();
					}
					if (d instanceof IASTDeclarator) {
						IASTDeclarator dtor = (IASTDeclarator) d;
						while (dtor.getNestedDeclarator() != null)
							dtor = dtor.getNestedDeclarator();
						IASTName name = dtor.getName();
						if (name instanceof ICPPASTQualifiedName) {
							return getContainingScope(name.getLastName());
						}
					}
				} else if (parent instanceof ICPPASTTemplateId
						&& node.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
					node = parent; // template-id
					while (node instanceof IASTName) {
						node = node.getParent();
					}
					continue;
				}
			} else if (node instanceof ICPPASTTemplateParameter) {
				if (node instanceof ICPPASTTemplatedTypeTemplateParameter && node != inputNode) {
					return ((ICPPASTTemplatedTypeTemplateParameter) node).asScope();
				}
				IASTNode parent = node.getParent();
				if (parent instanceof ICPPASTTemplateDeclaration) {
					return ((ICPPASTTemplateDeclaration) parent).getScope();
				}
			} else if (node instanceof ICPPASTBaseSpecifier) {
				ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) node.getParent();
				IASTName n = compSpec.getName().getLastName();
				return getContainingScope(n);
			} else if (node instanceof IASTEnumerator) {
				node = node.getParent();
				if (node instanceof ICPPASTEnumerationSpecifier) {
					ICPPASTEnumerationSpecifier enumSpec = (ICPPASTEnumerationSpecifier) node;
					IBinding binding = enumSpec.getName().resolveBinding();
					if (binding instanceof ICPPEnumeration) {
						ICPPEnumeration enumType = (ICPPEnumeration) binding;
						if (enumType.isScoped()) {
							return enumType.asScope();
						}
					}
				}
			}
			node = node.getParent();
		}
		return new CPPScope.CPPScopeProblem(inputNode, IProblemBinding.SEMANTIC_BAD_SCOPE,
				inputNode.getRawSignature().toCharArray());
	}

	private static IScope scopeViaFunctionDtor(ICPPASTFunctionDeclarator dtor) {
		if (ASTQueries.findTypeRelevantDeclarator(dtor) == dtor) {
			IASTDeclarator outerDtor = ASTQueries.findOutermostDeclarator(dtor);
			ASTNodeProperty prop = outerDtor.getPropertyInParent();
			if (prop == IASTSimpleDeclaration.DECLARATOR) {
				return dtor.getFunctionScope();
			}
			if (prop == IASTFunctionDefinition.DECLARATOR) {
				final IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition) outerDtor
						.getParent()).getBody();
				if (body != null)
					return body.getScope();
				return dtor.getFunctionScope();
			}
			if (prop == ICPPASTLambdaExpression.DECLARATOR) {
				final IASTCompoundStatement body = ((ICPPASTLambdaExpression) outerDtor.getParent()).getBody();
				if (body != null)
					return body.getScope();
				return dtor.getFunctionScope();
			}
		}
		return null;
	}

	public static IScope getContainingScope(IASTName name) {
		IScope scope = getContainingScopeOrNull(name);
		if (scope == null) {
			return new CPPScope.CPPScopeProblem(name, IProblemBinding.SEMANTIC_BAD_SCOPE);
		}

		return scope;
	}

	private static IScope getContainingScopeOrNull(IASTName name) {
		if (name == null) {
			return null;
		}
		IASTNode parent = name.getParent();
		try {
			if (parent instanceof ICPPASTTemplateId) {
				name = (IASTName) parent;
				parent = name.getParent();
			}

			if (parent instanceof ICPPASTQualifiedName) {
				final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) parent;
				final ICPPASTNameSpecifier[] qualifiers = qname.getQualifier();
				int i = 0;
				for (; i < qualifiers.length; i++) {
					if (qualifiers[i] == name)
						break;
				}
				final IASTTranslationUnit tu = parent.getTranslationUnit();
				if (i == 0) {
					if (qname.isFullyQualified()) {
						if (tu == null)
							return null;
						return tu.getScope();
					}
					if (qname.getParent() instanceof ICPPASTFieldReference) {
						name = qname;
						parent = name.getParent();
					}
				} else { // i > 0
					// For template functions we may need to resolve a template parameter
					// as a parent of an unknown type used as parameter type.
					IBinding binding = qualifiers[i - 1].resolvePreBinding();

					// 7.1.3-7 Unwrap typedefs, delete cv-qualifiers.
					if (binding instanceof ITypedef) {
						IType type = getNestedType((ITypedef) binding, TDEF | CVTYPE);
						if (type instanceof IBinding) {
							binding = (IBinding) type;
						}
					}
					boolean done = true;
					IScope scope = null;
					if (binding instanceof ICPPClassType) {
						binding = (ICPPClassType) SemanticUtil.mapToAST((ICPPClassType) binding);
						scope = ((ICPPClassType) binding).getCompositeScope();
					} else if (binding instanceof ICPPNamespace) {
						scope = ((ICPPNamespace) binding).getNamespaceScope();
					} else if (binding instanceof ICPPEnumeration) {
						scope = ((ICPPEnumeration) binding).asScope();
					} else if (binding instanceof ICPPUnknownBinding) {
						scope = ((ICPPUnknownBinding) binding).asScope();
					} else if (binding instanceof IProblemBinding) {
						if (binding instanceof ICPPScope)
							scope = (IScope) binding;
					} else {
						done = false;
					}
					if (done) {
						if (scope == null) {
							return new CPPScope.CPPScopeProblem(qualifiers[i - 1], IProblemBinding.SEMANTIC_BAD_SCOPE,
									null);
						}
						return scope;
					}
				}
			}

			if (parent instanceof ICPPASTFieldReference) {
				final ICPPASTFieldReference fieldReference = (ICPPASTFieldReference) parent;
				IType type = fieldReference.getFieldOwnerType();
				if (type instanceof ICPPParameterPackType) {
					type = ((ICPPParameterPackType) type).getType();
				}
				type = getUltimateTypeUptoPointers(type);
				if (type instanceof ICPPClassType) {
					type = SemanticUtil.mapToAST(type);
					return ((ICPPClassType) type).getCompositeScope();
				} else if (type instanceof ICPPUnknownBinding) {
					return ((ICPPUnknownBinding) type).asScope();
				} else if (type instanceof ICPPUnknownType) {
					return new CPPUnknownTypeScope(type, null);
				} else {
					return new CPPScope.CPPScopeProblem(name, ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
				}
			} else if (parent instanceof ICPPASTFieldDesignator) {
				IType type = null;
				IASTNode node = parent;
				do {
					if (node instanceof ICPPASTDeclarator) {
						type = createType((ICPPASTDeclarator) node);
						break;
					}
					if (node instanceof ICPPASTSimpleTypeConstructorExpression) {
						type = ((ICPPASTSimpleTypeConstructorExpression) node).getExpressionType();
						break;
					}
				} while ((node = node.getParent()) != null);

				if (type != null) {
					type = getNestedType(type, TDEF | CVTYPE);
					if (type instanceof ICPPClassType) {
						type = SemanticUtil.mapToAST(type);
						return ((ICPPClassType) type).getCompositeScope();
					}
				}
				return new CPPScope.CPPScopeProblem(name, ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
			} else if (parent instanceof IASTGotoStatement || parent instanceof IASTLabelStatement) {
				while (!(parent instanceof IASTFunctionDefinition)) {
					parent = parent.getParent();
				}
				IASTFunctionDefinition fdef = (IASTFunctionDefinition) parent;
				return ((ICPPASTFunctionDeclarator) fdef.getDeclarator()).getFunctionScope();
			}
		} catch (DOMException e) {
			IProblemBinding problem = e.getProblem();
			if (problem instanceof ICPPScope)
				return problem;
			return new CPPScope.CPPScopeProblem(problem.getASTNode(), problem.getID(), problem.getNameCharArray());
		}
		return getContainingScope(parent);
	}

	public static IScope getContainingScope(IASTStatement statement) {
		IASTNode parent = statement.getParent();
		IScope scope = null;
		if (parent instanceof IASTCompoundStatement) {
			IASTCompoundStatement compound = (IASTCompoundStatement) parent;
			scope = compound.getScope();
		} else if (parent instanceof IASTForStatement) {
			scope = ((IASTForStatement) parent).getScope();
		} else if (parent instanceof ICPPASTRangeBasedForStatement) {
			scope = ((ICPPASTRangeBasedForStatement) parent).getScope();
		} else if (parent instanceof ICPPASTSwitchStatement) {
			scope = ((ICPPASTSwitchStatement) parent).getScope();
		} else if (parent instanceof ICPPASTIfStatement) {
			scope = ((ICPPASTIfStatement) parent).getScope();
		} else if (parent instanceof ICPPASTWhileStatement) {
			scope = ((ICPPASTWhileStatement) parent).getScope();
		} else if (parent instanceof IASTStatement) {
			scope = getContainingScope((IASTStatement) parent);
		} else if (parent instanceof IASTFunctionDefinition) {
			final IASTFunctionDefinition fdef = (IASTFunctionDefinition) parent;
			if (statement instanceof ICPPASTCatchHandler)
				return fdef.getScope();

			IASTFunctionDeclarator fnDeclarator = fdef.getDeclarator();
			IASTName name = findInnermostDeclarator(fnDeclarator).getName().getLastName();
			return getContainingScope(name);
		}

		if (scope == null)
			return getContainingScope(parent);
		return scope;
	}

	public static IASTNode getContainingBlockItem(IASTNode node) {
		if (node == null)
			return null;
		if (node.getPropertyInParent() == null)
			return null;

		for (IASTNode parent = node.getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof IASTDeclaration) {
				IASTNode p = parent.getParent();
				if (p instanceof IASTDeclarationStatement)
					return p;
				return parent;
			} else if (parent instanceof IASTExpression) {
				IASTNode p = parent.getParent();
				if (p instanceof IASTForStatement)
					return parent;
				if (p instanceof ICPPASTRangeBasedForStatement)
					return parent;
				if (p instanceof IASTStatement)
					return p;
			} else if (parent instanceof IASTStatement || parent instanceof IASTTranslationUnit) {
				return parent;
			} else if (parent instanceof IASTFunctionDeclarator
					&& node.getPropertyInParent() == IASTStandardFunctionDeclarator.FUNCTION_PARAMETER) {
				return node;
			} else if (parent instanceof IASTEnumerationSpecifier.IASTEnumerator) {
				return parent;
			}
			node = parent;
		}
		return null;
	}

	static private IBinding resolveBinding(IASTNode node) {
		IASTName name = null;
		while (node != null) {
			if (node instanceof IASTIdExpression) {
				name = ((IASTIdExpression) node).getName();
				if (isLabelReference(node)) {
					return resolveLabel(name);
				}
				break;
			} else if (node instanceof ICPPASTFieldReference) {
				name = ((ICPPASTFieldReference) node).getFieldName();
				break;
			} else if (node instanceof ICPPASTFieldDesignator) {
				name = ((ICPPASTFieldDesignator) node).getName();
				break;
			} else if (node instanceof IASTFunctionCallExpression) {
				node = ((IASTFunctionCallExpression) node).getFunctionNameExpression();
			} else if (node instanceof IASTUnaryExpression) {
				node = ((IASTUnaryExpression) node).getOperand();
			} else if (node instanceof IASTBinaryExpression) {
				node = ((IASTBinaryExpression) node).getOperand2();
			} else {
				node = null;
			}
		}
		if (name != null) {
			name = name.getLastName();
			IBinding binding = name.getPreBinding();
			if (binding == null) {
				binding = CPPSemantics.resolveBinding(name);
				name.setBinding(binding);
				if (name instanceof ICPPASTTemplateId && binding instanceof ICPPSpecialization) {
					((ICPPASTTemplateId) name).getTemplateName()
							.setBinding(((ICPPSpecialization) binding).getSpecializedBinding());
				}
			}
			return binding;
		}
		return null;
	}

	private static class CollectProblemsAction extends ASTGenericVisitor {
		private List<IASTProblem> fProblems = null;

		CollectProblemsAction() {
			super(true);
		}

		private void addProblem(IASTProblem problem) {
			if (fProblems == null) {
				fProblems = new ArrayList<>();
			}
			fProblems.add(problem);
		}

		public IASTProblem[] getProblems() {
			if (fProblems == null)
				return new IASTProblem[0];

			return fProblems.toArray(new IASTProblem[fProblems.size()]);
		}

		@Override
		public int genericVisit(IASTNode node) {
			if (node instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder) node).getProblem());

			return PROCESS_CONTINUE;
		}
	}

	public static class CollectDeclarationsAction extends ASTVisitor {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] decls;
		private IBinding[] bindings;
		private int idx = 0;
		private int kind;
		private char[] requiredName;
		private IIndex index;
		private boolean permissive;

		private static final int KIND_LABEL = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE = 3;
		private static final int KIND_NAMESPACE = 4;
		private static final int KIND_COMPOSITE = 5;
		private static final int KIND_TEMPLATE_PARAMETER = 6;

		public CollectDeclarationsAction(IBinding binding, boolean permissive) {
			shouldVisitTranslationUnit = true;
			shouldVisitNames = true;
			this.decls = new IASTName[DEFAULT_LIST_SIZE];
			this.permissive = permissive;

			final String bname = binding.getName();
			if (bname.length() > 0 && !bname.startsWith("operator")) { //$NON-NLS-1$
				requiredName = bname.toCharArray();
			}
			this.bindings = new IBinding[] { binding };
			if (binding instanceof ICPPUsingDeclaration) {
				this.bindings = ((ICPPUsingDeclaration) binding).getDelegates();
				kind = KIND_COMPOSITE;
			} else if (binding instanceof ILabel) {
				kind = KIND_LABEL;
			} else if (binding instanceof ICPPTemplateParameter) {
				kind = KIND_TEMPLATE_PARAMETER;
			} else if (binding instanceof ICompositeType || binding instanceof ICPPAliasTemplate
					|| binding instanceof ITypedef || binding instanceof IEnumeration) {
				kind = KIND_TYPE;
			} else if (binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			} else if (binding instanceof IParameter) {
				requiredName = null;
				kind = KIND_OBJ_FN;
			} else {
				kind = KIND_OBJ_FN;
			}
		}

		@Override
		public int visit(IASTTranslationUnit tu) {
			index = tu.getIndex();
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName)
				return PROCESS_CONTINUE;
			if (requiredName != null && !CharArrayUtils.equals(name.getLookupKey(), requiredName)) {
				return PROCESS_CONTINUE;
			}

			ASTNodeProperty prop = name.getPropertyInParent();
			if (prop == ICPPASTQualifiedName.SEGMENT_NAME)
				prop = name.getParent().getPropertyInParent();

			switch (kind) {
			case KIND_TEMPLATE_PARAMETER:
				if (prop == ICPPASTSimpleTypeTemplateParameter.PARAMETER_NAME
						|| prop == ICPPASTTemplatedTypeTemplateParameter.PARAMETER_NAME) {
					break;
				} else if (prop == IASTDeclarator.DECLARATOR_NAME) {
					IASTNode d = name.getParent();
					while (d.getParent() instanceof IASTDeclarator)
						d = d.getParent();
					if (d.getPropertyInParent() == IASTParameterDeclaration.DECLARATOR) {
						break;
					}
				}
				return PROCESS_CONTINUE;

			case KIND_LABEL:
				if (prop == IASTLabelStatement.NAME)
					break;
				return PROCESS_CONTINUE;

			case KIND_TYPE:
			case KIND_COMPOSITE:
				if (prop == IASTCompositeTypeSpecifier.TYPE_NAME || prop == ICPPASTAliasDeclaration.ALIAS_NAME
						|| prop == IASTEnumerationSpecifier.ENUMERATION_NAME || prop == ICPPASTUsingDeclaration.NAME) {
					break;
				} else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
					IASTNode p = name.getParent().getParent();
					if (p instanceof IASTParameterDeclaration || (p instanceof IASTSimpleDeclaration
							&& ((IASTSimpleDeclaration) p).getDeclarators().length == 0)) {
						break;
					}
				} else if (prop == IASTDeclarator.DECLARATOR_NAME) {
					IASTNode p = name.getParent();
					while (p instanceof IASTDeclarator) {
						p = p.getParent();
					}
					if (p instanceof IASTSimpleDeclaration) {
						IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) p).getDeclSpecifier();
						if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef)
							break;
					}
				}

				if (kind == KIND_TYPE)
					return PROCESS_CONTINUE;
				// $FALL-THROUGH$

			case KIND_OBJ_FN:
				if (prop == IASTDeclarator.DECLARATOR_NAME
						|| prop == IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME
						|| prop == ICPPASTUsingDeclaration.NAME
						|| prop == ICPPASTStructuredBindingDeclaration.IDENTIFIER) {
					break;
				}
				return PROCESS_CONTINUE;

			case KIND_NAMESPACE:
				if (prop == ICPPASTNamespaceDefinition.NAMESPACE_NAME || prop == ICPPASTNamespaceAlias.ALIAS_NAME) {
					break;
				}
				return PROCESS_CONTINUE;
			}

			if (bindings != null) {
				if (isDeclarationBinding(name.resolveBinding())) {
					if (decls.length == idx) {
						decls = Arrays.copyOf(decls, decls.length * 2);
					}
					decls[idx++] = name;
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean isDeclarationBinding(IBinding nameBinding) {
			if (nameBinding != null) {
				for (IBinding binding : bindings) {
					if (areEquivalentBindings(nameBinding, binding, index, permissive)) {
						return true;
					}
					// A using declaration is a declaration for the references of its delegates.
					if (nameBinding instanceof ICPPUsingDeclaration) {
						if (ArrayUtil.contains(((ICPPUsingDeclaration) nameBinding).getDelegates(), binding)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		public IASTName[] getDeclarations() {
			if (idx < decls.length) {
				decls = Arrays.copyOf(decls, idx);
			}
			return decls;
		}
	}

	private static boolean areEquivalentBindings(IBinding candidate, IBinding target, IIndex index,
			boolean permissive) {
		if (permissive && candidate instanceof IProblemBinding && !(target instanceof IProblemBinding)) {
			IProblemBinding problem = (IProblemBinding) candidate;
			for (IBinding c : problem.getCandidateBindings()) {
				if (areEquivalentBindings(c, target, index)) {
					return true;
				}
			}
			return false;
		}
		return areEquivalentBindings(candidate, target, index);
	}

	private static boolean areEquivalentBindings(IBinding binding1, IBinding binding2, IIndex index) {
		if (binding1.equals(binding2)) {
			return true;
		}
		if ((binding1 instanceof IIndexBinding) != (binding2 instanceof IIndexBinding) && index != null) {
			// Even though we know one of them is an index binding, we need to adapt both because
			// they might not come from an index with the same number of fragments. So one of them
			// could be a composite binding and the other one not.
			binding1 = index.adaptBinding(binding1);
			binding2 = index.adaptBinding(binding2);

			if (binding1 == null || binding2 == null) {
				return false;
			}
			if (binding1.equals(binding2)) {
				return true;
			}
		}
		return false;
	}

	protected static IBinding unwindBinding(IBinding binding) {
		while (true) {
			if (binding instanceof ICPPSpecialization) {
				binding = ((ICPPSpecialization) binding).getSpecializedBinding();
			} else {
				break;
			}
		}
		return binding;
	}

	public static class CollectReferencesAction extends ASTVisitor {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] refs;
		private IBinding[] bindings;
		private int idx = 0;
		private int kind;
		private IIndex index;

		private static final int KIND_LABEL = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE = 3;
		private static final int KIND_NAMESPACE = 4;
		private static final int KIND_COMPOSITE = 5;

		public CollectReferencesAction(IBinding binding) {
			shouldVisitTranslationUnit = true;
			shouldVisitNames = true;
			this.refs = new IASTName[DEFAULT_LIST_SIZE];

			binding = unwindBinding(binding);
			this.bindings = new IBinding[] { binding };

			if (binding instanceof ICPPUsingDeclaration) {
				this.bindings = ((ICPPUsingDeclaration) binding).getDelegates();
				kind = KIND_COMPOSITE;
			} else if (binding instanceof ILabel) {
				kind = KIND_LABEL;
			} else if (binding instanceof ICompositeType || binding instanceof ITypedef
					|| binding instanceof IEnumeration) {
				kind = KIND_TYPE;
			} else if (binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			} else if (binding instanceof ICPPTemplateParameter) {
				kind = KIND_COMPOSITE;
			} else {
				kind = KIND_OBJ_FN;
			}
		}

		@Override
		public int visit(IASTTranslationUnit tu) {
			index = tu.getIndex();
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName || name instanceof ICPPASTTemplateId)
				return PROCESS_CONTINUE;

			ASTNodeProperty prop = name.getPropertyInParent();
			ASTNodeProperty p2 = null;
			if (prop == ICPPASTQualifiedName.SEGMENT_NAME) {
				p2 = prop;
				prop = name.getParent().getPropertyInParent();
			}

			switch (kind) {
			case KIND_LABEL:
				if (prop == IASTGotoStatement.NAME || prop == IASTIdExpression.ID_NAME)
					break;
				return PROCESS_CONTINUE;

			case KIND_TYPE:
			case KIND_COMPOSITE:
				if (prop == IASTNamedTypeSpecifier.NAME || prop == ICPPASTPointerToMember.NAME
						|| prop == ICPPASTUsingDeclaration.NAME
						|| prop == ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.NAME_SPECIFIER
						|| prop == ICPPASTTemplateId.TEMPLATE_NAME || p2 == ICPPASTQualifiedName.SEGMENT_NAME) {
					break;
				} else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
					IASTNode p = name.getParent().getParent();
					if (!(p instanceof IASTSimpleDeclaration)
							|| ((IASTSimpleDeclaration) p).getDeclarators().length > 0) {
						break;
					}
				}
				if (kind == KIND_TYPE)
					return PROCESS_CONTINUE;
				//$FALL-THROUGH$
			case KIND_OBJ_FN:
				if (prop == IASTIdExpression.ID_NAME || prop == IASTFieldReference.FIELD_NAME
						|| prop == ICPPASTUsingDirective.QUALIFIED_NAME || prop == ICPPASTUsingDeclaration.NAME
						|| prop == IASTFunctionCallExpression.FUNCTION_NAME || prop == ICPPASTUsingDeclaration.NAME
						|| prop == IASTNamedTypeSpecifier.NAME || prop == ICPPASTConstructorChainInitializer.MEMBER_ID
						|| prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT || prop == ICPPASTCapture.IDENTIFIER
						|| prop == IASTImplicitNameOwner.IMPLICIT_NAME) {
					break;
				}
				return PROCESS_CONTINUE;

			case KIND_NAMESPACE:
				if (prop == ICPPASTUsingDirective.QUALIFIED_NAME || prop == ICPPASTNamespaceAlias.MAPPING_NAME
						|| prop == ICPPASTUsingDeclaration.NAME || p2 == ICPPASTQualifiedName.SEGMENT_NAME) {
					break;
				}
				return PROCESS_CONTINUE;
			}

			if (bindings != null) {
				if (isReferenceBinding(name.resolveBinding())) {
					if (refs.length == idx) {
						refs = Arrays.copyOf(refs, refs.length * 2);
					}
					refs[idx++] = name;
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean isReferenceBinding(IBinding nameBinding) {
			nameBinding = unwindBinding(nameBinding);
			if (nameBinding != null) {
				for (IBinding binding : bindings) {
					if (areEquivalentBindings(nameBinding, binding, index)) {
						return true;
					}
				}
				if (nameBinding instanceof ICPPUsingDeclaration) {
					IBinding[] delegates = ((ICPPUsingDeclaration) nameBinding).getDelegates();
					for (IBinding delegate : delegates) {
						if (isReferenceBinding(delegate)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		public IASTName[] getReferences() {
			if (idx < refs.length) {
				refs = Arrays.copyOf(refs, idx);
			}
			return refs;
		}
	}

	/**
	 * Generate a function type for an implicit function.
	 * NOTE: This does not correctly handle parameters with typedef types.
	 */
	public static ICPPFunctionType createImplicitFunctionType(IType returnType, IParameter[] parameters,
			boolean isConst, boolean isVolatile) {
		IType[] pTypes = new IType[parameters.length];
		IType pt = null;

		for (int i = 0; i < parameters.length; i++) {
			pt = parameters[i].getType();

			// remove qualifiers
			if (pt instanceof IQualifierType) {
				pt = ((IQualifierType) pt).getType();
			}

			if (pt instanceof IArrayType) {
				pt = new CPPPointerType(((IArrayType) pt).getType());
			} else if (pt instanceof IFunctionType) {
				pt = new CPPPointerType(pt);
			}

			pTypes[i] = pt;
		}

		return new CPPFunctionType(returnType, pTypes,
				null /* TODO(havogt) [except.spec] p.14 (c++11) noexcept for implicitly declared special member functions not implemented */,
				isConst, isVolatile, false, false, false);
	}

	/**
	 * Creates the type for the given type id.
	 */
	public static IType createType(IASTTypeId typeid) {
		return createType(typeid.getAbstractDeclarator());
	}

	/**
	 * Creates the type for a parameter declaration.
	 */
	public static IType createType(final ICPPASTParameterDeclaration pdecl, boolean forFuncType) {
		IASTDeclSpecifier pDeclSpec = pdecl.getDeclSpecifier();
		ICPPASTDeclarator pDtor = pdecl.getDeclarator();
		IType pt;
		PlaceholderKind placeholder = usesAuto(pDeclSpec);
		if (placeholder != null) {
			pt = createAutoType(pDeclSpec, pDtor, 0, placeholder);
		} else {
			pt = createType(pDeclSpec);
			if (pDtor != null) {
				pt = createType(pt, pDtor);
			}
		}

		pt = adjustParameterType(pt, forFuncType);

		if (pDtor != null && findInnermostDeclarator(pDtor).declaresParameterPack()) {
			pt = new CPPParameterPackType(pt);
		}
		return pt;
	}

	private static boolean hasFieldNamedValue(ICPPClassSpecialization instance) {
		return Arrays.stream(instance.getFields()).anyMatch(field -> field.getName().equals(VALUE_STR));
	}

	public static Optional<ICPPClassSpecialization> findTupleSizeWithValueMember(IType type, IScope scope,
			IASTNode point) {
		ICPPTemplateArgument[] templateArguments = new ICPPTemplateArgument[] { new CPPTemplateTypeArgument(type) };
		return findClassTemplateInstance(scope, STD_TUPLE_SIZE_STR, templateArguments, point)
				.filter(CPPVisitor::hasFieldNamedValue);
	}

	private static Optional<ICPPClassSpecialization> findClassTemplateInstance(IScope scope, String templateName,
			ICPPTemplateArgument[] templateArguments, IASTNode point) {
		IBinding[] tupleSizeBindings = CPPSemantics.findBindingsForQualifiedName(scope, templateName, point);
		return Arrays.stream(tupleSizeBindings).filter(ICPPClassTemplate.class::isInstance)
				.map(ICPPClassTemplate.class::cast)
				.map(template -> CPPTemplates.instantiate(template, templateArguments))
				.filter(ICPPClassSpecialization.class::isInstance).map(ICPPClassSpecialization.class::cast)
				.filter(Objects::nonNull).findFirst();
	}

	private static IType determineTupleElementType(IType type, IScope scope, IASTName name, int index) {
		ICPPTemplateArgument indexArgument = new CPPTemplateNonTypeArgument(IntegralValue.create(index),
				CPPVisitor.get_SIZE_T());
		ICPPTemplateArgument[] templateArguments = new ICPPTemplateArgument[] { indexArgument,
				new CPPTemplateTypeArgument(type) };
		return toStream(findClassTemplateInstance(scope, STD_TUPLE_ELEMENT_STR, templateArguments, name))
				.map(ICPPClassSpecialization::getCompositeScope)
				.map(instanceScope -> CPPSemantics.findBindings(instanceScope, TYPE_STR.toCharArray(), false, name))
				.flatMap(Arrays::stream).filter(IType.class::isInstance).map(IType.class::cast)
				.map(SemanticUtil::getSimplifiedType).findFirst()
				.orElse(ProblemType.CANNOT_DEDUCE_STRUCTURED_BINDING_TYPE);
	}

	private static ICPPASTInitializerClause getInitializerClause(ICPPASTStructuredBindingDeclaration declaration) {
		Optional<IASTInitializer> initializer = declaration.getInitializer();
		ICPPASTInitializerClause initClause = null;
		if (initializer.isPresent()) {
			initClause = getInitClauseForInitializer(initializer.get());
		} else {
			IASTNode declarationParent = declaration.getParent();
			if (declarationParent instanceof ICPPASTRangeBasedForStatement) {
				ICPPASTRangeBasedForStatement rangeBasedForParent = (ICPPASTRangeBasedForStatement) declarationParent;
				initClause = getAutoInitClauseForRangeBasedFor(rangeBasedForParent);
			}
		}
		return initClause;
	}

	public static IType createType(ICPPASTStructuredBindingDeclaration declaration, IASTName name) {
		ICPPASTInitializerClause initClause = getInitializerClause(declaration);

		if (initClause == null) {
			return ProblemType.CANNOT_DEDUCE_STRUCTURED_BINDING_TYPE;
		}

		ICPPEvaluation evaluation = initClause.getEvaluation();
		IType eType = evaluation.getType();
		IType unwrappedType = SemanticUtil.getNestedType(eType, TDEF | ALLCVQ);
		IType initializerType = deduceInitializerType(declaration, name, unwrappedType);
		if (initializerType == ProblemType.CANNOT_DEDUCE_STRUCTURED_BINDING_TYPE) {
			return initializerType;
		}
		IASTDeclSpecifier declSpec = declaration.getDeclSpecifier();
		IType qualifiedType = SemanticUtil.addQualifiers(initializerType,
				declSpec.isConst() || SemanticUtil.isConst(eType),
				declSpec.isVolatile() || SemanticUtil.isVolatile(eType), declSpec.isRestrict());
		Optional<RefQualifier> refQualifier = declaration.getRefQualifier();
		IType refWrappedType = refQualifier.map(qualifier -> (IType) new CPPReferenceType(qualifiedType,
				shallBecomeRvalueReference(evaluation, qualifier, declSpec))).orElse(qualifiedType);
		return refWrappedType;
	}

	public static boolean hasConstexprStaticIntegralValueField(ICPPClassType type, long expectedValue) {
		return Arrays.stream(ClassTypeHelper.getFields(type)).filter(IS_STATIC_VARIABLE).filter(IS_NAMED_VALUE)
				.filter(HAS_INTEGRAL_TYPE).filter(IS_CONSTEXPR_FIELD).filter(field -> {
					return field.getInitialValue().numberValue().longValue() == expectedValue;
				}).findFirst().isPresent();
	}

	private static IType deduceInitializerType(ICPPASTStructuredBindingDeclaration declaration, IASTName name,
			IType unwrappedType) {
		if (unwrappedType instanceof IArrayType) {
			IArrayType arrayType = (IArrayType) unwrappedType;
			return arrayType.getType();
		} else if (unwrappedType instanceof ICPPClassType) {
			int index = Arrays.asList(declaration.getNames()).indexOf(name);
			IScope scope = CPPSemantics.getLookupScope(name);
			Optional<ICPPClassSpecialization> tupleSizeInstance = findTupleSizeWithValueMember(unwrappedType, scope,
					name);
			if (tupleSizeInstance.isPresent()) {
				int numberOfNames = declaration.getNames().length;
				if (hasConstexprStaticIntegralValueField(tupleSizeInstance.get(), numberOfNames)) {
					return determineTupleElementType(unwrappedType, scope, name, index);
				}
			} else {
				ICPPClassType classType = (ICPPClassType) unwrappedType;
				return Arrays.stream(ClassTypeHelper.getFields(classType)).filter(IS_STATIC_VARIABLE.negate())
						.skip(index).findFirst().map(IField::getType)
						.orElse(ProblemType.CANNOT_DEDUCE_STRUCTURED_BINDING_TYPE);
			}
		}
		return ProblemType.CANNOT_DEDUCE_STRUCTURED_BINDING_TYPE;
	}

	/**
	 *
	 * Determines the reference type of a name in a structured binding based on its {@code evaluation}, the {@code refQualifier} and the {@code declSpecifier}.
	 * It performs perfect forwarding in case of a forwarding reference, i.e. if the declaration specifier is exactly "auto &&",
	 * Otherwise it is always an rvalue reference if the declaration specifier contains &&
	 *
	 * Example:
	 * auto && [element] = get(); // element is an rvalue if get() is an rvalue, otherwise an lvalue
	 * auto const && [element] = get(); //element is always an rvalue, get() needs to be an rvalue too
	 *
	 */
	private static boolean shallBecomeRvalueReference(ICPPEvaluation evaluation, RefQualifier refQualifier,
			IASTDeclSpecifier declSpecifier) {

		if (refQualifier == RefQualifier.RVALUE) {
			return evaluation.getValueCategory() != ValueCategory.LVALUE || declSpecifier.isConst()
					|| declSpecifier.isVolatile();
		}
		return false;
	}

	private static IType createFunctionType(IType returnType, ICPPASTFunctionDeclarator fnDtor) {
		IType[] pTypes = createParameterTypes(fnDtor);

		IASTName name = fnDtor.getName().getLastName();
		if (name instanceof ICPPASTConversionName) {
			returnType = createType(((ICPPASTConversionName) name).getTypeId());
		} else {
			returnType = applyAttributes(returnType, fnDtor);
			returnType = getPointerTypes(returnType, fnDtor);
		}

		RefQualifier refQualifier = fnDtor.getRefQualifier();
		CPPFunctionType type = new CPPFunctionType(returnType, pTypes, fnDtor.getNoexceptEvaluation(), fnDtor.isConst(),
				fnDtor.isVolatile(), refQualifier != null, refQualifier == RefQualifier.RVALUE, fnDtor.takesVarArgs());
		final IASTDeclarator nested = fnDtor.getNestedDeclarator();
		if (nested != null) {
			return createType(type, nested);
		}
		return type;
	}

	/**
	 * Creates an array of types for the parameters of the given function declarator.
	 */
	public static IType[] createParameterTypes(ICPPASTFunctionDeclarator fnDtor) {
		ICPPASTParameterDeclaration[] params = fnDtor.getParameters();
		IType[] pTypes = new IType[params.length];
		for (int i = 0; i < params.length; i++) {
			pTypes[i] = createType(params[i], true);
		}

		if (pTypes.length == 1 && SemanticUtil.isVoidType(pTypes[0])) {
			return IType.EMPTY_TYPE_ARRAY; // f(void) is the same as f().
		}
		return pTypes;
	}

	/**
	 * Adjusts the parameter type according to 8.3.5-3:
	 * cv-qualifiers are deleted, arrays and function types are converted to pointers.
	 */
	static IType adjustParameterType(final IType pt, boolean forFunctionType) {
		// Bug 239975
		IType t = SemanticUtil.getNestedType(pt, TDEF);
		if (t instanceof IArrayType) {
			IArrayType at = (IArrayType) t;
			return new CPPPointerType(at.getType());
		}
		if (t instanceof IFunctionType) {
			return new CPPPointerType(pt);
		}

		// 8.3.5-3
		// Any cv-qualifier modifying a parameter type is deleted. The parameter type remains
		// to be qualified.
		if (forFunctionType && SemanticUtil.getCVQualifier(t) != CVQualifier.NONE) {
			return SemanticUtil.getNestedType(t, TDEF | ALLCVQ);
		}
		return pt;
	}

	private static IType getPointerTypes(IType type, IASTDeclarator declarator) {
		IASTPointerOperator[] ptrOps = declarator.getPointerOperators();
		for (IASTPointerOperator ptrOp : ptrOps) {
			if (ptrOp instanceof ICPPASTPointerToMember) {
				type = new CPPPointerToMemberType(type, (ICPPASTPointerToMember) ptrOp);
			} else if (ptrOp instanceof IASTPointer) {
				type = new CPPPointerType(type, (IASTPointer) ptrOp);
			} else if (ptrOp instanceof ICPPASTReferenceOperator) {
				final ICPPASTReferenceOperator refOp = (ICPPASTReferenceOperator) ptrOp;
				type = new CPPReferenceType(type, refOp.isRValueReference());
			}
		}
		return type;
	}

	private static IType applyAttributes(IType type, IASTDeclarator declarator) {
		if (type instanceof IBasicType) {
			IBasicType basicType = (IBasicType) type;
			if (basicType.getKind() == IBasicType.Kind.eInt) {
				IASTAttribute[] attributes = declarator.getAttributes();
				for (IASTAttribute attribute : attributes) {
					char[] name = attribute.getName();
					if (CharArrayUtils.equals(name, "__mode__") || CharArrayUtils.equals(name, "mode")) { //$NON-NLS-1$ //$NON-NLS-2$
						char[] mode = AttributeUtil.getSimpleArgument(attribute);
						if (CharArrayUtils.equals(mode, "__QI__") || CharArrayUtils.equals(mode, "QI")) { //$NON-NLS-1$ //$NON-NLS-2$
							type = new CPPBasicType(IBasicType.Kind.eChar,
									basicType.isUnsigned() ? IBasicType.IS_UNSIGNED : IBasicType.IS_SIGNED);
						} else if (CharArrayUtils.equals(mode, "__HI__") || CharArrayUtils.equals(mode, "HI")) { //$NON-NLS-1$ //$NON-NLS-2$
							type = new CPPBasicType(IBasicType.Kind.eInt,
									IBasicType.IS_SHORT | getSignModifiers(basicType));
						} else if (CharArrayUtils.equals(mode, "__SI__") || CharArrayUtils.equals(mode, "SI")) { //$NON-NLS-1$ //$NON-NLS-2$
							type = new CPPBasicType(IBasicType.Kind.eInt, getSignModifiers(basicType));
						} else if (CharArrayUtils.equals(mode, "__DI__") || CharArrayUtils.equals(mode, "DI")) { //$NON-NLS-1$ //$NON-NLS-2$
							SizeofCalculator sizeofs = new SizeofCalculator(declarator.getTranslationUnit());
							int modifier;
							if (sizeofs.sizeof_long != null && sizeofs.sizeof_int != null
									&& sizeofs.sizeof_long.size == 2 * sizeofs.sizeof_int.size) {
								modifier = IBasicType.IS_LONG;
							} else {
								modifier = IBasicType.IS_LONG_LONG;
							}
							type = new CPPBasicType(IBasicType.Kind.eInt, modifier | getSignModifiers(basicType));
						} else if (CharArrayUtils.equals(mode, "__word__") || CharArrayUtils.equals(mode, "word")) { //$NON-NLS-1$ //$NON-NLS-2$
							type = new CPPBasicType(IBasicType.Kind.eInt,
									IBasicType.IS_LONG | getSignModifiers(basicType));
						}
					}
				}
			}
		}
		return type;
	}

	private static int getSignModifiers(IBasicType type) {
		return type.getModifiers() & (IBasicType.IS_SIGNED | IBasicType.IS_UNSIGNED);
	}

	private static IType getArrayType(IType type, IASTArrayDeclarator declarator) {
		IASTArrayModifier[] mods = declarator.getArrayModifiers();
		for (int i = mods.length; --i >= 0;) {
			IASTArrayModifier mod = mods[i];
			IASTExpression sizeExpression = mod.getConstantExpression();
			if (sizeExpression != null) {
				type = new CPPArrayType(type, sizeExpression);
			} else {
				IValue sizeValue = null;
				IASTInitializer initializer = declarator.getInitializer();
				if (initializer instanceof IASTEqualsInitializer) {
					IASTInitializerClause clause = ((IASTEqualsInitializer) initializer).getInitializerClause();
					if (clause instanceof IASTInitializerList) {
						IASTInitializerClause[] clauses = ((IASTInitializerList) clause).getClauses();
						sizeValue = IntegralValue.create(clauses.length);
					} else if (clause instanceof ICPPASTLiteralExpression) {
						ICPPEvaluation value = ((ICPPASTExpression) clause).getEvaluation();
						IType valueType = value.getType();
						if (valueType instanceof IArrayType) {
							sizeValue = ((IArrayType) valueType).getSize();
						}
					}
				}
				type = new CPPArrayType(type, sizeValue);
			}
		}
		return type;
	}

	public static PlaceholderKind usesAuto(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTSimpleDeclSpecifier) {
			int declSpecType = ((ICPPASTSimpleDeclSpecifier) declSpec).getType();
			if (declSpecType == IASTSimpleDeclSpecifier.t_auto) {
				return PlaceholderKind.Auto;
			} else if (declSpecType == IASTSimpleDeclSpecifier.t_decltype_auto) {
				return PlaceholderKind.DecltypeAuto;
			}
		}
		return null;
	}

	public static IType createType(ICPPASTInitCapture capture) {
		IASTDeclarator declarator = capture.getDeclarator();
		ICPPASTInitializerClause initClause = getAutoInitClauseForDeclarator(declarator);
		if (initClause == null) {
			return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
		}
		return createAutoType(initClause.getEvaluation(), null, declarator);
	}

	public static IType createType(IASTDeclarator declarator) {
		// Resolve placeholders by default.
		return createType(declarator, RESOLVE_PLACEHOLDERS);
	}

	public static IType createType(IASTDeclarator declarator, int flags) {
		if (declarator == null)
			return ProblemType.NO_NAME;

		CPPSemantics.pushLookupPoint(declarator);
		try {
			declarator = findOutermostDeclarator(declarator);
			IASTNode parent = declarator.getParent();

			IASTDeclSpecifier declSpec = null;
			boolean isPackExpansion = false;
			if (parent instanceof IASTSimpleDeclaration) {
				declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
			} else if (parent instanceof IASTParameterDeclaration) {
				declSpec = ((IASTParameterDeclaration) parent).getDeclSpecifier();
			} else if (parent instanceof IASTFunctionDefinition) {
				declSpec = ((IASTFunctionDefinition) parent).getDeclSpecifier();
			} else if (parent instanceof ICPPASTTypeId) {
				final ICPPASTTypeId typeId = (ICPPASTTypeId) parent;
				declSpec = typeId.getDeclSpecifier();
				isPackExpansion = typeId.isPackExpansion();
			} else if (parent instanceof ICPPASTInitCapture) {
				return createType((ICPPASTInitCapture) parent);
			} else {
				throw new IllegalArgumentException();
			}

			PlaceholderKind placeholder = usesAuto(declSpec);
			if (placeholder != null) {
				return createAutoType(declSpec, declarator, flags, placeholder);
			}

			IType type = createType(declSpec);
			type = createType(type, declarator, flags);

			// constexpr implies toplevel-const
			type = makeConstIfConstexpr(type, declSpec, declarator);

			// C++ specification 8.3.4.3 and 8.5.1.4
			IASTNode initClause = declarator.getInitializer();
			if (initClause instanceof IASTEqualsInitializer) {
				initClause = ((IASTEqualsInitializer) initClause).getInitializerClause();
				if (initClause instanceof IASTLiteralExpression && SemanticUtil.isConst(type)) {
					IType t = SemanticUtil.getNestedType(type, TDEF | ALLCVQ);
					if (t instanceof CPPBasicType) {
						IValue v = SemanticUtil.getValueOfInitializer(declarator.getInitializer(), t);
						if (v.numberValue() != null)
							((CPPBasicType) t).setAssociatedNumericalValue(v.numberValue().longValue());
					}
				}
			}
			if (initClause instanceof IASTInitializerList) {
				IType t = SemanticUtil.getNestedType(type, TDEF);
				if (t instanceof IArrayType) {
					IArrayType at = (IArrayType) t;
					if (at.getSize() == null) {
						type = new CPPArrayType(at.getType(),
								IntegralValue.create(((IASTInitializerList) initClause).getSize()));
					}
				}
			}

			if (isPackExpansion) {
				type = new CPPParameterPackType(type);
			}
			return type;
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	private static IType createAutoParameterType(IASTDeclSpecifier declSpec, IASTDeclarator declarator,
			ICPPASTParameterDeclaration declaration, PlaceholderKind placeholder) {
		// decltype(auto) is not allowed in parameters.
		if (placeholder == PlaceholderKind.DecltypeAuto) {
			return ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
		}

		// In C++14, auto is only allowed in lambda parameters.
		// In the Concepts TS (not implemented yet), this restriction is removed.
		ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declaration.getParent();
		if (functionDeclarator.getParent() instanceof ICPPASTLambdaExpression) {
			ICPPASTLambdaExpression lambda = (ICPPASTLambdaExpression) functionDeclarator.getParent();
			CPPClosureType closure = (CPPClosureType) lambda.getExpressionType();
			ICPPTemplateParameter[] templateParameters = closure.getInventedTemplateParameterList();

			// Find the invented template parameter corresponding to this 'auto'.
			int templateParameterIndex = -1;
			for (ICPPASTParameterDeclaration parameter : functionDeclarator.getParameters()) {
				if (usesAuto(parameter.getDeclSpecifier()) != null) {
					++templateParameterIndex;
				}
				if (parameter == declaration)
					break;
			}
			if (templateParameterIndex >= 0 && templateParameterIndex < templateParameters.length) {
				ICPPTemplateParameter templateParameter = templateParameters[templateParameterIndex];
				if (templateParameter instanceof ICPPTemplateTypeParameter) {
					return createType((ICPPTemplateTypeParameter) templateParameter, declarator);
				}
			}
		}
		return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
	}

	private static ICPPASTInitializerClause getInitClauseForInitializer(IASTInitializer initClause) {
		if (initClause instanceof IASTEqualsInitializer) {
			return (ICPPASTInitializerClause) ((IASTEqualsInitializer) initClause).getInitializerClause();
		} else if (initClause instanceof ICPPASTConstructorInitializer) {
			IASTInitializerClause[] arguments = ((ICPPASTConstructorInitializer) initClause).getArguments();
			if (arguments.length == 1) {
				return (ICPPASTInitializerClause) arguments[0];
			}
		} else if (initClause instanceof ICPPASTInitializerClause) {
			if (initClause instanceof ICPPASTInitializerList) {
				IASTInitializerClause[] clauses = ((ICPPASTInitializerList) initClause).getClauses();
				if (clauses.length == 1) {
					return (ICPPASTInitializerClause) clauses[0];
				}
			}
			return (ICPPASTInitializerClause) initClause;
		}
		return null;
	}

	private static ICPPASTInitializerClause getAutoInitClauseForRangeBasedFor(ICPPASTRangeBasedForStatement forStmt) {
		// See 6.5.4 The range-based for statement [stmt.ranged]
		IASTInitializerClause forInit = forStmt.getInitializerClause();
		IASTExpression beginExpr = null;
		if (forInit instanceof IASTExpression) {
			final IASTExpression expr = (IASTExpression) forInit;
			IType type = SemanticUtil.getNestedType(expr.getExpressionType(), TDEF | CVTYPE);
			if (type instanceof IArrayType) {
				beginExpr = expr.copy();
			}
		}
		if (beginExpr == null) {
			IASTImplicitName[] implicits = forStmt.getImplicitNames();
			if (implicits.length > 0) {
				IBinding b = implicits[0].getBinding();
				CPPASTName name = new CPPASTName();
				name.setBinding(b);
				IASTInitializerClause[] beginCallArguments = new IASTInitializerClause[] { forInit.copy() };
				if (b instanceof ICPPMethod && forInit instanceof IASTExpression) {
					beginExpr = new CPPASTFunctionCallExpression(
							new CPPASTFieldReference(name, (IASTExpression) forInit.copy()), beginCallArguments);
				} else {
					beginExpr = new CPPASTFunctionCallExpression(new CPPASTIdExpression(name), beginCallArguments);
				}
			} else {
				return null;
			}
		}
		ICPPASTInitializerClause autoInitClause = new CPPASTUnaryExpression(IASTUnaryExpression.op_star, beginExpr);
		autoInitClause.setParent(forStmt);
		autoInitClause.setPropertyInParent(ICPPASTRangeBasedForStatement.INITIALIZER);
		return autoInitClause;
	}

	private static ICPPASTInitializerClause getAutoInitClauseForDeclarator(IASTDeclarator declarator) {
		IASTInitializer initClause = declarator.getInitializer();
		return getInitClauseForInitializer(initClause);
	}

	private static IType createAutoType(final IASTDeclSpecifier declSpec, IASTDeclarator declarator, int flags,
			PlaceholderKind placeholderKind) {
		IType cannotDeduce = placeholderKind == PlaceholderKind.Auto ? ProblemType.CANNOT_DEDUCE_AUTO_TYPE
				: ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
		Set<IASTDeclarator> recursionProtectionSet = autoTypeDeclarators.get();
		if (!recursionProtectionSet.add(declarator)) {
			// Detected a self referring auto type, e.g.: auto x = x;
			return cannotDeduce;
		}

		try {
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				return createAutoFunctionType(declSpec, (ICPPASTFunctionDeclarator) declarator, flags, placeholderKind);
			}
			if (declarator.getParent() instanceof ICPPASTParameterDeclaration) {
				if (declarator.getParent().getParent() instanceof ICPPASTFunctionDeclarator) {
					// 'auto' used as the type of a function parameter.
					return createAutoParameterType(declSpec, declarator,
							(ICPPASTParameterDeclaration) declarator.getParent(), placeholderKind);
				} else {
					if (placeholderKind == PlaceholderKind.Auto) {
						// 'auto' used as the type of a template parameter.
						// This is a partially supported C++17 feature.
						return new CPPPlaceholderType(placeholderKind);
					} else {
						return ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
					}
				}
			}
			ICPPASTInitializerClause autoInitClause = null;
			IASTNode parent = declarator.getParent().getParent();
			if (parent instanceof ICPPASTNewExpression) {
				IASTInitializer initializer = ((ICPPASTNewExpression) parent).getInitializer();
				if (initializer != null) {
					IASTInitializerClause[] arguments = ((ICPPASTConstructorInitializer) initializer).getArguments();
					if (arguments.length == 1) {
						autoInitClause = (ICPPASTInitializerClause) arguments[0];
					}
				}
			} else if (parent instanceof ICPPASTRangeBasedForStatement) {
				autoInitClause = getAutoInitClauseForRangeBasedFor((ICPPASTRangeBasedForStatement) parent);
				if (autoInitClause == null) {
					return cannotDeduce;
				}
			} else if (parent instanceof IASTCompositeTypeSpecifier
					&& declSpec.getStorageClass() != IASTDeclSpecifier.sc_static) {
				// Non-static auto-typed class members are not allowed.
				return ProblemType.AUTO_FOR_NON_STATIC_FIELD;
			} else {
				autoInitClause = getAutoInitClauseForDeclarator(declarator);
			}
			if (autoInitClause == null) {
				return cannotDeduce;
			}
			if (placeholderKind == PlaceholderKind.Auto) {
				return createAutoType(autoInitClause.getEvaluation(), declSpec, declarator);
			} else /* decltype(auto) */ {
				if (declarator.getPointerOperators().length > 0) {
					// 'decltype(auto)' cannot be combined with * or & the way 'auto' can.
					return ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
				}
				if (declSpec.isConst() || declSpec.isVolatile()) {
					// 'decltype(auto)' cannot be combined with any type specifiers. [dcl.type.auto.deduct]
					return ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
				}
				if (autoInitClause instanceof IASTExpression) {
					return getDeclType((IASTExpression) autoInitClause);
				} else {
					return ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
				}
			}
		} finally {
			recursionProtectionSet.remove(declarator);
		}
	}

	private static IType createAutoType(final ICPPEvaluation evaluation, IASTDeclSpecifier declSpec,
			IASTDeclarator declarator) {
		//  C++0x: 7.1.6.4
		IType type = AutoTypeResolver.AUTO_TYPE;
		IType initType = null;
		ValueCategory valueCat = null;
		initType = evaluation.getType();
		valueCat = evaluation.getValueCategory();
		if (initType == null || initType instanceof ISemanticProblem) {
			return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
		}
		ICPPClassTemplate initializer_list_template = null;
		if (evaluation instanceof EvalInitList) {
			initializer_list_template = get_initializer_list();
			if (initializer_list_template == null) {
				return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
			}
			type = (IType) CPPTemplates.instantiate(initializer_list_template,
					new ICPPTemplateArgument[] { new CPPTemplateTypeArgument(type) });
			if (type instanceof IProblemBinding) {
				return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
			}
		}
		if (declSpec != null && declarator != null) {
			type = decorateType(type, declSpec, declarator);
		}
		ICPPFunctionTemplate template = new AutoTypeResolver(type);
		CPPTemplateParameterMap paramMap = new CPPTemplateParameterMap(1);
		TemplateArgumentDeduction.deduceFromFunctionArgs(template, Collections.singletonList(initType),
				Collections.singletonList(valueCat), paramMap);
		ICPPTemplateArgument argument = paramMap.getArgument(0, 0);
		if (argument == null) {
			return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
		}
		type = argument.getTypeValue();
		if (type instanceof TypeOfDependentExpression) {
			// After binding to 'auto', a dependent type no longer acts like 'decltype(expr)'.
			// For example, after instantiation it's no longer wrapped into a reference type
			// if it's an lvalue the way 'decltype(expr)' usually is.
			((TypeOfDependentExpression) type).setIsForDecltype(false);
		}
		IType t = SemanticUtil.substituteTypedef(type, initType);
		if (t != null)
			type = t;
		if (evaluation instanceof EvalInitList) {
			type = (IType) CPPTemplates.instantiate(initializer_list_template,
					new ICPPTemplateArgument[] { new CPPTemplateTypeArgument(type) });
		}
		if (declSpec != null && declarator != null) {
			type = decorateType(type, declSpec, declarator);
		}
		return type;
	}

	private static class ReturnTypeDeducer extends ReturnStatementVisitor {
		private static final ICPPEvaluation voidEval = new EvalFixed(CPPSemantics.VOID_TYPE, ValueCategory.PRVALUE,
				IntegralValue.UNKNOWN);

		private ICPPEvaluation[] fReturnEvals = ICPPEvaluation.EMPTY_ARRAY;
		private boolean fEncounteredReturnStatement = false;

		protected ReturnTypeDeducer(IASTFunctionDefinition func) {
			super(func);
		}

		@Override
		protected void onReturnStatement(IASTReturnStatement stmt) {
			fEncounteredReturnStatement = true;
			IASTInitializerClause returnExpression = stmt.getReturnArgument();
			ICPPEvaluation returnEval;
			if (returnExpression == null) {
				returnEval = voidEval;
			} else {
				returnEval = ((ICPPASTInitializerClause) returnExpression).getEvaluation();
			}
			IType returnType = returnEval.getType();
			if (returnType instanceof ISemanticProblem) {
				// If a function makes a recursive call in some of its return statements,
				// the type those return expressions will be a problem type. We ignore
				// these, because we can still successfully deduce from another return
				// statement that is not recursive.
				// If all return statements are recursive, fReturnEvals will remain empty
				// and deduceReturnType() will error out as desired.
				return;
			}

			fReturnEvals = ArrayUtil.append(fReturnEvals, returnEval);
		}

		public ICPPEvaluation[] getReturnEvaluations() {
			if (fReturnEvals.length == 0 && !fEncounteredReturnStatement) {
				fReturnEvals = ArrayUtil.append(fReturnEvals, voidEval);
			}
			return ArrayUtil.trim(fReturnEvals);
		}
	}

	private static IType deduceTypeFromReturnEvaluation(ICPPEvaluation returnEval, IASTDeclSpecifier autoDeclSpec,
			IASTDeclarator autoDeclarator, PlaceholderKind placeholder) {
		// [dcl.type.auto.deduct] p3:
		//   If the deduction is for a return statement and the initializer is a
		//   braced-init-list, the proram is ill-formed.
		if (returnEval instanceof EvalInitList) {
			return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
		}

		if (placeholder == PlaceholderKind.DecltypeAuto) {
			if (autoDeclarator != null && autoDeclarator.getPointerOperators().length > 0) {
				// 'decltype(auto)' cannot be combined with * or & the way 'auto' can.
				return ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
			}
			if (autoDeclSpec.isConst() || autoDeclSpec.isVolatile()) {
				// 'decltype(auto)' cannot be combined with any type specifiers. [dcl.type.auto.deduct]
				return ProblemType.CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE;
			}
			return CPPSemantics.getDeclTypeForEvaluation(returnEval);
		} else /* auto */ {
			return createAutoType(returnEval, autoDeclSpec, autoDeclarator);
		}
	}

	public static IType deduceReturnType(IASTStatement functionBody, IASTDeclSpecifier autoDeclSpec,
			IASTDeclarator autoDeclarator, PlaceholderKind placeholder) {
		ICPPEvaluation[] returnEvals = ICPPEvaluation.EMPTY_ARRAY;
		if (functionBody != null) {
			ReturnTypeDeducer deducer = new ReturnTypeDeducer(null);
			functionBody.accept(deducer);
			returnEvals = deducer.getReturnEvaluations();
		}
		if (returnEvals.length == 0) {
			return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
		}

		// [dcl.spec.auto] p7:
		//   If a function with a declared return type that contains a placeholder type has multiple
		//   return statements, the return type is deduced for each such return statement.
		//   If the type deduced is not the same in each deduction, the program is ill-formed.
		IType returnType = deduceTypeFromReturnEvaluation(returnEvals[0], autoDeclSpec, autoDeclarator, placeholder);
		for (int i = 1; i < returnEvals.length; ++i) {
			IType otherType = deduceTypeFromReturnEvaluation(returnEvals[i], autoDeclSpec, autoDeclarator, placeholder);
			if (!returnType.isSameType(otherType)) {
				return ProblemType.CANNOT_DEDUCE_AUTO_TYPE;
			}
		}
		return returnType;
	}

	/**
	 * C++0x: [8.3.5-2]
	 */
	private static IType createAutoFunctionType(IASTDeclSpecifier declSpec, ICPPASTFunctionDeclarator declarator,
			int flags, PlaceholderKind placeholder) {
		IType returnType = null;
		IASTDeclSpecifier declSpecForDeduction = null;
		IASTDeclarator declaratorForDeduction = null;
		IASTTypeId trailingReturnType = declarator.getTrailingReturnType();
		if (trailingReturnType == null) {
			// No trailing return type.
			if ((flags & RESOLVE_PLACEHOLDERS) != 0) {
				declSpecForDeduction = declSpec;
				declaratorForDeduction = declarator;
			} else {
				returnType = new CPPPlaceholderType(PlaceholderKind.Auto);
			}
		} else {
			IASTDeclSpecifier trailingDeclSpec = trailingReturnType.getDeclSpecifier();
			IASTDeclarator trailingDeclarator = trailingReturnType.getAbstractDeclarator();
			PlaceholderKind trailingPlaceholder = usesAuto(trailingDeclSpec);
			if (trailingPlaceholder != null && !(trailingDeclarator instanceof IASTFunctionDeclarator)) {
				// Trailing return type uses 'auto', other than to introduce
				// another trailing return type for a function type, so we'll
				// need to look at the function body and deduce the return type.
				declSpecForDeduction = trailingDeclSpec;
				declaratorForDeduction = trailingDeclarator;
				placeholder = trailingPlaceholder;
			} else {
				// Trailing return type specifies the type.
				returnType = createType(trailingDeclarator);
				returnType = qualifyType(returnType, declSpec);
			}
		}

		if (returnType == null) {
			// Try to deduce return type from return statement.

			// [dcl.spec.auto] p12:
			//   A function declared with a return type that uses a placeholder type
			//   shall not be virtual.
			if (((ICPPASTDeclSpecifier) declSpec).isVirtual())
				return ProblemType.AUTO_FOR_VIRTUAL_METHOD;

			ICPPASTFunctionDefinition definition = CPPFunction.getFunctionDefinition(declarator);
			if (definition != null) {
				returnType = deduceReturnType(definition.getBody(), declSpecForDeduction, declaratorForDeduction,
						placeholder);
			}
		}

		if (returnType != null) {
			if ((flags & ONLY_RETURN_TYPE) != 0) {
				return returnType;
			}

			// Do not use createFunctionType() because that would decorate the return type
			// with pointer operators from e.g. an 'auto &', but we have already done that
			// above.
			IType[] pTypes = createParameterTypes(declarator);
			RefQualifier refQualifier = declarator.getRefQualifier();
			IType result = new CPPFunctionType(returnType, pTypes, declarator.getNoexceptEvaluation(),
					declarator.isConst(), declarator.isVolatile(), refQualifier != null,
					refQualifier == RefQualifier.RVALUE, declarator.takesVarArgs());
			final IASTDeclarator nested = declarator.getNestedDeclarator();
			if (nested != null) {
				result = createType(result, nested);
			}
			return result;
		}
		return ProblemType.NO_NAME;

	}

	public static IType createType(IASTDeclSpecifier declSpec) {
		IType type = getBaseType(declSpec);
		return qualifyType(type, declSpec);
	}

	private static IType getBaseType(IASTDeclSpecifier declSpec) {
		IASTName name;
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			name = ((ICPPASTCompositeTypeSpecifier) declSpec).getName();
		} else if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
			name = ((ICPPASTNamedTypeSpecifier) declSpec).getName();
		} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
			name = ((ICPPASTElaboratedTypeSpecifier) declSpec).getName();
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			name = ((IASTEnumerationSpecifier) declSpec).getName();
		} else if (declSpec instanceof ICPPASTTypeTransformationSpecifier) {
			ICPPASTTypeTransformationSpecifier spec = (ICPPASTTypeTransformationSpecifier) declSpec;
			return new CPPUnaryTypeTransformation(spec.getOperator(), createType(spec.getOperand()));
		} else if (declSpec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier spec = (ICPPASTSimpleDeclSpecifier) declSpec;
			// Check for decltype(expr)
			IType type = getDeclType(spec);
			if (type != null)
				return type;
			return new CPPBasicType(spec);
		} else {
			throw new IllegalArgumentException();
		}
		if (name == null)
			return ProblemType.NO_NAME;

		IBinding binding = name.resolvePreBinding();
		if (!(binding instanceof IProblemBinding)) {
			if (binding instanceof ICPPConstructor)
				return ((ICPPConstructor) binding).getClassOwner();

			if (binding instanceof IType)
				return (IType) binding;
		}
		return ProblemType.UNRESOLVED_NAME;
	}

	// Helper function for createAutoType().
	private static IType decorateType(IType type, IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		type = qualifyType(type, declSpec);
		type = makeConstIfConstexpr(type, declSpec, declarator);
		// Ignore function declarator because we already handled that in createAutoFunctionType().
		return createType(type, declarator, ONLY_RETURN_TYPE);
	}

	private static IType makeConstIfConstexpr(IType type, IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		// [dcl.constexpr] p9: constexpr on a variable makes it const
		if (!(declarator instanceof IASTFunctionDeclarator)) {
			if (declSpec instanceof ICPPASTDeclSpecifier) {
				if (((ICPPASTDeclSpecifier) declSpec).isConstexpr()) {
					return SemanticUtil.constQualify(type);
				}
			}
		}
		return type;
	}

	private static IType qualifyType(IType type, IASTDeclSpecifier declSpec) {
		return SemanticUtil.addQualifiers(type, declSpec.isConst(), declSpec.isVolatile(), declSpec.isRestrict());
	}

	private static IType createType(IType baseType, IASTDeclarator declarator) {
		return createType(baseType, declarator, 0);
	}

	private static IType createType(IType baseType, IASTDeclarator declarator, int flags) {
		if (((flags & ONLY_RETURN_TYPE) == 0) && declarator instanceof ICPPASTFunctionDeclarator)
			return createFunctionType(baseType, (ICPPASTFunctionDeclarator) declarator);

		IType type = baseType;
		type = applyAttributes(type, declarator);
		type = getPointerTypes(type, declarator);
		if (declarator instanceof IASTArrayDeclarator)
			type = getArrayType(type, (IASTArrayDeclarator) declarator);

		IASTDeclarator nested = declarator.getNestedDeclarator();
		if (nested != null) {
			return createType(type, nested);
		}
		return type;
	}

	/**
	 * Computes the type for decltype(expr) or typeof(expr).
	 */
	private static IType getDeclType(ICPPASTSimpleDeclSpecifier spec) {
		IASTExpression expr = spec.getDeclTypeExpression();
		if (expr == null) {
			return null;
		}
		int specifierType = spec.getType();
		if (specifierType == IASTSimpleDeclSpecifier.t_decltype) {
			return getDeclType(expr);
		}
		return expr.getExpressionType();
	}

	/**
	 * Computes the type for an expression in decltype(expr) context.
	 */
	private static IType getDeclType(IASTExpression expr) {
		IASTName namedEntity = null;
		if (expr instanceof IASTIdExpression) {
			namedEntity = ((IASTIdExpression) expr).getName();
		} else if (expr instanceof IASTFieldReference) {
			namedEntity = ((IASTFieldReference) expr).getFieldName();
		}
		if (namedEntity != null) {
			IBinding b = namedEntity.resolvePreBinding();
			if (b instanceof IType) {
				return (IType) b;
			}
			if (b instanceof IVariable) {
				return ((IVariable) b).getType();
			}
			if (b instanceof IFunction) {
				return ((IFunction) b).getType();
			}
		}
		IType type = expr.getExpressionType();
		switch (expr.getValueCategory()) {
		case XVALUE:
			type = new CPPReferenceType(type, true);
			break;
		case LVALUE:
			type = new CPPReferenceType(type, false);
			break;
		case PRVALUE:
			break;
		}
		return type;
	}

	public static IType getImpliedObjectType(IScope scope) {
		try {
			IASTNode node = null;
			while (scope != null) {
				if (scope instanceof ICPPBlockScope || scope instanceof ICPPFunctionScope) {
					node = ASTInternal.getPhysicalNodeOfScope(scope);
					if (node instanceof IASTFunctionDeclarator)
						break;
					if (node.getParent() instanceof IASTFunctionDefinition)
						break;
				} else if (scope instanceof ICPPClassScope) {
					// Reached a class scope without a function scope in between.
					// Might be in the default member initializer on a field.
					IType type = ((ICPPClassScope) scope).getClassType();
					if (type instanceof ICPPClassTemplate) {
						type = (ICPPClassType) ((ICPPClassTemplate) type).asDeferredInstance();
					}
					return type;
				}
				scope = scope.getParent();
			}
			if (node != null) {
				if (node.getParent() instanceof IASTFunctionDefinition) {
					IASTFunctionDefinition def = (IASTFunctionDefinition) node.getParent();
					node = def.getDeclarator();
				}
				if (node instanceof IASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node;
					IASTName funcName = findInnermostDeclarator(dtor).getName().getLastName();
					IScope s = getContainingScope(funcName);
					while (s instanceof ICPPTemplateScope) {
						s = s.getParent();
					}
					if (s instanceof ICPPClassScope) {
						ICPPClassScope cScope = (ICPPClassScope) s;
						IType type = cScope.getClassType();
						if (type instanceof ICPPClassTemplate) {
							type = (ICPPClassType) ((ICPPClassTemplate) type).asDeferredInstance();
						}
						return SemanticUtil.addQualifiers(type, dtor.isConst(), dtor.isVolatile(), false);
					}
				}
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
		return null;
	}

	public static IType getPointerDiffType() {
		IType t = getStdType(PTRDIFF_T);
		return t != null ? t : CPPBasicType.LONG;
	}

	private static IType getStdType(char[] name) {
		IASTNode node = CPPSemantics.getCurrentLookupPoint();
		if (node == null)
			return null;
		ASTTranslationUnit ast = (ASTTranslationUnit) node.getTranslationUnit();
		IBinding[] std = ast.getScope().find(STD, ast);
		for (IBinding binding : std) {
			if (binding instanceof ICPPNamespace) {
				final ICPPNamespaceScope scope = ((ICPPNamespace) binding).getNamespaceScope();
				IBinding[] bs = CPPSemantics.findBindings(scope, name, false, node);
				if (bs.length > 0) {
					for (IBinding b : bs) {
						if (b instanceof IType && CPPSemantics.declaredBefore(b, node, false)) {
							return (IType) b;
						}
					}
				}
			}
		}
		return null;
	}

	public static IType get_type_info() {
		IType t = getStdType(TYPE_INFO);
		return t != null ? t : CPPBasicType.INT;
	}

	public static IType get_SIZE_T() {
		IType t = getStdType(SIZE_T);
		return t != null ? t : CPPBasicType.UNSIGNED_LONG;
	}

	public static ICPPClassTemplate get_initializer_list() {
		IType t = getStdType(INITIALIZER_LIST);
		if (t instanceof ICPPClassTemplate)
			return (ICPPClassTemplate) t;
		return null;
	}

	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		tu.accept(action);
		return action.getProblems();
	}

	public static IASTName[] getReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction(binding);
		tu.accept(action);
		return action.getReferences();
	}

	public static IASTName[] getImplicitReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction(binding) {
			{
				shouldVisitNames = false;
				shouldVisitImplicitNames = true;
				shouldVisitImplicitNameAlternates = true;
			}
		};
		tu.accept(action);
		return action.getReferences();
	}

	public static IASTName[] getDeclarations(IASTTranslationUnit tu, IBinding binding) {
		return getDeclarations(tu, binding, false);
	}

	public static IASTName[] getDeclarations(IASTTranslationUnit tu, IBinding binding, boolean permissive) {
		CollectDeclarationsAction action = new CollectDeclarationsAction(binding, permissive);
		tu.accept(action);

		IASTName[] found = action.getDeclarations();
		if (found.length == 0 && binding instanceof ICPPSpecialization && binding instanceof ICPPInternalBinding) {
			IASTNode node = ((ICPPInternalBinding) binding).getDefinition();
			if (node == null) {
				IASTNode[] nds = ((ICPPInternalBinding) binding).getDeclarations();
				if (nds != null && nds.length > 0)
					node = nds[0];
			}
			if (node != null) {
				IASTName name = null;
				if (node instanceof IASTDeclarator) {
					name = ((IASTDeclarator) node).getName();
				} else if (node instanceof IASTName) {
					name = (IASTName) node;
				}
				if (name != null)
					found = new IASTName[] { name };
			}
		}

		return found;
	}

	public static String[] getQualifiedName(IBinding binding) {
		String[] ns = null;
		for (IBinding owner = binding.getOwner(); owner != null; owner = owner.getOwner()) {
			if (owner instanceof ICPPEnumeration && !((ICPPEnumeration) owner).isScoped()) {
				continue;
			}
			String name = owner.getName();
			if (name == null)
				break;
			if (owner instanceof ICPPFunction)
				break;
			if (owner instanceof ICPPNamespace && name.length() == 0) {
				continue;
			}

			ns = ArrayUtil.append(String.class, ns, name);
		}
		ns = ArrayUtil.trim(String.class, ns);
		String[] result = new String[ns.length + 1];
		for (int i = ns.length; --i >= 0;) {
			result[ns.length - i - 1] = ns[i];
		}
		result[ns.length] = binding.getName();
		return result;
	}

	public static char[][] getQualifiedNameCharArray(IBinding binding) {
		char[][] ns = EMPTY_CHAR_ARRAY_ARRAY;
		ns = ArrayUtil.append(ns, binding.getNameCharArray());
		for (IBinding owner = binding.getOwner(); owner != null; owner = owner.getOwner()) {
			char[] name = owner.getNameCharArray();
			if (name == null)
				break;
			if (owner instanceof ICPPFunction)
				break;
			if (owner instanceof ICPPNamespace && name.length == 0)
				continue;

			ns = ArrayUtil.append(ns, name);
		}
		ns = ArrayUtil.trim(ns);
		ArrayUtil.reverse(ns);
		return ns;
	}

	public static boolean isExternC(IASTNode node) {
		while (node != null) {
			node = node.getParent();
			if (node instanceof ICPPASTLinkageSpecification) {
				if ("\"C\"".equals(((ICPPASTLinkageSpecification) node).getLiteral())) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isExternC(IASTNode definition, IASTNode[] declarations) {
		if (isExternC(definition))
			return true;

		if (declarations != null) {
			for (IASTNode element : declarations) {
				if (isExternC(element))
					return true;
			}
		}
		return false;
	}

	/**
	 * Searches for the function or class enclosing the given node. May return <code>null</code>.
	 */
	public static IBinding findEnclosingFunctionOrClass(IASTNode node) {
		IASTName name = null;
		for (; node != null; node = node.getParent()) {
			if (node instanceof IASTFunctionDefinition) {
				IASTDeclarator dtor = findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
				if (dtor != null) {
					name = dtor.getName();
				}
				break;
			}
			if (node instanceof IASTCompositeTypeSpecifier) {
				name = ((IASTCompositeTypeSpecifier) node).getName();
				break;
			}
		}
		if (name == null)
			return null;

		return name.resolveBinding();
	}

	public static IBinding findNameOwner(IASTName name, boolean allowFunction) {
		IASTNode node = name.getLastName();
		while (node instanceof IASTName) {
			if (node instanceof ICPPASTQualifiedName) {
				ICPPASTNameSpecifier[] segments = ((ICPPASTQualifiedName) node).getAllSegments();
				int i = segments.length;
				while (--i >= 0) {
					if (segments[i] == name) {
						break;
					}
				}
				if (--i < 0)
					break;
				IBinding binding = segments[i].resolveBinding();
				if (binding instanceof IIndexBinding && binding instanceof ICPPClassType) {
					binding = (ICPPClassType) SemanticUtil.mapToAST((ICPPClassType) binding);
				}
				return bindingToOwner(binding);
			}
			name = (IASTName) node;
			node = node.getParent();
		}
		return findDeclarationOwner(node, allowFunction);
	}

	private static IBinding bindingToOwner(IBinding b) {
		if (b instanceof ITypedef) {
			IType t = SemanticUtil.getNestedType((IType) b, TDEF);
			if (t instanceof IBinding)
				return (IBinding) t;

			return b;
		}
		while (b instanceof ICPPNamespaceAlias) {
			b = ((ICPPNamespaceAlias) b).getBinding();
		}
		return b;
	}

	/**
	 * Searches for the first class, namespace, or function, if <code>allowFunction</code>
	 * is <code>true</code>, enclosing the declaration the provided node belongs to and returns
	 * the binding for it. Returns <code>null</code>, if the declaration is not enclosed by any
	 * of the above constructs.
	 */
	public static IBinding findDeclarationOwner(IASTNode node, boolean allowFunction) {
		IASTName name = findDeclarationOwnerDefinition(node, allowFunction);
		if (name == null)
			return null;

		return name.resolveBinding();
	}

	public static IASTName findDeclarationOwnerDefinition(IASTNode node, boolean allowFunction) {
		// Search for declaration
		boolean isNonSimpleElabDecl = false;
		while (!(node instanceof IASTDeclaration) && !(node instanceof ICPPASTLambdaExpression)) {
			if (node == null)
				return null;
			if (node instanceof IASTElaboratedTypeSpecifier) {
				isNonSimpleElabDecl = true;
				final IASTNode parent = node.getParent();
				if (parent instanceof IASTSimpleDeclaration) {
					final IASTSimpleDeclaration decl = (IASTSimpleDeclaration) parent;
					if (decl.getDeclarators().length == 0) {
						isNonSimpleElabDecl = false;
					}
				}
			} else if (node instanceof IASTEnumerator) {
				break;
			}
			node = node.getParent();
		}

		boolean isFriend = isFriendDeclaration(node);

		// Search for enclosing binding.
		for (node = node.getParent(); node != null; node = node.getParent()) {
			if (node instanceof IASTFunctionDefinition) {
				if (allowFunction) {
					IASTDeclarator dtor = findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
					if (dtor != null) {
						return dtor.getName();
					}
				}
				return null;
			}
			if (node instanceof IASTCompositeTypeSpecifier) {
				if (isFriend || isNonSimpleElabDecl)
					continue;
				return ((IASTCompositeTypeSpecifier) node).getName();
			}
			if (node instanceof ICPPASTNamespaceDefinition) {
				return ((ICPPASTNamespaceDefinition) node).getName();
			}
			if (node instanceof ICPPASTEnumerationSpecifier) {
				return ((ICPPASTEnumerationSpecifier) node).getName();
			}
			if (node instanceof ICPPASTLambdaExpression) {
				return ((ICPPASTLambdaExpression) node).getClosureTypeName();
			}
		}
		return null;
	}

	public static boolean doesNotSpecifyType(IASTDeclSpecifier declspec) {
		if (declspec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier ds = (ICPPASTSimpleDeclSpecifier) declspec;
			if (ds.getType() == IASTSimpleDeclSpecifier.t_unspecified) {
				if (ds.isShort() || ds.isLong() || ds.isLongLong() || ds.isSigned() || ds.isUnsigned())
					return false;

				return true;
			}
		}
		return false;
	}

	public static ICPPASTDeclarator findInnermostDeclarator(ICPPASTDeclarator dtor) {
		return (ICPPASTDeclarator) ASTQueries.findInnermostDeclarator(dtor);
	}

	/**
	 * Traverses a chain of nested homogeneous left-to-right-associative binary expressions and
	 * returns a list of their operands in left-to-right order. For example, for the expression
	 * a + b * c + d, it will return a list containing expressions: a, b * c, and d.
	 *
	 * @param binaryExpression the top-level binary expression
	 * @return a list of expression operands from left to right
	 */
	public static IASTExpression[] getOperandsOfMultiExpression(IASTBinaryExpression binaryExpression) {
		int operator = binaryExpression.getOperator();
		IASTExpression[] operands = new IASTExpression[2];
		IASTExpression node;
		int len = 0;
		do {
			operands = ArrayUtil.appendAt(operands, len++, binaryExpression.getOperand2());
			node = binaryExpression.getOperand1();
			if (!(node instanceof IASTBinaryExpression)) {
				break;
			}
			binaryExpression = (IASTBinaryExpression) node;
		} while (binaryExpression.getOperator() == operator);
		operands = ArrayUtil.appendAt(operands, len++, node);
		operands = ArrayUtil.trim(operands, len);
		ArrayUtil.reverse(operands);
		return operands;
	}

	/**
	 * Determines whether the given {@code namespace} definition denotes
	 * an anonymous namespace.
	 * @param namespace
	 * @return {@code true} if the {@code namespace} is anonymous, false otherwise
	 */
	public static boolean isAnonymousNamespace(ICPPASTNamespaceDefinition namespace) {
		IASTName name = namespace.getName();
		return name == null || name.toString().isEmpty();
	}
}
