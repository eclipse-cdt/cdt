package org.eclipse.cdt.codan.core.param;

import java.text.MessageFormat;

import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;

/**
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
