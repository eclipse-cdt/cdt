/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * Rupen Mardirossian (IBM) - [187713] Check to see if target is null before attempting to retrieve targetAdapter in tranferRSEResources method (line 248)
 * Martin Oberhuber (Wind River) - [200682] Fix drag&drop for elements just adaptable to IResource, like CDT elements
 * David McKnight   (IBM)        - [186363] get rid of obsolete calls to SubSystem.connect()
 * Xuan Chen        (IBM)        - [191370] [dstore] Supertransfer zip not deleted when cancelling copy
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 * David McKnight   (IBM)        - [228587] [dnd] NPE From Refresh on Copy/Paste
 * David McKnight   (IBM)        - [232889] Dragging and dropping files from a remote unix system to a local project does not work
 * David McKnight   (IBM)        - [234721] [dnd] When dragging a file from windows file explorer into RSE, a refresh error is given.
 * David McKnight   (IBM)        - [248922]  [dnd] Remote to local overwrite copy does not work
 * David McKnight   (IBM)        - [196166] [usability][dnd] Changing the sort order of hosts in the SystemView should work by drag & drop
 * David McKnight   (IBM)        - [248922]  [dnd] display error message when copy operation hits exception
 * Radoslav Gerganov (ProSyst)   - [231428] [files] NPE on canceling copy operation from remote host
 * David McKnight   (IBM)        - [328148] Dropping resource onto Eclipse IFile causes RSEG1003U unexpected exception
 * David McKnight   (IBM)        - [231971] [dnd] Drag and Drop Filter Displays Error
 * Martin Oberhuber (Wind River) - [357667] [dnd] Allow copy-onto-myself semantics
 * David McKnight   (IBM)        - [416544] drag and drop of filter into filter doesn't merge filter strings
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.internal.ui.GenericMessages;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.internal.model.SystemScratchpad;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CopyResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.progress.UIJob;


/**
  *  Runnable to perform actual transfer operation.
  *
  */
public class SystemDNDTransferRunnable extends WorkspaceJob
{

	public static final int SRC_TYPE_RSE_RESOURCE = 0;
	public static final int SRC_TYPE_ECLIPSE_RESOURCE = 1;
	public static final int SRC_TYPE_OS_RESOURCE = 2;
	public static final int SRC_TYPE_TEXT = 3;
	public static final int SRC_TYPE_UNKNOWN = 4;

	private List _srcObjects;
	private List _resultSrcObjects;
	private List _resultTgtObjects;
	private List _setList;


	private Object _currentTarget;
	private int _sourceType;
	private Viewer _originatingViewer;
	private boolean _ok;
	private Shell _shell;

