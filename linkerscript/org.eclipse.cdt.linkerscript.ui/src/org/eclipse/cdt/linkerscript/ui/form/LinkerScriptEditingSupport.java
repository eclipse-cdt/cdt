package org.eclipse.cdt.linkerscript.ui.form;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class LinkerScriptEditingSupport extends EditingSupport implements ITextEditingSupport {
	protected TextCellEditor editor;
	protected BiConsumer<String, String> setOp;
	protected Function<Memory, String> getOp;

	LinkerScriptEditingSupport(ColumnViewer viewer, ICellEditorValidator validator, Function<Memory, String> getOp,
			BiConsumer<String, String> setOp) {
		super(viewer);
		this.getOp = getOp;
		this.setOp = setOp;
		this.editor = new TextCellEditor((Composite) viewer.getControl());
		this.editor.setValidator(validator);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		ColumnViewer columnViewer = getViewer();
		Object input = columnViewer.getInput();
		if (input instanceof ILinkerScriptModel) {
			ILinkerScriptModel model = (ILinkerScriptModel) input;
			return model.readModel(element, Memory.class, "", getOp);
		}
		return "";
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof String && value instanceof String) {
			String uri = (String) element;
			String newValue = (String) value;
			setOp.accept(uri, newValue);
		}
		getViewer().update(element, null);
	}

	@Override
	public TextCellEditor getTextCellEditor() {
		return editor;
	}
}