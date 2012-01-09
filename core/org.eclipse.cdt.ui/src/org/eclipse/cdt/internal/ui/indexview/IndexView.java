/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.IPDOM;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;

import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;
import org.eclipse.cdt.internal.ui.viewsupport.ExtendedTreeViewer;

/**
 * @author Doug Schaefer
 *
 */
public class IndexView extends ViewPart implements PDOM.IListener, IElementChangedListener {

	private TreeViewer viewer;
	private ToggleLinkingAction toggleLinkingAction;
	private IndexAction countSymbolsAction;
	private IndexAction discardExternalDefsAction;
	private IndexAction openDefinitionAction;
	private IndexAction findDeclarationsAction;
	private IndexAction findReferencesAction;
	Filter filter = new Filter();
	public boolean isLinking = false;
	private volatile boolean fUpdateRequested= false;
	private Map<String, Long> fTimestampPerProject= new HashMap<String, Long>();
	private IndexContentProvider contentProvider;

	
	public void toggleExternalDefs() {
		filter.showExternalDefs = ! filter.showExternalDefs;
		if (!filter.showExternalDefs) {
			viewer.addFilter(filter);
		} else {
			viewer.removeFilter(filter);
		}
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
		if (isLinking) {
			openDefinitionAction.run();
		}
	}
	
