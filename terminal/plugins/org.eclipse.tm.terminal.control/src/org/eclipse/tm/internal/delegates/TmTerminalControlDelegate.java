package org.eclipse.tm.internal.delegates;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.terminal.control.ITerminalListener3.TerminalTitleRequestor;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public final class TmTerminalControlDelegate implements org.eclipse.terminal.connector.ITerminalControl {

	private ITerminalControl delegate;

	public TmTerminalControlDelegate(ITerminalControl delegate) {
		this.delegate = delegate;
	}

	@Override
	public org.eclipse.terminal.connector.TerminalState getState() {
		TerminalState state = delegate.getState();
		if (state == TerminalState.CONNECTED) {
			return org.eclipse.terminal.connector.TerminalState.CONNECTED;
		}
		if (state == TerminalState.CONNECTING) {
			return org.eclipse.terminal.connector.TerminalState.CONNECTING;
		}
		return org.eclipse.terminal.connector.TerminalState.CLOSED;
	}

	@Override
	public void setupTerminal(Composite parent) {
		delegate.setupTerminal(parent);
	}

	@Override
	public Shell getShell() {
		return delegate.getShell();
	}

	public void setEncoding(String encoding) throws UnsupportedEncodingException {
		delegate.setEncoding(encoding);
	}

	@Override
	public void setCharset(Charset charset) {
		delegate.setCharset(charset);
	}

	public String getEncoding() {
		return delegate.getEncoding();
	}

	@Override
	public Charset getCharset() {
		return delegate.getCharset();
	}

	@Override
	public void displayTextInTerminal(String text) {
		delegate.displayTextInTerminal(text);
	}

	@Override
	public OutputStream getRemoteToTerminalOutputStream() {
		return delegate.getRemoteToTerminalOutputStream();
	}

	public void setTerminalTitle(String title) {
		delegate.setTerminalTitle(title);
	}

	public void setTerminalTitle(String title, TerminalTitleRequestor requestor) {
		delegate.setTerminalTitle(title, requestor);
	}

	@Override
	public void setMsg(String msg) {
		delegate.setMsg(msg);
	}

	@Override
	public void setConnectOnEnterIfClosed(boolean on) {
		delegate.setConnectOnEnterIfClosed(on);
	}

	@Override
	public boolean isConnectOnEnterIfClosed() {
		return delegate.isConnectOnEnterIfClosed();
	}

	@Override
	public void setVT100LineWrapping(boolean enable) {
		delegate.setVT100LineWrapping(enable);
	}

	@Override
	public boolean isVT100LineWrapping() {
		return delegate.isVT100LineWrapping();
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
	public void setTerminalTitle(String title, org.eclipse.terminal.control.TerminalTitleRequestor requestor) {
		if (requestor == org.eclipse.terminal.control.TerminalTitleRequestor.ANSI) {
			delegate.setTerminalTitle(title, TerminalTitleRequestor.ANSI);
		}
		if (requestor == org.eclipse.terminal.control.TerminalTitleRequestor.MENU) {
			delegate.setTerminalTitle(title, TerminalTitleRequestor.MENU);
		}
		if (requestor == org.eclipse.terminal.control.TerminalTitleRequestor.OTHER) {
			delegate.setTerminalTitle(title, TerminalTitleRequestor.OTHER);
		}
	}

	@Override
	public void updateTerminalDimensions() {
		if (delegate instanceof VT100TerminalControl vt100) {
			vt100.getTerminalText().fontChanged();
		}
	}

}