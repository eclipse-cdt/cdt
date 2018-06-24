/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.cquery;

import java.util.List;

import org.eclipse.lsp4j.Range;

public class HighlightSymbol {
	private int stableId;
	private ExtendedSymbolKindType parentKind;
	private ExtendedSymbolKindType kind;
	private StorageClass storage;
	private List<Range> ranges;

	public HighlightSymbol(int stableId, ExtendedSymbolKindType parentKind, ExtendedSymbolKindType kind,
			StorageClass storage, List<Range> ranges) {
		this.stableId = stableId;
		this.parentKind = parentKind;
		this.kind = kind;
		this.storage = storage;
		this.ranges = ranges;
	}

	public int getStableId() {
		return stableId;
	}

	public ExtendedSymbolKindType getParentKind() {
		return parentKind;
	}

	public ExtendedSymbolKindType getKind() {
		return kind;
	}

	public StorageClass getStorage() {
		return storage;
	}

	public List<Range> getRanges() {
		return ranges;
	}
}