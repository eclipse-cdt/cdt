package org.eclipse.cdt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class GCCErrorParser implements IErrorParser {
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		return processLine(line, eoParser, IMarkerGenerator.SEVERITY_ERROR_RESOURCE);
	}

	public boolean processLine(String line, ErrorParserManager eoParser, int inheritedSeverity) {
		// Known patterns.
		// (a)
		// filename:lineno: description
		//
		// (b)
		// filename:lineno:column: description
		//
		// (c)
		// In file included from b.h:2,
		//				 from a.h:3,
		//				 from hello.c:3:
		// c.h:2:15: missing ')' in macro parameter list
		//
		// (d)
		// In file included from hello.c:3:
		// c.h:2:15: missing ')' in macro parameter list
		//
		// (e)
		// h.c: In function `main':
		// h.c:41: `foo' undeclared (first use in this function)
		// h.c:41: (Each undeclared identifier is reported only once
		// h.c:41: for each function it appears in.)
		// h.c:41: parse error before `char'
		// h.c:75: `p' undeclared (first use in this function)

		int firstColon = line.indexOf(':');

		/* Guard against drive in Windows platform.  */
		if (firstColon == 1) {
			try {
				String os = System.getProperty("os.name"); //$NON-NLS-1$
				if (os != null && os.startsWith("Win")) { //$NON-NLS-1$
					try {
						if (Character.isLetter(line.charAt(0))) {
							firstColon = line.indexOf(':', 2);
						}
					} catch (StringIndexOutOfBoundsException e) {
					}
				}
			} catch (SecurityException e) {
			}
		}

		if (firstColon != -1) {
			try {
				int secondColon = -1;
				int	num  = -1;

				while ((secondColon = line.indexOf(':', firstColon + 1)) != -1) {
					String lineNumber = line.substring(firstColon + 1, secondColon);
					try {
						num = Integer.parseInt(lineNumber);
					} catch (NumberFormatException e) {
						// Failed.
					}
					if (num != -1) {
						break; // Find possible match.
					}
					firstColon = secondColon;
				}

				if (secondColon != -1) {
					int col = -1;

					String fileName = line.substring(0, firstColon);
					String varName = null;
					String desc = line.substring(secondColon + 1).trim();
					/* Then check for the column  */
					int thirdColon= line.indexOf(':', secondColon + 1);
					if (thirdColon != -1) {
						String columnNumber = line.substring(secondColon + 1, thirdColon);
						try {
							col = Integer.parseInt(columnNumber);
						} catch (NumberFormatException e) {
						}
					}
					if (col != -1) {
						desc = line.substring(thirdColon + 1).trim();
					}

					// gnu c: filename:no: (Each undeclared identifier is reported
					// only once. filename:no: for each function it appears in.)
					if (desc.startsWith ("(Each undeclared")) { //$NON-NLS-1$
						// Do nothing.
						return false;
					}
					if (desc.endsWith(")")) { //$NON-NLS-1$
						String previous = eoParser.getPreviousLine();
						// It if is a "(Each undeclared ..." ignore this.
						// we already have the error.
						if (previous.indexOf("(Each undeclared") >= 0 ) { //$NON-NLS-1$
							// Do nothing.
							return false;
						}
					}

					/* See if we can get a var name
					 * Look for:
					 * `foo' undeclared
					 * `foo' defined but not used
					 * conflicting types for `foo'
					 * previous declaration of `foo'
					 * parse error before `foo'
					 *
					 */ 
					 int s;
					 if((s = desc.indexOf("\' undeclared")) != -1) { //$NON-NLS-1$
					 	int p = desc.indexOf("`"); //$NON-NLS-1$
					 	if (p != -1) {
					 		varName = desc.substring(p+1, s);
					 		//System.out.println("undex varName "+ varName);
					 	}
					 } else if((s = desc.indexOf("\' defined but not used")) != -1) { //$NON-NLS-1$
					 	int p = desc.indexOf("`"); //$NON-NLS-1$
					 	if (p != -1) {
					 		varName = desc.substring(p+1, s);
					 		//System.out.println("unused varName "+ varName);
					 	}
					 } else if((s = desc.indexOf("conflicting types for `")) != -1) { //$NON-NLS-1$
					 	int p = desc.indexOf("\'", s); //$NON-NLS-1$
					 	if (p != -1) {
					 		varName = desc.substring(desc.indexOf("`") + 1, p); //$NON-NLS-1$
					 		//System.out.println("confl varName "+ varName);
					 	}
					 } else if((s = desc.indexOf("previous declaration of `")) != -1) { //$NON-NLS-1$
					 	int p = desc.indexOf("\'", s); //$NON-NLS-1$
					 	if (p != -1) {
					 		varName = desc.substring(desc.indexOf("`") + 1, p); //$NON-NLS-1$
					 		//System.out.println("prev varName "+ varName);
					 	}
					 } else if ((s = desc.indexOf("parse error before ")) != -1) { //$NON-NLS-1$
						int p = desc.indexOf("\'", s); //$NON-NLS-1$
						if (p != -1) {
							varName = desc.substring(desc.indexOf("`") + 1, p); //$NON-NLS-1$
							//System.out.println("prev varName "+ varName);
						}
					 }

					/*
					 *	In file included from hello.c:3:
					 *	 c.h:2:15: missing ')' in macro parameter list
					 *
					 * We reconstruct the multiline gcc errors to multiple errors:
					 *    c.h:2:15: missing ')' in macro parameter list
					 *    hello.c:3:  in inclusion c.h:2:15
					 *     
					 */
					if (line.startsWith("In file included from ")) { //$NON-NLS-1$
						// We want the last error in the chain, so continue.
						eoParser.appendToScratchBuffer(line);
						return false;
					}

					/*
					 *	In file included from b.h:2,
					 *					 from a.h:3,
					 *					 from hello.c:3:
					 *	 c.h:2:15: missing ')' in macro parameter list
					 *
					 * We reconstruct the multiline gcc errors to multiple errors:
					 *    c.h:2:15: missing ')' in macro parameter list
					 *    b.h:2:  in inclusion c.h:3:15
					 *    a.h:3:  in inclusion b.h:2
					 *    hello.c:3:  in inclusion a.h:3
					 *     
					 */
					if (eoParser.getScratchBuffer().startsWith("In file included from ")) { //$NON-NLS-1$
						if (line.startsWith("from ")) { //$NON-NLS-1$
							// We want the last error in the chain, so continue.
							eoParser.appendToScratchBuffer(line);
							return false;
						}
						String buffer = eoParser.getScratchBuffer();
						eoParser.clearScratchBuffer();
						int from = -1;
						String inclusionError = fileName + ":" + num; //$NON-NLS-1$
						while ((from = buffer.indexOf("from ")) != -1) { //$NON-NLS-1$
							int coma = buffer.indexOf(',', from);
							String buf;
							if (coma != -1) {
								buf = buffer.substring(from + 5, coma) + ':';
								buffer = buffer.substring(coma);
							} else {
								buf = buffer.substring(from + 5);
								buffer = ""; //$NON-NLS-1$
							}
							String t = buf;
							buf += " in inclusion " + inclusionError; //$NON-NLS-1$
							inclusionError = t;
							// Call the parsing process again.
							processLine(buf, eoParser, extractSeverity(desc, inheritedSeverity));
						}
					}

					// The pattern is to generall we have to guard:
					// Before making this pattern a marker we do one more check
					// The fileName that we extract __must__ look like a valid file name.
					// We been having to much bad hits with patterns like
					//   /bin/sh ../libtool --mode=link gcc -version-info 0:1:0 foo.lo var.lo
					// Things like libtool that will fool the parser because of "0:1:0"
					if (!Path.EMPTY.isValidPath(fileName)) {
						return false;
					}
					IFile file = eoParser.findFileName(fileName);
					if (file != null) {
						if (eoParser.isConflictingName(fileName)) {
							desc = "[Conflicting names: " + fileName + " ] " + desc; //$NON-NLS-1$ //$NON-NLS-2$
							file = null;							
						}
					} else {
						file = eoParser.findFilePath(fileName);
						if (file == null) {
							// one last try before bailing out we may be in a wrong
							// directory.  This will happen, for example in the Makefile:
							// all: foo.c
							//    cd src3; gcc -c bar/foo.c
							// the user do a cd(1).
							IPath path = new Path(fileName);
							if (path.segmentCount() > 1) {
								String name = path.lastSegment();
								file = eoParser.findFileName(fileName);
								if (file != null) {
									if (eoParser.isConflictingName(fileName)) {
										desc = "[Conflicting names: " + name + " ] " + desc; //$NON-NLS-1$ //$NON-NLS-2$
										file = null;							
									}
								}
							}
						}
					}

					int severity = extractSeverity(desc, inheritedSeverity);
					if (desc.startsWith("warning") || desc.startsWith("Warning")) { //$NON-NLS-1$ //$NON-NLS-2$
						// Remove the warning.
						String d = desc.substring("warning".length()).trim(); //$NON-NLS-1$
						if (d.startsWith(":")) { //$NON-NLS-1$
							d = d.substring(1).trim();
						}

						if (d.length() != 0) {
							desc = d;
						}
					}
					
					// Display the fileName.
					if (file == null) {
						desc = desc +"[" + fileName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					}

					eoParser.generateMarker(file, num, desc, severity, varName);
				} else {
					if (line.startsWith("In file included from ")) { //$NON-NLS-1$
						eoParser.appendToScratchBuffer(line);
					} else if (line.startsWith("from ")) { //$NON-NLS-1$
						eoParser.appendToScratchBuffer(line);
					}
				}
			} catch (StringIndexOutOfBoundsException e) {
			} catch (NumberFormatException e) {
			}
		}
		return false;
	}
	
	private int extractSeverity(String desc, int defaultSeverity) {
		int severity = defaultSeverity; 
		if (desc.startsWith("warning") || desc.startsWith("Warning")) { //$NON-NLS-1$ //$NON-NLS-2$
			severity = IMarkerGenerator.SEVERITY_WARNING;
		}
		return severity;
	}
}
