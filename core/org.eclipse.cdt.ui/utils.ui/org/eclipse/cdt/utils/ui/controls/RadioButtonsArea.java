package org.eclipse.cdt.utils.ui.controls;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.util.Assert;

/**
 * A field editor for an enumeration type preference.
 * The choices are presented as a list of radio buttons.
 */
public class RadioButtonsArea extends Composite {

	/**
	 * List of radio button entries of the form [label,value].
	 */
	private String[][] labelsAndValues;

	/**
	 * Number of columns into which to arrange the radio buttons.
	 */
	private int numColumns;

	/**
	 * Indent used for the first column of the radion button matrix.
	 */
	private int indent = 0;

	/**
	 * The current value, or <code>null</code> if none.
	 */
	protected String value = null;
	
	private SelectionListener listener;
	
	private ArrayList externalListeners = new ArrayList();

	private Composite area = null; 
	/**
	 * The radio buttons, or <code>null</code> if none
	 * (before creation and after disposal).
	 */
	protected Button[] radioButtons;


	public RadioButtonsArea(Composite parent, String labelText, int numColumns, String[][] labelAndValues) {
		super(parent, SWT.NULL);
		Assert.isTrue(checkArray(labelAndValues));
		this.labelsAndValues = labelAndValues;
		this.numColumns = numColumns;
		createControl(parent, labelText);
	}
	
	public void setEnabled(boolean enabled) {
		for( int i = 0; i < radioButtons.length; i++ ) {
			radioButtons[i].setEnabled(enabled);
		}
	}
	
	/**
	 * Checks whether given <code>String[][]</code> is of "type" 
	 * <code>String[][2]</code>.
	 *
	 * @return <code>true</code> if it is ok, and <code>false</code> otherwise
	 */
	private boolean checkArray(String[][] table) {
		if (table == null)
			return false;
		for (int i = 0; i < table.length; i++) {
			String[] array = table[i];
			if (array == null || array.length != 2)
				return false;
		}
		return true;
	}

	protected void fireSelectionEvent(SelectionEvent event) {
		for(Iterator i = externalListeners.iterator(); i.hasNext(); ) {
			SelectionListener s = (SelectionListener)i.next();
			s.widgetSelected(event);
		}
	}
	
	/**
	 * Create control area
	 *
	 * @return nothing
	 */
	protected void createControl(Composite parent, String labelText) {
		GridLayout gl = new GridLayout();
		this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl.marginWidth = 0;
		gl.horizontalSpacing = 0;
		this.setLayout(gl);
	
		if(null != labelText) { // Create group box
			area = ControlFactory.createGroup(this, labelText, numColumns);
		} else {
			area = this;
		}

		radioButtons = new Button[labelsAndValues.length];
		listener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				value = (String) (event.widget.getData());
				fireSelectionEvent(event); // Infor any external listener
			}
		};
		
		for (int i = 0; i < labelsAndValues.length; i++) {
			radioButtons[i] = ControlFactory.createRadioButton(area, 
							  labelsAndValues[i][0],
							  labelsAndValues[i][1],
			  			      listener);
		}
		
		area.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				radioButtons = null;
			}
		});

	}
	
	/**
	 * Sets the indent used for the first column of the radion button matrix.
	 *
	 * @param indent the indent (in pixels)
	 */
	public void setIndent(int indent) {
		if(null == area)
			return;
		if (indent < 0)
			indent = 0;
		for(int i = 0; i < radioButtons.length; ++i) {
			((GridData)(radioButtons[i].getLayoutData())).horizontalIndent = indent;
		}
	}

	/**
	 * Select the radio button that conforms to the given value.
	 *
	 * @param selectedValue the selected value
	 */
	public void setSelectValue(String selectedValue) {
		this.value = selectedValue;
		if (radioButtons == null)
			return;
	
		if (this.value != null) {
			boolean found = false;
			for (int i = 0; i < radioButtons.length; i++) {
				Button radio = radioButtons[i];
				boolean selection = false;
				if (((String) radio.getData()).equals(this.value)) {
					selection = true;
					found = true;
				}
				radio.setSelection(selection);
			}
			if (found)
				return;
		}
	
		// We weren't able to find the value. So we select the first
		// radio button as a default.
		if (radioButtons.length > 0) {
			radioButtons[0].setSelection(true);
			this.value = (String) radioButtons[0].getData();
		}
		return;
	}

    public void setSelectedButton(int index) {
    	Button b;
    	
    	if((index < 0) || (index >= radioButtons.length))
    		return;
    		
		for(int i = 0; i < radioButtons.length; ++i) {
			b = radioButtons[i];
			boolean selected = b.getSelection();
			if(i == index) {
			   if(selected)
			   	return;
			} else {
				if(selected)
					b.setSelection(false);
			}
		}
		
		b = radioButtons[index];
		b.setSelection(true);
    }
	
	public String getSelectedValue() {
		return value;
	}
	
	public int getSeletedIndex() {
		if (radioButtons == null)
			return -1;
	
		if (value != null) {
			for (int i = 0; i < radioButtons.length; i++) {
				if (((String) radioButtons[i].getData()).equals(this.value)) 
					return i;
			}
		}
		
		return -1;
	}
	
	public void addSelectionListener(SelectionListener s) {
		if(externalListeners.contains(s))
			return;
		externalListeners.add(s);
	}
}
