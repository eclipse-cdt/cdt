/*******************************************************************************
 * Copyright (c) 2010, 2015 Alena Laskavaia and others.
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

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.codan.ui.AbstractCodanProblemDetailsProvider;

/**
 * Example of codan problem details provider for string search integration
 */
public class GrepCheckerHelpLink extends AbstractCodanProblemDetailsProvider {
	@Override
	public boolean isApplicable(String id) {
		return id.startsWith("org.eclipse.cdt.codan.examples.checkers.GrepCheckerProblem");
	}

	@Override
	public String getStyledProblemDescription() {
		String arg = CodanProblemMarker.getProblemArgument(marker, 0);
		String url = "http://www.google.ca/search?q=" + arg;
		return "Google " + "<a href=\"" + url + "\">" + arg + "</a>";
	}
}
