/*******************************************************************************
 * Copyright (c) 2011, 2016 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *     Teodor Madan (Freescale Semiconductor) - Bug 486521: attaching to selected process
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourcesInformation;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;

/**
 * @since 2.4
 */
public class OSResourcesView extends ViewPart implements DsfSession.SessionEndedListener {

	private final static String FETCH_LINK_TAG = "fetch"; //$NON-NLS-1$

	// The data model for the selected session, or null if no session is
	// selected.
	private SessionOSData fSessionData;
	private Map<String, SessionOSData> fSessionDataCache = new HashMap<>();
	// The data presently shown by table viewer.
	private OSData fTableShownData = null;
	// The data which was used to populate column selector menu
	private OSData fMenuShownData = null;
	private String fResourceClass = null;

	// Indicates that we've selected objects from different debug sessions.
	boolean fMultiple = false;
	// Indicates that we have selected object with a wrong type
	boolean fWrongType = false;

	// UI objects
	private TableViewer fViewer;
	private Comparator fComparator;
	private Composite fNothingLabelContainer;
	private Link fNothingLabel;
	private ResourceClassContributionItem fResourceClassEditor;
	private Action fRefreshAction;

	// Map from resource class name to table column layout.
	private Map<String, ColumnLayout> fColumnLayouts = new HashMap<>();

	private ColumnLayout fColumnLayout = null;

	private IDebugContextListener fDebugContextListener;

