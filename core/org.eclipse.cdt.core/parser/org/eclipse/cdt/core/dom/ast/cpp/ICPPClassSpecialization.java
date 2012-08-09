/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;

/**
 * Specializations of all sorts of class types.
 * @since 5.1
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassSpecialization extends ICPPSpecialization, ICPPClassType {
	@Override
	ICPPClassType getSpecializedBinding();

	/**
	 * @deprecated Specializing a member may require a point of instantiation.
	 */
	@Deprecated
	IBinding specializeMember(IBinding binding);

	/**
	 * Creates a specialized binding for a member of the original class. The result is 
	 * a member of this class specialization.
	 * @since 5.5
	 */
	IBinding specializeMember(IBinding binding, IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getBases()} but a accepts a starting point for template
	 * instantiation.
	 * @since 5.5
	 */
	ICPPBase[] getBases(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getConstructors()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	ICPPConstructor[] getConstructors(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getDeclaredFields()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	ICPPField[] getDeclaredFields(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getMethods()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	ICPPMethod[] getMethods(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getAllDeclaredMethods()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	ICPPMethod[] getAllDeclaredMethods(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getDeclaredMethods()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	ICPPMethod[] getDeclaredMethods(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getFriends()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	IBinding[] getFriends(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getFriends()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	IField[] getFields(IASTNode point);

	/**
	 * Similar to {@link ICPPClassType#getNestedClasses()} but a accepts a starting point
	 * for template instantiation.
	 * @since 5.5
	 */
	ICPPClassType[] getNestedClasses(IASTNode point);
}
