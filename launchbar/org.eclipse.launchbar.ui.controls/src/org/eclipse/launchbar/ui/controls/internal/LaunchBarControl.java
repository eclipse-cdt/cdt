/*******************************************************************************
 * Copyright (c) 2014, 2022 QNX Software Systems and others.
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
 *     Torkild U. Resheim - add preference to control target selector
 *     Vincent Guignot - Ingenico - add preference to control Build button
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.launchbar.core.ILaunchBarListener;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.ILaunchBarUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class LaunchBarControl implements ILaunchBarListener {
	public static final String ID = "org.eclipse.launchbar"; //$NON-NLS-1$
	public static final String CLASS_URI = "bundleclass://" + Activator.PLUGIN_ID + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ LaunchBarControl.class.getName();

	private ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);

	private Composite container;
	private ConfigSelector configSelector;
	private ModeSelector modeSelector;
	private Label onLabel;
	private TargetSelector targetSelector;

	private static final int SELECTION_DELAY = 200;

	@PostConstruct
	public void createControl(Composite parent) {
		manager.addListener(this);

		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		GridLayout layout = new GridLayout(5, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		container.setLayout(layout);
		container.addDisposeListener(e -> LaunchBarControl.this.dispose());

		Composite buttons = new Composite(container, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.spacing = 5;
		buttons.setLayout(fillLayout);

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean buildEnabled = store.getBoolean(Activator.PREF_ENABLE_BUILDBUTTON);
		if (buildEnabled) {
			Image imageBuild = Activator.getDefault().getImageRegistry().get(Activator.IMG_BUTTON_BUILD);
			createButton(buttons, imageBuild, Messages.LaunchBarControl_Build, ILaunchBarUIConstants.CMD_BUILD);
		}

		Image imageLaunch = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_LAUNCH_RUN);
		createButton(buttons, imageLaunch, Messages.LaunchBarControl_Launch, ILaunchBarUIConstants.CMD_LAUNCH);
		Image imageStop = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_LAUNCH_RUN_TERMINATED);
		createButton(buttons, imageStop, Messages.LaunchBarControl_Stop, ILaunchBarUIConstants.CMD_STOP);

		modeSelector = new ModeSelector(container, SWT.NONE);
		modeSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		modeSelector.setInput(manager);

		configSelector = new ConfigSelector(container, SWT.NONE);
		configSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		configSelector.setInput(manager);

		boolean supportsTargets;
		try {
			supportsTargets = supportsTargets(manager.getActiveLaunchDescriptor());
		} catch (CoreException e) {
			Activator.log(e);
			supportsTargets = true;
		}

		if (supportsTargets) {
			createTargetSelector();
		}

		syncSelectors();
	}

	private void createTargetSelector() {
		if (container.isDisposed()) {
			return;
		}

		onLabel = new Label(container, SWT.NONE);
		onLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		onLabel.setText(Messages.LaunchBarControl_0 + ":"); //$NON-NLS-1$

		targetSelector = new TargetSelector(container, SWT.NONE);
		targetSelector.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		targetSelector.setInput(manager);
	}

	protected void syncSelectors() {
		try {
			if (configSelector != null)
				configSelector.setSelection(manager.getActiveLaunchDescriptor());
			if (modeSelector != null)
				modeSelector.setSelection(manager.getActiveLaunchMode());
			if (targetSelector != null)
				targetSelector.setSelection(manager.getActiveLaunchTarget());
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	@PreDestroy
	public void dispose() {
		manager.removeListener(this);
	}

	private CLaunchButton createButton(Composite parent, Image image, String toolTipText, final String commandId) {
		CLaunchButton button = new CLaunchButton(parent, SWT.NONE);

		button.setImage(image);
		button.setToolTipText(toolTipText);
		button.setData("command", commandId); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = commandService.getCommand(commandId);
				final Event trigger = new Event();
				final IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, trigger);
				try {
					command.executeWithChecks(executionEvent);
				} catch (OperationCanceledException ex) {
					// abort
				} catch (Exception ex) {
					Activator.log(ex);
				}
			}
		});
		return button;
	}

	@Override
	public void activeLaunchDescriptorChanged(ILaunchDescriptor descriptor) {
		if (container == null || container.isDisposed()) {
			return;
		}

		container.getDisplay().asyncExec(() -> {
			if (configSelector != null) {
				configSelector.setDelayedSelection(descriptor, SELECTION_DELAY);
			}

			if (supportsTargets(descriptor)) {
				if (targetSelector == null || targetSelector.isDisposed()) {
					createTargetSelector();
					syncSelectors();
					if (!container.isDisposed()) {
						Composite parent = container.getParent();
						parent.layout(true);
					}
				}
			} else {
				if (targetSelector != null && !targetSelector.isDisposed()) {
					onLabel.dispose();
					targetSelector.dispose();
					if (!container.isDisposed()) {
						Composite parent = container.getParent();
						parent.layout(true);
					}
				}
			}
		});
	}

	private boolean supportsTargets(ILaunchDescriptor descriptor) {
		if (descriptor == null) {
			return true;
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store.getBoolean(Activator.PREF_ALWAYS_TARGETSELECTOR)) {
			return true;
		}

		try {
			return descriptor.getType().supportsTargets();
		} catch (CoreException e) {
			Activator.log(e);
			return true;
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
