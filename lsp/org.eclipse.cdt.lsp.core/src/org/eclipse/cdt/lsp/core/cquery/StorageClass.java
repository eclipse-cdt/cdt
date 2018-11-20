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

public enum StorageClass {
	Invalid(0), None(1), Extern(2), Static(3), PrivateExtern(4), Auto(5), Register(6);

	private int value;

	public int getValue() {
		return value;
	}

	StorageClass(int value) {
		this.value = value;
	}

	public static StorageClass forValue(int value) {
		StorageClass[] allValues = StorageClass.values();
		if (value < 1 || value > allValues.length) {
			throw new IllegalArgumentException("Illegal enum value: " + value); //$NON-NLS-1$
		}
		return allValues[value - 1];
	}
}