/*******************************************************************************
 * Copyright (c) 2010, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.cfgview.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.cxx.internal.model.cfg.ControlFlowGraphBuilder;
import org.eclipse.cdt.codan.core.cxx.internal.model.cfg.CxxControlFlowGraph;
import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.ICfgData;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IJumpNode;
import org.eclipse.cdt.codan.core.model.cfg.ISingleOutgoing;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;
import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.ui.cfgview.ControlFlowGraphPlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.text.SharedASTJob;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */
@SuppressWarnings("restriction")
public class ControlFlowGraphView extends ViewPart {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.eclipse.cdt.codan.ui.cfgview.views.ControlFlowGraphView";
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action actionSync;

	class DeadNodes extends ArrayList<IBasicBlock> {
	}

	class ViewContentProvider implements ITreeContentProvider {
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		@Override
		public Object getParent(Object child) {
			return null;
		}

		@Override
		public Object[] getChildren(Object parent) {
			if (parent instanceof Collection) {
				return ((Collection<?>) parent).toArray();
			} else if (parent instanceof IControlFlowGraph) {
				IControlFlowGraph cfg = (IControlFlowGraph) parent;
				Collection<IBasicBlock> blocks = getFlat(cfg.getStartNode(), new ArrayList<IBasicBlock>());
				DeadNodes dead = new DeadNodes();
				Iterator<IBasicBlock> iter = cfg.getUnconnectedNodeIterator();
				for (; iter.hasNext();) {
					IBasicBlock iBasicBlock = iter.next();
					dead.add(iBasicBlock);
				}
				ArrayList<Object> all = new ArrayList<>();
				all.addAll(blocks);
				// labeled statements disjoined from the rest
				for (IBasicBlock node : cfg.getNodes()) {
					if (node instanceof IBranchNode && node.getIncomingSize() == 0 && !dead.contains(node))
						all.add(node);
				}
				if (dead.size() > 0)
					all.add(dead);
				return all.toArray();
			} else if (parent instanceof IDecisionNode) {
				ArrayList<IBasicBlock> blocks = new ArrayList<>();
				IBasicBlock[] outgoingNodes = ((IDecisionNode) parent).getOutgoingNodes();
				for (int i = 0; i < outgoingNodes.length; i++) {
					IBasicBlock arc = outgoingNodes[i];
					blocks.add(arc);
				}
				blocks.add(((IDecisionNode) parent).getMergeNode());
				return blocks.toArray();
			} else if (parent instanceof IBranchNode) {
				Collection<IBasicBlock> blocks = getFlat(((IBranchNode) parent).getOutgoing(),
						new ArrayList<IBasicBlock>());
				return blocks.toArray();
			}
			return new Object[0];
		}

		@Override
		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}

