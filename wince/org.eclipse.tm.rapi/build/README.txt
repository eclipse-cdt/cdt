Instructions for building the native library (jrapi.dll) for win32-x86
======================================================================

Requirements: 
  Windows 2000/XP/Vista,
  Visual Studio 2005 or newer, 
  Windows Mobile (PocketPC/Smartphone) SDK 5.0 or newer,
  JDK 1.4 or newer

Building the library:
  1. Open the solution file (jrapi.sln) in Visual Studio.
   
  2. Navigate to "Tools" -> "Options", and expand "Projects and Solutions", 
  and click "VC++ Directories".
    
  3. In "Show directories for:" select "Include files".
    
  4. Add the Activesync include directory from your Windows Mobile SDK:
  <WinMobile-SDK-path>\Activesync\Inc
  
  5. Add the JNI include directories from your JDK:
  <JDK-path>\include
  <JDK-path>\include\win32

  6. In "Show directories for:" select "Library files".
    
  7. Add the Activesync library directory from your Windows Mobile SDK:
  <WinMobile-SDK-path>\Activesync\Lib
        
  8. Navigate to "Build", and click "Rebuild Solution" to rebuild the library. 
  The output dll will be placed in \lib\os\win32\x86

Environment used for building the committed jrapi.dll:
  Windows 2000 SP4
  Visual Studio 2005 Standard Edition
  Windows Mobile 5.0 Pocket PC SDK
  JDK 1.4.2_10
