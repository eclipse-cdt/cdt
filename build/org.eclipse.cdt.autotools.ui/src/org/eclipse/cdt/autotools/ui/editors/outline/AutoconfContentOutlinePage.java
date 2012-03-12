/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.outline;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;


public class AutoconfContentOutlinePage extends ContentOutlinePage {

	private ITextEditor editor;
	private IEditorInput input;
	
	public AutoconfContentOutlinePage(AutoconfEditor editor) {
		super();
		this.editor = editor;
	}

	public void setInput(IEditorInput editorInput) {
		this.input = editorInput;
		update();
	}

	protected ISelection updateSelection(ISelection sel) {
		ArrayList<AutoconfElement> newSelection= new ArrayList<AutoconfElement>();
		if (sel instanceof IStructuredSelection) {
			@SuppressWarnings("unchecked")
			Iterator iter= ((IStructuredSelection)sel).iterator();
			for (;iter.hasNext();) {
				//ICElement elem= fInput.findEqualMember((ICElement)iter.next());
				Object o = iter.next();
				if (o instanceof AutoconfElement) {
					newSelection.add((AutoconfElement)o);
				}
			}
		}
		return new StructuredSelection(newSelection);
	}

	public void update() {
		//set the input so that the outlines parse can be called
		//update the tree viewer state
		final TreeViewer viewer = getTreeViewer();

		if (viewer != null)
		{
			final Control control = viewer.getControl();
			if (control != null && !control.isDisposed())
			{
				control.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!control.isDisposed()) {
//							control.setRedraw(false);
//							if (input != null)
//								viewer.setInput(input);
//							viewer.expandAll();
//							control.setRedraw(true);
							ISelection sel= viewer.getSelection();
							viewer.setSelection(updateSelection(sel));		
							viewer.refresh();
						}
					}
				});
			}
		}
	}
	
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer= getTreeViewer();
		viewer.setContentProvider(new AutoconfContentProvider(editor));
		viewer.setLabelProvider(new AutoconfLabelProvider());
		viewer.addSelectionChangedListener(this);

		if (input != null)
			viewer.setInput(input);
	}
	
	/*
	 * Change in selection
	 */
	public void selectionChanged(SelectionChangedEvent event)
	{
		super.selectionChanged(event);
		
		//find out which item in tree viewer we have selected, and set highlight range accordingly
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			editor.resetHighlightRange();
		} else {
			AutoconfElement element = (AutoconfElement) ((IStructuredSelection) selection)
					.getFirstElement();		
			
			try {
				int offset = element.getStartOffset();
				int length = element.getEndOffset() - offset;
				editor.setHighlightRange(offset, length, true);
				editor.selectAndReveal(offset, length);
			} catch (IllegalArgumentException x) {
				editor.resetHighlightRange();
			}
		}
	}

}
