/*******************************************************************************
 * Copyright (c) 2005, 2010 Freescale Semiconductor, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval;
import org.eclipse.cdt.debug.internal.core.CMemoryBlockRetrievalExtension;
import org.eclipse.cdt.debug.internal.core.CRequest;
import org.eclipse.cdt.debug.internal.core.model.CRegister;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.debug.internal.ui.views.memory.AddMemoryBlockAction;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IAddMemoryBlocksTarget;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

/**
 * Adds memory blocks to the Memory view.
 * 
 * <p>
 * CDT adapter logic will link us to a CMemoryBlockRetrievalExtension  
 * if and only if the CDI backend support memory spaces. When this is the case,
 * the platform will call us to add a memory monitor to the Memory view. We
 * must put up a dialog, handle the user input, create the memory blocks 
 * with default renderings and add them to the view. 
 *   
 * <p>
 * @since 3.2
 *
 */
public class AddMemoryBlocks implements IAddMemoryBlocksTarget {
	
	/** Request object used to get the memory spaces */
	private static class GetMemorySpacesRequest extends CRequest implements IMemorySpaceAwareMemoryBlockRetrieval.GetMemorySpacesRequest  {
		String [] fMemorySpaces = new String[0];
		public String[] getMemorySpaces() {
			return fMemorySpaces;
		}
		public void setMemorySpaces(String[] memorySpaceIds) {
			fMemorySpaces = memorySpaceIds;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IAddMemoryBlocksTarget#addMemoryBlocks(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void addMemoryBlocks(final IWorkbenchPart part, final ISelection selection) throws CoreException {

		if (!(part instanceof IMemoryRenderingSite)) {
			assert false : "unexpected kind of view part"; //$NON-NLS-1$
			return;
		}
		final IMemoryRenderingSite renderingSite = (IMemoryRenderingSite)part;

		final IAdaptable debugViewElement = DebugUITools.getDebugContext();


		IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)debugViewElement.getAdapter(IMemoryBlockRetrieval.class);
		if (retrieval == null && debugViewElement instanceof IDebugElement) {
			// Added logic for CDI (which is based on the standard debug model)
			retrieval = ((IDebugElement)debugViewElement).getDebugTarget();
		}

		// If the backend doesn't support memory spaces, use the standard
		// (platform) dialog for adding a memory block
		if (!(retrieval instanceof IMemorySpaceAwareMemoryBlockRetrieval)) {
			invokePlatformAction(renderingSite);
			return;
		}
		final IMemorySpaceAwareMemoryBlockRetrieval msRetrieval = (IMemorySpaceAwareMemoryBlockRetrieval)retrieval;
		
