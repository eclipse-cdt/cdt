/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.cquery;

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