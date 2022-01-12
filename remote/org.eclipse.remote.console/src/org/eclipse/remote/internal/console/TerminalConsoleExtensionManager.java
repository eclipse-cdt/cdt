/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.remote.console.actions.IConsoleActionFactory;

public class TerminalConsoleExtensionManager {
	private static TerminalConsoleExtensionManager instance;
	private Map<String, List<String>> actions;
	private Map<String, IConsoleActionFactory> factories;
	private Map<String, IConfigurationElement> elements;

	private TerminalConsoleExtensionManager() {
	}

	public static TerminalConsoleExtensionManager getInstance() {
		if (instance == null) {
			instance = new TerminalConsoleExtensionManager();
		}
		return instance;
	}

	public List<String> getActionsForType(String id) {
		initialize();
		List<String> list = actions.get(id);
		return list == null ? new ArrayList<String>() : list;
	}

	public IConsoleActionFactory getFactory(String id) {
		return factories.get(id);
	}

	private void initialize() {
		if (actions == null) {
			actions = new LinkedHashMap<>();
			elements = new LinkedHashMap<>();
			factories = new HashMap<>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry
					.getExtensionPoint(Activator.getDefault().getBundle().getSymbolicName() + ".toolbar");
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id != null) {
						elements.put(id, element);
						IConsoleActionFactory factory = null;
						try {
							factory = (IConsoleActionFactory) element.createExecutableExtension("actionFactory");
						} catch (CoreException e) {
							Activator.log(e);
						}
						if (factory != null) {
							String connectionType = element.getAttribute("connectionType");
							if (connectionType != null) {
								List<String> actionList = actions.get(connectionType);
								if (actionList == null) {
									actionList = new ArrayList<>();
								}
								actionList.add(id);
								actions.put(connectionType, actionList);
								factories.put(id, factory);
							}
						}
					}
				}
			}
		}
	}
}
