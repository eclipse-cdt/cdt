/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ActivationRecord;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalInitList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.core.runtime.CoreException;

public class CompositeValue implements IValue {
	private final ICPPEvaluation[] values;
	private ICPPEvaluation evaluation;

	public CompositeValue(ICPPEvaluation evaluation, ICPPEvaluation[] values) {
		this.evaluation = evaluation;
		this.values = values;
	}

	@Override
	public Number numericalValue() {
		return null;
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		return evaluation;
	}

	@Override
	public char[] getSignature() {
		if(evaluation != null) {
			return evaluation.getSignature();
		}
		return new char[]{};
	}

	@Deprecated
	@Override
	public char[] getInternalExpression() {
		return CharArrayUtils.EMPTY_CHAR_ARRAY;
	}

	@Deprecated
	@Override
	public IBinding[] getUnknownBindings() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public boolean isCompound() {
		return true;
	}

	@Override
	public int numberOfValues() {
		return values.length;
	}

	@Override
	public ICPPEvaluation get(final int index) {
		return rangeIsValid(index) ? values[index] : EvalFixed.INCOMPLETE;
	}

	private boolean rangeIsValid(final int index) {
		return numberOfValues() > index && index >= 0;
	}

	public static IValue create(EvalInitList initList) {
		ICPPEvaluation[] values = new ICPPEvaluation[initList.getClauses().length];
		for (int i = 0; i < initList.getClauses().length; i++) {
			ICPPEvaluation eval = initList.getClauses()[i];
			values[i] = new EvalFixed(eval.getType(null), eval.getValueCategory(null),	eval.getValue(null));
		}
		return new CompositeValue(initList, values);
	}

	public static IValue create(EvalInitList initList, IArrayType type) {
		int arraySize = type.getSize().numericalValue().intValue();
		IType arrayType = type.getType();
		ICPPEvaluation[] values = new ICPPEvaluation[arraySize];
		for (int i = 0; i < initList.getClauses().length; i++) {
			ICPPEvaluation eval = initList.getClauses()[i];
			IValue value = getValue(arrayType, eval);
			values[i] = new EvalFixed(arrayType, eval.getValueCategory(null), value);
		}
		return new CompositeValue(initList, values);
	}

	private static IValue getValue(IType arrayType, ICPPEvaluation eval) {
		IValue value;
		if (arrayType instanceof IArrayType && eval instanceof EvalInitList) {
			value = CompositeValue.create((EvalInitList) eval, (IArrayType) arrayType);
		} else if (arrayType instanceof ICompositeType && eval instanceof EvalInitList) {
			value = CompositeValue.create((EvalInitList) eval, (ICompositeType) arrayType);
		} else if (eval instanceof EvalInitList) {
			value = IntegralValue.UNKNOWN;
		} else {
			value = eval.getValue(null);
		}
		return value;
	}

	public static IValue create(EvalInitList initList, ICompositeType type) {
		IField[] fields = type.getFields();
		ICPPEvaluation[] values = new ICPPEvaluation[fields.length];
		for (int i = 0; i < fields.length; i++) {
			IField field = fields[i];
			ICPPEvaluation eval = initList.getClauses()[i];
			IType fieldType = field.getType();
			IValue value = getValue(fieldType, eval);
			values[i] = new EvalFixed(fieldType, eval.getValueCategory(null), value);
		}
		return new CompositeValue(initList, values);
	}

	public static CompositeValue createForConstructor(ICPPClassType classType) {
		ActivationRecord record = new ActivationRecord();
		ICPPEvaluation[] values = new ICPPEvaluation[ClassTypeHelper.getFields(classType, null).length];
		
		// recursively create all the base class member variables
		ICPPBase[] bases = ClassTypeHelper.getBases(classType, null);
		for(ICPPBase base : bases) {
			IBinding baseClass = base.getBaseClass();
			if(baseClass instanceof ICPPClassType) {
				ICPPClassType baseClassType = (ICPPClassType) baseClass;
				ICPPField[] baseFields = ClassTypeHelper.getDeclaredFields(baseClassType, null);
				IValue compValue = CompositeValue.create(baseClassType);
				for(ICPPField baseField : baseFields) {
					int fieldPos = CPPASTFieldReference.getFieldPosition(baseField);
					record.update(baseField, compValue.get(fieldPos));
					values[fieldPos] = compValue.get(fieldPos);
				}
			}
		}
		return new CompositeValue(null, values);
	}
	
	// The set of class types for which composite value creation is in progress on each thread.
	// Used to guard against infinite recursion due to a class (invalidly) aggregating itself. 
	private static final ThreadLocal<Set<ICPPClassType>> fCreateInProgress = 
			new ThreadLocal<Set<ICPPClassType>>() {
		@Override
		protected Set<ICPPClassType> initialValue() {
			return new HashSet<>();
		}
	};
	
