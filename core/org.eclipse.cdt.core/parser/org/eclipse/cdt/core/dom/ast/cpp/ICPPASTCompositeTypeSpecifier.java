/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author jcamelon
 */
public interface ICPPASTCompositeTypeSpecifier extends
		IASTCompositeTypeSpecifier, ICPPASTDeclSpecifier {

	/**
	 * <code>k_class</code> C++ introduces the class concept for composite
	 * types.
	 */
	public static final int k_class = IASTCompositeTypeSpecifier.k_last + 1;

	/**
	 * <code>k_last</code> allows for subinterfaces to extend the kind type.
	 */
	public static final int k_last = k_class;

	/**
	 * <code>VISIBILITY_LABEL</code> is used to express the relationship for a
	 * visibility label "declaration".
	 */
	public static final ASTNodeProperty VISIBILITY_LABEL = new ASTNodeProperty(
			"ICPPASTCompositeTypeSpecifier.VISIBILITY_LABEL - Visibility label \"declaration\""); //$NON-NLS-1$

	/**
	 * <code>BASE_SPECIFIER</code> expresses the subclass role.
	 */
	public static final ASTNodeProperty BASE_SPECIFIER = new ASTNodeProperty(
			"ICPPASTCompositeTypeSpecifier.BASE_SPECIFIER - Expresses the subclass role"); //$NON-NLS-1$

	/**
	 * Base Specifiers are where a class expresses from whom it inherits.
	 * 
	 * @author jcamelon
	 */
	public static interface ICPPASTBaseSpecifier extends IASTNode, IASTNameOwner  {
		/**
		 * Constant.
		 */
		public static final ICPPASTBaseSpecifier[] EMPTY_BASESPECIFIER_ARRAY = new ICPPASTBaseSpecifier[0];

		/**
		 * Is the keyword virtual used?
		 * 
		 * @return boolean
		 */
		public boolean isVirtual();

		/**
		 * Set the virtual flag on/off.
		 * 
		 * @param value
		 *            boolean
		 */
		public void setVirtual(boolean value);

		/**
		 * <code>v_public</code> was public keyword used in describing this
		 * base class?
		 */
		public static final int v_public = 1;

		/**
		 * <code>v_protected</code> was protected keyword used in describing
		 * this base class?
		 */
		public static final int v_protected = 2;

		/**
		 * <code>v_private</code> was private keyword used in describing this
		 * base class?
		 */
		public static final int v_private = 3;

		/**
		 * Get the visibility.
		 * 
		 * @return int
		 */
		public int getVisibility();

		/**
		 * Set the visibility.
		 * 
		 * @param visibility
		 */
		public void setVisibility(int visibility);

		/**
		 * <code>NAME</code> is the name of the base class.
		 */
		public static final ASTNodeProperty NAME = new ASTNodeProperty(
				"ICPPASTBaseSpecifier.NAME - Name of base class"); //$NON-NLS-1$

		/**
		 * Get the name.
		 * 
		 * @return <code>IASTName</code>
		 */
		public IASTName getName();

		/**
		 * Set the name.
		 * 
		 * @param name
		 *            <code>IASTName</code>
		 */
		public void setName(IASTName name);
	}

	/**
	 * Get the base specifiers.
	 * 
	 * @return <code>ICPPASTBaseSpecifier []</code>
	 */
	public ICPPASTBaseSpecifier[] getBaseSpecifiers();

	/**
	 * Add a base specifier.
	 * 
	 * @param baseSpec
	 *            <code>ICPPASTBaseSpecifier</code>
	 */
	public void addBaseSpecifier(ICPPASTBaseSpecifier baseSpec);

}
