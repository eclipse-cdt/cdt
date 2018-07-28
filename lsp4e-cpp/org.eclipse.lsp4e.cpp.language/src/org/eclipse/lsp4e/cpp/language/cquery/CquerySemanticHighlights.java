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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CquerySemanticHighlights {
	private URI uri;
	private List<HighlightSymbol> symbols;
	public static ConcurrentMap<URI,List<HighlightSymbol>> semanticHighlightingsMap = new ConcurrentHashMap<>(16, 0.75f, 1);

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
