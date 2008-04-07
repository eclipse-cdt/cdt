/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight (IBM) - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/
package org.eclipse.rse.ui.actions;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuListener;

/**
 * An interface to be implemented for listening for menu show events for the
 * purpose of applying mnemonics to the menu items.
 *
 * @since 3.0
 */
public interface ISystemViewMenuListener extends IMenuListener, MenuListener, ArmListener
{
}
