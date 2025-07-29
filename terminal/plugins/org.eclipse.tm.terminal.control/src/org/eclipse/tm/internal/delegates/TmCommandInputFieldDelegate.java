package org.eclipse.tm.internal.delegates;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.control.ICommandInputField;

public class TmCommandInputFieldDelegate implements org.eclipse.terminal.control.ICommandInputField {

	private ICommandInputField delegate;

	public TmCommandInputFieldDelegate(ICommandInputField inputField) {
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
	public void createControl(Composite parent, org.eclipse.terminal.control.ITerminalViewControl terminal) {
		delegate.createControl(parent, new TerminalViewControlTmDelegate(terminal));
	}

}
