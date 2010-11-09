/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * Uwe Stieber      (Wind River) - [319618] [context-menu] Tool tip not shown in status bar for command contributions items
 * Martin Oberhuber (Wind River) - [323141] [context-menu] Cleanup introspection code for command contribution items
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.rse.internal.ui.actions.SystemSubMenuManager;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.actions.ISystemViewMenuListener;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.menus.CommandContributionItem;


/**
 * A class that listens for menu show events for the purpose of applying mnemonics
 * to the menu items.
 */
public class SystemViewMenuListener 
implements ISystemViewMenuListener
{


    protected boolean menuListenerAdded = false;
    protected boolean menuMnemonicsAdded = false;    
    protected boolean doOnce = false;
    protected boolean armListeners = false;
    protected Mnemonics m = new Mnemonics();  
    protected ISystemMessageLine msgLine;  

    /**
     * Default constructor
     */
    public SystemViewMenuListener()
    {
    }
    /**
     * Constructor for a persistent menu (vs a popup that's fresh each time)
     */
    public SystemViewMenuListener(boolean doOnce)
    {
    	this.doOnce = doOnce;
    }
    /**
     * Enable tooltip text for the menu items too?
     * Requires a message line to display the text on.
     */
    public void setShowToolTipText(boolean set, ISystemMessageLine msgLine)
    {
    	this.armListeners = set;
    	this.msgLine = msgLine;
    }
        
    // ---------------------
    // IMenuListener methods
    // ---------------------
    /**
     * Called when the context menu is about to open.
     */
    public void menuAboutToShow(IMenuManager menu)
    {
   	    if (!menuListenerAdded)
   	    {
   	      if (menu instanceof MenuManager)
   	      {
   	      	Menu m = ((MenuManager)menu).getMenu();
   	      	if (m != null)
   	      	{
   	      		menuListenerAdded = true;
   	      		m.addMenuListener(this);
   	      	}
   	      }
   	    }
    }
  
    // --------------------
    // MenuListener methods
    // --------------------
    /**
     * Menu hidden
     */
    public void menuHidden(MenuEvent event)
    {
    	
    }
    /**
     * Menu shown
     */
    public void menuShown(MenuEvent event)
    {
    	if (!menuMnemonicsAdded || !doOnce)
    	{
      	  m.clear();
      	  Menu menu = (Menu)event.getSource();
  	      m.setMnemonics(menu);
      	  if (armListeners) {
      		  setArmListener(menu);
      	  }
    	  menuMnemonicsAdded = true;
    	  if (doOnce)
   	        ((Menu)event.getSource()).removeMenuListener(this);    	  
    	}
    }
    
    private void setArmListener(Menu menu) {
    	MenuItem[] items = menu.getItems();
    	for (int i = 0; i < items.length; i++) {
			MenuItem menuItem = items[i];
			setArmListener(menuItem);
		}
    }
    
    private void setArmListener(MenuItem item) {
    	item.addArmListener(this);
    	Menu menu = item.getMenu();
    	if (menu != null) {
    		setArmListener(menu);
    	}
    }
    
    // --------------------
    // ArmListener methods
    // --------------------
    /**
     * Menu item is currently selected by user. Try to show tooltip text.
     */
    public void widgetArmed(ArmEvent event)
    {
    	if (msgLine == null)
    	  return;
    	msgLine.clearMessage();
		Widget w = event.widget;
		//System.out.println("inside widgetArmed. w = "+w.getClass().getName());
		if (w instanceof MenuItem)
		{
		  MenuItem mi = (MenuItem)w;
		  Object data = mi.getData();
		  //System.out.println("... data = "+data+", msgLine null? "+(msgLine==null));
		  if (data != null)
		  {
		  	String tip = null; //data.getClass().getName();
		    if (data instanceof ActionContributionItem)
		      tip = ((ActionContributionItem)data).getAction().getToolTipText();
		    else if (data instanceof CommandContributionItem) {
		    	try {
		    		tip = ((CommandContributionItem) data).getData().tooltip;
		    	} catch (Exception apiNotYetAvailable) {
		    		//API was introduced with Eclipse 3.7m3 -- use introspection on older Eclipse
			    	try {
			    		Field f = data.getClass().getDeclaredField("widget"); //$NON-NLS-1$
			    		f.setAccessible(true);
			    		Widget widget = (Widget)f.get(data);
						Method m = data.getClass().getDeclaredMethod("getToolTipText", new Class[] { String.class }); //$NON-NLS-1$
						m.setAccessible(true);
						tip = (String)m.invoke(data, new Object[] { widget instanceof Item ? ((Item)widget).getText() : (String)null });
					} catch (Exception e) { /* ignored on purpose */ }
		    	}
		    }
		    else if (data instanceof SystemSubMenuManager)
		      tip = ((SystemSubMenuManager)data).getToolTipText();
		    if (tip != null)
              msgLine.setMessage(tip);
		  }
		}            	
    }
  
}
