/********************************************************************************
 * Copyright (c) 2006 IBM Corporation and Wind River Systems, Inc.
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
 * Martin Oberhuber (Wind River) - adapted template for daytime example.
 ********************************************************************************/

package org.eclipse.rse.examples.daytime.model;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * This factory maps requests for an adapter object from a given remote object.
 */
public class DaytimeAdapterFactory extends AbstractSystemRemoteAdapterFactory
		implements IAdapterFactory {

	private DaytimeResourceAdapter daytimeAdapter = new DaytimeResourceAdapter();

	public DaytimeAdapterFactory() {
		super();
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		ISystemViewElementAdapter adapter = null;
		if (adaptableObject instanceof DaytimeResource) {
			adapter = daytimeAdapter;
		}
		// these lines are very important! 
		if ((adapter != null) && (adapterType == IPropertySource.class)) {
			adapter.setPropertySourceInput(adaptableObject);
		}
		return adapter;
	}

}
