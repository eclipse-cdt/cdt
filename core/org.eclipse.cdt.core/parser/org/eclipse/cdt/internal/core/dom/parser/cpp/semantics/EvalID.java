/*******************************************************************************
 * Copyright (c) 2012, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public class EvalID extends CPPDependentEvaluation {
	private final ICPPEvaluation fFieldOwner;
	private final char[] fName;
	private final IBinding fNameOwner;
	private final boolean fAddressOf;
	private final boolean fQualified;
	private final boolean fIsPointerDeref;
	private final ICPPTemplateArgument[] fTemplateArgs;

	public EvalID(ICPPEvaluation fieldOwner, IBinding nameOwner, char[] simpleID, boolean addressOf,
			boolean qualified, boolean isPointerDeref, ICPPTemplateArgument[] templateArgs, 
			IASTNode pointOfDefinition) {
		this(fieldOwner, nameOwner, simpleID, addressOf, qualified, isPointerDeref, templateArgs, 
				findEnclosingTemplate(pointOfDefinition));
	}

	public EvalID(ICPPEvaluation fieldOwner, IBinding nameOwner, char[] simpleID, boolean addressOf,
			boolean qualified, boolean isPointerDeref, ICPPTemplateArgument[] templateArgs, 
			IBinding templateDefinition) {
		super(templateDefinition);
		if (simpleID == null)
			throw new NullPointerException("simpleID"); //$NON-NLS-1$
		fFieldOwner= fieldOwner;
		fName= simpleID;
		fNameOwner= nameOwner;
		fAddressOf= addressOf;
		fQualified= qualified;
		fIsPointerDeref= isPointerDeref;
		fTemplateArgs= templateArgs;
	}

	/**
	 * Returns the field owner expression, or {@code null}.
	 */
	public ICPPEvaluation getFieldOwner() {
		return fFieldOwner;
	}

	public IBinding getNameOwner() {
		return fNameOwner;
	}

	public char[] getName() {
		return fName;
	}

	public boolean isAddressOf() {
		return fAddressOf;
	}

	public boolean isQualified() {
		return fQualified;
	}
	
	public boolean isPointerDeref() {
		return fIsPointerDeref;
	}

	/**
	 * Returns the template arguments, or {@code null} if there are no template arguments.
	 */
	public ICPPTemplateArgument[] getTemplateArgs() {
		return fTemplateArgs;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	@Override
	public boolean isTypeDependent() {
		return true;
	}

	@Override
	public boolean isValueDependent() {
		return true;
	}

	@Override
	public boolean isConstantExpression(IASTNode point) {
		return false;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		return new TypeOfDependentExpression(this);
	}

	@Override
	public IValue getValue(IASTNode point) {
		// Name lookup is not needed here because it was already done in the "instantiate" method.
		return Value.create(this);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_ID;
		if (fAddressOf)
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (fQualified)
			firstBytes |= ITypeMarshalBuffer.FLAG2;
		if (fTemplateArgs != null)
			firstBytes |= ITypeMarshalBuffer.FLAG3;
		if (fIsPointerDeref)
			firstBytes |= ITypeMarshalBuffer.FLAG4;

		buffer.putShort(firstBytes);
		buffer.marshalEvaluation(fFieldOwner, false);
		buffer.putCharArray(fName);
		buffer.marshalBinding(fNameOwner);
		if (fTemplateArgs != null) {
			buffer.putInt(fTemplateArgs.length);
			for (ICPPTemplateArgument arg : fTemplateArgs) {
				buffer.marshalTemplateArgument(arg);
			}
		}
		marshalTemplateDefinition(buffer);
	}

	public static ISerializableEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean addressOf= (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		final boolean qualified= (firstBytes & ITypeMarshalBuffer.FLAG2) != 0;
		final boolean isPointerDeref= (firstBytes & ITypeMarshalBuffer.FLAG4) != 0;
		ICPPEvaluation fieldOwner= (ICPPEvaluation) buffer.unmarshalEvaluation();
		char[] name= buffer.getCharArray();
		IBinding nameOwner= buffer.unmarshalBinding();
		ICPPTemplateArgument[] args= null;
		if ((firstBytes & ITypeMarshalBuffer.FLAG3) != 0) {
			int len= buffer.getInt();
			args = new ICPPTemplateArgument[len];
			for (int i = 0; i < args.length; i++) {
				args[i]= buffer.unmarshalTemplateArgument();
			}
		}
		IBinding templateDefinition= buffer.unmarshalBinding();
		return new EvalID(fieldOwner, nameOwner, name, addressOf, qualified, isPointerDeref, args, 
				templateDefinition);
	}

	public static ICPPEvaluation create(IASTIdExpression expr) {
		final IASTName name = expr.getName();
		IBinding binding = name.resolvePreBinding();
		boolean qualified = name instanceof ICPPASTQualifiedName;
		if (binding instanceof IProblemBinding || binding instanceof IType || binding instanceof ICPPConstructor)
			return EvalFixed.INCOMPLETE;
		if (binding instanceof CPPFunctionSet) {
			return new EvalFunctionSet((CPPFunctionSet) binding, qualified, isAddressOf(expr), null, expr);
		}
		if (binding instanceof ICPPUnknownBinding) {
			ICPPTemplateArgument[] templateArgs = null;
			final IASTName lastName = name.getLastName();
			if (lastName instanceof ICPPASTTemplateId) {
				try {
					templateArgs= CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) lastName);
				} catch (DOMException e) {
					return EvalFixed.INCOMPLETE;
				}
			}

			if (binding instanceof CPPDeferredFunction) {
				ICPPFunction[] candidates = ((CPPDeferredFunction) binding).getCandidates();
				if (candidates != null) {
					CPPFunctionSet functionSet = new CPPFunctionSet(candidates, templateArgs, null);
					return new EvalFunctionSet(functionSet, qualified, isAddressOf(expr), null, expr);
				} else {
					// Just store the name. ADL at the time of instantiation might come up with bindings.
					return new EvalFunctionSet(name.getSimpleID(), qualified, isAddressOf(expr), expr);
				}
			}

			IBinding owner = binding.getOwner();
			if (owner instanceof IProblemBinding)
				return EvalFixed.INCOMPLETE;

			ICPPEvaluation fieldOwner= null;
			IType fieldOwnerType= withinNonStaticMethod(expr);
			if (fieldOwnerType != null) {
				fieldOwner= new EvalFixed(fieldOwnerType, ValueCategory.LVALUE, Value.UNKNOWN);
			}

			return new EvalID(fieldOwner, owner, name.getSimpleID(), isAddressOf(expr),
					name instanceof ICPPASTQualifiedName, false, templateArgs, expr);
		}
		/**
		 * 9.3.1-3 Transformation to class member access within a non-static member function.
		 */
		if (binding instanceof ICPPMember && !(binding instanceof IType)
				&& !(binding instanceof ICPPConstructor) &&!((ICPPMember) binding).isStatic()) {
			IType fieldOwnerType= withinNonStaticMethod(expr);
			if (fieldOwnerType != null) {
				return new EvalMemberAccess(fieldOwnerType, LVALUE, binding, true, expr);
			}
		}

		if (binding instanceof IEnumerator) {
			IType type= ((IEnumerator) binding).getType();
			if (type instanceof ICPPEnumeration) {
				ICPPEnumeration enumType= (ICPPEnumeration) type;
				// [dcl.enum] 7.2-5
				if (isInsideEnum(expr, enumType)) {
					if (binding instanceof ICPPInternalEnumerator) {
						type = enumType.getFixedType();
						if (type == null) {
							type = ((ICPPInternalEnumerator) binding).getInternalType();
						}
					}
					return new EvalBinding(binding, type, expr);
				}
			}
			return new EvalBinding(binding, null, expr);
		}
		if (binding instanceof ICPPTemplateNonTypeParameter || binding instanceof IVariable
				|| binding instanceof IFunction) {
			return new EvalBinding(binding, null, expr);
		}
		return EvalFixed.INCOMPLETE;
	}

	/**
	 * Returns {@code true} if the given node is located inside the given enum.
	 */
	private static boolean isInsideEnum(IASTNode node, ICPPEnumeration enumBinding) {
		IASTEnumerator enumeratorNode = CPPVisitor.findAncestorWithType(node, IASTEnumerator.class);
		if (enumeratorNode == null)
			return false;
		IBinding enumerator = enumeratorNode.getName().getBinding();
		return enumerator != null && enumBinding == enumerator.getOwner();
	}

	private static IType withinNonStaticMethod(IASTExpression expr) {
		IASTNode parent= expr.getParent();
		while (parent != null && !(parent instanceof ICPPASTFunctionDefinition)) {
			parent= parent.getParent();
		}
		if (parent instanceof ICPPASTFunctionDefinition) {
			ICPPASTFunctionDefinition fdef= (ICPPASTFunctionDefinition) parent;
			final IBinding methodBinding = fdef.getDeclarator().getName().resolvePreBinding();
			if (methodBinding instanceof ICPPMethod && !((ICPPMethod) methodBinding).isStatic()) {
				IScope scope = CPPVisitor.getContainingScope(expr);
				return CPPVisitor.getImpliedObjectType(scope);
			}
		}
		return null;
	}

	private static boolean isAddressOf(IASTIdExpression expr) {
		IASTNode e = expr.getParent();
		while (e instanceof IASTUnaryExpression) {
			final IASTUnaryExpression unary = (IASTUnaryExpression) e;
			final int op= unary.getOperator();
			if (op == IASTUnaryExpression.op_bracketedPrimary) {
				e= unary.getOperand();
			} else {
				return op == IASTUnaryExpression.op_amper;
			}
		}
		return false;
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPTypeSpecialization within, int maxdepth, IASTNode point) {
		ICPPTemplateArgument[] templateArgs = fTemplateArgs;
		if (templateArgs != null) {
			templateArgs = instantiateArguments(templateArgs, tpMap, packOffset, within, point);
		}

		ICPPEvaluation fieldOwner = fFieldOwner;
		if (fieldOwner != null) {
			fieldOwner = fieldOwner.instantiate(tpMap, packOffset, within, maxdepth, point);
		}

		IBinding nameOwner = fNameOwner;
		if (nameOwner instanceof ICPPClassTemplate) {
			nameOwner = resolveUnknown(CPPTemplates.createDeferredInstance((ICPPClassTemplate) nameOwner),
					tpMap, packOffset, within, point);
		} else if (nameOwner instanceof IType) {
			IType type = CPPTemplates.instantiateType((IType) nameOwner, tpMap, packOffset, within, point);
			type = getNestedType(type, TDEF | REF | CVTYPE);
			if (!(type instanceof IBinding))
				return EvalFixed.INCOMPLETE;
			nameOwner = (IBinding) type;
		}

		if (fieldOwner instanceof IProblemBinding || nameOwner instanceof IProblemBinding)
			return EvalFixed.INCOMPLETE;

		if (templateArgs == fTemplateArgs && fieldOwner == fFieldOwner && nameOwner == fNameOwner)
			return this;

		if (nameOwner instanceof ICPPClassType) {
			ICPPEvaluation eval = resolveName((ICPPClassType) nameOwner, templateArgs, null, point);
			if (eval != null)
				return eval;
			if (!CPPTemplates.isDependentType((ICPPClassType) nameOwner))
				return EvalFixed.INCOMPLETE;
		}
		
		if (fieldOwner != null && !fieldOwner.isTypeDependent()) {
			IType fieldOwnerType = fieldOwner.getTypeOrFunctionSet(point);
			if (fIsPointerDeref) {
				if (fieldOwnerType instanceof IPointerType) {
					fieldOwnerType = ((IPointerType) fieldOwnerType).getType();
				} else {
					return EvalFixed.INCOMPLETE;
				}
			}
			IType fieldOwnerClassTypeCV = SemanticUtil.getNestedType(fieldOwnerType, TDEF | REF);
			IType fieldOwnerClassType = SemanticUtil.getNestedType(fieldOwnerClassTypeCV, CVTYPE);
			if (fieldOwnerClassType instanceof ICPPClassType) {
				ICPPEvaluation eval = resolveName((ICPPClassType) fieldOwnerClassType, templateArgs, fieldOwnerClassTypeCV, point);
				if (eval != null)
					return eval;
			}
		}

		return new EvalID(fieldOwner, nameOwner, fName, fAddressOf, fQualified, fIsPointerDeref, templateArgs, 
				getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(CPPFunctionParameterMap parameterMap,
			ConstexprEvaluationContext context) {
		if (fFieldOwner == null)
			return this;
		ICPPEvaluation fieldOwner = fFieldOwner.computeForFunctionCall(parameterMap, context.recordStep());
		if (fieldOwner == fFieldOwner)
			return this;
		return new EvalID(fieldOwner, fNameOwner, fName, fAddressOf, fQualified, fIsPointerDeref, 
				fTemplateArgs, getTemplateDefinition());
	}

	private ICPPEvaluation resolveName(ICPPClassType nameOwner, ICPPTemplateArgument[] templateArgs,
			IType impliedObjectType, IASTNode point) {
		LookupData data = new LookupData(fName, templateArgs, point);
		data.qualified = fQualified;
		try {
			CPPSemantics.lookup(data, nameOwner.getCompositeScope());
		} catch (DOMException e) {
		}
		IBinding[] bindings = data.getFoundBindings();
		if (bindings.length > 1 && bindings[0] instanceof ICPPFunction) {
			ICPPFunction[] functions = new ICPPFunction[bindings.length];
			System.arraycopy(bindings, 0, functions, 0, bindings.length);
			return new EvalFunctionSet(new CPPFunctionSet(functions, templateArgs, null), fQualified, fAddressOf, 
					impliedObjectType, getTemplateDefinition());
		}
		IBinding binding = bindings.length == 1 ? bindings[0] : null;
		if (binding instanceof IEnumerator) {
			return new EvalBinding(binding, null, getTemplateDefinition());
		} else if (binding instanceof ICPPMember) {
			return new EvalMemberAccess(nameOwner, ValueCategory.PRVALUE, binding, false, getTemplateDefinition());
		} else if (binding instanceof CPPFunctionSet) {
			return new EvalFunctionSet((CPPFunctionSet) binding, fQualified, fAddressOf, impliedObjectType, 
					getTemplateDefinition());
		}
		return null;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = fFieldOwner != null ? fFieldOwner.determinePackSize(tpMap) : CPPTemplates.PACK_SIZE_NOT_FOUND;
		if (fNameOwner instanceof ICPPUnknownBinding) {
			r = CPPTemplates.combinePackSize(r, CPPTemplates.determinePackSize((ICPPUnknownBinding) fNameOwner, tpMap));
		}
		if (fTemplateArgs != null) {
			for (ICPPTemplateArgument arg : fTemplateArgs) {
				r = CPPTemplates.combinePackSize(r, CPPTemplates.determinePackSize(arg, tpMap));
			}
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fFieldOwner != null && fFieldOwner.referencesTemplateParameter();
	}
}
