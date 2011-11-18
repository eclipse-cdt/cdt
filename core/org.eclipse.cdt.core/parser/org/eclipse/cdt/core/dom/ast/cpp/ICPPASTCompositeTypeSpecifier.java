/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTCompositeTypeSpecifier extends IASTCompositeTypeSpecifier, ICPPASTDeclSpecifier {

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
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public static interface ICPPASTBaseSpecifier extends IASTNode, IASTNameOwner, ICPPASTPackExpandable  {
		public static final ICPPASTBaseSpecifier[] EMPTY_BASESPECIFIER_ARRAY = new ICPPASTBaseSpecifier[0];

		/**
		 * Relation between base specifier and its name.
		 */
		public static final ASTNodeProperty NAME = new ASTNodeProperty(
				"ICPPASTBaseSpecifier.NAME - Name of base class"); //$NON-NLS-1$

		
		public static final int v_public = 1;
		public static final int v_protected = 2;
		public static final int v_private = 3;

		/**
		 * Returns whether this specifies a virtual base.
		 */
		public boolean isVirtual();

		/**
		 * Returns the accessibility for the base.
		 */
		public int getVisibility();

		/**
		 * Returns the name of this specifier.
		 */
		public IASTName getName();

		/**
		 * @since 5.1
		 */
		@Override
		public ICPPASTBaseSpecifier copy();
		
		/**
		 * @since 5.3
		 */
		@Override
		public ICPPASTBaseSpecifier copy(CopyStyle style);
		
		/**
		 * Sets the name for this specifier, not allowed on frozen AST.
		 */
		public void setName(IASTName name);
		
		/**
		 * Sets whether this specifier is for a virtual base. Not allowed on frozen AST.
		 */
		public void setVirtual(boolean value);

		/**
		 * Sets the visibility of this specifier, not allowed on frozen AST.
		 */
		public void setVisibility(int visibility);
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

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPClassScope getScope();
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTCompositeTypeSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTCompositeTypeSpecifier copy(CopyStyle style);
}
