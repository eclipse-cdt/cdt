/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import java.math.BigInteger;


public class Addr2Line {
    public BigInteger addr;
    public Addr2Line next;
    public int first;
    public int last;
    
    public static int hash(BigInteger addr, int size) {
        return (int)((addr.shiftRight(2).longValue()) % size);
    }
}
