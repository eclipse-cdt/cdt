package org.eclipse.cdt.linkerscript;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.INode;

public class IDValueConverter implements IValueConverter<String> {

	protected boolean idNeedsQuoting(String id) {
		char char0 = id.charAt(0);
		if (char0 != '.' && !Character.isLetter(char0)) {
			return true;
		}
		for (int i = 0; i < id.length(); i++) {
			if (Character.isWhitespace(id.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toValue(String string, INode node) throws ValueConverterException {
		if (string == null) {
			return null;
		}
		if (string.startsWith("\"") || string.endsWith("\"")) {
			if (string.length() < 3 || !string.startsWith("\"") || !string.endsWith("\"")) {
				throw new ValueConverterException("Could not convert '" + string + "' to an ID", node, null);
			}
			return string.substring(1, string.length() - 1);
		}
		return string;
	}

	@Override
	public String toString(String value) throws ValueConverterException {
		if (value == null) {
			throw new ValueConverterException("ID may not be null.", null, null);
		} else if (value.contains("\"")) {
			throw new ValueConverterException(
					"Invalid ID, identifiers in Linker Scripts cannot contain double-quotes \"", null, null);
		}
		if (idNeedsQuoting(value)) {
			return '"' + value + '"';
		}
		return value;
	}

}
