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

public class HighlightSymbol {
	private int stableId;
	private ExtendedSymbolKindType parentKind;
	private ExtendedSymbolKindType kind;
	private StorageClass storage;
	private List<Range> ranges;
	public static Map<Integer, String> semanticHighlightSymbolsMap = new HashMap<>();

	static {
		semanticHighlightSymbolsMap.put(SymbolKind.Namespace.getValue(), SemanticHighlightings.NAMESPACE);
		semanticHighlightSymbolsMap.put(SymbolKind.Class.getValue(), SemanticHighlightings.CLASS);
		semanticHighlightSymbolsMap.put(SymbolKind.Method.getValue(), SemanticHighlightings.METHOD);
		semanticHighlightSymbolsMap.put(SymbolKind.Constructor.getValue(), SemanticHighlightings.METHOD);
		semanticHighlightSymbolsMap.put(SymbolKind.Enum.getValue(), SemanticHighlightings.ENUM);
		semanticHighlightSymbolsMap.put(SymbolKind.Function.getValue(), SemanticHighlightings.FUNCTION);
		semanticHighlightSymbolsMap.put(SymbolKind.EnumMember.getValue(), SemanticHighlightings.ENUMERATOR);
		semanticHighlightSymbolsMap.put(SymbolKind.Struct.getValue(), SemanticHighlightings.CLASS);
		semanticHighlightSymbolsMap.put(SymbolKind.TypeParameter.getValue(), SemanticHighlightings.TEMPLATE_PARAMETER);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.TypeAlias.getValue(), SemanticHighlightings.TYPEDEF);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.Parameter.getValue(), SemanticHighlightings.PARAMETER_VARIABLE);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.StaticMethod.getValue(), SemanticHighlightings.STATIC_METHOD_INVOCATION);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.Macro.getValue(), SemanticHighlightings.MACRO_DEFINITION);
	}

	public static String getHighlightingName(ExtendedSymbolKindType kind, ExtendedSymbolKindType parentKind, StorageClass storage) {
		String highlightingName = semanticHighlightSymbolsMap.get(kind.getValue());
		if (highlightingName == null) {
			if (kind.getValue() == SymbolKind.Variable.getValue()) {
				if (parentKind.getValue() == SymbolKind.Function.getValue()
						|| parentKind.getValue() == SymbolKind.Method.getValue()
						|| parentKind.getValue() == SymbolKind.Constructor.getValue()) {

					highlightingName = SemanticHighlightings.LOCAL_VARIABLE;
				} else {
					highlightingName = SemanticHighlightings.GLOBAL_VARIABLE;
			}
			} else if (kind.getValue() == SymbolKind.Field.getValue()) {
				if (storage == StorageClass.Static) {
					highlightingName = SemanticHighlightings.STATIC_FIELD;
				} else {
				highlightingName = SemanticHighlightings.FIELD;
				}
			}
		}
		return highlightingName;
	}

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