/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * The view controller for the working set configurations pane in the dialog. It takes care of coordinating
 * the user gestures in the pane with the working set configuration model, and vice-versa. It also implements
 * the handling of the action buttons.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 * 
 */
class WorkingSetConfigsController implements SelectionListener, ISelectionChangedListener {

	private TreeViewer tree;
	private Button addButton;
	private Button removeButton;
	private Button renameButton;
	private Button activateButton;
	private Button buildButton;

	private IWorkingSetProxy.ISnapshot initialWorkingSet;
	private IWorkingSetProxy.ISnapshot currentWorkingSet;
	private IWorkingSetConfiguration.ISnapshot currentConfig;

	private final WorkspaceSnapshot workspace;

	private ProjectConfigsController projectsController;

	/**
	 * Initializes me we an initial working set to select.
	 */
	WorkingSetConfigsController(WorkspaceSnapshot workspace, IWorkingSetProxy.ISnapshot initialWorkingSet) {
		this.workspace = workspace;

		if (initialWorkingSet != null) {
			this.initialWorkingSet = initialWorkingSet;
		} else {
			IWorkingSet[] recent = WorkingSetConfigurationManager.WS_MGR.getRecentWorkingSets();
			if ((recent != null) && (recent.length > 0)) {
				this.initialWorkingSet = workspace.getWorkingSet(recent[0].getName());
			}
		}
	}

	/**
	 * Initializes me.
	 */
	WorkingSetConfigsController(WorkspaceSnapshot workspace) {
		this(workspace, null);
	}

