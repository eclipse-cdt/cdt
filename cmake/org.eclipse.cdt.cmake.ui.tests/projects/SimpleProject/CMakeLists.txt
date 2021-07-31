cmake_minimum_required(VERSION 3.10)

# Set the project name and version
project(SimpleProject VERSION 1.0)

set(CMAKE_EXPORT_COMPILE_COMMANDS "true")

# Configuration header
configure_file(simple.h.in simple.h)

# Add project executable
add_executable(${PROJECT_NAME} simple.c)

# Include the configuration header
target_include_directories(${PROJECT_NAME} PUBLIC "${PROJECT_BINARY_DIR}")
