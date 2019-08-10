/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.docker.launcher.ContainerCommandLauncher;
import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.internal.docker.launcher.ContainerPropertyVolumesModel.MountType;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageListener;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

@SuppressWarnings("restriction")
public class ContainerPropertyTab extends AbstractCBuildPropertyTab
		implements IDockerConnectionManagerListener, IDockerImageListener {

	public final static String VOLUME_SEPARATOR = "|"; //$NON-NLS-1$

	private final static String GNU_ELF_PARSER_ID = "org.eclipse.cdt.core.GNU_ELF"; //$NON-NLS-1$
	private final static String ELF_PARSER_ID = "org.eclipse.cdt.core.ELF"; //$NON-NLS-1$
	private static final String RUN_IN_CONFIGURE_LAUNCHER = "org.eclipse.cdt.autotools.core.property.launchAutotoolsInContainer"; //$NON-NLS-1$

	private Combo imageCombo;
	private Combo connectionSelector;
	private Button enableButton;
	private Button launchAutotoolsButton;
	private Button addButton;
	private IDockerConnection connection;
	private IDockerConnection[] connections;
	private IDockerImageListener containerTab;
	private boolean isAutotoolsProject;

	private String connectionName;
	private String connectionUri = ""; //$NON-NLS-1$

	private boolean initialEnabled;
	private boolean initialAutotoolsLaunchEnabled;
	private String initialConnection;
	private String initialImageId;
	private String initialVolumes;
	private String initialSelectedVolumes;

	private boolean multiChange;

	private List<IDockerImage> displayedImages = new ArrayList<>();

	private IConfiguration iCfg;
	private ICConfigurationDescription iCfgd;

	private final DataBindingContext dbc = new DataBindingContext();
	private final ContainerPropertyVolumesModel model;

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
				refreshImages();
				setVolumeControlsEnabled(new Button[] { addButton }, false);
			}
			connectionName = connection.getName();
			setConnection(connectionUri);
			model.setConnection(connection);
		}

	};

	public ContainerPropertyTab() {
		this.containerTab = this;
		this.model = new ContainerPropertyVolumesModel((IDockerConnection) null);
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
		iCfgd = getResDesc().getConfiguration();

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 5;
		enableButton.setLayoutData(gd);

		Label connectionSelectorLabel = new Label(usercomp, SWT.NULL);
		connectionSelectorLabel.setText(Messages.ContainerTab_Connection_Selector_Label);
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
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		connectionSelector.setLayoutData(gd);

		Label label1 = new Label(usercomp, SWT.NULL);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = false;
		label1.setLayoutData(gd);

		Label imageSelectorLabel = new Label(usercomp, SWT.NULL);
		imageSelectorLabel.setText(Messages.ContainerTab_Image_Selector_Label);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		connectionSelectorLabel.setLayoutData(gd);

		imageCombo = new Combo(usercomp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		imageCombo.setLayoutData(gd);

		Label label2 = new Label(usercomp, SWT.NULL);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = false;
		label2.setLayoutData(gd);

		initializeImageCombo();

		imageCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setImageId(imageCombo.getText());
				model.setSelectedImage(displayedImages.get(imageCombo.getSelectionIndex()));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		createVolumeSettingsContainer(usercomp);

		try {
			IProject project = iCfgd.getProjectDescription().getProject();
			IProjectNature nature = project.getNature("org.eclipse.cdt.autotools.core.autotoolsNatureV2"); //$NON-NLS-1$
			isAutotoolsProject = (nature != null);
			if (isAutotoolsProject) {
				launchAutotoolsButton = new Button(usercomp, SWT.CHECK);
				launchAutotoolsButton.setText(Messages.ContainerPropertyTab_Run_Autotools_In_Container_Msg);
				launchAutotoolsButton.setToolTipText(Messages.ContainerPropertyTab_Run_Autotools_In_Container_Tooltip);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 5;
				launchAutotoolsButton.setLayoutData(gd);
				initializeLaunchAutotoolsButton();
				launchAutotoolsButton.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						setLaunchAutotoolsEnablement(launchAutotoolsButton.getSelection());
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// ignore
					}
				});
			}
		} catch (CoreException e) {
			DockerLaunchUIPlugin.log(e);
		}

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

	private void createVolumeSettingsContainer(final Composite container) {
		final Label volumesLabel = new Label(container, SWT.NONE);
		volumesLabel.setText(WizardMessages.getString("ImageRunResourceVolVarPage.dataVolumesLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(false, false).applyTo(volumesLabel);
		final CheckboxTableViewer dataVolumesTableViewer = createVolumesTable(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).hint(400, 200)
				.applyTo(dataVolumesTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(addButton);
		addButton.setText(WizardMessages.getString("ImageRunResourceVolVarPage.addButton")); //$NON-NLS-1$
		addButton.addSelectionListener(onAddDataVolume(dataVolumesTableViewer));
		if (imageCombo.getText() != null && !imageCombo.getText().equals("")) {
			setVolumeControlsEnabled(new Button[] { addButton }, true);
		}
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(editButton);
		editButton.setText(WizardMessages.getString("ImageRunResourceVolVarPage.editButton")); //$NON-NLS-1$
		editButton.addSelectionListener(onEditDataVolume(dataVolumesTableViewer));
		editButton.setEnabled(false);
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(removeButton);
		removeButton.setText(WizardMessages.getString("ImageRunResourceVolVarPage.removeButton")); //$NON-NLS-1$
		removeButton.addSelectionListener(onRemoveDataVolumes(dataVolumesTableViewer));
		removeButton.setEnabled(false);
		// update table content when selected image changes
		bind(dataVolumesTableViewer, model.getDataVolumes(), BeanProperties.values(DataVolumeModel.class,
				DataVolumeModel.CONTAINER_PATH, DataVolumeModel.MOUNT, DataVolumeModel.READ_ONLY_VOLUME));
		dbc.bindSet(ViewersObservables.observeCheckedElements(dataVolumesTableViewer, DataVolumeModel.class),
				BeanProperties.set(ContainerPropertyVolumesModel.SELECTED_DATA_VOLUMES).observe(model));
		// disable the edit and removeButton if the table is empty
		dataVolumesTableViewer.addSelectionChangedListener(onSelectionChanged(editButton, removeButton));

		initializeVolumesTable();
	}

	/**
	 * Same as
	 * {@link ViewerSupport#bind(StructuredViewer, IObservableList, org.eclipse.core.databinding.property.value.IValueProperty[])
	 * but with a custom LabelProvider, DataVolumesLabelProvider
	 *
	 * @param viewer
	 * @param input
	 * @param labelProperties
	 */
	private void bind(final StructuredViewer viewer, final IObservableList input,
			final IBeanValueProperty[] labelProperties) {
		final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		if (viewer.getInput() != null) {
			viewer.setInput(null);
		}
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new DataVolumesLabelProvider(
				Properties.observeEach(contentProvider.getKnownElements(), labelProperties)));
		if (input != null) {
			viewer.setInput(input);
		}

	}

	private ISelectionChangedListener onSelectionChanged(final Button... targetButtons) {
		return e -> {
			if (e.getSelection().isEmpty()) {
				setVolumeControlsEnabled(targetButtons, false);
			} else {
				setVolumeControlsEnabled(targetButtons, true);
			}
		};
	}

	private static void setVolumeControlsEnabled(final Control[] controls, final boolean enabled) {
		for (Control control : controls) {
			if (control != null) {
				control.setEnabled(enabled);
			}
		}
	}

	private SelectionListener onAddDataVolume(final CheckboxTableViewer dataVolumesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final ContainerDataVolumeDialog dialog = new ContainerDataVolumeDialog(
					Display.getDefault().getActiveShell(), model.getConnection());
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final DataVolumeModel dataVolume = dialog.getDataVolume();
				dataVolume.setSelected(true);
				model.getDataVolumes().add(dataVolume);
				model.getSelectedDataVolumes().add(dataVolume);
				dataVolumesTableViewer.setChecked(dataVolume, true);
				setVolumes();
			}
		});
	}

	private SelectionListener onEditDataVolume(final CheckboxTableViewer dataVolumesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = (IStructuredSelection) dataVolumesTableViewer.getSelection();
			if (selection.isEmpty()) {
				return;
			}
			final DataVolumeModel selectedDataVolume = (DataVolumeModel) selection.getFirstElement();
			final ContainerDataVolumeDialog dialog = new ContainerDataVolumeDialog(
					Display.getDefault().getActiveShell(), model.getConnection(), selectedDataVolume);
			dialog.create();
			if (dialog.open() == IDialogConstants.OK_ID) {
				final DataVolumeModel dialogDataVolume = dialog.getDataVolume();
				selectedDataVolume.setContainerMount(dialogDataVolume.getContainerMount());
				selectedDataVolume.setMountType(dialogDataVolume.getMountType());
				selectedDataVolume.setHostPathMount(dialogDataVolume.getHostPathMount());
				selectedDataVolume.setContainerMount(dialogDataVolume.getContainerMount());
				selectedDataVolume.setReadOnly(dialogDataVolume.isReadOnly());
				model.getSelectedDataVolumes().add(selectedDataVolume);
				dataVolumesTableViewer.setChecked(selectedDataVolume, true);
				setVolumes();
			}
		});
	}

	private SelectionListener onRemoveDataVolumes(final TableViewer dataVolumesTableViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final IStructuredSelection selection = dataVolumesTableViewer.getStructuredSelection();
			for (@SuppressWarnings("unchecked")
			Iterator<DataVolumeModel> iterator = selection.iterator(); iterator.hasNext();) {
				final DataVolumeModel volume = iterator.next();
				model.removeDataVolume(volume);
				model.getSelectedDataVolumes().remove(volume);
			}
			setVolumes();
		});
	}

	private CheckboxTableViewer createVolumesTable(final Composite container) {
		final Table table = new Table(container,
				SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		final CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		dbc.bindSet(ViewersObservables.observeCheckedElements(tableViewer, DataVolumeModel.class),
				BeanProperties.set(ContainerPropertyVolumesModel.SELECTED_DATA_VOLUMES).observe(model));
		addTableViewerColumn(tableViewer, WizardMessages.getString("ImageRunResourceVolVarPage.containerPathColumn"), //$NON-NLS-1$
				180);
		addTableViewerColumn(tableViewer, WizardMessages.getString("ImageRunResourceVolVarPage.mountColumn"), //$NON-NLS-1$
				180);
		addTableViewerColumn(tableViewer, WizardMessages.getString("ImageRunResourceVolVarPage.readonlyColumn"), //$NON-NLS-1$
				60);
		return tableViewer;
	}

	private TableViewerColumn addTableViewerColumn(final TableViewer tableViewer, final String title, final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		if (title != null) {
			column.setText(title);
		}
		column.setWidth(width);
		return viewerColumn;
	}

	private static final class DataVolumesLabelProvider extends ObservableMapLabelProvider {

		private Image CONTAINER_IMAGE = SWTImagesFactory.DESC_CONTAINER.createImage();
		private Image FOLDER_CLOSED_IMAGE = SWTImagesFactory.DESC_FOLDER_CLOSED.createImage();
		private Image FILE_IMAGE = SWTImagesFactory.DESC_FILE.createImage();

		public DataVolumesLabelProvider(final IObservableMap[] attributeMaps) {
			super(attributeMaps);
		}

		@Override
		public void dispose() {
			CONTAINER_IMAGE.dispose();
			FOLDER_CLOSED_IMAGE.dispose();
			FILE_IMAGE.dispose();
			super.dispose();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			final DataVolumeModel dataVolume = ((DataVolumeModel) element);
			if (dataVolume.getMountType() != null && columnIndex == 1) {
				switch (dataVolume.getMountType()) {
				case CONTAINER:
					return CONTAINER_IMAGE;
				case HOST_FILE_SYSTEM:
					final File hostFile = new File(dataVolume.getMount());
					if (!hostFile.exists() || hostFile.isDirectory()) {
						return FOLDER_CLOSED_IMAGE;
					} else {
						return FILE_IMAGE;
					}
				default:
					return null;
				}
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			final DataVolumeModel dataVolume = ((DataVolumeModel) element);
			switch (columnIndex) {
			case 0:
				return dataVolume.getContainerPath();
			case 1:
				return dataVolume.getMount();
			case 2:
				if (dataVolume.getMountType() != MountType.HOST_FILE_SYSTEM) {
					return null;
				} else if (dataVolume.isReadOnly()) {
					return WizardMessages.getString("ImageRunResourceVolVarPage.true"); //$NON-NLS-1$
				}
				return WizardMessages.getString("ImageRunResourceVolVarPage.false"); //$NON-NLS-1$
			default:
				return null;
			}
		}
	}

	private void setVolumes() {
		StringBuffer buffer = new StringBuffer();
		String separator = ""; //$NON-NLS-1$
		for (DataVolumeModel volume : model.getDataVolumes()) {
			buffer.append(separator);
			buffer.append(volume.toString());
			separator = VOLUME_SEPARATOR;
		}
		StringBuffer selectedBuffer = new StringBuffer();
		separator = ""; //$NON-NLS-1$
		for (DataVolumeModel volume : model.getSelectedDataVolumes()) {
			selectedBuffer.append(separator);
			selectedBuffer.append(volume.toString());
			separator = VOLUME_SEPARATOR;
		}
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg).getItems();
			for (int i = 0; i < cfs.length; i++) {
				IConfiguration cfg = cfs[i];
				IOptionalBuildProperties p = cfg.getOptionalBuildProperties();
				p.setProperty(ContainerCommandLauncher.VOLUMES_ID, buffer.toString());
				p.setProperty(ContainerCommandLauncher.SELECTED_VOLUMES_ID, selectedBuffer.toString());
			}
		} else {
			IOptionalBuildProperties p = iCfg.getOptionalBuildProperties();
			p.setProperty(ContainerCommandLauncher.VOLUMES_ID, buffer.toString());
			p.setProperty(ContainerCommandLauncher.SELECTED_VOLUMES_ID, selectedBuffer.toString());
		}
	}

	private void setEnablement(boolean enabled) {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg).getItems();
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
		// if enabled, make sure we have ELF binary parsers specified
		if (enabled) {
			String[] ids = CoreModelUtil.getBinaryParserIds(page.getCfgsEditable());
			List<String> idList = new ArrayList<>(Arrays.asList(ids));
			if (!idList.contains(GNU_ELF_PARSER_ID)) {
				idList.add(GNU_ELF_PARSER_ID);
			}
			if (!idList.contains(ELF_PARSER_ID)) {
				idList.add(ELF_PARSER_ID);
			}
			CoreModelUtil.setBinaryParserIds(page.getCfgsEditable(), idList.toArray(new String[0]));
		}
	}

	private void setLaunchAutotoolsEnablement(boolean enabled) {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg).getItems();
			for (int i = 0; i < cfs.length; i++) {
				IConfiguration cfg = cfs[i];
				IOptionalBuildProperties p = cfg.getOptionalBuildProperties();
				p.setProperty(RUN_IN_CONFIGURE_LAUNCHER, Boolean.toString(launchAutotoolsButton.getSelection()));
			}
		} else {
			IOptionalBuildProperties p = iCfg.getOptionalBuildProperties();
			p.setProperty(RUN_IN_CONFIGURE_LAUNCHER, Boolean.toString(launchAutotoolsButton.getSelection()));
		}
	}

	private void setImageId(String imageId) {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg).getItems();
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
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg).getItems();
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
		if (isAutotoolsProject) {
			launchAutotoolsButton.setEnabled(enabled);
		}
		setVolumeControlsEnabled(new Button[] { addButton }, enabled);
	}

	private void initializeEnablementButton() {
		initialEnabled = false;
		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		String savedEnabled = properties.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		if (savedEnabled != null) {
			initialEnabled = Boolean.parseBoolean(savedEnabled);
		}
		enableButton.setSelection(initialEnabled);
		setControlsEnabled(initialEnabled);
	}

	private void initializeLaunchAutotoolsButton() {
		initialEnabled = false;
		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		String savedEnabled = properties.getProperty(RUN_IN_CONFIGURE_LAUNCHER);
		if (savedEnabled != null) {
			initialAutotoolsLaunchEnabled = Boolean.parseBoolean(savedEnabled);
		}
		launchAutotoolsButton.setSelection(initialAutotoolsLaunchEnabled);
	}

	private void initializeConnectionSelector() {
		int defaultIndex = -1;
		initialConnection = null;
		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		String id = properties.getProperty(ContainerCommandLauncher.CONNECTION_ID);
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
			model.setConnection(connection);
		}
	}

	private void refreshImages() {
		if (connection != null) {
			java.util.List<IDockerImage> images = connection.getImages();
			if (images == null || images.size() == 0) {
				// setsetErrorMessage(Messages.ContainerTab_Error_No_Images);
				return;
			}
			connection.removeImageListener(containerTab);
			ArrayList<String> imageNames = new ArrayList<>();
			displayedImages = new ArrayList<>();
			for (IDockerImage image : images) {
				java.util.List<String> tags = image.repoTags();
				if (tags != null) {
					for (String tag : tags) {
						if (!tag.equals("<none>:<none>")) { //$NON-NLS-1$
							imageNames.add(tag);
							displayedImages.add(image);
						}
					}
				}
			}
			imageCombo.setItems(imageNames.toArray(new String[0]));
			if (initialImageId != null) {
				int index = imageCombo.indexOf(initialImageId);
				if (index > -1) {
					imageCombo.select(index);
					model.setSelectedImage(displayedImages.get(index));
					setVolumeControlsEnabled(new Button[] { addButton }, true);
				} else {
				}
			}
			connection.addImageListener(containerTab);
		}

	}

	private void initializeImageCombo() {
		initialImageId = null;
		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		String id = properties.getProperty(ContainerCommandLauncher.IMAGE_ID);
		if (id != null) {
			initialImageId = id;
		}
		refreshImages();
	}

	private void initializeVolumesTable() {
		model.clearDataVolumes();
		int imageSelectionIndex = imageCombo.getSelectionIndex();
		if (imageSelectionIndex >= 0 && imageSelectionIndex < displayedImages.size()) {
			model.setSelectedImage(displayedImages.get(imageSelectionIndex));
		}

		IOptionalBuildProperties properties = iCfg.getOptionalBuildProperties();
		initialVolumes = properties.getProperty(ContainerCommandLauncher.VOLUMES_ID);
		Map<String, DataVolumeModel> volumeMap = parseVolumes(initialVolumes);
		initialSelectedVolumes = properties.getProperty(ContainerCommandLauncher.SELECTED_VOLUMES_ID);
		Map<String, DataVolumeModel> selectedVolumeMap = parseVolumes(initialSelectedVolumes);
		Set<DataVolumeModel> selectedVolumes = new HashSet<>();
		for (DataVolumeModel dvm : selectedVolumeMap.values()) {
			// we need selected volumes to be volumes that are in the volumes
			// collection, so just replace them in the volumes Map so they will
			// be
			// the same objects
			volumeMap.put(dvm.getContainerPath(), dvm);
			selectedVolumes.add(dvm);
		}
		model.setDataVolumes(volumeMap.values());
		model.setSelectedDataVolumes(selectedVolumes);
	}

	private Map<String, DataVolumeModel> parseVolumes(String volumesString) {
		Map<String, DataVolumeModel> volumeMap = new HashMap<>();
		if (volumesString != null && !volumesString.equals("")) { //$NON-NLS-1$
			String[] volumes = volumesString.split("[" + VOLUME_SEPARATOR + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			for (String volume : volumes) {
				if (volume != null && !volume.equals("")) { //$NON-NLS-1$
					DataVolumeModel dataVolume = DataVolumeModel.parseString(volume);
					volumeMap.put(dataVolume.getContainerPath(), dataVolume);
				}
			}
		}
		return volumeMap;
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		setVolumes();
		boolean needToRecalculate = false;
		ICConfigurationDescription defaultCfg = null;
		if (page.isMultiCfg()) {
			ICMultiConfigDescription mc1 = (ICMultiConfigDescription) src.getConfiguration();
			ICMultiConfigDescription mc2 = (ICMultiConfigDescription) dst.getConfiguration();
			ICConfigurationDescription[] cds1 = (ICConfigurationDescription[]) mc1.getItems();
			ICConfigurationDescription[] cds2 = (ICConfigurationDescription[]) mc2.getItems();
			defaultCfg = cds1[0];
			for (int i = 0; i < cds1.length; i++)
				needToRecalculate |= applyToCfg(cds1[i], cds2[i]);
		} else {
			defaultCfg = src.getConfiguration();
			needToRecalculate = applyToCfg(src.getConfiguration(), dst.getConfiguration());
		}
		if (needToRecalculate) {
			recalculateSpecs(defaultCfg, true);
		}
	}

	private boolean applyToCfg(ICConfigurationDescription c1, ICConfigurationDescription c2) {
		Configuration cfg01 = (Configuration) getCfg(c1);
		Configuration cfg02 = (Configuration) getCfg(c2);
		IOptionalBuildProperties prop1 = cfg01.getOptionalBuildProperties();
		IOptionalBuildProperties prop2 = cfg02.getOptionalBuildProperties();
		boolean needToRecalculate = false;

		ICTargetPlatformSetting tps = c1.getTargetPlatformSetting();
		String[] pids = tps.getBinaryParserIds();
		ICTargetPlatformSetting tps2 = c2.getTargetPlatformSetting();
		tps2.setBinaryParserIds(pids);

		String enablementProperty = prop1.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		String enablementProperty2 = prop2.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		if (enablementProperty != null && !enablementProperty.equals(enablementProperty2)) {
			needToRecalculate = true;
		}
		prop2.setProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED, enablementProperty);

		String connectionProperty = prop1.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		String connectionProperty2 = prop2.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		if (connectionProperty != null && !connectionProperty.equals(connectionProperty2)) {
			needToRecalculate = true;
		}
		prop2.setProperty(ContainerCommandLauncher.CONNECTION_ID, connectionProperty);

		String imageProperty = prop1.getProperty(ContainerCommandLauncher.IMAGE_ID);
		String imageProperty2 = prop2.getProperty(ContainerCommandLauncher.IMAGE_ID);
		if (imageProperty != null && !imageProperty.equals(imageProperty2)) {
			needToRecalculate = true;
		}
		prop2.setProperty(ContainerCommandLauncher.IMAGE_ID, imageProperty);

		String volumesProperty = prop1.getProperty(ContainerCommandLauncher.VOLUMES_ID);
		prop2.setProperty(ContainerCommandLauncher.VOLUMES_ID, volumesProperty);

		String selectedVolumesProperty = prop1.getProperty(ContainerCommandLauncher.SELECTED_VOLUMES_ID);
		prop2.setProperty(ContainerCommandLauncher.SELECTED_VOLUMES_ID, selectedVolumesProperty);

		return needToRecalculate;
	}

	protected void recalculateSpecs(ICConfigurationDescription cfgd, boolean performingApply) {
		IConfiguration cfg = getCfg(cfgd);
		IOptionalBuildProperties properties = cfg.getOptionalBuildProperties();
		initialEnabled = Boolean.parseBoolean(properties.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED));
		initialConnection = properties.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		initialImageId = properties.getProperty(ContainerCommandLauncher.IMAGE_ID);
		initialVolumes = properties.getProperty(ContainerCommandLauncher.VOLUMES_ID);
		initialSelectedVolumes = properties.getProperty(ContainerCommandLauncher.SELECTED_VOLUMES_ID);
		initialAutotoolsLaunchEnabled = Boolean.parseBoolean(properties.getProperty(RUN_IN_CONFIGURE_LAUNCHER));
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
		setVolumes();
		if (iCfg instanceof IMultiConfiguration) {
			needToRecalculate = multiChange;
		} else {
			IOptionalBuildProperties p = iCfg.getOptionalBuildProperties();
			if (initialEnabled != Boolean
					.parseBoolean(p.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED))) {
				needToRecalculate = true;
			} else if (initialEnabled == true) {
				if (!initialConnection.equals(p.getProperty(ContainerCommandLauncher.CONNECTION_ID))
						|| !initialImageId.equals(p.getProperty(ContainerCommandLauncher.IMAGE_ID))) {
					needToRecalculate = true;
				}
			}
		}
		if (needToRecalculate) {
			recalculateSpecs(ManagedBuildManager.getDescriptionForConfiguration(iCfg), false);
		}
	}

	@Override
	protected void performDefaults() {
		if (iCfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[]) ((IMultiConfiguration) iCfg).getItems();
			for (int i = 0; i < cfs.length; i++) {
				IOptionalBuildProperties props = cfs[i].getOptionalBuildProperties();
				props.setProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED, Boolean.toString(false));
				if (connections.length > 0) {
					props.setProperty(ContainerCommandLauncher.CONNECTION_ID, connections[0].getUri());
				} else {
					props.setProperty(ContainerCommandLauncher.CONNECTION_ID, null);
				}
				props.setProperty(ContainerCommandLauncher.IMAGE_ID, null);
				props.setProperty(ContainerCommandLauncher.VOLUMES_ID, null);
				props.setProperty(ContainerCommandLauncher.SELECTED_VOLUMES_ID, null);
			}
		} else {
			IOptionalBuildProperties props = iCfg.getOptionalBuildProperties();
			props.setProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED, Boolean.toString(false));
			if (connections.length > 0) {
				props.setProperty(ContainerCommandLauncher.CONNECTION_ID, connections[0].getUri());
			} else {
				props.setProperty(ContainerCommandLauncher.CONNECTION_ID, null);
			}
			props.setProperty(ContainerCommandLauncher.IMAGE_ID, null);
		}
		initialEnabled = false;
		initialConnection = null;
		initialImageId = null;
		initialVolumes = null;
		initialSelectedVolumes = null;
		if (connections.length > 0) {
			connectionSelector.select(0);
		}
		imageCombo.setText(""); //$NON-NLS-1$
		model.setDataVolumes(null);
		model.setSelectedDataVolumes(null);
		enableButton.setSelection(false);
		setControlsEnabled(false);
	}

	@Override
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null)
			return;
		iCfg = getCfg(cfgd.getConfiguration());
		iCfgd = cfgd.getConfiguration();

		multiChange = false;

		initializeConnectionSelector();
		initializeImageCombo();
		initializeEnablementButton();
		initializeVolumesTable();
	}

	@Override
	protected void updateButtons() {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeEvent(IDockerConnection changedConnection, int type) {
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
			model.setConnection(connection);
			connectionUri = connection.getUri();
		} else {
			connection = null;
			model.setConnection(null);
			model.setSelectedImage(null);
			connectionUri = "";
			connectionSelector.setText("");
		}
		connectionSelector.addModifyListener(connectionModifyListener);
	}

	@Override
	public void listChanged(IDockerConnection c, java.util.List<IDockerImage> list) {
		final IDockerImage[] finalList = list.toArray(new IDockerImage[0]);
		if (c.getName().equals(connection.getName())) {
			Display.getDefault().syncExec(() -> {
				connection.removeImageListener(containerTab);
				ArrayList<String> imageNames = new ArrayList<>();
				displayedImages = new ArrayList<>();
				for (IDockerImage image : finalList) {
					java.util.List<String> tags = image.repoTags();
					if (tags != null) {
						for (String tag : tags) {
							imageNames.add(tag);
							displayedImages.add(image);
						}
					}
				}
				if (!imageCombo.isDisposed())
					imageCombo.setItems(imageNames.toArray(new String[0]));
				connection.addImageListener(containerTab);
			});
		}
	}

}
