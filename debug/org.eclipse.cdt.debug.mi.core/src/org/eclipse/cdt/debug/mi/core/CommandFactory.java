package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.command.MIBreakAfter;
import org.eclipse.cdt.debug.mi.core.command.MIBreakCondition;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDelete;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDisable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakEnable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakInsert;
import org.eclipse.cdt.debug.mi.core.command.MIBreakList;
import org.eclipse.cdt.debug.mi.core.command.MIBreakWatch;
import org.eclipse.cdt.debug.mi.core.command.MIDataDisassemble;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIDataListChangedRegisters;
import org.eclipse.cdt.debug.mi.core.command.MIDataListRegisterNames;
import org.eclipse.cdt.debug.mi.core.command.MIDataListRegisterValues;
import org.eclipse.cdt.debug.mi.core.command.MIDataReadMemory;

/**
 *
 */
public class CommandFactory {

	public MIBreakAfter createMIBreakAfter(int brknum, int count) {
		return new MIBreakAfter(brknum, count);
	}
	
	public MIBreakCondition createMIBreakCondition (int brknum, String expr) {
		return new MIBreakCondition(brknum, expr);
	}

	public MIBreakDelete createMIBreakDelete (int[] brknum) {
		return new MIBreakDelete(brknum);
	}

	public MIBreakDisable createMIBreakDisable(int[] brknum) {
		return new MIBreakDisable(brknum);
	}

	public MIBreakEnable createMIBreakEnable(int[] brknum) {
		return new MIBreakEnable(brknum);
	}

	public MIBreakInsert createMIBreakInsert(boolean isTemporary, boolean isHardware,
						 String condition, int ignoreCount, String line) {
		return new MIBreakInsert(isTemporary, isHardware, condition, ignoreCount, line);
	}

	public MIBreakInsert createMIBreakInsert(String regex) {
		return new MIBreakInsert(regex);
	}

	public MIBreakList createMIBreakList() {
		return new MIBreakList();
	}

	public MIBreakWatch createMIBreakWatch(boolean access, boolean read, String expression) {
		return new MIBreakWatch(access, read, expression);
	}

	public MIDataDisassemble createMIDataDisassemble(String start, String end, boolean mixed) {
		return new MIDataDisassemble(start, end, mixed);
	}

	public MIDataDisassemble createMIDataDisassemble(String file, int linenum, int lines, boolean mixed) {
		return new MIDataDisassemble(file, linenum, lines, mixed);
	}

	public MIDataEvaluateExpression createMIDataEvaluateExpression(String expression) {
		return new MIDataEvaluateExpression(expression);
	}

	public MIDataListChangedRegisters createMIDataListChangedRegisters() {
		return new MIDataListChangedRegisters();
	}

	public MIDataListRegisterNames createMIDataListRegisterNames(int[] regno) {
		return new MIDataListRegisterNames(regno);
	}

	public MIDataListRegisterValues createMIDataLIstRegisterValues(int fmt, int[] regno) {
		return new MIDataListRegisterValues(fmt, regno);
	}

	public MIDataReadMemory createMIDataReadMemory(int offset, String address,
							String wordFormat, int wordSize,
							int rows, int cols, Character asChar) {
		return new MIDataReadMemory(offset, address, wordFormat, wordSize,
						rows, cols, asChar);
	}
}
