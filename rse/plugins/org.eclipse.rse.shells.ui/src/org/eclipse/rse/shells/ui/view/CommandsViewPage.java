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

package org.eclipse.rse.shells.ui.view;

import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemThemeConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
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
public class CommandsViewPage implements SelectionListener, ISystemThemeConstants, IPropertyChangeListener, ISelectionChangedListener, 
FocusListener
{

	private Listener _keyListener = new Listener()
	{
		private boolean checkState(Event event)
		{
			if (event.character == '\r')
			{
				_inputEntry.setInCodeAssist(false);
			}
			
			return !_inputEntry.isInCodeAssist();
		}

		public void handleEvent(Event e)
		{
			if (checkState(e))
			{
				
				if (e.character == '\r') // "enter" key
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
					if (((int)e.character) == 3)
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
	private Group _tabFolderPage;

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

			Display display = _viewer.getShell().getDisplay();
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
		_viewer.setWorkbenchPart(_viewPart);

		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				handleDoubleClick(event);
			}
		});

		
		SystemWidgetHelpers.setHelp(_viewer.getControl(), RSEUIPlugin.HELPPREFIX + "ucmd0000");

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

		_inputEntry = new SystemCommandEditor(_viewPart.getViewSite(), _inputContainer, SWT.SINGLE | SWT.BORDER, 50, _entryViewerConfiguration, "", SystemResources.ACTION_CONTENT_ASSIST);
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
	    Display display = getViewer().getControl().getDisplay();
	    IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
		Color bg = mgr.getCurrentTheme().getColorRegistry().get(REMOTE_COMMANDS_VIEW_BG_COLOR);
		Color fg = mgr.getCurrentTheme().getColorRegistry().get(REMOTE_COMMANDS_VIEW_FG_COLOR);
		Font fFont =mgr.getCurrentTheme().getFontRegistry().get(REMOTE_COMMANDS_VIEW_FONT);
	
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
		boolean alreadyHandled = false;
		if (adapter != null)
		{
			if (adapter.hasChildren(element))
			{
				// special case for folders
				if (element instanceof IRemoteFile)
				{
					IRemoteFile folder = (IRemoteFile) element;
					if (folder.isDirectory())
					{
						String path = folder.getAbsolutePath();
						ISubSystem cmdSubSystem = adapter.getSubSystem(element);

						String cdCmd = "cd " + "\"" + path + "\"";
						if (cmdSubSystem.getHost().getSystemType().equals("Local")
							&& System.getProperty("os.name").toLowerCase().startsWith("win")
							|| cmdSubSystem.getHost().getSystemType().equals("Windows"))
						{
							cdCmd = "cd /d " + path;
						}
						sendInput(cdCmd);
					}
				}
			}
			else
			{
				alreadyHandled = adapter.handleDoubleClick(element);
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
				commandSubSystem.sendCommandToShell(inputStr, _viewer.getShell(), remoteCommand);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		_inputEntry.getTextWidget().setText("");
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
				commandSubSystem.sendCommandToShell("#break", _viewer.getShell(), remoteCommand);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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
		if (object instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell command = (IRemoteCommandShell) object;
			String title = command.getType();

			SystemMessage msg = null;
			if (!command.isActive())
			{
				msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_FINISHED);
			}
			else
			{
				msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_RUNNING);
			}

			msg.makeSubstitution(title);
			_title = msg.getLevelOneText();
			_tabFolderPage.setText(_title);
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
			_inputEntry.getTextWidget().setText("");
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