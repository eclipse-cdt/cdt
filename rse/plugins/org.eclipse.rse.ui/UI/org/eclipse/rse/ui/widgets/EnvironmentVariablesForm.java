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

package org.eclipse.rse.ui.widgets;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.EnvironmentVariablesPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


/**
 * Preference page which allows the user to manage persisted environment
 * variables for an RSE connection.
 */
public class EnvironmentVariablesForm extends SystemBaseForm implements SelectionListener, MouseListener {


	// constants
	private static final String VAR_NAME = "NAME";
	private static final String VAR_VALUE = "VALUE";
	private static final String[] COLUMN_PROPERTIES = {VAR_NAME, VAR_VALUE};

	// instance variables
	private Table envVarTable;
	private Vector envVars;
	private EnvironmentVariablesTableContentProvider provider;
	private Object selectedObject;
	private TableViewer envVarTableViewer;
	private String systemType;
	private String invalidNameChars;

	private Button addButton, changeButton, removeButton, moveUpButton, moveDownButton;

	// Temporary class for storing environment variable information
	// until we pass it to the commands subsystem.
	public class EnvironmentVariable {
		protected String _name;
		protected String _value;
		
		public EnvironmentVariable(String name, String value)
		{
			_name = name;
			_value = value;
		}
		
		public String getName() { return _name;}
		public String getValue() { return _value;}
		public void setName(String name) {_name = name;}
		public void setValue(String value) { _value = value;}
	}
	
	protected class EnvironmentVariablesTableContentProvider implements IStructuredContentProvider,
																		ITableLabelProvider,
																		ICellModifier, ICellEditorValidator
	{
		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return envVars.toArray();
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return ((EnvironmentVariable) element).getName();
			} else {
				return ((EnvironmentVariable) element).getValue();
			}
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

		// yantzi:2.1.2 Added ability to directly edit in env var table
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			if (VAR_NAME.equals(property))
			{ 
				return ((EnvironmentVariable) element).getName();
			}
			else // VAR_VALUE 
			{
				return ((EnvironmentVariable) element).getValue();				
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			if (VAR_NAME.equals(property)) 
			{	
				SystemMessage msg = validateName((String)value);
				if (msg != null)
				{
					getMessageLine().setErrorMessage(msg);				
				}
				else
				{
					if (invalidNameChars != null && invalidNameChars.indexOf(' ') != -1)
					{
						((EnvironmentVariable) ((TableItem)element).getData()).setName(((String) value).trim());					
					}
					else
					{
						((EnvironmentVariable) ((TableItem)element).getData()).setName((String) value);					
					}
					getMessageLine().clearErrorMessage();
				}
			}
			else
			{
				((EnvironmentVariable) ((TableItem)element).getData()).setValue((String) value);
			}

			envVarTableViewer.refresh();						
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
		 */
		public String isValid(Object value) 
		{
			SystemMessage msg = validateName((String) value);
			if (msg != null)
			{
				getMessageLine().setErrorMessage(msg);
			}
			else
			{
				getMessageLine().clearErrorMessage();
			}

			// always return null because we handle displaying the error message
			return null;
		}

	}

	/**
	 * Constructor for EnvironmentVariablesForm.
	 * @param msgLine
	 */
	public EnvironmentVariablesForm(Shell shell, ISystemMessageLine msgLine, Object selectedObject, String invalidNameChars) {
		super(shell, msgLine);
		this.selectedObject = selectedObject;
		this.invalidNameChars = invalidNameChars;
		envVars = new Vector();
		provider = new EnvironmentVariablesTableContentProvider();
		
		if (selectedObject instanceof ISubSystem)
		{
			systemType = ((ISubSystem) selectedObject).getHost().getSystemType();
		}
		
	}

