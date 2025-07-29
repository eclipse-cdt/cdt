package org.eclipse.tm.internal.delegates;

import org.eclipse.terminal.control.TerminalTitleRequestor;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class TmTerminalListenerDelegate implements org.eclipse.terminal.control.ITerminalListener {
	private ITerminalListener delegate;

	public TmTerminalListenerDelegate(ITerminalListener delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setState(org.eclipse.terminal.connector.TerminalState state) {
		if (state == org.eclipse.terminal.connector.TerminalState.CONNECTED) {
			delegate.setState(TerminalState.CONNECTED);
		} else if (state == org.eclipse.terminal.connector.TerminalState.CONNECTING) {
			delegate.setState(TerminalState.CONNECTING);
		} else {
			delegate.setState(TerminalState.CLOSED);
		}
	}

	@Override
	public void setTerminalSelectionChanged() {
	}

	@Override
	public void setTerminalTitle(String title, TerminalTitleRequestor requestor) {
		delegate.setTerminalTitle(title);
	}
}
