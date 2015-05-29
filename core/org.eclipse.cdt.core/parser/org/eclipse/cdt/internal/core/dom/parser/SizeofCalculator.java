/*******************************************************************************
 * Copyright (c) 2011, 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeTraits;

/**
 * Calculator of in-memory size and alignment of types.
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

	private static final SizeofCalculator defaultInstance = new SizeofCalculator();

	private static final SizeAndAlignment SIZE_1 = new SizeAndAlignment(1, 1);

	public final SizeAndAlignment size_2;
	public final SizeAndAlignment size_4;
	public final SizeAndAlignment size_8;
	public final SizeAndAlignment size_16;
	public final SizeAndAlignment sizeof_pointer;
	public final SizeAndAlignment sizeof_int;
	public final SizeAndAlignment sizeof_long;
	public final SizeAndAlignment sizeof_long_long;
	public final SizeAndAlignment sizeof_int128;
	public final SizeAndAlignment sizeof_short;
	public final SizeAndAlignment sizeof_bool;
	public final SizeAndAlignment sizeof_wchar_t;
	public final SizeAndAlignment sizeof_float;
	public final SizeAndAlignment sizeof_complex_float;
	public final SizeAndAlignment sizeof_double;
	public final SizeAndAlignment sizeof_complex_double;
	public final SizeAndAlignment sizeof_long_double;
	public final SizeAndAlignment sizeof_complex_long_double;
	public final SizeAndAlignment sizeof_float128;
	public final SizeAndAlignment sizeof_complex_float128;
	public final SizeAndAlignment sizeof_decimal32;
	public final SizeAndAlignment sizeof_decimal64;
	public final SizeAndAlignment sizeof_decimal128;

	private final IASTTranslationUnit ast;

	/**
	 * Calculates size and alignment for the given type.
	 *
	 * @param type the type to get size and alignment for.
	 * @param point a node belonging to the AST of the translation unit defining context for
	 *     the size calculation.
	 * @return size and alignment, or {@code null} if could not be calculated.
	 */
	public static SizeAndAlignment getSizeAndAlignment(IType type, IASTNode point) {
		SizeofCalculator calc = point == null ?
				getDefault() : ((ASTTranslationUnit) point.getTranslationUnit()).getSizeofCalculator();
		return calc.sizeAndAlignment(type);
	}

	/**
	 * Returns the default instance of sizeof calculator. The default instance is not aware
	 * of the parser configuration and can only calculate sizes that are the same across all
	 * C/C++ implementations.
	 */
	public static SizeofCalculator getDefault() {
		return defaultInstance;
	}

	public SizeofCalculator(IASTTranslationUnit ast) {
		this.ast = ast;
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
		size_16 = new SizeAndAlignment(16, Math.min(16, maxAlignment));
		sizeof_pointer = getSize(sizeofMacros, "__SIZEOF_POINTER__", maxAlignment); //$NON-NLS-1$
		sizeof_int = getSize(sizeofMacros, "__SIZEOF_INT__", maxAlignment); //$NON-NLS-1$
		sizeof_long = getSize(sizeofMacros, "__SIZEOF_LONG__", maxAlignment); //$NON-NLS-1$
		sizeof_long_long = getSize(sizeofMacros, "__SIZEOF_LONG_LONG__", maxAlignment); //$NON-NLS-1$
		sizeof_int128 = getSize(sizeofMacros, "__SIZEOF_INT128__", maxAlignment); //$NON-NLS-1$
		sizeof_short = getSize(sizeofMacros, "__SIZEOF_SHORT__", maxAlignment); //$NON-NLS-1$
		SizeAndAlignment size = getSize(sizeofMacros, "__SIZEOF_BOOL__", maxAlignment); //$NON-NLS-1$
		// __SIZEOF_BOOL__ is not defined by GCC but sizeof(bool) is needed for template resolution.
		if (size == null)
			size = SIZE_1;
		sizeof_bool = size;
		sizeof_wchar_t = getSize(sizeofMacros, "__SIZEOF_WCHAR_T__", maxAlignment); //$NON-NLS-1$
		sizeof_float = getSize(sizeofMacros, "__SIZEOF_FLOAT__", maxAlignment); //$NON-NLS-1$
		sizeof_complex_float = getSizeOfPair(sizeof_float);
		sizeof_double = getSize(sizeofMacros, "__SIZEOF_DOUBLE__", maxAlignment); //$NON-NLS-1$
		sizeof_complex_double = getSizeOfPair(sizeof_double);
		sizeof_long_double = getSize(sizeofMacros, "__SIZEOF_LONG_DOUBLE__", maxAlignment); //$NON-NLS-1$
		sizeof_complex_long_double = getSizeOfPair(sizeof_long_double);
		sizeof_float128 = size_16;  // GCC does not define __SIZEOF_FLOAT128__
		sizeof_complex_float128 = getSizeOfPair(sizeof_float128);
		sizeof_decimal32 = size_4;  // GCC does not define __SIZEOF_DECIMAL32__
		sizeof_decimal64 = size_8;  // GCC does not define __SIZEOF_DECIMAL64__
		sizeof_decimal128 = size_16;  // GCC does not define __SIZEOF_DECIMAL128__
	}

	private SizeofCalculator() {
		size_2 = new SizeAndAlignment(2, 2);
		size_4 = new SizeAndAlignment(4, 4);
		size_8 = new SizeAndAlignment(8, 8);
		size_16 = new SizeAndAlignment(16, 16);
		sizeof_pointer = null;
		sizeof_int = null;
		sizeof_long = null;
		sizeof_long_long = null;
		sizeof_int128 = size_16;
		sizeof_short = null;
		sizeof_bool = SIZE_1;
		sizeof_wchar_t = null;
		sizeof_float = null;
		sizeof_complex_float = null;
		sizeof_double = null;
		sizeof_complex_double = null;
		sizeof_long_double = null;
		sizeof_complex_long_double = null;
		sizeof_float128 = size_16;
		sizeof_complex_float128 = getSizeOfPair(sizeof_float128);
		sizeof_decimal32 = size_4;
		sizeof_decimal64 = size_8;
		sizeof_decimal128 = size_16;
		ast = null;
	}

	/**
	 * Calculates size and alignment for the given type.
	 * @param type the type to get size and alignment for.
	 * @return size and alignment, or {@code null} if could not be calculated.
	 */
	public SizeAndAlignment sizeAndAlignment(IType type) {
		type = SemanticUtil.getNestedType(type, SemanticUtil.CVTYPE | SemanticUtil.TDEF);
		if (type instanceof IFunctionType) {
			return sizeAndAlignment(((IFunctionType) type).getReturnType());
		}
		if (type instanceof IBasicType) {
			return sizeAndAlignment((IBasicType) type);
		}
		// [expr.sizeof]/2: "When applied to a reference or a reference type,
		// the result is the size of the referenced type."
		if (type instanceof ICPPReferenceType) {
			return sizeAndAlignment(((ICPPReferenceType) type).getType());
		}
		if (type instanceof IPointerType) {
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

	/**
	 * Returns size and alignment of pointer types.
	 * @return size and alignment of pointer types, or {@code null} if unknown.
	 */
	public SizeAndAlignment sizeAndAlignmentOfPointer() {
		return sizeof_pointer;
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
		case eInt128:
			return sizeof_int128;
		case eFloat:
			return type.isComplex() ? sizeof_complex_float : sizeof_float;
		case eDouble:
			return type.isComplex() ?
					(type.isLong() ? sizeof_long_double : sizeof_double) :
					(type.isLong() ? sizeof_complex_long_double : sizeof_complex_double);
		case eFloat128:
			return type.isComplex() ? sizeof_complex_float128 : sizeof_float128;
		case eDecimal32:
			return sizeof_decimal32;
		case eDecimal64:
			return sizeof_decimal64;
		case eDecimal128:
			return sizeof_decimal128;
		case eWChar:
			return sizeof_wchar_t;
		case eChar16:
			return size_2;
		case eChar32:
			return size_4;
		case eNullPtr:
			return sizeof_pointer;
		default:
			return null;
		}
	}

	private SizeAndAlignment sizeAndAlignment(IEnumeration type) {
		IType underlyingType = TypeTraits.underlyingType(type);
		if (underlyingType instanceof IBasicType) {
			return sizeAndAlignment((IBasicType) underlyingType);
		}
		return null;
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
			for (ICPPBase base : ClassTypeHelper.getBases(classType, ast)) {
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
				for (ICPPMethod method : ClassTypeHelper.getDeclaredMethods(classType, ast)) {
					if (method.isVirtual()) {
						// Don't know how to calculate size when there are virtual functions.
						return null;
					}
				}
			}
			fields = ClassTypeHelper.getDeclaredFields(classType, ast);
		} else {
			fields = type.getFields();
		}

		boolean union = type.getKey() == ICompositeType.k_union;
		for (IField field : fields) {
			if (field.isStatic())
				continue;
			IType fieldType = field.getType();
			SizeAndAlignment info;
			// sizeof() on a reference type returns the size of the referenced type.
			// However, a reference field in a structure only occupies as much space
			// as a pointer.
			if (fieldType instanceof ICPPReferenceType) {
				info = sizeof_pointer;
			} else {
				info = sizeAndAlignment(fieldType);
			}
			if (info == null)
				return null;
			if (union) {
				if (size < info.size)
					size = info.size;
			} else {
				if (size > 0)
					size += info.alignment - (size - 1) % info.alignment - 1;
				size += info.size;
			}
			if (maxAlignment < info.alignment)
				maxAlignment = info.alignment;
		}
		if (size == 0)  // a structure cannot have size 0
			size = 1;
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

	private SizeAndAlignment getSizeOfPair(SizeAndAlignment sizeAndAlignment) {
		return sizeAndAlignment == null ?
				null : new SizeAndAlignment(sizeAndAlignment.size * 2, sizeAndAlignment.alignment);
	}
}
