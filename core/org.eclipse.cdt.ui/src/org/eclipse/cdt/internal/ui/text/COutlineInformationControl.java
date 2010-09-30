/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.editor.CContentOutlinerProvider;
import org.eclipse.cdt.internal.ui.editor.LexicalSortingAction;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCLabelProvider;

/**
 * Control which shows outline information in C/C++ editor. Based on
 * AbstracInformationContol/JavaOutlineInformationControl from JDT.
 * 
 * @author P.Tomaszewski
 */
public class COutlineInformationControl extends AbstractInformationControl {

	private static final long TEXT_FLAGS = AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | CElementLabels.F_APP_TYPE_SIGNATURE | CElementLabels.M_APP_RETURNTYPE;
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
    @Override
	protected TreeViewer createTreeViewer(Composite parent, int treeStyle) {
        TreeViewer treeViewer = new ProblemTreeViewer(parent, treeStyle);
        final Tree tree = treeViewer.getTree();
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        fOutlineContentProvider = new CContentOutlinerProvider(treeViewer);
        treeViewer.setContentProvider(fOutlineContentProvider);
        fSortingAction= new LexicalSortingAction(treeViewer, ".isChecked"); //$NON-NLS-1$
		treeViewer.addFilter(new NamePatternFilter());
		treeViewer.setLabelProvider(new DecoratingCLabelProvider(new AppearanceAwareLabelProvider(TEXT_FLAGS,
				IMAGE_FLAGS), true));
        treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        return treeViewer;
    }

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractInformationControl#getId()
	 */
	@Override
	protected String getId() {
		return "org.eclipse.cdt.internal.ui.text.QuickOutline"; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
	protected void fillViewMenu(IMenuManager viewMenu) {
		super.fillViewMenu(viewMenu);

		viewMenu.add(new Separator("Sorters")); //$NON-NLS-1$
		viewMenu.add(fSortingAction);

	}
}
