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
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
******************************************************************************/
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
	 * {@code k_class} C++ introduces the class concept for composite types.
	 */
	public static final int k_class = IASTCompositeTypeSpecifier.k_last + 1;

	/**
	 * {@code k_last} allows for subinterfaces to extend the kind type.
	 */
	public static final int k_last = k_class;

	/**
	 * {@code VISIBILITY_LABEL} is used to express the relationship for a visibility label "declaration".
	 */
	public static final ASTNodeProperty VISIBILITY_LABEL = new ASTNodeProperty(
			"ICPPASTCompositeTypeSpecifier.VISIBILITY_LABEL - Visibility label \"declaration\""); //$NON-NLS-1$

	/**
	 * {@code BASE_SPECIFIER} expresses the subclass role.
	 */
	public static final ASTNodeProperty BASE_SPECIFIER = new ASTNodeProperty(
			"ICPPASTCompositeTypeSpecifier.BASE_SPECIFIER - Expresses the subclass role"); //$NON-NLS-1$

	/** @since 5.7 */
	public static final ASTNodeProperty CLASS_VIRT_SPECIFIER = new ASTNodeProperty(
			"ICPPASTCompositeTypeSpecifier.CLASS_VIRT_SPECIFIER [ICPPASTClassVirtSpecifier]"); //$NON-NLS-1$

	/**
	 * Base specifiers are where a class expresses from whom it inherits.
	 *
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public static interface ICPPASTBaseSpecifier extends IASTNode, IASTNameOwner, ICPPASTPackExpandable {
		public static final ICPPASTBaseSpecifier[] EMPTY_BASESPECIFIER_ARRAY = {};

		/**
		 * Relation between base specifier and its name specifier.
		 *
		 * @since 5.8
		 */
		public static final ASTNodeProperty NAME_SPECIFIER = new ASTNodeProperty(
				"ICPPASTBaseSpecifier.NAME_SPECIFIER - Name specifier of base class"); //$NON-NLS-1$

		/**
		 * @deprecated Use {@link ICPPASTBaseSpecifier#NAME_SPECIFIER} instead.
		 * @noreference This field is not intended to be referenced by clients.
		 */
		@Deprecated
		public static final ASTNodeProperty NAME = new ASTNodeProperty(
				"ICPPASTBaseSpecifier.NAME - Name of base class"); //$NON-NLS-1$

		public static final int v_public = ICPPASTVisibilityLabel.v_public;
		public static final int v_protected = ICPPASTVisibilityLabel.v_protected;
		public static final int v_private = ICPPASTVisibilityLabel.v_private;

		/**
		 * Returns whether this specifies a virtual base.
		 */
		public boolean isVirtual();

		/**
		 * Returns the accessibility for the base.
		 */
		public int getVisibility();

		/**
		 * @deprecated Use getNameSpecifier() instead.
		 * @noreference This method is not intended to be referenced by clients.
		 */
		@Deprecated
		public IASTName getName();

		/**
		 * Returns the name specifier inside this base specifier.
		 *
		 * @since 5.8
		 */
		public ICPPASTNameSpecifier getNameSpecifier();

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
		 * @deprecated Use setNameSpecifier() instead.
		 * @noreference This method is not intended to be referenced by clients.
		 */
		@Deprecated
		public void setName(IASTName name);

		/**
		 * Sets the name specifier for this base specifier. Not allowed on frozen AST.
		 *
		 * @since 5.8
		 */
		public void setNameSpecifier(ICPPASTNameSpecifier nameSpecifier);

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
	 * Returns the base specifiers.
	 *
	 * @return {@code ICPPASTBaseSpecifier[]}
	 */
	public ICPPASTBaseSpecifier[] getBaseSpecifiers();

	/**
	 * Adds a base specifier.
	 *
	 * @param baseSpec {@code ICPPASTBaseSpecifier}
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

	/**
	 * Queries whether the type is final.
	 *
	 * @since 5.5
	 */
	public boolean isFinal();

	/**
	 * Sets whether the type is final.
	 *
	 * @since 5.5
	 * @deprecated Use setVirtSpecifier() instead.
	 */
	@Deprecated
	public void setFinal(boolean isFinal);

	/**
	 * Returns the class-virt-specifier of this class, or null if it doesn't have one.
	 * @since 5.7
	 */
	public ICPPASTClassVirtSpecifier getVirtSpecifier();

	/**
	 * Sets the class-virt-specifier for this class.
	 * @since 5.7
	 */
	public void setVirtSpecifier(ICPPASTClassVirtSpecifier virtSpecifier);
}
