package org.eclipse.cdt.core.parser;

/**
 * Interface for providing settings for the parser.
 * @since 5.6
 */
public interface IParserSettings {

	/**
	 * Returns the maximum number of trivial expressions in aggregate initializers. Exceeding numbers
	 * of trivial aggregate initializers should be skipped by the parser for performance reasons.
	 */
	public int getMaximumTrivialExpressionsInAggregateInitializers();
}
