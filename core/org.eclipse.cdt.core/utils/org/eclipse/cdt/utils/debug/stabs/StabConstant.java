/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.utils.debug.stabs;

public final class StabConstant {

	// Stab Symbol Types
	public final static int N_UNDF = 0x00;
	public final static int N_GSYM = 0x20;
	public final static int N_FNAME = 0x22;
	public final static int N_FUN = 0x24;
	public final static int N_STSYM = 0x26;
	public final static int N_LCSYM = 0x28;
	public final static int N_MAIN = 0x2a;
	public final static int N_ROSYM = 0x2c;
	public final static int N_PC = 0x30;
	public final static int N_NSYMS = 0x32;
	public final static int N_NOMAP = 0x34;
	public final static int N_OBJ = 0x38;
	public final static int N_OPT = 0x3c;
	public final static int N_RSYM = 0x40;
	public final static int N_M2C = 0x42;
	public final static int N_SLINE = 0x44;
	public final static int N_DSLINE = 0x46;
	public final static int N_BSLINE = 0x48;
	public final static int N_DEFD = 0x4a;
	public final static int N_FLINE = 0x4c;
	public final static int N_EHDECL = 0x50;
	public final static int N_CATCH = 0x54;
	public final static int N_SSYM = 0x60;
	public final static int N_ENDM = 0x62;
	public final static int N_SO = 0x64;
	public final static int N_LSYM = 0x80;
	public final static int N_BINCL = 0x82;
	public final static int N_SOL = 0x84;
	public final static int N_PSYM = 0xa0;
	public final static int N_EINCL = 0xa2;
	public final static int N_ENTRY = 0xa4;
	public final static int N_LBRAC = 0xc0;
	public final static int N_EXCL = 0xc2;
	public final static int N_SCOPE = 0xc4;
	public final static int N_RBRAC = 0xe0;
	public final static int N_BCOMM = 0xe2;
	public final static int N_ECOMM = 0xe4;
	public final static int N_ECOML = 0xe8;
	public final static int N_WITH = 0xea;
	public final static int N_NBTEXT = 0xef;
	public final static int N_NBDATA = 0xf2;
	public final static int N_NBBSS = 0xf4;
	public final static int N_NBSTS = 0xf6;
	public final static int N_NBLCS = 0xf8;

	public final static int SIZE = 12; // 4 + 1 + 1 + 2 + 4

	public static String type2String(int t) {
		switch (t) {
				case N_UNDF :
					return "UNDF";
				case N_GSYM :
					return "GSYM";
				case N_FNAME :
					return "FNAME";
				case N_FUN :
					return "FUN";
				case N_STSYM :
					return "STSYM";
				case N_LCSYM :
					return "LCSYM";
				case N_MAIN :
					return "MAIN";
				case N_ROSYM :
					return "ROSYM";
				case N_PC :
					return "PC";
				case N_NSYMS :
					return "SSYMS";
				case N_NOMAP :
					return "NOMAP";
				case N_OBJ :
					return "OBJ";
				case N_OPT :
					return "OPT";
				case N_RSYM :
					return "RSYM";
				case N_M2C :
					return "M2C";
				case N_SLINE :
					return "SLINE";
				case N_DSLINE :
					return "DSLINE";
				case N_BSLINE :
					return "BSLINE";
				case N_DEFD :
					return "DEFD";
				case N_FLINE :
					return "FLINE";
				case N_EHDECL :
					return "EHDECL";
				case N_CATCH :
					return "CATCH";
				case N_SSYM :
					return "SSYM";
				case N_ENDM :
					return "ENDM";
				case N_SO :
					return "SO";
				case N_LSYM :
					return "LSYM";
				case N_BINCL :
					return "BINCL";
				case N_SOL :
					return "SOL";
				case N_PSYM :
					return "PSYM";
				case N_EINCL :
					return "EINCL";
				case N_ENTRY :
					return "ENTRY";
				case N_LBRAC :
					return "LBRAC";
				case N_EXCL :
					return "EXCL";
				case N_SCOPE:
					return "SCOPE";
				case N_RBRAC :
					return "RBRAC";
				case N_BCOMM :
					return "COMM";
				case N_ECOMM :
					return "ECOMM";
				case N_ECOML :
					return "ECOML";
				case N_WITH :
					return "WITH";
				case N_NBTEXT :
					return "NBTEXT";
				case N_NBDATA :
					return "NBDATA";
				case N_NBBSS :
					return "NBBSS";
				case N_NBSTS :
					return "NBSTS";
				case N_NBLCS :
					return "NBLCS";
			}
			return "" + t;
	}
}
