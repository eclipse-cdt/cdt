package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParserConfiguration;

/**
 */
public class BinaryParserConfiguration implements IBinaryParserConfiguration {

	String format;
	String name;

	public BinaryParserConfiguration(String format, String name) {
		this.format = format;
		this.name = name;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParserConfiguration#getFormat()
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParserConfiguration#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParserConfiguration#getParser()
	 */
	public IBinaryParser getParser() {
		return CCorePlugin.getDefault().getBinaryParser(format);
	}

}
