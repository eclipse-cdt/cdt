/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.resources;

import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.cdt.internal.core.Cygwin;

public class CygwinEFSExtensionProvider extends EFSExtensionProvider {
	@Override
	public String getMappedPath(URI locationURI) {
		String cygwinPath = getPathFromURI(locationURI);
		String windowsPath = null;
		try {
			windowsPath = Cygwin.cygwinToWindowsPath(cygwinPath);
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		return windowsPath;
	}
}
