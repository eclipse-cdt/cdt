package org.eclipse.tm.internal.delegates;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.terminal.control.ITerminalListener3.TerminalTitleRequestor;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public final class TerminalControlTmDelegate implements ITerminalControl {

	private org.eclipse.terminal.connector.ITerminalControl delegate;

	public TerminalControlTmDelegate(org.eclipse.terminal.connector.ITerminalControl delegate) {
		this.delegate = delegate;
	}

	@Override
	public TerminalState getState() {
		org.eclipse.terminal.connector.TerminalState state = delegate.getState();
		if (state == org.eclipse.terminal.connector.TerminalState.CONNECTED) {
			return TerminalState.CONNECTED;
		}
		if (state == org.eclipse.terminal.connector.TerminalState.CONNECTING) {
			return TerminalState.CONNECTING;
		}
		return TerminalState.CLOSED;
	}

	@Override
	public void setState(TerminalState state) {
		if (state == TerminalState.CONNECTED) {
			delegate.setState(org.eclipse.terminal.connector.TerminalState.CONNECTED);
		}
		if (state == TerminalState.CONNECTING) {
			delegate.setState(org.eclipse.terminal.connector.TerminalState.CONNECTING);
		}
		if (state == TerminalState.CLOSED) {
			delegate.setState(org.eclipse.terminal.connector.TerminalState.CLOSED);
		}
	}

	@Override
	public void setupTerminal(Composite parent) {
		delegate.setupTerminal(parent);
	}

	@Override
	public Shell getShell() {
		return delegate.getShell();
	}

	@Override
	public void setEncoding(String encoding) throws UnsupportedEncodingException {
		delegate.setCharset(Charset.forName(encoding));
	}

	@Override
	public void setCharset(Charset charset) {
		delegate.setCharset(charset);
	}

	@Override
	public String getEncoding() {
		return delegate.getCharset().name();
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

	@Override
	public void setTerminalTitle(String title) {
		delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.OTHER);
	}

	@Override
	public void setTerminalTitle(String title, TerminalTitleRequestor requestor) {
		if (requestor == TerminalTitleRequestor.ANSI) {
			delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.OTHER);
		}
		if (requestor == TerminalTitleRequestor.MENU) {
			delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.MENU);
		}
		delegate.setTerminalTitle(title, org.eclipse.terminal.control.TerminalTitleRequestor.OTHER);
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
}