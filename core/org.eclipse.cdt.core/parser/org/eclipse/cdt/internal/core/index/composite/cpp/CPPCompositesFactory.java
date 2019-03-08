/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacroContainer;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPAliasTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnaryTypeTransformation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredVariableInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPFunctionSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinaryTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalComma;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalCompositeAccess;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalCompoundStatementExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalConditional;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionCall;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalID;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalInitList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalMemberAccess;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalNaryTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalPackExpansion;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalPointer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnaryTypeID;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.InitializerListType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfDependentExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfUnknownMember;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.AbstractCompositeFactory;
import org.eclipse.cdt.internal.core.index.composite.CompositeMacroContainer;
import org.eclipse.cdt.internal.core.index.composite.CompositingNotImplementedError;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CPPCompositesFactory extends AbstractCompositeFactory {

	public CPPCompositesFactory(IIndex index) {
		super(index);
	}

	@Override
	public IIndexScope getCompositeScope(IIndexScope rscope) {
		try {
			if (rscope == null) {
				return null;
			}
			if (rscope.getKind() == EScopeKind.eGlobal) {
				return rscope;
			}
			if (rscope instanceof ICPPClassScope) {
				if (rscope instanceof ICPPClassSpecializationScope) {
					return new CompositeCPPClassSpecializationScope(this,
							(IIndexFragmentBinding) rscope.getScopeBinding());
				}
				ICPPClassScope classScope = (ICPPClassScope) rscope;
				return new CompositeCPPClassScope(this, findOneBinding(classScope.getClassType()));
			}
			if (rscope instanceof ICPPEnumScope) {
				ICPPEnumScope enumScope = (ICPPEnumScope) rscope;
				return new CompositeCPPEnumScope(this, findOneBinding(enumScope.getEnumerationType()));
			}
			if (rscope instanceof ICPPNamespaceScope) {
				ICPPNamespace[] namespaces;
				if (rscope instanceof CompositeCPPNamespace) {
					// Avoid duplicating the search.
					namespaces = ((CompositeCPPNamespace) rscope).namespaces;
				} else {
					namespaces = getNamespaces(rscope.getScopeBinding());
				}
				return new CompositeCPPNamespaceScope(this, namespaces);
			}
			throw new CompositingNotImplementedError(rscope.getClass().getName());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			throw new CompositingNotImplementedError(e.getMessage());
		}
	}

	@Override
	public IType getCompositeType(IType rtype) {
		if (rtype instanceof IIndexFragmentBinding) {
			return (IType) getCompositeBinding((IIndexFragmentBinding) rtype);
		}
		if (rtype instanceof ICPPFunctionType) {
			ICPPFunctionType ft = (ICPPFunctionType) rtype;
			IType r = ft.getReturnType();
			IType r2 = getCompositeType(r);
			IType[] p = ft.getParameterTypes();
			IType[] p2 = getCompositeTypes(p);
			ICPPEvaluation n = ft.getNoexceptSpecifier();
			ICPPEvaluation n2 = getCompositeEvaluation(n);
			if (r != r2 || p != p2 || n != n2) {
				return new CPPFunctionType(r2, p2, n2, ft.isConst(), ft.isVolatile(), ft.hasRefQualifier(),
						ft.isRValueReference(), ft.takesVarArgs());
			}
			return ft;
		}
		if (rtype instanceof ICPPPointerToMemberType) {
			ICPPPointerToMemberType pmt = (ICPPPointerToMemberType) rtype;
			IType ct = pmt.getMemberOfClass();
			IType ct2 = getCompositeType(ct);
			IType t = pmt.getType();
			IType t2 = getCompositeType(t);
			if (ct != ct2 || t != t2) {
				return new CPPPointerToMemberType(t2, ct2, pmt.isConst(), pmt.isVolatile(), pmt.isRestrict());
			}
			return pmt;
		}
		if (rtype instanceof IPointerType) {
			IPointerType pt = (IPointerType) rtype;
			IType r = pt.getType();
			IType r2 = getCompositeType(r);
			if (r != r2) {
				return new CPPPointerType(r2, pt.isConst(), pt.isVolatile(), pt.isRestrict());
			}
			return pt;
		}
		if (rtype instanceof ICPPReferenceType) {
			ICPPReferenceType rt = (ICPPReferenceType) rtype;
			IType r = rt.getType();
			IType r2 = getCompositeType(r);
			if (r != r2) {
				return new CPPReferenceType(r2, rt.isRValueReference());
			}
			return rt;
		}
		if (rtype instanceof ICPPParameterPackType) {
			ICPPParameterPackType rt = (ICPPParameterPackType) rtype;
			IType r = rt.getType();
			IType r2 = getCompositeType(r);
			if (r != r2 && r2 != null) {
				return new CPPParameterPackType(r2);
			}
			return rt;
		}
		if (rtype instanceof IQualifierType) {
			IQualifierType qt = (IQualifierType) rtype;
			IType r = qt.getType();
			IType r2 = getCompositeType(r);
			if (r != r2) {
				return new CPPQualifierType(r2, qt.isConst(), qt.isVolatile());
			}
			return qt;
		}
		if (rtype instanceof IArrayType) {
			IArrayType at = (IArrayType) rtype;
			IType r = at.getType();
			IType r2 = getCompositeType(r);
			IValue v = at.getSize();
			IValue v2 = getCompositeValue(v);
			if (r != r2 || v != v2) {
				return new CPPArrayType(r2, v2);
			}
			return at;
		}
		if (rtype instanceof ICPPAliasTemplateInstance) {
			ICPPAliasTemplateInstance instance = (ICPPAliasTemplateInstance) rtype;
			ICPPAliasTemplate aliasTemplate = instance.getTemplateDefinition();
			if (aliasTemplate instanceof IIndexFragmentBinding)
				aliasTemplate = (ICPPAliasTemplate) getCompositeBinding((IIndexFragmentBinding) aliasTemplate);
			IType aliasedType = getCompositeType(instance.getType());
			IBinding owner = instance.getOwner();
			if (owner instanceof IIndexFragmentBinding)
				owner = getCompositeBinding((IIndexFragmentBinding) owner);
			ICPPTemplateArgument[] args = TemplateInstanceUtil.convert(this, instance.getTemplateArguments());
			ICPPTemplateParameterMap map = TemplateInstanceUtil.getTemplateParameterMap(this, instance);
			return new CPPAliasTemplateInstance(aliasTemplate, aliasedType, owner, map, args);
		}
		if (rtype instanceof TypeOfDependentExpression) {
			TypeOfDependentExpression type = (TypeOfDependentExpression) rtype;
			ICPPEvaluation e = type.getEvaluation();
			ICPPEvaluation e2 = getCompositeEvaluation(e);
			if (e != e2)
				return new TypeOfDependentExpression(e2);
			return type;
		}
		if (rtype instanceof TypeOfUnknownMember) {
			CPPUnknownMember member = ((TypeOfUnknownMember) rtype).getUnknownMember();
			if (member instanceof IIndexFragmentBinding)
				member = (CPPUnknownMember) getCompositeBinding((IIndexFragmentBinding) member);
			return new TypeOfUnknownMember(member);

		}
		if (rtype instanceof ICPPUnaryTypeTransformation) {
			ICPPUnaryTypeTransformation typeTransformation = (ICPPUnaryTypeTransformation) rtype;
			IType operand = getCompositeType(typeTransformation.getOperand());
			return new CPPUnaryTypeTransformation(typeTransformation.getOperator(), operand);
		}
		if (rtype instanceof InitializerListType) {
			EvalInitList e = ((InitializerListType) rtype).getEvaluation();
			EvalInitList e2 = (EvalInitList) getCompositeEvaluation(e);
			if (e2 != e)
				return new InitializerListType(e2);
			return rtype;
		}
		if (rtype instanceof IBasicType || rtype == null || rtype instanceof ISemanticProblem) {
			return rtype;
		}

		throw new CompositingNotImplementedError(rtype.getClass().getName());
	}

	public ICPPEvaluation getCompositeEvaluation(ICPPEvaluation eval) {
		if (eval == null)
			return null;
		IBinding templateDefinition = eval.getTemplateDefinition();
		IBinding compositeTemplateDefinition = templateDefinition instanceof IProblemBinding ? templateDefinition
				: getCompositeBinding((IIndexFragmentBinding) templateDefinition);
		if (eval instanceof EvalBinary) {
			EvalBinary e = (EvalBinary) eval;
			ICPPEvaluation a = e.getArg1();
			ICPPEvaluation b = e.getArg2();

			ICPPEvaluation a2 = getCompositeEvaluation(a);
			ICPPEvaluation b2 = getCompositeEvaluation(b);
			if (a != a2 || b != b2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalBinary(e.getOperator(), a2, b2, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalBinaryTypeId) {
			EvalBinaryTypeId e = (EvalBinaryTypeId) eval;
			IType a = e.getType1();
			IType b = e.getType2();

			IType a2 = getCompositeType(a);
			IType b2 = getCompositeType(b);
			if (a != a2 || b != b2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalBinaryTypeId(e.getOperator(), a2, b2, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalBinding) {
			EvalBinding e = (EvalBinding) eval;
			ICPPFunction parameterOwner = e.getParameterOwner();
			if (parameterOwner != null) {
				IType b = e.getFixedType();
				IBinding a2 = parameterOwner;
				if (parameterOwner instanceof IIndexFragmentBinding) {
					a2 = getCompositeBinding((IIndexFragmentBinding) parameterOwner);
				}
				IType b2 = getCompositeType(b);
				if (parameterOwner != a2 || b != b2 || templateDefinition != compositeTemplateDefinition) {
					int parameterPosition = e.getFunctionParameterPosition();
					e = new EvalBinding((ICPPFunction) a2, parameterPosition, b2, compositeTemplateDefinition);
				}
			} else {
				IBinding a = e.getBinding();
				IType b = e.getFixedType();

				IBinding a2 = a;
				if (a instanceof IIndexFragmentBinding) {
					a2 = getCompositeBinding((IIndexFragmentBinding) a);
				}
				IType b2 = getCompositeType(b);
				if (a != a2 || b != b2 || templateDefinition != compositeTemplateDefinition)
					e = new EvalBinding(a2, b2, compositeTemplateDefinition);
			}
			return e;
		}
		if (eval instanceof EvalComma) {
			EvalComma e = (EvalComma) eval;
			ICPPEvaluation[] a = e.getArguments();

			ICPPEvaluation[] a2 = getCompositeEvaluationArray(a);

			if (a != a2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalComma(a2, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalCompositeAccess) {
			EvalCompositeAccess e = (EvalCompositeAccess) eval;
			ICPPEvaluation a = e.getParent();
			ICPPEvaluation a2 = getCompositeEvaluation(a);
			if (a != a2)
				e = new EvalCompositeAccess(a2, e.getElementId());
			return e;
		}
		if (eval instanceof EvalCompoundStatementExpression) {
			EvalCompoundStatementExpression e = (EvalCompoundStatementExpression) eval;
			ICPPEvaluation a = e.getLastEvaluation();
			ICPPEvaluation a2 = getCompositeEvaluation(a);
			if (a != a2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalCompoundStatementExpression(a2, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalConditional) {
			EvalConditional e = (EvalConditional) eval;
			ICPPEvaluation a = e.getCondition();
			ICPPEvaluation b = e.getPositive();
			ICPPEvaluation c = e.getNegative();
			ICPPEvaluation a2 = getCompositeEvaluation(a);
			ICPPEvaluation b2 = getCompositeEvaluation(b);
			ICPPEvaluation c2 = getCompositeEvaluation(c);
			if (a != a2 || b != b2 || c != c2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalConditional(a2, b2, c2, e.isPositiveThrows(), e.isNegativeThrows(),
						compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalConstructor) {
			EvalConstructor e = (EvalConstructor) eval;
			IType a = e.getType();
			ICPPConstructor b = e.getConstructor();
			ICPPEvaluation[] c = e.getArguments();
			IType a2 = getCompositeType(a);
			ICPPConstructor b2 = b;
			if (b instanceof IIndexFragmentBinding) {
				IBinding binding = getCompositeBinding((IIndexFragmentBinding) b);
				if (binding instanceof ICPPConstructor) {
					b2 = (ICPPConstructor) binding;
				}
			}
			ICPPEvaluation[] c2 = getCompositeEvaluationArray(c);
			if (a != a2 || b != b2 || c != c2 || templateDefinition != compositeTemplateDefinition) {
				e = new EvalConstructor(a2, b2, c2, compositeTemplateDefinition);
			}
			return e;
		}
		if (eval instanceof EvalFixed) {
			EvalFixed e = (EvalFixed) eval;
			IType a = e.getType();
			IValue b = e.getValue();
			IType a2 = getCompositeType(a);
			IValue b2 = getCompositeValue(b);
			if (a != a2 || b != b2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalFixed(a2, e.getValueCategory(), b2);
			return e;
		}
		if (eval instanceof EvalFunctionCall) {
			EvalFunctionCall e = (EvalFunctionCall) eval;
			ICPPEvaluation[] a = e.getArguments();
			ICPPEvaluation[] a2 = getCompositeEvaluationArray(a);
			if (a != a2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalFunctionCall(a2, null, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalFunctionSet) {
			EvalFunctionSet e = (EvalFunctionSet) eval;
			final CPPFunctionSet fset = e.getFunctionSet();
			if (fset != null) {
				ICPPFunction[] a = fset.getBindings();
				ICPPTemplateArgument[] b = fset.getTemplateArguments();
				IType c = e.getImpliedObjectType();

				ICPPFunction[] a2 = getCompositeFunctionArray(a);
				ICPPTemplateArgument[] b2 = TemplateInstanceUtil.convert(this, b);
				IType c2 = getCompositeType(c);
				if (a != a2 || b != b2 || c != c2 || templateDefinition != compositeTemplateDefinition)
					e = new EvalFunctionSet(new CPPFunctionSet(a2, b2, null), e.isQualified(), e.isAddressOf(), c2,
							compositeTemplateDefinition);
			}
			return e;
		}
		if (eval instanceof EvalID) {
			EvalID e = (EvalID) eval;
			ICPPEvaluation a = e.getFieldOwner();
			IBinding b = e.getNameOwner();
			ICPPTemplateArgument[] c = e.getTemplateArgs();

			ICPPEvaluation a2 = getCompositeEvaluation(a);
			IBinding b2 = b;
			if (b instanceof IIndexFragmentBinding) {
				b2 = getCompositeBinding((IIndexFragmentBinding) b);
			} else if (b instanceof IType) {
				b2 = (IBinding) getCompositeType((IType) b);
			}
			ICPPTemplateArgument[] c2 = TemplateInstanceUtil.convert(this, c);

			if (a != a2 || b != b2 || c != c2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalID(a2, b2, e.getName(), e.isAddressOf(), e.isQualified(), e.isPointerDeref(), c2,
						compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalInitList) {
			EvalInitList e = (EvalInitList) eval;
			ICPPEvaluation[] a = e.getClauses();
			ICPPEvaluation[] a2 = getCompositeEvaluationArray(a);
			if (a != a2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalInitList(a2, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalMemberAccess) {
			EvalMemberAccess e = (EvalMemberAccess) eval;
			IType a = e.getOwnerType();
			IBinding b = e.getMember();
			ICPPEvaluation c = e.getOwnerEval();
			IType a2 = getCompositeType(a);
			IBinding b2 = b;
			ICPPEvaluation c2 = c;
			if (b instanceof IIndexFragmentBinding) {
				b2 = getCompositeBinding((IIndexFragmentBinding) b);
			}
			if (c != null) {
				c2 = getCompositeEvaluation(c);
			}
			if (a != a2 || b != b2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalMemberAccess(a2, e.getOwnerValueCategory(), b2, c2, e.isPointerDeref(),
						compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalNaryTypeId) {
			EvalNaryTypeId e = (EvalNaryTypeId) eval;
			IType[] operands = e.getOperands();
			IType[] operands2 = getCompositeTypes(operands);
			if (operands != operands2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalNaryTypeId(e.getOperator(), operands2, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalPackExpansion) {
			EvalPackExpansion e = (EvalPackExpansion) eval;
			ICPPEvaluation a = e.getExpansionPattern();
			ICPPEvaluation a2 = getCompositeEvaluation(a);
			if (a != a2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalPackExpansion(a2, compositeTemplateDefinition);
			return e;
		}
		// EvalPointer is handled as a sub-case of EvalReference.
		if (eval instanceof EvalReference) {
			EvalReference e = (EvalReference) eval;
			IBinding a = e.getReferredBinding();
			// TODO: Does the ActivationRecord need conversion to composite bindings?
			if (a != null) {
				IBinding a2 = a;
				if (a instanceof IIndexFragmentBinding) {
					a2 = getCompositeBinding((IIndexFragmentBinding) a);
				}
				if (a != a2 || templateDefinition != compositeTemplateDefinition) {
					e = new EvalReference(e.getOwningRecord(), a2, compositeTemplateDefinition);
				}
			} else {
				EvalCompositeAccess b = e.getReferredSubValue();
				EvalCompositeAccess b2 = b;
				ICPPEvaluation composite = getCompositeEvaluation(b2);
				if (eval instanceof EvalCompositeAccess) {
					b2 = (EvalCompositeAccess) composite;
				}
				if (b != b2 || templateDefinition != compositeTemplateDefinition) {
					if (e instanceof EvalPointer) {
						e = new EvalPointer(e.getOwningRecord(), b2, compositeTemplateDefinition,
								((EvalPointer) e).getPosition());
					} else {
						e = new EvalReference(e.getOwningRecord(), b2, compositeTemplateDefinition);
					}
				}
			}
			return e;
		}
		if (eval instanceof EvalTypeId) {
			EvalTypeId e = (EvalTypeId) eval;
			IType a = e.getInputType();
			ICPPEvaluation[] b = e.getArguments();
			IType a2 = getCompositeType(a);
			ICPPEvaluation[] b2 = getCompositeEvaluationArray(b);
			if (a != a2 || b != b2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalTypeId(a2, compositeTemplateDefinition, e.representsNewExpression(), e.usesBracedInitList(),
						b2);
			return e;
		}
		if (eval instanceof EvalUnary) {
			EvalUnary e = (EvalUnary) eval;
			ICPPEvaluation a = e.getArgument();
			ICPPEvaluation a2 = getCompositeEvaluation(a);
			IBinding b = e.getAddressOfQualifiedNameBinding();
			IBinding b2 = b;
			if (b instanceof IIndexFragmentBinding) {
				b2 = getCompositeBinding((IIndexFragmentBinding) b);
			}
			if (a != a2 || b != b2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalUnary(e.getOperator(), a2, b2, compositeTemplateDefinition);
			return e;
		}
		if (eval instanceof EvalUnaryTypeID) {
			EvalUnaryTypeID e = (EvalUnaryTypeID) eval;
			IType a = e.getArgument();
			IType a2 = getCompositeType(a);
			if (a != a2 || templateDefinition != compositeTemplateDefinition)
				e = new EvalUnaryTypeID(e.getOperator(), a2, compositeTemplateDefinition);
			return e;
		}

		throw new CompositingNotImplementedError(eval.getClass().getName());
	}

	private ICPPEvaluation[] getCompositeEvaluationArray(ICPPEvaluation[] array) {
		ICPPEvaluation[] array2 = array;
		for (int i = 0; i < array.length; i++) {
			ICPPEvaluation a = array[i];
			ICPPEvaluation a2 = getCompositeEvaluation(a);
			if (array != array2) {
				array2[i] = a2;
			} else if (a != a2) {
				array2 = new ICPPEvaluation[array.length];
				System.arraycopy(array, 0, array2, 0, i);
				array2[i] = a2;
			}
		}
		return array2;
	}

	private ICPPFunction[] getCompositeFunctionArray(ICPPFunction[] array) {
		ICPPFunction[] array2 = array;
		for (int i = 0; i < array.length; i++) {
			ICPPFunction a = array[i];
			ICPPFunction a2 = (ICPPFunction) getCompositeBinding((IIndexFragmentBinding) a);
			if (array != array2) {
				array2[i] = a2;
			} else if (a != a2) {
				array2 = new ICPPFunction[array.length];
				System.arraycopy(array, 0, array2, 0, i);
				array2[i] = a2;
			}
		}
		return array2;
	}

	@Override
	public IValue getCompositeValue(IValue v) {
		if (v == null)
			return null;

		ICPPEvaluation eval = v.getEvaluation();
		if (eval == null)
			return v;

		eval = getCompositeEvaluation(eval);
		return DependentValue.create(eval);
	}

	private ICPPNamespace[] getNamespaces(IBinding rbinding) throws CoreException {
		CIndex cindex = (CIndex) index;
		IIndexBinding[] ibs = cindex.findEquivalentBindings(rbinding);
		ICPPNamespace[] namespaces = new ICPPNamespace[ibs.length];
		for (int i = 0; i < namespaces.length; i++)
			namespaces[i] = (ICPPNamespace) ibs[i];
		return namespaces;
	}

	IIndex getContext() {
		return index;
	}

	protected IIndexFragmentBinding findOneBinding(IBinding binding) {
		return findOneBinding(binding, false);
	}

	@Override
	public IIndexBinding getCompositeBinding(IIndexFragmentBinding binding) {
		IIndexBinding result;

		try {
			if (binding == null) {
				result = null;
			} else if (binding instanceof ICPPSpecialization) {
				if (binding instanceof ICPPTemplateInstance) {
					if (binding instanceof ICPPDeferredClassInstance) {
						ICPPDeferredClassInstance def = (ICPPDeferredClassInstance) binding;
						ICPPClassTemplate t0 = def.getClassTemplate();
						ICPPTemplateArgument[] args0 = def.getTemplateArguments();

						ICPPClassTemplate t = (ICPPClassTemplate) getCompositeType(t0);
						ICPPTemplateArgument[] args = TemplateInstanceUtil.convert(this, args0);
						return new CompositeCPPDeferredClassInstance(t, args);
					} else if (binding instanceof ICPPDeferredVariableInstance) {
						ICPPDeferredVariableInstance def = (ICPPDeferredVariableInstance) binding;
						ICPPVariableTemplate t0 = def.getTemplateDefinition();
						ICPPTemplateArgument[] args0 = def.getTemplateArguments();

						ICPPVariableTemplate t = (ICPPVariableTemplate) getCompositeBinding((IIndexFragmentBinding) t0);
						ICPPTemplateArgument[] args = TemplateInstanceUtil.convert(this, args0);
						return new CompositeCPPDeferredVariableInstance(t, args);
					} else {
						if (binding instanceof ICPPClassType) {
							return new CompositeCPPClassInstance(this, (ICPPClassType) findOneBinding(binding));
						} else if (binding instanceof ICPPConstructor) {
							return new CompositeCPPConstructorInstance(this, (ICPPConstructor) binding);
						} else if (binding instanceof ICPPMethod) {
							return new CompositeCPPMethodInstance(this, (ICPPMethod) binding);
						} else if (binding instanceof ICPPFunction) {
							return new CompositeCPPFunctionInstance(this, (ICPPFunction) binding);
						} else if (binding instanceof ICPPField) {
							return new CompositeCPPFieldInstance(this, (ICPPVariableInstance) binding);
						} else if (binding instanceof ICPPVariable) {
							return new CompositeCPPVariableInstance(this, (ICPPVariableInstance) binding);
						} else if (binding instanceof ICPPAliasTemplateInstance) {
							return new CompositeCPPAliasTemplateInstance(this, (ICPPAliasTemplateInstance) binding);
						} else {
							throw new CompositingNotImplementedError(
									"Composite binding unavailable for " + binding + " " + binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				} else if (binding instanceof ICPPTemplateDefinition) {
					if (binding instanceof ICPPClassTemplatePartialSpecialization) {
						return new CompositeCPPClassTemplatePartialSpecializationSpecialization(this,
								(ICPPClassTemplatePartialSpecializationSpecialization) binding);
					} else if (binding instanceof ICPPClassType) {
						return new CompositeCPPClassTemplateSpecialization(this, (ICPPClassType) binding);
					} else if (binding instanceof ICPPConstructor) {
						return new CompositeCPPConstructorTemplateSpecialization(this, (ICPPConstructor) binding);
					} else if (binding instanceof ICPPMethod) {
						return new CompositeCPPMethodTemplateSpecialization(this, (ICPPMethod) binding);
					} else if (binding instanceof ICPPFunction) {
						return new CompositeCPPFunctionTemplateSpecialization(this, (ICPPFunction) binding);
					} else if (binding instanceof ICPPAliasTemplate) {
						return new CompositeCPPAliasTemplateSpecialization(this, (ICPPAliasTemplate) binding);
					} else {
						throw new CompositingNotImplementedError(
								"Composite binding unavailable for " + binding + " " + binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					if (binding instanceof ICPPClassType) {
						return new CompositeCPPClassSpecialization(this, (ICPPClassType) findOneBinding(binding));
					} else if (binding instanceof ICPPConstructor) {
						return new CompositeCPPConstructorSpecialization(this,
								(ICPPConstructor) findOneBinding(binding, true));
					} else if (binding instanceof ICPPMethod) {
						return new CompositeCPPMethodSpecialization(this, (ICPPMethod) findOneBinding(binding, true));
					} else if (binding instanceof ICPPFunction) {
						return new CompositeCPPFunctionSpecialization(this,
								(ICPPFunction) findOneBinding(binding, true));
					} else if (binding instanceof ICPPField) {
						return new CompositeCPPField(this, (ICPPField) binding);
					} else if (binding instanceof ICPPParameter) {
						return new CompositeCPPParameterSpecialization(this, (ICPPParameter) binding);
					} else if (binding instanceof ITypedef) {
						return new CompositeCPPTypedefSpecialization(this, (ICPPBinding) binding);
					} else if (binding instanceof ICPPUsingDeclaration) {
						return new CompositeCPPUsingDeclarationSpecialization(this, (ICPPUsingDeclaration) binding);
					} else if (binding instanceof ICPPEnumeration) {
						return new CompositeCPPEnumerationSpecialization(this, (ICPPEnumeration) binding);
					} else if (binding instanceof ICPPInternalEnumerator) {
						return new CompositeCPPEnumeratorSpecialization(this, (ICPPInternalEnumerator) binding);
					} else {
						throw new CompositingNotImplementedError(
								"Composite binding unavailable for " + binding + " " + binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else if (binding instanceof ICPPClassTemplatePartialSpecialization) {
				return new CompositeCPPClassTemplatePartialSpecialization(this,
						(ICPPClassTemplatePartialSpecialization) findOneBinding(binding));
			} else if (binding instanceof ICPPVariableTemplatePartialSpecialization) {
				if (binding instanceof ICPPField) {
					return new CompositeCPPFieldTemplatePartialSpecialization(this,
							(ICPPVariableTemplatePartialSpecialization) binding);
				} else {
					return new CompositeCPPVariableTemplatePartialSpecialization(this,
							(ICPPVariableTemplatePartialSpecialization) binding);
				}
			} else if (binding instanceof ICPPTemplateParameter) {
				if (binding instanceof ICPPTemplateTypeParameter) {
					result = new CompositeCPPTemplateTypeParameter(this, (ICPPTemplateTypeParameter) binding);
				} else if (binding instanceof ICPPTemplateNonTypeParameter) {
					result = new CompositeCPPTemplateNonTypeParameter(this, (ICPPTemplateNonTypeParameter) binding);
				} else if (binding instanceof ICPPTemplateTemplateParameter) {
					result = new CompositeCPPTemplateTemplateParameter(this, (ICPPTemplateTemplateParameter) binding);
				} else {
					throw new CompositingNotImplementedError(
							"Composite binding unavailable for " + binding + " " + binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if (binding instanceof ICPPTemplateDefinition) {
				if (binding instanceof ICPPClassTemplate) {
					ICPPClassType def = (ICPPClassType) findOneBinding(binding);
					return new CompositeCPPClassTemplate(this, def);
				} else if (binding instanceof ICPPConstructor) {
					return new CompositeCPPConstructorTemplate(this, (ICPPConstructor) binding);
				} else if (binding instanceof ICPPMethod) {
					return new CompositeCPPMethodTemplate(this, (ICPPMethod) binding);
				} else if (binding instanceof ICPPFunctionTemplate) {
					return new CompositeCPPFunctionTemplate(this, (ICPPFunction) binding);
				} else if (binding instanceof ICPPAliasTemplate) {
					return new CompositeCPPAliasTemplate(this, (ICPPBinding) binding);
				} else if (binding instanceof ICPPFieldTemplate) {
					ICPPField def = (ICPPField) findOneBinding(binding);
					return new CompositeCPPFieldTemplate(this, def);
				} else if (binding instanceof ICPPVariableTemplate) {
					ICPPVariable def = (ICPPVariable) findOneBinding(binding);
					return new CompositeCPPVariableTemplate(this, def);
				} else {
					throw new CompositingNotImplementedError(
							"Composite binding unavailable for " + binding + " " + binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if (binding instanceof ICPPUnknownBinding) {
				if (binding instanceof ICPPUnknownMember) {
					ICPPUnknownMember def = (ICPPUnknownMember) binding;
					IType b = getCompositeType(def.getOwnerType());
					if (binding instanceof ICPPUnknownMemberClass) {
						if (binding instanceof ICPPUnknownMemberClassInstance) {
							ICPPTemplateArgument[] args0 = ((ICPPUnknownMemberClassInstance) binding).getArguments();
							ICPPTemplateArgument[] args = TemplateInstanceUtil.convert(this, args0);
							return new CompositeCPPUnknownMemberClassInstance(b, def.getNameCharArray(), args);
						} else {
							return new CompositeCPPUnknownMemberClass(b, def.getNameCharArray());
						}
					} else if (binding instanceof ICPPField) {
						return new CompositeCPPUnknownField(b, def.getNameCharArray());
					} else if (binding instanceof ICPPMethod) {
						return new CompositeCPPUnknownMethod(b, def.getNameCharArray());
					}
				}
				throw new CompositingNotImplementedError(
						"Composite binding unavailable for " + binding + " " + binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (binding instanceof ICPPParameter) {
				result = new CompositeCPPParameter(this, (ICPPParameter) binding);
			} else if (binding instanceof ICPPField) {
				result = new CompositeCPPField(this, (ICPPField) binding);
			} else if (binding instanceof ICPPVariable) {
				result = new CompositeCPPVariable(this, (ICPPVariable) binding);
			} else if (binding instanceof ICPPClassType) {
				ICPPClassType def = (ICPPClassType) findOneBinding(binding);
				result = def == null ? null : new CompositeCPPClassType(this, def);
			} else if (binding instanceof ICPPConstructor) {
				result = new CompositeCPPConstructor(this, (ICPPConstructor) binding);
			} else if (binding instanceof ICPPMethod) {
				result = new CompositeCPPMethod(this, (ICPPMethod) binding);
			} else if (binding instanceof ICPPNamespaceAlias) {
				result = new CompositeCPPNamespaceAlias(this, (ICPPNamespaceAlias) binding);
			} else if (binding instanceof ICPPNamespace) {
				ICPPNamespace[] ns = getNamespaces(binding);
				result = ns.length == 0 ? null : new CompositeCPPNamespace(this, ns);
			} else if (binding instanceof ICPPUsingDeclaration) {
				result = new CompositeCPPUsingDeclaration(this, (ICPPUsingDeclaration) binding);
			} else if (binding instanceof ICPPEnumeration) {
				ICPPEnumeration def = (ICPPEnumeration) findOneBinding(binding);
				result = def == null ? null : new CompositeCPPEnumeration(this, def);
			} else if (binding instanceof ICPPFunction) {
				result = new CompositeCPPFunction(this, (ICPPFunction) binding);
			} else if (binding instanceof ICPPInternalEnumerator) {
				result = new CompositeCPPEnumerator(this, (ICPPInternalEnumerator) binding);
			} else if (binding instanceof ICPPAliasTemplateInstance) {
				return new CompositeCPPAliasTemplateInstance(this, (ICPPBinding) binding);
			} else if (binding instanceof ITypedef) {
				result = new CompositeCPPTypedef(this, (ICPPBinding) binding);
			} else if (binding instanceof IIndexMacroContainer) {
				result = new CompositeMacroContainer(this, binding);
			} else {
				throw new CompositingNotImplementedError(
						"Composite binding unavailable for " + binding + " " + binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
			throw new CompositingNotImplementedError(e.getMessage());
		}

		return result;
	}

	private static class Key {
		final long i;
		final int j;
		final long k;

		public Key(long id1, int id2, long id3) {
			i = id1;
			j = id2;
			k = id3;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (i ^ (i >>> 32));
			result = prime * result + j;
			result = prime * result + (int) k;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key other = (Key) obj;
				return i == other.i && j == other.j && k == other.k;
			}
			return false;
		}
	}

	public static Object createInstanceCacheKey(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		return new Key(Thread.currentThread().getId(), cf.hashCode(), rbinding.getBindingID());
	}

	public static Object createSpecializationKey(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		return new Key(Thread.currentThread().getId(), cf.hashCode(), rbinding.getBindingID() + 1);
	}
}
