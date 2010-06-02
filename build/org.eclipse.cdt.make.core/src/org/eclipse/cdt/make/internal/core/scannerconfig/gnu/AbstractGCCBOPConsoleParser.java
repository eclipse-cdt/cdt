/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Gerhard Schaber (Wind River Systems) - bug 203059
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.BuildOutputProvider;
import org.eclipse.core.resources.IProject;

/**
 * Common stuff for all GNU build output parsers
 * 
 * @author vhirsl
 */
public abstract class AbstractGCCBOPConsoleParser implements IScannerInfoConsoleParser {
    private static final String[] COMPILER_INVOCATION = {
            "gcc", "g++", "cc", "c++" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    };
    protected static final String DASHIDASH= "-I-"; //$NON-NLS-1$
    protected static final String DASHI= "-I"; //$NON-NLS-1$
    protected static final String DASHD= "-D"; //$NON-NLS-1$
    
    private IProject project;
    private IScannerInfoCollector collector;
    
    private boolean bMultiline = false;
    private String sMultiline = ""; //$NON-NLS-1$

	private String[] fCompilerCommands;

    /**
     * @return Returns the project.
     */
    protected IProject getProject() {
        return project;
    }
    /**
     * @return Returns the collector.
     */
    protected IScannerInfoCollector getCollector() {
        return collector;
    }

    public void startup(IProject project, IScannerInfoCollector collector) {
        this.project = project;
        this.collector = collector;
        fCompilerCommands= computeCompilerCommands();
    }

