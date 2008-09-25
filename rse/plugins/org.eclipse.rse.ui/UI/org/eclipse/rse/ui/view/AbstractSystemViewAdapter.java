/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Uwe Stieber (Wind River) - Allow to extend action filter by dynamic system type providers.
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [189163] Update IActionFilter constants from subsystemFactory to subsystemConfiguration
 * Tobias Schwarz   (Wind River) - [173267] "empty list" should not be displayed
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David McKnight   (IBM) - [208803] add exists() method
 * Xuan Chen        (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * Martin Oberhuber (Wind River) - [234215] improve API documentation for doDelete and doDeleteBatch
 * David McKnight (IBM)          - [239368] Expand to action ignores the filter string
 * David McKnight (IBM)          - [243263] NPE on expanding a filter - null pointer checks
 *******************************************************************************/

package org.eclipse.rse.ui.view;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterStringReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.SystemPropertyResources;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.ISystemMementoConstants;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.internal.ui.view.SystemViewResources;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.model.ISystemPromptableObject;
import org.eclipse.rse.ui.operations.Policy;
import org.eclipse.rse.ui.operations.SystemFetchOperation;
import org.eclipse.rse.ui.operations.SystemSchedulingRule;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Base class for adapters needed for the SystemView viewer.
 * It implements the ISystemViewElementAdapter interface.
 * @see AbstractSystemRemoteAdapterFactory
 */
public abstract class AbstractSystemViewAdapter implements ISystemViewElementAdapter, IWorkbenchAdapter,
                                                           IDeferredWorkbenchAdapter
{
	// Static action filter per system type cache. Filled from testAttribute.
	private final static Map ACTION_FILTER_CACHE = new HashMap();

	// Internal helper class to cache system type -> no action filter relation ships.
	// Used from testAttribute.
	private final static class NULL_ACTION_FILTER implements IActionFilter {
		public boolean testAttribute(Object target, String name, String value) {
			return false;
		}
	}

	//protected boolean isEditable = false;

	private String filterString = null;

	/**
	 * Current viewer. Set by content provider.
	 *
	 * @deprecated use {@link #getViewer()} and {@link #setViewer(Viewer)}
	 */
	protected Viewer viewer = null;

	/**
	 * Current input provider. Set by content provider.
	 */
	protected Object propertySourceInput = null;

	/**
	 * Current shell, set by the content provider.
	 *
	 * @deprecated Use {@link #getShell()} and {@link #setShell(Shell)}
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
	 * An empty object list instance, for returning an empty list from
	 * getChildren: new Object[0].
	 *
	 * @deprecated Do not use directly. Use {@link #checkForEmptyList(Object[],
	 * 	Object, boolean)} when needed.
	 */
	protected Object[] emptyList = new Object[0];

	/**
	 * For returning a message object from getChildren. Will be an array with
	 * one item, one of nullObject, cancelledObject or errorObject.
	 *
	 * @deprecated Do not use directly. Use {@link #getCancelledMessageObject()}
	 * 	or {@link #getFailedMessageObject()} or {@link
	 * 	#checkForEmptyList(Object[], Object, boolean)} when needed.
	 */
	protected Object[] msgList   = new Object[1];

	/**
	 * Frequently returned message object from getChildren: "empty list"
	 *
	 * @deprecated Use {@link #checkForEmptyList(Object[], Object, boolean)}
	 *             instead.
	 */
	protected SystemMessageObject nullObject     = null;

	/**
	 * Frequently returned message object from getChildren: "operation
	 * cancelled".
	 *
	 * This field was renamed from "canceledObject" in RSE 3.0.
	 *
	 * @since org.eclipse.rse.ui 3.0
	 * @deprecated Use {@link #getCancelledMessageObject()} instead.
	 */
	protected SystemMessageObject cancelledObject = null;

	/**
	 * Frequently returned message object from getChildren: "operation ended in
	 * error"
	 *
	 * @deprecated Use {@link #getFailedMessageObject()} instead.
	 */
	protected SystemMessageObject errorObject    = null;

    /**
	 * Message substitution prefix: "&"
	 *
	 * @deprecated use Eclipse NLS or Java Messageformat for String
	 *             substitution.
	 */
	protected static final String MSG_SUB_PREFIX = "&"; //$NON-NLS-1$

	/**
	 * Message substitution variable 1: "&1"
	 *
	 * @deprecated use Eclipse NLS or Java Messageformat for String
	 * 	substitution.
	 */
	protected static final String MSG_SUB1       = MSG_SUB_PREFIX+"1"; //$NON-NLS-1$

	/**
	 * Message substitution variable 2: "&2"
	 *
	 * @deprecated use Eclipse NLS or Java Messageformat for String
	 * 	substitution.
	 */
	protected static final String MSG_SUB2       = MSG_SUB_PREFIX+"2"; //$NON-NLS-1$

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

	private Preferences fPrefStore = null;

	/**
	 * Static constructor.
	 */
	static {
		ACTION_FILTER_CACHE.clear();
	}

	// ------------------------------------------------------------------
	// Configuration methods, called by the label and content provider...
	// ------------------------------------------------------------------

	/**
	 * Set the viewer that is driving this adapter Called by label and content
	 * provider.
	 * <p>
	 * <i>Configuration method. Typically called by content provider, viewer or
	 * action. Do not override.</i>
	 */
    public final void setViewer(Viewer viewer)
    {
    	this.viewer = viewer;
    }

	/**
	 * Set the shell to be used by any method that requires it.
	 * <p>
	 * <i>Configuration method. Typically called by content provider, viewer or
	 * action. Do not override.</i>
	 */
    public final void setShell(Shell shell)
    {
    	this.shell = shell;
    }

	/**
	 * Set the input object used to populate the viewer with the roots. May be
	 * used by an adapter to retrieve context-sensitive information. This is set
	 * by the Label and Content providers that retrieve this adapter.
	 * <p>
	 * <i>Configuration method. Typically called by content provider, viewer or
	 * action. Do not override.</i>
	 */
    public final void setInput(ISystemViewInputProvider input)
    {
    	this.input = input;
    }

	// ------------------------------------------------------------------
	// Getter methods, for use by subclasses and actions...
	// ------------------------------------------------------------------

   	/**
	 * Get the shell currently hosting the objects in this adapter.
	 * <p>
	 * <i>Getter method. Callable by subclasses. Do not override.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
    public Shell getShell()
	{
		if (shell == null || shell.isDisposed() || !shell.isVisible() || !shell.isEnabled())
		{
			// get a new shell
			// FIXME it looks like actions could be contributed into a wrong shell with this.
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
	 * Return the current viewer, as set via setViewer or its deduced from the
	 * setInput input object if set. May be null so test it.
	 * <p>
	 * <i>Getter method. Callable by subclasses. Do not override.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	public Viewer getViewer()
	{
		if (viewer == null)
		{
	      ISystemViewInputProvider ip = getInput();
	      if (ip != null)
	      {
	        return (Viewer)ip.getViewer();
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
	 * Return the current viewer as an ISystemTree if it is one, or null
	 * otherwise.
	 * <p>
	 * <i>Getter method. Callable by subclasses. Do not override.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
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
	 * Get the input object used to populate the viewer with the roots. May be
	 * used by an adapter to retrieve context-sensitive information.
	 * <p>
	 * <i>Getter method. Callable by subclasses. Do not override.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
    public ISystemViewInputProvider getInput()
    {
    	return input;
    }

	/**
	 * Returns the subsystem that contains this object. By default, if the given
	 * element is an instance of {@link
	 * org.eclipse.rse.core.subsystems.AbstractResource AbstractResource}, it
	 * calls getSubSystem on it, else returns null.
	 * <p>
	 * <i><b>Overridable</b> by subclasses. You should override if not using
	 * AbstractResource.</i> <br>
	 */
	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof AbstractResource)
		  return ((AbstractResource)element).getSubSystem();
		else if (element instanceof IContextObject)
			return ((IContextObject)element).getSubSystem();
		else
		  return null;
	}

	/**
	 * Returns any framework-supplied remote object actions that should be contributed to the popup menu
	 * for the given selection list. This does nothing if this adapter does not implement ISystemViewRemoteElementAdapter,
	 * else it potentially adds menu items for "User Actions" and Compile", for example. It queries the subsystem
	 * factory of the selected objects to determine if these actions are appropriate to add.
	 * <p>
     * <i>Called by SystemView viewer. No need to override or call.</i><br>
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
				ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssf.getAdapter(ISubSystemConfigurationAdapter.class);
				adapter.addCommonRemoteActions(ssf, menu, selection, shell, menuGroup, ss);
			}
		}

	}

	/**
     * Add or remove custom actions dynamically to a context menu.
     *
     * This method is called by the system viewers. Extenders may
     * override this method in order to modify the context menu
     * shown for elements of the type they adapt to.
     * Unlike addCommonRemoteActions(), these contributions are for
     * any artifact in the RSE views and are contributed independently
     * of subsystem factories.
	 *
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell of viewer calling this. Most dialogs require a shell.
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addDynamicPopupMenuActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		// empty by default, extenders may override.
		// these extensions are independent of subsystem factories and are contributed via extension point
	}

	/**
	 * This is your opportunity to add actions to the popup menu for the given
	 * selection.
	 * <p>
	 * To put your action into the given menu, use the menu's {@link
	 * org.eclipse.
	 * rse.ui.SystemMenuManager#add(String,org.eclipse.jface.action.IAction)
	 * add} method. If you don't care where it goes within the popup, just pass
	 * the given <samp>menuGroup</samp> location id, otherwise pass one of the
	 * GROUP_XXX values from {@link
	 * org.eclipse.rse.ui.ISystemContextMenuConstants}. If you pass one that
	 * identifies a predefined cascading menu, such as GROUP_OPENWITH, your
	 * action will magically appear in that cascading menu, even if it was
	 * otherwise empty.
	 * <p>
	 * For the actions themselves, you will probably use one of the base action
	 * classes:
	 * <ul>
	 * <li>{@link org.eclipse.rse.ui.actions.SystemBaseAction SystemBaseAction}
	 * . For a simple action doesn't present any UI.
	 * <li>{@link org.eclipse.rse.ui.actions.SystemBaseDialogAction
	 * SystemBaseDialogAction}. For an action that presents a {@link
	 * org.eclipse.rse.ui.dialogs.SystemPromptDialog dialog}.
	 * <li>{@link org.eclipse.rse.ui.actions.SystemBaseDialogAction
	 * SystemBaseWizardAction}. For an action that presents a {@link
	 * org.eclipse.rse.ui.wizards.AbstractSystemWizard wizard}.
	 * <li>{@link org.eclipse.rse.ui.actions.SystemBaseSubMenuAction
	 * SystemBaseSubMenuAction} . For an action that cascades into a submenu
	 * with other actions.
	 * </ul>
	 *
	 * @param menu the popup menu you can contribute to
	 * @param selection the current selection in the calling tree or table view
	 * @param parent the shell of the calling tree or table view
	 * @param menuGroup the default menu group to place actions into if you
	 * 		don't care where they. Pass this to the SystemMenuManager {@link
	 * 		org.eclipse.rse.ui.SystemMenuManager#add(String,org.eclipse.jface.
	 * 		action.IAction) add} method.
	 *
	 * @see org.eclipse.rse.ui.view.ISystemViewElementAdapter#addActions(
	 * 	SystemMenuManager, IStructuredSelection, Shell, String)
	 */
	public abstract void addActions(SystemMenuManager menu,IStructuredSelection selection,Shell parent,String menuGroup);

	/**
	 * {@inheritDoc} <i><b>Abstract</b>. Must be overridden by subclasses.</i>
	 * <br>
	 */
	public abstract ImageDescriptor getImageDescriptor(Object element);

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
	 * {@inheritDoc} By default, returns <samp>getText(element);</samp>, but
	 * child classes can override if display name doesn't equal real name.
	 * <p>
	 * Called by common rename and delete actions, and used to populate property
	 * sheet.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but rarely needs to be.</i> <br>
	 *
	 * @see #getText(Object)
	 * @see #getAbsoluteName(Object)
	 */
	public String getName(Object element)
	{
		return getText(element);
	}

	/**
	 * {inheritDoc} We map to <samp>getText(element)</samp>.
	 * <p>
	 * <i>Internal use. Can be safely ignored.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	public String getLabel(Object element)
	{
		if (element instanceof IContextObject)
		{
			element = ((IContextObject)element).getModelObject();
		}
		return getText(element);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <i><b>Abstract</b>. Must be overridden by subclasses.</i> <br>
	 */
	public abstract String getType(Object element);

	/**
	 * {@inheritDoc} The default is:
	 * <pre>
	 * getType(): getName()
	 * </pre>
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but rarely needs to be.</i> <br>
	 */
	public String getStatusLineText(Object element)
	{
		return getType(element) + ": " + getName(element); //$NON-NLS-1$
	}

	/**
	 * {inheritDoc} By default, this method returns true - override this method
	 * to customize the behavior.
	 *
	 * @param element the element to check
	 * @return true if the element exists
	 * @since 3.0
	 */
	public boolean exists(Object element)
	{
		return true;
	}

	/**
	 * {@inheritDoc} <i><b>Abstract</b>. Must be overridden by subclasses.</i>
	 * <br>
	 */
	public abstract Object getParent(Object element);

	/**
	 * {@inheritDoc} <i><b>Abstract</b>. Must be overridden by subclasses.</i>
	 * <br>
	 *
	 * @param element the element to check
	 * @return <code>true</code> if this element can have children.
	 */
	public abstract boolean hasChildren(IAdaptable element);

	/**
	 * {@inheritDoc} Override this to provide context-specific support.
	 *
	 * @param element the context object
	 * @return whether the context has children
	 */
	public boolean hasChildren(IContextObject element)
	{
		return hasChildren(element.getModelObject());
	}

	/**
	 * {@inheritDoc} Rather than overriding this, adapter implementors should
	 * override the getChildren() methods that take a progress monitor.
	 */
	public final Object[] getChildren(Object object)
	{
		return getChildren((IAdaptable)object, new NullProgressMonitor());
	}

	/**
	 * {@inheritDoc} This should be overridden by subclasses in order to provide
	 * deferred query support via the Eclipse Jobs mechanism. Return the
	 * children of this object. Return null if children not supported.
	 *
	 * @param element the model object to get children from
	 * @param monitor the progress monitor
	 * @return the children of element
	 */
	public abstract Object[] getChildren(IAdaptable element, IProgressMonitor monitor);

	/**
	 * {@inheritDoc} This should be overridden by subclasses in order to provide
	 * deferred query support via the Eclipse Jobs mechanism, if your adapter
	 * supports context objects. If not, this will fall back to the model object
	 * version of the method.
	 *
	 * The context object is passed in in place of the model object. By default,
	 * we just fall back to the original mechanism Return the children of this
	 * object. Return null if children not supported.
	 *
	 * @param element the context object that wrappers a model object, it's
	 * 		subsystem and filter reference
	 * @param monitor the progress monitor
	 * @return the children of the model object within the context object that
	 * 	matches the containing filter reference criteria.
	 */
	public Object[] getChildren(IContextObject element, IProgressMonitor monitor)
	{
		return getChildren(element.getModelObject(), monitor);
	}

	/**
	 * Return the children of this object, using the given Expand-To filter. By
	 * default, this calls getChildren(element). Override only if you support
	 * Expand-To menu actions.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but rarely needs to be.</i> <br>
	 */
    public Object[] getChildrenUsingExpandToFilter(Object element, String expandToFilter)
    {
    	return getChildren(element);
    }

	/**
	 * Return the default descriptors for all system elements. <i>Callable by
	 * subclasses.</i><br>
	 */
	protected static IPropertyDescriptor[] getDefaultDescriptors()
	{
		if (propertyDescriptorArray == null)
		{
		  propertyDescriptorArray = new PropertyDescriptor[3];
	      // The following determine what properties will be displayed in the PropertySheet
	      // resource type
	      int idx = 0;
	      propertyDescriptorArray[idx++] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_TYPE, SystemPropertyResources.RESID_PROPERTY_TYPE_LABEL, SystemPropertyResources.RESID_PROPERTY_TYPE_TOOLTIP);
	      // resource name
	      propertyDescriptorArray[idx++] = createSimplePropertyDescriptor(IBasicPropertyConstants.P_TEXT, SystemPropertyResources.RESID_PROPERTY_NAME_LABEL, SystemPropertyResources.RESID_PROPERTY_NAME_TOOLTIP);
	      // number of children in tree currently
	      propertyDescriptorArray[idx++] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_NBRCHILDREN, SystemViewResources.RESID_PROPERTY_NBRCHILDREN_LABEL, SystemViewResources.RESID_PROPERTY_NBRCHILDREN_TOOLTIP);

		}
		//System.out.println("In getDefaultDescriptors() in AbstractSystemViewAdapter");
		return propertyDescriptorArray;
	}

	/**
	 * Create and return a simple string read-only property descriptor.
	 * <i>Callable by subclasses.</i><br>
	 *
	 * @param propertyKey Key for this property, sent back in getPropertyValue.
	 * @param label A user-readable translated label for the Property.
	 * @param description A description for the Property (to be displayed as a
	 * 		tooltip).
	 */
	protected static PropertyDescriptor createSimplePropertyDescriptor(String propertyKey, String label, String description)
	{
	    PropertyDescriptor pd = new PropertyDescriptor(propertyKey, label);
	    pd.setDescription(description);
	    return pd;
	}

	/**
	 * Returns a value for this object that can be edited in a property sheet.
	 * <i>Needed by framework for property sheet. No need to call or
	 * override.</i><br>
	 *
	 * @return a value that can be edited.
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	public Object getEditableValue()
	{
		return this;
	}

	/**
	 * Returns the property descriptors defining what properties are seen in the
	 * property sheet. By default returns descriptors for name, type and
	 * number-of-children only plus whatever is returned from
	 * internalGetPropertyDescriptors().
	 * <p>
	 * <i>Implemented. Do not override typically. See {@link
	 * #internalGetPropertyDescriptors()}.</i> <br>
	 *
	 * @return an array containing all descriptors.
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
	 * Implement this to return the property descriptors for the properties in
	 * the property sheet. This is beyond the Name, Type and NbrOfChildren
	 * properties which already implemented and done for you.
	 * <p>
	 * Override if want to include more properties in the property sheet,
	 * </p>
	 * <p>
	 * If you override this for read-only properties, you must also override:
	 * </p>
	 * <ul>
	 * <li>{@link #getPropertyValue(Object)}
	 * </ul>
	 * <p>
	 * If you override this for editable properties, you must also override:
	 * </p>
	 * <ul>
	 * <li>{@link #isPropertySet(Object)}
	 * <li>{@link #resetPropertyValue(Object)}
	 * <li>{@link #setPropertyValue(Object,Object)}
	 * </ul>
	 *
	 * @return an array containing all descriptors to be added to the default
	 * 	set of descriptors, or null if no additional properties desired.
	 * @see #createSimplePropertyDescriptor(String, String, String)
	 */
	protected abstract IPropertyDescriptor[] internalGetPropertyDescriptors();

	/**
	 * Returns the list of property descriptors that are unique for this
	 * particular adapter - that is the difference between the default property
	 * descriptors and the total list of property descriptors.
	 * <p>
	 * If internalGetPropertyDescriptors() returns non-null, then returns that,
	 * else computes the difference. This is called by the table views like
	 * {@link org.eclipse.rse.ui.view.SystemTableView}.
	 * </p>
	 * <i>Callable by subclasses. Do not override.</i><br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
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
	 * Similar to getPropertyValue(Object key) but takes an argument for
	 * determining whether to return a raw value or formatted value. <b> By
	 * default, simply calls getPropertyValue(key).
	 * <p>
	 * This is called by the table views in order to get values that can be
	 * sorted when the user clicks on the column heading. To support this for a
	 * numeric property say, return a Long/Integer object if false, versus
	 * returning string.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i> <br>
	 *
	 * @param key the name or key of the property as named by its property
	 * 		descriptor
	 * @param formatted indication of whether to return the value in formatted
	 * 		or raw form
	 * @return the current value of the given property
	 */
	public Object getPropertyValue(Object key, boolean formatted)
	{
		return getPropertyValue(key);
	}

	/**
	 * Returns the current value for the named property.<br>
	 * By default handles ISystemPropertyConstants.P_TEXT, P_TYPE and
	 * P_NBRCHILDREN only, then defers to {@link
	 * #internalGetPropertyValue(Object)} for subclasses. <br>
	 * <b>Note</b>: you will need to reference <code>propertySourceInput</code>,
	 * which is the currently selected object. Just case it to what you expect
	 * the selected object's type to be.
	 * <p>
	 * <i>Implemented. Do not override typically. See {@link
	 * #internalGetPropertyValue(Object)}.</i> <br>
	 *
	 * @param key the name of the property as named by its property descriptor
	 * @return the current value of the property
	 */
	public Object getPropertyValue(Object key)
	{
		String name = (String)key;
		if (name.equals(IBasicPropertyConstants.P_TEXT))
		  	//return getText(propertySourceInput);
		  	return getName(propertySourceInput);
		else if (name.equals(ISystemPropertyConstants.P_TYPE))
		  	return getType(propertySourceInput);
		else if (name.equals(ISystemPropertyConstants.P_NBRCHILDREN))
		{
			ISystemTree tree = getSystemTree();
			if (tree != null)
			  	return Integer.toString(tree.getChildCount(propertySourceInput));
			else
			{
			  	if ((viewer != null) && (viewer instanceof TreeViewer))
			    	return Integer.toString(getChildCount((TreeViewer)viewer, propertySourceInput));
			  	else
			    	return "0"; //$NON-NLS-1$
			}
		}
		else
 		  return internalGetPropertyValue(key);
	}

	/**
	 * Implement this to return the property descriptors for the properties in
	 * the property sheet. This is beyond the Name, Type and NbrOfChildren
	 * properties which already implemented and done for you.
	 *
	 * @param key the name of the property as named by its property descriptor
	 * @return the current value of the property or null if not a known
	 * 	property.
	 */
	protected abstract Object internalGetPropertyValue(Object key);


	/**
	 * Return the number of immediate children in the tree, for the given tree node
	 * @deprecated this should be done in the SystemView only
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

    /** @deprecated this should be done in the SystemView only */
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

    /** @deprecated this should be done in the SystemView only */
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

    /** @deprecated this should be done in the SystemView only */
    private Item[] getChildren(Widget o)
    {
    	if (o instanceof TreeItem)
    		return ((TreeItem) o).getItems();
    	if (o instanceof Tree)
    		return ((Tree) o).getItems();
    	return null;
    }

	/**
	 * Returns whether the property value has changed from the default. Only
	 * applicable for editable properties.
	 * <p>
	 * <i><b>Overridable</b> by subclasses. Must be overridden only if editable
	 * properties are supported.</i><br>
	 * <br>
	 * RETURNS FALSE BY DEFAULT.
	 *
	 * @return <code>true</code> if the value of the specified property has
	 * 	changed from its original default value; <code>false</code> otherwise.
	 */
	public boolean isPropertySet(Object key)
	{
		return false;
	}

	/**
	 * Resets the specified property's value to its default value. Called on
	 * editable property when user presses reset button in property sheet
	 * viewer.
	 * <p>
	 * <i><b>Overridable</b> by subclasses. Must be overridden only if editable
	 * properties are supported.</i><br>
	 * DOES NOTHING BY DEFAULT.
	 *
	 * @param key the key identifying property to reset
	 */
	public void resetPropertyValue(Object key)
	{
	}

	/**
	 * Sets the named property to the given value. Called after an editable
	 * property is changed by the user.
	 * <p>
	 * <i><b>Overridable</b> by subclasses. Must be overridden only if editable
	 * properties are supported.</i><br>
	 * DOES NOTHING BY DEFAULT.
	 *
	 * @param key the key identifying property to reset
	 * @param value the new value for the property
	 */
	public void setPropertyValue(Object key, Object value)
	{
	}

	/**
	 * Set input object for property source queries. This <b>must</b> be called
	 * by your XXXAdaptorFactory before returning this adapter object.
	 * <p>
	 * <i>Called from adapter factories. Do not override.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	public void setPropertySourceInput(Object propertySourceInput)
	{
		this.propertySourceInput = propertySourceInput;
	}

   	/**
	 * User has double clicked on an object. If you want to do something
	 * special, do it and return true. Otherwise return false to have the viewer
	 * do the default behaviour.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i> <br>
	 */
    public boolean handleDoubleClick(Object element)
    {
    	return false;
    }

	// ------------------------------------------
	// METHODS TO SUPPORT GLOBAL DELETE ACTION...
	// ------------------------------------------

	/**
	 * Return true if we should show the delete action in the popup for the
	 * given element. If true, then canDelete will be called to decide whether
	 * to enable delete or not.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * By default, returns true.
	 *
	 * @see #canDelete(Object)
	 */
	public boolean showDelete(Object element)
	{
		return true;
	}

	/**
	 * Return true if this object is deletable by the user. If so, when
	 * selected, the Edit->Delete menu item will be enabled.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * By default, returns false. Override if your object is deletable.
	 *
	 * @see #showDelete(Object)
	 */
	public boolean canDelete(Object element)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default does nothing. Override if your object is deletable. Return
	 * true if this was successful. Return false if it failed and you issued a
	 * message. Throw an exception if it failed and you want to use the generic
	 * message. In case of cancellation, either return <code>false</code> or
	 * throw a {@link SystemOperationCancelledException}.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is.</i> <br>
	 *
	 * @see #showDelete(Object)
	 * @see #canDelete(Object)
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default just calls the doDelete method for each item in the
	 * resourceSet. Override if you wish to perform some sort of optimization
	 * for the batch delete. Return true if this was successful. Return false if
	 * ANY delete operation failed and a message was issued. Throw an exception
	 * if ANY failed and you want to use the generic message.
	 * <p>
	 * In case of cancellation, either return <code>false</code> or throw a
	 * {@link SystemOperationCancelledException}.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is.</i> <br>
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

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON RENAME ACTION...
	// ------------------------------------------

	/**
	 * Return true if we should show the rename action in the popup for the
	 * given element. If true, then canRename will be called to decide whether
	 * to enable rename or not.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * By default, returns true.
	 *
	 * @return true if we should show the rename action in the popup for the
	 * 	given element.
	 * @see #canRename(Object)
	 * @see #doRename(Shell,Object,String, IProgressMonitor)
	 */
	public boolean showRename(Object element)
	{
		return true;
	}

	/**
	 * Return true if this object is renameable by the user. If so, when
	 * selected, the Rename popup menu item will be enabled.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * By default, returns false. Override if your object can be renamed.
	 *
	 * @return true if this object is renameable by the user
	 * @see #showRename(Object)
	 * @see #doRename(Shell,Object,String,IProgressMonitor)
	 * @see #getNameValidator(Object)
	 * @see #getCanonicalNewName(Object,String)
	 * @see #namesAreEqual(Object,String)
	 */
	public boolean canRename(Object element)
	{
		return false;
	}

	/**
	 * Perform the rename action. By default does nothing.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Override if your object can be renamed. Return true if this was
	 * successful. Return false if it failed and you issued a message. Throw an
	 * exception if it failed and you want to use the generic message.
	 *
	 * @return true if the rename was successful
	 * @see #showRename(Object)
	 * @see #canRename(Object)
	 * @since 3.0
	 */
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor) throws Exception
	{
		//org.eclipse.rse.core.ui.SystemMessage.displayErrorMessage("INSIDE DORENAME");
		return false;
	}

	/**
	 * Return a validator for verifying the new name is correct. If you return
	 * null, no error checking is done on the new name in the common rename
	 * dialog!!
	 * <p>
	 * Used in the common rename dialogs, and only if you return true to {@link
	 * #canRename(Object)}.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is if canRename is.</i>
	 * <br>
	 * Suggest you use at least UniqueStringValidator or a subclass to ensure
	 * new name is at least unique.
	 *
	 * @see #canRename(Object)
	 */
    public ISystemValidator getNameValidator(Object element)
    {
    	return null;
    }

    /**
	 * Form and return a new canonical (unique) name for this object, given a
	 * candidate for the new name. This is called by the generic multi-rename
	 * dialog to test that all new names are unique. To do this right, sometimes
	 * more than the raw name itself is required to do uniqueness checking.
	 * <p>
	 * For example, two connections or filter pools can have the same name if
	 * they are in different profiles. Two iSeries QSYS objects can have the
	 * same name if their object types are different.
	 * <p>
	 * Used in the common rename dialogs, and only if you return true to {@link
	 * #canRename(Object)}.
	 * <p>
	 * This method returns a name that can be used for uniqueness checking
	 * because it is qualified sufficiently to make it unique.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is if canRename is.</i>
	 * <br>
	 * By default, this simply returns the given name. It is overridden by child
	 * classes when appropriate.
	 *
	 * @see #canRename(Object)
	 */
    public String getCanonicalNewName(Object element, String newName)
    {
    	// this is all for defect 42145. Phil
    	return newName;
    }

	/**
	 * Compare the name of the given element to the given new name to decide if
	 * they are equal. Allows adapters to consider case and quotes as
	 * appropriate.
	 * <p>
	 * Used in the common rename dialogs, and only if you return true to {@link
	 * #canRename(Object)}.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * By default does an equalsIgnoreCase comparison
	 *
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
	 * Return true if we should show the refresh action in the popup for the
	 * given element. Note the actual work to do the refresh is handled for you.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Default is true.
	 */
	public boolean showRefresh(Object element)
	{
		return true;
	}

	// ----------------------------------------------
	// METHODS TO SUPPORT COMMON PROPERTIES ACTION...
	// ----------------------------------------------
	/**
	 * Return true if we should show the properties action in the popup for the
	 * given element. Note the actual work to show the properties dialog is
	 * handled for you.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is.</i><br>
	 * Default is true.
	 */
	public boolean showProperties(Object element)
	{
		return true;
	}

	// ------------------------------------------------------------
	// METHODS TO SUPPORT COMMON OPEN-IN-NEW-PERSPECTIVE ACTIONS...
	// ------------------------------------------------------------
	/**
	 * Return true if we should show the <b>Go Into;</b> and <b>Open In New
	 * Window</b> and <b>Go To</b> actions in the popup for the given element.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is NOT.</i><br>
	 * Only applicable for non-remote resources. Remote always show <b>Go To</b>
	 * only.
	 */
	public boolean showOpenViewActions(Object element)
	{
		if (element instanceof IAdaptable)
			return hasChildren((IAdaptable)element);
		return false;
	}

	/**
	 * Return true if we should show the generic show in table action in the
	 * popup for the given element.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is NOT.</i> <br>
	 */
	public boolean showGenericShowInTableAction(Object element)
	{
		return true;
	}

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON DRAG AND DROP FUNCTION...
	// ------------------------------------------
	/**
	 * Return true if this object can be copied to another location.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i><br>
	 * By default, we return false. Extenders may decide whether or not certain
	 * objects can be dragged with this method.
	 *
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
	 * Return true if this object can be copied to another location.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i><br>
	 * By default, we return false. Extenders may decide whether or not certain
	 * objects can be dragged with this method. Return true if these objects can
	 * be copied to another location via drag and drop, or clipboard copy.
	 */
	public boolean canDrag(SystemRemoteResourceSet elements)
	{
		return false;
	}

	/**
	 * Perform the drag on the given object.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i><br>
	 * By default this does nothing and returns nothing. Extenders supporting
	 * DnD are expected to implement this method to perform a copy to a
	 * temporary object, the return value.
	 *
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
	 * Return true if another object can be copied into this object.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i><br>
	 * By default we return false. Extenders may decide whether or not certain
	 * objects can accept other objects with this method.
	 *
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
	 * Perform the drag on the given objects.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i><br>
	 * This default implementation simply iterates through the set. For optimal
	 * performance, this should be overridden.
	 *
	 * @param set the set of objects to copy
	 * @param monitor the progress monitor
	 * @return the set of objects as a result of the drag
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
	 * Perform drop from the "fromSet" of objects to the "to" object.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i> <br>
	 *
	 * @param fromSet the source objects for the drop
	 * @param to the target object for the drop
	 * @param sameSystemType indication of whether the source and target reside
	 * 		of the same type of system
	 * @param sameSystem indication of whether the source and target are on the
	 * 		same system
	 * @param srcType the type of objects to be dropped
	 * @param monitor the progress monitor
	 *
	 * @return the set of new objects created from the drop
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
	 * Perform drop from the "from" object to the "to" object.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i><br>
	 * By default this does nothing and we return false. Extenders supporting
	 * DnD are expected to implement this method to perform a "paste" into an
	 * object.
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
		// for backward compatibility
		return doDrop(from, to, sameSystemType, sameSystem, monitor);
	}

	/**
	 * Perform drop from the "from" object to the "to" object.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and is only if drag and drop
	 * supported.</i><br>
	 * By default this does nothing and we return false. Extenders supporting
	 * DnD are expected to implement this method to perform a "paste" into an
	 * object.
	 *
	 * @return the new object that was copied
	 *
	 * @see #canDrag(Object)
	 * @see #doDrag(Object,boolean,IProgressMonitor)
	 * @see #canDrop(Object)
	 * @see #validateDrop(Object,Object,boolean)
	 *
	 * @deprecated use doDrop(Object from, Object to, boolean sameSystemType,
	 * 	boolean sameSystem, int srcType, IProgressMonitor monitor) instead
	 */
	public Object doDrop(Object from, Object to, boolean sameSystemType, boolean sameSystem,  IProgressMonitor monitor)
	{
		return null;
	}

	/**
	 * Return true if it is valid for the src object to be dropped in the
	 * target.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and usually is only if drag and drop
	 * supported.</i><br>
	 * We return false by default.
	 *
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
	 * Return what to save to disk to identify this element in the persisted
	 * list of expanded elements.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * This just defaults to getName, but if that is not sufficient override it
	 * here.
	 */
	public String getMementoHandle(Object element)
	{
		if (this instanceof ISystemRemoteElementAdapter)
		  return ((ISystemRemoteElementAdapter)this).getAbsoluteName(element);
		else
		  return getName(element);
	}

	/**
	 * Return what to save to disk to identify this element when it is the input
	 * object to a secondary Remote System Explorer perspective.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Defaults to getMementoHandle(element).
	 */
	public String getInputMementoHandle(Object element)
	{
		return getMementoHandle(element);
	}

	/**
	 * Return a short string to uniquely identify the type of resource. Eg
	 * "conn" for connection.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * This just defaults to getType, but if that is not sufficient override it
	 * here, since that is a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		if (this instanceof ISystemRemoteElementAdapter)
		  return ISystemMementoConstants.MEMENTO_KEY_REMOTE;
		else
		  return getType(element);
	}

	/**
	 * Sometimes we don't want to remember an element's expansion state, such as
	 * for temporarily inserted messages. In these cases return false from this
	 * method. The default is true.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i> <br>
	 */
    public boolean saveExpansionState(Object element)
    {
    	return true;
    }

	/**
	 * Return true if this object is a "prompting" object that prompts the user
	 * when expanded. For such objects, we do not try to save and restore their
	 * expansion state on F5 or between sessions.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Default is false unless element implements ISystemPromptable object.
	 * Override as appropriate.
	 */
    public boolean isPromptable(Object element)
    {
    	return (element instanceof ISystemPromptableObject);
    }

	/**
	 * Return true if this object is remote. In this case, the default is true.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i> <br>
	 */
    public boolean isRemote(Object element)
    {
    	return true;
    }

	/**
	 * Selection has changed in the Remote Systems view. Empty by default, but
	 * override if you need to track selection changed. For example, this is
	 * used to drive table views that respond to selection.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i> <br>
	 *
	 * @param element - first selected object
	 */
    public void selectionChanged(Object element)    // d40615
    {
    }

	/**
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
	 *  <li>name="subsystemConfigurationId". The given value is a subsystem factory Id, and this returns true if this object's
	 *       subsystem is from that subsystem factory. For connections, returns false.
	 *       You can specify multiple values by comma-separating them, and this returns if there is a match on any them.
	 *  <li>name="subsystemConfigurationCategory". The given value is a subsystem category, and this returns true if this object's
	 *       subsystem is from a subsystem factory of that category. For connections, returns false.
	 *       You can specify multiple values by comma-separating them, and this returns if there is a match on any them.
	 * </ol>
	 * <p>
     * <i><b>Overridable</b> by subclasses, typically if additional properties are supported.</i><br>
	 * If desired, override, and call super(), to support additional filter criteria for &lt;filter&gt;, &lt;enablement&gt; and &lt;visibility&gt;.
	 *
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value)
	{
		//System.out.println("Inside testAttribute: name = " + name + ", value = " + value);
		if (name.equalsIgnoreCase("name")) //$NON-NLS-1$
		{
			if (value.endsWith("*"))  //$NON-NLS-1$
			{
				// we have a wild card test, and * is the last character in the value
				if (getName(target).startsWith(value.substring(0, value.length() - 1)))
					return true;
			}
			else
				return value.equals(getName(target));
		}
		else if (name.equalsIgnoreCase("type")) //$NON-NLS-1$
		  return value.equals(getType(target));
		else if (name.equalsIgnoreCase("hasChildren")) //$NON-NLS-1$
		{
			return hasChildren((IAdaptable)target) ? value.equals("true") : value.equals("false"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if (name.equalsIgnoreCase("connected")) //$NON-NLS-1$
		{
			ISubSystem ss = getSubSystem(target);
			if (ss != null)
			  return ss.isConnected() ? value.equals("true") : value.equals("false"); //$NON-NLS-1$ //$NON-NLS-2$
			else
			  return false;
		}
		else if (name.equalsIgnoreCase("offline")) //$NON-NLS-1$
		{
			ISubSystem ss = getSubSystem(target);
			if (ss != null)
			  return ss.isOffline() ? value.equals("true") : value.equals("false"); //$NON-NLS-1$ //$NON-NLS-2$
			else
			  return false;
		}
		else if (name.equalsIgnoreCase("systemType")) //$NON-NLS-1$
		{
			ISubSystem ss = getSubSystem(target);
			String[] values = tokenize(value);
			if (ss == null)
			{
				if (!(target instanceof IHost))
			      return false;
			    String connSysType = ((IHost)target).getSystemType().getName();
			    for (int idx=0; idx<values.length; idx++)
			    {
			    	if (connSysType.equals(values[idx]))
			    	   return true;
			    }
			    return false;
			}
			for (int idx=0; idx<values.length; idx++)
			{
			  if (ss.getHost().getSystemType().getName().equals(values[idx]))
			    return true;
			}
			return false;
		}
		else if (name.equalsIgnoreCase("systemTypeId")) //$NON-NLS-1$
		{
			ISubSystem ss = getSubSystem(target);
			String[] values = tokenize(value);
			if (ss == null)
			{
				if (!(target instanceof IHost))
			      return false;
			    String connSysTypeId = ((IHost)target).getSystemType().getId();
			    for (int idx=0; idx<values.length; idx++)
			    {
			    	if (connSysTypeId.equals(values[idx]))
			    	   return true;
			    }
			    return false;
			}
			for (int idx=0; idx<values.length; idx++)
			{
			  if (ss.getHost().getSystemType().getId().equals(values[idx]))
			    return true;
			}
			return false;
		}
		else if (name.equalsIgnoreCase("subsystemConfigurationId")) //$NON-NLS-1$
		{
			ISubSystem ss = getSubSystem(target);
			//System.out.println("ss null? " + (ss == null));
			if (ss == null)
			  return false;
			String[] values = tokenize(value);
			//System.out.println("Nbr of values: " + (values.length));
			//System.out.println("Comparing against: " + (ss.getParentSubSystemConfiguration().getId()));
			boolean ok = false;
			for (int idx=0; !ok && (idx<values.length); idx++)
			{
			  if (ss.getSubSystemConfiguration().getId().equals(values[idx]))
			    ok = true;
			}
			//System.out.println("Returning: " + ok);
			return ok;
		}
		else if (name.equalsIgnoreCase("subsystemConfigurationCategory")) //$NON-NLS-1$
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
		else if (name.equalsIgnoreCase("isRemote")) //$NON-NLS-1$
		{
			return isRemote(target) ? value.equals("true") : value.equals("false"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Give the ISV's as the element owners/contributors the chance to extend the standard RSE action
		// filters for their specific needs. We do this by trying to determine the system type from the
		// target object and try to adapt the system type to an IActionFilter.
		//
		// Note: Everything we do here is performance critical to the menu to show up. Therefore
		//       we cache as much as possible here. The cache is static to all AbstractSystemViewAdapter
		//       instances throughout the whole hierarchy.
		IHost conn = null;
		if (target instanceof IHost) {
			conn = (IHost)target;
		} else if (target instanceof ISubSystem) {
			conn = ((ISubSystem)target).getHost();
		} else if (target instanceof ISystemFilterPoolReference) {
			ISystemFilterPoolReference modelObject = (ISystemFilterPoolReference)target;
			if (modelObject.getProvider() != null) conn = ((ISubSystem)modelObject.getProvider()).getHost();
		} else if (target instanceof ISystemFilterReference) {
			ISystemFilterReference modelObject = (ISystemFilterReference)target;
			if (modelObject.getProvider() != null) conn = ((ISubSystem)modelObject.getProvider()).getHost();
		} else if (target instanceof ISystemFilterStringReference) {
			ISystemFilterStringReference modelObject = (ISystemFilterStringReference)target;
			if (modelObject.getProvider() != null) conn = ((ISubSystem)modelObject.getProvider()).getHost();
		}

		if (conn != null) {
			IRSESystemType systemType = conn.getSystemType();
			if (systemType != null) {
				IActionFilter actionFilter = (IActionFilter)ACTION_FILTER_CACHE.get(systemType);
				if (actionFilter == null) {
					Object adapter = systemType.getAdapter(IActionFilter.class);
					if (adapter instanceof IActionFilter && !adapter.equals(this)) {
						// put the association in the cache
						ACTION_FILTER_CACHE.put(systemType, adapter);
						actionFilter = (IActionFilter)adapter;
					} else if (!(adapter instanceof IActionFilter)) {
						// put the association in the cache
						ACTION_FILTER_CACHE.put(systemType, new NULL_ACTION_FILTER());
					}
				}
				if (actionFilter instanceof NULL_ACTION_FILTER) actionFilter = null;
				if (actionFilter != null) return actionFilter.testAttribute(target, name, value);
			}
		}

		return false;
	}

	/**
	 * Break given comma-delimited string into tokens
	 */
	private String[] tokenize(String input)
	{
          	StringTokenizer tokens = new StringTokenizer(input,";"); //$NON-NLS-1$
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
	 * From {@link
	 * org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubSubType
	 * (Object)}. Pre-supplied for convenience for subclasses that want to
	 * implement this interface for remote object adapters.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Returns null. Override if you want to supply a sub-sub-type for filtering
	 * in the popupMenus extension point.
	 */
	public String getRemoteSubSubType(Object element)
	{
		return null; // Extremely fine grained. We don't use it.
	}

	/**
	 * From {@link
	 * org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubSubType
	 * (Object)}. Pre-supplied for convenience for subclasses that want to
	 * implement this interface for remote object adapters.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, but usually is not.</i><br>
	 * Returns null. Override if the remote resource is compilable.
	 */
	public String getRemoteSourceType(Object element)
	{
		return null;
	}

	/**
	 * Return the remote edit wrapper for this object.
	 * <p>
	 * <i><b>Overridable</b> by subclasses, and must be for editable
	 * objects.</i> <br>
	 *
	 * @param object the object to edit
	 * @return the editor wrapper for this object
	 */
	public ISystemEditableRemoteObject getEditableRemoteObject(Object object)
	{
		return null;
	}

	/**
	 * Indicates whether the specified object can be edited or not.
	 * <i><b>Overridable</b> by subclasses, and must be for editable
	 * objects.</i><br>
	 *
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
	 * Returns the implementation of ISystemViewElement for the given object.
	 * Returns null if the adapter is not defined or the object is not
	 * adaptable.
	 * <p>
	 * <i>Callable by subclasses.</i><br>
	 * Just a convenient shortcut to {@link
	 * org.eclipse.rse.ui.view.SystemAdapterHelpers#getViewAdapter(Object,
	 * Viewer)} <p/> Should we allow clients to override this in order to
	 * provide an optimized implementation for their models? But it's being
	 * called by subclasses only anyways...
	 *
	 * @deprecated use SystemAdapterHelpers.getViewAdapter(o, getViewer())
	 * 	instead
	 */
    protected ISystemViewElementAdapter getSystemViewElementAdapter(Object o)
    {
        return SystemAdapterHelpers.getViewAdapter(o, getViewer());
        /*
    	ISystemViewElementAdapter adapter = null;
    	if (!(o instanceof IAdaptable))
          adapter = (ISystemViewElementAdapter)Platform.getAdapterManager().getAdapter(o,ISystemViewElementAdapter.class);
        else
    	  adapter = (ISystemViewElementAdapter)((IAdaptable)o).getAdapter(ISystemViewElementAdapter.class);
    	if (adapter == null)
    	  RSEUIPlugin.logDebugMessage(this.getClass().getName(), "ADAPTER IS NULL FOR ELEMENT : " + o);
    	else
    	{
    		adapter.setViewer(getViewer()); // added this in V5.0, just in case. Phil
    	}
    	return adapter;
    	*/
    }

    /**
	 * Returns the implementation of ISystemRemoteElement for the given object.
	 * Returns null if this object does not adaptable to this.
	 * <p>
	 * <i>Callable by subclasses.</i><br>
	 * Just a convenient shortcut to {@link
	 * org.eclipse.rse.ui.view.SystemAdapterHelpers#getRemoteAdapter(Object,
	 * Viewer)} <p/> Should we allow clients to override this in order to
	 * provide an optimized implementation for their models? But it's being
	 * called by subclasses only anyways...
	 *
	 * @deprecated use SystemAdapterHelpers.getRemoteAdapter(o, getViewer())
	 * 	instead
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
	 * Substitute all occurrences of a given String with another String.
	 *
	 * <i>Callable by subclasses.</i> This method does message variable
	 * substitution. Using you are replacing &1 (say) with a string.
	 *
	 * @param msg message containing substitution variable. Eg "Connect failed
	 *            with return code &1"
	 * @param subOld substitution variable. Eg "%1"
	 * @param subNew substitution data. Eg "001"
	 * @return message with all occurrences of variable substituted with data.
	 *
	 * @deprecated Clients should use Eclipse {@link org.eclipse.osgi.util.NLS}
	 *             or Java {@link java.text.MessageFormat} or
	 *             {@link java.lang.String#replaceAll(String, String)} to do
	 *             substring replacement and variable substitution.
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
	 * Return the current viewer as an ISystemTree if the viewer is set and it
	 * implements this interface (SystemView does). May be null so test it.
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
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
	 * Return "Yes" translated. <i>Callable by subclasses. Do not override</i>
	 * <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	public String getTranslatedYes()
	{
		if (xlatedYes == null)
		  xlatedYes = SystemResources.TERM_YES;
		return xlatedYes;
	}

	/**
	 * Return "No" translated. <i>Callable by subclasses. Do not override.</i>
	 * <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	protected String getTranslatedNo()
	{
		if (xlatedNo == null)
		  xlatedNo = SystemResources.TERM_NO;
		return xlatedNo;
	}

	/**
	 * Return "True" translated. <i>Callable by subclasses. Do not override.</i>
	 * <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	protected String getTranslatedTrue()
	{
		if (xlatedTrue == null)
		  xlatedTrue = SystemResources.TERM_TRUE;
		return xlatedTrue;
	}

	/**
	 * Return "False" translated. <i>Callable by subclasses. Do not
	 * override.</i><br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	protected String getTranslatedFalse()
	{
		if (xlatedFalse == null)
		  xlatedFalse = SystemResources.TERM_FALSE;
		return xlatedFalse;
	}

	/**
	 * Return "Not applicable" translated. <i>Callable by subclasses. Do not
	 * override.</i><br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	protected String getTranslatedNotApplicable()
	{
		if (xlatedNotApplicable == null)
		  xlatedNotApplicable = SystemPropertyResources.RESID_TERM_NOTAPPLICABLE;
		return xlatedNotApplicable;
	}

	/**
	 * Return "Not available" translated. <i>Callable by subclasses. Do not
	 * override.</i><br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	protected String getTranslatedNotAvailable()
	{
		if (xlatedNotAvailable == null)
		  xlatedNotAvailable = SystemPropertyResources.RESID_TERM_NOTAVAILABLE;
		return xlatedNotAvailable;
	}

	/**
	 * Initialize Message Objects. <i>Internal use. Do not call or override.</i>
	 *
	 * @deprecated Internal use. Do not call this method.
	 */
	protected final void initMsgObjects()
	{
		nullObject     = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_EMPTY),ISystemMessageObject.MSGTYPE_EMPTY, null);
		cancelledObject = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_LIST_CANCELLED),ISystemMessageObject.MSGTYPE_CANCEL, null);
		errorObject    = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED),ISystemMessageObject.MSGTYPE_ERROR, null);
	}

	/**
	 * In getChildren, return <samp>checkForEmptyList(children, parent,
	 * true/false)<.samp> versus your array directly. This method checks for a
	 * null array which is not allowed and replaces it with an empty array. If
	 * true is passed then it returns the "Empty list" message object if the
	 * array is null or empty.
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 *
	 * @param children The list of children.
	 * @param parent The parent for the children.
	 * @param returnNullMsg <code>true</code> if an "Empty List" message should
	 * 		be returned.
	 * @return The list of children, a list with the "Empty List" message object
	 * 	or an empty list.
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
	 */
	protected Object[] checkForEmptyList(Object[] children, Object parent, boolean returnNullMsg) {
		if ((children == null) || (children.length == 0)) {
			if (fPrefStore == null) {
				fPrefStore = RSEUIPlugin.getDefault().getPluginPreferences();
			}
			if (!returnNullMsg
					|| (fPrefStore != null && !fPrefStore
							.getBoolean(ISystemPreferencesConstants.SHOW_EMPTY_LISTS))) {
				return emptyList;
			} else {
				return new Object[] {
					new SystemMessageObject(
						RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_EMPTY),
						ISystemMessageObject.MSGTYPE_EMPTY,
						parent)};
			}
		}
		return children;
	}

	/**
	 * In getChildren, return <samp>checkForNull(children, true/false)</samp>
	 * versus your array directly. This method checks for a null array which is
	 * not allowed and replaces it with an empty array. If true is passed then
	 * it returns the "Empty list" message object if the array is null or empty.
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 *
	 * @deprecated Use {@link #checkForEmptyList(Object[], Object, boolean)}
	 * 	instead.
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
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
	 * Return the "Operation cancelled by user" message as an object array so
	 * can be used to answer getChildren().
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 */
    protected final Object[] getCancelledMessageObject()
    {
		 if (cancelledObject == null)
		   initMsgObjects();
		 msgList[0] = cancelledObject;
		 return msgList;
    }

	/**
	 * Return the "Operation failed" message as an object array so can be used
	 * to answer getChildren().
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 */
    protected final Object[] getFailedMessageObject()
    {
		 if (errorObject == null)
		   initMsgObjects();
		 msgList[0] = errorObject;
		 return msgList;
    }

	/**
	 * Return the "Empty list" message as an object array so can be used to
	 * answer getChildren().
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 *
	 * @deprecated Use {@link #checkForEmptyList(Object[], Object, boolean)}
	 * 	instead.
	 */
    protected final Object[] getEmptyMessageObject()
    {
		 if (nullObject == null)
		   initMsgObjects();
		 msgList[0] = nullObject;
		 return msgList;
    }

    /**
	 * Get the first selected object of the given selection.
	 * <p>
	 * <i>Callable by subclasses. Do not override.</i> <br>
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients.
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

	/* ---------------------------------------------------
	 * these methods are for deferred fetch operations
	 * ---------------------------------------------------
	 */

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewElementAdapter#supportsDeferredQueries(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public boolean supportsDeferredQueries(ISubSystem subSys)
	{
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object o, IElementCollector collector, IProgressMonitor monitor)
	{
		SystemFetchOperation operation = null;
        monitor = Policy.monitorFor(monitor);
        String taskName = SystemViewResources.RESID_FETCHING;
        monitor.beginTask(taskName, 100);
        operation = getSystemFetchOperation(o, collector);
        try
        {
			operation.run(Policy.subMonitorFor(monitor, 100));
        }
        catch (InvocationTargetException e)
        {
        	operation.setException(e);
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
		IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
		IWorkbenchPart currentPart = null;
		if (win != null){
			IWorkbenchPage page = win.getActivePage();
			if (page != null){
				currentPart = page.getActivePart();
			}
		}
	    return new SystemFetchOperation(currentPart, o, this, collector);
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
     */
    public boolean isContainer()
    {
        return true;
    }

    public ISchedulingRule getRule(Object element)
    {
    	if (element instanceof IContextObject)
    	{
    		element = ((IContextObject)element).getModelObject();
    	}
    	IAdaptable location = (IAdaptable)element;
        return new SystemSchedulingRule(location);
    }

}
