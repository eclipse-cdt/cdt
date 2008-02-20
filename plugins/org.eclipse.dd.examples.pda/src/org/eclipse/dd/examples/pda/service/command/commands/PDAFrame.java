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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PDAFrame {
    PDAFrame(String frameString) {
        StringTokenizer st = new StringTokenizer(frameString, "|");
        
        fFilePath = new Path(st.nextToken());
        fLine = Integer.parseInt(st.nextToken());
        fFunction = st.nextToken();
        
        List<String> variablesList = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            variablesList.add(st.nextToken());
        }
        fVariables = variablesList.toArray(new String[variablesList.size()]);
    }
    final public IPath fFilePath;
    final public int fLine;
    final public String fFunction;
    final public String[] fVariables;
}