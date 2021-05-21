package org.eclipse.tm.terminal.view.ui.preferences;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.4
 *
 */
public class BooleanCellEditor extends CellEditor {
	private Button button;
	private ViewerRow row;
	private int index;
	private String restoredText;
	private Image restoredImage;

	private boolean changeOnActivation;

	/**
	 * @param parent
	 */
	public BooleanCellEditor(Composite parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public BooleanCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public LayoutData getLayoutData() {
		LayoutData data = super.getLayoutData();
		data.horizontalAlignment = SWT.CENTER;
		data.grabHorizontal = false;
		return data;
	}

	@Override
	protected Control createControl(Composite parent) {
		Font font = parent.getFont();
		Color bg = parent.getBackground();

		button = new Button(parent, getStyle() | SWT.CHECK);
		button.setFont(font);
		button.setBackground(bg);

		button.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.ESC) {
					fireCancelEditor();
				}
			}

		});

		return button;
	}

	@Override
	protected Object doGetValue() {
		return Boolean.valueOf(button.getSelection());
	}

	@Override
	protected void doSetValue(Object value) {
		boolean selection = Boolean.TRUE.equals(value);
		button.setSelection(selection);
	}

	@Override
	protected void doSetFocus() {
		if (button != null) {
			button.setFocus();
		}
	}

	@Override
	protected void deactivate(ColumnViewerEditorDeactivationEvent event) {
		super.deactivate(event);
		if (event.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED) {
			row.setImage(index, restoredImage);
			row.setText(index, restoredText);
		}

		//TODO Add a way to enable key traversal when CheckBoxes don't get focus
		//		if( Util.isMac() ) {
		//			button.getParent().removeKeyListener(macSelectionListener);
		//		}

		row = null;
		restoredImage = null;
		restoredText = null;
	}

	@Override
	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		ViewerCell cell = (ViewerCell) activationEvent.getSource();
		index = cell.getColumnIndex();
		row = (ViewerRow) cell.getViewerRow().clone();
		restoredImage = row.getImage(index);
		restoredText = row.getText(index);
		row.setImage(index, null);
		row.setText(index, ""); //$NON-NLS-1$

		if (activationEvent.eventType != ColumnViewerEditorActivationEvent.TRAVERSAL && changeOnActivation) {
			button.setSelection(!button.getSelection());
		}

		//TODO Add a way to enable key traversal when CheckBoxes don't get focus
		//		if( Util.isMac() ) {
		//			button.getParent().addKeyListener(macSelectionListener);
		//		}

		super.activate(activationEvent);
	}

	@Override
	protected int getDoubleClickTimeout() {
		return 0;
	}

	public void setChangeOnActivation(boolean changeOnActivation) {
		this.changeOnActivation = changeOnActivation;
	}
}