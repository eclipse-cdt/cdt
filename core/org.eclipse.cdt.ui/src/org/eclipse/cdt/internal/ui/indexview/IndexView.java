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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMember;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPNamespace;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Doug Schaefer
 *
 */
public class IndexView extends ViewPart implements PDOM.IListener {

	private TreeViewer viewer;
//	private DrillDownAdapter drillDownAdapter;
	private ToggleLinkingAction toggleLinkingAction;
	private IndexAction rebuildAction;
	private IndexAction discardExternalDefsAction;
	private IndexAction openDefinitionAction;
	private IndexAction findDeclarationsAction;
	private IndexAction findReferencesAction;
	Filter filter = new Filter();
	public boolean isLinking = false;
	
	public void toggleExternalDefs() {
		filter.showExternalDefs = ! filter.showExternalDefs;
		viewer.refresh();
	}
	
	public void toggleLinking() {
		isLinking = ! isLinking;
		if (isLinking) {
			openDefinitionAction.run();
		}
	}
	
	
	/**
	 * Handles selection changed in viewer. Updates global actions. Links to
	 * editor (if option enabled)
	 */
	void handleSelectionChanged(SelectionChangedEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//		updateStatusLine(selection);
//		updateActionBars(selection);
		if (isLinking) {
			openDefinitionAction.run();
		}
	}
	private static class Filter {
		public boolean showExternalDefs = false;
		public boolean accept(PDOMBinding binding) {
			if(showExternalDefs)
				return true;
			return isLocalToWorkspace(binding);
		}
		
		private static boolean isLocalToWorkspace(PDOMBinding binding) {
			try {
				PDOMName name = binding.getFirstReference();
				if (name == null)
					name = binding.getFirstDeclaration();
				if (name == null)
					name = binding.getFirstDefinition();
				
				if (name == null)
					return false;
				
				IASTFileLocation location = name.getFileLocation();
				IPath path = new Path(location.getFileName());
				Object input = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
				if (input == null)
					return false;
				return true;
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
			return false;
		}
	}
	private static class Counter implements IBTreeVisitor {
		int count;
		PDOM pdom;
		public Counter(PDOM pdom) {
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

	private static class Children implements IBTreeVisitor {
		final PDOM pdom;
		final PDOMBinding[] bindings;
		final Filter filter;
		int index;
		public Children(PDOM pdom, PDOMBinding[] bindings, Filter filter) {
			this.pdom = pdom;
			this.bindings = bindings;
			this.filter = filter;
		}
		public int compare(int record) throws CoreException {
			return 1;
		};
		public boolean visit(int record) throws CoreException {
			if (record == 0 || PDOMBinding.isOrphaned(pdom, record))
				return true;
			
			PDOMBinding binding = pdom.getBinding(record);
			if( filter.accept(binding) )
				bindings[index++] = binding;
			return true;
		};
	}

// TODO - turned off until M5 lands.
//	private class IndexLazyContentProvider implements ILazyTreeContentProvider {
//
//		public Object getParent(Object element) {
//			return null;
//		}
//
//		public void updateElement(Object parent, int index) {
//			try {
//				if (parent instanceof ICModel) {
//					ICModel model = (ICModel)parent;
//					ICProject[] cprojects = model.getCProjects();
//					int n = -1;
//					for (int i = 0; i < cprojects.length; ++i) {
//						ICProject cproject = cprojects[i];
//						PDOMDatabase pdom = (PDOMDatabase)PDOM.getPDOM(cproject.getProject());
//						if (pdom != null)
//							++n;
//						if (n == index) {
//							viewer.replace(parent, index, cproject);
//							int nl = 0;
//							for (PDOMLinkage linkage = pdom.getFirstLinkage(); linkage != null; linkage = linkage.getNextLinkage())
//								++nl;
//							viewer.setChildCount(cproject, nl);
//							return;
//						}
//					}
//				} else if (parent instanceof ICProject) {
//					ICProject cproject = (ICProject)parent;
//					PDOMDatabase pdom = (PDOMDatabase)PDOM.getPDOM(cproject.getProject());
//					PDOMLinkage linkage = pdom.getFirstLinkage();
//					if (linkage == null)
//						return;
//					for (int n = 0; n < index; ++n) {
//						linkage = linkage.getNextLinkage();
//						if (linkage == null)
//							return;
//					}
//					LinkageCache linkageCache = new LinkageCache(pdom, linkage);
//					viewer.replace(parent, index, linkageCache);
//					viewer.setChildCount(linkageCache, linkageCache.getCount());
//				} else if (parent instanceof LinkageCache) {
//					LinkageCache linkageCache = (LinkageCache)parent;
//					PDOMBinding binding = linkageCache.getItem(index);
//					if (binding != null) {
//						viewer.replace(parent, index, binding);
//						if (binding instanceof PDOMMemberOwner) {
//							PDOMMemberOwner owner = (PDOMMemberOwner)binding;
//							viewer.setChildCount(binding, owner.getNumMembers());
//						} else
//							viewer.setChildCount(binding, 0);
//					}
//				} else if (parent instanceof PDOMMemberOwner) {
//					PDOMMemberOwner owner = (PDOMMemberOwner)parent;
//					PDOMMember member = owner.getMember(index);
//					viewer.replace(parent, index, member);
//					viewer.setChildCount(member, 0);
//				}
//			} catch (CoreException e) {
//				CUIPlugin.getDefault().log(e);
//			}
//		}
//
//		public void dispose() {
//		}
//
//		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//		}
//
//	}
	
	static PDOMBinding[] trim(PDOMBinding []binding) {
		int len;
		for (len = 0; len < binding.length; len++)
			if(binding[len] == null) {
				PDOMBinding [] newBinding = new PDOMBinding [len];
				System.arraycopy(binding, 0, newBinding, 0, len);
				return newBinding;
			}
		return binding;
	}
	private class IndexContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ICProject) {
				try {
					PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(((ICProject)parentElement).getProject());
					int n = 0;
					for (PDOMLinkage linkage = pdom.getFirstLinkage(); linkage != null; linkage = linkage.getNextLinkage())
						++n;
					PDOMLinkage[] linkages = new PDOMLinkage[n];
					int i = 0;
					for (PDOMLinkage linkage = pdom.getFirstLinkage(); linkage != null; linkage = linkage.getNextLinkage())
						linkages[i++] = linkage;
					return linkages;
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			} else if (parentElement instanceof PDOMLinkage) {
				try {
					PDOMLinkage linkage = (PDOMLinkage)parentElement;
					PDOM pdom = linkage.getPDOM();
					Counter counter = new Counter(pdom);
					linkage.getIndex().visit(counter);
					PDOMBinding[] bindings = new PDOMBinding[counter.count];
					Children children = new Children(pdom, bindings, filter);
					linkage.getIndex().visit(children);
					return trim(bindings);
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				} 
			} else if (parentElement instanceof PDOMCPPNamespace) {
				try {
					PDOMCPPNamespace namespace = (PDOMCPPNamespace)parentElement;
					PDOM pdom = namespace.getPDOM();
					Counter counter = new Counter(pdom);
					namespace.getIndex().visit(counter);
					PDOMBinding[] bindings = new PDOMBinding[counter.count];
					Children children = new Children(pdom, bindings, filter);
					namespace.getIndex().visit(children);
					return trim(bindings);
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				} 
			} else if (parentElement instanceof PDOMMemberOwner) {
				try {
					PDOMMemberOwner owner = (PDOMMemberOwner)parentElement;
					int n = 0;
					for (PDOMMember member = owner.getFirstMember(); member != null; member = member.getNextMember())
						++n;
					PDOMMember[] members = new PDOMMember[n];
					int i = 0;
					for (PDOMMember member = owner.getFirstMember(); member != null; member = member.getNextMember())
						members[i++] = member;
					return members;
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ICProject) {
				IPDOM ipdom = CCorePlugin.getPDOMManager().getPDOM(((ICProject)element).getProject());
				if (ipdom == null || !(ipdom instanceof PDOM))
					return false;
				
				try {
					PDOM pdom = (PDOM)ipdom;
					return pdom.getFirstLinkage() != null;
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			} else if (element instanceof PDOMLinkage
					|| element instanceof PDOMCPPNamespace) {
				return true;
			} else if (element instanceof PDOMMemberOwner) {
				try {
					PDOMMemberOwner owner = (PDOMMemberOwner)element;
					return owner.getFirstMember() != null;
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			}
			
			return false;
		}

		public Object[] getElements(Object inputElement) {
			try {
				if (inputElement instanceof ICModel) {
			
					ICModel model = (ICModel)inputElement;
					return model.getCProjects();
				}
			} catch (CModelException e) { }
			
			return new Object[0];
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
			} else if (element instanceof LinkageCache) {
				try {
					return ((LinkageCache)element).getName();
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
			else if (element instanceof ICompositeType)
				return CUIPlugin.getImageDescriptorRegistry().get(
						CElementImageProvider.getStructImageDescriptor());
			else if (element instanceof ICPPNamespace)
				return CUIPlugin.getImageDescriptorRegistry().get(
						CElementImageProvider.getNamespaceImageDescriptor());
			else if (element instanceof IBinding)
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJ_ELEMENT);
			else if (element instanceof ICProject)
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						IDE.SharedImages.IMG_OBJ_PROJECT);
			else
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJ_ELEMENT);
		}
		
	}
	
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		viewer = new TreeViewer(parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new IndexContentProvider());
		viewer.setLabelProvider(new IndexLabelProvider());
		
