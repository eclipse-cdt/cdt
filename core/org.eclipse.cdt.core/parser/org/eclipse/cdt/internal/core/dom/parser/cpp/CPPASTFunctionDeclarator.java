/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier.SpecifierKind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

/**
 * Represents a function declarator.
 */
public class CPPASTFunctionDeclarator extends CPPASTDeclarator implements ICPPASTFunctionDeclarator {
	public static final ICPPEvaluation NOEXCEPT_TRUE = new EvalFixed(CPPBasicType.BOOLEAN, ValueCategory.PRVALUE,
			IntegralValue.create(true));

	private ICPPASTParameterDeclaration[] parameters;
	private IASTTypeId[] typeIds = NO_EXCEPTION_SPECIFICATION;
	private ICPPASTExpression noexceptExpression;
	private IASTTypeId trailingReturnType;
	private ICPPASTVirtSpecifier[] virtSpecifiers = NO_VIRT_SPECIFIERS;

	private boolean varArgs;
	private boolean pureVirtual;
	private boolean isVolatile;
	private boolean isConst;
	private boolean isMutable;
	private RefQualifier refQualifier;

	private ICPPFunctionScope scope;

	public CPPASTFunctionDeclarator() {
	}

	public CPPASTFunctionDeclarator(IASTName name) {
		super(name);
	}

	@Override
	public CPPASTFunctionDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFunctionDeclarator copy(CopyStyle style) {
		CPPASTFunctionDeclarator copy = new CPPASTFunctionDeclarator();
		copy.varArgs = varArgs;
		copy.pureVirtual = pureVirtual;
		copy.isVolatile = isVolatile;
		copy.isConst = isConst;
		copy.isMutable = isMutable;
		copy.refQualifier = refQualifier;

		for (IASTParameterDeclaration param : getParameters()) {
			copy.addParameterDeclaration(param == null ? null : param.copy(style));
		}
		for (IASTTypeId typeId : getExceptionSpecification()) {
			copy.addExceptionSpecificationTypeId(typeId == null ? null : typeId.copy(style));
		}
		if (noexceptExpression != null) {
			copy.setNoexceptExpression(noexceptExpression == NOEXCEPT_DEFAULT ? noexceptExpression
					: (ICPPASTExpression) noexceptExpression.copy(style));
		}
		if (trailingReturnType != null) {
			copy.setTrailingReturnType(trailingReturnType.copy(style));
		}
		for (ICPPASTVirtSpecifier virtSpecifier : getVirtSpecifiers()) {
			copy.addVirtSpecifier(virtSpecifier.copy(style));
		}
		return copy(copy, style);
	}

	@Override
	public ICPPASTParameterDeclaration[] getParameters() {
		if (parameters == null)
			return ICPPASTParameterDeclaration.EMPTY_CPPPARAMETERDECLARATION_ARRAY;

		return parameters = ArrayUtil.trim(parameters);
	}

	@Override
	public void addParameterDeclaration(IASTParameterDeclaration parameter) {
		assertNotFrozen();
		if (parameter != null) {
			parameter.setParent(this);
			parameter.setPropertyInParent(FUNCTION_PARAMETER);
			parameters = ArrayUtil.append(ICPPASTParameterDeclaration.class, parameters,
					(ICPPASTParameterDeclaration) parameter);
		}
	}

	@Override
	public boolean takesVarArgs() {
		return varArgs;
	}

	@Override
	public void setVarArgs(boolean value) {
		assertNotFrozen();
		varArgs = value;
	}

	@Override
	public boolean isConst() {
		return isConst;
	}

	@Override
	public void setConst(boolean value) {
		assertNotFrozen();
		this.isConst = value;
	}

	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public void setVolatile(boolean value) {
		assertNotFrozen();
		this.isVolatile = value;
	}

	@Override
	public boolean isMutable() {
		return isMutable;
	}

	@Override
	public void setMutable(boolean value) {
		assertNotFrozen();
		this.isMutable = value;
	}

	@Override
	public IASTTypeId[] getExceptionSpecification() {
		return typeIds = ArrayUtil.trim(typeIds);
	}

	@Override
	public void setEmptyExceptionSpecification() {
		assertNotFrozen();
		typeIds = IASTTypeId.EMPTY_TYPEID_ARRAY;
	}

	@Override
	public void addExceptionSpecificationTypeId(IASTTypeId typeId) {
		assertNotFrozen();
		if (typeId != null) {
			assert typeIds != null;
			typeIds = ArrayUtil.append(typeIds, typeId);
			typeId.setParent(this);
			typeId.setPropertyInParent(EXCEPTION_TYPEID);
		}
	}

	@Override
	public ICPPASTExpression getNoexceptExpression() {
		return noexceptExpression;
	}

	@Override
	public void setNoexceptExpression(ICPPASTExpression expression) {
		assertNotFrozen();
		noexceptExpression = expression;
		if (expression != null && expression != NOEXCEPT_DEFAULT) {
			expression.setParent(this);
			expression.setPropertyInParent(NOEXCEPT_EXPRESSION);
		}
	}

	@Override
	public IASTTypeId getTrailingReturnType() {
		return trailingReturnType;
	}

