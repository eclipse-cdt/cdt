/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Patrick Hofer [bug 325488]
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.Iterator;

import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
	private KeyAdapter fKeyAdapter;
	
	public THInformationControl(Shell parent, int shellStyle, int treeStyle) {
		super(parent, shellStyle, treeStyle, ICEditorActionDefinitionIds.OPEN_QUICK_TYPE_HIERARCHY, true);
	}

	private KeyAdapter getKeyAdapter() {
		if (fKeyAdapter == null) {
			fKeyAdapter= new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
					KeyStroke keyStroke = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
					Trigger[] triggers = getInvokingCommandTriggerSequence().getTriggers();
					if (triggers == null)
						return;

					for (Trigger trigger : triggers) {
						if (trigger.equals(keyStroke)) {
							e.doit= false;
							toggleHierarchy();
							return;
						}
					}
				}
			};
		}
		return fKeyAdapter;
	}

	@Override
	protected boolean hasHeader() {
		return true;
	}

	@Override
	protected Text createFilterText(Composite parent) {
		Text text= super.createFilterText(parent);
		text.addKeyListener(getKeyAdapter());
		return text;
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
		fHierarchyTreeViewer.getTree().addKeyListener(getKeyAdapter());
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
				updateTitle();
			}
		}
	}

	protected void updateTitle() {
		setTitleText(computeTitleText());
	}
	
	private String computeTitleText() {
		final ICElement input = fModel.getInput();
		final ICElement member= fModel.getSelectedMember();
		String elemName= member != null ? member.getElementName() : input.getElementName();
		return NLS.bind(getFormatString(fModel.getHierarchyKind(), member != null), elemName);
	}
	
	private String getFormatString(int hierarchyKind, boolean forMember) {
		switch (hierarchyKind) {
		case THHierarchyModel.SUB_TYPE_HIERARCHY:
			return forMember
					? Messages.THInformationControl_titleMemberInSubHierarchy
					: Messages.THInformationControl_titleSubHierarchy;
			
		case THHierarchyModel.SUPER_TYPE_HIERARCHY:
			return forMember 
					? Messages.THInformationControl_titleMemberInSuperHierarchy
					: Messages.THInformationControl_titleSuperHierarchy;
			
		case THHierarchyModel.TYPE_HIERARCHY:
		default:
			return forMember 
					? Messages.THInformationControl_titleMemberInHierarchy
					: Messages.THInformationControl_titleHierarchy;
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

	@Override
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

	@Override
	public void setMessage(String msg) {
	}

	@Override
	public IWorkbenchSiteProgressService getProgressService() {
		return null;
	}

	@Override
	protected String getStatusFieldText() {
		TriggerSequence sequence = getInvokingCommandTriggerSequence();
		String keyName= ""; //$NON-NLS-1$
		if (sequence != null)
			keyName= sequence.format();

		String message= ""; //$NON-NLS-1$
		switch (fModel.getHierarchyKind()) {
		case THHierarchyModel.TYPE_HIERARCHY:
			message = Messages.THInformationControl_toggle_superTypeHierarchy_label;
			break;

		case THHierarchyModel.SUB_TYPE_HIERARCHY:
			message = Messages.THInformationControl_toggle_typeHierarchy_label;
			break;

		case THHierarchyModel.SUPER_TYPE_HIERARCHY:
			message = Messages.THInformationControl_toggle_subTypeHierarchy_label;
			break;

		default:
			break;
		}
		return MessageFormat.format(message, new Object[] {keyName} );
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

	protected void toggleHierarchy() {
		fHierarchyTreeViewer.getTree().setRedraw(false);
		switch (fModel.getHierarchyKind()) {
		case THHierarchyModel.TYPE_HIERARCHY:
			fModel.setHierarchyKind(THHierarchyModel.SUPER_TYPE_HIERARCHY);
			break;

		case THHierarchyModel.SUB_TYPE_HIERARCHY:
			fModel.setHierarchyKind(THHierarchyModel.TYPE_HIERARCHY);
			break;

		case THHierarchyModel.SUPER_TYPE_HIERARCHY:
			fModel.setHierarchyKind(THHierarchyModel.SUB_TYPE_HIERARCHY);
			break;

		default:
			break;
		}
		fHierarchyTreeViewer.refresh();
		fHierarchyTreeViewer.expandAll();
		fHierarchyTreeViewer.getTree().setRedraw(true);
		updateStatusFieldText();
		updateTitle();
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
