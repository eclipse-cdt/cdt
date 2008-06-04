/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [225506][api] make RSEAdapter API
 *******************************************************************************/
package org.eclipse.rse.ui;

import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * Abstract base class with basic implementations of the
 * <code>IRSEAdapter</code> interface. Intended to be subclassed.
 *
 * @since org.eclipse.rse.ui 3.0
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
