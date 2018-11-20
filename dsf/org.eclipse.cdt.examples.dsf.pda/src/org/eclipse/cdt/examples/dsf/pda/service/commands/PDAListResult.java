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
 * @see PDADataCommand
 */
@Immutable
public class PDAListResult extends PDACommandResult {

	final public String[] fValues;

	PDAListResult(String response) {
		super(response);
		StringTokenizer st = new StringTokenizer(response, "|");
		List<String> valuesList = new ArrayList<>();

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.length() != 0) {
				valuesList.add(token);
			}
		}

		fValues = new String[valuesList.size()];
		for (int i = 0; i < valuesList.size(); i++) {
			fValues[i] = valuesList.get(i);
		}
	}
}
