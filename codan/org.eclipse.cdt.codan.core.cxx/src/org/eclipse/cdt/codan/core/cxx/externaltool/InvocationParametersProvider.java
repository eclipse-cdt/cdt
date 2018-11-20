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
 * Default implementation of <code>{@link InvocationParameters}</code>
 *
 * @since 2.1
 */
public class InvocationParametersProvider implements IInvocationParametersProvider {
	/**
	 * Creates the parameters to pass when invoking an external tool.
	 * <p>
	 * In this implementation:
	 * <ul>
	 * <li>the <em>actual</em> file to process is the same as the <em>original</em> file</li>
	 * <li>the path of the actual file is its absolute path in the file system</li>
	 * <li>the working directory is {@code null}</li>
	 * </ul>
	 * @param fileToProcess the file to process.
	 * @return the created parameters.
	 */
	@Override
	public InvocationParameters createParameters(IResource fileToProcess) {
		String path = fileToProcess.getLocation().toOSString();
		return new InvocationParameters(fileToProcess, fileToProcess, path, null);
	}
}
