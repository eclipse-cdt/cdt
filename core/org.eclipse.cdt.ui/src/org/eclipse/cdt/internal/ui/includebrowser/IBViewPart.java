/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Ed Swartz (Nokia)
 *     Martin Oberhuber (Wind River) - bug 398195: consider external API in IB
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.CollapseAllAction;
import org.eclipse.cdt.internal.ui.actions.CopyTreeAction;
import org.eclipse.cdt.internal.ui.actions.ExpandAllAction;
import org.eclipse.cdt.internal.ui.navigator.OpenCElementAction;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.EditorOpener;
import org.eclipse.cdt.internal.ui.viewsupport.ExtendedTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.internal.ui.viewsupport.TreeNavigator;
import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

/**
 * The view part for the include browser.
 */
public class IBViewPart extends ViewPart implements IShowInSource, IShowInTarget, IShowInTargetList {
	private static final int MAX_HISTORY_SIZE = 10;
	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$
	private static final String KEY_WORKING_SET_FILTER = "workingSetFilter"; //$NON-NLS-1$
	private static final String KEY_FILTER_SYSTEM = "systemFilter"; //$NON-NLS-1$
	private static final String KEY_FILTER_INACTIVE = "inactiveFilter"; //$NON-NLS-1$
	private static final String KEY_INPUT_PATH = "inputPath"; //$NON-NLS-1$
	private static final String KEY_INCLUDED_BY = "includedBy"; //$NON-NLS-1$
	private static final String KEY_SHOW_FOLDERS = "showFolders"; //$NON-NLS-1$

	private IMemento fMemento;
	private boolean fShowsMessage;
	private IBNode fLastNavigationNode;
	private ArrayList<ITranslationUnit> fHistoryEntries = new ArrayList<>(MAX_HISTORY_SIZE);

	// widgets
	private PageBook fPagebook;
	private Composite fViewerPage;
	private Label fInfoText;

	// treeviewer
	private IBContentProvider fContentProvider;
	private IBLabelProvider fLabelProvider;
	private ExtendedTreeViewer fTreeViewer;

	// filters, sorter
	private IBWorkingSetFilter fWorkingSetFilter;
	private ViewerFilter fInactiveFilter;
	private ViewerFilter fSystemFilter;
	private ViewerComparator fSorterAlphaNumeric;
	private ViewerComparator fSorterReferencePosition;

	// actions
	private Action fIncludedByAction;
	private Action fIncludesToAction;
	private Action fFilterInactiveAction;
	private Action fFilterSystemAction;
	private Action fShowFolderInLabelsAction;
	private Action fNextAction;
	private Action fPreviousAction;
	private Action fRefreshAction;
	private Action fHistoryAction;
	private Action fRemoveFromViewAction;
	private CopyTreeAction fCopyAction;
	private IContextActivation fContextActivation;
	private WorkingSetFilterUI fWorkingSetFilterUI;
	private IBSetInputJob fSetInputJob;

	private IBindingService bindingService;
	private IBindingManagerListener bindingManagerListener;

	@Override
	public void setFocus() {
		fPagebook.setFocus();
	}

	public void setMessage(String msg) {
		fInfoText.setText(msg);
		fPagebook.showPage(fInfoText);
		fShowsMessage = true;
		updateActionEnablement();
		updateDescription();
	}

	public void setInput(ITranslationUnit input) {
		if (fPagebook.isDisposed()) {
			return;
		}
		if (input instanceof IWorkingCopy) {
			input = ((IWorkingCopy) input).getOriginalElement();
		}
		fSetInputJob.cancel();
		if (input == null) {
			setMessage(IBMessages.IBViewPart_instructionMessage);
			fTreeViewer.setInput(null);
			return;
		}

		if (CCorePlugin.getIndexManager().isIndexerIdle()) {
			setInputIndexerIdle(input);
		} else {
			setMessage(IBMessages.IBViewPart_waitingOnIndexerMessage);
			fSetInputJob.setInput(input);
			fSetInputJob.schedule();
		}
	}

