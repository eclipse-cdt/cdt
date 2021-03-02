package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;

public class IsAssignableEvaluator {
	private IType front;
	private IType frontInner;
	private IType behind;
	private IType behindInner;

	public IsAssignableEvaluator(IType front, IType behind) {
		this.front = front;
		this.frontInner = resolveInnerType(front);
		this.behind = behind;
		this.behindInner = resolveInnerType(behind);
	}

	public IntegralValue evaluate() {
		if (isClassReference(front)) {
			return evaluateClass((ICPPClassType) getChild(front));
		}
		if (isReference(front)) {
			return evaluateReference(front);
		}
		if (isConst(front)) {
			return evaluateDelCopyAssign(front);
		}
		if (isClass(front)) {
			return evaluateClass((ICPPClassType) front);
		}
		return IntegralValue.create(false);
	}

	private IntegralValue evaluateClass(ICPPClassType toCheck) {
		IType toCheckOut = toCheck;
		if (isAnyAssign(toCheck)) {
			if (isFunction(behind) && (isRValueReference(behind) && isVolatile(behind)
					|| (isFktReference(behind) || isConst(behind)) && isVoid(behindInner))) {
				return IntegralValue.create(false);
			} else if (isFunction(behind)) {
				return IntegralValue.create(true);
			} else if (isReference(behind) && isVoid(behindInner)) {
				return IntegralValue.create(true);
			} else if (isPtr(behind) && isVoid(behindInner)) {
				return IntegralValue.create(true);
			} else {
				return IntegralValue.create(!isVoid(behindInner));
			}
		}

		if (isDelCopyAssign(toCheck)) {
			return evaluateDelCopyAssign(toCheck);
		}

		if (isUnion(toCheck)) {
			if (isNonTrivial(toCheck)) {
				return IntegralValue.create(false);
			}
		}

		if (frontInner.isSameType(behindInner) && isReference(behind)) {
			if (hasCopyConstructor(toCheck)) {
				return IntegralValue.create(true);
			} else {
				return IntegralValue.create(false);
			}
		}

		if (isEllipsisConstructor(toCheck)) {
			if (!isVoid(behind)) {
				return IntegralValue.create(true);
			} else {
				return IntegralValue.create(false);
			}
		}

		if (isTemplatedImplicidFromConversion(toCheck)) {
			return IntegralValue.create(true);
		}

		if (isRValueReference(behind)) {
			return IntegralValue.create(false);
		}

		if (!isReference(toCheck)) {
			toCheckOut = new CPPReferenceType(toCheck, true);
		}
		return evaluateReference(toCheckOut);
	}

	private IntegralValue evaluateReference(IType toCheck) {
		if (isArrayReference(toCheck)) {
			return evaluateArray(toCheck);
		}
		if (isConstReference(toCheck)) {
			return IntegralValue.create(false);
		}

		if (isPtr(getChild(toCheck))) {
			return evaluatePtr(getChild(toCheck));
		}

		if (getChild(resolveInnerType(toCheck)).isSameType(CPPBasicType.VOID)) {
			return IntegralValue.create(isNull(behind) && !isReference(front));
		}

		if (isImplicitTo(behindInner, frontInner)) {
			return IntegralValue.create(true);
		}

		if (isDeletedImplicitTo(behindInner, frontInner)) {
			return IntegralValue.create(false);
		}

		if (isExplicitTo(behindInner, frontInner)) {
			return IntegralValue.create(false);
		}

		return IntegralValue.create(typeMatches());
	}

	private IntegralValue evaluateDelCopyAssign(IType toCheck) {
		if (typeMatches() && isReference(front) && !isRValueReference(front) && !isReference(behind)
				&& !isConst(behind)) {
			return IntegralValue.create(true);
		}
		return IntegralValue.create(false);
	}

	private IntegralValue evaluatePtr(IType child) {
		if (isInt(frontInner) && isNull(behindInner)) {
			return IntegralValue.create(true);
		}
		if (child instanceof IPointerType && ((IPointerType) child).isConst()) {
			return IntegralValue.create(false);
		}

		if (isImplicitTo(behindInner, child)) {
			return IntegralValue.create(true);
		}
		return IntegralValue.create(typeMatches());
	}

	private IntegralValue evaluateArray(IType toCheck) {
		if (!isReference(behindInner)) {
			return IntegralValue.create(false);

		}
		return IntegralValue.create(typeMatches());
	}