	@Override
	public void createPartControl(Composite xparent) {

		Composite parent = new Composite(xparent, SWT.NONE);
		GridLayout topLayout = new GridLayout(1, false);
		topLayout.marginWidth = 0;
		topLayout.marginHeight = 0;
		parent.setLayout(topLayout);

		fViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewerData.exclude = true;
		fViewer.getControl().setLayoutData(viewerData);

		fViewer.setComparator(fComparator = new Comparator());

		Table table = fViewer.getTable();
		table.setHeaderVisible(true);
		table.setVisible(false);

		fNothingLabelContainer = new Composite(parent, SWT.NONE);

		GridData nothingLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
		fNothingLabelContainer.setLayoutData(nothingLayout);

		GridLayout containerLayout = new GridLayout(1, false);
		fNothingLabelContainer.setLayout(containerLayout);

		fNothingLabel = new Link(fNothingLabelContainer, SWT.BORDER);
		fNothingLabel.setText(Messages.OSView_4);
		fNothingLabel.setBackground(fNothingLabel.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		fNothingLabel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.text.equals("fetch")) //$NON-NLS-1$
					if (fSessionData != null && getResourceClass() != null)
						fSessionData.fetchData(getResourceClass());
			}
		});
		fNothingLabelContainer.setBackground(fNothingLabel.getBackground());

		GridData nothingLabelLayout = new GridData(SWT.CENTER, SWT.TOP, true, false);
		fNothingLabel.setLayoutData(nothingLabelLayout);

		fResourceClassEditor = new ResourceClassContributionItem();
		fResourceClassEditor.setListener(new ResourceClassContributionItem.Listener() {

			@Override
			public void resourceClassChanged(String newClass) {
				setResourceClass(newClass);
				// Since user explicitly changed the class, initiate fetch immediately.
				fSessionData.fetchData(getResourceClass());
				// Do not call 'update()' here. fetchData call above will notify
				// us at necessary moments.
			}
		});
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(fResourceClassEditor);

		fRefreshAction = new Action() {
			@Override
			public void run() {
				if (fSessionData != null && getResourceClass() != null)
					fSessionData.fetchData(getResourceClass());
			}
		};
		fRefreshAction.setText(Messages.OSView_3);
		fRefreshAction.setToolTipText(Messages.OSView_3);
		try {
			Bundle bundle = Platform.getBundle("org.eclipse.ui"); //$NON-NLS-1$
			URL url = bundle.getEntry("/"); //$NON-NLS-1$
			url = new URL(url, "icons/full/elcl16/refresh_nav.png"); //$NON-NLS-1$
			ImageDescriptor candidate = ImageDescriptor.createFromURL(url);
			if (candidate != null && candidate.getImageData() != null) {
				fRefreshAction.setImageDescriptor(candidate);
			}
		} catch (Exception e) {
		}
		bars.getToolBarManager().add(fRefreshAction);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
		bars.updateActionBars();

		createContextMenu();
		getSite().setSelectionProvider(fViewer);

		setResourceClass(fResourceClassEditor.getResourceClassId());

		setupContextListener();
		DsfSession.addSessionEndedListener(this);

	}

	private void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new CopyAction());
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);

		// register the context menu such that other plug-ins may contribute to it
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	private void setupContextListener() {
		IDebugContextManager contextManager = DebugUITools.getDebugContextManager();
		IDebugContextService contextService = contextManager.getContextService(getSite().getWorkbenchWindow());

		fDebugContextListener = new IDebugContextListener() {

			@Override
			public void debugContextChanged(DebugContextEvent event) {

				if ((event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
					ISelection s = event.getContext();
					setDebugContext(s);
				}
			}
		};
		contextService.addDebugContextListener(fDebugContextListener);
		setDebugContext(contextService.getActiveContext());
	}

	@Override
	public void dispose() {
		super.dispose();

		IDebugContextManager contextManager = DebugUITools.getDebugContextManager();
		IDebugContextService contextService = contextManager.getContextService(getSite().getWorkbenchWindow());
		contextService.removeDebugContextListener(fDebugContextListener);

		setDebugContext((ICommandControlDMContext) null);
		DsfSession.removeSessionEndedListener(this);
	}

	private void setDebugContext(ISelection s) {

		ICommandControlDMContext context = null;
		fMultiple = false;
		fWrongType = false;
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) s;
			if (!ss.isEmpty()) {
				@SuppressWarnings("rawtypes")
				Iterator i = ss.iterator();
				context = getCommandControlContext(i.next());
				if (context == null)
					fWrongType = true;

				while (i.hasNext()) {
					ICommandControlDMContext nextContext = getCommandControlContext(i.next());
					if (nextContext == null)
						fWrongType = true;
					if (nextContext == null && context != null || nextContext != null && context == null
							|| nextContext != null && context != null && !nextContext.equals(context)) {
						context = null;
						fMultiple = true;
						break;
					}
				}
			}
		}

		setDebugContext(context);
	}

	private ICommandControlDMContext getCommandControlContext(Object obj) {
		IDMContext context = null;
		if (obj instanceof IDMVMContext)
			context = ((IDMVMContext) obj).getDMContext();
		else if (obj instanceof GdbLaunch) {
			GdbLaunch l = (GdbLaunch) obj;
			final DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(),
					l.getSession().getId());
			Query<IDMContext> contextQuery = new Query<IDMContext>() {
				@Override
				protected void execute(DataRequestMonitor<IDMContext> rm) {
					ICommandControlService commandControl = tracker.getService(ICommandControlService.class);
					tracker.dispose();
					if (commandControl != null) {
						rm.setData(commandControl.getContext());
					}
					rm.done();
				}
			};
			l.getSession().getExecutor().submit(contextQuery);
			try {
				context = contextQuery.get();
			} catch (Exception e) {
			}
		}
		return DMContexts.getAncestorOfType(context, ICommandControlDMContext.class);
	}

	private void setDebugContext(ICommandControlDMContext context) {
		DsfSession newSession = null;
		SessionOSData newSessionData = null;

		if (context != null) {
			newSession = DsfSession.getSession(context.getSessionId());
		}

		if (newSession != null) {
			newSessionData = fSessionDataCache.get(newSession.getId());
			if (newSessionData == null) {
				newSessionData = new SessionOSData(newSession, context);
				fSessionDataCache.put(newSession.getId(), newSessionData);

				newSessionData.setUIListener(new SessionOSData.Listener() {

					@Override
					public void update() {
						// Note that fSessionData always calls the listener in
						// UI thread, so we can directly call 'update' here.
						OSResourcesView.this.updateSessionDataContents();
					}
				}, fViewer.getControl());
			}
		}

		update(newSessionData);
	}

	@Override
	public void sessionEnded(DsfSession session) {
		String id = session.getId();
		SessionOSData data = fSessionDataCache.remove(id);
		if (data != null) {
			data.dispose();
		}
	}

	// Update UI to showing new session data. If this session data is already
	// shown, does nothing.
	private void update(SessionOSData newSessionData) {
		if (fViewer == null || fViewer.getControl() == null)
			return;

		if (fViewer.getControl().isDisposed())
			return;

		if (newSessionData == null) {
			fSessionData = null;
			if (fMultiple)
				hideTable(Messages.OSView_14);
			else if (fWrongType)
				hideTable(Messages.OSView_4);
			else
				hideTable(Messages.OSView_15);
			fResourceClassEditor.setEnabled(false);
			fRefreshAction.setEnabled(false);
			return;
		}

		if (newSessionData != fSessionData) {
			fSessionData = newSessionData;
			updateSessionDataContents();
		}
	}

	// Update the UI according to actual content of fSessionData,
	// which must be not null.
	private void updateSessionDataContents() {
		if (fSessionData == null)
			return;

		if (fViewer == null || fViewer.getControl() == null)
			return;

		if (fViewer.getControl().isDisposed())
			return;

		boolean enable = fSessionData.canFetchData();
		fRefreshAction.setEnabled(enable);
		setResourceClass(fResourceClassEditor.updateClasses(fSessionData.getResourceClasses()));
		fResourceClassEditor.setEnabled(enable);

		if (!fSessionData.osResourcesSupported()) {
			fRefreshAction.setEnabled(false);
			fResourceClassEditor.setEnabled(false);
			hideTable(Messages.OSView_10);
			return;
		}

		if (fSessionData.waitingForSessionInitialization()) {
			fRefreshAction.setEnabled(false);
			fResourceClassEditor.setEnabled(false);
			hideTable(Messages.OSView_13);
			return;
		}

		if (fSessionData.fetchingClasses()) {
			fRefreshAction.setEnabled(false);
			fResourceClassEditor.setEnabled(false);
			hideTable(Messages.OSView_11);
			return;
		}

		if (getResourceClass() == null) {
			fRefreshAction.setEnabled(false);
			fResourceClassEditor.setEnabled(true);
			hideTable(Messages.OSView_5);
			return;
		}

		final OSData data = fSessionData.existingData(getResourceClass());

		if (fSessionData.fetchingContent()) {
			hideTable(Messages.OSView_6);
		} else if (data == null) {
			if (fSessionData.canFetchData())
				hideTable(NLS.bind(Messages.OSView_7, FETCH_LINK_TAG));
			else
				hideTable(Messages.OSView_12);
		} else {
			SimpleDateFormat format = new SimpleDateFormat(Messages.OSView_8);
			fRefreshAction.setToolTipText(format.format(fSessionData.timestamp(getResourceClass())));
			if (data != fTableShownData) {
				Job job = new UIJob(Messages.OSView_9) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						fTableShownData = data;
						populateTable(data);
						showTable();
						return Status.OK_STATUS;
					}

				};
				job.setPriority(Job.INTERACTIVE);
				job.schedule();
			} else {
				assert fViewer.getTable().getColumnCount() == data.getColumnCount();
				showTable();
			}
		}
		fRefreshAction.setEnabled(fSessionData.canFetchData());
		fResourceClassEditor.setEnabled(fSessionData.canFetchData());

	}

	/* Hide the table that would show OS awareness data if it were available. Display
	 * 'message' instead.
	 */
	private void hideTable(String message) {
		setContentDescription(""); //$NON-NLS-1$
		fViewer.getControl().setVisible(false);
		((GridData) fViewer.getControl().getLayoutData()).exclude = true;
		fNothingLabelContainer.setVisible(true);
		((GridData) fNothingLabelContainer.getLayoutData()).exclude = false;
		fNothingLabelContainer.getParent().layout();
		fNothingLabel.setText(message);
		fNothingLabelContainer.layout();

		// If the table is not shown, we don't want the menu to have stale
		// list of columns to select from.
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().removeAll();
		bars.updateActionBars();
		fMenuShownData = null;
	}

	private void showTable() {
		assert fTableShownData != null;

		fViewer.getControl().setVisible(true);
		((GridData) fViewer.getControl().getLayoutData()).exclude = false;
		fNothingLabelContainer.setVisible(false);
		((GridData) fNothingLabelContainer.getLayoutData()).exclude = true;
		fNothingLabelContainer.getParent().layout();

		populateViewMenu(fTableShownData);
	}

	private void populateTable(final OSData data) {
		final Table table = fViewer.getTable();

		while (table.getColumnCount() > 0)
			table.getColumns()[0].dispose();

		fColumnLayout = fColumnLayouts.get(getResourceClass());
		if (fColumnLayout == null) {
			fColumnLayout = new ColumnLayout(getResourceClass());
			fColumnLayouts.put(getResourceClass(), fColumnLayout);
		}

		for (int i = 0; i < data.getColumnCount(); ++i) {
			final String cn = data.getColumnName(i);
			final TableColumn c = new TableColumn(table, SWT.LEFT);
			c.setText(cn);

			c.addListener(SWT.Resize, new Listener() {

				@Override
				public void handleEvent(Event event) {
					fColumnLayout.setWidth(cn, c.getWidth());
				}
			});

			final int final_index = i;
			c.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int dir = table.getSortDirection();
					if (table.getSortColumn() == c) {
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					} else {
						dir = SWT.DOWN;
					}
					table.setSortDirection(dir);
					table.setSortColumn(c);
					fComparator.configure(final_index, data);
					fComparator.setDirection(dir == SWT.DOWN ? 1 : -1);
					fColumnLayout.setSortColumn(final_index);
					fColumnLayout.setSortDirection(dir == SWT.DOWN ? 1 : -1);
					fViewer.refresh();
				}
			});
		}

		populateViewMenu(data);

		int sortColumn = fColumnLayout.getSortColumn();
		if (sortColumn < data.getColumnCount()) {
			fComparator.configure(sortColumn, data);
		}
		fComparator.setDirection(fColumnLayout.getSortDirection());

		fViewer.getTable().setEnabled(true);

		if (fViewer.getContentProvider() == null) {
			ContentLabelProviderWrapper<OSData> wrapper = new ContentLabelProviderWrapper<>(data);
			fViewer.setContentProvider(wrapper);
			fViewer.setLabelProvider(wrapper);
		} else {
			// Retarget current content/label providers in atomic fashion. See comments
			// on ContentLabelProviderWrapper.
			@SuppressWarnings("unchecked")
			ContentLabelProviderWrapper<OSData> wrapper = (ContentLabelProviderWrapper<OSData>) fViewer
					.getContentProvider();
			wrapper.setData(data);
		}
		fViewer.setInput(getViewSite());
		fViewer.getControl().setVisible(true);

		for (int i = 0; i < fViewer.getTable().getColumnCount(); ++i) {
			TableColumn col = fViewer.getTable().getColumns()[i];
			String cn = col.getText();

			if (i == sortColumn) {
				table.setSortDirection(fColumnLayout.getSortDirection() == 1 ? SWT.DOWN : SWT.UP);
				table.setSortColumn(col);
			}

			if (fColumnLayout.getVisible(cn)) {
				int w = fColumnLayout.getWidth(cn);
				if (w > 0)
					col.setWidth(w);
				else
					col.pack();
			} else {
				col.setWidth(0);
				col.setResizable(false);
			}
		}
	}

	private void populateViewMenu(final OSData data) {

		assert data.getColumnCount() == fViewer.getTable().getColumnCount();

		if (data == fMenuShownData)
			return;

		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().setVisible(true);
		bars.getMenuManager().removeAll();

		for (int i = 0; i < data.getColumnCount(); ++i) {
			final String cn = data.getColumnName(i);
			final TableColumn c = fViewer.getTable().getColumns()[i];

			Action a = new Action(cn, IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					if (isChecked()) {
						int w = fColumnLayout.getWidth(cn);
						if (w > 0)
							c.setWidth(w);
						else
							c.pack();
						c.setResizable(true);
					} else {
						int w = c.getWidth();
						c.setWidth(0);
						// Make sure we remember the width the column
						// had before hiding.
						fColumnLayout.setWidth(cn, w);
						c.setResizable(false);
					}
					fColumnLayout.setVisible(cn, isChecked());
				}
			};
			a.setChecked(fColumnLayout.getVisible(cn));
			a.setText(cn);
			bars.getMenuManager().add(a);
		}
		bars.updateActionBars();

		fMenuShownData = data;
	}

	class Comparator extends ViewerComparator {
		private int fColumn = 0;
		private OSData fData;
		private boolean fInteger = false;
		private int fDirection = 1;

		public void configure(int column, OSData data) {
			fColumn = column;
			fData = data;
			fInteger = data.getColumnIsInteger(column);
		}

		public void setDirection(int direction) {
			assert direction == 1 || direction == -1;
			fDirection = direction;
		}

		@Override
		public int compare(Viewer viewer, Object xe1, Object xe2) {

			String v1 = fData.getColumnText(xe1, fColumn);
			String v2 = fData.getColumnText(xe2, fColumn);
			if (fInteger) {
				Integer i1 = Integer.parseInt(v1);
				Integer i2 = Integer.parseInt(v2);
				return fDirection * (i1 - i2);
			} else {
				return fDirection * (v1.compareTo(v2));
			}
		}
	}

	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	/**
	 * @return the currently selected and displayed resource class
	 */
	public String getResourceClass() {
		return fResourceClass;
	}

	/**
	 * @param resourceClass the resource class to set
	 */
	private void setResourceClass(String resourceClass) {
		fResourceClass = resourceClass;
	}

	/**
	 * @return currently debug context for which resources are displayed
	 */
	public ICommandControlDMContext getSessionContext() {
		return fSessionData != null ? fSessionData.getContext() : null;
	}

	/**
	 * Retargetted copy to clipboard action
	 */
	private final class CopyAction extends Action {
		private static final char COLUMN_SEPARATOR = ',';
		private final String EOL_CHAR = System.getProperty("line.separator"); //$NON-NLS-1$

		private CopyAction() {
			setText(Messages.OSView_CopyAction);
			setImageDescriptor(
					PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		}

		@Override
		public boolean isEnabled() {
			return !fViewer.getSelection().isEmpty();
		}

		@Override
		public void run() {
			ISelection selection = fViewer.getSelection();
			if (selection.isEmpty())
				return;

			if (selection instanceof IStructuredSelection) {
				@SuppressWarnings("unchecked")
				OSData data = ((ContentLabelProviderWrapper<OSData>) fViewer.getContentProvider()).getData();
				StringBuilder exportStr = new StringBuilder();
				for (Object elmnt : ((IStructuredSelection) selection).toList()) {
					assert elmnt instanceof IResourcesInformation;
					if (elmnt instanceof IResourcesInformation) {
						IResourcesInformation ri = (IResourcesInformation) elmnt;
						exportStr.append(data.getColumnText(ri, 0));
						for (int i = 1; i < data.getColumnCount(); i++) {
							exportStr.append(COLUMN_SEPARATOR).append(data.getColumnText(ri, i));
						}
						exportStr.append(EOL_CHAR);
					}
				}
				Clipboard cb = new Clipboard(Display.getDefault());
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { exportStr.toString() }, new Transfer[] { textTransfer });
			}
		}
	}
}
