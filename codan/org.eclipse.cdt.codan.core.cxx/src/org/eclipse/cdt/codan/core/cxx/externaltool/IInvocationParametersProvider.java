/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.externaltool;

import org.eclipse.core.resources.IResource;

/**
 * Provides the parameters to pass when invoking an external tool.
 *
 * @author alruiz@google.com (Alex Ruiz)
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
