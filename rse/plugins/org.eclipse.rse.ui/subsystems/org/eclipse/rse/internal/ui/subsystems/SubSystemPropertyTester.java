/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - [227535] [rseterminal][api] terminals.ui should not depend on files.core
 * Anna Dushistova (MontaVista) - [251492] Launch Shell Action is enabled in Offline mode
 ********************************************************************************/
package org.eclipse.rse.internal.ui.subsystems;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

public class SubSystemPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.toLowerCase().equals("hassubsystemcategory")) { //$NON-NLS-1$

			boolean test = ((Boolean) expectedValue).booleanValue();

			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) receiver)
					.getAdapter(ISystemViewElementAdapter.class);
			if (adapter != null) {
				ISubSystem subsystem = adapter.getSubSystem(receiver);
				if (subsystem != null) {
					IHost host = subsystem.getHost();
					ISystemRegistry registry = RSECorePlugin
							.getTheSystemRegistry();
					String category = (String) args[0];
					ISubSystemConfigurationProxy[] proxies = registry
							.getSubSystemConfigurationProxiesByCategory(category);
					for (int i = 0; i < proxies.length; i++) {
						if (proxies[i]
								.appliesToSystemType(host.getSystemType())) {
							return test;
						}
					}
				}
				return !test;
			} else {
				return !test;
			}
		}else if (property.toLowerCase().equals("isoffline")){ //$NON-NLS-1$
			boolean test = ((Boolean) expectedValue).booleanValue();

			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) receiver)
					.getAdapter(ISystemViewElementAdapter.class);
			if (adapter != null) {
				ISubSystem subsystem = adapter.getSubSystem(receiver);
				if (subsystem != null) {
					if(subsystem.isOffline()){
						return test;
					}
				}
				return !test;
			} else {
				return !test;
			}
		}
		return false;
	}

}
