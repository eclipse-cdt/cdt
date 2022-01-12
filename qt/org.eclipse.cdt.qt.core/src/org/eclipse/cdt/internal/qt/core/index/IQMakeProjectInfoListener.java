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
 * A listener used for notifying that a QMake information provided by IQMakeProjectInfo might have changed.
 */
public interface IQMakeProjectInfoListener {

	/**
	 * Notifies that a QMake information provided by IQMakeProjectInfo might have changed.
	 * A new QMake information can be read via IQMakeProjectInfo.getQMakeInfo() method.
	 *
	 * Note that this method might be called even after the listener is removed from
	 * IQMakeProjectInfo. To prevent this, your implementation should maintain a flag
	 * representing whether qmakeInfoChanged() notifications should be processed or not.
	 */
	void qmakeInfoChanged();

}
