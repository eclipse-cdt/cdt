package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.core.index.TagFlags;

public class CTagsEntry implements ITagEntry {

	final static String TAB_SEPARATOR = "\t";
	final static String PATTERN_SEPARATOR = ";\"";
	final static String LANGUAGE = "language";
	final static String KIND = "kind";
	final static String LINE = "line";
	final static String FILE = "file";
	final static String INHERITS = "inherits";
	final static String ACCESS = "access";
	final static String IMPLEMENTATION = "implementation";
	final static String CLASS = "class";

	final String[] NONE = new String[0];

	String tagName;

	/* Path of source file containing definition of tag.  */
	String fileName;
	
	/* IFile of parsed file */
	IFile file;

	/* pattern for locating source line
	 * (may be NULL if not present) */
	String pattern;

	int lineNumber;

	/* Miscellaneous extension fields */
	HashMap tagExtensionField;

	String line;

	public CTagsEntry(String line, IFile file) {
		this.line = line; 
		this.file = file;
		tagName = "";
		fileName ="";
		pattern = null;
		lineNumber = 0;
		tagExtensionField = new HashMap();
		parse();
	}

	void parse () {
		String delim = TAB_SEPARATOR;
		StringTokenizer st = new StringTokenizer(line, delim);
		for (int state = 0; st.hasMoreTokens(); state++) {
			String token = st.nextToken(delim);
			switch (state) {
				case 0: // TAG_NAME:
					tagName = token;
				break;

				case 1: // FILE_NAME:
					fileName = token;
					delim = PATTERN_SEPARATOR;
				break;

				case 2: // PATTERN;
					try {
						String sub = token.substring(1);
						if (Character.isDigit(sub.charAt(0))) {
							lineNumber = Integer.parseInt(sub);
						} else {
							// Filter out the delimiters
							int i = sub.indexOf("/^");
							int j = sub.lastIndexOf("$/");
							try {
								if(i >= 0 && j >= 0 ) {
									sub = sub.substring(i + 2, j);
								} else if(i >= 0) {
									sub = sub.substring(i + 2, sub.length()-1);
								}
							} catch (Exception e) {}
							pattern = sub;
						}
					} catch (NumberFormatException e) {
						//e.printStackTrace();
					} catch (IndexOutOfBoundsException e) {
						//e.printStackTrace();
					}
					delim = TAB_SEPARATOR;
				break;

				default: // EXTENSION_FIELDS:
					int i = token.indexOf(':');
					if (i != -1) {
						String key = token.substring(0, i);
						String value = token.substring(i + 1);
						tagExtensionField.put(key, value);
					}
				break;
			}
		}
	}

	public String getTagName () {
		return tagName;
	}

	public String getFileName() {
		return fileName;
	}
	public IFile getIFile() {
		return file;
	}

	public String getPattern() {
		return pattern;
	}

	// line:
	public int getLineNumber() {
		try {
			String sub = (String)tagExtensionField.get(LINE);
			if (sub != null) {
				lineNumber = Integer.parseInt(sub);
			}
		} catch (NumberFormatException e) {
			//System.out.println(e);
		}
		return lineNumber;
	}

	// kind:
	public int getKind() {
		String kind = (String)tagExtensionField.get(KIND);
		return TagFlags.value(kind);
	}

	// language:
	public String getLanguage() {
		return (String)tagExtensionField.get(LANGUAGE);
	}

	// Implementation:
	public int getImplementation() {
		String impl = (String)tagExtensionField.get(IMPLEMENTATION);
		return TagFlags.value(impl);
	}

	// Class:
	public String getClassName() {
		return (String)tagExtensionField.get(CLASS);
	}

	// file:
	public boolean hasFileScope() {
		return (tagExtensionField.get(FILE) != null);
	}

	// inherits:
	public String[] getInherits() {
		String base = (String)tagExtensionField.get(INHERITS);
		if (base != null) {
			StringTokenizer st = new StringTokenizer(base, ",");
			List list = new ArrayList();
			while (st.hasMoreTokens()) {
				list.add(st.nextToken());
			}
			return (String[])list.toArray(new String[0]);
		}
		return NONE;
	}

	// access:
	public int getAccessControl() {
		String access = (String)tagExtensionField.get(ACCESS);
		return TagFlags.value(access);
	}

	public String getLine() {
		return line;
	}

	public static String makeTagLine(ITagEntry tagEntry) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(tagEntry.getTagName());
		buffer.append("\t");
		buffer.append(tagEntry.getFileName());
		buffer.append("\t");
        String pat = tagEntry.getPattern();
		if (pat != null) {
			buffer.append(pat);
		} else {
			buffer.append(tagEntry.getLineNumber());
		}
		buffer.append(";\"");
		buffer.append("\t");

		String kind = TagFlags.value(tagEntry.getKind());
		if (kind != null) {
	        buffer.append(KIND + ":" + tagEntry.getKind());
			buffer.append("\t");
		}

		String lang = tagEntry.getLanguage();
		if (lang != null) {
	        buffer.append(LANGUAGE + ":" + tagEntry.getLanguage());
			buffer.append("\t");
		}

        if (tagEntry.hasFileScope()) {
	        buffer.append(FILE + ":");
	        buffer.append("\t");
		}

        String[] inherits = tagEntry.getInherits();
		for (int i = 0; i < inherits.length; i++) {
			if (i == 0) {
				buffer.append(INHERITS + ":");
			} else {
				buffer.append(",");
			}
			buffer.append(inherits[i]);
		}

		String access = TagFlags.value(tagEntry.getAccessControl());
		if (access != null) {
			buffer.append(ACCESS + ":" + access);
	        buffer.append("\t");
		}

		String impl = TagFlags.value(tagEntry.getImplementation());
		if (impl != null) {
        	buffer.append(IMPLEMENTATION + ":" + impl);
	        buffer.append("\t");
		}

		String clazz = tagEntry.getClassName();
		if (clazz != null) {
			buffer.append(CLASS + ":" + clazz);
	        buffer.append("\t");
		}
		return buffer.toString().trim();
	}

	public void print() {
		System.out.println("TagName " + getTagName());
		System.out.println("FileName " + getFileName());
        System.out.println("Pattern " + getPattern());
        System.out.println("LineNumber " + getLineNumber());
        System.out.println("Kind " + getKind());
        System.out.println("Language " + getLanguage());
        System.out.println("FileScope " + hasFileScope());
        String[] inherits = getInherits();
		for (int i = 0; i < inherits.length; i++) {
			System.out.println("Inherit " + inherits[i]);
		}
		System.out.println("AccessControl " + getAccessControl());
        System.out.println("Implementation " + getImplementation());
		System.out.println("ClassName " + getClassName());
	}
}
