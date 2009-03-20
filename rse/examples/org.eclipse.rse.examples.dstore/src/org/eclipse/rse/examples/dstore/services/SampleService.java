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
package org.eclipse.rse.examples.dstore.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.services.dstore.AbstractDStoreService;

public class SampleService extends AbstractDStoreService {

	public SampleService(IDataStoreProvider dataStoreProvider) {
		super(dataStoreProvider);
	}

	@Override
	protected String getMinerId() {
 		return "org.eclipse.rse.examples.dstore.miners.SampleMiner";
	}
 
	public IHostSampleContainer getContainer(String name, IProgressMonitor monitor){
		DataElement universalTemp = getMinerElement();		
		DataElement containerElement = getDataStore().createObject(universalTemp, "Sample Container", name,"", "", false); //$NON-NLS-1$

		return new HostSampleContainer(containerElement);
	}
	
	public IHostSampleObject[] query(IHostSampleContainer container, IProgressMonitor monitor){
		DataElement containerElement = container.getDataElement();
		if (!isInitialized())
		{
			waitForInitialize(monitor);
		}

		DataElement[] results = dsQueryCommand(containerElement, "C_SAMPLE_QUERY", monitor);
		
		List<IHostSampleObject> returned = new ArrayList<IHostSampleObject>();
		for (int i = 0; i < results.length; i++){
			DataElement result = results[i];
			if (result.isDeleted()){
				// don't add deleted items
			}
			else if (result.getType().equals("Sample Container")){
				returned.add(new HostSampleContainer(result));
			}
			else {
				returned.add(new HostSampleObject(result));
			}
		}
		
		return (IHostSampleObject[])returned.toArray(new IHostSampleObject[returned.size()]);
	}

	public void doAction(IHostSampleObject object, IProgressMonitor monitor){
		DataElement subject = object.getDataElement();		
		dsStatusCommand(subject, "C_SAMPLE_ACTION", monitor);		
	}
	
	public void createSampleObject(IHostSampleObject object, IProgressMonitor monitor){
		DataElement subject = object.getDataElement();		
		dsStatusCommand(subject, "C_CREATE_SAMPLE_OBJECT", monitor);		
	}
	
	public void createSampleContainer(IHostSampleObject object, IProgressMonitor monitor){
		DataElement subject = object.getDataElement();		
		dsStatusCommand(subject, "C_CREATE_SAMPLE_CONTAINER", monitor);		
	}
	

	public boolean rename(IHostSampleObject object, String newName, IProgressMonitor monitor){
		DataElement subject = object.getDataElement();		
		
		ArrayList<DataElement> args = new ArrayList<DataElement>();		
		DataElement newNameArg = getDataStore().createObject(null, "Name", newName);		
		args.add(newNameArg);
		
		dsStatusCommand(subject, args, "C_RENAME", monitor);		
		return true;
	}
	
	public boolean delete(IHostSampleObject object, IProgressMonitor monitor){
		DataElement subject = object.getDataElement();		
		
		dsStatusCommand(subject, "C_DELETE", monitor);		
		
		return true;
	}
}
