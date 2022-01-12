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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

public class OptimizedReplaceEdit {
	int offset;
	int length;
	String replacement;

	public OptimizedReplaceEdit(int offset, int length, CharSequence replacement) {
		this.offset = offset;
		this.length = length;
		this.replacement = replacement.toString();
	}

	@Override
	public String toString() {
		return "(" + this.offset + ", length " + this.length + " :>" + this.replacement + "<"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}
}
