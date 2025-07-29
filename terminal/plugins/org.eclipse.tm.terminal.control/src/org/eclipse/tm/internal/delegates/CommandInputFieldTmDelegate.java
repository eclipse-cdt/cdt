package org.eclipse.tm.internal.delegates;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.control.ICommandInputField;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;

public class CommandInputFieldTmDelegate implements ICommandInputField {

	private org.eclipse.terminal.control.ICommandInputField delegate;

	public CommandInputFieldTmDelegate(org.eclipse.terminal.control.ICommandInputField inputField) {
		this.delegate = inputField;
	}

	@Override
	public void dispose() {
		delegate.dispose();
	}

	@Override
	public void setFont(Font font) {
		delegate.setFont(font);
	}

	@Override
	public void createControl(Composite parent, ITerminalViewControl terminal) {
		// TODO Auto-generated method stub
		//TODO  delegate.createControl(parent, new TmTer terminal);

	}

}
