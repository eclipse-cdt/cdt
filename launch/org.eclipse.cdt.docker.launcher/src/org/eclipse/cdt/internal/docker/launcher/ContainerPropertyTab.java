/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

@SuppressWarnings("restriction")
public class ContainerPropertyTab extends AbstractCBuildPropertyTab
		implements IDockerConnectionManagerListener, IDockerImageListener {

	private Combo imageCombo;
	private Combo connectionSelector;
	private Button enableButton;

	private IDockerConnection connection;
	private IDockerConnection[] connections;
	private IDockerImageListener containerTab;

	private String connectionName;
	private String connectionUri = ""; //$NON-NLS-1$

	private boolean initialEnabled;
	private String initialConnection;
	private String initialImageId;

	private boolean multiChange;

	private IConfiguration iCfg;

	private ModifyListener connectionModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			int index = connectionSelector.getSelectionIndex();
			if (index < 0) {
				connection = null;
				connectionName = "";
				return;
			}
			if (connection != null)
				connection.removeImageListener(containerTab);
			connection = connections[index];
			connectionUri = connection.getUri();
			if (!connectionName.equals(connection.getName())) {
				imageCombo.setText("");
				initialImageId = null;
			}
			connectionName = connection.getName();
			setConnection(connectionUri);
		}

	};

	public ContainerPropertyTab() {
		this.containerTab = this;
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		usercomp.setLayout(new GridLayout(5, false));
		usercomp.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;

		usercomp.setLayoutData(gd);

		enableButton = new Button(usercomp, SWT.CHECK);
		enableButton.setText(Messages.ContainerPropertyTab_Enable_Msg);

		iCfg = getCfg();

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 5;
		enableButton.setLayoutData(gd);

		Label connectionSelectorLabel = new Label(usercomp, SWT.NULL);
		connectionSelectorLabel
				.setText(Messages.ContainerTab_Connection_Selector_Label);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = false;
		connectionSelectorLabel.setLayoutData(gd);

		connectionSelector = new Combo(usercomp, SWT.BORDER | SWT.READ_ONLY);
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
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		gd.grabExcessHorizontalSpace = true;
		connectionSelector.setLayoutData(gd);

		Label imageSelectorLabel = new Label(usercomp, SWT.NULL);
		imageSelectorLabel.setText(Messages.ContainerTab_Image_Selector_Label);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		connectionSelectorLabel.setLayoutData(gd);

		imageCombo = new Combo(usercomp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		gd.grabExcessHorizontalSpace = true;
		imageCombo.setLayoutData(gd);

		initializeImageCombo();

		imageCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setImageId(imageCombo.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		initializeEnablementButton();
		enableButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setControlsEnabled(enableButton.getSelection());
				setEnablement(enableButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}

		});

	}

	private void setEnablement(boolean enabled) {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg)
					.getItems();
			for (int i = 0; i < cfs.length; i++) {
				IConfiguration cfg = cfs[i];
				IOptionalBuildProperties p = cfg.getOptionalBuildProperties();
				p.setProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED,
						Boolean.toString(enableButton.getSelection()));
			}
		} else {
			IOptionalBuildProperties p = iCfg.getOptionalBuildProperties();
			p.setProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED,
					Boolean.toString(enableButton.getSelection()));
		}
	}

	private void setImageId(String imageId) {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg)
					.getItems();
			for (int i = 0; i < cfs.length; i++) {
				IConfiguration cfg = cfs[i];
				IOptionalBuildProperties p = cfg.getOptionalBuildProperties();
				p.setProperty(ContainerCommandLauncher.IMAGE_ID, imageId);
			}
		} else {
			IOptionalBuildProperties p = iCfg.getOptionalBuildProperties();
			p.setProperty(ContainerCommandLauncher.IMAGE_ID, imageId);
		}
	}

	private void setConnection(String uri) {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg)
					.getItems();
			for (int i = 0; i < cfs.length; i++) {
				IConfiguration cfg = cfs[i];
				IOptionalBuildProperties p = cfg.getOptionalBuildProperties();
				p.setProperty(ContainerCommandLauncher.CONNECTION_ID, uri);
			}
		} else {
			IOptionalBuildProperties p = iCfg.getOptionalBuildProperties();
			p.setProperty(ContainerCommandLauncher.CONNECTION_ID, uri);
		}
	}

	private void setControlsEnabled(boolean enabled) {
		imageCombo.setEnabled(enabled);
		connectionSelector.setEnabled(enabled);
	}

	private void initializeEnablementButton() {
		initialEnabled = false;
		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		String savedEnabled = properties
				.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		if (savedEnabled != null) {
			initialEnabled = Boolean
					.parseBoolean(savedEnabled);
		}
		enableButton.setSelection(initialEnabled);
		setControlsEnabled(initialEnabled);
	}

	private void initializeConnectionSelector() {
		int defaultIndex = -1;
		initialConnection = null;
		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		String id = properties
				.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		if (id != null) {
			initialConnection = id;
		}
		connections = DockerConnectionManager.getInstance().getConnections();
		if (connections.length == 0) {
			// setErrorMessage(Messages.ContainerTab_Error_No_Connections);
			return;
		}
		String[] connectionNames = new String[connections.length];
		for (int i = 0; i < connections.length; ++i) {
			connectionNames[i] = connections[i].getName();
			if (connections[i].getUri().equals(initialConnection))
				defaultIndex = i;
		}
		if (defaultIndex < 0) {
			initialEnabled = false;
			defaultIndex = 0;
		}
		connectionSelector.setItems(connectionNames);
		if (connections.length > 0) {
			connectionSelector.select(defaultIndex);
			connection = connections[defaultIndex];
			connectionName = connection.getName();
			connectionUri = connection.getUri();
			initialConnection = connectionUri;
		}
	}

	private void initializeImageCombo() {
		initialImageId = null;
		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		String id = properties
				.getProperty(ContainerCommandLauncher.IMAGE_ID);
		if (id != null) {
			initialImageId = id;
		}
		if (connection != null) {
			java.util.List<IDockerImage> images = connection.getImages();
			if (images == null || images.size() == 0) {
				// setsetErrorMessage(Messages.ContainerTab_Error_No_Images);
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
			if (initialImageId != null) {
				int index = imageCombo.indexOf(initialImageId);
				if (index > -1) {
					imageCombo.select(index);
				} else {
				}
			}
			connection.addImageListener(containerTab);
		}
	}

	@Override
	protected void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
		boolean needToRecalculate = false;
		ICConfigurationDescription defaultCfg = null;
		if (page.isMultiCfg()) {
			ICMultiConfigDescription mc1 = (ICMultiConfigDescription) src
					.getConfiguration();
			ICMultiConfigDescription mc2 = (ICMultiConfigDescription) dst
					.getConfiguration();
			ICConfigurationDescription[] cds1 = (ICConfigurationDescription[]) mc1
					.getItems();
			ICConfigurationDescription[] cds2 = (ICConfigurationDescription[]) mc2
					.getItems();
			defaultCfg = cds1[0];
			for (int i = 0; i < cds1.length; i++)
				needToRecalculate |= applyToCfg(cds1[i], cds2[i]);
		} else {
			defaultCfg = src.getConfiguration();
			needToRecalculate = applyToCfg(src.getConfiguration(),
					dst.getConfiguration());
		}
		if (needToRecalculate) {
			recalculateSpecs(defaultCfg, true);
		}
	}

	private boolean applyToCfg(ICConfigurationDescription c1,
			ICConfigurationDescription c2) {
		Configuration cfg01 = (Configuration) getCfg(c1);
		Configuration cfg02 = (Configuration) getCfg(c2);
		IOptionalBuildProperties prop1 = cfg01.getOptionalBuildProperties();
		IOptionalBuildProperties prop2 = cfg02.getOptionalBuildProperties();
		boolean needToRecalculate = false;
		String enablementProperty = prop1
				.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		String enablementProperty2 = prop2
				.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		if (!enablementProperty.equals(enablementProperty2)) {
			needToRecalculate = true;
		}
		prop2.setProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED,
				enablementProperty);
		String connectionProperty = prop1
				.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		String connectionProperty2 = prop2
				.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		if (connectionProperty != null
				&& !connectionProperty.equals(connectionProperty2)) {
			needToRecalculate = true;
		}
		prop2.setProperty(ContainerCommandLauncher.CONNECTION_ID,
				connectionProperty);
		String imageProperty = prop1
				.getProperty(ContainerCommandLauncher.IMAGE_ID);
		String imageProperty2 = prop2
				.getProperty(ContainerCommandLauncher.IMAGE_ID);
		if (imageProperty != null && !imageProperty.equals(imageProperty2)) {
			needToRecalculate = true;
		}
		prop2.setProperty(ContainerCommandLauncher.IMAGE_ID, imageProperty);
		return needToRecalculate;
	}


	protected void recalculateSpecs(ICConfigurationDescription cfgd,
			boolean performingApply) {
		IConfiguration cfg = getCfg(cfgd);
		IOptionalBuildProperties properties = cfg.getOptionalBuildProperties();
		initialEnabled = Boolean.parseBoolean(properties
				.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED));
		initialConnection = properties
				.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		initialImageId = properties
				.getProperty(ContainerCommandLauncher.IMAGE_ID);
		List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgd)
				.getLanguageSettingProviders();
		for (ILanguageSettingsProvider provider : providers) {
			if (provider instanceof GCCBuiltinSpecsDetector) {
				GCCBuiltinSpecsDetector d = (GCCBuiltinSpecsDetector) provider;
				// force recalculation of gcc include path
				d.clear();
				if (performingApply) {
					d.handleEvent(null);
				}
				// final IProject project = getProject();
				// CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));
			}
		}
	}


	@Override
	protected void performOK() {
		boolean needToRecalculate = false;
		if (iCfg instanceof IMultiConfiguration) {
			needToRecalculate = multiChange;
		} else {
			IOptionalBuildProperties p = iCfg.getOptionalBuildProperties();
			if (initialEnabled != Boolean.parseBoolean(p.getProperty(
					ContainerCommandLauncher.CONTAINER_BUILD_ENABLED))) {
				needToRecalculate = true;
			} else if (initialEnabled == true) {
				if (!initialConnection.equals(
						p.getProperty(ContainerCommandLauncher.CONNECTION_ID))
						|| !initialImageId.equals(p.getProperty(
								ContainerCommandLauncher.IMAGE_ID))) {
					needToRecalculate = true;
				}
			}
		}
		if (needToRecalculate) {
			recalculateSpecs(
					ManagedBuildManager.getDescriptionForConfiguration(iCfg),
					false);
		}
	}

	@Override
	protected void performDefaults() {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg)
					.getItems();
			for (int i = 0; i < cfs.length; i++) {
				IOptionalBuildProperties props = cfs[i]
						.getOptionalBuildProperties();
				props.setProperty(
						ContainerCommandLauncher.CONTAINER_BUILD_ENABLED,
						Boolean.toString(false));
				if (connections.length > 0) {
					props.setProperty(ContainerCommandLauncher.CONNECTION_ID,
							connections[0].getUri());
				} else {
					props.setProperty(ContainerCommandLauncher.CONNECTION_ID,
							null);
				}
				props.setProperty(ContainerCommandLauncher.IMAGE_ID, null);
			}
		} else {
			IOptionalBuildProperties props = iCfg.getOptionalBuildProperties();
			props.setProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED,
					Boolean.toString(false));
			if (connections.length > 0) {
				props.setProperty(ContainerCommandLauncher.CONNECTION_ID,
						connections[0].getUri());
			} else {
				props.setProperty(ContainerCommandLauncher.CONNECTION_ID, null);
			}
			props.setProperty(ContainerCommandLauncher.IMAGE_ID, null);
		}
		initialEnabled = false;
		initialConnection = null;
		initialImageId = null;
		if (connections.length > 0) {
			connectionSelector.select(0);
		}
		imageCombo.setText(""); //$NON-NLS-1$
		enableButton.setSelection(false);
		setControlsEnabled(false);
	}

	@Override
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null)
			return;
		iCfg = getCfg(cfgd.getConfiguration());

		multiChange = false;

		initializeConnectionSelector();
		initializeImageCombo();
		initializeEnablementButton();
	}

	@Override
	protected void updateButtons() {
		// TODO Auto-generated method stub

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

	@Override
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

}
