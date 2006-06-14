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

package org.eclipse.rse.internal.references;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.rse.references.ISystemBasePersistableReferenceManager;
import org.eclipse.rse.references.ISystemBasePersistableReferencedObject;
import org.eclipse.rse.references.ISystemBasePersistableReferencingObject;


/**
 * <b>YOU MUST OVERRIDE resolveReferencesAfterRestore() IN THIS CLASS!</b>
 * <p>
 * <b>YOU MUST OVERRIDE getReferenceName() IN SYSTEMPERSISTABLEREFERENCEDOBJECT!</b>
 * <p>
 * @see org.eclipse.rse.references.ISystemBasePersistableReferenceManager
 * 
 * @lastgen class SystemPersistableReferenceManagerImpl Impl implements SystemPersistableReferenceManager, EObject {}
 */
public class SystemPersistableReferenceManager implements ISystemBasePersistableReferenceManager 
{
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

    private ISystemBasePersistableReferencingObject[] listAsArray = null;
    public static boolean debug = true;
    public static HashMap EMPTY_MAP = new HashMap();

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String name = NAME_EDEFAULT;
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected List referencingObjectList = null;
	/**
	 * Constructor. Typically called by EMF framework via factory create method.
	 */
	protected SystemPersistableReferenceManager() 
	{
		super();
	}
	/**
     * Internal method to get the mof List that is the current list.
     */
    protected List internalGetList()
    {
    	return getReferencingObjectList();
    }
    
    /**
     * Internal method to invalidate any cached info.
     * Must be called religiously by any method affecting list.
     */
    protected void invalidateCache()
    {
    	listAsArray = null;
    }

	/**
	 * Return an array of the referencing objects currently being managed.
	 * @param array of the referencing objects currently in this list.
	 */
	public ISystemBasePersistableReferencingObject[] getReferencingObjects()
	{
        if ((listAsArray == null) || (listAsArray.length!=internalGetList().size()))
        {
          List list = internalGetList();
          listAsArray = new ISystemBasePersistableReferencingObject[list.size()];
          Iterator i = list.iterator();
          int idx=0;
          while (i.hasNext())
          {
          	listAsArray[idx++] = (ISystemBasePersistableReferencingObject)i.next();
          }
        }
        return listAsArray;
	}
	
	/**
	 * Set in one shot the list of referencing objects. Replaces current list.
	 * @param objects An array of referencing objects which is to become the new list.
     * @param deReference true to first de-reference all objects in the existing list.
	 */
	public void setReferencingObjects(ISystemBasePersistableReferencingObject[] objects, 
	                                  boolean deReference)
	{
		listAsArray = objects;
		if (deReference)
		  removeAndDeReferenceAllReferencingObjects();
		else
		  removeAllReferencingObjects();
		List list = internalGetList();		
		for (int idx=0; idx<objects.length; idx++)
		  list.add(objects[idx]);
	}
	
/*
 * DWD this should probably operate on ISystemPersistableReferencingObject
 * instead and call setParentManager. This involves recasting this class to 
 * implement a new type or changing ISystemBasePersistableReferenceManager to
 * deal with parent references - probably changing its name in the process.
 * We could collapse ISystemBasePersistableReferencingObject and its subinterface
 * into one interface.
 */
	/**
	 * Add a referencing object to the managed list.
	 * @return new count of referenced objects being managed.
	 */
	public int addReferencingObject(ISystemBasePersistableReferencingObject object)
	{
      	List list = internalGetList();
      	list.add(object);
      	invalidateCache();
      	return getReferencingObjectCount();
	}

	/**
	 * Remove a referencing object from the managed list.
	 * <p>Does NOT call removeReference on the master referenced object.
	 * @return new count of referenced objects being managed.
	 */
	public int removeReferencingObject(ISystemBasePersistableReferencingObject object)
	{
      	List list = internalGetList();
      	list.remove(object);
      	invalidateCache();
      	return getReferencingObjectCount();		
	}

	/**
	 * Remove and dereferences a referencing object from the managed list.
	 * <p>DOES call removeReference on the master referenced object.
	 * @return new count of referenced objects being managed.
	 */
	public int removeAndDeReferenceReferencingObject(ISystemBasePersistableReferencingObject object)
	{
		object.removeReference();
      	return removeReferencingObject(object);		
	}

	/**
	 * Remove all objects from the list.
	 * <p>Does NOT call removeReference on the master referenced objects.
	 */
	public void removeAllReferencingObjects()
	{
		internalGetList().clear();
	}