	public SystemDNDTransferRunnable(Object target, ArrayList srcObjects, Viewer originatingViewer, int sourceType)
	{
		super(GenericMessages.TransferOperation_message);
		_srcObjects = srcObjects;
		_currentTarget = target;
		_sourceType = sourceType;
		_originatingViewer = originatingViewer;
		_resultSrcObjects = new ArrayList(_srcObjects.size());
		_resultTgtObjects = new ArrayList();
		_setList = new ArrayList();
		this.setUser(true);
		_shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	protected SystemRemoteResourceSet getSetFor(ISubSystem subSystem, ISystemDragDropAdapter adapter)
	{
		for (int i = 0; i < _setList.size(); i++)
		{
			SystemRemoteResourceSet set = (SystemRemoteResourceSet)_setList.get(i);
			if (set.getAdapter() == adapter && set.getSubSystem() == subSystem)
			{
				return set;
			}
		}

		// no existing set - create one
		SystemRemoteResourceSet newSet = new SystemRemoteResourceSet(subSystem, adapter);
		_setList.add(newSet);
		return newSet;
	}

	/**
	 * Transfer RSE resources.
	 *
	 * @param target actual target object (parent item) to drop into
	 * @param targetSubSystem subsystem of target object
	 * @param targetAdapter RSE ISystemDragDropAdapter of target object
	 * @param monitor progress monitor for cancellation
	 * @return <code>true</code> if successfully transferred, or
	 *         <code>false</code> if cancelled.
	 */
	protected boolean transferRSEResources(Object target, ISubSystem targetSubSystem, ISystemDragDropAdapter targetAdapter, IProgressMonitor monitor)
	{
		// transfer local artificts and categorize remote objects
		for (int i = 0; i < _srcObjects.size() && _ok; i++)
		{
			Object srcObject = _srcObjects.get(i);
			_resultSrcObjects.add(srcObject);

			if (srcObject instanceof SystemMessage)
			{
				operationFailed(monitor);
				showErrorMessage((SystemMessage) srcObject);
				return _ok;
			}
			else if (srcObject != null)
			{
				ISystemDragDropAdapter srcAdapter = (ISystemDragDropAdapter) ((IAdaptable) srcObject).getAdapter(ISystemDragDropAdapter.class);

				if (srcAdapter != null)
				{
				    if (srcObject instanceof IHost)
				    {
				        Object tempObject = srcAdapter.doDrag(srcObject, false, monitor);
				        if (targetAdapter.validateDrop(tempObject, target, false))
				        {
				            targetAdapter.doDrop(tempObject, target, false, false, _sourceType, monitor);
				            _resultTgtObjects.add(tempObject);
				        }
				    }
				    else
				    {
				    	ISubSystem srcSubSystem = srcAdapter.getSubSystem(srcObject);
				    	if (srcSubSystem.isConnected() ||
						        srcObject instanceof ISystemFilterReference ||
						        srcObject instanceof ISubSystem)
						{
				    		SystemRemoteResourceSet set = getSetFor(srcSubSystem, srcAdapter);
				    		set.addResource(srcObject);				    		
						}
				    }
				}
			}
		}

		String targetPath = targetAdapter.getAbsoluteName(target);
	    boolean sameSubSystemType = true;
	    String targetType = ""; //$NON-NLS-1$
	    if (targetSubSystem != null)
	    {
	        targetType = targetSubSystem.getName();
	    }

		// now we have things divided into sets
		// transfer 1 set at a time
		for (int s = 0; s < _setList.size(); s++)
		{
			SystemRemoteResourceSet set = (SystemRemoteResourceSet)_setList.get(s);




			ISubSystem srcSubSystem = set.getSubSystem();
			ISystemDragDropAdapter srcAdapter = set.getAdapter();

			String srcType = srcSubSystem.getName();
			sameSubSystemType = targetType.equals(srcType);

			if (!sameSubSystemType && targetSubSystem != null)
			{
				ISystemResourceSet tempObjects = srcAdapter.doDrag(set, monitor);
				if (monitor.isCanceled()) {
					monitor.done();
					return false;
				}
				if (tempObjects == null)
				{
					// drag failed
					operationFailed(monitor);
					showInvalidTransferMessage(set, targetPath);
				}
				else if (tempObjects.hasMessage())
				{
					operationFailed(monitor);
					showErrorMessage(tempObjects.getMessage());
				}
				else
				{
					if (targetAdapter.validateDrop(tempObjects, target, (targetSubSystem == srcSubSystem)))
					{
						//	special case for filters
					    if (target instanceof ISystemFilterReference)
					    {
					        ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
					        if (factory.supportsDropInFilters())
					        {
					            target = targetSubSystem.getTargetForFilter((ISystemFilterReference)target);
					            targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);
					        }
					    }


						ISystemResourceSet droppedObjects = targetAdapter.doDrop(tempObjects, target, sameSubSystemType, (targetSubSystem == srcSubSystem), _sourceType, monitor);
						if (droppedObjects == null)
						{
							operationFailed(monitor);
						}
						else if (droppedObjects.hasMessage())
						{
							//Even the droppedObject has message, it could still has
							//dropped results.  (user cancels the operation, but some objects
							//has already been copied.
							//Need to make sure we refresh those copied object.
							List results = droppedObjects.getResourceSet();
							for (int d = 0; d < results.size(); d++)
							{
								_resultTgtObjects.add(results.get(d));
							}
							operationFailed(monitor);
							showErrorMessage(droppedObjects.getMessage());
						}
						else
						{
							List results = droppedObjects.getResourceSet();
							for (int d = 0; d < results.size(); d++)
							{
								_resultTgtObjects.add(results.get(d));
							}
						}
					}
					else
					{
						// invalid drop
						operationFailed(monitor);
						showInvalidTransferMessage(set, targetPath);
					}
				}
			}
			else
			{
			    // special case for filters
			    if (target instanceof ISystemFilterReference && targetSubSystem != null && !(srcAdapter instanceof SystemViewFilterReferenceAdapter))
			    {
			        ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
			        if (factory.supportsDropInFilters())
			        {
			            target = targetSubSystem.getTargetForFilter((ISystemFilterReference)target);
			            if (target == null)
			            {
			            	return false;
			            }
			            else
			            {
			            	targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);
			            }
			        }
			    }
				if (targetAdapter.validateDrop(set, target, (targetSubSystem == srcSubSystem)))
				{

					ISystemResourceSet droppedObjects = targetAdapter.doDrop(set, target, sameSubSystemType, (targetSubSystem == srcSubSystem), _sourceType, monitor);
					if (droppedObjects == null)
					{
						operationFailed(monitor);
					}
					else if (droppedObjects.hasMessage())
					{
						operationFailed(monitor);
						showErrorMessage(droppedObjects.getMessage());
					}
					else
					{
						List results = droppedObjects.getResourceSet();
						for (int d = 0; d < results.size(); d++)
						{
							_resultTgtObjects.add(results.get(d));
						}
					}
				}
				else
				{
					// invalid drop
					operationFailed(monitor);
					showInvalidTransferMessage(set, targetPath);
				}
			}
		}

