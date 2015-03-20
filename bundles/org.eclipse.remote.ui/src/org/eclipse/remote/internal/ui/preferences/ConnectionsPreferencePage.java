/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Tasse - [462499] set viewer comparator
 *     Bernd Hufmann - [462709] Display Host and User per connection
 *******************************************************************************/
package org.eclipse.remote.internal.ui.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionControlService;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemotePreferenceConstants;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.core.preferences.Preferences;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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

		@Override
		public void dispose() {
			// Nothing to do
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return fWorkingCopies.values().toArray(new IRemoteConnection[fWorkingCopies.size()]);
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do
		}

	}

	private class ConnectionLabelProvider implements ITableLabelProvider, ILabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			// Nothing to do
		}

		@Override
		public void dispose() {
			// Nothing to do
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			IRemoteConnection connection = getOriginalIfClean((IRemoteConnection) element);
			switch (columnIndex) {
			case 0:
				return connection.isOpen() ? Messages.ConnectionsPreferencePage_open : Messages.ConnectionsPreferencePage_closed;
			case 1:
				return connection.getName();
			case 2:
				if (connection.hasService(IRemoteConnectionHostService.class)) {
					return connection.getService(IRemoteConnectionHostService.class).getHostname();
				}
				break;
			case 3:
				if (connection.hasService(IRemoteConnectionHostService.class)) {
					return connection.getService(IRemoteConnectionHostService.class).getUsername();
				}
				break;
			}
			return null;
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			/*
			 * This interface is used by the default ViewerComparator.
			 */
			IRemoteConnection connection = (IRemoteConnection) element;
			return connection.getName();
		}

		@Override
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
			} else if (source == fOpenButton) {
				toggleConnection();
			} else if (source == fCloseButton) {
				toggleConnection();
			} else if (source == fConnectionTable) {
				selectConnection();
			} else if (source == fServicesCombo) {
				String id = fServiceIDs[fServicesCombo.getSelectionIndex()];
				selectServices(id);
			}
		}

	}

	private final String[] fTableColumnHeaders = { Messages.ConnectionsPreferencePage_Status,
			Messages.ConnectionsPreferencePage_Connection_Name, Messages.ConnectionsPreferencePage_Host,
			Messages.ConnectionsPreferencePage_User };

	private final ColumnLayoutData[] fTableColumnLayouts = { new ColumnWeightData(15), new ColumnWeightData(35),
			new ColumnWeightData(30), new ColumnWeightData(20) };

	private Combo fServicesCombo;
	private Button fAddButton;
	private Button fEditButton;
	private Button fRemoveButton;
	private Button fOpenButton;
	private Button fCloseButton;
	private Table fConnectionTable;
	private TableViewer fConnectionViewer;
	private EventHandler fEventHandler;

	private String[] fServiceIDs;
	private boolean fIsDirty;
	private IRemoteConnection fSelectedConnection;
	private IRemoteConnectionType fConnectionType;
	private IRemoteUIConnectionService fUIConnectionManager;

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
	 * Add a new connection
	 */
	private void addConnection() {
		if (fIsDirty) {
			MessageDialog dialog = new MessageDialog(getShell(), Messages.ConnectionsPreferencePage_Confirm_Actions, null,
					Messages.ConnectionsPreferencePage_There_are_unsaved_changes, MessageDialog.QUESTION, new String[] {
							IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
			if (dialog.open() == 1) {
				return;
			}
			performOk();
		}
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

		IRemoteServicesManager manager = RemoteUIPlugin.getService(IRemoteServicesManager.class);
		List<IRemoteConnectionType> services = manager.getRemoteConnectionTypes();
		String[] names = new String[services.size()];
		fServiceIDs = new String[services.size()];
		{
			int i = 0;
			for (IRemoteConnectionType s : services) {
				names[i] = s.getName();
				fServiceIDs[i] = s.getId();
				i++;
			}
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
		fConnectionTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (fSelectedConnection != null) {
					IRemoteConnection original = getOriginalIfClean(fSelectedConnection);
					if (original.isOpen()) {
						editConnection();
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// Nothing
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// Nothing
			}
		});

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
		fConnectionViewer.setComparator(new ViewerComparator());
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
		fOpenButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(fOpenButton);
		fOpenButton.setText(Messages.ConnectionsPreferencePage_Open);
		fOpenButton.addSelectionListener(fEventHandler);
		fOpenButton.setEnabled(false);
		fCloseButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(fCloseButton);
		fCloseButton.setText(Messages.ConnectionsPreferencePage_Close);
		fCloseButton.addSelectionListener(fEventHandler);
		fCloseButton.setEnabled(false);

		String id = Preferences.getString(IRemotePreferenceConstants.PREF_CONNECTION_TYPE_ID);
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
			IRemoteConnectionWorkingCopy copy;
			if (fSelectedConnection instanceof IRemoteConnectionWorkingCopy) {
				copy = (IRemoteConnectionWorkingCopy) fSelectedConnection;
			} else {
				copy = fSelectedConnection.getWorkingCopy();
			}
			IRemoteUIConnectionWizard wizard = fUIConnectionManager.getConnectionWizard(getShell());
			if (wizard != null) {
				wizard.setConnection(copy);
				wizard.setInvalidConnectionNames(invalidConnectionNames());
				IRemoteConnectionWorkingCopy conn = wizard.open();
				if (conn != null && conn.isDirty()) {
					fWorkingCopies.put(copy.getName(), copy);
					fConnectionViewer.refresh();
					fIsDirty = true;
				}
			}
		}
	}

	@Override
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

	private void initWorkingConnections() {
		fWorkingCopies.clear();
		for (IRemoteConnection conn : fConnectionType.getConnections()) {
			fWorkingCopies.put(conn.getName(), conn);
		}
	}

	private Set<String> invalidConnectionNames() {
		return fWorkingCopies.keySet();
	}

	@Override
	protected void performDefaults() {
		initWorkingConnections();
		fIsDirty = false;
		fConnectionViewer.refresh();
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
			fConnectionTable.deselectAll();
			fSelectedConnection = null;
		}
		updateEnablement();
	}

	/**
	 * Record the selected connection and enable the buttons.
	 */
	private void selectConnection() {
		TableItem[] selection = fConnectionTable.getSelection();
		if (selection.length > 0) {
			fSelectedConnection = (IRemoteConnection) selection[0].getData();
		} else {
			fSelectedConnection = null;
		}
		updateEnablement();
	}

	private void selectServices(String id) {
		IRemoteServicesManager manager = RemoteUIPlugin.getService(IRemoteServicesManager.class);
		fConnectionType = manager.getConnectionType(id);
		if (fConnectionType != null) {
			fUIConnectionManager = fConnectionType.getService(IRemoteUIConnectionService.class);
			initWorkingConnections();
			fConnectionViewer.refresh();
			fAddButton.setEnabled((fConnectionType.getCapabilities() & IRemoteConnectionType.CAPABILITY_ADD_CONNECTIONS) != 0);
		}
		fIsDirty = false;
	}

	/**
	 * Toggle the connection
	 */
	private void toggleConnection() {
		TableItem[] items = fConnectionTable.getSelection();
		if (items.length > 0) {
			IRemoteConnection conn = getOriginalIfClean((IRemoteConnection) items[0].getData());
			if (conn.hasService(IRemoteConnectionControlService.class) && conn.isOpen()) {
				conn.close();
			} else {
				if (conn instanceof IRemoteConnectionWorkingCopy) {
					IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) conn;
					if (wc.isDirty()) {
						MessageDialog dialog = new MessageDialog(getShell(), Messages.ConnectionsPreferencePage_Confirm_Actions,
								null, Messages.ConnectionsPreferencePage_This_connection_contains_unsaved_changes,
								MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
						if (dialog.open() == 1) {
							return;
						}
						try {
							conn = wc.save();
						} catch (RemoteConnectionException e) {
							RemoteUIPlugin.log(e);
						}
						/*
						 * Replace working copy with original so that the correct version will be used in the future
						 */
						fWorkingCopies.put(conn.getName(), conn);
					}
				}
				IRemoteUIConnectionService mgr = conn.getConnectionType().getService(IRemoteUIConnectionService.class);
				if (mgr != null) {
					mgr.openConnectionWithProgress(getShell(), null, conn);
				}
			}
			fConnectionViewer.refresh();
			updateEnablement();
		}
	}

	/**
	 * Update the connection manager with changes to the connections.
	 */
	private void updateConnections() {
		/*
		 * Remove any deleted connections
		 */
		for (IRemoteConnection conn : fConnectionType.getConnections()) {
			if (!fWorkingCopies.containsKey(conn.getName())
					&& (!conn.hasService(IRemoteConnectionControlService.class) || !conn.isOpen())) {
				try {
					fConnectionType.removeConnection(conn);
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
				IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) conn;
				if (wc.isDirty()) {
					try {
						wc.save();
					} catch (RemoteConnectionException e) {
						RemoteUIPlugin.log(e);
					}
				}
			}
		}
		initWorkingConnections();
	}

	private void updateEnablement() {
		fEditButton.setEnabled(false);
		fRemoveButton.setEnabled(false);
		fOpenButton.setEnabled(false);
		fCloseButton.setEnabled(false);
		if (fSelectedConnection != null) {
			IRemoteConnection conn = getOriginalIfClean(fSelectedConnection);
			if (conn.hasService(IRemoteConnectionControlService.class)) {
				if (!conn.isOpen()) {
					fEditButton
							.setEnabled((conn.getConnectionType().getCapabilities() & IRemoteConnectionType.CAPABILITY_EDIT_CONNECTIONS) != 0);
					fRemoveButton
							.setEnabled((conn.getConnectionType().getCapabilities() & IRemoteConnectionType.CAPABILITY_REMOVE_CONNECTIONS) != 0);
					fOpenButton.setEnabled(true);
				} else {
					fCloseButton.setEnabled(true);
				}
			} else {
				fEditButton
						.setEnabled((conn.getConnectionType().getCapabilities() & IRemoteConnectionType.CAPABILITY_EDIT_CONNECTIONS) != 0);
			}
		}
	}

	/**
	 * Get the original connection if the working copy is not dirty
	 * 
	 * @param conn
	 * @return
	 */
	private IRemoteConnection getOriginalIfClean(IRemoteConnection conn) {
		if (conn instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) conn;
			if (!wc.isDirty()) {
				return wc.getOriginal();
			}
		}
		return conn;
	}
}
