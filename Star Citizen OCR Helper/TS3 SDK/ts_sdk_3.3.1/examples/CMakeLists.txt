cmake_minimum_required (VERSION 3.18)

#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_BINARY_DIR})
#set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${PROJECT_BINARY_DIR})
#set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${PROJECT_BINARY_DIR})

set(CMAKE_SKIP_BUILD_RPATH FALSE)
set(CMAKE_MACOSX_RPATH TRUE)
set(CMAKE_BUILD_WITH_INSTALL_RPATH TRUE)
if (APPLE)
    set(CMAKE_INSTALL_RPATH "@executable_path")
else()
    set(CMAKE_INSTALL_RPATH "\$ORIGIN")
endif()
set(CMAKE_INSTALL_RPATH_USE_LINK_PATH FALSE)
#set(CMAKE_BUILD_RPATH "\$ORIGIN")
set(BUILD_RPATH_USE_ORIGIN TRUE)

list(APPEND TS_SAMPLES
    client
    client_customdevice
    client_minimal
    client_minimal_filetransfer
    client_cpp_repeater
    client_cpp_whisperer
    server
    server_creation_params
    server_filetransfer
    server_minimal
    server_permissions
)
list(APPEND TS_SAMPLE_TYPES client client client client client client server server server server server)
foreach(sample_folder sample_type IN ZIP_LISTS TS_SAMPLES TS_SAMPLE_TYPES)
    if("${sample_type}" STREQUAL "server" AND ((CMAKE_SYSTEM_NAME STREQUAL "Linux") AND (CMAKE_SYSTEM_PROCESSOR STREQUAL "x86")))
        continue()
    endif()

    message(STATUS "sample_folder=${sample_folder}, sample_type=${sample_type}")
    project ("ts_${sample_folder}_sample" LANGUAGES C CXX)

    include("${CMAKE_CURRENT_LIST_DIR}/../cmake/ide.cmake")

    list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/../cmake")

    include("${CMAKE_CURRENT_LIST_DIR}/${sample_folder}/sources.cmake")
    message("TS_SAMPLE_SRC ${TS_SAMPLE_SRC}")
    # Create named folders for the sources within the .vcproj
    # Empty name lists them directly under the .vcproj
    source_group("" FILES ${TS_SAMPLE_SRC})

    # Turn on the ability to create folders to organize projects (.vcproj)
    # It creates "CMakePredefinedTargets" folder by default and adds CMake
    # defined projects like INSTALL.vcproj and ZERO_CHECK.vcproj
    set_property(GLOBAL PROPERTY USE_FOLDERS ON)

    # Set compiler flags and options. 
    # Here it is setting the Visual Studio warning level to 4
    # set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /W4")

    # Command to output information to the console
    # Useful for displaying errors, warnings, and debugging
    # message ("cxx Flags:" ${CMAKE_CXX_FLAGS})
    set(ts_sample_bin "ts_${sample_folder}")
    add_executable(${ts_sample_bin} ${TS_SAMPLE_SRC} ${TS_COMMON_SDK_IDE} ${TS_SDK_IDE})

    if ("${sample_type}" STREQUAL "client")
        set(TSClientSdk_DIR "${CMAKE_CURRENT_LIST_DIR}/../cmake")
        if (CMAKE_SYSTEM_NAME STREQUAL "Darwin")
            # set(XCFRAMEWORK_VARIANT "macos-arm64_x86_64")
            # set_target_properties(${ts_sample_bin} PROPERTIES INSTALL_RPATH "@executable_path/ts3client.xcframework/${XCFRAMEWORK_VARIANT}")
            # universal for client
            set_target_properties(${ts_sample_bin} PROPERTIES INSTALL_RPATH "@executable_path")
            set_target_properties(${ts_sample_bin} PROPERTIES OSX_ARCHITECTURES "arm64;x86_64")
        endif()
        find_package(TSClientSdk REQUIRED)
        message("TSClientSdk_FOUND: ${TSClientSdk_FOUND}")
        message("TSClientSdk_INCLUDE_DIRS: ${TSClientSdk_INCLUDE_DIRS}")
        message("TSClientSdk_LIBRARIES: ${TSClientSdk_LIBRARIES}")
        find_package( Threads REQUIRED )
        target_include_directories(${ts_sample_bin} PUBLIC ${TSClientSdk_INCLUDE_DIRS})
        target_link_libraries(${ts_sample_bin} "${CMAKE_THREAD_LIBS_INIT}" "${TSClientSdk_LIBRARIES}")
    elseif("${sample_type}" STREQUAL "server")
        set(TSServerSdk_DIR "${CMAKE_CURRENT_LIST_DIR}/../cmake")
        if (CMAKE_SYSTEM_NAME STREQUAL "Darwin")
            # only x64 for server
            set_target_properties(${ts_sample_bin} PROPERTIES OSX_ARCHITECTURES x86_64)
        endif()
        find_package(TSServerSdk REQUIRED)
        message("TSServerSdk_FOUND: ${TSServerSdk_FOUND}")
        message("TSServerSdk_INCLUDE_DIRS: ${TSServerSdk_INCLUDE_DIRS}")
        message("TSServerSdk_LIBRARIES: ${TSServerSdk_LIBRARIES}")
        target_include_directories(${ts_sample_bin} PUBLIC ${TSServerSdk_INCLUDE_DIRS})
        target_link_libraries(${ts_sample_bin} "${TSServerSdk_LIBRARIES}")
    else()
        message("example type not specified.")
    endif()

    if(NOT CMAKE_RUNTIME_OUTPUT_DIRECTORY)
        set_target_properties(${ts_sample_bin}
            PROPERTIES
            CXX_STANDARD 17
            ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${ts_sample_bin}/lib"
            LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${ts_sample_bin}/lib"
            RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/${ts_sample_bin}/bin"
        )
    else()
        set_target_properties(${ts_sample_bin}
            PROPERTIES
            CXX_STANDARD 17
            ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/${ts_sample_bin}/lib"
            LIBRARY_OUTPUT_DIRECTORY "${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/${ts_sample_bin}/lib"
            RUNTIME_OUTPUT_DIRECTORY "${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/${ts_sample_bin}/bin"
        )
    endif()

    set(ts_bin_flavor "")
    set(ts_dest_os "")
    if (CMAKE_SYSTEM_NAME STREQUAL "Windows")
        set(ts_dest_os "windows")
        if("${CMAKE_GENERATOR_PLATFORM}" STREQUAL "x64" OR "${CMAKE_VS_PLATFORM_NAME}" STREQUAL "x64")
            set(ts_bin_flavor "win64")
        elseif("${CMAKE_GENERATOR_PLATFORM}" STREQUAL "Win32" OR "${CMAKE_VS_PLATFORM_NAME}" STREQUAL "Win32")
            set(ts_bin_flavor "win32")
        endif()
        if ("${sample_type}" STREQUAL "client")
            add_custom_command(TARGET ${ts_sample_bin} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_SOURCE_DIR}/../bin/windows/${ts_bin_flavor}/ts3client.dll $<TARGET_FILE_DIR:${ts_sample_bin}>/.
            )
        elseif("${sample_type}" STREQUAL "server")
            add_custom_command(TARGET ${ts_sample_bin} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy
                ${CMAKE_SOURCE_DIR}/../bin/windows/${ts_bin_flavor}/ts3server.dll $<TARGET_FILE_DIR:${ts_sample_bin}>/.
            )
        endif()
    elseif (CMAKE_SYSTEM_NAME STREQUAL "Linux")
        set(ts_dest_os "linux")
        if ("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "arm")
            set(ts_bin_flavor "armv7")
        elseif ("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "aarch64")
            set(ts_bin_flavor "armv8")
        elseif("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "x86_64")
            set(ts_bin_flavor "amd64")
        elseif("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "x86")
            set(ts_bin_flavor "x86")
        endif()
        target_link_libraries(${ts_sample_bin} dl)
        if ("${sample_type}" STREQUAL "client")
            add_custom_command(TARGET ${ts_sample_bin} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy ${CMAKE_SOURCE_DIR}/../bin/linux/${ts_bin_flavor}/libts3client.so $<TARGET_FILE_DIR:${ts_sample_bin}>/.
            )
        elseif("${sample_type}" STREQUAL "server")
            add_custom_command(TARGET ${ts_sample_bin} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy
                ${CMAKE_SOURCE_DIR}/../bin/linux/${ts_bin_flavor}/libts3server.so $<TARGET_FILE_DIR:${ts_sample_bin}>/.
            )
        endif()
    elseif (CMAKE_SYSTEM_NAME STREQUAL "Darwin")
        set(ts_dest_os "mac")
        if ("${sample_type}" STREQUAL "client")
            set(ts_bin_flavor "arm64_x86_64")
        elseif("${sample_type}" STREQUAL "server")
            set(ts_bin_flavor "x86_64")
        endif()
    #     #target_link_libraries(${ts_sample_bin} dl)
    #     if ("${sample_type}" STREQUAL "client")
    #         # cmake command line (see above) does follow symlinks, so...
    #         file(COPY ${CMAKE_SOURCE_DIR}/../frameworks/mac/ts3client.framework DESTINATION ${ts_sample_bin})
    #         add_custom_command(TARGET ${ts_sample_bin} POST_BUILD
    #             COMMAND ${CMAKE_COMMAND} -E rename $<TARGET_FILE_DIR:${ts_sample_bin}>/${ts_sample_bin} $<TARGET_FILE_DIR:${ts_sample_bin}>/../${ts_sample_bin}
    #             COMMAND ${CMAKE_COMMAND} -E rm -rf $<TARGET_FILE_DIR:${ts_sample_bin}>/../bin
    #         )
    #     endif()
    endif()
    install(TARGETS ${ts_sample_bin}
        RUNTIME DESTINATION ${CMAKE_SOURCE_DIR}/../bin/${ts_dest_os}/${ts_bin_flavor}/)

endforeach()
# hack to create a specified visual studio solution
project("TeamSpeak SDK Samples" NONE)
