package org.eclipse.cdt.linkerscript.tests

import java.util.Map
import org.eclipse.cdt.linkerscript.linkerScript.AlignCall
import org.eclipse.cdt.linkerscript.linkerScript.LBinaryOperation
import org.eclipse.cdt.linkerscript.linkerScript.LExpression
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral
import org.eclipse.cdt.linkerscript.linkerScript.LUnaryOperation
import org.eclipse.cdt.linkerscript.linkerScript.LengthCall
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory
import org.eclipse.cdt.linkerscript.linkerScript.impl.LinkerScriptFactoryImpl

import static extension java.lang.Long.divideUnsigned
import static extension java.lang.Long.remainderUnsigned
import static extension java.lang.Long.compareUnsigned
import org.eclipse.cdt.linkerscript.linkerScript.LVariable

// TODO: this can move to core
class LExpressionReducer {
	var LinkerScriptFactory factory

	var Map<String, Long> memorySizes
	var Map<String, Long> variableValues

	new(Map<String, Long> memorySizes, Map<String, Long> variableValues) {
		// TODO: How to do this with injection?
		this.factory = new LinkerScriptFactoryImpl()
		this.memorySizes = memorySizes
		this.variableValues = variableValues
	}

	new(Map<String, Long> memorySizes) {
		this(memorySizes, emptyMap)
	}

	def LExpression reduce(LVariable exp) {
		val value = variableValues.get(exp.feature)
		if (value != null) {
			return value.toLExpression
		}
		return exp
	}

	def LExpression reduce(AlignCall exp) {
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

	def LExpression reduce(LengthCall exp) {
		val size = memorySizes.get(exp.memory)
		if (size != null) {
			return size.toLExpression
		}
		return exp
	}

	def LExpression reduce(LNumberLiteral exp) {
		// nothing more to simplify
		return exp
	}

	def LExpression reduce(LUnaryOperation exp) {
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

	def LExpression reduce(LBinaryOperation exp) {
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

	// TODO: Bad API, returning null :-(
	def public Long reduceToLong(LExpression exp) {
		val reduced = exp.reduce
		if (reduced instanceof LNumberLiteral) {
			return reduced.value
		}
		return null
	}

	def public LExpression reduce(LExpression exp) {
		return switch (exp) {
			AlignCall: exp.reduce
			LengthCall: exp.reduce
			LNumberLiteral: exp.reduce
			LBinaryOperation: exp.reduce
			LUnaryOperation: exp.reduce
			LVariable: exp.reduce
			default: throw new UnsupportedOperationException("Other types not yet implemented " + exp)
		}
	}

}
