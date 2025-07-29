package org.eclipse.tm.internal.delegates;

import org.eclipse.tm.internal.terminal.control.ITerminalListener3;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class TerminalListenerTmDelegate implements ITerminalListener3 {
	private org.eclipse.terminal.control.ITerminalListener delegate;

	public TerminalListenerTmDelegate(org.eclipse.terminal.control.ITerminalListener delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setState(TerminalState state) {
		if (state == TerminalState.CONNECTED) {
			delegate.setState(org.eclipse.terminal.connector.TerminalState.CONNECTED);
		} else if (state == TerminalState.CONNECTING) {
			delegate.setState(org.eclipse.terminal.connector.TerminalState.CONNECTING);
		} else {
			delegate.setState(org.eclipse.terminal.connector.TerminalState.CLOSED);
		}
	}

	@Override
	public void setTerminalSelectionChanged() {
	}

	@Override
	public void setTerminalTitle(String title, TerminalTitleRequestor requestor) {
		if (requestor == TerminalTitleRequestor.ANSI) {
			delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.ANSI);
		}
		if (requestor == TerminalTitleRequestor.MENU) {
			delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.MENU);
		}
		if (requestor == TerminalTitleRequestor.OTHER) {
			delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.OTHER);
		}
	}

	@Override
	public void setTerminalTitle(String title) {
		delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.OTHER);
	}
}
