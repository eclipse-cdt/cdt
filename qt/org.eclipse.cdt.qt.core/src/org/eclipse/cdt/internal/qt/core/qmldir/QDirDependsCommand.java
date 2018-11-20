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

import org.eclipse.cdt.qt.core.qmldir.IQDirDependsCommand;

public class QDirDependsCommand extends QDirASTNode implements IQDirDependsCommand {

	private QDirWord moduleName;
	private QDirVersion version;

	public void setModuleIdentifier(QDirWord value) {
		this.moduleName = value;
	}

	@Override
	public QDirWord getModuleIdentifier() {
		return moduleName;
	}

	public void setInitialVersion(QDirVersion value) {
		this.version = value;
	}

	@Override
	public QDirVersion getInitialVersion() {
		return version;
	}

}
