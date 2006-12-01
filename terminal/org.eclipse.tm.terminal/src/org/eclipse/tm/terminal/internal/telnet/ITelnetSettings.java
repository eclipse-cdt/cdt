package org.eclipse.tm.terminal.internal.telnet;

import org.eclipse.tm.terminal.ISettingsStore;

public interface ITelnetSettings {
	String getHost();
	int getNetworkPort();
	int getTimeout();
	String getStatusString(String strConnected);
	void load(ISettingsStore store);
	void save(ISettingsStore store);
}
