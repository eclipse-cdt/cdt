/**********************************************************************
 * Copyright (c) 2005 Symbian Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Symbian Ltd - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * This interface represents an option value handler in the managed build 
 * system. It is used to enable a tool integrator to use the MBS configuration
 * GUI, while linking to an alternative back-end.
 * 
 * @since 3.0
 */
public interface IManagedOptionValueHandler{   

	public final int EVENT_OPEN = 1;       /** The option is opened, i.e. its UI element 
	                                        *  is created. The valueHandler can override 
	                                        *  the value of the option. If it does not, 
	                                        *  the last persisted value is used. */
	public final int EVENT_CLOSE = 2;      /** The option is closed. i.e. its value has been
											*  destroyed when a configuration/resource gets deleted.
											*  The valuehandler can do various things assocaited with
											*  destroying the option such as freeing the memory
											*  associated with this option callback, if needed. */
	public final int EVENT_SETDEFAULT = 3; /** The default value option::defaultValue has 
	                                        *  been set. The handleValue callback is called 
	                                        *  afterwards to give the handler a chance to 
	                                        *  override the value or to update the value in 
	                                        *  its back-end. Typically this event will be called 
	                                        *  when the Restore Defaults button is pressed. */
	public final int EVENT_APPLY = 4;      /** The option has been set by pressing the Apply 
	                                        *  button (or the OK button). The valueHandler can 
	                                        *  transfer the value of the option to its own 
	                                        *  back-end. */
	
/**
 * Handles transfer between values between UI element and 
 * back-end in different circumstances.
 * 
 * @param configuration  build configuration of option 
 *                       (may be IConfiguration or IResourceConfiguration)
 * @param holder         contains the holder of the option
 * @param option         the option that is handled
 * @param extraArgument  extra argument for handler
 * @param event          event to be handled 
 *
 * @return  True when the event was handled, false otherwise.
 * This enables default event handling can take place.
 */
boolean handleValue(IBuildObject configuration, 
                    IHoldsOptions holder, 
                    IOption option,
                    String extraArgument, 
                    int event);

/**
 * Checks whether the value of an option is its default value.
 * 
 * @param configuration  build configuration of option 
 *                       (may be IConfiguration or IResourceConfiguration)
 * @param holder         contains the holder of the option
 * @param option         the option that is handled
 * @param extraArgument  extra argument for handler
 *
 * The additional options besides configuration are supplied to  
 * provide enough information for querying the default value from 
 * a potential data storage back-end.
 * 
 * @return  True if the options value is its default value and
 * False otherwise. This enables that default event handling can 
 * take place.
 */
boolean isDefaultValue(IBuildObject configuration, 
                       IHoldsOptions holder, 
                       IOption option, 
                       String extraArgument);

/**
 * Checks whether an enumeration value of an option is currently a 
 * valid choice. The use-case for this method is the case, where
 * the set of valid enumerations in the plugin.xml file changes.
 * The UI will remove entries from selection lists if the value 
 * returns false.
 * 
 * @param configuration  build configuration of option 
 *                       (may be IConfiguration or IResourceConfiguration)
 * @param holder         contains the holder of the option
 * @param option         the option that is handled
 * @param extraArgument  extra argument for handler
 * @param enumValue      enumeration value that is to be checked
 *
 * The additional options besides configuration are supplied to  
 * provide enough information for querying information from a 
 * a potential data storage back-end.
 * 
 * @return  True if the enumeration value is valid and False 
 * otherwise.
 */
boolean isEnumValueAppropriate(IBuildObject configuration, 
                               IHoldsOptions holder, 
                               IOption option,
                               String extraArgument, 
                               String enumValue);
}
