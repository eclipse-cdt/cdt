/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.launch.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.tm.internal.terminal.local.ILocalTerminalSettings;
import org.eclipse.tm.internal.terminal.local.LocalTerminalMessages;
import org.eclipse.tm.internal.terminal.local.launch.LocalTerminalLaunchDelegate;
import org.eclipse.tm.internal.terminal.local.launch.LocalTerminalLaunchUtilities;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * The class {@link LocalTerminalSettingsTab} provides the UI for custom settings that are specific
 * to terminal-based launches. Currently, the tab allows the user to control the local echo settings
 * and the line separator string.
 *
 * @author Mirko Raner
 * @version $Revision: 1.2 $
 **/
public class LocalTerminalSettingsTab extends AbstractLaunchConfigurationTab
implements SelectionListener {

	private final static String NULL = null;

	private Button buttonEcho;
	private Button buttonCtrlC;
	private Button separatorDefault;
	private Button separatorLF;
	private Button separatorCRLF;
	private Button separatorCR;

	/**
	 * Creates a new {@link LocalTerminalSettingsTab}.
	 **/
	public LocalTerminalSettingsTab() {

		super();
	}

	/**
	 * Creates the top-level control for this launch configuration tab under the given parent
	 * composite. This method is called once on tab creation.
	 *
	 * @param parent the parent {@link Composite}
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 **/
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		container.setLayout(new GridLayout());
		Group composite = new Group(container, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setText(LocalTerminalMessages.terminalSettings);
		composite.setLayout(new GridLayout());

		// Create echo check box:
		//
		buttonEcho = button(composite, LocalTerminalMessages.enableLocalEcho, SWT.CHECK);
		buttonEcho.setLayoutData(new GridData());

		// Create Ctrl-C/SIGINT check box:
		//
		buttonCtrlC = button(composite, LocalTerminalMessages.sendInterruptOnCtrlC, SWT.CHECK);
		buttonCtrlC.setLayoutData(new GridData());

		// Create radio buttons for line separator settings:
		//
		Composite separator = new Composite(composite, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		separator.setLayout(rowLayout);
		separatorDefault = button(separator, LocalTerminalMessages.lineSeparatorDefault, SWT.RADIO);
		separatorLF = button(separator, LocalTerminalMessages.lineSeparatorLF, SWT.RADIO);
		separatorCRLF = button(separator, LocalTerminalMessages.lineSeparatorCRLF, SWT.RADIO);
		separatorCR = button(separator, LocalTerminalMessages.lineSeparatorCR, SWT.RADIO);
		separator.setLayoutData(new GridData());
	}

	/**
	 * Returns the name of this tab.
	 *
	 * @return the name of this tab
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 **/
	public String getName() {

		return LocalTerminalMessages.terminalTabName;
	}

	/**
	 * Returns the image for this tab, or <code>null</code> if none
	 *
	 * @return the image for this tab, or <code>null</code> if none
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 **/
	public Image getImage() {

		return DebugUITools.getImage(LocalTerminalLaunchDelegate.LAUNCH_CONFIGURATION_TYPE_ID);
	}

	/**
	 * Initializes this tab's controls with values from the given launch configuration. This method
	 * is called when a configuration is selected to view or edit, after the tab's control has been
	 * created.
	 *
	 * @param configuration the launch configuration
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 **/
	public void initializeFrom(ILaunchConfiguration configuration) {

		boolean echo;
		try {

			echo = configuration.getAttribute(LocalTerminalLaunchUtilities.ATTR_LOCAL_ECHO, false);
		}
		catch (CoreException exception) {

			Logger.logException(exception);
			echo = false;
		}
		boolean ctrlC;
		try {

			ctrlC = configuration.getAttribute(LocalTerminalLaunchUtilities.ATTR_CTRL_C, false);
		}
		catch (CoreException exception) {

			Logger.logException(exception);
			ctrlC = false;
		}
		String ls;
		try {

			ls = configuration.getAttribute(LocalTerminalLaunchUtilities.ATTR_LINE_SEPARATOR, NULL);
		}
		catch (CoreException exception) {

			Logger.logException(exception);
			ls = null;
		}
		buttonEcho.setSelection(echo);
		buttonCtrlC.setSelection(ctrlC);
		if (ILocalTerminalSettings.LINE_SEPARATOR_LF.equals(ls)) {

			separatorLF.setSelection(true);
		}
		else if (ILocalTerminalSettings.LINE_SEPARATOR_LF.equals(ls)) {

			separatorLF.setSelection(true);
		}
		else if (ILocalTerminalSettings.LINE_SEPARATOR_CRLF.equals(ls)) {

			separatorCRLF.setSelection(true);
		}
		else if (ILocalTerminalSettings.LINE_SEPARATOR_CR.equals(ls)) {

			separatorCR.setSelection(true);
		}
		else  {

			separatorDefault.setSelection(true);
		}
	}

	/**
	 * Copies values from this tab into the given launch configuration.
	 * 
	 * @param configuration the launch configuration
	 * @see AbstractLaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 **/
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		boolean echo = buttonEcho.getSelection();
		configuration.setAttribute(LocalTerminalLaunchUtilities.ATTR_LOCAL_ECHO, echo);
		boolean ctrlC = buttonCtrlC.getSelection();
		configuration.setAttribute(LocalTerminalLaunchUtilities.ATTR_CTRL_C, ctrlC);
		String lineSeparator = null;
		if (separatorCRLF.getSelection()) {

			lineSeparator = ILocalTerminalSettings.LINE_SEPARATOR_CRLF;
		}
		else if (separatorCR.getSelection()) {

			lineSeparator = ILocalTerminalSettings.LINE_SEPARATOR_CR;
		}
		else if (separatorLF.getSelection()) {

			lineSeparator = ILocalTerminalSettings.LINE_SEPARATOR_LF;
		}
		configuration.setAttribute(LocalTerminalLaunchUtilities.ATTR_LINE_SEPARATOR, lineSeparator);
	}

	/**
	 * Initializes the given launch configuration with default values for this tab. This method is
	 * called when a new launch configuration is created such that the configuration can be
	 * initialized with meaningful values. This method may be called before the tab's control is
	 * created.
	 * 
	 * @param configuration the launch configuration
	 * @see AbstractLaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 **/
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		configuration.setAttribute(LocalTerminalLaunchUtilities.ATTR_LOCAL_ECHO, false);
		configuration.setAttribute(LocalTerminalLaunchUtilities.ATTR_CTRL_C, false);
		configuration.setAttribute(LocalTerminalLaunchUtilities.ATTR_LINE_SEPARATOR, NULL);
	}

	/**
	 * Prevents Terminal launch configurations from being started directly from the launch
	 * configuration dialog. The <b>Run</b> button in the dialog will only be enabled if all tabs
	 * consider a launch configuration valid.
	 *
	 * TODO: previously used launches can still be launched via the launch history
	 *       (see {@code ExternalToolMenuDelegate#fillMenu(Menu)})
	 *
	 * @param configuration the {@link ILaunchConfiguration}
	 * @return always {@code false}
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {

		return false;
	}

	/**
	 * Handles selection of any of the buttons in the tab.
	 *
	 * @param event the {@link SelectionEvent}
	 * @see SelectionListener#widgetSelected(SelectionEvent)
	 **/
	public void widgetSelected(SelectionEvent event) {

		setDirty(true);
		getLaunchConfigurationDialog().updateButtons();
	}

	/**
	 * Handles default selection of any of the buttons in the tab.
	 *
	 * @param event the {@link SelectionEvent}
	 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
	 **/
	public void widgetDefaultSelected(SelectionEvent event) {

		widgetSelected(event);
	}

	//-------------------------------------- PRIVATE SECTION -------------------------------------//

	private Button button(Composite parent, String label, int buttonType) {

		Button button = new Button(parent, buttonType);
		button.addSelectionListener(this);
		button.setText(label);
		return button;
	}
}
