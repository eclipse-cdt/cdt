/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @see PDADataCommand
 */
public class PDADataCommandResult extends PDACommandBaseResult {
    
    final public int[] fValues;
    
    PDADataCommandResult(String response) {
        super(response);
        StringTokenizer st = new StringTokenizer(response, "|");
        List<Integer> valuesList = new ArrayList<Integer>();
        
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() != 0) {
                valuesList.add(new Integer(st.nextToken()));
            }
        }
        
        
        fValues = new int[valuesList.size()];
        for (int i = 0; i < valuesList.size(); i++) {
            fValues[i] = valuesList.get(i);
        }
    }
}
