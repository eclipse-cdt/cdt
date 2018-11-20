/*******************************************************************************
 * Copyright (c) 2006, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Method.
 */
class PDOMCPPMethod extends PDOMCPPFunction implements ICPPMethod {
	/** Offset of remaining annotation information (relative to the beginning of the record). */
	private static final int METHOD_ANNOTATION = PDOMCPPFunction.RECORD_SIZE; // byte
	/** The size in bytes of a PDOMCPPMethod record in the database. */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunction.RECORD_SIZE + 1;

	private byte methodAnnotation = -1;

	public PDOMCPPMethod(PDOMCPPLinkage linkage, PDOMNode parent, ICPPMethod method)
			throws CoreException, DOMException {
		super(linkage, parent, method, true);
		methodAnnotation = PDOMCPPAnnotations.encodeExtraMethodAnnotations(method);
		getDB().putByte(record + METHOD_ANNOTATION, methodAnnotation);
	}

	public PDOMCPPMethod(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public final void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPMethod) {
			ICPPMethod method = (ICPPMethod) newBinding;
			super.update(linkage, newBinding);
			methodAnnotation = -1;
			byte annot = PDOMCPPAnnotations.encodeExtraMethodAnnotations(method);
			getDB().putByte(record + METHOD_ANNOTATION, annot);
			methodAnnotation = annot;
		} else if (newBinding == null && isImplicit()) {
			// Clear the implicit flag, such that the binding will no longer be picked up.
			byte annot = PDOMCPPAnnotations.clearImplicitMethodFlag(getMethodAnnotation());
			getDB().putByte(record + METHOD_ANNOTATION, annot);
			methodAnnotation = annot;
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPMETHOD;
	}

	@Override
	public boolean isVirtual() {
		return PDOMCPPAnnotations.isVirtualMethod(getMethodAnnotation());
	}

	private byte getMethodAnnotation() {
		if (methodAnnotation == -1)
			methodAnnotation = getByte(record + METHOD_ANNOTATION);
		return methodAnnotation;
	}

	@Override
	public boolean isPureVirtual() {
		return PDOMCPPAnnotations.isPureVirtualMethod(getMethodAnnotation());
	}

	@Override
	public boolean isDestructor() {
		return PDOMCPPAnnotations.isDestructor(getMethodAnnotation());
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isImplicit() {
		return PDOMCPPAnnotations.isImplicitMethod(getMethodAnnotation());
	}

	@Override
	public boolean isExplicit() {
		return PDOMCPPAnnotations.isExplicitMethod(getMethodAnnotation());
	}

	@Override
	public IScope getFunctionScope() {
		return null;
	}

	@Override
	public boolean isExtern() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isAuto() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public boolean isRegister() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotations.getVisibility(getAnnotations());
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		if ((standardFlags & PDOMName.IS_REFERENCE) == PDOMName.IS_REFERENCE) {
			IASTNode parent = name.getParent();
			if (parent instanceof ICPPASTQualifiedName) {
				// When taking the address of a method it will be called without suppressing
				// the virtual mechanism
				parent = parent.getParent();
				if (parent instanceof IASTIdExpression) {
					parent = parent.getParent();
					if (parent instanceof IASTUnaryExpression) {
						if (((IASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_amper)
							return PDOMName.COULD_BE_POLYMORPHIC_METHOD_CALL;
					}
				}
			} else if (parent instanceof ICPPASTFieldReference) {
				// The name is not qualified
				ICPPASTFieldReference fr = (ICPPASTFieldReference) parent;
				parent = parent.getParent();
				if (parent instanceof IASTFunctionCallExpression) {
					// v->member()
					if (fr.isPointerDereference()) {
						return PDOMName.COULD_BE_POLYMORPHIC_METHOD_CALL;
					}
					// v.member()
					IASTExpression fieldOwner = fr.getFieldOwner();
					if (fieldOwner.getValueCategory().isGLValue()) {
						while (fieldOwner instanceof IASTUnaryExpression && ((IASTUnaryExpression) fieldOwner)
								.getOperator() == IASTUnaryExpression.op_bracketedPrimary)
							fieldOwner = ((IASTUnaryExpression) fieldOwner).getOperand();
						if (fieldOwner instanceof IASTIdExpression) {
							IBinding b = ((IASTIdExpression) fieldOwner).getName().resolveBinding();
							if (b instanceof IVariable) {
								IType t = ((IVariable) b).getType();
								if (!(t instanceof ICPPReferenceType)) {
									return 0;
								}
							}
						}
						return PDOMName.COULD_BE_POLYMORPHIC_METHOD_CALL;
					}
				}
			} else if (parent instanceof IASTIdExpression) {
				// Calling a member from within a member
				if (parent.getParent() instanceof IASTFunctionCallExpression) {
					return PDOMName.COULD_BE_POLYMORPHIC_METHOD_CALL;
				}
			}
		}
		return 0;
	}

	@Override
	public IType[] getExceptionSpecification() {
		if (isImplicit()) {
			return ClassTypeHelper.getInheritedExceptionSpecification(this);
		}
		return super.getExceptionSpecification();
	}

	@Override
	public boolean isOverride() {
		return PDOMCPPAnnotations.isOverrideMethod(getMethodAnnotation());
	}

	@Override
	public boolean isFinal() {
		return PDOMCPPAnnotations.isFinalMethod(getMethodAnnotation());
	}
}
