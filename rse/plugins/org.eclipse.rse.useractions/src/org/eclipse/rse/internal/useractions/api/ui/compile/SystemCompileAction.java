/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * Kevin Doyle		(IBM)		 - [252707] Everytime a Compile command is selected a saving profile job is performed
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.api.ui.compile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsImageIds;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompilableSource;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCommand;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileType;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * This is the action for an individual compile command, either prompted or not
 * prompted. The label for the action is simply the compile command's label. If
 * promptable, then "..." is appended.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public class SystemCompileAction extends SystemBaseAction {
	private SystemCompileCommand compileCmd;
	private boolean isPrompt;

	/**
	 * Constructor
	 */
	public SystemCompileAction(Shell shell, SystemCompileCommand compileCommand, boolean isPrompt) {
		super(
				isPrompt ? compileCommand.getLabel() + "..." : compileCommand.getLabel(), compileCommand.getLabel(), Activator.getDefault().getImageDescriptor(IUserActionsImageIds.COMPILE_1), shell); // null == image //$NON-NLS-1$
		this.compileCmd = compileCommand;
		this.isPrompt = isPrompt;
		SystemCompileManager mgr = compileCommand.getParentType().getParentProfile().getParentManager();
		allowOnMultipleSelection(mgr.isMultiSelectSupported(compileCommand));
		if (isPrompt)
			setHelp(RSEUIPlugin.HELPPREFIX + "scpa0000"); //$NON-NLS-1$
		else
			setHelp(RSEUIPlugin.HELPPREFIX + "scna0000"); //$NON-NLS-1$
		SystemCompileCommand lucc = compileCmd.getParentType().getLastUsedCompileCommand();
		if ((lucc != null) && lucc.getLabel().equals(compileCmd.getLabel())) {
			setChecked(true);
			// if (!isPrompt)
			//	setAccelerator(SWT.CTRL | SWT.SHIFT | 'c');
		} else
			setChecked(false);
	}

	/**
	 * Intercept of parent method that is our first opportunity to enable/disable this action, typically
	 *  by interrogating the current selection, retrievable via getSelection.
	 * <p>
	 * For this compile action, we disable if we are not currently connected.
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		boolean enable = true;
		Object selected = getFirstSelection();
		if (selected == null) return false;
		ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(selected);
		if (rmtAdapter == null) enable = false;
		// yantzi:artemis6.0, we need to allow the menu item to show up even if disconnected in order
		// to allow customers to restore the tree view from cache on startup and still have all actions
		// available.  It is up to the subsystme to make sure to connect if required when the compile
		// command is run
		//else
		//	enable = rmtAdapter.getSubSystem(selected).isConnected();
		if (!enable) return false;
		SystemCompileManager mgr = compileCmd.getParentType().getParentProfile().getParentManager();
		while (enable && (selected != null)) {
			enable = mgr.isCompilable(selected);
			selected = getNextSelection();
		}
		return enable;
	}

	/**
	 * Intercept of parent method that is our opportunity to enable/disable this action, typically
	 *  by interrogating the current selection, retrievable via getSelection.
	 * <p>
	 * For this compile action, we disable if we are not currently connected.
	 */
	public boolean checkObjectType(Object selectedObject) {
		ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(selectedObject);
		if (rmtAdapter == null)
			return false;
		else
			return rmtAdapter.getSubSystem(selectedObject).isConnected();
	}

	/**
	 * Called by eclipse when the user selects this action. Does the actual running of the action.
	 */
	public void run() {
		if (checkDirtyEditors()) {
			Object element = getFirstSelection();
			boolean ok = true;
			while (ok && (element != null)) {
				SystemCompileType compType = compileCmd.getParentType();
				if (!compileCmd.equals(compType.getLastUsedCompileCommand())) {
					compType.setLastUsedCompileCommand(compileCmd);
					compType.getParentProfile().writeToDisk();
				}
				SystemCompilableSource compilableSrc = compType.getParentProfile().getCompilableSourceObject(getShell(), element, compileCmd, isPrompt, viewer);
				ok = compilableSrc.runCompileCommand();
				if (ok) element = getNextSelection();
			}
		}
	}

	protected List getDirtyEditors() {
		IStructuredSelection sel = getSelection();
		List selection = sel.toList();
		List dirtyEditors = new ArrayList();
		for (int i = 0; i < selection.size(); i++) {
			Object selected = selection.get(i);
			if (selected instanceof IAdaptable) {
				ISystemEditableRemoteObject editable = getEditableFor((IAdaptable) selected);
				if (editable != null) {
					try {
						// is the file being edited?
						if (editable.checkOpenInEditor() == 0) {
							// reference the editing editor
							editable.openEditor();
							// file is open in editor - prompt for save
							if (editable.isDirty()) {
								dirtyEditors.add(editable);
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}
		return dirtyEditors;
	}

	protected ISystemEditableRemoteObject getEditableFor(IAdaptable selected) {
		ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter) selected.getAdapter(ISystemRemoteElementAdapter.class);
		if (adapter.canEdit(selected)) {
			ISystemEditableRemoteObject editable = adapter.getEditableRemoteObject(selected);
			try {
				editable.setLocalResourceProperties();
			} catch (Exception e) {
			}
			return editable;
		}
		return null;
	}

	protected boolean checkDirtyEditors() {
		List dirtyEditors = getDirtyEditors();
		if (dirtyEditors.size() > 0) {
			AdaptableList input = new AdaptableList();
			for (int i = 0; i < dirtyEditors.size(); i++) {
				ISystemEditableRemoteObject rmtObj = (ISystemEditableRemoteObject) dirtyEditors.get(i);
				input.add(rmtObj.getRemoteObject());
			}
			WorkbenchContentProvider cprovider = new WorkbenchContentProvider();
			SystemTableViewProvider lprovider = new SystemTableViewProvider(null);
			ListSelectionDialog dlg = new ListSelectionDialog(getShell(), input, cprovider, lprovider, SystemUDAResources.EditorManager_saveResourcesMessage);
			dlg.setInitialSelections(input.getChildren());
			dlg.setTitle(SystemUDAResources.EditorManager_saveResourcesTitle);
			int result = dlg.open();
			//Just return false to prevent the operation continuing
			if (result == IDialogConstants.CANCEL_ID) return false;
			Object[] filesToSave = dlg.getResult();
			for (int s = 0; s < filesToSave.length; s++) {
				IAdaptable rmtObj = (IAdaptable) filesToSave[s];
				ISystemEditableRemoteObject editable = getEditableFor(rmtObj);
				editable.doImmediateSaveAndUpload();
			}
		}
		return true;
	}
}
