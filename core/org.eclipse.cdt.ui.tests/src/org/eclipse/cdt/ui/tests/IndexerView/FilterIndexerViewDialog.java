/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.IndexerView;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoMessages;
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

    private static final int DECL_BUTTON_ID = 4;
    private static final int REF_BUTTON_ID = 3;
    private static final int TYPE_BUTTON_ID = 2;
    private static final int ALL_BUTTON_ID = 1;
    private static final String GROUPED_SELECTIONS_LABEL = "Grouped Selections:"; //$NON-NLS-1$
    private static final String ALL_BUTTON = "All"; //$NON-NLS-1$
    private static final String TYPE_BUTTON = "type"; //$NON-NLS-1$
    private static final String DECL_BUTTON = "Decl"; //$NON-NLS-1$
    private static final String REF_BUTTON = "Ref"; //$NON-NLS-1$
    private static final String BLANK_STRING = ""; //$NON-NLS-1$
    private static final String PAGE_SIZE_ = "Page Size:"; //$NON-NLS-1$
    private static final String TYPESELECTIONDIALOG_FILTERLABEL = "TypeSelectionDialog.filterLabel"; //$NON-NLS-1$
    private static final String FILTER_INDEXER_RESULTS = "Filter Indexer Results"; //$NON-NLS-1$
    private static final String SETTINGS = "Settings"; //$NON-NLS-1$
    private static final String PAGE_SIZE = "PAGE_SIZE"; //$NON-NLS-1$
    private String fFilter = BLANK_STRING; //$NON-NLS-1$
    Text filterText = null;
    private String pageSize = BLANK_STRING; //$NON-NLS-1$
    Text pageSizeText = null;
    protected Collection fFilterMatcher = new HashSet();
    protected Collection groupedButtonSelections = new HashSet();

    private String message = "Filter Indexer Results (. = any character, .* = any string):"; //$NON-NLS-1$

    public static final int ENTRY_REF = 1;
    public static final String ENTRY_REF_STRING = String.valueOf(IIndexConstants.REF);
    public static final int ENTRY_TYPE_REF = 2;
    public static final String ENTRY_TYPE_REF_STRING = String.valueOf(IIndexConstants.TYPE_REF);
    public static final String ENTRY_TYPE_DECL_STRING = String.valueOf(IIndexConstants.TYPE_DECL);
    public static final int ENTRY_FUNCTION_REF = 4;
    public static final String ENTRY_FUNCTION_REF_STRING = String.valueOf(IIndexConstants.FUNCTION_REF);
    public static final int ENTRY_FUNCTION_DECL = 5;
    public static final String ENTRY_FUNCTION_DECL_STRING = String.valueOf(IIndexConstants.FUNCTION_DECL);
    public static final int ENTRY_CONSTRUCTOR_REF = 6;
    public static final String ENTRY_CONSTRUCTOR_REF_STRING = String.valueOf(IIndexConstants.CONSTRUCTOR_REF);
    public static final int ENTRY_CONSTRUCTOR_DECL = 7;
    public static final String ENTRY_CONSTRUCTOR_DECL_STRING = String.valueOf(IIndexConstants.CONSTRUCTOR_DECL);
    public static final int ENTRY_NAMESPACE_REF = 8;
    public static final String ENTRY_NAMESPACE_REF_STRING = String.valueOf(IIndexConstants.NAMESPACE_REF);
    public static final int ENTRY_NAMESPACE_DECL = 9;
    public static final String ENTRY_NAMESPACE_DECL_STRING = String.valueOf(IIndexConstants.NAMESPACE_DECL);
    public static final int ENTRY_FIELD_REF = 10;
    public static final String ENTRY_FIELD_REF_STRING = String.valueOf(IIndexConstants.FIELD_REF);
    public static final int ENTRY_FIELD_DECL = 11;
    public static final String ENTRY_FIELD_DECL_STRING = String.valueOf(IIndexConstants.FIELD_DECL);
    public static final int ENTRY_ENUMTOR_REF = 12;
    public static final String ENTRY_ENUMTOR_REF_STRING = String.valueOf(IIndexConstants.ENUMTOR_REF);
    public static final int ENTRY_ENUMTOR_DECL = 13;
    public static final String ENTRY_ENUMTOR_DECL_STRING = String.valueOf(IIndexConstants.ENUMTOR_DECL);
    public static final int ENTRY_METHOD_REF = 14;
    public static final String ENTRY_METHOD_REF_STRING = String.valueOf(IIndexConstants.METHOD_REF);
    public static final int ENTRY_METHOD_DECL = 15;
    public static final String ENTRY_METHOD_DECL_STRING = String.valueOf(IIndexConstants.METHOD_DECL);
    public static final int ENTRY_MACRO_DECL = 16;
    public static final String ENTRY_MACRO_DECL_STRING = String.valueOf(IIndexConstants.MACRO_DECL);
    public static final int ENTRY_INCLUDE_REF = 17;
    public static final String ENTRY_INCLUDE_REF_STRING = String.valueOf(IIndexConstants.INCLUDE_REF);
    public static final int ENTRY_SUPER_REF = 18;
    public static final String ENTRY_SUPER_REF_STRING = String.valueOf(IIndexConstants.SUPER_REF);
    public static final int ENTRY_TYPE_DECL_T = 19;
    public static final String ENTRY_TYPE_DECL_T_STRING = String.valueOf(IIndexConstants.TYPEDEF_DECL);
    public static final int ENTRY_TYPE_DECL_C = 20;
    public static final String ENTRY_TYPE_DECL_C_STRING = String.valueOf(IIndexConstants.CLASS_DECL);
    public static final int ENTRY_TYPE_DECL_V = 21;
    public static final String ENTRY_TYPE_DECL_V_STRING = String.valueOf(IIndexConstants.VAR_DECL);
    public static final int ENTRY_TYPE_DECL_S = 22;
    public static final String ENTRY_TYPE_DECL_S_STRING = String.valueOf(IIndexConstants.STRUCT_DECL);
    public static final int ENTRY_TYPE_DECL_E = 23;
    public static final String ENTRY_TYPE_DECL_E_STRING = String.valueOf(IIndexConstants.ENUM_DECL);
    public static final int ENTRY_TYPE_DECL_U = 24;
    public static final String ENTRY_TYPE_DECL_U_STRING = String.valueOf(IIndexConstants.UNION_DECL);
    public static final int ENTRY_TYPE_DECL_D = 25;
    public static final String ENTRY_TYPE_DECL_D_STRING = String.valueOf(IIndexConstants.TYPE_DECL) + String.valueOf(IIndexConstants.DERIVED_SUFFIX) + String.valueOf(IIndexConstants.SEPARATOR);
    public static final int ENTRY_TYPE_DECL_F = 26;
    public static final String ENTRY_TYPE_DECL_F_STRING = String.valueOf(IIndexConstants.TYPE_DECL) + String.valueOf(IIndexConstants.FRIEND_SUFFIX) + String.valueOf(IIndexConstants.SEPARATOR);
    public static final int ENTRY_TYPE_DECL_G = 27;
    public static final String ENTRY_TYPE_DECL_G_STRING = String.valueOf(IIndexConstants.TYPE_DECL) + String.valueOf(IIndexConstants.FWD_CLASS_SUFFIX) + String.valueOf(IIndexConstants.SEPARATOR);
    public static final int ENTRY_TYPE_DECL_H = 28;
    public static final String ENTRY_TYPE_DECL_H_STRING = String.valueOf(IIndexConstants.TYPE_DECL) + String.valueOf(IIndexConstants.FWD_STRUCT_SUFFIX) + String.valueOf(IIndexConstants.SEPARATOR);
    public static final int ENTRY_TYPE_DECL_I = 29;
    public static final String ENTRY_TYPE_DECL_I_STRING = String.valueOf(IIndexConstants.TYPE_DECL) + String.valueOf(IIndexConstants.FWD_UNION_SUFFIX) + String.valueOf(IIndexConstants.SEPARATOR);

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
    private static final int[] fAllTypes = { // ENTRY_TYPE_DECL,
        ENTRY_REF,              ENTRY_SUPER_REF,        ENTRY_MACRO_DECL,
        ENTRY_FUNCTION_DECL,    ENTRY_NAMESPACE_DECL,   ENTRY_CONSTRUCTOR_DECL,    
        ENTRY_FUNCTION_REF,     ENTRY_NAMESPACE_REF,    ENTRY_CONSTRUCTOR_REF,
        ENTRY_FIELD_DECL,       ENTRY_ENUMTOR_DECL,     ENTRY_METHOD_DECL,
        ENTRY_FIELD_REF,        ENTRY_ENUMTOR_REF,      ENTRY_METHOD_REF,
        ENTRY_TYPE_REF,         ENTRY_TYPE_DECL_T,      ENTRY_TYPE_DECL_C,      
        ENTRY_TYPE_DECL_V,      ENTRY_TYPE_DECL_S,      ENTRY_TYPE_DECL_E,
        ENTRY_TYPE_DECL_U,      ENTRY_TYPE_DECL_D,      ENTRY_TYPE_DECL_F,
        ENTRY_TYPE_DECL_G,      ENTRY_TYPE_DECL_H,      ENTRY_TYPE_DECL_I, 
        ENTRY_INCLUDE_REF };
    
    private Set fKnownTypes = new HashSet(fAllTypes.length);
    
    // keep track of the buttons to programmatically change their state
    protected Button[] buttons = new Button[fAllTypes.length];
    protected Button allButton = null;
    protected Button typeButton = null;
    protected Button declButton = null;
    protected Button refButton = null;

    protected FilterIndexerViewDialog(Shell parentShell, IndexerNodeParent root, String projName) {
        super(parentShell);

        this.root = root;
        this.projName = projName;
        
        setVisibleTypes(fAllTypes);
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
     * Sets which CElement types are visible in the dialog.
     * 
     * @param types
     *            Array of CElement types.
     */
    public void setVisibleTypes(int[] types) {
        fKnownTypes.clear();
        for (int i = 0; i < types.length; ++i) {
            fKnownTypes.add(new Integer(types[i]));
        }
    }

    /**
     * Creates a type filter checkbox.
     */
    private void createTypeCheckbox(Composite parent, Integer typeObject) {
        String name;
        int type = typeObject.intValue();
        switch (type) {
        case ENTRY_REF:
            name = ENTRY_REF_STRING;
            break;
        case ENTRY_TYPE_REF:
            name = ENTRY_TYPE_REF_STRING;
            break;
//        case ENTRY_TYPE_DECL:
//            name = ENTRY_TYPE_DECL_STRING;
//            break;
        case ENTRY_FUNCTION_REF:
            name = ENTRY_FUNCTION_REF_STRING;
            break;
        case ENTRY_FUNCTION_DECL:
            name = ENTRY_FUNCTION_DECL_STRING;
            break;
        case ENTRY_CONSTRUCTOR_REF:
            name = ENTRY_CONSTRUCTOR_REF_STRING;
            break;
        case ENTRY_CONSTRUCTOR_DECL:
            name = ENTRY_CONSTRUCTOR_DECL_STRING;
            break;
        case ENTRY_NAMESPACE_REF:
            name = ENTRY_NAMESPACE_REF_STRING;
            break;
        case ENTRY_NAMESPACE_DECL:
            name = ENTRY_NAMESPACE_DECL_STRING;
            break;
        case ENTRY_FIELD_REF:
            name = ENTRY_FIELD_REF_STRING;
            break;
        case ENTRY_FIELD_DECL:
            name = ENTRY_FIELD_DECL_STRING;
            break;
        case ENTRY_ENUMTOR_REF:
            name = ENTRY_ENUMTOR_REF_STRING;
            break;
        case ENTRY_ENUMTOR_DECL:
            name = ENTRY_ENUMTOR_DECL_STRING;
            break;
        case ENTRY_METHOD_REF:
            name = ENTRY_METHOD_REF_STRING;
            break;
        case ENTRY_METHOD_DECL:
            name = ENTRY_METHOD_DECL_STRING;
            break;
        case ENTRY_MACRO_DECL:
            name = ENTRY_MACRO_DECL_STRING;
            break;
        case ENTRY_INCLUDE_REF:
            name = ENTRY_INCLUDE_REF_STRING;
            break;
        case ENTRY_SUPER_REF:
            name = ENTRY_SUPER_REF_STRING;
            break;
        case ENTRY_TYPE_DECL_T:
            name = ENTRY_TYPE_DECL_T_STRING;
            break;
        case ENTRY_TYPE_DECL_C:
            name = ENTRY_TYPE_DECL_C_STRING;
            break;
        case ENTRY_TYPE_DECL_V:
            name = ENTRY_TYPE_DECL_V_STRING;
            break;
        case ENTRY_TYPE_DECL_S:
            name = ENTRY_TYPE_DECL_S_STRING;
            break;
        case ENTRY_TYPE_DECL_E:
            name = ENTRY_TYPE_DECL_E_STRING;
            break;
        case ENTRY_TYPE_DECL_U:
            name = ENTRY_TYPE_DECL_U_STRING;
            break;
        case ENTRY_TYPE_DECL_D:
            name = ENTRY_TYPE_DECL_D_STRING;
            break;
        case ENTRY_TYPE_DECL_F:
            name = ENTRY_TYPE_DECL_F_STRING;
            break;
        case ENTRY_TYPE_DECL_G:
            name = ENTRY_TYPE_DECL_G_STRING;
            break;
        case ENTRY_TYPE_DECL_H:
            name = ENTRY_TYPE_DECL_H_STRING;
            break;
        case ENTRY_TYPE_DECL_I:
            name = ENTRY_TYPE_DECL_I_STRING;
            break;
        default:
            return;
        }
        Image icon = getTypeIcon(type); 

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        composite.setLayout(layout);

        final Integer fTypeObject = typeObject;
        Button checkbox = new Button(composite, SWT.CHECK);
        checkbox.setFont(composite.getFont());
        checkbox.setText(name);
        checkbox.setImage(icon);
        checkbox.setSelection(fFilterMatcher.contains(fTypeObject));
        checkbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    if (aCheckbox.getSelection())
                        fFilterMatcher.add(fTypeObject);
                    else
                        fFilterMatcher.remove(fTypeObject);
                }
            }
        });

        Label label = new Label(composite, SWT.LEFT);
        label.setFont(composite.getFont());
        label.setText(name);
        
        buttons = (Button[])ArrayUtil.append(Button.class, buttons, checkbox);
    }
    
    private Image getTypeIcon(int type)
    {
        switch (type)
        {
        case ENTRY_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_REF);
        case ENTRY_TYPE_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_TYPE_REF);
