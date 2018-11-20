/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * This interface represents a class template partial specialization.  A partial specialization is
 * a class template in its own right.
 *
 * E.g.:
 * template <class T> class A {};     // the primary class template
 * template <class T> class A<T*> {}; // a partial specialization of the primary class template
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassTemplatePartialSpecialization extends ICPPClassTemplate, ICPPPartialSpecialization {
	/** @since 6.0 */
	public static final ICPPClassTemplatePartialSpecialization[] EMPTY_ARRAY = {};
	/**
	 * @deprecated Use {@link #EMPTY_ARRAY}
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final ICPPClassTemplatePartialSpecialization[] EMPTY_PARTIAL_SPECIALIZATION_ARRAY = EMPTY_ARRAY;

	/**
	 * Returns the ICPPTemplateDefinition which this is a specialization of.
	 */
	public ICPPClassTemplate getPrimaryClassTemplate();

	/**
	 * Returns the arguments of this partial specialization.
	 * @since 5.1
	 */
	@Override
	public ICPPTemplateArgument[] getTemplateArguments();
}
