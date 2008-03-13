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
package org.eclipse.dd.examples.pda.service.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.dd.dsf.concurrent.Immutable;


/**
 * @see PDADataCommand
 */
@Immutable
public class PDADataCommandResult extends PDACommandResult {
    
    final public String[] fValues;
    
    PDADataCommandResult(String response) {
        super(response);
        StringTokenizer st = new StringTokenizer(response, "|");
        List<String> valuesList = new ArrayList<String>();
        
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() != 0) {
                valuesList.add(st.nextToken());
            }
        }
        
        fValues = new String[valuesList.size()];
        for (int i = 0; i < valuesList.size(); i++) {
            fValues[i] = valuesList.get(i);
        }
    }
}
