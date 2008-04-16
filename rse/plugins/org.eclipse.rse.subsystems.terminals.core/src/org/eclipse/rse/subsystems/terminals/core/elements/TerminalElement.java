/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Yu-Fen Kuo (MontaVista)      - initial API and implementation
 * Anna Dushistova (MontaVista) - initial API and implementation
 ********************************************************************************/
package org.eclipse.rse.subsystems.terminals.core.elements;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;

public class TerminalElement extends AbstractResource {
    private String name;

    public String getName() {
        return name;
    }

    public TerminalElement(String name,
            ITerminalServiceSubSystem terminalServiceSubSystem) {
        super(terminalServiceSubSystem);
        this.name = name;
    }

    public String toString() {
        return getName();
    }

    public boolean equals(Object obj) {
        if (obj instanceof TerminalElement) {
            return name.equals(((TerminalElement) obj).getName());
        }
        return super.equals(obj);
    }

    public int hashCode() {
        return name.hashCode();
    }

}
