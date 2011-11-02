/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
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
 * with the supplied base URI.<p>
 * <em>Note: The supplied base URI must end with a forward slash</em>
 * <br>
 * This location converter is internal-representation-compatible with ResourceContainerRelativeLocationConverter
 * @since 4.0
 */
/*
 * The associated internal PDOM representation is the relative path (non encoded form)
 */
public class URIRelativeLocationConverter implements IIndexLocationConverter {
	private URI baseURI;
	
	/**
	 * Constructs an URIRelativeLocationConverter which will relative paths
	 * by prefixing the supplied base URI.
	 * @param baseURI the URI which will form the absolute base that relative paths
	 * are concatenated to. <em>Note: It must end with a forward slash</em>.
	 */
	public URIRelativeLocationConverter(URI baseURI) {
		this.baseURI = baseURI;
	}
	
	@Override
	public IIndexFileLocation fromInternalFormat(String raw) {
		String rawPath = URIUtil.toURI(raw).getRawPath();
		if (rawPath.length() > 0 && rawPath.charAt(0) == '/')
			rawPath= rawPath.substring(1);
		URI uri= baseURI.resolve(rawPath);
		return new IndexFileLocation(uri, null);
	}
	
	@Override
	public String toInternalFormat(IIndexFileLocation location) {
		URI relative = baseURI.relativize(location.getURI());
		return relative.isAbsolute() ? null : relative.getPath();
	}
}
