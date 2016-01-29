/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *     Torkild U. Resheim - add preference to control target selector
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.controls;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class LaunchBarControl implements ILaunchBarListener {
	public static final String ID = "org.eclipse.launchbar"; //$NON-NLS-1$
	public static final String CLASS_URI = "bundleclass://" + Activator.PLUGIN_ID + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ LaunchBarControl.class.getName();

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
		GridLayout layout = new GridLayout(5, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		container.setLayout(layout);
		container.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				LaunchBarControl.this.dispose();
			}
		});

		ToolBar toolBar = new ToolBar(container, SWT.FLAT);
		createButton(toolBar, Activator.IMG_BUTTON_BUILD, Messages.LaunchBarControl_Build, Activator.CMD_BUILD);
		createButton(toolBar, Activator.IMG_BUTTON_LAUNCH, Messages.LaunchBarControl_Launch, Activator.CMD_LAUNCH);
		createButton(toolBar, Activator.IMG_BUTTON_STOP, Messages.LaunchBarControl_Stop, Activator.CMD_STOP);

		modeSelector = new ModeSelector(container, SWT.NONE);
		modeSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		modeSelector.setInput(manager);

		configSelector = new ConfigSelector(container, SWT.NONE);
		configSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		configSelector.setInput(manager);

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean enabled = store.getBoolean(Activator.PREF_ENABLE_TARGETSELECTOR);
		if (enabled) {
			Label label = new Label(container, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label.setText(Messages.LaunchBarControl_0 + ":"); //$NON-NLS-1$

			targetSelector = new TargetSelector(container, SWT.NONE);
			targetSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			targetSelector.setInput(manager);
		}

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

	private ToolItem createButton(Composite parent, String imageName, String toolTipText, final String command) {
		ToolItem button = new ToolItem((ToolBar) parent, SWT.FLAT);
		Image srcImage = Activator.getDefault().getImage(imageName);
		button.setImage(srcImage);
		button.setToolTipText(toolTipText);
		button.setData("command", command); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				Activator.runCommand(command);
			};
		});
		return button;
	}

	@Override
	public void activeLaunchDescriptorChanged(ILaunchDescriptor descriptor) {
		if (configSelector != null) {
			configSelector.setDelayedSelection(descriptor, SELECTION_DELAY);
		}
	}

	@Override
	public void activeLaunchModeChanged(ILaunchMode mode) {
		if (modeSelector != null) {
			modeSelector.setDelayedSelection(mode, SELECTION_DELAY);
		}
	}

	@Override
	public void activeLaunchTargetChanged(ILaunchTarget target) {
		if (targetSelector != null) {
			targetSelector.setDelayedSelection(target, SELECTION_DELAY);
		}
	}

	@Override
	public void launchTargetsChanged() {
		if (targetSelector != null) {
			targetSelector.refresh();
		}
	}

	public ConfigSelector getConfigSelector() {
		return configSelector;
	}
}
