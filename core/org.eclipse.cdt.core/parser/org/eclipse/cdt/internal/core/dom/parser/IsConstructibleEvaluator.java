package org.eclipse.cdt.internal.core.dom.parser;

import static org.eclipse.cdt.core.parser.util.ObjectUtil.allInstanceOf;
import static org.eclipse.cdt.core.parser.util.TypeUtil.hasCopyConstructor;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isAbtract;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isArray;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isBool;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isClass;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isConst;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isConstReference;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isEnum;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isNonTrivial;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isNumeric;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isPointer;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isRValueReference;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isReference;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isUnConstReference;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isUnion;
import static org.eclipse.cdt.core.parser.util.TypeUtil.isVolatile;
import static org.eclipse.cdt.internal.core.dom.parser.ArithmeticConversion.fitsIntoType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.CHAR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.DOUBLE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.INT;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.NULL_PTR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.VOID;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateType;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;

public class IsConstructibleEvaluator {

	private IBinding pointOfDefinition;
	private IType typeToConstruct;
	private IType[] argumentTypes;
	private boolean checkTrivial;
	private boolean targetIsAbstract;

	private IType targetNested;
	private IType source;
	private IType sourceNested;

	public IsConstructibleEvaluator(IType typeToConstruct, IType[] argumentTypes, IBinding pointOfDefinition,
			boolean checkTrivial) {
		this.typeToConstruct = typeToConstruct;
		this.argumentTypes = argumentTypes;
		this.pointOfDefinition = pointOfDefinition;
		this.checkTrivial = checkTrivial;

		targetNested = getUltimateType(typeToConstruct, false);

		if (argumentTypes.length == 1) {
			source = argumentTypes[0];
			sourceNested = getUltimateType(source, false);
		}
		this.targetIsAbstract = isAbtract(targetNested);
	}

	public IValue evaluate() {
		boolean isCastConstructible = isCastConstructible();
		boolean isParameterConstructible = isParameterConstructible();
		boolean isArrayWithLengthWithNoSource = isArrayWithLengthWithNoSource();
		return IntegralValue.create(isCastConstructible || isParameterConstructible || isArrayWithLengthWithNoSource);
	}

	private boolean isArrayWithLengthWithNoSource() {
		return argumentTypes.length == 0 && targetNested instanceof IArrayType;
	}

	private boolean isParameterConstructible() {
		return isParameterConstructibleX(typeToConstruct, pointOfDefinition, argumentTypes);
	}

	/**
	 * Invent (the evaluation of) a type constructor expression of the form "T(declval<Args>()...)".
	 * (The standard says a variable declaration of the form "T t(declval<Args>()...)",
	 * but we don't currently type-check variable initialization, and a type constructor expression
	 * should have the same semantics.)
	 **/
	private boolean isParameterConstructibleX(IType typeToConstruct2, IBinding pointOfDefinition2,
			IType[] argumentTypes2) {

		ICPPEvaluation[] arguments = new ICPPEvaluation[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			// Value category is xvalue because declval() returns an rvalue reference.
			arguments[i] = new EvalFixed(argumentTypes[i], ValueCategory.XVALUE, IntegralValue.UNKNOWN);
		}

		if (arguments.length == 0 && hasTargetSingleArgumentTemplateConstructor()) {
			return false;
		}
		EvalTypeId eval = new EvalTypeId(typeToConstruct, pointOfDefinition, false, false, arguments);
		ICPPFunction constructor = eval.getConstructor();
		if (!(constructor instanceof ICPPMethod)) {
			return false;
		}

		if (constructor.isDeleted()) {
			return false;
		}

		if (isUnion(typeToConstruct2)) {
			if (isNonTrivial(typeToConstruct2) && (arguments.length == 0 || isReference(source))) {
				return false;
			}
		}

		boolean hasDefaultParams = false;

		// Default value evaluates to nullptr
		for (int i = arguments.length; i < constructor.getParameters().length; i++) {
			hasDefaultParams = true;
			ICPPParameter constructorParam = constructor.getParameters()[i];
			if (constructorParam.getDefaultValue() == IntegralValue.NULL_PTR) {
				return false;
			}
		}

		if (argumentTypes.length == 0 && isClass(targetNested) && !hasDefaultParams) {
			if (!((ICPPMethod) constructor).isImplicit()) {
				// No parameters
				return false;
			}
		}

		if (eval.getDestructor().isDeleted()) {
			return false;
		}

		// TODO check that conversions are trivial as well
		if (checkTrivial && !((ICPPMethod) constructor).isImplicit()) {
			return false;
		}
		return !targetIsAbstract && !VOID.isSameType(source);
	}

