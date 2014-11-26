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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI tuple value.
 */
public class MITuple extends MIValue {

    final static MIResult[] nullResults = new MIResult[0];
    final static MIValue[] nullValues = new MIValue[0];
                 MIResult[] results = nullResults;
                 MIValue[] values = nullValues;

    public MIResult[] getMIResults() {
        return results;
    }

    public void setMIResults(MIResult[] res) {
        results = res;
    }

    public MIValue[] getMIValues() {
        return values;
    }

    /**
	 * @since 4.6
	 */
    public MIValue getMIValue(String name) {
        for (MIResult r : results) {
            if (r.getVariable().equals(name)) {
                return r.getMIValue();
            }
        }
        return null;
    }

    public void setMIValues(MIValue[] vals) {
        values = vals;
    }

    @Override
    public String toString() {
        return toString("{", "}"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // Return comma-separated values, with start and end prepended and appended
    String toString(String start, String end)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(start);
        for (int i = 0; i < results.length; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(results[i].toString());
        }
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(values[i].toString());
        }
        buffer.append(end);
        return buffer.toString();
    }
}