	/**
	 * Assigns the tree viewer that I control.
	 * 
	 * @param tree
	 *            my tree viewer
	 */
	void setTreeViewer(final TreeViewer tree) {
		if (this.tree != null) {
			this.tree.removeSelectionChangedListener(this);
		}

		this.tree = tree;

		if (this.tree != null) {
			this.tree.addSelectionChangedListener(this);

			this.tree.setInput(workspace.getWorkingSets());

			if (!workspace.getWorkingSets().isEmpty()) {
				tree.getTree().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						Object initialSelection;

						ITreeContentProvider content = (ITreeContentProvider) tree.getContentProvider();

						if ((initialWorkingSet != null)
								&& Arrays.asList(content.getElements(tree.getInput())).contains(
										initialWorkingSet)) {
							initialSelection = initialWorkingSet;
						} else {
							// we have a most-recently-used working set. Just
							// take the first in tree order
							initialSelection = tree.getTree().getItem(0).getData();
						}

						Object[] children = content.getChildren(initialSelection);
						IStructuredSelection sel;

						if ((children == null) || (children.length == 0)) {
							// Shouldn't happen: there should at least be the
							// read-only config.
							// Can only select the initial working set
							sel = new StructuredSelection(initialSelection);
						} else {
							Object[] toSort = new Object[children.length];
							System.arraycopy(children, 0, toSort, 0, children.length);
							tree.getComparator().sort(tree, toSort);
							sel = new StructuredSelection(toSort[0]);
						}

						// make the selection
						tree.setSelection(sel, true);
					}
				});
			}
		}
	}

	private Button updateButton(Button oldButton, Button newButton) {
		if (oldButton != null) {
			oldButton.removeSelectionListener(this);
		}

		if (newButton != null) {
			newButton.addSelectionListener(this);
		}

		return newButton;
	}

	/**
	 * Assigns me my "Add..." button.
	 * 
	 * @param addButton
	 *            my add button
	 */
	void setAddButton(Button addButton) {
		this.addButton = updateButton(this.addButton, addButton);
	}

	/**
	 * Assigns me my "Remove" button.
	 * 
	 * @param removeButton
	 *            my remove button
	 */
	void setRemoveButton(Button removeButton) {
		this.removeButton = updateButton(this.removeButton, removeButton);
	}

	/**
	 * Assigns me my "Rename..." button.
	 * 
	 * @param renameButton
	 *            my rename button
	 */
	void setRenameButton(Button renameButton) {
		this.renameButton = updateButton(this.renameButton, renameButton);
	}

	/**
	 * Assigns me my "Activate" button.
	 * 
	 * @param activateButton
	 *            my activate button
	 */
	void setActivateButton(Button activateButton) {
		this.activateButton = updateButton(this.activateButton, activateButton);
	}

	/**
	 * Assigns me my "Build" button.
	 * 
	 * @param buildButton
	 *            my build button
	 */
	void setBuildButton(Button buildButton) {
		this.buildButton = updateButton(this.buildButton, buildButton);
	}

	/**
	 * Connects me to the controller for the project configurations pane, into which I inject the currently
	 * selected working set configuration.
	 * 
	 * @param controller
	 *            my project configurations controller
	 */
	void setProjectConfigsController(ProjectConfigsController controller) {
		this.projectsController = controller;

		if (controller != null) {
			controller.setWorkingSetConfiguration(currentConfig);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// not interesting
	}

	/**
	 * Handles button presses in the working set configurations pane.
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		// handle button press
		if (e.widget == addButton) {
			addConfig();
		} else if (e.widget == removeButton) {
			removeConfig();
		} else if (e.widget == renameButton) {
			renameConfig();
		} else if (e.widget == activateButton) {
			activateConfig();
		} else if (e.widget == buildButton) {
			buildConfig();
		}
	}

	/**
	 * Handles selection of working sets and their configurations. Among potentially other actions, this
	 * injects the working-set configuration selection into the project configurations controller and updates
	 * the enablement of the buttons.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		currentConfig = null;
		currentWorkingSet = null;

		if (event.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();

			if (!sel.isEmpty()) {
				Object first = sel.getFirstElement();

				if (first instanceof IWorkingSetConfiguration) {
					currentConfig = (IWorkingSetConfiguration.ISnapshot) first;
					currentWorkingSet = currentConfig.getWorkingSet();
				} else if (first instanceof IWorkingSetProxy) {
					currentWorkingSet = (IWorkingSetProxy.ISnapshot) first;
				}
			}
		}

		if (projectsController != null) {
			// tell the project controller
			projectsController.setWorkingSetConfiguration(currentConfig);
		}

		updateButtons();
	}

	/**
	 * Handler for the "Add..." button.
	 */
	private void addConfig() {
		InputDialog dlg = new InputDialog(tree.getTree().getShell(),
				WorkingSetMessages.WSConfigsController_addDlg_title,
				WorkingSetMessages.WSConfigsController_addDlg_msg,
				WorkingSetMessages.WSConfigsController_addDlg_defaultName, new IInputValidator() {

					@Override
					public String isValid(String newText) {
						if (currentWorkingSet.getConfiguration(newText) != null) {
							return WorkingSetMessages.WSConfigsController_addDlg_nameExists;
						}
						if (newText.length() == 0) {
							return WorkingSetMessages.WSConfigsController_addDlg_emptyName;
						}
						return null;
					}
				});

		if (dlg.open() == IDialogConstants.OK_ID) {
			IWorkingSetConfiguration.ISnapshot newConfig = currentWorkingSet.createConfiguration(dlg
					.getValue());
			tree.refresh(currentWorkingSet);
			tree.setSelection(new StructuredSelection(newConfig), true);
			currentConfig = newConfig;
			currentWorkingSet = currentConfig.getWorkingSet();

			// this is a "recently used" working set
			IWorkingSet ws = currentWorkingSet.resolve();
			if (ws != null) {
				WorkingSetConfigurationManager.WS_MGR.addRecentWorkingSet(ws);
			}
		}
	}

	/**
	 * Handler for the "Remove" button.
	 */
	private void removeConfig() {
		currentWorkingSet.removeConfiguration(currentConfig);
		tree.refresh(currentWorkingSet);
	}

	/**
	 * Handler for the "Rename..." button.
	 */
	private void renameConfig() {
		InputDialog dlg = new InputDialog(tree.getTree().getShell(),
				WorkingSetMessages.WSConfigsController_renameDlg_title,
				WorkingSetMessages.WSConfigsController_renameDlg_msg, currentConfig.getName(),
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						if (newText.equals(currentConfig.getName())) {
							return ""; //$NON-NLS-1$
						}
						if (currentWorkingSet.getConfiguration(newText) != null) {
							return WorkingSetMessages.WSConfigsController_addDlg_nameExists;
						}
						if (newText.length() == 0) {
							return WorkingSetMessages.WSConfigsController_addDlg_emptyName;
						}
						return null;
					}
				});

		if (dlg.open() == IDialogConstants.OK_ID) {
			currentConfig.setName(dlg.getValue());
			tree.refresh(currentWorkingSet);
		}
	}

	/**
	 * Handler for the "Activate" button.
	 */
	private void activateConfig() {
		currentConfig.activate();
		projectsController.update();
		updateForActivation();
	}

	/**
	 * Updates the display to reflect potential changes in project activation and the resulting changes in
	 * working-set config activation, if any.
	 */
	private void updateForActivation() {
		// update all working-set configs that intersect this config
		Collection<IWorkingSetProxy.ISnapshot> unaffectedWorkingSets = new java.util.HashSet<IWorkingSetProxy.ISnapshot>(
				workspace.getWorkingSets());

		for (IProject project : currentConfig.getWorkingSet().resolveProjects()) {
			for (Iterator<IWorkingSetProxy.ISnapshot> iter = unaffectedWorkingSets.iterator(); iter.hasNext();) {
				IWorkingSetProxy.ISnapshot next = iter.next();

				if (next.resolveProjects().contains(project)) {
					iter.remove();

					if (next.updateActiveConfigurations()) {
						// major change. Refresh it altogether
						tree.refresh(next);
					} else {
						// lighter-weight updates of its configs
						for (IWorkingSetConfiguration config : next.getConfigurations()) {
							tree.update(config, null);
						}
					}
				}
			}
		}

		updateButtons();
	}

	/**
	 * Handler for the "Build" button.
	 */
	private void buildConfig() {
		final IStatus[] problem = new IStatus[1];

		try {
			ProgressMonitorDialog dlg = new ProgressMonitorDialog(tree.getControl().getShell());
			dlg.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) {
					IStatus status = currentConfig.build(monitor);
					if (status.matches(IStatus.WARNING | IStatus.ERROR)) {
						problem[0] = status;
					}
				}
			});
		} catch (Exception e) {
			CUIPlugin.log(WorkingSetMessages.WSConfigsController_buildFailedLog, e);
		}

		// it is possible that some project configurations had to applied in
		// order to effect a build. Refresh to handle that case
		updateForActivation();

		if (problem[0] != null) {
			// show the problem
			ErrorDialog.openError(tree.getControl().getShell(),
					WorkingSetMessages.WSConfigsController_buildFailedDlgTitle,
					WorkingSetMessages.WSConfigsController_buildFailedDlgMsg, problem[0]);
		}
	}

	/**
	 * Updates the enablement state of the action buttons according to the current selection.
	 */
	private void updateButtons() {
		if (addButton != null) {
			addButton.setEnabled(currentWorkingSet != null);
		}
		if (removeButton != null) {
			removeButton.setEnabled((currentConfig != null) && !currentConfig.isReadOnly());
		}
		if (renameButton != null) {
			renameButton.setEnabled((currentConfig != null) && !currentConfig.isReadOnly());
		}
		if (activateButton != null) {
			activateButton.setEnabled((currentConfig != null) && !currentConfig.isActive());
		}
		if (buildButton != null) {
			buildButton.setEnabled(currentConfig != null);
		}
	}

	/**
	 * Notification that the selection of configuration(s) in some project in the current working set
	 * configuration has changed. I accordingly update the visuals of the working-set configuration to
	 * indicate whether it is active or not.
	 * 
	 * @param project
	 *            the project configuration whose active configuration selections have changed
	 */
	void projectSelectionsChanged(IWorkingSetProjectConfiguration project) {
		tree.update(currentConfig, null);
		updateButtons(); // depends on whether the ws config is active
	}
}