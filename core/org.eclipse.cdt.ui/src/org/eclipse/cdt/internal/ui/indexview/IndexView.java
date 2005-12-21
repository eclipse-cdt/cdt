/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.PDOMUpdator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Doug Schaefer
 *
 */
public class IndexView extends ViewPart implements PDOMDatabase.IListener {

	private TreeViewer viewer;
//	private DrillDownAdapter drillDownAdapter;
	private IndexAction rebuildAction;
	private IndexAction openDefinitionAction;
	
	private static class BTreeCounter implements IBTreeVisitor {
		int count;
		PDOMDatabase pdom;
		public BTreeCounter(PDOMDatabase pdom) {
			this.pdom = pdom;
		}
		public int compare(int record) throws CoreException {
			return 1;
		}
		public boolean visit(int record) throws CoreException {
			if (record != 0 && ! PDOMBinding.isOrphaned(pdom, record))
				++count;
			return true;
		}
	}

	private static class BTreeIndex implements IBTreeVisitor {
		final int index;
		int count;
		int result;
		PDOMDatabase pdom;
		public BTreeIndex(PDOMDatabase pdom, int index) {
			this.pdom = pdom;
			this.index = index;
		}
		public int compare(int record) throws CoreException {
			return 1;
		};
		public boolean visit(int record) throws CoreException {
			if (record == 0 || PDOMBinding.isOrphaned(pdom, record))
				return true;
			
			if (count++ == index) {
				result = record;
				return false;
			} else
				return true;
		};
	}

	private class IndexContentProvider implements ILazyTreeContentProvider {

		public Object getParent(Object element) {
			return null;
		}

		public void updateElement(Object parent, int index) {
			try {
				if (parent instanceof ICModel) {
					ICModel model = (ICModel)parent;
					ICProject[] cprojects = model.getCProjects();
					int n = -1;
					for (int i = 0; i < cprojects.length; ++i) {
						ICProject cproject = cprojects[i];
						PDOMDatabase pdom = (PDOMDatabase)PDOM.getPDOM(cproject.getProject());
						if (pdom != null)
							++n;
						if (n == index) {
							viewer.replace(parent, index, cproject);
							int nl = 0;
							for (PDOMLinkage linkage = pdom.getFirstLinkage(); linkage != null; linkage = linkage.getNextLinkage())
								++nl;
							viewer.setChildCount(cproject, nl);
							
							if (viewer.getExpandedState(cproject))
								for (int j = 0; j < nl; ++j)
									updateElement(cproject, j);
							
							return;
						}

					}
				} else if (parent instanceof ICProject) {
					ICProject cproject = (ICProject)parent;
					PDOMDatabase pdom = (PDOMDatabase)PDOM.getPDOM(cproject.getProject());
					PDOMLinkage linkage = pdom.getFirstLinkage();
					if (linkage == null)
						return;
					for (int n = 0; n < index; ++n) {
						linkage = linkage.getNextLinkage();
						if (linkage == null)
							return;
					}
					viewer.replace(parent, index, linkage);
					
					BTreeCounter counter = new BTreeCounter(pdom);
					linkage.getIndex().visit(counter);
					viewer.setChildCount(linkage, counter.count);

					if (viewer.getExpandedState(linkage))
						for (int j = 0; j < counter.count; ++j)
						updateElement(linkage, j);
					return;
				} else if (parent instanceof PDOMLinkage) {
					PDOMLinkage linkage = (PDOMLinkage)parent;
					PDOMDatabase pdom = linkage.getPDOM();
					BTreeIndex visitor = new BTreeIndex(pdom, index);
					linkage.getIndex().visit(visitor);
					PDOMBinding binding = pdom.getBinding(visitor.result);
					if (binding != null)
						viewer.replace(parent, index, binding);
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}
	
	private class IndexLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element == null) {
				return "null :(";
			} else if (element instanceof PDOMNode) {
				try {
					return ((PDOMNode)element).getName();
				} catch (CoreException e) {
					return e.getMessage();
				}
			} else
				return super.getText(element);
		}
		
