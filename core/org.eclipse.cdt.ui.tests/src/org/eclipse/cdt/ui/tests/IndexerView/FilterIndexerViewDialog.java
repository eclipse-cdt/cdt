/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.IndexerView;
import org.eclipse.cdt.internal.core.index.IIndex;
//import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author dsteffle
 */
public class FilterIndexerViewDialog extends Dialog {

	private static String getStringDescription(int index) {
		return buttonNames[index];
	}
	
	private static String getUniqueStringDescription(int index) {
		//return Index.getDescriptionOf(meta, kind, ref);
		//return buttonNames[index] + new String(ICIndexStorageConstants.encodingTypes[iAllTypes[index][2]]);
		return String.valueOf(index);
	}
    private static final int DECL_BUTTON_ID = 3;
    private static final int REF_BUTTON_ID = 2;
    private static final int TYPE_BUTTON_ID = 1;
    private static final int ALL_BUTTON_ID = 0;
    private static final String GROUPED_SELECTIONS_LABEL = "Grouped Selections:"; //$NON-NLS-1$
    private static final String ALL_BUTTON = "All"; //$NON-NLS-1$
    private static final String TYPE_BUTTON = "type"; //$NON-NLS-1$
    private static final String DECL_BUTTON = "Decl"; //$NON-NLS-1$
    private static final String REF_BUTTON = "Ref"; //$NON-NLS-1$
    private static final String BLANK_STRING = ""; //$NON-NLS-1$
    private static final String PAGE_SIZE_ = "Page Size:"; //$NON-NLS-1$
    private static final String TYPESELECTIONDIALOG_DeclLABEL = "Declarations:"; //$NON-NLS-1$
    private static final String TYPESELECTIONDIALOG_RefLABEL = "References:"; //$NON-NLS-1$
    private static final String FILTER_INDEXER_RESULTS = "Filter Indexer Results"; //$NON-NLS-1$
    private static final String SETTINGS = "Settings"; //$NON-NLS-1$
    private static final String PAGE_SIZE = "PAGE_SIZE"; //$NON-NLS-1$
	private static final String FILTER_TEXT = "Filter"; //$NON-NLS-1$
    private String fFilter = BLANK_STRING; //$NON-NLS-1$
    Text filterText = null;
    private int pageSize = IndexerNodeParent.PAGE_SIZE;
    Text pageSizeText = null;
    protected boolean [] fFilterMatcher = new boolean [iAllTypes.length];
    protected boolean[] groupedButtonSelections;

    private String message = "Filter Indexer Results (. = any character, .* = any string):"; //$NON-NLS-1$

    public static final int ENTRY_MACRO_DECL = 0;
    public static final int ENTRY_FUNCTION_DECL = 1;
    public static final int ENTRY_NAMESPACE_DECL = 2;
    public static final int ENTRY_FUNCTION_REF = 3;
    public static final int ENTRY_NAMESPACE_REF = 4;
    public static final int ENTRY_FIELD_DECL = 5;
    public static final int ENTRY_ENUMTOR_DECL = 6;
    public static final int ENTRY_METHOD_DECL = 7;
    public static final int ENTRY_FIELD_REF = 8;
    public static final int ENTRY_ENUMTOR_REF = 9;
    public static final int ENTRY_METHOD_REF = 10;
    public static final int ENTRY_TYPE_REF = 11;
    public static final int ENTRY_TYPE_DECL_TYPEDEF = 12;
    public static final int ENTRY_TYPE_DECL_CLASS = 13;
    public static final int ENTRY_TYPE_DECL_VAR = 14;
    public static final int ENTRY_TYPE_DECL_STRUCT = 15;
    public static final int ENTRY_TYPE_DECL_ENUM = 16;
    public static final int ENTRY_TYPE_DECL_UNION = 17;
    public static final int ENTRY_TYPE_DECL_DERIVED = 18;
    public static final int ENTRY_TYPE_DECL_FRIEND = 19;
    public static final int ENTRY_INCLUDE_REF = 20;

    private String fDialogSection;
    
    private static final String DIALOG_SETTINGS = FilterIndexerViewDialog.class.getName();
    private static final String SETTINGS_X_POS = "x"; //$NON-NLS-1$
    private static final String SETTINGS_Y_POS = "y"; //$NON-NLS-1$
    private static final String SETTINGS_WIDTH = "width"; //$NON-NLS-1$
    private static final String SETTINGS_HEIGHT = "height"; //$NON-NLS-1$
    
