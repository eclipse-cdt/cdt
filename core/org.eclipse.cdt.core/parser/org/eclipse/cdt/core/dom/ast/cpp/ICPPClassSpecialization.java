/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public interface ICPPClassSpecialization extends ICPPTypeSpecialization, ICPPClassType {
	@Override
	ICPPClassType getSpecializedBinding();

	/**
	 * Creates a specialized binding for a member of the original class. The result is
	 * a member of this class specialization.
	 */
	IBinding specializeMember(IBinding binding);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassSpecialization#specializeMember(IBinding)} instead.
	 */
	@Deprecated
	IBinding specializeMember(IBinding binding, IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getBases()} instead.
	 */
	@Deprecated
	ICPPBase[] getBases(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getConstructors()} instead.
	 */
	@Deprecated
	ICPPConstructor[] getConstructors(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getDeclaredFields()} instead.
	 */
	@Deprecated
	ICPPField[] getDeclaredFields(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getMethods()} instead.
	 */
	@Deprecated
	ICPPMethod[] getMethods(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getAllDeclaredMethods()} instead.
	 */
	@Deprecated
	ICPPMethod[] getAllDeclaredMethods(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getDeclaredMethods()} instead.
	 */
	@Deprecated
	ICPPMethod[] getDeclaredMethods(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getFriends()} instead.
	 */
	@Deprecated
	IBinding[] getFriends(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getFields()} instead.
	 */
	@Deprecated
	IField[] getFields(IASTNode point);

	/**
	 * @since 5.5
	 * @deprecated Use {@link ICPPClassType#getNestedClasses()} instead.
	 */
	@Deprecated
	ICPPClassType[] getNestedClasses(IASTNode point);

	/**
	 * @since 6.3
	 * @deprecated Use {@link ICPPClassType#getUsingDeclarations()} instead.
	 */
	@Deprecated
	ICPPUsingDeclaration[] getUsingDeclarations(IASTNode point);
}
