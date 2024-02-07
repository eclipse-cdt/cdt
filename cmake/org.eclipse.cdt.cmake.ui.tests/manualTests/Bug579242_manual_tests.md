Bug 579242 - Ninja generator not connected

## Overview
Tests that it is possible to control the CMake build using the Launch Bar Launch Configuration > Build Settings tab.
The Build Settings page allows the user to control:

  * "Use these settings" checkbox allows user to use these settings instead of operating system defaults.
  * Generator "Unix Makefiles" or "Ninja" radio group allows choice of CMake generator.
  * "Additional CMake arguments" allows variable=value arguments to be passed to CMake CACHE entry to customise settings for the project.

## Test cases
The following test cases use a Launch Target set to Local.

### 1) Operating system defaults used
#### 1.1) Launch Bar Launch Mode=Run, CMake Settings > "Use these settings"=unchecked
  Expected: Build uses default generator (win32: -G MinGW Makefiles)
#### 1.2) Launch Bar Launch Mode=Debug, CMake Settings > "Use these settings"=unchecked
  Expected: Build uses default generator (win32: -G MinGW Makefiles)

### 2) Build Settings specific generator used:
Note, the Build Settings tab settings are stored separately for Run mode and Debug mode.
#### 2.1) Launch Bar Launch Mode=Run, CMake Settings > Use these settings=checked, Generator=Ninja
  Expected: Build uses generator Ninja
#### 2.2) Launch Bar Launch Mode=Debug, CMake Settings > Use these settings=checked, Generator=Unix Makefiles
  Expected: Build uses generator Unix Makefiles
#### 2.3) Build Settings are remembered
#### 2.4) Build Settings for Run mode can be different to settings stored for Debug

### 3) Build Settings specific Additional CMake arguments:  
#### 3.1) CMake Settings > Use these settings=checked, Additional CMake arguments are used during build
#### 3.2) CMake Settings > Use these settings=unchecked, Additional CMake arguments are NOT used during build

### 4) Build Settings specific Build and Clean command:  
Note, not tested. These may be removed in future.

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


## Test Execution
### 20240129 Windows 10. All tests pass.

### Setup
  Create CMake Project called helloworld.
### 1) Operating system defaults used
#### 1.1) Launch Bar Launch Mode=Run, CMake Settings > "Use these settings"=unchecked
  * Set Launch Bar Launch Mode to Run.
  * Set CMake Settings > "Use these settings" to unchecked.
  * On the Launch Bar, click Build button.
  
Expected: Build uses default generator (win32: -G MinGW Makefiles)

Actual:

    Configuring in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\default
    cmake -G MinGW Makefiles -DCMAKE_EXPORT_COMPILE_COMMANDS=ON C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld
    -- The C compiler identification is GNU 11.2.0
    -- The CXX compiler identification is GNU 11.2.0
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/mingw64/bin/cc.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/mingw64/bin/c++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (3.1s)
    -- Generating done (0.0s)
    -- Build files have been written to: C:/Users/betamax/cdt-only2/runtime-New_configuration(1)/helloworld/build/default
    Building in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\default
    cmake --build . --target all
    [ 50%] Building CXX object CMakeFiles/helloworld.dir/helloworld.cpp.obj
    [100%] Linking CXX executable helloworld.exe
    [100%] Built target helloworld
    Build complete (0 errors, 0 warnings): C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\default

#### 1.2) Launch Bar Launch Mode=Debug, CMake Settings > "Use these settings"=unchecked
  * Set Launch Bar Launch Mode to Debug.
  * Make sure CMake Settings > "Use these settings" to unchecked.
  * On the Launch Bar, click Build button.
  
Expected: Build uses default generator (win32: -G MinGW Makefiles)

Actual:

    Configuring in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake -G MinGW Makefiles -DCMAKE_EXPORT_COMPILE_COMMANDS=ON C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld
    -- The C compiler identification is GNU 11.2.0
    -- The CXX compiler identification is GNU 11.2.0
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/mingw64/bin/cc.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/mingw64/bin/c++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (2.8s)
    -- Generating done (0.0s)
    -- Build files have been written to: C:/Users/betamax/cdt-only2/runtime-New_configuration(1)/helloworld/build/cmake.debug.win32.x86_64
    Building in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake --build . --target all
    [ 50%] Building CXX object CMakeFiles/helloworld.dir/helloworld.cpp.obj
    [100%] Linking CXX executable helloworld.exe
    [100%] Built target helloworld
    Build complete (0 errors, 0 warnings): C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64


### 2) Build Settings specific generator used:
#### 2.1) Launch Bar Launch Mode=Run, CMake Settings > Use these settings=checked, Generator=Ninja
  * Set Launch Bar Launch Mode to Run.
  * Set CMake Settings > "Use these settings" to checked.
  * Make sure CMake Settings > Generator is set to Ninja.
  * In Project Explorer, delete the build directory.
  * On the Launch Bar, click Build button.

Expected: Build uses generator Ninja

