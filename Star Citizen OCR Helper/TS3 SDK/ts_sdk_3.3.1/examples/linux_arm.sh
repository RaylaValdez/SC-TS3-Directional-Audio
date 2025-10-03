pushd build
CFLAGS=-marm CXXFLAGS=-marm cmake -DCMAKE_TOOLCHAIN_FILE=../cmake/linux_arm32_toolchain.cmake ..
popd