	private boolean hasTargetSingleArgumentTemplateConstructor() {
		if (targetNested instanceof CPPClassType) {
			CPPClassType clazzToCheck = (CPPClassType) targetNested;

			for (ICPPConstructor conzt : clazzToCheck.getConstructors()) {
				if (conzt.getParameters().length == 1 && conzt instanceof CPPConstructorTemplate) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isCastConstructible() {
		if (argumentTypes.length == 1) {
			if (allInstanceOf(ICPPPointerToMemberType.class, typeToConstruct, source)) {
				IType targetClz = ((ICPPPointerToMemberType) typeToConstruct).getMemberOfClass();
				IType srcClz = ((ICPPPointerToMemberType) source).getMemberOfClass();
				return targetNested.isSameType(sourceNested) && targetClz.isSameType(srcClz);
			}
			if (allInstanceOf(ICPPBasicType.class, targetNested, sourceNested)) {
				if (isReference(typeToConstruct) && isReference(source)) {
					if (targetNested.isSameType(sourceNested) || (INT.isSameType(targetNested) && isNumeric(source))
							|| (isCastconstructible(targetNested, sourceNested) && !CHAR.isSameType(sourceNested))) {
						if (isRValueCompatible()) {
							if (isRValueReference(typeToConstruct, source) && isConst(typeToConstruct, source)) {
								return true;
							}

							if (isArray(typeToConstruct, source) && !arrayLengthFits(typeToConstruct, source)) {
								return false;
							}

							if (isArray(typeToConstruct) != isArray(source)) {
								return false;
							}
							return ((!isConstReference(source)) && (!isVolatile(source)))
									|| isConstReference(typeToConstruct, source);
						} else if (isRValueReference(typeToConstruct) && INT.isSameType(targetNested)
								&& DOUBLE.isSameType(sourceNested)) {
							return true;
						} else if (isConstReference(typeToConstruct) && isRValueReference(source)
								&& !isVolatile(typeToConstruct)) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
				if (isPointer(typeToConstruct) && isPointer(source)) {
					if (targetNested.isSameType(VOID) && ((ICPPBasicType) sourceNested).isNumeric()) {
						return true;
					}
					//return fitsIntoType((ICPPBasicType) targetNested, (ICPPBasicType) sourceNested); //targetNested.isSameType(sourceNested);
					return targetNested.isSameType(sourceNested);
				}
				if (isPointer(typeToConstruct) && sourceNested.isSameType(NULL_PTR)) {
					return true;
				}
				if (isPointer(typeToConstruct) != isPointer(source)) {
					return false;
				}

				return isCastconstructible(targetNested, sourceNested);
			} else if (isClass(targetNested, sourceNested)) {
				if (targetNested.isSameType(sourceNested)) {
					ICPPClassType targetClz = (ICPPClassType) targetNested;
					for (ICPPConstructor conzt : targetClz.getConstructors()) {
						if (conzt.isDeleted() && conzt.getParameters().length == 1) {
							IType paramUlimate = getUltimateType(conzt.getParameters()[0].getType(), false);
							if (paramUlimate.isSameType(sourceNested)) {
								return false;
							}
						}
					}

					if (evaluateDestructor().isDeleted()) {
						return false;
					}

					if (targetIsAbstract) {
						return false;
					}

					if (isConstReference(typeToConstruct) && !isReference(source)
					/*&& !isRValueReference(typeToConstruct)*/) {
						return !isVolatile(typeToConstruct) || isRValueReference(typeToConstruct);
					}

					if (isUnConstReference(typeToConstruct) && !isReference(source)
							&& !isRValueReference(typeToConstruct)) {
						return false;
					}

					if (isRValueReference(source) && isConst(typeToConstruct) && !isVolatile(typeToConstruct)) {
						return true;
					}

					if (!isConst(typeToConstruct, source) && isRValueReference(typeToConstruct)
							&& !isReference(source)) {
						return true;
					}

					if (isConst(typeToConstruct, source) && isRValueReference(typeToConstruct)
							&& isVolatile(typeToConstruct)) {
						return isRValueReference(source);
					}

					if (!isReference(typeToConstruct, source) && hasCopyConstructor(targetClz, source)) {
						return false;
					}

					return isRValueCompatible() && !isConst(source);
				}
				ICPPClassType srcClz = (ICPPClassType) sourceNested;
				ICPPClassType[] srcBases = ClassTypeHelper.getAllBases(srcClz);

				//Cast operator
				if (isImplicitelyCastable(srcClz)) {
					return true;
				}

				for (ICPPClassType srcBase : srcBases) {
					if (srcBase.isSameType(targetNested)) {
						if ((isReference(source) && isReference(typeToConstruct) || isConst(source))
								&& !isConstReference(source))
							return isRValueCompatible();
					}
				}
			} else if (isEnum(targetNested, sourceNested)) {
				return targetNested.isSameType(sourceNested);
			} else if ((isBool(targetNested) || isNumeric(targetNested)) && isEnum(sourceNested)) {
				return !((ICPPEnumeration) sourceNested).isScoped();
			} else if (allInstanceOf(ICPPBasicType.class, targetNested)
					&& allInstanceOf(ICPPClassType.class, sourceNested)) {
				if (isImplicitelyCastable(((ICPPClassType) sourceNested))) {
					//Cast operator
					return true;
				}
			} else if (allInstanceOf(ICPPFunctionType.class, sourceNested, targetNested)
					&& (!VOID.isSameType(((ICPPFunctionType) targetNested).getReturnType())
							|| isReference(typeToConstruct))) {
				return sourceNested.isSameType(targetNested);
			}
		}
		return false;
	}

	private boolean arrayLengthFits(IType t1, IType t2) {
		try {
			if (isArray(t1, t2)) {
				Number l1 = getArray(t1).getSize() == null ? 0 : getArray(t1).getSize().numberValue();
				Number l2 = getArray(t2).getSize() == null ? 0 : getArray(t2).getSize().numberValue();

				return l1.longValue() == l2.longValue();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return false;
	}

	private IArrayType getArray(IType check) {
		if (check instanceof IArrayType) {
			return (IArrayType) check;
		} else if (check instanceof ICPPReferenceType) {
			return getArray(((ICPPReferenceType) check).getType());
		}
		return null;
	}

	private boolean isImplicitelyCastable(ICPPClassType srcClz) {
		for (ICPPMethod potentialCast : srcClz.getAllDeclaredMethods()) {
			if (potentialCast.isDeleted()) {
				continue;
			}
			if (potentialCast.isExplicit() && (!allInstanceOf(ICPPBasicType.class, targetNested))) {
				if (isConstReference(typeToConstruct)) {
					return true;
				}
				continue;
			}

			IType potentialTarget = getUltimateType(potentialCast.getType(), false);
			IType potentialTargetOrig = potentialCast.getType();
			IType origReturnType = potentialCast.getType();
			if (potentialTarget instanceof CPPFunctionType) {
				potentialTargetOrig = ((CPPFunctionType) potentialTarget).getReturnType();
				origReturnType = ((CPPFunctionType) potentialTarget).getReturnType();
				potentialTarget = getUltimateType(potentialTargetOrig, false);
			}

			if (!potentialCast.isExplicit() && isConstReference(typeToConstruct) && isRValueReference(origReturnType)) {
				return true;
			}

			if (potentialCast.isExplicit() && !isReference(origReturnType) && targetNested instanceof ICPPBasicType
					&& isConstReference(typeToConstruct)) {
				return false;
			}

			if (potentialTarget.isSameType(targetNested)
					&& (!isRValueReference(origReturnType) || isRValueReference(typeToConstruct))
					&& (!isRValueReference(typeToConstruct) || isRValueReference(potentialTargetOrig))) {
				return true;
			}

			if (potentialTarget instanceof ICPPClassType) {
				ICPPClassType potTargetClz = (ICPPClassType) potentialTarget;

				for (ICPPClassType potTargetBase : ClassTypeHelper.getAllBases(potTargetClz)) {
					if (potTargetBase.isSameType(targetNested)) {
						if ((isReference(typeToConstruct) || isConst(source)) && !isConstReference(source))
							return isRValueCompatible(typeToConstruct, potentialTargetOrig);
					}
				}
			}
		}
		return false;
	}

	private boolean isRValueCompatible(IType typeToConstruct2, IType potentialTargetOrig) {
		if (isRValueReference(typeToConstruct2)) {
			return isRValueReference(potentialTargetOrig);
		} else {
			return true;
		}
	}

	private boolean isRValueCompatible() {
		if (isRValueReference(typeToConstruct)) {
			return isRValueReference(source);
		} else {
			return !isRValueReference(source);
		}
	}

	private ICPPFunction evaluateDestructor() {
		ICPPEvaluation[] arguments = new ICPPEvaluation[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			// Value category is xvalue because declval() returns an rvalue reference.
			arguments[i] = new EvalFixed(argumentTypes[i], ValueCategory.XVALUE, IntegralValue.UNKNOWN);
		}
		EvalTypeId eval = new EvalTypeId(typeToConstruct, pointOfDefinition, false, false, arguments);

		return eval.getDestructor();
	}

	private boolean isCastconstructible(IType basicType, IType basicSource) {
		if (allInstanceOf(ICPPBasicType.class, basicType, basicSource)) {
			if (typeToConstruct instanceof IArrayType) {
				return false;
			}
			if (isReference(typeToConstruct) && !isConstReference(typeToConstruct) && !isReference(source)
					&& isRValueReference(source)) {
				return false;
			}
			if (targetNested.isSameType(INT) && isNumeric(sourceNested) && !(isVolatile(typeToConstruct))
					&& !isConstReference(typeToConstruct)) {
				return (isReference(source) == isReference(typeToConstruct)) || isRValueReference(typeToConstruct);
			} else if (isVolatile(typeToConstruct) && !isRValueReference(typeToConstruct)) {
				return false;
			}
			return fitsIntoType((ICPPBasicType) basicType, (ICPPBasicType) basicSource);
		} else {
			return false;
		}
	}

}
