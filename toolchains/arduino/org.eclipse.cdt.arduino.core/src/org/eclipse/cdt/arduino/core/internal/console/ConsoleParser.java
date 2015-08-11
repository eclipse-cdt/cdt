package org.eclipse.cdt.arduino.core.internal.console;

import java.nio.file.Path;

public interface ConsoleParser {

	/**
	 * Returns the pattern to be used for matching. The pattern is a string
	 * representing a regular expression.
	 * 
	 * @return the regular expression to be used for matching
	 */
	public String getPattern();

	/**
	 * Returns the flags to use when compiling this pattern match listener's
	 * regular expression, as defined by by
	 * <code>Pattern.compile(String regex, int flags)</code>
	 * 
	 * @return the flags to use when compiling this pattern match listener's
	 *         regular expression
	 * @see java.util.regex.Pattern#compile(java.lang.String, int)
	 */
	public int getCompilerFlags();

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
	public String getLineQualifier();

	/**
	 * The pattern has been matched. Perform any necessary actions. Generally
	 * this would include creating markers for the errors.
	 * 
	 * @param text text that matched the pattern
	 * @param directory calculated current directory
	 */
	public void patternMatched(String text, Path directory);

}
