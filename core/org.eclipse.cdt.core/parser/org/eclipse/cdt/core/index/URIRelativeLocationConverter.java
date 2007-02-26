/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import java.net.URI;

import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.core.filesystem.URIUtil;

/**
 * A IIndexLocationConverter for converting relative paths within an index, by prefixing them
 * with the supplied base URI
 * <br>
 * This location converter is internal-representation-compatible with ProjectRelativeLocationConverter
 */
/*
 * Internal representation is uri relative path (non encoded form)
 */
public class URIRelativeLocationConverter implements IIndexLocationConverter {
	private URI baseURI;
	
	/**
	 * Constructs an URIRelativeLocationConverter which will relative paths
	 * by prefixing the supplied base URI
	 * @param baseURI
	 */
	public URIRelativeLocationConverter(URI baseURI) {
		this.baseURI = baseURI;
	}
	
	public IIndexFileLocation fromInternalFormat(String raw) {
		URI uri= baseURI.resolve(URIUtil.toURI(raw).getRawPath().substring(1));
		return new IndexFileLocation(uri, null);
	}
	
	public String toInternalFormat(IIndexFileLocation location) {
		URI relative = baseURI.relativize(location.getURI());
		return relative.isAbsolute() ? null : relative.getPath();
	}
}
