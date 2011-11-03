/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Calculator of in-memory size and of types.
 */
public class SizeofCalculator {
	/** Size and alignment pair */
	public static class SizeAndAlignment {
		public final long size;
		public final int alignment;

		public SizeAndAlignment(long size, int alignment) {
			this.size = size;
			this.alignment = alignment;
		}
	}

	private static final SizeAndAlignment SIZE_1 = new SizeAndAlignment(1, 1);

	private final SizeAndAlignment size_2;
	private final SizeAndAlignment size_4;
	private final SizeAndAlignment size_8;
	private final SizeAndAlignment sizeof_pointer;
	private final SizeAndAlignment sizeof_int;
	private final SizeAndAlignment sizeof_long;
	private final SizeAndAlignment sizeof_long_long;
	private final SizeAndAlignment sizeof_short;
	private final SizeAndAlignment sizeof_bool;
	private final SizeAndAlignment sizeof_wchar_t;
	private final SizeAndAlignment sizeof_float;
	private final SizeAndAlignment sizeof_complex_float;
	private final SizeAndAlignment sizeof_double;
	private final SizeAndAlignment sizeof_complex_double;
	private final SizeAndAlignment sizeof_long_double;
	private final SizeAndAlignment sizeof_complex_long_double;

	public SizeofCalculator(IASTTranslationUnit ast) {
		int maxAlignment = 32;
		Map<String, String> sizeofMacros = new HashMap<String, String>();
		for (IASTPreprocessorMacroDefinition macro : ast.getBuiltinMacroDefinitions()) {
			String name = macro.getName().toString();
			if ("__BIGGEST_ALIGNMENT__".equals(name)) { //$NON-NLS-1$
				try {
					maxAlignment = Integer.parseInt(macro.getExpansion());
				} catch (NumberFormatException e) {
					// Ignore.
				}
			} else {
				if (name.startsWith("__SIZEOF_")) { //$NON-NLS-1$
					sizeofMacros.put(name, macro.getExpansion());
				}
			}
		}
		size_2 = new SizeAndAlignment(2, Math.min(2, maxAlignment));
		size_4 = new SizeAndAlignment(4, Math.min(4, maxAlignment));
		size_8 = new SizeAndAlignment(8, Math.min(8, maxAlignment));
		sizeof_pointer = getSize(sizeofMacros, "__SIZEOF_POINTER__", maxAlignment); //$NON-NLS-1$
		sizeof_int = getSize(sizeofMacros, "__SIZEOF_INT__", maxAlignment); //$NON-NLS-1$
		sizeof_long = getSize(sizeofMacros, "__SIZEOF_LONG__", maxAlignment); //$NON-NLS-1$
		sizeof_long_long = getSize(sizeofMacros, "__SIZEOF_LONG_LONG__", maxAlignment); //$NON-NLS-1$
		sizeof_short = getSize(sizeofMacros, "__SIZEOF_SHORT__", maxAlignment); //$NON-NLS-1$
		sizeof_bool = getSize(sizeofMacros, "__SIZEOF_BOOL__", maxAlignment); //$NON-NLS-1$
		sizeof_wchar_t = getSize(sizeofMacros, "__SIZEOF_WCHAR_T__", maxAlignment); //$NON-NLS-1$
		sizeof_float = getSize(sizeofMacros, "__SIZEOF_FLOAT__", maxAlignment); //$NON-NLS-1$
		sizeof_complex_float = getDoubleSize(sizeof_float);
		sizeof_double = getSize(sizeofMacros, "__SIZEOF_DOUBLE__", maxAlignment); //$NON-NLS-1$
		sizeof_complex_double = getDoubleSize(sizeof_double);
		sizeof_long_double = getSize(sizeofMacros, "__SIZEOF_LONG_DOUBLE__", maxAlignment); //$NON-NLS-1$
		sizeof_complex_long_double = getDoubleSize(sizeof_long_double);
	}

	/**
	 * Calculates size and alignment for the given type.
	 * @param type
	 * @return size and alignment, or <code>null</code> if could not be calculated.
	 */
	public SizeAndAlignment sizeAndAlignment(IType type) {
		type = SemanticUtil.getNestedType(type, SemanticUtil.CVTYPE | SemanticUtil.TDEF);

		if (type instanceof IBasicType) {
			return sizeAndAlignment((IBasicType) type);
		}
		if (type instanceof IPointerType || type instanceof ICPPReferenceType) {
			if (type instanceof ICPPPointerToMemberType)
				return null;
			return sizeof_pointer;
		}
		if (type instanceof IEnumeration) {
			return sizeAndAlignment((IEnumeration) type);
		}
		if (type instanceof IArrayType) {
			return sizeAndAlignment((IArrayType) type);
		}
		if (type instanceof ICompositeType) {
			return sizeAndAlignment((ICompositeType) type);
		}
		return null;
	}

