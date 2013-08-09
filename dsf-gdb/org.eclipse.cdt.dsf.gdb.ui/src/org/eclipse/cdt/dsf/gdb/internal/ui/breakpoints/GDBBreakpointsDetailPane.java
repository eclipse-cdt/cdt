/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.WorkbenchJob;

public class GDBBreakpointsDetailPane implements IDetailPane {

	class DetailJob extends Job {
		
		private IBreakpoints fBreakpointService;
		private IBreakpointDMContext[] fBreakpointDmcs;
		private IProgressMonitor fProgressMonitor;

		public DetailJob(IBreakpoints breakService, IBreakpointDMContext[] bpDmc) {
			super("Compute breakpoint detail"); //$NON-NLS-1$
			fBreakpointService = breakService;
			fBreakpointDmcs = bpDmc;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (fBreakpointDmcs.length == 0) {
				return Status.OK_STATUS;
			}
			DsfSession session = DsfSession.getSession(fBreakpointDmcs[0].getSessionId());
			if (!session.isActive()) {
				return Status.OK_STATUS;
			}
			fProgressMonitor = monitor;
			Query<List<IBreakpointDMData>> query = new Query<List<IBreakpointDMData>>() {

				@Override
				protected void execute(final DataRequestMonitor<List<IBreakpointDMData>> rm) {
					if (fProgressMonitor.isCanceled()) {
						rm.setStatus(Status.CANCEL_STATUS);
						rm.done();
						return;
					}
					
					final CountingRequestMonitor crm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), rm) {

						@Override
						protected void handleSuccess() {
							rm.done();
						}
					};
					crm.setDoneCount(fBreakpointDmcs.length);
					for (IBreakpointDMContext bpDmc : fBreakpointDmcs) {
						
						if (fProgressMonitor.isCanceled()) {
							rm.setStatus(Status.CANCEL_STATUS);
							rm.done();
							return;
						}

						fBreakpointService.getBreakpointDMData(
							bpDmc, 
							new DataRequestMonitor<IBreakpointDMData>(ImmediateExecutor.getInstance(), crm) {
								
								@Override
								protected void handleSuccess() {
									if (rm.getData() == null) {
										rm.setData(new ArrayList<IBreakpoints.IBreakpointDMData>());
									}
									rm.getData().add(getData());
									crm.done();
								};
							});
						
					}
				}
			};
			session.getExecutor().execute(query);
			try {
				List<IBreakpointDMData> list = query.get();
				detailComputed(list.toArray(new IBreakpointDMData[list.size()]));
			}
			catch(InterruptedException e) {
			}
			catch(ExecutionException e) {
			}
			return Status.OK_STATUS;
		}

		private void detailComputed(final IBreakpointDMData[] bpData) {
            if (!fProgressMonitor.isCanceled()) {
                WorkbenchJob setDetail = new WorkbenchJob("set details") { //$NON-NLS-1$
                    @Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (!fProgressMonitor.isCanceled()) {
                			fViewer.setInput(bpData);
                        }
                        return Status.OK_STATUS;
                    }
                };
                setDetail.setSystem(true);
                setDetail.schedule();
            }
		}
	}
	
	private class BreakpointDetailContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			List<BreakpointProperty> list = new ArrayList<BreakpointProperty>();
			if (inputElement instanceof IBreakpointDMData[]) {
				for(IBreakpointDMData data : (IBreakpointDMData[])inputElement) {
					if (data instanceof MIBreakpointDMData) {
						fillBreakpointProperties(list, (MIBreakpointDMData)data);
					}
				}
				return list.toArray(new BreakpointProperty[list.size()]);
			}
			return new Object[0];
		}

		private void fillBreakpointProperties(List<BreakpointProperty> list, MIBreakpointDMData data) {
			list.add(new BreakpointProperty("Id", Integer.toString(data.getNumber())));
			if (data.isPending()) {
				StringBuilder message = new StringBuilder();
				if (data.getMessages() != null) {
					for (String m : data.getMessages()) {
						message.append(m);
						message.append(' ');
					}
					list.add(new BreakpointProperty("Pending", message.toString()));
				}
			}
			if (!data.isPending()) {
				list.add(new BreakpointProperty("Hit count", Integer.toString(data.getHits())));
			}
		}
	}

	private class BreakpointProperty {
		private String fName;
		private String fValue;

		private BreakpointProperty(String name, String value) {
			fName = name;
			fValue = value;
		}

		private String getName() {
			return fName;
		}

		private String getValue() {
			return fValue;
		}
	}

	final static public String ID = "GDBBreakpointsDetailPane";  //$NON-NLS-1$
	final static public String NAME = "Detail Pane For GDB Breakpoints";
	final static public String DESCRIPTION = "Displays the details of the target breakpoint";

	private Composite fControlParent;
	private TableViewer fViewer;

    private DetailJob fDetailJob = null;

	@Override
	public void init(IWorkbenchPartSite partSite) {
		IDebugContextService service = DebugUITools.getDebugContextManager().getContextService(partSite.getWorkbenchWindow());
		
	}

	@Override
	public Control createControl(Composite parent) {
		fControlParent = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		fControlParent.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		fControlParent.setLayoutData(gd);

		fViewer = new TableViewer(fControlParent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		final Table table = fViewer.getTable();
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gd);

		table.setLinesVisible(true);

		TableViewerColumn nameColumn = new TableViewerColumn(fViewer, SWT.LEFT);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BreakpointProperty) {
					return ((BreakpointProperty)element).getName();
				}
				return super.getText(element);
			}
			
		});

		TableViewerColumn valueColumn = new TableViewerColumn(fViewer, SWT.LEFT);
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BreakpointProperty) {
					return ((BreakpointProperty)element).getValue();
				}
				return super.getText(element);
			}
			
		});

		fViewer.setContentProvider(new BreakpointDetailContentProvider());

		table.addControlListener(new ControlAdapter() {
			
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = table.getClientArea();
				if (area.width > 0) {
					TableColumn[] cols = table.getColumns();
					cols[0].setWidth(area.width * 20 / 100);
					cols[1].setWidth(area.width * 80 / 100 );
					table.removeControlListener(this);
				}
			}
		});

		return fControlParent;
	}

	@Override
	public void dispose() {
		if (fDetailJob != null) {
			fDetailJob.cancel();
			fDetailJob = null;
		}
		fControlParent.dispose();
	}

	@Override
	public void display(IStructuredSelection selection) {
		if (selection.size() == 1 && selection.getFirstElement() instanceof ICBreakpoint) {
			ICBreakpoint breakpoint = (ICBreakpoint)selection.getFirstElement();
			IAdaptable dc = DebugUITools.getDebugContext();
			if (dc instanceof IDMVMContext) {
				IBreakpointsTargetDMContext btDmc = 
					DMContexts.getAncestorOfType(((IDMVMContext)dc).getDMContext(), IBreakpointsTargetDMContext.class);
				if (btDmc != null) {
					DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), btDmc.getSessionId());
					MIBreakpointsManager bm = tracker.getService(MIBreakpointsManager.class);
					IBreakpoints breakService = tracker.getService(IBreakpoints.class);
					tracker.dispose();
					if (bm == null || breakService == null) {
						// TODO: display error message
						return;
					}
					IBreakpointDMContext[] targetBps = bm.getTargetBreakpoints(btDmc, breakpoint);
					if (targetBps.length > 0) {
				        synchronized(this) {
				            if (fDetailJob != null) {
				                fDetailJob.cancel();
				            }
				            fDetailJob = new DetailJob(breakService, targetBps);
				            fDetailJob.schedule();
				        }
						
					}
					
				}
			}
			
		}
	}

	@Override
	public boolean setFocus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

}
