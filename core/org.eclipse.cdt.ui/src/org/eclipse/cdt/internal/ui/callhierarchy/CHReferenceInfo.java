/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.Comparator;

public class CHReferenceInfo {
	public static final Comparator<CHReferenceInfo> COMPARE_OFFSET = new Comparator<CHReferenceInfo>() {
		@Override
		public int compare(CHReferenceInfo r1, CHReferenceInfo r2) {
			return r1.fOffset - r2.fOffset;
		}
	};

	private int fOffset;
	private int fLength;

	public CHReferenceInfo(int offset, int length) {
		fOffset = offset;
		fLength = length;
	}

	public int getOffset() {
		return fOffset;
	}

	public int getLength() {
		return fLength;
	}
}
