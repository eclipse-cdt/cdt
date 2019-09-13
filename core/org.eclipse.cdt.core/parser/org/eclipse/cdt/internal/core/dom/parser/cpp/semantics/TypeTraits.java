/*******************************************************************************
 * Copyright (c) 2012, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ARRAY;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation.Operator;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.ArithmeticConversion;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnaryTypeTransformation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper.MethodKind;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction;

/**
 * A collection of static methods for determining type traits.
 */
public class TypeTraits {
	private static final ICPPBasicType[] SIGNED_UNDERLYING_ENUM_TYPES = { CPPBasicType.INT, CPPBasicType.LONG,
			CPPBasicType.LONG_LONG, CPPBasicType.INT128 };
	private static final ICPPBasicType[] UNSIGNED_UNDERLYING_ENUM_TYPES = { CPPBasicType.UNSIGNED_INT,
			CPPBasicType.UNSIGNED_LONG, CPPBasicType.UNSIGNED_LONG_LONG, CPPBasicType.UNSIGNED_INT128 };

	private TypeTraits() {
	}

	public static boolean isDefaultedMethod(ICPPMethod method) {
		if (method instanceof ICPPInternalFunction) {
			ICPPInternalFunction internalFunc = (ICPPInternalFunction) method;
			IASTNode definition = internalFunc.getDefinition();
			ICPPASTFunctionDefinition functionDefinition = CPPFunction.getFunctionDefinition(definition);
			if (functionDefinition != null) {
				return functionDefinition.isDefaulted();
			}
		}
		return false;
	}

