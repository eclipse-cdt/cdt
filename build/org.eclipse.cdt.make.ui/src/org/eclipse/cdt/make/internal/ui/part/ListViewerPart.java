package org.eclipse.cdt.make.internal.ui.part;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ListViewerPart extends StructuredViewerPart {

	public ListViewerPart(String[] buttonLabels) {
		super(buttonLabels);
	}

	protected StructuredViewer createStructuredViewer(Composite parent, int style) {
		TableViewer tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				ListViewerPart.this.selectionChanged((IStructuredSelection) e.getSelection());
			}
		});
		return tableViewer;
	}

	protected void buttonSelected(Button button, int index) {
	}
	
	protected void selectionChanged(IStructuredSelection selection) {
	}


}
