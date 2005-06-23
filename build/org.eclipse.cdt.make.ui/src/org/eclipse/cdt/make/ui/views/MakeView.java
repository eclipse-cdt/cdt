/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.make.ui.MakeContentProvider;
import org.eclipse.cdt.make.ui.MakeLabelProvider;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

public class MakeView extends ViewPart {

	private BuildTargetAction buildTargetAction;
	private EditTargetAction editTargetAction;
	private DeleteTargetAction deleteTargetAction;
	AddTargetAction addTargetAction;
	TreeViewer fViewer;
	DrillDownAdapter drillDownAdapter;
	private Action trimEmptyFolderAction;

	public MakeView() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		fViewer.getTree().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(parent, IMakeHelpContextIds.MAKE_VIEW);
		fViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setUseHashlookup(true);
		fViewer.setContentProvider(new MakeContentProvider());
		fViewer.setLabelProvider(new MakeLabelProvider());

		drillDownAdapter = new DrillDownAdapter(fViewer);

		fViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		fViewer.getControl().addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					handleDeleteKeyPressed();
				}
			}
		});

		fViewer.setContentProvider(new MakeContentProvider());
		fViewer.setLabelProvider(new MakeLabelProvider());
		fViewer.setSorter(new ViewerSorter() {

			public int category(Object element) {
				if (element instanceof IResource) {
					return 0;
				}
				return 1;
			}
		});
		fViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		getSite().setSelectionProvider(fViewer);

		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	/**
	 * Returns setting for this control.
	 * 
	 * @return Settings.
	 */
	IDialogSettings getSettings() {
		final String sectionName = "org.eclipse.cdt.internal.ui.MakeView"; //$NON-NLS-1$
		IDialogSettings settings = MakeUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null) {
			settings = MakeUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
		}
		return settings;
	}

	protected class FilterEmtpyFoldersAction extends Action {

		private static final String FILTER_EMPTY_FOLDERS = "FilterEmptyFolders"; //$NON-NLS-1$

		public FilterEmtpyFoldersAction() {
			super(MakeUIPlugin.getResourceString("FilterEmptyFolderAction.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
			setToolTipText(MakeUIPlugin.getResourceString("FilterEmptyFolderAction.tooltip")); //$NON-NLS-1$
			setChecked(getSettings().getBoolean(FILTER_EMPTY_FOLDERS));
			MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_FILTER); //$NON-NLS-1$
			fViewer.addFilter(new ViewerFilter() {

				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (isChecked() && element instanceof IFolder) {
						ITreeContentProvider provider = (ITreeContentProvider) ((ContentViewer)viewer).getContentProvider();
						Object[] children = provider.getChildren(element);
						for (int i = 0; i < children.length; i++) {
							if (select(viewer, element, children[i]))
								return true;
						}
						return false;
					}
					return true;
				}
			});
		}

		public void run() {
			fViewer.refresh();
			getSettings().put(FILTER_EMPTY_FOLDERS, isChecked());
		}
	}

	private void makeActions() {
		buildTargetAction = new BuildTargetAction(fViewer.getControl().getShell());
		addTargetAction = new AddTargetAction(fViewer.getControl().getShell());
		deleteTargetAction = new DeleteTargetAction(fViewer.getControl().getShell());
		editTargetAction = new EditTargetAction(fViewer.getControl().getShell());
		trimEmptyFolderAction = new FilterEmtpyFoldersAction();
	}
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager toolBar) {
		drillDownAdapter.addNavigationActions(toolBar);
		toolBar.add(buildTargetAction);
		toolBar.add(trimEmptyFolderAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				MakeView.this.fillContextMenu(manager);
				updateActions((IStructuredSelection)fViewer.getSelection());
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		// getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(buildTargetAction);
		manager.add(addTargetAction);
		manager.add(deleteTargetAction);
		manager.add(editTargetAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);

		// Other plug-ins can contribute there actions here
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void handleDeleteKeyPressed() {
		deleteTargetAction.run();
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
		buildTargetAction.run();
	}

	void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();
		updateActions(sel);
	}

	void updateActions(IStructuredSelection sel) {
		addTargetAction.selectionChanged(sel);
		buildTargetAction.selectionChanged(sel);
		deleteTargetAction.selectionChanged(sel);
		editTargetAction.selectionChanged(sel);
	}
}
