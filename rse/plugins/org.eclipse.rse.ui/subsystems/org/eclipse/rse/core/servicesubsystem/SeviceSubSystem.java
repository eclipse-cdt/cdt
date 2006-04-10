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

package org.eclipse.rse.core.servicesubsystem;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PropertyPage;




public abstract class SeviceSubSystem extends SubSystem implements IServiceSubSystem 
{

	/**
	 * Constructor 
	 * Subclasses must call this via super().
	 */
	protected SeviceSubSystem(IHost host, IConnectorService connectorService) 
	{
		super(host, connectorService);
	}

	// -------------------------------------
	// GUI methods 
	// -------------------------------------
	/**
	 * <i>Optionally override in order to supply a property sub-page to the tabbed
	 *  notebook in the owning connection's Properties page.</i>
	 * 
	 * Return the single property page to show in the tabbed notebook for the
	 *  for SubSystem property of the parent Connection object, in the Remote Systems
	 *  view. <br>
	 * Return null if no page is to be contributed for this. You are limited to a single
	 *  page, so you may have to compress. It is recommended you prompt for the port
	 *  if applicable since the common base subsystem property page is not shown
	 *  To help with this you can use the {@link org.eclipse.rse.ui.widgets.SystemPortPrompt} widget.
	 * <br>
	 * Returns null by default.
	 */
    public PropertyPage getPropertyPage(Composite parent)
    {
    	return null;
    }
    
    
    // ----------------------------------
	// METHODS THAT MUST BE OVERRIDDEN...
	// ----------------------------------

	
	/**
     * <i>Remote-accessing method, that does nothing by default. <b>Override</b> if filter strings are supported.</i><br>
     * Resolve an <i>absolute</i> filter string. This is only applicable if the subsystem
     *  factory reports true for {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()}, 
     *  which is the default. Otherwise, {@link org.eclipse.rse.core.subsystems.SubSystem#getChildren()}
     *  is called when the subsystem itself is expanded.
     * <p>
     * When a user <u>expands a filter</u> this method is invoked for each filter string and the 
     *  results are concatenated and displayed to the user. You can affect the post-concatenated
     *  result by overriding {@link #sortResolvedFilterStringObjects(Object[])} if you desire to
     *  sort the result, say, or pick our redundancies.
     * <p>
     * The resulting objects are displayed in the tree in the Remote System {@link org.eclipse.rse.ui.view.SystemView view}. 
     * There are <u>two requirements</u> on the returned objects:</p>
     * <ol>
     *   <li>They must implement {@link org.eclipse.core.runtime.IAdaptable}.
     *   <li>Their must be an RSE {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter} registered
     *        for the object's class or interface type. Further, if this subsystem is {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#isVisible() visible}
     *        in the RSE, which is the default, then there must also be an RSE {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter GUI-adapter} registered
     *        with the platform. The base class implementation of this interface is {@link org.eclipse.rse.ui.view.AbstractSystemViewAdapter}.
     * </ol>
     * <p>A good place to start with your remote-resource classes to subclasss {@link org.eclipse.rse.core.internal.subsystems.AbstractResource}, as it
     * already implements IAdaptable, and maintains a reference to this owning subsystem, which helps when 
     * implementing the {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter}.
     * <p>
     * Be sure to register your adapter factory in your plugin's startup method.
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT FILTERS!
	 * 
	 * @param monitor - the progress monitor in effect while this operation performs
	 * @param filterString - one of the filter strings from the expanded filter
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, String filterString)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException
    {
    	return null;
    }

	/**
     * <i>Remote-accessing method, that does nothing by default. <b>Override</b> if filter strings are supported.</i><br>
     * 
     * Resolve a <i>relative</i> filter string. 
     * <p>
     * When a user <u>expands a remote resource</u> this method is invoked and the 
     *  results are potentially sorted and displayed to the user. You can affect the sorting
     *  behaviour by overriding {@link #sortResolvedFilterStringObjects(Object[])} if you desire to
     *  sort the result, say, or pick our redundancies. This is only called if the parent object's adapter indicated it can have children.
     * <p>
     * The resulting objects are displayed in the tree in the Remote System {@link org.eclipse.rse.ui.view.SystemView view}. 
     * There are <u>two requirements</u> on the returned objects:</p>
     * <ol>
     *   <li>They must implement {@link org.eclipse.core.runtime.IAdaptable}.
     *   <li>Their must be an RSE {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter} registered
     *        for the object's class or interface type. Further, if this subsystem is {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#isVisible() visible}
     *        in the RSE, which is the default, then there must also be an RSE {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter GUI-adapter} registered
     *        with the platform. The base class implementation of this interface is {@link org.eclipse.rse.ui.view.AbstractSystemViewAdapter}.
     * </ol>
     * <p>A good place to start with your remote-resource classes to subclasss {@link org.eclipse.rse.core.internal.subsystems.AbstractResource}, as it
     * already implements IAdaptable, and maintains a reference to this owning subsystem, which helps when 
     * implementing the {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter}.
     * <p>
     * Be sure to register your adapter factory in your plugin's startup method.
	 * 
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT FILTERS!
	 * 
	 * @param monitor - the progress monitor in effect while this operation performs
	 * @param parent - the parent resource object being expanded
	 * @param filterString - typically defaults to "*". In future additional user-specific quick-filters may be supported.
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, Object parent, String filterString)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException
    {
    	return null;
    }
    

    // ------------------------
    // MOF GENERATED METHODS...
    // ------------------------
    
	/**
     * <i><b>Private</b> method. Do not override or call.</i><br>
	 * @see org.eclipse.emf.ecore.InternalEObject //GENERICRULES.JSED - replaces InternalEObject. Is this needed? // #refSetValueForEnumAttribute(EAttribute, EEnumLiteral, EEnumLiteral)
	 *
	public void refSetValueForEnumAttribute(EAttribute arg0,EEnumLiteral arg1,EEnumLiteral arg2)
	{
		super.refSetValueForEnumAttribute(arg0, arg1, arg2);
	}*/
	
} //DefaultSubSystemImpl