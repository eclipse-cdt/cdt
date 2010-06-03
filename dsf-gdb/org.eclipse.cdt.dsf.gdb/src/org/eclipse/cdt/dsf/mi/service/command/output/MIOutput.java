/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for additional features in DSF Reference Implementation 
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI response.
 */
public class MIOutput {

    private final MIResultRecord rr;
    private final MIOOBRecord[] oobs;
	private MIStreamRecord[] streamRecords;

    public MIOutput() {
        this(null, (MIOOBRecord[])null);
    }

    /**
     * @param oob
     * @deprecated Use {@link #MIOutput(MIOOBRecord, MIStreamRecord[])} 
     */
    @Deprecated
	public MIOutput(MIOOBRecord oob) {
        this(null, new MIOOBRecord[] { oob });
    }

	/**
	 * Constructor used when handling a single out-of-band record
	 * 
	 * @param the
	 *            out-of-bound record
	 * @param streamRecords
	 *            any stream records that preceded the out-of-bound record,
	 *            since the last command result. This need not contain *all*
	 *            such records if it's been a while since the last command (for
	 *            practical reasons, there is a cap on the number of stream
	 *            records that are remembered). This will have the most recent
	 *            records. Must not be null; may be empty
	 * @since 3.0
	 */
    public MIOutput(MIOOBRecord oob, MIStreamRecord[] streamRecords) {
    	this(null, new MIOOBRecord[] { oob });
        this.streamRecords = streamRecords;
        assert streamRecords != null;
    }

	/**
	 * Constructor used when handling a command result.
	 * 
	 * @param rr
	 *            the result record
	 * @param oobs
	 *            any out-of-band records that preceded this particular command
	 *            result. This need not contain *all* such records if it's been
	 *            a while since the last command (for practical reasons, there
	 *            is a cap on the number of OOB records that are remembered).
	 *            This will have the most recent records.
	 * 
	 */
    public MIOutput(MIResultRecord rr, MIOOBRecord[] oobs) {
        this.rr = rr;
        this.oobs = oobs;
    }
    
    public MIResultRecord getMIResultRecord() {
        return rr;
    }

    public MIOOBRecord[] getMIOOBRecords() {
        return oobs;
    }

	/**
	 * See param in {@link #MIOutput(MIOOBRecord, MIStreamRecord[])}
	 * 
	 * @return Only instances created for an OOB record will have stream
	 *         records; may be an empty collection in that case, but not null.
	 *         Instances created for a command result will return null from this
	 *         method. Stream records can be retrieved from
	 *         {@link #getMIOOBRecords()} in that case.
	 * @since 3.0
	 */
    public MIStreamRecord[] getStreamRecords() {
    	return streamRecords;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < oobs.length; i++) {
            buffer.append(oobs[i].toString());
        }
        if (rr != null) {
            buffer.append(rr.toString());
        }
        return buffer.toString();
    }
}
