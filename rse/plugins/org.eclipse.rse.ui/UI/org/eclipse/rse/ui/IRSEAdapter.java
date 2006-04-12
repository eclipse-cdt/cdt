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
package org.eclipse.rse.ui;

import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

/**
 * Base interface for all RSE adapters.
 */
public interface IRSEAdapter extends IWorkbenchAdapter, IWorkbenchAdapter2 {

	/**
	 * Returns the description text for this element. This is typically used to show the description of an object
	 * when displayed in the UI. Returns an empty string if there is no appropriate description text for this object.
	 * @param o the object to get the description text for
	 * @return the description text for the given object
	 */
	public String getDescription(Object o);

	/**
	 * Returns the RSE UI registry.
	 * @return the RSE UI registry
	 */
	public IRSEUIRegistry getRegistry();
}
