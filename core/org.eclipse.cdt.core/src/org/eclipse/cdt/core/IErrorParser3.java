/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * @since 5.4
 */
public interface IErrorParser3 extends IErrorParser {
	/**
	 * Called to let the parser know that the end of the error stream has been reached.
	 * Can be used by the parser to flush its internal buffers.
	 */
	void shutdown();
}
