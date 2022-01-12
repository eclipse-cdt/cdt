/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter.align;

/**
 * Exception used to backtrack and break available alignments
 * When the exception is thrown, it is assumed that some alignment will be changed.
 *
 * @since 4.0
 */
public class AlignmentException extends RuntimeException {
	private static final long serialVersionUID = -1081237230006524966L;
	public static final int LINE_TOO_LONG = 1;
	public static final int ALIGN_TOO_SMALL = 2;

	int reason;
	int value;
	public int relativeDepth;

	public AlignmentException(int reason, int relativeDepth) {
		this(reason, 0, relativeDepth);
	}

	public AlignmentException(int reason, int value, int relativeDepth) {
		this.reason = reason;
		this.value = value;
		this.relativeDepth = relativeDepth;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(40);
		switch (reason) {
		case LINE_TOO_LONG:
			buffer.append("LINE_TOO_LONG"); //$NON-NLS-1$
			break;
		case ALIGN_TOO_SMALL:
			buffer.append("ALIGN_TOO_SMALL"); //$NON-NLS-1$
			break;
		}
		buffer.append("<relativeDepth: ").append(relativeDepth).append(">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return buffer.toString();
	}
}