	/**
	 * @see org.eclipse.rse.ui.SystemBaseForm#createContents(Composite)
	 */
	public Control createContents(Composite parent) {
		
		SystemWidgetHelpers.createLabel(parent, SystemResources.RESID_SUBSYSTEM_ENVVAR_DESCRIPTION);
		
		// Environment Variables List 
		envVarTable = new Table(parent, SWT.FULL_SELECTION |SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		envVarTable.setLinesVisible(true);
		envVarTable.setHeaderVisible(true);
		envVarTable.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_TOOLTIP);
		envVarTable.addSelectionListener(this);
		envVarTable.addMouseListener(this);

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		envVarTable.setLayout(tableLayout);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
	    gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;		  
		gd.heightHint = 200;
		envVarTable.setLayoutData(gd);
		
		// Name column
		TableColumn hostnameColumn = new TableColumn(envVarTable, SWT.NONE);
		hostnameColumn.setText(SystemResources.RESID_SUBSYSTEM_ENVVAR_NAME_TITLE);		
		
		// Value column
		TableColumn sysTypeColumn = new TableColumn(envVarTable, SWT.NONE);
		sysTypeColumn.setText(SystemResources.RESID_SUBSYSTEM_ENVVAR_VALUE_TITLE);
		
		envVarTableViewer = new TableViewer(envVarTable);
		envVarTableViewer.setContentProvider(provider);
		envVarTableViewer.setLabelProvider(provider);
		
		// yantzi:2.1.2 Added ability to directly edit in env var table		
		envVarTableViewer.setCellModifier(provider);
		envVarTableViewer.setColumnProperties(COLUMN_PROPERTIES);

		CellEditor[] cellEditors = new CellEditor[2];
		cellEditors[0] = new TextCellEditor(envVarTable);
		cellEditors[0].setValidator(provider);
		cellEditors[1] = new TextCellEditor(envVarTable);
		envVarTableViewer.setCellEditors(cellEditors);
				
		envVarTableViewer.setInput(selectedObject);		
		
		// Create the buttons for add, change, remove, move up and move down		
		Composite buttonBar = SystemWidgetHelpers.createComposite(parent, 5);

		addButton = SystemWidgetHelpers.createPushButton(buttonBar, SystemResources.RESID_PREF_SIGNON_ADD_LABEL, null);
		addButton.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_ADD_TOOLTIP);
		addButton.addSelectionListener(this);