	private static class Filter extends ViewerFilter {
		public boolean showExternalDefs = false;
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IndexNode) {
				IndexNode node= (IndexNode)element;
				return node.fHasDeclarationInProject;
			}
			return true;
		}
		public static boolean hasDeclarationInProject(IPDOMNode element) {
			if (element instanceof PDOMBinding) {
				try {
					PDOMBinding binding = (PDOMBinding)element;
					final PDOM pdom= binding.getPDOM();
					IIndexName[] names= pdom.findNames(binding, IIndex.FIND_DECLARATIONS);
					for (int i = 0; i < names.length; i++) {
						IIndexName name = names[i];
						if (name.getFile().getLocation().getFullPath() != null) {
							return true;
						}
					}
					names= pdom.findNames(binding, IIndex.FIND_DEFINITIONS);
					for (int i = 0; i < names.length; i++) {
						IIndexName name = names[i];
						if (name.getFile().getLocation().getFullPath() != null) {
							return true;
						}
					}
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
			else if (element instanceof PDOMLinkage) {
				return true;
			}
			return false;
		}
	}
		
	private static class Children implements IPDOMVisitor {
		private ArrayList<IPDOMNode> fNodes;
		public Children() {
			fNodes= new ArrayList<IPDOMNode>();
		}
		@Override
		public boolean visit(IPDOMNode node) throws CoreException {
			fNodes.add(node);
			return false;
		}
		@Override
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IPDOMNode[] getNodes() {
			return fNodes.toArray(new IPDOMNode[fNodes.size()]);
		}
	}
			
	private class IndexContentProvider extends AsyncTreeContentProvider {
		public IndexContentProvider(Display disp) {
			super(disp);
		}
		
		@Override
		public Object getParent(Object element) {
			if (element instanceof IndexNode) {
				return ((IndexNode) element).fParent;
			}
			if (element instanceof ICElement) {
				return ((ICElement) element).getParent();
			}
			return null;
		}

		@Override
		protected Object[] syncronouslyComputeChildren(Object parentElement) {
			if (parentElement instanceof ICModel) {
				ICModel element = (ICModel) parentElement;
				try {
					return element.getCProjects();
				} catch (CModelException e) {
					CUIPlugin.log(e);
					return new Object[0];
				}
			}
			else if (parentElement instanceof IndexNode) {
				final IndexNode node= (IndexNode) parentElement;
				if (node.fObject instanceof PDOMBinding) {
					final PDOMBinding binding= (PDOMBinding) node.fObject;
					if (!binding.mayHaveChildren()) {
						return new Object[0];
					}
				}
			}
			// allow for async computation
			return null;
		}


		@Override
		protected Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
			try {
				if (parentElement instanceof ICProject) {
					ICProject cproject= (ICProject)parentElement;
					if (!cproject.getProject().isOpen()) {
						return new Object[0];
					}
					return computeChildren(cproject);
				}
				else if (parentElement instanceof IndexNode) {
					IndexNode node= (IndexNode) parentElement;
					ICProject cproject= node.getProject();
					if (cproject != null && cproject.getProject().isOpen()) {
						Long ts= fTimestampPerProject.get(cproject.getElementName());
						IPDOM pdom= CCoreInternals.getPDOMManager().getPDOM(cproject);
						pdom.acquireReadLock();
						try {
							if (ts == null || ts.longValue() == pdom.getLastWriteAccess()) {
								return computeChildren(parentElement, node.fObject);
							}
						}
						finally {
							pdom.releaseReadLock();
						}
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return new Object[0];
		}

		private Object[] computeChildren(ICProject cproject) throws CoreException, InterruptedException {
			IPDOM pdom = CCoreInternals.getPDOMManager().getPDOM(cproject);
			pdom.acquireReadLock();
			try {
				fTimestampPerProject.put(cproject.getElementName(), new Long(pdom.getLastWriteAccess()));
				IPDOMNode[] linkages= pdom.getLinkageImpls();
				if (linkages.length == 1) {
					// Skip linkages in hierarchy if there is only one
					return computeChildren(cproject, linkages[0]);
				}
				return wrap(cproject, linkages);
			}
			finally {
				pdom.releaseReadLock();
			}
		}

		private Object[] computeChildren(Object parent, IPDOMNode node) throws CoreException {
			Children collector = new Children();
			node.accept(collector);
			return wrap(parent, collector.getNodes());
		}

		private Object[] wrap(Object parent, IPDOMNode[] nodes) {
			if (nodes.length == 0) {
				return nodes;
			}
			IndexNode[] result= new IndexNode[nodes.length];
			for (int i = 0; i < result.length; i++) {
				final IndexNode indexNode = result[i]= new IndexNode();
				final IPDOMNode node= nodes[i];
				indexNode.fParent= parent;
				indexNode.fObject= node;
				indexNode.fText= IndexLabelProvider.getText(node);
				indexNode.fImage= IndexLabelProvider.getImage(node);
				indexNode.fHasDeclarationInProject= Filter.hasDeclarationInProject(node);
				if (node instanceof PDOMNode) {
					indexNode.fBindingKind= ((PDOMNode) node).getNodeType();
				}
			}
			return result;
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new ExtendedTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		contentProvider= new IndexContentProvider(getSite().getShell().getDisplay());
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new IndexLabelProvider());
		viewer.setUseHashlookup(true);
		
		ICModel model = CoreModel.getDefault().getCModel();
		viewer.setInput(model);
		viewer.addFilter(filter);
		try {
			ICProject[] projects = model.getCProjects();
			for (int i = 0; i < projects.length; ++i) {
				IPDOM pdom = CCoreInternals.getPDOMManager().getPDOM(projects[i]); 
				pdom.addListener(this);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		CoreModel.getDefault().addElementChangedListener(this);
		
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

            @Override
			public void menuAboutToShow(IMenuManager manager) {
                IndexView.this.fillContextMenu(manager);
                hideMenuItems(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
        
        getSite().setSelectionProvider(viewer);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
	}
	
	@Override
	public void dispose() {
		super.dispose();
		ICModel model = CoreModel.getDefault().getCModel();
		try {
			ICProject[] projects = model.getCProjects();
			for (int i = 0; i < projects.length; ++i) {
				IPDOM pdom = CCoreInternals.getPDOMManager().getPDOM(projects[i]); 
				pdom.removeListener(this);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		CoreModel.getDefault().removeElementChangedListener(this);
	}
	
	private void makeActions() {
		countSymbolsAction = new CountNodeAction(this, viewer);
		discardExternalDefsAction = new DiscardExternalDefsAction(viewer, this);
		toggleLinkingAction = new ToggleLinkingAction(this);
		openDefinitionAction = new OpenDefinitionAction(this, viewer);
		findDeclarationsAction = new FindDeclarationsAction(this, viewer);
		findReferencesAction = new FindReferencesAction(this, viewer);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IndexView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		if (countSymbolsAction.valid())
			manager.add(countSymbolsAction);
		if (discardExternalDefsAction.valid())
			manager.add(discardExternalDefsAction);
		if (openDefinitionAction.valid())
			manager.add(openDefinitionAction);
		if (findDeclarationsAction.valid())
			manager.add(findDeclarationsAction);
		if (findReferencesAction.valid())
			manager.add(findReferencesAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
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
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void handleChange(PDOM pdom, PDOM.ChangeEvent e) {
		requestUpdate();
	}

	private void requestUpdate() {
		if (!fUpdateRequested) {
			fUpdateRequested= true;
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					fUpdateRequested= false;
					if (!viewer.getControl().isDisposed()) {
						contentProvider.recompute();
					}
				}
			});
		}
	}
	
	@Override
	public void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;

		// TODO we'll get fancier when we do a virtual tree.
		processDelta(event.getDelta());
	}
	
	private void processDelta(ICElementDelta delta) {
		int type = delta.getElement().getElementType();
		switch (type) {
		case ICElement.C_MODEL:
			// Loop through the children
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i)
				processDelta(children[i]);
			break;
		case ICElement.C_PROJECT:
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				try {
					IPDOM pdom = CCoreInternals.getPDOMManager().getPDOM((ICProject)delta.getElement());
					pdom.addListener(this);
					handleChange(null, null);
				} catch (CoreException e) {
				}
				break;
			case ICElementDelta.REMOVED:
				handleChange(null, null);
				break;
			}
		}
	}

	public long getLastWriteAccess(ICProject cproject) {
		Long result= fTimestampPerProject.get(cproject.getElementName());
		return result == null ? -1 : result.longValue();
	}
}
