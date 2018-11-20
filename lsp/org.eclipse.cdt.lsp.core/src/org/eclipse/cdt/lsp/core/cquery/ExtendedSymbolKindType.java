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

import java.lang.reflect.Type;

import org.eclipse.lsp4j.SymbolKind;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(ExtendedSymbolKindParsers.class)
public class ExtendedSymbolKindType {
	int value;
	transient boolean isProtocolSymbol;

	public ExtendedSymbolKindType(int _v) {
		try {
			SymbolKind.forValue(_v);
			value = _v;
			isProtocolSymbol = true;
		} catch (IllegalArgumentException e) {
			try {
				CquerySymbolKind.forValue(_v);
				value = _v;
				isProtocolSymbol = false;
			} catch (IllegalArgumentException y) {
				throw new IllegalArgumentException("Illegal value for SymbolKind"); //$NON-NLS-1$
			}
		}
	}

	public int getValue() {
		return value;
	}
}

class ExtendedSymbolKindParsers
		implements JsonDeserializer<ExtendedSymbolKindType>, JsonSerializer<ExtendedSymbolKindType> {
	@Override
	public ExtendedSymbolKindType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		int symbolKindValue = json.getAsInt();
		return new ExtendedSymbolKindType(symbolKindValue);
	}

	@Override
	public JsonElement serialize(ExtendedSymbolKindType src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.getValue());
	}
}
