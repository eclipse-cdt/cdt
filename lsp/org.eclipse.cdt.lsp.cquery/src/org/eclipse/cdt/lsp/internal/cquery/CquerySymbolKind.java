/*******************************************************************************
 * Copyright (c) 2018, 2020 Manish Khurana, Nathan Ridge and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.cquery;

import org.eclipse.osgi.util.NLS;

public enum CquerySymbolKind {
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
			throw new IllegalArgumentException(NLS.bind(CqueryMessages.CquerySymbolKind_e_illegal_value, value));
		}
	}
}