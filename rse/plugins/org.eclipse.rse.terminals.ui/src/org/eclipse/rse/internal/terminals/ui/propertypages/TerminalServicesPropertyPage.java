/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anna Dushistova  (MontaVista) - adapted from ProcessServicesPropertyPage
 * Anna Dushistova  (MontaVista) - [257638] [rseterminal] Terminal subsystem doesn't have service properties
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystem;
import org.eclipse.rse.ui.propertypages.ServicesPropertyPage;
import org.eclipse.rse.ui.widgets.services.FactoryServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;

public class TerminalServicesPropertyPage extends ServicesPropertyPage {
	private ITerminalServiceSubSystemConfiguration _currentFactory;

	protected TerminalServiceSubSystem getTerminalServiceSubSystem() {
		return (TerminalServiceSubSystem) getElement();
	}

	protected ServiceElement[] getServiceElements() {
		TerminalServiceSubSystem subSystem = getTerminalServiceSubSystem();

		IHost host = subSystem.getHost();
		_currentFactory = subSystem.getParentRemoteTerminalSubSystemConfiguration();
		ITerminalServiceSubSystemConfiguration[] factories = getTerminalServiceSubSystemConfigurations(host
				.getSystemType());

		// create elements for each
		ServiceElement[] elements = new ServiceElement[factories.length];
		for (int i = 0; i < factories.length; i++) {
			ITerminalServiceSubSystemConfiguration factory = factories[i];
			elements[i] = new FactoryServiceElement(host, factory);
			if (factory == _currentFactory) {
				elements[i].setSelected(true);
			}
		}

		return elements;
	}

	protected ITerminalServiceSubSystemConfiguration[] getTerminalServiceSubSystemConfigurations(
			IRSESystemType systemType) {
		List results = new ArrayList();
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] factories = sr
				.getSubSystemConfigurationsBySystemType(systemType, false, true);

		for (int i = 0; i < factories.length; i++) {
			ISubSystemConfiguration factory = factories[i];
			if (factory instanceof ITerminalServiceSubSystemConfiguration) {
				results.add(factory);
			}
		}

		return (ITerminalServiceSubSystemConfiguration[]) results
				.toArray(new ITerminalServiceSubSystemConfiguration[results
						.size()]);
	}

	protected ISubSystemConfiguration getCurrentSubSystemConfiguration() {
		return _currentFactory;
	}

	public void setSubSystemConfiguration(ISubSystemConfiguration factory) {
		_currentFactory = (ITerminalServiceSubSystemConfiguration) factory;
	}

}
