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

package org.eclipse.rse.ui.dialogs;
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * When we populate a TreeViewer in a dialog, we need a simple
 * representation of the objects to populate the tree. 
 * <p>
 * Works in concert with {@link org.eclipse.rse.ui.dialogs.SystemSimpleContentProvider}
 * @see org.eclipse.rse.ui.dialogs.SystemSimpleContentElement
 * @see org.eclipse.rse.ui.dialogs.SystemSimpleSelectDialog
 */
public class SystemSimpleContentElement 
{
    private String name;
    private Object data;
    private SystemSimpleContentElement parent;
    private SystemSimpleContentElement[] children;
    private ImageDescriptor imageDescriptor;
    private boolean selected = false;
    private boolean isDeletable = true;
    private boolean isRenamable = true;
    private boolean isReadonly = false;
    
    /**
     * Constructor when given children as an array.
     * @param name - the display name to show for this element
     * @param data - the real object which is to be contained by this element
     * @param parent - the parent element of this element. Pass null for the root.
     * @param children - an array of SystemSimpleContentElement objects that are to be the children of this element. Can be null.
     */
    public SystemSimpleContentElement(String name, Object data, 
                                      SystemSimpleContentElement parent, SystemSimpleContentElement[] children)
    {
    	setName(name);
    	setData(data);
    	setParent(parent);
    	setChildren(children);
    }
    /**
     * Constructor when given children as a vector.
     * @param name - the display name to show for this element
     * @param data - the real object which is to be contained by this element
     * @param parent - the parent element of this element. Pass null for the root.
     * @param children - a vector of SystemSimpleContentElement objects that are to be the children of this element. Can be null.
     */
    public SystemSimpleContentElement(String name, Object data, 
                                      SystemSimpleContentElement parent, Vector children)
    {
    	setName(name);
    	setData(data);
    	setParent(parent);
    	setChildren(children);
    }    

    /**
     * Return the display name for this element
     */    
    public String getName()
    {
    	return name;
    }
    
    /**
     * Set the display name for this element
     */        
    public void setName(String name)
    {
    	this.name = name;
    }

    /**
     * Return the real object which this element wraps or represents
     */        
    public Object getData()
    {
    	return data;
    }

    /**
     * Set the real object which this element wraps or represents
     */            
    public void setData(Object data)
    {
    	this.data = data;
    }

    /**
     * Get the parent element
     */                
    public SystemSimpleContentElement getParent()
    {
    	return parent;
    }

    /**
     * Set the parent element
     */                    
    public void setParent(SystemSimpleContentElement parent)
    {
    	this.parent = parent;
    }

    /**
     * Walk up the parent tree until we find the root
     */                
    public SystemSimpleContentElement getRoot()
    {
    	SystemSimpleContentElement currParent = parent;
    	while (currParent.getParent() != null)
    	     currParent = currParent.getParent();
    	return currParent;
    }

    /**
     * Return the child elements, or null if no children
     */                    
    public SystemSimpleContentElement[] getChildren()
    {
    	return children;
    }

    /**
     * Return true if this element has children
     */                    
    public boolean hasChildren()
    {
    	return ((children!=null) && (children.length>0));
    }

    /**
     * Set the child elements of this element, as an array of SystemSimpleContentElement elements
     */                        
    public void setChildren(SystemSimpleContentElement[] children)
    {
    	this.children = children;
    }
    
    /**
     * Set the child elements of this element, as a vector of SystemSimpleContentElement elements
     */                        
    public void setChildren(Vector childrenVector)
    {
    	if (childrenVector != null)
    	{
    	  children = new SystemSimpleContentElement[childrenVector.size()];
    	  for (int idx=0; idx<childrenVector.size(); idx++)
    	     children[idx] = (SystemSimpleContentElement)childrenVector.elementAt(idx);  
    	}
    	else
    	  children = null;
    }

