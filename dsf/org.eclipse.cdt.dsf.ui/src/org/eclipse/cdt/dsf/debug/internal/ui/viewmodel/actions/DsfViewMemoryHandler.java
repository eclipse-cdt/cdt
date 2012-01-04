/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData.BasicType;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode.VariableExpressionVMC;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * DSF version of handler for viewing variable in memory view command.
 *
 */
public class DsfViewMemoryHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			List<Object> list = new ArrayList<Object>();
			Iterator<?> iter = ((IStructuredSelection)selection).iterator();
            while (iter.hasNext()) {
            	Object obj = iter.next();
            	if (obj instanceof VariableExpressionVMC) {
            		list.add(obj);
            	}
            }
            showInMemoryView(list.toArray(new VariableExpressionVMC[list.size()]));
		} 
		return null;
	}

	private void addDefaultRenderings(IMemoryBlock memoryBlock, IMemoryRenderingSite memRendSite) {

		// This method was mostly lifted from the platform's AddMemoryBlockAction

		IMemoryRenderingType primaryType = DebugUITools.getMemoryRenderingManager().getPrimaryRenderingType(memoryBlock);
		IMemoryRenderingType renderingTypes[] = DebugUITools.getMemoryRenderingManager().getDefaultRenderingTypes(memoryBlock);

		try {
			if (primaryType != null) {
				createRenderingInContainer(memoryBlock, memRendSite,
						primaryType, IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
			} else if (renderingTypes.length > 0) {
				primaryType = renderingTypes[0];
				createRenderingInContainer(memoryBlock, memRendSite,
						renderingTypes[0],
						IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
			}
		} catch (CoreException e) {
			DsfUIPlugin.logErrorMessage(e.getMessage());
		}

		for (int i = 0; i < renderingTypes.length; i++) {
			try {
				boolean create = true;
				if (primaryType != null) {
					if (primaryType.getId().equals(renderingTypes[i].getId()))
						create = false;
				}
				if (create)
					createRenderingInContainer(memoryBlock, memRendSite,
							renderingTypes[i],
							IDebugUIConstants.ID_RENDERING_VIEW_PANE_2);
			} catch (CoreException e) {
				DsfUIPlugin.logErrorMessage(e.getMessage());
			}
		}
	}

	private void createRenderingInContainer(IMemoryBlock memoryBlock,
			IMemoryRenderingSite memRendSite, IMemoryRenderingType primaryType,
			String paneId) throws CoreException {

		// This method was mostly lifted from the platform's AddMemoryBlockAction

		IMemoryRendering rendering = primaryType.createRendering();
		IMemoryRenderingContainer container = memRendSite.getContainer(paneId);
		rendering.init(container, memoryBlock);
		container.addMemoryRendering(rendering);
	}

	private void renderMemoryBlock(IMemoryBlockExtension memBlock, IMemoryRenderingSite memRendSite) {
		IMemoryBlock[] memArray = new IMemoryBlock[] { memBlock };
		DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(memArray);
		addDefaultRenderings(memBlock, memRendSite);
	}

	private IStatus showExpressionInMemoryView(VariableExpressionVMC context, IExpressionDMData exprData, 
			IMemoryRenderingSite memRendSite) {
			BasicType type = exprData.getBasicType();
			String exprString;
			if (type == BasicType.array || type == BasicType.pointer) {
				exprString = context.getExpression();
			}
			else {
				exprString = "&(" + context.getExpression() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			try {
			IDMContext dmc = context.getDMContext();
			IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval) context.getAdapter(IMemoryBlockRetrieval.class);
			if (retrieval == null && context instanceof IDebugElement)
				retrieval = ((IDebugElement)context).getDebugTarget();
			if (retrieval == null || !(retrieval instanceof IMemoryBlockRetrievalExtension))
				return Status.OK_STATUS;
			IMemoryBlockRetrievalExtension dsfRetrieval = (IMemoryBlockRetrievalExtension) retrieval;
			IMemoryBlockExtension memBlock = dsfRetrieval.getExtendedMemoryBlock(exprString, dmc);
			renderMemoryBlock(memBlock, memRendSite);
			return Status.OK_STATUS;
		} catch (DebugException e) {
			return DsfUIPlugin.newErrorStatus(IStatus.OK, "Can't view memory on " + exprString, e); //$NON-NLS-1$
		}
	}

	private void showInMemoryView(VariableExpressionVMC contexts[]) {
		try {
			IWorkbenchPage page = DsfUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart memoryView = page.showView(IDebugUIConstants.ID_MEMORY_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
			final IMemoryRenderingSite memRendSite = (IMemoryRenderingSite) memoryView;
			for (final VariableExpressionVMC context : contexts) {
				final IExpressionDMContext dmc = DMContexts.getAncestorOfType(context.getDMContext(), IExpressionDMContext.class);
				if (dmc == null) {
					continue;
				}
				final DsfSession session = DsfSession.getSession(context.getDMContext().getSessionId());
				if (session == null) {
					continue;
				}
				final DsfExecutor executor = session.getExecutor();
				if (executor == null) {
					continue;
				}
				executor.execute(new DsfRunnable() {
					@Override
					public void run() {
						DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
						IExpressions service = tracker.getService(IExpressions.class);
						tracker.dispose();
						if (service != null) {
							service.getExpressionData(dmc, new DataRequestMonitor<IExpressionDMData>(executor, null) {
								@Override
								protected void handleSuccess() {
									final IExpressionDMData exprData = getData();
									if (exprData != null) {
										Job job = new Job("View Memory") { //$NON-NLS-1$
											@Override
											protected IStatus run(IProgressMonitor monitor) {
												return showExpressionInMemoryView(context, exprData, memRendSite);
											}
										};
										job.setSystem(true);
										job.schedule();
									}
								}
							});
						}
					}
				});
			}
		} catch (PartInitException e) {
			DsfUIPlugin.log(e);
		} catch (RejectedExecutionException e) {
			DsfUIPlugin.log(e);
		}
	}

}
