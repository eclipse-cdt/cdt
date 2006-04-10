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

import org.eclipse.ui.IActionFilter;

/**
 * This interface is implemented by the adapters for every object shown in the
 * Remote System Explorer. It enables complex filtering of action and popup menu
 * extensions via the <samp>&lt;filter&gt;</samp> element, and action extensions
 * via the <samp>&lt;visibility&gt;</samp> and <samp>&lt;enablement&gt;</samp>
 * elements.
 * <p>
 * The base adapter class used for all RSE objects supports the following properties
 * by default:
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
 */
public interface ISystemViewActionFilter extends IActionFilter
{
	
}