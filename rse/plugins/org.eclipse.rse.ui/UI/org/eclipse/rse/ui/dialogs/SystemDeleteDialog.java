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
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Dialog for confirming resource deletion. 
 * <p>
 * This is a re-usable dialog that you can use  directly, or via the {@link org.eclipse.rse.ui.actions.SystemCommonDeleteAction}
 *  action. It asks the user to confirm the deletion of the input selection.
 * <p>If the input objects do not adapt to {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter} or 
 *  {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter}, then they should implement the 
 *  interface {@link org.eclipse.rse.ui.dialogs.ISystemTypedObject} so that their type can be
 *  displayed in this delete confirmation dialog.
 * 
 * @see org.eclipse.rse.ui.actions.SystemCommonDeleteAction
 */
public class SystemDeleteDialog extends SystemPromptDialog 
                                implements ISystemMessages, ISystemPropertyConstants,
                                           ISelectionChangedListener
{
    private String warningMessage, warningTip;
    private String promptLabel;
    private SystemDeleteTableProvider sdtp;
    private Label prompt;
    private Table table;
    private TableViewer tableViewer;    
    private GridData tableData;
    
    // column headers
	private String columnHeaders[] = {
		"",
		SystemResources.RESID_DELETE_COLHDG_OLDNAME,
		SystemResources.RESID_DELETE_COLHDG_TYPE
	};
	
	// column layout
	private ColumnLayoutData columnLayouts[] = 
	{
		new ColumnPixelData(19, false),		
		new ColumnWeightData(150,150,true),
		new ColumnWeightData(120,120,true)
    };
	
    // give each column a property value to identify it
	private static String[] tableColumnProperties = 
	{
		ISystemPropertyConstants.P_OK,		
		IBasicPropertyConstants.P_TEXT,
		ISystemPropertyConstants.P_TYPE,		
    };    
    
	/**
	 * Constructor for SystemUpdateConnectionDialog
	 */
	public SystemDeleteDialog(Shell shell) 
	{
		super(shell, SystemResources.RESID_DELETE_TITLE);				
		super.setOkButtonLabel(SystemResources.RESID_DELETE_BUTTON);
		setHelp(RSEUIPlugin.HELPPREFIX+"ddlt0000");
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
	 * Specify a warning message to show at the top of the dialog
	 */
	public void setWarningMessage(String msg, String tip)
	{
		this.warningMessage = msg;
		this.warningTip = tip;		
	}
	
	/**
	 * Specify the text to show for the label prompt. The default is 
	 *  "Delete selected resources?"
	 */
	public void setPromptLabel(String text)
	{
		this.promptLabel = text;
	}
	
	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		return tableViewer.getControl();
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		// Inner composite
		int nbrColumns = 2;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);			
		   
        // PROMPT
        if (promptLabel == null) {
        	Object input = getInputObject();
        	
        	if (input != null && input instanceof IStructuredSelection) {
        		int size = ((IStructuredSelection)input).size();
        		
        		if (size > 1) {
        			prompt = SystemWidgetHelpers.createLabel(composite, SystemResources.RESID_DELETE_PROMPT, nbrColumns);
        		}
        		else {
        			prompt = SystemWidgetHelpers.createLabel(composite, SystemResources.RESID_DELETE_PROMPT_SINGLE, nbrColumns);
        		}
        	}
        	// should never get here
        	else {
        		prompt = SystemWidgetHelpers.createLabel(composite, SystemResources.RESID_DELETE_PROMPT, nbrColumns);       		
        	}
        }
		else {
			prompt = (Label)SystemWidgetHelpers.createVerbiage(composite, promptLabel, nbrColumns, false, 200);
		}

        // WARNING
        if (warningMessage != null)
        {
          // filler line
          SystemWidgetHelpers.createLabel(composite, "", nbrColumns);
		  // create image
		  Image image = getShell().getDisplay().getSystemImage(SWT.ICON_WARNING);
		  Label imageLabel = null;
		  if (image != null) 
		  {
			 imageLabel = new Label(composite, 0);
			 image.setBackground(imageLabel.getBackground());
			 imageLabel.setImage(image);
			 imageLabel.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_CENTER |
				GridData.VERTICAL_ALIGN_BEGINNING));
		  }
          Label warningLabel = SystemWidgetHelpers.createLabel(composite, warningMessage);
          if (warningTip != null)
          {
          	warningLabel.setToolTipText(warningTip);
          	imageLabel.setToolTipText(warningTip);
          }
          // filler line
          SystemWidgetHelpers.createLabel(composite, "", nbrColumns);
        }
			        		   
        // TABLE
        tableViewer = createTableViewer(composite, nbrColumns);        
        createColumns();
	    tableViewer.setColumnProperties(tableColumnProperties);    
                
        sdtp = new SystemDeleteTableProvider();

		int width = tableData.widthHint;
		int nbrRows = Math.min(getRows().length,8);
		int rowHeight = table.getItemHeight() + table.getGridLineWidth();
		int sbHeight = table.getHorizontalBar().getSize().y;	    
		int height = (nbrRows * rowHeight) + sbHeight;
	    
		tableData.heightHint = height;
		table.setLayoutData(tableData);  	  	    
		table.setSize(width, height);         
 
        tableViewer.setLabelProvider(sdtp);
        tableViewer.setContentProvider(sdtp);

        Object input = getInputObject();        
        tableViewer.setInput(input);       
        
		return composite;
	}
	
    private TableViewer createTableViewer(Composite parent, int nbrColumns) 
    {
	   table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.HIDE_SELECTION);
  	   table.setLinesVisible(true);
  	   tableViewer = new TableViewer(table);
	   tableData = new GridData();
	   tableData.horizontalAlignment = GridData.FILL;
	   tableData.grabExcessHorizontalSpace = true;
	   tableData.widthHint = 350;    
	   tableData.heightHint = 30;    
	   tableData.verticalAlignment = GridData.CENTER;
	   tableData.grabExcessVerticalSpace = true;
	   tableData.horizontalSpan = nbrColumns;
	   table.setLayoutData(tableData);  	  	   
	   return tableViewer;
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
	    }    	
    }
    
    public void selectionChanged(SelectionChangedEvent event)
    {   	
    }
    
	/**
	 * Override of parent. Must pass selected object onto the form for initializing fields.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 */
	public void setInputObject(Object inputObject)
	{
		super.setInputObject(inputObject);
	}

	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		return true;
	}	
	
	/**
	 * Returns the rows of deletable items.
	 */
	public SystemDeleteTableRow[] getRows()
	{
		return (SystemDeleteTableRow[])sdtp.getElements(getInputObject());
	}
	
    /**
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    protected ISystemViewElementAdapter getAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getAdapter(o);
    }	
}