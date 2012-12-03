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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.IInternalVariable;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public class EvalBinding extends CPPEvaluation {
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
		IType t= null;
		if (fFixedType) {
			t = fType;
		} else if (fBinding instanceof IEnumerator) {
			t= ((IEnumerator) fBinding).getType();
		} else if (fBinding instanceof ICPPTemplateNonTypeParameter) {
			t= ((ICPPTemplateNonTypeParameter) fBinding).getType();
		} else if (fBinding instanceof IVariable) {
			t = ((IVariable) fBinding).getType();
		} else if (fBinding instanceof ICPPUnknownBinding) {
			return true;
		} else if (fBinding instanceof IFunction) {
			t= ((IFunction) fBinding).getType();
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
		if (fBinding instanceof ICPPUnknownBinding) {
			return true;
		}
		if (fBinding instanceof IFunction) {
			return false;
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
			return SemanticUtil.mapToAST(type, point);
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getValue(IASTNode point) {
		if (isValueDependent())
			return Value.create(this);

		IValue value= null;
		if (fBinding instanceof IInternalVariable) {
			value= ((IInternalVariable) fBinding).getInitialValue(Value.MAX_RECURSION_DEPTH);
		} else if (fBinding instanceof IVariable) {
			value= ((IVariable) fBinding).getInitialValue();
		} else if (fBinding instanceof IEnumerator) {
			value= ((IEnumerator) fBinding).getValue();
		}
		if (value == null)
			value = Value.UNKNOWN;

		return value;
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

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		IBinding binding = fBinding;
		if (fBinding instanceof IEnumerator) {
			IEnumerator enumerator = (IEnumerator) binding;
			IType originalType = enumerator.getType();
			IType type = CPPTemplates.instantiateType(originalType, tpMap, packOffset, within, point);
			IValue originalValue = enumerator.getValue();
			IValue value = CPPTemplates.instantiateValue(originalValue, tpMap, packOffset, within, maxdepth, point);
			// TODO(sprigogin): Not sure if following condition is correct.
			if (type != originalType || value != originalValue)
				return new EvalFixed(type, ValueCategory.PRVALUE, value);
		} else if (fBinding instanceof ICPPTemplateNonTypeParameter) {
			ICPPTemplateArgument argument = tpMap.getArgument((ICPPTemplateNonTypeParameter) fBinding);
			if (argument != null && argument.isNonTypeValue()) {
				return argument.getNonTypeEvaluation();
			}
			// TODO(sprigogin): Do we need something similar for pack expansion?
		} else if (fBinding instanceof ICPPUnknownBinding) {
			binding = resolveUnknown((ICPPUnknownBinding) fBinding, tpMap, packOffset, within, point);
		} else if (fBinding instanceof ICPPMethod) {
			IBinding owner = fBinding.getOwner();
			if (owner instanceof ICPPClassTemplate) {
				owner = resolveUnknown(CPPTemplates.createDeferredInstance((ICPPClassTemplate) owner),
						tpMap, packOffset, within, point);
			}
			if (owner instanceof ICPPClassSpecialization) {
				binding = CPPTemplates.createSpecialization((ICPPClassSpecialization) owner,
						fBinding, point);
			}
		}
		if (binding == fBinding)
			return this;
		return new EvalBinding(binding, getFixedType());
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		if (fBinding instanceof IEnumerator) {
			return CPPTemplates.determinePackSize(((IEnumerator) fBinding).getValue(), tpMap);
		}
		if (fBinding instanceof ICPPTemplateNonTypeParameter) {
			return CPPTemplates.determinePackSize((ICPPTemplateNonTypeParameter) fBinding, tpMap);
		}
		if (fBinding instanceof ICPPUnknownBinding) {
			return CPPTemplates.determinePackSize((ICPPUnknownBinding) fBinding, tpMap);
		}
		
		IBinding binding = fBinding;
		if (fBinding instanceof ICPPSpecialization) {
			binding = ((ICPPSpecialization) fBinding).getSpecializedBinding();
		}

		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		if (binding instanceof ICPPTemplateDefinition) {
			ICPPTemplateParameter[] parameters = ((ICPPTemplateDefinition) binding).getTemplateParameters();
			for (ICPPTemplateParameter param : parameters) {
				r = CPPTemplates.combinePackSize(r, CPPTemplates.determinePackSize(param, tpMap));
			}
		}

		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return fBinding instanceof ICPPTemplateParameter;
	}
}
