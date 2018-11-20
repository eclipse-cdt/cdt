/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import org.eclipse.cdt.debug.ui.memory.floatingpoint.FPutilities.Endian;
import org.eclipse.cdt.debug.ui.memory.floatingpoint.FPutilities.FPDataType;
import org.eclipse.cdt.debug.ui.memory.floatingpoint.FPutilities.Justification;

public class FPDisplayCharacteristics {
	private FPDataType dataType;
	private Endian endian;
	private int displayedPrecision;
	private Justification justification;

	// Constructors

	FPDisplayCharacteristics() {
		// Default values

		this.dataType = FPDataType.FLOAT;
		this.endian = Endian.LITTLE;
		this.displayedPrecision = FPDataType.FLOAT.getDisplayedPrecision();
		this.justification = Justification.LEFT;
	}

	public FPDisplayCharacteristics(FPDataType dataType, Endian endian, Justification justification) {
		this.dataType = dataType;
		this.endian = endian;
		this.justification = justification;
	}

	// Getters and Setters

	public FPDataType getDataType() {
		return dataType;
	}

	public void setDataType(FPDataType dataType) {
		this.dataType = dataType;
	}

	public Endian getEndian() {
		return endian;
	}

	public void setEndian(Endian endian) {
		this.endian = endian;
	}

	public int getDisplayedPrecision() {
		return displayedPrecision;
	}

	public void setDisplayedPrecision(int displayedPrecision) {
		this.displayedPrecision = displayedPrecision;
	}

	public Justification getJustification() {
		return justification;
	}

	public void setJustification(Justification justification) {
		this.justification = justification;
	}
}
