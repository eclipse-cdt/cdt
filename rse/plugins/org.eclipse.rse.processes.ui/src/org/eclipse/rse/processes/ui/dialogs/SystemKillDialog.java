/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.processes.ui.dialogs;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.processes.ui.ProcessesPlugin;
import org.eclipse.rse.processes.ui.SystemProcessesResources;
import org.eclipse.rse.processes.ui.view.ISystemProcessPropertyConstants;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * Dialog for confirming killing of a process or group of processes. User
 * selects the type of signal to be sent to the process. 
 */
public class SystemKillDialog extends SystemPromptDialog
{
    private String warningMessage = SystemProcessesResources.RESID_KILL_WARNING_LABEL;
    private String warningTip = SystemProcessesResources.RESID_KILL_WARNING_TOOLTIP;
    private String promptLabel;
    private SystemKillTableProvider sktp;
    private Label prompt;
    private Table table;
    private TableViewer tableViewer;    
    private GridData tableData;
    private Combo cmbSignal;
    private String signalType;
    
    // column headers
	private String columnHeaders[] = {
		"",
		SystemProcessesResources.RESID_KILL_COLHDG_EXENAME,
		SystemProcessesResources.RESID_KILL_COLHDG_PID
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
		ISystemProcessPropertyConstants.P_PROCESS_NAME,
		ISystemProcessPropertyConstants.P_PROCESS_PID,		
    };

	/**
	 * Constructor for SystemKillDialog
	 */
	public SystemKillDialog(Shell shell) 
	{
		super(shell, SystemProcessesResources.RESID_KILL_TITLE);				
		super.setOkButtonLabel(SystemProcessesResources.RESID_KILL_BUTTON);
		setHelp(ProcessesPlugin.HELPPREFIX+"dkrp0000");
	}
	
	/**
	 * Create message line. Intercept so we can set msg line of form.
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		super.createMessageLine(c);
		return fMessageLine;
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
        			prompt = SystemWidgetHelpers.createLabel(composite, SystemProcessesResources.RESID_KILL_PROMPT, nbrColumns);
        		}
        		else {
        			prompt = SystemWidgetHelpers.createLabel(composite, SystemProcessesResources.RESID_KILL_PROMPT_SINGLE, nbrColumns);
        		}
        	}
        	// should never get here
        	else {
        		prompt = SystemWidgetHelpers.createLabel(composite, SystemProcessesResources.RESID_KILL_PROMPT, nbrColumns);       		
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
          GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
          data.widthHint = 350;
          data.grabExcessVerticalSpace = true;
          warningLabel.setLayoutData(data);
          
          // filler line
          SystemWidgetHelpers.createLabel(composite, "", nbrColumns);
        }
			        		   
        // TABLE
        tableViewer = createTableViewer(composite, nbrColumns);        
        createColumns();
	    tableViewer.setColumnProperties(tableColumnProperties);    
                
        sktp = new SystemKillTableProvider();

		int width = tableData.widthHint;
		int nbrRows = Math.min(getRows().length,8);
		int rowHeight = table.getItemHeight() + table.getGridLineWidth();
		int sbHeight = table.getHorizontalBar().getSize().y;	    
		int height = (nbrRows * rowHeight) + sbHeight;
	    
		tableData.heightHint = height;
		table.setLayoutData(tableData);  	  	    
		table.setSize(width, height);         
 
        tableViewer.setLabelProvider(sktp);
        tableViewer.setContentProvider(sktp);

        Object input = getInputObject();        
        tableViewer.setInput(input);
        
        // Signal Type combo box
        cmbSignal = SystemWidgetHelpers.createLabeledReadonlyCombo(composite, null, SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_LABEL, SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_TOOLTIP);
		cmbSignal.addModifyListener(
				new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						selectionChanged();
					}
				}
			);
        cmbSignal.setItems(getSignalTypes());
        cmbSignal.add(SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_DEFAULT, 0);
        cmbSignal.setText(cmbSignal.getItem(0));
        signalType = cmbSignal.getText();
        cmbSignal.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        
		return composite;
	}
	
	/**
	 * @return all the possible signal types that can be sent to the selected processes
	 * on that system
	 */
	private String[] getSignalTypes()
	{
		Object selObj = getInputObject();
		if (selObj instanceof IStructuredSelection)
		{
			IStructuredSelection selection = (IStructuredSelection) selObj;
			IRemoteProcess process = (IRemoteProcess) selection.getFirstElement();
			String[] types = null;
			try
			{
				types = process.getParentRemoteProcessSubSystem().getSignalTypes();
			}
			catch (SystemMessageException e)
			{
				SystemBasePlugin.logMessage(e.getSystemMessage(), e);
				return new String[] { SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_DEFAULT };
			}
			if (types == null) types = new String[] { SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_DEFAULT };
			return types;
		}
		else return new String[] { SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_DEFAULT };
	}
	
    private TableViewer createTableViewer(Composite parent, int nbrColumns) 
    {
	   table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.HIDE_SELECTION);
  	   table.setLinesVisible(true);
  	   tableViewer = new TableViewer(table);
	   tableData = new GridData();
	   tableData.horizontalAlignment = GridData.BEGINNING;
	   tableData.grabExcessHorizontalSpace = false;
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
    
    public void selectionChanged()
    { 
    	signalType = cmbSignal.getText();    	
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
	public SystemKillTableRow[] getRows()
	{
		return (SystemKillTableRow[])sktp.getElements(getInputObject());
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
    
    public String getSignal()
    {
    	if (cmbSignal == null) return "";
    	if (cmbSignal.isDisposed()) return signalType;
    	String signal = cmbSignal.getText();
    	if (signal == null) return "";
    	return signal;
    }
}