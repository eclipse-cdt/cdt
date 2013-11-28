/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.core.index;

/**
 * A listener used for notifying that a QMake information provided by IQMakeProjectInfo might have changed.
 */
public interface IQMakeProjectInfoListener {

	/**
	 * Notifies that a QMake information provided by IQMakeProjectInfo might have changed.
	 * A new QMake information can be read via IQMakeProjectInfo.getQMakeInfo() method.
	 */
	void qmakeInfoChanged();

}