	private IType getChild(IType parent) {
		if (parent instanceof ICPPReferenceType) {
			return ((ICPPReferenceType) parent).getType();
		} else if (parent instanceof IPointerType) {
			return ((IPointerType) parent).getType();
		} else if (parent instanceof IQualifierType) {
			return ((IQualifierType) parent).getType();
		} else if (parent instanceof ITypedef) {
			return ((ITypedef) parent).getType();
		} else if (parent instanceof ICPPFunctionType) {
			return ((ICPPFunctionType) parent).getReturnType();
		} else {
			return parent;
		}
	}

	private IType resolveInnerType(IType toResolve) {
		if (isReference(toResolve)) {
			return resolveInnerType(((ICPPReferenceType) toResolve).getType());
		} else if (toResolve instanceof IPointerType) {
			return resolveInnerType(((IPointerType) toResolve).getType());
		} else if (toResolve instanceof IQualifierType) {
			return resolveInnerType(((IQualifierType) toResolve).getType());
		} else if (toResolve instanceof ICPPFunctionType) {
			return resolveInnerType(((ICPPFunctionType) toResolve).getReturnType());
		} else if (toResolve instanceof CPPTypedef) {
			return resolveInnerType(((CPPTypedef) toResolve).getType());
		} else {
			return toResolve;
		}
	}

	private boolean typeMatches() {
		if ((isBool(frontInner) || isInt(frontInner)) && isEnum(behindInner)) {
			return !isClassEnum(behindInner);
		}
		if (isBool(frontInner) && isPtr(behind) && isVoid(behindInner)) {
			return true;
		}
		if (isBool(frontInner) && isInt(behindInner)) {
			return true;
		}
		if (isVoid(frontInner) && isNull(behindInner)) {
			return true;
		}
		if (isSuper(behindInner, frontInner)) {
			return true;
		}
		if (isInt(frontInner) && isDouble(behindInner)) {
			return true;
		}
		return frontInner.isSameType(behindInner);
	}

	private boolean isEnum(IType toCheck) {
		return toCheck instanceof ICPPEnumeration;
	}

	private boolean isClassEnum(IType toCheck) {
		if (isEnum(toCheck)) {
			return ((ICPPEnumeration) toCheck).isScoped();
		}

		return false;
	}

	private boolean isBool(IType toCheck) {
		return toCheck.isSameType(CPPBasicType.BOOLEAN);
	}

	private boolean isVoid(IType toCheck) {
		return toCheck.isSameType(CPPBasicType.VOID);
	}

	private boolean isInt(IType toCheck) {
		return toCheck.isSameType(CPPBasicType.INT);
	}

	private boolean isDouble(IType check) {
		return check.isSameType(CPPBasicType.DOUBLE);
	}

	private boolean isNull(IType toCheck) {
		return resolveInnerType(toCheck).isSameType(CPPBasicType.NULL_PTR);
	}

	private boolean isReference(IType toCheck) {
		return toCheck instanceof ICPPReferenceType;
	}

	private boolean isConstReference(IType toCheck) {
		if (isReference(toCheck)) {
			return isConst(((ICPPReferenceType) toCheck).getType());
		}
		return false;
	}

	private boolean isConst(IType toCheck) {
		if (toCheck instanceof IQualifierType) {
			return ((IQualifierType) toCheck).isConst();
		} else if (toCheck instanceof ICPPFunctionType) {
			return ((ICPPFunctionType) toCheck).isConst();
		}

		return false;
	}

	private boolean isClass(IType type) {
		return type instanceof ICPPClassType;
	}

	private boolean isClassReference(IType toCheck) {
		if (isReference(toCheck)) {
			return ((ICPPReferenceType) toCheck).getType() instanceof ICPPClassType;
		}
		return false;
	}

	private boolean isFktReference(IType toCheck) {
		return isFunction(toCheck) && ((ICPPFunctionType) toCheck).hasRefQualifier();
	}

	private boolean isVolatile(IType check) {
		if (check instanceof ICPPFunctionType) {
			return ((ICPPFunctionType) check).isVolatile();
		}
		return false;
	}

	private boolean isFunction(IType toCheck) {
		return toCheck instanceof ICPPFunctionType;
	}

	private boolean isArrayReference(IType toCheck) {
		if (isReference(toCheck)) {
			return ((ICPPReferenceType) toCheck).getType().getClass().isAssignableFrom(CPPArrayType.class);
		}
		return false;
	}

	private boolean isPtr(IType toCheck) {
		return toCheck instanceof IPointerType;
	}

