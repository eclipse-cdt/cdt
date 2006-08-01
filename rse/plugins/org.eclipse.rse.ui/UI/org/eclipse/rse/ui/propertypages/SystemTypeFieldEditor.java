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


package org.eclipse.rse.ui.propertypages;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


/**
 * This is a field type editor for the Remote Systems preference page,
 *   used for setting system type preferences.
 */
public class SystemTypeFieldEditor extends FieldEditor 
	implements ICellModifier, ITableLabelProvider, IStructuredContentProvider				
{
	private Table table;
	private GridData tableData;
	private TableViewer tableViewer;
	private CellEditor enabledCellEditor, userIdCellEditor;
    private static final char KEYVALUE_DELIMITER='=';
    private static final char KEYVALUEPAIR_DELIMITER=';';
    public static final char EACHVALUE_DELIMITER='+';
    private Hashtable keyValues;
    private IRSESystemType[] systemTypes;
    
    private boolean enabledStateChanged = false;
    
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_ENABLED = 1;
	private static final int COLUMN_USERID = 2;
	private static final String P_NAME = "name";
	private static final String P_ENABLED = "enabled";
	private static final String P_DESC = "desc";
	private static final String P_USERID = "userid";
	private static final String columnHeaders[] = 
	{
		   SystemResources.RESID_PREF_SYSTYPE_COLHDG_NAME,
		   SystemResources.RESID_PREF_SYSTYPE_COLHDG_ENABLED,
		   SystemResources.RESID_PREF_SYSTYPE_COLHDG_USERID,
		   SystemResources.RESID_PREF_SYSTYPE_COLHDG_DESC		   
	};
	private static ColumnLayoutData columnLayouts[] = 
	{		
		new ColumnWeightData(20,80,true),
		new ColumnWeightData(20,15,true),
		new ColumnWeightData(20,100,true),
		new ColumnWeightData(55,280,true)		
	};
	// give each column a property value to identify it
	private static final String[] tableColumnProperties = 
	{
		P_NAME, P_ENABLED, P_USERID, P_DESC
	};
	
	private static final boolean[] enabledStates = {Boolean.TRUE.booleanValue(), Boolean.FALSE.booleanValue()};
    private static final String[] enabledStateStrings = {Boolean.TRUE.toString(), Boolean.FALSE.toString()};

	/**
	 * Constructor
	 * @param name
	 * @param labelText
	 * @param parent
	 */
	public SystemTypeFieldEditor(String name, String labelText, Composite parent) 
	{
		super(name, labelText, parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) 
	{
		((GridData)table.getLayoutData()).horizontalSpan = numColumns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) 
	{
        table = createTable(parent);       
        ((GridData)table.getLayoutData()).horizontalSpan = numColumns;
        tableViewer = new TableViewer(table);
        createColumns();
	    tableViewer.setColumnProperties(tableColumnProperties);        
	    tableViewer.setCellModifier(this);
	    CellEditor editors[] = new CellEditor[columnHeaders.length];
	    userIdCellEditor = new TextCellEditor(table);
	    enabledCellEditor = new ComboBoxCellEditor(table, enabledStateStrings, SWT.READ_ONLY); // DWD should consider a checkbox for this.
	    editors[COLUMN_USERID] = userIdCellEditor;
	    editors[COLUMN_ENABLED] = enabledCellEditor;
	    tableViewer.setCellEditors(editors);

        tableViewer.setLabelProvider(this);
        tableViewer.setContentProvider(this);
        tableViewer.setInput(new Object());        
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() 
	{
    	if (systemTypes == null)
    		systemTypes = getSystemTypes(false);
    	
		String value = getPreferenceStore().getString(ISystemPreferencesConstants.SYSTEMTYPE_VALUES);
		keyValues = null;
		
	    if ((value == null) || (value.length() == 0))
	    {
    		keyValues = new Hashtable();
	    }
	    else
	    {
	    	keyValues = parseString(value);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() 
	{
		// when Defaults button pressed, we re-read the system types from disk
		systemTypes = getSystemTypes(true);
		keyValues.clear();
		tableViewer.refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() 
	{
		if (systemTypes != null)
		{
			String s = createString(keyValues);

			if (s != null)
				getPreferenceStore().setValue(getPreferenceName(), s);	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() 
	{
		return 1;
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	public void setEnabled(boolean enabled, Composite parent)
	{
		if (table != null)
			table.setEnabled(enabled);
	}

	/*
	 * @see FieldEditor.isValid().
	 */	
	public boolean isValid() 
	{
		return true;
	}
	
	
	// ----------------
	// local methods...
	// ----------------

	private Table createTable(Composite parent) 
    {
	   table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);	   
  	   table.setLinesVisible(true);
	   tableData = new GridData();
	   tableData.horizontalAlignment = GridData.FILL;
	   tableData.grabExcessHorizontalSpace = true;
	   tableData.widthHint = 410;        
	   tableData.heightHint= 30;
	   tableData.verticalAlignment = GridData.FILL;
	   tableData.grabExcessVerticalSpace = true;
	   table.setLayoutData(tableData);
	   
	   SystemWidgetHelpers.setHelp(table, RSEUIPlugin.HELPPREFIX+"systype_preferences");
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
	    }    	
    }

	/**
	 * Parse out list of key-value pairs into a hashtable
	 */
	private static Hashtable parseString(String allvalues)
	{
		StringTokenizer tokens = new StringTokenizer(allvalues, makeString(KEYVALUE_DELIMITER, KEYVALUEPAIR_DELIMITER));
		Hashtable keyValues = new Hashtable(10);
		int count = 0;
		String token1=null;
		String token2=null;
		while (tokens.hasMoreTokens())
		{
			count++;
			if ((count % 2) == 0) // even number
			{
			  token2 = tokens.nextToken();
			  keyValues.put(token1, token2);
			}
			else
			  token1 = tokens.nextToken();
		}
		return keyValues;
	}
	
	private static String makeString(char charOne, char charTwo)
	{
		StringBuffer s = new StringBuffer(2);
		s.append(charOne);
		s.append(charTwo);
		return s.toString();
	}
	
	/**
	 * Convert hashtable of key-value pairs into a single string
	 */
	public static String createString(Hashtable keyValues)
	{
		if (keyValues == null)
			return null;
		Enumeration keys = keyValues.keys();
		StringBuffer sb = new StringBuffer();
		while (keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			String value = (String)keyValues.get(key);
			if ((value != null) && (value.length()>0))
			{
				sb.append(key);
				sb.append(KEYVALUE_DELIMITER);
				sb.append(value);
				sb.append(KEYVALUEPAIR_DELIMITER);
			}
		}

		return sb.toString();
	}
	
	private IRSESystemType[] getSystemTypes(boolean defaults)
	{
		IRSESystemType[] types = RSECorePlugin.getDefault().getRegistry().getSystemTypes();
		
		ArrayList list = new ArrayList();

		if (systemTypes == null) {
			
			for (int i = 0; i < types.length; i++) {
				
				ISubSystemConfiguration[] configurations = RSEUIPlugin.getTheSystemRegistry().getSubSystemConfigurationsBySystemType(types[i].getName());
				
				if (configurations != null && configurations.length > 0) {
					list.add(types[i]);
				}
			}
		}
		
		types = new IRSESystemType[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			types[i] = (IRSESystemType)(list.get(i));
		}
		
		return types;
	}

    // ------------------------
    // ICellModifier methods...
    // ------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property)
	{	
		if (property.equals(P_ENABLED))
		{
			return true;
		}
		else if (property.equals(P_USERID))
		{
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property)
	{
		IRSESystemType row = (IRSESystemType)element;
		RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(row.getAdapter(IRSESystemType.class));
		Object value = "";
		
		if (property.equals(P_NAME))
			value = row.getName();
		else if (property.equals(P_ENABLED))
			value = (adapter.isEnabled(row) ? new Integer(0) : new Integer(1));
		else if (property.equals(P_USERID))
			value = (adapter.getDefaultUserId(row) == null) ? "" : adapter.getDefaultUserId(row);
		else
			value = (row.getDescription() == null) ? "" : row.getDescription();

		return value;
	}
	
	public boolean enabledStateChanged()
	{
		return enabledStateChanged;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value)
	{
		IRSESystemType row = (IRSESystemType)(((TableItem)element).getData());			
		RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(row.getAdapter(IRSESystemType.class));

		if (property.equals(P_ENABLED))
		{
		    Integer val = (Integer)value;
			adapter.setIsEnabled(row, enabledStates[val.intValue()]);
			enabledStateChanged = true;
		}
		else if (property.equals(P_USERID))
		{
			adapter.setDefaultUserId(row, (String)value);			
		}
		else
			return;
		
		keyValues.put(row.getName(), "");		
		tableViewer.update(row, null);
	}

    // ------------------------------
    // ITableLabelProvider methods...
    // ------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex)
	{
		IRSESystemType currType = (IRSESystemType)element;
		RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(currType.getAdapter(IRSESystemType.class));

		if (columnIndex == COLUMN_NAME)
			return currType.getName();
		else if (columnIndex == COLUMN_ENABLED)
			return Boolean.toString(adapter.isEnabled(currType));
		else if (columnIndex == COLUMN_USERID)
			return (adapter.getDefaultUserId(currType)==null ? "" : adapter.getDefaultUserId(currType));
		else 
			return (currType.getDescription()==null ? "" : currType.getDescription());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener)
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property)
	{	
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener)
	{
	}

    // -------------------------------------
    // IStructuredContentProvider methods...
    // -------------------------------------
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement)
	{
		if (systemTypes == null)
			systemTypes = getSystemTypes(false);
		return systemTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}
	
	// ----------------
	// Other methods...
	// ----------------
    /**
     * Set the tooltip text
     */
    public void setToolTipText(String tip)
    {
    	table.setToolTipText(tip);
    }

    public static Hashtable initSystemTypePreferences(IPreferenceStore store, IRSESystemType[] systemTypes)
    {
		String value = store.getString(ISystemPreferencesConstants.SYSTEMTYPE_VALUES);
		Hashtable keyValues = null;
	    if ((value == null) || (value.length()==0)) // not initialized yet?
	    {
	    	return null;
	    	// nothing to do, as we have read from systemTypes extension points already
	    }
	    else
	    {
	    	keyValues = parseString(value);
	    	// we have now a hashtable, where the keys are the system type names,
	    	//  and the values are the column-value attributes for that type, separated
	    	//  by a '+' character: enabled+userid. eg: "true+bob"
			Enumeration keys = keyValues.keys();
			while (keys.hasMoreElements())
			{
				String key = (String)keys.nextElement();
				String attributes = (String)keyValues.get(key);
				String attr1="true", attr2="";
				if ((attributes != null) && (attributes.length()>0))
				{
					StringTokenizer tokens = new StringTokenizer(attributes, Character.toString(EACHVALUE_DELIMITER));
					if (tokens.hasMoreTokens())
					{
						attr1 = tokens.nextToken();
						if (tokens.hasMoreTokens())
						{
							attr2 = tokens.nextToken();
						}
						else
						{
							attr2 = "null";
						}
					}
				}
				// find this system type in the array...
				IRSESystemType matchingType = RSECorePlugin.getDefault().getRegistry().getSystemType(key);
				RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(matchingType.getAdapter(IRSESystemType.class));
				
				// update this system type's attributes as per preferences...
				if (matchingType!=null)
				{
					adapter.setIsEnabled(matchingType, attr1.equals("true"));
					if (!attr2.equals("null"))
						adapter.setDefaultUserId(matchingType, attr2);
				}
			}	    		    	
	    }    
	    return keyValues;
    }
}