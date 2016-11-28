/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.validation

import java.util.Map
import java.util.Optional
import org.eclipse.cdt.linkerscript.linkerScript.AlignCall
import org.eclipse.cdt.linkerscript.linkerScript.LBinaryOperation
import org.eclipse.cdt.linkerscript.linkerScript.LExpression
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral
import org.eclipse.cdt.linkerscript.linkerScript.LParenthesizedExpression
import org.eclipse.cdt.linkerscript.linkerScript.LTernaryOperation
import org.eclipse.cdt.linkerscript.linkerScript.LUnaryOperation
import org.eclipse.cdt.linkerscript.linkerScript.LVariable
import org.eclipse.cdt.linkerscript.linkerScript.LengthCall
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory
import org.eclipse.cdt.linkerscript.validation.LExpressionReducer

import static extension java.lang.Long.compareUnsigned
import static extension java.lang.Long.divideUnsigned
import static extension java.lang.Long.remainderUnsigned
import org.eclipse.cdt.linkerscript.linkerScript.OriginCall

class LExpressionReducerImpl implements LExpressionReducer {
	var LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE

	var Map<String, Long> memoryLengths = newHashMap()
	var Map<String, Long> memoryOrigins = newHashMap()
	var Map<String, Long> variableValues = newHashMap()

	override Map<String, Long> getMemoryLengthMap() {
		return memoryLengths
	}

	override Map<String, Long> getMemoryOriginMap() {
		return memoryOrigins
	}

	override Map<String, Long> getVariableValuesMap() {
		return variableValues
	}

	def dispatch LExpression reduce(LVariable exp) {
		val value = variableValues.get(exp.feature)
		if (value != null) {
			return value.toLExpression
		}
		return exp
	}

	def dispatch LExpression reduce(AlignCall exp) {
		if (exp.align == null) {
			// can't reduce one arg form
			return exp
		}
		val expToAlignExp = exp.expOrAlign.reduce
		val alignExp = exp.align.reduce

		if (expToAlignExp instanceof LNumberLiteral && alignExp instanceof LNumberLiteral) {
			val expToAlign = (expToAlignExp as LNumberLiteral).value
			val align = (alignExp as LNumberLiteral).value

			if (align == 0L) {
				return expToAlign.toLExpression
			}
			return ((expToAlign + align - 1).divideUnsigned(align) * align).toLExpression
		}
		return null
	}

	def dispatch LExpression reduce(LengthCall exp) {
		val size = memoryLengths.get(exp.memory)
		if (size != null) {
			return size.toLExpression
		}
		return exp
	}

	def dispatch LExpression reduce(OriginCall exp) {
		val origin = memoryOrigins.get(exp.memory)
		if (origin != null) {
			return origin.toLExpression
		}
		return exp
	}

	def dispatch LExpression reduce(LNumberLiteral exp) {
		// nothing more to simplify
		return exp
	}

	def dispatch LExpression reduce(LTernaryOperation exp) {
		val condExp = exp.condition.reduce
		if (condExp instanceof LNumberLiteral) {
			val cond = (condExp as LNumberLiteral).value
			if (cond.toBool) {
				return exp.ifPart.reduce
			} else {
				return exp.thenPart.reduce
			}
		}
		return exp
	}

	def dispatch LExpression reduce(LUnaryOperation exp) {
		val operand = exp.operand.reduce
		if (operand instanceof LNumberLiteral) {
			val literal = switch (exp.feature) {
				case "!":
					(operand.value == 0).toLong
				case "-":
					-operand.value
				case "+":
					operand.value
				case "~":
					operand.value.bitwiseNot
				default:
					throw new UnsupportedOperationException("Other unary not yet implemented")
			}

			val r = factory.createLNumberLiteral()
			r.value = literal
			return r
		} else {
			return exp
		}
	}

	def dispatch LExpression reduce(LBinaryOperation exp) {
		val lhsExp = exp.leftOperand.reduce
		val rhsExp = exp.rightOperand.reduce
		if (lhsExp instanceof LNumberLiteral && rhsExp instanceof LNumberLiteral) {
			val lhs = (lhsExp as LNumberLiteral).value
			val rhs = (rhsExp as LNumberLiteral).value
			switch (exp.feature) {
				case "*":
					lhs * rhs
				case "/":
					lhs.divideUnsigned(rhs)
				case "%":
					lhs.remainderUnsigned(rhs)
				case "+":
					lhs + rhs
				case "-":
					lhs - rhs
				case ">>":
					lhs.operator_tripleGreaterThan(rhs.intValue)
				case "<<":
					lhs << rhs.intValue
				case "==":
					(lhs == rhs).toLong
				case "!=":
					(lhs != rhs).toLong
				case ">":
					(lhs.compareUnsigned(rhs) > 0).toLong
				case "<":
					(lhs.compareUnsigned(rhs) < 0).toLong
				case "<=":
					(lhs.compareUnsigned(rhs) <= 0).toLong
				case ">=":
					(lhs.compareUnsigned(rhs) >= 0).toLong
				case "&":
					lhs.bitwiseAnd(rhs)
				case "!":
					lhs.bitwiseOr(rhs)
				case "&&":
					(lhs.toBool && rhs.toBool).toLong
				case "||":
					(lhs.toBool && rhs.toBool).toLong
				default:
					throw new UnsupportedOperationException("Other unary not yet implemented")
			}.toLExpression
		} else {
			return exp
		}
	}

	def dispatch LExpression reduce(LParenthesizedExpression exp) {
		return exp.exp.reduce
	}

	def toBool(long l) {
		if (l != 0L) {
			true
		} else {
			false
		}
	}

	def toLong(boolean b) {
		if (b) {
			1L
		} else {
			0L
		}
	}

	def toLExpression(long l) {
		val r = factory.createLNumberLiteral()
		r.value = l
		return r
	}

	override public Optional<Long> reduceToLong(LExpression exp) {
		val reduced = exp.reduce
		if (reduced instanceof LNumberLiteral) {
			return Optional.of(reduced.value)
		}
		return Optional.empty
	}

	def dispatch LExpression reduce(LExpression exp) {
		throw new UnsupportedOperationException("LExpression type not yet implemented: " + exp)
	}

}
