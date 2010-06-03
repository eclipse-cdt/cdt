/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson AB - Modified for new DSF Reference Implementation
 *     Ericsson AB - Reverted to byte[] and processed multi-line results
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;

/**
 * 
 * -data-read-memory result
 * 
 * (gdb)
 * nn-data-read-memory [command parameters]
 * nn^done,addr="ADDRESS",nr-bytes="NR_BYTES",total-bytes="TOTAL_BYTES",
 * next-row="NEXT_ROW",prev-row="PREV_ROW",next-page="NEXT_PAGE",
 * prev-page="PREV_PAGE",memory=[
 * {addr="addr1",data=["0x00","0x01", ...]},
 * {addr="addr2",data=["0x02","0x03", ...]},
 * {addr="addr3",data=["0x04","0x05", ...]},
 * ...]
 * (gdb)
 * 
 * where:
 * 
 * 'ADDRESS'
 *      Address (in hex) of the first byte fetched.
 *  
 * 'NR_BYTES'
 * 		Number of bytes read.
 *
 * 'TOTAL_BYTES'
 * 		Number of bytes requested (nr-rows * nr-columns * word-size).
 *
 * 'NEXT_ROW'
 * 		Address (in hex) of the next row.
 *
 * 'PREV_ROW'
 * 		Address (in hex) of the previous row.
 *
 * 'NEXT_PAGE'
 * 		Address (in hex) of the next page.
 *
 * 'PREV_PAGE'
 * 		Address (in hex) of the previous page.
 *
 * 'MEMORY'
 * 		Memory bytes retrieved, nr-rows of nr-columns words.
 * 
 */
public class MIDataReadMemoryInfo extends MIInfo {

	// The parsed values of interest
	BigInteger fAddress = new BigInteger("0"); //$NON-NLS-1$
	int fBytesRead;
	int fBytesRequested;
    MemoryByte[] fMemoryBlock;

    /**
     * Constructor
     * 
     * @param output
     */
    public MIDataReadMemoryInfo(MIOutput output) {

    	super(output);

    	fMemoryBlock = new MemoryByte[0];
        if (isDone()) {
        	parseResult(1);
        }
    }

    /**
     * Constructor
     * 
     * @param output
     */
    public MIDataReadMemoryInfo(MIOutput output, int word_size) {

    	super(output);

    	fMemoryBlock = new MemoryByte[0];
        if (isDone()) {
        	parseResult(word_size);
        }
    }

    /**
     *  Parse the back-end-result. The result is an array of the following form:
     *  
     *  [0] addr="address"
     *  [1] nr-bytes="x"
     *  [2] total-bytes="y"
     *  [3] next-row="address2"
     *  [4] prev-row="address3"
     *  [5] next-page="address4"
     *  [6] prev-page="address5"
     *  [7] memory=[{addr="addr1",data=["0x00","0x01",...]}]
     *  
     *  At this point, we only have interest in "memory".
     */
    private void parseResult(int word_size) {

		// Get the GDB/MI result record
    	MIOutput output = getMIOutput();
    	MIResultRecord record = output.getMIResultRecord();

    	// Parse the result record
    	if (record != null) {

    		// Parse the output results
        	// Note: we assume that the result respects the output format
    		//  i.e. nothing missing, nothing out of order.
    		MIResult[] results =  record.getMIResults();
    		for (int i = 0; i < results.length; i++) {

    			// Get the variable name
    			String var = results[i].getVariable();

    			// Parse 'addr="address"', the address of the first byte to read
    			if (var.equals("addr")) { //$NON-NLS-1$
    				MIValue value = results[i].getMIValue();
    				if (value instanceof MIConst) {
    					String address = ((MIConst) value).getCString();
    					fAddress = new BigInteger(address.substring(2), 16);	// Strip the "0x"
    				}
    			}

    			// Parse 'nr-bytes="x"', the number of bytes read
    			if (var.equals("total-bytes")) { //$NON-NLS-1$
    				MIValue value = results[i].getMIValue();
    				if (value instanceof MIConst) {
    					String size = ((MIConst) value).getCString();
    					fBytesRead = Integer.parseInt(size);
    				}
    			}

    			// Parse '"total-bytes="y"', the number of bytes requested
    			// Instantiate the corresponding output buffer with invalid bytes
    			if (var.equals("total-bytes")) { //$NON-NLS-1$
    				MIValue value = results[i].getMIValue();
    				if (value instanceof MIConst) {
    					String size = ((MIConst) value).getCString();
    					fBytesRequested = Integer.parseInt(size);
    					fMemoryBlock = new MemoryByte[fBytesRequested];
    					for (int j = 0; j < fMemoryBlock.length; j++)
    						fMemoryBlock[j] = new MemoryByte((byte) 0, (byte) 0);
    				}
    			}

    			// Parse 'memory=[{addr="addr1",data=["0x00","0x01",...]}]'
    			if (var.equals("memory")) { //$NON-NLS-1$
     				MIValue value = results[i].getMIValue();
    				if (value instanceof MIList) {
    					parseMemoryLines((MIList) value, word_size);
    				}
    			}
    		}
    	}
    }