    /**
     * Set selected state.
     * Used in SimpleSimpleSelectDialog to pre-check item in CheckboxTreeViewer
     */
    public void setSelected(boolean selected)
    {
    	this.selected = selected;
    }
    /**
     * Return true if this element has been flagged as selected.
     */
    public boolean isSelected()
    {
    	return selected;
    }
    
    /**
     * Set whether this item is renamable or not. Default is true.
     * Used to enable/disable rename action.
     */
    public void setRenamable(boolean renamable)
    {
    	this.isRenamable = renamable;
    }
    /**
     * Return true if this item is renamable or not. Default is true.
     */
    public boolean isRenamable()
    {
    	return isRenamable;
    }    

    /**
     * Set whether this item is deletable or not. Default is true.
     * Used to enable/disable rename action.
     */
    public void setDeletable(boolean deletable)
    {
    	this.isDeletable = deletable;
    }
    /**
     * Return true if this item is deletable. Will be true unless setDeletable(false) has been called.
     */                        
    public boolean isDeletable()
    {
    	return isDeletable;
    }
    
    /**
     * Set whether this node is readonly or not. If readonly, users cannot change its selected state.
     */        
    public void setReadOnly(boolean readonly)
    {
    	this.isReadonly = readonly;
    }
    /**
     * Return whether this node is readonly or not. Readonly nodes cannot be selected/deselected by the user.
     */
    public boolean isReadOnly()
    {
    	return isReadonly;
    }

    /**
     * Set the image to display for this element, in the tree viewer
     */                                
    public void setImageDescriptor(ImageDescriptor imageDescriptor)
    {
    	this.imageDescriptor = imageDescriptor;
    }

    /**
     * Get the image to display for this element, in the tree viewer
     */                                    
    public ImageDescriptor getImageDescriptor()
    {
    	if (imageDescriptor != null)
    	  return imageDescriptor;
    	else
    	  return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FOLDER_ID);
    	  //return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
    }    

    /**
     * Delete the given child element.
     */                                
    public void deleteChild(SystemSimpleContentElement child)
    {
    	if (children != null)
    	{
    	  int nbrChildren = children.length;
    	  if ((nbrChildren == 1) && (child.equals(children[0])))
    	    children = null;
    	  else
    	  {    	  	
    	  	SystemSimpleContentElement[] newChildren = new SystemSimpleContentElement[nbrChildren-1];
    	  	int newIdx = 0;
    	  	for (int idx=0; idx<children.length; idx++)
    	  	   if (!(children[idx].equals(child)))
    	  	     newChildren[newIdx++] = children[idx];
    	  	children = newChildren;
    	  }
    	}
    }

    /**
     * Add the given child element at the given zero-based position
     */                                        
    public void addChild(SystemSimpleContentElement child, int pos)
    {
    	if (children == null)
    	{
    	  children = new SystemSimpleContentElement[1];
    	  children[0] = child;
    	}
    	else
    	{
    		int newNbr = children.length + 1;
    	  	SystemSimpleContentElement[] newChildren = new SystemSimpleContentElement[newNbr];
    	  	int oldIdx = 0;
    	  	for (int idx=0; idx<newNbr; idx++)
    	  	   if (idx == pos)
    	  	     newChildren[idx] = child;
    	  	   else
    	  	     newChildren[idx] = children[oldIdx++];
    	  	children = newChildren;    	    
    	}
    }
    
    /**
     * Maps to getName()
     */        
    public String toString()
    {
    	return getName();
    }
    
	/**
	 * Find element corresponding to given data
	 */
	public static SystemSimpleContentElement getDataElement(SystemSimpleContentElement root, Object data)
	{
        SystemSimpleContentElement[] children = root.getChildren();
        SystemSimpleContentElement match = null;
        if ((children!=null)&&(children.length>0))
        {
        	for (int idx=0; (match==null)&&(idx<children.length); idx++)
        	   if (children[idx].getData() == data)
        	     match = children[idx];
        }
        if ((match==null)&&(children!=null)&&(children.length>0))
        {
        	for (int idx=0; (match==null)&&(idx<children.length); idx++)
        	   match = getDataElement(children[idx], data);
        }        
        return match;
	}
    
}