Actual:

    Configuring in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\default
    cmake -G Ninja  -DCMAKE_EXPORT_COMPILE_COMMANDS=ON C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld
    -- The C compiler identification is GNU 11.2.0
    -- The CXX compiler identification is GNU 11.2.0
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/mingw64/bin/cc.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/mingw64/bin/c++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (2.3s)
    -- Generating done (0.0s)
    -- Build files have been written to: C:/Users/betamax/cdt-only2/runtime-New_configuration(1)/helloworld/build/default
    Building in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\default
    cmake --build . --target all
    [1/2] Building CXX object CMakeFiles/helloworld.dir/helloworld.cpp.obj
    [2/2] Linking CXX executable helloworld.exe
    Build complete (0 errors, 0 warnings): C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\default

#### 2.2) Launch Bar Launch Mode=Debug, CMake Settings > Use these settings=checked, Generator=Unix Makefiles
  * Set Launch Bar Launch Mode to Debug.
  * Set CMake Settings > "Use these settings" to checked.
  * Make sure CMake Settings > Generator is set to Unix Makefiles.
  * On the Launch Bar, click Build button.

Expected: Build uses generator Unix Makefiles

Actual:

    Configuring in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake -G Unix Makefiles  -DCMAKE_EXPORT_COMPILE_COMMANDS=ON C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld
    -- The C compiler identification is GNU 11.2.0
    -- The CXX compiler identification is GNU 11.2.0
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/mingw64/bin/cc.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/mingw64/bin/c++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (3.3s)
    -- Generating done (0.0s)
    -- Build files have been written to: C:/Users/betamax/cdt-only2/runtime-New_configuration(1)/helloworld/build/cmake.debug.win32.x86_64
    Building in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake --build . --target all
    [ 50%] [32mBuilding CXX object CMakeFiles/helloworld.dir/helloworld.cpp.obj[0m
    [100%] [32m[1mLinking CXX executable helloworld.exe[0m
    [100%] Built target helloworld
    Build complete (0 errors, 0 warnings): C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64


#### 2.3) Build Settings are remembered
  * With Launch Bar Launch Mode still set to Debug.
  * Open CMake Settings > "Use these settings".

Expected:

    checked.

Actual:

    checked.

#### 2.4) Build Settings for Run mode can be different to settings stored for Debug
  * Make sure Launch Bar Launch Mode still set to Debug.
  * Set CMake Settings > "Use these settings" to unchecked.
  * Set Launch Bar Launch Mode to Run.
  * Open CMake Settings > "Use these settings".

Expected:

    checked.

Actual:

    checked.
  * Set Launch Bar Launch Mode to Debug.
  * Open CMake Settings > "Use these settings".

Expected:

    unchecked.

Actual:

    unchecked.


### 3) Build Settings specific Additional CMake arguments:  
#### 3.1) CMake Settings > Use these settings=checked, Additional CMake arguments are used during build
  * Make sure Launch Bar Launch Mode still set to Debug.
  * Set CMake Settings > "Use these settings" to checked.
  * Set CMake Settings > Generator to Ninja.
  * Set CMake Settings > "Additional CMake arguments" to "-DVAR=TEST"
  * On the Launch Bar, click Build button.

Expected: args in "Additional CMake arguments" are used during build.

Actual: 

    Configuring in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake -G Ninja -DVAR=TEST -DCMAKE_EXPORT_COMPILE_COMMANDS=ON C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld
    -- The C compiler identification is GNU 11.2.0
    -- The CXX compiler identification is GNU 11.2.0
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/mingw64/bin/cc.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/mingw64/bin/c++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (2.4s)
    -- Generating done (0.0s)
    CMake Warning:
      Manually-specified variables were not used by the project:

        VAR


    -- Build files have been written to: C:/Users/betamax/cdt-only2/runtime-New_configuration(1)/helloworld/build/cmake.debug.win32.x86_64
    Building in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake --build . --target all
    [1/2] Building CXX object CMakeFiles/helloworld.dir/helloworld.cpp.obj
    [2/2] Linking CXX executable helloworld.exe
    Build complete (0 errors, 0 warnings): C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64

#### 3.2) CMake Settings > Use these settings=unchecked, Additional CMake arguments are NOT used during build
  * Make sure Launch Bar Launch Mode still set to Debug.
  * Set CMake Settings > "Use these settings" to unchecked.
  * On the Launch Bar, click Build button.

Expected: args in "Additional CMake arguments" are NOT used during build.

Actual:

    Configuring in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake -G MinGW Makefiles -DCMAKE_EXPORT_COMPILE_COMMANDS=ON C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld
    -- The C compiler identification is GNU 11.2.0
    -- The CXX compiler identification is GNU 11.2.0
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/mingw64/bin/cc.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/mingw64/bin/c++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (2.5s)
    -- Generating done (0.0s)
    -- Build files have been written to: C:/Users/betamax/cdt-only2/runtime-New_configuration(1)/helloworld/build/cmake.debug.win32.x86_64
    Building in: C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
    cmake --build . --target all
    [ 50%] Building CXX object CMakeFiles/helloworld.dir/helloworld.cpp.obj
    [100%] Linking CXX executable helloworld.exe
    [100%] Built target helloworld
    Build complete (0 errors, 0 warnings): C:\Users\betamax\cdt-only2\runtime-New_configuration(1)\helloworld\build\cmake.debug.win32.x86_64
