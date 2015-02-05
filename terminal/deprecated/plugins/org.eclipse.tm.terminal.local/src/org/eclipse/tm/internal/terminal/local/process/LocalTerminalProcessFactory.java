/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.process;

import java.util.Map;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;

/**
 * The class {@link LocalTerminalProcessFactory} is an {@link IProcessFactory} that produces
 * {@link LocalTerminalProcess} objects.
 *
 * @author Mirko Raner
 * @version $Revision: 1.2 $
 */
public class LocalTerminalProcessFactory implements IProcessFactory {

	/**
	 * The ID of this factory (as used in <code>plugin.xml</code>).
	 */
	public final static String ID = "org.eclipse.tm.terminal.localProcess.factory"; //$NON-NLS-1$

	/**
	 * @see IProcessFactory#newProcess(ILaunch, Process, String, Map)
	 */
	public IProcess newProcess(ILaunch launch, Process process, String label, Map attributes) {

		return new LocalTerminalProcess(launch, process, label, attributes);
	}
}
