/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.ui.controls;


import java.util.StringTokenizer;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ControlFactory {

	public static Control setParentColors(Control control) {
		Composite parent = control.getParent();
	    control.setBackground(parent.getBackground());
	    control.setForeground(parent.getForeground());
	    return control;
	}
		
	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	public static Composite createComposite(Composite parent, int numColumns) {
		return createCompositeEx(parent, numColumns, GridData.FILL_HORIZONTAL);
	}

	/**
	 * Creates composite control and sets the specified layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @param layoutMode - GridData modes that should be applied to this control
	 * @return the newly-created coposite
	 */
	public static Composite createCompositeEx(Composite parent, int numColumns, int layoutMode) {
		Composite composite = new Composite(parent, SWT.NULL);
	
		composite.setLayout(new GridLayout(numColumns, true));
		composite.setLayoutData(new GridData(layoutMode));
		return composite;
	}

	/**
	 * Creates thick separator.
	 *
	 * @param parent  the parent of the new composite
	 * @param color the separator color
	 * @return preferedThickness - the  prefered thickness of separator (or 2 if SWT.DEFAULT)
	 */
	public static Composite createCompositeSeparator(Composite parent, Color color, int preferedHeight) {
		Composite separator = createComposite(parent, 1);
		GridData gd = (GridData)separator.getLayoutData();
		gd.heightHint = ((SWT.DEFAULT == preferedHeight) ? 2 : preferedHeight);
		separator.setLayoutData(gd);
		separator.setBackground(color);
		return separator;
	}

	/**
	 * Creates thick separator.
	 *
	 * @param parent  the parent of the new composite
	 * @param color the separator color
	 * @return preferedThickness - the  prefered thickness of separator (or 2 if SWT.DEFAULT)
	 */
	public static Label createSeparator(Composite parent, int nCols) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = nCols;
		separator.setLayoutData(data);
		return separator;
	}
	/**
	 * Creates a spacer control.
	 * @param parent The parent composite
	 */		
	public static Control createEmptySpace(Composite parent) {
		return createEmptySpace(parent, 1);
	}
	/**
	 * Creates a spacer control with the given span.
	 * The composite is assumed to have <code>MGridLayout</code> as
	 * layout.
	 * @param parent The parent composite
	 */			
	public static Control createEmptySpace(Composite parent, int span) {
		Label label= new Label(parent, SWT.LEFT);
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= span;
		gd.horizontalIndent= 0;
		gd.widthHint= 0;
		gd.heightHint= 0;
		label.setLayoutData(gd);
		return label;
	}

	/**
	 * Creates an new label (basic method)
	 * 
	 *
	 * @param parent  parent object
	 * @param text  the label text
	 * @param widthHint - recommended widget width
	 * @param heightHint - recommended widget height
	 * @param style - control style
	 * @return the new label
	 */ 
	public static Label createLabel(Composite parent, String text, int widthHint, int heightHint, int style) {

		Label label = new Label(parent, style);		
		label.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = widthHint;
		gd.heightHint = heightHint;
		label.setLayoutData(gd);
		return label;
	}

	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	public static Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, SWT.DEFAULT, SWT.DEFAULT, SWT.LEFT);
	}

    /**
	 * Utility method that creates a label instance
	 * and sets the default layout data and sets the 
     * font attributes to be SWT.BOLD.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	public static Label createBoldLabel(Composite parent, String text) {
		Label label = createLabel( parent, text );
        FontData[] fd = label.getFont().getFontData();
        fd[0].setStyle( SWT.BOLD );
        Font font = new Font( Display.getCurrent(), fd[0] );
        label.setFont( font );
		return label;
	}


	/**
	 * Creates an new Wrapped label 
	 * 
	 *
	 * @param parent  parent object
	 * @param text  the label text
	 * @param widthHint - recommended widget width
	 * @param heightHint - recommended widget height
	 * @return the new label
	 */ 
	public static Label createWrappedLabel(Composite parent, String text, int widthHint, int heightHint) {
		return createLabel(parent, text, widthHint, heightHint, SWT.LEFT | SWT.WRAP);
	}


	/**
	 * Creates an new checkbox instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */ 
	public static Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
	    button.setBackground(group.getBackground());
	    button.setForeground(group.getForeground());
		return button;
	}

	/**
	 * Creates an new checkbox instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */ 
	public static Button createCheckBoxEx(Composite group, String label, int style) {
		Button button = new Button(group, SWT.CHECK | style);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
	    button.setBackground(group.getBackground());
	    button.setForeground(group.getForeground());
		return button;
	}

	/**
	 * Creates an new radiobutton instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the radiobutton
	 * @param label  the string to set into the radiobutton
	 * @param value  the string to identify radiobutton
	 * @return the new checkbox
	 */ 
	public static Button createRadioButton(Composite group, String label, String value, SelectionListener listener) {
		Button button = new Button(group, SWT.RADIO | SWT.LEFT);
		button.setText(label);
		button.setData((null == value) ? label : value);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		if(null != listener)
			button.addSelectionListener(listener);
		return button;
	}
	
	/**
	 * Utility method that creates a push button instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new button
	 * @param label  the label for the new button
	 * @return the newly-created button
	 */
	public static Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
