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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dstore.core.util.DataElementRemover;
import org.eclipse.dstore.core.util.StringCompare;
import org.eclipse.dstore.extra.internal.extra.DataElementActionFilter;
import org.eclipse.dstore.extra.internal.extra.DesktopElement;
import org.eclipse.dstore.extra.internal.extra.IDataElement;
import org.eclipse.dstore.extra.internal.extra.PropertySource;

/**
 * <code>DataElement</code> is the unit of information for the <code>DataStore</code>.  All objects including
 * schema descriptors, commands and instance objects are represented by <code>DataElement</code>s.
 * <code>DataElement</code>s should not be constructed directly, rather they are requested via the createObject()
 * method in <code>DataStore</code>
 *
 */
public final class DataElement implements IDataElement
{


	private String _attributes[];
	private StringBuffer _buffer;

	private boolean _isReference = false;
	private boolean _isDescriptor = false;
	private boolean _isExpanded = false;
	private boolean _isUpdated = false;
	private boolean _isPendingTransfer = false;
	private boolean _isSpirit = false;
	
	private int _depth = 1;

	private DataStore _dataStore = null;
	private DataElement _parent = null;
	private DataElement _descriptor = null;

	private ArrayList _nestedData = null;
	private DataElement _referencedObject = null;

	private PropertySource _propertySource = null;


	/**
	 * Creates a new <code>DataElement</code> without initializing it.
	 *
	 */
	public DataElement()
	{
		_dataStore = null;
		_parent = null;
		DataElementRemover.addToCreatedCount();
	}

	/**
	 * Creates a new <code>DataElement</code> without initializing it.
	 *
	 * @param dataStore the owner <code>DataStore</code> for this element
	 */
	protected DataElement(DataStore dataStore)
	{
		_dataStore = dataStore;
		_parent = null;
		DataElementRemover.addToCreatedCount();
	}
	
	protected void finalize()
	{
		DataElementRemover.addToGCedCount();
	}

	/**
	 * Initializes a <code>DataElement</code> to be reference to some other <code>DataElement</code>.  
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the element that contains this reference
	 * @param originalObject the element that gets referenced
	 * @param refType the type descriptor of the reference
	 */
	public void reInit(DataElement parent, DataElement originalObject, DataElement refType)
	{
		if ((parent != null) && (originalObject != null))
		{
			_parent = parent;

			_attributes = getAttributes();
			String type = refType.getName();
			_attributes[DE.A_TYPE] = type;
			
			String originalId = originalObject.getId();
			StringBuffer idBuf = new StringBuffer(parent.getId());
			idBuf.append(type);
			idBuf.append(originalId);
			_attributes[DE.A_ID] = idBuf.toString();
			_attributes[DE.A_NAME] = originalId;
			_attributes[DE.A_VALUE] = originalId;

			initialize(refType);

			_referencedObject = originalObject;
			_isReference = true;
		}
	}

	/**
	 * Initializes a <code>DataElement</code> to be reference to some other <code>DataElement</code> 
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the element that contains this reference
	 * @param originalObject the element that gets referenced
	 * @param refType the a string representing the type of reference
	 */
	public void reInit(DataElement parent, DataElement originalObject, String refType)
	{
		if ((parent != null) && (originalObject != null))
		{
			_parent = parent;

			_attributes = getAttributes();
			_attributes[DE.A_TYPE] = refType;
			
			String originalId = originalObject.getId();
			StringBuffer idBuf = new StringBuffer(parent.getId());
			idBuf.append(refType);
			idBuf.append(originalId);
			
			_attributes[DE.A_ID] = idBuf.toString();
			_attributes[DE.A_NAME] = originalId;
			_attributes[DE.A_VALUE] = originalId;

			initialize();

			_referencedObject = originalObject;
			_isReference = true;
		}
	}

	/**
	 * Initializes a <code>DataElement</code> 
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the parent of the element
	 * @param type the type descriptor of the element
	 * @param id the ID of the element
	 * @param name the name of the element
	 * @param source the source location of the element
	 */
	public void reInit(DataElement parent, DataElement type, String id, String name, String source)
	{
		_parent = parent;

		_attributes = getAttributes();
		_attributes[DE.A_TYPE] = type.getAttribute(DE.A_NAME);
		_attributes[DE.A_ID] = id;
		_attributes[DE.A_NAME] = name;
		_attributes[DE.A_VALUE] = name;
		_attributes[DE.A_SOURCE] = source;

		initialize(type);
	}

