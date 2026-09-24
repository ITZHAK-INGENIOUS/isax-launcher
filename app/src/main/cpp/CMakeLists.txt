cmake_minimum_required(VERSION 3.22.1)
project(minishell C)

# On compile minishell comme un EXECUTABLE (pas une lib .so classique),
# puis on le renomme en libminishell.so pour profiter du packaging/
# exécution natif d'Android (contourne les restrictions W^X sur les
# fichiers exécutables hors nativeLibraryDir depuis Android 10+).
add_executable(minishell
    minishell.c
    minigit.c
    editor.c
)

target_compile_options(minishell PRIVATE -std=c11 -Wall -Wextra)

set_target_properties(minishell PROPERTIES
    OUTPUT_NAME "minishell"
    SUFFIX ".so"          # -> libminishell.so, packagé dans nativeLibraryDir
    PREFIX "lib"
    LINKER_LANGUAGE C
)

target_link_libraries(minishell log)