		return _ok;
	}

	protected boolean transferRSEResourcesToEclipseResource(IResource target, ISubSystem targetSubSystem, IProgressMonitor monitor)
	{
		boolean alwaysOverwrite = false;

		List resourcesToCopy = new ArrayList();
		IWorkspaceRoot root = target.getWorkspace().getRoot();
		for (int i = 0; i < _srcObjects.size() && _ok; i++)
		{
			Object srcObject = _srcObjects.get(i);

			_resultSrcObjects.add(srcObject);

			if (srcObject instanceof SystemMessage)
			{
				operationFailed(monitor);
				showErrorMessage((SystemMessage) srcObject);
			}
			else if (srcObject != null)
			{
				// find all the files to copy and check that they don't exist first
				ISystemDragDropAdapter srcAdapter = (ISystemDragDropAdapter) ((IAdaptable) srcObject).getAdapter(ISystemDragDropAdapter.class);
				Object tempFile = srcAdapter.doDrag(srcObject, true, monitor);
				if (tempFile instanceof IResource)
				{
					boolean canCopy = true;
					IResource res = (IResource)tempFile;
					try
					{
						if (target instanceof IFile){
							// as per bug 328148, we should always use the parent of a file when it's a target
							target = ((IFile)target).getParent();
						}
						IPath destPath = target.getFullPath();
						destPath = destPath.append(res.getName());

						IResource newResource = root.findMember(destPath);

						// check for existing files
						if (!alwaysOverwrite){
							if (newResource != null && newResource.exists()){
								int result = checkOverwrite(res, newResource);
								if (result != IDialogConstants.YES_ID && result != IDialogConstants.YES_TO_ALL_ID){
									canCopy = false;
									if (result == IDialogConstants.CANCEL_ID){
										// cancel the whole operation
										monitor.setCanceled(true);
										return false;
									}

									_resultSrcObjects.remove(srcObject);
								}
								else if (result == IDialogConstants.YES_TO_ALL_ID){
									alwaysOverwrite = true; // don't ask anymore
								}
							}
						}

						// add to the list of files to copy
						if (canCopy){
							resourcesToCopy.add(res);
						}
					}
					catch (Exception e)
					{
						operationFailed(monitor);
						SystemBasePlugin.logError(e.getMessage(), e);
					}
				}
			}
		}

		// now doing the actual copy
		if (!resourcesToCopy.isEmpty()) {
			IResource[] resources = (IResource[])resourcesToCopy.toArray(new IResource[resourcesToCopy.size()]);
			IPath destinationPath = target.getFullPath();
			CopyResourcesOperation op = new CopyResourcesOperation(resources, destinationPath,
					SystemResources.RESID_COPY_TITLE);


			IAdaptable adaptable = WorkspaceUndoUtil.getUIInfoAdapter(_shell);
			try {
				PlatformUI.getWorkbench().getOperationSupport()
						.getOperationHistory().execute(op, monitor,adaptable);
			} catch (ExecutionException e) {
				SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_OCCURRED);
				if (e.getCause() instanceof CoreException) {
					SystemBasePlugin.logError(e.getMessage(), e);
					errorMessage.makeSubstitution(e.getCause().getMessage());
				} else {
					SystemBasePlugin.logError(e.getMessage(), e);
					errorMessage.makeSubstitution(e.getMessage());
				}

				showErrorMessage(errorMessage);
				operationFailed(monitor);
				return false;
			}
		}

		return true;
	}


	private int checkOverwrite(final IResource source, final IResource destination) {
		final int[] result = new int[1]; // using array since you can't change a final int

		Runnable query = new Runnable() {
			public void run() {
				int resultId[] = {
						IDialogConstants.YES_ID,
						IDialogConstants.YES_TO_ALL_ID,
						IDialogConstants.NO_ID,
						IDialogConstants.CANCEL_ID };

				String labels[] = new String[] {
						IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL };

				String title = SystemResources.RESID_COLLISION_DUPLICATE_RESOURCE_TITLE;
				String msg = NLS.bind(SystemResources.RESID_COLLISION_OVERWRITE_RESOURCE_MESSAGE, destination.getFullPath().makeRelative());

				MessageDialog dialog = new MessageDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						title,
						null, msg, MessageDialog.QUESTION, labels, 0);
				dialog.open();
				if (dialog.getReturnCode() == SWT.DEFAULT) {
					// A window close returns SWT.DEFAULT - mapped to a cancel
					result[0] = IDialogConstants.CANCEL_ID;
				} else {
					result[0] = resultId[dialog.getReturnCode()];
				}
			}
		};

		PlatformUI.getWorkbench().getDisplay().syncExec(query);
		return result[0];
	}

	protected boolean transferNonRSEResources(Object target, ISubSystem targetSubSystem, ISystemDragDropAdapter targetAdapter, IProgressMonitor monitor)
	{

		for (int i = 0; i < _srcObjects.size() && _ok; i++)
		{
			Object srcObject = _srcObjects.get(i);

			_resultSrcObjects.add(srcObject);

			if (srcObject instanceof SystemMessage)
			{
				operationFailed(monitor);
				showErrorMessage((SystemMessage) srcObject);
			}
			else if (srcObject != null)
			{


					// special case for filters
				    if (target instanceof ISystemFilterReference && targetSubSystem != null)
				    {

				        ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
				        if (factory.supportsDropInFilters() && factory.providesCustomDropInFilters())
				        {
				        	((ISystemFilterReference)target).markStale(true);

				            target = targetSubSystem.getTargetForFilter((ISystemFilterReference)target);
				            targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);

				        }
				    }

				if (_sourceType == SRC_TYPE_ECLIPSE_RESOURCE)
				{
					// Eclipse resource transfer
					IResource resource = null;
					if (srcObject instanceof IResource) {
						resource = (IResource) srcObject;
					} else if (srcObject instanceof IAdaptable) {
						resource = (IResource)((IAdaptable)srcObject).getAdapter(IResource.class);
					} else {
						resource = (IResource)Platform.getAdapterManager().getAdapter(srcObject, IResource.class);
					}
					if (resource!=null) {
						Object droppedObject = targetAdapter.doDrop(resource, target, false, false, _sourceType, monitor);
						if (droppedObject == null)
							operationFailed(monitor);
						else
							_resultTgtObjects.add(droppedObject);
					}
				}
				else if (_sourceType == SRC_TYPE_OS_RESOURCE)
				{
					if (srcObject instanceof String)
					{
						// non-Eclipse file transfer
						String file = (String) srcObject;

						Object droppedObject = targetAdapter.doDrop(file, target, false, false, _sourceType, monitor);
						if (droppedObject == null)
							operationFailed(monitor);
						else
							_resultTgtObjects.add(droppedObject);
					}
				}
				else if (_sourceType == SRC_TYPE_TEXT)
				{
					if (srcObject instanceof String)
					{
						String text = (String) srcObject;
						Object droppedObject = targetAdapter.doDrop(text, target, false, false, _sourceType, monitor);
						if (droppedObject == null)
							operationFailed(monitor);
						else
							_resultTgtObjects.add(droppedObject);
					}
				}
				else if (_sourceType == SRC_TYPE_RSE_RESOURCE)
				{
					ISystemDragDropAdapter srcAdapter = (ISystemDragDropAdapter) ((IAdaptable) srcObject).getAdapter(ISystemDragDropAdapter.class);

					if (srcAdapter != null)
					{
					    if (srcObject instanceof IHost)
					    {
					        Object tempObject = srcAdapter.doDrag(srcObject, false, monitor);
					        if (targetAdapter.validateDrop(tempObject, target, false))
					        {
					            targetAdapter.doDrop(tempObject, target, false, false, _sourceType, monitor);
					            _resultTgtObjects.add(tempObject);
					        }
					    }
					    else
					    {
							ISubSystem srcSubSystem = srcAdapter.getSubSystem(srcObject);

							if (srcSubSystem.isConnected() ||
							        srcObject instanceof ISystemFilterReference ||
							        srcObject instanceof ISubSystem)
							{
								String srcType = srcSubSystem.getName();
								String srcPath = srcAdapter.getAbsoluteName(srcObject);
								String targetPath = targetAdapter.getAbsoluteName(target);
							    boolean sameSubSystemType = true;
							    if (targetSubSystem != null)
							    {
							        String targetType = targetSubSystem.getName();
									sameSubSystemType = targetType.equals(srcType);
							    }

								if (!sameSubSystemType)
								{
									Object tempObject = srcAdapter.doDrag(srcObject, sameSubSystemType, monitor);
									if (tempObject == null)
									{
										// drag failed
										operationFailed(monitor);
										showInvalidTransferMessage(srcPath, targetPath);
									}
									else if (tempObject instanceof SystemMessage)
									{
										operationFailed(monitor);
										showErrorMessage((SystemMessage) tempObject);
									}
									else
									{

										if (targetAdapter.validateDrop(tempObject, target, (targetSubSystem == srcSubSystem)))
										{
											//	special case for filters
										    if (target instanceof ISystemFilterReference && targetSubSystem != null)
										    {
										        ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
										        if (factory.supportsDropInFilters() && factory.providesCustomDropInFilters())
										        {
										            target = targetSubSystem.getTargetForFilter((ISystemFilterReference)target);
										            targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);

										        }
										    }

											Object droppedObject = targetAdapter.doDrop(tempObject, target, sameSubSystemType, (targetSubSystem == srcSubSystem), _sourceType, monitor);
											if (droppedObject == null)
											{
												operationFailed(monitor);
											}
											else if (droppedObject instanceof SystemMessage)
											{
												operationFailed(monitor);
												showErrorMessage((SystemMessage) droppedObject);
											}
											else
												_resultTgtObjects.add(droppedObject);
										}
										else
										{
											// invalid drop
											operationFailed(monitor);
											showInvalidTransferMessage(srcPath, targetPath);
										}
									}
								}
								else if (srcObject != target && !srcPath.equals(targetPath))
								{
								    // special case for filters

								    if (target instanceof ISystemFilterReference && targetSubSystem != null)
								    {
								        ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
								        if (factory.supportsDropInFilters() && factory.providesCustomDropInFilters())
								        {
								            target = targetSubSystem.getTargetForFilter((ISystemFilterReference)target);
								            targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);

								        }
								    }
									if (targetAdapter.validateDrop(srcObject, target, (targetSubSystem == srcSubSystem)))
									{

										Object droppedObject = targetAdapter.doDrop(srcObject, target, sameSubSystemType, (targetSubSystem == srcSubSystem), _sourceType, monitor);
										if (droppedObject == null)
										{
											operationFailed(monitor);
										}
										else if (droppedObject instanceof SystemMessage)
										{
											operationFailed(monitor);
											showErrorMessage((SystemMessage) droppedObject);
										}
										else
											_resultTgtObjects.add(droppedObject);
									}
									else
									{
										// invalid drop
										operationFailed(monitor);
										showInvalidTransferMessage(srcPath, targetPath);
									}
								}
								else
								{
									// can't drop src onto itself
									// invalid drop
									operationFailed(monitor);
									showInvalidTransferMessage(srcPath, targetPath);
								}
							}
						}
					}
				}
			}
			if (monitor != null && monitor.isCanceled())
			{
				_ok = false;
				return _ok;
			}
		}


		return true;
	}

	public IStatus runInWorkspace(IProgressMonitor monitor)
	{

		_ok = true;

		Object target = _currentTarget;
		ISubSystem targetSubSystem = null;
		//boolean expandFolder = false;


		if (target instanceof IAdaptable)
		{
			ISystemDragDropAdapter targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);

			// checking for IResource since we already handle them specially
			if (targetAdapter != null && !(target instanceof IResource))
			{
				targetSubSystem = targetAdapter.getSubSystem(target);

				if (targetSubSystem != null && !targetSubSystem.isConnected())
				{
				    try
				    {
				        targetSubSystem.connect(monitor, false);
				    }
				    catch (Exception e)
				    {
				    }
				}

				SystemMessage copyMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPYGENERIC_PROGRESS);
				if (monitor != null)
				    monitor.beginTask(copyMessage.getLevelOneText(), IProgressMonitor.UNKNOWN);

				if (_sourceType == SRC_TYPE_RSE_RESOURCE)
				{
					transferRSEResources(target, targetSubSystem, targetAdapter, monitor);
				}
				else
				{
					transferNonRSEResources(target, targetSubSystem, targetAdapter, monitor);
				}
			}
			else if (target instanceof IResource)
			{
				transferRSEResourcesToEclipseResource((IResource)target, targetSubSystem, monitor);
			}
		}

		// fire refresh for target
		if (_ok && monitor != null) // I added this test: phil
		{
			monitor.done();
		}


		if (target != null && target instanceof ISystemContainer)
		{
			((ISystemContainer)target).markStale(true);
		}

		RefreshJob refresh = new RefreshJob(target, targetSubSystem);
		refresh.schedule();
		return Status.OK_STATUS;
	}

	public class RefreshJob extends UIJob
	{
		private Object _target;
		private ISubSystem _targetSubSystem;
		public RefreshJob(Object target, ISubSystem targetSubSystem)
		{
			super("Refresh"); //$NON-NLS-1$
			_target = target;
			_targetSubSystem = targetSubSystem;
		}

		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			String[] oldNames = new String[_resultSrcObjects.size()];
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			if (_resultTgtObjects.size() > 0)
			{
				boolean doRefresh = _ok;

				for (int t = 0; t < _resultTgtObjects.size() && t < _resultSrcObjects.size(); t++)
				{
				    Object tgt = _resultTgtObjects.get(t);
				    Object src = _resultSrcObjects.get(t);
				    if (tgt == src || tgt == null)
				    {
				        doRefresh = false;
				    }
				    else
				    {
				        doRefresh = true;
				    }
				    ISystemDragDropAdapter adapter = null;
				    if (src instanceof IAdaptable) {
				    	adapter = (ISystemDragDropAdapter)((IAdaptable)src).getAdapter(ISystemDragDropAdapter.class);
				    }
				    else {
				    	adapter = (ISystemDragDropAdapter)Platform.getAdapterManager().getAdapter(src, ISystemDragDropAdapter.class);
				    }

				    if (adapter != null){
				    	oldNames[t] = adapter.getAbsoluteName(src);
				    }
				    else if (src instanceof IResource){ // could be an eclipse resource
				    	oldNames[t] = ((IResource)src).getFullPath().toOSString();
				    }
				    else if (src instanceof String){ // local os resource
				    	oldNames[t] = (String)src;
				    }
				    else {
				    	oldNames[t] = ""; // source resource unknown //$NON-NLS-1$
				    }
				}

				if (_originatingViewer instanceof TreeViewer)
				{
					try
					{
						TreeViewer viewer = (TreeViewer) _originatingViewer;

						if (!(_target instanceof IHost)){ // not sure when we'd want to expand a host
							viewer.setExpandedState(_target, true);
						}
					}
					catch (Exception e)
					{

					}
				}

				if (doRefresh)
				{
					String operation = ISystemRemoteChangeEvents.SYSTEM_REMOTE_OPERATION_COPY;
				    registry.fireRemoteResourceChangeEvent(operation, ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, _resultTgtObjects, _target, _targetSubSystem, oldNames, _originatingViewer);
				}
				else if (_target instanceof SystemScratchpad)
				{
					registry.fireEvent(new SystemResourceChangeEvent(_resultTgtObjects, ISystemResourceChangeEvents.EVENT_ADD_MANY, _target));
				}
			}

			return Status.OK_STATUS;
		}
	}

	private void operationFailed(IProgressMonitor monitor)
	{

		_ok = false;
		if (monitor != null)
		    monitor.done();
	}

	private void showInvalidTransferMessage(String srcPath, String targetPath)
	{
		SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_TRANSFER_INVALID);
		errorMessage.makeSubstitution(srcPath, targetPath);
		showErrorMessage(errorMessage);
	}

	private void showInvalidTransferMessage(ISystemResourceSet resourceSet, String targetPath)
	{
		SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_TRANSFER_INVALID);
		errorMessage.makeSubstitution(resourceSet.toString(), targetPath);
		showErrorMessage(errorMessage);
	}

	private void showErrorMessage(SystemMessage errorMessage)
	{
		Display.getDefault().asyncExec(new ShowErrorRunnable(errorMessage));
	}

	public class ShowErrorRunnable implements Runnable
	{
		SystemMessage _errorMessage;
		public ShowErrorRunnable(SystemMessage errorMessage)
		{
			_errorMessage = errorMessage;
		}

		public void run()
		{
			Shell shell = SystemBasePlugin.getActiveWorkbenchShell();
			SystemMessageDialog dlg = new SystemMessageDialog(shell, _errorMessage);
			dlg.open();
		}

	}

	public boolean dropOkay()
	{
		return _ok;
	}

}
