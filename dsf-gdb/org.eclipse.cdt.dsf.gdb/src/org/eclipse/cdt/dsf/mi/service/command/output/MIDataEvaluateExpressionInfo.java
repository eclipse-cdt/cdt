/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Mathias Kunter       - use MIConst.getString which is for human consumption (Bug 307311)
 *     Vladimir Prus (Mentor Graphics) - use MIResultRecord.getMIValue
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI Data evaluate expression parsing response.
 */
public class MIDataEvaluateExpressionInfo extends MIInfo {

    String fValue;

    public MIDataEvaluateExpressionInfo(MIOutput rr) {
        super(rr);
        fValue = ""; //$NON-NLS-1$
        if (isDone()) {
            MIOutput out = getMIOutput();
            MIResultRecord outr = out.getMIResultRecord();
            if (outr != null) {
                MIValue value = outr.getField("value"); //$NON-NLS-1$
                if (value instanceof MIConst) {
                    fValue = ((MIConst)value).getString();
                }
            }
        }
    }

    public String getValue() {
        return fValue;
    }
}
