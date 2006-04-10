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

package org.eclipse.rse.ui.view;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertySheetPage;



/**
 * This re-usable widget is for a property-sheet widget that is imbeddable in dialogs.
 * It is similar to the workbench property sheet but there are some important differences.
 */
public class SystemPropertySheetForm extends Composite 
{

	private PropertySheetPage tree = null;
	private boolean        enabledMode = true;
    //private ISystemViewInputProvider inputProvider = null;
    //private ISystemViewInputProvider emptyProvider = new SystemEmptyListAPIProviderImpl();
	public static final int DEFAULT_WIDTH = 300;
	public static final int DEFAULT_HEIGHT = 250;
	
	/**
	 * Constructor
	 * @param shell The owning window
	 * @param parent The owning composite
	 * @param style The swt style to apply to the overall composite. Typically SWT.NULL
	 * @param msgLine where to show messages and tooltip text
	 */
	public SystemPropertySheetForm(Shell shell, Composite parent, int style, ISystemMessageLine msgLine)
	{
		this(shell, parent, style, msgLine, 1, 1);	
	}
	/**
	 * Constructor when you want to span more than one column or row
	 * @param shell The owning window
	 * @param parent The owning composite
	 * @param style The swt style to apply to the overall composite. Typically SWT.NULL
	 * @param horizontalSpan how many columns in parent composite to span
	 * @param verticalSpan how many rows in parent composite to span
	 * @param msgLine where to show messages and tooltip text
	 */
	public SystemPropertySheetForm(Shell shell, Composite parent, int style, ISystemMessageLine msgLine, int horizontalSpan, int verticalSpan)
	{
		super(parent, style);	
		prepareComposite(1, horizontalSpan, verticalSpan);
	    createPropertySheetView(shell);
        addOurSelectionListener();
        addOurMouseListener();
        addOurKeyListener();
	}
	
	/**
	 * Return the system view tree viewer
	 */
	public PropertySheetPage getPropertySheetView()
	{
		return tree;
	}
	/**
	 * Return the underlying control
	 */
	public Control getControl()
	{
		return tree.getControl();
	}

    /**
     * Set the tree's tooltip text
     */
    public void setToolTipText(String tip)
    {
    	tree.getControl().setToolTipText(tip);
    }
    /**
     * Refresh contents
     */
    public void refresh()
    {
    	tree.refresh();
    }

    /**
     * Method declared on ISelectionListener.
     */
    public void selectionChanged(ISelection selection) 
    {
    	tree.selectionChanged(null, selection);
    }

	/**
	 * Disable/Enable all the child controls.
	 */
	public void setEnabled(boolean enabled)
	{
		enabledMode = enabled;
	}
	
	// -----------------------
	// INTERNAL-USE METHODS...
	// -----------------------
	/**
	 * Prepares this composite control and sets the default layout data.
	 * @param Number of columns the new group will contain.     
	 */
	protected Composite prepareComposite(int numColumns,
	                                     int horizontalSpan, int verticalSpan)	
	{
		Composite composite = this;
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;		
		layout.verticalSpacing = 0;		
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
	    data.grabExcessHorizontalSpace = true;        
	    data.grabExcessVerticalSpace = true;	    
	    data.widthHint = DEFAULT_WIDTH;	    
	    data.heightHint = DEFAULT_HEIGHT;	
	    data.horizontalSpan = horizontalSpan;
	    data.verticalSpan = verticalSpan;    
		composite.setLayoutData(data);
		return composite;
	}
	
	protected void createPropertySheetView(Shell shell)
	{		
		tree = new PropertySheetPage();
		tree.createControl(this);
		Control c = tree.getControl();
	    GridData treeData = new GridData();
	    treeData.horizontalAlignment = GridData.FILL;
	    treeData.verticalAlignment = GridData.FILL;	    
	    treeData.grabExcessHorizontalSpace = true;
	    treeData.grabExcessVerticalSpace = true;	    
	    treeData.widthHint = 220;        
	    treeData.heightHint= 200;
	    c.setLayoutData(treeData);  	  	    		
	    //tree.setShowActions(showActions);

	}
	
	
	protected void addOurSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionListener() 
	   {
		  public void widgetDefaultSelected(SelectionEvent event) 
		  {
		  };
		  public void widgetSelected(SelectionEvent event) 
		  {
			  if (!enabledMode)
				return;
		  	  Object src = event.getSource();
		  };
	   };
	   //tree.getControl().addSelectionListener(selectionListener);
	}

	protected void addOurMouseListener()
	{
	   MouseListener mouseListener = new MouseAdapter() 
	   {
		   public void mouseDown(MouseEvent e) 
		   {
				if (!enabledMode)
				  return;
			   //requestActivation();
		   }
	   };	
	   tree.getControl().addMouseListener(mouseListener);
	}

	protected void addOurKeyListener()
	{
		KeyListener keyListener = new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e) 
			{
				if (!enabledMode)
				{
				  //e.doit = false;
				  return;
				}
				//handleKeyPressed(e);
			}
		};		
	   tree.getControl().addKeyListener(keyListener);
	}

}