/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

public class CompositingNotImplementedError extends Error {
	private static final long serialVersionUID = -7296443480526626589L;

	public CompositingNotImplementedError() {
		super();
		printStackTrace();
	}

	public CompositingNotImplementedError(String msg) {
		super(msg);
		printStackTrace();
	}
}
