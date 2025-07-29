package org.eclipse.tm.internal.delegates;

import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;

public final class TerminalConnectorTmDelegate implements ITerminalConnector {

	private org.eclipse.terminal.connector.ITerminalConnector delegate;

	public TerminalConnectorTmDelegate(org.eclipse.terminal.connector.ITerminalConnector delegate) {
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
		delegate.connect(new TmTerminalControlDelegate(control));
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
		delegate.load(new TmSettingsStoreDelegate(store));
	}

	@Override
	public void save(ISettingsStore store) {
		delegate.save(new TmSettingsStoreDelegate(store));
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