/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
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
 * Kevin Doyle (IBM) - [195537] Move ElementComparer From SystemView to Separate File
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 * David Dykstal (IBM) - [225911] Exception received after deleting a profile containing a connection
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.core.model.SystemRegistry;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

/**
 * An implememtation of an element comparer for the system view.
 */
public class ElementComparer implements IElementComparer {
	
	public boolean equals(Object a, Object b) {
		boolean result = false;
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		if (registry instanceof SystemRegistry) {
			result = ((SystemRegistry) registry).isSameObjectByAbsoluteName(a, null, b, null);
		}
		return result;
	}

	public int hashCode(Object element) {
		/*
		 * Hashcodes should be invariant across the lifetime of the object.
		 * Since one adapter is typically used for many elements in RSE,
		 * performance would be better if the original Element's hashCode
		 * were used rather than the adapter's hashCode. The problem with
		 * this is, that if the remote object changes, it cannot be 
		 * identified any more.
		 * Note that currently the hashCode of the object can change 
		 * over time if properties are modified (this is probably a bug).
		 * Therefore, if there is no absolute name, play it safe and return the adapter's hashCode which won't ever change.
		 */
		int result = 0;
		ISystemViewElementAdapter ident = null;
		if (element instanceof IAdaptable) {
			ident = (ISystemViewElementAdapter) ((IAdaptable) element).getAdapter(ISystemViewElementAdapter.class);
			Assert.isNotNull(ident, NLS.bind("no adapter found for element {0}", element)); //$NON-NLS-1$
			String absName = ident.getAbsoluteName(element);
			if (absName != null) {
				result = absName.hashCode();
			} else {
				result = ident.hashCode();
			}
		} else if (element != null) {
			result = element.hashCode();
		}
		return result;
	}

}
