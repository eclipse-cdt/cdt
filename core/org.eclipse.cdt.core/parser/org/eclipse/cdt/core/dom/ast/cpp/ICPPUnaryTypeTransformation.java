/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IType;

/**
 * A type used to represent the result of applying an unary
 * type transformation operator like __underlying_type(T).
 *
 * This representation is only used when T is dependent (and thus
 * we cannot evaluate the type transformation yet). If T is not
 * dependent, we simply use the result of evaluating the type
 * transformation.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.6
 */
public interface ICPPUnaryTypeTransformation extends IType {
	/**
	 * Identifies the type transformation operator being applied.
	 */
	public enum Operator {
		underlying_type // the integer type underlying an enumeration type
	}

	/**
	 * Returns the type transformation operator being applied.
	 */
	Operator getOperator();

	/**
	 * Returns the type to which the type transformation operator is being applied.
	 */
	IType getOperand();
}