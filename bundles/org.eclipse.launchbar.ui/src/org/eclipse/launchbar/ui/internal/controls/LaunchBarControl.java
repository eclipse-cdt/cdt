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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.core.internal.LaunchBarManager.Listener;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LaunchBarControl implements Listener {
	public static final String ID = "org.eclipse.launchbar"; //$NON-NLS-1$
	public static final String CLASS_URI = "bundleclass://" + Activator.PLUGIN_ID + "/" + LaunchBarControl.class.getName(); //$NON-NLS-1$ //$NON-NLS-2$

	private LaunchBarManager manager = Activator.getDefault().getLaunchBarUIManager().getManager();

	private ConfigSelector configSelector;
	private ModeSelector modeSelector;
	private TargetSelector targetSelector;

	private static final int SELECTION_DELAY = 200;

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
		label.setText(Messages.LaunchBarControl_0 + ":"); //$NON-NLS-1$

		targetSelector = new TargetSelector(container, SWT.NONE);
		targetSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		targetSelector.setInput(manager);

		syncSelectors();
	}

	protected void syncSelectors() {
		if (configSelector != null)
			configSelector.setSelection(manager.getActiveLaunchDescriptor());
		if (modeSelector != null)
			modeSelector.setSelection(manager.getActiveLaunchMode());
		if (targetSelector != null)
			targetSelector.setSelection(manager.getActiveLaunchTarget());
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
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				Activator.runCommand(command);
			};
		});
	}

	@Override
	public void activeLaunchDescriptorChanged() {
		if (configSelector != null) {
			final ILaunchDescriptor descriptor = manager.getActiveLaunchDescriptor();
			configSelector.setDelayedSelection(descriptor, SELECTION_DELAY);
		}
	}

	@Override
	public void activeLaunchModeChanged() {
		if (modeSelector != null) {
			final ILaunchMode mode = manager.getActiveLaunchMode();
			modeSelector.setDelayedSelection(mode, SELECTION_DELAY);
		}
	}

	@Override
	public void activeLaunchTargetChanged() {
		if (targetSelector != null) {
			final IRemoteConnection target = manager.getActiveLaunchTarget();
			targetSelector.setDelayedSelection(target, SELECTION_DELAY);
		}
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void launchTargetsChanged() {
		// TODO Auto-generated method stub

	}

	public ConfigSelector getConfigSelector() {
		return configSelector;
	}
}
