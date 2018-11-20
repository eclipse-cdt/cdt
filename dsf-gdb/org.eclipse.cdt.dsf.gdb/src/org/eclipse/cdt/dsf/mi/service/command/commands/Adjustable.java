/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ericsson - initial API and implementation for bug 219920
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

/**
 * The implementor of this interface may adjust its output.
 *
 * This is used for MICommands where the output of each option and/or parameter
 * may be adjusted independently to conform to the current version of the MI
 * interface.
 *
 */
public interface Adjustable {

	String getValue();

	String getAdjustedValue();
}