    private Point fLocation;
    private Point fSize;
    
    IndexerNodeParent root = null;
    
    private String projName = null;
    
    // this also determines the order that the buttons are displayed
    private static int[][] iAllTypes = {
    	{IIndex.MACRO,     IIndex.ANY,             IIndex.DECLARATION},
        {IIndex.FUNCTION,  IIndex.ANY,             IIndex.DECLARATION},
        {IIndex.NAMESPACE, IIndex.ANY,             IIndex.DECLARATION},     
        {IIndex.FUNCTION,  IIndex.ANY,             IIndex.REFERENCE},
        {IIndex.NAMESPACE, IIndex.ANY,             IIndex.REFERENCE},    
        {IIndex.FIELD,     IIndex.ANY,             IIndex.DECLARATION},
        {IIndex.ENUMTOR,   IIndex.ANY,             IIndex.DECLARATION},
        {IIndex.METHOD,    IIndex.ANY,             IIndex.DECLARATION},
        {IIndex.FIELD,     IIndex.ANY,             IIndex.REFERENCE},
        {IIndex.ENUMTOR,   IIndex.ANY,             IIndex.REFERENCE},
        {IIndex.METHOD,    IIndex.ANY,             IIndex.REFERENCE},
        {IIndex.TYPE,      IIndex.ANY,             IIndex.REFERENCE},
        {IIndex.TYPE,      IIndex.TYPE_TYPEDEF,    IIndex.DECLARATION},
        {IIndex.TYPE,      IIndex.TYPE_CLASS,      IIndex.DECLARATION},      
        {IIndex.VAR,       IIndex.ANY,             IIndex.DECLARATION},
        {IIndex.TYPE,      IIndex.TYPE_STRUCT,     IIndex.DECLARATION},
        {IIndex.TYPE,      IIndex.TYPE_ENUM,       IIndex.DECLARATION},
        {IIndex.TYPE,      IIndex.TYPE_UNION,      IIndex.DECLARATION},
        {IIndex.TYPE,      IIndex.TYPE_DERIVED,    IIndex.DECLARATION},
        {IIndex.TYPE,      IIndex.TYPE_FRIEND,     IIndex.DECLARATION},
        {IIndex.INCLUDE,   IIndex.ANY,             IIndex.REFERENCE}
    };
    
	private static String [] buttonNames = {
		"Macro",
		"Function",
		"Namepace",
		"Function",
		"Namespace",
		"Field",
		"Enumtor",
		"Method",
		"Field",
		"Enumtor",
		"Method",
		"Type",
		"Typedef",
		"Class",
		"Variable",
		"Struct",
		"Enum",
		"Union",
		"Derived",
		"Friend",
		"Include"
	};
    // keep track of the buttons to programmatically change their state
    protected Button[] buttons = new Button[iAllTypes.length];
    protected Button allButton = null;
    protected Button typeButton = null;
    protected Button declButton = null;
    protected Button refButton = null;

    protected FilterIndexerViewDialog(Shell parentShell, IndexerNodeParent root, String projName) {
        super(parentShell);

        this.root = root;
        this.projName = projName;
        
        for (int i = 0; i < iAllTypes.length; i++)
        	fFilterMatcher[i] = false;
        
        groupedButtonSelections = new boolean[DECL_BUTTON_ID + 1];
        for (int j = ALL_BUTTON_ID; j <= DECL_BUTTON_ID; j++)
        	groupedButtonSelections[j] = false;
        
        setDialogSettings(DIALOG_SETTINGS);
    }
    
    /**
     * Sets section name to use when storing the dialog settings.
     * 
     * @param section Name of section.
     */
    public void setDialogSettings(String section) {
        fDialogSection = section + SETTINGS + projName; 
    }
    
    public boolean close() {
        return super.close();
    }

    /*
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(FILTER_INDEXER_RESULTS);
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        readSettings(getDialogSettings());
        
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        createMessageArea(composite);
        createFilterText(composite);
        createTypeFilterArea(composite);
        createGroupedArea(composite);
        createPageSizeArea(composite);

        return composite;
    }

    /**
     * Creates a label if name was not <code>null</code>.
     * 
     * @param parent
     *            the parent composite.
     * @param name
     *            the name of the label.
     * @return returns a label if a name was given, <code>null</code>
     *         otherwise.
     */
    protected Label createLabel(Composite parent, String name) {
        if (name == null)
            return null;
        Label label = new Label(parent, SWT.NONE);
        label.setText(name);
        label.setFont(parent.getFont());
        return label;
    }