//		button.addSelectionListener(this);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		button.setLayoutData(data);
		return button;
	}

    	
	/**
	 * Create a text field specific for this application
	 *
	 * @param parent  the parent of the new text field
	 * @return the new text field
	 */
	public static Text createTextField(Composite parent) {
		return createTextField(parent, SWT.SINGLE | SWT.BORDER);
	}
		
	public static Text createTextField(Composite parent, int style) {
		Text text = new Text(parent, style);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData(data);
		
		return text;
	}
	
	/**
	 * Create a group box
	 *
	 * @param parent  the parent of the new control
	 * @param label  the group box label
	 * @param nColumns - number of layout columns
	 * @return the new group box
	 */
    public static Group createGroup(Composite parent, String label, int nColumns) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(label);
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
        return group;    	
    }


	/**
	 * Create a List box
	 *
	 * @param parent  the parent of the new control
	 * @param label  the group box label
	 * @param nColumns - number of layout columns
	 * @return the new group box
	 */
    public static List createList(Composite parent, String strdata, String selData) {
		List list = new List(parent, SWT.SINGLE);
		GridData data = new GridData();
		list.setLayoutData(data);
		StringTokenizer st = new StringTokenizer(strdata, ","); //$NON-NLS-1$
		while(st.hasMoreTokens())
			list.add(st.nextToken());
	    if(selData == null) {
	    	if(list.getItemCount() > 0)
				list.select(0);
	    }
	    else
			selectList(list, selData);	    
		return list;
	}

	public static void selectList(List list, String selData)	{
		int n_sel = list.indexOf(selData);
		if(0 > n_sel)
			n_sel = 0;
	    list.select(n_sel);
	}
	



	/**
	 *	Create this group's list viewer.
	 */
	public static TableViewer createTableViewer(Composite parent, String[] opt_list, 
	    int width, int height, int style) {
		TableViewer listViewer = new TableViewer(parent, SWT.BORDER | style);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.widthHint = width;
		data.heightHint = height;
		listViewer.getTable().setLayoutData(data);
		if(null != opt_list)
			listViewer.add(opt_list);
        return listViewer; 
	}


	/**
	 *	Create this group's list viewer.
	 */
	public static TableViewer createTableViewer(Composite parent,  
	    int width, int height, int style, String[] columns, int[] colWidths) {
		TableViewer listViewer = createTableViewer(parent, null, width, height, style);

		Table table= listViewer.getTable();
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableLayout tableLayout= new TableLayout();
/*
		TableColumn column= table.getColumn(0);
		column.setText(columns[0]); 	    
		tableLayout.addColumnData(new ColumnWeightData(colWidths[0], false));
*/		
		TableColumn column;
		for(int i = 0; i < columns.length; ++i) {
			column= new TableColumn(table, SWT.NULL);
			column.setText(columns[i]); 	
			tableLayout.addColumnData(new ColumnWeightData(colWidths[i], true));
			
		}

		table.setLayout(tableLayout);
		
        return listViewer; 
	}

	public static void deactivateCellEditor(TableViewer viewer) {
		if(null == viewer)
			return;
		CellEditor[] es = viewer.getCellEditors();
		TableItem[] items = viewer.getTable().getSelection();
		if(items.length >= 0)  {
			for(int i = 0; i < es.length; ++i) {
				CellEditor e = es[i];
				if(e.isActivated()) {
					if(e.isValueValid()) {
						Object[] properties = viewer.getColumnProperties();
						Object value = e.getValue();
						viewer.cancelEditing();
						viewer.getCellModifier().modify(items[0],(String)properties[i], value);
					} else
						viewer.cancelEditing();
					break;
				}
			}
		}
	}

	/**
	 *	Create this group's list viewer.
	 */
	public static CheckboxTableViewer createListViewer(Composite parent, String[] opt_list, 
	    int width, int height, int style) {
	    	
	    Table table = new Table(parent, SWT.BORDER | SWT.CHECK);
	    CheckboxTableViewer listViewer = new CheckboxTableViewer(table);
		GridData data = new GridData(style);
		data.widthHint = width;
		data.heightHint = height;
		listViewer.getTable().setLayoutData(data);
		if(null != opt_list)
			listViewer.add(opt_list);
//		listViewer.setLabelProvider(listLabelProvider);
//		listViewer.addCheckStateListener(this);
        return listViewer; 
	}



	public static CheckboxTableViewer createListViewer(Composite parent,  
	    int width, int height, int style, String[] columns, int[] colWidths) {
	    CheckboxTableViewer listViewer = createListViewer(parent, null, 
	    	width, height, style);
		
		Table table= listViewer.getTable();
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableLayout tableLayout= new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column= table.getColumn(0);
		column.setText(columns[0]); 	    
		tableLayout.addColumnData(new ColumnWeightData(colWidths[0], false));
		
		for(int i = 1; i < columns.length; ++i) {
			column= new TableColumn(table, SWT.NULL);
			column.setText(columns[i]); 	
			tableLayout.addColumnData(new ColumnWeightData(colWidths[i], false));
			
		}
	    
        return listViewer; 
	}

  
	/**
	 * Create a selection combo
	 *
	 * @param parent  the parent of the new text field
	 * @param string of comma separated tokens to fill selection list
	 * @return the new combo
	 */
	public static CCombo createSelectCCombo(Composite parent, String strdata, String selData) {
		return createSelectCCombo(parent, strdata, selData, 
			SWT.READ_ONLY | SWT.BORDER);
	}
		
	public static CCombo createSelectCCombo(Composite parent, String strdata, String selData, int style) {
		CCombo combo = new CCombo(parent, style);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		combo.setLayoutData(data);
		StringTokenizer st = new StringTokenizer(strdata, ","); //$NON-NLS-1$
		while(st.hasMoreTokens())
			combo.add(st.nextToken());
	    if(selData == null || selData.length() == 0) {
	    	if(combo.getItemCount() > 0)
				combo.select(0);
	    }
	    else
			selectCCombo(combo, selData);	    
		return combo;
	}


	/**
	 * Create a selection combo
	 *
	 * @param parent  the parent of the new text field
	 * @param array of elements + selected element
	 * @return the new combo
	 */
	public static CCombo createSelectCCombo(Composite parent, String[] strdata, String selData) {
		return createSelectCCombo(parent, strdata, selData, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
	}
	
	public static CCombo createSelectCCombo(Composite parent, String[] strdata, String selData, int style) {
		CCombo combo = new CCombo(parent, style);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		combo.setLayoutData(data);
		for(int i = 0; i < strdata.length; ++i) {
			combo.add(strdata[i]);
		}
	    if(selData == null)
			combo.select(0);
	    else
			selectCCombo(combo, selData);	    
		return combo;
	}

	public static void selectCCombo(CCombo combo, String selData)	{
		int n_sel = combo.indexOf(selData);
		if(0 > n_sel)
			n_sel = 0;
	    combo.select(n_sel);
	} 










	/**
	 * Create a selection combo
	 *
	 * @param parent  the parent of the new text field
	 * @param string of comma separated tokens to fill selection list
	 * @return the new combo
	 */
	public static Combo createSelectCombo(Composite parent, String strdata, String selData) {
		return createSelectCombo(parent, strdata, selData, 
			SWT.READ_ONLY | SWT.BORDER);
	}
		
	public static Combo createSelectCombo(Composite parent, String strdata, String selData, int style) {
		Combo combo = new Combo(parent, style);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		combo.setLayoutData(data);
		StringTokenizer st = new StringTokenizer(strdata, ","); //$NON-NLS-1$
		while(st.hasMoreTokens())
			combo.add(st.nextToken());
	    if(selData == null || selData.length() == 0) {
	    	if(combo.getItemCount() > 0)
				combo.select(0);
	    }
	    else
			selectCombo(combo, selData);	    
		return combo;
	}


	/**
	 * Create a selection combo
	 *
	 * @param parent  the parent of the new text field
	 * @param array of elements + selected element
	 * @return the new combo
	 */
	public static Combo createSelectCombo(Composite parent, String[] strdata, String selData) {
		return createSelectCombo(parent, strdata, selData, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
	}
	
	public static Combo createSelectCombo(Composite parent, String[] strdata, String selData, int style) {
		Combo combo = new Combo(parent, style);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		combo.setLayoutData(data);
		for(int i = 0; i < strdata.length; ++i) {
			combo.add(strdata[i]);
		}
	    if(selData == null)
			combo.select(0);
	    else
			selectCombo(combo, selData);	    
		return combo;
	}

	public static void selectCombo(Combo combo, String selData)	{
		int n_sel = combo.indexOf(selData);
		if(0 > n_sel) {
			if( ( combo.getStyle() & SWT.READ_ONLY ) == 0 ) {
				combo.setText( selData );
				return;
			}
			n_sel = 0;
		}
	    combo.select(n_sel);
	} 











    /**
	 * Create a dialog shell, child to the top level workbench shell.
	 *
	 * @return The new Shell useable for a dialog.
     *
	 */
    public static Shell createDialogShell() {
        Shell parent = PlatformUI.getWorkbench()
                                 .getActiveWorkbenchWindow()
                                 .getShell();
        return new Shell( parent, SWT.DIALOG_TRIM );
    }

	

	public static Composite insertSpace(Composite parent, int nSpan, int height) {
		Composite space = ControlFactory.createCompositeSeparator(parent, parent.getBackground(),
			 (SWT.DEFAULT != height ? height : 5));
		((GridData)space.getLayoutData()).horizontalSpan = nSpan;
		return space;
	}

    public static MessageBox createDialog( String title, String message, int style ) {
        MessageBox box = new MessageBox( createDialogShell(), style | SWT.APPLICATION_MODAL );
        box.setText( title );
        box.setMessage( message );
        return box;
    }

    public static MessageBox createYesNoDialog( String title, String message ) {
        return createDialog( title, message, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    }

    public static MessageBox createOkDialog( String title, String message ) {
        return createDialog( title, message, SWT.OK | SWT.ICON_INFORMATION );
    }

    public static MessageBox createOkCancelDialog( String title, String message ) {
        return createDialog( title, message, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION );
    }
	
}
