package org.eclipse.cdt.internal.ui.util;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Set;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;


/**
 * Extends a  TreeViewer to allow more performance when showing error ticks.
 * A <code>ProblemItemMapper</code> is contained that maps all items in
 * the tree to underlying resource
 */
public class ProblemTreeViewer extends TreeViewer implements IProblemChangedListener {

	protected ProblemItemMapper fProblemItemMapper;

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
		fProblemItemMapper= new ProblemItemMapper();
	}
	
	
	/*
	 * @see IProblemChangedListener#problemsChanged
	 */
	public void problemsChanged(final Set changed) {
		Control control= getControl();
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				public void run() {
					fProblemItemMapper.problemsChanged(changed, (ILabelProvider)getLabelProvider());
				}
			});
		}
	}
	
	/*
	 * @see StructuredViewer#mapElement(Object, Widget)
	 */
	protected void mapElement(Object element, Widget item) {
		super.mapElement(element, item);
		if (item instanceof Item) {
			fProblemItemMapper.addToMap(element, (Item) item);
		}
	}

	/*
	 * @see StructuredViewer#unmapElement(Object, Widget)
	 */
	protected void unmapElement(Object element, Widget item) {
		if (item instanceof Item) {
			fProblemItemMapper.removeFromMap(element, (Item) item);
		}		
		super.unmapElement(element);
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
		
		/* 
		// map the event to the Java elements if possible
		// this does not handle the ambiguity of default packages
		Object[] mapped= new Object[source.length];
		for (int i= 0; i < source.length; i++) {
			Object o= source[i];
			// needs to handle the case of:
			// default package
			// package fragment root on project
			if (o instanceof IResource) {
				IResource r= (IResource)o;
				IJavaElement element= JavaCore.create(r);
				if (element != null) 
					mapped[i]= element;
				else
					mapped[i]= o;
			} else {
				mapped[i]= o;
			}
		}
		super.handleLabelProviderChanged(new LabelProviderChangedEvent((IBaseLabelProvider)event.getSource(), mapped));	*/
		super.handleLabelProviderChanged(event);
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#handleInvalidSelection(org.eclipse.jface.viewers.ISelection, org.eclipse.jface.viewers.ISelection)
	 */
	protected void handleInvalidSelection(ISelection invalidSelection, ISelection newSelection) {
		super.handleInvalidSelection(invalidSelection, newSelection);
	}

	
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
}

