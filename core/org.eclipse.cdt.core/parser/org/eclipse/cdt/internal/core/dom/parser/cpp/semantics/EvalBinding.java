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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public class EvalBinding implements ICPPEvaluation {
	private final IBinding fBinding;
	private final boolean fFixedType;

	private IType fType;
	private boolean fCheckedIsValueDependent;
	private boolean fIsValueDependent;
	private boolean fIsTypeDependent;
	private boolean fCheckedIsTypeDependent;


	public EvalBinding(IBinding binding, IType type) {
		fBinding= binding;
		fType= type;
		fFixedType= type != null;
	}

	public IBinding getBinding() {
		return fBinding;
	}

	public IType getFixedType() {
		return fFixedType ? fType : null;
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
		if (!fCheckedIsTypeDependent) {
			fCheckedIsTypeDependent= true;
			fIsTypeDependent= computeIsTypeDependent();
		}
		return fIsTypeDependent;
 	}

	private boolean computeIsTypeDependent() {
		if (fBinding instanceof ICPPUnknownBinding)
			return true;

		IType t= null;
		if (fBinding instanceof IEnumerator) {
			t= ((IEnumerator) fBinding).getType();
		} else if (fBinding instanceof ICPPTemplateNonTypeParameter) {
			t= ((ICPPTemplateNonTypeParameter) fBinding).getType();
		} else if (fBinding instanceof IVariable) {
			t = ((IVariable) fBinding).getType();
		} else if (fBinding instanceof IFunction) {
			t= ((IFunction) fBinding).getType();
		} else if (fBinding instanceof ICPPUnknownBinding) {
			return true;
		} else {
			return false;
		}
		return CPPTemplates.isDependentType(t);
	}

 	@Override
 	public boolean isValueDependent() {
		if (!fCheckedIsValueDependent) {
			fCheckedIsValueDependent= true;
			fIsValueDependent= computeIsValueDependent();
		}
		return fIsValueDependent;
 	}

 	private boolean computeIsValueDependent() {
		if (fBinding instanceof IEnumerator) {
			return Value.isDependentValue(((IEnumerator) fBinding).getValue());
		}
		if (fBinding instanceof ICPPTemplateNonTypeParameter) {
			return true;
		}
		if (fBinding instanceof IVariable) {
			return Value.isDependentValue(((IVariable) fBinding).getInitialValue());
		}
		if (fBinding instanceof IFunction) {
			return false;
		}
		if (fBinding instanceof ICPPUnknownBinding) {
			return true;
		}
		return false;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		if (fType == null) {
			fType= computeType(point);
		}
		return fType;
	}

	private IType computeType(IASTNode point) {
		if (fBinding instanceof IEnumerator) {
			return ((IEnumerator) fBinding).getType();
		}
		if (fBinding instanceof ICPPTemplateNonTypeParameter) {
			IType type= ((ICPPTemplateNonTypeParameter) fBinding).getType();
			if (CPPTemplates.isDependentType(type))
				return new TypeOfDependentExpression(this);
			return prvalueType(type);
		}
		if (fBinding instanceof IVariable) {
			final IType type = ((IVariable) fBinding).getType();
			if (CPPTemplates.isDependentType(type))
				return new TypeOfDependentExpression(this);
			return SemanticUtil.mapToAST(glvalueType(type), point);
		}
		if (fBinding instanceof IFunction) {
			final IFunctionType type = ((IFunction) fBinding).getType();
			if (CPPTemplates.isDependentType(type))
				return new TypeOfDependentExpression(this);
			return  SemanticUtil.mapToAST(type, point);
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getValue(IASTNode point) {
		return Value.create(this, point);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
        if (fBinding instanceof ICPPTemplateNonTypeParameter)
        	return ValueCategory.PRVALUE;

		if (fBinding instanceof IVariable || fBinding instanceof IFunction) {
			return ValueCategory.LVALUE;
		}
		return ValueCategory.PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.EVAL_BINDING);
		buffer.marshalBinding(fBinding);
		buffer.marshalType(fFixedType ? fType : null);
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		IBinding binding= buffer.unmarshalBinding();
		IType type= buffer.unmarshalType();
		return new EvalBinding(binding, type);
	}
}
