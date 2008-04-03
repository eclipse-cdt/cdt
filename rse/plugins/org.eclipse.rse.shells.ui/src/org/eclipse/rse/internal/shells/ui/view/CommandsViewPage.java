/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Fix 154874 - handle files with space or $ in the name 
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [180562] dont implement ISystemThemeConstants 
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [212940] Duplicate Help Context Identifiers
 * David McKnight (IBM) - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Radoslav Gerganov (ProSyst) - [181563] Fix hardcoded Ctrl+Space for remote shell content assist
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.internal.shells.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.shells.ui.ShellResources;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.shells.ui.view.CommandEntryViewerConfiguration;
import org.eclipse.rse.shells.ui.view.SystemCommandEditor;
import org.eclipse.rse.shells.ui.view.SystemCommandsView;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.model.ISystemOutputRemoteTypes;
import org.eclipse.rse.subsystems.shells.core.model.RemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemThemeConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.themes.IThemeManager;


/**
 * Class for a remote shell session on a connection
 */
public class CommandsViewPage implements SelectionListener, IPropertyChangeListener, ISelectionChangedListener, 
FocusListener
{

	private Listener _keyListener = new Listener()
	{
		private boolean checkState(Event event)
		{
			boolean isEnabled = !_inputEntry.isInCodeAssist();
			if (event.character == '\r')
			{
				_inputEntry.setInCodeAssist(false);
			}
			
			return isEnabled;
		}

		public void handleEvent(Event e)
		{
			if (checkState(e))
			{
				
				if (e.character == '\r' && e.stateMask!=262144) // "enter" key, but not Ctrl+M (bug 160786)
				{
					sendInput();
				}
				else if (e.keyCode == 13)
				{
					sendInput();
				}
				else if (e.keyCode == 16777217) // "up" arrow
				{
					handleUp();
				}
				else if (e.keyCode == 16777218) // "down" arrow
				{
					handleDown();
				}
				else if (e.stateMask == 262144)
				{
					if (e.character == 3)
					{
					    if (_inputEntry.getSelectedText().length() == 0)
					    {
					        sendBreak();
					    }
					    else
					    {
					        _inputEntry.doOperation(ITextOperationTarget.COPY);
					    }
					}
				}
			}
		}
	};
	
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
		    SystemCommandsView view = _viewer;
		    view.getTable().selectAll();
		    view.setSelection(view.getSelection());
		}
	}
	

	private CommandEntryViewerConfiguration _entryViewerConfiguration;

	private SystemCommandsView _viewer;

	private SystemCommandEditor _inputEntry;

	private Button _upButton;
	private Button _downButton;
	private Composite _inputContainer;
	private Composite _tabFolderPage;

	private int _commandHistoryOffset;
	private SystemCommandsViewPart _viewPart;

	private String _title;

	private SystemCopyToClipboardAction _copyAction;
	private SystemPasteFromClipboardAction _pasteAction;
	private SelectAllAction _selectAllAction;
	private IActionBars _actionBars;
	
	public CommandsViewPage(SystemCommandsViewPart viewPart)
	{
		_commandHistoryOffset = 0;
		_entryViewerConfiguration = new CommandEntryViewerConfiguration();
		_viewPart = viewPart;
		_actionBars = _viewPart.getViewSite().getActionBars();
	}

	public Composite createTabFolderPage(CTabFolder tabFolder, CellEditorActionHandler editorActionHandler)
	{ 
		_tabFolderPage = new Composite(tabFolder, SWT.NULL);
	
		 Font font = tabFolder.getFont();		
		 _tabFolderPage.setFont(font);
		 // dummy title so that sizings work
		 // fix for 138311
		 // String dummyTitle = ShellResources.RESID_SHELLS_COMMAND_SHELL_LABEL;
		 
//		 _tabFolderPage.setText(dummyTitle);
		GridLayout gridLayout = new GridLayout();
		_tabFolderPage.setLayout(gridLayout);
		createControl(_tabFolderPage);
		
		// global actions
		Clipboard clipboard = RSEUIPlugin.getTheSystemRegistryUI().getSystemClipboard();
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
		_inputEntry.getControl().addFocusListener(this);

		return _tabFolderPage;
	}

	public void setFocus()
	{
		_inputEntry.getTextWidget().setFocus();

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
	
	
	

	public void setEnabled(boolean flag)
	{
		if (!flag)
		{
			_inputEntry.getTextWidget().setEnabled(flag);
			_inputEntry.setEditable(flag);

			Table table = _viewer.getTable();

			//Display display = _viewer.getShell().getDisplay();
			Color bgcolour = _tabFolderPage.getBackground();

			table.setBackground(bgcolour);
			_inputEntry.getTextWidget().setBackground(bgcolour);
		}
	}

	public void createControl(Composite parent)
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);

		// create table portion
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		_viewer = new SystemCommandsView(table, _viewPart);

		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				handleDoubleClick(event);
			}
		});

		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		table.setLayoutData(gridData);

		_inputContainer = new Composite(parent, SWT.NONE);
		GridLayout ilayout = new GridLayout();
		ilayout.numColumns = 4;

		Label label = new Label(_inputContainer, SWT.NONE);
		label.setText(ShellResources.RESID_COMMANDSVIEW_COMMAND_LABEL);

		_inputEntry = new SystemCommandEditor(_viewPart.getViewSite(), _inputContainer, SWT.SINGLE | SWT.BORDER, 50, _entryViewerConfiguration, "", ShellResources.ACTION_CONTENT_ASSIST); //$NON-NLS-1$
		_inputEntry.getTextWidget().setToolTipText(ShellResources.RESID_COMMANDSVIEW_COMMAND_TOOLTIP);
		
			
		_upButton = new Button(_inputContainer, SWT.ARROW | SWT.UP);
		_upButton.addSelectionListener(this);
		_upButton.setToolTipText(ShellResources.RESID_COMMANDSVIEW_PREVIOUS_TOOLTIP);

		_downButton = new Button(_inputContainer, SWT.ARROW | SWT.DOWN);
		_downButton.addSelectionListener(this);
		_downButton.setToolTipText(ShellResources.RESID_COMMANDSVIEW_PREVIOUS_TOOLTIP);

		GridData idata = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
		idata.heightHint = 22;
		_inputEntry.getTextWidget().setLayoutData(idata);
		_inputEntry.getTextWidget().addListener(SWT.KeyUp, _keyListener);

		enableEntry(false);

		GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL);
		_inputContainer.setLayout(ilayout);
		_inputContainer.setLayoutData(gridData1);
		
		updateTheme();
		IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
		mgr.addPropertyChangeListener(this);
		


	}

	public void propertyChange(PropertyChangeEvent e)
	{
	    updateTheme();
	}
	public void updateTheme()
	{
	    //Display display = getViewer().getControl().getDisplay();
	    IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
		Color bg = mgr.getCurrentTheme().getColorRegistry().get(ISystemThemeConstants.REMOTE_COMMANDS_VIEW_BG_COLOR);
		Color fg = mgr.getCurrentTheme().getColorRegistry().get(ISystemThemeConstants.REMOTE_COMMANDS_VIEW_FG_COLOR);
		Font fFont =mgr.getCurrentTheme().getFontRegistry().get(ISystemThemeConstants.REMOTE_COMMANDS_VIEW_FONT);
	
		_inputEntry.getControl().setBackground(bg);
		_inputEntry.getControl().setForeground(fg);
		_inputEntry.getControl().setFont(fFont);	 
	}
	
	private void handleDoubleClick(DoubleClickEvent event)
	{
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		if (element == null)
			return;

		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) element).getAdapter(ISystemViewElementAdapter.class);
		//boolean alreadyHandled = false;
		if (adapter != null)
		{
			if (adapter.hasChildren((IAdaptable)element))
			{
				// special case for folders
				if (element instanceof IRemoteFile)
				{
					IRemoteFile folder = (IRemoteFile) element;
					if (folder.isDirectory())
					{
						String path = folder.getAbsolutePath();
						ISubSystem cmdSubSystem = adapter.getSubSystem(element);

						String cdCmd = "cd " + PathUtility.enQuoteUnix(path); //$NON-NLS-1$
						if (cmdSubSystem.getHost().getSystemType().isWindows())
						{
							cdCmd = "cd /d \"" + path + '\"'; //$NON-NLS-1$
						}
						sendInput(cdCmd);
					}
				}
			}
			else if (element instanceof RemoteOutput)
			{
				RemoteOutput out = (RemoteOutput)element;
				if (out.getType().equals(ISystemOutputRemoteTypes.TYPE_DIRECTORY))
				{
					String path = out.getAbsolutePath();
					ISubSystem cmdSubSystem = adapter.getSubSystem(element);

					String cdCmd = "cd " + PathUtility.enQuoteUnix(path);  //$NON-NLS-1$
					if (cmdSubSystem.getHost().getSystemType().isWindows())
					{
						cdCmd = "cd /d \"" + path + '\"'; //$NON-NLS-1$
					}
					sendInput(cdCmd);
				}
				else
				{
					/*alreadyHandled =*/ adapter.handleDoubleClick(element);
				}
			}
			else
			{
				/*alreadyHandled =*/ adapter.handleDoubleClick(element);
			}
		}
	}

	public void dispose()
	{
		IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
		mgr.removePropertyChangeListener(this);

		_inputEntry.getTextWidget().removeListener(SWT.KeyUp, _keyListener);
		_inputEntry.getTextWidget().dispose();
		_inputContainer.dispose();
		_viewer.dispose();
		_tabFolderPage.dispose();
	}

	private void enableEntry(boolean flag)
	{
		if (!_inputEntry.getTextWidget().isDisposed())
			_inputEntry.getTextWidget().setEnabled(flag);
	}

	public void sendInput()
	{
		String inputStr = _inputEntry.getTextWidget().getText();
		sendInput(inputStr);
	}

	public void sendInput(String inputStr)
	{
		Object input = _viewer.getInput();
		if (input instanceof IRemoteCommandShell)
		{
			_commandHistoryOffset = getCommandHistory().length + 1;
			IRemoteCommandShell remoteCommand = (IRemoteCommandShell) input;
			IRemoteCmdSubSystem commandSubSystem = remoteCommand.getCommandSubSystem();
			try
			{
				commandSubSystem.sendCommandToShell(inputStr, remoteCommand, new NullProgressMonitor());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		_inputEntry.getTextWidget().setText(""); //$NON-NLS-1$
		_inputEntry.getTextWidget().setFocus();
	}

	public void sendBreak()
	{
		Object input = _viewer.getInput();
		if (input instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell remoteCommand = (IRemoteCommandShell) input;
			IRemoteCmdSubSystem commandSubSystem = remoteCommand.getCommandSubSystem();
			try
			{
				commandSubSystem.sendCommandToShell("#break", remoteCommand, new NullProgressMonitor()); //$NON-NLS-1$
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public Object getInput()
	{
		if (_viewer != null)
			return _viewer.getInput();
		return null;
	}

	public void setInput(IAdaptable object)
	{
		setInput(object, true);
		updateTitle(object);
	}

	public void updateTitle(IAdaptable object)
	{
		if (object instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell command = (IRemoteCommandShell) object;
			String title = command.getType();

			String msgTxt = null;
			if (!command.isActive())
			{
				msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_FINISHED, title);
			}
			else
			{
				msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_RUNNING, title);
			}

			_title = msgTxt;

		}
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
			_entryViewerConfiguration.setRemoteCommand((IRemoteCommandShell) object);
			//_inputEntry.getTextWidget().setFocus();
		}
	}

	public void clearInput()
	{
		if (_viewer != null)
		{
			_viewer.setInput(null);
			_entryViewerConfiguration.setRemoteCommand(null);
		}
	}

	public SystemCommandsView getViewer()
	{
		return _viewer;
	}
	
	SystemCommandEditor getEditor() {
	  return _inputEntry;
	}

	public void updateOutput()
	{
		if (_viewer != null)
		{
			((SystemTableViewProvider) _viewer.getContentProvider()).flushCache();
			_viewer.updateChildren();
			_commandHistoryOffset = getCommandHistory().length;
		}
	}

	public void updateActionStates()
	{
		Object input = _viewer.getInput();
		if (input != null)
		{
			if (input instanceof IRemoteCommandShell)
			{
				IRemoteCommandShell cmdInput = (IRemoteCommandShell) input;
				if (cmdInput.isActive())
				{
					enableEntry(true);
				}
				else
				{
					enableEntry(false);
				}
			}
			else
			{
				enableEntry(false);
			}
		}
		else
		{
			enableEntry(false);
		}

	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e)
	{
		Widget source = e.widget;

		if (source == _upButton)
		{
			handleUp();
		}
		else if (source == _downButton)
		{
			handleDown();
		}
	}

	private String[] getCommandHistory()
	{
		Object input = _viewer.getInput();
		if (input != null && input instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell command = (IRemoteCommandShell) input;
			return command.getHistory();
		}
		return null;
	}

	private void handleUp()
	{
		String[] history = getCommandHistory();
		_commandHistoryOffset--;
		if (_commandHistoryOffset > 0)
		{
			if (history.length > _commandHistoryOffset)
			{
				String lastCommand = history[_commandHistoryOffset];
				_inputEntry.getTextWidget().setText(lastCommand);
				_inputEntry.getTextWidget().setCaretOffset(_inputEntry.getTextWidget().getCharCount());
			}
		}
		else
		{
			_commandHistoryOffset = 0;

			if (history.length > 0)
			{
				_inputEntry.getTextWidget().setText(history[0]);
				_inputEntry.getTextWidget().setCaretOffset(_inputEntry.getTextWidget().getCharCount());
			}
		}
	}

	private void handleDown()
	{
		_commandHistoryOffset++;
		if (_commandHistoryOffset >= getCommandHistory().length)
		{
			_commandHistoryOffset = getCommandHistory().length;
			_inputEntry.getTextWidget().setText(""); //$NON-NLS-1$
		}
		else
		{
			String[] history = getCommandHistory();
			if (history.length > 0)
			{
				String lastCommand = history[_commandHistoryOffset];
				_inputEntry.getTextWidget().setText(lastCommand);
				_inputEntry.getTextWidget().setCaretOffset(_inputEntry.getTextWidget().getCharCount());
			}
		}
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
	        else if (arg0.widget == _inputEntry.getControl())
	        {
		        _inputEntry.setActionHandlers();
	        }
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
     */
    public void focusLost(FocusEvent arg0)
    {        
        
    }
}