/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataDisassemble;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetAutoSolib;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowDirectories;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;
import org.eclipse.cdt.debug.mi.core.output.MIDataDisassembleInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowSolibSearchPathInfo;
import org.eclipse.cdt.debug.mi.core.output.MISrcAsm;


/**
 */
public class SourceManager extends SessionObject implements ICDISourceManager {

	public SourceManager(CSession session) {
		super(session);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#addSourcePaths(String[])
	 */
	public void addSourcePaths(String[] dirs) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIEnvironmentDirectory dir = factory.createMIEnvironmentDirectory(dirs);
		try {
			mi.postCommand(dir);
			dir.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getSourcePaths()
	 */
	public String[] getSourcePaths() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShowDirectories dir = factory.createMIGDBShowDirectories();
		try {
			mi.postCommand(dir);
			MIGDBShowDirectoriesInfo info = dir.getMIGDBShowDirectoriesInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public void setLibraryPaths(String[] libPaths) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetSolibSearchPath solib = factory.createMIGDBSetSolibSearchPath(libPaths);
		try {
			mi.postCommand(solib);
			solib.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public String[] getLibraryPaths() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShowSolibSearchPath dir = factory.createMIGDBShowSolibSearchPath();
		try {
			mi.postCommand(dir);
			MIGDBShowSolibSearchPathInfo info = dir.getMIGDBShowSolibSearchPathInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public void setAutoSolib() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetAutoSolib solib = factory.createMIGDBSetAutoSolib(true);
		try {
			mi.postCommand(solib);
			solib.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(String, int, int)
	 */
	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataDisassemble dis = factory.createMIDataDisassemble(filename, linenum, lines, false);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MIAsm[] asm = info.getMIAsms();
			Instruction[] instructions = new Instruction[asm.length];
			for (int i = 0; i < instructions.length; i++) {
				instructions[i] = new Instruction(getCSession().getCTarget(), asm[i]);
			}
			return instructions;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(String, int)
	 */
	public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
		return getInstructions(filename, linenum, -1);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(long, long)
	 */
	public ICDIInstruction[] getInstructions(long start, long end) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x";
		String sa = hex + Long.toHexString(start);
		String ea = hex + Long.toHexString(end);
		MIDataDisassemble dis = factory.createMIDataDisassemble(sa, ea, false);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MIAsm[] asm = info.getMIAsms();
			Instruction[] instructions = new Instruction[asm.length];
			for (int i = 0; i < instructions.length; i++) {
				instructions[i] = new Instruction(getCSession().getCTarget(), asm[i]);
			}
			return instructions;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(String, int, int)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataDisassemble dis = factory.createMIDataDisassemble(filename, linenum, lines, true);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MISrcAsm[] srcAsm = info.getMISrcAsms();
			ICDIMixedInstruction[] mixed = new ICDIMixedInstruction[srcAsm.length];
			for (int i = 0; i < mixed.length; i++) {
				mixed[i] = new MixedInstruction(getCSession().getCTarget(), srcAsm[i]);
			}
			return mixed;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(String, int)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
		return getMixedInstructions(filename, linenum, -1);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(long, long)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(long start, long end) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x";
		String sa = hex + Long.toHexString(start);
		String ea = hex + Long.toHexString(end);
		MIDataDisassemble dis = factory.createMIDataDisassemble(sa, ea, true);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MISrcAsm[] srcAsm = info.getMISrcAsms();
			ICDIMixedInstruction[] mixed = new ICDIMixedInstruction[srcAsm.length];
			for (int i = 0; i < mixed.length; i++) {
				mixed[i] = new MixedInstruction(getCSession().getCTarget(), srcAsm[i]);
			}
			return mixed;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
