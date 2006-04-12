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
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ISystemValidatorUniqueString;
import org.eclipse.rse.ui.validators.ValidatorConnectionName;
import org.eclipse.rse.ui.validators.ValidatorUniqueString;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


/**
 * Dialog for renaming multiple resources.
 * <p>
 * This is a re-usable dialog that you can use  directly, or via the {@link org.eclipse.rse.ui.actions.SystemCommonRenameAction}
 *  action. 
 * <p>
 * To use this dialog, you must call setInputObject with a StructuredSelection of the objects to be renamed.
 * If those objects adapt to {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter} or 
 * {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter}, the dialog will offer built-in error checking.
 * <p>
 * If the input objects do not adapt to org.eclipse.rse.ui.view.ISystemRemoteElementAdapter or ISystemViewElementAdapter, then you
 * should call {@link #setNameValidator(org.eclipse.rse.ui.validators.ISystemValidator)} to 
 * specify a validator that is called to verify the typed new name is valid. Further, to show the type value
 * of the input objects, they should implement {@link org.eclipse.rse.ui.dialogs.ISystemTypedObject}.
 * <p>
 * This dialog does not do the actual renames. Rather, it will return an array of the user-typed new names. These
 * are queriable via {@link #getNewNames()}, after testing that {@link #wasCancelled()} is false. The array entries
 * will match the input order.
 * 
 * @see org.eclipse.rse.ui.actions.SystemCommonRenameAction
 */