		/**
		 * @param list
		 * @param startNode
		 * @return
		 */
		public Collection<IBasicBlock> getFlat(IBasicBlock node, Collection<IBasicBlock> list) {
			list.add(node);
			if (node instanceof IJumpNode)
				return list;
			if (node instanceof ISingleOutgoing) {
				getFlat(((ISingleOutgoing) node).getOutgoing(), list);
			} else if (node instanceof IDecisionNode) {
				getFlat(((IDecisionNode) node).getMergeNode().getOutgoing(), list);
			}
			return list;
		}
	}

	class ViewLabelProvider extends LabelProvider {
		@Override
		public String getText(Object obj) {
			if (obj == null)
				return null;
			String strdata = "";
			if (obj instanceof ICfgData) {
				strdata = ((AbstractBasicBlock) obj).toStringData();
			}
			if (obj instanceof IConnectorNode) {
				strdata = blockHexLabel(obj);
			} else if (obj instanceof IJumpNode) {
				strdata = "jump to " + blockHexLabel(((IJumpNode) obj).getJumpNode());
			}
			return obj.getClass().getSimpleName() + ": " + strdata;
		}

		/**
		 * @param obj
		 * @return
		 */
		protected String blockHexLabel(Object obj) {
			return "0x" + Integer.toHexString(System.identityHashCode(obj));
		}

		@Override
		public Image getImage(Object obj) {
			String imageKey = "task.png";
			if (obj instanceof IDecisionNode || obj instanceof IControlFlowGraph)
				imageKey = "decision.png";
			else if (obj instanceof IExitNode)
				imageKey = "exit.png";
			else if (obj instanceof IStartNode)
				imageKey = "start.png";
			else if (obj instanceof IJumpNode)
				imageKey = "jump.png";
			else if (obj instanceof IBranchNode)
				imageKey = "labeled.png";
			else if (obj instanceof IConnectorNode)
				imageKey = "connector.png";
			return ControlFlowGraphPlugin.getDefault().getImage("icons/" + imageKey);
		}
	}

	/**
	 * The constructor.
	 */
	public ControlFlowGraphView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookSingleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ControlFlowGraphView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionSync);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionSync);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionSync);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		actionSync = new Action() {
			@Override
			public void run() {
				IEditorPart e = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				ITranslationUnit tu = (ITranslationUnit) CDTUITools.getEditorInputCElement(e.getEditorInput());
				Job job = new SharedASTJob("Building Control Flow Grath", tu) {
					@Override
					public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
						if (ast == null) {
							return Status.CANCEL_STATUS;
						}
						processAst(ast);
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		};
		actionSync.setText("Synchronize");
		actionSync.setToolTipText("Synchronize");
		actionSync.setImageDescriptor(ControlFlowGraphPlugin.getDefault().getImageDescriptor("icons/refresh_view.gif"));
	}

	protected void processAst(IASTTranslationUnit ast) {
		final ArrayList<IControlFlowGraph> functions = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration decl) {
				if (decl instanceof IASTFunctionDefinition) {
					CxxControlFlowGraph graph = new ControlFlowGraphBuilder().build((IASTFunctionDefinition) decl);
					functions.add(graph);
					return PROCESS_SKIP;
				}
				return PROCESS_CONTINUE;
			}
		};
		ast.accept(visitor);
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				viewer.setInput(functions);
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private class ASTHighlighterAction extends Action {
		IEditorPart aPart = null;

		public ASTHighlighterAction(IEditorPart part) {
			this.aPart = part;
		}

		protected boolean open(String filename) throws PartInitException, CModelException {
			if (filename.isEmpty())
				return false;
			IResource r = ParserUtil.getResourceForFilename(filename);
			if (r != null) {
				aPart = EditorUtility.openInEditor(r);
			} else {
				IPath path = new Path(filename);
				FileStorage storage = new FileStorage(null, path);
				aPart = EditorUtility.openInEditor(storage);
			}
			return true;
		}

		@Override
		public void run() {
			ISelection selection = viewer.getSelection();
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof ICfgData) {
				Object data = ((ICfgData) obj).getData();
				if (data instanceof IASTNode) {
					IASTNode node = (IASTNode) data;
					if (node instanceof IASTTranslationUnit) // don't
						return;
					IASTFileLocation loc = node.getFileLocation();
					String filename = loc.getFileName();
					try {
						open(filename);
					} catch (PartInitException e) {
						ControlFlowGraphPlugin.log(e);
					} catch (CModelException e) {
						ControlFlowGraphPlugin.log(e);
					}

					if (aPart instanceof AbstractTextEditor) {
						((AbstractTextEditor) aPart).selectAndReveal(loc.getNodeOffset(), loc.getNodeLength());
					}
					// re-activate view
					if (aPart != null) {
						IWorkbenchPage page = aPart.getSite().getPage();
						page.activate(page.findView(ID));
					}
				}
			}
		}
	}

	private void hookSingleClickAction() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				new ASTHighlighterAction(null).run();
			}
		});
	}
}
