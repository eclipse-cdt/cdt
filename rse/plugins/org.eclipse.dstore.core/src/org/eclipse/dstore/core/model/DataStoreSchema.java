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

package org.eclipse.dstore.core.model;


/**
 * This class is used for defining the base DataStore schema.  All
 * miner schemas are derived from the schema defined here.  This class
 * provides getters for quickly getting at commonly used schema descriptors.
 */
public class DataStoreSchema
{


	private DataStore _dataStore;
	private DataElement _abstractedBy;
	private DataElement _abstracts;
	private DataElement _contents;
	private DataElement _container;
	private DataElement _attributes;
	
	private DataElement _objectDescriptor;
	private DataElement _commandDescriptor;
	private DataElement _relationDescriptor;
	
	private DataElement _abstractObjectDescriptor;
	private DataElement _abstractCommandDescriptor;
	private DataElement _abstractRelationDescriptor;


	public static final String C_VALIDATE_TICKET = "C_VALIDATE_TICKET";
	public static final String C_SET             = "C_SET";
	public static final String C_MODIFY          = "C_MODIFY";
	public static final String C_SET_HOST        = "C_SET_HOST";
	public static final String C_SCHEMA          = "C_SCHEMA";
	public static final String C_SET_PREFERENCE  = "C_SET_PREFERENCE";
	public static final String C_ADD_MINERS      = "C_ADD_MINERS";
	public static final String C_ACTIVATE_MINER  = "C_ACTIVATE_MINER";
	public static final String C_INIT_MINERS     = "C_INIT_MINERS";
	public static final String C_OPEN	           = "C_OPEN";
	public static final String C_CANCEL          = "C_CANCEL";
	public static final String C_SEND_INPUT      = "C_SEND_INPUT";
	public static final String C_QUERY           = "C_QUERY";
	public static final String C_REFRESH         = "C_REFRESH";
	public static final String C_EXIT            = "C_EXIT";
	public static final String C_CLOSE           = "C_CLOSE";
	public static final String C_NOTIFICATION    = "C_NOTIFICATION";
	public static final String C_QUERY_INSTALL   = "C_QUERY_INSTALL";
	public static final String C_QUERY_CLIENT_IP = "C_QUERY_CLIENT_IP";
	public static final String C_QUERY_JVM	   = "C_QUERY_JVM";

	
	/**
	 * Constructor
	 * @param dataStore the associated DataStore
	 */
	public DataStoreSchema(DataStore dataStore)
	{
		_dataStore = dataStore;
	}

	/**
	 * Returns the <i>abstracted by</i> relationship descriptor
	 * @return the descriptor
	 */
	public DataElement getAbstractedByRelation()
	{
		return _abstractedBy;
	}

	/**
	 * Returns the <i>abstracts</i> relationship descriptor
	 * @return the descriptor
	 */
	public DataElement getAbstractsRelation()
	{
		return _abstracts;
	}

	/**
	 * Returns the <i>contents</i> relationship descriptor
	 * @return the descriptor
	 */
	public DataElement getContentsRelation()
	{
		return _contents;
	}

	/**
	 * Returns the <i>attributes</i> relationship descriptor
	 * @return the descriptor
	 */
	public DataElement getAttributesRelation()
	{
		return _attributes;
	}

	/**
	 * Returns the <i>container</i> base object descriptor
	 * @return the descriptor
	 */
	public DataElement getContainerType()
	{
		return _container;
	}
	
	/**
	 * Returns the base object descriptor
	 * @return the descriptor
	 */
	public DataElement getObjectDescriptor()
	{
		return _objectDescriptor;
	}
	
	
	/**
	 * Returns the base command descriptor
	 * @return the descriptor
	 */
	public DataElement getCommandDescriptor()
	{
		return _commandDescriptor;
	}
	
	/**
	 * Returns the base relation descriptor
	 * @return the descriptor
	 */
	public DataElement getRelationDescriptor()
	{
		return _relationDescriptor;
	}
	
	
	/**
	 * Returns the base object descriptor
	 * @return the descriptor
	 */
	public DataElement getAbstractObjectDescriptor()
	{
		return _abstractObjectDescriptor;
	}
	
	
	/**
	 * Returns the base command descriptor
	 * @return the descriptor
	 */
	public DataElement getAbstractCommandDescriptor()
	{
		return _abstractCommandDescriptor;
	}
	
	/**
	 * Returns the base relation descriptor
	 * @return the descriptor
	 */
	public DataElement getAbstractRelationDescriptor()
	{
		return _abstractRelationDescriptor;
	}
	
	

