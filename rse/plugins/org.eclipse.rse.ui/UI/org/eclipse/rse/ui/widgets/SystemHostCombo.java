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
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * This re-usable widget is for selecting a connection from the master list of connections.
 * The list is subsettable by one or more system types.
 * There is also the option of including a "New" button to the right of the connection 
 * dropdown, for the purpose of creating a new connection.
 * <p>
 * Without the New button, the composite is layed as follows:</p>
 * <pre><code>
 *   Connection: ______________________v  
 * </code></pre>
 * <p>
 * With the New button, the composite is layed as follows:</p>
 * <pre><code>
 *   Connection: ______________v  New...
 * </code></pre>
 * <p>
 * There are numerous ways to subset the connection list:</p>
 * <ul>
 * <li>By system type, either by a single type or an array of types. Only connections of these types are listed.
 * <li>By subsystem factory. Only connections with subsystems owned by the given subsystem factory are listed.
 * <li>By subsystem factory category. Only connections which contain subsystems owned by subsystem factories which
 *       are defined in their xml extension point as being of the given category are listed. 
 *       For a list of pre-defined categories, see {@link org.eclipse.rse.model.ISubSystemFactoryCategories}.
 * </ul>
 */
public class SystemHostCombo extends Composite implements ISelectionProvider, ISystemCombo,
																org.eclipse.rse.model.ISystemResourceChangeListener,
			                                                      ISystemResourceChangeEvents, DisposeListener
{
	protected Label              connectionLabel = null;
	protected Combo              connectionCombo = null;
	protected Button             newButton = null;	
	protected boolean           showNewButton = true;
	protected boolean 		  showLabel = true;
	protected boolean           showQualifiedNames;
    protected boolean		    listeningForConnectionEvents = false;
    private IHost[] connections = null;
    private SystemNewConnectionAction newConnectionAction = null;
    private String[]           restrictSystemTypesTo = null;
    private int                gridColumns = 2;
	//private static final int DEFAULT_COMBO_WIDTH = 300;
	//private static final int DEFAULT_BUTTON_WIDTH = 80;
	private String             label;
	private String             populateSystemType = null;			/* used as criteria when refresh is done */
	private String[]           populateSystemTypes = null;			/* used as criteria when refresh is done */
	private ISubSystemConfiguration   populateSSFactory = null;			/* used as criteria when refresh is done */
	private String             populateSSFactoryId = null;			/* used as criteria when refresh is done */
	private String             populateSSFactoryCategory = null;	/* used as criteria when refresh is done */
	private Cursor             waitCursor;
		
	/**
	 * Constructor for SystemConnectionCombo when there is only a single system type to restrict the connection list to.
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget. Typically just pass SWT.NULL
	 * @param systemType the system type to restrict the connection list to. Can be null or * for all.
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param showNewButton true if a New... button is to be included in this composite
	 */
	public SystemHostCombo(Composite parent, int style, String systemType, IHost defaultConnection, boolean showNewButton)
	{
		super(parent, style);		
	    restrictSystemTypesTo = new String[1];
	    restrictSystemTypesTo[0] = systemType;
		init(parent, showNewButton);	
		populateSystemType = systemType;
	    populateConnectionCombo(connectionCombo, systemType, defaultConnection, true);
        setConnectionToolTipText();
	    addOurConnectionSelectionListener();
	}
	/**
	 * Constructor for SystemConnectionCombo when there is an array of system types to restrict the connection list to.
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget. Typically just pass SWT.NULL
	 * @param systemTypes the system type array to restrict the connection list to.
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param showNewButton true if a New... button is to be included in this composite
	 */
	public SystemHostCombo(Composite parent, int style, String[] systemTypes, IHost defaultConnection, boolean showNewButton)
	{
		super(parent, style);		
	    restrictSystemTypesTo = systemTypes;
		init(parent, showNewButton);	
		populateSystemTypes = systemTypes;
	    populateConnectionCombo(connectionCombo, systemTypes, defaultConnection);
        setConnectionToolTipText();
	    addOurConnectionSelectionListener();
	}
	/**
	 * Constructor for SystemConnectionCombo when there is a subsystem factory to restrict the list to.
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget. Typically just pass SWT.NULL
	 * @param subsystemFactory. Only connections with subsystems owned by this factory are returned.
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param showNewButton true if a New... button is to be included in this composite
	 */
	public SystemHostCombo(Composite parent, int style, ISubSystemConfiguration ssFactory, IHost defaultConnection, boolean showNewButton)
	{
		super(parent, style);	
	    restrictSystemTypesTo = ssFactory.getSystemTypes();
		init(parent, showNewButton);	
		populateSSFactory = ssFactory;
	    populateConnectionCombo(connectionCombo, ssFactory, defaultConnection);
        setConnectionToolTipText();
	    addOurConnectionSelectionListener();
	}
	/**
	 * Constructor for SystemConnectionCombo when there is a subsystem factory id to restrict the list to.
	 * To avoid collision with the constructor that takes a string for the system type, this one places the 
	 * subystem factory Id string parameter after the defaultConnection constructor
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget. Typically just pass SWT.NULL
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param subsystemFactoryId. Only connections with subsystems owned by this factory are returned. 
	 * @param showNewButton true if a New... button is to be included in this composite
	 */
	public SystemHostCombo(Composite parent, int style, IHost defaultConnection, String ssFactoryId, boolean showNewButton)
	{
		super(parent, style);	
	    restrictSystemTypesTo = RSEUIPlugin.getTheSystemRegistry().getSubSystemConfiguration(ssFactoryId).getSystemTypes();
		init(parent, showNewButton);	
		populateSSFactoryId = ssFactoryId;
	    populateConnectionCombo(connectionCombo, ssFactoryId, defaultConnection);
        setConnectionToolTipText();
	    addOurConnectionSelectionListener();
	}

	/**
	 * Constructor for SystemConnectionCombo when there is a subsystem factory category to restrict the list to.
	 * To avoid collision with the constructor that takes a string for the system type, this one places the 
	 * string parameter at the end.
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget. Typically just pass SWT.NULL
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param showNewButton true if a New... button is to be included in this composite
	 * @param subsystemFactoryCategory. Only connections with subsystems owned by factories of this category are returned.
	 */
	public SystemHostCombo(Composite parent, int style, IHost defaultConnection, boolean showNewButton, String ssFactoryCategory)
	{
		this(parent, style, defaultConnection, showNewButton, ssFactoryCategory, true);
	}

	/**
	 * Constructor for SystemConnectionCombo when there is a subsystem factory category to restrict the list to.
	 * To avoid collision with the constructor that takes a string for the system type, this one places the 
	 * string parameter at the end.
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget. Typically just pass SWT.NULL
	 * @param defaultConnection the system connection to preselect. Pass null to preselect first connection.
	 * @param showNewButton true if a New... button is to be included in this composite
	 * @param subsystemFactoryCategory. Only connections with subsystems owned by factories of this category are returned.
	 * @param showLabel. true if a 'Connection' label is to be included in this composite
	 */
	public SystemHostCombo(Composite parent, int style, IHost defaultConnection, boolean showNewButton, String ssFactoryCategory, boolean showLabel)
	{
		super(parent, style);	
		if (showNewButton) // this is expensive, so only need to do this if New is enabled
		{
		  ISubSystemConfigurationProxy[] ssfProxies = RSEUIPlugin.getTheSystemRegistry().getSubSystemConfigurationProxiesByCategory(ssFactoryCategory);
		  Vector vTypes = new Vector();
		  for (int idx=0; idx<ssfProxies.length; idx++)
		  {
			String[] types = ssfProxies[idx].getSystemTypes();
			for (int jdx=0; jdx<types.length; jdx++)
			{
			   if (!vTypes.contains(types[jdx]))
			     vTypes.addElement(types[jdx]);
			}			
		  }
		  restrictSystemTypesTo = new String[vTypes.size()];
		  for (int idx=0; idx<vTypes.size(); idx++)
		     restrictSystemTypesTo[idx] = (String)vTypes.elementAt(idx);
		}	
		init(parent, showNewButton, showLabel);	
		populateSSFactoryCategory = ssFactoryCategory;
	    populateConnectionCombo(connectionCombo, defaultConnection, ssFactoryCategory);
        setConnectionToolTipText();
	    addOurConnectionSelectionListener();
	}

	/**
	 * Set auto-uppercase. When enabled, all non-quoted values are uppercases when added to the history.
	 * <p>
	 * This method is part of ISystemCombo, so we must support it, but it does not apply this combo widget since the
	 *  contents are read-only. Hence, it does nothing!
	 */
	public void setAutoUpperCase(boolean enable)
	{
		
	}


    protected void init(Composite parent, boolean showNewButton)
    {
    	init(parent, showNewButton, true);
    }


    protected void init(Composite parent, boolean showNewButton, boolean showLabel)
    {
		this.showNewButton = showNewButton;
		this.showLabel = showLabel;
		showQualifiedNames = SystemPreferencesManager.getPreferencesManager().getQualifyConnectionNames();
		//prepareComposite(showNewButton ? 3 : 2);
		prepareComposite(3);

		if ( showLabel )
		    //connectionLabel = SystemWidgetHelpers.createLabel(this,rb,ISystemConstants.WIDGET_CONNECTION_ROOT);
		{
			if (label == null)
				connectionLabel = SystemWidgetHelpers.createLabel(this,SystemResources.WIDGET_CONNECTION_LABEL);
			else
				connectionLabel = SystemWidgetHelpers.createLabel(this,label);
		}		    
	    connectionCombo = createConnectionCombo(this);
	    if (showNewButton)
	    {
	      //newConnectionAction = new SystemNewConnectionAction(parent.getShell(), false, this);
	      newConnectionAction = getNewConnectionAction(parent.getShell(), this);
		  newConnectionAction.restrictSystemTypes(restrictSystemTypesTo);
	      newButton = createPushButton(this,SystemResources.WIDGET_BUTTON_NEWCONNECTION_LABEL,SystemResources.WIDGET_BUTTON_NEWCONNECTION_TOOLTIP);
	      addOurButtonSelectionListener();
	      if ( !showLabel )
	        ((GridData)connectionCombo.getLayoutData()).horizontalSpan = 2;	    
	    }
	    else
	    {
	        if( showLabel )
		        ((GridData)connectionCombo.getLayoutData()).horizontalSpan = 2;	    
		    else
		        ((GridData)connectionCombo.getLayoutData()).horizontalSpan = 3;	    
	    }
	    addDisposeListener(this);
    }
	/**
	 * Overridable method.
	 * Returns action to be called when New... pressed.
	 */
	protected SystemNewConnectionAction getNewConnectionAction(Shell shell, ISelectionProvider selectionProvider)
	{
		return new SystemNewConnectionAction(shell, false, selectionProvider);
	}
	
    /**
     * Get the user selected SystemConnection object.
     * Might be null if the list is empty.
     */
    public IHost getHost()
    {
        IHost connection = null;    	
    	int idx = connectionCombo.getSelectionIndex();
    	if ((idx >= 0) && (connections!=null) && (idx<connections.length))
    	  connection = connections[idx];
        return connection;
    }
    
	/**
	 * Return the combo box widget
	 */
	public Combo getCombo()
	{
		return connectionCombo;
	}
	/**
	 * Set the width hint for this whole composite
	 * Default is 180.
	 */
	public void setWidthHint(int widthHint)
	{
		// after much research it was decided that it was the wrong thing to do to
		// explicitly set the widthHint of a child widget without our composite, as 
		// that could end up being a bigger number than the composites widthHint itself
		// if the caller set its it directly.
		// Rather, we just set the overall composite width and specify the combo child
		// widget is to grab all the space within that which the little button does not use.
	    ((GridData)getLayoutData()).widthHint = widthHint;
	}
	/**
	 * Set button width hint
	 */
	public void setButtonWidthHint(int widthHint)
	{
		if (newButton != null)
		{
	      ((GridData)newButton.getLayoutData()).widthHint = widthHint;			
		}
	}
	/**
	 * Set button width hint, based on the width of another widget
	 */
	public void setButtonWidthHint(Control otherWidget)
	{
		if (newButton != null)
		{
			//System.out.println("Curr button width       = " + newButton.getSize().x);
			//System.out.println("Setting button width to = " + otherWidget.getSize().x);
	        ((GridData)newButton.getLayoutData()).widthHint = otherWidget.getSize().x;			
			//System.out.println("New button width        = " + newButton.getSize().x);
		}
		//else
		//  System.out.println("New button is null");
	}
	
    /**
     * Return the number of grid data columns within this composite.
     * Will vary depending if there is a New button or not.
     */
    public int getGridColumns()
    {
    	return gridColumns;
    }
	/**
	 * Return the New... button widget
	 */
	public Button getNewButton()
	{
		return newButton;
	}
    /**
     * Get the prompt Label widget
     */
    public Label getPromptLabel()
    {
    	return connectionLabel;
    }
    /**
     * Set the items in the combo field
     */
    public void setItems(String[] items)
    {
    	connectionCombo.setItems(items);
    }
    /**
     * Get the items in the combo field
     */
    public String[] getItems()
    {
    	return connectionCombo.getItems();
    }
    /**
     * Return the text in the connection combo entry field.
     * This is only of limited value. You should call getSystemConnection() instead.
     */
    public String getText()
    {
    	return connectionCombo.getText();
    }
	/**
	 * Disable/Enable all the child controls.
	 */
	public void setEnabled(boolean enabled)
	{
		connectionCombo.setEnabled(enabled);
		if (newButton != null)
		  newButton.setEnabled(enabled);		
	}
	/**
	 * Set the tooltip text for the directory combo field
	 */
	public void setToolTipText(String tip)
	{
		if ( connectionLabel != null )
			connectionLabel.setToolTipText(tip);
		//connectionCombo.setToolTipText(tip);
	}
	/**
	 * Set the tooltip text for the new button
	 */
	public void setNewButtonToolTipText(String tip)
	{
		if (newButton != null)
	  	  newButton.setToolTipText(tip);
	}
	/**
	 * Same as {@link #setNewButtonToolTipText(String)}
	 */
	public void setButtonToolTipText(String tip)
	{
		setNewButtonToolTipText(tip);
	}
	
	/**
	 * Set the label to use for the prompt.
	 */
	public void setLabel(String label)
	{
		this.label = label;
		if (connectionLabel != null)
			connectionLabel.setText(label);
	}
	
	/**
	 * Set the focus to the directory combo field
	 */
	public boolean setFocus()
	{
		return connectionCombo.setFocus();
	}
	/**
	 * Set the focus to the new button
	 */
	public void setNewButtonFocus()
	{
		if (newButton != null)
		  newButton.setFocus();
	}

    /**
     * Select the combo dropdown list entry at the given index.
     */
    public void select(int selIdx)
    {
    	connectionCombo.select(selIdx);
    	//if (fireEvent)
    	{
		  Event e = new Event();
	      //e.time = event.time;
	      //e.stateMask = event.stateMask;
		  //e.doit = event.doit;
		  connectionCombo.notifyListeners(SWT.Selection, e);    		
    	}
    }
    /**
     * Same as {@link #select(int)}
     */
    public void setSelectionIndex(int selIdx)
    {
    	select(selIdx);
    }
    /**
     * Select a connection from the dropdown
     */
    public void select(IHost connection)
    {
        if (connections != null)
        {
        	int matchIdx = -1;
        	for (int idx=0; (matchIdx==-1) && (idx<connections.length); idx++)
        	   if (connection == connections[idx])
        	     matchIdx = idx;
        	if (matchIdx != -1)
        	  select(matchIdx);
        }
    }

    
    /**
     * Deselect
     */
    public void clearSelection()
    {
    	connectionCombo.clearSelection();
    	connectionCombo.deselectAll();
		Event e = new Event();
	    //e.time = event.time;
	    //e.stateMask = event.stateMask;
		//e.doit = event.doit;
		connectionCombo.notifyListeners(SWT.Selection, e);    		
    }
    /**
     * Clear the entered/selected contents of the combo box. Clears only the text selection, not the list selection
     */
    public void clearTextSelection()
    {
    	connectionCombo.clearSelection();
    }

    /**
     * Get the index number of the currently selected item. 
     */
    public int getSelectionIndex()
    {
    	return connectionCombo.getSelectionIndex();
    }

	/**
	 * Register a listener interested in an item is selected in the combo box
     * @see #removeSelectionListener(SelectionListener)
     */
    public void addSelectionListener(SelectionListener listener) 
    {
	    connectionCombo.addSelectionListener(listener);
    }
    /** 
     * Remove a previously set combo box selection listener.
     * @see #addSelectionListener(SelectionListener)
     */
    public void removeSelectionListener(SelectionListener listener) 
    {
	    connectionCombo.removeSelectionListener(listener);
    }

	/**
	 * Register a listener interested in when the new button is selected
     * @see #removeNewButtonSelectionListener(SelectionListener)
     */
    public void addNewButtonSelectionListener(SelectionListener listener) 
    {
		if (newButton != null)
	      newButton.addSelectionListener(listener);
    }
    /** 
     * Remove a previously set new button selection listener.
     * @see #addNewButtonSelectionListener(SelectionListener)
     */
    public void removeNewButtonSelectionListener(SelectionListener listener) 
    {
		if (newButton != null)
	      newButton.removeSelectionListener(listener);
    }

	/**
	 * Register a listener interested in entry field modify events
     * @see #removeModifyListener(ModifyListener)
     */
    public void addModifyListener(ModifyListener listener) 
    {
	    connectionCombo.addModifyListener(listener);
    }
    /** 
     * Remove a previously set entry field listener.
     * @see #addModifyListener(ModifyListener)
     */
    public void removeModifyListener(ModifyListener listener) 
    {
	    connectionCombo.removeModifyListener(listener);
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
		gridColumns = numColumns;
		Composite composite = this;
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		//layout.horizontalSpacing = 0;
		//layout.verticalSpacing   = 0;
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		// horizontal clues
		data.horizontalAlignment = GridData.FILL;
	    data.grabExcessHorizontalSpace = true;        
        data.widthHint = showNewButton ? 250 : 200;
		// vertical clues
		data.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING; //GridData.CENTER;
	    data.grabExcessVerticalSpace = false; // true;        
	    
		composite.setLayoutData(data);
		return composite;
	}
	/**
	 * Creates a new readonly connection combobox instance and sets the default
	 * layout data, with tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * This fills the combination with the names of all the active connections of the given
	 * system type.
	 * @param parent composite to put the button into.
	 */
	protected Combo createConnectionCombo(Composite parent)
	{
		Combo combo = createCombo(parent,true);
		setToolTipText(SystemResources.WIDGET_CONNECTION_TOOLTIP);
	    return combo;
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
	    combo.setLayoutData(data);	   
	    return combo;
	}

	/**
	 * Populates a readonly connection combobox instance with system connections for the given
	 * system type.
	 * <p>
	 * This fills the combination with the names of all the active connections of the given
	 * system type.
	 * @param connectionCombo composite to populate
	 * @param systemType the system type to restrict the connection list to. Pass null or * for all system types
	 * @param defaultConnection the default system connection to preselect.
	 * @param preSelectIfNoMatch true if we should preselect the first item if the given connection is not found
	 * @return true if given default connection was found and selected
	 */
	protected boolean populateConnectionCombo(Combo combo, String systemType, IHost defaultConnection,
	                                       boolean preSelectIfNoMatch)
	{
	    return populateConnectionCombo(combo, systemType, defaultConnection, preSelectIfNoMatch, false);
	}
	
	/**
	 * Populates a readonly connection combobox instance with system connections for the given
	 * system type.
	 * <p>
	 * This fills the combination with the names of all the active connections of the given
	 * system type.
	 * @param connectionCombo composite to populate
	 * @param systemType the system type to restrict the connection list to. Pass null or * for all system types
	 * @param defaultConnection the default system connection to preselect.
	 * @param preSelectIfNoMatch true if we should preselect the first item if the given connection is not found
	 * @param appendToCombo indicates whether or not to append to combo with population or replace 
	 * @return true if given default connection was found and selected
	 */
	protected boolean populateConnectionCombo(Combo combo, String systemType, IHost defaultConnection,
	                                       boolean preSelectIfNoMatch, boolean appendToCombo)
	{
		boolean matchFound = false;
		IHost[] additionalConnections = null;
        if ( (systemType == null) || (systemType.equals("*")) )        
          additionalConnections = RSEUIPlugin.getTheSystemRegistry().getHosts();
        else
          additionalConnections = RSEUIPlugin.getTheSystemRegistry().getHostsBySystemType(systemType);
        if (additionalConnections != null)
        {
          String[] connectionNames = new String[additionalConnections.length];
          int selectionIndex = -1;
          for (int idx=0; idx<connectionNames.length; idx++)
          {
             connectionNames[idx] = getConnectionName(additionalConnections[idx]);
             if ((defaultConnection != null) && (additionalConnections[idx] == defaultConnection))
             {
               if (connections == null)
                 selectionIndex = idx;
               else 
                 selectionIndex = connections.length+idx;
             }
          }
          // DKM - fix for 55830
          if (appendToCombo)
          {
              for (int i = 0; i < connectionNames.length; i++)
                  combo.add(connectionNames[i]);
          }
          else
          {
              combo.setItems(connectionNames);
          }
          if (selectionIndex >=0)
          {
            //combo.select(selectionIndex);
            select(selectionIndex);
            matchFound = true;
          }
          else if (preSelectIfNoMatch && (combo.getItemCount()>0))
            //combo.select(0);
            select(0);
        }
        if (connections == null)
          connections = additionalConnections;
        else if ((additionalConnections != null) && (additionalConnections.length>0))
        {
           IHost[] totalConnections = new IHost[connections.length+additionalConnections.length];
           int totalIdx = 0;
           for (int idx=0; idx<connections.length; idx++)
              totalConnections[totalIdx++] = connections[idx];
           for (int idx=0; idx<additionalConnections.length; idx++)
              totalConnections[totalIdx++] = additionalConnections[idx];
           connections = totalConnections;
        }
        return matchFound;
	}	
	/**
	 * Populates a readonly connection combobox instance with system connections for the given
	 * array of system types.
	 * @param connectionCombo composite to populate
	 * @param systemTypes the system types to restrict the connection list to. Pass null or * for all system types
	 * @param defaultConnection the default system connection to preselect.
	 */
	protected void populateConnectionCombo(Combo combo, String[] systemTypes, IHost defaultConnection)
	{
		 boolean match = false;
		 boolean anyMatch = false;
         for (int idx=0; idx<systemTypes.length; idx++)
         {
         	match = populateConnectionCombo(combo, systemTypes[idx], defaultConnection, false, true);
         	if (match)
         	  anyMatch = true;
         }
         if (!anyMatch && (combo.getItemCount()>0))
           //combo.select(0);
           select(0);
	}	
	/**
	 * Populates a readonly connection combobox instance with system connections which have subsystems
	 * owned by the given subsystem factory.
	 * <p>
	 * @param connectionCombo composite to populate
	 * @param subsystemFactory the subsystem factory to restrict the connection list to.
	 * @param defaultConnection the default system connection to preselect.
	 * @return true if given default connection was found and selected
	 */
	protected boolean populateConnectionCombo(Combo combo, ISubSystemConfiguration ssFactory, IHost defaultConnection)
	{
		connections = RSEUIPlugin.getTheSystemRegistry().getHostsBySubSystemConfiguration(ssFactory);
        return addConnections(combo, connections, defaultConnection);
	}	
	/**
	 * Populates a readonly connection combobox instance with system connections which have subsystems
	 * owned by a subsystem factory of the given subsystem factory id.
	 * <p>
	 * @param connectionCombo composite to populate
	 * @param defaultConnection the default system connection to preselect.
	 * @param subsystemFactoryId the subsystem factory id to restrict the connection list by.
	 * @return true if given default connection was found and selected
	 */
	protected boolean populateConnectionCombo(Combo combo, String ssFactoryId, IHost defaultConnection)
	{
		connections = RSEUIPlugin.getTheSystemRegistry().getHostsBySubSystemConfigurationId(ssFactoryId);
        return addConnections(combo, connections, defaultConnection);
	}

	/**
	 * Populates a readonly connection combobox instance with system connections which have subsystems
	 * owned by a subsystem factory of the given subsystem factory category.
	 * <p>
	 * @param connectionCombo composite to populate
	 * @param defaultConnection the default system connection to preselect.
	 * @param subsystemFactoryCategory the subsystem factory category to restrict the connection list by.
	 * @return true if given default connection was found and selected
	 */
	protected boolean populateConnectionCombo(Combo combo, IHost defaultConnection, String ssFactoryCategory)
	{
		connections = RSEUIPlugin.getTheSystemRegistry().getHostsBySubSystemConfigurationCategory(ssFactoryCategory);
        return addConnections(combo, connections, defaultConnection);
	}
	/**
	 * An attempt to get some abstraction
	 */	
	private boolean addConnections(Combo combo, IHost[] connections, IHost defaultConnection)
	{
		boolean matchFound = false;
        if (connections != null)
        {
          String[] connectionNames = new String[connections.length];
          int selectionIndex = -1;
          for (int idx=0; idx<connectionNames.length; idx++)
          {
             connectionNames[idx] = getConnectionName(connections[idx]);
             if ((defaultConnection!=null) && (connections[idx] == defaultConnection))
               selectionIndex = idx;
          }
          combo.setItems(connectionNames);
          if (selectionIndex >=0)
          {
            //combo.select(selectionIndex);
            select(selectionIndex);
            matchFound = true;
          }
          else if (combo.getItemCount()>0)
            //combo.select(0);
            select(0);
        }
        return matchFound;
	}

	/**
	 * Do string variable substitution. Using you are replacing %1 (say) with a string
	 * @param message containing substitution variable. Eg "Connect failed with return code &1"
	 * @param substitution variable. Eg "%1"
	 * @param substitution data. Eg "001"
	 * @return message with all occurrences of variable substituted with data.
	 */
	protected static String sub(String msg, String subOld, String subNew)
	{
		StringBuffer temp = new StringBuffer();
		int lastHit = 0;
		int newHit = 0;
		for (newHit = msg.indexOf(subOld,lastHit); newHit != -1;
			 lastHit = newHit, newHit = msg.indexOf(subOld,lastHit))
		   {
			 if (newHit >= 0)
			   temp.append(msg.substring(lastHit,newHit));
			 temp.append(subNew);
			 newHit += subOld.length();
		   }
		if (lastHit >= 0)
		  temp.append(msg.substring(lastHit));
		return temp.toString();
	}  	
	
	/**
	 * Return the connection name to display in the combo, given the connection
	 */
	private String getConnectionName(IHost conn)
	{
		//String connectionName = sub(nameString,"%1",conn.getAliasName());
        //connectionName = sub(connectionName,"%2",conn.getSystemProfileName());
        //return connectionName;
		if (showQualifiedNames)
		  return conn.getSystemProfileName() + "." + conn.getAliasName();
		else
		  return conn.getAliasName();
	}


	/**
	 * Refresh the list of connections
	 */
	public void refreshConnections()
	{
		connections = null;
		connectionCombo.removeAll();
		
		if ( populateSystemType != null )
		{
			populateConnectionCombo(connectionCombo, populateSystemType, null, false);
		}
		else if ( populateSystemTypes != null )
		{
			populateConnectionCombo(connectionCombo, populateSystemTypes, null);
		}
		else if ( populateSSFactory != null )
		{
			populateConnectionCombo(connectionCombo, populateSSFactory, null);
		}
		else if ( populateSSFactoryId != null )
		{
			populateConnectionCombo(connectionCombo, populateSSFactoryId, null);
		}
		else if ( populateSSFactoryCategory != null )
		{
			populateConnectionCombo(connectionCombo, null, populateSSFactoryCategory);
		}
	}
	
	/**
	 * This is the method in your class that will be called when a
	 *  system resource changes.  We want to listen to connection changes.
	 * @see ISystemResourceChangeEvent
	 */
    public void systemResourceChanged(ISystemResourceChangeEvent event)
    {
		int type = event.getType();    	   
		Object src = event.getSource();
		Object parent = event.getParent();
		switch ( type )
		{
			case EVENT_ADD:
			case EVENT_ADD_RELATIVE:
			case EVENT_DELETE_MANY:
			case EVENT_RENAME:
					if ( src instanceof IHost )
					{
						// if RENAME, update showQualifiedNames in case it changed
						if ( type == EVENT_RENAME )
							showQualifiedNames = SystemPreferencesManager.getPreferencesManager().getQualifyConnectionNames();
							
						refreshConnections();
					}
				break;
		}    	
    }

	/**
	 * Have the SystemConnectionCombo listen to add/delete/rename events on connections
	 * and automatically update it's list of connections	
	 */
	public void listenToConnectionEvents(boolean start)
	{
		if ( start )
		{
			// ----------------------------------------
			// register with system registry for events
			// ----------------------------------------
			listeningForConnectionEvents = true;
			RSEUIPlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);
		}
		else
		{
			// ----------------------------------------
			// remove register with system registry for events
			// ----------------------------------------
			listeningForConnectionEvents = false;
			RSEUIPlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
		}
	}
	/**
	 * We are going away. De-Register ourselves as a listener for system resource change events
	 */
	public void widgetDisposed(DisposeEvent e)
	{
		if (listeningForConnectionEvents)
		{
			listeningForConnectionEvents = false;
			RSEUIPlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);			
		}
	}
	
	protected void addOurButtonSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
		  	  setBusyCursor(true);
		  	  newConnectionAction.run();
			  setBusyCursor(false);
		  	  IHost newConnection = (IHost)newConnectionAction.getValue();
		  	  //System.out.println("newConnection == " + newConnection);
		  	  if (newConnection != null )
		  	  {
		  	  	
		  	  	// if listening for events, combo box will be updated automatically
		  	  	if ( listeningForConnectionEvents )
		  	  	{
		  	  		select(newConnection); 	// it should be added by now
		  	  	}
		  	  	// if not listening for events, add new connection
		  	  	else
		  	  	{
			  	  	if (connections == null)
			  	  	{
			  	  	   connections = new IHost[1];
			  	  	   connections[0] = newConnection;
		               addConnections(connectionCombo,connections,newConnection);
			  	  	   //connectionCombo.select(0);
			  	  	}
			  	  	else
			  	  	{
		               IHost[] totalConnections = new IHost[connections.length+1];
		               int totalIdx = 0;
		               for (int idx=0; idx<connections.length; idx++)
		                 totalConnections[totalIdx++] = connections[idx];
		               totalConnections[totalIdx] = newConnection;
		               connections = totalConnections;
		               addConnections(connectionCombo,connections,newConnection);
		               //connectionCombo.select(totalIdx);
			  	  	}
		  	  	}		  	  		
		  	  }
		  };
	   };
	   newButton.addSelectionListener(selectionListener);
	}

	protected void addOurConnectionSelectionListener()
	{
	   // Add the combo listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
		  	  setConnectionToolTipText();
		  };
	   };
	   connectionCombo.addSelectionListener(selectionListener);
	}
	
	protected void setConnectionToolTipText()
	{
	   String tooltipText = "";
       IHost currConn = getHost();
       if (currConn != null)
         tooltipText = currConn.getHostName();
       connectionCombo.setToolTipText(tooltipText);
	}
	
	protected static Button createPushButton(Composite group, String label)
	{
	   Button button = new Button(group, SWT.PUSH);
	   button.setText(label);
	   GridData data = new GridData();
	   //data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;	   
	   data.horizontalAlignment = GridData.FILL;
	   data.grabExcessHorizontalSpace = false;	   
	   button.setLayoutData(data);
	   return button;
	}
	
	protected static Button createPushButton(Composite group, String label, String tooltip)
	{
	    Button button = createPushButton(group,label);
	    button.setToolTipText(tooltip);
	    return button;
	}
	
    // -----------------------------
    // ISelectionProvider methods...
    // -----------------------------
	/**
	 * Register a listener interested in an item is selected in the combo box
     * @see #removeSelectionChangedListener(ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) 
    {
	    //connectionCombo.addSelectionChangedListener(listener);
    }
    /** 
     * Remove a previously set combo box selection listener.
     * @see #addSelectionChangedListener(ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) 
    {
	    //connectionCombo.removeSelectionChangedListener(listener);
    }

    public ISelection getSelection()
    {
    	ISelection selection = StructuredSelection.EMPTY;
    	IHost connection = getHost();
    	if (connection != null)
    	  selection = new StructuredSelection(connection);
    	return selection;
    }
    public void setSelection(ISelection selection)
    {
    }
    
    public IHost[] getConnections()
    {
    	  return connections;
    }

    public void setConnections(IHost[] input)
    {
    	  connections = input;
    }
    
	/**
	 * <i>Helper method.</i><br>
	 * Set the cursor to the wait cursor (true) or restores it to the normal cursor (false).
	 */
	protected void setBusyCursor(boolean setBusy)
	{
		if (setBusy)
		{
		  // Set the busy cursor to all shells.
		  Display d = getShell().getDisplay();
		  waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
		  org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(getShell(), waitCursor);
		}
		else
		{
		  org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(getShell(), null);
		  if (waitCursor != null)
			waitCursor.dispose();
		  waitCursor = null;
		}
	}
}