/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public String getMappedPath(URI locationURI) {
		
		String path = locationURI.getPath();
		if (path.contains("/BeingMappedFrom/Folder")) {
			return path.replaceFirst("/BeingMappedFrom/Folder", "/LocallyMappedTo/Folder");
		}
		
		return super.getMappedPath(locationURI);
	}

}
