/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarCreateInfo;


/**
 * 
 *    -var-create {NAME | "-"}
 *       {FRAME-ADDR | "*"} EXPRESSION
 *
 * This operation creates a variable object, which allows the
 * monitoring of a variable, the result of an expression, a memory cell or
 * a CPU register.
 *
 *   The NAME parameter is the string by which the object can be
 * referenced.  It must be unique.  If `-' is specified, the varobj system
 * will generate a string "varNNNNNN" automatically.  It will be unique
 * provided that one does not specify NAME on that format.  The command
 * fails if a duplicate name is found.
 *
 *  The frame under which the expression should be evaluated can be
 * specified by FRAME-ADDR.  A `*' indicates that the current frame should
 * be used.
 *
 *   EXPRESSION is any expression valid on the current language set (must
 * not begin with a `*'), or one of the following:
 *
 *  * `*ADDR', where ADDR is the address of a memory cell
 *
 *   * `*ADDR-ADDR' -- a memory address range (TBD)
 *
 *   * `$REGNAME' -- a CPU register name
 * 
 */
public class MIVarCreate extends MICommand<MIVarCreateInfo> 
{
    public MIVarCreate(IExpressionDMContext dmc, String expression) {
        this(dmc, "-", "*", expression); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public MIVarCreate(IExpressionDMContext dmc, String name, String expression) {
        this(dmc, name, "*", expression); //$NON-NLS-1$
    }

    public MIVarCreate(IExpressionDMContext dmc, String name, String frameAddr, String expression) {
        super(dmc, "-var-create", new String[]{name, frameAddr, expression}); //$NON-NLS-1$
    }
        
    @Override
    public MIVarCreateInfo getResult(MIOutput out)  {
        return new MIVarCreateInfo(out);
    }
}
