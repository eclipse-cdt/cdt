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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.qt.core.qmldir.IQDirAST;
import org.eclipse.cdt.qt.core.qmldir.IQDirCommand;

public class QDirAST extends QDirASTNode implements IQDirAST {
	private final List<IQDirCommand> commands;

	public QDirAST() {
		commands = new ArrayList<>();
	}

	public void addCommand(IQDirCommand command) {
		commands.add(command);
	}

	@Override
	public List<IQDirCommand> getCommands() {
		return Collections.unmodifiableList(commands);
	}

}
