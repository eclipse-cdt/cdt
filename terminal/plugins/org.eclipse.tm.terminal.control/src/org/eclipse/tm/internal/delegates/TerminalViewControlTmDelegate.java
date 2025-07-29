package org.eclipse.tm.internal.delegates;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.terminal.control.ICommandInputField;
import org.eclipse.tm.internal.terminal.control.ITerminalListener3.TerminalTitleRequestor;
import org.eclipse.tm.internal.terminal.control.ITerminalMouseListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class TerminalViewControlTmDelegate implements ITerminalViewControl {

	private org.eclipse.terminal.control.ITerminalViewControl delegate;

	public TerminalViewControlTmDelegate(org.eclipse.terminal.control.ITerminalViewControl delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setCharset(Charset charset) {
		delegate.setCharset(charset);
	}

	@Override
	public Charset getCharset() {
		return delegate.getCharset();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public void setFont(String fontName) {
		delegate.setFont(fontName);
	}

	@Override
	public void setInvertedColors(boolean invert) {
		delegate.setInvertedColors(invert);
	}

	@Override
	public boolean isInvertedColors() {
		return delegate.isInvertedColors();
	}

	@Override
	public Font getFont() {
		return delegate.getFont();
	}

	@Override
	public Control getControl() {
		return delegate.getControl();
	}

	@Override
	public Control getRootControl() {
		return delegate.getRootControl();
	}

	@Override
	public boolean isDisposed() {
		return delegate.isDisposed();
	}

	@Override
	public void selectAll() {
		delegate.selectAll();
	}

	@Override
	public void clearTerminal() {
		delegate.clearTerminal();
	}

	@Override
	public void copy() {
		delegate.copy();
	}

	@Override
	public void paste() {
		delegate.paste();
	}

	@Override
	public String getSelection() {
		return delegate.getSelection();
	}

	@Override
	public TerminalState getState() {
		org.eclipse.terminal.connector.TerminalState state = delegate.getState();
		if (state == org.eclipse.terminal.connector.TerminalState.CONNECTED) {
			return TerminalState.CONNECTED;
		} else if (state == org.eclipse.terminal.connector.TerminalState.CONNECTING) {
			return TerminalState.CONNECTING;
		} else {
			return TerminalState.CLOSED;
		}
	}

	@Override
	public Clipboard getClipboard() {
		return delegate.getClipboard();
	}

	@Override
	public void disconnectTerminal() {
		delegate.disconnectTerminal();
	}

	@Override
	public void disposeTerminal() {
		delegate.disposeTerminal();
	}

	@Override
	public String getSettingsSummary() {
		return delegate.getSettingsSummary();
	}

	@Override
	public ITerminalConnector[] getConnectors() {
		org.eclipse.terminal.connector.ITerminalConnector[] connectors = delegate.getConnectors();
		if (connectors == null) {
			return null;
		}
		return Arrays.stream(connectors).map(c -> new TerminalConnectorTmDelegate(c))
				.toArray(ITerminalConnector[]::new);
	}

	@Override
	public void setFocus() {
		delegate.setFocus();
	}

	@Override
	public ITerminalConnector getTerminalConnector() {
		return new TerminalConnectorTmDelegate(delegate.getTerminalConnector());
	}

	@Override
	public void connectTerminal() {
		delegate.connectTerminal();
	}

	@Override
	public void sendKey(char c) {
		delegate.sendKey(c);
	}

	@Override
	public boolean pasteString(String string) {
		return delegate.pasteString(string);
	}

	@Override
	public boolean isConnected() {
		return delegate.isConnected();
	}

	@Override
	public void setCommandInputField(ICommandInputField inputField) {
		delegate.setCommandInputField(new TmCommandInputFieldDelegate(inputField));
	}

	@Override
	public ICommandInputField getCommandInputField() {
		return new CommandInputFieldTmDelegate(delegate.getCommandInputField());
	}

	@Override
	public int getBufferLineLimit() {
		return delegate.getBufferLineLimit();
	}

	@Override
	public void setBufferLineLimit(int bufferLineLimit) {
		delegate.setBufferLineLimit(bufferLineLimit);
	}

	@Override
	public boolean isScrollLock() {
		return delegate.isScrollLock();
	}

	@Override
	public void setScrollLock(boolean on) {
		delegate.setScrollLock(on);
	}

	@Override
	public String getHoverSelection() {
		return delegate.getHoverSelection();
	}

	@Override
	public void setEncoding(String encoding) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFont(Font font) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConnector(ITerminalConnector connector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMouseListener(ITerminalMouseListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMouseListener(ITerminalMouseListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTerminalTitle(String newTitle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTerminalTitle(String newTitle, TerminalTitleRequestor requestor) {
		// TODO Auto-generated method stub

	}

}
