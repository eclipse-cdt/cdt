/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.controls;

import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.DefaultDescriptorLabelProvider;
import org.eclipse.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.launchbar.ui.internal.commands.ConfigureActiveLaunchHandler;
import org.eclipse.launchbar.ui.internal.dialogs.NewLaunchConfigWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

@SuppressWarnings("restriction")
public class ConfigSelector extends CSelector {
	private LaunchBarUIManager uiManager = Activator.getDefault().getLaunchBarUIManager();
	private DefaultDescriptorLabelProvider defaultProvider;
	
	private static final String[] noConfigs = new String[] { "No Launch Configurations" };
	
	public ConfigSelector(Composite parent, int style) {
		super(parent, style);

		setToolTipText("Launch configuration");
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
				ILaunchDescriptor[] descs = uiManager.getManager().getLaunchDescriptors();
				if (descs.length == 0)
					return noConfigs;
				return descs;
			}
		});

		setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof ILaunchDescriptor) {
					try {
						ILaunchDescriptor configDesc = (ILaunchDescriptor)element;
						ILabelProvider labelProvider = uiManager.getLabelProvider(configDesc);
						if (labelProvider != null) {
							Image img = labelProvider.getImage(element);
							if (img != null)
								return img;
						}
					} catch (CoreException e) {
						Activator.log(e.getStatus());
					}
				}
				return defaultProvider.getImage(element);
			}
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return (String)element;
				} else if (element instanceof ILaunchDescriptor) {
					try {
						ILaunchDescriptor configDesc = (ILaunchDescriptor)element;
						ILabelProvider labelProvider = uiManager.getLabelProvider(configDesc);
						if (labelProvider != null) {
							String text = labelProvider.getText(element);
							if (text != null)
								return text;
						}
					} catch (CoreException e) {
						Activator.log(e.getStatus());
					}
				}
				return defaultProvider.getText(element);
			}
		});
		// no sorter for top, data is sorted by provider in historical order
		setHistorySortComparator(null);
		// alphabetic sorter
		setSorter(new Comparator<ILaunchDescriptor>() {
			@Override
			public int compare(ILaunchDescriptor o1, ILaunchDescriptor o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

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
				uiManager.getManager().setActiveLaunchDescriptor(configDesc);
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		}
	}

	@Override
	public boolean isEditable(Object element) {
		return element instanceof ILaunchDescriptor;
	}

	@Override
	public void handleEdit(Object element) {
		ConfigureActiveLaunchHandler.openConfigurationEditor((ILaunchDescriptor) element);
	}

	@Override
	public boolean hasActionArea() {
		return true;
	}

	@Override
	public void createActionArea(Composite parent) {
		Composite actionArea = new Composite(parent, SWT.NONE);
		GridLayout actionLayout = new GridLayout();
		actionLayout.marginWidth = actionLayout.marginHeight = 0;
		actionArea.setLayout(actionLayout);
		actionArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Composite createButton = new Composite(actionArea, SWT.NONE);
		createButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 7;
		createButton.setLayout(buttonLayout);
		createButton.setBackground(backgroundColor);
		createButton.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point size = createButton.getSize();
				GC gc = e.gc;
				gc.setForeground(outlineColor);
				gc.drawLine(0, 0, size.x, 0);
			}
		});

		final Label createLabel = new Label(createButton, SWT.None);
		createLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createLabel.setText("Create New Configuration...");
		createLabel.setBackground(backgroundColor);

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				final NewLaunchConfigWizard wizard = new NewLaunchConfigWizard();
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					new Job("Create Launch Configuration") {
						protected IStatus run(IProgressMonitor monitor) {
							try {
								ILaunchConfiguration config = wizard.getWorkingCopy().doSave();
								final LaunchBarManager barManager = uiManager.getManager();
								final ILaunchDescriptor desc = barManager.getDefaultDescriptorType().getDescriptor(config);
								barManager.setLaunchMode(desc, wizard.getLaunchMode());
								barManager.setActiveLaunchDescriptor(desc);
								return Status.OK_STATUS;
							} catch (CoreException e) {
								return e.getStatus();
							}
						};
					}.schedule();
				}
			}
		};

		createButton.addMouseListener(mouseListener);
		createLabel.addMouseListener(mouseListener);

		MouseTrackListener mouseTrackListener = new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				createButton.setBackground(highlightColor);
				createLabel.setBackground(highlightColor);
			}
			@Override
			public void mouseExit(MouseEvent e) {
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
	
	public void openPopup() {
		super.openPopup();
	}
	
}
