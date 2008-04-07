/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * Uwe Stieber (Wind River) - [174418] SystemAdapterHelpers.getViewAdapter(Object) try to find adapter twice
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 *******************************************************************************/

package org.eclipse.rse.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.model.ISystemViewInputProvider;


/**
 * This class has static helper methods that will get an adapter given an object.
 */
public class SystemAdapterHelpers {

	/**
	 * Returns the implementation of ISystemViewElement for the given
	 * object.  Returns null if the adapter is not defined or the
	 * object is not adaptable.
	 */
	public static ISystemViewElementAdapter getViewAdapter(Object o) {
		ISystemViewElementAdapter adapter = null;

		// In case the object itself is an adaptable, call the objects getAdapter() method
		if (o instanceof IAdaptable) {
			adapter = (ISystemViewElementAdapter)((IAdaptable)o).getAdapter(ISystemViewElementAdapter.class);
		} else if (o != null) {
			// object is not an adaptable itself, call the adapter manager
			adapter = (ISystemViewElementAdapter)Platform.getAdapterManager().getAdapter(o, ISystemViewElementAdapter.class);
		}

		return adapter;
	}

	/**
	 * Overload to use when calling from a viewer. This not only finds and returns
	 *  the adapter, but also sets its viewer to the given viewer. Many actions rely
	 *  on this being set.
	 */
	public static ISystemViewElementAdapter getViewAdapter(Object o, Viewer viewer) {
		ISystemViewElementAdapter adapter = getViewAdapter(o);
		if (adapter != null) {
			//FIXME This is not thread-safe.
			adapter.setViewer(viewer);
		}
		return adapter;
	}

	/**
	 * Overload to use when calling from a viewer. This not only finds and returns
	 * the adapter, but also sets its viewer and input provider to the given viewer.
	 * Many actions rely on this being set.
	 */
	public static ISystemViewElementAdapter getViewAdapter(Object o, Viewer viewer, ISystemViewInputProvider inputProvider) {
		ISystemViewElementAdapter adapter = getViewAdapter(o, viewer);

		if (adapter != null) {
			//FIXME This is not thread-safe.
			adapter.setInput(inputProvider);
		}

		return adapter;
	}

	/**
	 * Returns the implementation of ISystemRemoteElementAdapter for the given
	 *  remote object.  Returns null if this object does not adaptable to this.
	 */
	public static ISystemRemoteElementAdapter getRemoteAdapter(Object o) {
		ISystemRemoteElementAdapter adapter = null;

		// In case the object itself is an adaptable, call the objects getAdapter() method
		if (o instanceof IAdaptable) {
			adapter = (ISystemRemoteElementAdapter)((IAdaptable)o).getAdapter(ISystemRemoteElementAdapter.class);
		} else if (o != null) {
			// object is not an adaptable itself, call the adapter manager
			adapter = (ISystemRemoteElementAdapter)Platform.getAdapterManager().getAdapter(o, ISystemRemoteElementAdapter.class);
		}

		return adapter;
	}

	/**
	 * Overload to use when calling from a viewer. This not only finds and returns
	 *  the adapter, but also sets its viewer to the given viewer. Many actions rely
	 *  on this being set.
	 */
	public static ISystemRemoteElementAdapter getRemoteAdapter(Object o, Viewer viewer) {
		ISystemRemoteElementAdapter adapter = getRemoteAdapter(o);
		if ((adapter != null) && (adapter instanceof ISystemViewElementAdapter))
			((ISystemViewElementAdapter)adapter).setViewer(viewer);
		return adapter;
	}
}
