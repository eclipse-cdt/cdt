package org.eclipse.cdt.linkerscript

import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService
import org.eclipse.xtext.nodemodel.INode

class LinkerScriptConverters extends AbstractDeclarativeValueConverterService {

	@ValueConverter(rule="MemoryName")
	def public IValueConverter<String> MemoryName() {
		return ID()
	}

	@ValueConverter(rule="ValidID")
	def public IValueConverter<String> ValidID() {
		return ID()
	}

	@ValueConverter(rule="WildID")
	def public IValueConverter<String> WildID() {
		return ID()
	}

	@ValueConverter(rule="ID")
	def public IValueConverter<String> ID() {
		return new IValueConverter<String>() {

			override toString(String value) throws ValueConverterException {
				// TODO only surround with " if needed
				return "\"" + value + "\"";
			}

			override toValue(String string, INode node) throws ValueConverterException {
				if (string.startsWith("\"") || string.endsWith("\"")) {
					if (string.length < 3 || !string.startsWith("\"") || !string.endsWith("\"")) {
						throw new ValueConverterException("Could not convert '" + string + "' to an ID", node, null);
					}
					return string.substring(1, string.length() - 1)
				}
				return string
			}

		}
	}

	@ValueConverter(rule="MemoryAttribute")
	def public IValueConverter<String> MemoryAttribute() {
		return new IValueConverter<String>() {

			override toString(String value) throws ValueConverterException {
				return "(" + value + ")";
			}

			override toValue(String string, INode node) throws ValueConverterException {
				try {
					return memoryAttributeToValue(string);
				} catch (NumberFormatException e) {
					throw new ValueConverterException("Could not convert '" + string + "' to a number", node, e);
				}
			}

		}
	}

	@ValueConverter(rule="Number")
	def public IValueConverter<Long> Number() {
		return new IValueConverter<Long>() {

			override toString(Long value) throws ValueConverterException {
				return "0x" + Long.toUnsignedString(value, 16);
			}

			override toValue(String string, INode node) throws ValueConverterException {
				try {
					return numberToValue(string);
				} catch (NumberFormatException e) {
					throw new ValueConverterException("Could not convert '" + string + "' to a number", node, e);
				}
			}

		}
	}

	def public static String memoryAttributeToValue(String string) {
		// TODO: Validate attributes
		// XXX: Consider changing rule to return an int representing attributes as flags
		return string;
	}

	def public static long numberToValue(String string) throws NumberFormatException {
		var numeralPart = string;
		var radix = 10;
		var shift = 0;
		if (numeralPart.startsWith("0x") || numeralPart.startsWith("0X")) {
			radix = 16;
			numeralPart = numeralPart.substring(2);
		} else if (numeralPart.startsWith("$")) {
			radix = 16;
			numeralPart = numeralPart.substring(1);
		}

		if (numeralPart.empty) {
			throw new NumberFormatException
		}

		var hasSuffix = true
		val suffix = numeralPart.substring(numeralPart.length - 1, numeralPart.length).toLowerCase
		if (radix == 10) {
			switch (suffix) {
				case 'h',
				case 'x': {
					radix = 16
				}
				case 'b': {
					radix = 2
				}
				case 'o': {
					radix = 8
				}
				case 'd': {
					radix = 10
				}
				case 'k': {
					shift = 10
				}
				case 'm': {
					shift = 20
				}
				default: {
					hasSuffix = false
				}
			}
		} else {
			switch (suffix) {
				case 'k': {
					shift = 10
				}
				case 'm': {
					shift = 20
				}
				default: {
					hasSuffix = false
				}
			}
		}

		if (hasSuffix) {
			numeralPart = numeralPart.substring(0, numeralPart.length - 1)
		}

		val result = Long.parseUnsignedLong(numeralPart, radix)
		val shifted = switch (shift) {
			case 0:
				result
			case 10: {
				if ((0xffc0_0000_0000_0000#L.bitwiseAnd(result) != 0)) {
					throw new NumberFormatException(
						String.format("String value %s exceeds " + "range of unsigned long.", string))
				}
				result << 10
			}
			case 20: {
				if ((0xffff_f000_0000_0000#L.bitwiseAnd(result) != 0)) {
					throw new NumberFormatException(
						String.format("String value %s exceeds " + "range of unsigned long.", string))
				}
				result << 20
			}
			default: {
				throw new NumberFormatException("Unreachable/Logic error, shift can only be 0, 10, or 20")
			}
		}

		return shifted
	}
}
