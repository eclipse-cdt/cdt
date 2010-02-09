/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;


/**
 * -trace-stop result.
 * 
 * The result of this command has the same fields as -trace-status, except that the 
 * 'supported' and 'running' fields are not output.
 * The output is therefore a subset of the output of the -trace-status command.  
 * The way MI fields are optional allows us to simply re-use the MITraceStatusInfo class
 * 
 * @since 3.0
 */
public class MITraceStopInfo extends MITraceStatusInfo {
	public MITraceStopInfo(MIOutput out) {
		super(out);
	}
}
