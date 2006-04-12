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

package org.eclipse.rse.ui.filters;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterActionCopyString;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterActionDeleteString;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterActionMoveStringDown;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterActionMoveStringUp;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterActionPasteString;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ISystemValidatorUniqueString;
import org.eclipse.rse.ui.validators.ValidatorFilterString;
import org.eclipse.rse.ui.view.SystemViewMenuListener;
import org.eclipse.rse.ui.widgets.ISystemEditPaneStates;
import org.eclipse.rse.ui.widgets.SystemEditPaneStateMachine;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


/**
 * Since we have decided to allow the same editing in both the Change Filter dialog
 *  and the Properties page, we have to abstract the meat of the change filter dialog
 *  into a re-usable composite. That is what this is, and it is used by both the 
 *  dialog and the property page.
 */
public class SystemChangeFilterPane extends SystemBaseForm
                implements SelectionListener, ISystemFilterStringEditPaneListener, IMenuListener
{

    protected Button                applyButton, revertButton, testButton;
    protected SystemEditPaneStateMachine sm;
    protected List                  listView;    
    protected Label                 filterNameLabel, filterPoolNameLabel, fsLabel;
    // context menu actions support
	private   SystemChangeFilterActionCopyString     copyAction;
	private   SystemChangeFilterActionPasteString    pasteAction;
	private   SystemChangeFilterActionDeleteString   deleteAction;
	private   SystemChangeFilterActionMoveStringUp   moveUpAction;
	private   SystemChangeFilterActionMoveStringDown moveDownAction;
	private   MenuManager           menuMgr;
	private   Clipboard             clipboard;
	private   boolean              menuListenerAdded;
	
	// inputs
	protected ISystemChangeFilterPaneEditPaneSupplier editPaneSupplier;
	protected ISystemFilter          inputFilter;
    protected ISystemFilterPoolReferenceManagerProvider refProvider;
	protected ISystemFilterPoolManagerProvider provider;
    protected String namePromptLabel, namePromptTip;	    	
    protected String poolPromptLabel, poolPromptTip;	    	
    protected String listPromptLabel, listPromptTip;
    protected String newEntryLabel;

    protected ISystemValidator filterStringValidator = null;
    protected SystemMessage duplicateFilterStringMsg;
    protected boolean      wantTestButton;
    protected boolean      editable = true;
    
    // state
	protected boolean caseSensitiveStrings = false;
	protected boolean allowDuplicateStrings = false;
	protected boolean ignoreEvents = false;
	protected boolean resetting = false;
	protected boolean giveEditorFocus = true;
	protected boolean showingNew = true;
	protected boolean supportsMultipleStrings = true;	
	protected String[] listItems;

	/**
	 * Constructor.
	 */
	public SystemChangeFilterPane(Shell shell, ISystemMessageLine msgLine, ISystemChangeFilterPaneEditPaneSupplier editPaneSupplier)
	{
		super(shell, msgLine);
		this.editPaneSupplier = editPaneSupplier;

        
        // default mri values
        namePromptLabel = SystemResources.RESID_CHGFILTER_NAME_LABEL;
        namePromptTip   = SystemResources.RESID_CHGFILTER_NAME_TOOLTIP;
        poolPromptLabel = SystemResources.RESID_CHGFILTER_POOL_LABEL;
        poolPromptTip   = SystemResources.RESID_CHGFILTER_POOL_TOOLTIP;
        listPromptLabel = SystemResources.RESID_CHGFILTER_LIST_LABEL;
        listPromptTip   = SystemResources.RESID_CHGFILTER_LIST_TOOLTIP;
	}		

	// INPUT/CONFIGURATION
    /**
	 * <i>Configuration method</i><br>
     * Set the contextual system filter pool reference manager provider. Will be non-null if the
     * current selection is a reference to a filter pool or filter, or a reference manager
     * provider.
     * <p>
     * This is passed into the filter and filter string wizards and dialogs in case it is needed
     * for context. 
     */
    public void setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider provider)
    {
    	 this.refProvider = provider;
    }	
	/**
	 * <i>Configuration method</i><br>
	 * Set the contextual system filter pool manager provider. Will be non-null if the
	 * current selection is a filter pool or filter, or reference to them, or a manager provider.
	 * Generally this is called when the setSystemFilterPoolReferenceManagerProvider can't be called
	 *  for some reason.
	 * <p>
	 * This is passed into the filter and filter string wizards and dialogs in case it is needed
	 * for context. 
	 */
	public void setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider provider)
	{
		this.provider = provider;
	}	
	
	/**
	 * <i>Configuration method</i><br>
	 * Set the Parent Filter Pool prompt label and tooltip text.
	 */
	public void setParentPoolPromptLabel(String label, String tip)
	{
		this.poolPromptLabel = label;
		this.poolPromptTip = tip;
	}
	/**
	 * Return the parent filter pool prompt label, as set by {@link #setParentPoolPromptLabel(String, String)}
	 */
	public String getParentPoolPromptLabel()
	{
		return poolPromptLabel;
	}
	/**
	 * Return the parent filter pool prompt tip, as set by {@link #setParentPoolPromptLabel(String, String)}
	 */
	public String getParentPoolPromptTip()
	{
		return poolPromptTip;
	}

	/**
	 * <i>Configuration method</i><br>
	 * Set the name prompt label and tooltip text.
	 */
	public void setNamePromptLabel(String label, String tip)
	{
		this.namePromptLabel = label;
		this.namePromptTip = tip;
	}
	/**
	 * Return the name prompt label as set by {@link #setNamePromptLabel(String, String)}
	 */
	public String getNamePromptLabel()
	{
		return namePromptLabel;
	}
	/**
	 * Return the name prompt tip as set by {@link #setNamePromptLabel(String, String)}
	 */
	public String getNamePromptTip()
	{
		return namePromptTip;
	}

	/**
	 * <i>Configuration method</i><br>
	 * Set the label shown in group box around the filter string list, and the tooltip text for the
	 *  list box.
	 */
	public void setListLabel(String label, String tip)
	{
		this.listPromptLabel = label;
		this.listPromptTip = tip;
	}
	/**
	 * Return list label as set by {@link #setListLabel(String, String)}
	 */
	public String getListLabel()
	{
		return listPromptLabel;
	}
	/**
	 * Return list tip as set by {@link #setListLabel(String, String)}
	 */
	public String getListTip()
	{
		return listPromptTip;
	}
	
	/**
	 * Set the string to show as the first item in the list. 
	 * The default is "New filter string"
	 */
	public void setNewListItemText(String label)
	{
		this.newEntryLabel = label;
	}
	/**
	 * Return the text for the list item, as set by {@link #setNewListItemText(String)},
	 *  or the default if not set.
	 */
	public String getNewListItemText()
	{
		return (newEntryLabel != null) ? newEntryLabel : SystemResources.RESID_CHGFILTER_LIST_NEWITEM; 
	}
	
	/**
	 * <i>Configuration method</i><br>
	 * Call this to specify a validator for the filter string. It will be called per keystroke.
	 * A default validator is supplied otherwise: ValidatorFilterString.
	 * <p>
	 * Your validator should extend ValidatorFilterString to inherited the uniqueness error checking.
	 * <p>
	 * Alternatively, if all you want is a unique error message for the case when duplicates are found,
	 *  call setDuplicateFilterStringErrorMessage, and it will be used in the default validator.
	 */
	public void setFilterStringValidator(ISystemValidator v)
	{
		filterStringValidator = v;
	}
	/**
	 * Return the result of {@link #setFilterStringValidator(ISystemValidator)}.
	 */
	public ISystemValidator getFilterStringValidator()
	{
		return filterStringValidator;
	}
	/**
	 * <i>Configuration method</i><br>
	 * Set the error message to use when the user is editing or creating a filter string, and the 
	 *  Apply processing detects a duplicate filter string in the list.
	 */
	public void setDuplicateFilterStringErrorMessage(SystemMessage msg)
	{
		this.duplicateFilterStringMsg = msg;
	}
	/**
	 * Return results of {@link #setDuplicateFilterStringErrorMessage(SystemMessage)}
	 */
	public SystemMessage getDuplicateFilterStringErrorMessage()
	{
		return duplicateFilterStringMsg;
	}
	/**
	 * <i>Configuration method</i><br>
	 * Specify if you want to include a test button or not. Appears with "Apply" and "Reset"
	 */
	public void setWantTestButton(boolean wantTestButton)
	{
		this.wantTestButton = wantTestButton;
	}
	/**
	 * Return whether a test button is wanted or not, as set by {@link #setWantTestButton(boolean)}
	 */
	public boolean getWantTestButton()
	{
		return wantTestButton;
	}
	
	/**
	 * Set if the edit pane is not to be editable
	 */
	public void setEditable(boolean editable)
	{
		this.editable = editable;
		this.showingNew = editable;
	}
	/**
	 * Return whether the edit pane is editable, as set by {@link #setEditable(boolean)}
	 */
	public boolean getEditable()
	{
		return editable;
	}
	/**
	 * Set if the user is to be allowed to create multiple filter strings or not. Default is true
	 */
	public void setSupportsMultipleStrings(boolean multi)
	{
		this.showingNew = multi;
		this.supportsMultipleStrings = multi;
	}
	/**
	 * Return whether the user is to be allowed to create multiple filter strings or not. Default is true
	 */
	public boolean getSupportsMultipleStrings()
	{
		return supportsMultipleStrings;
		//return (!showingNew && editable);
	}

	// LIFECYCLE
	/**
	 * Intercept of parent so we can set the input filter, and deduce whether
	 *  strings are case sensitive and if duplicates are allowed.<br>
	 * Not typically overridden, but if you do, be sure to call super!
	 */
	public void setInputObject(Object inputObject)
	{
		//System.out.println("INSIDE SETINPUTOBJECT: " + inputObject);
		super.setInputObject(inputObject);
		inputFilter = getSystemFilter(inputObject);
        caseSensitiveStrings = inputFilter.areStringsCaseSensitive();
    	allowDuplicateStrings = inputFilter.supportsDuplicateFilterStrings();
	}
			
	/**
	 * Returns the control (the list view) to recieve initial focus control
	 */
	public Control getInitialFocusControl()
	{
		return listView;
	}
	/**
	 *  Populates the content area
	 */
	public Control createContents(Composite parent)
	{
		SystemWidgetHelpers.setHelp(parent, RSEUIPlugin.HELPPREFIX+"dufr0000");
		
		if (getShell()==null)
			setShell(parent.getShell());
	    SystemFilterStringEditPane editpane = getFilterStringEditPane(getShell());
	    editpane.setSystemFilterPoolReferenceManagerProvider(refProvider);
		editpane.setSystemFilterPoolManagerProvider(provider);
	    editpane.setChangeFilterMode(true);

		// Inner composite
		int nbrColumns = 2;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		
		// composite at top to hold readonly info
		//Composite topComposite = SystemWidgetHelpers.createFlushComposite(composite, 2);
		//((GridData)topComposite.getLayoutData()).horizontalSpan = nbrColumns;  
		Composite topComposite = composite;		
		// filter name
		SystemWidgetHelpers.createLabel(topComposite, namePromptLabel);
		filterNameLabel = SystemWidgetHelpers.createLabel(topComposite, "");
		filterNameLabel.setToolTipText(namePromptTip);
		filterNameLabel.setText(inputFilter.getName());
		// filter pool
		SystemWidgetHelpers.createLabel(topComposite, poolPromptLabel);
		filterPoolNameLabel = SystemWidgetHelpers.createLabel(topComposite, "");
		filterPoolNameLabel.setToolTipText(namePromptTip);
		ISystemFilterPool parentPool = inputFilter.getParentFilterPool();
		filterPoolNameLabel.setText(parentPool.getName());
		
		addFillerLine(composite, nbrColumns);
		
	    // create list view on left
	    if (supportsMultipleStrings)
	    {
			listView = SystemWidgetHelpers.createListBox(composite, listPromptLabel, null, false, 1);
			//listView.setToolTipText(listPromptTip); VERY ANNOYING
			GridData data = (GridData)listView.getLayoutData();
			data.grabExcessHorizontalSpace = false;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessVerticalSpace = true;
			data.verticalAlignment = GridData.FILL;				
			data.widthHint = 130;
	    }
		String[] strings = inputFilter.getFilterStrings();
		if (strings == null)
			strings = new String[] {};
		int delta = (showingNew ? 1 : 0);
		listItems = new String[delta+strings.length];
		if (showingNew)	
			listItems[0] = getNewListItemText(); // "New filter string" or caller-supplied
		for (int idx=0; idx<strings.length; idx++)
			listItems[idx+delta] = strings[idx];
		if (listView != null)
			listView.setItems(listItems);

	    // we want the tree view on the left to extend to the bottom of the page, so on the right
	    // we create a 1-column composite that will hold the edit pane on top, and the apply/revert
	    // buttons on the bottom...
	    Composite rightSideComposite = SystemWidgetHelpers.createFlushComposite(composite, 1);
	    if (listView == null)
	    {
			GridData data = (GridData)rightSideComposite.getLayoutData();
			data.grabExcessHorizontalSpace = true;
			data.horizontalAlignment = GridData.FILL;
			data.horizontalSpan = nbrColumns;
	    }

		// now add a top spacer line and visual separator line, for the right side
		if (listView != null)
		{		
			addFillerLine(rightSideComposite, 1);
			fsLabel = SystemWidgetHelpers.createLabel(rightSideComposite, "");
			addSeparatorLine(rightSideComposite,1);
		}
	
	    // now populate top of right-side composite with edit pane...
		Control editPaneComposite = editpane.createContents(rightSideComposite);
		if (!allowDuplicateStrings && (filterStringValidator == null))
		{
			String[] existingStrings = inputFilter.getFilterStrings();
			if (existingStrings!=null)
			{
			  filterStringValidator = new ValidatorFilterString(existingStrings, caseSensitiveStrings);
			  if (duplicateFilterStringMsg != null)
			    ((ValidatorFilterString)filterStringValidator).setDuplicateFilterStringErrorMessage(duplicateFilterStringMsg);
			}
		}
						
		// now add a bottom visual separator line
		addSeparatorLine(rightSideComposite,1);
		
        // now populate bottom of right-side composite with apply/revert buttons within their own composite
        int nbrColumns_buttonComposite = 3;
        Composite applyResetButtonComposite = SystemWidgetHelpers.createFlushComposite(rightSideComposite, nbrColumns_buttonComposite);		
        if (!wantTestButton)
        {
		    ((GridData)applyResetButtonComposite.getLayoutData()).horizontalIndent = 200; // shift buttons to the right
		    // now populate the buttons composite with apply and revert buttons
		    Label filler = SystemWidgetHelpers.createLabel(applyResetButtonComposite, "");
		    ((GridData)filler.getLayoutData()).grabExcessHorizontalSpace = true;
		    ((GridData)filler.getLayoutData()).horizontalAlignment = GridData.FILL;
        }
		if (wantTestButton)
		{
		  	testButton  = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, 
					SystemResources.RESID_CHGFILTER_BUTTON_TEST_LABEL,SystemResources.RESID_CHGFILTER_BUTTON_TEST_TOOLTIP);		
		  	editpane.setTestButton(testButton);
		  	testButton.addSelectionListener(this);
		}
		applyButton  = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, SystemResources.RESID_CHGFILTER_BUTTON_APPLY_LABEL, SystemResources.RESID_CHGFILTER_BUTTON_APPLY_TOOLTIP);
		revertButton = SystemWidgetHelpers.createPushButton(applyResetButtonComposite, this, SystemResources.RESID_CHGFILTER_BUTTON_REVERT_LABEL,SystemResources.RESID_CHGFILTER_BUTTON_REVERT_TOOLTIP);

		// now add a spacer to soak up left-over height...
		addGrowableFillerLine(rightSideComposite, 1);

        // create state machine to manage edit pane
        sm = new SystemEditPaneStateMachine(rightSideComposite, applyButton, revertButton);
        sm.setUnsetMode();
	        		    	
 	    composite.layout(true);
 	    
	 	// add listeners
	 	if (listView!=null)
			listView.addSelectionListener(this);
		applyButton.addSelectionListener(this);
		revertButton.addSelectionListener(this);
		editpane.addChangeListener(this);
		
		// add special listeners for accessibility -- do not change focus when navigating list with keys
		if (listView != null)
		{
			listView.addMouseListener(new MouseListener() {
				public void mouseDoubleClick(MouseEvent e) {
					giveEditorFocus = true;
				}
				public void mouseDown(MouseEvent e) {
					giveEditorFocus = true;
				}
				public void mouseUp(MouseEvent e) {
					giveEditorFocus = true;
				}
			});
			listView.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					giveEditorFocus = false;
				}
				public void keyReleased(KeyEvent e) {
					giveEditorFocus = false;
				}
			});
		}
				
		// add context menu
		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		if (listView != null)
		{
			menuMgr = new MenuManager("#ChangeFilterPopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(this);
			Menu menu = menuMgr.createContextMenu(listView); 
			listView.setMenu(menu);
		} 	    
		
		// preselect list item..		
		// edit mode..	
		if ((listItems.length > 1) || !showingNew)
		{
			if (listView!=null)
			{
				if (showingNew)
		  			listView.select(1);
		  		else
		  			listView.select(0);
			}
		  	sm.setEditMode();
		  	editpane.setFilterString(listItems[showingNew ? 1 : 0], 1);
		}
		// new...
		else
		{
			if (listView!=null)
		  		listView.select(0);
		  	sm.setNewMode();
		  	editpane.setFilterString(null, 0);
		}
		editpane.configureHeadingLabel(fsLabel);
		setPageComplete(editpane.isComplete());// side effect is initial enablement of test button

		if (!editable)
		{
			if (listView!=null)
				listView.setEnabled(false);
			if (strings.length > 0)
				editpane.setFilterString(strings[0], 0);
			editPaneComposite.setEnabled(false);
			//editpane.setEditable(false);
			setPageComplete(true);
		}
		else if (!getSupportsMultipleStrings())
			setPageComplete(true);
			
		return composite;
	}
	/*
	 * Intercept of parent so we can reset the default button
	 *
	protected void createButtonsForButtonBar(Composite parent) 
	{
		super.createButtonsForButtonBar(parent);
		getShell().setDefaultButton(applyButton); // defect 46129
	}*/
	/**
	 * Return our edit pane, by referring back to our caller. You can use this,
	 *  but do not override it!
	 */
	protected SystemFilterStringEditPane getFilterStringEditPane(Shell shell)
	{
		return editPaneSupplier.getFilterStringEditPane(shell);
	}
	
	/**
	 * Return the Apply button
	 */
	public Button getApplyButton()
	{
		return applyButton;
	}
	/**
	 * Return the Revert button
	 */
	public Button getRevertButton()
	{
		return revertButton;
	}

	/**
	 * Call when user presses OK button on containing dialog or pane.
	 * This is when we save all the changes the user made.
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	public boolean processOK() 
	{
		if (!editable)
			return true;
		if (!verify(true))
			return false;		
		/*
	    ignoreEvents = true;
	    SystemFilterStringEditPane editpane = getFilterStringEditPane(getShell());
		if (sm.isSaveRequired())
		{
			if (editpane.verify()!=null)
			{
				ignoreEvents = false;
				sm.setChangesMade(); // defect 45773
			    return false; // pending errors. Cannot save, so cannot close!
			}
		    saveFilterString(editpane.getFilterString(),editpane.getCurrentSelectionIndex());
		}
		else if (!sm.getNewSetByDelete())  //d47125 
		{
		   if (editpane.verify() != null)
		   {
			  ignoreEvents = false;
			  return false;
		   } 
		}    
		ignoreEvents = false;
		if (!allowDuplicateStrings && (listView != null))
		{
			String duplicate = checkForDuplicates();
			if (duplicate != null)
			{
			  SystemMessage errMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_FILTERSTRING_DUPLICATES).makeSubstitution(duplicate);
			  getMessageLine().setErrorMessage(errMsg);
			  listView.setFocus();
			  return false;
			}
		}        				
		*/
		ISystemFilterPool pool = inputFilter.getParentFilterPool(); // recurses for nested filter
		ISystemFilterPoolManager mgr = pool.getSystemFilterPoolManager();
		if (listView != null)		  
			listItems = listView.getItems();
		String[] filterStrings = null;
		if (showingNew)
		{
			filterStrings = new String[listItems.length - 1];
			for (int idx=0; idx<filterStrings.length; idx++)
		   		filterStrings[idx] = listItems[idx+1];
		}
		else
		{
			filterStrings = listItems;	
		}
		
		try {
		    mgr.updateSystemFilter(inputFilter, inputFilter.getName(), filterStrings);
		} 
		catch (SystemMessageException exc) 
		{
			getMessageLine().setErrorMessage(exc.getSystemMessage());
		  	return false;
		}
		catch (Exception exc) 
		{
		  	//displayErrorMessage("Error updating filter: " + exc.getMessage());
		  	SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_UPDATEFILTER_FAILED);
		  	String excText = exc.getMessage();
		  	if (excText == null)
		  	  excText = exc.getClass().getName();
    	    msg.makeSubstitution(excText,exc);
			getMessageLine().setErrorMessage(msg);
		  	return false;
		}
		//return super.processOK();
		return true;		
	}	
	/**
	 * Perform the same validation that is done when OK is pressed.
	 * @param doSave - true to actually save pending changes in filter string editor (eg, when called when actually processing OK).
	 */
	public boolean verify(boolean doSave)
	{
		ignoreEvents = true;
		getMessageLine().clearErrorMessage();
		SystemFilterStringEditPane editpane = getFilterStringEditPane(getShell());
		if (editpane.canSaveImplicitly() && sm.isSaveRequired())
		{
			if (editpane.verify()!=null)
			{
				ignoreEvents = false;
				sm.setChangesMade(); // defect 45773
				return false; // pending errors. Cannot save, so cannot close!
			}
			if (doSave)
				saveFilterString(editpane.getFilterString(),editpane.getCurrentSelectionIndex());
		}
		else if (!sm.getNewSetByDelete())  //d47125 
		{
		   if (editpane.verify() != null)
		   {
			  ignoreEvents = false;
			  return false;
		   } 
		}    
		ignoreEvents = false;
		if (!allowDuplicateStrings && (listView != null))
		{
			String duplicate = checkForDuplicates();
			if (duplicate != null)
			{
			  SystemMessage errMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_FILTERSTRING_DUPLICATES).makeSubstitution(duplicate);
			  getMessageLine().setErrorMessage(errMsg);
			  listView.setFocus();
			  return false;
			}
		}        				
		return true;
	}
	
	/**
	 * Call when user presses Cancel button in containing dialog. We simply blow away all their changes!
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	public boolean processCancel() 
	{
		/*
		if (sm.isSaveRequired())
		{
			if (editpane.verify()!=null)
			  return false; // pending errors. Cannot save, so cannot close!
		    saveFilterString();
		}
		*/
		//return super.processCancel();
		return true;		
	}	
				
	/**
	 * Handles events generated by controls on this page.
	 */
	public void widgetSelected(SelectionEvent e)
	{
	    if (resetting)
	      return;
	    Widget source = e.widget;
	    if (source == applyButton) 
	    {
			getMessageLine().clearMessage();
			applyPressed();			
		} 
		else if (source == revertButton) 
		{
			getMessageLine().clearMessage();
			revertPressed();			
		}
		else if (source == testButton) 
		{
			getMessageLine().clearMessage();
			getFilterStringEditPane(getShell()).processTest(getShell());
		}

		else if (source == listView)
		{
	    	// check for unresolved errors...
	    	if (getFilterStringEditPane(getShell()).areErrorsPending())
	    	{
	    	  e.doit = false; // dang, this doesn't work! 
	    	  resetting = true; // d45795
	    	  listView.select(getFilterStringEditPane(getShell()).getCurrentSelectionIndex()); // d45795
	    	  resetting = false; // d45795
	    	  return;
	    	}
	    	// check if we can save implicitly and if there are pending changes
	    	else if (getFilterStringEditPane(getShell()).canSaveImplicitly() && sm.isSaveRequired())
	    	{
				getMessageLine().clearMessage();
				
				// if error, do not change selection and we keep pending changes state
	    		if (getFilterStringEditPane(getShell()).verify() != null)
	    		{
	    			e.doit = false; // dang, this doesn't work!
	    			sm.setChangesMade();
	    	        resetting = true; // d45795
	    	        listView.select(getFilterStringEditPane(getShell()).getCurrentSelectionIndex()); // d45795
	    	        resetting = false; // d45795
	    	        applyButton.setEnabled(false); // d45795
	    			return;	    			
	    		}
	    		
	    		// no errors
	    		saveFilterString(getFilterStringEditPane(getShell()).getFilterString(), 
	    							getFilterStringEditPane(getShell()).getCurrentSelectionIndex());
	    	}
            else {
				getMessageLine().clearMessage();
            }
				            
	    	// proceed with selection change request...
	    	processListSelect();
	    	
			// KM: defect 53009
			// need to check if Create button can be enabled if new filter string is selected
			if (isNewSelected()) {
				handleNewFilterStringItemSelection();
			}
		}
	}	    		
	/**
	 * An item selected in list. Process it
	 */
	private void processListSelect()
	{
		sm.setNewSetByDelete(false);    //d47125
	    int newIndex = listView.getSelectionIndex();
		if (isNewSelected() || (newIndex == -1))
		{
			SystemFilterStringEditPane editpane = getFilterStringEditPane(getShell());
			editpane.setFilterString(null, 0);
		  	
		  	if (isNewSelected()) {
		    	sm.setNewMode();
		  	}
		  	else
		    	sm.setUnsetMode();
    	   	if (testButton != null)
    	       	testButton.setEnabled(false);
		}
		else
		{				
			getFilterStringEditPane(getShell()).setFilterString(getCurrentSelection(), newIndex);
		  	sm.setEditMode();
    	   	if (testButton != null)
    	       	testButton.setEnabled(true);
		}
		getFilterStringEditPane(getShell()).configureHeadingLabel(fsLabel);
		if (giveEditorFocus) 
		{
		    Control c = getFilterStringEditPane(getShell()).getInitialFocusControl();
		    if ((c!=null) && !c.isDisposed() && c.isVisible())
		      c.setFocus();
		}		      
	}
	/**
	 * User pressed Apply to save the pending changes the current filter string
	 */
	protected void applyPressed()
	{
		ignoreEvents = true;

		SystemFilterStringEditPane editpane = getFilterStringEditPane(getShell());
		if (editpane.verify() == null)
		{
		   boolean ok = true;
		   String editedFilterString = editpane.getFilterString();
		   if (filterStringValidator != null)
		   {
		   		String[] names = (listView!=null) ? listView.getItems() : listItems; 
			 	if (!allowDuplicateStrings && (filterStringValidator instanceof ISystemValidatorUniqueString))
			   		((ISystemValidatorUniqueString)filterStringValidator).setExistingNamesList(names);
			 	SystemMessage errorMessage = filterStringValidator.validate(editedFilterString);
			 	if (errorMessage != null)
			 	{
					ok = false;
					getMessageLine().setErrorMessage(errorMessage);
			 	}
		   }      				

		   if (ok)
		   {
			  sm.applyPressed();
			  saveFilterString(editedFilterString, editpane.getCurrentSelectionIndex());
		   }
		}

		ignoreEvents = false;
	}

	/**
	 * User pressed Revert to discard the pending changes the current filter string
	 */
	protected void revertPressed()
	{
		ignoreEvents = true;
		sm.resetPressed();
		boolean newMode = (isNewSelected() || listView == null || (listView.getSelectionIndex() == -1));
		if (newMode)
			getFilterStringEditPane(getShell()).setFilterString(null, 0);
		else
			getFilterStringEditPane(getShell()).setFilterString(getCurrentSelection(), listView.getSelectionIndex());
		getMessageLine().clearErrorMessage();
    	setPageComplete(true);
    	if (testButton != null)
    	  testButton.setEnabled(!newMode);
		ignoreEvents = false;
	}

	/**
	 * Handles events generated by controls on this page.
	 */
	public void widgetDefaultSelected(SelectionEvent e)
	{
	}	    		

	/**
	 * The completeness of the page has changed.
	 * We direct it to the Apply button versus just the OK button
	 */
	public void setPageComplete(boolean complete)
	{
		// d45795
		if (applyButton != null)
		{
			if (!complete)
			  applyButton.setEnabled(false);
			// else: we never enable it because the state machine does that anyway on any user-input change
		}
		super.setPageComplete(complete);
	}
		
	// ------------	
	// List methods
	// ------------
	
	/**
	 * Return true if currently selected item is "New"
	 */
	protected boolean isNewSelected()
	{
		if (showingNew && (listView!=null))
	    	return (listView.getSelectionIndex() == 0);
	    else
	    	return false;
	}
	
	/**
	 * Return currently selected filter string
	 */
	protected String getCurrentSelection()
	{
		if (showingNew)
		{
			if (listView.getSelectionCount() >= 1)
		  		return listView.getSelection()[0];
			else
		  		return null;
		}
		else if (listView != null)
		{
			if (listView.getSelectionCount() >= 0)
				return listView.getSelection()[0];
			else
				return null;
		} 
		else
		{
			return listItems[0];
		}
	}
	
	// LIFECYCLE OF STRINGS
	
	/**
	 * Create a new filter string from the contents of the filter string edit pane
	 */
	protected String createFilterString()
	{
		String newFilterString = getFilterStringEditPane(getShell()).getFilterString();
		listView.add(newFilterString);	
		int selectionIndex = listView.getItemCount()-1;
		listView.select(selectionIndex);
		sm.setEditMode();
		getFilterStringEditPane(getShell()).setFilterString(newFilterString, selectionIndex);
		getFilterStringEditPane(getShell()).configureHeadingLabel(fsLabel);
		return newFilterString;
	}
	/**
	 * Update the current selection with the values in the edit pane
	 */
	protected void saveFilterString(String editedFilterString, int currSelectionIndex)
	{
		//System.out.println("inside savefilterstring. current sel index = " + currSelectionIndex);
		if (currSelectionIndex == -1)
		  return; 
		else if (showingNew && (currSelectionIndex == 0))
		  createFilterString();
		else
		{
		   //System.out.println("Updating list item " + currSelectionIndex + " to " + editedFilterString);
		   	if (listView != null)
		   		listView.setItem(currSelectionIndex, editedFilterString);
		   	else
		   		listItems[0] = editedFilterString;
		}
	}
	
	
	// private methods
	
	private ISystemFilter getSystemFilter(Object selectedObject)
	{
		if (selectedObject instanceof ISystemFilter)
		  return (ISystemFilter)selectedObject;
		else
		  return ((ISystemFilterReference)selectedObject).getReferencedFilter();
	}
	
	// ----------------------------------------------
	// EDIT PANE CHANGE LISTENER INTERFACE METHODS...
	// ----------------------------------------------
    /**
     * Callback method from the edit pane. The user has changed the filter string. It may or may not
     *  be valid. If not, the given message is non-null. If it is, and you want it,
     *  call getSystemFilterString() in the edit pane.
     */
    public void filterStringChanged(SystemMessage message)
    {
    	if (message != null)
			getMessageLine().setErrorMessage(message);
    	else
			getMessageLine().clearErrorMessage();
    	if (testButton != null)
    	  testButton.setEnabled(message == null);
        if (!ignoreEvents) // this is set on while verifying, indicating these are not real change events per se
        {
          sm.setChangesMade();
        }
    	setPageComplete(message == null);
    }
    /**
     * Callback method. We are about to do a verify,the side effect of which is to
     *  change the current state of the dialog, which we don't want. This tells the
     *  dialog to back up that state so it can be restored.
     */
    public void backupChangedState()
    {
    	sm.backup();    	
    }    
    /**
     * Callback method. After backup and change events this is called to restore state
     */
    public void restoreChangedState()
    {
    	sm.restore();    
    }
    
    // ------------------------------
    // CONTEXT MENU ACTION SUPPORT...
    // ------------------------------
	/**
	 * Called when the context menu is about to open.
     * Calls {@link #fillContextMenu(IMenuManager)}
	 */
	public void menuAboutToShow(IMenuManager menu) 
	{
		
		fillContextMenu(menu);
   	    if (!menuListenerAdded)
   	    {
   	        if (menu instanceof MenuManager)
   	        {
   	      	   Menu m = ((MenuManager)menu).getMenu();
   	      	   if (m != null)
   	      	   {
   	      	      menuListenerAdded = true;
   	      	      SystemViewMenuListener ml = new SystemViewMenuListener();
   	      	      //ml.setShowToolTipText(true, wwDialog.getMessageLine()); does not work for some reason
   	      	      m.addMenuListener(ml);
   	      	   }
   	        }
   	    }
	}

	/**
	 * This is method is called to populate the popup menu
	 */
	public void fillContextMenu(IMenuManager menu) 
	{		
		String currentString = getCurrentSelection();
		IStructuredSelection selection= null;
		if (currentString != null)
          selection = new StructuredSelection(currentString);
		// Partition into groups...
        createStandardGroups(menu);
        ISystemAction action = null;
        boolean isNewSelected = isNewSelected();
        //System.out.println("new selected? " + isNewSelected);
        if ((selection != null) && !isNewSelected)
        {
            action = getDeleteAction(selection);
            menu.appendToGroup(action.getContextMenuGroup(), action);

            action = getCopyAction(selection);
            menu.appendToGroup(action.getContextMenuGroup(), action);

            action = getMoveUpAction(selection);
            menu.appendToGroup(action.getContextMenuGroup(), action);

            action = getMoveDownAction(selection);
            menu.appendToGroup(action.getContextMenuGroup(), action);
        }
        //if (!isNewSelected)
        {
            action = getPasteAction(selection);
            menu.appendToGroup(action.getContextMenuGroup(), action);               
        }
	}
	/**
	 * Creates the Systems plugin standard groups in a context menu.
	 */
	public void createStandardGroups(IMenuManager menu) 
	{
		if (!menu.isEmpty())
			return;			
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE));   // rename,move,copy,delete,bookmark,refactoring
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER));      // move up, move down		
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS));    // user or BP/ISV additions
	}

	/**
	 * Get the delete action
	 */
	private SystemChangeFilterActionDeleteString getDeleteAction(ISelection selection)
	{
		if (deleteAction == null)
		  deleteAction = new SystemChangeFilterActionDeleteString(this);
		deleteAction.setShell(getShell());
		deleteAction.setSelection(selection);
		return deleteAction;
	}	

	/**
	 * Get the move up action
	 */
	private SystemChangeFilterActionMoveStringUp getMoveUpAction(ISelection selection)
	{
		if (moveUpAction == null)
		  moveUpAction = new SystemChangeFilterActionMoveStringUp(this);
		moveUpAction.setShell(getShell());
		moveUpAction.setSelection(selection);
		return moveUpAction;
	}	
	/**
	 * Get the move down action
	 */
	private SystemChangeFilterActionMoveStringDown getMoveDownAction(ISelection selection)
	{
		if (moveDownAction == null)
		  moveDownAction = new SystemChangeFilterActionMoveStringDown(this);
		moveDownAction.setShell(getShell());
		moveDownAction.setSelection(selection);
		return moveDownAction;
	}	
	/**
	 * Get the copy action
	 */
	private SystemChangeFilterActionCopyString getCopyAction(ISelection selection)
	{
		if (copyAction == null)
		  copyAction = new SystemChangeFilterActionCopyString(this);
		copyAction.setShell(getShell());
		copyAction.setSelection(selection);
		return copyAction;
	}	
	/**
	 * Get the paste action
	 */
	private SystemChangeFilterActionPasteString getPasteAction(ISelection selection)
	{
		if (pasteAction == null)
		  pasteAction = new SystemChangeFilterActionPasteString(this);
		pasteAction.setShell(getShell());
		if (selection != null)
		  pasteAction.setSelection(selection);
		return pasteAction;
	}		
	// -------------------------------------------------------------
	// CALLBACK METHODS FROM THE RIGHT CLICK CONTEXT MENU ACTIONS...
	// -------------------------------------------------------------
		
	/**
	 * Decide if we can do the delete or not.
	 * Will decide the enabled state of the delete action.
	 */
	public boolean canDelete()
	{
		 return (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected() && (listView.getSelectionIndex()!=-1) && 
		         (listView.getItemCount()>2);	 // defect 46149
	}
	/**
	 * Perform the delete action
	 */
	public void doDelete()
	{
		int idx = listView.getSelectionIndex();		
	    SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONFIRM_DELETE);
	    SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), msg);
	    try{
	      if (msgDlg.openQuestion())
	      {
		     	listView.remove(idx); // remove item from list
		    	 // defect 46097...
		     	listView.select(0);		     
		     	processListSelect();
		     	sm.setNewSetByDelete(true);   //d47125
		     	
				// KM: defect 53009
				// since we have new set by delete action, the verify method
				// will ignore actual verification, and just check for duplicates
				boolean ok = verify(false);
				
				// set page to complete
				// but this does not affect Create button
				setPageComplete(ok);

				// if the verify above went ok, i.e. no duplicates found, then
				// need to check if Create button can be enabled if new filter string is selected
				if (ok && isNewSelected()) {
					handleNewFilterStringItemSelection();
				}
	      	}
	    } catch (Exception exc) {}
	}
	
	/**
	 * Handles new filter string selection from the filter string list.
	 * Enables/disables Create button depending on whether the
	 * default filter string is valid.
	 */
	protected void handleNewFilterStringItemSelection() {
		
		ignoreEvents = true;
		
		SystemFilterStringEditPane editpane = getFilterStringEditPane(getShell());
				
		// we check if there are any existing errors
		// shouldn't be unless setting the default filter string for new filter string item
		// in processListSelect() above caused a problem
		boolean anyErrors = editpane.areErrorsPending();
			
		// if no errors currently, then do a verify
		if (!anyErrors) {
			
			// verify the default filter string
			boolean result = (editpane.verify() == null);
					
			// enable Create button if there are no errors
			if (result) {
				applyButton.setEnabled(true);
			}
			// otherwise clear pending errors, since verify() call could have
			// resulted in errors showing through 
			else {
				editpane.clearErrorsPending();
				getMessageLine().clearErrorMessage();
			}
		}
		
		// yantzi: RSE 6.2, added call to setPageComplete to enable the OK button, otherwise
		// it is disabled when the user deletes a filter or selects the newfilterstring dialog
		setPageComplete(true);
		
		ignoreEvents = false;
	}
			
	/**
	 * Decide if we can do the move up or not.
	 * Will decide the enabled state of the move up action.
	 */
	public boolean canMoveUp()
	{
		 boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected() && (listView.getSelectionIndex()!=-1);
		 if (can)
		 {
		 	int idx = listView.getSelectionIndex();
		 	can = (idx > 1); // skip new at index 0, skip first actual string
         }
		 return can;		
	}
	/**
	 * Perform the move up action
	 */
	public void doMoveUp()
	{
		int idx = listView.getSelectionIndex();		
		String currentString = getCurrentSelection();
		listView.remove(idx); // remove item from list
		listView.add(currentString,idx-1);
	}
	/**
	 * Decide if we can do the move down or not.
	 * Will decide the enabled state of the move down action.
	 */
	public boolean canMoveDown()
	{
		 boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending() && !isNewSelected() && (listView.getSelectionIndex()!=-1);
		 if (can)
		 {
		 	int idx = listView.getSelectionIndex();
		 	can = (idx <= (listView.getItemCount()-2)); // -1 is to be zero-based. Another -1 is to discount "New".
         }
		 return can;		
	}
	/**
	 * Perform the move down action
	 */
	public void doMoveDown()
	{
		int idx = listView.getSelectionIndex();		
		String currentString = getCurrentSelection();
		listView.remove(idx); // remove item from list
		listView.add(currentString,idx+1);
	}

	/**
	 * Decide if we can do the copy or not.
	 * Will decide the enabled state of the copy action.
	 */
	public boolean canCopy()
	{
		 boolean can = (sm.getMode() == ISystemEditPaneStates.MODE_EDIT) && !sm.areChangesPending()  && !isNewSelected() && (listView.getSelectionIndex()!=-1);
		 return can;		
	}
	/**
	 * Actually do the copy of the current filter string to the clipboard.
	 */
	public void doCopy()
	{
		if (clipboard == null)
		  clipboard = new Clipboard(getShell().getDisplay());
		
		String selection = getCurrentSelection();
		TextTransfer transfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] {selection}, new Transfer[] {transfer});		 
	}
	/**
	 * Decide if we can do the paste or not.
	 * Will decide the enabled state of the copy action.
	 */
	public boolean canPaste()
	{
		 if (clipboard == null)
		   return false;
		 TextTransfer textTransfer = TextTransfer.getInstance();
		 String textData = (String)clipboard.getContents(textTransfer);
		 return ((textData != null) && (textData.length() > 0));
	}
	/**
	 * Actually do the copy of the current filter string to the clipboard.
	 * If an existing string is selected, it is pasted before it. Else. it is appended to the end of the list.
	 */
	public void doPaste()
	{
		if (clipboard == null)
		  return;
		TextTransfer textTransfer = TextTransfer.getInstance();
		String textData = (String)clipboard.getContents(textTransfer);
		
		String newCopy = new String(textData);
		int newLocation = listView.getSelectionIndex();
	    if (newLocation <= 0)
	    {
		  listView.add(newCopy);
		  newLocation = listView.getItemCount()-1;
		  listView.select(newLocation);
	    }
		else
		{
		  listView.add(newCopy, newLocation);
		  listView.select(newLocation);
		}			        	
        processListSelect(); // defect 45790...
		setPageComplete(verify(false));
	}
	
	// --------------	
	// ERROR CHECKING
	// --------------

  	/**
	 * This hook method is called when ok is pressed. It checks for blatantly duplicate filter strings.
	 * @return filterstring duplicate if there ARE duplicates. NULL if there are no duplicates
	 */
	protected String checkForDuplicates() 
	{			
		if (listView == null)
		  return null;
		String strings[] = (listView != null) ? listView.getItems() : listItems;
		String duplicate = null;
		boolean noDupes = true;
	    for (int idx=1; noDupes && (idx < strings.length); idx++)
	    {
	    	for (int ydx=1; noDupes && (ydx < strings.length); ydx++)
	    	{
	    		if (idx != ydx) 
	    		{
	    		    if (compareFilterStrings(caseSensitiveStrings, strings[idx], strings[ydx]))
	    		    {
	    		      noDupes = false;
	    		      duplicate = strings[idx];
	    		    }
	    		} 
	    	}
	    }
		return duplicate;
	}
	/**
	 * Compares one filter string to another, while searching for duplicates.
	 * Override if you want to do more intelligent compares. This one checks
	 * for equality. The case-sensitivity is specified by the caller. 
	 */
	public boolean compareFilterStrings(boolean caseSensitive, String filterString1, String filterString2)
	{
		boolean cs = caseSensitive;
		if (cs)
		   return filterString1.equals(filterString2);
		else
		   return filterString1.equalsIgnoreCase(filterString2);
	}
	
}