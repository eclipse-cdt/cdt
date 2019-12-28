/*******************************************************************************
 * Copyright (c) 2018-2019 Manish Khurana, Nathan Ridge and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.cquery.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cquery.ExtendedSymbolKindType;
import org.eclipse.cdt.cquery.StorageClass;
import org.eclipse.cdt.cquery.SymbolRole;
import org.eclipse.cdt.internal.cquery.CquerySymbolKind;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.lsp4j.SymbolKind;

//FIXME: AF: rework this compilation unit from a container of static functions to a normal type
public class HighlightingNames {

	private static Map<Integer, String> semanticHighlightSymbolsMap = new HashMap<>();

	static {
		semanticHighlightSymbolsMap.put(SymbolKind.Namespace.getValue(), SemanticHighlightings.NAMESPACE);
		semanticHighlightSymbolsMap.put(SymbolKind.Class.getValue(), SemanticHighlightings.CLASS);
		semanticHighlightSymbolsMap.put(SymbolKind.Enum.getValue(), SemanticHighlightings.ENUM);
		semanticHighlightSymbolsMap.put(SymbolKind.EnumMember.getValue(), SemanticHighlightings.ENUMERATOR);
		semanticHighlightSymbolsMap.put(SymbolKind.Struct.getValue(), SemanticHighlightings.CLASS);
		semanticHighlightSymbolsMap.put(SymbolKind.TypeParameter.getValue(), SemanticHighlightings.TEMPLATE_PARAMETER);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.TypeAlias.getValue(), SemanticHighlightings.TYPEDEF);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.Parameter.getValue(),
				SemanticHighlightings.PARAMETER_VARIABLE);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.StaticMethod.getValue(),
				SemanticHighlightings.STATIC_METHOD_INVOCATION);
		semanticHighlightSymbolsMap.put(CquerySymbolKind.Macro.getValue(), SemanticHighlightings.MACRO_DEFINITION);
	}

	public static String getHighlightingName(ExtendedSymbolKindType kind, ExtendedSymbolKindType parentKind,
			StorageClass storage, int role) {
		// semanticHighlightSymbolsMap contains mappings where the color is determined entirely
		// by the symbol kind.
		// The additional checks below handle cases where the color also depends on the parent kind,
		// storage class, or role.
		String highlightingName = semanticHighlightSymbolsMap.get(kind.getValue());
		if (highlightingName == null) {
			if (kind.getValue() == SymbolKind.Variable.getValue()) {
				if (parentKind.getValue() == SymbolKind.Function.getValue()
						|| parentKind.getValue() == SymbolKind.Method.getValue()
						|| parentKind.getValue() == SymbolKind.Constructor.getValue()) {
					highlightingName = isDeclaration(role) ? SemanticHighlightings.LOCAL_VARIABLE_DECLARATION
							: SemanticHighlightings.LOCAL_VARIABLE;
				} else {
					highlightingName = SemanticHighlightings.GLOBAL_VARIABLE;
				}
			} else if (kind.getValue() == SymbolKind.Field.getValue()) {
				if (storage == StorageClass.Static) {
					highlightingName = SemanticHighlightings.STATIC_FIELD;
				} else {
					highlightingName = SemanticHighlightings.FIELD;
				}
			} else if (kind.getValue() == SymbolKind.Function.getValue()) {
				highlightingName = isDeclaration(role) ? SemanticHighlightings.FUNCTION_DECLARATION
						: SemanticHighlightings.FUNCTION;
			} else if (kind.getValue() == SymbolKind.Method.getValue()
					|| kind.getValue() == SymbolKind.Constructor.getValue()) {
				highlightingName = isDeclaration(role) ? SemanticHighlightings.METHOD_DECLARATION
						: SemanticHighlightings.METHOD;
			}
		}
		return highlightingName;
	}

	private static boolean isDeclaration(int role) {
		return (role & SymbolRole.Declaration) != 0 || (role & SymbolRole.Definition) != 0;
	}

}
