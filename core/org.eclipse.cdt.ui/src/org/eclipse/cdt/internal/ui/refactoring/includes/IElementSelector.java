/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
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
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.Collection;

/**
 * Interface for selecting one of a set of elements.
 */
public interface IElementSelector {
	/**
	 * Selects one element from a set of elements.
	 *
	 * @param elements the objects to select from
	 * @return the selected element or {@code null} if nothing was selected
	 */
	public <T> T selectElement(Collection<T> elements);
}
