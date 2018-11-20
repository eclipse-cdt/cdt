/*******************************************************************************
 * Copyright (c) 2007, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagImages {

	private static ImageRegistry imageRegistry = new ImageRegistry();

	private static URL iconBaseURL = Activator.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private static final String NAME_PREFIX = Activator.PLUGIN_ID + '.';

	private static final String T_TABS = "view16/"; //$NON-NLS-1$

	private static final String IMG_VIEW_DEBUGGER_TAB = NAME_PREFIX + "debugger_tab.gif"; //$NON-NLS-1$
	private static final String IMG_VIEW_STARTUP_TAB = NAME_PREFIX + "startup_tab.gif"; //$NON-NLS-1$

	public static Image getDebuggerTabImage() {
		return imageRegistry.get(IMG_VIEW_DEBUGGER_TAB);
	}

	public static Image getStartupTabImage() {
		return imageRegistry.get(IMG_VIEW_STARTUP_TAB);
	}

	static {
		createManaged(T_TABS, IMG_VIEW_DEBUGGER_TAB);
		createManaged(T_TABS, IMG_VIEW_STARTUP_TAB);
	}

	private static void createManaged(String prefix, String name) {
		imageRegistry.put(name,
				ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX.length()))));
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuilder buffer = new StringBuilder(prefix);
		buffer.append(name);
		try {
			return new URL(iconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			Activator.log(e);
			return null;
		}
	}

}