		// We still don't really know if this session actually involves any
		// memory spaces. Finding out is not trivial since it requires an
		// asynchronous call.
		final Object context = getContextSelectionForPart(part);
		Job job = new Job("update memory space choices") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				msRetrieval.getMemorySpaces(context, new GetMemorySpacesRequest(){
					public void done() {
						runOnUIThread(new Runnable(){
							public void run() {
								if (isSuccess()) {
									String[] memorySpaces = getMemorySpaces();

									// We shouldn't be using the custom dialog 
									// if there are none or only one memory 
									// spaces involved.
									// https://bugs.eclipse.org/bugs/show_bug.cgi?id=309032#c50
									if (memorySpaces.length >= 2) {
										doAddMemoryBlocks(renderingSite, context, msRetrieval, memorySpaces);
										return;
									}
								}

								// If we get here, then the custom dialog isn't 
								// necessary. Use the standard (platform) one
								invokePlatformAction(renderingSite);
							}});
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
	
	/**
	 * Invoke the platform's Add Memory Block action.
	 * 
	 * @param site the rendering site
	 */
	void invokePlatformAction(IMemoryRenderingSite site) {
		// We have to subclass to get access to the protected dispose method
		class PlatformAction extends AddMemoryBlockAction {
			PlatformAction(IMemoryRenderingSite site) {
				super(site);
			}
			@Override 
			protected void dispose() {
				super.dispose();
			}
		};
		PlatformAction action = new PlatformAction(site);
		action.run();
		action.dispose();
	}

	private void doAddMemoryBlocks(final IMemoryRenderingSite renderingSite, final Object context, final IMemorySpaceAwareMemoryBlockRetrieval retrieval, String[] memorySpaces) {		
		Shell shell = CDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();

		// create dialog to ask for expression/address to block
		AddMemoryBlockDialog dialog = new AddMemoryBlockDialog(shell, memorySpaces);
		dialog.open();
		int returnCode = dialog.getReturnCode();
		if (returnCode == Window.CANCEL)
			return;

		String input = dialog.enteredExpression() ? dialog.getExpression() : dialog.getAddress();

		List<String> list = new ArrayList<String>();

		if (input.length() == 0)
			list.add(""); //$NON-NLS-1$
		else {
			StringTokenizer tokenizer = new StringTokenizer(input, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens())
				list.add(tokenizer.nextToken());
		}

		final String[] addrsOrExprs = list.toArray(new String[list.size()]);

		ParamHolder params;
		if (dialog.enteredExpression())
			params = new ExpressionsHolder(addrsOrExprs);
		else
			params = new AddressAndSpaceHolder(addrsOrExprs, dialog.getMemorySpace());

		final ParamHolder params_f = params;
		Job job = new Job("Add Memory Block") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				addMemoryBlocks(context, retrieval, params_f, renderingSite);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();

	}

	public boolean canAddMemoryBlocks(IWorkbenchPart part, ISelection selection)
	throws CoreException {
		return true;
	}

	public boolean supportsAddMemoryBlocks(IWorkbenchPart part) {
		return (IDebugUIConstants.ID_MEMORY_VIEW.equals(part.getSite().getId()));
	}

	// In order to avoid duplicating the addMemoryBlocks method--one 
	// version for expressions, one for memory-space+address, we pass in a 
	// an opaque parameter and let the logic within addMemoryBlocks
	// differentiate where needed via isinstanceof

	private class ParamHolder {
	}

	class AddressAndSpaceHolder extends ParamHolder {
		public AddressAndSpaceHolder(final String[] addresses,
				final String memorySpace) {
			this.addresses = addresses;
			this.memorySpace = memorySpace;
		}

		public String[] addresses;

		public String memorySpace;
	}

	private class ExpressionsHolder extends ParamHolder {
		public ExpressionsHolder(final String[] expressions) {
			this.expressions = expressions;
		}

		public String[] expressions;
	}

	private void addMemoryBlocks(Object context,
			IMemorySpaceAwareMemoryBlockRetrieval memRetrieval,
			final ParamHolder params, IMemoryRenderingSite memRendSite) {

		final String[] addrsOrExprs = (params instanceof AddressAndSpaceHolder) ? ((AddressAndSpaceHolder) params).addresses
				: ((ExpressionsHolder) params).expressions;

		for (int i = 0; i < addrsOrExprs.length; i++) {
			String addrOrExpr = addrsOrExprs[i].trim();
			try {

				// get extended memory block with the expression or address
				// entered
				IMemoryBlockExtension memBlock;

				if (params instanceof AddressAndSpaceHolder)
					memBlock = memRetrieval.getMemoryBlock(
							addrOrExpr,
							context,
							((AddressAndSpaceHolder) params).memorySpace);
				else
					memBlock = memRetrieval.getExtendedMemoryBlock(addrOrExpr,
							context);

				// add block to memory block manager
				if (memBlock != null) {
					IMemoryBlock[] memArray = new IMemoryBlock[] { memBlock };

					DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(memArray);

					addDefaultRenderings(memBlock, memRendSite);
				} else {
					// open error if it failed to retrieve a memory block
					openError(Messages.AddMemBlocks_title,
							Messages.AddMemBlocks_noMemoryBlock,
							null);
				}
			} catch (DebugException e1) {
				openError(Messages.AddMemBlocks_title,
						Messages.AddMemBlocks_failed, e1);
			} catch (NumberFormatException e2) {
				String message = Messages.AddMemBlocks_failed + "\n" + Messages.AddMemBlocks_input_invalid; //$NON-NLS-1$
				openError(Messages.AddMemBlocks_title, message,
						null);
			}
		}
	}

	private void addDefaultRenderings(IMemoryBlock memoryBlock,
			IMemoryRenderingSite memRendSite) {

		// This method was mostly lifted from the platform's AddMemoryBlockAction

		IMemoryRenderingType primaryType = DebugUITools.getMemoryRenderingManager().getPrimaryRenderingType(
				memoryBlock);
		IMemoryRenderingType renderingTypes[] = DebugUITools.getMemoryRenderingManager().getDefaultRenderingTypes(
				memoryBlock);

		// create primary rendering
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
		} catch (CoreException e1) {
			CDebugUIPlugin.log(e1);
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
				CDebugUIPlugin.log(e);
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

	public void addMemoryBlocksForVariables(ICVariable[] variables, IMemoryRenderingSite memSite) throws DebugException {

		IAdaptable debugViewElement = DebugUITools.getDebugContext();


		CMemoryBlockRetrievalExtension cdtRetrieval = null;

		{
			IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)debugViewElement.getAdapter(IMemoryBlockRetrieval.class);

			if (retrieval == null && debugViewElement instanceof IDebugElement)
				retrieval = ((IDebugElement)debugViewElement).getDebugTarget();

			if (retrieval == null || !(retrieval instanceof CMemoryBlockRetrievalExtension))
				return;

			cdtRetrieval = (CMemoryBlockRetrievalExtension) retrieval;
		}

		String[] expressions = new String[variables.length];
		for (int i = 0; i < variables.length; i++) {
			
			String exp = variables[i].getExpressionString();
			
			if (variables[i].getType().isPointer() ||  variables[i].getType().isArray() ||
                    variables[i].getType().isReference() || variables[i] instanceof CRegister)
				expressions[i] = exp;
			else
				expressions[i] = "&" + exp; //$NON-NLS-1$
		}

		ParamHolder params;
			params = new ExpressionsHolder(expressions);

		final IAdaptable debugViewElement_f = debugViewElement;
		final CMemoryBlockRetrievalExtension retrieval_f = cdtRetrieval;
		final ParamHolder params_f = params;
		final IMemoryRenderingSite memRendSite = memSite;
		Job job = new Job("Add Memory Block") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				addMemoryBlocks(debugViewElement_f, retrieval_f, params_f,
						memRendSite);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * Helper function to open an error dialog.
	 * @param title
	 * @param message
	 * @param e
	 */
	static public void openError (final String title, final String message, final Exception e)
	{
		UIJob uiJob = new UIJob("open error"){ //$NON-NLS-1$

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// open error for the exception
				String detail = ""; //$NON-NLS-1$
				if (e != null)
					detail = e.getMessage();

				Shell shell = CDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();

				MessageDialog.openError(
						shell,
						title,
						message + "\n" + detail); //$NON-NLS-1$
				return Status.OK_STATUS;
			}};
			uiJob.setSystem(true);
			uiJob.schedule();
	}

	private static Object getContextSelectionForPart(IWorkbenchPart part) {
		IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(
				part.getSite().getWorkbenchWindow());

		ISelection debugContext = contextService.getActiveContext(getPartId(part));
		if (debugContext == null) {
			debugContext = contextService.getActiveContext();
		}

		if (debugContext instanceof IStructuredSelection) {
			return ((IStructuredSelection) debugContext).getFirstElement();
		}

		return null;
	}
	
	private static String getPartId(IWorkbenchPart part) {
		if (part instanceof IViewPart) {
			IViewSite site = (IViewSite) part.getSite();
			return site.getId() + (site.getSecondaryId() != null ? (":" + site.getSecondaryId()) : ""); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return part.getSite().getId();
		}
	}
	/**
	 * Execute runnable on UI thread if the current thread is not an UI thread.
	 * Otherwise execute it directly.
	 * 
	 * @param runnable
	 *            the runnable to execute
	 */
	private void runOnUIThread(final Runnable runnable)	{
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			UIJob job = new UIJob("Memory Browser UI Job"){ //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					runnable.run();
					return Status.OK_STATUS;
				}};
			job.setSystem(true);
			job.schedule();
		}
	}
}