    /**
     * Creates a type filter checkbox.
     */
    private void createTypeCheckbox(Composite parent, int type) {
    	String name = getStringDescription(type);
        Image icon = IndexerViewPluginImages.get(type);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        composite.setLayout(layout);

        final int type1 = type;
        Button checkbox = new Button(composite, SWT.CHECK);
        checkbox.setFont(composite.getFont());
        checkbox.setText(name);
        checkbox.setImage(icon);
        checkbox.setSelection(fFilterMatcher[type]);
        checkbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    fFilterMatcher[type1] = aCheckbox.getSelection();
                }
            }
        });

        Label label = new Label(composite, SWT.LEFT);
        label.setFont(composite.getFont());
        label.setText(name);
        
        buttons[type] = checkbox;
    }


    /**
     * Creates an area to filter types.
     * 
     * @param parent
     *            area to create controls in
     */
    private void createTypeFilterArea(Composite parent) {
        createLabel(parent,TYPESELECTIONDIALOG_DeclLABEL); 

        Composite upperRow = new Composite(parent, SWT.NONE);
        GridLayout upperLayout = new GridLayout(3, true);
        upperLayout.verticalSpacing = 2;
        upperLayout.marginHeight = 0;
        upperLayout.marginWidth = 0;
        upperRow.setLayout(upperLayout);

        // the for loop is here to guarantee we always
        // create the checkboxes in the same order
        for (int i = 0; i < iAllTypes.length; ++i) {
				if(iAllTypes[i][2] == IIndex.DECLARATION)
					createTypeCheckbox(upperRow, i);
        }
		
        createLabel(parent,TYPESELECTIONDIALOG_RefLABEL); 
		
        Composite lowerRow = new Composite(parent, SWT.NONE);
        lowerRow.setLayout(upperLayout);
		
        // the for loop is here to guarantee we always
        // create the checkboxes in the same order
        for (int i = 0; i < iAllTypes.length; ++i) {
			if(iAllTypes[i][2] == IIndex.REFERENCE)
                createTypeCheckbox(lowerRow, i);
        }
    }

    private void createGroupedArea(Composite parent) {
        createLabel(parent, GROUPED_SELECTIONS_LABEL); 

        Composite upperRow = new Composite(parent, SWT.NONE);
        GridLayout upperLayout = new GridLayout(8, true);
        upperLayout.verticalSpacing = 2;
        upperLayout.marginHeight = 0;
        upperLayout.marginWidth = 0;
        upperRow.setLayout(upperLayout);

        allButton = new Button(upperRow, SWT.CHECK);
        allButton.setFont(upperRow.getFont());
        allButton.setText(ALL_BUTTON);
        allButton.setImage(IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_GROUPED_ALL));
        allButton.setSelection(groupedButtonSelections[ALL_BUTTON_ID]);
        allButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) { 
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i]!=null) {
                            buttons[i].setSelection(isChecked);
                            event.widget = buttons[i];
                            buttons[i].notifyListeners(SWT.Selection, event);
                        }
                    }
                    
                    // select/deselect the type, decl, ref buttons
                    typeButton.setSelection(isChecked);
                    groupedButtonSelections[TYPE_BUTTON_ID] = isChecked;
                    declButton.setSelection(isChecked);
                    groupedButtonSelections[DECL_BUTTON_ID] = isChecked;
                    refButton.setSelection(isChecked);
                    groupedButtonSelections[REF_BUTTON_ID] = isChecked;
                    groupedButtonSelections[ALL_BUTTON_ID] = isChecked;                        

                }
            }
        });

        Label label = new Label(upperRow, SWT.LEFT);
        label.setFont(upperRow.getFont());
        label.setText(ALL_BUTTON);
        
        typeButton = new Button(upperRow, SWT.CHECK);
        typeButton.setFont(upperRow.getFont());
        typeButton.setText(TYPE_BUTTON);
        typeButton.setImage(IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_GROUPED_TYPE));
        typeButton.setSelection(groupedButtonSelections[TYPE_BUTTON_ID]);
        typeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i] != null) {
                            if (iAllTypes[i][0] == IIndex.TYPE) {
                                buttons[i].setSelection(isChecked);
                                event.widget = buttons[i];
                                buttons[i].notifyListeners(SWT.Selection, event);
                            }
                        }
                    }
                    
                    groupedButtonSelections[TYPE_BUTTON_ID] = isChecked;
                    checkAllButton();
                }
            }
        });

        label = new Label(upperRow, SWT.LEFT);
        label.setFont(upperRow.getFont());
        label.setText(TYPE_BUTTON);
        
        declButton = new Button(upperRow, SWT.CHECK);
        declButton.setFont(upperRow.getFont());
        declButton.setText(DECL_BUTTON);
        declButton.setImage(IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_GROUPED_DECL));
        declButton.setSelection(groupedButtonSelections[DECL_BUTTON_ID]);
        declButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i] != null) {
                            if (iAllTypes[i][2] == IIndex.DECLARATION) {
                                buttons[i].setSelection(isChecked);
                                event.widget = buttons[i];
                                buttons[i].notifyListeners(SWT.Selection, event);
                            }
                        }
                    }
                    
                    groupedButtonSelections[DECL_BUTTON_ID] = isChecked;
                    checkAllButton();
                }
            }
        });

        label = new Label(upperRow, SWT.LEFT);
        label.setFont(upperRow.getFont());
        label.setText(DECL_BUTTON);
        
        refButton = new Button(upperRow, SWT.CHECK);
        refButton.setFont(upperRow.getFont());
        refButton.setText(REF_BUTTON);
        refButton.setImage(IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_GROUPED_REF));
        refButton.setSelection(groupedButtonSelections[REF_BUTTON_ID]);
        refButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i] != null) {
                            if (iAllTypes[i][2] == IIndex.REFERENCE) {
                                buttons[i].setSelection(isChecked);
                                event.widget = buttons[i];
                                buttons[i].notifyListeners(SWT.Selection, event);
                            }
                        }
                    }
                    
                    groupedButtonSelections[REF_BUTTON_ID] = isChecked;
                    checkAllButton();
                }
            }
        });

        label = new Label(upperRow, SWT.LEFT);
        label.setFont(upperRow.getFont());
        label.setText(REF_BUTTON);
    }
    
    void checkAllButton() {
        // alter the state of allButton if everything is checked or not
        boolean isChecked=true;
        for(int i=0; i<buttons.length; i++) {
            if (!buttons[i].getSelection()) {
                isChecked=false;
                break;
            }
        }
        
        allButton.setSelection(isChecked);
        groupedButtonSelections[ALL_BUTTON_ID] = isChecked;
    }
    
    /**
     * Creates an area to filter types.
     * 
     * @param parent
     *            area to create controls in
     */
    private void createPageSizeArea(Composite parent) {
        createLabel(parent, PAGE_SIZE_);
        
        pageSizeText = new Text(parent, SWT.BORDER);
        
        GridData data = new GridData();
        data.grabExcessVerticalSpace = false;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        pageSizeText.setLayoutData(data);
        pageSizeText.setFont(parent.getFont());

        pageSizeText.setText(String.valueOf(pageSize));
    }

    /**
     * Creates the message text widget and sets layout data.
     * 
     * @param composite
     *            the parent composite of the message area.
     */
    protected Label createMessageArea(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        if (message != null) {
            label.setText(message);
        }
        label.setFont(composite.getFont());

        GridData data = new GridData();
        data.grabExcessVerticalSpace = false;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        label.setLayoutData(data);

        return label;
    }

    protected Text createFilterText(Composite parent) {
        filterText = new Text(parent, SWT.BORDER);
        
        GridData data = new GridData();
        data.grabExcessVerticalSpace = false;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        filterText.setLayoutData(data);
        filterText.setFont(parent.getFont());

        filterText.setText((fFilter == null ? BLANK_STRING : fFilter)); 

        return filterText;
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            apply();
        }
        
        super.buttonPressed(buttonId);
    }

    public IDialogSettings getDialogSettings() {
        IDialogSettings allSettings = CUIPlugin.getDefault().getDialogSettings(); 
        IDialogSettings section = allSettings.getSection(fDialogSection);
        if (section == null) {
            section = allSettings.addNewSection(fDialogSection);
            writeDefaultSettings(section);
        }
        return section;
    }
    
    /**
     * Stores current configuration in the dialog store.
     */
    protected void writeSettings(IDialogSettings section) {
        Point location = getShell().getLocation();
        section.put(SETTINGS_X_POS, location.x);
        section.put(SETTINGS_Y_POS, location.y);

        Point size = getShell().getSize();
        section.put(SETTINGS_WIDTH, size.x);
        section.put(SETTINGS_HEIGHT, size.y);

        for(int i = 0; i < iAllTypes.length; i++) {
            section.put(
            		getUniqueStringDescription(i), 
            		fFilterMatcher[i] );
        }
        
        section.put(ALL_BUTTON, groupedButtonSelections[ALL_BUTTON_ID]);
        section.put(TYPE_BUTTON, groupedButtonSelections[TYPE_BUTTON_ID]);
        section.put(REF_BUTTON, groupedButtonSelections[REF_BUTTON_ID]);
        section.put(DECL_BUTTON, groupedButtonSelections[DECL_BUTTON_ID]);
        
        section.put(PAGE_SIZE, pageSize);
		section.put(FILTER_TEXT, fFilter);
    }

    /**
     * Stores default dialog settings.
     */
    protected void writeDefaultSettings(IDialogSettings section) {	
        for(int i = 0; i < iAllTypes.length; i++) {
        	String description = getUniqueStringDescription(i);
            section.put(description, true);
        }
              
		section.put(FILTER_TEXT, fFilter);
        section.put(PAGE_SIZE, IndexerNodeParent.PAGE_SIZE);
    }

    protected Point getInitialLocation(Point initialSize) {
        Point result = super.getInitialLocation(initialSize);
        if (fLocation != null) {
            result.x = fLocation.x;
            result.y = fLocation.y;
            Rectangle display = getShell().getDisplay().getClientArea();
            int xe = result.x + initialSize.x;
            if (xe > display.width) {
                result.x -= xe - display.width; 
            }
            int ye = result.y + initialSize.y;
            if (ye > display.height) {
                result.y -= ye - display.height; 
            }
        }
        return result;
    }
    
    protected Point getInitialSize() {
        Point result = super.getInitialSize();
        if (fSize != null) {
            result.x = Math.max(result.x, fSize.x);
            result.y = Math.max(result.y, fSize.y);
            Rectangle display = getShell().getDisplay().getClientArea();
            result.x = Math.min(result.x, display.width);
            result.y = Math.min(result.y, display.height);
        }
        return result;
    }
    
    /**
     * Initializes itself from the dialog settings with the same state
     * as at the previous invocation.
     */
    public void readSettings(IDialogSettings section) {
        try {
            int x = section.getInt(SETTINGS_X_POS);
            int y = section.getInt(SETTINGS_Y_POS);
            fLocation = new Point(x, y);
            int width = section.getInt(SETTINGS_WIDTH);
            int height = section.getInt(SETTINGS_HEIGHT);
            fSize = new Point(width, height);
            
            pageSize = section.getInt(PAGE_SIZE);
        } catch (NumberFormatException e) {
            fLocation = null;
            fSize = null;
        }
        
        for(int i = 0; i < iAllTypes.length; i++) {
        	fFilterMatcher[i] = section.getBoolean(getUniqueStringDescription(i));
        }
     
        // get the grouped button selection status
        groupedButtonSelections[ALL_BUTTON_ID] = section.getBoolean(ALL_BUTTON);
        groupedButtonSelections[TYPE_BUTTON_ID] = section.getBoolean(TYPE_BUTTON);
        groupedButtonSelections[REF_BUTTON_ID] = section.getBoolean(REF_BUTTON);
        groupedButtonSelections[DECL_BUTTON_ID] = section.getBoolean(DECL_BUTTON);
		fFilter = section.get(FILTER_TEXT);
		if (fFilter == null)
			fFilter = BLANK_STRING;
    }
    
    public IndexerFilterManager createFilterManager() {
        return new IndexerFilterManager(fFilterMatcher, fFilter);
    }
    
    private void apply() {
        fFilter = filterText.getText();
		pageSize=IndexerNodeParent.PAGE_SIZE;
        try {
            pageSize = Integer.valueOf(pageSizeText.getText()).intValue();
            if (pageSize<=0) 
				pageSize=IndexerNodeParent.PAGE_SIZE;
        } catch (NumberFormatException e) {}
		
        root.setFilterManager(fFilterMatcher, fFilter);
        root.setPageSize(pageSize);
        root.reset();
		writeSettings(getDialogSettings());
    }
    
    public int getPageSize() {
        return pageSize;
    }

}
