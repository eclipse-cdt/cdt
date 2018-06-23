/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.cquery;

enum CquerySymbolKind {
	Unknown(0), TypeAlias(252), Parameter(253), StaticMethod(254), Macro(255);

	private int value;

	public int getValue() {
		return value;
	}

	private CquerySymbolKind(int value) {
		this.value = value;
	}

	public static CquerySymbolKind forValue(int value) {
		switch (value) {
		case 0:
			return CquerySymbolKind.Unknown;
		case 252:
			return CquerySymbolKind.TypeAlias;
		case 253:
			return CquerySymbolKind.Parameter;
		case 254:
			return CquerySymbolKind.StaticMethod;
		case 255:
			return CquerySymbolKind.Macro;
		default:
			throw new IllegalArgumentException("Illegal value for cquery symbol kind"); //$NON-NLS-1$
		}
	}
}