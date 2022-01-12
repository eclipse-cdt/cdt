/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.viewsupport;

public class AsyncTreeWorkInProgressNode {

	private Object fParent;

	public AsyncTreeWorkInProgressNode(Object parentElement) {
		fParent = parentElement;
	}

	public Object getParent() {
		return fParent;
	}

	@Override
	public String toString() {
		return "..."; //$NON-NLS-1$
	}
}
