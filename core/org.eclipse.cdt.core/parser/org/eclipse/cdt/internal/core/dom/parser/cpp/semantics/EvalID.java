/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public class EvalID implements ICPPEvaluation {
	private final ICPPEvaluation fFieldOwner;
	private final char[] fName;
	private final IBinding fNameOwner;
	private final boolean fAddressOf;
	private final boolean fQualified;
	private final ICPPTemplateArgument[] fTemplateArgs;

	public EvalID(ICPPEvaluation fieldOwner, IBinding nameOwner, char[] simpleID, boolean addressOf, boolean qualified, ICPPTemplateArgument[] templateArgs) {
		fFieldOwner= fieldOwner;
		fName= simpleID;
		fNameOwner= nameOwner;
		fAddressOf= addressOf;
		fQualified= qualified;
		fTemplateArgs= templateArgs;
	}

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
		return Value.create(this, point);
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
			// mstodo marshall arguments
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
			// mstodo marshall arguments
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
}
