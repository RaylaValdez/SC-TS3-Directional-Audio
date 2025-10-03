pushd build
rm -rf linux_x86
mkdir linux_x86
pushd linux_x86
#CFLAGS=-m32 CXXFLAGS=-m32 
cmake -G Ninja ../.. -DCMAKE_TOOLCHAIN_FILE=../../../cmake/linux_x86_toolchain.cmake
ninja
popd
popd