/*******************************************************************************
 * Copyright (c) 2014, 2015 Google, Inc and others.
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
package org.eclipse.cdt.internal.core;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A collection of static adaptor-related methods.
 */
public class AdapterUtil {
	/**
	 * Adapts the given object to the given type.
	 *
	 * @param object the object to adapt
	 * @param type the type to adapt to
	 * @return the adapted object, or {@code null} if the object cannot be adapted.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T adapt(Object object, Class<T> type) {
		if (object == null)
			return null;

		if (type.isInstance(object))
			return (T) object;

		if (object instanceof IAdaptable)
			return ((IAdaptable) object).getAdapter(type);

		return null;
	}
}