		ICModel model = CoreModel.getDefault().getCModel();
		viewer.setInput(model);
		try {
			ICProject[] cprojects = model.getCProjects();
			int n = 0;
			for (int i = 0; i < cprojects.length; ++i) {
				PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(cprojects[i].getProject()); 
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
        
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
	}
	
	private void makeActions() {
		rebuildAction = new RebuildIndexAction(viewer);
		discardExternalDefsAction = new DiscardExternalDefsAction(viewer, this);
		toggleLinkingAction = new ToggleLinkingAction(this);
		openDefinitionAction = new OpenDefinitionAction(viewer);
		findDeclarationsAction = new FindDeclarationsAction(viewer);
		findReferencesAction = new FindReferencesAction(viewer);
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
		if (discardExternalDefsAction.valid())
			manager.add(discardExternalDefsAction);
		if (openDefinitionAction.valid())
			manager.add(openDefinitionAction);
		if (findDeclarationsAction.valid())
			manager.add(findDeclarationsAction);
		if (findReferencesAction.valid())
			manager.add(findReferencesAction);
		//manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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
//		drillDownAdapter.addNavigationActions(manager);
		manager.add(toggleLinkingAction);
		manager.add(discardExternalDefsAction);
	}
	
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void handleChange(PDOM pdom) {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
//				ICModel model = CoreModel.getDefault().getCModel();
//				viewer.setInput(model);
//				try {
//					ICProject[] cprojects = model.getCProjects();
//					int n = 0;
//					for (int i = 0; i < cprojects.length; ++i) {
//						PDOMDatabase pdom = (PDOMDatabase)PDOM.getPDOM(cprojects[i].getProject()); 
//						if (pdom != null)
//							++n;
//					}
//					viewer.setChildCount(model, n);
//				} catch (CModelException e) {
//					CUIPlugin.getDefault().log(e);
//				}
			}
		});
	}
	
}
