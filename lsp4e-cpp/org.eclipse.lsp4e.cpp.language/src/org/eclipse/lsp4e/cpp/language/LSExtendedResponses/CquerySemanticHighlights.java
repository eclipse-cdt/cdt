/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.LSExtendedResponses;

import java.net.URI;
import java.util.List;

public class CquerySemanticHighlights {
	private URI uri;
	private List<HighlightSymbol> symbols;

	public CquerySemanticHighlights(URI uri, List<HighlightSymbol> symbols) {
		this.uri = uri;
		this.symbols = symbols;
	}
}
