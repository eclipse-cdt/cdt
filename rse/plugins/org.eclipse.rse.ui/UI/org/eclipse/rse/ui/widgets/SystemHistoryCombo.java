/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.widgets;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemWorkWithHistoryDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;


/**
 * This re-usable widget is for a combox box that persists its history and
 *  allows the user to manipulate that history.
 * <p>
 * The composite is layed as follows:</p>
 * <pre><code>
 *   ______________v...
 * </code></pre>
 * @see #updateHistory()
 */
public class SystemHistoryCombo extends Composite implements ISystemCombo, TraverseListener, KeyListener
{
	private Combo    historyCombo = null;
	private Button   historyButton = null;	
	private String   historyKey = null;
	private String[] defaultHistory; // pc41439
	private boolean  readonly = false;
	private boolean  autoUppercase = false;
	
	private int maxComboEntries;								// DY:  Debugger requested we provide support to limit the number of entries
	private static final int DEFAULT_MAX_COMBO_ENTRIES = 20;	// in the combo box for transient data like job name / number.  Note:  this does
													 			// not affect the edit history portion of this widget.  I have guessed at a
													 			// default limit of 20 entries.  
	
	private static final int DEFAULT_COMBO_WIDTH  = 100;
	// dwd private static final int DEFAULT_BUTTON_WIDTH = 10;
	private static final int DEFAULT_BUTTON_WIDTH = 13; // dwd: changed from 10 to accomodate focus rectangle
	private static final int DEFAULT_MARGIN = 1;

	
	/**
	 * Constructor for SystemHistoryCombo
	 * @param parent The owning composite
	 * @param style The swt style to apply to the overall composite. Typically it is just SWT.NULL
	 * @param key The unique string used as a preferences key to persist the history for this widget
	 * @param readonly Set to true for a readonly combo vs user-editable combo box
	 */
	public SystemHistoryCombo(Composite parent, int style, String key, boolean readonly)
	{
		this(parent, style, key, DEFAULT_MAX_COMBO_ENTRIES, readonly);
	}
	
	/**
	 * Constructor for SystemHistoryCombo
	 * @param parent The owning composite
	 * @param style The swt style to apply to the overall composite. Typically it is just SWT.NULL
	 * @param key The unique string used as a preferences key to persist the history for this widget
	 * @param maxComboEntries The number of history entries to show in the combo box.  This only restricts the 
	 *                         combo box not the full history list
	 * @param readonly Set to true for a readonly combo vs user-editable combo box
	 */
	public SystemHistoryCombo(Composite parent, int style, String key, int maxComboEntries, boolean readonly)
	{
		super(parent, style);			
		historyKey = key;	
		this.readonly = readonly;
		prepareComposite(2);
		historyCombo = createCombo(this, readonly);
	    //historyCombo.addTraverseListener(this);
	    historyCombo.addKeyListener(this);
	    //setWidthHint(DEFAULT_COMBO_WIDTH+DEFAULT_BUTTON_WIDTH+DEFAULT_MARGIN);
	    this.maxComboEntries = maxComboEntries;
	    createHistoryButton();
	    String[] history = getHistory();
	    if (history.length > 0)
	    	setItems(history);
        addOurButtonSelectionListener();
	}
	
	/**
	 * Return the combo box widget
	 */
	public Combo getCombo()
	{
		return historyCombo;
	}
	/**
	 * Set the width hint for the combo box widget (in pixels).
	 * Default is only 100, so you may want to set it.
	 * A rule of thumb is 10 pixels per character, but allow 15 for the litte button on the right.
	 * You must call this versus setting it yourself, else you may see truncation.
	 */
	public void setWidthHint(int widthHint)
	{
		// after much research it was decided that it was the wrong thing to do to
		// explicitly set the widthHint of a child widget without our composite, as 
		// that could end up being a bigger number than the composites widthHint itself
		// if the caller set its it directly.
		// Rather, we just set the overall composite width and specify the combo child
		// widget is to grab all the space within that which the little button does not use.
		/*((GridData)historyCombo.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridData)historyCombo.getLayoutData()).horizontalAlignment = GridData.FILL;
	    ((GridData)historyCombo.getLayoutData()).widthHint = widthHint;*/
	    ((GridData)getLayoutData()).widthHint = widthHint + DEFAULT_BUTTON_WIDTH + DEFAULT_MARGIN;	      	    
	}
	
