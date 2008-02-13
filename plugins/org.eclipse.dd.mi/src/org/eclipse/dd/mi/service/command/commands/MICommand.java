/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson 		  	- Modified for additional features in DSF Reference implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.command.ICommand;
import org.eclipse.dd.dsf.debug.service.command.ICommandResult;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.dd.mi.service.command.output.MIOutput;

/**
 * Represents any MI command.
 */
public class MICommand<V extends MIInfo> implements ICommand<V> {
    
    /*
     *  Variables.
     */
    final static String[] empty = new String[0];
    
    String[] fOptions = empty;
    String[] fParameters = empty;
    String   fOperation = new String();
    IDMContext fCtx;
    
    /*
     * Constructors.
     */
    
    /*public DsfMICommand(String operation) {
        this(operation, empty, empty);
    }*/
    
    public MICommand(IDMContext ctx, String operation) {
        this(ctx, operation, empty, empty);
    }
    
    /*public DsfMICommand(String operation, String[] options) {
        this(operation, options, empty);
    }*/
    
    public MICommand(IDMContext ctx, String operation, String[] options) {
        this(ctx, operation, options, empty);
    }
    
    public MICommand(IDMContext ctx, String operation, String[] options, String[] params) {
    	assert(ctx != null && DMContexts.getAncestorOfType(ctx, MIControlDMContext.class) != null);
    	fCtx = ctx;
        fOperation = operation;
        fOptions = options;
        fParameters = params;
    }
    
    public String getCommandControlFilter() {
        MIControlDMContext controlDmc = DMContexts.getAncestorOfType(getContext(), MIControlDMContext.class);
        return controlDmc.getCommandControlFilter();
    }
    
    /*
     * Returns the operation of this command.
     */
    public String getOperation() {
        return fOperation;
    }

    /*
     * Returns an array of command's options. An empty collection is 
     * returned if there are no options.
     */
    public String[] getOptions() {
        return fOptions;
    }

    public void setOptions(String[] options) {
        fOptions = options;
    }

    /*
     * Returns an array of command's parameters. An empty collection is 
     * returned if there are no parameters.
     */
    public String[] getParameters() {
        return fParameters;
    }

    public void setParameters(String[] params) {
        fParameters = params;
    }

    /*
     * Returns the constructed command.
     */
    public String constructCommand() {
        StringBuffer command = new StringBuffer(getOperation());
        String opt = optionsToString();
        if (opt.length() > 0) {
            command.append(' ').append(opt);
        }
        String p = parametersToString();
        if (p.length() > 0) {
            command.append(' ').append(p);
        }
        command.append('\n');
        return command.toString();
    }
    
//    /*
//     * Checks to see if the current command can be coalesced with the 
//     * supplied command.
//     */
//    public boolean canCoalesce( ICommand<? extends ICommandResult> command ) {
//        return false ;
//    }
    
    /*
     * Takes the supplied command and coalesces it with this one.
     * The result is a new third command which represent the two
     * original command.
     */
    public ICommand<? extends ICommandResult> coalesceWith( ICommand<? extends ICommandResult> command ) {
        return null ;
    }
    

    public IDMContext getContext(){
    	return fCtx;
    }
    /**
     * Produces the corresponding ICommandResult result for this
     * command. 
     * 
     * @return result for this command
     */
    public MIInfo getResult(MIOutput MIresult) {
        return ( new MIInfo(MIresult) );
    }
    
    protected String optionsToString() {
        String[] options = getOptions();
        StringBuffer sb = new StringBuffer();
        if (options != null && options.length > 0) {
            for (int i = 0; i < options.length; i++) {
                String option = options[i];
                // If the option argument contains " or \ it must be escaped
                if (option.indexOf('"') != -1 || option.indexOf('\\') != -1) {
                    StringBuffer buf = new StringBuffer();
                    for (int j = 0; j < option.length(); j++) {
                        char c = option.charAt(j);
                        if (c == '"' || c == '\\') {
                            buf.append('\\');
                        }
                        buf.append(c);
                    }
                    option = buf.toString();
                }

                // If the option contains a space according to
                // GDB/MI spec we must surround it with double quotes.
                if (option.indexOf('\t') != -1 || option.indexOf(' ') != -1) {
                    sb.append(' ').append('"').append(option).append('"');
                } else {
                    sb.append(' ').append(option);
                }
            }
        }
        return sb.toString().trim();
    }

    protected String parametersToString() {
        String[] parameters = getParameters();
        String[] options = getOptions();
        StringBuffer buffer = new StringBuffer();
        if (parameters != null && parameters.length > 0) {
            // According to GDB/MI spec
            // Add a "--" separator if any parameters start with "-"
            if (options != null && options.length > 0) {
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].startsWith("-")) { //$NON-NLS-1$
                        buffer.append('-').append('-');
                        break;
                    }
                }
            }

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < parameters.length; i++) {
                // We need to escape the double quotes and the backslash.
                sb.setLength(0);
                String param = parameters[i];
                for (int j = 0; j < param.length(); j++) {
                    char c = param.charAt(j);
                    if (c == '"' || c == '\\') {
                        sb.append('\\');
                    }
                    sb.append(c);
                }

                // If the string contains spaces instead of escaping
                // surround the parameter with double quotes.
                if (containsWhitespace(param)) {
                    sb.insert(0, '"');
                    sb.append('"');
                }
                buffer.append(' ').append(sb);
            }
        }
        return buffer.toString().trim();
    }

    protected boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Compare commands based on the MI command string that they generate, 
     * without the token.  
     */
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof MICommand<?>){
    	    MICommand<?> otherCmd = (MICommand<?>)obj;
        	return ((fCtx == null && otherCmd.fCtx == null) || (fCtx != null && fCtx.equals(otherCmd.fCtx))) &&   
        	       constructCommand().equals(otherCmd.constructCommand());
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
        return constructCommand().hashCode();
    }
    
    @Override
    public String toString() {
        return constructCommand();
    }
}
