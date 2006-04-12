/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceSet;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.GenericMessages;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.ui.progress.UIJob;


/**
  *  Runnable to perform actual transfer operation.
  * 
  */
public class SystemDNDTransferRunnable extends UIJob
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

	public SystemDNDTransferRunnable(Object target, ArrayList srcObjects, Viewer originatingViewer, int sourceType)
	{
		super(GenericMessages.DragAndDropOperation_message);
		_srcObjects = srcObjects;
		_currentTarget = target;
		_sourceType = sourceType;
		_originatingViewer = originatingViewer;
		_resultSrcObjects = new ArrayList(_srcObjects.size());
		_resultTgtObjects = new ArrayList();
		_setList = new ArrayList();
		this.setUser(true);
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
	
	protected boolean transferRSEResources(IProgressMonitor monitor, Object target, ISubSystem targetSubSystem, ISystemDragDropAdapter targetAdapter)
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
	    String targetType = "";
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
			    if (target instanceof ISystemFilterReference)
			    {
			        ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
			        if (factory.supportsDropInFilters())
			        {											        
			            target = targetSubSystem.getTargetForFilter((ISystemFilterReference)target);										            
			            targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);
				
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
	
	protected boolean transferNonRSEResources(IProgressMonitor monitor, Object target, ISubSystem targetSubSystem, ISystemDragDropAdapter targetAdapter)
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
				    if (target instanceof ISystemFilterReference)
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

					if (srcObject instanceof IResource)
					{
						// Eclipse resource transfer
						IResource resource = (IResource) srcObject;
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
										    if (target instanceof ISystemFilterReference)
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
								 
								    if (target instanceof ISystemFilterReference)
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

	public IStatus runInUIThread(IProgressMonitor monitor)
	{

		_ok = true;

		Object target = _currentTarget;
		ISubSystem targetSubSystem = null;
		//boolean expandFolder = false;
	
		
		if (target instanceof IAdaptable)
		{
			ISystemDragDropAdapter targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);

			if (targetAdapter != null)
			{
				targetSubSystem = targetAdapter.getSubSystem(target);				

				if (targetSubSystem != null && !targetSubSystem.isConnected())
				{
				    try
				    {
				        targetSubSystem.connect(SystemBasePlugin.getActiveWorkbenchShell());
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
					transferRSEResources(monitor, target, targetSubSystem, targetAdapter);
				}				
				else
				{
					transferNonRSEResources(monitor, target, targetSubSystem, targetAdapter);
				}
			}
		}

		// fire refresh for target
		if (_ok && monitor != null) // I added this test: phil
		{
			monitor.done();
		}
		// always refresh
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		
		if (target != null && target instanceof ISystemContainer)
		{
			((ISystemContainer)target).markStale(true);
		}
	
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
			}
			
			if (_originatingViewer instanceof TreeViewer)
			{
				TreeViewer viewer = (TreeViewer) _originatingViewer;
				viewer.setExpandedState(target, true);
			}
			
			if (doRefresh)
			{
			    registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, _resultTgtObjects, target, targetSubSystem, null, _originatingViewer);
			}
		}
		
		registry.fireEvent(new SystemResourceChangeEvent(target, ISystemResourceChangeEvents.EVENT_REFRESH, target));
		return Status.OK_STATUS;
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
		SystemMessageDialog dlg = new SystemMessageDialog(SystemMessageDialog.getDefaultShell(), errorMessage);
		dlg.open();
	}

	public boolean dropOkay()
	{
		return _ok;
	}

}