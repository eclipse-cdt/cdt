/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.qmldir;

import org.eclipse.cdt.qt.core.qmldir.IQDirPluginCommand;

public class QDirPluginCommand extends QDirASTNode implements IQDirPluginCommand {

	private QDirWord qid;
	private QDirWord path;

	public void setName(QDirWord value) {
		this.qid = value;
	}

	@Override
	public QDirWord getName() {
		return qid;
	}

	public void setPath(QDirWord value) {
		this.path = value;
	}

	@Override
	public QDirWord getPath() {
		return path;
	}

}
