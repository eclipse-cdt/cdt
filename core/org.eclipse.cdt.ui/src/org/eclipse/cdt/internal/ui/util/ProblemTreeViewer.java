/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;


/**
 * Extends a  TreeViewer to allow more performance when showing error ticks.
 * A <code>ProblemItemMapper</code> is contained that maps all items in
 * the tree to underlying resource
 */
public class ProblemTreeViewer extends TreeViewer {

	protected ResourceToItemsMapper fResourceToItemsMapper;
	private CEditor editor = null;

	/*
	 * @see TreeViewer#TreeViewer(Composite)
	 */
	public ProblemTreeViewer(Composite parent) {
		super(parent);
		initMapper();
	}

	/*
	 * @see TreeViewer#TreeViewer(Composite, int)
	 */
	public ProblemTreeViewer(Composite parent, int style) {
		super(parent, style);
		initMapper();
	}

	/*
	 * @see TreeViewer#TreeViewer(Tree)
	 */
	public ProblemTreeViewer(Tree tree) {
		super(tree);
		initMapper();
	}
	
	private void initMapper() {
		fResourceToItemsMapper= new ResourceToItemsMapper(this);
	}
	
	
	protected void doUpdateItem(Item item) {
		doUpdateItem(item, item.getData(), true);
	}
	/*
	 * @see StructuredViewer#mapElement(Object, Widget)
	 */
	protected void mapElement(Object element, Widget item) {
		super.mapElement(element, item);
		if (item instanceof Item) {
			fResourceToItemsMapper.addToMap(element, (Item) item);
		}
	}
	
	/*
	 * @see StructuredViewer#unmapElement(Object, Widget)
	 */
	protected void unmapElement(Object element, Widget item) {
		if (item instanceof Item) {
			fResourceToItemsMapper.removeFromMap(element, (Item) item);
		}		
		super.unmapElement(element);
	}

	/*
	 * @see StructuredViewer#unmapAllElements()
	 */
	protected void unmapAllElements() {
		fResourceToItemsMapper.clearMap();
		super.unmapAllElements();
	}

	/*
	 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
	 */
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
		Object source= event.getElement();
		if (source == null) {
			super.handleLabelProviderChanged(event);
			return;
		}
		Object[] changed= event.getElements();
		if (changed != null && !fResourceToItemsMapper.isEmpty()) {
			ArrayList others= new ArrayList(changed.length);
			for (int i= 0; i < changed.length; i++) {
				Object curr= changed[i];
				if (curr instanceof IResource) {
					fResourceToItemsMapper.resourceChanged((IResource) curr);
				} else {
					others.add(curr);
				}
			}
			if (others.isEmpty()) {
				return;
			}
			event= new LabelProviderChangedEvent((IBaseLabelProvider) event.getSource(), others.toArray());
		}		
		super.handleLabelProviderChanged(event);
		return;
	}
	
//    /**
//     * @see org.eclipse.jface.viewers.StructuredViewer#update(java.lang.Object, java.lang.String[])
//     */
//    public void update(Object element, String[] properties)
//    {
        /* Calling StructuredViewer.update() causes
         * RunnableLock deadlock with StructuredViewer.doInternalUpdate()
         * when long h file (with lots of declarations) is edited.
         * This is only workaround, it only protects against
         * deadlock but may cause other problems. */
//    }
// Yeah, and the problem tree no longer updates after a schecdule decoration job!!!!
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#isExpandable(java.lang.Object)
	 */
	public boolean isExpandable(Object element) {
		ITreeContentProvider cp = (ITreeContentProvider) getContentProvider();
		if (cp == null)
			return false;
		// since AbstractTreeViewer will run filteres here on element children this will cause binary search threads and TU parsering 
		// to be started for each project/file tested for expandablity, this can be expensive if lots of project exists in workspace 
		// or lots of TUs exist in one folder so lets skip it....
		return cp.hasChildren(element);
	}
	
    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        super.addSelectionChangedListener(listener);
        if (listener instanceof CContentOutlinePage) {
        	editor =((CContentOutlinePage)listener).getEditor(); 
        }
    }
    
    /**
     * This returns the editor corresponding to the opened CEditor that is listening to the selection changes on the Outline View.
     * @return
     */
    public CEditor getEditor() {
    	return editor;
    }
}

