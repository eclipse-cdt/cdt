/*******************************************************************************
 * Copyright (c) 2015,2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.internal;

import java.util.List;

import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class RemoteSettingsPage extends AbstractSettingsPage {
	private final RemoteSettings fTerminalSettings;
	private RemoteConnectionWidget fRemoteConnectionWidget;

	public RemoteSettingsPage(RemoteSettings settings) {
		fTerminalSettings = settings;
	}

	@Override
	public void saveSettings() {
		if (fTerminalSettings != null && fRemoteConnectionWidget != null && !fRemoteConnectionWidget.isDisposed()) {
			if (fRemoteConnectionWidget.getConnection() != null) {
				if (fRemoteConnectionWidget.getConnection().getConnectionType() != null) {
					fTerminalSettings
							.setConnectionTypeId(fRemoteConnectionWidget.getConnection().getConnectionType().getId());
				}
				fTerminalSettings.setConnectionName(fRemoteConnectionWidget.getConnection().getName());
			}
		}
	}

	@Override
	public void loadSettings() {
		if (fTerminalSettings != null && fRemoteConnectionWidget != null && !fRemoteConnectionWidget.isDisposed()) {
			fRemoteConnectionWidget.setConnection(fTerminalSettings.getConnectionTypeId(),
					fTerminalSettings.getConnectionName());
		}
	}

	String get(String value, String def) {
		if (value == null || value.length() == 0) {
			return def;
		}
		return value;
	}

	@Override
	public boolean validateSettings() {
		if (fRemoteConnectionWidget == null || fRemoteConnectionWidget.isDisposed()
				|| fRemoteConnectionWidget.getConnection() == null) {
			return false;
		}
		return true;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);

		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		ServiceReference<IRemoteServicesManager> ref = context.getServiceReference(IRemoteServicesManager.class);
		IRemoteServicesManager manager = context.getService(ref);
		@SuppressWarnings("unchecked")
		List<IRemoteConnectionType> types = manager.getConnectionTypesSupporting(IRemoteCommandShellService.class);

		fRemoteConnectionWidget = new RemoteConnectionWidget(composite, SWT.NONE, null, 0, types);
		fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireListeners(fRemoteConnectionWidget);
			}
		});
		loadSettings();
	}
}
