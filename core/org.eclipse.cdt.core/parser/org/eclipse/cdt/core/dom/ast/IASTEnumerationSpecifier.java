/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents enumerations in C and C++.
 * 
 * @author jcamelon
 */
public interface IASTEnumerationSpecifier extends IASTDeclSpecifier, IASTNameOwner {

	/**
	 * This interface represents an enumerator member of an enum specifier.
	 * 
	 * @author jcamelon
	 */
	public interface IASTEnumerator extends IASTNode, IASTNameOwner {
		/**
		 * Empty array (constant).
		 */
		public static final IASTEnumerator[] EMPTY_ENUMERATOR_ARRAY = new IASTEnumerator[0];

		/**
		 * <code>ENUMERATOR_NAME</code> describes the relationship between
		 * <code>IASTEnumerator</code> and <code>IASTName</code>.
		 */
		public static final ASTNodeProperty ENUMERATOR_NAME = new ASTNodeProperty(
				"Enumerator Name"); //$NON-NLS-1$

		/**
		 * Set the enumerator's name.
		 * 
		 * @param name
		 */
		public void setName(IASTName name);

		/**
		 * Get the enumerator's name.
		 * 
		 * @return <code>IASTName</code>
		 */
		public IASTName getName();

		/**
		 * <code>ENUMERATOR_VALUE</code> describes the relationship between
		 * <code>IASTEnumerator</code> and <code>IASTExpression</code>.
		 */
		public static final ASTNodeProperty ENUMERATOR_VALUE = new ASTNodeProperty(
				"Enumerator Value"); //$NON-NLS-1$

		/**
		 * Set enumerator value.
		 * 
		 * @param expression
		 */
		public void setValue(IASTExpression expression);

		/**
		 * Get enumerator value.
		 * 
		 * @return <code>IASTExpression</code> value
		 */
		public IASTExpression getValue();

	}

	/**
	 * <code>ENUMERATOR</code> describes the relationship between
	 * <code>IASTEnumerationSpecifier</code> and the nested
	 * <code>IASTEnumerator</code>s.
	 */
	public static final ASTNodeProperty ENUMERATOR = new ASTNodeProperty(
			"Enumerator"); //$NON-NLS-1$

	/**
	 * Add an enumerator.
	 * 
	 * @param enumerator
	 *            <code>IASTEnumerator</code>
	 */
	public void addEnumerator(IASTEnumerator enumerator);

	/**
	 * Get enumerators.
	 * 
	 * @return <code>IASTEnumerator []</code> array
	 */
	public IASTEnumerator[] getEnumerators();

	/**
	 * <code>ENUMERATION_NAME</code> describes the relationship between
	 * <code>IASTEnumerationSpecifier</code> and its <code>IASTName</code>.
	 */
	public static final ASTNodeProperty ENUMERATION_NAME = new ASTNodeProperty(
			"Enum Name"); //$NON-NLS-1$

	/**
	 * Set the enum's name.
	 * 
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * Get the enum's name.
	 * 
	 * @return
	 */
	public IASTName getName();

}