	/**
	 * Initializes a <code>DataElement</code> 
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the parent of the element
	 * @param type a string representing the type descriptor of the element
	 * @param id the ID of the element
	 * @param name the name of the element
	 * @param source the source location of the element
	 */
	public void reInit(DataElement parent, String type, String id, String name, String source)
	{
		_parent = parent;

		_attributes = getAttributes();
		_attributes[DE.A_TYPE] = type;
		_attributes[DE.A_ID] = id;
		_attributes[DE.A_NAME] = name;
		_attributes[DE.A_VALUE] = name;
		_attributes[DE.A_SOURCE] = source;

		initialize();
	}

	/**
	 * Initializes a <code>DataElement</code> 
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the parent of the element
	 * @param type the type descriptor of the element
	 * @param id the ID of the element
	 * @param name the name of the element
	 * @param source the source location of the element
	 * @param isRef an indication of whether the element is a reference or not
	 */
	public void reInit(DataElement parent, DataElement type, String id, String name, String source, boolean isRef)
	{
		_parent = parent;

		_attributes = getAttributes();
		_attributes[DE.A_TYPE] = type.getAttribute(DE.A_NAME);
		_attributes[DE.A_ID] = id;
		_attributes[DE.A_NAME] = name;
		_attributes[DE.A_VALUE] = name;
		_attributes[DE.A_SOURCE] = source;

		initialize(type);
		_isReference = isRef;
	}

	/**
	 * Initializes a <code>DataElement</code> 
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the parent of the element
	 * @param type a string representing the type descriptor of the element
	 * @param id the ID of the element
	 * @param name the name of the element
	 * @param source the source location of the element
	 * @param isRef an indication of whether the element is a reference or not
	 */
	public void reInit(DataElement parent, String type, String id, String name, String source, boolean isRef)
	{
		_parent = parent;

		_attributes = getAttributes();
		_attributes[DE.A_TYPE] = type;
		_attributes[DE.A_ID] = id;
		_attributes[DE.A_NAME] = name;
		_attributes[DE.A_VALUE] = name;
		_attributes[DE.A_SOURCE] = source;

		initialize();
		_isReference = isRef;
	}

	/**
	 * Initializes a <code>DataElement</code> 
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the parent of the element
	 * @param type the type descriptor of the element
	 * @param attributes the attributes for this element (name, source, id, etc.) 
	 */
	public void reInit(DataElement parent, DataElement type, String[] attributes)
	{
		_parent = parent;

		_attributes = attributes;
		_attributes[DE.A_TYPE] = type.getName();

		initialize(type);
	}

	/**
	 * Initializes a <code>DataElement</code> 
	 * This method should only be called from the <code>DataStore</code>.
	 *
	 * @param parent the parent of the element
	 * @param attributes the attributes for this element (type, name, source, id, etc.) 
	 */
	public void reInit(DataElement parent, String[] attributes)
	{
		_parent = parent;

		_attributes = attributes;

		initialize();
	}
	
	public void reInitAsTransient(String attributes[])
	{
		_attributes = attributes;
		
		_isReference = false;
		_isDescriptor = false;
		_depth = 2;

		_referencedObject = null;
		_isExpanded = false;
		_isUpdated = false;
		
		String depthStr = getAttribute(DE.A_DEPTH);
		if (depthStr != null && depthStr.length() > 0)
		{
			if (!depthStr.equals("2"))
			{
				try
				{
					_depth = Integer.parseInt(depthStr);
				}
				catch (Exception e)
				{
				}
			}
		}
		
		if (_nestedData != null)
			_nestedData.clear();
	}

	/**
	 * Indicates whether the <code>DataElement</code> is deleted or not.
	 *
	 * @return whehther the element is deleted or not
	 */
	public boolean isDeleted()
	{
		if (_attributes == null)
		{
			return true;
		}

		String valueAttribute = getAttribute(DE.A_VALUE);

		if (valueAttribute != null && valueAttribute.equals(DataStoreResources.DELETED))
		{
			return true;
		}
		
		if (_isSpirit && !_dataStore.isVirtual())
		{
			return true; 
		}
		
		return false;
	}

	/**
	 * Adds a set of elements as children to this element.
	 *
	 * @param nestedData a set of elements to add to this element 
	 * @param checkUnique whether to prevent duplicates from being added
	 */
	public synchronized void addNestedData(List nestedData, boolean checkUnique)
	{
		if (nestedData != null)
		{
			if (_nestedData == null)
			{
				_nestedData = new ArrayList(nestedData.size());
			}

			for (int i = 0; i < nestedData.size(); i++)
			{
				DataElement child = (DataElement) nestedData.get(i);
				if (child != null && child != this)
				{
					addNestedData(child, checkUnique);
				}
			}
		}
	}