		changeButton = SystemWidgetHelpers.createPushButton(buttonBar, SystemResources.RESID_PREF_SIGNON_CHANGE_LABEL, null);
		changeButton.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_CHANGE_TOOLTIP);
		changeButton.addSelectionListener(this);
		changeButton.setEnabled(false);

		removeButton = SystemWidgetHelpers.createPushButton(buttonBar, SystemResources.RESID_PREF_SIGNON_REMOVE_LABEL, null);
		removeButton.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_REMOVE_TOOLTIP);
		removeButton.addSelectionListener(this);
		removeButton.setEnabled(false);

		moveUpButton = SystemWidgetHelpers.createPushButton(buttonBar, SystemResources.RESID_SUBSYSTEM_ENVVAR_MOVEUP_LABEL, null);
		moveUpButton.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_MOVEUP_TOOLTIP);
		moveUpButton.addSelectionListener(this);
		moveUpButton.setEnabled(false);

		moveDownButton = SystemWidgetHelpers.createPushButton(buttonBar, SystemResources.RESID_SUBSYSTEM_ENVVAR_MOVEDOWN_LABEL, null);
		moveDownButton.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_MOVEDOWN_TOOLTIP);
		moveDownButton.addSelectionListener(this);
		moveDownButton.setEnabled(false);

		SystemWidgetHelpers.setCompositeHelp(parent, RSEUIPlugin.HELPPREFIX + "envv0000");
		
		return parent;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * Helper method for retrieving the list of existing environment variable names
	 */
	private String[] getVariableNames()
	{
		String[] existingNames = new String[envVars.size()];
		for (int i = 0; i < envVars.size(); i++)
		{
			existingNames[i] = ((EnvironmentVariable) envVars.get(i))._name;
		}
		
		return existingNames;
	}
	
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {

		if (e.getSource() == addButton) 
		{
			// Prompt the user for the new environment variable
			EnvironmentVariablesPromptDialog dialog = 
				new EnvironmentVariablesPromptDialog(
							getShell(), 
							SystemResources.RESID_SUBSYSTEM_ENVVAR_ADD_TITLE, 
							systemType,  
							invalidNameChars, 
							getVariableNames(),
							false);
				
			if (dialog.open() == Window.OK) {
				// Add new variable to model
				EnvironmentVariable var = new EnvironmentVariable(dialog.getName(), dialog.getValue());
				envVars.add(var);
				envVarTableViewer.refresh();					
				
				//dy defect 47040 position table so new variable is shown
				envVarTableViewer.reveal(var);
			}			
		} 
		else if (e.getSource() == removeButton)
		{
			int[] selections = envVarTable.getSelectionIndices();
			if (selections != null && selections.length > 0) {
				for (int loop = selections.length - 1; loop >= 0; loop--) {
					envVars.remove(selections[loop]);
				}
				envVarTableViewer.refresh();
			}
		}
		else if (e.getSource() == changeButton)
		{
			changeCurrentlySelectedVariable();
		}
		else if (e.getSource() == moveUpButton)
		{
			int[] selections = envVarTable.getSelectionIndices();
			envVarTable.deselectAll();
			
			if (selections != null) {			
				// Move the selected objects up one position in the table
				Object temp;
				for (int loop=0; loop < selections.length; loop++) {
					temp = envVars.remove(selections[loop]);
					envVars.add(selections[loop] - 1, temp);
				}
				
				envVarTableViewer.refresh();
				
				//dy defect 47040 position table so top variable is shown
				envVarTableViewer.reveal(envVars.get(selections[0] - 1));
				
				// Reselect the entries
				for (int loop=0; loop < selections.length; loop++) {
					if (selections[loop] > 0)
						envVarTable.select(selections[loop] - 1);
					else
						envVarTable.select(selections[loop]);
				}
			}
		}
		else if (e.getSource() == moveDownButton)
		{
			int[] selections = envVarTable.getSelectionIndices();
			envVarTable.deselectAll();
						
			// move the selected entries down one position in the table
			if (selections != null) {			
				Object temp;
				for (int loop=selections.length - 1; loop >= 0; loop--) {
					if (selections[loop] < envVars.size() - 1) {
						temp = envVars.remove(selections[loop]);
						envVars.add(selections[loop] + 1, temp);
					}
				}

				envVarTableViewer.refresh();

				//dy defect 47040 position table so bottom variable is shown
				envVarTableViewer.reveal(envVars.get(selections[selections.length - 1] + 1));
				
				// reselect the entries
				for (int loop=0; loop < selections.length; loop++) {
					if (selections[loop] < envVars.size() - 1)
						envVarTable.select(selections[loop] + 1);
					else
						envVarTable.select(selections[loop]);
				}
			}
		}

		// Update table buttons based on changes
		if (envVarTable.getSelectionCount() == 0) {
			changeButton.setEnabled(false);
			removeButton.setEnabled(false);
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(false);
		} else {
			int[] selectionIndices = envVarTable.getSelectionIndices();
			boolean upEnabled = true;
			boolean downEnabled = true;
			for (int loop = 0; loop < selectionIndices.length; loop++) {
				if (selectionIndices[loop] == 0)
					upEnabled = false;
				
				if (selectionIndices[loop] == (envVarTable.getItemCount() - 1))
					downEnabled = false;
			}		
			
			if (selectionIndices.length == 1)
			{
				changeButton.setEnabled(true);
			}	
			else
			{
				changeButton.setEnabled(false);
			}
			removeButton.setEnabled(true);
			moveUpButton.setEnabled(upEnabled);
			moveDownButton.setEnabled(downEnabled);
		}

	}

	/**
	 * Get the environment variables currently in the table.
	 */
	public Collection getEnvVars()
	{
		return envVars;
	}

	/**
	 * Set the input for the environment variables table
	 */	
	public void setEnvVars(Vector envVars)
	{
		this.envVars = envVars;
		if (envVarTableViewer != null)
		{
			envVarTableViewer.refresh();
		}
	}
	
	/**
	 * Helper method so the same code can be run when the user selets the chagne button or 
	 * double clicks in the table.
	 */
	private void changeCurrentlySelectedVariable()
	{
		int[] selections = envVarTable.getSelectionIndices();
		if (selections != null && selections.length == 1)
		{
			EnvironmentVariablesPromptDialog dialog = new
					EnvironmentVariablesPromptDialog(
						getShell(), 
						SystemResources.RESID_SUBSYSTEM_ENVVAR_CHANGE_TITLE, 
						systemType,  
						invalidNameChars,  
						getVariableNames(),
						true);		
			dialog.setName(((EnvironmentVariable)envVars.get(selections[0])).getName());
			dialog.setValue(((EnvironmentVariable)envVars.get(selections[0])).getValue());
			
			if (dialog.open() == Window.OK)
			{
				envVars.remove(selections[0]);
				envVars.add(selections[0], new EnvironmentVariable(dialog.getName(), dialog.getValue()));
				envVarTableViewer.refresh();					
			}						
		}
	}
	
	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e)
	{
		// Shortcut for change
		changeCurrentlySelectedVariable();
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(MouseEvent)
	 */
	public void mouseDown(MouseEvent e)
	{
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(MouseEvent)
	 */
	public void mouseUp(MouseEvent e)
	{
	}

	/**
	 * validate the environment variable's name
	 */
	private SystemMessage validateName(String value)
	{
		SystemMessage msg = null;
		String name = (String) value;
		if (name == null || name.trim().equals(""))
		{
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_ENVVAR_NONAME);					
		}
		else
		{
			if (invalidNameChars != null)
			{
				if (invalidNameChars.indexOf(' ') != -1)
				{
					name = ((String) value).trim();
				}

				// first check for invalid characters
				if (invalidNameChars != null)
				{
					for (int i = 0; i < invalidNameChars.length() && msg == null; i++)
					{
						if (name.indexOf(invalidNameChars.charAt(i)) != -1)
						{
							msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_ENVVAR_INVALIDCHAR);
						}
					}
				}
						
				// next check for duplicate env var names
				String existingName;
				int currentSelection = envVarTable.getSelectionIndex();
				for (int i = 0; i < envVars.size() && msg == null; i++)
				{
					existingName = ((EnvironmentVariable) envVars.get(i)).getName();
					if (currentSelection != i && existingName.equals(name))
					{
						msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_ENVVAR_DUPLICATE);
						msg.makeSubstitution(existingName);
					}
				}							
			}					
		}
		
		return msg;		
	}
}