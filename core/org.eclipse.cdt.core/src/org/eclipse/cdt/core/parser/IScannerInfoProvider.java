/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import org.eclipse.core.resources.IResource;

public interface IScannerInfoProvider {

	/**
	 * The receiver will answer the current state of the build information for the
	 * resource specified in the argument.
	 */
	public IScannerInfo getScannerInformation(IResource resource);

	/**
	 * The receiver will register the listener specified in the argument
	 * to receive change notifications when the information for the
	 * <code>IResource</code> it is responsible for changes.
	 *
	 * @param listener
	 */
	public void subscribe(IResource resource, IScannerInfoChangeListener listener);

	/**
	 * The receiver will no longer notify the listener specified in
	 * the argument when information about the reource it is responsible
	 * for changes.
	 *
	 * @param listener
	 */
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener);
}
