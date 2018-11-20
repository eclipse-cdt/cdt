/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

/**
 * @see ChangeParametersControl
 */
public interface IParameterListChangeListener {
	/**
	 * Gets fired when the given parameter has changed
	 * @param parameter the parameter that has changed.
	 */
	public void parameterChanged(NameInformation parameter);

	/**
	 * Gets fired when the given parameter has been added
	 * @param parameter the parameter that has been added.
	 */
	public void parameterAdded(NameInformation parameter);

	/**
	 * Gets fired if the parameter list got modified by reordering or removing
	 * parameters (note that adding is handled by <code>parameterAdded</code>))
	 */
	public void parameterListChanged();
}
