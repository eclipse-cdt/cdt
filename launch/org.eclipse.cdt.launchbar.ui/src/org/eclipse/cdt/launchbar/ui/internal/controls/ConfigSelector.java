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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.internal.LaunchBarManager;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.cdt.launchbar.ui.internal.DefaultDescriptorLabelProvider;
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

	private LaunchBarUIManager uiManager = Activator.getDefault().getLaunchBarUIManager();
	private DefaultDescriptorLabelProvider defaultProvider;
	
	private static final String[] noConfigs = new String[] { "No Launch Configurations" };
	private static final int SEPARATOR_INDEX = 3;
	
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
				if (descs.length > 0) {
					if (descs.length > SEPARATOR_INDEX + 1) {
						ILaunchDescriptor[] descsCopy = new ILaunchDescriptor[SEPARATOR_INDEX + descs.length];
						System.arraycopy(descs, 0, descsCopy, 0, SEPARATOR_INDEX); // copy first 3 elements
						System.arraycopy(descs, 0, descsCopy, SEPARATOR_INDEX, descs.length); // copy all into rest
						// sort rest
						Arrays.sort(descsCopy, SEPARATOR_INDEX, descsCopy.length, new Comparator<ILaunchDescriptor>() {
							@Override
							public int compare(ILaunchDescriptor o1, ILaunchDescriptor o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});
						return descsCopy;
					} else
						return descs;
				}
				return noConfigs;
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
		// no sorter on view, data is sorted by provider
		setSorter(null);
		setSeparatorIndex(SEPARATOR_INDEX);
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
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			LaunchBarManager manager = uiManager.getManager();
			ILaunchDescriptor desc = (ILaunchDescriptor) element;
			ILaunchMode mode = manager.getActiveLaunchMode();
			ILaunchTarget target = manager.getActiveLaunchTarget();
			if (target == null) {
				MessageDialog.openError(shell, "No Active Target", "You must create a target to edit this launch configuration.");
				return;
			}
			ILaunchConfigurationType configType = manager.getLaunchConfigurationType(desc, target);
			if (configType == null) {
				MessageDialog.openError(shell, "No launch configuration type", "Cannot edit this configuration");
				return;
			}
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(configType, mode.getIdentifier());
			LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(group.getIdentifier());
			if (groupExt != null) {
				ILaunchConfiguration config = manager.getLaunchConfiguration(desc, target);
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
								uiManager.getManager().setActiveLaunchMode(lm);
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

	@Override
	public void setSelection(Object element) {
		if (element == null)
			element = noConfigs[0];
		super.setSelection(element);
	}
	
}
