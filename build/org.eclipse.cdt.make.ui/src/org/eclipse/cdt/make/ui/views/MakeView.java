package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

public class MakeView extends ViewPart {

	TreeViewer viewer;

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
	* Handles double clicks in viewer.
	* Opens editor if file double-clicked.
	*/
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

	/**
	* called to create the context menu of the outline
	*/
	protected void contextMenuAboutToShow(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Object element = selection.getFirstElement();
		if (element instanceof MakeTarget) {
			final MakeTarget ta = (MakeTarget) element;
			Action add = new Action("Add...") {
				public void run() {
					InputDialog dialog =
						new InputDialog(getViewSite().getShell(), "Target Dialog: ", "Enter Target(s): ", null, null);
					dialog.open();
					String value = dialog.getValue();
					if (value != null && value.length() > 0) {
//						IResource res = ta.getResource();
//						MakeUtil.addPersistentTarget(res, value);
						viewer.getControl().setRedraw(false);
						viewer.refresh();
						viewer.getControl().setRedraw(true);
						viewer.expandToLevel(ta, 2);
					}
				}
			};
			Action edit = new Action("Edit...") {
				public void run() {
					String oldtarget = ta.toString();
					InputDialog dialog =
						new InputDialog(getViewSite().getShell(), "Target Dialog: ", "Enter Target(s): ", oldtarget, null);
					dialog.open();
					String value = dialog.getValue();
					if (value != null && value.length() > 0 && !value.equals(oldtarget)) {
//						IResource res = ta.getResource();
//						MakeUtil.replacePersistentTarget(res, oldtarget, value);
						viewer.getControl().setRedraw(false);
						viewer.refresh();
						viewer.getControl().setRedraw(true);
						viewer.expandToLevel(ta, 2);
					}
				}
			};

			Action del = new Action("Delete") {
				public void run() {
					String target = ta.toString();
					if (target != null) {
//						IResource res = ta.getResource();
//						MakeUtil.removePersistentTarget(res, target);
						viewer.getControl().setRedraw(false);
						viewer.refresh();
						viewer.getControl().setRedraw(true);
					}
				}
			};

//			Action build = new MakeBuildAction(new MakeTarget[] { ta }, getViewSite().getShell(), "Build");

			menu.add(add);
			menu.add(edit);
			menu.add(del);
			//menu.add (new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//			menu.add(build);
			if (ta.isLeaf()) {
				add.setEnabled(false);
			} else {
				edit.setEnabled(false);
				del.setEnabled(false);
			}
		}
		//menu.add (new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
//		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager toolBar) {
		Action refreshAllAction = new Action("Refresh") {
			public void run() {
				viewer.refresh();
			}

		};
		toolBar.add(refreshAllAction);
	}

	/**
	* @see ContentOutlinePage#createControl
	*/
	public void createPartControl(Composite parent) {

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new MakeContentProvider());
		viewer.setLabelProvider(new MakeLabelProvider());

		MenuManager manager = new MenuManager("#PopUp");
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});

		Control control = viewer.getControl();
		Menu menu = manager.createContextMenu(control);
		control.setMenu(menu);

		viewer.setInput(new MakeTarget(ResourcesPlugin.getWorkspace().getRoot()));

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});

		contributeToActionBars();

		getSite().setSelectionProvider(viewer);
	}
}