	private void setInputIndexerIdle(final ITranslationUnit input) {
		fShowsMessage = false;
		boolean isHeader = input.isHeaderUnit();

		fTreeViewer.setInput(null);
		if (!isHeader) {
			fContentProvider.setComputeIncludedBy(isHeader);
			fIncludedByAction.setChecked(isHeader);
			fIncludesToAction.setChecked(!isHeader);
			fIncludedByAction.setEnabled(false);
			updateSorter();
		} else {
			fIncludedByAction.setEnabled(true);
		}
		fTreeViewer.setInput(input);
		fPagebook.showPage(fViewerPage);
		updateHistory(input);

		updateActionEnablement();
		updateDescription();
		final Display display = Display.getCurrent();
		Job job = new Job(IBMessages.IBViewPart_jobCheckInput) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
					IIndex index = CCorePlugin.getIndexManager().getIndex(projects,
							IIndexManager.ADD_EXTENSION_FRAGMENTS_INCLUDE_BROWSER);
					index.acquireReadLock();
					try {
						if (!IndexUI.isIndexed(index, input)) {
							// Bug 306879: Try to find an alternative translation unit for the file by the location.
							final ITranslationUnit alt = CoreModelUtil
									.findTranslationUnitForLocation(input.getLocation(), input.getCProject());
							if (alt != null && IndexUI.isIndexed(index, alt)) {
								display.asyncExec(() -> {
									if (fTreeViewer.getInput() == input) {
										setInput(alt);
									}
								});
							} else {
								final String msg = IndexUI.getFileNotIndexedMessage(input);
								display.asyncExec(() -> {
									if (fTreeViewer.getInput() == input) {
										setMessage(msg);
										fTreeViewer.setInput(null);
									}
								});
							}
						}
						return Status.OK_STATUS;
					} finally {
						index.releaseReadLock();
					}
				} catch (CoreException e) {
					return Status.OK_STATUS;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private void updateActionEnablement() {
		fHistoryAction.setEnabled(!fHistoryEntries.isEmpty());
		fNextAction.setEnabled(!fShowsMessage);
		fPreviousAction.setEnabled(!fShowsMessage);
		fRefreshAction.setEnabled(!fShowsMessage);
	}

	@Override
	public void createPartControl(Composite parent) {
		fSetInputJob = new IBSetInputJob(this, Display.getCurrent());

		fPagebook = new PageBook(parent, SWT.NULL);
		createInfoPage();
		createViewerPage();

		initDragAndDrop();
		createActions();
		createContextMenu();

		bindingService = PlatformUI.getWorkbench().getService(IBindingService.class);
		if (bindingService != null) {
			bindingManagerListener = event -> {
				if (event.isActiveBindingsChanged()) {
					String keyBinding = bindingService
							.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.EDIT_DELETE);
					if (keyBinding != null) {
						fRemoveFromViewAction.setText(IBMessages.IBViewPart_RemoveFromView_label + '\t' + keyBinding);
					}
				}
			};
			bindingService.addBindingManagerListener(bindingManagerListener);
		}

		getSite().setSelectionProvider(fTreeViewer);
		setMessage(IBMessages.IBViewPart_instructionMessage);

		initializeActionStates();
		restoreInput();
		fMemento = null;

		IContextService ctxService = getSite().getService(IContextService.class);
		if (ctxService != null) {
			fContextActivation = ctxService.activateContext(CUIPlugin.CVIEWS_SCOPE);
		}

		PlatformUI.getWorkbench().getHelpSystem().setHelp(fPagebook, ICHelpContextIds.INCLUDE_BROWSER_VIEW);
	}

