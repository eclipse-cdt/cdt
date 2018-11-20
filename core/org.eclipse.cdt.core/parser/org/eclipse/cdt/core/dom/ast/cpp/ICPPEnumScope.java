/*******************************************************************************
 * Copyright (c) 2014 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Interface for enumeration scopes.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.7
 */
public interface ICPPEnumScope extends ICPPScope {
	/**
	 * Returns the binding for the enumeration this scope is associated with.
	 */
	ICPPEnumeration getEnumerationType();
}