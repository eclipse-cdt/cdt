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

/**
 * Represents provider of QMake information.
 *
 * This class is not meant to be implemented.
 */
public interface IQMakeProjectInfo {

	/**
	 * Add a listener that notifies about possible changes of IQMakeInfo retrieved from getActualInfo() method.
	 *
	 * @param listener the listener
	 */
	void addListener(IQMakeProjectInfoListener listener);

	/**
	 * Removes a listener that was added via addListener() method.
	 *
	 * @param listener the listener
	 */
	void removeListener(IQMakeProjectInfoListener listener);

	/**
	 * Returns an actual QMake information.
	 *
	 * @return non-null IQMakeInfo instance representing the actual QMake information
	 */
	IQMakeInfo getActualInfo();

	/**
	 * Updates the actual QMake information and returns it.
	 *
	 * Note that this is a long-term operation and the method call is blocked until an actual QMake information is calculated.
	 *
	 * @return non-null IQMakeInfo instance representing the actual QMake information calculated at the time of this method call.
	 */
	IQMakeInfo updateActualInfo();

}