	@Override
	public void dispose() {
		putDialogSettings();
		if (fContextActivation != null) {
			IContextService ctxService = getSite().getService(IContextService.class);
			if (ctxService != null) {
				ctxService.deactivateContext(fContextActivation);
			}
		}

		if (bindingService != null) {
			bindingService.removeBindingManagerListener(bindingManagerListener);
			bindingService = null;
		}

		if (fWorkingSetFilterUI != null) {
			fWorkingSetFilterUI.dispose();
		}
	}

	private void initializeActionStates() {
		IDialogSettings ds = getDialogSettings();

		boolean includedBy = !FALSE.equals(ds.get(KEY_INCLUDED_BY));
		fIncludedByAction.setChecked(includedBy);
		fIncludesToAction.setChecked(!includedBy);
		fContentProvider.setComputeIncludedBy(includedBy);

		fFilterInactiveAction.setChecked(TRUE.equals(ds.get(KEY_FILTER_INACTIVE)));
		fFilterInactiveAction.run();
		fFilterSystemAction.setChecked(TRUE.equals(ds.get(KEY_FILTER_SYSTEM)));
		fFilterSystemAction.run();
		fShowFolderInLabelsAction.setChecked(TRUE.equals((ds.get(KEY_SHOW_FOLDERS))));
		fShowFolderInLabelsAction.run();
		updateSorter();
	}

	private void restoreInput() {
		if (fMemento != null) {
			String pathStr = fMemento.getString(KEY_INPUT_PATH);
			if (pathStr != null) {
				IPath path = Path.fromPortableString(pathStr);
				if (path.segmentCount() > 1) {
					String name = path.segment(0);
					ICProject project = CoreModel.getDefault().getCModel().getCProject(name);
					if (project != null) {
						ICElement celement;
						try {
							celement = project.findElement(path);
							if (celement instanceof ITranslationUnit) {
								setInput((ITranslationUnit) celement);
							}
						} catch (CModelException e) {
							// ignore
						}
					}
				}
			}
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		fMemento = memento;
		super.init(site, memento);
	}

	@Override
	public void saveState(IMemento memento) {
		putDialogSettings();
		if (memento != null) {
			if (fWorkingSetFilter != null) {
				fWorkingSetFilter.getUI().saveState(memento, KEY_WORKING_SET_FILTER);
			}
			ITranslationUnit input = getInput();
			if (input != null) {
				IPath path = input.getPath();
				if (path != null) {
					memento.putString(KEY_INPUT_PATH, path.toPortableString());
				}
			}
		}
		super.saveState(memento);
	}

	private void putDialogSettings() {
		IDialogSettings ds = getDialogSettings();
		ds.put(KEY_FILTER_INACTIVE, String.valueOf(fFilterInactiveAction.isChecked()));
		ds.put(KEY_FILTER_SYSTEM, String.valueOf(fFilterSystemAction.isChecked()));
		ds.put(KEY_INCLUDED_BY, String.valueOf(fIncludedByAction.isChecked()));
		ds.put(KEY_SHOW_FOLDERS, String.valueOf(fShowFolderInLabelsAction.isChecked()));
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings ds = CUIPlugin.getDefault().getDialogSettings();
		final String name = IBViewPart.class.getName();
		IDialogSettings result = ds.getSection(name);
		if (result == null) {
			result = ds.addNewSection(name);
		}
		return result;
	}

	private void createContextMenu() {
		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(m -> onContextMenuAboutToShow(m));
		Menu menu = manager.createContextMenu(fTreeViewer.getControl());
		fTreeViewer.getControl().setMenu(menu);
		IWorkbenchPartSite site = getSite();
		site.registerContextMenu(CUIPlugin.ID_INCLUDE_BROWSER, manager, fTreeViewer);
	}

	private void createViewerPage() {
		Display display = getSite().getShell().getDisplay();
		fViewerPage = new Composite(fPagebook, SWT.NULL);
		fViewerPage.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewerPage.setSize(100, 100);
		fViewerPage.setLayout(new FillLayout());

		fContentProvider = new IBContentProvider(display);
		fLabelProvider = new IBLabelProvider(display, fContentProvider);
		fTreeViewer = new ExtendedTreeViewer(fViewerPage);
		fTreeViewer.setUseHashlookup(true);
		fTreeViewer.setContentProvider(fContentProvider);
		fTreeViewer.setLabelProvider(fLabelProvider);
		fTreeViewer.setAutoExpandLevel(2);
		fTreeViewer.addOpenListener(event -> onShowInclude(event.getSelection()));
	}

	private void createInfoPage() {
		fInfoText = new Label(fPagebook, SWT.TOP | SWT.LEFT | SWT.WRAP);
	}

	private void initDragAndDrop() {
		IBDropTargetListener dropListener = new IBDropTargetListener(this);
		Transfer[] dropTransfers = new Transfer[] { LocalSelectionTransfer.getTransfer(),
				ResourceTransfer.getInstance(), FileTransfer.getInstance() };
		DropTarget dropTarget = new DropTarget(fPagebook,
				DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT);
		dropTarget.setTransfer(dropTransfers);
		dropTarget.addDropListener(dropListener);

		Transfer[] dragTransfers = new Transfer[] { ResourceTransfer.getInstance(), FileTransfer.getInstance() };
		IBDragSourceListener dragListener = new IBDragSourceListener(fTreeViewer);
		dragListener.setDependentDropTargetListener(dropListener);
		fTreeViewer.addDragSupport(DND.DROP_COPY, dragTransfers, dragListener);
	}

	private void createActions() {
		fWorkingSetFilterUI = new WorkingSetFilterUI(this, fMemento, KEY_WORKING_SET_FILTER) {
			@Override
			protected void onWorkingSetChange() {
				updateWorkingSetFilter(this);
			}

			@Override
			protected void onWorkingSetNameChange() {
				updateDescription();
			}
		};

		fIncludedByAction = new Action(IBMessages.IBViewPart_showIncludedBy_label, IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				if (isChecked()) {
					onSetDirection(true);
				}
			}
		};
		fIncludedByAction.setToolTipText(IBMessages.IBViewPart_showIncludedBy_tooltip);
		CPluginImages.setImageDescriptors(fIncludedByAction, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_REF_BY);

		fIncludesToAction = new Action(IBMessages.IBViewPart_showIncludesTo_label, IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				if (isChecked()) {
					onSetDirection(false);
				}
			}
		};
		fIncludesToAction.setToolTipText(IBMessages.IBViewPart_showIncludesTo_tooltip);
		CPluginImages.setImageDescriptors(fIncludesToAction, CPluginImages.T_LCL,
				CPluginImages.IMG_ACTION_SHOW_RELATES_TO);

		fInactiveFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IBNode) {
					IBNode node = (IBNode) element;
					return node.isActiveCode();
				}
				return true;
			}
		};
		fFilterInactiveAction = new Action(IBMessages.IBViewPart_hideInactive_label, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (isChecked()) {
					fTreeViewer.addFilter(fInactiveFilter);
				} else {
					fTreeViewer.removeFilter(fInactiveFilter);
				}
			}
		};
		fFilterInactiveAction.setToolTipText(IBMessages.IBViewPart_hideInactive_tooltip);
		CPluginImages.setImageDescriptors(fFilterInactiveAction, CPluginImages.T_LCL,
				CPluginImages.IMG_ACTION_HIDE_INACTIVE);

		fSystemFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IBNode) {
					IBNode node = (IBNode) element;
					return !node.isSystemInclude();
				}
				return true;
			}
		};
		fFilterSystemAction = new Action(IBMessages.IBViewPart_hideSystem_label, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (isChecked()) {
					fTreeViewer.addFilter(fSystemFilter);
				} else {
					fTreeViewer.removeFilter(fSystemFilter);
				}
			}
		};
		fFilterSystemAction.setToolTipText(IBMessages.IBViewPart_hideSystem_tooltip);
		CPluginImages.setImageDescriptors(fFilterSystemAction, CPluginImages.T_LCL,
				CPluginImages.IMG_ACTION_HIDE_SYSTEM);

		fSorterAlphaNumeric = new ViewerComparator();
		fSorterReferencePosition = new ViewerComparator() {
			@Override
			public int category(Object element) {
				if (element instanceof IBNode) {
					return 0;
				}
				return 1;
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (!(e1 instanceof IBNode)) {
					if (!(e2 instanceof IBNode)) {
						return 0;
					}
					return -1;
				}
				if (!(e2 instanceof IBNode)) {
					return 1;
				}
				IBNode n1 = (IBNode) e1;
				IBNode n2 = (IBNode) e2;
				return n1.getDirectiveCharacterOffset() - n2.getDirectiveCharacterOffset();
			}
		};

		fShowFolderInLabelsAction = new Action(IBMessages.IBViewPart_showFolders_label, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				onShowFolderInLabels(isChecked());
			}
		};
		fShowFolderInLabelsAction.setToolTipText(IBMessages.IBViewPart_showFolders_tooltip);
		fNextAction = new Action(IBMessages.IBViewPart_nextMatch_label) {
			@Override
			public void run() {
				onNextOrPrevious(true);
			}
		};
		fNextAction.setToolTipText(IBMessages.IBViewPart_nextMatch_tooltip);
		CPluginImages.setImageDescriptors(fNextAction, CPluginImages.T_LCL, CPluginImages.IMG_SHOW_NEXT);

		fPreviousAction = new Action(IBMessages.IBViewPart_previousMatch_label) {
			@Override
			public void run() {
				onNextOrPrevious(false);
			}
		};
		fPreviousAction.setToolTipText(IBMessages.IBViewPart_previousMatch_tooltip);
		CPluginImages.setImageDescriptors(fPreviousAction, CPluginImages.T_LCL, CPluginImages.IMG_SHOW_PREV);

		fRefreshAction = new Action(IBMessages.IBViewPart_refresh_label) {
			@Override
			public void run() {
				onRefresh();
			}
		};
		fRefreshAction.setToolTipText(IBMessages.IBViewPart_refresh_tooltip);
		CPluginImages.setImageDescriptors(fRefreshAction, CPluginImages.T_LCL, CPluginImages.IMG_REFRESH);

		fHistoryAction = new IBHistoryDropDownAction(this);
		ExpandAllAction expandAll = new ExpandAllAction(getTreeViewer());
		CollapseAllAction collapseAll = new CollapseAllAction(getTreeViewer());

		fCopyAction = new CopyCallHierarchyAction(this, fTreeViewer);
		fRemoveFromViewAction = new IBRemoveFromView(this);

		// setup action bar
		// global action hooks
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAction);
		actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAction);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
		actionBars.updateActionBars();

		TextActionHandler textActionHandler = new TextActionHandler(actionBars);
		textActionHandler.setDeleteAction(fRemoveFromViewAction);

		// local toolbar
		IToolBarManager tm = actionBars.getToolBarManager();
		tm.add(fNextAction);
		tm.add(fPreviousAction);
		tm.add(new Separator());
		tm.add(expandAll);
		tm.add(collapseAll);
		tm.add(new Separator());
		tm.add(fFilterSystemAction);
		tm.add(fFilterInactiveAction);
		tm.add(new Separator());
		tm.add(fIncludedByAction);
		tm.add(fIncludesToAction);
		tm.add(fHistoryAction);
		tm.add(fRefreshAction);

		// local menu
		IMenuManager mm = actionBars.getMenuManager();

		fWorkingSetFilterUI.fillActionBars(actionBars);
		mm.add(fIncludedByAction);
		mm.add(fIncludesToAction);
		mm.add(new Separator());
		mm.add(fShowFolderInLabelsAction);
		mm.add(new Separator());
		mm.add(fFilterSystemAction);
		mm.add(fFilterInactiveAction);
	}

	private IBNode getNextNode(boolean forward) {
		TreeNavigator navigator = new TreeNavigator(fTreeViewer.getTree(), IBNode.class);
		TreeItem selectedItem = navigator.getSelectedItemOrFirstOnLevel(1, forward);
		if (selectedItem == null) {
			return null;
		}

		if (selectedItem.getData().equals(fLastNavigationNode)) {
			selectedItem = navigator.getNextSibbling(selectedItem, forward);
		}

		return selectedItem == null ? null : (IBNode) selectedItem.getData();
	}

	protected void onNextOrPrevious(boolean forward) {
		IBNode nextItem = getNextNode(forward);
		if (nextItem != null) {
			StructuredSelection sel = new StructuredSelection(nextItem);
			fTreeViewer.setSelection(sel);
			onShowInclude(sel);
		}
	}

	protected void onRefresh() {
		fContentProvider.recompute();
	}

	protected void onShowFolderInLabels(boolean show) {
		fLabelProvider.setShowFolders(show);
		fTreeViewer.refresh();
	}

	private void updateHistory(ITranslationUnit input) {
		if (input != null) {
			fHistoryEntries.remove(input);
			fHistoryEntries.add(0, input);
			if (fHistoryEntries.size() > MAX_HISTORY_SIZE) {
				fHistoryEntries.remove(MAX_HISTORY_SIZE - 1);
			}
		}
	}

	private void updateSorter() {
		if (fIncludedByAction.isChecked()) {
			fTreeViewer.setComparator(fSorterAlphaNumeric);
		} else {
			fTreeViewer.setComparator(fSorterReferencePosition);
		}
	}

	private void updateDescription() {
		String message = ""; //$NON-NLS-1$
		if (!fShowsMessage) {
			ITranslationUnit tu = getInput();
			if (tu != null) {
				IPath path = tu.getPath();
				if (path != null) {
					String format, file, scope;

					file = path.lastSegment() + "(" + path.removeLastSegments(1) + ")"; //$NON-NLS-1$//$NON-NLS-2$
					if (fWorkingSetFilter == null) {
						scope = IBMessages.IBViewPart_workspaceScope;
					} else {
						scope = fWorkingSetFilter.getLabel();
					}

					if (fIncludedByAction.isChecked()) {
						format = IBMessages.IBViewPart_IncludedByContentDescription;
					} else {
						format = IBMessages.IBViewPart_IncludesToContentDescription;
					}
					message = Messages.format(format, file, scope);
				}
			}
		}
		setContentDescription(message);
	}

	private void updateWorkingSetFilter(WorkingSetFilterUI filterUI) {
		if (filterUI.getWorkingSet() == null) {
			if (fWorkingSetFilter != null) {
				fTreeViewer.removeFilter(fWorkingSetFilter);
				fWorkingSetFilter = null;
			}
		} else {
			if (fWorkingSetFilter != null) {
				fTreeViewer.refresh();
			} else {
				fWorkingSetFilter = new IBWorkingSetFilter(filterUI);
				fTreeViewer.addFilter(fWorkingSetFilter);
			}
		}
	}

	public void onSetDirection(boolean includedBy) {
		if (includedBy != fContentProvider.getComputeIncludedBy()) {
			Object input = fTreeViewer.getInput();
			fTreeViewer.setInput(null);
			fContentProvider.setComputeIncludedBy(includedBy);
			updateSorter();
			fTreeViewer.setInput(input);
			updateDescription();
		}
	}

	protected void onContextMenuAboutToShow(IMenuManager m) {
		final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
		final IBNode node = IBConversions.selectionToNode(selection);

		if (node != null) {
			final IWorkbenchPage page = getSite().getPage();

			// open include
			if (node.getParent() != null && node.getDirectiveFile() != null) {
				m.add(new Action(IBMessages.IBViewPart_showInclude_label) {
					@Override
					public void run() {
						onShowInclude(selection);
					}
				});
			}

			final ITranslationUnit tu = node.getRepresentedTranslationUnit();
			if (tu != null) {
				// open
				OpenCElementAction ofa = new OpenCElementAction(page);
				ofa.selectionChanged(selection);
				m.add(ofa);

				// show in
				IMenuManager submenu = new MenuManager(IBMessages.IBViewPart_ShowInMenu_label);
				submenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getSite().getWorkbenchWindow()));
				m.add(submenu);
				if (node.getParent() != null) {
					m.add(new Action(Messages.format(IBMessages.IBViewPart_FocusOn_label, tu.getPath().lastSegment())) {
						@Override
						public void run() {
							setInput(tu);
						}
					});
				}
			}

			m.add(new Separator(IContextMenuConstants.GROUP_EDIT));
			if (fCopyAction.canActionBeAdded()) {
				m.appendToGroup(ICommonMenuConstants.GROUP_EDIT, fCopyAction);
			}
			m.appendToGroup(ICommonMenuConstants.GROUP_EDIT, fRemoveFromViewAction);

		}
		m.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
	}

	protected void onShowInclude(ISelection selection) {
		IBNode node = IBConversions.selectionToNode(selection);
		if (node != null) {
			fLastNavigationNode = node;

			IWorkbenchPage page = getSite().getPage();
			IBFile ibf = node.getDirectiveFile();
			if (ibf != null) {
				IRegion region = new Region(node.getDirectiveCharacterOffset(), node.getDirectiveLength());
				long timestamp = node.getTimestamp();

				IFile f = ibf.getResource();
				if (f != null) {
					EditorOpener.open(page, f, region, timestamp);
				} else {
					IIndexFileLocation ifl = ibf.getLocation();
					if (ifl != null) {
						IPath location = IndexLocationFactory.getAbsolutePath(ifl);
						if (location != null) {
							EditorOpener.openExternalFile(page, location, region, timestamp);
						}
					}
				}
			} else {
				ITranslationUnit tu = IBConversions.selectionToTU(selection);
				if (tu != null) {
					IResource r = tu.getResource();
					if (r != null) {
						OpenFileAction ofa = new OpenFileAction(page);
						ofa.selectionChanged((IStructuredSelection) selection);
						ofa.run();
					}
				}
			}
		}
	}

	@Override
	public ShowInContext getShowInContext() {
		return new ShowInContext(null, IBConversions.nodeSelectionToRepresentedTUSelection(fTreeViewer.getSelection()));
	}

	@Override
	public boolean show(ShowInContext context) {
		ITranslationUnit tu = IBConversions.selectionToTU(context.getSelection());
		if (tu == null) {
			tu = IBConversions.objectToTU(context.getInput());
			if (tu == null) {
				setMessage(IBMessages.IBViewPart_falseInputMessage);
				return false;
			}
		}

		setInput(tu);
		return true;
	}

	@Override
	public String[] getShowInTargetIds() {
		return new String[] { ProjectExplorer.VIEW_ID, IPageLayout.ID_PROJECT_EXPLORER };
	}

	// access for tests
	public TreeViewer getTreeViewer() {
		return fTreeViewer;
	}

	private static class CopyCallHierarchyAction extends CopyTreeAction {
		public CopyCallHierarchyAction(ViewPart view, TreeViewer viewer) {
			super(IBMessages.IBViewPart_CopyIncludeHierarchy_label, view, viewer);
		}
	}

	public ITranslationUnit[] getHistoryEntries() {
		return fHistoryEntries.toArray(new ITranslationUnit[fHistoryEntries.size()]);
	}

	public void setHistoryEntries(ITranslationUnit[] remaining) {
		fHistoryEntries.clear();
		fHistoryEntries.addAll(Arrays.asList(remaining));
	}

	public ITranslationUnit getInput() {
		Object input = fTreeViewer.getInput();
		if (input instanceof ITranslationUnit) {
			return (ITranslationUnit) input;
		}
		return null;
	}
}
