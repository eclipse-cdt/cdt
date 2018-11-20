/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import org.eclipse.core.resources.IProject;

/**
 * A factory for QMakeProjectInfo instances.
 */
public final class QMakeProjectInfoFactory {

	private QMakeProjectInfoFactory() {
	}

	/**
	 * Provides a IQMakeProjectInfo for an active project configuration
	 * in a specified project.
	 *
	 * @param project the project
	 * @return IQMakeProjectInfo representing an activate project configuration
	 *         in the specified project.
	 */
	public static IQMakeProjectInfo getForActiveConfigurationIn(IProject project) {
		return QMakeProjectInfoManager.getQMakeProjectInfoFor(project);
	}

}
