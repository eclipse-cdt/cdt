/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;

/**
 * Composite scope of a class specialization. Supports creating instances for bindings found
 * in the scope of the specialized class template.
 *
 * @since 5.0
 */
public interface ICPPClassSpecializationScope extends ICPPClassScope {
	/**
	 * Returns the class that was specialized to get this scope.
	 */
	ICPPClassType getOriginalClassType();

	/**
	 * The specialized class.
	 */
	@Override
	ICPPClassSpecialization getClassType();

	/**
	 * Computes the bases via the original class.
	 */
	ICPPBase[] getBases();

	/**
	 * Computes the methods via the original class.
	 */
	ICPPMethod[] getDeclaredMethods();

	/**
	 * Computes the fields via the original class.
	 */
	ICPPField[] getDeclaredFields();

	/**
	 * Computes the friends via the original class.
	 */
	IBinding[] getFriends();

	/**
	 * Computes the nested classes via the original class.
	 */
	ICPPClassType[] getNestedClasses();

	/**
	 * Computes the using declarations via the original class.
	 */
	ICPPUsingDeclaration[] getUsingDeclarations();
}
