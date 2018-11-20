/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents enumerations in C and C++.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTEnumerationSpecifier extends IASTDeclSpecifier, IASTNameOwner {
	/**
	 * This interface represents an enumerator member of an enum specifier.
	 *
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public interface IASTEnumerator extends IASTNameOwner, IASTAttributeOwner {
		/**
		 * Empty array (constant).
		 */
		public static final IASTEnumerator[] EMPTY_ENUMERATOR_ARRAY = {};

		/**
		 * {@code ENUMERATOR_NAME} describes the relationship between
		 * {@code IASTEnumerator} and {@code IASTName}.
		 */
		public static final ASTNodeProperty ENUMERATOR_NAME = new ASTNodeProperty(
				"IASTEnumerator.ENUMERATOR_NAME - IASTName for IASTEnumerator"); //$NON-NLS-1$

		/**
		 * {@code ENUMERATOR_VALUE} describes the relationship between
		 * {@code IASTEnumerator} and {@code IASTExpression}.
		 */
		public static final ASTNodeProperty ENUMERATOR_VALUE = new ASTNodeProperty(
				"IASTEnumerator.ENUMERATOR_VALUE - IASTExpression (value) for IASTEnumerator"); //$NON-NLS-1$

		/**
		 * Set the enumerator's name.
		 *
		 * @param name
		 */
		public void setName(IASTName name);

		/**
		 * Get the enumerator's name.
		 *
		 * @return {@code IASTName}
		 */
		public IASTName getName();

		/**
		 * Sets enumerator value.
		 *
		 * @param expression
		 */
		public void setValue(IASTExpression expression);

		/**
		 * Returns enumerator value.
		 *
		 * @return {@code IASTExpression} value
		 */
		public IASTExpression getValue();

		/**
		 * @since 5.1
		 */
		@Override
		public IASTEnumerator copy();

		/**
		 * @since 5.3
		 */
		@Override
		public IASTEnumerator copy(CopyStyle style);
	}

	/**
	 * {@code ENUMERATION_NAME} describes the relationship between
	 * {@code IASTEnumerationSpecifier} and its {@link IASTName}.
	 */
	public static final ASTNodeProperty ENUMERATION_NAME = new ASTNodeProperty(
			"IASTEnumerationSpecifier.ENUMERATION_NAME - IASTName for IASTEnumerationSpecifier"); //$NON-NLS-1$

	/**
	 * {@code ENUMERATOR} describes the relationship between
	 * {@code IASTEnumerationSpecifier} and the nested
	 * {@link IASTEnumerator}s.
	 */
	public static final ASTNodeProperty ENUMERATOR = new ASTNodeProperty(
			"IASTEnumerationSpecifier.ENUMERATOR - nested IASTEnumerator for IASTEnumerationSpecifier"); //$NON-NLS-1$

	/**
	 * Adds an enumerator.
	 *
	 * @param enumerator {@code IASTEnumerator}
	 */
	public void addEnumerator(IASTEnumerator enumerator);

	/**
	 * Returns enumerators.
	 *
	 * @return {@code IASTEnumerator[]} array
	 */
	public IASTEnumerator[] getEnumerators();

	/**
	 * Sets the enum's name.
	 *
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * Returns the enum's name.
	 */
	public IASTName getName();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTEnumerationSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTEnumerationSpecifier copy(CopyStyle style);
}
