/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Object representing a frame in the stack command results.
 *
 * @see PDAStackCommand
 */
@Immutable
public class PDAFrame {

	final public IPath fFilePath;
	final public int fLine;
	final public String fFunction;
	final public String[] fVariables;

	PDAFrame(String frameString) {
		StringTokenizer st = new StringTokenizer(frameString, "|");

		fFilePath = new Path(st.nextToken());
		fLine = Integer.parseInt(st.nextToken());
		fFunction = st.nextToken();

		List<String> variablesList = new ArrayList<>();
		while (st.hasMoreTokens()) {
			variablesList.add(st.nextToken());
		}
		fVariables = variablesList.toArray(new String[variablesList.size()]);
	}
}