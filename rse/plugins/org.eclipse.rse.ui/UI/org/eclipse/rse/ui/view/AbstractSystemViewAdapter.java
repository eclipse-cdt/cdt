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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.internal.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemPromptableObject;
import org.eclipse.rse.model.ISystemResourceSet;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemDynamicPopupMenuExtensionManager;
import org.eclipse.rse.ui.operations.Policy;
import org.eclipse.rse.ui.operations.SystemFetchOperation;
import org.eclipse.rse.ui.operations.SystemSchedulingRule;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Base class for adapters needed for the SystemView viewer.
 * It implements the ISystemViewElementAdapter interface. 
 * @see AbstractSystemRemoteAdapterFactory
 */
public abstract class AbstractSystemViewAdapter 
                implements ISystemViewElementAdapter, IPropertySource, ISystemPropertyConstants, IWorkbenchAdapter,
                             ISystemViewActionFilter, IDeferredWorkbenchAdapter
{	
	//protected boolean isEditable = false;
	
	protected String filterString = null;
	
	/**
	 * Current viewer. Set by content provider
	 */
	protected Viewer viewer = null;
	/**
	 * Current input provider. Set by content provider
	 */
	protected Object propertySourceInput = null;
	/**
	 * Current shell, set by the content provider
	 */
	protected Shell shell;
	private ISystemViewInputProvider input;
	private String xlatedYes = null;
	private String xlatedNo  = null;
	private String xlatedTrue = null;
	private String xlatedFalse  = null;
	private String xlatedNotApplicable = null;
	private String xlatedNotAvailable = null;
	/**
	 * For returning an empty list from getChildren: new Object[0]
	 */
	protected Object[] emptyList = new Object[0];
	/**
	 * For returning a msg object from getChildren. Will be an array with one item, 
	 *  one of nullObject, canceledObject or errorObject
	 */
	protected Object[] msgList   = new Object[1];	
	/**
	 * Frequently returned msg object from getChildren: "empty list"
	 */
	protected SystemMessageObject nullObject     = null;
	/**
	 * Frequently returned msg object from getChildren: "operation canceled"
	 */
	protected SystemMessageObject canceledObject = null;	
	/**
	 * Frequently returned msg object from getChildren: "operation ended in error"
	 */
	protected SystemMessageObject errorObject    = null;		
		
    /**
     * Message substitution prefix: "&"
     */
	protected static final String MSG_SUB_PREFIX = "&";
    /**
     * Message substitution variable 1: "&1"
     */
	protected static final String MSG_SUB1       = MSG_SUB_PREFIX+"1";
    /**
     * Message substitution variable 2: "&2"
     */
	protected static final String MSG_SUB2       = MSG_SUB_PREFIX+"2";

    /**
     * Delimiter for each object's key in a memento, used to persist tree view expansion state: "///"
     */
	public static final String MEMENTO_DELIM = SystemViewPart.MEMENTO_DELIM;
	
	/**
	 * A handy constant of "new String[0]"
	 */
	protected static final String[] EMPTY_STRING_LIST = new String[0];
			
	// -------------------
	// default descriptors
	// -------------------
	private static PropertyDescriptor[] propertyDescriptorArray = null;
	
	// DKM: temporary memory caching stuff - we should replace this with something
	//    more comprehensive later
	/**
	 * A variable that can be used in getChildren to cache last returned results, if desired
	 */
	protected Object[] _lastResults = null;
	/**
	 * A variable that can be used to cache last selection, if desired
	 */
	protected Object   _lastSelected = null;
	
	// ------------------------------------------------------------------ 	 
	// Configuration methods, called by the label and content provider...
	// ------------------------------------------------------------------ 	 

    /**
     * <i>Configuration method. Typically called by content provider, viewer or action. Do not override.</i><br>
     * Set the viewer that is driving this adapter
     * Called by label and content provider.
     */
    public void setViewer(Viewer viewer)
    {
    	this.viewer = viewer;
    }	
    /**
     * <i>Configuration method. Typically called by content provider, viewer or action. Do not override.</i><br>
     * Set the shell to be used by any method that requires it.
     */
    public void setShell(Shell shell)
    {
    	this.shell = shell;
    }	
    /**
     * <i>Configuration method. Typically called by content provider, viewer or action. Do not override.</i><br>
     * Set the input object used to populate the viewer with the roots.
     * May be used by an adapter to retrieve context-sensitive information.
     * This is set by the Label and Content providers that retrieve this adapter.
     */
    public void setInput(ISystemViewInputProvider input)
    { 
    	this.input = input;
    }

	// ------------------------------------------------------------------ 	 
	// Getter methods, for use by subclasses and actions...
	// ------------------------------------------------------------------ 	 
	
    /**
     * <i>Getter method. Callable by subclasses. Do not override.</i><br>
     * Get the shell currently hosting the objects in this adapter
     */
    public Shell getShell()
	{
		if (shell == null || shell.isDisposed() || !shell.isVisible() || !shell.isEnabled())
		{
			// get a new shell    			
			Shell[] shells = Display.getCurrent().getShells();
			Shell lshell = null;
			for (int i = 0; i < shells.length && lshell == null; i++)
			{
				if (!shells[i].isDisposed() && shells[i].isEnabled() && shells[i].isVisible())
				{
					lshell = shells[i];
				}
			}
			if (lshell == null)
				lshell = SystemBasePlugin.getActiveWorkbenchShell();
			shell = lshell;
		}
		return shell;
	}
	/**
     * <i>Getter method. Callable by subclasses. Do not override.</i><br>
	 * Return the current viewer, as set via setViewer or its deduced from the 
	 *  setInput input object if set. May be null so test it.
	 */
	public Viewer getViewer()
	{        
		if (viewer == null)
		{
	      ISystemViewInputProvider ip = getInput();
	      if (ip != null)
	      {
	        return ip.getViewer();
	      }
	      else
	      {
	    	  IWorkbenchPart currentPart = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
	    	  if (currentPart instanceof IRSEViewPart)
	    	  {
	    		return ((IRSEViewPart)currentPart).getRSEViewer();  
	    	  }
	      }
	        
		}
	    return viewer;
	}
	/**
     * <i>Getter method. Callable by subclasses. Do not override.</i><br>
	 * Return the current viewer as an ISystemTree if it is one, or null otherwise
	 */
	protected ISystemTree getCurrentTreeView()
	{
		  Viewer v = getViewer();
		  if (v instanceof ISystemTree)
            return (ISystemTree)v;
          else
            return null;
	}

    /**
     * <i>Getter method. Callable by subclasses. Do not override.</i><br>
     * Get the input object used to populate the viewer with the roots.
     * May be used by an adapter to retrieve context-sensitive information.
     */
    public ISystemViewInputProvider getInput()
    {
    	return input;
    }
	
	/**
     * <i><b>Overridable</b> by subclasses. You should override if not using AbstractResource.</i><br>
	 * Returns the subsystem that contains this object. By default, if the
	 *  given element is instanceof {@link org.eclipse.rse.core.internal.subsystems.AbstractResource AbstractResource},
	 *  it calls getSubSystem on it, else returns null.
	 */ 
	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof AbstractResource)
		  return ((AbstractResource)element).getSubSystem();
		else
		  return null;	
	}
	
	/**
     * <i>Called by SystemView viewer. No need to override or call.</i><br>
	 * Returns any framework-supplied remote object actions that should be contributed to the popup menu
	 * for the given selection list. This does nothing if this adapter does not implement ISystemViewRemoteElementAdapter,
	 * else it potentially adds menu items for "User Actions" and Compile", for example. It queries the subsystem
	 * factory of the selected objects to determine if these actions are appropriate to add.
	 * 
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell of viewer calling this. Most dialogs require a shell.
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addCommonRemoteActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		if (this instanceof ISystemRemoteElementAdapter)
		{
			ISystemRemoteElementAdapter rmtAdapter = (ISystemRemoteElementAdapter)this;
			Object firstSelection = getFirstSelection(selection);
			ISubSystem ss = rmtAdapter.getSubSystem(firstSelection);
			if (ss != null)
			{
				ISubSystemConfiguration ssf = ss.getSubSystemConfiguration();
				ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)ssf.getAdapter(ISubsystemConfigurationAdapter.class);
				adapter.addCommonRemoteActions(ssf, menu, selection, shell, menuGroup, ss);
			}			
		}

	}

	
	/**
     * <i>Called by system viewers. No need to override or call.</i><br>
	 * Contributes actions provided via the <code>dynamicPopupMenuExtensions</code> extension point.  Unlike
	 * addCommonRemoteActions(), these contributions are for any artifact in the RSE views and are contributed
	 * independently of subsystem factories.
	 * 
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell of viewer calling this. Most dialogs require a shell.
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addDynamicPopupMenuActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		// system view adapter menu extensions
		// these extensions are independent of subsystem factories and are contributed via extension point
		SystemDynamicPopupMenuExtensionManager.getInstance().populateMenu(shell, menu.getMenuManager(), selection, menuGroup);	
	}

	/**
	 * This is your opportunity to add actions to the popup menu for the given selection. 
	 * <p>
	 * To put your action into the given menu, use the menu's {@link org.eclipse.rse.ui.SystemMenuManager#add(String,IAction) add} method.
	 * If you don't care where it goes within the popup, just pass the given <samp>menuGroup</samp> location id,
	 * otherwise pass one of the GROUP_XXX values from {@link ISystemContextMenuConstants}. If you pass one that
	 * identifies a pre-defined cascading menu, such as GROUP_OPENWITH, your action will magically appear in that
	 * cascading menu, even if it was otherwise empty.
	 * <p>
	 * For the actions themselves, you will probably use one of the base action classes:
	 * <ul>
	 *   <li>{@link org.eclipse.rse.ui.actions.SystemBaseAction SystemBaseAction}. For a simple action doesn't present any UI.
	 *   <li>{@link org.eclipse.rse.ui.actions.SystemBaseDialogAction SystemBaseDialogAction}. For an action that presents a {@link org.eclipse.rse.ui.dialogs.SystemPromptDialog dialog}.
	 *   <li>{@link org.eclipse.rse.ui.actions.SystemBaseDialogAction SystemBaseWizardAction}. For an action that presents a {@link org.eclipse.rse.ui.wizards.AbstractSystemWizard wizard}.
	 *   <li>{@link org.eclipse.rse.ui.actions.SystemBaseSubMenuAction SystemBaseSubMenuAction}. For an action that cascades into a submenu with other actions.
	 * </ul>
	 * 
	 * @param menu the popup menu you can contribute to
	 * @param selection the current selection in the calling tree or table view
	 * @param parent the shell of the calling tree or table view
	 * @param menuGroup the default menu group to place actions into if you don't care where they. Pass this to the SystemMenuManager {@link org.eclipse.rse.ui.SystemMenuManager#add(String,IAction) add} method. 
	 * 
	 * @see org.eclipse.rse.ui.view.ISystemViewElementAdapter#addActions(SystemMenuManager, IStructuredSelection, Shell, String)
	 */
	public abstract void addActions(SystemMenuManager menu,IStructuredSelection selection,Shell parent,String menuGroup);
	
	/**
     * <i><b>Abstract</b>. Must be overridden by subclasses.</i><br>
	 * IWorkbenchAdapter method. Returns an image descriptor for the image. 
	 * More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public abstract ImageDescriptor getImageDescriptor(Object element);

		
	/**
     * <i><b>Abstract</b>. Must be overridden by subclasses.</i><br>
	 * Return the label for this object.
	 * @see #getName(Object)
	 * @see #getAbsoluteName(Object)
	 */
	public abstract String getText(Object element);

	/**
	 * Return the alternate label for this object.  By default this
	 * just returns the regular label.  If a custom label is required,
	 * this provides the means to it.
	 * @see #getName(Object)
	 * @see #getAbsoluteName(Object)
	 */
	public String getAlternateText(Object element)
	{
		return getText(element);
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, but rarely needs to be.</i><br>
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * By default, returns <samp>getText(element);</samp>, but child classes can override if display name doesn't equal real name.
	 * <p>
	 * Called by common rename and delete actions, and used to populate property sheet.
	 * @see #getText(Object)
	 * @see #getAbsoluteName(Object)
	 */
	public String getName(Object element)
	{
		return getText(element);
	}
    /**
     * <i><b>Abstract</b>. Must be overridden.</i><br>.
     * Return the fully-qualified name, versus just the displayable name, for this object.
     * For remote objects, this should be sufficient to uniquely identify this object within its
     *  subsystem.
	 * @see #getText(Object)
	 * @see #getName(Object)
	 */
	public abstract String getAbsoluteName(Object object);	
	
	/**
     * <i>Internal use. Can be safely ignored.</i><br>
	 * Return the name for this object. Unique requirement for IWorkbenchAdapter.
	 * We map to <samp>getText(element)</samp>.
	 */
	public String getLabel(Object element)
	{
		return getText(element);
	}
	
	/**
     * <i><b>Abstract</b>. Must be overridden by subclasses.</i><br>
	 * Return the type label for this object.
	 */
	public abstract String getType(Object element);	

	/**
     * <i><b>Overridable</b> by subclasses, but rarely needs to be.</i><br>
	 * Return the string to display in the status line when the given object is selected.
	 * The default is: 
	 * <pre><samp>
	 *   getType(): getName()
	 * </pre></samp>
	 */
	public String getStatusLineText(Object element)
	{
		return getType(element) + ": " + getName(element);
	}
		
	/**
     * <i><b>Abstract</b>. Must be overridden by subclasses.</i><br>
	 * Return the parent of this object. This is required by eclipse UI adapters, but
	 *  we try desperately not to use in the RSE. So, you are probably safe returning null,
	 *  but if can return a parent, why not, go for it.
	 */
	public abstract Object getParent(Object element);

	/**
     * <i><b>Abstract</b>. Must be overridden by subclasses.</i><br>
	 * Return true if this object has children.
	 */
	public abstract boolean hasChildren(Object element);
	
	/**
     * <i><b>Abstract</b>. Must be overridden by subclasses.</i><br>
	 * Return the children of this object. Return null if children not supported.
	 */
	public abstract Object[] getChildren(Object element);
	
	/**
     * This should be overridden by subclasses in order to provide
     * deferred query support via the Eclipse Jobs mechanism
	 * Return the children of this object. Return null if children not supported.
	 */
	public Object[] getChildren(IProgressMonitor monitor, Object element)
	{
		return getChildren(element);
	}
	

	/**
     * <i><b>Overridable</b> by subclasses, but rarely needs to be.</i><br>
	 * Return the children of this object, using the given Expand-To filter.
	 * By default, this calls getChildren(element). Override only if you support Expand-To menu actions.
	 */
    public Object[] getChildrenUsingExpandToFilter(Object element, String expandToFilter)
    {
    	return getChildren(element);
    }	
	
	/**
     * <i>Callable by subclasses.</i><br>
	 * Return the default descriptors for all system elements.
 	 */
	protected static IPropertyDescriptor[] getDefaultDescriptors() 
	{
		if (propertyDescriptorArray == null)
		{
		  propertyDescriptorArray = new PropertyDescriptor[3];
	      // The following determine what properties will be displayed in the PropertySheet
	      // resource type
	      int idx = 0;
	      propertyDescriptorArray[idx++] = createSimplePropertyDescriptor(P_TYPE, SystemPropertyResources.RESID_PROPERTY_TYPE_LABEL, SystemPropertyResources.RESID_PROPERTY_TYPE_TOOLTIP);
	      // resource name
	      propertyDescriptorArray[idx++] = createSimplePropertyDescriptor(P_TEXT, SystemPropertyResources.RESID_PROPERTY_NAME_LABEL, SystemPropertyResources.RESID_PROPERTY_NAME_TOOLTIP);
	      // number of children in tree currently
	      propertyDescriptorArray[idx++] = createSimplePropertyDescriptor(P_NBRCHILDREN, SystemViewResources.RESID_PROPERTY_NBRCHILDREN_LABEL, SystemViewResources.RESID_PROPERTY_NBRCHILDREN_TOOLTIP);

		}
		//System.out.println("In getDefaultDescriptors() in AbstractSystemViewAdapter");
		return propertyDescriptorArray;
	}

	/**
     * <i>Callable by subclasses.</i><br>
	 * Create and return a simple string readonly property descriptor.
	 * @param propertyKey Key for this property, sent back in getPropertyValue.
	 * @param label
	 * @param description
	 */
	protected static PropertyDescriptor createSimplePropertyDescriptor(String propertyKey, String label, String description)
	{
	    PropertyDescriptor pd = new PropertyDescriptor(propertyKey, label);
	    pd.setDescription(description);
	    return pd;
	}
	

	/**
     * <i>Needed by framework for property sheet. No need to call or override.</i><br>
 	 * Returns a value for this object that can be edited in a property sheet.
 	 *
 	 * @return a value that can be editted
 	 */
	public Object getEditableValue() 
	{
		return this;
	}
	/**
     * <i>Implemented. Do not override typically. See {@link #internalGetPropertyDescriptors()}.</i><br>
	 * Returns the property descriptors defining what properties are seen in the property sheet.
	 * By default returns descriptors for name, type and number-of-children only plus whatever
	 *  is returned from internalGetPropertyDescriptors().
	 * 
	 * @return an array containing all descriptors.  
	 * 
	 * @see #internalGetPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() 
	{
		IPropertyDescriptor[] addl = internalGetPropertyDescriptors();
		if ((addl == null)	|| (addl.length==0))
		   return getDefaultDescriptors();
		else
		{
			IPropertyDescriptor[] defaults = getDefaultDescriptors();
			IPropertyDescriptor[] all = new IPropertyDescriptor[defaults.length+addl.length];
			int allIdx=0;
			for (int idx=0; idx<defaults.length; idx++)
			   all[allIdx++] = defaults[idx];
			for (int idx=0; idx<addl.length; idx++)
			   all[allIdx++] = addl[idx];			   
			return all;
		}
	}	
	
	/**
	 * <i><b>Abstract</b>.<i><br>
	 *  Implement this to return the property descriptors for the
	 *  properties in the property sheet. This is beyond the Name, Type and NbrOfChildren
	 *  properties which already implemented and done for you.
	 * <p>
	 * Override if want to include more properties in the property sheet,</p>
	 * <p>If you override this for readonly properties, you must also override:</p>
	 * <ul>
	 *  <li>{@link #getPropertyValue(Object)}
	 * </ul>
	 * <p>If you override this for editable properties, you must also override:</p>
	 * <ul>
	 *  <li>{@link #isPropertySet(Object)}
	 *  <li>{@link #resetPropertyValue(Object)}
	 *  <li>{@link #setPropertyValue(Object,Object)}
	 * </ul>
	 * 
	 * @return an array containing all descriptors to be added to the default set of descriptors, or null
	 *   if no additional properties desired.
	 * @see #createSimplePropertyDescriptor(String,ResourceBundle,String)
	 */
	protected abstract IPropertyDescriptor[] internalGetPropertyDescriptors();	
	

	/**
     * <i>Callable by subclasses. Do not override.</i><br>
	 * Returns the list of property descriptors that are unique for this
	 * particular adapter - that is the difference between the default
	 * property descriptors and the total list of property descriptors.
	 * <p>
	 * If internalGetPropertyDescriptors() returns non-null, then returns that,
	 *  else computes the difference.
	 * <p>
	 * This is called by the table views like {@link org.eclipse.rse.ui.view.SystemTableView}.
	 */
	public IPropertyDescriptor[] getUniquePropertyDescriptors()
	{
		//optimization by phil in 5.1.2:
		IPropertyDescriptor[] internalDescriptors = internalGetPropertyDescriptors();
		if (internalDescriptors != null)
			return internalDescriptors;
		
		IPropertyDescriptor[] allDescriptors = getPropertyDescriptors();
		IPropertyDescriptor[] commonDescriptors = getDefaultDescriptors();
		
		int totalSize = allDescriptors.length;
		int commonSize = commonDescriptors.length;
		int uniqueSize = totalSize - commonSize;
		
		int uniqueIndex = 0;
		
		IPropertyDescriptor[] uniqueDescriptors = new IPropertyDescriptor[uniqueSize];
		for (int i = 0; i < totalSize; i++)
		{
			IPropertyDescriptor descriptor = allDescriptors[i];
			
			boolean isUnique = true;
			for (int j = 0; j < commonSize; j++)
			{
				IPropertyDescriptor commonDescriptor = commonDescriptors[j];
				if (descriptor == commonDescriptor)
				{
					isUnique = false;
				}	
			}	
			
			if (isUnique && uniqueSize > uniqueIndex)
			{
				uniqueDescriptors[uniqueIndex] = descriptor;
				uniqueIndex++;	
			}
		}
		
		return uniqueDescriptors;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Similar to getPropertyValue(Object key) but takes an argument
	 * for determining whether to return a raw value or formatted value.
	 * <b>
	 * By default, simply calls getPropertyValue(key).
	 * <p>
	 * This is called by the table views in order to get values that can be sorted when the
	 *  user clicks on the column heading. To support this for a numeric property say, return
	 *  a Long/Integer object if false, versus returning string.
	 * 
	 * @param property the name or key of the property as named by its property descriptor
	 * @param formatted indication of whether to return the value in formatted or raw form
	 * @return the current value of the given property
	 */
	public Object getPropertyValue(Object key, boolean formatted)
	{
		return getPropertyValue(key);
	}
		
	/**
     * <i>Implemented. Do not override typically. See {@link #internalGetPropertyValue(Object)}.</i><br>
	 * Returns the current value for the named property.<br>
	 * By default handles ISystemPropertyConstants.P_TEXT, P_TYPE and P_NBRCHILDREN only, then defers to {@link #internalGetPropertyValue(Object)} for
	 *  subclasses.
	 * <br><b>Note</b>: you will need to reference <code>propertySourceInput</code>, which is the currently selected object. Just case it to what you expect the selected object's type to be.
	 * 
	 * @param key	the name of the property as named by its property descriptor
	 * @return the current value of the property
	 */
	public Object getPropertyValue(Object key) 
	{
		String name = (String)key;
		if (name.equals(P_TEXT))
		  	//return getText(propertySourceInput);
		  	return getName(propertySourceInput);
		else if (name.equals(P_TYPE))
		  	return getType(propertySourceInput);
		else if (name.equals(P_NBRCHILDREN))
		{
			ISystemTree tree = getSystemTree();
			if (tree != null)
			  	return Integer.toString(tree.getChildCount(propertySourceInput)); 
			else
			{
			  	if ((viewer != null) && (viewer instanceof TreeViewer))
			    	return Integer.toString(getChildCount((TreeViewer)viewer, propertySourceInput));
			  	else
			    	return "0";
			}
		}
		else
 		  return internalGetPropertyValue(key);	
	}
	/**
	 * <i><b>Abstract</b>.<i><br>
	 *  Implement this to return the property descriptors for the
	 *  properties in the property sheet. This is beyond the Name, Type and NbrOfChildren
	 *  properties which already implemented and done for you.
	 * 
	 * @param key	the name of the property as named by its property descriptor
	 * @return the current value of the property or null if not a known property.
	 */
	protected abstract Object internalGetPropertyValue(Object key);
	

	/**
	 * Return the number of immediate children in the tree, for the given tree node
	 */
    private int getChildCount(TreeViewer viewer, Object element)
	{		
		if (viewer.getControl().isDisposed())
		  return 0;
		if (viewer.getExpandedState(element) == false)
		  return 0;
		
		Widget w = findItemInTree(viewer, element);
		if (w != null)
		{
		  if (w instanceof TreeItem)
		    return ((TreeItem)w).getItemCount();
		  else if (w instanceof Tree)
		    return ((Tree)w).getItemCount();
		}		
		return 0;
	}

    private Widget findItemInTree(TreeViewer tree, Object element) 
    {
  	   Item[] items = getChildren(tree.getControl());
	   if (items != null) 
	   {
		  for (int i= 0; i < items.length; i++) 
		  {
			  Widget o = internalFindItem(tree.getTree(), items[i], element);
			  if (o != null)
				return o;
		  }
	   }
	   return null;
    }
    
    private Widget internalFindItem(Tree tree, Item parent, Object element) 
    {
    	// compare with node
    	Object data= parent.getData();
    	if (data != null) 
    	{
    		if (data.equals(element))
    			return parent;
    	}
    	// recurse over children
    	Item[] items= getChildren(parent);
    	for (int i= 0; i < items.length; i++) 
    	{
    		Item item= items[i];
    		Widget o = internalFindItem(tree, item, element);
    		if (o != null)
    			return o;
    	}
    	return null;
    }
    private Item[] getChildren(Widget o) 
    {
    	if (o instanceof TreeItem)
    		return ((TreeItem) o).getItems();
    	if (o instanceof Tree)
    		return ((Tree) o).getItems();
    	return null;
    }

	
	/**
     * <i><b>Overridable</b> by subclasses. Must be iff editable properties are supported.</i><br>
	 * Returns whether the property value has changed from the default.
	 * Only applicable for editable properties.
	 * <br>RETURNS FALSE BY DEFAULT.
	 * @return	<code>true</code> if the value of the specified property has changed
	 *			from its original default value; <code>false</code> otherwise.
	 */
	public boolean isPropertySet(Object key) 
	{
		return false;
	}
	/**
     * <i><b>Overridable</b> by subclasses. Must be iff editable properties are supported.</i><br>
	 * Resets the specified property's value to its default value.
	 * Called on editable property when user presses reset button in property sheet viewer.
	 * DOES NOTHING BY DEFAULT.
	 * 
	 * @param 	key	the key identifying property to reset
	 */
	public void resetPropertyValue(Object key) 
	{
	}
	/**
     * <i><b>Overridable</b> by subclasses. Must be iff editable properties are supported.</i><br>
	 * Sets the named property to the given value.
	 * Called after an editable property is changed by the user.
	 * 
	 * DOES NOTHING BY DEFAULT.
	 * 
	 * @param 	key	the key identifying property to reset
	 * @param	value 	the new value for the property
	 */
	public void setPropertyValue(Object key, Object value) 
	{
	}	
	
	/**
     * <i>Called from adapter factories. Do not override.</i><br>
	 * Set input object for property source queries. This <b>must</b> be called by your
	 *  XXXAdaptorFactory before returning this adapter object.
	 */
	public void setPropertySourceInput(Object propertySourceInput)
	{
		this.propertySourceInput = propertySourceInput;
	}
  
    /**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
     * User has double clicked on an object. If you want to do something special, 
     *  do it and return true. Otherwise return false to have the viewer do the default behaviour.   
     */
    public boolean handleDoubleClick(Object element)
    {
    	return false;
    }
     
	// ------------------------------------------
	// METHODS TO SUPPORT GLOBAL DELETE ACTION...
	// ------------------------------------------
	
	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Return true if we should show the delete action in the popup for the given element.
	 * If true, then canDelete will be called to decide whether to enable delete or not.
	 * <p>By default, returns true.
	 * @see #canDelete(Object)
	 * @see #doDelete(Shell,Object)
	 */
	public boolean showDelete(Object element)
	{
		return true;
	}	
	/**
     * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 * <p>
	 * By default, returns false. Override if your object is deletable.
	 * @see #showDelete(Object)
	 * @see #doDelete(Shell,Object)
	 */
	public boolean canDelete(Object element)
	{
		return false;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Perform the delete action. By default does nothing. Override if your object is deletable.
	 * Return true if this was successful. Return false if it failed and you issued a msg. 
	 * Throw an exception if it failed and you want to use the generic msg.
	 * @see #showDelete(Object)
	 * @see #canDelete(Object)
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception
	{
		return doDelete(shell, element);
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Perform the delete action. By default just calls the doDelete method for each item in the resourceSet. 
	 * Override if you wish to perform some sort of optimization for the batch delete.
	 * Return true if this was successful. Return false if ANY delete op failed and a msg was issued. 
	 * Throw an exception if ANY failed and you want to use the generic msg.
	 */
	public boolean doDeleteBatch(Shell shell, List resourceSet, IProgressMonitor monitor) throws Exception
	{
		boolean ok = true;
		for (int i = 0; i < resourceSet.size(); i++)
		{
			ok = ok && doDelete(shell, resourceSet.get(i), monitor);
		}
		return ok;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Perform the delete action. By default does nothing. Override if your object is deletable.
	 * Return true if this was successful. Return false if it failed and you issued a msg. 
	 * Throw an exception if it failed and you want to use the generic msg.
	 * @see #showDelete(Object)
	 * @see #canDelete(Object)
	 * @deprecated use the one with the monitor
	 */
	public boolean doDelete(Shell shell, Object element) throws Exception
	{
		return false;
	}

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON RENAME ACTION...
	// ------------------------------------------
	
	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Return true if we should show the rename action in the popup for the given element.
	 * If true, then canRename will be called to decide whether to enable rename or not.
	 * <p>By default, returns true.
	 * @return true if we should show the rename action in the popup for the given element.
	 * @see #canRename(Object)
	 * @see #doRename(Shell,Object,String)
	 */
	public boolean showRename(Object element)
	{
		return true;
	}	
	/**
     * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename popup menu item will be enabled.
	 * By default, returns false. Override if your object is renamable.
	 * @return true if this object is renamable by the user
	 * @see #showRename(Object)
	 * @see #doRename(Shell,Object,String)
	 * @see #getNameValidator(Object)
	 * @see #getCanonicalNewName(Object,String)
	 * @see #namesAreEqual(Object,String)
	 */
	public boolean canRename(Object element)
	{
		return false;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Perform the rename action. By default does nothing. Override if your object is renamable.
	 * Return true if this was successful. Return false if it failed and you issued a msg. 
	 * Throw an exception if it failed and you want to use the generic msg.
	 * @return true if the rename was successful
	 * @see #showRename(Object)
	 * @see #canRename(Object)
	 */
	public boolean doRename(Shell shell, Object element, String name) throws Exception
	{
		//org.eclipse.rse.core.ui.SystemMessage.displayErrorMessage("INSIDE DORENAME");
		return false;
	}

	/**
     * <i><b>Overridable</b> by subclasses, and usually is iff canRename is.</i><br>
	 * Return a validator for verifying the new name is correct.
	 * If you return null, no error checking is done on the new name in the common rename dialog!! 
	 * <p>
	 * Used in the common rename dialogs, and only if you return true to {@link #canRename(Object)}.
	 * <p>
	 * Suggest you use at least UniqueStringValidator or a subclass to ensure
	 *  new name is at least unique.
	 * @see #canRename(Object)
	 */
    public ISystemValidator getNameValidator(Object element)
    {
    	return null;
    }    		

    /**
     * <i><b>Overridable</b> by subclasses, and usually is iff canRename is.</i><br>
     * Form and return a new canonical (unique) name for this object, given a candidate for the new
     *  name. This is called by the generic multi-rename dialog to test that all new names are unique.
     *  To do this right, sometimes more than the raw name itself is required to do uniqueness checking.
     * <p>
     *  For example, two connections or filter pools can have the same name if they are
     *  in different profiles. Two iSeries QSYS objects can have the same name if their object types 
     *  are different. 
	 * <p>
	 * Used in the common rename dialogs, and only if you return true to {@link #canRename(Object)}.
     * <p>
     * This method returns a name that can be used for uniqueness checking because it is qualified 
     *  sufficiently to make it unique.
     * <p>
     * By default, this simply returns the given name. It is overridden by child classes when appropriate.
	 * @see #canRename(Object)
     */
    public String getCanonicalNewName(Object element, String newName)
    {
    	// this is all for defect 42145. Phil
    	return newName;
    }
    /**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
     * Compare the name of the given element to the given new name to decide if they are equal.
     * Allows adapters to consider case and quotes as appropriate.
	 * <p>
	 * Used in the common rename dialogs, and only if you return true to {@link #canRename(Object)}.
     * <p>
     * By default does an equalsIgnoreCase comparison
	 * @see #canRename(Object)
     */
    public boolean namesAreEqual(Object element, String newName)
    {
    	return getName(element).equalsIgnoreCase(newName);
    }

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON REFRESH ACTION...
	// ------------------------------------------
	/**
     * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Return true if we should show the refresh action in the popup for the given element.
	 * Note the actual work to do the refresh is handled for you.
	 * <p>
	 * Default is true.
	 */
	public boolean showRefresh(Object element)
	{
		return true;
	}    

	// ------------------------------------------------------------
	// METHODS TO SUPPORT COMMON OPEN-IN-NEW-PERSPECTIVE ACTIONS...
	// ------------------------------------------------------------
	/**
     * <i><b>Overridable</b> by subclasses, and usually is NOT.</i><br>
	 * Return true if we should show the <b>Go Into;</b> and <b>Open In New Window</b> 
	 * and <b>Go To</b> actions in the popup for the given element.
	 * <p>
	 * Only applicable for non-remote resources. Remote always show <b>Go To</b> only.
	 */
	public boolean showOpenViewActions(Object element)
	{
		return true;
	}    
	
	/**
	  * <i><b>Overridable</b> by subclasses, and usually is NOT.</i><br>
	  * Return true if we should show the generic show in table action in the popup for the given element.
	  */
	public boolean showGenericShowInTableAction(Object element)
	{
		return true;	
	}

	
	// ------------------------------------------
	// METHODS TO SUPPORT COMMON DRAG AND DROP FUNCTION...
	// ------------------------------------------	
	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 *  Return true if this object can be copied to another location.  By default,
	 *  we return false.  Extenders may decide whether or not
	 *  certain objects can be dragged with this method.
	 * @see #doDrag(Object,boolean,IProgressMonitor)
	 * @see #canDrop(Object)
	 * @see #doDrop(Object,Object,boolean,boolean,IProgressMonitor)
	 * @see #validateDrop(Object,Object,boolean)
	 */
	public boolean canDrag(Object element)
	{
		return false;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 *  Return true if this object can be copied to another location.  By default,
	 *  we return false.  Extenders may decide whether or not
	 *  certain objects can be dragged with this method.
	 *  Return true if these objects can be copied to another location via drag and drop, or clipboard copy.
	 */
	public boolean canDrag(SystemRemoteResourceSet elements)
	{
		return false;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 *  Perform the drag on the given object. By default this does nothing
	 *  and returns nothing.  Extenders supporting DnD are expected to implement
	 *  this method to perform a copy to a temporary object, the return value. 
	 * @see #canDrag(Object)
	 * @see #canDrop(Object)
	 * @see #doDrop(Object,Object,boolean,boolean,IProgressMonitor)
	 * @see #validateDrop(Object,Object,boolean)
	 */
	public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor)
	{
		return null;	
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 * Return true if another object can be copied into this object.  By default
	 * we return false.  Extenders may decide whether or not certain objects can 
	 * accept other objects with this method.
	 * @see #canDrag(Object)
	 * @see #doDrag(Object,boolean,IProgressMonitor)
	 * @see #doDrop(Object,Object,boolean,boolean,IProgressMonitor)
	 * @see #validateDrop(Object,Object,boolean)
	 */
	public boolean canDrop(Object element)
	{
		return false;	
		
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 * Perform the drag on the given objects.  This default implementation simply iterates through the
	 * set.  For optimal performance, this should be overridden.
	 * 
	 * @param set the set of objects to copy
	 * @param sameSystemType indication of whether the source and target reside on the same type of system
	 * @param monitor the progress monitor
	 * @return a temporary workspace copies of the object that was copied
	 * 
	 */
	public ISystemResourceSet doDrag(SystemRemoteResourceSet set, IProgressMonitor monitor)
	{
		SystemWorkspaceResourceSet results = new SystemWorkspaceResourceSet();
		List resources = set.getResourceSet();
		for (int i = 0; i < resources.size(); i++)
		{
			results.addResource(doDrag(resources.get(i), true, monitor));
		}
		return results;
	}

	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 *  Perform drop from the "fromSet" of objects to the "to" object
	 * @param from the source objects for the drop
	 * @param to the target object for the drop
	 * @param sameSystemType indication of whether the source and target reside of the same type of system
	 * @param sameSystem indication of whether the source and target are on the same system
	 * @param srcType the type of objects to be dropped
	 * @param monitor the progress monitor
	 * 
	 * @return the set of new objects created from the drop
	 * 
	 */ 
	public ISystemResourceSet doDrop(ISystemResourceSet fromSet, Object to, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor)
	{
		SystemRemoteResourceSet results = new SystemRemoteResourceSet(getSubSystem(to), this);
		
		List resources = fromSet.getResourceSet();
		for (int i = 0; i < resources.size(); i++)
		{
			results.addResource(doDrop(resources.get(i), to, sameSystemType, sameSystem, srcType, monitor));
		}
		
		return results;
	}
	
	/**
	 * Sets filter context for querying.  Override to provide specialized 
	 * behaviour.
	 */
	public void setFilterString(String filterString)
	{
		this.filterString = filterString;
	}
	
	/**
	 * Gets filter context for querying.  Override to provide specialized 
	 * behaviour.
	 */
	public String getFilterString()
	{
		return filterString;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 *  Perform drop from the "from" object to the "to" object.  By default this does 
	 *  nothing and we return false.   Extenders supporting DnD are expected to implement
	 *  this method to perform a "paste" into an object.   
	 * 
	 * @return the new object that was copied
	 * 
	 * @see #canDrag(Object)
	 * @see #doDrag(Object,boolean,IProgressMonitor)
	 * @see #canDrop(Object)
	 * @see #validateDrop(Object,Object,boolean)
	 */ 
	public Object doDrop(Object from, Object to, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor)
	{
		// for backward compatability
		return doDrop(from, to, sameSystemType, sameSystem, monitor);
	}
	
	/**
	 * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 *  Perform drop from the "from" object to the "to" object.  By default this does 
	 *  nothing and we return false.   Extenders supporting DnD are expected to implement
	 *  this method to perform a "paste" into an object.   
	 * 
	 * @return the new object that was copied
	 * 
	 * @see #canDrag(Object)
	 * @see #doDrag(Object,boolean,IProgressMonitor)
	 * @see #canDrop(Object)
	 * @see #validateDrop(Object,Object,boolean)
	 * 
	 * @deprecated use doDrop(Object from, Object to, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor) instead
	 */ 
	public Object doDrop(Object from, Object to, boolean sameSystemType, boolean sameSystem,  IProgressMonitor monitor)
	{
		return null;	
	}
	  
    /**
     * <i><b>Overridable</b> by subclasses, and usually is iff drag and drop supported..</i><br>
     * Return true if it is valid for the src object to be dropped in the target. We return false by default.
     * @param src the object to drop
     * @param target the object which src is dropped in
     * @param sameSystem whether this is the same system or not
     * @return whether this is a valid operation
     * 
	 * @see #canDrag(Object)
	 * @see #doDrag(Object,boolean,IProgressMonitor)
	 * @see #canDrop(Object)
	 * @see #doDrop(Object,Object,boolean,boolean,IProgressMonitor)
     */ 
    public boolean validateDrop(Object src, Object target, boolean sameSystem)
    {
    	return false;	
    }
    
	public boolean validateDrop(ISystemResourceSet set, Object target, boolean sameSystem)
	{
		boolean valid = true;
		List resources = set.getResourceSet();
		for (int i = 0; i < resources.size() && valid; i++)
		{
			valid = validateDrop(resources.get(i), target, sameSystem);
		}
		return valid;
	}

	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 * This just defaults to getName, but if that is not sufficient override it here.
	 */
	public String getMementoHandle(Object element)
	{
		if (this instanceof ISystemRemoteElementAdapter)
		  return ((ISystemRemoteElementAdapter)this).getAbsoluteName(element);
		else
		  return getName(element);
	}
	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Return what to save to disk to identify this element when it is the input object to a secondary
	 *  Remote Systems Explorer perspective. Defaults to getMementoHandle(element).
	 */
	public String getInputMementoHandle(Object element)
	{
		return getMementoHandle(element);
	}
	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		if (this instanceof ISystemRemoteElementAdapter)
		  return ISystemMementoConstants.MEMENTO_KEY_REMOTE;
		else
		  return getType(element);
	}

    /**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
     * Somtimes we don't want to remember an element's expansion state, such as for temporarily inserted 
     *  messages. In these cases return false from this method. The default is true
     */
    public boolean saveExpansionState(Object element)
    {
    	return true;
    }
    
    /**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
     * Return true if this object is a "prompting" object that prompts the user when expanded.
     * For such objects, we do not try to save and restore their expansion state on F5 or between
     * sessions.
     * <p>
     * Default is false unless element implements ISystemPromptable object. Override as appropriate.
     */
    public boolean isPromptable(Object element)
    {
    	return (element instanceof ISystemPromptableObject);
    } 
    /**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
     * Selection has changed in the Remote Systems view. Empty by default, but override if you need
     *  to track selection changed. For example, this is used to drive table views that respond to
     *  selection.
     * @param element - first selected object
     */
    public void selectionChanged(Object element)    // d40615   
    {
    }

	/**
     * <i><b>Overridable</b> by subclasses, typically if additional properties are supported.</i><br>
	 * From <samp>IActionFilter</samp> so the popupMenus extension point can use &lt;filter&gt;, &lt;enablement&gt;
	 * or &lt;visibility&gt;. The support is for the following:
	 * <ol>
	 *  <li>name="value". The given value must match getName(target) exactly, or if ends with asterisk must match the beginning.
	 *  <li>name="type". The given value must match getType(target) exactly. Be careful, as this is usually translated.
	 *  <li>name="hasChildren". If the given value is "true", returns true if hasChildren(target) returns true. If given "false",
	 *       returns true if the hasChildren returns false.
	 *  <li>name="connection". If the given value is "true", returns true if the subsystem is connected. If given "false",
	 *       returns true if the subsystem is not connected.
	 *  <li>name="offline". If the given value is "true", returns true if the subsystem is offline. If given "false", 
	 *       returns true if the subsystem is offline.
	 *  <li>name="systemType". The given value is a system type, and this returns true if this object's connection is of that
	 *       type. You can specify multiple values by comma-separating them, and this returns if there is a match on any them.
	 *  <li>name="subsystemFactoryId". The given value is a subsystem factory Id, and this returns true if this object's 
	 *       subsystem is from that subsystem factory. For connections, returns false.
	 *       You can specify multiple values by comma-separating them, and this returns if there is a match on any them.
	 *  <li>name="subsystemFactoryCategory". The given value is a subsystem category, and this returns true if this object's
	 *       subsystem is from a subsystem factory of that category. For connections, returns false.
	 *       You can specify multiple values by comma-separating them, and this returns if there is a match on any them.
	 * </ol>
	 * <p>
	 * If desired, override, and call super(), to support additional filter criteria for &lt;filter&gt;, &lt;enablement&gt; and &lt;visibility&gt;.
	 * 
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value)
	{
		//System.out.println("Inside testAttribute: name = " + name + ", value = " + value);
		if (name.equalsIgnoreCase("name"))
		{
			if (value.endsWith("*")) 
			{
				// we have a wild card test, and * is the last character in the value
				if (getName(target).startsWith(value.substring(0, value.length() - 1)))
					return true;
			} 
			else
				return value.equals(getName(target));
		}
		else if (name.equalsIgnoreCase("type"))
		  return value.equals(getType(target));
		else if (name.equalsIgnoreCase("hasChildren"))
		{
			return hasChildren(target) ? value.equals("true") : value.equals("false");
		}
		else if (name.equalsIgnoreCase("connected"))
		{
			ISubSystem ss = getSubSystem(target);
			if (ss != null)
			  return ss.isConnected() ? value.equals("true") : value.equals("false");
			else
			  return false;
		} 
		else if (name.equalsIgnoreCase("offline"))
		{ 
			ISubSystem ss = getSubSystem(target);
			if (ss != null)
			  return ss.isOffline() ? value.equals("true") : value.equals("false");
			else
			  return false;		    				
		}
		else if (name.equalsIgnoreCase("systemType"))
		{
			ISubSystem ss = getSubSystem(target);
			String[] values = tokenize(value);
			if (ss == null)
			{
				if (!(target instanceof IHost))
			      return false;	
			    String connSysType = ((IHost)target).getSystemType();		
			    for (int idx=0; idx<values.length; idx++)			
			    {
			    	if (connSysType.equals(values[idx]))
			    	   return true;
			    }
			    return false;
			}
			for (int idx=0; idx<values.length; idx++)			
			{
			  if (ss.getHost().getSystemType().equals(values[idx]))
			    return true;
			}
			return false;
		}
		else if (name.equalsIgnoreCase("subsystemFactoryId"))
		{
			ISubSystem ss = getSubSystem(target);
			//System.out.println("ss null? " + (ss == null));
			if (ss == null)
			  return false;
			String[] values = tokenize(value);
			//System.out.println("Nbr of values: " + (values.length));			
			//System.out.println("Comparing against: " + (ss.getParentSubSystemFactory().getId()));			
			boolean ok = false;
			for (int idx=0; !ok && (idx<values.length); idx++)			
			{
			  if (ss.getSubSystemConfiguration().getId().equals(values[idx]))
			    ok = true;
			}
			//System.out.println("Returning: " + ok);
			return ok;
		}
		else if (name.equalsIgnoreCase("subsystemFactoryCategory"))
		{
			ISubSystem ss = getSubSystem(target);
			if (ss == null)
			  return false;
			String[] values = tokenize(value);
			for (int idx=0; idx<values.length; idx++)			
			{
			  if (ss.getSubSystemConfiguration().getCategory().equals(values[idx]))
			    return true;
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Break given comma-delimited string into tokens
	 */
	private String[] tokenize(String input)
	{
          	StringTokenizer tokens = new StringTokenizer(input,";");
            Vector v = new Vector();
            while (tokens.hasMoreTokens())
              v.addElement(tokens.nextToken());
            String[] stringArray = new String[v.size()];
            for (int idx=0; idx<v.size(); idx++)
               stringArray[idx] = (String)v.elementAt(idx);
            return stringArray;
	}
	
	// --------------------------------------
	// ISystemRemoteElementAdapter methods...
	// We include these here so that if a BP
	// creates a class that extends this one
	// and implements the remote resource
	// adapter interface, these methods will
	// be already created, reducing work. 
	// These are low-usage methods.
	// --------------------------------------
	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
     * From {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubSubType(Object)}.
     * Pre-supplied for convenience for subclasses that want to implement this interface for
     *  remote object adapters.
     * <p>
     * Returns null. Override if you want to supply a sub-sub-type for filtering in the popupMenus extension point.
	 */
	public String getRemoteSubSubType(Object element)
	{
		return null; // Extremely fine grained. We don't use it.
	}

	/**
     * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
     * From {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubSubType(Object)}.
     * Pre-supplied for convenience for subclasses that want to implement this interface for
     *  remote object adapters.
     * <p>
     * Returns null. Override if the remote resource is compilable.
	 */
	public String getRemoteSourceType(Object element)
	{
		return null;
	}
	/**
     * <i><b>Overridable</b> by subclasses, and must be for editable objects.</i><br>
	 * Return the remote edit wrapper for this object.
	 * @param object the object to edit
	 * @return the editor wrapper for this object
	 */
	public ISystemEditableRemoteObject getEditableRemoteObject(Object object)
	{
		return null;
	}
	
	/**
     * <i><b>Overridable</b> by subclasses, and must be for editable objects.</i><br>
	 * Indicates whether the specified object can be edited or not.
	 * @param object the object to edit
	 * @return true if the object can be edited. Returns false by default.
	 */
	public boolean canEdit(Object object)
	{
		return false;	
	}
	
	// ------------------
	// HELPER METHODS...
	// ------------------	
    /**
     * <i>Callable by subclasses.</i><br>
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     * <p>Just a convenient shortcut to {@link org.eclipse.rse.core.SystemAdapterHelpers#getAdapter(Object, Viewer)}
     */
    protected ISystemViewElementAdapter getAdapter(Object o) 
    {
        return SystemAdapterHelpers.getAdapter(o, getViewer());
        /*
    	ISystemViewElementAdapter adapter = null;
    	if (!(o instanceof IAdaptable)) 
          adapter = (ISystemViewElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemViewElementAdapter.class);
        else
    	  adapter = (ISystemViewElementAdapter)((IAdaptable)o).getAdapter(ISystemViewElementAdapter.class);
    	if (adapter == null)
    	  SystemPlugin.logDebugMessage(this.getClass().getName(), "ADAPTER IS NULL FOR ELEMENT : " + o);
    	else
    	{
    		adapter.setViewer(getViewer()); // added this in V5.0, just in case. Phil
    	}
    	return adapter;
    	*/
    }    
    /**
     * <i>Callable by subclasses.</i><br>
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     * <p>Just a convenient shortcut to {@link org.eclipse.rse.core.SystemAdapterHelpers#getRemoteAdapter(Object, Viewer)}
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	// hmmm, any reason why we shouldn't do the following 2 lines of code for performance reasons?
		//if (this instanceof ISystemRemoteElementAdapter)
		//  return (ISystemRemoteElementAdapter)this;
        return SystemAdapterHelpers.getRemoteAdapter(o, getViewer());
        /*
    	if (!(o instanceof IAdaptable)) 
          adapter = (ISystemRemoteElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemRemoteElementAdapter.class);
    	adapter = (ISystemRemoteElementAdapter)((IAdaptable)o).getAdapter(ISystemRemoteElementAdapter.class);
    	if ((adapter != null) && (adapter instanceof ISystemViewElementAdapter))
    	{
    		((ISystemViewElementAdapter)adapter).setViewer(getViewer()); // added this in V5.0, just in case. Phil
    	}
    	return adapter;
    	*/
    }
    
	/**
     * <i>Callable by subclasses.</i><br>
	 * Do message variable substitution. Using you are replacing &1 (say) with
	 *  a string.
	 * @param message containing substitution variable. Eg "Connect failed with return code &1"
	 * @param substitution variable. Eg "%1"
	 * @param substitution data. Eg "001"
	 * @return message with all occurrences of variable substituted with data.
	 */
	public static String sub(String msg, String subOld, String subNew)
	{
		StringBuffer temp = new StringBuffer();
		int lastHit = 0;
		int newHit = 0;
		for (newHit = msg.indexOf(subOld,lastHit); newHit != -1;
			 lastHit = newHit, newHit = msg.indexOf(subOld,lastHit))
		   {
			 if (newHit >= 0)
			   temp.append(msg.substring(lastHit,newHit));
			 temp.append(subNew);
			 newHit += subOld.length();
		   }
		if (lastHit >= 0)
		  temp.append(msg.substring(lastHit));
		return temp.toString();
	}  		
	
	/**
     * <i>Callable by subclasses. Do not override</i><br>
	 * Return the current viewer as an ISystemTree if the viewer is set and
	 *  it implements this interface (SystemView does). May be null so test it.
	 */
	protected ISystemTree getSystemTree()
	{
		Viewer v = getViewer();
	    if ((v != null) && (v instanceof ISystemTree))
		  return (ISystemTree)v;
		else 
		  return null;
	}
	
	/**
     * <i>Callable by subclasses. Do not override</i><br>
	 * Return "Yes" translated
	 */
	public String getTranslatedYes()
	{
		if (xlatedYes == null)
		  xlatedYes = SystemResources.TERM_YES;
		return xlatedYes;
	} 

	/**
     * <i>Callable by subclasses. Do not override</i><br>
	 * Return "No" translated
	 */
	protected String getTranslatedNo()
	{
		if (xlatedNo == null)
		  xlatedNo = SystemResources.TERM_NO;
		return xlatedNo;
	} 

	/**
     * <i>Callable by subclasses. Do not override</i><br>
	 * Return "True" translated
	 */
	protected String getTranslatedTrue()
	{
		if (xlatedTrue == null)
		  xlatedTrue = SystemResources.TERM_TRUE;
		return xlatedTrue;
	} 
	/**
     * <i>Callable by subclasses. Do not override</i><br>
	 * Return "False" translated
	 */
	protected String getTranslatedFalse()
	{
		if (xlatedFalse == null)
		  xlatedFalse = SystemResources.TERM_FALSE;
		return xlatedFalse;
	} 
	/**
     * <i>Callable by subclasses. Do not override</i><br>
	 * Return "Not application" translated
	 */
	protected String getTranslatedNotApplicable()
	{
		if (xlatedNotApplicable == null)
		  xlatedNotApplicable = SystemPropertyResources.RESID_TERM_NOTAPPLICABLE;
		return xlatedNotApplicable;
	} 
	/**
     * <i>Callable by subclasses. Do not override</i><br>
	 * Return "Not available" translated
	 */
	protected String getTranslatedNotAvailable()
	{
		if (xlatedNotAvailable == null)
		  xlatedNotAvailable = SystemPropertyResources.RESID_TERM_NOTAVAILABLE;
		return xlatedNotAvailable;
	} 
	
	/**
     * <i>Internal use. Do not override</i><br>
     */
	protected void initMsgObjects()
	{
		nullObject     = new SystemMessageObject(SystemPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_EMPTY),ISystemMessageObject.MSGTYPE_EMPTY, null);
		canceledObject = new SystemMessageObject(SystemPlugin.getPluginMessage(ISystemMessages.MSG_LIST_CANCELLED),ISystemMessageObject.MSGTYPE_CANCEL, null);
		errorObject    = new SystemMessageObject(SystemPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED),ISystemMessageObject.MSGTYPE_ERROR, null);
	}
	
    /**
     * <i>Callable by subclasses. Do not override</i><br>
     * In getChildren, return <samp>checkForNull(children, true/false)<.samp> versus your array directly.
     * This method checks for a null array which is not allowed and replaces it with an empty array.
     * If true is passed then it returns the "Empty list" message object if the array is null or empty
     */
    protected Object[] checkForNull(Object[] children, boolean returnNullMsg)
    {
	   if ((children == null) || (children.length==0))
	   {
	   	 if (!returnNullMsg)
           return emptyList;
         else
         {
	 	   if (nullObject == null)
	 	     initMsgObjects();
	 	   msgList[0] = nullObject;
	 	   return msgList;
         }
	   }
       else
         return children;
    }

    /**
     * <i>Callable by subclasses. Do not override</i><br>
     * Return the "Operation cancelled by user" msg as an object array so can be used to answer getChildren()
     */
    protected Object[] getCancelledMessageObject()
    {    	
		 if (canceledObject == null)
		   initMsgObjects();
		 msgList[0] = canceledObject;
		 return msgList;
    }    
    /**
     * <i>Callable by subclasses. Do not override</i><br>
     * Return the "Operation failed" msg as an object array so can be used to answer getChildren()
     */
    protected Object[] getFailedMessageObject()
    {    	
		 if (errorObject == null)
		   initMsgObjects();
		 msgList[0] = errorObject;
		 return msgList;
    }    
    /**
     * <i>Callable by subclasses. Do not override</i><br>
     * Return the "Empty list" msg as an object array so can be used to answer getChildren()
     */
    protected Object[] getEmptyMessageObject()
    {    	
		 if (nullObject == null)
		   initMsgObjects();
		 msgList[0] = nullObject;
		 return msgList;
    }    
    
    /**
     * <i>Callable by subclasses. Do not override</i><br>
     * Get the first selected object of the given selection
     */
    protected Object getFirstSelection(IStructuredSelection selection)
    {
    	return selection.getFirstElement();
    }

	/**
	 * Return a filter string that corresponds to this object.
	 * @param object the object to obtain a filter string for
	 * @return the corresponding filter string if applicable
	 */
	public String getFilterStringFor(Object object)
	{
		return null;
	}
	
	
	/**
	 * these methods are for deferred fetch operations
	 */	
	
	/*
	 * Return whether deferred queries are supported. By default
	 * they are not supported.  Subclasses must override this to
	 * return true if they are to support this.
	 */
	public boolean supportsDeferredQueries()
	{
	    return false;
	}
	
	
	public void fetchDeferredChildren(Object o, IElementCollector collector, IProgressMonitor monitor) 
	{
        try 
        {
            monitor = Policy.monitorFor(monitor);
            monitor.beginTask(Policy.bind("RemoteFolderElement.fetchingRemoteChildren", getLabel(o)), 100); //$NON-NLS-1$
			SystemFetchOperation operation = getSystemFetchOperation(o, collector);
			operation.run(Policy.subMonitorFor(monitor, 100));
        } 
        catch (InvocationTargetException e) 
        {
            e.printStackTrace();
		} 
        catch (InterruptedException e) 
		{
			// Cancelled by the user;
		} 
        finally 
		{
            monitor.done();
        }
    }
	
	
	/**
	 * Returns the SystemFetchOperation to be used in performing a query.  Adapters should override
	 * this to provide customizations where appropriate.
	 * @param o
	 * @param collector
	 * @return the fetch operation.  By default it returns the base implementation
	 */
	protected SystemFetchOperation getSystemFetchOperation(Object o, IElementCollector collector)
	{
	    return new SystemFetchOperation(null, (IAdaptable)o, this, collector);
	}
	
	
	  /* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
     */
    public boolean isContainer()
    {
        return true;
    }

    public ISchedulingRule getRule(Object element) {
    	IAdaptable location = (IAdaptable)element;
        return new SystemSchedulingRule(location); 
    }
}