	@Override
	public void setTrailingReturnType(IASTTypeId typeId) {
		assertNotFrozen();
		trailingReturnType = typeId;
		if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TRAILING_RETURN_TYPE);
		}
	}

	@Override
	public boolean isPureVirtual() {
		return pureVirtual;
	}

	@Override
	public void setPureVirtual(boolean isPureVirtual) {
		assertNotFrozen();
		this.pureVirtual = isPureVirtual;
	}

	@Override
	public RefQualifier getRefQualifier() {
		return refQualifier;
	}

	@Override
	public void setRefQualifier(RefQualifier value) {
		assertNotFrozen();
		refQualifier = value;
	}

	@Override
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer[] getConstructorChain() {
		if (ASTQueries.findTypeRelevantDeclarator(this) == this) {
			IASTNode parent = getParent();
			while (!(parent instanceof IASTDeclaration)) {
				if (parent == null)
					break;
				parent = parent.getParent();
			}
			if (parent instanceof ICPPASTFunctionDefinition) {
				return ((ICPPASTFunctionDefinition) parent).getMemberInitializers();
			}
		}
		return org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer.EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY;
	}

	@Override
	@Deprecated
	public void addConstructorToChain(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer initializer) {
		assertNotFrozen();
	}

	@Override
	public ICPPFunctionScope getFunctionScope() {
		if (scope != null)
			return scope;

		// introduce a scope for function declarations and definitions, only.
		IASTNode node = getParent();
		while (!(node instanceof IASTDeclaration)) {
			if (node == null)
				return null;
			node = node.getParent();
		}
		if (node instanceof IASTParameterDeclaration)
			return null;

		if (ASTQueries.findTypeRelevantDeclarator(this) == this) {
			scope = new CPPFunctionScope(this);
		}
		return scope;
	}

	@Override
	protected boolean postAccept(ASTVisitor action) {
		IASTParameterDeclaration[] params = getParameters();
		for (int i = 0; i < params.length; i++) {
			if (!params[i].accept(action))
				return false;
		}

		IASTTypeId[] ids = getExceptionSpecification();
		for (IASTTypeId id : ids) {
			if (!id.accept(action)) {
				return false;
			}
		}

		if (noexceptExpression != null && noexceptExpression != NOEXCEPT_DEFAULT) {
			if (!noexceptExpression.accept(action))
				return false;
		}

		if (trailingReturnType != null && !trailingReturnType.accept(action))
			return false;

		ICPPASTVirtSpecifier[] virtSpecifiers = getVirtSpecifiers();
		for (ICPPASTVirtSpecifier virtSpecifier : virtSpecifiers) {
			if (!virtSpecifier.accept(action)) {
				return false;
			}
		}

		return super.postAccept(action);
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (parameters != null) {
			for (int i = 0; i < parameters.length; ++i) {
				if (child == parameters[i]) {
					other.setPropertyInParent(child.getPropertyInParent());
					other.setParent(child.getParent());
					parameters[i] = (ICPPASTParameterDeclaration) other;
					return;
				}
			}
		}
		if (child == noexceptExpression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			noexceptExpression = (ICPPASTExpression) other;
			return;
		}
		assert false;
	}

	@Override
	public boolean isOverride() {
		for (ICPPASTVirtSpecifier virtSpecifier : getVirtSpecifiers()) {
			if (virtSpecifier.getKind() == SpecifierKind.Override) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setOverride(boolean value) {
		assertNotFrozen();
		// Do nothing here. Use addVirtSpecifier() instead.
	}

	@Override
	public boolean isFinal() {
		for (ICPPASTVirtSpecifier virtSpecifier : getVirtSpecifiers()) {
			if (virtSpecifier.getKind() == SpecifierKind.Final) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setFinal(boolean value) {
		assertNotFrozen();
		// Do nothing here. Use addVirtSpecifier() instead.
	}

	@Override
	public ICPPASTVirtSpecifier[] getVirtSpecifiers() {
		return virtSpecifiers = ArrayUtil.trim(virtSpecifiers);
	}

	@Override
	public void addVirtSpecifier(ICPPASTVirtSpecifier virtSpecifier) {
		assertNotFrozen();
		if (virtSpecifier != null) {
			assert virtSpecifiers != null;
			virtSpecifiers = ArrayUtil.append(virtSpecifiers, virtSpecifier);
			virtSpecifier.setParent(this);
			virtSpecifier.setPropertyInParent(VIRT_SPECIFIER);
		}
	}

	@Override
	public void setVirtSpecifiers(ICPPASTVirtSpecifier[] newVirtSpecifiers) {
		assertNotFrozen();
		if (newVirtSpecifiers == null) {
			virtSpecifiers = NO_VIRT_SPECIFIERS;
		} else {
			virtSpecifiers = newVirtSpecifiers;
			for (ICPPASTVirtSpecifier virtSpecifier : newVirtSpecifiers) {
				virtSpecifier.setParent(this);
				virtSpecifier.setPropertyInParent(VIRT_SPECIFIER);
			}
		}
	}

	@Override
	public ICPPEvaluation getNoexceptEvaluation() {
		if (getNoexceptExpression() != null) {
			return getNoexceptExpression().getEvaluation();
		} else if (getExceptionSpecification() == IASTTypeId.EMPTY_TYPEID_ARRAY) {
			return NOEXCEPT_TRUE;
		}
		return null;
	}
}
