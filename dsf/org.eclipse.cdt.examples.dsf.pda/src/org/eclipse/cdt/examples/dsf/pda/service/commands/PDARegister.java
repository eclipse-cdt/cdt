/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * Object representing a register in the registers command results.
 * 
 * @see PDARCommand 
 */
@Immutable
public class PDARegister {

    final public String fName;
    final public boolean fWritable;
    final public PDABitField[] fBitFields;
    
    PDARegister(String regString) {
        StringTokenizer st = new StringTokenizer(regString, "|");
        
        String regInfo = st.nextToken();
        StringTokenizer regSt = new StringTokenizer(regInfo, " ");
        fName = regSt.nextToken();
        fWritable = Boolean.parseBoolean(regSt.nextToken());
        
        List<PDABitField> bitFieldsList = new ArrayList<PDABitField>();
        while (st.hasMoreTokens()) {
            bitFieldsList.add(new PDABitField(st.nextToken()));
        }
        fBitFields = bitFieldsList.toArray(new PDABitField[bitFieldsList.size()]);
    }
}