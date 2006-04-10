/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvent;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemRemoteChangeListener;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.actions.SystemTablePrintAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemSelectAnythingDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.osgi.framework.Bundle;



/**
 * Comment goes here
 */
public class SystemTableViewPart extends ViewPart implements ISelectionListener, ISelectionChangedListener, 
				ISystemMessageLine, ISystemResourceChangeListener, ISystemRemoteChangeListener, IRSEViewPart
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
			super(SystemResources.ACTION_HISTORY_MOVEFORWARD_LABEL, getEclipseImageDescriptor("elcl16/forward_nav.gif"));

			setTitleToolTip(SystemResources.ACTION_HISTORY_MOVEFORWARD_TOOLTIP);
			setDisabledImageDescriptor(getEclipseImageDescriptor("dlcl16/forward_nav.gif"));
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
			super(SystemResources.ACTION_HISTORY_MOVEBACKWARD_LABEL, getEclipseImageDescriptor("elcl16/backward_nav.gif"));
			setTitleToolTip(SystemResources.ACTION_HISTORY_MOVEBACKWARD_TOOLTIP);
			setDisabledImageDescriptor(getEclipseImageDescriptor("dlcl16/backward_nav.gif"));
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
			super(SystemResources.ACTION_MOVEUP_LABEL, getEclipseImageDescriptor("elcl16/up_nav.gif"));

			setDisabledImageDescriptor(getEclipseImageDescriptor("dlcl16/up_nav.gif"));
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
						boolean enabled = _parent != null;
						setEnabled(enabled);
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
			setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_LOCK_ID));
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
			super(SystemResources.ACTION_REFRESH_LABEL, 
					//SystemPlugin.getDefault().getImageDescriptor(ICON_SYSTEM_REFRESH_ID));
					SystemPlugin.getDefault().getImageDescriptorFromIDE(ISystemIconConstants.ICON_IDE_REFRESH_ID));
		}

		public void run()
		{
			Object inputObject = _viewer.getInput();
			if (inputObject instanceof ISystemContainer)
			{
				((ISystemContainer)inputObject).markStale(true);
			}
			((SystemTableViewProvider) _viewer.getContentProvider()).flushCache();
			ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
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
			Object inputObject = _viewer.getInput();
			if (inputObject == null)
			{
				inputObject = SystemPlugin.getTheSystemRegistry();
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
				_cbName.setText("*");
				_cbName.setToolTipText(SystemResources.RESID_TABLE_POSITIONTO_ENTRY_TOOLTIP);

				this.getShell().setText(SystemResources.RESID_TABLE_POSITIONTO_LABEL);
				setHelp();
				return c;
			}

			private void setHelp()
			{
				setHelp(SystemPlugin.HELPPREFIX + "gnpt0000");
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
				_controls[0].setText("*");
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
					_controls[i + 1].setLayoutData(textData3);
					_controls[i + 1].setText("*");
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
				setHelp(SystemPlugin.HELPPREFIX + "gnss0000");
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

	class RestoreStateRunnable extends UIJob
	{
		private IMemento _memento;
		public RestoreStateRunnable(IMemento memento)
		{
			super("Restore RSE Table");
			_memento = memento;
		}

		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			IMemento memento = _memento;
			String profileId = memento.getString(TAG_TABLE_VIEW_PROFILE_ID);
			String connectionId = memento.getString(TAG_TABLE_VIEW_CONNECTION_ID);
			String subsystemId = memento.getString(TAG_TABLE_VIEW_SUBSYSTEM_ID);
			String filterID = memento.getString(TAG_TABLE_VIEW_FILTER_ID);
			String objectID = memento.getString(TAG_TABLE_VIEW_OBJECT_ID);

			ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
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
				    // TODO why did we use null for a while?
					//input = null;
					input = registry;
				}
			}
			else
			{
				// from the subsystem ID determine the profile, system and subsystem
				ISubSystem subsystem = registry.getSubSystem(subsystemId);

				if (subsystem != null)
				{
					if (filterID == null && objectID == null)
					{
						input = subsystem;
					}
					else
					{

						if (!subsystem.isConnected())
						{
							try
							{
								subsystem.connect();
							}
							catch (Exception e)
							{
								return Status.CANCEL_STATUS;
							}
						}
						if (subsystem.isConnected())
						{

							if (filterID != null)
							{
								try
								{
									input = subsystem.getObjectWithAbsoluteName(filterID);
								}
								catch (Exception e)
								{
								}
							}
							else
							{

								if (objectID != null)
								{

									try
									{
										input = subsystem.getObjectWithAbsoluteName(objectID);
									}
									catch (Exception e)
									{
										return Status.CANCEL_STATUS;
									}
								}
							} // end else
						} // end if (subsystem.isConnected)
					} // end else
				} // end if (subsystem != null)
			} // end else

			if (input != null && input instanceof IAdaptable)
			{
				_mementoInput = (IAdaptable) input;
				if (_mementoInput != null && _viewer != null)
				{
					String columnWidths = memento.getString(TAG_TABLE_VIEW_COLUMN_WIDTHS_ID);
					if (columnWidths != null)
					{
						StringTokenizer tok = new StringTokenizer(columnWidths, ",");
						int[] colWidths = new int[tok.countTokens()];
						int t = 0;
						while (tok.hasMoreTokens())
						{
							String columnStr = tok.nextToken();
							colWidths[t] = Integer.parseInt(columnStr);
							t++;
						}

						_viewer.setLastColumnWidths(colWidths);
					}

					setInput(_mementoInput);
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
	        private SystemTableViewColumnManager _columnManager;
			private IPropertyDescriptor[] _uniqueDescriptors;
			private ArrayList _currentDisplayedDescriptors;
			private ArrayList _availableDescriptors;
			
			private List _availableList;
			private List _displayedList;
			
			private Button _addButton;
			private Button _removeButton;
			private Button _upButton;
			private Button _downButton;
			

			public SelectColumnsDialog(Shell shell, ISystemViewElementAdapter viewAdapter, SystemTableViewColumnManager columnManager)
			{
				super(shell, SystemResources.RESID_TABLE_SELECT_COLUMNS_LABEL);
				_adapter = viewAdapter;
				_columnManager = columnManager;
				_uniqueDescriptors = viewAdapter.getUniquePropertyDescriptors();
				IPropertyDescriptor[] initialDisplayedDescriptors = _columnManager.getVisibleDescriptors(_adapter);
				_currentDisplayedDescriptors = new ArrayList(initialDisplayedDescriptors.length);
				for (int i = 0; i < initialDisplayedDescriptors.length;i++)
				{
					if (!_currentDisplayedDescriptors.contains(initialDisplayedDescriptors[i]))
				    _currentDisplayedDescriptors.add(initialDisplayedDescriptors[i]);
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
			    }
			    else if (source == _removeButton)
			    {
			        int[] toAdd = _displayedList.getSelectionIndices();
			        removeFromDisplay(toAdd);	   
			    }
			    else if (source == _upButton)
			    {
			        int index = _displayedList.getSelectionIndex();
			        moveUp(index);
			        _displayedList.select(index - 1);
			    }
			    else if (source == _downButton)
			    {
			        int index = _displayedList.getSelectionIndex();
			        moveDown(index);
			        _displayedList.select(index + 1);
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
				
				Label label = SystemWidgetHelpers.createLabel(main, SystemResources.RESID_TABLE_SELECT_COLUMNS_DESCRIPTION_LABEL);
				
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
				setHelp(SystemPlugin.HELPPREFIX + "gntc0000");
			}
		}
	    
		public SelectColumnsAction()
		{
			super(SystemResources.ACTION_SELECTCOLUMNS_LABEL, null);
			setToolTipText(SystemResources.ACTION_SELECTCOLUMNS_TOOLTIP);
			setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTER_ID));
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
		    SystemTableViewColumnManager mgr = _viewer.getColumnManager();		    
		    ISystemViewElementAdapter adapter = _viewer.getAdapterForContents();
		    SelectColumnsDialog dlg = new SelectColumnsDialog(getShell(), adapter, mgr);
		    if (dlg.open() == Window.OK)
		    {
		        mgr.setCustomDescriptors(adapter, dlg.getDisplayedColumns());
		        _viewer.computeLayout(true);
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

	private IMemento _memento = null;
	private IAdaptable _mementoInput = null;
	private Object _lastSelection = null;

	private boolean _isLocked = false;

	//  for ISystemMessageLine
	private String _message, _errorMessage;
	private SystemMessage sysErrorMessage;
	private IStatusLineManager _statusLine = null;
	
	// constants			
	public static final String ID = "org.eclipse.rse.ui.view.systemTableView"; // matches id in plugin.xml, view tag	

	// Restore memento tags
	public static final String TAG_TABLE_VIEW_PROFILE_ID = "tableViewProfileID";
	public static final String TAG_TABLE_VIEW_CONNECTION_ID = "tableViewConnectionID";
	public static final String TAG_TABLE_VIEW_SUBSYSTEM_ID = "tableViewSubsystemID";
	public static final String TAG_TABLE_VIEW_OBJECT_ID = "tableViewObjectID";
	public static final String TAG_TABLE_VIEW_FILTER_ID = "tableViewFilterID";

	// Subset memento tags
	public static final String TAG_TABLE_VIEW_SUBSET = "subset";

	// layout memento tags
	public static final String TAG_TABLE_VIEW_COLUMN_WIDTHS_ID = "columnWidths";

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
	            setInput(SystemPlugin.getTheSystemRegistry());
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
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		_viewer = new SystemTableView(table, this);
		_viewer.setWorkbenchPart(this);

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
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		Clipboard clipboard = registry.getSystemClipboard();

		CellEditorActionHandler editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());

		_copyAction = new SystemCopyToClipboardAction(_viewer.getShell(), clipboard);
		_pasteAction = new SystemPasteFromClipboardAction(_viewer.getShell(), clipboard);
		_deleteAction = new SystemCommonDeleteAction(_viewer.getShell(), _viewer);

		editorActionHandler.setCopyAction(_copyAction);
		editorActionHandler.setPasteAction(_pasteAction);
		editorActionHandler.setDeleteAction(_deleteAction);
		editorActionHandler.setSelectAllAction(new SelectAllAction());

		registry.addSystemResourceChangeListener(this);
		registry.addSystemRemoteChangeListener(this);

		SystemWidgetHelpers.setHelp(_viewer.getControl(), SystemPlugin.HELPPREFIX + "sysd0000");
		
		getSite().registerContextMenu(_viewer.getContextMenuManager(), _viewer);
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
						_lastSelection = first;
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

		SystemPlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
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
			if (adapter.hasChildren(element))
			{
				setInput((IAdaptable) element);
			}
			else
			{
				alreadyHandled = adapter.handleDoubleClick(element);
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
			URL installURL = bundle.getEntry("/");
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
		menuManager.add(new Separator("View"));
		menuManager.add(_selectInputAction);
		menuManager.add(new Separator("Filter"));
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
	
		
		toolBarManager.add(new Separator("Navigate"));
		// only support history when we're locked
		if (_isLocked)
		{
			toolBarManager.add(_backwardAction);
			toolBarManager.add(_forwardAction);
		}

		toolBarManager.add(_upAction);

		toolBarManager.add(new Separator("View"));
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
		setInput(object, null, _isLocked);

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
	        setContentDescription("");
	    }
	    else
	    {
		ISystemViewElementAdapter va = (ISystemViewElementAdapter) object.getAdapter(ISystemViewElementAdapter.class);
		if (va != null)
		{
			String type = va.getType(object);
			String name = va.getName(object);
			//setPartName(type + " " + name);
			
			setContentDescription(type + " "+ name);

			//SystemTableViewProvider provider = (SystemTableViewProvider) _viewer.getContentProvider();
			//setTitleImage(provider.getImage(object));
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
		{
			if (child == input)
			{
				setTitle((IAdaptable) child);
			}
		}
		break;
	 case ISystemResourceChangeEvents.EVENT_DELETE:   	    	  
  	  case ISystemResourceChangeEvents.EVENT_DELETE_MANY:
  	  	{
  	      if (child instanceof ISystemFilterReference)
  	      {
  	          
  	          if (child == input)
  	          {
  	              removeFromHistory(input);
	    	  }
  	      }
  	  	}
  	      break;  
  	      default:
  	          break;
		}
	}
	
	protected void removeFromHistory(Object c)
	{
	    // if the object is in history, remove it since it's been deleted
	    for (int i = 0; i < _browseHistory.size(); i++)
	    {
	        HistoryItem hist = (HistoryItem)_browseHistory.get(i);
	        if (hist.getObject() == c)
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
	            if (hist == _currentItem)
	            {
	                if (_browseHistory.size() > 0)
	                {
	                    _currentItem = (HistoryItem)_browseHistory.get(_browsePosition);
	                    setInput(_currentItem.getObject(), null, false);
	                }
	                else
	                {
	                    _currentItem = null;
	                    setInput((IAdaptable)null, null, false);
	                }
	                
	               
	            }
	        }
	    }
	}
	
	/**
	 * This is the method in your class that will be called when a remote resource
	 *  changes. You will be called after the resource is changed.
	 * @see org.eclipse.rse.model.ISystemRemoteChangeEvent
	 */
	public void systemRemoteResourceChanged(ISystemRemoteChangeEvent event)
	{
		int eventType = event.getEventType();
		Object remoteResourceParent = event.getResourceParent();
		Object remoteResource = event.getResource();
	
		Vector remoteResourceNames = null;
		if (remoteResource instanceof Vector)
		{
			remoteResourceNames = (Vector) remoteResource;
			remoteResource = remoteResourceNames.elementAt(0);
		}

		Object child = event.getResource();
		
		
		Object input = _viewer.getInput();
		if (input == child || child instanceof Vector)
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
				    	if (child instanceof Vector)
				    	{
				    	    Vector vec = (Vector)child;
				    	    for (int v = 0; v < vec.size(); v++)
				    	    {
				    	        Object c = vec.get(v);
				    	        
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
		rsr.setRule(SystemPlugin.getTheSystemRegistry());
		rsr.schedule();
		_memento = null;
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

		if (memento != null && SystemPreferencesManager.getPreferencesManager().getRememberState())
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

		if (!SystemPreferencesManager.getPreferencesManager().getRememberState())
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
						ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
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

				Table table = _viewer.getTable();
				if (table != null && !table.isDisposed())
				{
					String columnWidths = new String();
					TableColumn[] columns = table.getColumns();
					for (int i = 0; i < columns.length; i++)
					{
						TableColumn column = columns[i];
						int width = column.getWidth();
						if (i == columns.length - 1)
						{
							columnWidths += width;
						}
						else
						{
							columnWidths += width + ",";
						}
					}
					memento.putString(TAG_TABLE_VIEW_COLUMN_WIDTHS_ID, columnWidths);
				}
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