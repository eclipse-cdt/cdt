/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo (MontaVista)      - initial API and implementation
 * Anna Dushistova (MontaVista) - initial API and implementation
 * Yu-Fen Kuo (MontaVista)      - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Martin Oberhuber (Wind River) - [228577] [rseterminal] Further cleanup
 ********************************************************************************/
package org.eclipse.rse.subsystems.terminals.core.elements;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.services.terminals.ITerminalShell;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;

/**
 * An element in the RSE Tree that resembles a Terminal connection.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a> team.
 * </p>
 */
public class TerminalElement extends AbstractResource {
    private String name;
    private ITerminalShell terminalShell;

    /**
	 * Constructor.
	 */
    public TerminalElement(String name,
            ITerminalServiceSubSystem terminalServiceSubSystem) {
        super(terminalServiceSubSystem);
        this.name = name;
    }

    /**
	 * Return the name of this element, which will also be used as the label in
	 * the tree.
	 */
    public final String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public boolean equals(Object obj) {
    	if (obj == this)
			return true;
		if (!(obj instanceof TerminalElement))
			return false;
		TerminalElement other = (TerminalElement) obj;
		return name.equals(other.getName())
				&& getSubSystem().equals(other.getSubSystem())
		        && (terminalShell == null ? other.getTerminalShell() == null
				   : terminalShell.equals(other.getTerminalShell()));
    }

    public int hashCode() {
        if (terminalShell != null)
            return terminalShell.hashCode() * 37 + name.hashCode();
        return name.hashCode() ;
    }

	/**
	 * Return the back-end connection of this terminal instance.
	 *
	 * @since 1.0
	 */
    public ITerminalShell getTerminalShell() {
        return terminalShell;
    }

	/**
	 * Set the back-end connection of this terminal instance.
	 * 
	 * @since 1.0
	 */
    public void setTerminalShell(ITerminalShell terminalShell) {
        this.terminalShell = terminalShell;
    }

}