	/**
	 * Remove and dereference all objects from the list.
	 * <p>DOES call removeReference on the master referenced objects.
	 */
	public void removeAndDeReferenceAllReferencingObjects()
	{
		ISystemBasePersistableReferencingObject[] objs = getReferencingObjects();
		for (int idx=0; idx<objs.length; idx++)
		{
			objs[idx].removeReference();
		}
		removeAllReferencingObjects();
	}
	
	/**
	 * Return how many referencing objects are currently in the list.
	 * @return current count of referenced objects being managed.
	 */
	public int getReferencingObjectCount()
	{
		return internalGetList().size();
	}

	/**
	 * Return the zero-based position of the given referencing object within the list.
     * Does a memory address comparison (==) to find the object.
	 * @param object The referencing object to find position of.
	 * @return zero-based position within the list. If not found, returns -1
	 */
	public int getReferencingObjectPosition(ISystemBasePersistableReferencingObject object)
	{
        List list = internalGetList();
    	int position = -1;
    	boolean match = false;    	
    	
    	Iterator i = list.iterator();
    	int idx = 0;
    	
    	while (!match && i.hasNext())
    	{
    		ISystemBasePersistableReferencingObject curr = (ISystemBasePersistableReferencingObject)i.next();
    		if (curr == object)
    		{
    		  match = true;
    		  position = idx;
    		}
    		else
    		  idx++;
    	}    	
    	return position;
	}

	/**
	 * Move the given referencing object to a new zero-based position in the list.
	 * @param newPosition New zero-based position
	 * @param object The referencing object to move
	 */
	public void moveReferencingObjectPosition(int newPosition, ISystemBasePersistableReferencingObject object)
	{
//    	List list = internalGetList(); 
    //FIXME	list.move(newPosition, object);		
	}

	/**
	 * Return true if the given referencable object is indeed referenced by a referencing object
	 * in the current list. This is done by comparing the reference names of each, not the
	 * in-memory pointers.
	 * @param object The referencable object to which to search for a referencing object within this list
	 * @return true if found in list, false otherwise.
	 */
	public boolean isReferenced(ISystemBasePersistableReferencedObject object)
	{
        return (getReferencedObject(object) != null);
	}

	/**
	 * Search list of referencing objects to see if one of them references the given referencable object.
	 * This is done by comparing the reference names of each, not the in-memory pointers.
	 * @param object The referencable object to which to search for a referencing object within this list
	 * @return the referencing object within this list which references the given referencable object, or
	 * null if no reference found.
	 */
	public ISystemBasePersistableReferencingObject getReferencedObject(ISystemBasePersistableReferencedObject object)
	{
        List list = internalGetList();
    	ISystemBasePersistableReferencingObject match = null;
    	Iterator i = list.iterator();
    	int idx = 0;
    	
    	while ((match==null) && i.hasNext())
    	{
    		ISystemBasePersistableReferencingObject curr = (ISystemBasePersistableReferencingObject)i.next();
    		if (curr.getReferencedObjectName().equals(object.getReferenceName()))
    		{
    		  match = curr;
    		}
    		else
    		  idx++;
    	}    	
    	return match;				
	}

    /**
     * Return string identifying this filter
     */
    public String toString()
    {
    	return getName();
    }    

    // ---------------------------------------------------------------------------
    // Methods for saving and restoring if not doing your own in your own subclass
    // ---------------------------------------------------------------------------

    /**
     * <b>YOU MUST OVERRIDE THIS METHOD!</b>
     * <p>
     * After restoring this from disk, there is only the referenced object name,
     * not the referenced object pointer, for each referencing object.
     * <p>
     * This method is called after restore and for each restored object in the list must:
     * <ol>
     *   <li>Do what is necessary to find the referenced object, and set the internal reference pointer.
     *   <li>Call addReference(this) on that object so it can maintain it's in-memory list
     *          of all referencing objects.
     * </ol>
     * @return true if resolved successfully. False if some references were not found and
     *  hence those referencing objects removed from the restored list.
     */
    public boolean resolveReferencesAfterRestore()
    {
    	return false;
    }
	

