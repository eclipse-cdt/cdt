/********************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Michael Berger (IBM) - 146339 Added refresh action graphic.
 * David Dykstal (IBM) - moved SystemsPreferencesManager to a new package
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [189005] Changed setFocus() to setInput to SystemRegistryUI
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David McKnight (IBM) - [191288] Up To Action doesn't go all the way back to the connections
 * Xuan Chen        (IBM)        - [192716] Refresh Error in Table View after Renaming folder shown in table
 * Xuan Chen        (IBM)        - [194838] Move the code for comparing two objects by absolute name to a common location
 * Kevin Doyle (IBM) - [193394] After Deleting the folder shown in Table get an error
 * Kevin Doyle (IBM) - [197971] NPE when table has no input and doing commands in Systems View
 * Martin Oberhuber (Wind River) - [199585] Fix NPE during testConnectionRemoval unit test
 * David McKnight   (IBM)        - [187543] use view filter to only show containers for set input dialog
 * David McKnight   (IBM)        - [210229] table refresh needs unique table-specific tooltip-text
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * Xuan Chen        (IBM)        - [225685] NPE when running archive testcases
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API type
 * Martin Oberhuber (Wind River) - [228774] Improve ElementComparer Performance
 * David McKnight   (IBM)		 - [229116] NPE in when editing remote file in new workspace
 * David McKnight   (IBM)        - [231867] TVT34:TCT196: PLK: "Subset" window too narrow
 * David Dykstal (IBM) - [231867] TVT34:TCT196: PLK: "Subset" window too narrow
 * David Dykstal (IBM) - [188150] adding "go up one level" tooltip
 * David McKnight   (IBM)        - [232320] remote system details view restore problem
 * David McKnight   (IBM)        - [233578] Promptable Filter Displayed 3 times when clicking cancel
 * David Dykstal (IBM) - [233678] title string is constructed by concatenation, should be substituted
 * Kevin Doyle 		(IBM)		 - [242431] Register a new unique context menu id, so contributions can be made to all our views
 * David McKnight   (IBM)        - [260346] RSE view for jobs does not remember resized columns
 * David McKnight   (IBM)        - [333702] Remote Systems details view does not maintain column width settings across sessions
 * David McKnight   (IBM)        - [330398] RSE leaks SWT resources
 * David McKnight   (IBM)        - [340912] inconsistencies with columns in RSE table viewers
 * David McKnight   (IBM)        - [341240] Remote Systems Details view not remembering locked/unlocked state between sessions
 * David McKnight   (IBM)        - [341244] folder selection input to unlocked Remote Systems Details view sometimes fails
 * David McKnight   (IBM)        - [363829] Closing Eclipse with a populated Remote System Details view forces a remote system connection
 * David McKnight   (IBM)        - [372674] Enhancement - Preserve state of Remote Monitor view
 * David McKnight   (IBM)        - [373673] Remote Systems Details view calling wrong method for setting action tooltips
 * David McKnight   (IBM)        - [425113] Improve performance of RSE table views by using SWT.VIRTUAL
*******************************************************/

package org.eclipse.rse.internal.ui.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvent;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemRemoteChangeListener;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.internal.core.model.SystemRegistry;
import org.eclipse.rse.internal.ui.SystemPropertyResources;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.internal.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemActionViewerFilter;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.actions.SystemTablePrintAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemSelectAnythingDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.model.ISystemShellProvider;
import org.eclipse.rse.ui.view.IRSEViewPart;
import org.eclipse.rse.ui.view.ISystemTableViewColumnManager;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableView;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.osgi.framework.Bundle;

import com.ibm.icu.text.MessageFormat;

/**
 * Comment goes here
 */
