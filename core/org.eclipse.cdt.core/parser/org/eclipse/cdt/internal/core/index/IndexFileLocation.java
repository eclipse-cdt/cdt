/*******************************************************************************
 * Copyright (c) 2006, 2009 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import java.net.URI;

import org.eclipse.cdt.core.index.IIndexFileLocation;

/**
 * An implementation of IIndexFileLocation
 */
public class IndexFileLocation implements IIndexFileLocation {
	private final URI uri;
	private final String fullPath;
	
	public IndexFileLocation(URI uri, String fullPath) {
		if (uri == null)
			throw new IllegalArgumentException();
		this.uri = uri;
		this.fullPath = fullPath;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFileLocation#getFullPath()
	 */
	public String getFullPath() {
		return fullPath;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFileLocation#getURI()
	 */
	public URI getURI() {
		return uri;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IIndexFileLocation) {
			return uri.equals(((IIndexFileLocation) obj).getURI());
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return uri.hashCode();
	}
	
	@Override
	public String toString() {
		if (fullPath == null) {
			return uri.toString();
		}
		return fullPath.toString() + " (" + uri.toString() + ')'; //$NON-NLS-1$
	}
}