		public Image getImage(Object element) {
			if (element instanceof IVariable)
				return CUIPlugin.getImageDescriptorRegistry().get(
						CElementImageProvider.getVariableImageDescriptor());
			else if (element instanceof IFunction)
				return CUIPlugin.getImageDescriptorRegistry().get(
						CElementImageProvider.getFunctionImageDescriptor());
			else if (element instanceof ICPPClassType)
				return CUIPlugin.getImageDescriptorRegistry().get(
						CElementImageProvider.getClassImageDescriptor());
			else if (element instanceof IBinding)
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJ_ELEMENT);
//			else if (element instanceof ICProject)
//				return super.getImage(element);
			else
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJ_ELEMENT);
		}
		
	}
	
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new IndexContentProvider());
		viewer.setLabelProvider(new IndexLabelProvider());
		
		ICModel model = CoreModel.getDefault().getCModel();
		viewer.setInput(model);
		try {
			ICProject[] cprojects = model.getCProjects();
			int n = 0;
			for (int i = 0; i < cprojects.length; ++i) {
				PDOMDatabase pdom = (PDOMDatabase)PDOM.getPDOM(cprojects[i].getProject()); 
				if (pdom != null) {
					++n;
					pdom.addListener(this);
				}
			}
			viewer.setChildCount(model, n);
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		// Menu
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            private void hideMenuItems(IMenuManager manager) {
            }

            public void menuAboutToShow(IMenuManager manager) {
                IndexView.this.fillContextMenu(manager);
                hideMenuItems(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
	}
	
	private abstract static class IndexAction extends Action {
		public abstract boolean valid();
	}
	private void makeActions() {
		rebuildAction = new IndexAction() {
			public void run() {
				ISelection selection = viewer.getSelection();
				if (!(selection instanceof IStructuredSelection))
					return;
				
				Object[] objs = ((IStructuredSelection)selection).toArray();
				for (int i = 0; i < objs.length; ++i) {
					if (!(objs[i] instanceof ICProject))
						continue;
					
					ICProject cproject = (ICProject)objs[i];
					try {
						PDOM.deletePDOM(cproject.getProject());
						PDOMUpdator job = new PDOMUpdator(cproject, null);
						job.schedule();
					} catch (CoreException e) {
						CUIPlugin.getDefault().log(e);
					}
				}
			}
			public boolean valid() {
				ISelection selection = viewer.getSelection();
				if (!(selection instanceof IStructuredSelection))
					return false;
				Object[] objs = ((IStructuredSelection)selection).toArray();
				for (int i = 0; i < objs.length; ++i)
					if (objs[i] instanceof ICProject)
						return true;
				return false;
			}
		};
		rebuildAction.setText(CUIPlugin.getResourceString("IndexView.rebuildIndex.name")); //$NON-NLS-1$
		
		openDefinitionAction = new IndexAction() {
			public void run() {
				ISelection selection = viewer.getSelection();
				if (!(selection instanceof IStructuredSelection))
					return;
				
				Object[] objs = ((IStructuredSelection)selection).toArray();
				for (int i = 0; i < objs.length; ++i) {
					if (!(objs[i] instanceof PDOMBinding))
						continue;
					
					try {
						PDOMBinding binding = (PDOMBinding)objs[i];
						PDOMName name = binding.getFirstDefinition();
						if (name == null)
							name = binding.getFirstDeclaration();
						if (name == null)
							continue;
						
						IASTFileLocation location = name.getFileLocation();
						IPath path = new Path(location.getFileName());
						Object input = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
						if (input == null)
							input = new FileStorage(path);

						IEditorPart editor = EditorUtility.openInEditor(input);
						if (editor != null && editor instanceof ITextEditor) {
							((ITextEditor)editor).selectAndReveal(location.getNodeOffset(), location.getNodeLength());
							return;
						}
					} catch (CoreException e) {
						CUIPlugin.getDefault().log(e);
					}
				}
			}
			public boolean valid() {
				ISelection selection = viewer.getSelection();
				if (!(selection instanceof IStructuredSelection))
					return false;
				Object[] objs = ((IStructuredSelection)selection).toArray();
				for (int i = 0; i < objs.length; ++i)
					if (objs[i] instanceof PDOMBinding)
						return true;
				return false;
			}
		};
		openDefinitionAction.setText(CUIPlugin.getResourceString("IndexView.openDefinition.name"));//$NON-NLS-1$
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				IndexView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		if (rebuildAction.valid())
			manager.add(rebuildAction);
		if (rebuildAction.valid())
			manager.add(openDefinitionAction);
		//manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openDefinitionAction.run();
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		//fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		//drillDownAdapter.addNavigationActions(manager);
	}
	
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void handleChange(PDOMDatabase pdom) {
		try {
			final ICModel model = (ICModel)viewer.getInput();
			if (model == null)
				return;
			ICProject[] cprojects = model.getCProjects();
			int n = -1;
			for (int i = 0; i < cprojects.length; ++i) {
				ICProject cproject = cprojects[i];
				IPDOM pp = PDOM.getPDOM(cproject.getProject()); 
				if (pp != null) {
					++n;
					if (pp == pdom){
						final int index = n;
						viewer.getControl().getDisplay().asyncExec(new Runnable() {
							public void run() {
								((IndexContentProvider)viewer.getContentProvider()).updateElement(model, index);
							};
						});
						return;
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
}
