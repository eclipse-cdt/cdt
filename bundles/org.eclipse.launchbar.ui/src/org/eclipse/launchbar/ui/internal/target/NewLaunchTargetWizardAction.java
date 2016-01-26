/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.target;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Invoke the resource creation wizard selection Wizard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * This method automatically registers listeners so that it can keep its
 * enablement state up to date. Ordinarily, the window's references to these
 * listeners will be dropped automatically when the window closes. However,
 * if the client needs to get rid of an action while the window is still open,
 * the client must call #dispose() to give the
 * action an opportunity to deregister its listeners and to perform any other
 * cleanup.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NewLaunchTargetWizardAction extends Action implements
		ActionFactory.IWorkbenchAction {
	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;
	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;
	/**
	 * The title of the wizard window or <code>null</code> to use the default
	 * wizard window title.
	 */
	private String windowTitle = null;
	/**
	 * The workbench window; or <code>null</code> if this
	 * action has been <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;

	/**
	 * Create a new instance of this class.
	 */
	public NewLaunchTargetWizardAction() {
		super(WorkbenchMessages.NewWizardAction_text);
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		// @issues should be IDE-specific images
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(images
				.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		setDisabledImageDescriptor(images
				.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
		setToolTipText(WorkbenchMessages.NewWizardAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.NEW_ACTION);
	}

	/**
	 * <p>
	 * Sets the title of the wizard window
	 * <p>
	 *
	 * <p>
	 * If the title of the wizard window is <code>null</code>, the default
	 * wizard window title will be used.
	 * </p>
	 *
	 * @param windowTitle
	 *            The title of the wizard window, otherwise <code>null</code>
	 *            (default wizard window title).
	 *
	 * @since 3.6
	 */
	public void setWizardWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		NewLaunchTargetWizard wizard = new NewLaunchTargetWizard();
		wizard.setWindowTitle(windowTitle);
		wizard.init(workbenchWindow.getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		String settingsSection = getClass().getSimpleName();
		IDialogSettings wizardSettings = workbenchSettings.getSection(settingsSection);
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings.addNewSection(settingsSection);
		}
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);
		Shell parent = workbenchWindow.getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		dialog.getShell().setSize(
				Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
				SIZING_WIZARD_HEIGHT);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.NEW_WIZARD);
		dialog.open();
	}

	@Override
	public void dispose() {
		if (workbenchWindow == null) {
			// action has already been disposed
			return;
		}
		workbenchWindow = null;
	}
}
