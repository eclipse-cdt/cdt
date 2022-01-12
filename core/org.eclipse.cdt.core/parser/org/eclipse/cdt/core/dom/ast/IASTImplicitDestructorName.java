/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * An implicit name corresponding to a destructor call for a temporary or a variable going out of scope.
 *
 * @since 5.10
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTImplicitDestructorName extends IASTImplicitName {
	public static final IASTImplicitDestructorName[] EMPTY_NAME_ARRAY = {};

	/**
	 * Returns the name corresponding to the constructor call.
	 */
	IASTName getConstructionPoint();
}
