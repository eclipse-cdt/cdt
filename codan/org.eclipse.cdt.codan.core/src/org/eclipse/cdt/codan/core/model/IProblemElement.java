/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Problem element represents problem category {@link IProblemCategory} or
 * problem {@link IProblem}
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemElement extends Cloneable {
	/**
	 * clone method should be implemented to support problem cloning
	 *
	 * @see {@link Object#clone}
	 * @return new object which is copy of this one
	 */
	Object clone();

	/**
	 * @return problem profile where element belongs, can return null if profile
	 *         is not define for some reason
	 * @since 2.0
	 */
	IProblemProfile getProfile();

	/**
	 * @return problem category where element belongs for a give profile, can
	 *         return null if profile is not define
	 * @since 2.0
	 */
	IProblemCategory getParentCategory();
}
