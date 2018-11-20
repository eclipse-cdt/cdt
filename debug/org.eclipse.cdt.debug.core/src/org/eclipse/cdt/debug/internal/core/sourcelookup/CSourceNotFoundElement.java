/*******************************************************************************
 * Copyright (c) 2006, 2015 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Wrapper for debug elements that have missing source, for example a stack
 * frame whose source file can not be located. Used to enable the
 * CSourceNotFoundEditor that will let you find the missing file.
 *
 */
public class CSourceNotFoundElement {

	private IAdaptable element;
	private ILaunchConfiguration launch;
	private String file;

	public IAdaptable getElement() {
		return element;
	}

	public CSourceNotFoundElement(IAdaptable element, ILaunchConfiguration launch, String file) {
		this.element = element;
		this.launch = launch;

		// client assumes empty string rather than null
		this.file = file != null ? file : ""; //$NON-NLS-1$
	}

	public ILaunchConfiguration getLaunch() {
		return launch;
	}

	public String getFile() {
		return file;
	}

	/**
	 * @return a description string or null if not available
	 */
	public String getDescription() {
		ICSourceNotFoundDescription description = element.getAdapter(ICSourceNotFoundDescription.class);
		if (description != null)
			return description.getDescription();
		else
			return element.toString();
	}

}
