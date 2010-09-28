/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.text.AbstractInformationControl;
import org.eclipse.cdt.internal.ui.viewsupport.EditorOpener;

public class THInformationControl extends AbstractInformationControl implements ITHModelPresenter {

	private THHierarchyModel fModel;
	private THLabelProvider fHierarchyLabelProvider;
	private TreeViewer fHierarchyTreeViewer;
	private boolean fDisposed= false;

	public THInformationControl(Shell parent, int shellStyle, int treeStyle) {
		super(parent, shellStyle, treeStyle, ICEditorActionDefinitionIds.OPEN_QUICK_TYPE_HIERARCHY, true);
	}

	@Override
	protected boolean hasHeader() {
		return true;
	}

	@Override
	protected TreeViewer createTreeViewer(Composite parent, int style) {
		Display display= getShell().getDisplay();
		fModel= new THHierarchyModel(this, display, true);
		fHierarchyLabelProvider= new THLabelProvider(display, fModel);
		fHierarchyLabelProvider.setMarkImplementers(false);
    	fHierarchyTreeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    	fHierarchyTreeViewer.setContentProvider(new THContentProvider());
    	fHierarchyTreeViewer.setLabelProvider(fHierarchyLabelProvider);
    	fHierarchyTreeViewer.setSorter(new ViewerSorter());
    	fHierarchyTreeViewer.setUseHashlookup(true);
    	return fHierarchyTreeViewer;
	}

	protected void onOpenElement(ISelection selection) {
		ICElement elem= (ICElement) getSelectedElement();
		if (elem != null) {
			try {
				EditorOpener.open(CUIPlugin.getActivePage(), elem);
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof ICElement[]) {
			ICElement[] splitInput= (ICElement[]) input;
			if (TypeHierarchyUI.isValidTypeInput(splitInput[0])) {
				fModel.setInput(splitInput[0], splitInput[1]);
				fHierarchyLabelProvider.setHideNonImplementers(splitInput[1] != null);
				fHierarchyTreeViewer.setInput(fModel);
		    	fModel.computeGraph();
		    	String msgfmt= Messages.THInformationControl_regularTitle;
		    	String obj= splitInput[0].getElementName();
		    	if (splitInput[1] != null) {
		    		msgfmt= Messages.THInformationControl_showDefiningTypesTitle;
		    		obj= splitInput[1].getElementName();
		    	}
		    	String title= MessageFormat.format(msgfmt, new Object[] {obj});
		    	setTitleText(title);
			}
        }
	}

	@Override
	protected String getId() {
		return "org.eclipse.cdt.internal.ui.typehierarchy.QuickHierarchy"; //$NON-NLS-1$
	}

	@Override
	protected Object getSelectedElement() {
		THNode node= selectionToNode(fHierarchyTreeViewer.getSelection());
		if (node != null) {
			ICElement elem= node.getElement();
			if (node.isImplementor()) {
				fModel.onHierarchySelectionChanged(node);
				ICElement melem= fModel.getSelectedMember();
				if (melem != null) {
					return melem;
				}
			}
			return elem;
		}
		return null;
	}

	private THNode selectionToNode(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			for (Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
				Object cand= iter.next();
				if (cand instanceof THNode) {
					return (THNode) cand;
				}
			}
		}
		return null;
	}

	@Override
	public void widgetDisposed(DisposeEvent event) {
		fDisposed= true;
		super.widgetDisposed(event);
	}

	public void onEvent(int event) {
		if (!fDisposed) {
			switch (event) {
			case THHierarchyModel.END_OF_COMPUTATION:
				if (fModel.hasTrivialHierarchy()) {
					fHierarchyLabelProvider.setHideNonImplementers(false);
				}
				fHierarchyTreeViewer.refresh();
				THNode selection= fModel.getSelectionInHierarchy();
				if (selection != null) {
					fHierarchyTreeViewer.setSelection(new StructuredSelection(selection));
					fHierarchyTreeViewer.expandToLevel(selection, 2);
				}
				break;
			}		
		}
	}

	public void setMessage(String msg) {
	}

	public IWorkbenchSiteProgressService getProgressService() {
		return null;
	}
	
	@Override
	protected void selectFirstMatch() {
		Tree tree= fHierarchyTreeViewer.getTree();
		Object element= findElement(tree.getItems());
		if (element != null)
			fHierarchyTreeViewer.setSelection(new StructuredSelection(element), true);
		else
			fHierarchyTreeViewer.setSelection(StructuredSelection.EMPTY);
	}

	private THNode findElement(TreeItem[] items) {
		for (TreeItem item2 : items) {
			Object item= item2.getData();
			THNode element= null;
			if (item instanceof THNode) {
				element= (THNode)item;
				if (fStringMatcher == null)
					return element;
	
				String label= fHierarchyLabelProvider.getText(element);
				if (fStringMatcher.match(label))
					return element;
			}
			element= findElement(item2.getItems());
			if (element != null)
				return element;
		}
		return null;
	}
}
