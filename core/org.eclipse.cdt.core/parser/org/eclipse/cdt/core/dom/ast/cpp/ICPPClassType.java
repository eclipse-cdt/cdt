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
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Thomas Corbat (IFS)
 ******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;

/**
 * Represents a C++ class.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassType extends ICompositeType, ICPPBinding {
	public static final ICPPClassType[] EMPTY_CLASS_ARRAY = {};
	public static final int k_class = ICPPASTCompositeTypeSpecifier.k_class;
	/**
	 * @since 5.5
	 */
	public static final int v_public = ICPPASTVisibilityLabel.v_public;
	/**
	 * @since 5.5
	 */
	public static final int v_protected = ICPPASTVisibilityLabel.v_protected;
	/**
	 * @since 5.5
	 */
	public static final int v_private = ICPPASTVisibilityLabel.v_private;

	/**
	 * Returns an array of base class relationships. The returned array is empty if there
	 * are none.
	 */
	public ICPPBase[] getBases();

	/**
	 * The method is restated here just to point out that this method returns a list of ICPPField
	 * objects representing all fields, declared or inherited.
	 */
	@Override
	public IField[] getFields();

	/**
	 * The method is restated here to point out that this method looks through the inheritance tree
	 * of this class while looking for a field with the given name. If no field is found, {@code null} is
	 * returned, if the name is found to be ambiguous a IProblemBinding is returned.
	 *
	 * @param name
	 */
	@Override
	public IField findField(String name);

	/**
	 * Returns a list of ICPPField objects representing fields declared in this class. It does not
	 * include fields inherited from base classes.
	 *
	 * @return List of ICPPField
	 */
	public ICPPField[] getDeclaredFields();

	/**
	 * Returns a list of ICPPMethod objects representing all methods defined for this class
	 * including those declared, inherited, or generated (e.g. default constructors and the like).
	 *
	 * @return List of ICPPMethod
	 */
	public ICPPMethod[] getMethods();

	/**
	 * Returns a list of ICPPMethod objects representing all method explicitly declared by this
	 * class and inherited from base classes. It does not include automatically generated methods.
	 *
	 * @return List of ICPPMethod
	 */
	public ICPPMethod[] getAllDeclaredMethods();

	/**
	 * Returns a list of ICPPMethod objects representing all methods explicitly declared by this
	 * class. It does not include inherited methods or automatically generated methods.
	 *
	 * @return List of ICPPMethod
	 */
	public ICPPMethod[] getDeclaredMethods();

	/**
	 * Returns an array of ICPPConstructor objects representing the constructors
	 * for this class. This list includes both declared and implicit constructors.
	 */
	public ICPPConstructor[] getConstructors();

	/**
	 * Returns an array of bindings for those classes/functions declared as friends of this class.
	 */
	public IBinding[] getFriends();

	/**
	 * Returns an array of nested classes/structures
	 */
	public ICPPClassType[] getNestedClasses();

	/**
	 * Returns an array of using declarations in this class.
	 *
	 * @since 6.3
	 */
	public ICPPUsingDeclaration[] getUsingDeclarations();

	/**
	 * Returns whether this type is declared final.
	 *
	 * @since 5.5
	 */
	public boolean isFinal();

	/**
	 * Returns the access specifier of the {@code member}.
	 *
	 * @param member The binding of the member to get the visibility for.
	 * {@code member} must be a member of this class.
	 *
	 * @return the visibility of the specified member.
	 * @throws IllegalArgumentException if {@code member} is not a member of this class.
	 *
	 * @since 5.5
	 */
	public int getVisibility(IBinding member);
}
