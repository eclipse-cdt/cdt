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

/**
 * @see PDAStackCommand
 */
@Immutable
public class PDAStackCommandResult extends PDACommandResult {

	/**
	 * Array of frames return by the stack commands.  The frames are ordered
	 * with the highest-level frame first.
	 */
	final public PDAFrame[] fFrames;

	PDAStackCommandResult(String response) {
		super(response);
		StringTokenizer st = new StringTokenizer(response, "#");
		List<PDAFrame> framesList = new ArrayList<>();

		while (st.hasMoreTokens()) {
			framesList.add(new PDAFrame(st.nextToken()));
		}
		fFrames = framesList.toArray(new PDAFrame[framesList.size()]);
	}
}
