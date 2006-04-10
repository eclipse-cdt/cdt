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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.rse.ui.GenericMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FileEditorMappingContentProvider;
import org.eclipse.ui.dialogs.FileEditorMappingLabelProvider;


/**
 * A public implementation of the eclipse Select Types dialog.
 * <p>
 * File types are extension names without the dot. 
 * For example "java" and "class".
 * <p>
 * Call getResult() to get the array of selected types.
 */
public class SystemSelectFileTypesDialog 
        extends SystemPromptDialog
        //extends TypeFilteringDialog 
        implements ISystemMessageLine
{
	
	protected Collection initialSelections;
	
	// instruction to show user
	protected String instruction;
	
	// the final collection of selected elements, or null if this dialog was canceled
	protected Object[] result;

	// the visual selection widget group
	protected CheckboxTableViewer listViewer;

	// sizing constants
	protected final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	protected final static int SIZING_SELECTION_WIDGET_WIDTH = 300;

	// TODO: Cannot use WorkbenchMessages -- it's internal
	protected final static String TYPE_DELIMITER = GenericMessages.TypesFiltering_typeDelimiter; 
	protected Text userDefinedText;

	protected IFileEditorMapping[] currentInput;			

	/**
	 * Constructor when there are no existing types
	 * @param shell The window hosting this dialog
	 */
	public SystemSelectFileTypesDialog(Shell shell)
	{
		this(shell, new ArrayList());
	}

	/**
	 * Constructor when there are existing types.
	 * @param shell The window hosting this dialog
	 * @param currentTypes The current types as a java.util.Collection. Typically ArrayList is used
	 */
	public SystemSelectFileTypesDialog(Shell shell, Collection currentTypes) 
	{
		// TODO: Cannot use WorkbenchMessages -- it's internal
		super(shell, GenericMessages.TypesFiltering_title);
		this.initialSelections = currentTypes;
		// TODO: Cannot use WorkbenchMessages -- it's internal
	    setInstruction(GenericMessages.TypesFiltering_message); 
	    
	    // TODO - hack to make this work in  3.1
	    String id = PlatformUI.PLUGIN_ID + ".type_filtering_dialog_context";
	    setHelp(id);
	}

	/**
	 * Constructor when there are existing types.
	 * @param shell The window hosting this dialog
	 * @param currentTypes The current types as an array of Strings
	 */
	public SystemSelectFileTypesDialog(Shell shell, String[] currentTypes) 
	{
		this(shell, Arrays.asList(currentTypes));
	}

	/**
	 * Method declared on Dialog.
	 */
	protected Control createInner(Composite parent)
	{
		// page group
		Composite composite = (Composite)createInnerComposite(parent);
		createInstructionArea(composite);

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(FileEditorMappingLabelProvider.INSTANCE);
		listViewer.setContentProvider(FileEditorMappingContentProvider.INSTANCE);

		addSelectionButtons(composite);
		createUserEntryGroup(composite);
		initializeViewer();

		// initialize page
		if (this.initialSelections != null && !this.initialSelections.isEmpty())
			checkInitialSelections();

		return composite;
	}
	/**
	 * Return the Control to be given initial focus.
	 * Child classes must override this, but can return null.
	 */
	protected Control getInitialFocusControl()
	{
		return listViewer.getControl();
	}

	private Control createInnerComposite(Composite parent) 
	{
		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		return composite;
	}
	
	/**
	 * Sets the instruction text for this dialog.
	 *
	 * @param instr the instruction text
	 */
	public void setInstruction(String instr) 
	{
		this.instruction = instr;
	}
	/**
	 * Creates the message area for this dialog.
	 * <p>
	 * This method is provided to allow subclasses to decide where the message
	 * will appear on the screen.
	 * </p>
	 *
	 * @param parent the parent composite
	 * @return the message label
	 */
	protected Label createInstructionArea(Composite composite) 
	{
		Label label = new Label(composite,SWT.NONE);
		label.setText(instruction); 
		return label;
	}

	/**
	 * Add the selection and deselection buttons to the dialog.
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) 
	{
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		//Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, GenericMessages.getString("WizardTransferPage.selectAll"), false); //$NON-NLS-1$
		Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, 
		                                   SystemResources.RESID_SELECTFILES_SELECTALL_BUTTON_ROOT_LABEL, false); 

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);


		//Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, GenericMessages.getString("WizardTransferPage.deselectAll"), false); //$NON-NLS-1$
		Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, 
		                                     SystemResources.RESID_SELECTFILES_DESELECTALL_BUTTON_ROOT_LABEL, false); 
		
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(false);
			}
		};
		deselectButton.addSelectionListener(listener);
	}
	/**
	 * Add the currently-specified extensions.
	 */
	protected void addUserDefinedEntries(java.util.List result) 
	{
		StringTokenizer tokenizer = new StringTokenizer(userDefinedText.getText(), TYPE_DELIMITER);
		//Allow the *. and . prefix and strip out the extension
		while (tokenizer.hasMoreTokens()) 
		{
			String currentExtension = tokenizer.nextToken().trim();
			if (!currentExtension.equals("")) 
			{ 
				if (currentExtension.startsWith("*."))//$NON-NLS-1$
					result.add(currentExtension.substring(2));
				else 
				{
					if (currentExtension.startsWith("."))//$NON-NLS-1$
						result.add(currentExtension.substring(1));
					else
						result.add(currentExtension);
				}
			}
		}
	}
	/**
	 * Visually checks the previously-specified elements in this dialog's list 
	 * viewer.
	 */
	protected void checkInitialSelections() 
	{

		IFileEditorMapping editorMappings[] =
			PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
		ArrayList selectedMappings = new ArrayList();

		for (int i = 0; i < editorMappings.length; i++) 
		{
			IFileEditorMapping mapping = editorMappings[i];
			if (this.initialSelections.contains(mapping.getExtension()))
			{
				listViewer.setChecked(mapping, true);
				selectedMappings.add(mapping.getExtension());
			}
		}

		//Now add in the ones not selected to the user defined list
		Iterator initialIterator = this.initialSelections.iterator();
		StringBuffer entries = new StringBuffer();
		while(initialIterator.hasNext())
		{
			String nextExtension = (String) initialIterator.next();
			if(!selectedMappings.contains(nextExtension))
			{
				entries.append(nextExtension);
				entries.append(',');
			}
		}
		this.userDefinedText.setText(entries.toString());
	}

	/**
	 * Create the group that shows the user defined entries for the dialog.
	 * @param parent the parent this is being created in.
	 */
	private void createUserEntryGroup(Composite parent) 
	{
		// destination specification group
		Composite userDefinedGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		userDefinedGroup.setLayout(layout);
		userDefinedGroup.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		// TODO: Cannot use WorkbenchMessages -- it's internal
		new Label(userDefinedGroup, SWT.NONE).setText(GenericMessages.TypesFiltering_otherExtensions); 

		// user defined entry field
		userDefinedText = new Text(userDefinedGroup, SWT.SINGLE | SWT.BORDER);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		userDefinedText.setLayoutData(data);

		userDefinedText.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent event) 
			{
				if (event.widget == userDefinedText) 
				{
					//okButton.setEnabled(validateFileType(userDefinedText.getText().trim()));
					setPageComplete(validateFileType(userDefinedText.getText().trim()));
				}
			}
		});

	}
	/**
	 * Return the input to the dialog.
	 */
	protected IFileEditorMapping[] getInput() 
	{
		//Filter the mappings to be just those with a wildcard extension
		if (currentInput == null) 
		{
			java.util.List wildcardEditors = new ArrayList();
			IFileEditorMapping [] allMappings =
				PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
			for (int i = 0; i < allMappings.length; i++) 
			{
				if (allMappings[i].getName().equals("*"))//$NON-NLS-1$
					wildcardEditors.add(allMappings[i]);
			}
			currentInput = new IFileEditorMapping[wildcardEditors.size()];
			wildcardEditors.toArray(currentInput);
		}
		return currentInput;
	}
	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() 
	{
		listViewer.setInput(getInput());
	}
	/**
	 * User pressed OK.
	 */
	protected boolean processOK() 
	{
        if (!validateFileType(userDefinedText.getText().trim()))
          return false;
        
		// Get the input children.
		IFileEditorMapping[] children = getInput();
		java.util.List list = new ArrayList();
		// Build a list of selected children.
		for (int i = 0; i < children.length; ++i) 
		{
			IFileEditorMapping element = children[i];
			if (listViewer.getChecked(element))
				list.add(element.getExtension());
		}
		addUserDefinedEntries(list);
		setResult(list);
		return true;
	}
	/**
	 * Set the selections made by the user, or <code>null</code> if
	 * the selection was canceled.
	 *
	 * @param the list of selected elements, or <code>null</code> if Cancel was
	 *   pressed
	 */
	protected void setResult(java.util.List newResult) 
	{
		if (newResult == null) 
		{
			result = null;
		} 
		else 
		{
			result = new Object[newResult.size()];
			newResult.toArray(result);
		}
	}

	/**
	 * Validate the user input for a file type
	 */
	protected boolean validateFileType(String filename) 
	{
		// We need kernel api to validate the extension or a filename

		// check for empty name and extension
		if (filename.length() == 0) 
		{
			clearErrorMessage();
			return true;
		}

		// check for empty extension if there is no name
		int index = filename.indexOf('.');
		if (index == filename.length() - 1) 
		{
			if (index == 0 || (index == 1 && filename.charAt(0) == '*')) 
			{
				// TODO: Cannot use WorkbenchMessages -- it's internal
				setErrorMessage(GenericMessages.FileExtension_extensionEmptyMessage); 
				return false;
			}
		}

        int startScan = 0;		
		if (filename.startsWith("*."))
		  startScan = 2;

		// check for characters before * 
		// or no other characters
		// or next character not '.'
		index = filename.indexOf('*', startScan);
		if (index > -1) 
		{
			if (filename.length() == 1) 
			{
				// TODO: Cannot use WorkbenchMessages -- it's internal
				setErrorMessage(GenericMessages.FileExtension_extensionEmptyMessage); 
				return false;
			}		
			if (index != 0 || filename.charAt(1) != '.') 
			{
				// TODO: Cannot use WorkbenchMessages -- it's internal
				setErrorMessage(GenericMessages.FileExtension_fileNameInvalidMessage); 
				return false;
			}
		}

		clearErrorMessage();
		return true;
	}

	/**
	 * Returns the list of selections made by the user, or <code>null</code> if
	 * the selection was canceled.
	 *
	 * @return the array of selected elements, or <code>null</code> if Cancel was
	 *   pressed
	 */
	public Object[] getResult() 
	{
		return result;
	}
}