/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import java.util.ArrayList;

import org.eclipse.cdt.debug.gdbjtag.core.Activator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;

public class GDBJtagDeviceContributionFactory {
	private static final String EXTENSION_POINT_NAME = "JTagDevice";
	private static final String MAIN_ELEMENT = "device";

	private static GDBJtagDeviceContributionFactory instance;
	protected ArrayList contributions;

	private GDBJtagDeviceContributionFactory() {
		contributions = new ArrayList();
		loadSubtypeContributions();
	}
	
	public GDBJtagDeviceContribution[] getGDBJtagDeviceContribution() {
		return (GDBJtagDeviceContribution[]) contributions.toArray(
				new GDBJtagDeviceContribution[contributions.size()]);
	}

	private void loadSubtypeContributions() {

		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(
				Activator.getUniqueIdentifier(), EXTENSION_POINT_NAME);
		if (ep == null)
			return;
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement configurationElement = elements[i];
			if (configurationElement.getName().equals(MAIN_ELEMENT)) {
				String id = getRequired(configurationElement, "id");
				String name = getRequired(configurationElement, "name");
				String className = getRequired(configurationElement, "class");
	            GDBJtagDeviceContribution adapter = new GDBJtagDeviceContribution();
				adapter.setDeviceId(id);
				adapter.setDeviceName(name);
				adapter.setDeviceClassName(className);
				adapter.setDeviceClassBundleName(configurationElement.getContributor().getName());
				addContribution(adapter);
			}
		}
	}

	public void addContribution(GDBJtagDeviceContribution contribution) {
		contributions.add(contribution);

	}

	public static GDBJtagDeviceContributionFactory getInstance() {
		if (instance == null) {
			instance = new GDBJtagDeviceContributionFactory();
		}
		return instance;
	}

	private static String getRequired(IConfigurationElement configurationElement, String name) {
		String elementValue = configurationElement.getAttribute(name);
		if (elementValue == null)
			Activator.log(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(),
					DebugPlugin.INTERNAL_ERROR, "Extension "
							+ configurationElement.getDeclaringExtension().getUniqueIdentifier()
							+ " missing required attribute: " + name, null));
		return elementValue;
	}

}