	/**
	 * Set auto-uppercase. When enabled, all non-quoted values are uppercases when added to the history.
	 */
	public void setAutoUpperCase(boolean enable)
	{
		this.autoUppercase = enable;
	}
	
	/**
	 * Return the history button widget
	 */
	public Button getHistoryButton()
	{
		return historyButton;
	}

	/**
	 * Set the combo field's current contents
	 */
	public void setText(String text)
	{
		if (!readonly)
		{
		  historyCombo.setText(text);
		  updateHistory();
		}
		else
		{
		  int selIdx = -1;
		  String[] currentItems = historyCombo.getItems();		  
		  String[] newItems = new String[currentItems.length + 1];
		  newItems[0] = text;
		  for (int idx=0; (selIdx==-1) && (idx<currentItems.length); idx++)
		  {
		  	 if (text.equals(currentItems[idx]))
		  	   selIdx = idx;
		  	 else
		       newItems[idx+1] = currentItems[idx];
		  }		
		  // did not find the given text in the history, so update history...
		  if (selIdx == -1)
		  {
		  	setItems(newItems);
		  	selIdx = currentItems.length;
		    SystemPreferencesManager.getPreferencesManager().setWidgetHistory(historyKey, newItems);				  	
		  }
		  if (selIdx >= 0)
		  {
		  	historyCombo.select(selIdx);
		  }
	    }
	}

	/**
	 * Query the history combo field's current contents
	 */
	public String getText()
	{
		return historyCombo.getText();
	}
	
	/**
	 * Disable/Enable all the child controls.
	 */
	public void setEnabled(boolean enabled)
	{
		historyCombo.setEnabled(enabled);
		historyButton.setEnabled(enabled);		
	}
	/**
	 * Set the tooltip text for the combo field
	 */
	public void setToolTipText(String tip)
	{
		historyCombo.setToolTipText(tip);
	}
	/**
	 * Set the tooltip text for the history button
	 */
	public void setHistoryButtonToolTipText(String tip)
	{
		historyButton.setToolTipText(tip);
	}
	/**
	 * Same as #setHistoryButtonToolTipText(String)
	 */
	public void setButtonToolTipText(String tip)
	{
		historyButton.setToolTipText(tip);
	}

	/**
	 * Set the combo field's text limit
	 */
	public void setTextLimit(int limit)
	{
		historyCombo.setTextLimit(limit);
	}
	/**
	 * Set the focus to the combo field
	 */
	public boolean setFocus()
	{
		return historyCombo.setFocus();
	}
    
    /**
     * Set the items in the combo field
     */
    public void setItems(String[] items)
    {
    	// DY;  Modified to add maxComboSize restriction
	    if ((items != null) && (items.length > maxComboEntries)) 
    	{
			String[] historySubSet = new String[maxComboEntries];
			System.arraycopy(items, 0, historySubSet, 0, maxComboEntries);
			historyCombo.setItems(historySubSet);
    	}
    	else 
    	{
			historyCombo.setItems(items);
    	}
    }
    
    /**
     * Set the items to default the history to, IF the history
     * is currently empty.
     */
    public void setDefaultHistory(String[] items)
    {
    	this.defaultHistory = items; // pc41439
    	if (historyCombo.getItemCount() == 0)
    	{
    	  setItems(items);
    	  SystemPreferencesManager.getPreferencesManager().setWidgetHistory(historyKey, items);	//d41439
    	  //updateHistory();    d41439  
    	}
    }
    
    /**
     * Reset the history key. This changes the contents!
     * You should re-call setDefaultHistory() as well after this, if you had called it before
     */
    public void setHistoryKey(String key)
    {
    	this.historyKey = key;
	    String[] history = getHistory();
	    if (history.length > 0)
	       setItems(history);
	    else
	       historyCombo.removeAll();
    }

    /**
     * Get the items in the combo field
     */
    public String[] getItems()
    {
    	return historyCombo.getItems();
    }
    
