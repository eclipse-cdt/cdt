/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;


/**
 * GDB/MI show parsing.
 * (gdb) 
 * -data-evaluate-expression $_exitcode
 * ^done,value="10"
 * (gdb)
 */
public class MIGDBShowExitCodeInfo extends MIDataEvaluateExpressionInfo {

	public MIGDBShowExitCodeInfo(MIOutput o) {
		super(o);
	}

	public int getCode() {
		int code = 0;
		String exp = getExpression();
		try {
			code = Integer.parseInt(exp);
		} catch (NumberFormatException e) {
		}
		return code;
	}

}
