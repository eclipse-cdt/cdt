/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser.GDBDerivedType;
import org.eclipse.cdt.debug.mi.core.GDBTypeParser.GDBType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Instruction;
import org.eclipse.cdt.debug.mi.core.cdi.model.MixedInstruction;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
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
import org.eclipse.cdt.debug.mi.core.command.MIPType;
import org.eclipse.cdt.debug.mi.core.command.MIWhatis;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;
import org.eclipse.cdt.debug.mi.core.output.MIDataDisassembleInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIPTypeInfo;
import org.eclipse.cdt.debug.mi.core.output.MISrcAsm;
import org.eclipse.cdt.debug.mi.core.output.MIWhatisInfo;


/**
 */
public class SourceManager extends Manager implements ICDISourceManager {

	GDBTypeParser gdbTypeParser;

	public SourceManager(Session session) {
		super(session, false);
		gdbTypeParser = new GDBTypeParser();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#addSourcePaths(String[])
	 */
	public void addSourcePaths(String[] dirs) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		addSourcePaths(target, dirs);
	}
	public void addSourcePaths(Target target, String[] dirs) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = target.getMISession();
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
		Target target = (Target)getSession().getCurrentTarget();
		return getSourcePaths(target);
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

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getInstructions(String, int, int)
	 */
	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		return getInstructions(target, filename, linenum, lines);
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
		Target target = (Target)getSession().getCurrentTarget();
		return getInstructions(target, start, end);
	}
	public ICDIInstruction[] getInstructions(Target target, long start, long end) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x"; //$NON-NLS-1$
		String sa = hex + Long.toHexString(start);
		String ea = hex + Long.toHexString(end);
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

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getMixedInstructions(String, int, int)
	 */
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		return getMixedInstructions(target, filename, linenum, lines);
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
		Target target = (Target)getSession().getCurrentTarget();
		return getMixedInstructions(target, start, end);
	}
	public ICDIMixedInstruction[] getMixedInstructions(Target target, long start, long end) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String hex = "0x"; //$NON-NLS-1$
		String sa = hex + Long.toHexString(start);
		String ea = hex + Long.toHexString(end);
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

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#update()
	 */
	public void update() throws CDIException {
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
			return new IntType(target, typename);
		} else if (typename.equals("int128_t")) { //$NON-NLS-1$
			return new IntType(target, typename);
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

	public String getDetailTypeName(Target target, String typename) throws CDIException {
		try {
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIPType ptype = factory.createMIPType(typename);
			mi.postCommand(ptype);
			MIPTypeInfo info = ptype.getMIPtypeInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			return info.getType();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public String getTypeName(Target target, String variable) throws CDIException {
		try {
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIWhatis whatis = factory.createMIWhatis(variable);
			mi.postCommand(whatis);
			MIWhatisInfo info = whatis.getMIWhatisInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			return info.getType();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
