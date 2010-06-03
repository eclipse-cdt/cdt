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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * Object representing a bit field in the stack command results.
 * 
 * @see PDARegistersCommand 
 */
@Immutable
public class PDABitField {

    final public String fName;
    final public int fOffset;
    final public int fCount;
    final public Map<String, String> fMnemonics;
    
    PDABitField(String bitFieldString) {
        StringTokenizer st = new StringTokenizer(bitFieldString, "   ");
        
        fName = st.nextToken();
        fOffset = Integer.parseInt(st.nextToken());
        fCount = Integer.parseInt(st.nextToken());
        
        fMnemonics = new LinkedHashMap<String, String>(0);
        while (st.hasMoreTokens()) {
            fMnemonics.put(st.nextToken(), st.nextToken());
        }
    }
}