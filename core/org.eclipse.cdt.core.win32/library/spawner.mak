# Microsoft Developer Studio Generated NMAKE File, Based on spawner.dsp
!IF "$(CFG)" == ""
CFG=spawner - Win32 Debug
!MESSAGE No configuration specified. Defaulting to spawner - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "spawner - Win32 Release" && "$(CFG)" != "spawner - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "spawner.mak" CFG="spawner - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "spawner - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "spawner - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "spawner - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\spawner.dll"


CLEAN :
	-@erase "$(INTDIR)\iostream.obj"
	-@erase "$(INTDIR)\raise.obj"
	-@erase "$(INTDIR)\spawner.obj"
	-@erase "$(INTDIR)\spawner.pch"
	-@erase "$(INTDIR)\StdAfx.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\Win32ProcessEx.obj"
	-@erase "$(OUTDIR)\spawner.dll"
	-@erase "$(OUTDIR)\spawner.exp"
	-@erase "$(OUTDIR)\spawner.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /Gz /MT /W3 /GX /O2 /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\spawner.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\spawner.pdb" /machine:I386 /out:"$(OUTDIR)\spawner.dll" /implib:"$(OUTDIR)\spawner.lib" 
LINK32_OBJS= \
	"$(INTDIR)\iostream.obj" \
	"$(INTDIR)\raise.obj" \
	"$(INTDIR)\spawner.obj" \
	"$(INTDIR)\StdAfx.obj" \
	"$(INTDIR)\Win32ProcessEx.obj"

"$(OUTDIR)\spawner.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "spawner - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\spawner.dll" "$(OUTDIR)\spawner.bsc"


CLEAN :
	-@erase "$(INTDIR)\iostream.obj"
	-@erase "$(INTDIR)\iostream.sbr"
	-@erase "$(INTDIR)\raise.obj"
	-@erase "$(INTDIR)\raise.sbr"
	-@erase "$(INTDIR)\spawner.obj"
	-@erase "$(INTDIR)\spawner.pch"
	-@erase "$(INTDIR)\spawner.sbr"
	-@erase "$(INTDIR)\StdAfx.obj"
	-@erase "$(INTDIR)\StdAfx.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(INTDIR)\Win32ProcessEx.obj"
	-@erase "$(INTDIR)\Win32ProcessEx.sbr"
	-@erase "$(OUTDIR)\spawner.bsc"
	-@erase "$(OUTDIR)\spawner.dll"
	-@erase "$(OUTDIR)\spawner.exp"
	-@erase "$(OUTDIR)\spawner.ilk"
	-@erase "$(OUTDIR)\spawner.lib"
	-@erase "$(OUTDIR)\spawner.map"
	-@erase "$(OUTDIR)\spawner.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /Gz /MD /W3 /Gm /GX /ZI /Od /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\spawner.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\iostream.sbr" \
	"$(INTDIR)\raise.sbr" \
	"$(INTDIR)\spawner.sbr" \
	"$(INTDIR)\StdAfx.sbr" \
	"$(INTDIR)\Win32ProcessEx.sbr"

"$(OUTDIR)\spawner.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\spawner.pdb" /map:"$(INTDIR)\spawner.map" /debug /machine:I386 /out:"$(OUTDIR)\spawner.dll" /implib:"$(OUTDIR)\spawner.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\iostream.obj" \
	"$(INTDIR)\raise.obj" \
	"$(INTDIR)\spawner.obj" \
	"$(INTDIR)\StdAfx.obj" \
	"$(INTDIR)\Win32ProcessEx.obj"

"$(OUTDIR)\spawner.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
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
!IF EXISTS("spawner.dep")
!INCLUDE "spawner.dep"
!ELSE 
!MESSAGE Warning: cannot find "spawner.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "spawner - Win32 Release" || "$(CFG)" == "spawner - Win32 Debug"
SOURCE=.\iostream.c

!IF  "$(CFG)" == "spawner - Win32 Release"

CPP_SWITCHES=/nologo /Gz /MT /W3 /GX /O2 /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /I "E:\Java\jdk1.3.1\include" /I "E:\Java\jdk1.3.1\include\Win32" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /FAcs /Fa"$(INTDIR)\\" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\iostream.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "spawner - Win32 Debug"

CPP_SWITCHES=/nologo /Gz /MD /W3 /Gm /GX /ZI /Od /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

"$(INTDIR)\iostream.obj"	"$(INTDIR)\iostream.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\raise.c

!IF  "$(CFG)" == "spawner - Win32 Release"


"$(INTDIR)\raise.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"


!ELSEIF  "$(CFG)" == "spawner - Win32 Debug"


"$(INTDIR)\raise.obj"	"$(INTDIR)\raise.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"


!ENDIF 

SOURCE=.\spawner.c

!IF  "$(CFG)" == "spawner - Win32 Release"

CPP_SWITCHES=/nologo /Gz /MT /W3 /GX /O2 /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /I "E:\Java\jdk1.3.1\include" /I "E:\Java\jdk1.3.1\include\Win32" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\spawner.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "spawner - Win32 Debug"

CPP_SWITCHES=/nologo /Gz /MD /W3 /Gm /GX /ZI /Od /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

"$(INTDIR)\spawner.obj"	"$(INTDIR)\spawner.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\StdAfx.c

!IF  "$(CFG)" == "spawner - Win32 Release"

CPP_SWITCHES=/nologo /Gz /MT /W3 /GX /O2 /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /I "E:\Java\jdk1.3.1\include" /I "E:\Java\jdk1.3.1\include\Win32" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /Fp"$(INTDIR)\spawner.pch" /Yc"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\StdAfx.obj"	"$(INTDIR)\spawner.pch" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "spawner - Win32 Debug"

CPP_SWITCHES=/nologo /Gz /MD /W3 /Gm /GX /ZI /Od /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\spawner.pch" /Yc"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

"$(INTDIR)\StdAfx.obj"	"$(INTDIR)\StdAfx.sbr"	"$(INTDIR)\spawner.pch" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\Win32ProcessEx.c

!IF  "$(CFG)" == "spawner - Win32 Release"

CPP_SWITCHES=/nologo /Gz /MT /W3 /GX /O2 /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /I "E:\Java\jdk1.3.1\include" /I "E:\Java\jdk1.3.1\include\Win32" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /FA /Fa"$(INTDIR)\Win32ProcessEx.asm-only" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\Win32ProcessEx.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "spawner - Win32 Debug"

CPP_SWITCHES=/nologo /Gz /MD /W3 /Gm /GX /ZI /Od /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\Win32" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_UNICODE" /D "_USRDLL" /D "SPAWNER_EXPORTS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\spawner.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

"$(INTDIR)\Win32ProcessEx.obj"	"$(INTDIR)\Win32ProcessEx.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\spawner.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 


!ENDIF 

