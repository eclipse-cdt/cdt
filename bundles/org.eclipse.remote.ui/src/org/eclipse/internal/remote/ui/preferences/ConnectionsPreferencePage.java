/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.ui.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.internal.remote.core.RemoteServicesDescriptor;
import org.eclipse.internal.remote.core.RemoteServicesImpl;
import org.eclipse.internal.remote.core.preferences.Preferences;
import org.eclipse.internal.remote.ui.messages.Messages;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemotePreferenceConstants;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class implements a preference page which can be used to view a list of
 * JSch connections, create new connections or to delete existing connections.
 * 
 */
public class ConnectionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private class ConnectionContentProvider implements IStructuredContentProvider {

		public void dispose() {
			// Nothing to do
		}

		public Object[] getElements(Object inputElement) {
			return fWorkingCopies.values().toArray(new IRemoteConnection[fWorkingCopies.size()]);
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do
		}

	}

	private class ConnectionLabelProvider implements ITableLabelProvider {

		public void addListener(ILabelProviderListener listener) {
			// Nothing to do
		}

		public void dispose() {
			// Nothing to do
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IRemoteConnection connection = (IRemoteConnection) element;
			switch (columnIndex) {
			case 0:
				return connection.getName();
			case 1:
				return connection.getAddress();
			case 2:
				return connection.getUsername();
			}
			return null;
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// Nothing to do
		}

	}

	/**
	 * Handle widget selection events for this page
	 * 
	 */
	private class EventHandler extends SelectionAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source;

			source = e.getSource();
			if (source == fAddButton) {
				addConnection();
			} else if (source == fEditButton) {
				editConnection();
			} else if (source == fRemoveButton) {
				removeConnections();
			} else if (source == fConnectionTable) {
				selectConnection();
			} else if (source == fServicesCombo) {
				String id = fServiceIDs[fServicesCombo.getSelectionIndex()];
				selectServices(id);
			}
		}

	}

	private final String[] fTableColumnHeaders = { Messages.ConnectionsPreferencePage_Connection_Name,
			Messages.ConnectionsPreferencePage_Host, Messages.ConnectionsPreferencePage_User };

	private final ColumnLayoutData[] fTableColumnLayouts = { new ColumnWeightData(30), new ColumnWeightData(50),
			new ColumnWeightData(20) };

	private Combo fServicesCombo;
	private Button fAddButton;
	private Button fEditButton;
	private Button fRemoveButton;
	private Table fConnectionTable;
	private TableViewer fConnectionViewer;
	private EventHandler fEventHandler;

	private String[] fServiceIDs;
	private boolean fIsDirty;
	private IRemoteConnection fSelectedConnection;
	private IRemoteConnectionManager fConnectionManager;
	private IRemoteUIConnectionManager fUIConnectionManager;

	private final Map<String, IRemoteConnection> fWorkingCopies = new HashMap<String, IRemoteConnection>();

	private static final String DEFAULT_CONNECTION_NAME = "Remote Host"; //$NON-NLS-1$

	public ConnectionsPreferencePage() {
		super();
	}

	public ConnectionsPreferencePage(String title) {
		super(title);
	}

	public ConnectionsPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Add a service configuration to the set of service configurations
	 */
	private void addConnection() {
		IRemoteUIConnectionWizard wizard = fUIConnectionManager.getConnectionWizard(getShell());
		if (wizard != null) {
			wizard.setConnectionName(initialConnectionName());
			wizard.setInvalidConnectionNames(invalidConnectionNames());
			IRemoteConnectionWorkingCopy conn = wizard.open();
			if (conn != null) {
				fWorkingCopies.put(conn.getName(), conn);
				fConnectionViewer.refresh();
				fIsDirty = true;
			}
		}
	}

	/**
	 * Create the contents for this page
	 * 
	 * @param parent
	 *            - The parent widget for the client area
	 */
	@Override
	protected Control createContents(Composite parent) {
		return createWidgets(parent);
	}

	/**
	 * Create the widgets for this page
	 * 
	 * @param parent
	 *            The parent widget for the client area
	 * @return
	 */
	private Control createWidgets(Composite parent) {
		fEventHandler = new EventHandler();

		Composite selectComp = new Composite(parent, SWT.NONE);
		selectComp.setLayout(new GridLayout(2, false));
		selectComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		Label label = new Label(selectComp, SWT.NONE);
		label.setText(Messages.ConnectionsPreferencePage_Remote_Services);
		label.setLayoutData(new GridData());
		fServicesCombo = new Combo(selectComp, SWT.READ_ONLY);
		label.setLayoutData(new GridData());
		List<RemoteServicesDescriptor> descriptors = RemoteServicesImpl.getRemoteServiceDescriptors();
		String[] names = new String[descriptors.size()];
		fServiceIDs = new String[descriptors.size()];
		for (int i = 0; i < descriptors.size(); i++) {
			names[i] = descriptors.get(i).getName();
			fServiceIDs[i] = descriptors.get(i).getId();
		}
		fServicesCombo.addSelectionListener(fEventHandler);
		fServicesCombo.setItems(names);

		Composite preferencePane = new Composite(parent, SWT.NONE);
		preferencePane.setLayout(new GridLayout(2, false));

		fConnectionTable = new Table(preferencePane, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 425;
		data.heightHint = fConnectionTable.getItemHeight();
		data.horizontalSpan = 1;
		fConnectionTable.setLayoutData(data);
		fConnectionTable.setFont(parent.getFont());
		fConnectionTable.addSelectionListener(fEventHandler);

		TableLayout tableLayout = new TableLayout();
		fConnectionTable.setLayout(tableLayout);
		fConnectionTable.setHeaderVisible(true);
		fConnectionTable.setLinesVisible(true);

		for (int i = 0; i < fTableColumnHeaders.length; i++) {
			tableLayout.addColumnData(fTableColumnLayouts[i]);
			TableColumn column = new TableColumn(fConnectionTable, SWT.NONE, i);
			column.setResizable(fTableColumnLayouts[i].resizable);
			column.setText(fTableColumnHeaders[i]);
		}
		fConnectionViewer = new TableViewer(fConnectionTable);
		fConnectionViewer.setContentProvider(new ConnectionContentProvider());
		fConnectionViewer.setLabelProvider(new ConnectionLabelProvider());
		fConnectionViewer.setInput(this);

		Composite buttonPane = new Composite(preferencePane, SWT.NONE);
		buttonPane.setLayout(new GridLayout(1, false));
		buttonPane.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonPane.setFont(preferencePane.getFont());

		fAddButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(fAddButton);
		fAddButton.setText(Messages.ConnectionsPreferencePage_Add);
		fAddButton.addSelectionListener(fEventHandler);
		fEditButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(fEditButton);
		fEditButton.setText(Messages.ConnectionsPreferencePage_Edit);
		fEditButton.addSelectionListener(fEventHandler);
		fEditButton.setEnabled(false);
		fRemoveButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(fRemoveButton);
		fRemoveButton.setText(Messages.ConnectionsPreferencePage_Remove);
		fRemoveButton.addSelectionListener(fEventHandler);
		fRemoveButton.setEnabled(false);

		String id = Preferences.getString(IRemotePreferenceConstants.PREF_REMOTE_SERVICES_ID);
		if ("".equals(id)) { //$NON-NLS-1$
			id = fServiceIDs[0];
		}
		for (int i = 0; i < fServiceIDs.length; i++) {
			if (id.equals(fServiceIDs[i])) {
				fServicesCombo.select(i);
			}
		}
		selectServices(id);

		return preferencePane;
	}

	/**
	 * Edit an existing service configuration
	 */
	private void editConnection() {
		if (fSelectedConnection != null) {
			IRemoteConnectionWorkingCopy copy = fSelectedConnection.getWorkingCopy();
			IRemoteUIConnectionWizard wizard = fUIConnectionManager.getConnectionWizard(getShell());
			if (wizard != null) {
				wizard.setConnection(copy);
				wizard.setInvalidConnectionNames(invalidConnectionNames());
				IRemoteConnectionWorkingCopy conn = wizard.open();
				if (conn != null) {
					fWorkingCopies.put(copy.getName(), copy);
					fConnectionViewer.refresh();
					fIsDirty = true;
				}
			}
		}
	}

	public void init(IWorkbench workbench) {
		// Do nothing
	}

	private String initialConnectionName() {
		String name = RemoteConnectionWidget.DEFAULT_CONNECTION_NAME;
		int count = 2;
		while (fWorkingCopies.containsKey(name)) {
			name = DEFAULT_CONNECTION_NAME + " " + count++; //$NON-NLS-1$
		}
		return name;
	}

	private Set<String> invalidConnectionNames() {
		return fWorkingCopies.keySet();
	}

	private void initWorkingConnections() {
		fWorkingCopies.clear();
		for (IRemoteConnection conn : fConnectionManager.getConnections()) {
			fWorkingCopies.put(conn.getName(), conn);
		}
	}

	@Override
	protected void performDefaults() {
		initWorkingConnections();
		fIsDirty = false;
		super.performDefaults();
	}

	/**
	 * Delete service configurations when Ok button is pressed
	 * 
	 * @return Status from superclass indicating if Ok processing is to continue
	 */
	@Override
	public boolean performOk() {
		if (fIsDirty) {
			updateConnections();
			fIsDirty = false;
		}
		return super.performOk();
	}

	/**
	 * Remove the selected service configuration from the set of service
	 * configurations
	 */
	private void removeConnections() {
		TableItem[] items = fConnectionTable.getSelection();
		if (items.length > 0) {
			for (TableItem item : items) {
				fWorkingCopies.remove(((IRemoteConnection) item.getData()).getName());
			}
			fConnectionViewer.refresh();
			fIsDirty = true;
		}
	}

	/**
	 * Record the selected connection and enable the buttons.
	 */
	private void selectConnection() {
		TableItem[] selection = fConnectionTable.getSelection();
		fEditButton.setEnabled(false);
		fRemoveButton.setEnabled(false);
		if (selection.length > 0) {
			fSelectedConnection = (IRemoteConnection) selection[0].getData();
			IRemoteServices services = fSelectedConnection.getRemoteServices();
			fEditButton.setEnabled((services.getCapabilities() & IRemoteServices.CAPABILITY_EDIT_CONNECTIONS) != 0);
			fRemoveButton.setEnabled((services.getCapabilities() & IRemoteServices.CAPABILITY_REMOVE_CONNECTIONS) != 0);
		}
	}

	private void selectServices(String id) {
		IRemoteServices services = RemoteServices.getRemoteServices(id);
		if (services != null) {
			fConnectionManager = services.getConnectionManager();
			fUIConnectionManager = RemoteUIServices.getRemoteUIServices(services).getUIConnectionManager();
			initWorkingConnections();
			fConnectionViewer.refresh();
			fAddButton.setEnabled((services.getCapabilities() & IRemoteServices.CAPABILITY_ADD_CONNECTIONS) != 0);
		}
		fIsDirty = false;
	}

	/**
	 * Update the connection manager with changes to the connections.
	 */
	private void updateConnections() {
		/*
		 * Remove any deleted connections
		 */
		for (IRemoteConnection conn : fConnectionManager.getConnections()) {
			if (!fWorkingCopies.containsKey(conn.getName()) && !conn.isOpen()) {
				try {
					fConnectionManager.removeConnection(conn);
				} catch (RemoteConnectionException e) {
					// Ignore
				}
			}
		}
		/*
		 * Save any added/edited connections
		 */
		for (IRemoteConnection conn : fWorkingCopies.values()) {
			if (conn instanceof IRemoteConnectionWorkingCopy) {
				((IRemoteConnectionWorkingCopy) conn).save();
			}
		}
		initWorkingConnections();
	}
}