	/**
	 *	From $3.9 / 10:
	 *	A type is a literal type if it is:
	 *	[...]
	 *	- a possibly cv-qualified class type that has all the following properties:
	 *		- it has a trivial destructor
	 *		- it is an aggregate type or has at least one constexpr constructor or constructor template that is not a
	 *		  copy or move constructor, and
	 *		- all of its non-static data members and base classes are of non-volatile literal types
	 *  TODO: The last property isn't being checked.
	*/
	public static boolean isLiteralClass(ICPPClassType classType) {
		if (!hasTrivialDestructor(classType)) {
			return false;
		}

		if (isAggregateClass(classType)) {
			return true;
		}

		ICPPConstructor[] ctors = classType.getConstructors();
		for (ICPPConstructor ctor : ctors) {
			MethodKind methodKind = ClassTypeHelper.getMethodKind(classType, ctor);
			if (methodKind == MethodKind.COPY_CTOR || methodKind == MethodKind.MOVE_CTOR) {
				continue;
			}

			// implicit constructors are automatically constexpr when the class is a literal type
			if (ctor instanceof CPPImplicitConstructor || ctor.isConstexpr()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * C++11: 9-6
	 */
	public static boolean isTrivial(ICPPClassType classType) {
		return isTrivialImpl(classType, true);
	}

	private static boolean isTrivialImpl(ICPPClassType classType, boolean checkDefaultConstructors) {
		for (ICPPMethod method : classType.getDeclaredMethods()) {
			if (method.isVirtual())
				return false;
			switch (ClassTypeHelper.getMethodKind(classType, method)) {
			case DEFAULT_CTOR:
				if (checkDefaultConstructors) {
					return false;
				}
				break;
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
		ICPPField[] fields = classType.getDeclaredFields();
		for (ICPPField field : fields) {
			if (!field.isStatic()) {
				IType fieldType = SemanticUtil.getNestedType(field.getType(), TDEF);
				if (fieldType instanceof ICPPClassType && !isTrivial((ICPPClassType) fieldType))
					return false;
			}
		}
		for (ICPPBase base : classType.getBases()) {
			if (base.isVirtual())
				return false;
		}
		ICPPClassType[] baseClasses = ClassTypeHelper.getAllBases(classType);
		for (ICPPClassType baseClass : baseClasses) {
			if (!isTrivial(baseClass))
				return false;
		}
		return true;
	}

	/**
	 * C++11: 9-7
	 */
	public static boolean isStandardLayout(IType type) {
		type = SemanticUtil.getNestedType(type, ARRAY | CVTYPE | TDEF);
		if (type instanceof ICPPReferenceType)
			return false;
		if (!(type instanceof ICPPClassType))
			return true;
		ICPPClassType classType = (ICPPClassType) type;
		int visibility = 0;
		ICPPField firstNonStaticField = null;
		ICPPField[] fields = classType.getDeclaredFields();
		for (ICPPField field : fields) {
			if (!field.isStatic()) {
				if (!isStandardLayout(field.getType()))
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
		if (hasDeclaredVirtualMethod(classType))
			return false;
		for (ICPPBase base : classType.getBases()) {
			if (base.isVirtual())
				return false;
		}
		ICPPClassType[] baseClasses = ClassTypeHelper.getAllBases(classType);
		for (ICPPClassType baseClass : baseClasses) {
			if (!isStandardLayout(baseClass))
				return false;
			if (firstNonStaticField != null) {
				if (TypeTraits.hasNonStaticFields(baseClass))
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
	public static boolean isPOD(IType type) {
		if (!isStandardLayout(type))
			return false;
		type = SemanticUtil.getNestedType(type, ARRAY | CVTYPE | TDEF);
		if (!(type instanceof ICPPClassType))
			return true;
		return isTrivial((ICPPClassType) type);
	}

	/**
	 * Returns true if the given type is a class type, but not a union type, with no non-static
	 * data members other than bit-fields of length 0, no virtual member functions, no virtual
	 * base classes, and no base class for which isEmpty is false. [meta.unary.prop]
	 */
	public static boolean isEmpty(IType type) {
		type = SemanticUtil.getNestedType(type, CVTYPE | TDEF);
		if (!(type instanceof ICPPClassType))
			return false;
		ICPPClassType classType = (ICPPClassType) type;
		if (!isItselfEmpty(classType))
			return false;
		ICPPClassType[] baseClasses = ClassTypeHelper.getAllBases(classType);
		for (ICPPClassType baseClass : baseClasses) {
			if (!isItselfEmpty(baseClass))
				return false;
		}
		return true;
	}

	private static boolean isItselfEmpty(ICPPClassType classType) {
		ICPPField[] fields = classType.getDeclaredFields();
		for (ICPPField field : fields) {
			if (!field.isStatic()) {
				// TODO(sprigogin): Check for empty bit fields when bit field size becomes available.
				return false;
			}
		}
		ICPPMethod[] methods = classType.getDeclaredMethods();
		for (ICPPMethod method : methods) {
			if (method.isVirtual())
				return false;
		}
		ICPPBase[] bases = classType.getBases();
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
	public static boolean isAggregateClass(ICPPClassType classType) {
		// 8.1.5.1 p.2 (N4659): The closure type is not an aggregate type.
		if (classType instanceof CPPClosureType)
			return false;
		if (classType.getBases().length > 0) {
			// c++17 http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2015/p0017r1.html
			for (ICPPBase base : classType.getBases()) {
				if (base.isVirtual())
					return false;
				if (base.getVisibility() == ICPPBase.v_private || base.getVisibility() == ICPPBase.v_protected)
					return false;
			}
		}
		ICPPMethod[] methods = classType.getDeclaredMethods();
		for (ICPPMethod m : methods) {
			if (m instanceof ICPPConstructor)
				return false;
			if (m.isVirtual()) {
				return false;
			}
		}
		ICPPField[] fields = classType.getDeclaredFields();
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
	public static boolean hasTrivialCopyCtor(ICPPClassType classType) {
		if (getImplicitCopyCtor(classType) == null)
			return false;
		if (isPolymorphic(classType))
			return false;
		for (ICPPBase base : classType.getBases()) {
			if (base.isVirtual())
				return false;
		}
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType)) {
			if (!classType.isSameType(baseClass) && !hasTrivialCopyCtor(baseClass))
				return false;
		}
		for (ICPPField field : classType.getDeclaredFields()) {
			if (!field.isStatic()) {
				IType type = field.getType();
				type = SemanticUtil.getNestedType(type, TDEF | CVTYPE | ARRAY);
				if (type instanceof ICPPClassType && !classType.isSameType(type)
						&& !hasTrivialCopyCtor((ICPPClassType) type)) {
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
	 * @return {@code true} if the class has a trivial default constructor
	 */
	public static boolean hasTrivialDefaultConstructor(ICPPClassType classType, int maxdepth) {
		if (maxdepth <= 0) {
			return false;
		}
		for (ICPPConstructor ctor : classType.getConstructors()) {
			if (!ctor.isImplicit() && ctor.getParameters().length == 0)
				return false;
		}
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType)) {
			if (!classType.isSameType(baseClass) && !hasTrivialDefaultConstructor(baseClass, maxdepth - 1))
				return false;
		}
		for (ICPPField field : classType.getDeclaredFields()) {
			if (!field.isStatic()) {
				IType type = field.getType();
				type = SemanticUtil.getNestedType(type, TDEF | CVTYPE | ARRAY);
				if (type instanceof ICPPClassType && !classType.isSameType(type)
						&& !hasTrivialDefaultConstructor((ICPPClassType) type, maxdepth - 1)) {
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
	 * <li>it is implicitly defined by the compiler or defaulted, and</li>
	 * <li>every direct base class has trivial destructor, and</li>
	 * <li>for every nonstatic data member that has class type or array of class type, that type
	 * has trivial destructor.</li>
	 * </ul>
	 * Similar to {@code std::tr1::has_trivial_destructor}.
	 *
	 * @param classType the class to check
	 * @return {@code true} if the class has a trivial destructor
	 */
	public static boolean hasTrivialDestructor(ICPPClassType classType) {
		return hasTrivialDestructor(classType, new HashSet<>());
	}

	private static boolean hasTrivialDestructor(ICPPClassType classType, Set<ICPPClassType> checkedClasses) {
		if (!checkedClasses.add(classType))
			return true; // Checked already.

		for (ICPPMethod method : classType.getDeclaredMethods()) {
			if (method.isDestructor() && !isDefaultedMethod(method))
				return false;
		}
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType)) {
			if (!hasTrivialDestructor(baseClass, checkedClasses))
				return false;
		}
		for (ICPPField field : classType.getDeclaredFields()) {
			if (!field.isStatic()) {
				IType type = field.getType();
				type = SemanticUtil.getNestedType(type, TDEF | CVTYPE | ARRAY);
				if (type instanceof ICPPClassType && !hasTrivialDestructor((ICPPClassType) type, checkedClasses)) {
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
	public static boolean isPolymorphic(ICPPClassType classType) {
		if (hasDeclaredVirtualMethod(classType))
			return true;
		for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType)) {
			if (hasDeclaredVirtualMethod(baseClass))
				return true;
		}
		return false;
	}

	private static boolean hasNonStaticFields(ICPPClassType classType) {
		ICPPField[] fields = classType.getDeclaredFields();
		for (ICPPField field : fields) {
			if (!field.isStatic())
				return true;
		}
		return false;
	}

	public static boolean isAbstract(ICPPClassType classType) {
		return SemanticQueries.getPureVirtualMethods(classType).length != 0;
	}

	/**
	 * Returns the compiler-generated copy constructor for the given class, or {@code null}
	 * if the class doesn't have a compiler-generated copy constructor.
	 *
	 * @param classType the class to get the copy ctor for.
	 * @return the compiler-generated copy constructor, or {@code null} if the class doesn't
	 *     have a compiler-generated copy constructor.
	 */
	private static ICPPConstructor getImplicitCopyCtor(ICPPClassType classType) {
		for (ICPPConstructor ctor : classType.getConstructors()) {
			if (ctor.isImplicit() && ClassTypeHelper.getMethodKind(classType, ctor) == MethodKind.COPY_CTOR)
				return ctor;
		}
		return null;
	}

	private static boolean hasDeclaredVirtualMethod(ICPPClassType classType) {
		for (ICPPMethod method : classType.getDeclaredMethods()) {
			if (method.isVirtual()) {
				return true;
			}
		}
		return false;
	}

	public static IType underlyingType(IType type) {
		if (CPPTemplates.isDependentType(type)) {
			return new CPPUnaryTypeTransformation(Operator.underlying_type, type);
		}

		type = SemanticUtil.getSimplifiedType(type);
		if (!(type instanceof ICPPEnumeration)) {
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
		return types[types.length - 1]; // Assume it fits into the largest type provided.
	}

	/**
	 * Returns true if 'type' is scalar, as defined in [basic.types] p9:
	 *
	 * "Arithmetic types, enumeration types, pointer types, pointer to member
	 * types, std::nullptr_t, and cv-qualified versions of these types are
	 * collectively called scalar types."
	 */
	private static boolean isScalar(IType type) {
		type = SemanticUtil.getNestedType(type, SemanticUtil.ALLCVQ);
		return type instanceof IBasicType || type instanceof IEnumeration || type instanceof IPointerType;
	}

	/**
	 * Returns true if 'type' is a trivially copyable class, as defined in [class] p6:
	 *
	 * "A trivially copyable class is a class that:
	 *    - has no non-trivial copy constructors,
	 *    - has no non-trivial move constructors,
	 *    - has no non-trivial copy assignment operators,
	 *    - has no non-trivial move assignment operators, and
	 *    - has a trivial destructor."
	 */
	private static boolean isTriviallyCopyableClass(ICPPClassType type) {
		return isTrivialImpl(type, false);
	}

	/**
	 * Returns true if 'type' is trivially copyable, as defined in [basic.types] p9:
	 *
	 * "Cv-unqualified scalar types, trivially copyable class types, arrays
	 * of such types, and non-volatile const-qualified versions of these
	 * types are collectively called trivially copyable types."
	 */
	public static boolean isTriviallyCopyable(IType type) {
		type = SemanticUtil.getSimplifiedType(type);
		CVQualifier qualifier = SemanticUtil.getCVQualifier(type);
		if (qualifier.isVolatile()) {
			return false;
		} else if (qualifier.isConst()) {
			return isTriviallyCopyable(SemanticUtil.getNestedType(type, SemanticUtil.ALLCVQ));
		} else if (type instanceof IArrayType) {
			return isTriviallyCopyable(((IArrayType) type).getType());
		} else if (type instanceof ICPPClassType) {
			return isTriviallyCopyableClass((ICPPClassType) type);
		} else {
			return isScalar(type);
		}
	}

	/**
	 * Returns true if 'typeToConstruct' is constructible from arguments
	 * of type 'argumentTypes', as defined in [meta.unary.prop].
	 *
	 * If 'checkTrivial' is true, additionally checks if 'typeToConstruct'
	 * is trivially constructible from said argument types.
	 */
	public static boolean isConstructible(IType typeToConstruct, IType[] argumentTypes, IBinding pointOfDefinition,
			boolean checkTrivial) {
		IType type = SemanticUtil.getSimplifiedType(typeToConstruct);
		if (!(type instanceof ICPPClassType)) {
			return true;
		}
		// Invent (the evaluation of) a type constructor expression of the form "T(declval<Args>()...)".
		// (The standard says a variable declaration of the form "T t(declval<Args>()...)",
		// but we don't currently type-check variable initialization, and a type constructor expression
		// should have the same semantics.)
		ICPPEvaluation[] arguments = new ICPPEvaluation[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			// Value category is xvalue because declval() returns an rvalue reference.
			arguments[i] = new EvalFixed(argumentTypes[i], ValueCategory.XVALUE, IntegralValue.UNKNOWN);
		}
		EvalTypeId eval = new EvalTypeId(type, pointOfDefinition, false, false, arguments);
		ICPPFunction constructor = eval.getConstructor();
		if (!(constructor instanceof ICPPMethod)) {
			return false;
		}
		// TODO check that conversions are trivial as well
		if (checkTrivial && !((ICPPMethod) constructor).isImplicit()) {
			return false;
		}
		return true;
	}
}
