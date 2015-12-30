/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.launch;

import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleParser;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.TextConsole;

public abstract class ArduinoPatternMatchListener implements IPatternMatchListener {

	protected final ArduinoConsole arduinoConsole;
	protected final ArduinoConsoleParser parser;

	protected TextConsole textConsole;

	public ArduinoPatternMatchListener(ArduinoConsole arduinoConsole, ArduinoConsoleParser parser) {
		this.arduinoConsole = arduinoConsole;
		this.parser = parser;
	}

	@Override
	public void connect(TextConsole console) {
		this.textConsole = console;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public String getPattern() {
		return parser.getPattern();
	}

	@Override
	public int getCompilerFlags() {
		return parser.getCompilerFlags();
	}

	@Override
	public String getLineQualifier() {
		return parser.getLineQualifier();
	}

}
