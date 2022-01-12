/*******************************************************************************
 * Copyright (c) 2002, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.jface.text.ITextStore;

public class ConsoleOutputTextStore implements ITextStore {

	private StringBuilder fBuffer;

	public ConsoleOutputTextStore(int bufferSize) {
		fBuffer = new StringBuilder(bufferSize);
	}

	/**
	 * @see ITextStore#get(int)
	 */
	@Override
	public char get(int pos) {
		return fBuffer.charAt(pos);
	}

	/**
	 * @see ITextStore#get(int, int)
	 */
	@Override
	public String get(int pos, int length) {
		return fBuffer.substring(pos, pos + length);
	}

	/**
	 * @see ITextStore#getLength()
	 */
	@Override
	public int getLength() {
		return fBuffer.length();
	}

	/**
	 * @see ITextStore#replace(int, int, String)
	 */
	@Override
	public void replace(int pos, int length, String text) {
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		fBuffer.replace(pos, pos + length, text);
	}

	/**
	 * @see ITextStore#set(String)
	 */
	@Override
	public void set(String text) {
		fBuffer = new StringBuilder(text);
	}

	/**
	 * @see StringBuilder#ensureCapacity(int)
	 */
	public void setMinimalBufferSize(int bufferSize) {
		fBuffer.ensureCapacity(bufferSize);
	}
}
