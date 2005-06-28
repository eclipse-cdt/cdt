/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import java.math.BigInteger;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.RxThread;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser.GDBDerivedType;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser.GDBType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Instruction;
import org.eclipse.cdt.debug.mi.core.cdi.model.MixedInstruction;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ArrayType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.BoolType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.CharType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.DerivedType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.DoubleType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.EnumType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FloatType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FunctionType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.IntType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongLongType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.PointerType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ReferenceType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ShortType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.StructType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.Type;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.VoidType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.WCharType;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataDisassemble;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowDirectories;
import org.eclipse.cdt.debug.mi.core.command.CLIPType;
import org.eclipse.cdt.debug.mi.core.command.CLIWhatis;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;
import org.eclipse.cdt.debug.mi.core.output.MIDataDisassembleInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cdt.debug.mi.core.output.CLIPTypeInfo;
import org.eclipse.cdt.debug.mi.core.output.MISrcAsm;
import org.eclipse.cdt.debug.mi.core.output.CLIWhatisInfo;


/**
 */
public class SourceManager extends Manager {

	GDBTypeParser gdbTypeParser;

	public SourceManager(Session session) {
		super(session, false);
		gdbTypeParser = new GDBTypeParser();
	}

	public void setSourcePaths(Target target, String[] dirs) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIEnvironmentDirectory dir = factory.createMIEnvironmentDirectory(true, dirs);
		try {
			mi.postCommand(dir);
			dir.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public String[] getSourcePaths(Target target) throws CDIException {
		MISession mi = target.getMISession();
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

	public ICDIInstruction[] getInstructions(Target target, String filename, int linenum) throws CDIException {
		return getInstructions(target, filename, linenum, -1);
	}

	public ICDIInstruction[] getInstructions(Target target, String filename, int linenum, int lines) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataDisassemble dis = factory.createMIDataDisassemble(filename, linenum, lines, false);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MIAsm[] asm = info.getMIAsms();
			Instruction[] instructions = new Instruction[asm.length];
			for (int i = 0; i < instructions.length; i++) {
				instructions[i] = new Instruction(target, asm[i]);
			}
			return instructions;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public ICDIInstruction[] getInstructions(Target target, BigInteger start, BigInteger end) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x"; //$NON-NLS-1$
		String sa = hex + start.toString(16);
		String ea = hex + end.toString(16);
		MIDataDisassemble dis = factory.createMIDataDisassemble(sa, ea, false);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MIAsm[] asm = info.getMIAsms();
			Instruction[] instructions = new Instruction[asm.length];
			for (int i = 0; i < instructions.length; i++) {
				instructions[i] = new Instruction(target, asm[i]);
			}
			return instructions;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public ICDIMixedInstruction[] getMixedInstructions(Target target, String filename, int linenum) throws CDIException {
		return getMixedInstructions(target, filename, linenum, -1);
	}

	public ICDIMixedInstruction[] getMixedInstructions(Target target, String filename, int linenum, int lines) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataDisassemble dis = factory.createMIDataDisassemble(filename, linenum, lines, true);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MISrcAsm[] srcAsm = info.getMISrcAsms();
			ICDIMixedInstruction[] mixed = new ICDIMixedInstruction[srcAsm.length];
			for (int i = 0; i < mixed.length; i++) {
				mixed[i] = new MixedInstruction(target, srcAsm[i]);
			}
			return mixed;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public ICDIMixedInstruction[] getMixedInstructions(Target target, BigInteger start, BigInteger end) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x"; //$NON-NLS-1$
		String sa = hex + start.toString(16);
		String ea = hex + end.toString(16);
		MIDataDisassemble dis = factory.createMIDataDisassemble(sa, ea, true);
		try {
			mi.postCommand(dis);
			MIDataDisassembleInfo info = dis.getMIDataDisassembleInfo();
			MISrcAsm[] srcAsm = info.getMISrcAsms();
			ICDIMixedInstruction[] mixed = new ICDIMixedInstruction[srcAsm.length];
			for (int i = 0; i < mixed.length; i++) {
				mixed[i] = new MixedInstruction(target, srcAsm[i]);
			}
			return mixed;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public void update(Target target) throws CDIException {
	}

	public Type getType(Target target, String name) throws CDIException {
		if (name == null) {
			name = new String();
		}
		String typename = name.trim();

		// Parse the string.
		GDBType gdbType = gdbTypeParser.parse(typename);
		Type headType = null;
		Type type = null;

		// Convert the GDBType to an ICDIType.
		// So we go through the gdbType tree and reconstruct an ICDIType tree
		for (Type aType = null; gdbType != null; type = aType) {
			if (gdbType instanceof GDBDerivedType) {
				switch(gdbType.getType()) {
					case GDBType.ARRAY:
						int d = ((GDBDerivedType)gdbType).getDimension();
						aType = new ArrayType(target, gdbType.toString(), d);
					break;
					case GDBType.FUNCTION:
						aType = new FunctionType(target, gdbType.toString());
					break;
					case GDBType.POINTER:
						aType = new PointerType(target, gdbType.toString());
					break;
					case GDBType.REFERENCE:
						aType = new ReferenceType(target, gdbType.toString());
					break;
				}
				gdbType = ((GDBDerivedType)gdbType).getChild();
			} else {
				aType = toCDIType(target, gdbType.toString());
				gdbType = null;
			}
			if (type instanceof DerivedType) {
				((DerivedType)type).setComponentType(aType);
			}
			// Save the head to returning it.
			if (headType == null) {
				headType = aType;
			}
		}

		if (headType != null) {
			return headType;
		}
		throw new CDIException(CdiResources.getString("cdi.SourceManager.Unknown_type")); //$NON-NLS-1$
	}
	
	Type toCDIType(Target target, String name) throws CDIException {
		// Check the derived types and agregate types
		if (name == null) {
			name = new String();
		}
		String typename = name.trim();

		// Check the primitives.
		if (typename.equals("char")) { //$NON-NLS-1$
			return new CharType(target, typename);
		} else if (typename.equals("wchar_t")) { //$NON-NLS-1$
			return new WCharType(target, typename);
		} else if (typename.equals("short")) { //$NON-NLS-1$
			return new ShortType(target, typename);
		} else if (typename.equals("int")) { //$NON-NLS-1$
			return new IntType(target, typename);
		} else if (typename.equals("long")) { //$NON-NLS-1$
			return new LongType(target, typename);
		} else if (typename.equals("unsigned")) { //$NON-NLS-1$
			return new IntType(target, typename, true);
		} else if (typename.equals("signed")) { //$NON-NLS-1$
			return new IntType(target, typename);
		} else if (typename.equals("bool")) { //$NON-NLS-1$
			return new BoolType(target, typename);
		} else if (typename.equals("_Bool")) { //$NON-NLS-1$
			return new BoolType(target, typename);
		} else if (typename.equals("float")) { //$NON-NLS-1$
			return new FloatType(target, typename);
		} else if (typename.equals("double")) { //$NON-NLS-1$
			return new DoubleType(target, typename);
		} else if (typename.equals("void")) { //$NON-NLS-1$
			return new VoidType(target, typename);
		} else if (typename.equals("enum")) { //$NON-NLS-1$
			return new EnumType(target, typename);
		} else if (typename.equals("union")) { //$NON-NLS-1$
			return new StructType(target, typename);
		} else if (typename.equals("struct")) { //$NON-NLS-1$
			return new StructType(target, typename);
		} else if (typename.equals("class")) { //$NON-NLS-1$
			return new StructType(target, typename);
		}

		// GDB has some special types for int
		if (typename.equals("int8_t")) { //$NON-NLS-1$
			return new CharType(target, typename);
		} else if (typename.equals("int16_t")) { //$NON-NLS-1$
			return new ShortType(target, typename);
		} else if (typename.equals("int32_t")) { //$NON-NLS-1$
			return new LongType(target, typename);
		} else if (typename.equals("int64_t")) { //$NON-NLS-1$
			return new LongLongType(target, typename);
		} else if (typename.equals("int128_t")) { //$NON-NLS-1$
			return new IntType(target, typename); // ????
		}

		if (typename.equals("int8_t")) { //$NON-NLS-1$
			return new CharType(target, typename);
		} else if (typename.equals("uint8_t")) { //$NON-NLS-1$
			return new CharType(target, typename, true);
		} else if (typename.equals("int16_t")) { //$NON-NLS-1$
			return new ShortType(target, typename);
		} else if (typename.equals("uint16_t")) { //$NON-NLS-1$
			return new ShortType(target, typename, true);
		} else if (typename.equals("int32_t")) { //$NON-NLS-1$
			return new LongType(target, typename);
		} else if (typename.equals("uint32_t")) { //$NON-NLS-1$
			return new LongType(target, typename, true);
		} else if (typename.equals("int64_t")) { //$NON-NLS-1$
			return new LongLongType(target, typename);
		} else if (typename.equals("uint64_t")) { //$NON-NLS-1$
			return new LongLongType(target, typename, true);
		} else if (typename.equals("int128_t")) { //$NON-NLS-1$
			return new IntType(target, typename); // ????
		} else if (typename.equals("uint128_t")) { //$NON-NLS-1$
			return new IntType(target, typename, true); // ????			
		}

		StringTokenizer st = new StringTokenizer(typename);
		int count = st.countTokens();

		if (count == 2) {
			String first = st.nextToken();
			String second = st.nextToken();

			// ISOC allows permutations:
			// "signed int" and "int signed" are equivalent
			boolean isUnsigned =  (first.equals("unsigned") || second.equals("unsigned")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isSigned =    (first.equals("signed") || second.equals("signed")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isChar =      (first.equals("char") || second.equals("char")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isInt =       (first.equals("int") || second.equals("int")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isLong =      (first.equals("long") || second.equals("long")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isShort =     (first.equals("short") || second.equals("short")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isLongLong =  (first.equals("long") && second.equals("long")); //$NON-NLS-1$ //$NON-NLS-2$
			
			boolean isDouble =    (first.equals("double") || second.equals("double")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isFloat =     (first.equals("float") || second.equals("float")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isComplex =   (first.equals("complex") || second.equals("complex") || //$NON-NLS-1$ //$NON-NLS-2$
			                       first.equals("_Complex") || second.equals("_Complex")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isImaginery = (first.equals("_Imaginary") || second.equals("_Imaginary")); //$NON-NLS-1$ //$NON-NLS-2$

			boolean isStruct =     first.equals("struct"); //$NON-NLS-1$
			boolean isClass =      first.equals("class"); //$NON-NLS-1$
			boolean isUnion =      first.equals("union"); //$NON-NLS-1$
			boolean isEnum =       first.equals("enum"); //$NON-NLS-1$

			if (isChar && (isSigned || isUnsigned)) {
				return new CharType(target, typename, isUnsigned);
			} else if (isShort && (isSigned || isUnsigned)) {
				return new ShortType(target, typename, isUnsigned);
			} else if (isInt && (isSigned || isUnsigned)) {
				return new IntType(target, typename, isUnsigned);
			} else if (isLong && (isInt || isSigned || isUnsigned)) {
				return new LongType(target, typename, isUnsigned);
			} else if (isLongLong) {
				return new LongLongType(target, typename);
			} else if (isDouble && (isLong || isComplex || isImaginery)) {
				return new DoubleType(target, typename, isComplex, isImaginery, isLong);
			} else if (isFloat && (isComplex || isImaginery)) {
				return new FloatType(target, typename, isComplex, isImaginery);
			} else if (isStruct) {
				return new StructType(target, typename);
			} else if (isClass) {
				return new StructType(target, typename);
			} else if (isUnion) {
				return new StructType(target, typename);
			} else if (isEnum) {
				return new EnumType(target, typename);
			}
		} else if (count == 3) {
			// ISOC allows permutation. replace short by: long or short
			// "unsigned short int", "unsigned int short"
			// "short unsigned int". "short int unsigned"
			// "int unsinged short". "int short unsigned"
			//
			// "unsigned long long", "long long unsigned"
			// "signed long long", "long long signed"
			String first = st.nextToken();
			String second = st.nextToken();
			String third = st.nextToken();

			boolean isSigned =    (first.equals("signed") || second.equals("signed") || third.equals("signed")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			boolean unSigned =    (first.equals("unsigned") || second.equals("unsigned") || third.equals("unsigned")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			boolean isInt =       (first.equals("int") || second.equals("int") || third.equals("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			boolean isLong =      (first.equals("long") || second.equals("long") || third.equals("long")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			boolean isShort =     (first.equals("short") || second.equals("short") || third.equals("short")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			boolean isLongLong =  (first.equals("long") && second.equals("long")) || //$NON-NLS-1$ //$NON-NLS-2$
			                       (second.equals("long") && third.equals("long")); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isDouble =    (first.equals("double") || second.equals("double") || third.equals("double")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			boolean isComplex =   (first.equals("complex") || second.equals("complex") || third.equals("complex") || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                       first.equals("_Complex") || second.equals("_Complex") || third.equals("_Complex")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			boolean isImaginery = (first.equals("_Imaginary") || second.equals("_Imaginary") || third.equals("_Imaginary")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


			if (isShort && isInt && (isSigned || unSigned)) {
				return new ShortType(target, typename, unSigned);
			} else if (isLong && isInt && (isSigned || unSigned)) {
				return new LongType(target, typename, unSigned);
			} else if (isLongLong && (isSigned || unSigned)) {
				return new LongLongType(target, typename, unSigned);
			} else if (isDouble && isLong && (isComplex || isImaginery)) {
				return new DoubleType(target, typename, isComplex, isImaginery, isLong);
			}
		} else if (count == 4) {
			// ISOC allows permutation:
			// "unsigned long long int", "unsigned int long long"
			// "long long unsigned int". "long long int unsigned"
			// "int unsigned long long". "int long long unsigned"
			String first = st.nextToken();
			String second = st.nextToken();
			String third = st.nextToken();
			String fourth = st.nextToken();

			boolean unSigned = (first.equals("unsigned") || second.equals("unsigned") || third.equals("unsigned") || fourth.equals("unsigned")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			boolean isSigned = (first.equals("signed") || second.equals("signed") || third.equals("signed") || fourth.equals("signed")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			boolean isInt =    (first.equals("int") || second.equals("int") || third.equals("int") || fourth.equals("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			boolean isLongLong =   (first.equals("long") && second.equals("long")) //$NON-NLS-1$ //$NON-NLS-2$
				|| (second.equals("long") && third.equals("long")) //$NON-NLS-1$ //$NON-NLS-2$
				|| (third.equals("long") && fourth.equals("long")); //$NON-NLS-1$ //$NON-NLS-2$

			if (isLongLong && isInt && (isSigned || unSigned)) {
				return new LongLongType(target, typename, unSigned);
			}
		}
		throw new CDIException(CdiResources.getString("cdi.SourceManager.Unknown_type")); //$NON-NLS-1$
	}

	public String getDetailTypeNameFromVariable(StackFrame frame, String variable) throws CDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			return getDetailTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
	public String getDetailTypeName(Target target, String typename) throws CDIException {
		try {
			MISession mi = target.getMISession();
			RxThread rxThread = mi.getRxThread();
			rxThread.setEnableConsole(false);
			CommandFactory factory = mi.getCommandFactory();
			CLIPType ptype = factory.createCLIPType(typename);
			mi.postCommand(ptype);
			CLIPTypeInfo info = ptype.getMIPtypeInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			return info.getType();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			MISession mi = target.getMISession();
			RxThread rxThread = mi.getRxThread();
			rxThread.setEnableConsole(true);

		}
	}

	public String getTypeNameFromVariable(StackFrame frame, String variable) throws CDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			return getTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}

	public String getTypeName(Target target, String variable) throws CDIException {
		MISession miSession = target.getMISession();
		try {
			RxThread rxThread = miSession.getRxThread();
			rxThread.setEnableConsole(false);
			CommandFactory factory = miSession.getCommandFactory();
			CLIWhatis whatis = factory.createCLIWhatis(variable);
			miSession.postCommand(whatis);
			CLIWhatisInfo info = whatis.getMIWhatisInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			return info.getType();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			RxThread rxThread = miSession.getRxThread();
			rxThread.setEnableConsole(true);
		}
	}

}
