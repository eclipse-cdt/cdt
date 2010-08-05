/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.core.resources.IResource;

/**
 * Implementation of ICheckerInvocationContext
 */
public class CheckerInvocationContext implements ICheckerInvocationContext {
	private IResource resource;
	private IProblemReporter sessionReporter;

	/**
	 * @param resource
	 * @param sessionReporter
	 */
	public CheckerInvocationContext(IResource resource,
			IProblemReporter sessionReporter) {
		this.resource = resource;
		this.sessionReporter = sessionReporter;
	}

	public IResource getResource() {
		return resource;
	}

	public IProblemReporter getProblemReporter() {
		return sessionReporter;
	}
}
