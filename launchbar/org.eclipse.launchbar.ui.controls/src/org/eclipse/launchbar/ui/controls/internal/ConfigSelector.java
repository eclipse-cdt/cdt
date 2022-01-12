/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.ui.DefaultDescriptorLabelProvider;
import org.eclipse.launchbar.ui.ILaunchBarUIManager;
import org.eclipse.launchbar.ui.NewLaunchConfigWizard;
import org.eclipse.launchbar.ui.NewLaunchConfigWizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ConfigSelector extends CSelector {
	private ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
	private ILaunchBarUIManager uiManager = Activator.getService(ILaunchBarUIManager.class);
	private DefaultDescriptorLabelProvider defaultProvider;

	private static final String[] noConfigs = new String[] { Messages.ConfigSelector_0 };

	public ConfigSelector(Composite parent, int style) {
		super(parent, style);
		setData(LaunchBarWidgetIds.ID, LaunchBarWidgetIds.CONFIG_SELECTOR);

		setToolTipText(Messages.ConfigSelector_1);
		defaultProvider = new DefaultDescriptorLabelProvider();

		setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				try {
					ILaunchDescriptor[] descs = manager.getLaunchDescriptors();
					if (descs.length == 0)
						return noConfigs;
					return descs;
				} catch (CoreException e) {
					return noConfigs;
				}
			}
		});

		setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof ILaunchDescriptor) {
					try {
						ILaunchDescriptor configDesc = (ILaunchDescriptor) element;
						ILabelProvider labelProvider = uiManager.getLabelProvider(configDesc);
						if (labelProvider != null) {
							Image img = labelProvider.getImage(element);
							if (img != null)
								return img;
						}
					} catch (CoreException e) {
						Activator.log(e);
					}
				}
				return defaultProvider.getImage(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return (String) element;
				} else if (element instanceof ILaunchDescriptor) {
					try {
						ILaunchDescriptor configDesc = (ILaunchDescriptor) element;
						ILabelProvider labelProvider = uiManager.getLabelProvider(configDesc);
						if (labelProvider != null) {
							String text = labelProvider.getText(element);
							if (text != null)
								return text;
						}
					} catch (CoreException e) {
						Activator.log(e);
					}
				}
				return defaultProvider.getText(element);
			}
		});
		// no sorter for top, data is sorted by provider in historical order
		setHistorySortComparator(null);
		// alphabetic sorter
		setSorter((ILaunchDescriptor o1, ILaunchDescriptor o2) -> o1.getName().compareTo(o2.getName()));

	}

	@Override
	protected void initializeListViewer(LaunchBarListViewer listViewer) {
		listViewer.setHistorySupported(true);
		listViewer.setHistoryPreferenceName(Activator.PREF_LAUNCH_HISTORY_SIZE);
		super.initializeListViewer(listViewer);
	}

	@Override
	protected void fireSelectionChanged() {
		Object selected = getSelection();
		if (selected instanceof ILaunchDescriptor) {
			ILaunchDescriptor configDesc = (ILaunchDescriptor) selected;
			try {
				manager.setActiveLaunchDescriptor(configDesc);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public boolean isEditable(Object element) {
		return element instanceof ILaunchDescriptor;
	}

	@Override
	public void handleEdit(Object element) {
		uiManager.openConfigurationEditor((ILaunchDescriptor) element);
	}

	@Override
	public boolean hasActionArea() {
		return true;
	}

	@Override
	public void createActionArea(Composite parent) {
		final Composite createButton = new Composite(parent, SWT.BORDER);
		createButton.setData(LaunchBarWidgetIds.ID, LaunchBarWidgetIds.NEW);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(createButton);
		GridLayoutFactory.fillDefaults().margins(7, 7).applyTo(createButton);
		createButton.setBackground(getBackground());

		final Label createLabel = new Label(createButton, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(createLabel);
		createLabel.setBackground(getBackground());
		createLabel.setText(Messages.ConfigSelector_2);

		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				final NewLaunchConfigWizard wizard = new NewLaunchConfigWizard();
				WizardDialog dialog = new NewLaunchConfigWizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					new Job(Messages.ConfigSelector_3) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								wizard.getWorkingCopy().doSave();
								return Status.OK_STATUS;
							} catch (CoreException e) {
								return e.getStatus();
							}
						}
					}.schedule();
				}
			}
		};

		createButton.addMouseListener(mouseListener);
		createLabel.addMouseListener(mouseListener);

		MouseTrackListener mouseTrackListener = new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				Color highlightColor = getHighlightColor();
				createButton.setBackground(highlightColor);
				createLabel.setBackground(highlightColor);
			}

			@Override
			public void mouseExit(MouseEvent e) {
				Color backgroundColor = getBackground();
				createButton.setBackground(backgroundColor);
				createLabel.setBackground(backgroundColor);
			}
		};
		createButton.addMouseTrackListener(mouseTrackListener);
		createLabel.addMouseTrackListener(mouseTrackListener);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(250, hHint, changed);
	}

	@Override
	public void setSelection(Object element) {
		if (element == null)
			element = noConfigs[0];
		super.setSelection(element);
	}

}