    /**
     * Select the combo dropdown list entry at the given index
     */
    public void select(int selIdx)
    {
    	if (selIdx >= historyCombo.getItemCount())
    	  return;
    	historyCombo.deselectAll();
    	historyCombo.select(selIdx);
    	historyCombo.clearSelection(); // so text is not selected
        //sendEvent(SWT.Selection);    	
        // for some reason no event is fired on selection.
        // This overcomes that shortcoming ... uses same solution as jface
		Event e = new Event();
	    //e.time = event.time;
	    //e.stateMask = event.stateMask;
		//e.doit = event.doit;
		historyCombo.notifyListeners(SWT.Selection, e);
    }
    /**
     * Select the given text. This finds the given string in the list,
     * determines its zero-based offset, and calls select for that index.
     * If the item is not found it does nothing. 
     * Returns the index number of the found string, or -1 if not found.
     */
    public int select(String itemText)
    {
    	String[] items = historyCombo.getItems();
    	int matchIdx = -1;
    	if ((items==null) || (items.length==0))
    	  return matchIdx;
    	for (int idx=0; (matchIdx==-1) && (idx<items.length); idx++)
    	  if (itemText.equals(items[idx]))
    	    matchIdx = idx;
    	if (matchIdx != -1)    
    	  select(matchIdx);
    	return matchIdx;
    }
    /**
     * Same as {@link #select(int)}
     */
    public void setSelectionIndex(int selIdx)
    {
    	select(selIdx);
    }
    
    /**
     * Clear the selection of the text in the entry field part of the combo.
     * Also deselects the list part.
     */
    public void clearSelection()
    {
    	//if (!readonly)
    	historyCombo.clearSelection();
    	//else
    	historyCombo.deselectAll();
    }
    /**
     * Clear the selection of the text in the entry field part of the combo.
     * Does not deselect the list part.
     */
    public void clearTextSelection()
    {
    	//historyCombo.clearSelection();
    	String text = historyCombo.getText();
    	historyCombo.deselectAll();
    	historyCombo.setText(text);
    }

    /**
     * Get the index number of the currently selected item. Only really 
     * reliable in readonly mode.
     */
    public int getSelectionIndex()
    {
    	return historyCombo.getSelectionIndex();
    }

	/**
	 * Register a listener interested in an item is selected in the combo box
     * @see #removeSelectionListener(SelectionListener)
     */
    public void addSelectionListener(SelectionListener listener) 
    {
	    historyCombo.addSelectionListener(listener);
    }
    /** 
     * Remove a previously set combo box selection listener.
     * @see #addSelectionListener(SelectionListener)
     */
    public void removeSelectionListener(SelectionListener listener) 
    {
	    historyCombo.removeSelectionListener(listener);
    }
	/**
	 * Register a listener interested in entry field modify events
     * @see #removeModifyListener(ModifyListener)
     */
    public void addModifyListener(ModifyListener listener) 
    {
	    historyCombo.addModifyListener(listener);
    }
    /** 
     * Remove a previously set entry field listener.
     * @see #addModifyListener(ModifyListener)
     */
    public void removeModifyListener(ModifyListener listener) 
    {
	    historyCombo.removeModifyListener(listener);
    }
	