//        case ENTRY_TYPE_DECL:
//            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_TYPE_DECL);
        case ENTRY_FUNCTION_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FUNCTION_REF);
        case ENTRY_FUNCTION_DECL:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FUNCTION_DECL);
        case ENTRY_CONSTRUCTOR_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_CONSTRUCTOR_REF);
        case ENTRY_CONSTRUCTOR_DECL:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_CONSTRUCTOR_DECL);
        case ENTRY_NAMESPACE_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_NAMESPACE_REF);
        case ENTRY_NAMESPACE_DECL:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_NAMESPACE_DECL);
        case ENTRY_FIELD_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FIELD_REF);
        case ENTRY_FIELD_DECL:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FIELD_DECL);
        case ENTRY_ENUMTOR_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_ENUMTOR_REF);
        case ENTRY_ENUMTOR_DECL:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_ENUMTOR_DECL);
        case ENTRY_METHOD_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_METHOD_REF);
        case ENTRY_METHOD_DECL:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_METHOD_DECL);
        case ENTRY_MACRO_DECL:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_MACRO_DECL);
        case ENTRY_INCLUDE_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_INCLUDE_REF);
        case ENTRY_SUPER_REF:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_SUPER_REF);
        case ENTRY_TYPE_DECL_T:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_TYPEDEF);
        case ENTRY_TYPE_DECL_C:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_CLASS);
        case ENTRY_TYPE_DECL_V:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_VARIABLE);
        case ENTRY_TYPE_DECL_S:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_STRUCT);
        case ENTRY_TYPE_DECL_E:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_ENUM);
        case ENTRY_TYPE_DECL_U:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_UNION);
        case ENTRY_TYPE_DECL_D:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_DERIVED);
        case ENTRY_TYPE_DECL_F:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FRIEND);
        case ENTRY_TYPE_DECL_G:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FWD_CLASS);
        case ENTRY_TYPE_DECL_H:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FWD_STRUCT);
        case ENTRY_TYPE_DECL_I:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_FWD_UNION);
        default:
            return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_WARNING);
        }
    }

    /**
     * Creates an area to filter types.
     * 
     * @param parent
     *            area to create controls in
     */
    private void createTypeFilterArea(Composite parent) {
        createLabel(parent, TypeInfoMessages
                .getString(TYPESELECTIONDIALOG_FILTERLABEL)); 

        Composite upperRow = new Composite(parent, SWT.NONE);
        GridLayout upperLayout = new GridLayout(3, true);
        upperLayout.verticalSpacing = 2;
        upperLayout.marginHeight = 0;
        upperLayout.marginWidth = 0;
        upperRow.setLayout(upperLayout);

        // the for loop is here to guarantee we always
        // create the checkboxes in the same order
        for (int i = 0; i < fAllTypes.length; ++i) {
            Integer typeObject = new Integer(fAllTypes[i]);
            if (fKnownTypes.contains(typeObject))
                createTypeCheckbox(upperRow, typeObject);
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
        allButton.setSelection(groupedButtonSelections.contains(new Integer(ALL_BUTTON_ID)));
        allButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) { 
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i]!=null) {
                            if (isChecked) buttons[i].setSelection(true);
                            else buttons[i].setSelection(false);
                            event.widget = buttons[i];
                            buttons[i].notifyListeners(SWT.Selection, event);
                        }
                    }
                    
                    // select/deselect the type, decl, ref buttons
                    if (isChecked) {
                        typeButton.setSelection(true);
                        groupedButtonSelections.add(new Integer(TYPE_BUTTON_ID));
                        declButton.setSelection(true);
                        groupedButtonSelections.add(new Integer(DECL_BUTTON_ID));
                        refButton.setSelection(true);
                        groupedButtonSelections.add(new Integer(REF_BUTTON_ID));
                        groupedButtonSelections.add(new Integer(ALL_BUTTON_ID));                        
                    }
                    else {
                        typeButton.setSelection(false);
                        groupedButtonSelections.remove(new Integer(TYPE_BUTTON_ID));
                        declButton.setSelection(false);
                        groupedButtonSelections.remove(new Integer(DECL_BUTTON_ID));
                        refButton.setSelection(false);
                        groupedButtonSelections.remove(new Integer(REF_BUTTON_ID));
                        groupedButtonSelections.remove(new Integer(ALL_BUTTON_ID));
                    }
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
        typeButton.setSelection(groupedButtonSelections.contains(new Integer(TYPE_BUTTON_ID)));
        typeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i] != null) {
                            if (buttons[i].getText().indexOf(TYPE_BUTTON) >= 0) {
                                if (isChecked) buttons[i].setSelection(true);
                                else buttons[i].setSelection(false);
                                event.widget = buttons[i];
                                buttons[i].notifyListeners(SWT.Selection, event);
                            }
                        }
                    }
                    
                    if (isChecked) groupedButtonSelections.add(new Integer(TYPE_BUTTON_ID));
                    else groupedButtonSelections.remove(new Integer(TYPE_BUTTON_ID));
                    
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
        declButton.setSelection(groupedButtonSelections.contains(new Integer(DECL_BUTTON_ID)));
        declButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i] != null) {
                            if (buttons[i].getText().indexOf(DECL_BUTTON) >= 0) {
                                if (isChecked) buttons[i].setSelection(true);
                                else buttons[i].setSelection(false);
                                event.widget = buttons[i];
                                buttons[i].notifyListeners(SWT.Selection, event);
                            }
                        }
                    }
                    
                    if (isChecked) groupedButtonSelections.add(new Integer(DECL_BUTTON_ID));
                    else groupedButtonSelections.remove(new Integer(DECL_BUTTON_ID));
                    
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
        refButton.setSelection(groupedButtonSelections.contains(new Integer(REF_BUTTON_ID)));
        refButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget instanceof Button) {
                    Button aCheckbox = (Button) e.widget;
                    boolean isChecked = aCheckbox.getSelection();
                    
                    Event event = new Event();
                    
                    // select/deselect all of the buttons in the buttons array
                    for(int i=0; i<buttons.length; i++) {
                        if (buttons[i] != null) {
                            if (buttons[i].getText().toUpperCase().indexOf(REF_BUTTON.toUpperCase()) >= 0) {
                                if (isChecked) buttons[i].setSelection(true);
                                else buttons[i].setSelection(false);
                                event.widget = buttons[i];
                                buttons[i].notifyListeners(SWT.Selection, event);
                            }
                        }
                    }
                    
                    if (isChecked) groupedButtonSelections.add(new Integer(REF_BUTTON_ID));
                    else groupedButtonSelections.remove(new Integer(REF_BUTTON_ID));
                    
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
        
        if (isChecked) {
            allButton.setSelection(true);
            groupedButtonSelections.add(new Integer(ALL_BUTTON_ID));
        }
        else {
            allButton.setSelection(false);
            groupedButtonSelections.remove(new Integer(ALL_BUTTON_ID));
        }
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

        pageSizeText.setText((pageSize == null ? BLANK_STRING : pageSize));
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

        section.put(ENTRY_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_REF)));
        section.put(ENTRY_TYPE_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_REF)));
