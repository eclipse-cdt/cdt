/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.cquery;

import java.net.URI;
import java.util.List;

import org.eclipse.lsp4j.Range;

public class CqueryInactiveRegions {
	private URI uri;
	private List<Range> inactiveRegions;

	public CqueryInactiveRegions(URI uri, List<Range> inactiveRegions) {
		this.uri = uri;
		this.inactiveRegions = inactiveRegions;
	}

	public URI getUri() {
		return uri;
	}
	public List<Range> getInactiveRegions() {
		return inactiveRegions;
	}
}