	/**
	 * This method is called when the DataStore is initialized.  It sets
	 * up the base DataStore schema.
	 * @param schemaRoot the root object of the DataStore schema
	 */
	public void extendSchema(DataElement schemaRoot)
	{
		// miner-specific descriptors are defined in the miners when they extend the schema

		// these first elements are the most fundamental	  
		DataElement uiCmdD = _dataStore.createObject(schemaRoot, DE.T_UI_COMMAND_DESCRIPTOR, DE.T_UI_COMMAND_DESCRIPTOR);

		_commandDescriptor = _dataStore.createCommandDescriptor(schemaRoot, DE.T_COMMAND_DESCRIPTOR);
		_objectDescriptor = _dataStore.createObjectDescriptor(schemaRoot, DE.T_OBJECT_DESCRIPTOR);
		_relationDescriptor = _dataStore.createRelationDescriptor(schemaRoot, DE.T_RELATION_DESCRIPTOR);

		_abstractObjectDescriptor = _dataStore.createAbstractObjectDescriptor(schemaRoot, DE.T_ABSTRACT_OBJECT_DESCRIPTOR);
		_abstractCommandDescriptor = _dataStore.createAbstractCommandDescriptor(schemaRoot, DE.T_ABSTRACT_COMMAND_DESCRIPTOR);
		_abstractRelationDescriptor = _dataStore.createAbstractRelationDescriptor(schemaRoot, DE.T_ABSTRACT_RELATION_DESCRIPTOR);

		// cancellable command base descriptor
		DataElement cancellable = _dataStore.createAbstractObjectDescriptor(schemaRoot, DataStoreResources.model_Cancellable);

		DataElement rootD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_root);
		
