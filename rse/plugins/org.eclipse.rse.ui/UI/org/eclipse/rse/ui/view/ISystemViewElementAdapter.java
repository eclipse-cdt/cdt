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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David McKnight   (IBM)        - [208803] add exists() method
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * David Dykstal (IBM) - [234215] improve API documentation for doDelete and doDeleteBatch
 *******************************************************************************/

package org.eclipse.rse.ui.view;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;



/**
 * This is the interface for an adapter on objects in the system viewer.
 * Any input into the system viewer must register an adapter that implements this interface.
 * <p>
 * This interface supports a union of all the methods needed to support a TreeViewer
 *  content provider and label provider. The {@link org.eclipse.rse.internal.ui.view.SystemViewLabelAndContentProvider}
 *  delegates to objects of this interface almost completely. It gets such an
 *  object by calling:</p>
 * <pre><code>
 *  isve = object.getAdapter(ISystemViewElementAdapter.class);
 *  interestingInfo = isve.getXXXX(object);
 * </code></pre>
 * <p>
 * This interface also supports IPropertySource via inheritance, so we can feed the
 * PropertySheet.
 * </p>
 * <p>For remote resource objects, their adapter should also implement
 * {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter}
 * </p>
 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter
 */
public interface ISystemViewElementAdapter extends IPropertySource, ISystemDragDropAdapter, IActionFilter
{
	public static final IAction[] noActions = new IAction[0];

    /**
     * Set the shell to be used by any method that requires it.
     * This is set by the Label and Content providers that retrieve this adapter.
     */
    public void setShell(Shell shell);

    /**
     * Set the viewer that is driving this adapter. Used adapters that implements to access current viewer.
     * This is set by the Label and Content providers that retrieve this adapter.
     */
    public void setViewer(Viewer viewer);

    /**
     * Set the input object used to populate the viewer with the roots.
     * May be used by an adapter to retrieve context-sensitive information.
     * This is set by the Label and Content providers that retrieve this adapter.
     */
    public void setInput(ISystemViewInputProvider input);

    /**
     * Get the shell currently hosting the objects in this adapter, as last set
     *  by the label or content provider that retrieved this adapter.
     */
    public Shell getShell();
	/**
	 * Return the current viewer, as set via setViewer or its deduced from the
	 *  setInput input object if set. May be null so test it.
	 */
	public Viewer getViewer();
    /**
     * Get the input object used to populate the viewer with the roots.
     * May be used by an adapter to retrieve context-sensitive information.
     */
    public ISystemViewInputProvider getInput();


    /**
     * Get the subsystem that corresponds to this object if one exists.
     *
     * @param element The element to be identified. May be of type
     *                {@link IContextObject} (including the context of the element),
     *                {@link String} (giving the absolute path of a remote object),
     *                or the actual remote data element itself.
     */
	public ISubSystem getSubSystem(Object element);

	/**
	 * Wrapper to getPropertyValue(Object key) that takes an argument
	 * for determining whether to return a raw value or formatted value.
	 *
	 */
	public Object getPropertyValue(Object key, boolean formatted);


