package org.eclipse.tm.internal.delegates;

import java.io.OutputStream;

import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

public final class TmTerminalConnectorDelegate implements org.eclipse.terminal.connector.ITerminalConnector {

	private ITerminalConnector delegate;

	public TmTerminalConnectorDelegate(ITerminalConnector delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return delegate.getAdapter(adapter);
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public boolean isHidden() {
		return delegate.isHidden();
	}

	@Override
	public boolean isInitialized() {
		return delegate.isInitialized();
	}

	@Override
	public String getInitializationErrorMessage() {
		return delegate.getInitializationErrorMessage();
	}

	@Override
	public void connect(ITerminalControl control) {
		delegate.connect(new TerminalControlTmDelegate(control));
	}

	@Override
	public void disconnect() {
		delegate.disconnect();
	}

	@Override
	public boolean isLocalEcho() {
		return delegate.isLocalEcho();
	}

	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
		delegate.setTerminalSize(newWidth, newHeight);
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return delegate.getTerminalToRemoteStream();
	}

	@Override
	public void load(ISettingsStore store) {
		delegate.load(new SettingsStoreTmDelegate(store));
	}

	@Override
	public void save(ISettingsStore store) {
		delegate.save(new SettingsStoreTmDelegate(store));
	}

	@Override
	public void setDefaultSettings() {
		delegate.setDefaultSettings();
	}

	@Override
	public String getSettingsSummary() {
		return delegate.getSettingsSummary();
	}
}