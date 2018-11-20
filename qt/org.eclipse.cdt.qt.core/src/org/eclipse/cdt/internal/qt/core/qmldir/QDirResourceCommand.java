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

import org.eclipse.cdt.qt.core.qmldir.IQDirResourceCommand;

public class QDirResourceCommand extends QDirASTNode implements IQDirResourceCommand {
	private QDirWord typeName;
	private QDirVersion version;
	private QDirWord file;

	public void setResourceIdentifier(QDirWord value) {
		this.typeName = value;
	}

	@Override
	public QDirWord getResourceIdentifier() {
		return typeName;
	}

	public void setInitialVersion(QDirVersion value) {
		this.version = value;
	}

	@Override
	public QDirVersion getInitialVersion() {
		return version;
	}

	public void setFile(QDirWord value) {
		this.file = value;
	}

	@Override
	public QDirWord getFile() {
		return file;
	}

}
