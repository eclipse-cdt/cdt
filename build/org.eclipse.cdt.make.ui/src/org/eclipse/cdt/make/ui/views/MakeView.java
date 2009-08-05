/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev (Quoin Inc.)
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dnd.AbstractContainerAreaDropAdapter;
import org.eclipse.cdt.make.internal.ui.dnd.AbstractSelectionDragAdapter;
import org.eclipse.cdt.make.internal.ui.dnd.FileTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.LocalTransferDragSourceListener;
import org.eclipse.cdt.make.internal.ui.dnd.LocalTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransfer;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferDragSourceListener;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.TextTransferDragSourceListener;
import org.eclipse.cdt.make.internal.ui.dnd.TextTransferDropTargetListener;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.make.ui.MakeContentProvider;
import org.eclipse.cdt.make.ui.MakeLabelProvider;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.DelegatingDropAdapter;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

public class MakeView extends ViewPart {

	private Clipboard clipboard;

	private BuildTargetAction buildTargetAction;
	private EditTargetAction editTargetAction;
	private DeleteTargetAction deleteTargetAction;
	private AddTargetAction newTargetAction;
	private CopyTargetAction copyTargetAction;
	private PasteTargetAction pasteTargetAction;
	private TreeViewer fViewer;
	private DrillDownAdapter drillDownAdapter;
	private Action trimEmptyFolderAction;

	public MakeView() {
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		fViewer.getTree().setFocus();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(parent, IMakeHelpContextIds.MAKE_VIEW);
		fViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setUseHashlookup(true);
		fViewer.setContentProvider(new MakeContentProvider());
		fViewer.setLabelProvider(new MakeLabelProvider());
		initDragAndDrop();

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

		fViewer.setSorter(new ViewerSorter() {

			@Override
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
	 * Initialize drag and drop operations.
	 */
	private void initDragAndDrop() {
		int opers= DND.DROP_COPY | DND.DROP_MOVE;

		// LocalSelectionTransfer is used inside Make Target View
		// TextTransfer is used to drag outside the View or eclipse
		Transfer[] dragTransfers= {
			LocalSelectionTransfer.getTransfer(),
			MakeTargetTransfer.getInstance(),
			TextTransfer.getInstance(),
		};
		AbstractSelectionDragAdapter[] dragListeners = {
			new LocalTransferDragSourceListener(fViewer),
			new MakeTargetTransferDragSourceListener(fViewer),
			new TextTransferDragSourceListener(fViewer),
		};

		DelegatingDragAdapter delegatingDragAdapter = new DelegatingDragAdapter();
		for (AbstractSelectionDragAdapter dragListener : dragListeners) {
			delegatingDragAdapter.addDragSourceListener(dragListener);
		}
		fViewer.addDragSupport(opers, dragTransfers, delegatingDragAdapter);

		Transfer[] dropTransfers= {
			LocalSelectionTransfer.getTransfer(),
			MakeTargetTransfer.getInstance(),
			FileTransfer.getInstance(),
			TextTransfer.getInstance(),
		};
		AbstractContainerAreaDropAdapter[] dropListeners = {
			new LocalTransferDropTargetListener(fViewer),
			new MakeTargetTransferDropTargetListener(fViewer),
			new FileTransferDropTargetListener(fViewer),
			new TextTransferDropTargetListener(fViewer),
		};
		DelegatingDropAdapter delegatingDropAdapter = new DelegatingDropAdapter();
		for (AbstractContainerAreaDropAdapter dropListener : dropListeners) {
			delegatingDropAdapter.addDropTargetListener(dropListener);
		}
		fViewer.addDropSupport(opers | DND.DROP_DEFAULT, dropTransfers, delegatingDropAdapter);
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
				//Check the make targets of the specified container, and if they don't exist, run
				//through the children looking for the first match that we can find that contains
				//a make target.
				private boolean hasMakeTargets(IFolder container) throws CoreException {
					IMakeTarget [] targets = MakeCorePlugin.getDefault().getTargetManager().getTargets(container);
					if(targets != null && targets.length > 0) {
						return true;
					}

					final boolean [] haveTargets = new boolean[1];
					haveTargets[0] = false;

					IResourceProxyVisitor visitor = new IResourceProxyVisitor() {
						public boolean visit(IResourceProxy proxy) throws CoreException {
							if(haveTargets[0]) {
								return false;	//We found what we were looking for
							}
							if(proxy.getType() != IResource.FOLDER) {
								return true;	//We only look at folders for content
							}
							IFolder folder = (IFolder) proxy.requestResource();
							IMakeTarget [] targets = MakeCorePlugin.getDefault().getTargetManager().getTargets(folder);
							if(targets != null && targets.length > 0) {
								haveTargets[0] = true;
								return false;
							}
							return true;		//Keep looking
						}
					};
					container.accept(visitor, IResource.NONE);

					return haveTargets[0];
				}

				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (isChecked() && element instanceof IFolder) {
						try {
							return hasMakeTargets((IFolder)element);
						} catch(Exception ex) {
							return false;
						}
					}
					return true;
				}
			});
		}

		@Override
		public void run() {
			fViewer.refresh();
			getSettings().put(FILTER_EMPTY_FOLDERS, isChecked());
		}
	}

	private void makeActions() {
		Shell shell = fViewer.getControl().getShell();

		clipboard = new Clipboard(shell.getDisplay());

		buildTargetAction = new BuildTargetAction(shell);
		newTargetAction = new AddTargetAction(shell);
		copyTargetAction = new CopyTargetAction(shell, clipboard, pasteTargetAction);
		pasteTargetAction = new PasteTargetAction(shell, clipboard);
		deleteTargetAction = new DeleteTargetAction(shell);
		editTargetAction = new EditTargetAction(shell);
		trimEmptyFolderAction = new FilterEmtpyFoldersAction();
	}
	private void contributeToActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();
		fillLocalPullDown(actionBars.getMenuManager());
		fillLocalToolBar(actionBars.getToolBarManager());

		TextActionHandler textActionHandler = new TextActionHandler(actionBars); // hooks handlers
		textActionHandler.setCopyAction(copyTargetAction);
		textActionHandler.setPasteAction(pasteTargetAction);
		textActionHandler.setDeleteAction(deleteTargetAction);
	}

	private void fillLocalToolBar(IToolBarManager toolBar) {
		toolBar.add(newTargetAction);
		toolBar.add(buildTargetAction);
		toolBar.add(new Separator());
		drillDownAdapter.addNavigationActions(toolBar);
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
		manager.add(newTargetAction);
		manager.add(editTargetAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		manager.add(new Separator());
		manager.add(copyTargetAction);
		manager.add(pasteTargetAction);
		manager.add(deleteTargetAction);
		manager.add(new Separator());
		manager.add(buildTargetAction);

		// Other plug-ins can contribute there actions here
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
		buildTargetAction.run();
	}

	void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();
		updateActions(sel);
	}

	void updateActions(IStructuredSelection sel) {
		newTargetAction.selectionChanged(sel);
		buildTargetAction.selectionChanged(sel);
		deleteTargetAction.selectionChanged(sel);
		editTargetAction.selectionChanged(sel);
		copyTargetAction.selectionChanged(sel);
		pasteTargetAction.selectionChanged(sel);
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
		super.dispose();
	}

}
