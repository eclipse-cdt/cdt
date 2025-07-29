package org.eclipse.tm.internal.delegates;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public final class SettingsStoreTmDelegate implements ISettingsStore {

	private org.eclipse.terminal.connector.ISettingsStore delegate;

	public SettingsStoreTmDelegate(org.eclipse.terminal.connector.ISettingsStore delegate) {
		this.delegate = delegate;
	}

	@Override
	public String get(String key) {
		return delegate.get(key);
	}

	@Override
	public String get(String key, String defaultValue) {
		return delegate.get(key, defaultValue);
	}

	@Override
	public void put(String key, String value) {
		delegate.put(key, value);
	}

}