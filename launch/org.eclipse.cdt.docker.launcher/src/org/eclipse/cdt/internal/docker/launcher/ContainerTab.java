/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.service.prefs.Preferences;

public class ContainerTab extends AbstractLaunchConfigurationTab
		implements IDockerConnectionManagerListener, IDockerImageListener {

	private List directoriesList;
	private String imageName;
	private String connectionName;
	private String connectionUri = "";
	private Boolean keepValue;
	private Boolean stdinValue;
	private Boolean privilegedValue;
	private IDockerConnection connection;
	private IDockerConnection[] connections;
	private IDockerImageListener containerTab;

	private Button newButton;
	private Button removeButton;

	private CheckboxTableViewer tableViewer;

	private Button keepButton;
	private Button stdinButton;
	private Button privilegedButton;
	private Combo imageCombo;
	private Combo connectionSelector;

	private ContainerTabModel model;

	private static final int INDENT = 1;

	private final DataBindingContext dbc = new DataBindingContext();

	private ModifyListener connectionModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			int index = connectionSelector.getSelectionIndex();
			if (connection != null)
				connection.removeImageListener(containerTab);
			connection = connections[index];
			connectionUri = connection.getUri();
			if (!connectionName.equals(connection.getName())) {
				setErrorMessage(null);
				initializeImageCombo();
			}
			connectionName = connection.getName();
		}

	};

	public ContainerTab() {
		super();
		containerTab = this;
		model = new ContainerTabModel();
	}

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite mainComposite = createComposite(parent, 3, 1, GridData.FILL_HORIZONTAL);
		mainComposite.setFont(font);
		setControl(mainComposite);

		Label connectionSelectorLabel = new Label(mainComposite, SWT.NULL);
		connectionSelectorLabel.setText(Messages.ContainerTab_Connection_Selector_Label);

		connectionSelector = new Combo(mainComposite, SWT.BORDER | SWT.READ_ONLY);
		initializeConnectionSelector();
		connectionSelector.addModifyListener(connectionModifyListener);
		// Following is a kludge so that on Linux the Combo is read-only but
		// has a white background.
		connectionSelector.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = false;
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		connectionSelector.setLayoutData(gd);

		Label imageSelectorLabel = new Label(mainComposite, SWT.NULL);
		imageSelectorLabel.setText(Messages.ContainerTab_Image_Selector_Label);
		imageCombo = new Combo(mainComposite, SWT.DROP_DOWN);
		imageCombo.setLayoutData(gd);

		initializeImageCombo();

		imageCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!imageName.equals(imageCombo.getText()))
					updateLaunchConfigurationDialog();
				imageName = imageCombo.getText();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		createDirectoryList(mainComposite);
		createButtons(mainComposite);
		createPortSettingsSection(mainComposite);
		createOptions(mainComposite);
	}

	private void createDirectoryList(Composite parent) {
		Composite comp = createComposite(parent, 1, 2, GridData.FILL_BOTH);

		Group group = new Group(comp, SWT.NONE);
		Font font = parent.getFont();
		group.setFont(font);
		group.setText(Messages.ContainerTab_Group_Name);

		GridData gd2 = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd2);

		group.setLayout(new GridLayout());

		directoriesList = new List(group, SWT.SINGLE | SWT.V_SCROLL);
		GridData gd3 = new GridData(GridData.FILL_BOTH);
		directoriesList.setLayoutData(gd3);
		directoriesList.setFont(font);
		directoriesList.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeButton.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void createButtons(Composite parent) {
		Font font = parent.getFont();
		Composite composite = createComposite(parent, 1, 1,
				GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
		composite.setFont(font);
		newButton = createPushButton(composite, Messages.ContainerTab_New_Button, null); //$NON-NLS-1$
		newButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData gdb = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		gdb.grabExcessHorizontalSpace = false;
		gdb.horizontalAlignment = SWT.FILL;
		gdb.minimumWidth = 120;
		newButton.setLayoutData(gdb);
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handleNewButtonSelected(directoriesList);
			}
		});

		removeButton = createPushButton(composite, Messages.ContainerTab_Remove_Button, null); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handleRemoveButtonSelected(directoriesList);
			}
		});
		removeButton.setEnabled(false);
	}

	@SuppressWarnings("unchecked")
	private void createPortSettingsSection(final Composite parent) {
		Font font = parent.getFont();
		Composite comp = createComposite(parent, 1, 2, GridData.FILL_BOTH);

		Group group = new Group(comp, SWT.NONE);
		group.setFont(font);
		group.setText(Messages.ContainerTab_Ports_Group_Name);

		GridData gd2 = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd2);

		group.setLayout(new GridLayout());

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3, 1).grab(true, false).applyTo(group);

		group.setLayout(new GridLayout());
		// specify ports
		final Label portSettingsLabel = new Label(group, SWT.NONE);
		portSettingsLabel.setText(Messages.ContainerTab_Specify_Ports_Label);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(3, 1)
				.applyTo(portSettingsLabel);
		final CheckboxTableViewer exposedPortsTableViewer = createPortSettingsTable(group);
		tableViewer = exposedPortsTableViewer;
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(3 - 1, 1).indent(INDENT, 0)
				.hint(200, 70).applyTo(exposedPortsTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		final Button addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(addButton);
		addButton.setText(Messages.ContainerTab_Add_Button);
		addButton.addSelectionListener(onAddPort(exposedPortsTableViewer));
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(editButton);
		editButton.setText(Messages.ContainerTab_Edit_Button);
		editButton.setEnabled(false);
		editButton.addSelectionListener(onEditPort(exposedPortsTableViewer));
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(removeButton);
		removeButton.setText(Messages.ContainerTab_Remove_Button);
		removeButton.addSelectionListener(onRemovePorts(exposedPortsTableViewer));
		ViewerSupport.bind(exposedPortsTableViewer, model.getExposedPorts(),
				BeanProperties.values(ExposedPortModel.class, ExposedPortModel.CONTAINER_PORT,
						ExposedPortModel.PORT_TYPE, ExposedPortModel.HOST_ADDRESS, ExposedPortModel.HOST_PORT));
		dbc.bindSet(ViewersObservables.observeCheckedElements(exposedPortsTableViewer, ExposedPortModel.class),
				BeanProperties.set(ContainerTabModel.SELECTED_PORTS).observe(model));
		checkAllElements(exposedPortsTableViewer);

		// disable the edit and removeButton if the table is empty
		exposedPortsTableViewer.addSelectionChangedListener(onSelectionChanged(editButton, removeButton));
		exposedPortsTableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				ExposedPortModel e = (ExposedPortModel) event.getElement();
				e.setSelected(event.getChecked());
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void checkAllElements(final CheckboxTableViewer exposedPortsTableViewer) {
		exposedPortsTableViewer.setAllChecked(true);
		model.setSelectedPorts(new HashSet<>(model.getExposedPorts()));
	}

	private SelectionListener onAddPort(final CheckboxTableViewer exposedPortsTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerPortDialog dialog = new ContainerPortDialog(getShell());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final ExposedPortModel port = dialog.getPort();
				port.setSelected(true);
				model.addAvailablePort(port);
				model.getSelectedPorts().add(port);
				exposedPortsTableViewer.setChecked(port, true);
				updateLaunchConfigurationDialog();
			}
		});
	}

	private SelectionListener onEditPort(final CheckboxTableViewer exposedPortsTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = exposedPortsTableViewer.getStructuredSelection();
			final ExposedPortModel selectedContainerPort = (ExposedPortModel) selection.getFirstElement();
			final ContainerPortDialog dialog = new ContainerPortDialog(getShell(), selectedContainerPort);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final ExposedPortModel configuredPort = dialog.getPort();
				selectedContainerPort.setContainerPort(configuredPort.getContainerPort());
				selectedContainerPort.setHostAddress(configuredPort.getHostAddress());
				selectedContainerPort.setHostPort(configuredPort.getHostPort());
				exposedPortsTableViewer.refresh();
				updateLaunchConfigurationDialog();
			}
		});
	}

	private SelectionListener onRemovePorts(final TableViewer portsTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = portsTableViewer.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<ExposedPortModel> iterator = selection.iterator(); iterator.hasNext();) {
				final ExposedPortModel port = iterator.next();
				model.removeAvailablePort(port);
				model.getSelectedPorts().remove(port);
				updateLaunchConfigurationDialog();
			}
		});
	}

	private ISelectionChangedListener onSelectionChanged(final Button... targetButtons) {
		return e -> {
			if (e.getSelection().isEmpty()) {
				setControlsEnabled(targetButtons, false);
			} else {
				setControlsEnabled(targetButtons, true);
			}
		};
	}

	private static void setControlsEnabled(final Control[] controls, final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	private CheckboxTableViewer createPortSettingsTable(final Composite container) {
		final Table table = new Table(container,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		final CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableViewerColum(tableViewer, Messages.ContainerTab_Port_Column, 100);
		createTableViewerColum(tableViewer, Messages.ContainerTab_Type_Column, 50);
		createTableViewerColum(tableViewer, Messages.ContainerTab_HostAddress_Column, 100);
		createTableViewerColum(tableViewer, Messages.ContainerTab_HostPort_Column, 100);
		tableViewer.setContentProvider(new ObservableListContentProvider());
		return tableViewer;
	}

	private TableViewerColumn createTableViewerColum(final TableViewer tableViewer, final String title,
			final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		if (title != null) {
			column.setText(title);
		}
		column.setWidth(width);
		return viewerColumn;
	}

	private void createOptions(Composite parent) {
		Font font = parent.getFont();
		Composite comp = createComposite(parent, 1, 3, GridData.FILL_BOTH);

		Group group = new Group(comp, SWT.NONE);
		group.setFont(font);
		group.setText(Messages.ContainerTab_Option_Group_Name);

		GridData gd2 = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd2);

		group.setLayout(new GridLayout());
		Preferences prefs = InstanceScope.INSTANCE.getNode(DockerLaunchUIPlugin.PLUGIN_ID);
		keepButton = createCheckButton(group, Messages.ContainerTab_Keep_Label);
		keepButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Boolean keepPref = prefs.getBoolean(PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
		keepButton.setSelection(keepPref);
		keepValue = keepPref;
		keepButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!keepValue.equals(keepButton.getSelection()))
					updateLaunchConfigurationDialog();
				keepValue = keepButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		stdinButton = createCheckButton(group, Messages.ContainerTab_Stdin_Support_Label);
		stdinButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		stdinValue = false;
		stdinButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!stdinValue.equals(stdinButton.getSelection()))
					updateLaunchConfigurationDialog();
				stdinValue = stdinButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		privilegedButton = createCheckButton(group, Messages.ContainerTab_Privileged_Mode_Label);
		privilegedButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		privilegedValue = false;
		privilegedButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!privilegedValue.equals(privilegedButton.getSelection()))
					updateLaunchConfigurationDialog();
				privilegedValue = privilegedButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
	}

	private Composite createComposite(Composite parent, int columns, int hspan, int fill) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	/**
	 * A New entry button has been pressed for the given text field. Prompt the
	 * user for a directory to add and enter the result in the given field.
	 */
	protected void handleNewButtonSelected(List list) {
		String directory = getDirectory();
		if (directory != null) {
			list.add(directory);
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Prompts the user to choose and configure a variable and returns the
	 * resulting string, suitable to be used as an attribute.
	 */
	private String getDirectory() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		return dialog.open();
	}

	/**
	 * The remove entry button has been pressed for the given text field. Remove
	 * the currently selected directory.
	 */
	protected void handleRemoveButtonSelected(List list) {
		int index = list.getSelectionIndex();
		list.remove(index);
		updateLaunchConfigurationDialog();
		removeButton.setEnabled(false);
	}

	private void initializeConnectionSelector() {
		int defaultIndex = -1;
		connections = DockerConnectionManager.getInstance().getConnections();
		if (connections.length == 0) {
			setErrorMessage(Messages.ContainerTab_Error_No_Connections);
			return;
		}
		String[] connectionNames = new String[connections.length];
		for (int i = 0; i < connections.length; ++i) {
			connectionNames[i] = connections[i].getName();
			if (connections[i].getUri().equals(connectionUri))
				defaultIndex = i;
		}
		if (defaultIndex < 0) {
			defaultIndex = 0;
		}
		connectionSelector.setItems(connectionNames);
		if (connections.length > 0) {
			connectionSelector.setText(connectionNames[defaultIndex]);
			connection = connections[defaultIndex];
			connectionName = connection.getName();
			connectionUri = connection.getUri();
		}
	}

	private void initializeImageCombo() {
		if (connection != null) {
			java.util.List<IDockerImage> images = connection.getImages();
			if (images == null || images.size() == 0) {
				setErrorMessage(Messages.ContainerTab_Error_No_Images);
				return;
			}
			connection.removeImageListener(containerTab);
			ArrayList<String> imageNames = new ArrayList<>();
			for (IDockerImage image : images) {
				java.util.List<String> tags = image.repoTags();
				if (tags != null) {
					for (String tag : tags) {
						if (!tag.equals("<none>:<none>")) //$NON-NLS-1$
							imageNames.add(tag);
					}
				}
			}
			imageCombo.setItems(imageNames.toArray(new String[0]));
			if (imageName != null)
				imageCombo.setText(imageName);
			connection.addImageListener(containerTab);
		}
	}

	public void addControlAccessibleListener(Control control, String controlName) {
		// Strip mnemonic (&)
		String[] strs = controlName.split("&"); //$NON-NLS-1$
		StringBuilder stripped = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		control.getAccessible().addAccessibleListener(new ControlAccessibleListener(stripped.toString()));
	}

	private class ControlAccessibleListener extends AccessibleAdapter {
		private String controlName;

		ControlAccessibleListener(String name) {
			controlName = name;
		}

		@Override
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS, (String) null);
		configuration.setAttribute(ILaunchConstants.ATTR_EXPOSED_PORTS, (String) null);
		configuration.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI, ""); //$NON-NLS-1$
		Preferences prefs = InstanceScope.INSTANCE.getNode(DockerLaunchUIPlugin.PLUGIN_ID);
		String image = prefs.get(PreferenceConstants.DEFAULT_IMAGE, ""); //$NON-NLS-1$
		configuration.setAttribute(ILaunchConstants.ATTR_IMAGE, image);
		Boolean keepContainer = prefs.getBoolean(PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
		configuration.setAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, keepContainer);
		configuration.setAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT, false);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			java.util.List<String> additionalDirs = configuration.getAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS,
					(java.util.List<String>) null);

			if (additionalDirs != null)
				directoriesList.setItems(additionalDirs.toArray(new String[0]));

			java.util.List<String> exposedPortInfos = configuration.getAttribute(ILaunchConstants.ATTR_EXPOSED_PORTS,
					Collections.<String>emptyList());
			model.removeExposedPorts();
			for (String port : exposedPortInfos) {
				ExposedPortModel m = ExposedPortModel.createPortModel(port);
				model.addAvailablePort(m);
				if (m.getSelected()) {
					model.getSelectedPorts().add(m);
					tableViewer.setChecked(m, true);
				}
			}
			connectionUri = configuration.getAttribute(ILaunchConstants.ATTR_CONNECTION_URI, "");
			int defaultIndex = 0;
			connections = DockerConnectionManager.getInstance().getConnections();
			if (connections.length > 0) {
				if (!connectionUri.isEmpty()) {
					String[] connectionNames = new String[connections.length];
					for (int i = 0; i < connections.length; ++i) {
						connectionNames[i] = connections[i].getName();
						if (connections[i].getUri().equals(connectionUri))
							defaultIndex = i;
					}
					connectionSelector.select(defaultIndex);
				} else {
					connectionUri = connections[0].getUri();
				}
			}
			imageName = configuration.getAttribute(ILaunchConstants.ATTR_IMAGE, "");
			imageCombo.setText(imageName);
			keepValue = configuration.getAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, false);
			keepButton.setSelection(keepValue);
			stdinValue = configuration.getAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT, false);
			stdinButton.setSelection(stdinValue);
			privilegedValue = configuration.getAttribute(ILaunchConstants.ATTR_PRIVILEGED_MODE, false);
			privilegedButton.setSelection(privilegedValue);
		} catch (CoreException e) {
			setErrorMessage(
					Messages.bind(Messages.ContainerTab_Error_Reading_Configuration, e.getStatus().getMessage())); //$NON-NLS-1$
			DockerLaunchUIPlugin.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String[] dirs = directoriesList.getItems();
		configuration.setAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS, Arrays.asList(dirs));
		String image = imageCombo.getText();
		configuration.setAttribute(ILaunchConstants.ATTR_IMAGE, image);
		configuration.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI, connectionUri);
		configuration.setAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, keepButton.getSelection());
		configuration.setAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT, stdinButton.getSelection());
		configuration.setAttribute(ILaunchConstants.ATTR_PRIVILEGED_MODE, privilegedButton.getSelection());
		configuration.setAttribute(ILaunchConstants.ATTR_EXPOSED_PORTS,
				ExposedPortModel.toArrayString(model.getExposedPorts()));
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			String image = launchConfig.getAttribute(ILaunchConstants.ATTR_IMAGE, (String) null);
			if (image == null)
				return false;
			int index = image.lastIndexOf(':'); //$NON-NLS-1$
			if (index <= 0)
				return false;
			if (connection.hasImage(image.substring(0, index), image.substring(index + 1))) {
				setWarningMessage(null);
				return true;
			} else {
				setWarningMessage(
						Messages.bind(Messages.ContainerTab_Warning_Image_Not_Found, image, connections[0].getName()));
			}
		} catch (CoreException e) {
			return false;
		}
		return false;
	}

	@Override
	public String getName() {
		return Messages.ContainerTab_Name;
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_CONTAINER);
	}

	@Override
	public void changeEvent(IDockerConnection changedConnection, int type) {
		String currUri = null;
		int currIndex = 0;
		setErrorMessage(null);
		connections = DockerConnectionManager.getInstance().getConnections();
		if (connection != null) {
			currUri = connection.getUri();
			currIndex = connectionSelector.getSelectionIndex();
		}
		String[] connectionNames = new String[connections.length];
		int index = 0;
		for (int i = 0; i < connections.length; ++i) {
			connectionNames[i] = connections[i].getName();
			if (connections[i].getUri().equals(currUri))
				index = i;
		}
		if (type == IDockerConnectionManagerListener.RENAME_EVENT) {
			index = currIndex; // no change in connection displayed
		}
		connectionSelector.removeModifyListener(connectionModifyListener);
		connectionSelector.setItems(connectionNames);
		if (connectionNames.length > 0) {
			connectionSelector.setText(connectionNames[index]);
			connection = connections[index];
			connectionUri = connection.getUri();
			java.util.List<IDockerImage> images = connection.getImages();
			if (images == null || images.size() == 0) {
				setErrorMessage(Messages.ContainerTab_Error_No_Images);
			}
		} else {
			setErrorMessage(Messages.ContainerTab_Error_No_Connections);
			connection = null;
			connectionUri = "";
			connectionSelector.setText("");
		}
		connectionSelector.addModifyListener(connectionModifyListener);
	}

	@Override
	public void listChanged(IDockerConnection c, java.util.List<IDockerImage> list) {
		setErrorMessage(null);
		final IDockerImage[] finalList = list.toArray(new IDockerImage[0]);
		if (finalList.length == 0) {
			setErrorMessage(Messages.ContainerTab_Error_No_Images);
		}
		if (c.getName().equals(connection.getName())) {
			Display.getDefault().syncExec(() -> {
				connection.removeImageListener(containerTab);
				ArrayList<String> imageNames = new ArrayList<>();
				for (IDockerImage image : finalList) {
					java.util.List<String> tags = image.repoTags();
					if (tags != null) {
						for (String tag : tags) {
							imageNames.add(tag);
						}
					}
				}
				if (!imageCombo.isDisposed())
					imageCombo.setItems(imageNames.toArray(new String[0]));
				connection.addImageListener(containerTab);
			});
		}
	}

	@Override
	public void dispose() {
		if (connection != null)
			connection.removeImageListener(containerTab);
		super.dispose();
	}
}
