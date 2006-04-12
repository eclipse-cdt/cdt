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

package org.eclipse.rse.ui.dialogs;

import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;



/**
 * A dialog that allows the user to manipulate the history associated with
 *  a widget.
 * <p>
 * The history strings are shown in a simple list, and the user can delete
 * items from the list or re-order items in the list.
 */
public class SystemWorkWithHistoryDialog extends SystemPromptDialog implements ISystemIconConstants, Listener, ArmListener
{
	private String[] historyInput;
	private String[] historyOutput;
	private String[] defaultHistory;
	private Label verbage;
	private List  historyList;
	private Button rmvButton, clearButton, mupButton, mdnButton;
	private Group group;
	protected Menu        popupMenu;
	protected MenuItem    clearMI, rmvMI, mupMI, mdnMI;

	
	/**
	 * Constructor for SystemWorkWithHistoryDialog
	 */
	public SystemWorkWithHistoryDialog(Shell shell, String[] history)
	{
		super(shell, SystemResources.RESID_WORKWITHHISTORY_TITLE);
		historyInput = history;

		//pack();
        setHelp(RSEUIPlugin.HELPPREFIX+"dwwh0000");
        setInitialOKButtonEnabledState(false);     //d41471
	}

    /**
     * Set the items to default the history to. These are sacred and can't be
     * deleted in this dialog.
     */
    public void setDefaultHistory(String[] items)
    {
    	this.defaultHistory = items; // pc41439
    }
    /**
     * Return true if the given string is among the default history items
     */
    private boolean inDefaultHistory(String toTest) // pc41439
    {
    	boolean inDefault = false;
    	if (defaultHistory != null)
    	  for (int idx=0; !inDefault && (idx<defaultHistory.length); idx++)
    	     if (defaultHistory[idx].equals(toTest))
    	       inDefault = true;
    	return inDefault;
    }
    /**
     * Return true any of the currently selected strings are among the default history items
     */
    private boolean selectedInDefaultHistory() /// pc41439
    {
    	boolean inDefault = false;
    	if (defaultHistory != null)
    	{
          String[] currSelection = historyList.getSelection();
          if (currSelection != null)
    	  for (int idx=0; !inDefault && (idx<currSelection.length); idx++)
             inDefault = inDefaultHistory(currSelection[idx]);      
    	}
    	return inDefault;
    }

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl()
	{
		return historyList;
	}
	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent)
	{
		// Inner composite
		int nbrColumns = 2;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		
		// verbage
		verbage = SystemWidgetHelpers.createLabel(composite,SystemResources.RESID_WORKWITHHISTORY_VERBAGE, nbrColumns);
		
		// History list
		init(composite, nbrColumns);
		
		return composite;
	}
	/**
	 * Create and initialize labeled group
	 */
	protected Group init(Composite parent, int nbrColumns)
	{
		group = SystemWidgetHelpers.createGroupComposite(parent,nbrColumns,SystemResources.RESID_WORKWITHHISTORY_PROMPT);
		//((GridData)group.getLayoutData()).horizontalSpan =horizontalSpan;
		//((GridData)group.getLayoutData()).verticalSpan =verticalSpan;		
	    // CREATE THE LIST BOX
		historyList = SystemWidgetHelpers.createListBox(group,null,null,false);		
		((GridData)historyList.getLayoutData()).widthHint = 200;
		//((GridData)list.getLayoutData()).horizontalSpan = listboxspan;
		//((GridData)list.getLayoutData()).heightHint = SWT.DEFAULT;          
		//((GridData)list.getLayoutData()).heightHint = buttonHeight * maxButtonsHigh;          		
	    ((GridData)group.getLayoutData()).grabExcessVerticalSpace = true;
		
		popupMenu = new Menu(historyList);
		
	    Composite rightHandSide = SystemWidgetHelpers.createComposite(group,1);          	
	    clearButton = createPushButton(rightHandSide,SystemResources.ACTION_HISTORY_CLEAR_LABEL, SystemResources.ACTION_HISTORY_CLEAR_TOOLTIP);
	    rmvButton = createPushButton(rightHandSide,SystemResources.ACTION_HISTORY_DELETE_LABEL, SystemResources.ACTION_HISTORY_DELETE_TOOLTIP);
	    mupButton = createPushButton(rightHandSide,SystemResources.ACTION_HISTORY_MOVEUP_LABEL, SystemResources.ACTION_HISTORY_MOVEUP_TOOLTIP);
	    mdnButton = createPushButton(rightHandSide,SystemResources.ACTION_HISTORY_MOVEDOWN_LABEL, SystemResources.ACTION_HISTORY_MOVEDOWN_LABEL);            
		
		clearMI = createMenuItem(SystemResources.ACTION_HISTORY_CLEAR_LABEL, SystemResources.ACTION_HISTORY_CLEAR_TOOLTIP);
		rmvMI = createMenuItem(SystemResources.ACTION_HISTORY_DELETE_LABEL, SystemResources.ACTION_HISTORY_DELETE_TOOLTIP);
		mupMI = createMenuItem(SystemResources.ACTION_HISTORY_MOVEUP_LABEL, SystemResources.ACTION_HISTORY_MOVEUP_TOOLTIP);
		mdnMI = createMenuItem(SystemResources.ACTION_HISTORY_MOVEDOWN_LABEL, SystemResources.ACTION_HISTORY_MOVEDOWN_TOOLTIP);        
				
		historyList.setMenu(popupMenu);
		historyList.setItems(historyInput);
		
		clearButton.setEnabled((historyInput!=null) && (historyInput.length>0));
		rmvButton.setEnabled(false);		
		mupButton.setEnabled(false);		
		mdnButton.setEnabled(false);			
		clearMI.setEnabled((historyInput!=null) && (historyInput.length>0));
		rmvMI.setEnabled(false);		
		mupMI.setEnabled(false);		
		mdnMI.setEnabled(false);					
				
		// add selection listeners...
		historyList.addSelectionListener(
			new SelectionListener() 
			{
				public void widgetDefaultSelected(SelectionEvent e) 
				{
					//processRemoveButton(); // equate to selecting Remove...
					okButton.setEnabled(true);		  //d41471
				  	enableDisableAllActions();            		  
				}
				public void widgetSelected(SelectionEvent e) 
				{
					okButton.setEnabled(true);		  //d41471
					enableDisableAllActions();
				}
			}
		);        		
		return group;   		
	} // end init common
	/**
	 * Helper method for a separator menu item
	 */
	protected MenuItem addSeparator(Menu parent)
	{
		MenuItem mi = new MenuItem(parent,SWT.SEPARATOR);
		return mi;		
	}
	/**
	 * Helper method for creating cascading menu item
	 */
	protected MenuItem createMenuItem(String label, String description)
	{
		MenuItem mi = new MenuItem(popupMenu,SWT.NULL);
		mi.setText(label);
		mi.setData(description);
		mi.addArmListener(this);
		mi.addListener(SWT.Selection, this);
		return mi;		
	}
	/**
	 * Helper method for creating a pushbutton with tooltip text. 
	 * This method takes resolved label and tooltip values
	 */
    protected Button createPushButton(Composite c, String label, String tooltip)
    {
   	      Button button = SystemWidgetHelpers.createPushButton(c, this, label, tooltip);		
		  return button;   	
    }                           	
	/**
	 * Helper method to enable/disable all actions (buttons, menuitems) 
	 * based on current selections in list or current text contents.
	 */
	private void enableDisableAllActions()
	{
		boolean itemSelected = (historyList.getSelectionCount() > 0);
		boolean defaultItemSelected = itemSelected && selectedInDefaultHistory();
		// Remove action
		rmvButton.setEnabled(itemSelected && !defaultItemSelected);
		rmvMI.setEnabled(itemSelected && !defaultItemSelected);
		// Move Up action
		mupButton.setEnabled(itemSelected && !historyList.isSelected(0));    	
		mupMI.setEnabled(mupButton.isEnabled());
		// Move Down action
		mdnButton.setEnabled(itemSelected && !historyList.isSelected((historyList.getItemCount())-1));
		mupMI.setEnabled(mdnButton.isEnabled());
			
	    int emptyListCount = 0;
	    if (defaultHistory != null)
	      emptyListCount = defaultHistory.length;
		clearButton.setEnabled(historyList.getItemCount() > emptyListCount);
	}

	protected boolean processOK() 
	{
		historyOutput = historyList.getItems();		 
		return true;
	}	

	// --------------------------------- //
	// METHODS FOR INTERFACES... 
	// --------------------------------- //
	/**
	 * Handles events generated by controls on this page.
	 */
	public void handleEvent(Event e)
	{
		//get widget that generates the event
	    clearMessage();
	    Widget source = e.widget;
	    if ((source == clearButton) ||
	         (source == clearMI))	    	    
	    {
	    	historyList.removeAll();
	    	if (defaultHistory != null)
	    	  historyList.setItems(defaultHistory); // pc41439
	    	okButton.setEnabled(true);           //d41471
	    	enableDisableAllActions();           //d41421
        }
	    else if ((source == rmvButton) ||
	             (source == rmvMI))	    	    
	    {
	        int seld[] = historyList.getSelectionIndices();
	      	if (seld.length > 0)
	      	{
	 	       historyList.remove(seld);
	 	       enableDisableAllActions();         //d41421
	      	}  
	    }
	    else if ((source == mupButton) ||
	             (source == mupMI))	    	    
	    {
	      	int oldIndex = historyList.getSelectionIndex();
	      	if (oldIndex > 0)
	      	{
	      	    String seldObj = historyList.getSelection()[0];
	      	    historyList.remove(oldIndex);
	      	    historyList.add(seldObj,oldIndex-1);
	      	    historyList.select(oldIndex-1);	      	   
	      	    historyList.showSelection();	      	     
	      	    enableDisableAllActions();        //d41421
	      	}
	    }
	    else if ((source == mdnButton) ||
	             (source == mdnMI))	    	    
	    {
	      	int oldIndex = historyList.getSelectionIndex();
	      	if ((oldIndex >= 0) && (oldIndex < (historyList.getItemCount()-1)))
	      	{	      		 
	      	    String seldObj = historyList.getSelection()[0];
	      	    historyList.remove(oldIndex);
	      	    historyList.add(seldObj,oldIndex+1);
	      	    //historyList.select(oldIndex+1);
	      	    // historyList.showSelection();
	      	    historyList.select(oldIndex+2);  //d41427 To get around eclipse bug which does
	      	    historyList.showSelection();     //d41427 not show the selected one as expected.
	      	    historyList.select(oldIndex+1);  //d41427 Need to advance one more to show it
	      	    enableDisableAllActions();	     //d41421  	    
	      	}	      	
	    }
		
	}	
	/**
	 * Called by system when menu item is "armed" or in select state. 
	 * We use this to show the menu item description.
	 */
	public void widgetArmed(ArmEvent e) 
	{
	    clearMessage();
		Widget w = e.widget;
		if (w instanceof MenuItem)
		{
		  MenuItem mi = (MenuItem)w;
		  String desc = (String)mi.getData();
	      setMessage(desc);
		}
	}    
    
    // -----------------------
    // CALLER QUERY METHODS...
    // -----------------------
    	
	/**
	 * Return the updated history
	 */
	public String[] getHistory()
	{
		return historyOutput;
	}
}