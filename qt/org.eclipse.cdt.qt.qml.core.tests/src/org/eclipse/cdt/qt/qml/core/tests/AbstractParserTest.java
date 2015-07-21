package org.eclipse.cdt.qt.qml.core.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractParserTest {

	public static String extract() throws Exception {
		StackTraceElement element = Thread.currentThread().getStackTrace()[2];
		String className = element.getClassName();
		int lineNumber = element.getLineNumber();
		Class<?> cls = Class.forName(className);
		String fqn = className.replace('.', '/');
		fqn = fqn.indexOf("$") == -1 ? fqn : fqn.substring(0, fqn.indexOf("$")); //$NON-NLS-1$ //$NON-NLS-2$
		String srcFile = "/" + fqn + ".java"; //$NON-NLS-1$ //$NON-NLS-2$
		StringBuffer code = new StringBuffer();
		Pattern pattern = Pattern.compile("\\s*//\\s*(.*)"); //$NON-NLS-1$
		boolean inComment = false;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(cls.getResourceAsStream(srcFile)))) {
			int n = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (++n >= lineNumber) {
					return code.toString();
				} else {
					Matcher matcher = pattern.matcher(line);
					if (matcher.matches()) {
						if (!inComment) {
							code = new StringBuffer();
						}
						inComment = true;
						code.append(matcher.group(1));
						code.append('\n');
					} else {
						inComment = false;
					}
				}
			}
		}
		return null;
	}

}