	private SizeAndAlignment sizeAndAlignment(IBasicType type) {
		Kind kind = type.getKind();
		switch (kind) {
		case eBoolean:
			return sizeof_bool;
		case eChar:
			return SIZE_1;
		case eInt:
			return type.isShort() ?	sizeof_short : type.isLong() ? sizeof_long :
					type.isLongLong() ? sizeof_long_long : sizeof_int;
		case eFloat: {
			return type.isComplex() ? sizeof_complex_float : sizeof_float;
		}
		case eDouble:
			return type.isComplex() ?
					(type.isLong() ? sizeof_long_double : sizeof_double) :
					(type.isLong() ? sizeof_complex_long_double : sizeof_complex_double);
		case eWChar:
			return sizeof_wchar_t;
		case eChar16:
			return size_2;
		case eChar32:
			return size_4;
		default:
			return null;
		}
	}

	private SizeAndAlignment sizeAndAlignment(IEnumeration type) {
		if (type instanceof ICPPEnumeration) {
			IType fixedType = ((ICPPEnumeration) type).getFixedType();
			if (fixedType != null) {
				return sizeAndAlignment(fixedType);
			}
		}
		long range = Math.max(Math.abs(type.getMinValue()) - 1, Math.abs(type.getMaxValue()));
		if (range >= (2 << 32))
			return size_8;
		if (type.getMinValue() < 0)
			range *= 2;
		if (range >= (2 << 32))
			return size_8;
		if (range >= (2 << 16))
			return size_4;
		if (range >= (2 << 8))
			return size_2;
		return SIZE_1;
	}

	private SizeAndAlignment sizeAndAlignment(IArrayType type) {
		IValue value = type.getSize();
		if (value == null)
			return null;
		Long numElements = value.numericalValue();
		if (numElements == null)
			return null;
		IType elementType = type.getType();
		SizeAndAlignment info = sizeAndAlignment(elementType);
		if (numElements.longValue() == 1)
			return info;
		if (info == null)
			return null;
		return new SizeAndAlignment(info.size * numElements.longValue(), info.alignment);
	}

	private SizeAndAlignment sizeAndAlignment(ICompositeType type) {
		/* TODO(sprigogin): May produce incorrect result for structures containing bit fields.
		 * Unfortunately widths of bit fields are not preserved in the AST. */
		long size = 0;
		int maxAlignment = 1;
		IField[] fields;
		if (type instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) type;
			for (ICPPBase base : classType.getBases()) {
				if (base.isVirtual())
					return null;  // Don't know how to calculate size when there are virtual bases.
				IBinding baseClass = base.getBaseClass();
				if (!(baseClass instanceof IType))
					return null;
				SizeAndAlignment info = sizeAndAlignment((IType) baseClass);
				if (info == null)
					return null;
				size += info.alignment - (size - 1) % info.alignment - 1 + info.size;
				if (maxAlignment < info.alignment)
					maxAlignment = info.alignment;
				for (ICPPMethod method : classType.getDeclaredMethods()) {
					if (method.isVirtual()) {
						// Don't know how to calculate size when there are virtual functions.
						return null;
					}
				}
			}
			fields = classType.getDeclaredFields();
		} else {
			fields = type.getFields();
		}

		boolean union = type.getKey() == ICompositeType.k_union;
		for (IField field : fields) {
			if (field.isStatic())
				continue;
			IType fieldType = field.getType();
			SizeAndAlignment info = sizeAndAlignment(fieldType);
			if (info == null)
				return null;
			if (union) {
				if (size < info.size)
					size = info.size;
			} else {
				size += info.alignment - (size - 1) % info.alignment - 1 + info.size;
			}
			if (maxAlignment < info.alignment)
				maxAlignment = info.alignment;
		}
		if (size > 0)
			size += maxAlignment - (size - 1) % maxAlignment - 1;
		return new SizeAndAlignment(size, maxAlignment);
	}

	private static SizeAndAlignment getSize(Map<String, String> macros, String name,
			int maxAlignment) {
		String value = macros.get(name);
		if (value == null)
			return null;
		try {
			int size = Integer.parseInt(value);
			return new SizeAndAlignment(size, Math.min(size, maxAlignment));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private SizeAndAlignment getDoubleSize(SizeAndAlignment sizeAndAlignment) {
		return sizeAndAlignment == null ?
				null : new SizeAndAlignment(sizeAndAlignment.size * 2, sizeAndAlignment.alignment);
	}
}
