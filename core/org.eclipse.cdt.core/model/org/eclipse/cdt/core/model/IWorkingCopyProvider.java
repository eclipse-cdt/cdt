/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Defines a simple interface in order to provide
 * a level of abstraction between the Core and UI
 * code.
 */
public interface IWorkingCopyProvider {
	public IWorkingCopy[] getWorkingCopies();
}