	/**
	 * Returns the list of property descriptors that are unique for this
	 * particular adapter - that is the difference between the default
	 * property descriptors and the total list of property descriptors.
	 */
	public IPropertyDescriptor[] getUniquePropertyDescriptors();


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
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell parent, String menuGroup);
	/**
	 * Returns an image descriptor for the image. More efficient than getting
	 * the image. Implements {@link IWorkbenchAdapter#getImageDescriptor(Object)
	 * }.
	 *
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element);
	/**
	 * Return the label for this object.
	 */
	public String getText(Object element);
	/**
	 * Return the alternate label for this object.
	 */
	public String getAlternateText(Object element);
	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 */
	public String getName(Object element);
	/**
	 * Return a value for the type property for this object.
	 */
	public String getType(Object element);
	/**
	 * Return the string to display in the status line when the given object is
	 * selected.
	 */
	public String getStatusLineText(Object element);


	/**
	 * Returns whether the specified element is represented as existing.  Note that
	 * it's possible that the represented element will been seen to exist when on
	 * a remote host it may not - that is because this call does not query the host.
	 *
	 * @param element the element to check
	 * @return true if the element exists
	 * @since 3.0
	 */
	public boolean exists(Object element);

	/**
	 * Return the parent of this object. This is required by eclipse UI
	 * adapters, but we try desperately not to use in the RSE. So, you are
	 * probably safe returning null, but if you can return a parent, why not, go
	 * for it.
	 */
	public Object getParent(Object element);



	/**
	 * Return the children of this model object.
	 *
	 * When {@link #supportsDeferredQueries(ISubSystem)} returns false,
	 * this query will be called in the dispatch thread, so the implementation
	 * needs to make sure that SWT thread exceptions are avoided.
	 *
	 * @param element the model object to query
	 * @param monitor the progress monitor
	 * @return the children of element
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor);

	/**
	 * Return the children of this object.
	 *
	 * When a contextObject is passed in instead of an adaptable model
	 * object, the adapter needs handle both the model object as well
	 * as the associated filter.
	 *
	 * When {@link #supportsDeferredQueries(ISubSystem)} returns false,
	 * this query will be called in the dispatch thread, so the implementation
	 * needs to make sure that SWT thread exceptions are avoided.
	 *
	 * @param contextObject a wrapper object that contains the model object plus context information
	 * @param monitor the progress monitor
	 * @return the children of the model object in contextObject that matches the filter in contextObject
	 */
	public Object[] getChildren(IContextObject contextObject, IProgressMonitor monitor);

	/**
	 * Return the children of this object, using the given Expand-To filter
	 */
    public Object[] getChildrenUsingExpandToFilter(Object element, String expandToFilter);

    /**
	 * Return true if this object has children.
	 * <p>
	 * In case this adapter returns true for {@link #supportsDeferredQueries(ISubSystem)},
	 * it is expected that the underlying subsystem caches the hasChildren() attribute
	 * such that it does not necessarily perform a server round trip. In this case, it
	 * has more the semantics of "can have children". In that case,
	 * a deferred {@link #getChildren(IAdaptable, IProgressMonitor)}
	 * call is still allowed to return an empty array indicating no children.
	 * </p>
	 * @param element the element to check
	 * @return <code>true</code> if this element can have children.
	 */
	public boolean hasChildren(IAdaptable element);

	/**
	 * Return true if this object has children.
	 * @see #hasChildren(IAdaptable)
	 * @param element the element to check
	 * @return <code>true</code> if this element can have children.
	 */
	public boolean hasChildren(IContextObject element);

    /**
     * Return true if this object is a "prompting" object that prompts the user when expanded.
     * For such objects, we do not try to save and restore their expansion state on F5 or between
     * sessions
     */
    public boolean isPromptable(Object element);


    /**
     * Return true if this object is a "remote" object.  A remote object is defined as an object
     * that is not part of the base RSE artifacts (hosts, subsystems, filters).  This method is used
     * to decide whether certain actions will be available from views, and how views should handle refreshing
     * such objects.  Remote objects can appear more than once in the same view, while non-remote objects
     * are more confined and can be handled very clearly by the RSE UI framework.
     * @param element the object to check
     * @return true if the object is remote
     */
    public boolean isRemote(Object element);

	/**
	 * Set input object for property source queries. This is called by the
	 * SystemViewAdaptorFactory before returning this adapter object.
	 * Handled automatically if you start with AbstractSystemViewAdaptor.
	 */
	public void setPropertySourceInput(Object propertySourceInput);

    /**
     * User has double clicked on an object. If you want to do something special,
     *  do it and return true. Otherwise return false to have the viewer do the default behaviour.
     */
    public boolean handleDoubleClick(Object element);

	// ------------------------------------------
	// METHODS TO SUPPORT GLOBAL DELETE ACTION...
	// ------------------------------------------
	/**
	 * Return true if we should show the delete action in the popup for the given element.
	 * If true, then canDelete will be called to decide whether to enable delete or not.
	 */
	public boolean showDelete(Object element);
	/**
	 * Return true if this object is deletable by the user. If so, when selected,
	 *  the Edit->Delete menu item will be enabled.
	 */
	public boolean canDelete(Object element);


	/**
	 * Perform the delete action on single item. Implement if the object is
	 * deletable.
	 * <p>
	 * Implementers have two choices of dealing with error conditions: 
	 * handle the error themselves (for example, by showing an error dialog) and returning
	 * <code>false</code>; or by throwing an Exception and having the framework
	 * handle the error.
	 * <p>
	 * If the operation is cancelled, the implementation is free to either
	 * return <code>false</code> or throw a
	 * {@link SystemOperationCancelledException}, since in both cases the
	 * framework will refresh the element to see whether it has
	 * actually been deleted or not, but not show any other message.
	 *
	 * @param shell The shell that can act as a parent for an adapter-issued
	 *            message dialog.
	 * @param element The element that should be deleted.
	 * @param monitor The progress monitor for progress and cancellation. May be
	 *            <code>null</code>.
	 * @return <code>true</code> indicates that the deletion succeeded,
	 *         <code>false</code> indicates that the deletion failed but no user
	 *         feedback is required by the framework because the implementation
	 *         has already handled the error or cancellation.
	 * @throws Exception if the deletion failed and the adapter did not show a
	 *             message dialog.
	 * @see #showDelete(Object)
	 * @see #canDelete(Object)
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception;

	/**
	 * Perform the delete on the given set of items.
	 * <p>
	 * See {@link #doDelete(Shell, Object, IProgressMonitor)} for semantics of
	 * error handling and cancellation.
	 * 
	 * @param shell the shell that can act as a parent for an adapter-issued
	 *            message dialog.
	 * @param resourceSet a list of resources that should be deleted.
	 * @param monitor The progress monitor for progress and cancellation. May be
	 *            <code>null</code>.
	 * @return <code>true</code> if all deletions were successful,
	 *         <code>false</code> if any deletion was not successful but proper
	 *         error reporting has been done by the adapter already. The
	 *         framework will only refresh the items to see which ones have
	 *         actually been deleted.
	 * @throws Exception if any deletion was not successful and the adapter did
	 *             not perform any error reporting. If this is a
	 *             {@link SystemMessageException} then the caller should examine
	 *             the results and fire any necessary events.
	 */
	public boolean doDeleteBatch(Shell shell, List resourceSet, IProgressMonitor monitor) throws Exception;

	// ------------------------------------------
	// METHODS TO SUPPORT COMMON RENAME ACTION...
	// ------------------------------------------
	/**
	 * Return true if we should show the rename action in the popup for the given element.
	 * If true, then canRename will be called to decide whether to enable rename or not.
	 */
	public boolean showRename(Object element);
	/**
	 * Return true if this object is renamable by the user. If so, when selected,
	 *  the Rename popup menu item will be enabled.
	 */
	public boolean canRename(Object element);
	/**
	 * Perform the rename on the given item.
	 * @since 3.0
	 */
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor)
	    throws Exception;



	/**
	 * Return a validator for verifying the new name is correct.
	 * If you return null, no error checking is done on the new name!!
	 * Suggest you use at least UniqueStringValidator or a subclass to ensure
	 *  new name is at least unique.
	 */
    public ISystemValidator getNameValidator(Object element);
    /**
     * Form and return a new canonical (unique) name for this object, given a candidate for the new
     *  name. This is called by the generic multi-rename dialog to test that all new names are unique.
     *  To do this right, sometimes more than the raw name itself is required to do uniqueness checking.
     * <p>
     *  For example, two connections or filter pools can have the same name if they are
     *  in different profiles. Two iSeries QSYS objects can have the same name if their object types
     *  are different.
     * <p>
     * This method returns a name that can be used for uniqueness checking because it is qualified
     *  sufficiently to make it unique.
     */
    public String getCanonicalNewName(Object element, String newName);
    /**
     * Compare the name of the given element to the given new name to decide if they are equal.
     * Allows adapters to consider case and quotes as appropriate.
     */
    public boolean namesAreEqual(Object element, String newName);
	// ------------------------------------------
	// METHODS TO SUPPORT COMMON REFRESH ACTION...
	// ------------------------------------------
	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showRefresh(Object element);

	// ----------------------------------------------
	// METHODS TO SUPPORT COMMON PROPERTIES ACTION...
	// ----------------------------------------------
	/**
	 * Return true if we should show the properties action in the popup for the given element.
	 */
	public boolean showProperties(Object element);

	// ------------------------------------------------------------
	// METHODS TO SUPPORT COMMON OPEN-IN-NEW-PERSPECTIVE ACTIONS...
	// ------------------------------------------------------------
	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showOpenViewActions(Object element);

	/**
	 * Return true if we should show the generic show in table action in the popup for the given element.
	 */
	public boolean showGenericShowInTableAction(Object element);

	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	/**
	 * Return what to save to disk to identify this element in the persisted list of expanded elements.
	 * This just defaults to getName, but if that is not sufficient override it here.
	 */
	public String getMementoHandle(Object element);
	/**
	 * Return what to save to disk to identify this element when it is the input object to a secondary
	 *  Remote System Explorer perspective.
	 */
	public String getInputMementoHandle(Object element);
	/**
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element);
    /**
     * Somtimes we don't want to remember an element's expansion state, such as for temporarily inserted
     *  messages. In these cases return false from this method. The default is true
     */
    public boolean saveExpansionState(Object element);
    public void selectionChanged(Object element);    // d40615

	public void setFilterString(String filterString);
	public String getFilterString();

	/**
	 * Return whether deferred queries are supported. By default
	 * they are not supported.  Subclasses must override this to
	 * return true if they are to support this.
	 * @param subSys subsystem to use for deferred queries.
	 * @return <code>true</code> if it supports deferred queries, <code>false</code> otherwise.
	 */
	public boolean supportsDeferredQueries(ISubSystem subSys);
}
