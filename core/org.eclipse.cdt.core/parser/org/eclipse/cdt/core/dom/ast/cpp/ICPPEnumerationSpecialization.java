/*******************************************************************************
 * Copyright (c) 2013, 2014 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
