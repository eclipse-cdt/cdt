/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.launch.ui;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.tm.internal.terminal.local.LocalTerminalMessages;
import org.eclipse.tm.internal.terminal.local.LocalTerminalUtilities;
import org.eclipse.tm.internal.terminal.local.launch.LocalTerminalLaunchUtilities;
import org.eclipse.tm.internal.terminal.local.process.LocalTerminalProcessRegistry;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * The class {@link LocalTerminalStillRunningDialog} is a dialog that is shown when the workbench is
 * about to exit and one or more terminal launches are still running. It gives the user a choice
 * between aborting the workbench shut-down, proceeding, or terminating the outstanding launches
 * individually. When no more launches are running the dialog will automatically disappear and
 * workbench shut-down will proceed.
 *
 * @author Mirko Raner
 * @version $Revision: 1.1 $
 */
public class LocalTerminalStillRunningDialog extends MessageDialog
implements Runnable, SelectionListener, ILaunchesListener2 {

	private final static String TITLE = LocalTerminalMessages.warningTitleTerminalsStillRunning;
	private final static String MESSAGE = LocalTerminalMessages.warningMessageTerminalsStillRunning;
	private final static String QUIT_ANYWAY = LocalTerminalMessages.quitWorkbenchAnyway;
	private final static String DO_NOT_QUIT = LocalTerminalMessages.doNoQuitWorkbench;
	private final static String[] BUTTONS = {QUIT_ANYWAY, DO_NOT_QUIT};
	private final static RGB WHITE = new RGB(255, 255, 255);
	private final static int SCROLLABLE_HEIGHT = 100;

	// Image key copied from IInternalDebugUIConstants:
	//
	private final static String IMG_LCL_TERMINATE = "IMG_LCL_TERMINATE"; //$NON-NLS-1$

	private ILaunch[] unterminated;
	private Composite content;

	private LocalTerminalStillRunningDialog(Shell parentShell, ILaunch[] launches) {

		super(parentShell, TITLE, null, MESSAGE, WARNING, BUTTONS, 0);
		setShellStyle(SWT.BORDER|SWT.TITLE|SWT.APPLICATION_MODAL|SWT.RESIZE|SWT.MAX);
		unterminated = launches;
	}

	/**
	 * Opens a dialog that lists all terminal launches that have not yet terminated.
	 *
	 * @param shell the parent {@link Shell} for the dialog
	 * @param launches the launches that have not yet terminated
	 * @return <code>true</code> to allow the workbench to proceed with shutdown, <code>false</code>
	 * to prevent a shutdown (only for non-forced shutdown)
	 */
	public static boolean openDialog(Shell shell, ILaunch[] launches) {

		LocalTerminalStillRunningDialog dialog;
		dialog = new LocalTerminalStillRunningDialog(shell, launches);
		dialog.setBlockOnOpen(true);
		try {

			LocalTerminalUtilities.LAUNCH_MANAGER.addLaunchListener(dialog);
			return dialog.open() == 0;
		}
		finally {

			LocalTerminalUtilities.LAUNCH_MANAGER.removeLaunchListener(dialog);
		}
	}

	/**
	 * Creates the dialog buttons and sets the focus on the default button. This is done because
	 * otherwise the focus might land on one of the stop buttons of the unterminated launches, which
	 * looks somewhat funny.
	 *
	 * @param parent the parent {@link Composite}
	 */
	protected void createButtonsForButtonBar(Composite parent) {

		super.createButtonsForButtonBar(parent);
		getButton(1).forceFocus();
	}

	/**
	 * Creates the custom area of the dialog that shows the list of terminal launches that have not
	 * yet been terminated.
	 *
	 * @param parent the parent {@link Composite} into which the custom area is inserted
	 * @return the {@link ScrolledComposite} for the custom area
	 *
	 * @see MessageDialog#createCustomArea(Composite)
	 */
	protected Control createCustomArea(Composite parent) {

		ScrolledComposite scrollable = new ScrolledComposite(parent, SWT.BORDER|SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = SCROLLABLE_HEIGHT;
		scrollable.setLayoutData(gridData);
		scrollable.setExpandHorizontal(true);
		scrollable.setExpandVertical(true);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = gridLayout.marginHeight = gridLayout.verticalSpacing = 0;
		content = new Composite(scrollable, SWT.NONE);
		content.setLayout(gridLayout);
		content.setBackground(new Color(parent.getDisplay(), WHITE));
		scrollable.setContent(content);
		for (int index = 0; index < unterminated.length; index++) {

			Composite item = createItem(content, unterminated[index]);
			item.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		content.pack();
		scrollable.setMinHeight(content.getBounds().height);
		return scrollable;
	}

	/**
	 * Handles the {@link SelectionEvent}s that are sent when the user clicks the stop button of a
	 * launch. The stop button will immediately be disabled to indicate that the stop request is
	 * being processed. The actual launch termination will be confirmed in an asynchronous fashion
	 * by the {@link #launchesTerminated(ILaunch[])} method.
	 *
	 * @param event the {@link SelectionEvent}
	 *
	 * @see #launchesTerminated(ILaunch[])
	 */
	public void widgetSelected(SelectionEvent event) {

		ToolItem item = (ToolItem)event.widget;
		ILaunch launch = (ILaunch)item.getParent().getParent().getData();
		item.setEnabled(false);
		try {

			LocalTerminalProcessRegistry.addProcessBackToFinishedLaunch(launch);
			launch.terminate();
		}
		catch (DebugException exception) {

			Logger.logException(exception);
		}
	}

	/**
	 * Handles default selection events by passing them to {@link #widgetSelected(SelectionEvent)}.
	 *
	 * @param event the {@link SelectionEvent}
	 *
	 * @see #widgetSelected(SelectionEvent)
	 * @see SelectionListener#widgetSelected(SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent event) {

		widgetSelected(event);
	}

	/**
	 * Removes terminated launches from the list displayed by the dialog and closes the dialog once
	 * all outstanding launches have been terminated.
	 *
	 * @see #launchesTerminated(ILaunch[])
	 */
	public void run() {

		boolean allLaunchesTerminated = true;
		Control[] child = content.getChildren();
		int numberOfChildren = child.length;
		for (int number = 0; number < numberOfChildren; number++) {

			ILaunch launch = (ILaunch)child[number].getData();
			if (launch != null && launch.isTerminated()) {

				child[number].setData(null);
				String exitValue;
				try {

					exitValue = String.valueOf(launch.getProcesses()[0].getExitValue());
				}
				catch (DebugException couldNotGetExitValue) {

					exitValue = '(' + couldNotGetExitValue.getMessage() + ')';
				}
				Label label = (Label)((Composite)child[number]).getChildren()[1];
				String process = label.getText();
				process = NLS.bind(LocalTerminalMessages.terminatedProcess, process, exitValue);
				label.setText(process);

				// In case the launch terminated by itself (and not because the user pressed the
				// stop button) disable the stop button so that no attempt can be made to stop the
				// process twice:
				//
				((Composite)child[number]).getChildren()[2].setEnabled(false);
			}
			if (child[number].getData() != null) {

				allLaunchesTerminated = false;
			}
		}
		if (allLaunchesTerminated) {

			setReturnCode(0);
			close();
		}
	}

	/**
	 * Removes a recently terminated launch from the list displayed by the dialog. The actual work
	 * needs to be done in the UI thread and is performed by the {@link #run()} method.
	 *
	 * @param terminated a list of terminated launches
	 *
	 * @see #run()
	 */
	public void launchesTerminated(ILaunch[] terminated) {

		Display.getDefault().syncExec(this);
	}

	/**
	 * <i>Not implemented</i>.
	 * @see ILaunchesListener2#launchesAdded(ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {

		// Not implemented...
	}

	/**
	 * <i>Not implemented</i>.
	 * @see ILaunchesListener2#launchesChanged(ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {

		// Not implemented...
	}

	/**
	 * <i>Not implemented</i>.
	 * @see ILaunchesListener2#launchesRemoved(ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {

		// Not implemented...
	}

	//-------------------------------------- PRIVATE SECTION -------------------------------------//

	private Composite createItem(Composite parent, ILaunch launch) {

		Composite item = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout(3, false);
		item.setData(launch);
		item.setLayout(gridLayout);
		Image processImage = LocalTerminalLaunchUtilities.getImage(launch.getLaunchConfiguration());
		Label icon = new Label(item, SWT.NULL);
		icon.setImage(processImage);
		Label label = new Label(item, SWT.NULL);
		label.setText(launch.getLaunchConfiguration().getName());
		ToolItem stopButton = new ToolItem(new ToolBar(item, SWT.FLAT), SWT.PUSH);
		stopButton.addSelectionListener(this);
		Image deleteImage = DebugUITools.getImage(IMG_LCL_TERMINATE);
		stopButton.setImage(deleteImage);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		return item;
	}
}