	/**
	 * Adds another element as a child to this element.
	 *
	 * @param obj the element to add 
	 * @param checkUnique whether to prevent duplicates from being added
	 */
	public synchronized void addNestedData(DataElement obj, boolean checkUnique)
	{
		if (_nestedData == null)
		{
			_nestedData = new ArrayList(4);
		}

		synchronized (_nestedData)
		{
			boolean alreadyThere = false;
			if (checkUnique)
			{
				alreadyThere = _nestedData.contains(obj);
			}

			if (!checkUnique || !alreadyThere)
			{
				if (alreadyThere)
				{
					return;
				}
				else
				{
					_nestedData.add(obj);

					if (obj.getParent() == null)
						obj.setParent(this);
				}
			}

			_isUpdated = false;
			obj.setUpdated(false);
		}
	}

	/**
	 * Removes a specified child element from this element.
	 *
	 * @param object the element to remove 
	 */
	public synchronized void removeNestedData(DataElement object)
	{
		if (_nestedData != null)
		{
			synchronized(_nestedData)
			{
				_nestedData.remove(object);				
			}
		}
		_isExpanded = false;
		_isUpdated = false;
	}

	/**
	 * Removes all the children from this element.
	 */
	public synchronized void removeNestedData()
	{
		if (_nestedData != null)
		{
			synchronized (_nestedData)
			{
				while (_nestedData.size() > 0)
				{
					DataElement nestedObject = (DataElement) _nestedData.get(0);
					_nestedData.remove(nestedObject);
				}
			}
		}

		_isExpanded = false;
		_isUpdated = false;
	}

	/**
	 * Returns an attribute of this element.
	 *
	 * @param attributeIndex the index of the element 
	 * @return the attribute 
	 */
	public String getAttribute(int attributeIndex)
	{
		return _attributes[attributeIndex];
	}

	/**
	 * Returns the set of attributes for this element.
	 *
	 * @return the set of attributes 
	 */
	public String[] getAttributes()
	{
		if (_attributes == null)
		{
			return new String[DE.A_SIZE];
		}
		return _attributes;
	}

	/**
	 * Returns the type attribute for this element.
	 *
	 * @return the type attribute 
	 */
	public String getType()
	{
		return getAttribute(DE.A_TYPE);
	}

	/**
	 * Returns the ID attribute for this element.
	 *
	 * @return the ID attribute 
	 */
	public String getId()
	{
		return getAttribute(DE.A_ID);
	}

	/**
	 * Returns the name attribute for this element.
	 *
	 * @return the name attribute 
	 */
	public String getName()
	{
		return getAttribute(DE.A_NAME);
	}

	/**
	 * Returns the value attribute for this element.
	 *
	 * @return the value attribute 
	 */
	public String getValue()
	{
		return getAttribute(DE.A_VALUE);
	}

	/**
	 * Returns the source attribute for this element.
	 *
	 * @return the source attribute 
	 */
	public String getSource()
	{
		return getAttribute(DE.A_SOURCE);
	}
	
	/**
	 * Returns the buffer for this element.
	 *
	 * @return the buffer 
	 */
	public StringBuffer getBuffer()
	{
		return _buffer;
	}

	/**
	 * Returns the <code>DataStore</code> for this element.
	 *
	 * @return the <code>DataStore</code> 
	 */
	public DataStore getDataStore()
	{
		return _dataStore;
	}

	/**
	 * Initializes the children set of this element with a specified size.
	 *
	 * @param size the initial size 
	 */
	public void initializeNestedData(int size)
	{
		if (_nestedData == null)
		{
			_nestedData = new ArrayList(size);
		}
	}

	/**
	 * Returns the children of this element.
	 *
	 * @return the children of this element 
	 */
	public List getNestedData()
	{
		return _nestedData;
	}