		DataElement hostD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_host);

		DataElement logD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_log);
		DataElement statusD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_status);

		DataElement deletedD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_deleted);

		// misc
		DataElement allD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_all);

		DataElement invokeD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_invocation);
		DataElement patternD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_pattern);

		DataElement inputD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_input);
		DataElement outputD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_output);

		// types of relationships
		_contents = _dataStore.createRelationDescriptor(schemaRoot, DataStoreResources.model_contents);
		_contents.setDepth(100);

		DataElement descriptorForD = _dataStore.createRelationDescriptor(schemaRoot, DataStoreResources.model_descriptor_for);
		descriptorForD.setDepth(1);

		DataElement parentD = _dataStore.createRelationDescriptor(schemaRoot, DataStoreResources.model_parent);
		parentD.setDepth(1);

		_attributes = _dataStore.createRelationDescriptor(schemaRoot, "attributes");
		_attributes.setDepth(0);

		DataElement argsD = _dataStore.createRelationDescriptor(schemaRoot, DataStoreResources.model_arguments);
		_abstracts = _dataStore.createRelationDescriptor(schemaRoot, DataStoreResources.model_abstracts);

		_abstractedBy = _dataStore.createRelationDescriptor(schemaRoot, DataStoreResources.model_abstracted_by);

		DataElement caRelations = _dataStore.createAbstractRelationDescriptor(schemaRoot, DataStoreResources.model_contents_arguments);
		_dataStore.createReference(caRelations, _contents, _contents);
		_dataStore.createReference(caRelations, argsD, _contents);

		_dataStore.createReference(_objectDescriptor, _contents, _contents);
		_dataStore.createReference(_objectDescriptor, parentD, _contents);
		_dataStore.createReference(_objectDescriptor, _abstracts, _contents);
		_dataStore.createReference(_objectDescriptor, _abstractedBy, _contents);

		_dataStore.createReference(_abstractObjectDescriptor, _contents, _contents);
		_dataStore.createReference(_abstractObjectDescriptor, parentD, _contents);
		_dataStore.createReference(_abstractObjectDescriptor, _abstracts, _contents);
		_dataStore.createReference(_abstractObjectDescriptor, _abstractedBy, _contents);

		_dataStore.createReference(statusD, _contents, _contents);

		_dataStore.createReference(_commandDescriptor, allD, _contents);
		_dataStore.createReference(_commandDescriptor, caRelations, _contents);
		_dataStore.createReference(_commandDescriptor, argsD, _contents);
		_dataStore.createReference(_commandDescriptor, _contents, _contents);

		DataElement logDetails = _dataStore.createAbstractObjectDescriptor(logD, DataStoreResources.model_Commands);
		_dataStore.createReference(logDetails, _commandDescriptor, _contents);
		_dataStore.createReference(logDetails, allD, _contents);
		_dataStore.createReference(logD, caRelations, _contents);
		_dataStore.createReference(logD, _contents, _contents);

		//Base Container Object
		_container = _dataStore.createAbstractObjectDescriptor(schemaRoot, DataStoreResources.model_Container_Object);
		_dataStore.createCommandDescriptor(_container, DataStoreResources.model_Query, "*", C_QUERY, false);
		_dataStore.createReference(_container, _contents, _contents);

		// file objects
		DataElement fileD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_file);
		DataElement dirD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_directory);

		DataElement fsObject = _dataStore.createAbstractObjectDescriptor(schemaRoot, DataStoreResources.model_Filesystem_Objects);

	
		_dataStore.createReference(_container, fsObject, _abstracts, _abstractedBy);

		_dataStore.createReference(fileD, fsObject, _abstracts, _abstractedBy);
		_dataStore.createReference(fsObject, dirD, _abstracts, _abstractedBy);

		_dataStore.createReference(fsObject, fileD, _contents);
		_dataStore.createReference(fsObject, dirD, _contents);
		_dataStore.createReference(fsObject, fsObject, _contents);

		_dataStore.createReference(dirD, fileD, _contents);
		_dataStore.createReference(dirD, dirD, _contents);

		// miner descriptors
		DataElement minersD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_miners);
		DataElement minerD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_miner);
		DataElement dataD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_data);
		DataElement transientD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_transient);
		DataElement stateD = _dataStore.createObjectDescriptor(schemaRoot, DataStoreResources.model_state);
		// containers
		_dataStore.createReference(_container, rootD, _abstracts, _abstractedBy);
		_dataStore.createReference(_container, hostD, _abstracts, _abstractedBy);
		_dataStore.createReference(_container, logD, _abstracts, _abstractedBy);
		_dataStore.createReference(_container, minersD, _abstracts, _abstractedBy);
		_dataStore.createReference(_container, minerD, _abstracts, _abstractedBy);
		_dataStore.createReference(_container, dataD, _abstracts, _abstractedBy);

	

		// basic commands
		_dataStore.createCommandDescriptor(cancellable, DataStoreResources.model_Cancel, "*", C_CANCEL);
		_dataStore.createCommandDescriptor(rootD, DataStoreResources.model_Set, "-", C_SET, false);
		_dataStore.createCommandDescriptor(rootD, DataStoreResources.model_Set_Host, "-", C_SET_HOST, false);
		_dataStore.createCommandDescriptor(rootD, DataStoreResources.model_Init_Miners, "*", C_INIT_MINERS, false);
		_dataStore.createCommandDescriptor(rootD, "Add Miners", "-", C_ADD_MINERS, false);
		_dataStore.createCommandDescriptor(rootD, "Activate Miner", "-", C_ACTIVATE_MINER, false);
		_dataStore.createCommandDescriptor(rootD, "Set Preference", "-", C_SET_PREFERENCE, false);
	
		_dataStore.createCommandDescriptor(rootD, DataStoreResources.model_Show_Ticket, "-", C_VALIDATE_TICKET, false);
		_dataStore.createCommandDescriptor(rootD, DataStoreResources.model_Get_Schema, "*", C_SCHEMA, false);
		_dataStore.createCommandDescriptor(rootD, DataStoreResources.model_Exit, "*", C_EXIT, false);
		_dataStore.createCommandDescriptor(rootD, "Query Install", "*", C_QUERY_INSTALL, false);
		_dataStore.createCommandDescriptor(rootD, "Query Client IP", "*", C_QUERY_CLIENT_IP, false);
		_dataStore.createCommandDescriptor(rootD, "Query JVM", "*", C_QUERY_JVM, false);
		
		_dataStore.createCommandDescriptor(rootD, "Notification", "*", C_NOTIFICATION, false);
		_dataStore.createCommandDescriptor(rootD, "Send Input", "*", C_SEND_INPUT, false);
		
		
		// both ends have this base schema, so mark each descriptor as updated
		for (int i = 0; i < schemaRoot.getNestedSize(); i++)
		{
			DataElement descriptor = schemaRoot.get(i);
			descriptor.setUpdated(true);	
			
			for (int j = 0; j < descriptor.getNestedSize(); j++)
			{
				DataElement subDescriptor = descriptor.get(j);
				subDescriptor.setUpdated(true);	
			}
			
			schemaRoot.setUpdated(true);
		}
	}

}