/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.internal.ui;

import org.eclipse.rse.ui.IRSEAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * Abstract base class with basic implementations of the <code>IRSEAdapter</code> interface.
 * Intended to be subclassed.
 */
public abstract class RSEAdapter extends WorkbenchAdapter implements IRSEAdapter {

	/**
	 * Constructor.
	 */
	public RSEAdapter() {
		super();
	}

	/**
	 * The default implementation of this <code>IRSEAdapter<code> method returns the empty string.
	 * Subclasses may override.
	 * @see org.eclipse.rse.ui.IRSEAdapter#getDescription(java.lang.Object)
	 */
	public String getDescription(Object object) {
		return ""; //$NON-NLS-1$
	}
}