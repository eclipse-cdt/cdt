/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinerProvider;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCLabelProvider;

/**
 * Control which shows outline information in C/C++ editor. Based on
 * AbstracInformationContol/JavaOutlineInformationControl from JDT.
 * 
 * @author P.Tomaszewski
 */
public class COutlineInformationControl extends AbstractInformationControl {

	private static final int TEXT_FLAGS = AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | CElementBaseLabels.F_APP_TYPE_SIGNATURE | CElementBaseLabels.M_APP_RETURNTYPE;
	private static final int IMAGE_FLAGS = AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS;

	private ICElement fInput = null;
	
    private IContentProvider fOutlineContentProvider;

    /** The action to toggle sorting */
	private LexicalSortingAction fSortingAction;

    /**
     * Creates new outline control.
     * 
     * @param parent
     *            Shell parent.
     * @param shellStyle
     *            Style of new shell.
     * @param treeStyle
     *            Style of the tree viewer.
     */
    public COutlineInformationControl(Shell parent, int shellStyle, int treeStyle) {
        super(parent, shellStyle, treeStyle, null, false);
    }

	/**
	 * {@inheritDoc}
	 */
    protected TreeViewer createTreeViewer(Composite parent, int treeStyle) {
        TreeViewer treeViewer = new ProblemTreeViewer(parent, treeStyle);
        final Tree tree = treeViewer.getTree();
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        fOutlineContentProvider = new CContentOutlinerProvider(treeViewer);
        treeViewer.setContentProvider(fOutlineContentProvider);
        fSortingAction= new LexicalSortingAction(treeViewer);
		treeViewer.addFilter(new NamePatternFilter());
        treeViewer.setLabelProvider(new DecoratingCLabelProvider(
                new AppearanceAwareLabelProvider(TEXT_FLAGS, IMAGE_FLAGS), true));
        treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        return treeViewer;
    }

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractInformationControl#getId()
	 */
	protected String getId() {
		return "org.eclipse.cdt.internal.ui.text.QuickOutline"; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(Object information) {
		if (information == null || information instanceof String) {
			inputChanged(null, null);
			return;
		}
		ICElement ce = (ICElement)information;
		ITranslationUnit tu = (ITranslationUnit)ce.getAncestor(ICElement.C_UNIT);
		if (tu != null)
			fInput = tu;

		inputChanged(fInput, information);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.AbstractInformationControl#fillViewMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillViewMenu(IMenuManager viewMenu) {
		super.fillViewMenu(viewMenu);

		viewMenu.add(new Separator("Sorters")); //$NON-NLS-1$
		viewMenu.add(fSortingAction);

	}

	private class LexicalCSorter extends ViewerComparator {		
		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}
		
		public int category(Object obj) {
			if (obj instanceof ICElement) {
				ICElement elem= (ICElement)obj;
				switch (elem.getElementType()) {
					case ICElement.C_MACRO: return 1;
					case ICElement.C_INCLUDE: return 2;
					
					case ICElement.C_CLASS: return 3;
					case ICElement.C_STRUCT: return 4;
					case ICElement.C_UNION: return 5;
					
					case ICElement.C_FIELD: return 6;
					case ICElement.C_FUNCTION: return 7;		
				}
				
			}
			return 0;
		}
	}

    /**
     * 
     * The view menu's Sort action.
     *
     * @author P.Tomaszewski
     */
    private class LexicalSortingAction extends Action {

    	private static final String STORE_LEXICAL_SORTING_CHECKED= "LexicalSortingAction.isChecked"; //$NON-NLS-1$

    	/** The tree viewer */
    	private TreeViewer fOutlineViewer;
        /** Sorter for tree viewer. */
        private LexicalCSorter fSorter;

    	/**
    	 * Creates new action.
    	 */
    	public LexicalSortingAction(TreeViewer treeViewer) {
    		super(ActionMessages.getString("COutlineInformationControl.viewMenu.sort.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
    		CPluginImages.setLocalImageDescriptors(this, CPluginImages.IMG_ALPHA_SORTING);
    		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.LEXICAL_SORTING_BROWSING_ACTION);

    		fOutlineViewer= treeViewer;
            fSorter= new LexicalCSorter();
            
    		boolean checked= getDialogSettings().getBoolean(STORE_LEXICAL_SORTING_CHECKED);
    		setChecked(checked);

    		if (checked && fOutlineViewer != null) {
    			fOutlineViewer.setComparator(fSorter);
    		}
    	}

    	/*
    	 * @see org.eclipse.jface.action.Action#run()
    	 */
    	public void run() {
    		final boolean on= isChecked();
    		fOutlineViewer.setComparator(on ? fSorter : null);
    		getDialogSettings().put(STORE_LEXICAL_SORTING_CHECKED, on);
    	}
    }
}
