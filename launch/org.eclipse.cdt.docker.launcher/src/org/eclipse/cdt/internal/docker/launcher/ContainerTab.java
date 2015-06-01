/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
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
import org.osgi.service.prefs.Preferences;

public class ContainerTab extends AbstractLaunchConfigurationTab implements
		IDockerConnectionManagerListener, IDockerImageListener {

	private List directoriesList;
	private String imageName;
	private String connectionName;
	private String connectionUri;
	private Boolean keepValue;
	private Boolean stdinValue;
	private IDockerConnection connection;
	private IDockerConnection[] connections;
	private IDockerImageListener containerTab;

	private Button newButton;
	private Button removeButton;
	private Button keepButton;
	private Button stdinButton;
	private Combo imageCombo;
	private Combo connectionSelector;

	private ModifyListener connectionModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			int index = connectionSelector.getSelectionIndex();
			if (connection != null)
				connection.removeImageListener(containerTab);
			connection = connections[index];
			if (!connectionName.equals(connection.getName()))
				updateLaunchConfigurationDialog();
			connectionName = connection.getName();
			connectionUri = connection.getUri();
			connection.addImageListener(containerTab);
		}

	};

	public ContainerTab() {
		super();
		containerTab = this;
	}

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite mainComposite = createComposite(parent, 3, 1,
				GridData.FILL_HORIZONTAL);
		mainComposite.setFont(font);
		setControl(mainComposite);

		Label connectionSelectorLabel = new Label(mainComposite, SWT.NULL);
		connectionSelectorLabel
				.setText(Messages.ContainerTab_Connection_Selector_Label);

		connectionSelector = new Combo(mainComposite, SWT.BORDER
				| SWT.READ_ONLY);
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
				GridData.VERTICAL_ALIGN_BEGINNING
						| GridData.HORIZONTAL_ALIGN_END);
		composite.setFont(font);
		newButton = createPushButton(composite,
				Messages.ContainerTab_New_Button, null); //$NON-NLS-1$
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

		removeButton = createPushButton(composite,
				Messages.ContainerTab_Remove_Button, null); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handleRemoveButtonSelected(directoriesList);
			}
		});
		removeButton.setEnabled(false);
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
		Boolean keepPref = prefs.getBoolean(
				PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
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
		stdinButton = createCheckButton(group,
				Messages.ContainerTab_Stdin_Support_Label);
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
	}

	private Composite createComposite(Composite parent, int columns, int hspan,
			int fill) {
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
			setWarningMessage(Messages.bind(
					Messages.ContainerTab_Warning_Connection_Not_Found,
					connectionUri, connections[0].getName()));
			defaultIndex = 0;
		}
		connectionSelector.setItems(connectionNames);
		if (connections.length > 0) {
			connectionSelector.setText(connectionNames[defaultIndex]);
			connection = connections[defaultIndex];
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
			ArrayList<String> imageNames = new ArrayList<String>();
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
		StringBuffer stripped = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		control.getAccessible().addAccessibleListener(
				new ControlAccessibleListener(stripped.toString()));
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
		configuration.setAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS,
				(String) null);
		configuration.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI, ""); //$NON-NLS-1$
		Preferences prefs = InstanceScope.INSTANCE
				.getNode(DockerLaunchUIPlugin.PLUGIN_ID);
		String image = prefs.get(PreferenceConstants.DEFAULT_IMAGE, ""); //$NON-NLS-1$
		configuration.setAttribute(ILaunchConstants.ATTR_IMAGE, image);
		Boolean keepContainer = prefs.getBoolean(
				PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
		configuration.setAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH,
				keepContainer);
		configuration.setAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT, false);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			java.util.List<String> additionalDirs = configuration.getAttribute(
					ILaunchConstants.ATTR_ADDITIONAL_DIRS,
					(java.util.List<String>) null);

			if (additionalDirs != null)
				directoriesList.setItems(additionalDirs.toArray(new String[0]));
			connectionUri = configuration.getAttribute(
					ILaunchConstants.ATTR_CONNECTION_URI, (String) "");
			int defaultIndex = 0;
			connections = DockerConnectionManager.getInstance()
					.getConnections();
			if (connections.length > 0) {
				if (!connectionUri.equals("")) { //$NON-NLS-1$
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
			imageName = configuration.getAttribute(ILaunchConstants.ATTR_IMAGE,
					"");
			imageCombo.setText(imageName);
			keepValue = configuration.getAttribute(
					ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, false);
			keepButton.setSelection(keepValue);
			stdinValue = configuration.getAttribute(
					ILaunchConstants.ATTR_STDIN_SUPPORT, false);
			stdinButton.setSelection(stdinValue);
		} catch (CoreException e) {
			setErrorMessage(Messages.bind(
					Messages.ContainerTab_Error_Reading_Configuration, e
							.getStatus().getMessage())); //$NON-NLS-1$
			DockerLaunchUIPlugin.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String[] dirs = directoriesList.getItems();
		configuration.setAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS,
				Arrays.asList(dirs));
		String image = imageCombo.getText();
		configuration.setAttribute(ILaunchConstants.ATTR_IMAGE, image);
		configuration.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI,
				connectionUri);
		configuration.setAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH,
				keepButton.getSelection());
		configuration.setAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT,
				stdinButton.getSelection());
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			return launchConfig.getAttribute(ILaunchConstants.ATTR_IMAGE,
					(String) null) != null;
		} catch (CoreException e) {
			return false;
		}
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
	public void changeEvent(int type) {
		String currUri = null;
		int currIndex = 0;
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
		} else {
			connection = null;
			connectionUri = "";
			connectionSelector.setText("");
		}
		connectionSelector.addModifyListener(connectionModifyListener);
	}

	public void listChanged(IDockerConnection c,
			java.util.List<IDockerImage> list) {
		final IDockerImage[] finalList = list.toArray(new IDockerImage[0]);
		if (c.getName().equals(connection.getName())) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					connection.removeImageListener(containerTab);
					ArrayList<String> imageNames = new ArrayList<String>();
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
				}

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