	/**
	 * Return the current history for the directory combo box
	 */
	public String[] getHistory()
	{
		return SystemPreferencesManager.getPreferencesManager().getWidgetHistory(historyKey);
	}
	/**
	 * Update the history with current entry field setting, but don't refresh contents.
	 * <p>
	 * This is called automatically for you when setText is called. However, for non-readonly
	 *   versions, you should still call this yourself when OK is successfully pressed on the
	 *   dialog box.
	 */
	public void updateHistory()
	{
		updateHistory(false);
	}
	/**
	 * Update the history with current entry field setting, and optionally refresh the list from the new history
	 * <p>
	 * This is called automatically for you when setText is called. However, for non-readonly
	 *   versions, you should still call this yourself when OK is successfully pressed on the
	 *   dialog box.
	 */
	public void updateHistory(boolean refresh)
	{
		String textValue = historyCombo.getText().trim();		 
		if (autoUppercase)
		  if (!(textValue.startsWith("\"")&& textValue.endsWith("\""))) 	 
		    textValue = textValue.toUpperCase(); 
		boolean alreadyThere = false;
		String[] newHistory = null;
		if (textValue.length() > 0)
		{
			// d41463 - seletced item should go to the top 
			String[] currentHistory = historyCombo.getItems();
			if ( currentHistory.length > 0)
			{	    
			   if (!textValue.equals(currentHistory[0]))
			   {
				  alreadyThere = false;
				  // if string exists
		          for (int idx=0; !alreadyThere && (idx<currentHistory.length); idx++)
		          {
		            if (textValue.equals(currentHistory[idx]))
		  	          alreadyThere = true;		  	 
		          }
		          
		          if (alreadyThere)
				     newHistory = new String[currentHistory.length];
				  else
				  {				  	
				  	newHistory = new String[currentHistory.length >= maxComboEntries ? maxComboEntries : currentHistory.length + 1];
				  }
		          newHistory[0] = textValue;
		          int idx2 = 1;
		          // copy the rest
		          for (int idx=0; idx< currentHistory.length && idx2 < newHistory.length; idx++)
		          {
		  	         if (!textValue.equals(currentHistory[idx]))
		  	         {		  	 
		                newHistory[idx2] = currentHistory[idx];
		                ++idx2;
		  	         }   
		          }	            
		          SystemPreferencesManager.getPreferencesManager().setWidgetHistory(historyKey, newHistory);				  
			   }   
		    }
		    else
		    {
		       newHistory =new String[1]; 
		       newHistory[0] = textValue;
		       SystemPreferencesManager.getPreferencesManager().setWidgetHistory(historyKey, newHistory);				  
		    }   	
		    if (refresh && (newHistory != null))
		    {
		    	setItems(newHistory);		    	
		    	setText(textValue);
		    }
		  /*	
		  String[] currentHistory = historyCombo.getItems();		  
		  String[] newHistory = new String[currentHistory.length + 1];
		  newHistory[0] = textValue;
		  boolean alreadyThere = false;
		  for (int idx=0; !alreadyThere && (idx<currentHistory.length); idx++)
		  {
		  	 if (textValue.equals(currentHistory[idx]))
		  	   alreadyThere = true;
		  	 else
		       newHistory[idx+1] = currentHistory[idx];
		  }
		  if (!alreadyThere)
		    SystemPreferencesManager.getGlobalPreferences().setWidgetHistory(historyKey, newHistory);		
		  */  
		}
	}
	/**
	 * Set the history to the given array of strings. Replaces what is there.
	 */
	public void setHistory(String[] newHistory)
	{
		SystemPreferencesManager.getPreferencesManager().setWidgetHistory(historyKey, newHistory);
	}
	
	// -----------------------
	// INTERNAL-USE METHODS...
	// -----------------------
	/**
	 * Prepares this composite control and sets the default layout data.
	 * @param Number of columns the new group will contain.     
	 */
	protected Composite prepareComposite(int numColumns)
	{
		Composite composite = this;
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;		
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		// horizontal data...
		data.horizontalAlignment = GridData.FILL;
	    data.grabExcessHorizontalSpace = true;        
	    data.widthHint = DEFAULT_COMBO_WIDTH+DEFAULT_BUTTON_WIDTH+DEFAULT_MARGIN;	    
		// vertical data...
		data.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
	    data.grabExcessVerticalSpace = false;	    

		composite.setLayoutData(data);
		return composite;
	}	
	/**
	 * Creates a new combobox instance and sets the default
	 * layout data.
	 * <p>
	 * Does NOT set the widthHint as that causes problems. Instead the combo will
	 * consume what space is available within this composite.
	 * @param parent composite to put the button into.
	 */
	public static Combo createCombo(Composite parent, boolean readonly)
	{
		Combo combo = null;
		if (!readonly)
		  combo = new Combo(parent, SWT.DROP_DOWN);
		else
		  combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
	    GridData data = new GridData();
	    data.horizontalAlignment = GridData.FILL;
	    data.grabExcessHorizontalSpace = true;
	    data.verticalAlignment = GridData.CENTER;
	    data.grabExcessVerticalSpace = false;
	    //System.out.println("Default widthHint = " + data.widthHint);
	    combo.setLayoutData(data);	   
	    return combo;
	}

