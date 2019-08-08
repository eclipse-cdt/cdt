/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.docker.launcher.ContainerPropertyVolumesModel.MountType;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author xcoulon
 *
 */
public class ContainerDataVolumeDialog extends Dialog {

	private final DataVolumeModel model;

	private final DataBindingContext dbc = new DataBindingContext();

	private final List<String> containerNames;

	private final IDockerConnection connection;

	public ContainerDataVolumeDialog(final Shell parentShell, final IDockerConnection connection,
			final DataVolumeModel selectedDataVolume) {
		super(parentShell);
		this.connection = connection;
		this.model = new DataVolumeModel(selectedDataVolume);
		this.containerNames = WizardUtils.getContainerNames(connection);
	}

	public ContainerDataVolumeDialog(final Shell parentShell, final IDockerConnection connection) {
		super(parentShell);
		this.connection = connection;
		this.model = new DataVolumeModel();
		this.containerNames = WizardUtils.getContainerNames(connection);
	}

	public DataVolumeModel getDataVolume() {
		return model;
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		shell.setText(WizardMessages.getString("ContainerDataVolumeDialog.title")); //$NON-NLS-1$
	}

	/**
	 * Disable the 'OK' button by default
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		final Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			button.setEnabled(false);
		}
		return button;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, super.getInitialSize().y);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final int COLUMNS = 3;
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1).grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().margins(10, 10).numColumns(COLUMNS).applyTo(container);

		// Container path
		final Label containerPathLabel = new Label(container, SWT.NONE);
		containerPathLabel.setText(WizardMessages.getString("ContainerDataVolumeDialog.containerPathLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(containerPathLabel);
		final Text containerPathText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(containerPathText);
		final IObservableValue containerPathObservable = BeanProperties
				.value(DataVolumeModel.class, DataVolumeModel.CONTAINER_PATH).observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(containerPathText), containerPathObservable);
		// mount type
		final Label explanationLabel = new Label(container, SWT.NONE);
		explanationLabel.setText(WizardMessages.getString("ContainerDataVolumeDialog.explanationLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(COLUMNS, 1).grab(true, false)
				.applyTo(explanationLabel);
		final int INDENT = 20;
		// No mount
		final Button noMountButton = new Button(container, SWT.RADIO);
		noMountButton.setText(WizardMessages.getString("ContainerDataVolumeDialog.noMountButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).span(COLUMNS, 1).grab(true, false)
				.applyTo(noMountButton);
		bindButton(noMountButton, MountType.NONE);
		// File System mount
		final Button fileSystemMountButton = new Button(container, SWT.RADIO);
		fileSystemMountButton.setText(WizardMessages.getString("ContainerDataVolumeDialog.fileSystemMountButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).span(COLUMNS, 1).grab(true, false)
				.applyTo(fileSystemMountButton);
		final Label hostPathLabel = new Label(container, SWT.NONE);
		hostPathLabel.setText(WizardMessages.getString("ContainerDataVolumeDialog.hostPathLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(2 * INDENT, SWT.DEFAULT).grab(false, false)
				.applyTo(hostPathLabel);
		final Text hostPathText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(hostPathText);
		final IObservableValue hostPathObservable = BeanProperties
				.value(DataVolumeModel.class, DataVolumeModel.HOST_PATH_MOUNT).observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(hostPathText), hostPathObservable);
		// browse for directory
		final Button hostPathDirectoryButton = new Button(container, SWT.NONE);
		hostPathDirectoryButton.setText(WizardMessages.getString("ContainerDataVolumeDialog.directoryButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(hostPathDirectoryButton);
		hostPathDirectoryButton.addSelectionListener(onHostDirectoryPath());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false)
				.applyTo(new Label(container, SWT.NONE));
		// optional read-only access
		final Button readOnlyButton = new Button(container, SWT.CHECK);
		readOnlyButton.setText(WizardMessages.getString("ContainerDataVolumeDialog.readOnlyButton")); //$NON-NLS-1$
		readOnlyButton.setToolTipText(WizardMessages.getString("ContainerDataVolumeDialog.readOnlyButtonTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(COLUMNS - 2, 1).grab(true, false)
				.applyTo(readOnlyButton);
		final ISWTObservableValue readOnlyButtonObservable = WidgetProperties.selection().observe(readOnlyButton);
		dbc.bindValue(readOnlyButtonObservable,
				BeanProperties.value(DataVolumeModel.class, DataVolumeModel.READ_ONLY_VOLUME).observe(model));
		// browse for file
		final Button hostPathFileButton = new Button(container, SWT.NONE);
		hostPathFileButton.setText(WizardMessages.getString("ContainerDataVolumeDialog.fileButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(hostPathFileButton);
		hostPathFileButton.addSelectionListener(onHostFilePath());
		bindButton(fileSystemMountButton, MountType.HOST_FILE_SYSTEM, hostPathText, hostPathDirectoryButton,
				hostPathFileButton, readOnlyButton);

		// Container mount
		final Button containerMountButton = new Button(container, SWT.RADIO);
		containerMountButton.setText(WizardMessages.getString("ContainerDataVolumeDialog.containerMountButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(INDENT, 0).span(COLUMNS, 1).grab(true, false)
				.applyTo(containerMountButton);
		final Label containerSelectionLabel = new Label(container, SWT.NONE);
		containerSelectionLabel.setText(WizardMessages.getString("ContainerDataVolumeDialog.containerSelectionLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(2 * INDENT, SWT.DEFAULT)
				.applyTo(containerSelectionLabel);
		final Combo containerSelectionCombo = new Combo(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(1, 1)
				.applyTo(containerSelectionCombo);
		new ControlDecoration(containerSelectionCombo, SWT.TOP | SWT.LEFT);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false)
				.applyTo(new Label(container, SWT.NONE));
		bindButton(containerMountButton, MountType.CONTAINER, containerSelectionCombo);
		final ComboViewer containerSelectionComboViewer = new ComboViewer(containerSelectionCombo);
		containerSelectionComboViewer.setContentProvider(new ArrayContentProvider());
		containerSelectionComboViewer.setInput(this.containerNames);
		final IObservableValue selectedContainerObservable = BeanProperties
				.value(DataVolumeModel.class, DataVolumeModel.CONTAINER_MOUNT).observe(model);
		dbc.bindValue(WidgetProperties.selection().observe(containerSelectionCombo), selectedContainerObservable);
		new ContentProposalAdapter(containerSelectionCombo, new ComboContentAdapter() {
			@Override
			public void insertControlContents(Control control, String text, int cursorPosition) {
				final Combo combo = (Combo) control;
				final Point selection = combo.getSelection();
				combo.setText(text);
				selection.x = text.length();
				selection.y = selection.x;
				combo.setSelection(selection);
			}
		}, getContainerNameContentProposalProvider(containerSelectionCombo), null, null);

		// error message
		final Composite errorContainer = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(COLUMNS, 1).grab(true, true)
				.applyTo(errorContainer);
		GridLayoutFactory.fillDefaults().margins(6, 6).numColumns(2).applyTo(errorContainer);

		final Label errorMessageIcon = new Label(errorContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(20, SWT.DEFAULT).applyTo(errorMessageIcon);
		final Label errorMessageLabel = new Label(errorContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(errorMessageLabel);
		setupValidationSupport(errorMessageIcon, errorMessageLabel);
		return container;
	}

	private void setupValidationSupport(final Label errorMessageIcon, final Label errorMessageLabel) {
		for (Iterator<Binding> iterator = dbc.getBindings().iterator(); iterator.hasNext();) {
			final Binding binding = iterator.next();
			binding.getModel().addChangeListener(onDataVolumeSettingsChanged(errorMessageIcon, errorMessageLabel));
		}
	}

	/**
	 * Binds the given {@link MountType} to the given {@link Button} when it is
	 * selected, and set the enablement of the associated {@link Control} at the
	 * same time (ie: the {@link Control} are only enabled when the given
	 * {@link Button} is selected.
	 *
	 * @param button
	 *            the {@link Button} to bind
	 * @param mountType
	 *            the {@link MountType} to bind to the {@link Button}
	 * @param controls
	 *            the {@link Control}s to enable or disable when the Button is
	 *            selected/unselected.
	 * @return
	 */
	private Binding bindButton(final Button button, final MountType mountType, final Control... controls) {
		return dbc.bindValue(WidgetProperties.selection().observe(button),
				BeanProperties.value(DataVolumeModel.class, DataVolumeModel.MOUNT_TYPE).observe(model),
				new UpdateValueStrategy() {
					@Override
					public Object convert(Object value) {
						if (value.equals(Boolean.TRUE)) {
							setEnabled(controls, true);
							return mountType;
						}
						setEnabled(controls, false);
						return null;
					}

					private void setEnabled(final Control[] controls, final boolean enabled) {
						for (Control control : controls) {
							control.setEnabled(enabled);
						}
					}
				}, new UpdateValueStrategy() {
					@Override
					public Object convert(final Object value) {
						if (mountType.equals(value)) {
							button.setEnabled(true);
						}
						return mountType.equals(value);
					}
				});
	}

