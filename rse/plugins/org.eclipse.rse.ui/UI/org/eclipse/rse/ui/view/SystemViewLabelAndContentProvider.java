/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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


import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.DeferredTreeContentManager;


/**
 * Provides tree contents for objects that have the ISystemViewElement
 * adapter registered. Also provides label contents, so can be used for
 * both a content and label provider for TreeViewers. 
 * <p>
 * This has a general flavour, which is used in most cases, and also has
 * a specialized flavour for universal file systems, which allows restricting
 * the list to files only or folders only. It also allows further subsetting by
 * setting an input filter or filterstring.
 */
public class SystemViewLabelAndContentProvider extends LabelProvider
       implements ITreeContentProvider, ILabelProvider, ITableLabelProvider
       // ,IResourceChangeListener
{
	private static final Object[] NO_OBJECTS = new Object[0];	

	protected Viewer                    viewer;
	private boolean                     filesOnly, foldersOnly;
	private String                      filterString = null;
	private Hashtable                   resolvedChildrenPerFolder = null; // local cache to improve performance
	
	private DeferredTreeContentManager manager;
	/**
	 * The cache of images that have been dispensed by this provider.
	 * Maps ImageDescriptor->Image.
	 */
	private Map imageTable = new Hashtable(40);	
	
	/**
	 * Constructor
	 */
	public SystemViewLabelAndContentProvider()
	{
        // System.out.println("inside ctor for LCProvider " + this);		 
	}
	/**
	 * Constructor to restrict to remote folders or files
	 */
	public SystemViewLabelAndContentProvider(boolean foldersOnly, boolean filesOnly)
	{
		this();
        this.foldersOnly = foldersOnly;
        this.filesOnly = filesOnly;		
	}
    /**
     * Set a filter string to subset the list by. For example, "A*.java" or "java,class,"
     * Only valid if filesOnly or foldersOnly mode. If the latter, its recorded and used
     *  the next time files are requested from any folder.
     */
    public void setFilterString(String filterString)
    {
    	this.filterString = filterString;

    	flushCache();
    }
    /**
     * Get the current filter string being used to subset the list by.
     * Will be null unless setFilterString has previously been called.
     */
    public String getFilterString()
    {
    	return filterString;
    }
    

    
    /**
     * Flush the in-memory cache which remembers the result of the last
     *  getChildren request when we are in files-only or folders-only
     *  mode. Only applies when the two-boolean constructor is used.
     */
    public void flushCache()
    {
    	resolvedChildrenPerFolder = null;
    }
	
	/**
	 * Return the current viewer we are associated with
	 */
	public Viewer getViewer()
	{
		return viewer;
	}
	
	/**
	 * The visual part that is using this content provider is about
	 * to be disposed. Deallocate all allocated SWT resources.
	 */
	public void dispose() 
	{
	    // AS LONG AS WE DON'T SUPPORT IWORKSPACE OBJECT THIS IS NOT NEEDED.
	    // WE LEAVE IT IN BECAUSE IT IS HARMLESS AND MIGHT BE OF VALUE SOMEDAY.		
	    if (viewer != null) 
	    {
		  Object obj = viewer.getInput();
		  if (obj != null)
		  {
	  	    if (obj instanceof IWorkspace) 
	  	    {
			  //IWorkspace workspace = (IWorkspace) obj;
			  //workspace.removeResourceChangeListener(this);
		    } 
		    else if (obj instanceof IContainer) 
		    {
		      //IWorkspace workspace = ((IContainer) obj).getWorkspace();
		      //workspace.removeResourceChangeListener(this);
		    }
		  }
	    }
        // The following we got from WorkbenchLabelProvider
        if (imageTable != null)
        {
	      Collection imageValues = imageTable.values();
	      if (imageValues!=null)
	      {
	        Iterator images = imageValues.iterator();	    	
	        if (images!=null)
	          while (images.hasNext())
    	        ((Image)images.next()).dispose();
    	    imageTable = null;	    
	      }
        }
    }
    /**
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    protected ISystemViewElementAdapter getAdapter(Object o) 
    {
    	ISystemViewElementAdapter adapter = null;    	
    	if (o == null)
    	{
    	  	SystemBasePlugin.logWarning("ERROR: null passed to getAdapter in SystemViewLabelAndContentProvider");    	  
    	}

    	{
    	  	if (!(o instanceof IAdaptable)) 
            	adapter = (ISystemViewElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemViewElementAdapter.class);
          	else
    	    	adapter = (ISystemViewElementAdapter)((IAdaptable)o).getAdapter(ISystemViewElementAdapter.class);
    	  	if (adapter == null)
    	    	SystemBasePlugin.logWarning("ADAPTER IS NULL FOR ELEMENT OF TYPE: " + o.getClass().getName());
    	}
    	if ((adapter!=null) && (viewer != null))
    	{    	
    	  	Shell shell = null;
    	  	if (viewer instanceof ISystemResourceChangeListener)
    	    	shell = ((ISystemResourceChangeListener)viewer).getShell();
    	  	else
    	    	shell = viewer.getControl().getShell();
    	  	adapter.setShell(shell);
    	  	//System.out.println("Inside getAdapter for LCProvider "+this+", setting viewer of adapter to "+viewer);
    	  	adapter.setViewer(viewer);
    	  	if (viewer.getInput() instanceof ISystemViewInputProvider)
    	  	{
    	    	ISystemViewInputProvider inputProvider = (ISystemViewInputProvider)viewer.getInput();
            	//inputProvider.setShell(shell); this is now done in the getInput() method of viewer.
    	    	adapter.setInput(inputProvider);
    	  	}
    	}
    	else if (viewer == null)
    	  	SystemBasePlugin.logWarning("VIEWER IS NULL FOR SYSTEMVIEWLABELANDCONTENTPROVIDER");    	
    	return adapter;
    }
    
    /**
	 * Cancel any jobs that are fetching content from the given location.
	 * @param location
	 */
	public void cancelJobs(Object location) 
	{
		if (manager != null) {
			manager.cancel(location);
		}
	}
	
	protected boolean supportsDeferredQueries()
	{
	    IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
	    return store.getBoolean(ISystemPreferencesConstants.USE_DEFERRED_QUERIES);
	}

    
    /**
     * @see ITreeContentProvider
     */
    public Object[] getChildren(Object element) 
    {
    	ISystemViewElementAdapter adapter = getAdapter(element);
    	if (supportsDeferredQueries())
    	{
	        if (manager != null && adapter.supportsDeferredQueries()) 
	        {
	            ISubSystem ss = adapter.getSubSystem(element);
	            if (ss != null)
	            {
	               // if (ss.isConnected())
	                {
			            
						Object[] children = manager.getChildren(element);
						if (children != null) 
						{
							// This will be a placeholder to indicate 
							// that the real children are being fetched
							return children;
						}
	                }
	            }
			}
    	}
        
    
    	//System.out.println("inside getChildren for landcProvider");
    	//System.out.println("...element = " + element);
    	//System.out.println("...adapter = " + adapter);    	
    	if (adapter != null) 
    	{    	  
    	  // we first test to see if this is an expand-to filter in effect for this
    	  //  object, and if so use it...
    	  if ((viewer instanceof SystemView) && (((SystemView)viewer).getSystemViewPart() != null) &&
    	      (adapter instanceof ISystemRemoteElementAdapter))
    	  {
    	  	  String expandToFilter = ((SystemView)viewer).getExpandToFilter(element);
    	  	  if (expandToFilter != null)
    	  	    return adapter.getChildrenUsingExpandToFilter(element, expandToFilter);
    	  }
    	  Object[] children = null;    	  
    	  // The re-usable Eclipse GUI widgets are not very efficient.
    	  // The are always re-asking for children, which for remote requests
    	  //  causes a lot of flashing and unnecessary trips to the host.
    	  //  To overcome this, once we successfully resolve a request,
    	  //  we remember the result so on the subsequent request for the
    	  //  same files or folders, we can return that remembered cache.
    	  // The tricky part is what to key each request by. We use the element
    	  //  as the key ... this is the parent folder which children are being
    	  //  asked for. However, for the same folder we will be asked for 
    	  //  folders and files in separate requests. It turns out this is not
    	  //  not a problem though, because a separate instance of us is used 
    	  //  for files versus folders so each maintains its own cache.
    	  
    	  if ((filesOnly || foldersOnly) && (resolvedChildrenPerFolder != null))
    	  {
    	  	children = (Object[])resolvedChildrenPerFolder.get(element);
    	  	if (children != null) // found cached list?
    	  	  return children;    //  return it to caller
    	  }
    	  
    	  children = adapter.getChildren(element);

    	  if ((filesOnly || foldersOnly) && 
    	      // an array of one SystemMessageObject item implies some kind of error, so don't cache...
    	      ((children.length != 1) || !(children[0] instanceof SystemMessageObject)) )
    	  {
    	  	if (resolvedChildrenPerFolder == null)
    	  	  resolvedChildrenPerFolder = new Hashtable();
    	  	resolvedChildrenPerFolder.put(element, children);
    	  }

    	  return children;
    	}
    	return NO_OBJECTS;
    }
    /**
     * @see ITreeContentProvider
     */
    public Object[] getElements(Object element) 
    {
    	return getChildren(element);
    }
    /**
     * @see ITreeContentProvider
     */
    public Object getParent(Object element) 
    {
    	ISystemViewElementAdapter adapter = getAdapter(element);
    	if (adapter != null) 
    	  return adapter.getParent(element);
    	return null;
    }
    /**
     * hasChildren method comment.
     * This method has been optimized over its original code so
     *  that we don't actually retrieve all the children (horrors!)
     *  just to decide if it has children. For performance reasons we
     *  just assume if it can have children it does. That means we always
     *  get a plus but that is way better than a very slow remote
     *  system query just to decide if we want a plus or not!
     */
    public boolean hasChildren(Object element) 
    {
    	//return getChildren(element).hasNext();
    	ISystemViewElementAdapter adapter = getAdapter(element);
    	if (adapter != null) 
    	{
    	    return adapter.hasChildren(element);
    	}
    	if (manager != null) {
			if (manager.isDeferredAdapter(element))
				return manager.mayHaveChildren(element);
		}
    	return false;
    }
    /**
     * inputChanged method comment.
	 * AS LONG AS WE DON'T SUPPORT IWORKSPACE OBJECT THIS IS NOT NEEDED.
	 * WE LEAVE IT IN BECAUSE IT IS HARMLESS AND MIGHT BE OF VALUE SOMEDAY.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
    {
    	//System.out.println("Inside LCProvider "+this+". viewer = " + viewer);
    	this.viewer = viewer;
    	if (newInput instanceof IWorkspace) 
    	{
    		//IWorkspace workspace = (IWorkspace)newInput;
    		//workspace.addResourceChangeListener(this);
    	} 
    	else if (newInput instanceof IContainer) 
    	{
    		//IWorkspace workspace = ((IContainer)newInput).getWorkspace();
    		//workspace.addResourceChangeListener(this);
    	}
    	if (viewer instanceof AbstractTreeViewer) 
    	{
			manager = new DeferredTreeContentManager(this, (AbstractTreeViewer) viewer);
		}
    }
    
    public Image getColumnImage(Object element, int columnIndex) 
    {
    	return getImage(element);
    }
    public String getColumnText(Object element, int columnIndex) 
    {
    	return getText(element);
    }
    public Image getImage(Object element) 
    {
    	ISystemViewElementAdapter adapter = getAdapter(element);
    	//System.out.println("Inside getImage. element = " + element + ", adapter = " + adapter);
	    if (adapter == null)
		  return null;
		/*
		boolean isOpen = false;
		if (viewer instanceof AbstractTreeViewer)
		{
			AbstractTreeViewer atv = (AbstractTreeViewer)viewer;
			isOpen = true; //atv.getExpandedState(element);
			//System.out.println("In getImage for " + adapter.getName(element) + ": isOpen = " + isOpen);
		}
	    ImageDescriptor descriptor = adapter.getImageDescriptor(element, isOpen);
	    */
	    ImageDescriptor descriptor = adapter.getImageDescriptor(element);
    	//System.out.println("...descriptor = " + descriptor);
	    
	    if (descriptor == null)
		  return null;
	    //add any annotations to the image descriptor
	    descriptor = decorateImage(descriptor, element);
	    //obtain the cached image corresponding to the descriptor
	    Image image = (Image) imageTable.get(descriptor);
	    if (image == null) 
	    {
		  image = descriptor.createImage();
		  imageTable.put(descriptor, image);
	    }
    	//System.out.println("...image = " + image);	    
	    return image;    	
    }
    /**
     * Returns the label text for the given object.
     */
    public String getText(Object element) 
    {
    	ISystemViewElementAdapter adapter = getAdapter(element);
    	//System.out.println("INSIDE GETTEXT FOR SVLandCprovider: " + element + ", adapter = " + adapter);
	    if (adapter == null)
	    {
	        IWorkbenchAdapter wadapter = (IWorkbenchAdapter)((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
	   
			if (wadapter == null) 
			{
			    return super.getText(element);
			}
			return wadapter.getLabel(element);
	    }
	    //return the decorated label (FROM WorkbenchLabelProvider)
	    return decorateText(adapter.getText(element), element);    	
    }

    /**
     * Returns an image descriptor that is based on the given descriptor,
     * but decorated with additional information relating to the state
     * of the provided object.
     *
     * Subclasses may reimplement this method to decorate an object's
     * image.
     * @see org.eclipse.jface.resource.ImageDescriptor
     */
    protected ImageDescriptor decorateImage(ImageDescriptor input, Object element) 
    {
    	return input;
    }
    /**
     * Returns a label that is based on the given label,
     * but decorated with additional information relating to the state
     * of the provided object.
     *
     * Subclasses may implement this method to decorate an object's
     * label.
     */
    protected String decorateText(String input, Object element) 
    {
    	return input;
    }    
}