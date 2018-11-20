/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.tests.filesystem.ram;

import java.net.URI;

import org.eclipse.cdt.core.EFSExtensionProvider;

/**
 * Test stub to test EFSExtensionProvider mappings.
 *
 */
public class MemoryEFSExtensionProvider extends EFSExtensionProvider {

	@Override
	public String getMappedPath(URI locationURI) {

		String path = locationURI.getPath();
		if (path.contains("/BeingMappedFrom/Folder")) {
			return path.replaceFirst("/BeingMappedFrom/Folder", "/LocallyMappedTo/Folder");
		}

		return super.getMappedPath(locationURI);
	}

}