    /**
     * Returns array of additional compiler commands to look for
     * 
     * @return String[]
     */
    private String[] computeCompilerCommands() {
    	if (project != null) {
	        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
	                getSCProfileInstance(project, ScannerConfigProfileManager.NULL_PROFILE_ID);
	        BuildOutputProvider boProvider = profileInstance.getProfile().getBuildOutputProviderElement();
	        if (boProvider != null) {
	            String compilerCommandsString = boProvider.getScannerInfoConsoleParser().getCompilerCommands();
	            if (compilerCommandsString != null && compilerCommandsString.length() > 0) {
	                String[] compilerCommands = compilerCommandsString.split(",\\s*"); //$NON-NLS-1$
	                if (compilerCommands.length > 0) {
	                    String[] compilerInvocation = new String[COMPILER_INVOCATION.length + compilerCommands.length];
	                    System.arraycopy(COMPILER_INVOCATION, 0, compilerInvocation, 0, COMPILER_INVOCATION.length);
	                    System.arraycopy(compilerCommands, 0, compilerInvocation, COMPILER_INVOCATION.length, compilerCommands.length);
	                    return compilerInvocation;
	                }
	            }
	        }
    	}
        return COMPILER_INVOCATION; 
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#processLine(java.lang.String)
     */
    public boolean processLine(String line) {
        boolean rc = false;
        int lineBreakPos = line.length()-1;
        char[] lineChars = line.toCharArray();
        while(lineBreakPos >= 0 && Character.isWhitespace(lineChars[lineBreakPos])) {
        	lineBreakPos--;
        }
        if (lineBreakPos >= 0) {
        	if (lineChars[lineBreakPos] != '\\'
        	    || (lineBreakPos > 0 && lineChars[lineBreakPos-1] == '\\')) {
        		lineBreakPos = -1;
        	}
        }
        // check for multiline commands (ends with '\')
        if (lineBreakPos >= 0) {
       		sMultiline += line.substring(0, lineBreakPos);
            bMultiline = true;
            return rc;
        }
        if (bMultiline) {
            line = sMultiline + line;
            bMultiline = false;
            sMultiline = ""; //$NON-NLS-1$
        }
        line= line.trim();
        TraceUtil.outputTrace("AbstractGCCBOPConsoleParser parsing line: [", line, "]");    //$NON-NLS-1$ //$NON-NLS-2$
        // make\[[0-9]*\]:  error_desc
        int firstColon= line.indexOf(':');
        String make = line.substring(0, firstColon + 1);
        if (firstColon != -1 && make.indexOf("make") != -1) { //$NON-NLS-1$
            boolean enter = false;
            String msg = line.substring(firstColon + 1).trim();     
            if ((enter = msg.startsWith(MakeMessages.getString("AbstractGCCBOPConsoleParser_EnteringDirectory"))) || //$NON-NLS-1$
                (msg.startsWith(MakeMessages.getString("AbstractGCCBOPConsoleParser_LeavingDirectory")))) { //$NON-NLS-1$
                int s = msg.indexOf('`');
                int e = msg.indexOf('\'');
                if (s != -1 && e != -1) {
                    String dir = msg.substring(s+1, e);
                    if (getUtility() != null) {
                        getUtility().changeMakeDirectory(dir, getDirectoryLevel(line), enter);
                    }
                    return rc;
                }
            }
        }
        // call sublclass to process a single line
        return processSingleLine(line.trim());
    }

    private int getDirectoryLevel(String line) {
        int s = line.indexOf('[');
        int num = 0;
        if (s != -1) {
            int e = line.indexOf(']');
            String number = line.substring(s + 1, e).trim();        
            try {
                num = Integer.parseInt(number);
            } catch (NumberFormatException exc) {
            }
        }
        return num;
    }

    protected abstract AbstractGCCBOPConsoleParserUtility getUtility();
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
     */
    public void shutdown() {
        if (getUtility() != null) {
            getUtility().reportProblems();
        }
    }
    
	/**
	 * Tokenizes a line into an array of commands. Commands are separated by 
	 * ';', '&&' or '||'. Tokens are separated by whitespace unless found inside
	 * of quotes, back-quotes, or double quotes.
	 * Outside of single-, double- or back-quotes a backslash escapes white-spaces, all quotes, 
	 * the backslash, '&' and '|'.
	 * A backslash used for escaping is removed.
	 * Quotes other than the back-quote plus '&&', '||', ';' are removed, also.
	 * @param line to tokenize
	 * @param escapeInsideDoubleQuotes if quotes need to be escaped [\"] in the resulting array of commands
	 * @return array of commands
	 */
	protected String[][] tokenize(String line, boolean escapeInsideDoubleQuotes) {
		ArrayList<String[]> commands= new ArrayList<String[]>();
		ArrayList<String> tokens= new ArrayList<String>();
		StringBuffer token= new StringBuffer();
		
		final char[] input= line.toCharArray();
		boolean nextEscaped= false;
		char currentQuote= 0;
		for (int i = 0; i < input.length; i++) {
			final char c = input[i];
			final boolean escaped= nextEscaped; nextEscaped= false;
			
			if (currentQuote != 0) {
				if (c == currentQuote) {
					if (escaped) {
						token.append(c);
					}
					else {
						if (c=='`') {
							token.append(c);	// preserve back-quotes
						}
						currentQuote= 0;
					}
				}
				else {
					if (escapeInsideDoubleQuotes && currentQuote == '"' && c == '\\') {
						nextEscaped= !escaped;
						if (escaped) {
							token.append(c);
						}
					}
					else {
						if (escaped) {
							token.append('\\');
						}
						token.append(c);
					}
				}
			}
			else {
				switch(c) {
				case '\\':
					if (escaped) {
						token.append(c);
					}
					else {
						nextEscaped= true;
					}
					break;
				case '\'': case '"': case '`':
					if (escaped) {
						token.append(c);
					}
					else {
						if (c == '`') {
							token.append(c);
						}
						currentQuote= c;
					}
					break;
				case ';':
					if (escaped) {
						token.append(c);
					}
					else {
						endCommand(token, tokens, commands);
					}
					break;
				case '&': case '|':
					if (escaped || i+1 >= input.length || input[i+1] != c) {
						token.append(c);
					}
					else {
						i++;
						endCommand(token, tokens, commands);
					}
					break;
					
				default:
					if (Character.isWhitespace(c)) {
						if (escaped) {
							token.append(c);
						}
						else {
							endToken(token, tokens);
						}
					}
					else {
						if (escaped) {
							token.append('\\');	// for windows put backslash back onto the token.
						}
						token.append(c);
					}
				}
			}
		}
		endCommand(token, tokens, commands);
		return commands.toArray(new String[commands.size()][]);
	}
	
	private void endCommand(StringBuffer token, ArrayList<String> tokens, ArrayList<String[]> commands) {
		endToken(token, tokens);
		if (!tokens.isEmpty()) {
			commands.add(tokens.toArray(new String[tokens.size()]));
			tokens.clear();
		}
	}
	private void endToken(StringBuffer token, ArrayList<String> tokens) {
		if (token.length() > 0) {
			tokens.add(token.toString());
			token.setLength(0);
		}
	}
	
    protected boolean processSingleLine(String line) {
    	boolean rc= false;
		String[][] tokens= tokenize(line, true);
		for (int i = 0; i < tokens.length; i++) {
			String[] command = tokens[i];
			if (processCommand(command)) {
				rc= true;
			}
			else {  // go inside quotes, if the compiler is called per wrapper or shell script
				for (int j = 0; j < command.length; j++) {
					String[][] subtokens= tokenize(command[j], true);
					for (int k = 0; k < subtokens.length; k++) {
						String[] subcommand = subtokens[k];
						if (subcommand.length > 1) {  // only proceed if there is any additional info
							if (processCommand(subcommand)) {
								rc= true;
							}
						}
					}
				}
			}
		}
		return rc;
    }
    
    protected int findCompilerInvocation(String[] tokens) {
    	for (int i = 0; i < tokens.length; i++) {
			final String token = tokens[i].toLowerCase();
    		final int searchFromOffset= Math.max(token.lastIndexOf('/'), token.lastIndexOf('\\')) + 1;
    		for (int j=0; j < fCompilerCommands.length; j++) {
    			if (token.indexOf(fCompilerCommands[j], searchFromOffset) != -1) {
    				return i;
    			}
    		}
    	}
    	return -1;
    }

    abstract protected boolean processCommand(String[] command);
}
