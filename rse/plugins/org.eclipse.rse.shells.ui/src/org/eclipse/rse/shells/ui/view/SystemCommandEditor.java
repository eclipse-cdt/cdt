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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorUserActionCommand;
import org.eclipse.rse.ui.view.SystemViewMenuListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;



/**
 *  Class used for constructing a command editor widget with
 *  UDA substitution variable completion
 */
public class SystemCommandEditor extends SourceViewer
{
	protected boolean menuListenerAdded;
	private Map fGlobalActions = new HashMap(10);
	private java.util.List fSelectionActions = new ArrayList(3);
	protected ISystemValidator cmdValidator;
	protected boolean ignoreChanges;
	private String contentAssistText;
	private boolean isInCodeAssist = false;
	private IViewSite _site;
	private Vector listeners = new Vector();
	
	private TextViewerAction _undoAction = null;
	private TextViewerAction  _cutAction = null;
	private TextViewerAction _copyAction = null;
	private TextViewerAction _pasteAction = null;
	private TextViewerAction _selectAllAction = null;
	private TextViewerAction _caAction = null;
	
	/**
	 * Constructor for the editor
	 * Create the editor widget
	*/
	public SystemCommandEditor(
		IViewSite site,
		Composite parent,
		int attributes,
		int columnSpan,
		SourceViewerConfiguration sourceViewerConfiguration,
		String cmd,
		String contentAssistText)
	{
		super(parent, null, attributes);
		this.contentAssistText = contentAssistText;
		_site = site;
		init(columnSpan, sourceViewerConfiguration, cmd);
	}
	private void init(int columnSpan, SourceViewerConfiguration sourceViewerConfiguration, String cmd)
	{
		if (cmdValidator == null)
		{
			setCommandValidator(new ValidatorUserActionCommand());
		}
		IDocument document = new Document();
		configure(sourceViewerConfiguration);
		setEditable(true);
		setDocument(document);
		Control control = getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = columnSpan;
		data.widthHint = 200;
		data.heightHint = 45;
		control.setLayoutData(data);
		document.set(cmd);
		addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				updateSelectionDependentActions();
			}
		});
		addTextListener(new ITextListener()
		{
			public void textChanged(TextEvent event)
			{
				updateUndoAction();
				if (ignoreChanges || !getTextWidget().isEnabled())
					return;
				String cmdText = getCommandText();
				updateSelectionDependentActions(); // defect 46369 ... just in case
				_selectAllAction.setEnabled(cmdText.length() > 0); // defect 46369
				SystemMessage errorMessage = validateCommand();
				fireModifyEvents(cmdText, errorMessage);
			}
		});
		getTextWidget().addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
	
				if (!e.doit)
					return;
					
				if (e.stateMask == SWT.CTRL)
				{
					// DKM - using this now since SWT isn't providing statemask with verify key events
					switch (e.character)
					{
						case ' ' :
							setInCodeAssist(true);
							doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
							//e.doit = false;
							break;
							// CTRL-Z
						case (int) 'z' - (int) 'a' + 1 :
							doOperation(ITextOperationTarget.UNDO);
							//e.doit = false;
							break;
					}
				}

			}
		});
		prependVerifyKeyListener(new VerifyKeyListener()
		{
			public void verifyKey(VerifyEvent event)
			{
				handleVerifyKeyPressed(event);
			}
		});
		initializeActions();
	}
	public String getCommandText()
	{
		return getDocument().get().trim();
	}
	
	public String getSelectedText()
	{
	    return getTextWidget().getSelectionText();
	}
	/**
	  * Add a modify listener
	  */
	public void addModifyListener(ISystemCommandTextModifyListener listener)
	{
		listeners.add(listener);
	}
	/**
	 * Remove a modify listener
	 */
	public void removeModifyListener(ISystemCommandTextModifyListener listener)
	{
		listeners.remove(listener);
	}
	/**
	 * Fire an event to listeners
	 */
	private void fireModifyEvents(String cmdText, SystemMessage errorMessage)
	{
		for (int idx = 0; idx < listeners.size(); idx++)
			 ((ISystemCommandTextModifyListener) listeners.elementAt(idx)).commandModified(cmdText, errorMessage);
	}
	/**
	 * Turn on or off event ignoring flag
	 */
	public void setIgnoreChanges(boolean ignore)
	{
		ignoreChanges = ignore;
	}
	/**
	 * Validate command input
	 */
	public SystemMessage validateCommand()
	{
		if (cmdValidator != null)
			return cmdValidator.validate(getCommandText());
		else
			return null;
	}
	/**
	 * Set the command validator to validate contents per keystroke
	 */
	public void setCommandValidator(ISystemValidator cmdValidator)
	{
		this.cmdValidator = cmdValidator;
	}
	private void fillContextMenu(IMenuManager menu)
	{
		menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_UNDO));
		menu.appendToGroup(
			ITextEditorActionConstants.GROUP_UNDO,
			(IAction) fGlobalActions.get(ITextEditorActionConstants.UNDO));
		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		menu.appendToGroup(
			ITextEditorActionConstants.GROUP_EDIT,
			(IAction) fGlobalActions.get(ITextEditorActionConstants.CUT));
		menu.appendToGroup(
			ITextEditorActionConstants.GROUP_EDIT,
			(IAction) fGlobalActions.get(ITextEditorActionConstants.COPY));
		menu.appendToGroup(
			ITextEditorActionConstants.GROUP_EDIT,
			(IAction) fGlobalActions.get(ITextEditorActionConstants.PASTE));
		menu.appendToGroup(
			ITextEditorActionConstants.GROUP_EDIT,
			(IAction) fGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));
		menu.add(new Separator("group.generate"));
		menu.appendToGroup("group.generate", (IAction) fGlobalActions.get("ContentAssistProposal")); //$NON-NLS-1$
	}
	private IActionBars getActionBars()
	{
		if (_site != null)
		{
			IActionBars actionBars = _site.getActionBars();
			return actionBars;
		}
		return null;
	}
	public void setViewSite(IViewSite site)
	{
		_site = site;
	}
	private void initializeActions()
	{
		_undoAction = new TextViewerAction(this, UNDO);
		_undoAction.setText(SystemResources.ACTION_UNDO_LABEL); 
		fGlobalActions.put(ITextEditorActionConstants.UNDO, _undoAction);
		_undoAction.setEnabled(false); // defect 46369		
		_cutAction = new TextViewerAction(this, CUT);
		_cutAction.setText(SystemResources.ACTION_CUT_LABEL); 
		fGlobalActions.put(ITextEditorActionConstants.CUT, _cutAction);
		_cutAction.setEnabled(false); // defect 46369
		_copyAction = new TextViewerAction(this, COPY);
		_copyAction.setText(SystemResources.ACTION_COPY_LABEL); 
		fGlobalActions.put(ITextEditorActionConstants.COPY, _copyAction);
		_copyAction.setEnabled(false); // defect 46369
		_pasteAction = new TextViewerAction(this, PASTE);
		_pasteAction.setText(SystemResources.ACTION_PASTE_LABEL); 
		fGlobalActions.put(ITextEditorActionConstants.PASTE, _pasteAction);
		_pasteAction.setEnabled(false); // defect 46369		
		if (_pasteAction instanceof IUpdate)
			 ((IUpdate) _pasteAction).update();
		_selectAllAction = new TextViewerAction(this, SELECT_ALL);
		_selectAllAction.setText(SystemResources.ACTION_SELECT_ALL_LABEL); 
		fGlobalActions.put(ITextEditorActionConstants.SELECT_ALL, _selectAllAction);
		_selectAllAction.setEnabled(false); // defect 46369
		_caAction = new TextViewerAction(this, CONTENTASSIST_PROPOSALS);
		_caAction.setText(contentAssistText); 
		_caAction.setEnabled(true);
		fGlobalActions.put("ContentAssistProposal", _caAction); //$NON-NLS-1$
		
		
		setActionHandlers();
		 
		fSelectionActions.add(ITextEditorActionConstants.CUT);
		fSelectionActions.add(ITextEditorActionConstants.COPY);
		fSelectionActions.add(ITextEditorActionConstants.PASTE);
		// create context menu
		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager mgr)
			{
				fillContextMenu(mgr);
				if (!menuListenerAdded)
				{
					if (mgr instanceof MenuManager)
					{
						Menu m = ((MenuManager) mgr).getMenu();
						if (m != null)
						{
							menuListenerAdded = true;
							SystemViewMenuListener ml = new SystemViewMenuListener();
							//if (messageLine != null)
							// ml.setShowToolTipText(true, messageLine);
							m.addMenuListener(ml);
						}
					}
				}
			}
		});
		StyledText text = this.getTextWidget();
		Menu menu = manager.createContextMenu(text);
		text.setMenu(menu);
	}
	
	public void setActionHandlers()
	{
	    IActionBars actionBars = getActionBars();
		if (actionBars != null)
		{
			actionBars.setGlobalActionHandler(ITextEditorActionConstants.UNDO, _undoAction);
			actionBars.setGlobalActionHandler(ITextEditorActionConstants.CUT, _cutAction);
			actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, _copyAction);
			actionBars.setGlobalActionHandler(ITextEditorActionConstants.PASTE, _pasteAction);
			actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, _selectAllAction);
			actionBars.updateActionBars();
		}
	}
	
	protected void updateSelectionDependentActions()
	{
		Iterator iterator = fSelectionActions.iterator();
		while (iterator.hasNext())
			updateAction((String) iterator.next());
	}
	protected void updateUndoAction()
	{
		Object obj = fGlobalActions.get(ITextEditorActionConstants.UNDO);
		if (obj instanceof IAction)
		{
			IAction action = (IAction) obj;
			if (action instanceof IUpdate)
				 ((IUpdate) action).update();
		}
	}
	protected void updateAction(String actionId)
	{
		Object obj = fGlobalActions.get(actionId);
		if (obj instanceof IAction)
		{
			IAction action = (IAction) obj;
			if (action instanceof IUpdate)
				 ((IUpdate) action).update();
		}
	}
	private void handleVerifyKeyPressed(VerifyEvent event)
	{		
		if (!event.doit)
			return;
			
		// DKM - moved to key press handler since for some reason SWT doesn't provide statemask anymore
			/*
		if (event.character == 0 && (event.keyCode & SWT.KEYCODE_BIT) == 0)
		{
			return;
		}
		switch (event.character)
		{
			case ' ' :
				doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				event.doit = false;
				break;
				// CTRL-Z
			case (int) 'z' - (int) 'a' + 1 :
				doOperation(ITextOperationTarget.UNDO);
				event.doit = false;
				break;
		}
	*/
	}
	
	
	public void setInCodeAssist(boolean flag)
	{
		isInCodeAssist = flag;
	}
	
	public boolean requestWidgetToken(IWidgetTokenKeeper requester)
	{
		boolean result = super.requestWidgetToken(requester);
		if (result)
		{
			setInCodeAssist(true);
		}
		return result;
	}
	public void releaseWidgetToken(IWidgetTokenKeeper tokenKeeper)
	{
		super.releaseWidgetToken(tokenKeeper);
		DelayedIsInCodeAssistUntoggler untoggler = new DelayedIsInCodeAssistUntoggler();
		untoggler.start();
	}
	public boolean isInCodeAssist()
	{
		return isInCodeAssist;
	}
	private class DelayedIsInCodeAssistUntoggler extends Thread
	{
		public void run()
		{
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{
			}
			setInCodeAssist(false);
		}
	}
	private static class TextViewerAction extends Action implements IUpdate
	{
		private int fOperationCode = -1;
		private ITextOperationTarget fOperationTarget;
		public TextViewerAction(ITextViewer viewer, int operationCode)
		{
			fOperationCode = operationCode;
			fOperationTarget = viewer.getTextOperationTarget();
			update();
		}
		/**
		 * Updates the enabled state of the action.
		 * Fires a property change if the enabled state changes.
		 * 
		 * @see Action#firePropertyChange(String, Object, Object)
		 */
		public void update()
		{
			boolean wasEnabled = isEnabled();
			boolean isEnabled = (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
			setEnabled(isEnabled);
			if (wasEnabled != isEnabled)
			{
				firePropertyChange(
					ENABLED,
					wasEnabled ? Boolean.TRUE : Boolean.FALSE,
					isEnabled ? Boolean.TRUE : Boolean.FALSE);
			}
		}
		/**
		 * @see Action#run()
		 */
		public void run()
		{
			if (fOperationCode != -1 && fOperationTarget != null)
			{
				fOperationTarget.doOperation(fOperationCode);
			}
		}
	}
	
	public void doOperation(int operation) 
	{
	    if (operation == CONTENTASSIST_PROPOSALS)
	    {
	        isInCodeAssist = true;
	    }
	    super.doOperation(operation);
	}
}