	private SelectionListener onHostDirectoryPath() {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
			final String selectedPath = directoryDialog.open();
			if (selectedPath != null) {
				model.setHostPathMount(selectedPath);
			}
		});
	}

	private SelectionListener onHostFilePath() {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final FileDialog fileDialog = new FileDialog(getShell());
			final String selectedPath = fileDialog.open();
			if (selectedPath != null) {
				model.setHostPathMount(selectedPath);
			}
		});
	}

	/**
	 * Creates an {@link IContentProposalProvider} to propose
	 * {@link IDockerContainer} names based on the current text.
	 *
	 * @param items
	 * @return
	 */
	private IContentProposalProvider getContainerNameContentProposalProvider(final Combo containerSelectionCombo) {
		return (contents, position) -> {
			final List<IContentProposal> proposals = new ArrayList<>();
			for (String containerName : containerSelectionCombo.getItems()) {
				if (containerName.contains(contents)) {
					proposals.add(new ContentProposal(containerName, containerName, containerName, position));
				}
			}
			return proposals.toArray(new IContentProposal[0]);
		};
	}

	private IChangeListener onDataVolumeSettingsChanged(final Label errorMessageIcon, final Label errorMessageLabel) {

		return event -> {
			// skip if dialog has been closed
			if (Display.getCurrent() == null || getShell().isDisposed()) {
				return;
			}
			final IStatus status = validateInput();
			Display.getCurrent().syncExec(() -> {
				if (status.isOK()) {
					errorMessageIcon.setVisible(false);
					errorMessageLabel.setVisible(false);
					setOkButtonEnabled(true);
				} else if (status.matches(IStatus.WARNING)) {
					errorMessageIcon.setVisible(true);
					errorMessageIcon.setImage(SWTImagesFactory.DESC_WARNING.createImage());
					errorMessageLabel.setVisible(true);
					errorMessageLabel.setText(status.getMessage());
					setOkButtonEnabled(true);
				} else if (status.matches(IStatus.ERROR)) {
					if (status.getMessage() != null && !status.getMessage().isEmpty()) {
						errorMessageIcon.setVisible(true);
						errorMessageIcon.setImage(SWTImagesFactory.DESC_ERROR.createImage());
						errorMessageLabel.setVisible(true);
						errorMessageLabel.setText(status.getMessage());
					}
					setOkButtonEnabled(false);
				}
			});
		};
	}

	private IStatus validateInput() {
		final String containerPath = model.getContainerPath();
		final MountType mountType = model.getMountType();
		final String hostPath = model.getHostPathMount();
		if (containerPath == null || containerPath.isEmpty()) {
			return ValidationStatus.error(null);
		} else if (mountType == null) {
			return ValidationStatus.error(null);
		} else if (mountType == MountType.HOST_FILE_SYSTEM && (hostPath == null || hostPath.isEmpty())) {
			return ValidationStatus.error(null);
		} else if (mountType == MountType.HOST_FILE_SYSTEM && !new File(hostPath).exists()) {
			return ValidationStatus.warning("The specified path does not exist on the host."); //$NON-NLS-1$
		} else if (mountType == MountType.CONTAINER) {
			final IDockerContainer container = WizardUtils.getContainer(connection, model.getContainerMount());
			if (container == null) {
				// just make sure that the dialog cannot complete
				return ValidationStatus.error(null);
			}
			final IDockerContainerInfo selectedContainerInfo = container.info();
			if (selectedContainerInfo != null && selectedContainerInfo.volumes() != null
					&& !selectedContainerInfo.volumes().containsKey(model.getContainerPath())) {
				return ValidationStatus
						.warning(WizardMessages.getFormattedString("ContainerDataVolumeDialog.volumeWarning", //$NON-NLS-1$
								model.getContainerPath()));
			}
		}
		return ValidationStatus.ok();
	}

	private void setOkButtonEnabled(final boolean enabled) {
		final Button okButton = getButton(IDialogConstants.OK_ID);
		// skip if 'OK' button does not exist yet.
		if (okButton != null) {
			okButton.setEnabled(enabled);
		}
	}

}