	private boolean isRValueReference(IType toCheck) {
		if (isReference(toCheck)) {
			return ((ICPPReferenceType) toCheck).isRValueReference();
		} else if (isFunction(toCheck)) {
			return ((ICPPFunctionType) toCheck).isRValueReference();
		}
		return false;
	}

	private boolean isSuper(IType clazz, IType superClazz) {
		if (superClazz instanceof ICPPClassType && clazz instanceof ICPPClassType) {
			for (ICPPBase base : ((ICPPClassType) clazz).getBases()) {
				if (base.getBaseClass() != null) {
					if (base.getBaseClassType().isSameType(superClazz)) {
						return true;
					} else {
						return isSuper(base.getBaseClassType(), superClazz);
					}
				}
			}
		}
		return false;
	}

	private boolean isMoveAssignment(ICPPMethod m, ICPPClassType toCheck) {
		if (isEqualsOperator(m)) {
			if (m.getParameters().length == 1) {
				ICPPParameter mp = m.getParameters()[0];
				IType pt = mp.getType();

				if (isReference(pt) && isRValueReference(toCheck) && isSameAsSecondInnerType(toCheck, pt)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isCopyAssignment(ICPPMethod m, ICPPClassType toCheck) {
		if (isEqualsOperator(m)) {
			if (m.getParameters().length == 1) {
				ICPPParameter mp = m.getParameters()[0];
				IType pt = mp.getType();

				if (isReference(pt) && !isRValueReference(toCheck) && isSameAsSecondInnerType(toCheck, pt)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isCopyConstructor(ICPPConstructor c, ICPPClassType toCheck) {
		if (c.getParameters().length == 1) {
			ICPPParameter p = c.getParameters()[0];
			IType pt = p.getType();

			if (isReference(pt) && !isRValueReference(toCheck) && isSameAsSecondInnerType(toCheck, pt)) {
				return true;
			}
		}
		return false;
	}

	private boolean isEllipsisConstructor(ICPPClassType toCheck) {
		for (ICPPConstructor c : toCheck.getConstructors()) {
			if (isEllipsisContructor(c)) {
				return true;
			}
		}

		return false;
	}

	private boolean isEllipsisContructor(ICPPConstructor c) {
		if (c.getType().takesVarArgs()) {
			if (c.getType().toString().contains(new String(Keywords.cpELLIPSIS))) {
				return !c.isDeleted();
			}
		}

		return false;
	}

	private boolean isTemplatedImplicidFromConversion(ICPPClassType toCheck) {
		if (toCheck instanceof CPPClassInstance) {
			CPPClassInstance template = (CPPClassInstance) toCheck;

			for (ICPPConstructor c : toCheck.getConstructors()) {
				if (c.getParameters().length == 1) {
					ICPPParameter param = c.getParameters()[0];

					if (param.getType().isSameType(behindInner)) {
						for (ICPPTemplateArgument ta : template.getTemplateArguments()) {
							if (ta.getTypeValue().isSameType(behindInner)) {
								return !c.isDeleted();
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isNonTrivial(ICPPClassType toCheck) {
		//TODO: explicit non-trivial copy operators/constructors can be defined in the union explicitly.
		for (ICPPField member : toCheck.getDeclaredFields()) {

			if (isClass(member.getType())) {
				ICPPClassType chkMbr = (ICPPClassType) member.getType();

				for (ICPPConstructor c : chkMbr.getConstructors()) {
					if (isCopyConstructor(c, chkMbr)) {
						return true;
					}

					if (isMoveConstructor(c, chkMbr)) {
						return true;
					}
				}

				for (ICPPMethod m : chkMbr.getDeclaredMethods()) {

					if (isCopyAssignment(m, chkMbr)) {
						return true;
					}

					if (isMoveAssignment(m, chkMbr)) {
						return true;
					}

					if (m.isDestructor()) {
						return true;
					}
				}
			}

		}
		return false;
	}

	private boolean isEqualsOperator(ICPPMethod m) {
		return m.getName().startsWith("operator ="); //$NON-NLS-1$
	}

	private boolean isBracketOperator(ICPPMethod mtd) {
		return mtd.toString().startsWith("operator =("); //$NON-NLS-1$
	}

	private boolean isToOperation(ICPPMethod mtd) {
		return mtd.getName().equals("operator #0"); //$NON-NLS-1$
	}

	private boolean isSameAsSecondInnerType(IType toCheck, IType pt) {
		return resolveInnerType(pt).isSameType(resolveInnerType(toCheck));
	}

	private boolean isMoveConstructor(ICPPConstructor c, ICPPClassType toCheck) {
		if (c.getParameters().length == 1) {
			ICPPParameter p = c.getParameters()[0];
			IType pt = p.getType();

			if (isRValueReference(pt) && resolveInnerType(pt).isSameType(resolveInnerType(toCheck))) {
				return true;
			}
		}
		return false;
	}

	private boolean isUnion(ICPPClassType toCheck) {
		if (toCheck instanceof CPPClassType) {
			IASTNode parent = ((CPPClassType) toCheck).getDefinition().getParent();

			if (parent instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier compositeTypeSpecifier = (ICPPASTCompositeTypeSpecifier) parent;
				int key = compositeTypeSpecifier.getKey();
				return key == IASTCompositeTypeSpecifier.k_union;
			}
		}
		return false;
	}

	private boolean hasCopyConstructor(ICPPClassType toCheck) {
		for (ICPPConstructor constructor : toCheck.getConstructors()) {
			if (isCopyConstructor(constructor, toCheck)) {
				ICPPParameter p2c = constructor.getParameters()[0];
				if (isRValueReference(p2c.getType()) && (!isRValueReference(behind) || isConst(getChild(behind)))) {
					return false;
				}
				return true;
			}
		}

		return false;
	}

	private boolean isDelCopyAssign(ICPPClassType toCheck) {
		if (isClass(resolveInnerType(toCheck))) {
			ICPPClassType toTypeClassTp = (ICPPClassType) resolveInnerType(toCheck);

			for (ICPPMethod mtd : toTypeClassTp.getAllDeclaredMethods()) {
				if (mtd.getParameters().length == 1) {
					if (isBracketOperator(mtd)) {
						IType paramType = mtd.getParameters()[0].getType();

						if (isConstReference(paramType) && resolveInnerType(paramType).isSameType(behindInner)) {
							return mtd.isDeleted();
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isAnyAssign(IType toCheck) {
		if (isClass(resolveInnerType(toCheck))) {
			ICPPClassType toTypeClassTp = (ICPPClassType) resolveInnerType(toCheck);
			for (ICPPMethod mtd : toTypeClassTp.getAllDeclaredMethods()) {
				if (isEqualsOperator(mtd)) {
					IType paramType = mtd.getParameters()[0].getType();

					if (isReference(paramType)) {
						if (mtd instanceof ICPPFunctionTemplate) {
							CPPMethodTemplate mtdt = (CPPMethodTemplate) mtd;

							for (ICPPTemplateParameter templateArgument : mtdt.getTemplateParameters()) {
								if (templateArgument instanceof IType) {
									if (((ICPPReferenceType) paramType).getType()
											.isSameType((IType) templateArgument)) {
										return !mtd.isDeleted();
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean isImplicitTo(IType to, IType from) {
		if (isClass(resolveInnerType(to))) {
			ICPPClassType toTypeClassTp = (ICPPClassType) resolveInnerType(to);

			for (ICPPMethod mtd : toTypeClassTp.getAllDeclaredMethods()) {
				if (mtd.getParameters().length == 0) {
					if (isToOperation(mtd) && mtd.getType().getReturnType().isSameType(from)) {
						return !mtd.isExplicit() && !mtd.isDeleted();
					}
				}
			}
		}
		return false;
	}

	private boolean isDeletedImplicitTo(IType to, IType from) {
		if (isClass(resolveInnerType(to))) {
			ICPPClassType toTypeClassTp = (ICPPClassType) resolveInnerType(to);

			for (ICPPMethod mtd : toTypeClassTp.getAllDeclaredMethods()) {
				if (mtd.getParameters().length == 0) {
					if (isToOperation(mtd) && mtd.getType().getReturnType().isSameType(from)) {
						return !mtd.isExplicit() && mtd.isDeleted();
					}
				}
			}
		}
		return false;
	}

	private boolean isExplicitTo(IType to, IType from) {
		if (isClass(resolveInnerType(to))) {
			ICPPClassType toTypeClassTp = (ICPPClassType) resolveInnerType(to);

			for (ICPPMethod mtd : toTypeClassTp.getAllDeclaredMethods()) {
				if (mtd.getParameters().length == 0) {
					if (isToOperation(mtd) && mtd.getType().getReturnType().isSameType(from)) {
						return mtd.isExplicit() && !mtd.isDeleted();
					}
				}
			}
		}
		return false;
	}
}
