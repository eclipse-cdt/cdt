/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo (MontaVista) - initial API and implementation
 * Yu-Fen Kuo (MontaVista) - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 ********************************************************************************/
package org.eclipse.rse.subsystems.terminals.core;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;

public interface ITerminalServiceSubSystem extends ISubSystem {
	public void addChild(TerminalElement element);

	public void removeChild(TerminalElement element);
	
	public void removeChild(String terminalTitle);
	
	public TerminalElement getChild(String terminalTitle);
}
