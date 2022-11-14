cmake_minimum_required(VERSION 3.10)

# Set some basic project attributes
project (${projectName?replace(" ", "_")}
	VERSION 0.1
	DESCRIPTION "A Hello World Project")

# This project will output an executable file
add_executable(${r"${PROJECT_NAME}"} ${projectName?replace(" ", "_")}.cpp)

# Create a simple configuration header
configure_file(config.h.in config.h)

# Include the configuration header in the build
target_include_directories(${r"${PROJECT_NAME}"} PUBLIC "${r"${PROJECT_BINARY_DIR}"}")