//        section.put(ENTRY_TYPE_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL)));
        section.put(ENTRY_FUNCTION_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_FUNCTION_REF)));
        section.put(ENTRY_FUNCTION_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_FUNCTION_DECL)));
        section.put(ENTRY_CONSTRUCTOR_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_CONSTRUCTOR_REF)));
        section.put(ENTRY_CONSTRUCTOR_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_CONSTRUCTOR_DECL)));
        section.put(ENTRY_NAMESPACE_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_NAMESPACE_REF)));
        section.put(ENTRY_NAMESPACE_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_NAMESPACE_DECL)));
        section.put(ENTRY_FIELD_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_FIELD_REF)));
        section.put(ENTRY_FIELD_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_FIELD_DECL)));
        section.put(ENTRY_ENUMTOR_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_ENUMTOR_REF)));
        section.put(ENTRY_ENUMTOR_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_ENUMTOR_DECL)));
        section.put(ENTRY_METHOD_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_METHOD_REF)));
        section.put(ENTRY_METHOD_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_METHOD_DECL)));
        section.put(ENTRY_MACRO_DECL_STRING, fFilterMatcher.contains(new Integer(ENTRY_MACRO_DECL)));
        section.put(ENTRY_INCLUDE_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_INCLUDE_REF)));
        section.put(ENTRY_SUPER_REF_STRING, fFilterMatcher.contains(new Integer(ENTRY_SUPER_REF)));
        section.put(ENTRY_TYPE_DECL_T_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_T)));
        section.put(ENTRY_TYPE_DECL_C_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_C)));
        section.put(ENTRY_TYPE_DECL_V_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_V)));
        section.put(ENTRY_TYPE_DECL_S_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_S)));
        section.put(ENTRY_TYPE_DECL_E_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_E)));
        section.put(ENTRY_TYPE_DECL_U_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_U)));
        section.put(ENTRY_TYPE_DECL_D_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_D)));
        section.put(ENTRY_TYPE_DECL_F_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_F)));
        section.put(ENTRY_TYPE_DECL_G_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_G)));
        section.put(ENTRY_TYPE_DECL_H_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_H)));
        section.put(ENTRY_TYPE_DECL_I_STRING, fFilterMatcher.contains(new Integer(ENTRY_TYPE_DECL_I)));
        
        section.put(ALL_BUTTON, groupedButtonSelections.contains(new Integer(ALL_BUTTON_ID)));
        section.put(TYPE_BUTTON, groupedButtonSelections.contains(new Integer(TYPE_BUTTON_ID)));
        section.put(REF_BUTTON, groupedButtonSelections.contains(new Integer(REF_BUTTON_ID)));
        section.put(DECL_BUTTON, groupedButtonSelections.contains(new Integer(DECL_BUTTON_ID)));
        
        section.put(PAGE_SIZE, pageSize);
    }

    /**
     * Stores default dialog settings.
     */
    protected void writeDefaultSettings(IDialogSettings section) {
        section.put(ENTRY_REF_STRING, true); 
        section.put(ENTRY_TYPE_REF_STRING, true); 
//        section.put(ENTRY_TYPE_DECL_STRING, true); 
        section.put(ENTRY_FUNCTION_REF_STRING, true); 
        section.put(ENTRY_FUNCTION_DECL_STRING, true); 
        section.put(ENTRY_CONSTRUCTOR_REF_STRING, true); 
        section.put(ENTRY_CONSTRUCTOR_DECL_STRING, true); 
        section.put(ENTRY_NAMESPACE_REF_STRING, true); 
        section.put(ENTRY_NAMESPACE_DECL_STRING, true); 
        section.put(ENTRY_FIELD_REF_STRING, true); 
        section.put(ENTRY_FIELD_DECL_STRING, true); 
        section.put(ENTRY_ENUMTOR_REF_STRING, true); 
        section.put(ENTRY_ENUMTOR_DECL_STRING, true); 
        section.put(ENTRY_METHOD_REF_STRING, true); 
        section.put(ENTRY_METHOD_DECL_STRING, true); 
        section.put(ENTRY_MACRO_DECL_STRING, true); 
        section.put(ENTRY_INCLUDE_REF_STRING, true); 
        section.put(ENTRY_SUPER_REF_STRING, true); 
        section.put(ENTRY_TYPE_DECL_T_STRING, true); 
        section.put(ENTRY_TYPE_DECL_C_STRING, true); 
        section.put(ENTRY_TYPE_DECL_V_STRING, true); 
        section.put(ENTRY_TYPE_DECL_S_STRING, true); 
        section.put(ENTRY_TYPE_DECL_E_STRING, true); 
        section.put(ENTRY_TYPE_DECL_U_STRING, true); 
        section.put(ENTRY_TYPE_DECL_D_STRING, true); 
        section.put(ENTRY_TYPE_DECL_F_STRING, true); 
        section.put(ENTRY_TYPE_DECL_G_STRING, true); 
        section.put(ENTRY_TYPE_DECL_H_STRING, true); 
        section.put(ENTRY_TYPE_DECL_I_STRING, true);
        
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
            
            pageSize = String.valueOf(section.getInt(PAGE_SIZE));
        } catch (NumberFormatException e) {
            fLocation = null;
            fSize = null;
        }
        
        if (section.getBoolean(ENTRY_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
//        if (section.getBoolean(ENTRY_TYPE_DECL_STRING)) {
//            Integer typeObject = new Integer(ENTRY_TYPE_DECL);
//            if (fKnownTypes.contains(typeObject))
//                fFilterMatcher.add(typeObject);
//        }
        if (section.getBoolean(ENTRY_FUNCTION_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_FUNCTION_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_FUNCTION_DECL_STRING)) {
            Integer typeObject = new Integer(ENTRY_FUNCTION_DECL);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_CONSTRUCTOR_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_CONSTRUCTOR_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_CONSTRUCTOR_DECL_STRING)) {
            Integer typeObject = new Integer(ENTRY_CONSTRUCTOR_DECL);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_NAMESPACE_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_NAMESPACE_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_NAMESPACE_DECL_STRING)) {
            Integer typeObject = new Integer(ENTRY_NAMESPACE_DECL);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_FIELD_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_FIELD_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_FIELD_DECL_STRING)) {
            Integer typeObject = new Integer(ENTRY_FIELD_DECL);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_ENUMTOR_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_ENUMTOR_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_ENUMTOR_DECL_STRING)) {
            Integer typeObject = new Integer(ENTRY_ENUMTOR_DECL);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_METHOD_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_METHOD_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_METHOD_DECL_STRING)) {
            Integer typeObject = new Integer(ENTRY_METHOD_DECL);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_MACRO_DECL_STRING)) {
            Integer typeObject = new Integer(ENTRY_MACRO_DECL);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_INCLUDE_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_INCLUDE_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_SUPER_REF_STRING)) {
            Integer typeObject = new Integer(ENTRY_SUPER_REF);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_T_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_T);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_C_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_C);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_V_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_V);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_S_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_S);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_E_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_E);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_U_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_U);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_D_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_D);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_F_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_F);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_G_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_G);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_H_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_H);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        if (section.getBoolean(ENTRY_TYPE_DECL_I_STRING)) {
            Integer typeObject = new Integer(ENTRY_TYPE_DECL_I);
            if (fKnownTypes.contains(typeObject))
                fFilterMatcher.add(typeObject);
        }
        
        // get the grouped button selection status
        if (section.getBoolean(ALL_BUTTON)) {
            Integer typeObject = new Integer(ALL_BUTTON_ID);
            groupedButtonSelections.add(typeObject);
        }
        if (section.getBoolean(TYPE_BUTTON)) {
            Integer typeObject = new Integer(TYPE_BUTTON_ID);
            groupedButtonSelections.add(typeObject);
        }
        if (section.getBoolean(REF_BUTTON)) {
            Integer typeObject = new Integer(REF_BUTTON_ID);
            groupedButtonSelections.add(typeObject);
        }
        if (section.getBoolean(DECL_BUTTON)) {
            Integer typeObject = new Integer(DECL_BUTTON_ID);
            groupedButtonSelections.add(typeObject);
        }
    }
    
    public IndexerFilterManager createFilterManager() {
        return new IndexerFilterManager(fFilterMatcher, fFilter);
    }
    
    private void apply() {
        fFilter = filterText.getText();
        pageSize = pageSizeText.getText();
        writeSettings(getDialogSettings());
        root.setFilterManager(fFilterMatcher, fFilter);
        
        int size=IndexerNodeParent.PAGE_SIZE;
        try {
            size = Integer.valueOf(pageSize).intValue();
            if (size<=0) size=IndexerNodeParent.PAGE_SIZE;
        } catch (NumberFormatException e) {}
        
        root.setPageSize(size);
        root.reset();
    }
    
    public String getPageSize() {
        return pageSize;
    }

}
