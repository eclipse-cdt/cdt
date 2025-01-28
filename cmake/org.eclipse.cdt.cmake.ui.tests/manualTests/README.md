## Overview

This document captures the manual tests that should be completed on the
CMake setting.

## Test cases
The following test cases use a Launch Target set to Local.

### 1) Operating system defaults used
#### 1.1) Launch Bar Launch Mode=Run, CMake Settings > "Use default CMake settings"=checked
  Expected: Build uses default generator (Ninja)
#### 1.2) Launch Bar Launch Mode=Debug, CMake Settings > "Use default CMake settings"=checked
  Expected: Build uses default generator (Ninja)

### 2) Build Settings specific generator used:
Note, the Build Settings tab settings are stored separately for Run mode and Debug mode.
#### 2.1) Launch Bar Launch Mode=Run, CMake Settings > "Use default CMake settings"=unchecked, Generator=Unix Makefiles
  Expected: Build uses generator Unix Makefiles
#### 2.2) Launch Bar Launch Mode=Debug, CMake Settings > "Use default CMake settings"=unchecked, Generator=Unix Makefiles
  Expected: Build uses generator Unix Makefiles
#### 2.3) Build Settings are remembered
#### 2.4) Build Settings for Run mode can be different to settings stored for Debug

### 3) Build Settings specific Additional CMake arguments:  
#### 3.1) CMake Settings > "Use default CMake settings"=unchecked, Additional CMake arguments are used during build
#### 3.2) CMake Settings > "Use default CMake settings"=checked, Additional CMake arguments are NOT used during build and text box is blank

### 4) Build Settings specific Build command:  
#### 4.1) Enter a custom build command, such as the full path to cmake, and run build
  Expected: the custom cmake command is used

### 5) Build Settings specific all target:  
#### 5.1) Enter a custom all target, such as `helloworld`, and run build
  Expected: the custom target is used in the cmake command line
  
### 6) Build Settings specific clean target:
#### 6.1) Enter a custom clean target, such as `help`, and clean project
  Expected: the custom target is used in the cmake command line

### 8) Restart Eclipse
#### 8.1) Perform build, clean and open build settings
  Expected: Settings are persisted

## Setup & prerequisites
### Setup Host
  Note, these instructions do not require the following tools to be added to the system path environment variable in the OS before starting Eclipse. This allows a clean environment to be maintained.

- Install Eclipse/CDT on host.
- Install gcc toolchain and make tools on host.
  - On Windows I used msys64 (https://www.msys2.org/), which contains mingw64.
- Install CMake and Ninja on host.

### In Eclipse, setup tool paths.

#### Toolchain
  When using a recognised gcc toolchain (mingw64 is one of these), CDT automatically registers the toolchain for use within the workbench.
  * To check if the toolchain is registered, open Preferences > C/C++ > Core Build Toolchains. In the Available Toolchains list, check if it contains the toolchain you installed on the host.

For example, when using mingw64 the following toolchain is available:

    Type Name                                       OS    Arch 
    GCC  win32 x86_64 C:\msys64\mingw64\bin\gcc.exe win32 x86_64
Otherwise, register your toolchain by clicking Add... in the User Defined Toolchains list.

#### CMake & Ninja
*  Open Preferences > C/C++ > Build > Environment and click Add...

  In Name enter "PATH" and in Value enter the path to CMake and Ninja, for example 
  
  `C:\Program Files\CMake\bin;C:\Ninja\bin`


  You probably want to make sure "Append variables to native environment" (default) is selected.

#### Create a CMake project
* In Eclipse, choose File > C/C++ Project.
*  In the New C/C++ Project wizard, choose CMake in the left hand side sash and then CMake Project and click Next.
*  Enter a project name, for example helloworld and click Finish.
*  In the Project Explorer, expand the generated project. It contains 3 files :
 
    helloworld.cpp

    CMakeLists.txt

    config.h.in
