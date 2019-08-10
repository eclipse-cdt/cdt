/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contibutors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher.ui.launchbar;

import java.util.ArrayList;

import org.eclipse.cdt.docker.launcher.IContainerLaunchTarget;
import org.eclipse.cdt.internal.docker.launcher.SWTImagesFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.2
 * @author jjohnstn
 *
 */
public class NewContainerTargetWizardPage extends WizardPage
		implements IDockerImageListener, IDockerConnectionManagerListener {

	private final ILaunchTarget launchTarget;

	private Text nameText;
	private Combo imageCombo;
	private Combo connectionSelector;
	private IDockerConnection connection;
	private IDockerConnection[] connections;
	private IDockerImageListener wizardPage;
	private String imageName;
	private String connectionName;
	private String connectionUri = "";

	public NewContainerTargetWizardPage(ILaunchTarget launchTarget) {
		super(NewContainerTargetWizardPage.class.getName());
		if (launchTarget == null) {
			setTitle(Messages.NewContainerTargetWizardPage_title);
			setDescription(Messages.NewContainerTargetWizardPage_description);
		} else {
			setTitle(Messages.EditContainerTargetWizardPage_title);
			setDescription(Messages.EditContainerTargetWizardPage_description);
		}
		this.launchTarget = launchTarget;
		this.wizardPage = this;
		if (launchTarget != null) {
			connectionUri = launchTarget.getAttribute(IContainerLaunchTarget.ATTR_CONNECTION_URI, null);
			imageName = launchTarget.getAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID, null);
		}
	}

	private ModifyListener connectionModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			int index = connectionSelector.getSelectionIndex();
			if (connection != null)
				connection.removeImageListener(wizardPage);
			connection = connections[index];
			connectionUri = connection.getUri();
			if (!connectionName.equals(connection.getName())) {
				setErrorMessage(null);
				imageName = null;
				initializeImageCombo();
				setPageComplete(false);
			}
			connectionName = connection.getName();
		}

	};

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewContainerTargetWizardPage_name);

		nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (launchTarget != null) {
			nameText.setText(launchTarget.getId());
			nameText.setEnabled(false);
		}

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.NewContainerTargetWizardPage_connection);

		connectionSelector = new Combo(comp, SWT.BORDER | SWT.READ_ONLY);
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
		gd.horizontalIndent = 5;
		connectionSelector.setLayoutData(gd);

		Label imageSelectorLabel = new Label(comp, SWT.NULL);
		imageSelectorLabel.setText(Messages.NewContainerTargetWizardPage_image);
		imageCombo = new Combo(comp, SWT.DROP_DOWN);
		GridData gd2 = new GridData();
		gd2.horizontalSpan = 2;
		gd2.horizontalIndent = 5;
		imageCombo.setLayoutData(gd2);

		initializeImageCombo();

		imageCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				imageName = imageCombo.getText();
				setPageComplete(imageName != null && !imageName.isEmpty());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		setPageComplete(false);
		setControl(comp);
	}

	public String getTargetName() {
		return nameText.getText().trim();
	}

	public String getConnectionURI() {
		return connectionUri;
	}

	public String getImageId() {
		return imageName;
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_CONTAINER);
	}

	private void initializeConnectionSelector() {
		int defaultIndex = -1;
		connections = DockerConnectionManager.getInstance().getConnections();
		if (connections.length == 0) {
			setErrorMessage(Messages.NewContainerTargetWizardPage_no_connections);
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
				setErrorMessage(Messages.NewContainerTargetWizardPage_no_images);
				return;
			}
			connection.removeImageListener(wizardPage);
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
			connection.addImageListener(wizardPage);
		}
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
				setErrorMessage(Messages.NewContainerTargetWizardPage_no_images);
			}
		} else {
			setErrorMessage(Messages.NewContainerTargetWizardPage_no_connections);
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
			setErrorMessage(Messages.NewContainerTargetWizardPage_no_images);
		}
		if (c.getName().equals(connection.getName())) {
			Display.getDefault().syncExec(() -> {
				connection.removeImageListener(wizardPage);
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
				connection.addImageListener(wizardPage);
			});
		}
	}

	@Override
	public void dispose() {
		if (connection != null)
			connection.removeImageListener(this);
		super.dispose();
	}

}