	public static CompositeValue create(ICPPClassType classType) {
		Set<ICPPClassType> recursionProtectionSet = fCreateInProgress.get();
		if (!recursionProtectionSet.add(classType)) {
			return new CompositeValue(null, ICPPEvaluation.EMPTY_ARRAY);
		}
		try {
			ActivationRecord record = new ActivationRecord();
			ICPPEvaluation[] values = new ICPPEvaluation[ClassTypeHelper.getFields(classType, null).length];
			
			// recursively create all the base class member variables
			ICPPBase[] bases = ClassTypeHelper.getBases(classType, null);
			for(ICPPBase base : bases) {
				IBinding baseClass = base.getBaseClass();
				if(baseClass instanceof ICPPClassType) {
					ICPPClassType baseClassType = (ICPPClassType) baseClass;
					ICPPField[] baseFields = ClassTypeHelper.getDeclaredFields(baseClassType, null);
					IValue compValue = CompositeValue.create(baseClassType);
					for(ICPPField baseField : baseFields) {
						int fieldPos = CPPASTFieldReference.getFieldPosition(baseField);
						record.update(baseField, compValue.get(fieldPos));
						values[fieldPos] = compValue.get(fieldPos);
					}
				}
			}
			
			ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, null);
			for (ICPPField field : fields) {
				InstantiationContext instantiationContext = null;
				if(classType instanceof ICPPClassSpecialization) {
					final ICPPClassSpecialization classSpecType = (ICPPClassSpecialization)classType;
					instantiationContext = new InstantiationContext(classSpecType.getTemplateParameterMap(), classSpecType, null);
				}
				
				final ICPPEvaluation value = EvalUtil.getVariableValue(field, record, instantiationContext);
				int fieldPos = CPPASTFieldReference.getFieldPosition(field);
				values[fieldPos] = value;
			}
			return new CompositeValue(null, values);
		} finally {
			recursionProtectionSet.remove(classType);
		}
	}

	static IASTName getDefinition(IBinding binding) {
		if(binding instanceof ICPPSpecialization) {
			ICPPSpecialization specialization = (ICPPSpecialization)binding;
			return getDefinition(specialization.getSpecializedBinding());
		} else if(binding instanceof ICPPInternalBinding) {
			ICPPInternalBinding internalBinding = (ICPPInternalBinding)binding;
			return (IASTName)internalBinding.getDefinition();
		}
		return null;
	}

	@Override
	public ICPPEvaluation[] getAll() {
		return values;
	}

	@Override
	public void set(int position, ICPPEvaluation newValue) {
		values[position] = newValue;
	}

	@Override
	public boolean isEqual(IValue other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CompositeValue)) {
			return false;
		}
		CompositeValue otherComposite = (CompositeValue) other;
		if (otherComposite.values.length != this.values.length) {
			return false;
		}
		for (int i = 0; i < values.length; i++) {
			if (values[i].getValue(null).isEqual(otherComposite.values[i].getValue(null))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");								//$NON-NLS-1$
		for(int i = 0; i < values.length; i++) {
			if(values[i] != null) {
				builder.append(values[i].toString());
			} else {
				builder.append("<null>");					//$NON-NLS-1$
			}
			if(i != values.length-1) {
				builder.append(", ");						//$NON-NLS-1$
			}
		}
		builder.append("]");								//$NON-NLS-1$
		return builder.toString();
	}
	
	@Override
	public IValue copy() {
		ICPPEvaluation[] newValues = new ICPPEvaluation[values.length];
		for(int i = 0; i < newValues.length; i++) {
			ICPPEvaluation eval = values[i];
			IValue newValue = eval.getValue(null).copy();
			newValues[i] = new EvalFixed(eval.getType(null), eval.getValueCategory(null), newValue);
		}
		return new CompositeValue(evaluation, newValues);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		buf.putShort(ITypeMarshalBuffer.COMPOSITE_VALUE);
		buf.marshalEvaluation(evaluation, true);
		buf.putInt(values.length);
		for(ICPPEvaluation value : values) {
			buf.marshalEvaluation(value, true);
		}
	}
	
	public static IValue unmarshal(short firstBytes, ITypeMarshalBuffer buf) throws CoreException {
		ICPPEvaluation evaluation = (ICPPEvaluation)buf.unmarshalEvaluation();
		int len = buf.getInt();
		ICPPEvaluation values[] = new ICPPEvaluation[len];
		for(int i = 0; i < len; i++) {
			values[i] = (ICPPEvaluation)buf.unmarshalEvaluation();
		}
		return new CompositeValue(evaluation, values);
	}
}
