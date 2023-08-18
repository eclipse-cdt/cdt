/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.emulator;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.tm.internal.terminal.control.ITerminalListener3.TerminalTitleRequestor;
import org.eclipse.tm.internal.terminal.control.impl.ITerminalControlForText;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class MockTerminalControlForText implements ITerminalControlForText {
	private List<String> allTitles = new ArrayList<>();

	@Override
	public TerminalState getState() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setState(TerminalState state) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTerminalTitle(String title, TerminalTitleRequestor requestor) {
		if (requestor == TerminalTitleRequestor.ANSI) {
			allTitles.add(title);
		}
	}

	public List<String> getAllTitles() {
		return Collections.unmodifiableList(allTitles);
	}

	@Override
	public ITerminalConnector getTerminalConnector() {
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void enableApplicationCursorKeys(boolean enable) {
		throw new UnsupportedOperationException();

	}

}