    /**
     * Attempt to save contents of manager to disk. Only call if not doing your own save from
     *  your own model that uses a subclass of this.
     * @param folder The folder in which to save the manager.
     * @param fileName The unqualified file name to save to. Should include extension, such as .xmi
     */
    public void save(IFolder folder, String fileName)
       throws Exception
    {
    	/* FIXME
        initMOF();        
        String path = folder.getLocation().toOSString();
        String saveFileName = addPathTerminator(path)+fileName;
        File saveFile = new File(saveFileName);
        boolean exists = saveFile.exists();    
        saveFileName = saveFile.toURL().toString();
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        
        Resource.Factory resFactory = reg.getFactory(URI.createURI(saveFileName));
        //java.util.List ext   = resFactory.createExtent(); // MOF way
        //ext.add(this); // MOF way
        Resource mofRes = resFactory.createResource(URI.createURI(saveFileName));
		mofRes.getContents().add(this);
        try
        {
          mofRes.save(EMPTY_MAP);
        } catch (Exception e)
        {
           if (debug)
           {        	
             System.out.println("Error saving SystemPersistableReferenceManager "+getName() + " to "+saveFile+": " + e.getClass().getName() + ": " + e.getMessage());
             e.printStackTrace();
           }
           throw e;
        }

        // if this is the first time we have created this file, we must update Eclipse
        // resource tree to know about it...
        if (!exists || !folder.exists())        
        {
          try 
          {
          	 //RSEUIPlugin.logWarning("Calling refreshLocal on project after saving MOF file: " + saveFileName);
             folder.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);               
          } catch(Exception exc) 
          {
          	 System.out.println("Exception doing refreshLocal on project: " + exc.getClass().getName());
          }
        }
        else
        {
          try 
          {
          	 //RSEUIPlugin.logWarning("Calling refreshLocal on project after saving MOF file: " + saveFileName);
             folder.refreshLocal(IResource.DEPTH_ONE, null);               
          } catch(Exception exc) 
          {
          	 System.out.println("Exception doing refreshLocal on project: " + exc.getClass().getName());
          }
        }        
        */
    }

    /**
     * Restore a persisted manager from disk.
     * <p>
     * After restoration, YOU MUST CALL {@link #resolveReferencesAfterRestore() resolveReferencesAfterRestore}
     * This presumes yours subclass has overridden that method!
     * <p>
     * @param folder The folder in which the saved manager exists.
     * @param fileName The unqualified save file name including extension such as .xmi
     * @return The restored object, or null if given file not found. Any other error gives an exception.
     */
    public static ISystemBasePersistableReferenceManager restore(IFolder folder, String fileName)
           throws Exception
    {
        ISystemBasePersistableReferenceManager mgr = new SystemPersistableReferenceManager();
/*FIXME        
        initMOF();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		String path = folder.getLocation().toOSString();
		String saveFile = addPathTerminator(path)+fileName;		
        //ResourceSet resourceSet = // MOF way
        // Resource.Factory.Registry.getResourceSetFactory().makeResourceSet();
        Resource res1 = null;
        try
        {
           // res1 = resourceSet.load(saveFile); MOF way           
		   Resource.Factory resFactory = reg.getFactory(URI.createURI(saveFile));
		   res1 = resFactory.createResource(URI.createURI(saveFile));
		   res1.load(EMPTY_MAP);
        }
        catch (java.io.FileNotFoundException e)
        {
           if (debug)
             System.out.println("SystemPersistableReferenceManager file not found: "+saveFile);
           return null;
        }
        catch (Exception e)
        {
           if (debug)
           {
             System.out.println("Error loading SystemPersistableReferenceManager from file: "+saveFile+": " + e.getClass().getName() + ": " + e.getMessage());
             e.printStackTrace();
           }
           throw e;
        }        

        java.util.List ext1 = res1.getContents();

        // should be exactly one...
        Iterator iList = ext1.iterator();
        mgr = (SystemPersistableReferenceManager)iList.next();

        if (debug)
          System.out.println("Ok. SystemPersistableReferenceManager "+mgr.getName()+" restored successfully.");
*/
        return mgr;
    }
    
    
    /**
     * Ensure given path ends with path separator.
     */
    public static String addPathTerminator(String path)
    {
        if (!path.endsWith(File.separator))
          path = path + File.separatorChar;
        //else
        //  path = path;
        return path;
    }    

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName)
	{
		name = newName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public List getReferencingObjectList()
	{
		if (referencingObjectList == null)
		{
			referencingObjectList = new ArrayList();
			//FIXME new EObjectContainmentWithInversejava.util.List(SystemPersistableReferencingObject.class, this, ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCE_MANAGER__REFERENCING_OBJECT_LIST, ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCING_OBJECT__PARENT_REFERENCE_MANAGER);
		}
		return referencingObjectList;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public String toStringGen()
	{
		

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(')');
		return result.toString();
	}

}