public class SystemRenameDialog extends SystemPromptDialog 
                                implements ISystemMessages, ISystemPropertyConstants,
                                           ISelectionChangedListener, 
                                           TraverseListener,
                                           ICellEditorListener, Runnable, FocusListener
{
	
    private SystemMessage errorMessage;
    private TextCellEditor cellEditor;
    private int currRow = 0;
    private GridData tableData = null;
    private boolean ignoreSelection = false;
    private Hashtable uniqueNameValidatorPerParent = new Hashtable();
    
    private String verbage;

    private SystemRenameTableProvider srtp;
    private Table table;
    private TableViewer tableViewer;    
    private static final int COLUMN_NEWNAME = SystemRenameTableProvider.COLUMN_NEWNAME;
	private String columnHeaders[] = {
		"",SystemResources.RESID_RENAME_COLHDG_OLDNAME,
		   SystemResources.RESID_RENAME_COLHDG_NEWNAME,
		   SystemResources.RESID_RENAME_COLHDG_TYPE		   
	};
	private ColumnLayoutData columnLayouts[] = 
	{
		new ColumnPixelData(19, false),		
		new ColumnWeightData(125,125,true),
		new ColumnWeightData(150,150,true),
		new ColumnWeightData(120,120,true)		
    };
    // give each column a property value to identify it
	private static String[] tableColumnProperties = 
	{
		ISystemPropertyConstants.P_ERROR,		
		IBasicPropertyConstants.P_TEXT,
		ISystemPropertyConstants.P_NEWNAME,
		ISystemPropertyConstants.P_TYPE,		
    };    
	// inner class to support cell editing	
	private ICellModifier cellModifier = new ICellModifier() 
	{
		public Object getValue(Object element, String property) 
		{
			SystemRenameTableRow row = (SystemRenameTableRow)element;
			String value = "";
			if (property.equals(P_TEXT))
			  value = row.getName();
			else
			  value = row.getNewName();
			//System.out.println("inside getvalue: " + row + "; " + property + " = " + value);
			return value;
		}

		public boolean canModify(Object element, String property) 
		{
			boolean modifiable = property.equals(P_NEWNAME);
			if ((cellEditor != null) && (cellEditor.getControl() != null))
			{
			  SystemRenameTableRow row = (SystemRenameTableRow)element;				
			  int limit = row.getNameLengthLimit();
			  if (limit == -1)
			    limit = 1000;
			  ((Text)cellEditor.getControl()).setTextLimit(limit);
			}
			return modifiable;
		}
		/**
		 * Modifies a marker as a result of a successfully completed direct editing.
		 */
		public void modify(Object element, String property, Object value) 
		{
			SystemRenameTableRow row = (SystemRenameTableRow)(((TableItem)element).getData());			
			//System.out.println("inside modify: " + row+"; "+property+", "+value);			
			if (property.equals(P_NEWNAME))
			{
			  row.setNewName((String)value);
			  tableViewer.update(row, null);
			}
		}
	};
		    
    
	/**
	 * Constructor for SystemRenameDialog
	 */
	public SystemRenameDialog(Shell shell) 
	{
		this(shell, SystemResources.RESID_RENAME_TITLE);				
	}
	/**
	 * Constructor when you have your own title
	 */
	public SystemRenameDialog(Shell shell, String title) 
	{
		super(shell, title);				

		//pack();
		setHelp(RSEUIPlugin.HELPPREFIX+"drnm0000");
	}
	/**
	 * Set the verbage to show above the table. The default is "Enter new name for each resource"
	 */
	public void setVerbage(String verbage)
	{
		this.verbage = verbage;
	}	
    /**
     * Set the validator for the new name,as supplied by the adaptor for name checking.
     * Overrides the default which is to query it from the object's adapter.
     */
    public void setNameValidator(ISystemValidator nameValidator)
    {
    }

	/**
	 * Create message line. Intercept so we can set msg line of form.
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		ISystemMessageLine msgLine = super.createMessageLine(c);
		return fMessageLine;
	}


	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		SystemRenameTableRow[] rows = getRows();
	   	tableViewer.setSelection(new StructuredSelection(rows[0]),true);
		tableViewer.editElement(rows[0], COLUMN_NEWNAME);
		return null;
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		// Inner composite
		int nbrColumns = 1;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);			
		
		if (verbage != null)
			SystemWidgetHelpers.createVerbage(composite, verbage, nbrColumns, false, 200);
		else
			SystemWidgetHelpers.createVerbage(composite, SystemResources.RESID_RENAME_VERBAGE, nbrColumns, false, 200);

        table = createTable(composite);        
        tableViewer = new TableViewer(table);
        createColumns();
	    tableViewer.setColumnProperties(tableColumnProperties);        
	    tableViewer.setCellModifier(cellModifier);
	    CellEditor editors[] = new CellEditor[columnHeaders.length];
	    cellEditor = new TextCellEditor(table);
	    cellEditor.addListener(this);	    
	    editors[COLUMN_NEWNAME] = cellEditor;
	    tableViewer.setCellEditors(editors);	    
	    cellEditor.getControl().addTraverseListener(this);
	    //System.out.println("CELL EDITOR CONTROL: " + cellEditor.getControl());
                
        srtp = new SystemRenameTableProvider();
	    int width = tableData.widthHint;
	    int nbrRows = Math.min(getRows().length,8);
	    int rowHeight = table.getItemHeight() + table.getGridLineWidth();
        int sbHeight = table.getHorizontalBar().getSize().y;	    
	    int height = (nbrRows * rowHeight) + sbHeight;
	    //System.out.println("#rows = "+nbrRows+", sbHeight = " + sbHeight+", totalHeight="+height);	    
	    tableData.heightHint = height;
	    table.setLayoutData(tableData);  	  	    
	    table.setSize(width, height);                
        tableViewer.setLabelProvider(srtp);
        tableViewer.setContentProvider(srtp);
        //System.out.println("Input Object: "+getInputObject());
        tableViewer.setInput(getInputObject());        

        tableViewer.addSelectionChangedListener(this);
        tableViewer.getTable().addFocusListener(this);  

		// test if we need a unique name validator
		Shell shell = getShell();
		Display display = shell.getDisplay();
		if (display != null)
		  display.asyncExec(this);
		else
          run();
	    
		return composite;
	}
	

    private Table createTable(Composite parent) 
    {
	   //table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
	   table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);	   
  	   table.setLinesVisible(true);
	   tableData = new GridData();
	   tableData.horizontalAlignment = GridData.FILL;
	   tableData.grabExcessHorizontalSpace = true;
	   tableData.widthHint = 450;        
	   tableData.heightHint= 30;
	   tableData.verticalAlignment = GridData.CENTER;
	   tableData.grabExcessVerticalSpace = true;
	   table.setLayoutData(tableData);  	  
	   
	   //table.addTraverseListener(this);
	   //getShell().addTraverseListener(this);

	   
	   return table;
    }	
    private void createColumns() 
    {
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);
	    for (int i = 0; i < columnHeaders.length; i++) 
	    {
		   layout.addColumnData(columnLayouts[i]);
		   TableColumn tc = new TableColumn(table, SWT.NONE,i);
		   tc.setResizable(columnLayouts[i].resizable);
		   tc.setText(columnHeaders[i]);
		   //tc.addSelectionListener(headerListener);
	    }    	
    }
    public void selectionChanged(SelectionChangedEvent event)
    {
		//System.out.println("Selection changed. ignoreSelection? "+ignoreSelection);
    	if (ignoreSelection)
    	  return;
	    IStructuredSelection selection = (IStructuredSelection) event.getSelection();		
	    if (selection.isEmpty()) 	   
	    {
	      currRow = -1;
		  return;
	    }
	    SystemRenameTableRow selectedRow = (SystemRenameTableRow)selection.getFirstElement();
	    int rowIdx = srtp.getRowNumber(selectedRow);
	    if (rowIdx == currRow)
	      return;
    	currRow = rowIdx;	    	      
		tableViewer.editElement(getRows()[rowIdx], COLUMN_NEWNAME);	    
    }
	/**
	 * Override of parent. Must pass selected object onto the form for initializing fields.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 */
	public void setInputObject(Object inputObject)
	{
		//System.out.println("INSIDE SETINPUTOBJECT: " + inputObject);
		super.setInputObject(inputObject);
	}

	/**
	 * Called when user presses OK button. 
	 * This does not do the actual renames, but rather updates the new name array.
	 * You need to query this via {@link #getNewNames()}, after ensuring the dialog was not 
	 *  cancelled by calling {@link #wasCancelled()}.
	 */
	protected boolean processOK() 
	{
		// the following is for defect 41565 where the changed name is not used when enter pressed after typing
		if ((currRow >=0) && (currRow <= (getRows().length - 1)))
	    {
	    	String newName = ((Text)cellEditor.getControl()).getText();
	    	//System.out.println("Testing. newName = "+newName);
	    	getRows()[currRow].setNewName(newName);
	    }
	    //else
	    //  System.out.println("currRow = "+currRow);
	    
		boolean closeDialog = verify();
		if (closeDialog)
		{
		}
		return closeDialog;
	}	
	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	public boolean verify() 
	{
		SystemMessage errMsg = null;
		SystemMessage firstErrMsg = null;
		SystemRenameTableRow firstErrRow = null;
		clearErrorMessage();		
		SystemRenameTableRow[] rows = getRows();
		Vector newNames = new Vector();
		// first, clear pending errors...
		for (int idx=0; (idx<rows.length); idx++)				
			rows[idx].setErrorMessage(null);		
		// check 1: all entries have a new name, and the new name is unique for this list.
		for (int idx=0; (firstErrMsg==null) && (idx<rows.length); idx++)
		//for (int idx=0; (idx<rows.length); idx++)		
		{
			errMsg = null;
			String oldName = rows[idx].getName();
			String newName = rows[idx].getNewName();
			String canonicalNewName = rows[idx].getCanonicalNewName(); // defect 42145
			//if (oldName.equalsIgnoreCase(newName)) // does not consider case for linux or unix or quoted names on iseries
			if (rows[idx].newNameEqualsOldName())
				errMsg = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_RENAME_OLDEQUALSNEW).makeSubstitution(oldName);
		     	//errMsg = SystemMessage.sub(RSEUIPlugin.getString(MSG_VALIDATE_RENAME_OLDEQUALSNEW),SystemMessage.MSG_SUB1,newName);
			//else if (newNames.contains(newName)) defect 42145
			else if (newNames.contains(canonicalNewName))
				errMsg = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_RENAME_NOTUNIQUE).makeSubstitution(newName);
             	//errMsg = SystemMessage.sub(RSEUIPlugin.getString(MSG_VALIDATE_RENAME_NOTUNIQUE),SystemMessage.MSG_SUB1,newName);
			else
			{
           	 	ISystemValidator nameValidator = rows[idx].getNameValidator();
           	 	if (nameValidator != null)
           	 		errMsg = nameValidator.validate(newName);
           	 	if (errMsg == null)
           	 	{
           	 		ValidatorUniqueString vun = rows[idx].getUniqueNameValidator();
           	 		if (vun != null)
           	 			errMsg = vun.validate(newName);
           	 	}
           	 	if ((errMsg == null) && (rows[idx].getElement() instanceof IHost))
           	 	{
           	 		boolean ok = ValidatorConnectionName.validateNameNotInUse(newName, getShell());
           	 		if (!ok)
           	 			errMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_CANCELLED);
           	 	}
           	 		
			}			
			if (errMsg == null)
			{           	 
				//newNames.addElement(newName);
				newNames.addElement(canonicalNewName); // defect 42145
			}
			if ((errMsg != null) && (firstErrMsg == null))
			{
				firstErrMsg = errMsg;
				firstErrRow = rows[idx];
			}
			rows[idx].setErrorMessage(errMsg); // todo: convert to handle SystemMessage object
		}
		if (firstErrMsg != null)
		{
			setErrorMessage(firstErrMsg);
			tableViewer.update(rows,null);		  
			tableViewer.setSelection(new StructuredSelection(firstErrRow),true);		  
			tableViewer.editElement(firstErrRow,COLUMN_NEWNAME);
		}
		return (firstErrMsg == null);
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setNameValidator(ISystemValidator)
	 */
	protected SystemMessage validateNameInput() 
	{			
	    errorMessage= null;
	    if (errorMessage == null)
	      clearErrorMessage();
	    else
		  setErrorMessage(errorMessage);
		setPageComplete();		
		return errorMessage;		
	}

 
	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		boolean pageComplete = false;
		if (errorMessage == null)
		  pageComplete = true;
		return pageComplete;
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		setPageComplete(isPageComplete());
	}

    /**
     * Required by TraverseListener.
     * We want to know when the tab key is pressed so we can give edit focus to the next name
     */
    public void keyTraversed(TraverseEvent e)
    {
    	int detail = e.detail;    	
    	//System.out.println("in keyTraversed: " + keycode + ", " + detail + ", " + doit);
    	e.doit = false;	
    	ignoreSelection = true;
    	
    	Control focusControl = Display.getCurrent().getFocusControl();
         	
    	//System.out.println("...Key pressed. currRow = "+currRow);    	   
    	 
    	// DEFECT 41807 STATED USERS SHOULD BE ALLOWED TO TAB TO THE BUTTONS 
    	if (detail == SWT.TRAVERSE_TAB_NEXT)
    	{
    	  if (currRow != getRows().length-1)
    	  {
    	    ++currRow;
    	    //System.out.println("...D TAB pressed. currRow = "+currRow);    	    
	   	    //tableViewer.setSelection(new StructuredSelection(getRows()[currRow]),true);    
		    tableViewer.editElement(getRows()[currRow], COLUMN_NEWNAME);	        	  
    	  }
		  else
		  {
		    tableViewer.editElement(getRows()[0], COLUMN_NEWNAME);
		  	currRow = 0;
		  	e.doit = true;
		  }
    	}
    	else if (detail == SWT.TRAVERSE_TAB_PREVIOUS)
    	{
    	  if (currRow != 0)
    	  {
    	  	if (currRow > 0)
    	      --currRow;
    	    else
    	      currRow = 0;
    	    //System.out.println("...D BACKTAB pressed. currRow = "+currRow);    	    	    
	   	    //tableViewer.setSelection(new StructuredSelection(getRows()[currRow]),true);        	  
		    tableViewer.editElement(getRows()[currRow], COLUMN_NEWNAME);	        	  	   	  
    	  }
    	  else
    	  {
		    tableViewer.editElement(getRows()[getRows().length-1], COLUMN_NEWNAME);
    	    currRow = getRows().length-1;
    	    e.doit = true;
    	  }
    	}
    	else
    	  e.doit = true;  
    	ignoreSelection = false;
    }
    
	/**
	 * Returns the rows of rename items.
	 */
	public SystemRenameTableRow[] getRows()
	{
		return (SystemRenameTableRow[])srtp.getElements(getInputObject());
	}    
	
	/**
	 * Returns an array of the new names.
	 */
	public String[] getNewNames()
	{
		SystemRenameTableRow[] rows = getRows();
		String[] names = new String[rows.length];
		for (int idx=0; idx<rows.length; idx++)
		   names[idx] = rows[idx].getNewName();
		return names;
	}    	
	
	// CELL EDITOR METHODS
	public void applyEditorValue()
	{
		//System.out.println("CELLEDITOR: INSIDE APPLYEDITORVALUE");
	}
	public void cancelEditor()
	{
		//System.out.println("CELLEDITOR: INSIDE CANCELEDITOR");		
	}
	public void editorValueChanged(boolean oldValidState,boolean newValidState)
	{
		//System.out.println("CELLEDITOR: INSIDE EDITORVALUECHANGED: "+oldValidState+","+newValidState);		
	}

	/**
	 * Runnable method
	 */
	public void run()
	{
		SystemRenameTableRow[] rows = getRows();
        ValidatorUniqueString uniqueNameValidator = null;
        Object inputElement = null;
        ISystemValidator nameValidator = null;
		for (int idx=0; idx<rows.length; idx++)
		{
           nameValidator = rows[idx].getNameValidator();
           inputElement = rows[idx].getElement();
		   uniqueNameValidator = setUniqueNameValidator(inputElement, nameValidator);		
		   rows[idx].setUniqueNameValidator(uniqueNameValidator);
		}
	}

	/**
	 * Given an input element and externally-suppplied name validator for it, determine if we
	 *  need to augment that validator with one that will check for uniqueness, and if so 
	 *  create and register that uniqueness validator
	 */
	protected ValidatorUniqueString setUniqueNameValidator(Object inputElement, ISystemValidator nameValidator)
	{
		ValidatorUniqueString uniqueNameValidator = null;
		ISystemRemoteElementAdapter ra = getRemoteAdapter(inputElement);
        boolean debug = false;
        String parentName = null;
		if (ra != null)
		  parentName = ra.getAbsoluteParentName(inputElement);
		if ((ra != null) && (parentName != null))
		{
			uniqueNameValidator = (ValidatorUniqueString)uniqueNameValidatorPerParent.get(parentName);
			if (uniqueNameValidator != null)
			{
			  if (debug)
		        System.out.println("Existing name list found for parent " + parentName);
		      return uniqueNameValidator;
			}
		    else if (debug)
		      System.out.println("No existing name list found for parent " + parentName);
		}
		if ((ra != null) && (parentName != null))
		{			
           String[] names = null;
           boolean caseSensitive = ra.getSubSystem(inputElement).getSubSystemConfiguration().isCaseSensitive();
           boolean needUniqueNameValidator = !(nameValidator instanceof ISystemValidatorUniqueString);
           if (!needUniqueNameValidator)
           {
           	 String[] existingNames = ((ISystemValidatorUniqueString)nameValidator).getExistingNamesList();
		  	 needUniqueNameValidator = ((existingNames == null) || (existingNames.length==0));
           }
		   if (needUniqueNameValidator)
		   {
    		  // Set the busy cursor to all shells.
    		  super.setBusyCursor(true);
		  	  try {		  	   	 
		  	         names = ra.getRemoteParentNamesInUse(getShell(), inputElement);
		  	  } catch (Exception exc) {SystemBasePlugin.logError("Exception getting parent's child names in rename dialog",exc);}		  			
		  	  if ((names != null) && (names.length>0))
		  	  {
		  		    uniqueNameValidator = new ValidatorUniqueString(names,caseSensitive);
		            uniqueNameValidator.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                                                 RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE));
		            uniqueNameValidatorPerParent.put(parentName, uniqueNameValidator);
		            if (debug)
		            {
		  		      System.out.println("Name validator set. Names = ");
		  		      for (int idx=0; idx<names.length; idx++)
		  		         System.out.println("..."+idx+": "+names[idx]);
		            }
		  	  }
    		  // Restore cursor
    		  super.setBusyCursor(false);
		   }
		}		
		return uniqueNameValidator;
	}

    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	if (!(o instanceof IAdaptable)) 
          return (ISystemRemoteElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemRemoteElementAdapter.class);
    	return (ISystemRemoteElementAdapter)((IAdaptable)o).getAdapter(ISystemRemoteElementAdapter.class);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusGained(FocusEvent e)
	{
		/*
		IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();		
		if (selection.isEmpty()) 	   
		{
		  currRow = -1;
		  return;
		}
		SystemRenameTableRow selectedRow = (SystemRenameTableRow)selection.getFirstElement();
		int rowIdx = srtp.getRowNumber(selectedRow);
		if (rowIdx == currRow)
		  return;
		currRow = rowIdx;	    	      
		tableViewer.editElement(getRows()[rowIdx], COLUMN_NEWNAME);
		*/
		//System.out.println("Focus gained");	    
	}
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
	}

}