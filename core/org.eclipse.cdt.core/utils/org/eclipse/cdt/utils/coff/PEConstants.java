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

package org.eclipse.cdt.utils.coff; 

public class PEConstants {

/* PE characteristics, FileHeader.f_flags.  */
public final static int IMAGE_FILE_RELOCS_STRIPPED         = 0x0001;
public final static int IMAGE_FILE_EXECUTABLE_IMAGE        = 0x0002;
public final static int IMAGE_FILE_LINE_NUMS_STRIPPED      = 0x0004;
public final static int IMAGE_FILE_LOCAL_SYMS_STRIPPED     = 0x0008;
public final static int IMAGE_FILE_AGGRESSIVE_WS_TRIM      = 0x0010;
public final static int IMAGE_FILE_LARGE_ADDRESS_AWARE     = 0x0020;
public final static int IMAGE_FILE_16BIT_MACHINE           = 0x0040;
public final static int IMAGE_FILE_BYTES_REVERSED_LO       = 0x0080;
public final static int IMAGE_FILE_32BIT_MACHINE           = 0x0100;
public final static int IMAGE_FILE_DEBUG_STRIPPED          = 0x0200;
public final static int IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP = 0x0400;
public final static int IMAGE_FILE_SYSTEM                  = 0x1000;
public final static int IMAGE_FILE_DLL                     = 0x2000;
public final static int IMAGE_FILE_UP_SYSTEM_ONLY          = 0x4000;
public final static int IMAGE_FILE_BYTES_REVERSED_HI       = 0x8000;

/* FileHader.f_magic.  Indicate the machine numbers.  */
public final static int IMAGE_FILE_MACHINE_UNKNOWN   = 0x0;
public final static int IMAGE_FILE_MACHINE_ALPHA     = 0x184;
public final static int IMAGE_FILE_MACHINE_ARM       = 0x1c0;
public final static int IMAGE_FILE_MACHINE_ALPHA64   = 0x284;
public final static int IMAGE_FILE_MACHINE_I386      = 0x14c;
public final static int IMAGE_FILE_MACHINE_IA64      = 0x200;
public final static int IMAGE_FILE_MACHINE_M68K      = 0x268;
public final static int IMAGE_FILE_MACHINE_MIPS16    = 0x266;
public final static int IMAGE_FILE_MACHINE_MIPSFPU   = 0x366;
public final static int IMAGE_FILE_MACHINE_MIPSFPU16 = 0x466;
public final static int IMAGE_FILE_MACHINE_POWERPC   = 0x1f0;
public final static int IMAGE_FILE_MACHINE_R3000     = 0x162;
public final static int IMAGE_FILE_MACHINE_R4000     = 0x166;
public final static int IMAGE_FILE_MACHINE_R10000    = 0x168;
public final static int IMAGE_FILE_MACHINE_SH3       = 0x1a2;
public final static int IMAGE_FILE_MACHINE_SH4       = 0x1a6;
public final static int IMAGE_FILE_MACHINE_THUMB     = 0x1c2;

/* OptionalHeader.magic  */
public final static int PE32     = 0x10b;
public final static int PE32PLUS = 0x20b;

/* Windows NT Subsystem. NTOptionalHeader.Subsystem  */
public final static int IMAGE_SUBSYSTEM_UNKNOWN			=  0;
public final static int IMAGE_SUBSYSTEM_NATIVE			=  1;
public final static int IMAGE_SUBSYSTEM_WINDOWS_GUI		=  2;
public final static int IMAGE_SUBSYSTEM_WINDOWS_CUI		=  3;
public final static int IMAGE_SUBSYSTEM_POSIX_CUI		=  7;
public final static int IMAGE_SUBSYSTEM_WINDOWS_CE_GUI		=  9;
public final static int IMAGE_SUBSYSTEM_EFI_APPLICATION		= 10;
public final static int IMAGE_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER	= 11;
public final static int IMAGE_SUBSYSTEM_EFI_RUNTIME_DRIVER	= 12;

/* DLL CHarcteristics, NTOptionalHeader.DLLCharcteristics */
public final static int IMAGE_DLLCHARACTERISTICS_NO_BIND         = 0x0800; // Do not bind image.
// Driver is a WDM Driver.
public final static int IMAGE_DLLCHARACTERISTICS_WDM_DRIVER      = 0x2000;
// Image is Terminal Sever aware.
public final static int IMAGE_DLLCHARACTERISTICS_TERMINAL_SERVER = 0x2000;

/* Array of Directories */
public final static int IMAGE_NUMBEROF_DIRECTORY_ENTRIES  = 16;

/* The directory of exported symbols; mostly used for DLLs.
   Described below.  */
public final static int IMAGE_DIRECTORY_ENTRY_EXPORT  = 0;
    
/* The directory of imported symbols; see below.  */
public final static int IMAGE_DIRECTORY_ENTRY_IMPORT = 1;
    
/* Directory of resources. Described below.  */
public final static int IMAGE_DIRECTORY_ENTRY_RESOURCE = 2;
    
/* Exception directory - structure and purpose unknown.  */
public final static int IMAGE_DIRECTORY_ENTRY_EXCEPTION = 3;
    
/* Security directory - structure and purpose unknown.  */
public final static int IMAGE_DIRECTORY_ENTRY_SECURITY = 4;
    
/* Base relocation table - see below.  */
public final static int IMAGE_DIRECTORY_ENTRY_BASERELOC = 5;
    
/* Debug directory - contents is compiler dependent. Moreover, many
   compilers stuff the debug information into the code section and
   don't create a separate section for it.  */
public final static int IMAGE_DIRECTORY_ENTRY_DEBUG = 6;
    
/* Description string - some arbitrary copyright note or the like.  */
public final static int IMAGE_DIRECTORY_ENTRY_COPYRIGHT = 7;
    
/* Machine Value (MIPS GP) - structure and purpose unknown.  */
public final static int IMAGE_DIRECTORY_ENTRY_GLOBALPTR = 8;
    
/* Thread local storage directory - structure unknown; contains
   variables that are declared "__declspec(thread)", i.e.
   per-thread global variables.  */
public final static int IMAGE_DIRECTORY_ENTRY_TLS = 9;

/* Load configuration directory - structure and purpose unknown.  */
public final static int IMAGE_DIRECTORY_ENTRY_LOAD_CONFIG = 10;
    
/* Bound import directory - see description of import directory.  */
public final static int IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT = 11;
    
/* Import Address Table - see description of import directory.  */
public final static int IMAGE_DIRECTORY_ENTRY_IAT = 12;
    
}
