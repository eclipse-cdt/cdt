package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

public class MakeView extends ViewPart {
	
	TreeViewer viewer;
	DrillDownAdapter drillDownAdapter;

	public MakeView() {
		super();
	}

	/**
	* @see IWorkbenchPart#setFocus()
	*/
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	/**
	* @see ContentOutlinePage#createControl
	*/
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new MakeContentProvider());
		viewer.setLabelProvider(new MakeLabelProvider());

		drillDownAdapter = new DrillDownAdapter(viewer);

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					handleDeleteKeyPressed();
				}
			}
		});

		viewer.setContentProvider(new MakeContentProvider());
		viewer.setLabelProvider(new MakeLabelProvider());
		viewer.setInput(MakeCorePlugin.getDefault().getTargetProvider());
		getSite().setSelectionProvider(viewer);

		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void makeActions() {
		// dinglis-TODO Auto-generated method stub
		
	}
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager toolBar) {
		drillDownAdapter.addNavigationActions(toolBar);
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}
	

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MakeView.this.fillContextMenu(manager);
				updateActions((IStructuredSelection) viewer.getSelection());
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}


	protected void fillContextMenu(IMenuManager manager) {
//		manager.add(deleteAction);
//		manager.add(renameAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator("Additions"));
	}

	protected void handleDeleteKeyPressed() {
		// dinglis-TODO Auto-generated method stub
		
	}


	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		//System.out.println ("Double click on " + element);
		if (element instanceof MakeTarget) {
			MakeTarget ta = (MakeTarget) element;
//			Action build = new MakeBuildAction(new MakeTarget[] { ta }, getViewSite().getShell(), "Build");
//			build.run();
		}
		//if (viewer.isExpandable(element)) {
		//	viewer.setExpandedState(element, !viewer.getExpandedState(element));
		//}
	}

	void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
//		updateStatusLine(sel);
		updateActions(sel);
	}
	
	void updateActions(IStructuredSelection sel) {
//		deleteAction.selectionChanged(sel);
//		renameAction.selectionChanged(sel);
	}



}
