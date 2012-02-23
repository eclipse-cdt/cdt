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
package org.eclipse.cdt.codan.core.externaltool;

import org.eclipse.cdt.codan.core.model.IProblemLocation;

/**
 * Reports problems found in code, reported by an external tool.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public interface IProblemDisplay {
	/**
	 * Reports a problem found by an external tool.
	 * @param location the problem's location (e.g. file and line number.)
	 * @param description the description of the problem.
	 */
	void reportProblem(IProblemLocation location, String description);

	/**
	 * Reports a problem found by an external tool.
	 * @param location the problem's location (e.g. file and line number.)
	 * @param description the description of the problem.
	 * @param severity the problem's severity (e.g. "error", "warning", etc.)
	 */
	void reportProblem(IProblemLocation location, String description, String severity);
}