	protected void addOurButtonSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
		  	  historyButtonPressed();
		  }
		  public void widgetDefaultSelected(SelectionEvent event) 
		  {
		  	  widgetSelected(event);  
		  }

	   };
	   historyButton.addSelectionListener(selectionListener);
	}
	
	protected void historyButtonPressed()
	{
		SystemWorkWithHistoryDialog dlg = new SystemWorkWithHistoryDialog(getShell(), getHistory());
		if (defaultHistory != null)
		  dlg.setDefaultHistory(defaultHistory);
		dlg.setBlockOnOpen(true);		  	  
		dlg.open();		  	  
		if (!dlg.wasCancelled())
		{
		  	String value = historyCombo.getText();		  	 // d41471 	 
		  	String[] newHistory = dlg.getHistory();
		    SystemPreferencesManager.getPreferencesManager().setWidgetHistory(historyKey, newHistory);				  	  	
		    setItems(newHistory);
		    historyCombo.setText(value);  // Restore the value  d41471
		}
	}
   
    protected Button createHistoryButton()
    {
		/*
		dwd: modified for defect 57974 - tab enable, provide focus rectangle, and accessibility text for history button.  Original
		scheme used an SWT.ARROW button style which was not tab enabled and could not provide a focus rectangle.  
		Changes: made the control a push button, programmatically drew the arrow on the button, and provided accessibility information. 
		*/
	    historyButton = new Button(this, SWT.PUSH);
		Display display = this.getDisplay();
		final Image upArrow = new Image(display, 5, 6);
		GC gc = new GC(upArrow);
		gc.setBackground(historyButton.getBackground());
		gc.fillRectangle(upArrow.getBounds());
		gc.setForeground(historyButton.getForeground());
		gc.drawLine(0, 5, 4, 5);
		gc.drawLine(0, 4, 4, 4);
		gc.drawLine(1, 3, 3, 3);
		gc.drawLine(1, 2, 3, 2);
		gc.drawLine(2, 1, 2, 1);
		gc.drawLine(2, 0, 2, 0);
		gc.dispose();
		historyButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				upArrow.dispose();
			}
		});
		historyButton.setImage(upArrow);
	    historyButton.setToolTipText(SystemResources.RESID_WORKWITHHISTORY_BUTTON_TIP);
		historyButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) { // this is the one that should supply the text heard.
				e.result = historyButton.getToolTipText();
			}
			public void getName(AccessibleEvent e) { // this is the one that apparently does supply the text heard.
				e.result = historyButton.getToolTipText();
			}
		});

	    GridData data = new GridData();	    
	    data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
	    data.grabExcessHorizontalSpace = false ;	     
	    data.widthHint = DEFAULT_BUTTON_WIDTH;	    
	    data.verticalAlignment = GridData.CENTER;
	    data.grabExcessVerticalSpace = true;
	    data.heightHint = 20;
	    historyButton.setLayoutData(data);	 
        return historyButton;
    }	
	
	public void setHistoryButtonHeight(int height)
	{
		((GridData)historyButton.getLayoutData()).heightHint = height;
		((GridData)historyButton.getLayoutData()).grabExcessVerticalSpace = false;
		((GridData)historyButton.getLayoutData()).verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
	}

    public void keyTraversed(TraverseEvent e)
    {
    	int detail = e.detail;
    	String s = "unknown";
    	switch (detail)
    	{
    		case SWT.TRAVERSE_ARROW_NEXT:     s="Arrow Next";     break;
    		case SWT.TRAVERSE_ARROW_PREVIOUS: s="Arrow Previous"; break;
    		case SWT.TRAVERSE_ESCAPE:         s="Escape";         break;
    		case SWT.TRAVERSE_RETURN:         s="Return";         break;
    		case SWT.TRAVERSE_TAB_NEXT:       
    		    s="Tab Next"; 
    		    //historyButton.setFocus(); 
    		    historyButton.forceFocus();
    		    //e.doit=false; 
    		    break;
    		case SWT.TRAVERSE_TAB_PREVIOUS:   s="Tab Previous";   break;
    	}
    	System.out.println("keyTraversed: "+s);
    }
    
    public void keyPressed(KeyEvent e)
    {
    	
    }
    public void keyReleased(KeyEvent e)
    {
    	if ((e.stateMask == SWT.CTRL) && (e.keyCode == SWT.ARROW_UP))
    	{
    		historyButtonPressed();
    	}
    }
	
}