	/**
	 * Returns the child at the specified index.
	 *
	 * @param index the index of the child to retrieve 
	 * @return the child element 
	 */
	public DataElement get(int index)
	{
		if (_nestedData == null)
		{
			return null;
		}
		else
		{
			if (getNestedSize() > index)
			{
				Object obj = _nestedData.get(index);
				return (DataElement) obj;
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * Returns the number of children this element contains.
	 *
	 * @return the number of children 
	 */
	public int getNestedSize()
	{
		if (_nestedData == null)
		{
			return 0;
		}
		else
		{
			return _nestedData.size();
		}
	}

	/**
	 * Returns the parent of this element.
	 *
	 * @return the parent 
	 */
	public DataElement getParent()
	{
		return _parent;
	}

	public boolean isDescriptor()
	{
		return _isDescriptor;
	}

	/**
	 * Explicitly sets the type descriptor for this element.
	 *
	 * @param theDescriptor the type descriptor for this element 
	 */
	public void setDescriptor(DataElement theDescriptor)
	{
		_descriptor = theDescriptor;
	}

	/**
	* Returns the type descriptor for this element.
	*
	* @return the type descriptor for this element 
	*/
	public DataElement getDescriptor()
	{
		if (isDeleted())
		{
			return null;
		}

		if (_isReference)
		{
			if (_referencedObject == null)
			{
				_referencedObject = dereference();
			}
			if (this == _referencedObject || _referencedObject.isDeleted())
			{
				_referencedObject = null;
			}
			else
			{
				return _referencedObject.getDescriptor();
			}
		}
		else if ((_descriptor == null) && (_dataStore != null))
		{
			if (_isDescriptor)
			{
				_descriptor = _dataStore.findDescriptor(getAttribute(DE.A_TYPE), getName());
			}
			else
			{
				_descriptor = _dataStore.findObjectDescriptor(getAttribute(DE.A_TYPE));
				if (_descriptor == null)
				{
					_descriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, getAttribute(DE.A_TYPE), 3);
				}
			}
		}

		return _descriptor;
	}


	/**
	 * Returns the visibility of this element.
	 *
	 * @return the level of visibility for this element 
	 */
	public int depth()
	{
		return _depth;
	}

	/**
	 * Indicates whether this is a reference or not.
	 *
	 * @return whether this is a reference or not 
	 */
	public boolean isReference()
	{
		return _isReference;
	}

	/**
	 * Indicates whether this element has been queried for it's children.
	 *
	 * @return whether element has been expanded 
	 */
	public boolean isExpanded()
	{
		return _isExpanded;
	}

	/**
	 * Indicates whether this element has been updated yet. 
	 * On a server, an element is updated if it has been transfered the the client.
	 * On a client, an element is updated if a notification has been sent out for the ui
	 *
	 * @return whether element has been updated yet 
	 */
	public boolean isUpdated()
	{
		return _isUpdated;
	}

	/**
	 * Indicates whether this element is pending a transfer 
	 * If the element is queued to be sent, the value will be true.  
	 *
	 * @return whether element has been updated yet 
	 */
	public boolean isPendingTransfer()
	{
		return _isPendingTransfer;
	}
	
	/**
	 * Indicates whether this element is a 'spirit' element. 
	 * In a client datastore, this means that the element's counterpart on the
	 * server is either also a spirit (and will be deleted soon) or has already
	 * been deleted. In a server datastore, this means that the element is
	 * to be deleted at the next opportunity in order to free memory.
	 */
	public boolean isSpirit()
	{
		return _isSpirit;
	}
	
	/**
	 * Sets an attribute of the element. 
	 *
	 * @param attributeIndex the index of the attribute to set 
	 * @param attribute the new value for the specified attribute 
	 */
	public void setAttribute(int attributeIndex, String attribute)
	{
		if (attribute != null)
		{
			if ((attributeIndex == DE.A_NAME) && (getAttribute(DE.A_NAME).equals(getAttribute(DE.A_VALUE))))
			{
				_attributes[DE.A_VALUE] = attribute;
			}
			else if (attributeIndex == DE.A_DEPTH)
			{
				_depth = Integer.parseInt(attribute);
			}
			else if (attributeIndex == DE.A_TYPE)
			{
				_descriptor = null;
			}

			_attributes[attributeIndex] = attribute;
			_isUpdated = false;
		}
	}

	/**
	 * Sets all of the attributes of the element. 
	 *
	 * @param attributes the new set of attributes for the element 
	 */
	public void setAttributes(String attributes[])
	{
		for (int i = 0; i < DE.A_SIZE; i++)
		{
			if (attributes[i] != null)
			{
				setAttribute(i, attributes[i]);
			}
		}

		_isUpdated = false;
	}

	/**
	 * Sets the buffer for this element. 
	 * The buffer is used if extra temporary information needs to be stored
	 * with this element
	 *
	 * @param buffer the new buffer for this element 
	 */
	public void setBuffer(StringBuffer buffer)
	{
		_buffer = buffer;
		if (_depth < 2 && buffer.length() > 0)
		{
			setDepth(2);
		}

		_isUpdated = false;
	}

	/**
	 * Appends to the buffer for this element. 
	 * The buffer is used if extra temporary information needs to be stored
	 * with this element
	 *
	 * @param text text to append to the buffer 
	 */
	public void appendToBuffer(String text)
	{
		if (_buffer == null)
		{
			_buffer = new StringBuffer();
		}
		_buffer.append(text);
		_isUpdated = false;
	}

	/**
	 * Sets the expanded indication for this element. 
	 *
	 * @param flag whether the element is expanded or not 
	 */
	public void setExpanded(boolean flag)
	{
		_isExpanded = flag;
	}

	/**
	 * Sets the updated indication for this element. 
	 *
	 * @param flag whether the element is updated or not 
	 */
	public void setUpdated(boolean flag)
	{
		_isUpdated = flag;
	}
	
	/**
	 * Sets indication of whether this element is waiting to be transferred.
	 * If so, the updated flag should not be set to true
	 * 
	 * @param flag whether the element is waiting to be transferred
	 */
	public void setPendingTransfer(boolean flag)
	{
		_isPendingTransfer = flag;
	}
	
	/**
	 * Sets indication of whether this element is a 'spirit' element. 
	 * In a client datastore, this means that the element's counterpart on the
	 * server is either also a spirit (and will be deleted soon) or has already
	 * been deleted. In a server datastore, this means that the element is
	 * to be deleted at the next opportunity in order to free memory.
	 */
	public void setSpirit(boolean flag)
	{
		_isSpirit = flag;
	}

	/**
	 * Sets the parent for this element. 
	 *
	 * @param parent the new parent 
	 */
	public void setParent(DataElement parent)
	{
		_parent = parent;
	}

	/**
	 * Sets the <code>DataStore</code> for this element. 
	 *
	 * @param dataStore the new dataStore 
	 */
	public void setDataStore(DataStore dataStore)
	{
		_dataStore = dataStore;
	}

	/**
	 * Sets the depth of visibility for this element. 
	 *
	 * @param depth the level of visibility 
	 */
	public void setDepth(int depth)
	{
		_depth = depth;
		setAttribute(DE.A_DEPTH, "" + _depth);
		_isUpdated = false;
		//_dataStore.refresh(this);
	}

	/**
	 * Tests if this element is of the specified type. 
	 *
	 * @param typeStr a string representing the type descriptor to compare with 
	 * @return whether the element is of the specified type
	 */
	public boolean isOfType(String typeStr)
	{
		DataElement typeDescriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, typeStr, 1);
		return isOfType(typeDescriptor);
	}

	/**
	 * Tests if this element is of the specified type. 
	 *
	 * @param typeStr a string representing the type descriptor to compare with 
	 * @return whether the element is of the specified type
	 */
	public boolean isOfType(String typeStr, boolean isDescriptor)
	{
		DataElement typeDescriptor = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, typeStr, 1);
		return isOfType(typeDescriptor, isDescriptor);
	}

