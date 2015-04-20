/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Greg Watson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui.widgets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemotePreferenceConstants;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.internal.core.preferences.Preferences;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Widget to allow the user to select a service provider and connection. Provides a "New" button to create a new connection.
 * 
 * If title is supplied then the widget will be placed in a group.
 * 
 * @since 5.0
 * 
 */
public class RemoteConnectionWidget extends Composite {
	/**
	 * Listener for widget selected events. Allows the events to be enabled/disabled.
	 * 
	 */
	protected class WidgetListener implements SelectionListener {
		/** State of the listener (enabled/disabled). */
		private boolean listenerEnabled = true;

		/**
		 * Disable listener, received events shall be ignored.
		 */
		public void disable() {
			setEnabled(false);
		}

		protected void doWidgetDefaultSelected(SelectionEvent e) {
			// Default empty implementation.
		}

		/**
		 * Enable the listener to handle events.
		 */
		public void enable() {
			setEnabled(true);
		}

		/**
		 * Test if the listener is enabled.
		 */
		public synchronized boolean isEnabled() {
			return listenerEnabled;
		}

		/**
		 * Set listener enabled state
		 * 
		 * @param enabled
		 */
		public synchronized void setEnabled(boolean enabled) {
			listenerEnabled = enabled;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			if (isEnabled()) {
				widgetSelected(e);
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (isEnabled()) {
				Object source = e.getSource();
				if (source == fConnectionTypeCombo) {
					handleConnectionTypeSelected(null);
				} else if (source == fConnectionCombo) {
					handleConnectionSelected();
				} else if (source == fNewConnectionButton) {
					handleNewRemoteConnectionSelected();
				} else if (source == fLocalButton) {
					handleButtonSelected();
				}
			}
		}

	}

	public static final String DEFAULT_CONNECTION_NAME = "Remote Host"; //$NON-NLS-1$

	/**
	 * Force the use of the connection type combo, regardless of the PREF_CONNECTION_TYPE preference setting.
	 * 
	 * @since 2.0
	 */
	public static int FLAG_FORCE_CONNECTION_TYPE_SELECTION = 1 << 0;

	/**
	 * Do not provide a selection for local services.
	 */
	public static int FLAG_NO_LOCAL_SELECTION = 1 << 1;

	private Combo fConnectionTypeCombo;
	private Button fLocalButton;
	private Button fRemoteButton;
	private final Combo fConnectionCombo;
	private final Button fNewConnectionButton;

	private List<IRemoteConnectionType> fConnectionTypes;
	private IRemoteConnection fSelectedConnection;
	private IRemoteServicesManager fRemoteServicesManager = RemoteUIPlugin.getService(IRemoteServicesManager.class);
	private IRemoteConnectionType fDefaultConnectionType;
	private boolean fSelectionListernersEnabled = true;
	private boolean fEnabled = true;

