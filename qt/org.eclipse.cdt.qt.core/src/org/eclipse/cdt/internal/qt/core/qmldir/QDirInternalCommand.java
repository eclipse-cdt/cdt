/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.qmldir;

import org.eclipse.cdt.qt.core.qmldir.IQDirInternalCommand;

public class QDirInternalCommand extends QDirASTNode implements IQDirInternalCommand {

	private QDirWord typeName;
	private QDirWord file;

	public void setTypeName(QDirWord value) {
		this.typeName = value;
	}

	@Override
	public QDirWord getTypeName() {
		return typeName;
	}

	public void setFile(QDirWord value) {
		this.file = value;
	}

	@Override
	public QDirWord getFile() {
		return file;
	}

}
