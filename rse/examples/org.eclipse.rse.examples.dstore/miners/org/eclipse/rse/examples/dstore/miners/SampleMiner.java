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
package org.eclipse.rse.examples.dstore.miners;

import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreResources;

public class SampleMiner extends Miner {

	public String getVersion() {
		return "1.0.0";
	}


	protected void load() {

	}

	public DataElement handleCommand(DataElement theElement) throws Exception {
		String         name = getCommandName(theElement);
		DataElement  status = getCommandStatus(theElement);
		DataElement subject = getCommandArgument(theElement, 0);
				
		if (name.equals("C_SAMPLE_QUERY")){ //$NON-NLS-1$
			return handleSampleQuery(subject, status);
		}
		else if (name.equals("C_SAMPLE_ACTION")){//$NON-NLS-1$
			return handleSampleAction(subject, status);
		}		
		else if (name.equals("C_RENAME")){
			return handleRename(subject, getCommandArgument(theElement, 1), status);
		}
		else if (name.equals("C_DELETE")){
			return handleDelete(subject, status);
		}
		else if (name.equals("C_CREATE_SAMPLE_OBJECT")){
			return handleCreateSampleObject(subject, status);
		}
		else if (name.equals("C_CREATE_SAMPLE_CONTAINER")){
			return handleCreateSampleContainer(subject, status);
		}
		return status;
	}

	public void extendSchema(DataElement schemaRoot) {
		
		DataElement sampleContainer = _dataStore.createObjectDescriptor(schemaRoot, "Sample Container"); //$NON-NLS-1$
		DataElement sampleObject = _dataStore.createObjectDescriptor(schemaRoot, "Sample Object"); //$NON-NLS-1$
		_dataStore.createReference(sampleObject, sampleContainer, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		
		createCommandDescriptor(sampleContainer, "Query", "C_SAMPLE_QUERY", false); //$NON-NLS-1$ //$NON-NLS-2$
		createCommandDescriptor(sampleObject, "Do Something", "C_SAMPLE_ACTION", false); //$NON-NLS-1$ //$NON-NLS-2$
		createCommandDescriptor(sampleObject, "Rename", "C_RENAME", false); //$NON-NLS-1$ //$NON-NLS-2$
		createCommandDescriptor(sampleObject, "Delete", "C_DELETE", false); //$NON-NLS-1$ //$NON-NLS-2$
		createCommandDescriptor(sampleObject, "Create Object", "C_CREATE_SAMPLE_OBJECT", false);
		createCommandDescriptor(sampleObject, "Create Container", "C_CREATE_SAMPLE_CONTAINER", false);		
		
		_dataStore.refresh(schemaRoot);
	}

	private DataElement handleSampleQuery(DataElement parent, DataElement status){
		String value = parent.getValue();
		if (!value.equals("queried")){
			_dataStore.createObject(parent, "Sample Object", "New Object 1"); //$NON-NLS-1$ //$NON-NLS-2$
			_dataStore.createObject(parent, "Sample Object", "New Object 2"); //$NON-NLS-1$ //$NON-NLS-2$
			_dataStore.createObject(parent, "Sample Container", "New Container 3"); //$NON-NLS-1$ //$NON-NLS-2$ 
		
			parent.setAttribute(DE.A_VALUE, "queried");
			_dataStore.refresh(parent);
		}
		status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
		return status;
	}
	
	private DataElement handleSampleAction(DataElement parent, DataElement status){
		parent.setAttribute(DE.A_NAME, parent.getName() + " - modified");
		_dataStore.refresh(parent.getParent());
		status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
		return status;
	}
	
	private DataElement handleRename(DataElement parent, DataElement newName, DataElement status){
		parent.setAttribute(DE.A_NAME, newName.getName());
		_dataStore.refresh(parent.getParent());
		status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
		return status;
	}
	
	private DataElement handleDelete(DataElement subject, DataElement status){	
		DataElement parent = subject.getParent();
		_dataStore.deleteObject(parent, subject);
		_dataStore.refresh(parent);
		status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
		return status;
	}
	
	private DataElement handleCreateSampleObject(DataElement parent, DataElement status){
		_dataStore.createObject(parent, "Sample Object", "Untitled"); //$NON-NLS-1$ //$NON-NLS-2$
		_dataStore.refresh(parent);
		status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
		return status;
	}
	
	private DataElement handleCreateSampleContainer(DataElement parent, DataElement status){
		_dataStore.createObject(parent, "Sample Container", "Untitled Container"); //$NON-NLS-1$ //$NON-NLS-2$
		_dataStore.refresh(parent);
		status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
		return status;
	}
}
