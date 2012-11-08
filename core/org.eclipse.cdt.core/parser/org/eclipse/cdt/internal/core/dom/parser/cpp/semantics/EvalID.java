/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
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
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public class EvalID extends CPPEvaluation {
	private final ICPPEvaluation fFieldOwner;
	private final char[] fName;
	private final IBinding fNameOwner;
	private final boolean fAddressOf;
	private final boolean fQualified;
	private final ICPPTemplateArgument[] fTemplateArgs;

	public EvalID(ICPPEvaluation fieldOwner, IBinding nameOwner, char[] simpleID, boolean addressOf,
			boolean qualified, ICPPTemplateArgument[] templateArgs) {
		fFieldOwner= fieldOwner;
		fName= simpleID;
		fNameOwner= nameOwner;
		fAddressOf= addressOf;
		fQualified= qualified;
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
	public IType getTypeOrFunctionSet(IASTNode point) {
		return new TypeOfDependentExpression(this);
	}

	@Override
	public IValue getValue(IASTNode point) {
		// Name lookup is not needed here because it was already done in the "instantiate" method.
//		IBinding nameOwner = fNameOwner;
//		if (nameOwner == null && fFieldOwner != null)
//			nameOwner = (IBinding) fFieldOwner.getTypeOrFunctionSet(point);
//
//		if (nameOwner instanceof ICPPClassType) {
//			ICPPEvaluation eval = resolveName((ICPPClassType) nameOwner, fTemplateArgs, point);
//			if (eval != null)
//				return eval.getValue(point);
//		}
		return Value.create(this);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		int firstByte = ITypeMarshalBuffer.EVAL_ID;
		if (fAddressOf)
			firstByte |= ITypeMarshalBuffer.FLAG1;
		if (fQualified)
			firstByte |= ITypeMarshalBuffer.FLAG2;
		if (fTemplateArgs != null)
			firstByte |= ITypeMarshalBuffer.FLAG3;

		buffer.putByte((byte) firstByte);
		buffer.marshalEvaluation(fFieldOwner, false);
		buffer.putCharArray(fName);
		buffer.marshalBinding(fNameOwner);
		if (fTemplateArgs != null) {
			buffer.putShort((short) fTemplateArgs.length);
			for (ICPPTemplateArgument arg : fTemplateArgs) {
				buffer.marshalTemplateArgument(arg);
			}
		}
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean addressOf= (firstByte & ITypeMarshalBuffer.FLAG1) != 0;
		final boolean qualified= (firstByte & ITypeMarshalBuffer.FLAG2) != 0;
		ICPPEvaluation fieldOwner= (ICPPEvaluation) buffer.unmarshalEvaluation();
		char[] name= buffer.getCharArray();
		IBinding nameOwner= buffer.unmarshalBinding();
		ICPPTemplateArgument[] args= null;
		if ((firstByte & ITypeMarshalBuffer.FLAG3) != 0) {
			int len= buffer.getShort();
			args = new ICPPTemplateArgument[len];
			for (int i = 0; i < args.length; i++) {
				args[i]= buffer.unmarshalTemplateArgument();
			}
		}
		return new EvalID(fieldOwner, nameOwner, name, addressOf, qualified, args);
	}

	public static ICPPEvaluation create(IASTIdExpression expr) {
		final IASTName name = expr.getName();
		IBinding binding = name.resolvePreBinding();
		if (binding instanceof IProblemBinding || binding instanceof IType || binding instanceof ICPPConstructor)
			return EvalFixed.INCOMPLETE;
		if (binding instanceof CPPFunctionSet) {
			return new EvalFunctionSet((CPPFunctionSet) binding, isAddressOf(expr));
		}
		if (binding instanceof ICPPUnknownBinding) {
			IBinding owner = binding.getOwner();
			if (owner instanceof IProblemBinding)
				return EvalFixed.INCOMPLETE;

			ICPPEvaluation fieldOwner= null;
			IType fieldOwnerType= withinNonStaticMethod(expr);
			if (fieldOwnerType != null) {
				fieldOwner= new EvalFixed(fieldOwnerType, ValueCategory.LVALUE, Value.UNKNOWN);
			}
			ICPPTemplateArgument[] templateArgs = null;
			final IASTName lastName = name.getLastName();
			if (lastName instanceof ICPPASTTemplateId) {
				templateArgs= CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) lastName);
			}
			return new EvalID(fieldOwner, owner, name.getSimpleID(), isAddressOf(expr),
					name instanceof ICPPASTQualifiedName, templateArgs);
		}
		/**
		 * 9.3.1-3 Transformation to class member access within a non-static member function.
		 */
		if (binding instanceof ICPPMember && !(binding instanceof IType)
				&& !(binding instanceof ICPPConstructor) &&!((ICPPMember) binding).isStatic()) {
			IType fieldOwnerType= withinNonStaticMethod(expr);
			if (fieldOwnerType != null) {
				return new EvalMemberAccess(fieldOwnerType, LVALUE, binding, true);
			}
		}

		if (binding instanceof IEnumerator) {
			IType type= ((IEnumerator) binding).getType();
			if (type instanceof ICPPEnumeration) {
				ICPPEnumeration enumType= (ICPPEnumeration) type;
				if (enumType.asScope() == CPPVisitor.getContainingScope(expr)) {
					// C++0x: 7.2-5
					type= enumType.getFixedType();
					if (type == null) {
						// This is a simplification, the actual type is determined
						// - in an implementation dependent manner - by the value
						// of the enumerator.
						type= CPPSemantics.INT_TYPE;
					}
					return new EvalBinding(binding, type);
				}
			}
			return new EvalBinding(binding, null);
		}
		if (binding instanceof ICPPTemplateNonTypeParameter || binding instanceof IVariable
				|| binding instanceof IFunction) {
			return new EvalBinding(binding, null);
		}
		return EvalFixed.INCOMPLETE;
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
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
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
			if (type instanceof IBinding)
				nameOwner = (IBinding) type;
		}

		if (fieldOwner instanceof IProblemBinding || nameOwner instanceof IProblemBinding)
			return this;

		if (templateArgs == fTemplateArgs && fieldOwner == fFieldOwner && nameOwner == fNameOwner)
			return this;

		if (nameOwner instanceof ICPPClassType) {
			ICPPEvaluation eval = resolveName((ICPPClassType) nameOwner, templateArgs, point);
			if (eval != null)
				return eval;
		}

		return new EvalID(fieldOwner, nameOwner, fName, fAddressOf, fQualified, templateArgs);
	}

	private ICPPEvaluation resolveName(ICPPClassType nameOwner, ICPPTemplateArgument[] templateArgs,
			IASTNode point) {
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
			return new EvalFunctionSet(new CPPFunctionSet(functions, templateArgs, null), fAddressOf);
		}
		IBinding binding = bindings.length == 1 ? bindings[0] : null;
		if (binding instanceof IEnumerator) {
			return new EvalBinding(binding, null);
		} else if (binding instanceof ICPPMember) {
			return new EvalMemberAccess(nameOwner, ValueCategory.PRVALUE, binding, false);
		} else if (binding instanceof CPPFunctionSet) {
			return new EvalFunctionSet((CPPFunctionSet) binding, fAddressOf);
		}
		return null;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = fFieldOwner.determinePackSize(tpMap);
		for (ICPPTemplateArgument arg : fTemplateArgs) {
			r = CPPTemplates.combinePackSize(r, CPPTemplates.determinePackSize(arg, tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fFieldOwner.referencesTemplateParameter();
	}
}
