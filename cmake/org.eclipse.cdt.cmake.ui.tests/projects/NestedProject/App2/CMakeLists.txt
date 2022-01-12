cmake_minimum_required(VERSION 3.10)

# Set the project name and version
project(App2 VERSION 2.0)

set(CMAKE_EXPORT_COMPILE_COMMANDS "true")

# Configuration header
configure_file(app2.h.in app2.h)

# Add project executable
add_executable(${PROJECT_NAME} app2.c)

# Include the configuration header
target_include_directories(${PROJECT_NAME} PUBLIC "${PROJECT_BINARY_DIR}")
