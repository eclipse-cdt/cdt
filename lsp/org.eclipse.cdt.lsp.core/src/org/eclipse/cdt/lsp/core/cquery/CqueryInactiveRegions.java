/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core.cquery;

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
