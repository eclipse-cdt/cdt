/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.serial;

/**
 * @since 5.8
 */
public enum ByteSize {

	B5(5),
	B6(6),
	B7(7),
	B8(8);
	
	private final int size;
	
	private ByteSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
}
