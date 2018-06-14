/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language.LSExtendedResponses;

enum StorageType {
	Invalid(0), None(1), Extern(2), Static(3), PrivateExtern(4), Auto(5), Register(6);

	private int value;

	StorageType(int value) {
		this.value = value;
	}

	public static StorageType forValue(int value) {
		StorageType[] allValues = StorageType.values();
		if (value < 1 || value > allValues.length) {
			throw new IllegalArgumentException("Illegal enum value: " + value);
		}
		return allValues[value - 1];
	}
}