    /**
     *  Parse the actual memory lines of the general form:
     *  
     *  [{addr="addr1",data=["0x00","0x01",...]}]
     *  [{addr="addr2",data=["0x00","0x01",...]}]
     *  
     *  Since we haven't implemented coalescing yet, we conveniently simplify
     *  the processing by assuming that the memory block address matches the
     *  one of the request. Therefore, we only have to fill the memoryBlock[]
     *  with the incoming bytes.
     *  
     *  This will have to be revisited as soon as we start considering
     *  multiple (and possibly canceled) requests.
     */
	private void parseMemoryLines(MIList lines, int word_size) {

		// Parse each line and append the results to the result block
		MIValue[] lineValues = lines.getMIValues();
		for (int i = 0; i < lineValues.length; i++) {

			// Each line has 2 tuples: "addr" and "data"
			if (lineValues[i] instanceof MITuple) {
				MITuple tuple = (MITuple) lineValues[i];
				MIResult[] results = tuple.getMIResults();

				// The offset of this particular output line in the result buffer
				int offset = 0;

				// The 1st tuple ('addr="addr1"') gives us the address of the first byte read
				MIValue addrValue = results[0].getMIValue();
				if (addrValue instanceof MIConst) {
					String address = ((MIConst) addrValue).getCString();
					BigInteger startAddress = new BigInteger(address.substring(2), 16);	// Strip the "0x"
					offset = startAddress.subtract(fAddress).intValue();
				}

				// The 2nd tuple ("data=[...]") gives us the actual bytes
				MIValue value = results[1].getMIValue();
				if (value instanceof MIList) {
					MIList list = (MIList) value;
					MIValue[] values = list.getMIValues();

					MemoryByte[] byteValues = new MemoryByte[values.length * word_size];

					// Parse the result array
					for (int j = 0; j < values.length; j++) {
						if (values[j] instanceof MIConst) {
							String str = ((MIConst) values[j]).getCString();
							try {
								long word = Long.decode(str.trim()).longValue();
								for (int k = 0; k < word_size; k++) {
									int bit_shift =  (word_size - k - 1) * 8;
									byteValues[j * word_size + k] = new MemoryByte((byte) ((word >> bit_shift) % 256));
								}
							} catch (NumberFormatException e) {
								for (int k = 0; k < word_size; k++)
									byteValues[j * word_size + k] = new MemoryByte((byte) -1, (byte) 0);
							}
						}
					}
					// Copy the parsed line to the memory block
					System.arraycopy(byteValues, 0, fMemoryBlock, offset, byteValues.length);
				}
			}
		}
	}

	/**
     *  Return the memory block
     */
    public MemoryByte[] getMIMemoryBlock() {
		return fMemoryBlock;
	}

}
