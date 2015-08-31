package org.eclipse.cdt.core.build;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * This could be temporary. Provides a core parser for the TextConsole's
 * IPatternMatchListener.
 * 
 * TODO that framework doesn't work well for builds that need to use different
 * parsers at different times. Should consider taking that architecture and
 * making it work well for our needs.
 * 
 * @since 5.12
 */
public abstract class CConsoleParser {

	private final String pattern;
	private final int flags;
	private final String lineQualifier;

	private final Pattern errorPattern;

	public static final String LINK_OFFSET = "cdt.link.offset"; //$NON-NLS-1$
	public static final String LINK_LENGTH = "cdt.link.length"; //$NON-NLS-1$

	protected CConsoleParser(String pattern, int flags, String lineQualifier) {
		this.pattern = pattern;
		this.flags = flags;
		this.lineQualifier = lineQualifier;
		this.errorPattern = Pattern.compile(pattern);
	}

	protected CConsoleParser(String pattern) {
		this(pattern, 0, null);
	}

	/**
	 * Returns the pattern to be used for matching. The pattern is a string
	 * representing a regular expression.
	 * 
	 * @return the regular expression to be used for matching
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Returns the flags to use when compiling this pattern match listener's
	 * regular expression, as defined by by
	 * <code>Pattern.compile(String regex, int flags)</code>
	 * 
	 * @return the flags to use when compiling this pattern match listener's
	 *         regular expression
	 * @see java.util.regex.Pattern#compile(java.lang.String, int)
	 */
	public int getCompilerFlags() {
		return flags;
	}

	/**
	 * Returns a simple regular expression used to identify lines that may match
	 * this pattern matcher's complete pattern, or <code>null</code>. Use of
	 * this attribute can improve performance by disqualifying lines from the
	 * search. When a line is found containing a match for this expression, the
	 * line is searched from the beginning for this pattern matcher's complete
	 * pattern. Lines not containing this pattern are discarded.
	 * 
	 * @return a simple regular expression used to identify lines that may match
	 *         this pattern matcher's complete pattern, or <code>null</code>
	 */
	public String getLineQualifier() {
		return lineQualifier;
	}

	protected abstract String getFileName(Matcher matcher);

	protected abstract int getLineNumber(Matcher matcher);

	protected abstract String getMessage(Matcher matcher);

	protected abstract int getSeverity(Matcher matcher);

	protected abstract int getLinkOffset(Matcher matcher);

	protected abstract int getLinkLength(Matcher matcher);

	public IMarker generateMarker(IFolder buildDirectory, String text) {
		Matcher matcher = errorPattern.matcher(text);
		if (matcher.matches()) {
			String fileName = getFileName(matcher);

			IFile file = buildDirectory.getFile(fileName);
			if (file.exists()) {
				try {
					IMarker marker = file.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
					marker.setAttribute(IMarker.MESSAGE, getMessage(matcher));
					marker.setAttribute(IMarker.SEVERITY, getSeverity(matcher));
					marker.setAttribute(IMarker.LINE_NUMBER, getLineNumber(matcher));
					marker.setAttribute(IMarker.CHAR_START, -1);
					marker.setAttribute(IMarker.CHAR_END, -1);
					marker.setAttribute(LINK_OFFSET, getLinkOffset(matcher));
					marker.setAttribute(LINK_LENGTH, getLinkLength(matcher));
					return marker;
				} catch (CoreException e) {
					CCorePlugin.log(e);
					return null;
				}
			}
		}
		return null;
	}

}
