/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
