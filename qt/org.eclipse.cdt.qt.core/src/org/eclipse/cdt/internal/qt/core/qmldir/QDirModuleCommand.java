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

import org.eclipse.cdt.qt.core.qmldir.IQDirModuleCommand;

public class QDirModuleCommand extends QDirASTNode implements IQDirModuleCommand {
	private QDirWord identifier;

	public void setModuleIdentifier(QDirWord value) {
		this.identifier = value;
	}

	@Override
	public QDirWord getModuleIdentifier() {
		return identifier;
	}

}