public class SystemTableViewPart extends ViewPart
	implements ISelectionListener, ISelectionChangedListener,
		ISystemMessageLine, ISystemShellProvider,
		ISystemResourceChangeListener, ISystemRemoteChangeListener,
		IRSEViewPart
{

	class BrowseAction extends Action
	{

		public BrowseAction()
		{
		}

		public BrowseAction(String label, ImageDescriptor des)
		{
			super(label, des);

			setToolTipText(label);
		}

		public void checkEnabledState()
		{
			if (_viewer != null && _viewer.getInput() != null)
			{
				setEnabled(true);
			}
			else
			{
				setEnabled(false);
			}
		}

		public void run()
		{
		}
	}

	class ForwardAction extends BrowseAction
	{
		public ForwardAction()
		{
			super(SystemResources.ACTION_HISTORY_MOVEFORWARD_LABEL, getEclipseImageDescriptor("elcl16/forward_nav.gif")); //$NON-NLS-1$

			setToolTipText(SystemResources.ACTION_HISTORY_MOVEFORWARD_TOOLTIP);
			setDisabledImageDescriptor(getEclipseImageDescriptor("dlcl16/forward_nav.gif")); //$NON-NLS-1$
		}

		public void checkEnabledState()
		{
			if (_isLocked && _browseHistory != null && _browseHistory.size() > 0)
			{
				if (_browsePosition < _browseHistory.size() - 1)
				{
					setEnabled(true);
					return;
				}
			}

			setEnabled(false);
		}

		public void run()
		{
			_browsePosition++;

			HistoryItem historyItem = (HistoryItem) _browseHistory.get(_browsePosition);
			setInput(historyItem);
		}
	}

	class BackwardAction extends BrowseAction
	{
		public BackwardAction()
		{
			super(SystemResources.ACTION_HISTORY_MOVEBACKWARD_LABEL, getEclipseImageDescriptor("elcl16/backward_nav.gif")); //$NON-NLS-1$
			setToolTipText(SystemResources.ACTION_HISTORY_MOVEBACKWARD_TOOLTIP);
			setDisabledImageDescriptor(getEclipseImageDescriptor("dlcl16/backward_nav.gif")); //$NON-NLS-1$
		}

		public void checkEnabledState()
		{
			if (_isLocked && _browseHistory != null && _browseHistory.size() > 0)
			{
				if (_browsePosition > 0)
				{
					setEnabled(true);
					return;
				}
			}

			setEnabled(false);
		}

		public void run()
		{
			_browsePosition--;

			HistoryItem historyItem = (HistoryItem) _browseHistory.get(_browsePosition);
			setInput(historyItem);
		}
	}

	class UpAction extends BrowseAction
	{
		private IAdaptable _parent;
		public UpAction()
		{
			super(SystemResources.ACTION_GOUPLEVEL_TOOLTIP, getEclipseImageDescriptor("elcl16/up_nav.gif")); //$NON-NLS-1$
			setDisabledImageDescriptor(getEclipseImageDescriptor("dlcl16/up_nav.gif")); //$NON-NLS-1$
		}

		public void checkEnabledState()
		{
			if (_viewer.getInput() != null)
			{
				SystemTableViewProvider provider = (SystemTableViewProvider) _viewer.getContentProvider();

				// assume there is a parent
				if (provider != null)
				{
					Object parent = provider.getParent(_viewer.getInput());
					if (parent instanceof IAdaptable)
					{
						_parent = (IAdaptable) parent;
						setEnabled(true);
					}
					else
					{
						_parent = null;
						setEnabled(false);
					}
				}
				else
				{
					_parent = null;
					setEnabled(false);
				}
			}
			else
			{
				_parent = null;
				setEnabled(false);
			}
		}

		public void run()
		{
			if (_parent != null)
			{
				setInput(_parent);
			}
		}
	}

	class LockAction extends BrowseAction
	{
		public LockAction()
		{
			super();
			setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_LOCK_ID));
			String label = determineLabel();
			setText(label);
			setToolTipText(label);
		}

		/**
		 * Sets as checked or unchecked, depending on the lock state. Also changes the text and tooltip.
		 */
		public void checkEnabledState()
		{
			setChecked(_isLocked);
			String label = determineLabel();
			setText(label);
			setToolTipText(label);
		}

		public void run()
		{
			_isLocked = !_isLocked;
			showLock();
		}

		/**
		 * Returns the label depending on lock state.
		 * @return the label.
		 */
		public String determineLabel() {

			if (!_isLocked) {
				return SystemResources.ACTION_LOCK_LABEL;
			}
			else {
				return SystemResources.ACTION_UNLOCK_LABEL;
			}
		}

		/**
		 * Returns the tooltip depending on lock state.
		 * @return the tooltip.
		 */
		public String determineTooltip() {

			if (!_isLocked) {
				return SystemResources.ACTION_LOCK_TOOLTIP;
			}
			else {
				return SystemResources.ACTION_UNLOCK_TOOLTIP;
			}
		}
	}

	class RefreshAction extends BrowseAction
	{
		public RefreshAction()
		{
			super(SystemResources.ACTION_REFRESH_TABLE_LABLE,
					//RSEUIPlugin.getDefault().getImageDescriptor(ICON_SYSTEM_REFRESH_ID));
					RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_REFRESH_ID));
			setToolTipText(SystemResources.ACTION_REFRESH_TABLE_TOOLTIP);
		}

		public void run()
		{
			Object inputObject = _viewer.getInput();
			if (inputObject instanceof ISystemContainer)
			{
				((ISystemContainer)inputObject).markStale(true);
			}
			((SystemTableViewProvider) _viewer.getContentProvider()).flushCache();
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(inputObject, ISystemResourceChangeEvents.EVENT_REFRESH, inputObject));

			//_viewer.refresh();

			// refresh layout too
			//_viewer.computeLayout(true);

		}
	}

	class SelectAllAction extends BrowseAction
	{
		public SelectAllAction()
		{
			super(SystemResources.ACTION_SELECT_ALL_LABEL, null);
			setToolTipText(SystemResources.ACTION_SELECT_ALL_TOOLTIP);
		}

		public void checkEnabledState()
		{
			if (_viewer != null && _viewer.getInput() != null)
			{
				setEnabled(true);
			}
			else
			{
				setEnabled(false);
			}
		}
		public void run()
		{
			_viewer.getTable().selectAll();
			// force viewer selection change
			_viewer.setSelection(_viewer.getSelection());
		}
	}

	class SelectInputAction extends BrowseAction
	{
		public SelectInputAction()
		{
			super(SystemResources.ACTION_SELECT_INPUT_LABEL, null);
			setToolTipText(SystemResources.ACTION_SELECT_INPUT_TOOLTIP);
		}

		public void checkEnabledState()
		{
			setEnabled(true);
		}

		public void run()
		{

			SystemSelectAnythingDialog dlg = new SystemSelectAnythingDialog(_viewer.getShell(), SystemResources.ACTION_SELECT_INPUT_DLG);

			SystemActionViewerFilter filter = new SystemActionViewerFilter();
			Class[] types = {Object.class};
			filter.addFilterCriterion(types, "hasChildren", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			dlg.setViewerFilter(filter);

			Object inputObject = _viewer.getInput();
			if (inputObject == null)
			{
				inputObject = RSECorePlugin.getTheSystemRegistry();
			}
			dlg.setInputObject(inputObject);
			if (dlg.open() == Window.OK)
			{
				Object selected = dlg.getSelectedObject();
				if (selected != null && selected instanceof IAdaptable)
				{
					IAdaptable adaptable = (IAdaptable)selected;
					((ISystemViewElementAdapter)adaptable.getAdapter(ISystemViewElementAdapter.class)).setViewer(_viewer);
					setInput(adaptable);
				}
			}
		}
	}

	class PositionToAction extends BrowseAction
	{
		class PositionToDialog extends SystemPromptDialog
		{
			private String _name;
			private Combo _cbName;
			public PositionToDialog(Shell shell, String title, HistoryItem historyItem)
			{
				super(shell, title);
			}

			public String getPositionName()
			{
				return _name;
			}

			protected void buttonPressed(int buttonId)
			{
				setReturnCode(buttonId);
				_name = _cbName.getText();
				close();
			}

			protected Control getInitialFocusControl()
			{
				return _cbName;
			}

			public Control createInner(Composite parent)
			{
				Composite c = SystemWidgetHelpers.createComposite(parent, 2);

				Label aLabel = new Label(c, SWT.NONE);
				aLabel.setText(SystemPropertyResources.RESID_PROPERTY_NAME_LABEL);

				_cbName = SystemWidgetHelpers.createCombo(c, null);
				GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
				_cbName.setLayoutData(textData);
				_cbName.setText("*"); //$NON-NLS-1$
				_cbName.setToolTipText(SystemResources.RESID_TABLE_POSITIONTO_ENTRY_TOOLTIP);

				this.getShell().setText(SystemResources.RESID_TABLE_POSITIONTO_LABEL);
				setHelp();
				return c;
			}

			private void setHelp()
			{
				setHelp(RSEUIPlugin.HELPPREFIX + "gnpt0000"); //$NON-NLS-1$
			}
		}

		public PositionToAction()
		{
			super(SystemResources.ACTION_POSITIONTO_LABEL, null);
			setToolTipText(SystemResources.ACTION_POSITIONTO_TOOLTIP);
		}

		public void run()
		{

			PositionToDialog posDialog = new PositionToDialog(getViewer().getShell(), getTitle(), _currentItem);
			if (posDialog.open() == Window.OK)
			{
				String name = posDialog.getPositionName();

				_viewer.positionTo(name);
			}
		}
	}

	class SubSetAction extends BrowseAction
	{
		class SubSetDialog extends SystemPromptDialog
		{
			private String[] _filters;
			private Text[] _controls;
			private IPropertyDescriptor[] _uniqueDescriptors;
			private HistoryItem _historyItem;

			public SubSetDialog(Shell shell, IPropertyDescriptor[] uniqueDescriptors, HistoryItem historyItem)
			{
				super(shell, SystemResources.RESID_TABLE_SUBSET_LABEL);
				_uniqueDescriptors = uniqueDescriptors;
				_historyItem = historyItem;
			}

			public String[] getFilters()
			{
				return _filters;
			}

			protected void buttonPressed(int buttonId)
			{
				setReturnCode(buttonId);

				for (int i = 0; i < _controls.length; i++)
				{
					_filters[i] = _controls[i].getText();
				}

				close();
			}

			protected Control getInitialFocusControl()
			{
				return _controls[0];
			}

			public Control createInner(Composite parent)
			{
				Composite c = SystemWidgetHelpers.createComposite(parent, 2);

				int numberOfFields = _uniqueDescriptors.length;
				_controls = new Text[numberOfFields + 1];
				_filters = new String[numberOfFields + 1];

				Label nLabel = new Label(c, SWT.NONE);
				nLabel.setText(SystemPropertyResources.RESID_PROPERTY_NAME_LABEL);

				String[] histFilters = null;
				if (_historyItem != null)
				{
					histFilters = _historyItem.getFilters();
				}

				_controls[0] = SystemWidgetHelpers.createTextField(c, null);
				GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
				_controls[0].setLayoutData(textData);
				_controls[0].setText("*"); //$NON-NLS-1$
				_controls[0].setToolTipText(SystemResources.RESID_TABLE_SUBSET_ENTRY_TOOLTIP);

				if (histFilters != null)
				{
					_controls[0].setText(histFilters[0]);
				}

				for (int i = 0; i < numberOfFields; i++)
				{
					IPropertyDescriptor des = _uniqueDescriptors[i];

					Label aLabel = new Label(c, SWT.NONE);
					aLabel.setText(des.getDisplayName());

					_controls[i + 1] = SystemWidgetHelpers.createTextField(c, null);
					GridData textData3 = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
					textData3.widthHint = 150;
					_controls[i + 1].setLayoutData(textData3);
					_controls[i + 1].setText("*"); //$NON-NLS-1$	

					if (histFilters != null)
					{
						_controls[i + 1].setText(histFilters[i + 1]);
						_controls[i + 1].setToolTipText(SystemResources.RESID_TABLE_SUBSET_ENTRY_TOOLTIP);
					}
				}

				setHelp();
				return c;
			}

			private void setHelp()
			{
				setHelp(RSEUIPlugin.HELPPREFIX + "gnss0000"); //$NON-NLS-1$
			}
		}

		public SubSetAction()
		{
			super(SystemResources.ACTION_SUBSET_LABEL, null);
			setToolTipText(SystemResources.ACTION_SUBSET_TOOLTIP);
		}

		public void run()
		{
			SubSetDialog subsetDialog = new SubSetDialog(getViewer().getShell(), _viewer.getVisibleDescriptors(_viewer.getInput()), _currentItem);
			if (subsetDialog.open() == Window.OK)
			{
				String[] filters = subsetDialog.getFilters();
				_currentItem.setFilters(filters);
				_viewer.setViewFilters(filters);
			}
		}
	}

	class HistoryItem
	{
		private String[] _filters;
		private IAdaptable _object;

		public HistoryItem(IAdaptable object, String[] filters)
		{
			_object = object;
			_filters = filters;
		}

		public IAdaptable getObject()
		{
			return _object;
		}

		public String[] getFilters()
		{
			return _filters;
		}

		public void setFilters(String[] filters)
		{
			_filters = filters;
		}
	}

	class RestoreStateRunnable extends Job
	{
		private IMemento _rmemento;
		public RestoreStateRunnable(IMemento memento)
		{
			super(SystemResources.RESID_RESTORE_RSE_TABLE_JOB); 
			_rmemento = memento;
		}

		public IStatus run(final IProgressMonitor monitor)
		{
			try {
				IStatus wstatus = RSECorePlugin.waitForInitCompletion();
				if (!wstatus.isOK() && wstatus.getSeverity() == IStatus.ERROR){
					return wstatus;
				}
			}
			catch (InterruptedException e){				
				return Status.CANCEL_STATUS;
			}
			
			
			final IMemento memento = _rmemento;
			
			// set the cached column widths (for later use)
			String columnWidths = memento.getString(TAG_TABLE_VIEW_COLUMN_WIDTHS_ID);			
			if (columnWidths != null)
			{			
				if (columnWidths.indexOf(";") > 0){	//$NON-NLS-1$
					// matches new format for column width memento
					// new code - as of RSE 3.1
					HashMap cachedColumnWidths = new HashMap();
	
					// parse out set of columns
					String[] columnSets = columnWidths.split(";"); //$NON-NLS-1$
					for (int i = 0; i < columnSets.length; i++){
						String columnSet = columnSets[i];
						
						// parse out columns for set
						String[] pair = columnSet.split("="); //$NON-NLS-1$
						String key = pair[0];
	
						// parse out widths
						String widthArray = pair[1];
						String[] widthStrs = widthArray.split(","); //$NON-NLS-1$
						
						int[] widths = new int[widthStrs.length];
						for (int w = 0; w < widths.length; w++){
							widths[w] = Integer.parseInt(widthStrs[w]);
						}
						
						cachedColumnWidths.put(key, widths);
					}										
					_viewer.setCachedColumnWidths(cachedColumnWidths);
				}
			}
						
			String profileId = memento.getString(TAG_TABLE_VIEW_PROFILE_ID);
			String connectionId = memento.getString(TAG_TABLE_VIEW_CONNECTION_ID);
			String subsystemId = memento.getString(TAG_TABLE_VIEW_SUBSYSTEM_ID);
			final String filterID = memento.getString(TAG_TABLE_VIEW_FILTER_ID);
			final String objectID = memento.getString(TAG_TABLE_VIEW_OBJECT_ID);
			
			Boolean locked = memento.getBoolean(TAG_TABLE_VIEW_LOCKED_ID);
			if (locked == null || locked.booleanValue()){
				_isLocked = true;
			}
			else {
				_isLocked = false;
			}

			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();

			Object input = null;
			if (subsystemId == null)
			{
				if (connectionId != null)
				{

					ISystemProfile profile = registry.getSystemProfile(profileId);
					input = registry.getHost(profile, connectionId);
				}
				else
				{
				    // 191288 we now use registry instead of registry ui as input
					input = registry;
				}
			}
			else
			{
				// from the subsystem ID determine the profile, system and subsystem
				final ISubSystem subsystem = registry.getSubSystem(subsystemId);

				if (subsystem != null) {
					if (filterID == null && objectID == null) {
						input = subsystem;
					}
					else {
						if (!subsystem.isConnected()) {
							try {
								final Object finInput = input;
								subsystem.connect(false, new IRSECallback() {
									public void done(IStatus status, Object result) {
										// this needs to be done on the main thread
										// so doing an asynchExec()
										Display.getDefault().asyncExec(new RunOnceConnectedOnMainThread(memento, finInput, subsystem, filterID, objectID));
									}
								});
								return Status.OK_STATUS;
							}
							catch (Exception e) {
								return Status.CANCEL_STATUS;
							}
						}
						return runOnceConnected(monitor, memento, input, subsystem, filterID, objectID);
					} // end else
				} // end if (subsystem != null)
			} // end else
			return runWithInput(monitor, input, memento);
		}

		private class RunOnceConnectedOnMainThread implements Runnable
		{
			private IMemento _inmemento;
			private Object _input;
			private ISubSystem _subSystem;
			private String _filterID;
			private String _objectID;
			public RunOnceConnectedOnMainThread(IMemento memento, Object input, ISubSystem subSystem, String filterID, String objectID)
			{
				_inmemento = memento;
				_input = input;
				_subSystem = subSystem;
				_filterID = filterID;
				_objectID = objectID;
			}

			public void run()
			{
				runOnceConnected(new NullProgressMonitor(), _inmemento, _input, _subSystem, _filterID, _objectID);
			}
		}

		public IStatus runOnceConnected(IProgressMonitor monitor, IMemento memento, Object input, ISubSystem subsystem, String filterID, String objectID)
		{
			if (subsystem.isConnected()) {
				if (filterID != null) {
					try {
						input = subsystem.getObjectWithAbsoluteName(filterID, monitor);
					}
					catch (Exception e) {
						//ignore
					}
				}
				else {
					if (objectID != null) {
						try {
							input = subsystem.getObjectWithAbsoluteName(objectID, monitor);
						}
						catch (Exception e)	{
							return Status.CANCEL_STATUS;
						}
					}
				} // end else
			} // end if (subsystem.isConnected)
			return runWithInput(monitor, input, memento);
		}

		public IStatus runWithInput(IProgressMonitor monitor, Object input, IMemento memento)
		{
			if (input != null && input instanceof IAdaptable)
			{
				_mementoInput = (IAdaptable) input;
				if (_mementoInput != null && _viewer != null)
				{
					// set input needs to be run on the main thread
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run(){
							setInput(_mementoInput);
						}
					});
				}
			}
			return Status.OK_STATUS;
		}

	}



	private class SelectColumnsAction extends BrowseAction
	{

	    class SelectColumnsDialog extends SystemPromptDialog
		{
	        private ISystemViewElementAdapter _adapter;
	        private ISystemTableViewColumnManager _columnManager;
			private IPropertyDescriptor[] _uniqueDescriptors;
			private ArrayList _currentDisplayedDescriptors;
			private ArrayList _availableDescriptors;

			private List _availableList;
			private List _displayedList;

			private Button _addButton;
			private Button _removeButton;
			private Button _upButton;
			private Button _downButton;
			
			private boolean _changed = false;


			public SelectColumnsDialog(Shell shell, ISystemViewElementAdapter viewAdapter, ISystemTableViewColumnManager columnManager, int[] originalOrder)
			{
				super(shell, SystemResources.RESID_TABLE_SELECT_COLUMNS_LABEL);
				setToolTipText(SystemResources.RESID_TABLE_SELECT_COLUMNS_TOOLTIP);
				setInitialOKButtonEnabledState(_changed);
				_adapter = viewAdapter;
				_columnManager = columnManager;
				_uniqueDescriptors = viewAdapter.getUniquePropertyDescriptors();
				IPropertyDescriptor[] initialDisplayedDescriptors = _columnManager.getVisibleDescriptors(_adapter);
								
				IPropertyDescriptor[] sortedDisplayedDescriptors = new IPropertyDescriptor[initialDisplayedDescriptors.length];
				for (int i = 0; i < initialDisplayedDescriptors.length; i++){
					int position = originalOrder[i+1];
					sortedDisplayedDescriptors[i] = initialDisplayedDescriptors[position-1];
				}				
				_currentDisplayedDescriptors = new ArrayList(initialDisplayedDescriptors.length);
				for (int i = 0; i < sortedDisplayedDescriptors.length;i++)
				{					
					_currentDisplayedDescriptors.add(sortedDisplayedDescriptors[i]);				
				}
				_availableDescriptors = new ArrayList(_uniqueDescriptors.length);
				for (int i = 0; i < _uniqueDescriptors.length;i++)
				{
				    if (!_currentDisplayedDescriptors.contains(_uniqueDescriptors[i]))
				    {
				        _availableDescriptors.add(_uniqueDescriptors[i]);
				    }
				}
			}


			public void handleEvent(Event e)
			{
			    Widget source = e.widget;
			    if (source == _addButton)
			    {
			        int[] toAdd = _availableList.getSelectionIndices();
			        addToDisplay(toAdd);
			        _changed = true;
			    }
			    else if (source == _removeButton)
			    {
			        int[] toAdd = _displayedList.getSelectionIndices();
			        removeFromDisplay(toAdd);
			        _changed = true;
			    }
			    else if (source == _upButton)
			    {
			        int index = _displayedList.getSelectionIndex();
			        moveUp(index);
			        _displayedList.select(index - 1);
			        _changed = true;
			    }
			    else if (source == _downButton)
			    {
			        int index = _displayedList.getSelectionIndex();
			        moveDown(index);
			        _displayedList.select(index + 1);
			        _changed = true;
			    }

			    // update button enable states
			    updateEnableStates();
			}

			public IPropertyDescriptor[] getDisplayedColumns()
			{
			    IPropertyDescriptor[] displayedColumns = new IPropertyDescriptor[_currentDisplayedDescriptors.size()];
			    for (int i = 0; i< _currentDisplayedDescriptors.size();i++)
			    {
			        displayedColumns[i]= (IPropertyDescriptor)_currentDisplayedDescriptors.get(i);
			    }
			    return displayedColumns;
			}

			private void updateEnableStates()
			{
			    boolean enableAdd = false;
			    boolean enableRemove = false;
			    boolean enableUp = false;
			    boolean enableDown = false;

			    int[] availableSelected = _availableList.getSelectionIndices();
			    for (int i = 0; i < availableSelected.length; i++)
			    {
			        int index = availableSelected[i];
			        IPropertyDescriptor descriptor = (IPropertyDescriptor)_availableDescriptors.get(index);
			        if (!_currentDisplayedDescriptors.contains(descriptor))
			        {
			            enableAdd = true;
			        }
			    }

			    if (_displayedList.getSelectionCount()>0)
			    {
			        enableRemove = true;

			        int index = _displayedList.getSelectionIndex();
			        if (index > 0)
			        {
			            enableUp = true;
			        }
			        if (index < _displayedList.getItemCount()-1)
			        {
			            enableDown = true;
			        }
			    }

			    _addButton.setEnabled(enableAdd);
			    _removeButton.setEnabled(enableRemove);
			    _upButton.setEnabled(enableUp);
			    _downButton.setEnabled(enableDown);
			    enableOkButton(_changed);
			}

			private void moveUp(int index)
			{
			    Object obj = _currentDisplayedDescriptors.remove(index);
		        _currentDisplayedDescriptors.add(index - 1, obj);
		        refreshDisplayedList();
			}

			private void moveDown(int index)
			{
			    Object obj = _currentDisplayedDescriptors.remove(index);
		        _currentDisplayedDescriptors.add(index + 1, obj);

		        refreshDisplayedList();
			}

			private void addToDisplay(int[] toAdd)
			{
			    ArrayList added = new ArrayList();
			    for (int i = 0; i < toAdd.length; i++)
			    {
			        int index = toAdd[i];

			        IPropertyDescriptor descriptor = (IPropertyDescriptor)_availableDescriptors.get(index);

			        if (!_currentDisplayedDescriptors.contains(descriptor))
			        {
			            _currentDisplayedDescriptors.add(descriptor);
			            added.add(descriptor);
			        }
			    }

			    for (int i = 0; i < added.size(); i++)
			    {
			      _availableDescriptors.remove(added.get(i));
			    }


			    refreshAvailableList();
			    refreshDisplayedList();

			}

			private void removeFromDisplay(int[] toRemove)
			{
			    for (int i = 0; i < toRemove.length; i++)
			    {
			        int index = toRemove[i];
			        IPropertyDescriptor descriptor = (IPropertyDescriptor)_currentDisplayedDescriptors.get(index);
			        _currentDisplayedDescriptors.remove(index);
			        _availableDescriptors.add(descriptor);
			    }
			    refreshDisplayedList();
			    refreshAvailableList();
			}

			protected void buttonPressed(int buttonId)
			{
				setReturnCode(buttonId);

				close();
			}

			protected Control getInitialFocusControl()
			{
				return _availableList;
			}

			public Control createInner(Composite parent)
			{
				Composite main = SystemWidgetHelpers.createComposite(parent, 1);


				Composite c = SystemWidgetHelpers.createComposite(main, 4);
				c.setLayoutData(new GridData(GridData.FILL_BOTH));
				_availableList = SystemWidgetHelpers.createListBox(c, SystemResources.RESID_TABLE_SELECT_COLUMNS_AVAILABLE_LABEL, this, true);

				Composite addRemoveComposite = SystemWidgetHelpers.createComposite(c, 1);
				addRemoveComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				_addButton = SystemWidgetHelpers.createPushButton(addRemoveComposite,
				        SystemResources.RESID_TABLE_SELECT_COLUMNS_ADD_LABEL,
				        this);
				_addButton.setToolTipText(SystemResources.RESID_TABLE_SELECT_COLUMNS_ADD_TOOLTIP);

				_removeButton = SystemWidgetHelpers.createPushButton(addRemoveComposite,
				        SystemResources.RESID_TABLE_SELECT_COLUMNS_REMOVE_LABEL,
				        this);
				_removeButton.setToolTipText(SystemResources.RESID_TABLE_SELECT_COLUMNS_REMOVE_TOOLTIP);

				_displayedList = SystemWidgetHelpers.createListBox(c, SystemResources.RESID_TABLE_SELECT_COLUMNS_DISPLAYED_LABEL, this, false);

				Composite upDownComposite = SystemWidgetHelpers.createComposite(c, 1);
				upDownComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				_upButton = SystemWidgetHelpers.createPushButton(upDownComposite,
				        SystemResources.RESID_TABLE_SELECT_COLUMNS_UP_LABEL,
				        this);
				_upButton.setToolTipText(SystemResources.RESID_TABLE_SELECT_COLUMNS_UP_TOOLTIP);

				_downButton = SystemWidgetHelpers.createPushButton(upDownComposite,
				        SystemResources.RESID_TABLE_SELECT_COLUMNS_DOWN_LABEL,
				        this);
				_downButton.setToolTipText(SystemResources.RESID_TABLE_SELECT_COLUMNS_DOWN_TOOLTIP);

				initLists();

				setHelp();
				return c;
			}

			private void initLists()
			{
			   refreshAvailableList();
			   refreshDisplayedList();
			   updateEnableStates();
			}

			private void refreshAvailableList()
			{
			    _availableList.removeAll();
			    // initialize available list
			    for (int i = 0; i < _availableDescriptors.size(); i++)
			    {
			        IPropertyDescriptor descriptor = (IPropertyDescriptor)_availableDescriptors.get(i);
			        _availableList.add(descriptor.getDisplayName());
			    }
			}

			private void refreshDisplayedList()
			{
			    _displayedList.removeAll();
			    // initialize display list
			    for (int i = 0; i < _currentDisplayedDescriptors.size(); i++)
			    {

			        Object obj = _currentDisplayedDescriptors.get(i);
			        if (obj != null && obj instanceof IPropertyDescriptor)
			        {
			            _displayedList.add(((IPropertyDescriptor)obj).getDisplayName());
			        }
			    }
			}

			private void setHelp()
			{
				setHelp(RSEUIPlugin.HELPPREFIX + "gntc0000"); //$NON-NLS-1$
			}
		}

		public SelectColumnsAction()
		{
			super(SystemResources.ACTION_SELECTCOLUMNS_LABEL, null);
			setToolTipText(SystemResources.ACTION_SELECTCOLUMNS_TOOLTIP);
			setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTER_ID));
		}

		public void checkEnabledState()
		{
			if (_viewer != null && _viewer.getInput() != null)
			{
				setEnabled(true);
			}
			else
			{
				setEnabled(false);
			}
		}
		public void run()
		{
		    ISystemTableViewColumnManager mgr = _viewer.getColumnManager();
		    ISystemViewElementAdapter adapter = _viewer.getAdapterForContents();
		    Table table = _viewer.getTable();
		    int[] originalOrder = table.getColumnOrder();
		    SelectColumnsDialog dlg = new SelectColumnsDialog(getShell(), adapter, mgr, originalOrder);
		    if (dlg.open() == Window.OK)
		    {
		    	IPropertyDescriptor[] newDescriptors = dlg.getDisplayedColumns();
		    	// reset column order
		    	int n = newDescriptors.length + 1;
		    	int[] newOrder = new int[n];		    	
		    	for (int i = 0; i < n; i++){	
		    		newOrder[i] = i;
		    	}
		    			    	
		    	mgr.setCustomDescriptors(adapter, newDescriptors);	
		        _viewer.computeLayout(true);
		        table.setColumnOrder(newOrder);

		        _viewer.refresh();
		    }
		}
	}

	private HistoryItem _currentItem;

	private SystemTableView _viewer;

	protected ArrayList _browseHistory;
	protected int _browsePosition;

	private ForwardAction _forwardAction = null;
	private BackwardAction _backwardAction = null;
	private UpAction _upAction = null;

	private LockAction _lockAction = null;
	private RefreshAction _refreshAction = null;
	private SystemRefreshAction _refreshSelectionAction = null;

	private SelectInputAction _selectInputAction = null;
	private PositionToAction _positionToAction = null;
	private SubSetAction _subsetAction = null;
	private SystemTablePrintAction _printTableAction = null;
	private SelectColumnsAction _selectColumnsAction = null;

	// common actions
	private SystemCopyToClipboardAction _copyAction;
	private SystemPasteFromClipboardAction _pasteAction;
	private SystemCommonDeleteAction _deleteAction;
	private SystemCommonRenameAction _renameAction;

	private IMemento _memento = null;
	private IAdaptable _mementoInput = null;
	private Object _lastSelection = null;

	private boolean _isLocked = false;

	//  for ISystemMessageLine
	private String _message, _errorMessage;
	private SystemMessage sysErrorMessage;
	private IStatusLineManager _statusLine = null;

	// constants
	public static final String ID = "org.eclipse.rse.ui.view.systemTableView"; // matches id in plugin.xml, view tag	 //$NON-NLS-1$

	// Restore memento tags
	public static final String TAG_TABLE_VIEW_PROFILE_ID = "tableViewProfileID"; //$NON-NLS-1$
	public static final String TAG_TABLE_VIEW_CONNECTION_ID = "tableViewConnectionID"; //$NON-NLS-1$
	public static final String TAG_TABLE_VIEW_SUBSYSTEM_ID = "tableViewSubsystemID"; //$NON-NLS-1$
	public static final String TAG_TABLE_VIEW_OBJECT_ID = "tableViewObjectID"; //$NON-NLS-1$
	public static final String TAG_TABLE_VIEW_FILTER_ID = "tableViewFilterID"; //$NON-NLS-1$
	public static final String TAG_TABLE_VIEW_LOCKED_ID = "tableViewLockedID"; //$NON-NLS-1$

	// Subset memento tags
	public static final String TAG_TABLE_VIEW_SUBSET = "subset"; //$NON-NLS-1$

	// layout memento tags
	public static final String TAG_TABLE_VIEW_COLUMN_WIDTHS_ID = "columnWidths"; //$NON-NLS-1$

	public void setFocus()
	{
	    if (_viewer.getInput() == null)
	    {
	        if (_memento != null)
	        {
	            restoreState(_memento);
	        }
	        else
	        {
	            setInput(RSECorePlugin.getTheSystemRegistry());
	        }
	    }

		_viewer.getControl().setFocus();
	}

	public SystemTableView getViewer()
	{
		return _viewer;
	}

	public Viewer getRSEViewer()
	{
		return _viewer;
	}

	public void createPartControl(Composite parent)
	{
		//Want to register SystemTableViewPart as resouce change listener first, since it may update the _inputObject
		//of the SystemTableView, which will affect the behaviour of the resource change event handling of SystemTableView.
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		registry.addSystemResourceChangeListener(this);
		registry.addSystemRemoteChangeListener(this);

		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.VIRTUAL);
		_viewer = new SystemTableView(table, this);

		table.setLinesVisible(true);

		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.addSelectionListener(this);
		_viewer.addSelectionChangedListener(this);
		getSite().setSelectionProvider(_viewer);

		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				handleDoubleClick(event);
			}
		});

		_isLocked = true;
		fillLocalToolBar();

		_browseHistory = new ArrayList();
		_browsePosition = 0;

		// register global edit actions
		CellEditorActionHandler editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());

		_copyAction = new SystemCopyToClipboardAction(_viewer.getShell(), null);
		_pasteAction = new SystemPasteFromClipboardAction(_viewer.getShell(), null);
		_deleteAction = new SystemCommonDeleteAction(_viewer.getShell(), _viewer);
		_renameAction = new SystemCommonRenameAction(_viewer.getShell(), _viewer);

		editorActionHandler.setCopyAction(_copyAction);
		editorActionHandler.setPasteAction(_pasteAction);
		editorActionHandler.setDeleteAction(_deleteAction);
		editorActionHandler.setSelectAllAction(new SelectAllAction());

		// register rename action as a global handler
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.RENAME.getId(), _renameAction);


		SystemWidgetHelpers.setHelp(_viewer.getControl(), RSEUIPlugin.HELPPREFIX + "sysd0000"); //$NON-NLS-1$

		getSite().registerContextMenu(_viewer.getContextMenuManager(), _viewer);
		getSite().registerContextMenu(ISystemContextMenuConstants.RSE_CONTEXT_MENU, _viewer.getContextMenuManager(), _viewer);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel)
	{
		if (part != this && (part instanceof SystemViewPart))
		{
			if (!_isLocked)
			{
				if (sel instanceof IStructuredSelection)
				{
					Object first = ((IStructuredSelection) sel).getFirstElement();
					if (_lastSelection != first)
					{						
						if (first instanceof IAdaptable)
						{
							{
								IAdaptable adapt = (IAdaptable) first;
								ISystemViewElementAdapter va = (ISystemViewElementAdapter) adapt.getAdapter(ISystemViewElementAdapter.class);
								if (va != null && !(va instanceof SystemViewPromptableAdapter))
								{
									if (va.hasChildren(adapt) && adapt != _viewer.getInput())
									{
										setInput(adapt);
										_lastSelection = first;
									}
								}
							}
						}
					}
				}
			}
		}
		else
			if (part == this)
			{
				updateActionStates();
			}
	}

	public void dispose()
	{
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.removeSelectionListener(this);
		_viewer.removeSelectionChangedListener(this);

		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		registry.removeSystemRemoteChangeListener(this);
		registry.removeSystemResourceChangeListener(this);

		if (_viewer != null)
		{
			_viewer.dispose();
		}

		super.dispose();
	}

	private void handleDoubleClick(DoubleClickEvent event)
	{
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		if (element == null)
			return;

		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) element).getAdapter(ISystemViewElementAdapter.class);
		boolean alreadyHandled = false;
		if (adapter != null)
		{
			alreadyHandled = adapter.handleDoubleClick(element);
			if (!alreadyHandled)
			{
				if (adapter.isPromptable(element))
				{
					adapter.getChildren((IAdaptable)element, new NullProgressMonitor());
				}
				else if (adapter.hasChildren((IAdaptable)element))
				{
					setInput((IAdaptable) element);
				}
			}
		}
	}

	public void updateActionStates()
	{
		if (_refreshAction == null)
			fillLocalToolBar();

		_backwardAction.checkEnabledState();
		_forwardAction.checkEnabledState();
		_upAction.checkEnabledState();
		_lockAction.checkEnabledState();
		_refreshAction.checkEnabledState();

		_selectInputAction.checkEnabledState();
		_positionToAction.checkEnabledState();
		_subsetAction.checkEnabledState();

		_printTableAction.checkEnabledState();
		_selectColumnsAction.checkEnabledState();
	}

	private ImageDescriptor getEclipseImageDescriptor(String relativePath)
	{
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try
		{
		    Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
			URL installURL = bundle.getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}

	public void fillLocalToolBar()
	{

		if (_refreshAction == null)
		{
			// refresh action
			_refreshAction = new RefreshAction();

			// history actions
			_backwardAction = new BackwardAction();
			_forwardAction = new ForwardAction();

			// parent/child actions
			_upAction = new UpAction();

			// lock action
			_lockAction = new LockAction();

			_selectInputAction = new SelectInputAction();
			_positionToAction = new PositionToAction();
			_subsetAction = new SubSetAction();

			_printTableAction = new SystemTablePrintAction(getTitle(), _viewer);
			_selectColumnsAction = new SelectColumnsAction();
		}

		updateActionStates();

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		IMenuManager menuMgr = actionBars.getMenuManager();


	_refreshSelectionAction = new SystemRefreshAction(getShell());
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), _refreshSelectionAction);
		_refreshSelectionAction.setSelectionProvider(_viewer);

		_statusLine = actionBars.getStatusLineManager();

		addToolBarItems(toolBarManager);
		addToolBarMenuItems(menuMgr);
	}

	private void addToolBarMenuItems(IMenuManager menuManager)
	{
		menuManager.removeAll();
		menuManager.add(_selectColumnsAction);
		menuManager.add(new Separator("View")); //$NON-NLS-1$
		menuManager.add(_selectInputAction);
		menuManager.add(new Separator("Filter")); //$NON-NLS-1$
		menuManager.add(_positionToAction);
		menuManager.add(_subsetAction);

	//DKM - this action is useless - remove it
	//	menuManager.add(new Separator("Print"));
	//	menuManager.add(_printTableAction);

	}

	private void addToolBarItems(IToolBarManager toolBarManager)
	{
		toolBarManager.removeAll();

		_lockAction.setChecked(_isLocked);

		toolBarManager.add(_lockAction);
		toolBarManager.add(_refreshAction);


		toolBarManager.add(new Separator("Navigate")); //$NON-NLS-1$
		// only support history when we're locked
		if (_isLocked)
		{
			toolBarManager.add(_backwardAction);
			toolBarManager.add(_forwardAction);
		}

		toolBarManager.add(_upAction);

		toolBarManager.add(new Separator("View")); //$NON-NLS-1$
		toolBarManager.add(_selectColumnsAction);
	}

	public void showLock()
	{
		if (_upAction != null)
		{
			IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
			toolBarManager.removeAll();

			updateActionStates();

			addToolBarItems(toolBarManager);
		}
	}

	public void selectionChanged(SelectionChangedEvent e)
	{
		// listener for this view
		updateActionStates();

		IStructuredSelection sel = (IStructuredSelection) e.getSelection();
		_copyAction.setEnabled(_copyAction.updateSelection(sel));
		_pasteAction.setEnabled(_pasteAction.updateSelection(sel));
		_deleteAction.setEnabled(_deleteAction.updateSelection(sel));
	}

	public void setInput(IAdaptable object)
	{
		String[] filters = null;
		if (_currentItem != null)
		{
			IAdaptable item = _currentItem.getObject();

			ISystemViewElementAdapter adapter1 = (ISystemViewElementAdapter)object.getAdapter(ISystemViewElementAdapter.class);
			ISystemViewElementAdapter adapter2 = (ISystemViewElementAdapter)item.getAdapter(ISystemViewElementAdapter.class);
			if (adapter1 == adapter2)
			{
				filters = _currentItem.getFilters();
			}
			else
			{
				_viewer.setViewFilters(null);
			}
		}
		setInput(object, filters, _isLocked);

		if (!_isLocked)
		{
			_currentItem = new HistoryItem(object, null);
		}
	}

	public void setInput(HistoryItem historyItem)
	{
		setInput(historyItem.getObject(), historyItem.getFilters(), false);

		_currentItem = historyItem;

	}

	public void setInput(IAdaptable object, String[] filters, boolean updateHistory)
	{
		if (_viewer != null /*&& object != null*/)
		{
			setTitle(object);
			_viewer.setInput(object);

			if (_refreshSelectionAction != null)
			{
				_refreshSelectionAction.updateSelection(new StructuredSelection(object));
			}
			if (filters != null)
			{
				_viewer.setViewFilters(filters);
			}

			if (updateHistory)
			{
				while (_browsePosition < _browseHistory.size() - 1)
				{
					_browseHistory.remove(_browseHistory.get(_browseHistory.size() - 1));
				}

				_currentItem = new HistoryItem(object, filters);


				_browseHistory.add(_currentItem);
				_browsePosition = _browseHistory.lastIndexOf(_currentItem);
			}

			updateActionStates();

		}
	}

	public void setTitle(IAdaptable object)
	{
	    if (object == null)
	    {
	        setContentDescription(""); //$NON-NLS-1$
	    }
	    else
	    {
		ISystemViewElementAdapter va = (ISystemViewElementAdapter) object.getAdapter(ISystemViewElementAdapter.class);
		if (va != null)
		{
			String type = va.getType(object);
			String name = va.getName(object);
			String title = MessageFormat.format(SystemResources.SystemTableViewPart_title, new String[] {type, name});
			setContentDescription(title);
		}
	    }
	}

	/**
	   * Used to asynchronously update the view whenever properties change.
	   */
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		Object child = event.getSource();
		Object input = _viewer.getInput();
		switch (event.getType())
		{
		case ISystemResourceChangeEvents.EVENT_RENAME:
			if (child == input)
			{
				setTitle((IAdaptable) child);
			}
			break;
		case ISystemResourceChangeEvents.EVENT_DELETE:
			removeFromHistory(event.getSource());
			break;
  	    case ISystemResourceChangeEvents.EVENT_DELETE_MANY:
  	  		Object[] multi = event.getMultiSource();
  	  		for (int i = 0; i < multi.length; i++) {
  	  			// Update the history to remove all references to object
  	  			removeFromHistory(multi[i]);
  	  		}
  	  		break;
  	    default:
  	    	break;
		}
	}

	protected void removeFromHistory(Object c)
	{
	    // if the object is in history, remove it since it's been deleted
		// and remove all objects whose parent is the deleted object
	    for (int i = 0; i < _browseHistory.size(); i++)
	    {
	        HistoryItem hist = (HistoryItem)_browseHistory.get(i);
	        Object historyObj = hist.getObject();
	        if (historyObj == c || historyObj.equals(c) || isParentOf(c,historyObj))
	        {
	            _browseHistory.remove(hist);
	            if (_browsePosition >= i)
	            {
	                _browsePosition--;
	                if (_browsePosition < 0)
	                {
	                    _browsePosition = 0;
	                }
	            }
	            // 	Since we are removing an item the size decreased by one so i
	            // needs to decrease by one or we will skip elements in _browseHistory
	            i--;
	        }
	    }

	    if (_currentItem != null) {
	    	Object currentObject = _currentItem.getObject();

		    // Update the input of the viewer to the closest item in the history
		    // that still exists if the current viewer item has been deleted.
		    if (c == currentObject || c.equals(currentObject) || isParentOf(c,currentObject))
	        {
	            if (_browseHistory.size() > 0)
	            {
	                _currentItem = (HistoryItem)_browseHistory.get(_browsePosition);
	                setInput(_currentItem.getObject(), null, false);
	            }
	            else
	            {
	                _currentItem = null;
	                setInput(RSECorePlugin.getTheSystemRegistry(), null, true);
	            }
	        }
	    }
	}

	protected boolean isParentOf(Object parent, Object child) {
		if (parent instanceof IAdaptable && child instanceof IAdaptable) {
			ISystemDragDropAdapter adapterParent = (ISystemDragDropAdapter) ((IAdaptable)parent).getAdapter(ISystemDragDropAdapter.class);
			ISystemDragDropAdapter adapterChild = (ISystemDragDropAdapter) ((IAdaptable)child).getAdapter(ISystemDragDropAdapter.class);
			// Check that both parent and child are from the same SubSystem
			if (adapterParent != null && adapterChild != null &&
					adapterParent.getSubSystem(parent) == adapterChild.getSubSystem(child)) {
				String parentAbsoluteName = adapterParent.getAbsoluteName(parent);
				String childAbsoluteName = adapterChild.getAbsoluteName(child);
				// Check if the child's absolute name starts with the parents absolute name
				// if it does then parent is the parent of child.
				if(childAbsoluteName != null && childAbsoluteName.startsWith(parentAbsoluteName)) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * This is the method in your class that will be called when a remote resource
	 *  changes. You will be called after the resource is changed.
	 * @see org.eclipse.rse.core.events.ISystemRemoteChangeEvent
	 */
	public void systemRemoteResourceChanged(ISystemRemoteChangeEvent event)
	{
		int eventType = event.getEventType();
		Object remoteResource = event.getResource();
		java.util.List remoteResourceNames = null;
		if (remoteResource instanceof java.util.List)
		{
			remoteResourceNames = (java.util.List) remoteResource;
			remoteResource = remoteResourceNames.get(0);
		}

		Object child = event.getResource();


		Object input = _viewer.getInput();

		String[] oldNames = event.getOldNames();
		// right now assuming only one resource
		String oldName = (oldNames == null) ? null : oldNames[0];
		boolean referToSameObject = false;
		if (input != null && oldName != null){
			referToSameObject = SystemRegistry.isSameObjectByAbsoluteName(input, null, child, oldName);
		}

		if (input == child || child instanceof java.util.List || referToSameObject)
		{
			switch (eventType)
			{
				// --------------------------
				// REMOTE RESOURCE CHANGED...
				// --------------------------
				case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED :
					break;

					// --------------------------
					// REMOTE RESOURCE CREATED...
					// --------------------------
				case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED :
					break;

					// --------------------------
					// REMOTE RESOURCE DELETED...
					// --------------------------
				case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED :
					{
				    	if (child instanceof java.util.List)
				    	{
				    		java.util.List list = (java.util.List)child;
				    	    for (int v = 0; v < list.size(); v++)
				    	    {
				    	        Object c = list.get(v);

				    	        removeFromHistory(c);
				    	        /*
				    	        if (c == input)
				    	        {
				    	            setInput((IAdaptable)null, null, false);

				    	            return;
				    	        }
				    	        */
				    	    }
				    	}
				    	else
				    	{
				    	    removeFromHistory(child);
				    	    //setInput((IAdaptable)null);

				    	    return;
				    	}
					}
					break;

					// --------------------------
					// REMOTE RESOURCE RENAMED...
					// --------------------------
				case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED :
					{
				    	setInput((IAdaptable)child);
					}

					break;
			}
		}
	}

	public Shell getShell()
	{
		return _viewer.getShell();
	}

	private void restoreState(IMemento memento)
	{
		RestoreStateRunnable rsr = new RestoreStateRunnable(memento);
		rsr.setRule(RSECorePlugin.getTheSystemRegistry());
		rsr.schedule();		
	}

	/**
	* Initializes this view with the given view site.  A memento is passed to
	* the view which contains a snapshot of the views state from a previous
	* session.  Where possible, the view should try to recreate that state
	* within the part controls.
	* <p>
	* The parent's default implementation will ignore the memento and initialize
	* the view in a fresh state.  Subclasses may override the implementation to
	* perform any state restoration as needed.
	*/
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);

		if (memento != null && SystemPreferencesManager.getRememberState())
		{
			_memento = memento;

		}
	}

	/**
	 * Method declared on IViewPart.
	 */
	public void saveState(IMemento memento)
	{
		super.saveState(memento);

		if (!SystemPreferencesManager.getRememberState())
			return;

		if (_viewer != null)
		{
			Object input = _viewer.getInput();

			if (input != null)
			{
				if (input instanceof ISystemRegistry)
				{

				}
				else if (input instanceof IHost)
				{
					IHost connection = (IHost) input;
					String connectionID = connection.getAliasName();
					String profileID = connection.getSystemProfileName();
					memento.putString(TAG_TABLE_VIEW_CONNECTION_ID, connectionID);
					memento.putString(TAG_TABLE_VIEW_PROFILE_ID, profileID);
				}
				else
				{
					ISystemViewElementAdapter va = (ISystemViewElementAdapter) ((IAdaptable) input).getAdapter(ISystemViewElementAdapter.class);

					ISubSystem subsystem = va.getSubSystem(input);
					if (subsystem != null)
					{
						ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
						String subsystemID = registry.getAbsoluteNameForSubSystem(subsystem);
						String profileID = subsystem.getHost().getSystemProfileName();
						String connectionID = subsystem.getHost().getAliasName();
						String objectID = va.getAbsoluteName(input);

						memento.putString(TAG_TABLE_VIEW_PROFILE_ID, profileID);
						memento.putString(TAG_TABLE_VIEW_CONNECTION_ID, connectionID);
						memento.putString(TAG_TABLE_VIEW_SUBSYSTEM_ID, subsystemID);

						if (input instanceof ISystemFilterReference)
						{
							memento.putString(TAG_TABLE_VIEW_FILTER_ID, objectID);
							memento.putString(TAG_TABLE_VIEW_OBJECT_ID, null);
						}
						else
							if (input instanceof ISubSystem)
							{
								memento.putString(TAG_TABLE_VIEW_OBJECT_ID, null);
								memento.putString(TAG_TABLE_VIEW_FILTER_ID, null);
							}
							else
							{
								memento.putString(TAG_TABLE_VIEW_OBJECT_ID, objectID);
								memento.putString(TAG_TABLE_VIEW_FILTER_ID, null);
							}
					}
				}


				boolean isConnected = false;
				// don't reconnect
				ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)input).getAdapter(ISystemViewElementAdapter.class);
				if (adapter != null){
					ISubSystem ss = adapter.getSubSystem(input);
					if (ss != null){
						isConnected = ss.isConnected();
					}
				}				
				// new code - as of RSE 3.1
				if (isConnected){ // calling this requires a connect so only do it if already connected
					_viewer.inputChanged(input, input); // make sure the latest widths are stored
				}
				Map cachedColumnWidths = _viewer.getCachedColumnWidths();
				StringBuffer columnWidths = new StringBuffer();
				Iterator keyIter = cachedColumnWidths.keySet().iterator();
				while (keyIter.hasNext()){
					String key = (String)keyIter.next();
					int[] widths = (int[])cachedColumnWidths.get(key);
					
					columnWidths.append(key);
					columnWidths.append('=');
					
					for (int w = 0; w < widths.length; w++){						
						columnWidths.append(widths[w]);
						if (w != widths.length - 1){
							columnWidths.append(',');
						}
					}
					
					// always append this, even with last item
					columnWidths.append(';');
				}
				memento.putString(TAG_TABLE_VIEW_COLUMN_WIDTHS_ID, columnWidths.toString());
								
				memento.putBoolean(TAG_TABLE_VIEW_LOCKED_ID, _isLocked);
			}
		}
	}


//	 -------------------------------
	// ISystemMessageLine interface...
	// -------------------------------
	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage()
	{
		_errorMessage = null;
		sysErrorMessage = null;
		if (_statusLine != null)
			_statusLine.setErrorMessage(_errorMessage);
	}
	/**
	 * Clears the currently displayed message.
	 */
	public void clearMessage()
	{
		_message = null;
		if (_statusLine != null)
			_statusLine.setMessage(_message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage()
	{
		return _errorMessage;
	}
	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage()
	{
		return _message;
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message)
	{
		this._errorMessage = message;
		if (_statusLine != null)
			_statusLine.setErrorMessage(message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage()
	{
		return sysErrorMessage;
	}

	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage message)
	{
		sysErrorMessage = message;
		setErrorMessage(message.getLevelOneText());
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(Throwable exc)
	{
		setErrorMessage(exc.getMessage());
	}

	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message)
	{
		this._message = message;
		if (_statusLine != null)
			_statusLine.setMessage(message);
	}
	/**
	 *If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message)
	{
		setMessage(message.getLevelOneText());
	}

}