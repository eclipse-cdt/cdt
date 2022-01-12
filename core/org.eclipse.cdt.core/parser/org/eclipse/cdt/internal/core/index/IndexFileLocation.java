/*******************************************************************************
 * Copyright (c) 2006, 2012 Symbian Software Ltd. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - initial API and implementation
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

	@Override
	public String getFullPath() {
		return fullPath;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IIndexFileLocation) {
			return uri.equals(((IIndexFileLocation) obj).getURI());
		}
		return false;
	}

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
