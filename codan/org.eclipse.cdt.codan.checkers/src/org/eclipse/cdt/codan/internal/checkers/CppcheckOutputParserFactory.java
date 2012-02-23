/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import static java.util.Collections.singletonList;

import java.util.List;

import org.eclipse.cdt.codan.core.externaltool.IOutputParser;
import org.eclipse.cdt.codan.core.externaltool.IOutputParserFactory;
import org.eclipse.cdt.codan.core.externaltool.IProblemDisplay;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;

/**
 * @author alruiz@google.com (Alex Ruiz)
 */
class CppcheckOutputParserFactory implements IOutputParserFactory {
	@Override
	public List<IOutputParser> createParsers(InvocationParameters parameters,
			IProblemDisplay problemDisplay) {
		IOutputParser parser = new CppcheckOutputParser(parameters, problemDisplay);
		return singletonList(parser);
	}
}
