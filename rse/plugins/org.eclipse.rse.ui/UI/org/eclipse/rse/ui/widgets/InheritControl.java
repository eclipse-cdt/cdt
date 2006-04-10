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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;

/**
 * A widget like the old ET/400 inherit/override widget.
 * There are left and right arrows beside each other. 
 * Typically, clicking on left means to inherit from parent,
 * clicking on right means to override locally. 
 * However, the control can be used for any binary decision!
 * THIS IS NOT USED AND NOT WORKING. USE INHERITABLEENTRYFIELD INSTEAD
 * 
 * @deprecated 
 * @see org.eclipse.rse.ui.widgets.InheritableEntryField
 */
public class InheritControl 
       extends Composite 
{
	private Image local,interim,inherit;
	private Button button;
	private boolean isLocal=false;
    /**
     * Constructor.
     * @param parent Composite to place this widget into
     * @param style Widget style. Passed on to 
     *    {@link org.eclipse.swt.widgets.Composite#Composite(org.eclipse.swt.widgets.Composite, int) constructor} of parent class Composite
     */   
    public InheritControl(Composite parent, int style) 
    {
	   super(parent, style);
	   setLayout(new InheritControlLayout());
	   //Class c = InheritControl.class;
	   //String imagePath = "icons" + java.io.File.separatorChar;
	   SystemPlugin sp = SystemPlugin.getDefault();
	   try 
	   {
		 //ImageData source = new ImageData(c.getResourceAsStream (imagePath+"local.gif"));	   	
		 /*
	   	 Image image = sp.getImage(ISystemConstants.ICON_INHERITWIDGET_LOCAL_ID);
		 ImageData source = image.getImageData();		 
		 ImageData mask = source.getTransparencyMask();
		 //local = new Image (null, source, mask);
		 local = image;

	   	 image = sp.getImage(ISystemConstants.ICON_INHERITWIDGET_INHERIT_ID);	
		 source = image.getImageData();
		 //source = new ImageData(c.getResourceAsStream (imagePath+"inherit.gif"));	   	 			 
		 mask = source.getTransparencyMask();
		 //inherit = new Image (null, source, mask);
		 inherit = image;
		
		 // don't know how to add third state, and don't really
		 // need it for Button.  Could use it if we implement as Label ....
		 //source = new ImageData(c.getResourceAsStream (imagePath+"interim.gif"));	   	 
	   	 image = sp.getImage(ISystemConstants.ICON_INHERITWIDGET_INTERIM_ID);		
		 source = image.getImageData();
		 mask = source.getTransparencyMask();
		 //interim = new Image (null, source, mask);
		 interim = image;
		 */
	   } catch (Throwable ex) 
	   {
		 System.out.println ("failed to load images");
		 ex.printStackTrace();
	   }
	   button=new Button(this,style);
	   setLocal(true);
	   addDisposeListener(new DisposeListener() 
	   {
		  public void widgetDisposed(DisposeEvent e) 
		  {
			// dispose of created resources!
			InheritControl.this.widgetDisposed(e);
		  }
	   });
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
			  setLocal(!isLocal());
	          notifyListeners(SWT.Selection, new Event());
		  };
	   };
	   button.addSelectionListener(selectionListener);
    } 
    /** 
     * Add a listener that is called whenever the left or right side is selected.
     * <p>
     * Call {@link #isLocal()} to determine if left (false) or right (true) was pressed.
     * @see #addSelectionListener(SelectionListener)
     */
    public void addSelectionListener(SelectionListener listener) 
    {
	    addListener(SWT.Selection, new TypedListener(listener));
    }
    /**
     * Returns true if the right-side is selected, false if the left is selected
     */
    public boolean isLocal()
    {
	    return isLocal;
    }
    /** 
     * Remove a previously set selection listener.
     * @see #addSelectionListener(SelectionListener)
     */
    public void removeSelectionListener(SelectionListener listener) 
    {
	    removeListener(SWT.Selection, listener);
    }
    /**
     * Programmatically select left (false) or right/local (true) arrow.
     */
    public void setLocal(boolean l)
    {
	   isLocal=l;
	   button.setImage(isLocal?local:inherit);
    }
    /**
     * Set tooltip text (hover help)
     */
    public void setToolTipText(String tip)
    {
    	button.setToolTipText(tip);
    }
    /**
     * Private hook called by system when this widget is disposed.
     */
    public void widgetDisposed(DisposeEvent e) 
    {
    	if (local!=null)
	      local.dispose();
	    if (interim!=null)
	      interim.dispose();
	    if (inherit!=null)
	      inherit.dispose();
    }
    /*
    public static void main(String[] args) 
    {
	    // Example on how to use widget
	    final InheritControl c1,c2,c3;
	    final Text text1,text2,text3;
	    Display display = new Display();
	    Shell shell = new Shell();
	    GridLayout g=new GridLayout();
	    g.numColumns=2;
	    shell.setLayout(g);
	    c1=new InheritControl(shell,SWT.NULL);
	    text1 = new Text (shell, SWT.BORDER);
	    c2=new InheritControl(shell,SWT.NULL);
	    text2 = new Text (shell, SWT.BORDER);
	    c3=new InheritControl(shell,SWT.NULL);
	    text3 = new Text (shell, SWT.BORDER);
	    Button b1=new Button(shell,SWT.NULL);
	    b1.setText("Normal button ....");
	    //Add listeners:
	    c1.addSelectionListener(new SelectionAdapter() 
	    {
		    public void widgetSelected(SelectionEvent event) 
		    {
			    text1.setEnabled(c1.isLocal);
		    };
	    });
	    c2.addSelectionListener(new SelectionAdapter() 
	    {
	    	public void widgetSelected(SelectionEvent event) 
	    	{
			    text2.setEnabled(c2.isLocal);
		    };
	    });
	    c3.addSelectionListener(new SelectionAdapter() 
	    {
		    public void widgetSelected(SelectionEvent event) 
		    {
			    text3.setEnabled(c3.isLocal);
		    };
	    });
	    shell.pack();
	    shell.open();
	    // Event loop
	    while (! shell.isDisposed()) 
	    {
		    if (! display.readAndDispatch()) display.sleep();
	    }
	    display.dispose();
	    System.exit(0);
    }
    */
}