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
package org.eclipse.cdt.codan.core.cxx.externaltool;

import org.eclipse.core.resources.IResource;

/**
 * Provides the parameters to pass when invoking an external tool.
 *
 * @since 2.1
 */
public interface IInvocationParametersProvider {
	/**
	 * Creates the parameters to pass when invoking an external tool.
	 * @param fileToProcess the file to process.
	 * @return the created parameters.
	 * @throws Throwable if something goes wrong.
	 */
	InvocationParameters createParameters(IResource fileToProcess) throws Throwable;
}
