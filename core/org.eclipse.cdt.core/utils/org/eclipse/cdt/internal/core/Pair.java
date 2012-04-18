/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

/**
 * A pair of values.
 */
public class Pair<F, S> {
	public final F first;
	public final S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<"); //$NON-NLS-1$
		builder.append(first);
		builder.append(">, <"); //$NON-NLS-1$
		builder.append(second);
		builder.append(">"); //$NON-NLS-1$
		return builder.toString();
	}
}
