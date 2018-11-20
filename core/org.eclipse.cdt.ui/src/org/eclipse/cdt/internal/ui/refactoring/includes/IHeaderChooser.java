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

import org.eclipse.core.runtime.IPath;

/**
 * Interface for selecting one of alternative headers.
 */
public interface IHeaderChooser {
	/**
	 * Chooses one header out of multiple alternatives.
	 *
	 * @param bindingName the name of the binding for which the header is selected
	 * @param headers absolute file system locations of the headers defining the binding
	 * @return the chosen header or {@code null} if nothing was selected
	 */
	public IPath chooseHeader(String bindingName, Collection<IPath> headers);
}
