/*******************************************************************************
 * Copyright (c) 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.text.MessageFormat;

import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;

/**
 * Preference for suppressing a problem using code comments. Automatically added to all problems.
 * @since 4.0
 */
public class SuppressionCommentProblemPreference extends BasicProblemPreference {
	public static final String KEY = "suppression_comment"; //$NON-NLS-1$;
	// Even if using English name it is really a keyword, so no externalizable.
	public static final String KEYWORD = "@suppress(\"{0}\")"; //$NON-NLS-1$;

	public SuppressionCommentProblemPreference() {
		super(KEY, Messages.SuppressionCommentProblemPreference_Label, PreferenceType.TYPE_STRING);
	}

	public static String generateDefaultComment(IProblemWorkingCopy problem) {
		String name = problem.getName();
		return MessageFormat.format(KEYWORD, name);
	}
}