	private final ListenerList fSelectionListeners = new ListenerList();
	private final WidgetListener fWidgetListener = new WidgetListener();

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param style
	 *            style or SWT.NONE
	 * @param title
	 *            if a title is supplied then the widget will be placed in a group. Can be null.
	 * @param flags
	 *            a combination of flags that modify the behavior of the widget.
	 */
	public RemoteConnectionWidget(Composite parent, int style, String title, int flags) {
		this(parent, style, title, flags, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param style
	 *            style or SWT.NONE
	 * @param title
	 *            if a title is supplied then the widget will be placed in a group. Can be null.
	 * @param flags
	 *            a combination of flags that modify the behavior of the widget.
	 * @param context
	 *            runnable context, or null
	 * @param connnectionTypes
	 *            list of connection types to select from
	 * @since 2.0
	 */
	public RemoteConnectionWidget(Composite parent, int style, String title, int flags, IRunnableContext context) {
		this(parent, style, title, flags, context, null);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param style
	 *            style or SWT.NONE
	 * @param title
	 *            if a title is supplied then the widget will be placed in a group. Can be null.
	 * @param flags
	 *            a combination of flags that modify the behavior of the widget.
	 * @param connnectionTypes
	 *            list of connection types to select from
	 * @since 2.0
	 */
	public RemoteConnectionWidget(Composite parent, int style, String title, int flags, List<IRemoteConnectionType> connectionTypes) {
		this(parent, style, title, flags, null, connectionTypes);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param style
	 *            style or SWT.NONE
	 * @param title
	 *            if a title is supplied then the widget will be placed in a group. Can be null.
	 * @param flags
	 *            a combination of flags that modify the behavior of the widget.
	 * @param context
	 *            runnable context, or null
	 * @param connnectionTypes
	 *            list of connection types to select from
	 * @since 2.0
	 */
	public RemoteConnectionWidget(Composite parent, int style, String title, int flags, IRunnableContext context, List<IRemoteConnectionType> connectionTypes) {
		super(parent, style);

		if (connectionTypes != null) {
			// Just present the connections that are provided
			flags |= FLAG_FORCE_CONNECTION_TYPE_SELECTION | FLAG_NO_LOCAL_SELECTION;
		}

		Composite body = this;

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 4;
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (title != null) {
			Group group = new Group(this, SWT.NONE);
			group.setText(title);
			GridLayout groupLayout = new GridLayout(1, false);
			groupLayout.marginHeight = 0;
			groupLayout.marginWidth = 0;
			groupLayout.numColumns = 4;
			group.setLayout(groupLayout);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			layout.numColumns = 1;
			body = group;
		}

		fRemoteServicesManager = RemoteUIPlugin.getService(IRemoteServicesManager.class);
		if (connectionTypes != null) {
			// No default if the list of connection types was supplied
			fConnectionTypes = connectionTypes;
		} else {
			fConnectionTypes = fRemoteServicesManager.getRemoteConnectionTypes();
			String id = Preferences.getString(IRemotePreferenceConstants.PREF_CONNECTION_TYPE_ID);
			if (id != null) {
				fDefaultConnectionType = fRemoteServicesManager.getConnectionType(id);
			}
		}

		/*
		 * Check if we need a connection type combo, or we should just use the default provider
		 */
		if ((flags & FLAG_FORCE_CONNECTION_TYPE_SELECTION) != 0) {
			Label label = new Label(body, SWT.NONE);
			label.setText(Messages.RemoteConnectionWidget_Connection_Type);
			label.setLayoutData(new GridData());

			fConnectionTypeCombo = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			fConnectionTypeCombo.setLayoutData(gd);
			fConnectionTypeCombo.addSelectionListener(fWidgetListener);
			fConnectionTypeCombo.setFocus();
		}

		if ((flags & FLAG_NO_LOCAL_SELECTION) == 0 && (flags & FLAG_FORCE_CONNECTION_TYPE_SELECTION) == 0) {
			fLocalButton = new Button(body, SWT.RADIO);
			fLocalButton.setText(Messages.RemoteConnectionWidget_Local);
			fLocalButton.setLayoutData(new GridData());
			fLocalButton.addSelectionListener(fWidgetListener);
			fLocalButton.setSelection(false);

			fRemoteButton = new Button(body, SWT.RADIO);
			fRemoteButton.setText(Messages.RemoteConnectionWidget_Remote);
			fRemoteButton.setLayoutData(new GridData());
		} else {
			Label remoteLabel = new Label(body, SWT.NONE);
			remoteLabel.setText(Messages.RemoteConnectionWidget_Connection_Name);
			remoteLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		}

		fConnectionCombo = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
		fConnectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fConnectionCombo.addSelectionListener(fWidgetListener);
		if (fDefaultConnectionType != null) {
			fConnectionCombo.setFocus();
		}
		fConnectionCombo.setEnabled(false);

		fNewConnectionButton = new Button(body, SWT.PUSH);
		fNewConnectionButton.setText(Messages.RemoteConnectionWidget_New);
		fNewConnectionButton.setLayoutData(new GridData());
		fNewConnectionButton.addSelectionListener(fWidgetListener);

		if (fConnectionTypeCombo != null) {
			initializeConnectionTypeCombo();
		}

		handleConnectionTypeSelected(null);

		if (fLocalButton != null) {
			handleButtonSelected();
		}
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the user changes the receiver's selection, by
	 * sending it one of the messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the user changes the service provider or connection.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
	 */
	public void addSelectionListener(SelectionListener listener) {
		fSelectionListeners.add(listener);
	}

	/**
	 * Limit the connection types that will be used when displaying valid connections. Only connection types that support
	 * connections with supplied services will be displayed in the connection type combo, and only connections from these connection
	 * types will be displayed in the connection combo.
	 * 
	 * @param services
	 *            list of services {@link IRemoteConnection.Service}
	 * @since 2.0
	 */
	@SafeVarargs
	public final void filterConnections(Class<? extends IRemoteConnection.Service>... services) {
		fConnectionTypes = fRemoteServicesManager.getConnectionTypesSupporting(services);
		if (fConnectionTypeCombo != null) {
			initializeConnectionTypeCombo();
		}
		handleConnectionTypeSelected(null);
	}

	/**
	 * Get the new button from the widget
	 * 
	 * @return button
	 * @since 7.0
	 */
	public Button getButton() {
		return fNewConnectionButton;
	}

	/**
	 * Get the connection that is currently selected in the widget, or null if there is no selected connection.
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection() {
		return fSelectedConnection;
	}

	private IRemoteConnectionType getSelectedConnectionType() {
		if (fDefaultConnectionType != null) {
			return fDefaultConnectionType;
		}
		if (fConnectionTypeCombo != null) {
			int selectionIndex = fConnectionTypeCombo.getSelectionIndex();
			if (fConnectionTypes.size() > 0 && selectionIndex > 0) {
				return fConnectionTypes.get(selectionIndex - 1);
			}
		}
		return null;
	}

	private IRemoteUIConnectionService getUIConnectionManager() {
		IRemoteConnectionType services = getSelectedConnectionType();
		if (services != null) {
			return services.getService(IRemoteUIConnectionService.class);
		}
		return null;
	}

	private void handleButtonSelected() {
		fRemoteButton.setSelection(!fLocalButton.getSelection());
		updateEnablement();
		handleConnectionSelected();
	}

	/**
	 * Handle the section of a new connection. Update connection option buttons appropriately.
	 * 
	 * @throws CoreException
	 */
	protected void handleConnectionSelected() {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		IRemoteConnection selectedConnection = null;
		if (fLocalButton != null && fLocalButton.getSelection()) {
			selectedConnection = fRemoteServicesManager.getLocalConnectionType().getConnections().get(0);
		} else {
			int currentSelection = fConnectionCombo.getSelectionIndex();
			if (currentSelection > 0) {
				String connectionName = fConnectionCombo.getItem(currentSelection);
				selectedConnection = getSelectedConnectionType().getConnection(connectionName);
			}
		}
		if (selectedConnection == null || fSelectedConnection == null
				|| !selectedConnection.getName().equals(fSelectedConnection.getName())) {
			fSelectedConnection = selectedConnection;
			Event evt = new Event();
			evt.widget = this;
			notifyListeners(new SelectionEvent(evt));
		}
		fWidgetListener.setEnabled(enabled);
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button. Calls handleRemoteServicesSelected() to update the
	 * connection combo with the new connection.
	 * 
	 * TODO should probably select the new connection
	 * 
	 * @throws CoreException
	 */
	protected void handleNewRemoteConnectionSelected() {
		if (getUIConnectionManager() != null) {
			IRemoteUIConnectionWizard wizard = getUIConnectionManager().getConnectionWizard(getShell());
			if (wizard != null) {
				wizard.setConnectionName(initialConnectionName());
				IRemoteConnectionWorkingCopy conn = wizard.open();
				if (conn != null) {
					try {
						handleConnectionTypeSelected(conn.save());
						handleConnectionSelected();
					} catch (CoreException e) {
						RemoteUIPlugin.log(e);
					}
				}
			}
		}
	}

	/**
	 * Handle selection of a new connection type from the connection type combo. Handles the special case where the
	 * connection type combo is null and a local connection is supplied. In this case, the selected connection type is not changed.
	 * 
	 * The assumption is that this will trigger a call to the selection handler for the connection combo.
	 * 
	 * @param conn
	 *            connection to select as current. If conn is null, select the first item in the list.
	 * @throws CoreException
	 * @since 2.0
	 */
	protected void handleConnectionTypeSelected(IRemoteConnection conn) {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		try {
			IRemoteConnectionType selectedConnectionType = getSelectedConnectionType();
			if (conn != null) {
				selectedConnectionType = conn.getConnectionType();
			}

			/*
			 * If a connection was supplied, set its connection type in the combo. Otherwise use the currently selected
			 * service.
			 */
			if (fDefaultConnectionType == null && conn != null) {
				for (int index = 0; index < fConnectionTypes.size(); index++) {
					if (fConnectionTypes.get(index).getId().equals(selectedConnectionType.getId())) {
						fConnectionTypeCombo.select(index + 1);
						break;
					}
				}
			}

			fConnectionCombo.removeAll();
			fConnectionCombo.add(Messages.RemoteConnectionWidget_selectConnection);

			if (selectedConnectionType == null) {
				fConnectionCombo.select(0);
				fConnectionCombo.setEnabled(false);
				fNewConnectionButton.setEnabled(false);
				handleConnectionSelected();
			} else {
				fConnectionCombo.setEnabled(true);

				/*
				 * Populate the connection combo and select the connection
				 */
				int selected = 0;
				int offset = 1;

				List<IRemoteConnection> sorted = selectedConnectionType.getConnections();
				Collections.sort(sorted, new Comparator<IRemoteConnection>() {
					@Override
					public int compare(IRemoteConnection o1, IRemoteConnection o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});

				for (IRemoteConnection s : sorted) {
					fConnectionCombo.add(s.getName());
					if (conn != null && s.getName().equals(conn.getName())) {
						selected = offset;
					}
					offset++;
				}

				fConnectionCombo.select(selected);
				handleConnectionSelected();

				/*
				 * Enable 'new' button if new connections are supported
				 */
				fNewConnectionButton
						.setEnabled((selectedConnectionType.getCapabilities() & IRemoteConnectionType.CAPABILITY_ADD_CONNECTIONS) != 0);
			}
		} finally {
			fWidgetListener.setEnabled(enabled);
		}
	}

	private String initialConnectionName() {
		String name = DEFAULT_CONNECTION_NAME;
		int count = 1;
		while (getSelectedConnectionType().getConnection(name) != null) {
			name = DEFAULT_CONNECTION_NAME + " " + count++; //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * Initialize the contents of the connection type combo. Keeps an array of connection types that matches the combo elements.
	 * Returns the id of the selected element.
	 * 
	 * @since 2.0
	 */
	protected void initializeConnectionTypeCombo() {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		fConnectionTypeCombo.removeAll();
		int offset = 1;
		int defIndex = 0;
		fConnectionTypeCombo.add(Messages.RemoteConnectionWidget_selectConnectionType);
		for (int i = 0; i < fConnectionTypes.size(); i++) {
			fConnectionTypeCombo.add(fConnectionTypes.get(i).getName());
			if (fDefaultConnectionType != null && fConnectionTypes.get(i).equals(fDefaultConnectionType)) {
				defIndex = i + offset;
			}
		}
		if (fConnectionTypes.size() > 0) {
			fConnectionTypeCombo.select(defIndex);
		}
		fWidgetListener.setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return fEnabled;
	}

	private void notifyListeners(SelectionEvent e) {
		if (fSelectionListernersEnabled) {
			for (Object listener : fSelectionListeners.getListeners()) {
				((SelectionListener) listener).widgetSelected(e);
			}
		}
	}

	/**
	 * Remove a listener that will be notified when one of the widget's controls are selected
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeSelectionListener(SelectionListener listener) {
		fSelectionListeners.remove(listener);
	}

	/**
	 * Set the connection that should be selected in the widget.
	 * 
	 * @param connection
	 *            connection to select
	 * @throws CoreException
	 */
	public void setConnection(IRemoteConnection connection) {
		fSelectionListernersEnabled = false;
		if (fLocalButton != null && connection != null
				&& connection.getConnectionType() == fRemoteServicesManager.getLocalConnectionType()) {
			fLocalButton.setSelection(true);
			handleButtonSelected();
		} else {
			handleConnectionTypeSelected(connection);
		}
		handleConnectionSelected();
		updateEnablement();
		fSelectionListernersEnabled = true;
	}

	/**
	 * Set the connection that should be selected in the widget.
	 * 
	 * @param id
	 *            connection type id
	 * @param name
	 *            connection name
	 * @throws CoreException
	 * @since 6.0
	 */
	public void setConnection(String id, String name) {
		IRemoteConnectionType connectionType = fRemoteServicesManager.getConnectionType(id);
		if (connectionType != null) {
			IRemoteConnection connection = connectionType.getConnection(name);
			if (connection != null) {
				setConnection(connection);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
		updateEnablement();
	}

	private void updateEnablement() {
		if (fDefaultConnectionType != null) {
			boolean isRemote = true;
			if (fLocalButton != null) {
				fLocalButton.setEnabled(fEnabled);
				fRemoteButton.setEnabled(fEnabled);
				isRemote = !fLocalButton.getSelection();
			}
			fConnectionCombo.setEnabled(fEnabled && isRemote);
			fNewConnectionButton.setEnabled(fEnabled && isRemote
					&& (fDefaultConnectionType.getCapabilities() & IRemoteConnectionType.CAPABILITY_ADD_CONNECTIONS) != 0);
		} else {
			IRemoteConnectionType services = getSelectedConnectionType();
			fConnectionCombo.setEnabled(fEnabled && services != null);
			fNewConnectionButton.setEnabled(fEnabled && services != null
					&& (services.getCapabilities() & IRemoteConnectionType.CAPABILITY_ADD_CONNECTIONS) != 0);
			fConnectionTypeCombo.setEnabled(fEnabled);
		}
	}
}
