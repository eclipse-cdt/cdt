/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeTraitType.TypeTraitOperator;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.ArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypeTraitType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper.MethodKind;

/**
 * A collection of static methods for determining type traits.
 */
public class TypeTraits {
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
	 * Returns <code>true</code> if and only if the given class has a trivial copy constructor.
	 * A copy constructor is trivial if:
	 * <ul>
	 * <li>it is implicitly defined by the compiler, and</li>
	 * <li><code>isPolymorphic(classType) is false</code>, and</li>
	 * <li>the class has no virtual base classes, and</li>
	 * <li>every direct base class has trivial copy constructor, and</li>
	 * <li>for every nonstatic data member that has class type or array of class type, that type
	 * has trivial copy constructor.</li>
	 * </ul>
	 * Similar to <code>std::tr1::has_trivial_copy</code>.
	 *
	 * @param classType the class to check
	 * @return <code>true</code> if the class has a trivial copy constructor
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
		for (ICPPField field : classType.getDeclaredFields()) {
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
	 * Returns <code>true</code> if and only if the given class has a trivial default constructor.
	 * A default constructor is trivial if:
	 * <ul>
	 * <li>it is implicitly defined by the compiler, and</li>
	 * <li>every direct base class has trivial default constructor, and</li>
	 * <li>for every nonstatic data member that has class type or array of class type, that type
	 * has trivial default constructor.</li>
	 * </ul>
	 * Similar to <code>std::tr1::has_trivial_default_constructor</code>.
	 *
	 * @param classType the class to check
	 * @param point
	 * @return <code>true</code> if the class has a trivial default constructor
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
	 * Returns <code>true</code> if and only if the given class has a trivial destructor.
	 * A destructor is trivial if:
	 * <ul>
	 * <li>it is implicitly defined by the compiler, and</li>
	 * <li>every direct base class has trivial destructor, and</li>
	 * <li>for every nonstatic data member that has class type or array of class type, that type
	 * has trivial destructor.</li>
	 * </ul>
	 * Similar to <code>std::tr1::has_trivial_destructor</code>.
	 *
	 * @param classType the class to check
	 * @return <code>true</code> if the class has a trivial destructor
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
	 * Returns <code>true</code> if and only if the given class declares or inherits a virtual
	 * function. Similar to <code>std::tr1::is_polymorphic</code>.
	 *
	 * @param classType the class to check
	 * @return <code>true</code> if the class declares or inherits a virtual function.
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
	 * Returns the compiler-generated copy constructor for the given class, or <code>null</code>
	 * if the class doesn't have a compiler-generated copy constructor.
	 *
	 * @param classType the class to get the copy ctor for.
	 * @return the compiler-generated copy constructor, or <code>null</code> if the class doesn't
	 * have a compiler-generated copy constructor.
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
			return new CPPTypeTraitType(TypeTraitOperator.underlying_type, type);
		} else if (!(type instanceof ICPPEnumeration)) {
			return ProblemType.ENUMERATION_EXPECTED;
		} else {
			ICPPEnumeration enumeration = (ICPPEnumeration) type;

			// [dcl.enum] p5
			// "The underlying type can be explicitly specified using enum-base;
			// if not explicitly specified, the underlying type of a scoped
			// enumeration type is int."
			IType fixedType = enumeration.getFixedType();
			if (fixedType != null)
				return fixedType;
			if (enumeration.isScoped())
				return CPPVisitor.INT_TYPE;
			
			// [dcl.enum] p6
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
				return CPPVisitor.INT_TYPE;
			if (enumeration.getMinValue() < 0 || enumeration.getMaxValue() < 0) {
				return smallestFittingType(enumeration,
						CPPVisitor.INT_TYPE, 
						CPPVisitor.LONG_TYPE, 
						CPPVisitor.LONG_LONG_TYPE,
						CPPVisitor.INT128_TYPE);
			} else {
				return smallestFittingType(enumeration,
						CPPVisitor.UNSIGNED_INT, 
						CPPVisitor.UNSIGNED_LONG, 
						CPPVisitor.UNSIGNED_LONG_LONG,
						CPPVisitor.UNSIGNED_INT128);
			}
		}
	}
	
	private static IBasicType smallestFittingType(ICPPEnumeration enumeration, ICPPBasicType... types) {
		for (int i = 0; i < types.length - 1; ++i) {
			if (ArithmeticConversion.fitsIntoType(types[i], enumeration.getMinValue())
			 && ArithmeticConversion.fitsIntoType(types[i], enumeration.getMaxValue())) {
				return types[i];
			}
		}
		return types[types.length - 1];  // assume it fits into the largest type provided
	}
}
