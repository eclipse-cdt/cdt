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

package org.eclipse.cdt.cquery;

import java.net.URI;
import java.util.List;

public class CquerySemanticHighlights {
	private URI uri;
	private List<HighlightSymbol> symbols;

	public URI getUri() {
		return uri;
	}

	public List<HighlightSymbol> getSymbols() {
		return symbols;
	}

	public CquerySemanticHighlights(URI uri, List<HighlightSymbol> symbols) {
		this.uri = uri;
		this.symbols = symbols;
	}
}
