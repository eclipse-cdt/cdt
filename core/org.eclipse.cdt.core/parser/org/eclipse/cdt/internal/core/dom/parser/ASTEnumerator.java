/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Base class for C and C++ enumerators.
 */
public abstract class ASTEnumerator extends ASTAttributeOwner implements IASTEnumerator, IASTAmbiguityParent {
	private IASTName name;
	private IASTExpression value;
	private IValue integralValue;

	public ASTEnumerator() {
	}

	public ASTEnumerator(IASTName name, IASTExpression value) {
		setName(name);
		setValue(value);
	}

	protected <T extends ASTEnumerator> T copy(T copy, CopyStyle style) {
		copy.setName(name == null ? null : name.copy(style));
		copy.setValue(value == null ? null : value.copy(style));
		return super.copy(copy, style);
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATOR_NAME);
		}
	}

	@Override
	public IASTName getName() {
		return name;
	}

	@Override
	public void setValue(IASTExpression expression) {
		assertNotFrozen();
		this.value = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(ENUMERATOR_VALUE);
		}
	}

	@Override
	public IASTExpression getValue() {
		return value;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitEnumerators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (name != null && !name.accept(action))
			return false;
		if (!acceptByAttributeSpecifiers(action))
			return false;
		if (value != null && !value.accept(action))
			return false;
		if (action.shouldVisitEnumerators) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (n == name)
			return r_definition;

		return r_reference;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == value) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			value = (IASTExpression) other;
		}
	}

	public IValue getIntegralValue() {
		if (integralValue == null) {
			IASTNode parent = getParent();
			if (parent instanceof IASTInternalEnumerationSpecifier) {
				IASTInternalEnumerationSpecifier enumeration = (IASTInternalEnumerationSpecifier) parent;
				if (enumeration.startValueComputation()) { // Prevent infinite recursion.
					computeEnumValues(enumeration);
				}
			}
			if (integralValue == null) {
				integralValue = IntegralValue.UNKNOWN;
			}
		}
		return integralValue;
	}

	private static void computeEnumValues(IASTInternalEnumerationSpecifier enumeration) {
		try {
			IType fixedType = null;
			if (enumeration instanceof ICPPASTEnumerationSpecifier) {
				IBinding binding = enumeration.getName().resolveBinding();
				if (binding instanceof ICPPEnumeration) {
					fixedType = ((ICPPEnumeration) binding).getFixedType();
				}
			}
			IType type = fixedType == null ? CPPBasicType.INT : null;
			IValue previousExplicitValue = null;
			int delta = 0;
			IASTEnumerator[] etors = enumeration.getEnumerators();
			for (IASTEnumerator etor : etors) {
				IBinding etorBinding = etor.getName().resolveBinding();
				IValue val;
				IASTExpression expr = etor.getValue();
				if (expr != null) {
					val = ValueFactory.create(expr);
					previousExplicitValue = val;
					delta = 1;
					if (fixedType == null) {
						type = expr.getExpressionType();
						type = SemanticUtil.getNestedType(type, SemanticUtil.CVTYPE | SemanticUtil.TDEF);
						if (etorBinding instanceof CPPEnumerator) {
							((CPPEnumerator) etorBinding).setInternalType(type);
						}
					}
				} else {
					if (previousExplicitValue != null) {
						val = IntegralValue.incrementedValue(previousExplicitValue, delta);
					} else {
						val = IntegralValue.create(delta);
					}
					delta++;
					if (fixedType == null && type instanceof IBasicType) {
						type = getTypeOfIncrementedValue((IBasicType) type, val);
						if (etorBinding instanceof CPPEnumerator) {
							((CPPEnumerator) etorBinding).setInternalType(type);
						}
					}
				}
				if (etor instanceof ASTEnumerator) {
					((ASTEnumerator) etor).integralValue = val;
				}
			}
		} finally {
			enumeration.finishValueComputation();
		}
	}

	/**
	 * [dcl.enum] 7.2-5:
	 * "... the type of the initializing value is the same as the type of the initializing value of
	 * the preceding enumerator unless the incremented value is not representable in that type, in
	 * which case the type is an unspecified integral type sufficient to contain the incremented
	 * value. If no such type exists, the program is ill-formed."
	 *
	 * @param type the type of the previous value
	 * @param val the incremented value
	 * @return the type of the incremented value
	 */
	public static IBasicType getTypeOfIncrementedValue(IBasicType type, IValue val) {
		Number numericalValue = val.numberValue();
		if (numericalValue != null) {
			long longValue = numericalValue.longValue();
			if ((type.getKind() != Kind.eInt && type.getKind() != Kind.eInt128) || type.isShort()) {
				type = type.isUnsigned() ? CPPBasicType.UNSIGNED_INT : CPPBasicType.INT;
			}
			if (!ArithmeticConversion.fitsIntoType(type, longValue)) {
				if (!type.isUnsigned()) {
					if (type.getKind() != Kind.eInt128) {
						if (type.isLongLong()) {
							type = CPPBasicType.UNSIGNED_INT128;
						} else if (type.isLong()) {
							type = CPPBasicType.UNSIGNED_LONG_LONG;
						} else {
							type = CPPBasicType.UNSIGNED_LONG;
						}
					}
				} else {
					if (type.getKind() == Kind.eInt128) {
						if (longValue >= 0) {
							type = CPPBasicType.UNSIGNED_INT128;
						}
					} else {
						if (type.isLongLong()) {
							type = CPPBasicType.INT128;
						} else if (type.isLong()) {
							type = CPPBasicType.LONG_LONG;
						} else {
							type = CPPBasicType.LONG;
						}
					}
				}
			}
		}
		return type;
	}
}
