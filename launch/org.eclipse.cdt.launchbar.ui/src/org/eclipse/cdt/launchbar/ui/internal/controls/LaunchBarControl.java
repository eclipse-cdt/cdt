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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.cdt.launchbar.ui.internal.Messages;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LaunchBarControl implements ILaunchBarManager.Listener {

	public static final String ID = "org.eclipse.cdt.launchbar"; //$NON-NLS-1$
	public static final String CLASS_URI = "bundleclass://" + Activator.PLUGIN_ID + "/" + LaunchBarControl.class.getName(); //$NON-NLS-1$ //$NON-NLS-2$

	@Inject
	private ILaunchBarManager manager;
	
	private ConfigSelector configSelector;
	private ModeSelector modeSelector;
	private TargetSelector targetSelector;
	
	@PostConstruct
	public void createControl(Composite parent) {
		manager.addListener(this);

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		GridLayout layout = new GridLayout(7, false);
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		container.setLayout(layout);
		container.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				LaunchBarControl.this.dispose();
			}
		});

		createButton(container, Activator.IMG_BUTTON_BUILD, Messages.LaunchBarControl_Build, Activator.CMD_BUILD);
		createButton(container, Activator.IMG_BUTTON_LAUNCH, Messages.LaunchBarControl_Launch, Activator.CMD_LAUNCH);
		createButton(container, Activator.IMG_BUTTON_STOP, Messages.LaunchBarControl_Stop, Activator.CMD_STOP);
		
		modeSelector = new ModeSelector(container, SWT.NONE);
		modeSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		modeSelector.setInput(manager);

		configSelector = new ConfigSelector(container, SWT.NONE);
		configSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		configSelector.setInput(manager);

		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		label.setText("on" + ":");
		
		targetSelector = new TargetSelector(container, SWT.NONE);
		targetSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		targetSelector.setInput(manager);

		ILaunchConfigurationDescriptor configDesc = manager.getActiveLaunchConfigurationDescriptor();
		configSelector.setSelection(configDesc == null ? null : configDesc);

		ILaunchMode mode = manager.getActiveLaunchMode();
		modeSelector.setSelection(mode == null ? null : mode);

		ILaunchTarget target = manager.getActiveLaunchTarget();
		targetSelector.setSelection(target == null ? null : target);
	}

	@PreDestroy
	public void dispose() {
		manager.removeListener(this);
	}

	private void createButton(Composite parent, String imageName, String toolTipText, final String command) {
		CButton button = new CButton(parent, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		Image srcImage = Activator.getDefault().getImage(imageName);
		Image image = new Image(parent.getDisplay(), srcImage, SWT.IMAGE_COPY);
		button.setHotImage(image);
		button.setToolTipText(toolTipText);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				Activator.runCommand(command);
			}
		});
	}

	@Override
	public void activeConfigurationDescriptorChanged() {
		if (configSelector != null && !configSelector.isDisposed()) {
			final ILaunchConfigurationDescriptor configDesc = manager.getActiveLaunchConfigurationDescriptor();
			configSelector.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!configSelector.isDisposed())
						configSelector.setSelection(configDesc == null ? null : configDesc);
				}
			});
		}
	}

	@Override
	public void activeLaunchModeChanged() {
		if (modeSelector != null && !modeSelector.isDisposed()) {
			final ILaunchMode mode = manager.getActiveLaunchMode();
			modeSelector.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!modeSelector.isDisposed())
						modeSelector.setSelection(mode == null ? null : mode);
				}
			});
		}
	}

	@Override
	public void activeLaunchTargetChanged() {
		if (targetSelector != null && !targetSelector.isDisposed()) {
			final ILaunchTarget target = manager.getActiveLaunchTarget();
			targetSelector.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!targetSelector.isDisposed())
						targetSelector.setSelection(target == null ? null : target);
				}
			});
		}
	}

}
