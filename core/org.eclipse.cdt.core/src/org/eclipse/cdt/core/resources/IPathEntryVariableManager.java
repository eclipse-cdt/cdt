/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.resources;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Manages a collection of variables
 * @since 3.0
 */
public interface IPathEntryVariableManager {

	/**
	 * Sets the variable with the given name to be the specified value.
	 * Depending on the value given and if the variable is currently defined
	 * or not, there are several possible outcomes for this operation:
	 * <p>
	 * <ul>
	 * <li>A new variable will be created, if there is no variable defined with
	 * the given name, and the given value is not <code>null</code>.
	 * </li>
	 * 
	 * <li>The referred variable's value will be changed, if it already exists
	 * and the given value is not <code>null</code>.</li>
	 * 
	 * <li>The referred variable will be removed, if a variable with the given
	 * name is currently defined and the given value is <code>null</code>.
	 * </li>
	 *  
	 * <li>The call will be ignored, if a variable with the given name is not
	 * currently defined and the given value is <code>null</code>, or if it is
	 * defined but the given value is equal to its current value.
	 * </li>
	 * </ul>
	 * <p>If a variable is effectively changed, created or removed by a call to
	 * this method, notification will be sent to all registered listeners.</p>
	 * 
	 * @param name the name of the variable 
	 * @param value the value for the variable (may be <code>null</code>)
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>The variable name is not valid</li>
	 * <li>The variable value is relative</li>
	 * </ul>
	 */
	public void setValue(String name, IPath value) throws CoreException;

	/**
	 * Returns the value of the variable with the given name. If there is
	 * no variable defined with the given name, returns <code>null</code>.
	 * 
	 * @param name the name of the variable to return the value for  
	 * @return the value for the variable, or <code>null</code> if there is no
	 *    variable defined with the given name
	 */
	public IPath getValue(String name);

	/**
	 * Returns an array containing all defined variable names.
	 *  
	 * @return an array containing all defined variable names
	 */
	public String[] getVariableNames();

	/**
	 * Registers the given listener to receive notification of changes to
	 * variables. The listener will be notified whenever a variable has been
	 * added, removed or had its value changed. Has no effect if an identical
	 * variable change listener is already registered.
	 * 
	 * @param listener the listener
	 * @see IPathEntryVariableChangeListener
	 */
	public void addChangeListener(IPathEntryVariableChangeListener listener);

	/**
	 * Removes the given variable change listener from the listeners list.
	 * Has no effect if an identical listener is not registered.
	 * 
	 * @param listener the listener 
	 * @see IPathEntryVariableChangeListener
	 */
	public void removeChangeListener(IPathEntryVariableChangeListener listener);

	/**
	 * Resolves a <code>String</code> potentially containing a
	 * variable reference, replacing the variable reference
	 * (if any) with the variable's value (which is a concrete absolute path).
	 * <p>
	 * If the given String is <code>null</code> then <code>null</code> will be
	 * returned.  In all other cases the result will be non-<code>null</code>.
	 * </p>
	 * 
	 * <p>
	 * For example, consider the following collection of path variables:
	 * </p>
	 * <ul>
	 * <li>TEMP = c:/temp</li>
	 * <li>BACKUP = /tmp/backup</li>
	 * </ul>
	 * <p>The following paths would be resolved as:
	 * <p>c:/bin => c:/bin</p>
	 * <p>c:${TEMP} => c:/temp</p>
	 * <p>/TEMP => /TEMP</p>
	 * <p>${TEMP}/foo  => /temp/foo</p>
	 * <p>${BACKUP}  => /tmp/backup</p>
	 * <p>${BACKUP}/bar.txt  => /tmp/backup/bar.txt</p>
	 * <p>SOMEPATH/foo => SOMEPATH/foo</p></p>
	 * 
	 * @param path the path to be resolved
	 * @return the resolved path or <code>null</code>
	 */
	public IPath resolvePath(IPath path);

	/**
	 * Returns <code>true</code> if the given variable is defined and
	 * <code>false</code> otherwise. Returns <code>false</code> if the given
	 * name is not a valid path variable name.
	 * 
	 * @param name the variable's name
	 * @return <code>true</code> if the variable exists, <code>false</code>
	 *    otherwise
	 */
	public boolean isDefined(String name);

}