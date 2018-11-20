/*******************************************************************************
 * Copyright (c) 2011 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.net.URL;

import org.eclipse.cdt.core.resources.IConsole;

/**
 * Extension of CDT console adaptor interface to UI plugin console. This extension
 * provides control over context id, name and icon in the Console view.
 */
public interface ICConsole extends IConsole {
	/**
	 * Initialize console with user-controlled context, name and icon
	 * in "Display Selected Console" dropbox in the Console view.
	 *
	 * @param contextId - context menu id in the Console view. A caller needs to define
	 *    a distinct one for own use.
	 * @param name - name of console to appear in the list of consoles in context menu
	 *    in the Console view.
	 * @param iconUrl - a {@link URL} of the icon for the context menu of the Console
	 *    view. The url is expected to point to an image in eclipse OSGi bundle.
	 *    {@code iconUrl} can be <b>null</b>, in that case the default image is supposed to be used.
	 */
	void init(String contextId, String name, URL iconUrl);
}
