/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IResource;

/**
 * @author sam.robb
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IMarkerGenerator {
	int SEVERITY_INFO = 0;
	int SEVERITY_WARNING = 1;
	int SEVERITY_ERROR_RESOURCE = 2;
	int SEVERITY_ERROR_BUILD = 3;

	/**
	 * callback from Output Parser
	 * @deprecated Use 	public void addMarker(org.eclipse.cdt.core.ProblemMarkerInfo problem) instead.
	 */
	@Deprecated
	void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar);
	void addMarker(ProblemMarkerInfo problemMarkerInfo);
}
