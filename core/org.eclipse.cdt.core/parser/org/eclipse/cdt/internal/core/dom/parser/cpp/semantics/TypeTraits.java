/*******************************************************************************
 * Copyright (c) 2012, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ARRAY;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation.Operator;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.ArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnaryTypeTransformation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper.MethodKind;

/**
 * A collection of static methods for determining type traits.
 */
public class TypeTraits {
	private static final ICPPBasicType[] SIGNED_UNDERLYING_ENUM_TYPES = {
		CPPBasicType.INT,
		CPPBasicType.LONG,
		CPPBasicType.LONG_LONG,
		CPPBasicType.INT128
	};
	private static final ICPPBasicType[] UNSIGNED_UNDERLYING_ENUM_TYPES = {
		CPPBasicType.UNSIGNED_INT,
		CPPBasicType.UNSIGNED_LONG,
		CPPBasicType.UNSIGNED_LONG_LONG,
		CPPBasicType.UNSIGNED_INT128
	};

	private TypeTraits() {}

	/**
	 * C++11: 9-6
	 */
	public static boolean isTrivial(ICPPClassType classType, IASTNode point) {
		for (ICPPMethod method : ClassTypeHelper.getDeclaredMethods(classType, point)) {
			if (method.isVirtual())
				return false;
			switch (ClassTypeHelper.getMethodKind(classType, method)) {
			case DEFAULT_CTOR:
			case COPY_CTOR:
			case MOVE_CTOR:
			case COPY_ASSIGNMENT_OP:
			case MOVE_ASSIGNMENT_OP:
			case DTOR:
				return false;
			default:
				break;
			}
		}
		ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, point);
		for (ICPPField field : fields) {
			if (!field.isStatic()) {
				IType fieldType = SemanticUtil.getNestedType(field.getType(), TDEF);
				if (fieldType instanceof ICPPClassType && !isTrivial((ICPPClassType) fieldType, point))
					return false;
			}
		}
		for (ICPPBase base : ClassTypeHelper.getBases(classType, point)) {
			if (base.isVirtual())
				return false;
		}
		ICPPClassType[] baseClasses = ClassTypeHelper.getAllBases(classType, point);
		for (ICPPClassType baseClass : baseClasses) {
			if (!isTrivial(baseClass, point))
				return false;
		}
		return true;
	}

	/**
	 * C++11: 9-7
	 */
	public static boolean isStandardLayout(IType type, IASTNode point) {
		type = SemanticUtil.getNestedType(type, ARRAY | CVTYPE | TDEF);
		if (type instanceof ICPPReferenceType)
			return false;
		if (!(type instanceof ICPPClassType))
			return true;
		ICPPClassType classType = (ICPPClassType) type;
		int visibility = 0;
		ICPPField firstNonStaticField = null;
		ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, point);
		for (ICPPField field : fields) {
			if (!field.isStatic()) {
				if (!isStandardLayout(field.getType(), point))
					return false;
				int vis = field.getVisibility();
				if (visibility == 0) {
					visibility = vis;
				} else if (vis != visibility) {
					return false;
				}
				if (firstNonStaticField == null)
					firstNonStaticField = field;
			}
		}
		if (hasDeclaredVirtualMethod(classType, point))
			return false;
		for (ICPPBase base : ClassTypeHelper.getBases(classType, point)) {
			if (base.isVirtual())
				return false;
		}
		ICPPClassType[] baseClasses = ClassTypeHelper.getAllBases(classType, point);
		for (ICPPClassType baseClass : baseClasses) {
			if (!isStandardLayout(baseClass, point))
				return false;
			if (firstNonStaticField != null) {
				if (TypeTraits.hasNonStaticFields(baseClass, point))
					return false;
				if (firstNonStaticField.getType().isSameType(baseClass))
					return false;
			}
		}
		return true;
	}

	/**
	 * C++11: 9-10
	 */
	public static boolean isPOD(IType type, IASTNode point) {
		if (!isStandardLayout(type, point))
			return false;
		type = SemanticUtil.getNestedType(type, ARRAY | CVTYPE | TDEF);
		if (!(type instanceof ICPPClassType))
			return true;
		return isTrivial((ICPPClassType) type, point);
	}

	/**
	 * Returns true if the given type is a class type, but not a union type, with no non-static
	 * data members other than bit-fields of length 0, no virtual member functions, no virtual
	 * base classes, and no base class for which isEmpty is false. [meta.unary.prop]
	 */
	public static boolean isEmpty(IType type, IASTNode point) {
		type = SemanticUtil.getNestedType(type, CVTYPE | TDEF);
		if (!(type instanceof ICPPClassType))
			return false;
		ICPPClassType classType = (ICPPClassType) type;
		if (!isItselfEmpty(classType, point))
			return false;
		ICPPClassType[] baseClasses = ClassTypeHelper.getAllBases(classType, point);
		for (ICPPClassType baseClass : baseClasses) {
			if (!isItselfEmpty(baseClass, point))
				return false;
		}
		return true;
	}

	private static boolean isItselfEmpty(ICPPClassType classType, IASTNode point) {
		ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, point);
		for (ICPPField field : fields) {
			if (!field.isStatic()) {
				// TODO(sprigogin): Check for empty bit fields when bit field size becomes available.
				return false;
			}
		}
		ICPPMethod[] methods = ClassTypeHelper.getDeclaredMethods(classType, point);
		for (ICPPMethod method : methods) {
			if (method.isVirtual())
				return false;
		}
		ICPPBase[] bases = ClassTypeHelper.getBases(classType, point);
		for (ICPPBase base : bases) {
			if (base.isVirtual())
				return false;
		}
		return true;
	}

	/**
	 * 8.5.1 Aggregates [dcl.init.aggr]
	 * An aggregate is an array or a class (Clause 9) with no user-provided constructors (12.1),
	 * no private or protected non-static data members (Clause 11),
	 * no base classes (Clause 10), and no virtual functions (10.3).
	 */
	public static boolean isAggregateClass(ICPPClassType classType, IASTNode point) {
		if (ClassTypeHelper.getBases(classType, point).length > 0)
			return false;
		ICPPMethod[] methods = ClassTypeHelper.getDeclaredMethods(classType, point);
		for (ICPPMethod m : methods) {
			if (m instanceof ICPPConstructor)
				return false;
			if (m.isVirtual()) {
				return false;
			}
		}
		ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, point);
		for (ICPPField field : fields) {
			if (!(field.getVisibility() == ICPPMember.v_public || field.isStatic())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns {@code true} if and only if the given class has a trivial copy constructor.
	 * A copy constructor is trivial if:
	 * <ul>
	 * <li>it is implicitly defined by the compiler, and</li>
	 * <li>{@code isPolymorphic(classType)} is {@code false}, and</li>
	 * <li>the class has no virtual base classes, and</li>
	 * <li>every direct base class has trivial copy constructor, and</li>
	 * <li>for every nonstatic data member that has class type or array of class type, that type
	 * has trivial copy constructor.</li>
	 * </ul>
	 * Similar to {@code std::tr1::has_trivial_copy}.
	 *
	 * @param classType the class to check
	 * @return {@code true} if the class has a trivial copy constructor
	 */
	public static boolean hasTrivialCopyCtor(ICPPClassType classType, IASTNode point) {
		if (getImplicitCopyCtor(classType, point) == null)
			return false;
		if (isPolymorphic(classType, point))
			return false;
		for (ICPPBase base : ClassTypeHelper.getBases(classType, point)) {
			if (base.isVirtual())
				return false;
		}
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType, point)) {
			if (!classType.isSameType(baseClass) && !hasTrivialCopyCtor(baseClass, point))
				return false;
		}
		for (ICPPField field : ClassTypeHelper.getDeclaredFields(classType, point)) {
			if (!field.isStatic()) {
				IType type = field.getType();
				type = SemanticUtil.getNestedType(type, TDEF | CVTYPE | ARRAY);
				if (type instanceof ICPPClassType && !classType.isSameType(type) &&
						!hasTrivialCopyCtor((ICPPClassType) type, point)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns {@code true} if and only if the given class has a trivial default constructor.
	 * A default constructor is trivial if:
	 * <ul>
	 * <li>it is implicitly defined by the compiler, and</li>
	 * <li>every direct base class has trivial default constructor, and</li>
	 * <li>for every nonstatic data member that has class type or array of class type, that type
	 * has trivial default constructor.</li>
	 * </ul>
	 * Similar to {@code std::tr1::has_trivial_default_constructor}.
	 *
	 * @param classType the class to check
	 * @param point
	 * @return {@code true} if the class has a trivial default constructor
	 */
	public static boolean hasTrivialDefaultConstructor(ICPPClassType classType, IASTNode point) {
		for (ICPPConstructor ctor : ClassTypeHelper.getConstructors(classType, point)) {
			if (!ctor.isImplicit() && ctor.getParameters().length == 0)
				return false;
		}
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType, null)) {
			if (!classType.isSameType(baseClass) && !hasTrivialDefaultConstructor(baseClass, point))
				return false;
		}
		for (ICPPField field : ClassTypeHelper.getDeclaredFields(classType, point)) {
			if (!field.isStatic()) {
				IType type = field.getType();
				type = SemanticUtil.getNestedType(type, TDEF | CVTYPE | ARRAY);
				if (type instanceof ICPPClassType && !classType.isSameType(type) &&
						!hasTrivialDefaultConstructor((ICPPClassType) type, point)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns {@code true} if and only if the given class has a trivial destructor.
	 * A destructor is trivial if:
	 * <ul>
	 * <li>it is implicitly defined by the compiler, and</li>
	 * <li>every direct base class has trivial destructor, and</li>
	 * <li>for every nonstatic data member that has class type or array of class type, that type
	 * has trivial destructor.</li>
	 * </ul>
	 * Similar to {@code std::tr1::has_trivial_destructor}.
	 *
	 * @param classType the class to check
	 * @return {@code true} if the class has a trivial destructor
	 */
	public static boolean hasTrivialDestructor(ICPPClassType classType, IASTNode point) {
		for (ICPPMethod method : ClassTypeHelper.getDeclaredMethods(classType, point)) {
			if (method.isDestructor())
				return false;
		}
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType, null)) {
			if (!classType.isSameType(baseClass) && !hasTrivialDestructor(baseClass, point))
				return false;
		}
		for (ICPPField field : ClassTypeHelper.getDeclaredFields(classType, point)) {
			if (!field.isStatic()) {
				IType type = field.getType();
				type = SemanticUtil.getNestedType(type, TDEF | CVTYPE | ARRAY);
				if (type instanceof ICPPClassType && !classType.isSameType(type) &&
						!hasTrivialDestructor((ICPPClassType) type, point)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns {@code true} if and only if the given class declares or inherits a virtual
	 * function. Similar to {@code std::tr1::is_polymorphic}.
	 *
	 * @param classType the class to check
	 * @return {@code true} if the class declares or inherits a virtual function.
	 */
	public static boolean isPolymorphic(ICPPClassType classType, IASTNode point) {
		if (hasDeclaredVirtualMethod(classType, point))
			return true;
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType, point)) {
			if (hasDeclaredVirtualMethod(baseClass, point))
				return true;
		}
		return false;
	}

	private static boolean hasNonStaticFields(ICPPClassType classType, IASTNode point) {
		ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, point);
		for (ICPPField field : fields) {
			if (!field.isStatic())
				return true;
		}
		return false;
	}

	public static boolean isAbstract(ICPPClassType classType, IASTNode point) {
		return SemanticQueries.getPureVirtualMethods(classType, point).length != 0;
	}

	/**
	 * Returns the compiler-generated copy constructor for the given class, or {@code null}
	 * if the class doesn't have a compiler-generated copy constructor.
	 *
	 * @param classType the class to get the copy ctor for.
	 * @return the compiler-generated copy constructor, or {@code null} if the class doesn't
	 *     have a compiler-generated copy constructor.
	 */
	private static ICPPConstructor getImplicitCopyCtor(ICPPClassType classType, IASTNode point) {
		for (ICPPConstructor ctor : ClassTypeHelper.getConstructors(classType, point)) {
			if (ctor.isImplicit() && ClassTypeHelper.getMethodKind(classType, ctor) == MethodKind.COPY_CTOR)
				return ctor;
		}
		return null;
	}

	private static boolean hasDeclaredVirtualMethod(ICPPClassType classType, IASTNode point) {
		for (ICPPMethod method : ClassTypeHelper.getDeclaredMethods(classType, point)) {
			if (method.isVirtual()) {
				return true;
			}
		}
		return false;
	}
	
	public static IType underlyingType(IType type) {
		if (CPPTemplates.isDependentType(type)) {
			return new CPPUnaryTypeTransformation(Operator.underlying_type, type);
		} else if (!(type instanceof ICPPEnumeration)) {
			return ProblemType.ENUMERATION_EXPECTED;
		} else {
			ICPPEnumeration enumeration = (ICPPEnumeration) type;

			IType fixedType = enumeration.getFixedType();
			if (fixedType != null)
				return fixedType;
			
			// [dcl.enum] 7.2-6:
			// "For an enumeration whose underlying type is not fixed, the
			// underlying type is an integral type that can represent all
			// the numerator values defined in the enumeration. ... It is
			// implementation-defined which integral type is used as the
			// underlying type except that the underlying type shall not be
			// larger than int unless the value of an enumerator cannot fit
			// in an int or unsigned int. If the enumerator-list is empty,
			// the underlying type is as if the enumeration had a single
			// enumerator with value 0."
			if (enumeration.getEnumerators().length == 0)
				return CPPBasicType.INT;
			long minValue = enumeration.getMinValue();
			long maxValue = enumeration.getMaxValue();
			if (minValue < 0 || maxValue < 0) {
				return smallestFittingType(minValue, maxValue, SIGNED_UNDERLYING_ENUM_TYPES);
			} else {
				return smallestFittingType(minValue, maxValue, UNSIGNED_UNDERLYING_ENUM_TYPES);
			}
		}
	}

	private static IBasicType smallestFittingType(long minValue, long maxValue, ICPPBasicType[] types) {
		for (ICPPBasicType type : types) {
			if (ArithmeticConversion.fitsIntoType(type, minValue)
					&& ArithmeticConversion.fitsIntoType(type, maxValue)) {
				return type;
			}
		}
		return types[types.length - 1];  // Assume it fits into the largest type provided.
	}
}
