/*******************************************************************************
 * Copyright (c) 2013, 2014 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IEnumerator;

/**
 * @since 5.5
 */
public interface ICPPEnumerationSpecialization extends ICPPEnumeration, ICPPTypeSpecialization {
	@Override
	ICPPEnumeration getSpecializedBinding();

	/**
	 * Return a specialized version of the given enumerator. The enumerator must be one
	 * of the enumerators of the enumeration being specialized.
	 */
	IEnumerator specializeEnumerator(IEnumerator enumerator);
}
