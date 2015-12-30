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
