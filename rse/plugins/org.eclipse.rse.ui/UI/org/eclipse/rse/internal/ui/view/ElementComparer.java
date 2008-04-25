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
 * David Dykstal (IBM) - [228774] [regression] AssertionFailedException when connecting to New Connection
 * Martin Oberhuber (Wind River) - [228774] Improve ElementComparer Performance
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.internal.core.model.SystemRegistry;

/**
 * An implementation of an element comparer for the system view.
 */
public class ElementComparer implements IElementComparer {

	public boolean equals(Object a, Object b) {
		// equal if same absolute name in same subsystem;
		// or, when adapters are not found, both are the same instance.
		return SystemRegistry.isSameObjectByAbsoluteName(a, null, b, null);
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
		if (element != null) {
			ISystemDragDropAdapter dda = SystemRegistry.getSystemDragDropAdapter(element);
			if (dda != null) {
				// adapter available
				String absName = dda.getAbsoluteName(element);
				if (absName != null) {
					result = absName.hashCode();
				} else {
					result = dda.hashCode();
				}
			} else {
				// --MOB: Usually, we should fall back to constant hashcode 0
				// here if no adapter is available, in order to ensure constant
				// hashcode even if object properties change. But as a matter of
				// fact, those elements that we have in the SystemView and which
				// do not have an adapter registered, are very few; and they are
				// always constant over their lifetime, such as the "Pending..."
				// node for instance. We therefore return the element's hashcode
				// here, along with the corresponding equals() code above,
				// which falls back to Object equality if no adapter is
				// available.
				result = element.hashCode();
			}
		}
		return result;
	}

}
