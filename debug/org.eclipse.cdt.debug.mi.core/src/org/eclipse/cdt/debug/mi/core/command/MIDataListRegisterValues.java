/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIDataListRegisterValuesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *       -data-list-register-values FMT [ ( REGNO )*]
 * 
 *    Display the registers' contents.  FMT is the format according to
 * which the registers' contents are to be returned, followed by an
 * optional list of numbers specifying the registers to display.  A
 * missing list of numbers indicates that the contents of all the
 * registers must be returned.
 *
 */
public class MIDataListRegisterValues extends MICommand 
{
	public final static int HEXADECIMAL = 0;
	public final static int OCTAL = 1;
	public final static int BINARY = 2;
	public final static int DECIMAL = 3;
	public final static int RAW = 4;
	public final static int NATURAL = 5;

	public MIDataListRegisterValues(int fmt) {
		this(fmt, null);
	}

	public MIDataListRegisterValues(int fmt, int [] regnos) {
		super("-data-list-register-values");

		String format = "x";
		switch (fmt) {
		case NATURAL:
			format = "N";
			break;
		case RAW:
			format = "r";
			break;
		case DECIMAL:
			format = "d";
			break;
		case BINARY:
			format = "t";
			break;
		case OCTAL:
			format = "o";
			break;
		/*
		case HEXADECIMAL:
		default:
			format = "x";
			break;
		*/
		}

		setOptions(new String[]{format});

		if (regnos != null && regnos.length > 0) {
			String[] array = new String[regnos.length];
			for (int i = 0; i < regnos.length; i++) {
				array[i] = Integer.toString(regnos[i]);
			}
			setParameters(array);
		}
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataListRegisterValuesInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
