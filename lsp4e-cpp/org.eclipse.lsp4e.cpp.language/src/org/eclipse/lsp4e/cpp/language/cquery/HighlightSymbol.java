/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.cquery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;

@SuppressWarnings("restriction")
public class HighlightSymbol {
	private int stableId;
	private ExtendedSymbolKindType parentKind;
	private ExtendedSymbolKindType kind;
	private StorageClass storage;
	private List<Range> ranges;
	public static Map<Integer, String> semanticHighlightSymbolsMap = new HashMap<Integer, String>() {{
		put(SymbolKind.Namespace.getValue(), SemanticHighlightings.NAMESPACE);
		put(SymbolKind.Class.getValue(), SemanticHighlightings.CLASS);
		put(SymbolKind.Method.getValue(), SemanticHighlightings.METHOD);
		put(SymbolKind.Constructor.getValue(), SemanticHighlightings.METHOD);
		put(SymbolKind.Enum.getValue(), SemanticHighlightings.ENUM);
		put(SymbolKind.Function.getValue(), SemanticHighlightings.FUNCTION);
		put(SymbolKind.Struct.getValue(), SemanticHighlightings.CLASS);
		put(SymbolKind.TypeParameter.getValue(), SemanticHighlightings.TEMPLATE_PARAMETER);
		put(CquerySymbolKind.TypeAlias.getValue(), SemanticHighlightings.TYPEDEF);
		put(CquerySymbolKind.Parameter.getValue(), SemanticHighlightings.PARAMETER_VARIABLE);
		put(CquerySymbolKind.StaticMethod.getValue(), SemanticHighlightings.STATIC_METHOD_INVOCATION);
		put(CquerySymbolKind.Macro.getValue(), SemanticHighlightings.MACRO_DEFINITION);
	}
	};
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