/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ActivationRecord;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalInitList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public final class CompositeValue implements IValue {
	public static boolean sDEBUG; // Initialized in the TranslationUnit.

	private final ICPPEvaluation evaluation;
	private final ICPPEvaluation[] values;

	public CompositeValue(ICPPEvaluation evaluation, ICPPEvaluation[] values) {
		this.evaluation = evaluation;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null)
				values[i] = EvalFixed.INCOMPLETE;
		}
		this.values = values;
	}

	@Override
	public Number numberValue() {
		return null;
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		return evaluation;
	}

	@Override
	public char[] getSignature() {
		if (evaluation != null) {
			return evaluation.getSignature();
		}
		return new char[] {};
	}

	@Override
	public int numberOfSubValues() {
		return values.length;
	}

	@Override
	public ICPPEvaluation getSubValue(final int index) {
		return rangeIsValid(index) ? values[index] : EvalFixed.INCOMPLETE;
	}

	private boolean rangeIsValid(int index) {
		return 0 <= index && index < values.length;
	}

	public static IValue create(EvalInitList initList) {
		ICPPEvaluation[] clauses = initList.getClauses();
		ICPPEvaluation[] values = new ICPPEvaluation[clauses.length];
		for (int i = 0; i < clauses.length; i++) {
			ICPPEvaluation eval = clauses[i];
			values[i] = new EvalFixed(eval.getType(), eval.getValueCategory(), eval.getValue());
		}
		return new CompositeValue(initList, values);
	}

	/**
	 * Creates a value representing an instance of the given array type initialized with
	 * the elements of the given initializer list.
	 */
	public static IValue create(EvalInitList initList, IArrayType type) {
		Number arraySize = type.getSize().numberValue();
		if (arraySize == null) {
			// Array size is dependent. TODO: Handle this?
			return IntegralValue.UNKNOWN;
		}
		// More initializers than array members
		if (arraySize.intValue() < initList.getClauses().length) {
			return IntegralValue.ERROR;
		}
		IType elementType = type.getType();
		ICPPEvaluation[] values = new ICPPEvaluation[arraySize.intValue()];
		for (int i = 0; i < initList.getClauses().length; i++) {
			ICPPEvaluation eval = initList.getClauses()[i];
			IValue value = getValue(elementType, eval);
			values[i] = new EvalFixed(elementType, eval.getValueCategory(), value);
		}
		return new CompositeValue(initList, values);
	}

	/**
	 * Gets the value of an evaluation, interpreted as a value of the given type.
	 */
	private static IValue getValue(IType type, ICPPEvaluation eval) {
		IValue value;
		if (type instanceof IArrayType && eval instanceof EvalInitList) {
			value = CompositeValue.create((EvalInitList) eval, (IArrayType) type);
		} else if (type instanceof ICompositeType && eval instanceof EvalInitList) {
			value = CompositeValue.create((EvalInitList) eval, (ICompositeType) type);
		} else if (eval instanceof EvalInitList) {
			value = IntegralValue.UNKNOWN;
		} else {
			value = eval.getValue();
		}
		return value;
	}

	/**
	 * Creates a value representing an instance of the given composite type initialized with
	 * the elements of the given initializer list.
	 */
	public static IValue create(EvalInitList initList, ICompositeType type) {
		IField[] fields;
		if (type instanceof ICPPClassType) {
			fields = ClassTypeHelper.getFields((ICPPClassType) type);
		} else {
			fields = type.getFields();
		}
		ICPPEvaluation[] values = new ICPPEvaluation[fields.length];
		ICPPEvaluation[] clauses = initList.getClauses();
		for (int i = 0; i < fields.length; i++) {
			if (i == clauses.length)
				break;
			IField field = fields[i];
			ICPPEvaluation eval = clauses[i];
			IType fieldType = field.getType();
			IValue value = getValue(fieldType, eval);
			values[i] = new EvalFixed(fieldType, eval.getValueCategory(), value);
		}
		return new CompositeValue(initList, values);
	}

	// The set of class types for which composite value creation is in progress on each thread.
	// Used to guard against infinite recursion due to a class (illegally) aggregating itself.
	private static final ThreadLocal<Set<ICPPClassType>> fCreateInProgress = new ThreadLocal<Set<ICPPClassType>>() {
		@Override
		protected Set<ICPPClassType> initialValue() {
			return new TreeSet<>((type1, type2) -> {
				if (type1.isSameType(type2))
					return 0;
				return ASTTypeUtil.getType(type1, true).compareTo(ASTTypeUtil.getType(type2, true));
			});
		}
	};

	/**
	 * Creates a value representing an instance of a class type, with the values of the fields
	 * determined by the default member initializers only. Constructors are not considered
	 * when determining the values of the fields.
	 */
	public static CompositeValue create(ICPPClassType classType) {
		return create(classType, 0);
	}

	/**
	 * Creates a value representing an instance of a class type, with the values of the fields
	 * determined by the default member initializers only. Constructors are not considered
	 * when determining the values of the fields.
	 */
	public static CompositeValue create(ICPPClassType classType, int nestingLevel) {
		Set<ICPPClassType> recursionProtectionSet = fCreateInProgress.get();
		if (!recursionProtectionSet.add(classType)) {
			return new CompositeValue(null, ICPPEvaluation.EMPTY_ARRAY);
		}
		try {
			if (sDEBUG && nestingLevel > 0) {
				System.out
						.println("CompositeValue.create(" + ASTTypeUtil.getType(classType) + ", " + nestingLevel + ")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				System.out.flush();
			}
			ActivationRecord record = new ActivationRecord();
			ICPPEvaluation[] values = new ICPPEvaluation[ClassTypeHelper.getFields(classType).length];

			// Recursively create all the base class member variables.
			ICPPBase[] bases = classType.getBases();
			for (ICPPBase base : bases) {
				IBinding baseClass = base.getBaseClass();
				if (baseClass instanceof ICPPClassType) {
					ICPPClassType baseClassType = (ICPPClassType) baseClass;
					ICPPField[] baseFields = baseClassType.getDeclaredFields();
					IValue compValue = CompositeValue.create(baseClassType, nestingLevel + 1);
					for (ICPPField baseField : baseFields) {
						int fieldPos = CPPASTFieldReference.getFieldPosition(baseField);
						if (fieldPos == -1) {
							continue;
						}
						record.update(baseField, compValue.getSubValue(fieldPos));
						// TODO(nathanridge): This won't work with multiple inheritance, since 'fieldPos'
						// is a field position in the base class' hierarchy, while values[] expects
						// as index a field position in classType's hierarchy.
						values[fieldPos] = compValue.getSubValue(fieldPos);
					}
				}
			}

			ICPPField[] fields = classType.getDeclaredFields();
			for (ICPPField field : fields) {
				if (field.isStatic())
					continue;
				final ICPPEvaluation value = EvalUtil.getVariableValue(field, record);
				int fieldPos = CPPASTFieldReference.getFieldPosition(field);
				if (fieldPos == -1) {
					continue;
				}
				record.update(field, value);
				values[fieldPos] = value;
			}
			return new CompositeValue(null, values);
		} finally {
			recursionProtectionSet.remove(classType);
		}
	}

	@Override
	public ICPPEvaluation[] getAllSubValues() {
		return values;
	}

	@Override
	public void setSubValue(int position, ICPPEvaluation newValue) {
		if (position >= 0 && position < values.length) {
			values[position] = newValue == null ? EvalFixed.INCOMPLETE : newValue;
		} else {
			CCorePlugin.log(IStatus.WARNING, "Out-of-bounds access to composite value: " + position + //$NON-NLS-1$
					" (length is " + values.length + ")"); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		for (int i = 0; i < values.length; i++) {
			if (i != 0) {
				builder.append(',').append(' ');
			}
			builder.append(values[i].toString());
		}
		builder.append(']');
		return builder.toString();
	}

	@Override
	public IValue clone() {
		ICPPEvaluation[] newValues = new ICPPEvaluation[values.length];
		for (int i = 0; i < newValues.length; i++) {
			ICPPEvaluation eval = values[i];
			if (eval == EvalFixed.INCOMPLETE) {
				newValues[i] = eval;
			} else {
				IValue newValue = eval.getValue().clone();
				newValues[i] = new EvalFixed(eval.getType(), eval.getValueCategory(), newValue);
			}
		}
		return new CompositeValue(evaluation, newValues);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		buf.putShort(ITypeMarshalBuffer.COMPOSITE_VALUE);
		buf.marshalEvaluation(evaluation, true);
		buf.putInt(values.length);
		for (ICPPEvaluation value : values) {
			buf.marshalEvaluation(value, true);
		}
	}

	public static IValue unmarshal(short firstBytes, ITypeMarshalBuffer buf) throws CoreException {
		ICPPEvaluation evaluation = buf.unmarshalEvaluation();
		int len = buf.getInt();
		ICPPEvaluation values[] = new ICPPEvaluation[len];
		for (int i = 0; i < len; i++) {
			values[i] = buf.unmarshalEvaluation();
		}
		return new CompositeValue(evaluation, values);
	}

	@Override
	public boolean isEquivalentTo(IValue other) {
		if (!(other instanceof CompositeValue)) {
			return false;
		}
		CompositeValue o = (CompositeValue) other;
		if (!((evaluation == null && o.evaluation == null) || (evaluation.isEquivalentTo(o.evaluation)))) {
			return false;
		}
		if (values.length != o.values.length) {
			return false;
		}
		for (int i = 0; i < values.length; i++) {
			if (!values[i].isEquivalentTo(o.values[i])) {
				return false;
			}
		}
		return true;
	}
}