	/**
	 * Tests if this element is of the specified type. 
	 *
	 * @param type the type descriptor to compare with 
	 * @return whether the element is of the specified type
	 */
	public boolean isOfType(DataElement type)
	{
		return isOfType(type, false);
	}

	/**
	 * Tests if this element is of the specified type. 
	 *
	 * @param type the type descriptor to compare with 
	 * @param isDescriptor whehter this element is a descriptor or an instance object 
	 * @return whether the element is of the specified type
	 */
	public boolean isOfType(DataElement type, boolean isDescriptor)
	{
		boolean result = false;

		if (type == null)
		{
			return result;
		}

		DataElement descriptor = this;
		if (!isDescriptor)
		{
			descriptor = getDescriptor();
		}

		if (descriptor == type)
		{
			return true;
		}

		if (descriptor != null && !descriptor.isDeleted())
		{
			String typeType = type.getType();
			String typeName = type.getName();
			if (typeType.equals(DE.T_OBJECT_DESCRIPTOR) || typeType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR))
			{
				if (descriptor.getName().equals(typeName) || (typeName.equals("all")))
				{
					result = true;
					return result;
				}
			}

			DataElement relationship = _dataStore.getAbstractedByRelation();
			List abstracted = null;

			if (relationship != null)
			{
				abstracted = descriptor.getAssociated(relationship);
			}

			for (int i = 0;(i < abstracted.size()) && !result; i++)
			{
				DataElement superDescriptor = (DataElement) abstracted.get(i);

				result = superDescriptor.isOfType(type, true);
			}
		}

