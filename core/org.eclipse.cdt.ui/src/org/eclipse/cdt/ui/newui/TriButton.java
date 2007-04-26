package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TriButton extends Composite {
	private static final String[] ITEMS = {"No", "Yes", "?"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final int NO  = 0;
	public static final int YES = 1;
	public static final int UNKNOWN = 2;
	
	private boolean triMode = false;
	private Button button = null;
	private Combo combo = null;
	private Label label = null;
	
	public TriButton(Composite parent, int style, boolean _triMode) {
		super(parent, style);
		triMode = _triMode;
		if (triMode) {
			setLayout(new GridLayout(2, false));
			combo = new Combo(this, style | SWT.READ_ONLY | SWT.DROP_DOWN);
			combo.setLayoutData(new GridData(GridData.BEGINNING));
			combo.setItems(ITEMS);
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		} else {
			setLayout(new GridLayout());
			button = new Button(this, style | SWT.CHECK);
			button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
	}

	public String getText () {
		return triMode ? label.getText() : button.getText(); 
	}
	public void setText (String string) {
		if (triMode) label.setText(string); 
		else button.setText(string);
	}

	public String getToolTipText () {
		return triMode ? label.getToolTipText() : button.getToolTipText(); 
	}
	public void setToolTipText (String string) {
		if (triMode) {
			label.setToolTipText(string); 
			combo.setToolTipText(string); 
		} else button.setToolTipText(string);
	}
	
	public boolean getSelection () {
		return triMode ? (combo.getSelectionIndex() == YES) : button.getSelection();
	}
	public void setSelection (boolean selected) {
		if (triMode) combo.select(selected ? YES : NO);
		else button.setSelection(selected);
	}
	
	public int getTriSelection () {
		return triMode ? combo.getSelectionIndex(): (button.getSelection() ? YES : NO);
	}
	public void setTriSelection(int selection) {
		if (selection != NO && 
			selection != YES && 
			selection != UNKNOWN) 
			selection = NO;
		if (triMode) combo.select(selection);
		else button.setSelection(selection == 1);
	}
	
	public int getAlignment () {
		return triMode ? label.getAlignment() : button.getAlignment();
	}
	public void setAlignment (int alignment) {
		if (triMode) label.setAlignment(alignment);
		else button.setAlignment(alignment);
	}

	public Image getImage () {
		return triMode ? label.getImage() : button.getImage();
	}
	public void setImage (Image image) {
		if (triMode) label.setImage(image);
		else button.setImage(image);
	}
	
	public void addSelectionListener (SelectionListener listener) {
		if (triMode) combo.addSelectionListener(listener);
		else button.addSelectionListener(listener);
	}
	public void removeSelectionListener (SelectionListener listener) {
		if (triMode) combo.removeSelectionListener(listener);
		else button.removeSelectionListener(listener);
	}
	
}
