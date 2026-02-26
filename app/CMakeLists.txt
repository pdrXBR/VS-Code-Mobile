cmake_minimum_required(VERSION 3.18.1)

project("vscodemobile")

# Exemplo de compilação de libvterm (baixar e extrair no diretório cpp)
add_library(vterm STATIC IMPORTED)
set_target_properties(vterm PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libvterm.a)

# Ponte PTY (exemplo)
add_library(pty_bridge SHARED src/main/cpp/pty_bridge.c)
target_link_libraries(pty_bridge vterm log)