		return result;
	}

	/**
	 * Tests if this element matches the specified patterns. 
	 *
	 * @param attributes the attribute indexes to compare with 
	 * @param patterns the values to compare the specified attributes with 
	 * @param numAttributes the number of attributes to compare 
	 * @param ignoreCase whether to ignore case or not 
	 * @return whether the element matches the patterns
	 */
	public boolean patternMatch(int attributes[], String patterns[], int numAttributes, boolean ignoreCase)
	{
		int index = 0;
		while (index < numAttributes)
		{
			String attribute = getAttribute(attributes[index]);
			String pattern = patterns[index];

			if (!StringCompare.compare(pattern, attribute, ignoreCase))
			{
				return false;
			}
			index++;
		}

		return true;
	}

	/**
	 * Tests if this element contains a specified element in a particular relationship. 
	 *
	 * @param object the object to look for 
	 * @param property relationship under which to find the object 
	 * @return whether the element is found
	 */
	public boolean contains(DataElement object, DataElement property)
	{
		return contains(object, property, 1);
	}

	/**
	 * Tests if this element contains a specified element in a particular relationship. 
	 *
	 * @param object the object to look for 
	 * @param property relationship under which to find the object 
	 * @param depth how deep to search for the specified element 
	 * @return whether the element is found
	 */
	public synchronized boolean contains(DataElement object, DataElement property, int depth)
	{
		if (depth > 0)
		{
			depth--;

			if (object == null)
			{
				return false;
			}

			if (property == null)
			{
				property = _dataStore.getContentsRelation();
			}

			if (getNestedSize() == 0)
			{
				return false;
			}

			if (property != null)
			{
				if (property.getType().equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
				{
					for (int i = 0; i < property.getNestedSize(); i++)
					{
						DataElement subProperty = property.get(i).dereference();
						if (contains(object, subProperty))
						{
							return true;
						}
					}
				}
				else
				{
					String type = property.getName();
					if (type.equals(DataStoreResources.model_parent) && (_parent != null))
					{
						if (object == _parent)
						{
							return true;
						}
						else
						{
							return _parent.contains(object, property, depth);
						}
					}
					else if (_nestedData != null)
					{
						for (int i = 0; i < _nestedData.size(); i++)
						{
							DataElement nestedObject = (DataElement) _nestedData.get(i);
							if (nestedObject != null)
							{
								if (nestedObject.isReference())
								{
									String relType = nestedObject.getAttribute(DE.A_TYPE);
									if (relType.equals(type))
									{
										DataElement referenced = nestedObject.dereference();

										if (referenced == object)
										{
											return true;
										}
										else
										{
											return referenced.contains(object, property, depth);
										}
									}
								}
								else if (type.equals("contents"))
								{
									if (nestedObject == object)
									{
										return true;
									}
									else
									{
										if (nestedObject.contains(object, property, depth))
										{
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Tests if this element contains a specified element in the default contents relationship. 
	 *
	 * @param object the object to look for 
	 * @return whether the element is found
	 */
	public boolean contains(DataElement object)
	{
		return contains(object, 1);
	}

	/**
	 * Tests if this element contains a specified element in the default contents relationship. 
	 *
	 * @param object the object to look for 
	 * @param depth how deep to search 
	 * @return whether the element is found
	 */
	public boolean contains(DataElement object, int depth)
	{
		boolean result = false;
		if (_nestedData != null)
		{
			depth--;
			for (int i = 0; i < getNestedSize(); i++)
			{
				DataElement child = get(i);

				if (child == object)
				{
					return true;
				}
				else if (child.dereference() == object)
				{
					return true;
				}
				else if (depth > 0)
				{
					result = child.contains(object, depth);
					if (result)
					{
						return true;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Tests if this element is the same as another. 
	 *
	 * @param arg the object to compare with 
	 * @return whether the element is the same
	 */
	public boolean equals(Object arg)
	{
		if (arg instanceof DataElement)
		{
			return arg == this;
		}

		return false;
	}

	/**
	 * Gets the set of elements that are related to this element via a specified relationship. 
	 *
	 * @param propertyStr a string representing the relationship that is required  
	 * @return the set of related elements
	 */
	public List getAssociated(String propertyStr)
	{
		DataElement property = _dataStore.findObjectDescriptor(propertyStr);
		if (property == null)
		{
		    property = _dataStore.findRelationDescriptor(propertyStr);
		}
		if (property != null)
		{
			return getAssociated(property);
		}
		else
		{
			return new ArrayList(1);
		}
	}

	/**
	 * Gets the set of elements that are related to this element via a specified relationship. 
	 *
	 * @param property the relationship that is required  
	 * @return the set of related elements
	 */
	public List getAssociated(DataElement property)
	{
		ArrayList set = new ArrayList();

		if (property == null || getNestedSize() == 0)
		{
			return set;
		}
		else
		{
			if (property.getType().equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
			{
				// recursively concat the abstracted matches
				for (int i = 0; i < property.getNestedSize(); i++)
				{
					DataElement subProperty = property.get(i).dereference();
					List subSet = getAssociated(subProperty);
					set.addAll(subSet);
				}
			}
			else
			{
				String type = property.getName();
				if (type.equals(DataStoreResources.model_parent) && (_parent != null))
				{
					set.add(_parent);
					return set;
				}
				else if (type.equals(DataStoreResources.model_descriptor_for))
				{
					getDescriptor();
					if (_descriptor != null)
						set.add(_descriptor);
				}
				else if (_nestedData != null)
				{
					for (int i = 0; i < _nestedData.size(); i++)
					{
						DataElement nestedObject = (DataElement) _nestedData.get(i);
						if (nestedObject != null)
						{
							if (nestedObject.isReference())
							{
								String relType = nestedObject.getType();
								if (relType.equals(type))
								{
									DataElement referenced = nestedObject.dereference();
									if ((referenced != null) && !referenced.isDeleted())
									{
										set.add(referenced);
									}
								}
							}
							else if (type.equals(DataStoreResources.model_contents))
							{
								if (!nestedObject.isDeleted())
									set.add(nestedObject);
							}
						}
					}
				}
			}
		}

		return set;
	}

	/**
	 * Returns the element that this references.
	 * If the element is not a reference, itself is returned
	 *
	 * @return the element that this references
	 */
	public DataElement dereference()
	{
		if (_isReference)
		{
			String name = getAttribute(DE.A_NAME);
			if ((_referencedObject != null))
			{
				if (_referencedObject.getId().equals(name))
				{
					return _referencedObject;
				}
				else
				{
					_referencedObject = null;
					delete();
					return null;
				}
			}
			else
			{
				_referencedObject = _dataStore.find(name);
				if ((_referencedObject != null))
				{
					return _referencedObject;
				}
				else
				{
					return null;
				}
			}
		}
		else
		{
			return this;
		}
	}

	/**
	 * Do the specified command on this element.
	 * This element becomes the subject of a command that has a value, command
	 *
	 * @param command the string representing the command to issue
	 * @param isSynchronized an indication of whether this command should be synchronized
	 * @return the status of the command
	 */
	public DataElement doCommandOn(String command, boolean isSynchronized)
	{
		DataElement status = null;
		if ((_dataStore != null) && (_dataStore.isConnected()))
		{
			DataElement cmdDescriptor = _dataStore.localDescriptorQuery(getDescriptor(), command);
			if (cmdDescriptor != null)
			{
				if (isSynchronized)
				{
					status = _dataStore.synchronizedCommand(cmdDescriptor, this);
				}
				else
				{
					status = _dataStore.command(cmdDescriptor, this);
				}
			}
		}
		return status;
	}

	

	/**
	 * Gets the adapter specified by key for this element
	 *
	 * @param key the identifier for this adapter
	 * @return the adapter
	 */
	public Object getAdapter(Class key)
	{
		Object adapter = DesktopElement.getPlatformAdapter(this, key);
		if (adapter != null)
		{
			return adapter;
		}
		else if (PropertySource.matches(key))
		{
			if (_propertySource == null)
			{
				_propertySource = new PropertySource(this);
			}

			return _propertySource;
		}
		else if (DataElementActionFilter.matches(key))
		{
			return DataElementActionFilter.getInstance();
		}
		return null;
	}

	/**
	 * Returns a string showing the attributes of this element
	 *
	 * @return a printable string
	 */
	public String toString()
	{
		return "DataElement "
			+ (_isReference ? "reference" : "")
			+ "\n{\n\tType:\t"
			+ getType()
			+ "\n\tName:\t"
			+ getName()
			+ "\n\tValue:\t"
			+ getValue()
			+ "\n\tID:\t"
			+ getId()
			+ "\n\tSource:\t"
			+ getSource()
			+ "\n\tDepth:\t"
			+ _depth
			+ "\n\tDataStore:\t"
			+ _dataStore.getName()
			+ "\n}\n";
	}

	/**
	 * Returns the property identified by name.
	 *
	 * @param name a specifier of which property to return
	 * @return the specified property
	 */
	public Object getElementProperty(Object name)
	{
		return getElementProperty(name, 3);
	}

	private Object getElementProperty(Object name, int depth)
	{
		if (depth == 0)
		{
			return null;
		}

		if (_isReference)
		{
			if (_referencedObject == null)
			{
				dereference();
			}

			if (_referencedObject != null)
			{
				return _referencedObject.getElementProperty(name, depth - 1);
			}
			else
			{
				return null;
			}
		}
		else
		{
			if (DE.P_NOTIFIER.equals(name))
			{
				return _dataStore.getDomainNotifier();
			}
			else if (DE.P_LABEL.equals(name))
			{
				return getAttribute(DE.A_NAME);
			}
			else if (DE.P_TYPE.equals(name))
			{
				return getAttribute(DE.A_TYPE);
			}
			else if (DE.P_NAME.equals(name))
			{
				return getAttribute(DE.A_NAME);
			}
			else if (DE.P_VALUE.equals(name))
			{
				return getAttribute(DE.A_VALUE);
			}
			else if (DE.P_ID.equals(name))
			{
				return getAttribute(DE.A_ID);
			}
			else if (DE.P_DESCRIPTOR.equals(name))
			{
				return _descriptor;
			}
			else if (DE.P_SOURCE_NAME.equals(name))
			{
				String source = getAttribute(DE.A_SOURCE);
				int locationIndex = source.lastIndexOf(":");
				if (locationIndex > 3)
				{
					return source.substring(0, locationIndex);
				}
				return source;
			}
			else if (DE.P_BUFFER.equals(name))
			{
				return _buffer;
			}
			else if (DE.P_SOURCE.equals(name))
			{
				return getAttribute(DE.A_SOURCE);
			}
			else if (DE.P_SOURCE_LOCATION_COLUMN.equals(name))
			{
				return null;
			}
			else if (DE.P_DATASTORE.equals(name))
			{
				return _dataStore.getName();
			}
			else if (DE.P_NESTED.equals(name) || DE.P_CHILDREN.equals(name))
			{
				return getNestedData();
			}
			else
			{
				return null;
			}
		}

	}

	private void initialize()
	{
		initialize(null);
	}

	private void initialize(DataElement typeDescriptor)
	{
		_isReference = false;
		_isDescriptor = false;
		_depth = 2;

		_referencedObject = null;
		_isExpanded = false;
		_isUpdated = false;
		_descriptor = typeDescriptor;

		
		String depthStr = getAttribute(DE.A_DEPTH);
		if (depthStr != null && depthStr.length() > 0)
		{
			if (!depthStr.equals("2"))
			{
				try
				{
					_depth = Integer.parseInt(depthStr);
				}
				catch (Exception e)
				{
				}
			}
		}
		
		

		String refType = getAttribute(DE.A_REF_TYPE);
		if (refType != null)
		{
			if (refType.equals(DataStoreResources.TRUE) || refType.equals(DataStoreResources.REFERENCE))
			{
				_isReference = true;
				_isSpirit = false;
			}
			else if (refType.equals(DataStoreResources.FALSE) || refType.equals(DataStoreResources.VALUE))
			{
				_isReference = false;
				_isSpirit = false;
			}
			else if (refType.equals(DataStoreResources.SPIRIT))
			{
				_isReference = false;
				_isSpirit = true;
			}
		}

		String type = getAttribute(DE.A_TYPE);
		if (type.equals(DE.T_OBJECT_DESCRIPTOR)
			|| type.equals(DE.T_COMMAND_DESCRIPTOR)
			|| type.equals(DE.T_RELATION_DESCRIPTOR)
			|| type.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR)
			|| type.equals(DE.T_ABSTRACT_COMMAND_DESCRIPTOR)
			|| type.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
		{
			_isDescriptor = true;
		}

		if (_nestedData != null)
			_nestedData.clear();
	}

	/**
	 * Removes all the attributes of a <code>DataElement</code>. 
	 * This method should only be called from the UpdateHandlers.
	 */
	protected synchronized void clear()
	{
		if (_attributes != null)
		{
			for (int i = 0; i < _attributes.length; i++)
			{
				String att = _attributes[i];
				if (att != null)
				{
					att = null;
				}
			}

		}

		if (_nestedData != null)
		{
			_nestedData.clear();
		}

		_parent = null;
		_descriptor = null;
		_referencedObject = null;

		_propertySource = null;
		_buffer = null;
	}

	/**
	 * Marks a <code>DataElement</code> as deleted.
	 * This method should only be called from the <code>DataStore</code>
	 */
	public synchronized void delete()
	{
		if (!isDeleted())
		{
			// set delete attribute
			
			setAttribute(DE.A_SOURCE, null);
			setAttribute(DE.A_VALUE, DataStoreResources.DELETED);
			setAttribute(DE.A_TYPE, null);
			setAttribute(DE.A_NAME, null);
			
			_isUpdated = false;
			_isExpanded = true;
			_buffer = null;
			_nestedData = null;
		}
	}
	
	public synchronized void notifyUpdate()
	{
		notify();
	}
	
	public synchronized void waitForUpdate(long timeout)
	{
		try
		{
			//System.out.println("waiting on:"+this);
			wait(timeout);
			//System.out.println("done waiting on:"+this);
		}
		catch (Exception e)
		{			
			// timeed out
		//	System.out.println("timeout:"+this);
		}
	}
}