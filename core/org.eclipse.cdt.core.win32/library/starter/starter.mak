# Microsoft Developer Studio Generated NMAKE File, Based on starter.dsp
!IF "$(CFG)" == ""
CFG=starter - Win32 Debug
!MESSAGE No configuration specified. Defaulting to starter - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "starter - Win32 Release" && "$(CFG)" != "starter - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "starter.mak" CFG="starter - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "starter - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "starter - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "starter - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\starter.exe" "$(OUTDIR)\starter.bsc"


CLEAN :
	-@erase "$(INTDIR)\killer.obj"
	-@erase "$(INTDIR)\killer.sbr"
	-@erase "$(INTDIR)\starter.obj"
	-@erase "$(INTDIR)\starter.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\starter.bsc"
	-@erase "$(OUTDIR)\starter.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MD /W3 /GX /O2 /D "NDEBUG" /D "WIN32" /D "_CONSOLE" /D "_UNICODE" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\starter.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\starter.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\killer.sbr" \
	"$(INTDIR)\starter.sbr"

"$(OUTDIR)\starter.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\starter.pdb" /machine:I386 /out:"$(OUTDIR)\starter.exe" 
LINK32_OBJS= \
	"$(INTDIR)\killer.obj" \
	"$(INTDIR)\starter.obj"

"$(OUTDIR)\starter.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "starter - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\starter.exe"


CLEAN :
	-@erase "$(INTDIR)\killer.obj"
	-@erase "$(INTDIR)\starter.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\starter.exe"
	-@erase "$(OUTDIR)\starter.ilk"
	-@erase "$(OUTDIR)\starter.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MD /W3 /Gm /GX /ZI /Od /D "_DEBUG" /D "WIN32" /D "_CONSOLE" /D "_UNICODE" /Fp"$(INTDIR)\starter.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\starter.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /incremental:yes /pdb:"$(OUTDIR)\starter.pdb" /debug /machine:I386 /out:"$(OUTDIR)\starter.exe" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\killer.obj" \
	"$(INTDIR)\starter.obj"

"$(OUTDIR)\starter.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("starter.dep")
!INCLUDE "starter.dep"
!ELSE 
!MESSAGE Warning: cannot find "starter.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "starter - Win32 Release" || "$(CFG)" == "starter - Win32 Debug"
SOURCE=.\killer.cpp

!IF  "$(CFG)" == "starter - Win32 Release"


"$(INTDIR)\killer.obj"	"$(INTDIR)\killer.sbr" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "starter - Win32 Debug"


"$(INTDIR)\killer.obj" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\starter.cpp

!IF  "$(CFG)" == "starter - Win32 Release"


"$(INTDIR)\starter.obj"	"$(INTDIR)\starter.sbr" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "starter - Win32 Debug"


"$(INTDIR)\starter.obj" : $(SOURCE) "$(INTDIR)"


!ENDIF 


!ENDIF 

