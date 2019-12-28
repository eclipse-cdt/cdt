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

import java.util.List;

import org.eclipse.lsp4j.Range;

public class HighlightSymbol {
	private int stableId;
	private ExtendedSymbolKindType parentKind;
	private ExtendedSymbolKindType kind;
	private StorageClass storage;
	private List<Range> ranges;
	private Integer role;

	public HighlightSymbol(int stableId, ExtendedSymbolKindType parentKind, ExtendedSymbolKindType kind,
			StorageClass storage, Integer role, List<Range> ranges) {
		this.stableId = stableId;
		this.parentKind = parentKind;
		this.kind = kind;
		this.storage = storage;
		this.role = role;
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

	public Integer getRole() {
		return role;
	}

	public List<Range> getRanges() {
		return ranges;
	}
}