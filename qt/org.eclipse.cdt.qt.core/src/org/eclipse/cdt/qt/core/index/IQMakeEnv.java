/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.core.index;

/**
 * Represents a QMake environment. It is usually created by IQMakeEnvProvider.createEnv() method for a specific project configuration.
 */
public interface IQMakeEnv {

	/**
	 * Notifies that this environment is no longer used.
	 */
	void destroy();

	/**
	 * Returns an actual QMake environment information that is used for a single qmake run to retrieve QMake information.
	 *
	 * @return the actual QMake environment information
	 */
	QMakeEnvInfo getQMakeEnvInfo();

}
