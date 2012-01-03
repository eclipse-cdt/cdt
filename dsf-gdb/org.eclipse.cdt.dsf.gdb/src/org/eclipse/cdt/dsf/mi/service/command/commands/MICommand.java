/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson 		  	- Modified for additional features in DSF Reference implementation and bug 219920
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * Represents any MI command.
 */
public class MICommand<V extends MIInfo> implements ICommand<V> {
    
    /*
     *  Variables.
     */
    final static String[] empty = new String[0];
    
    List<Adjustable> fOptions = new ArrayList<Adjustable>();
    List<Adjustable> fParameters = new ArrayList<Adjustable>();
    String   fOperation = new String();
    IDMContext fCtx;
    
    /*
     * Constructors.
     */

    public MICommand(IDMContext ctx, String operation) {
        this(ctx, operation, empty, empty);
    }
    
    public MICommand(IDMContext ctx, String operation, String[] params) {
        this(ctx, operation, empty, params);
    }
    
    public MICommand(IDMContext ctx, String operation, String[] options, String[] params) {
    	assert(ctx != null && DMContexts.getAncestorOfType(ctx, MIControlDMContext.class) != null);
    	fCtx = ctx;
        fOperation = operation;
        fOptions = optionsToAdjustables(options);
        fParameters = parametersToAdjustables(params);
    }

	private final List<Adjustable> optionsToAdjustables(String[] options) {
		List<Adjustable> result = new ArrayList<Adjustable>();
		if (options != null) {
			for (String option : options) {
				result.add(new MIStandardOptionAdjustable(option));
			}
		}
		return result;
	}
    
	private final List<Adjustable> parametersToAdjustables(String[] parameters) {
		List<Adjustable> result = new ArrayList<Adjustable>();
		if (parameters != null) {
			for (String parameter : parameters) {
				result.add(new MIStandardParameterAdjustable(parameter));
			}
		}
		return result;
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
		List<String> result = new ArrayList<String>();
		for (Adjustable option : fOptions) {
			result.add(option.getValue());
		}
		return result.toArray(new String[fOptions.size()]);
    }

    public void setOptions(String[] options) {
    	fOptions = optionsToAdjustables(options);
    }

    /*
     * Returns an array of command's parameters. An empty collection is 
     * returned if there are no parameters.
     */
    public String[] getParameters() {
		List<String> result = new ArrayList<String>();
		for (Adjustable parameter : fParameters) {
			result.add(parameter.getValue());
		}
		return result.toArray(new String[fParameters.size()]);
    }

    public void setParameters(String[] params) {
		fParameters = parametersToAdjustables(params);
    }

	public void setParameters(Adjustable... params) {
		fParameters = Arrays.asList(params);
	}
    
    /*
     * Returns the constructed command without using the --thread/--frame options.
     */
    public String constructCommand() {
    	return constructCommand(null, -1);
    }
    /*
     * Returns the constructed command potentially using the --thread/--frame options.
     */
    /**
     * @since 1.1
     */
    public String constructCommand(String threadId, int frameId) {
    	return constructCommand(null, threadId, frameId);
    }

    /**
     * With GDB 7.1 the --thread-group option is used to support multiple processes.
     * @since 4.0
     */
    public String constructCommand(String groupId, String threadId, int frameId) {
        StringBuffer command = new StringBuffer(getOperation());
        
        // Add the --thread option
        if (supportsThreadAndFrameOptions() && threadId != null && threadId.trim().length() > 0) {
        	command.append(" --thread " + threadId); //$NON-NLS-1$

        	// Add the --frame option, but only if we are using the --thread option
        	if (frameId >= 0) {
        		command.append(" --frame " + frameId); //$NON-NLS-1$
        	}
        } else if (supportsThreadGroupOption() && groupId != null && groupId.trim().length() > 0) {
        	// The --thread-group option is only allowed if we are not using the --thread option
        	command.append(" --thread-group " + groupId); //$NON-NLS-1$
        }

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
	@Override
    public ICommand<? extends ICommandResult> coalesceWith( ICommand<? extends ICommandResult> command ) {
        return null ;
    }
    

	@Override
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
		StringBuffer sb = new StringBuffer();
		if (fOptions != null && fOptions.size() > 0) {
			for (Adjustable option : fOptions) {
				sb.append(option.getAdjustedValue());
			}
		}
		return sb.toString().trim();
    }

    protected String parametersToString() {
		String[] options = getOptions();
		StringBuffer buffer = new StringBuffer();
		if (fParameters != null && fParameters.size() > 0) {
			// According to GDB/MI spec
			// Add a "--" separator if any parameters start with "-"
			if (options != null && options.length > 0) {
				for (Adjustable parameter : fParameters) {
					if (parameter.getValue().startsWith("-")) {//$NON-NLS-1$
						buffer.append('-').append('-');
						break;
					}
				}
			}

			for (Adjustable parameter : fParameters) {
				buffer.append(' ').append(parameter.getAdjustedValue());
			}
		}
		return buffer.toString().trim();
    }

    protected static boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @since 1.1
     */
    public boolean supportsThreadAndFrameOptions() { return true; }

    /**
     * @since 4.0
     */
    public boolean supportsThreadGroupOption() { return true; }
    
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

	public static class MIStandardOptionAdjustable extends MICommandAdjustable {

		public MIStandardOptionAdjustable(String option) {
			super(option);
		}

		@Override
		public String getAdjustedValue() {
			StringBuilder builder = new StringBuilder();
			String option = value;
			// If the option argument contains " or \ it must be escaped
			if (option.indexOf('"') != -1 || option.indexOf('\\') != -1) {
				StringBuilder buf = new StringBuilder();
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
				builder.append(' ').append('"').append(option).append('"');
			} else {
				builder.append(' ').append(option);
			}
			return builder.toString();
		}
	}

	public static class MIStandardParameterAdjustable extends
			MICommandAdjustable {
		public MIStandardParameterAdjustable(String parameter) {
			super(parameter);
		}

		@Override
		public String getAdjustedValue() {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; j < value.length(); j++) {
				char c = value.charAt(j);
				if (c == '"' || c == '\\') {
					builder.append('\\');
				}
				builder.append(c);
			}

			// If the string contains spaces instead of escaping
			// surround the parameter with double quotes.
			if (containsWhitespace(value)) {
				builder.insert(0, '"');
				builder.append('"');
			}

			return builder.toString();
		}
	}

	public static abstract class MICommandAdjustable implements Adjustable {

		protected final String value;

		/**
		 * Creates a new instance.
		 * 
		 * @param builder
		 *            The string builder is an optimization option, if two
		 *            commands are not processed at the same time a shared
		 *            builder can be used to save memory.
		 * @param value
		 *            The value that should be adjusted.
		 */
		public MICommandAdjustable(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}
	}
}
