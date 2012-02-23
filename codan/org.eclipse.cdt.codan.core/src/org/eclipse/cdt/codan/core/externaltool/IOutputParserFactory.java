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
package org.eclipse.cdt.codan.core.externaltool;

import java.util.List;

/**
 * Factory of instances of <code>{@link IOutputParser}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public interface IOutputParserFactory {
	/**
	 * Creates instances of <code>{@link IOutputParser}</code>.
	 * @param parameters the parameters to pass when invoking an external tool.
	 * @param problemDisplay displays problems found by the external tool.
	 * @return the created parsers.
	 */
	List<IOutputParser> createParsers(InvocationParameters parameters,
			IProblemDisplay problemDisplay);
}
