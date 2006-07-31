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

package org.eclipse.rse.ui.view.monitor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemThemeConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableTreeView;
import org.eclipse.rse.ui.view.SystemTableTreeViewProvider;
import org.eclipse.rse.ui.widgets.ISystemCollapsableSectionListener;
import org.eclipse.rse.ui.widgets.SystemCollapsableSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;



/**
 * Class for a remote shell session on a connection
 */
public class MonitorViewPage implements SelectionListener, ISystemThemeConstants, IPropertyChangeListener, ISelectionChangedListener, Listener,
FocusListener
{
	private static SystemMessage _queryMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_QUERY_PROGRESS);
	
	class PollingThread extends Thread
	{
		private boolean _querying = false;
		private ISystemViewElementAdapter _adapter;
		private Object _inputObject;
		private SystemTableTreeView _viewer;
		
			public PollingThread()
		{	
			_viewer = getViewer();
			_inputObject = _viewer.getInput();
			_adapter = (ISystemViewElementAdapter)((IAdaptable)_inputObject).getAdapter(ISystemViewElementAdapter.class);
		}
		
		public void run()
		{
			while (isPollingEnabled())
			{
				int interval = getPollingInterval() * 1000;
				try
				{
					Thread.sleep(interval);
					doQuery();
				//	while (_querying)
				//	{
				//		Thread.sleep(100);
				//	}
					doRedraw();
				}
				catch (InterruptedException e)
				{
					
				}
				catch (Exception e)
				{				
					e.printStackTrace();
				}						
			}			
		}
		
		protected void doQuery()
		{

			Display display = Display.getDefault();
			if (display != null && !_querying)
			{
				_querying= true;
				if (_inputObject instanceof ISystemContainer)
				{
					((ISystemContainer)_inputObject).markStale(true);
				}
				ISubSystem ss = _adapter.getSubSystem(_inputObject);
				if (!ss.isConnected())
					return;
				
				String name = _adapter.getName(_inputObject);
				_queryMessage.makeSubstitution(name);
				String txt = _queryMessage.getLevelOneText();
				   Job job = new Job(txt) 
				   {
					    public IStatus run(IProgressMonitor monitor) 
					    {			
						    Object[] children = _adapter.getChildren(monitor, _inputObject);
		   					if (children != null)
		   					{
		   						SystemTableTreeViewProvider provider = (SystemTableTreeViewProvider)_viewer.getContentProvider();
		   						if (provider!=null) {
		   							//bug 150924: provider can be lost when disconnecting while this job runs
									provider.setCache(children);
		   						}
								
		   					}

		   					_querying = false;
		   					return Status.OK_STATUS;
					    }
				   };
				   
				   job.schedule();		
				   
				   try
				   {
					   job.wait();
				   }
				   catch (Exception e)
				   {
					   
				   }
			}
	
		}
		
		protected void doRedraw()
		{
			Display display = Display.getDefault();
			if (display != null)
			{
				display.asyncExec(
						new Runnable()
						{
							public void run() 
							{
								ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
								registry.fireEvent(new SystemResourceChangeEvent(_inputObject, ISystemResourceChangeEvents.EVENT_REFRESH, _inputObject));
								//getViewer().refresh();
							}
						});
			}
		}
	}

	class SelectAllAction extends Action
	{
		public SelectAllAction()
		{
			super(SystemResources.ACTION_SELECT_ALL_LABEL, null);
			setToolTipText(SystemResources.ACTION_SELECT_ALL_TOOLTIP);
		}
		
		public void checkEnabledState()
		{
		    setEnabled(true);
		}

		public void run()
		{
		    SystemTableTreeView view = _viewer;
		    view.getTree().selectAll();
		    view.setSelection(view.getSelection());
		}
	}
	

	private SystemTableTreeView _viewer;
	
	private boolean _isPolling = false;
	private int _pollingInterval;
	
	private Group _tabFolderPage;
	private Button _pollCheckbox;
	private Scale _scale;
	private Text  _scaleValue;

	private PollingThread _pollingThread;
	
	private SystemMonitorViewPart _viewPart;

	private String _title;

	private SystemCopyToClipboardAction _copyAction;
	private SystemPasteFromClipboardAction _pasteAction;
	private SelectAllAction _selectAllAction;
	private IActionBars _actionBars;
	
	public MonitorViewPage(SystemMonitorViewPart viewPart)
	{
		_viewPart = viewPart;
		_actionBars = _viewPart.getViewSite().getActionBars();
	}

	
	
	public Composite createTabFolderPage(CTabFolder tabFolder, CellEditorActionHandler editorActionHandler)
	{ 
		_tabFolderPage = new Group(tabFolder, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		_tabFolderPage.setLayout(gridLayout);

		createControl(_tabFolderPage);

		
		
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			
		// global actions
		Clipboard clipboard = registry.getSystemClipboard();
		_copyAction = new SystemCopyToClipboardAction(_viewer.getShell(), clipboard);
		_copyAction.setEnabled(false);
		
		_pasteAction = new SystemPasteFromClipboardAction(_viewer.getShell(), clipboard);
		_pasteAction.setEnabled(false);

		editorActionHandler.setCopyAction(_copyAction);
		editorActionHandler.setPasteAction(_pasteAction);

		_selectAllAction = new SelectAllAction();
		_selectAllAction.setEnabled(false);
		editorActionHandler.setSelectAllAction(_selectAllAction);
		
		
		_viewer.addSelectionChangedListener(this);
		_viewer.getControl().addFocusListener(this);

		return _tabFolderPage;
	}

	public void setFocus()
	{
		_viewPart.getSite().setSelectionProvider(_viewer);
	}
	
	public IActionBars getActionBars()
	{
	    return _actionBars;
	}
	
	public void selectionChanged(SelectionChangedEvent e)
	{
		IStructuredSelection sel = (IStructuredSelection) e.getSelection();
		_copyAction.setEnabled(_copyAction.updateSelection(sel));
		_pasteAction.setEnabled(_pasteAction.updateSelection(sel));
		_selectAllAction.setEnabled(true);
		
		//setActionHandlers();
	}
	
	public int getPollingInterval()
	{
		return _pollingInterval;
	}
	
	public boolean isPollingEnabled()
	{
		if (_isPolling)
		{
			return true;
		}
		return false;
	}
	

	public void setEnabled(boolean flag)
	{
		if (!flag)
		{
			Tree tree = _viewer.getTree();

			Display display = _viewer.getShell().getDisplay();
			Color bgcolour = _tabFolderPage.getBackground();

			tree.setBackground(bgcolour);
		}
	}
	
	protected void createPollControls(Composite parent)
	{

		SystemCollapsableSection collapsable = new SystemCollapsableSection(parent);	
		collapsable.setText(SystemResources.RESID_MONITOR_POLL_CONFIGURE_POLLING_LABEL);
		collapsable.setToolTips(SystemResources.RESID_MONITOR_POLL_CONFIGURE_POLLING_COLLAPSE_TOOLTIP,
						SystemResources.RESID_MONITOR_POLL_CONFIGURE_POLLING_EXPAND_TOOLTIP 
								);
		
		Composite inputContainer = collapsable.getPageComposite();
		
		
		_pollCheckbox = SystemWidgetHelpers.createCheckBox(inputContainer, this, SystemResources.RESID_MONITOR_POLL_LABEL, SystemResources.RESID_MONITOR_POLL_TOOLTIP);
		GridData pg = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		_pollCheckbox.setLayoutData(pg);
		
		_pollingInterval = 100;
		Label label = SystemWidgetHelpers.createLabel(inputContainer, SystemResources.RESID_MONITOR_POLL_INTERVAL_LABEL);

		_scale = new Scale(inputContainer, SWT.NULL);
		_scale.setMaximum(200);
		_scale.setMinimum(5);
		_scale.setSelection(_pollingInterval);	
	
		_scale.addSelectionListener(
				new SelectionListener()
				{
				
					public void widgetDefaultSelected(SelectionEvent e)
					{
						widgetSelected(e);
					}
				
					public void widgetSelected(SelectionEvent e)
					{						
						_pollingInterval = _scale.getSelection();						
						_scaleValue.setText(_pollingInterval + "s");
						
						if (_pollingThread != null)
							_pollingThread.interrupt();
					}
				
				});
				
		_scale.setToolTipText(SystemResources.RESID_MONITOR_POLL_INTERVAL_TOOLTIP);
		GridData sd = new GridData(GridData.FILL_HORIZONTAL);
		_scale.setLayoutData(sd);
		
		_scaleValue = SystemWidgetHelpers.createReadonlyTextField(inputContainer);
		_scaleValue.setTextLimit(5);
		GridData scgd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		_scaleValue.setLayoutData(scgd);
		_scaleValue.setText(_pollingInterval + "s");
				
	
	
		GridLayout ilayout = new GridLayout();
		ilayout.numColumns = 4;GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL);
		inputContainer.setLayout(ilayout);
		inputContainer.setLayoutData(gridData1);
	
		
		// defaults
		_scale.setEnabled(_isPolling);
		_scaleValue.setEnabled(_isPolling);		
						
		collapsable.addCollapseListener(new CollapsableListener(inputContainer));
	}
	
	class CollapsableListener implements ISystemCollapsableSectionListener
	{
		Composite _child;
		public CollapsableListener(Composite child)
		{
			_child = child;
		}
		
		public void sectionCollapsed(boolean collapsed)
		{
			//System.out.println("collapsed");
		}		
	}

	public void createControl(Composite parent)
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);

		// create table portion
		//Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		//_viewer = new SystemTableView(table, _viewPart);
		
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		_viewer = new SystemTableTreeView(tree, _viewPart);
		_viewer.setWorkbenchPart(_viewPart);

		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				handleDoubleClick(event);
			}
		});

		
		SystemWidgetHelpers.setHelp(_viewer.getControl(), RSEUIPlugin.HELPPREFIX + "ucmd0000");

		//TableLayout layout = new TableLayout();
		//tree.setLayout(layout);
		//tree.setLayout(new GridLayout())
		tree.setHeaderVisible(false);
		tree.setLinesVisible(false);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		tree.setLayoutData(gridData);

		createPollControls(_tabFolderPage);		
	}

	public void propertyChange(PropertyChangeEvent e)
	{
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
		}
	}

	public void dispose()
	{
		_viewer.dispose();
		_tabFolderPage.dispose();
	}




	public Object getInput()
	{
		return _viewer.getInput();
	}

	public void setInput(IAdaptable object)
	{
		setInput(object, true);
		updateTitle(object);
	}

	public void updateTitle(IAdaptable object)
	{
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)object.getAdapter(ISystemViewElementAdapter.class);
			
			String title = adapter.getText(object);
			_tabFolderPage.setText(title);
	}

	public String getTitle()
	{
		return _title;
	}

	public void setInput(IAdaptable object, boolean updateHistory)
	{
		if (_viewer != null && object != null)
		{
			_viewer.setInput(object);
		}
	}

	public void clearInput()
	{
		if (_viewer != null)
		{
			_viewer.setInput(null);
		}
	}

	public SystemTableTreeView getViewer()
	{
		return _viewer;
	}



	public void updateActionStates()
	{
		Object input = _viewer.getInput();
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e)
	{
	}





    /* (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
     */
    public void focusGained(FocusEvent arg0)
    {
    	IActionBars actionBars = getActionBars();
		if (actionBars != null)
		{
	        if (arg0.widget == _viewer.getControl())
	        {
		        actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, _copyAction);
				actionBars.setGlobalActionHandler(ITextEditorActionConstants.PASTE, _pasteAction);
				actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, _selectAllAction);
				actionBars.updateActionBars();
	   
	        }
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
     */
    public void focusLost(FocusEvent arg0)
    {        
        
    }

	public void handleEvent(Event event)
	{
		Widget w = event.widget;
		if (w == _pollCheckbox)
		{
			boolean wasPolling = _isPolling;
			_isPolling = _pollCheckbox.getSelection();
			_scale.setEnabled(_isPolling);			
			_scaleValue.setEnabled(_isPolling);
			if (wasPolling != _isPolling && _isPolling)
			{
				_pollingThread = new PollingThread();
				_pollingThread.start();
			}
		}
	}
}