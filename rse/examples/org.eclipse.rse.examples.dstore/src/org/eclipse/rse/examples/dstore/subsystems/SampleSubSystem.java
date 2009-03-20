/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.examples.dstore.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.examples.dstore.services.IHostSampleContainer;
import org.eclipse.rse.examples.dstore.services.IHostSampleObject;
import org.eclipse.rse.examples.dstore.services.SampleService;

public class SampleSubSystem extends SubSystem {

	private SampleService _sampleService;
	private SampleRootResource _root;
	
	protected SampleSubSystem(IHost host, IConnectorService connectorService, SampleService sampleService) {
		super(host, connectorService);
		_sampleService = sampleService;
	}

	public boolean hasChildren() {
		return true;
	}
	
	public Object[] getChildren() {
		if (_root == null){
			_root = new SampleRootResource(this);
		}
		return new Object[] {_root};
	}
	
	public Object[] list(RemoteSampleObject containerObj, IProgressMonitor monitor){
		try {
			checkIsConnected(monitor);
		}
		catch (Exception e){
			e.printStackTrace();			
		}

		RemoteSampleObject[] contents = containerObj.getContents();
		if (contents == null){
			IHostSampleContainer container = (IHostSampleContainer)containerObj.getHostSampleObject();	
			if (container == null && containerObj instanceof SampleRootResource){
				container = getSampleService().getContainer(containerObj.getName(), monitor);
				((SampleRootResource)containerObj).setHostSampleObject(container);
			}			
			IHostSampleObject[] hostResults = getSampleService().query(container, monitor);
			
			contents = new RemoteSampleObject[hostResults.length];
			for (int i = 0; i < hostResults.length; i++){
				IHostSampleObject hobj = hostResults[i];
				contents[i] = new RemoteSampleObject(containerObj, hobj, this);
			}
			containerObj.setContents(contents);
		}		
		
		return contents;
	}
	
	public void doRemoteAction(RemoteSampleObject object, IProgressMonitor monitor){
		IHostSampleObject hostObject = object.getHostSampleObject();	
		getSampleService().doAction(hostObject, monitor);		
	}
	
	
	public void createSampleContainer(RemoteSampleObject parentObject, IProgressMonitor monitor){
		parentObject.setContents(null); // clear contents so that fresh contents arrive after refresh
		IHostSampleObject hostObject = parentObject.getHostSampleObject();	
		getSampleService().createSampleContainer(hostObject, monitor);		
	}
	
	public void createSampleObject(RemoteSampleObject parentObject, IProgressMonitor monitor){
		parentObject.setContents(null); // clear contents so that fresh contents arrive after refresh
		IHostSampleObject hostObject = parentObject.getHostSampleObject();	
		getSampleService().createSampleObject(hostObject, monitor);		
	}
	
	public boolean rename(RemoteSampleObject object, String newName, IProgressMonitor monitor){
		IHostSampleObject hostObject = object.getHostSampleObject();		
		return getSampleService().rename(hostObject, newName, monitor);		
	}
	
	public boolean delete(RemoteSampleObject object, IProgressMonitor monitor){
		object.getParent().setContents(null); // clear contents so that fresh contents arrive after refresh
		IHostSampleObject hostObject = object.getHostSampleObject();		
		return getSampleService().delete(hostObject, monitor);		
	}

	protected SampleService getSampleService(){
		return _sampleService;
	}

	@Override
	public void initializeSubSystem(IProgressMonitor monitor) {
		super.initializeSubSystem(monitor);
		
		getSampleService().initService(monitor);
	}

	@Override
	public void uninitializeSubSystem(IProgressMonitor monitor) {
		_root.setHostSampleObject(null);
		_root.setContents(null);
		super.uninitializeSubSystem(monitor);
		
		getSampleService().uninitService(monitor);
	}

	
	
	
}
