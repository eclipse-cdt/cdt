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
package org.eclipse.cdt.launchbar.ui.internal.controls;

import java.util.Comparator;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.cdt.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.cdt.launchbar.ui.internal.dialogs.LaunchConfigurationEditDialog;
import org.eclipse.cdt.launchbar.ui.internal.dialogs.NewLaunchConfigWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class ConfigSelector extends CSelector {

	private LaunchBarUIManager uiManager;
	
	private static final String[] noConfigs = new String[] { "No Launch Configurations" };
	
	public ConfigSelector(Composite parent, int style) {
		super(parent, style);

		setToolTipText("Launch configuration");

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
					ILaunchDescriptor[] descs = getManager().getLaunchDescriptors();
					if (descs.length > 0)
						return descs;
				} catch (CoreException e) {
					Activator.log(e.getStatus());
				}
				return noConfigs; 
			}
		});

		setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof ILaunchDescriptor) {
					ILaunchDescriptor configDesc = (ILaunchDescriptor)element;
					ILabelProvider labelProvider = uiManager.getLabelProvider(configDesc);
					if (labelProvider != null) {
						return labelProvider.getImage(element);
					}
				}
				return super.getImage(element);
			}
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return (String)element;
				} else if (element instanceof ILaunchDescriptor) {
					ILaunchDescriptor configDesc = (ILaunchDescriptor)element;
					ILabelProvider labelProvider = uiManager.getLabelProvider(configDesc);
					if (labelProvider != null) {
						return labelProvider.getText(element);
					}
					// Default
					return configDesc.getName();
				}
				return super.getText(element);
			}
		});

		setSorter(new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				String text1 = getLabelProvider().getText(o1);
				String text2 = getLabelProvider().getText(o2);
				return text1.compareTo(text2);
			}
		});
	}

	@Override
	protected void fireSelectionChanged() {
		Object selected = getSelection();
		if (selected instanceof ILaunchDescriptor) {
			ILaunchDescriptor configDesc = (ILaunchDescriptor) selected;
			try {
				getManager().setActiveLaunchDescriptor(configDesc);
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
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			ILaunchDescriptor desc = (ILaunchDescriptor) element;
			ILaunchMode mode = getManager().getActiveLaunchMode();
			ILaunchTarget target = getManager().getActiveLaunchTarget();
			if (target == null) {
				MessageDialog.openError(shell, "No Active Target", "You must create a target to edit this launch configuration.");
				return;
			}
			ILaunchConfigurationType configType = getManager().getLaunchConfigurationType(desc, target);
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(configType, mode.getIdentifier());
			LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(group.getIdentifier());
			if (groupExt != null) {
				ILaunchConfiguration config = getManager().getLaunchConfiguration(desc, target);
				if (config == null) {
					MessageDialog.openError(shell, "No launch configuration", "Cannot edit this configuration");
					return;
				}
				if (config.isWorkingCopy() && ((ILaunchConfigurationWorkingCopy) config).isDirty()) {
					config = ((ILaunchConfigurationWorkingCopy) config).doSave();
				}
				final LaunchConfigurationEditDialog dialog = new LaunchConfigurationEditDialog(shell, config, groupExt);
				dialog.setInitialStatus(Status.OK_STATUS);
				dialog.open();
			}
		} catch (CoreException e2) {
			Activator.log(e2);
		}
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
		createButton.setBackground(white);
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
		createLabel.setBackground(white);

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				final NewLaunchConfigWizard wizard = new NewLaunchConfigWizard();
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					new Job("Create Launch Configuration") {
						protected IStatus run(IProgressMonitor monitor) {
							try {
								wizard.getWorkingCopy().doSave();
								ILaunchMode lm = wizard.getLaunchMode();
								getManager().setActiveLaunchMode(lm);
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
				createButton.setBackground(white);
				createLabel.setBackground(white);
			}
		};
		createButton.addMouseTrackListener(mouseTrackListener);
		createLabel.addMouseTrackListener(mouseTrackListener);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(250, hHint, changed);
	}

	private ILaunchBarManager getManager() {
		return (ILaunchBarManager) getInput();
	}
	
	@Override
	public void setInput(Object input) {
		super.setInput(input);
		uiManager = (LaunchBarUIManager) ((ILaunchBarManager) input).getAdapter(LaunchBarUIManager.class);
	}

	@Override
	public void setSelection(Object element) {
		if (element == null)
			element = noConfigs[0];
		super.setSelection(element);
	}
	
}
