/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.flatpak.launcher.ui.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.flatpak.launcher.FlatpakLaunchPlugin;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class FlatpakPreferenceNode extends PreferenceNode {

	private String label;
	private String className;

	public FlatpakPreferenceNode(String id, String label, ImageDescriptor image, String className) {
		super(id, label, image, className);
		this.label = label;
		this.className = className;
	}

	@Override
	public void createPage() {
		IWorkbenchPreferencePage page = (IWorkbenchPreferencePage) getPage();
		if (page == null) {
			try {
				Class<?> cl = Class.forName(className);
				page = (IWorkbenchPreferencePage) cl.getDeclaredConstructor().newInstance();
				if (page != null) {
					page.setTitle(label);
					page.init(PlatformUI.getWorkbench());
					setPage(page);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodError
					| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				FlatpakLaunchPlugin.log(e);
			}
		}
	}

}
