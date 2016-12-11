/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

/**
 * @author sam.robb
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IMarkerGenerator {
	int SEVERITY_INFO = IMarker.SEVERITY_INFO; // 0
	int SEVERITY_WARNING = IMarker.SEVERITY_WARNING; // 1
	int SEVERITY_ERROR_RESOURCE = IMarker.SEVERITY_ERROR; // 2
	int SEVERITY_ERROR_BUILD = 3;

	/**
	 * @deprecated Use 	public void addMarker(org.eclipse.cdt.core.ProblemMarkerInfo problem) instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar);
	void addMarker(ProblemMarkerInfo problemMarkerInfo);
	default void deDuplicate() {
		/*
		 * for implementation that don't implement deDuplicate, they should not
		 * pay attention to ProblemMarkerInfo.deferDeDuplication
		 */
	}
}
