/*******************************************************************************
 * Copyright (c) 2010, 2011 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.uicontrib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.ui.AbstractCodanProblemDetailsProvider;

/**
 * Example of codan problem details provider for flexlint integration
 */
public class FlexlintHelpLink extends AbstractCodanProblemDetailsProvider {
	@Override
	public boolean isApplicable(String id) {
		String helpId = parseHelpId(getProblemMessage());
		return helpId != null;
	}

	private Pattern messagePattern = Pattern.compile("(Warning|Error|Note|Info) #([0-9]+)"); //$NON-NLS-1$

	private String parseHelpId(String problemMessage) {
		// Warning #613:
		// Note #613:
		// Error #613:
		String helpId = null;
		Matcher matcher = messagePattern.matcher(problemMessage);
		if (matcher.find()) {
			helpId = matcher.group(2);
		}
		return helpId;
	}

	@Override
	public String getStyledProblemDescription() {
		String helpId = parseHelpId(getProblemMessage());
		String url = "http://www.gimpel-online.com/MsgRef.html#" + helpId;
		return "<a href=\"" + url + "\">" + url + "</a>";
	}
}
