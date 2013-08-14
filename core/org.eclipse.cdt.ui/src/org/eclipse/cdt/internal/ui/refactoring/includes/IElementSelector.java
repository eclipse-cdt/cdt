/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
