/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Base interface for things that are accessed from the {@link QtIndex}.
 */
public interface IQElement {
	/**
	 * Returns the IBinding from the CDT index for the receiver element.
	 */
	public IBinding getBinding();
}
