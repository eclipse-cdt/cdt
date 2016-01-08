/*******************************************************************************
 * Copyright (c) 2016 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class SuppressionCommentProblemPreference extends BasicProblemPreference implements IProblemPreference {
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
