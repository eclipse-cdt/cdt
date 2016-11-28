package org.eclipse.cdt.linkerscript.tests;

import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;

public interface LExpressionReducer {

	/**
	 * Return modifiable map of known memory sizes
	 */
	Map<String, Long> getMemorySizesMap();

	/**
	 * Return modifiable map of known variable values
	 */
	Map<String, Long> getVariableValuesMap();

	/**
	 * Reduce input to a simpler version. If result is constant the returned
	 * type will be of {@link LNumberLiteral}
	 *
	 * @param exp
	 *            expression to reduce
	 * @return reduced
	 */
	LExpression reduce(LExpression exp);

	/**
	 * Reduce input to a constant.
	 *
	 * @param exp
	 *            expression to reduce
	 * @return reduced constant if present
	 */
	Optional<Long> reduceToLong(LExpression exp);
}
