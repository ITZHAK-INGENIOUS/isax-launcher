/*
 * minishell.c
 * Shell minimaliste embarqué dans Isax, lancé comme process fils depuis
 * MinishellBridge.kt (stdin/stdout reliés au Terminal Compose).
 *
 * Commandes intégrées : cd, pwd, ls, exit, help
 * Sous-commandes déléguées : "git ..." -> minigit.c, "edit <file>" -> editor.c
 * Toute suppression passe par trash_path() pour respecter le contrat
 * ".isax_trash" du cahier des charges (§2.3).
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <dirent.h>
#include <sys/stat.h>
#include <errno.h>

#include "common.h"
#include "minigit.h"
#include "editor.h"

#define LINE_MAX_LEN 4096
#define MAX_ARGS 64

static void print_prompt(void) {
    char cwd[PATH_MAX_LOCAL];
    if (getcwd(cwd, sizeof(cwd)) != NULL) {
        printf("isax:%s$ ", cwd);
    } else {
        printf("isax$ ");
    }
    fflush(stdout);
}

static int split_args(char *line, char **argv) {
    int argc = 0;
    char *tok = strtok(line, " \t");
    while (tok && argc < MAX_ARGS - 1) {
        argv[argc++] = tok;
        tok = strtok(NULL, " \t");
    }
    argv[argc] = NULL;
    return argc;
}

/* Déplace un fichier vers $ISAX_TRASH au lieu de le supprimer (cf. §2.3). */
static void cmd_trash(const char *path) {
    const char *trash_dir = getenv("ISAX_TRASH");
    if (!trash_dir) trash_dir = ".isax_trash";
    mkdir(trash_dir, 0755);

    char dest[PATH_MAX_LOCAL];
    const char *base = strrchr(path, '/');
    base = base ? base + 1 : path;
    snprintf(dest, sizeof(dest), "%s/%s", trash_dir, base);

    if (rename(path, dest) == 0) {
        printf("Déplacé vers la corbeille : %s\n", dest);
    } else {
        printf("Erreur trash: %s\n", strerror(errno));
    }
}

static void cmd_ls(const char *path) {
    DIR *d = opendir(path && *path ? path : ".");
    if (!d) { printf("ls: %s\n", strerror(errno)); return; }
    struct dirent *entry;
    while ((entry = readdir(d)) != NULL) {
        if (entry->d_name[0] == '.') continue;
        printf("%s\n", entry->d_name);
    }
    closedir(d);
}

int main(void) {
    char line[LINE_MAX_LEN];
    char *argv[MAX_ARGS];

    printf("Isax minishell v0.1 — tapez 'help' pour la liste des commandes\n");

    for (;;) {
        print_prompt();
        if (!fgets(line, sizeof(line), stdin)) break;

        size_t len = strlen(line);
        if (len && line[len - 1] == '\n') line[len - 1] = '\0';
        if (line[0] == '\0') continue;

        char line_copy[LINE_MAX_LEN];
        strncpy(line_copy, line, sizeof(line_copy));
        int argc = split_args(line_copy, argv);
        if (argc == 0) continue;

        if (strcmp(argv[0], "exit") == 0) {
            break;
        } else if (strcmp(argv[0], "help") == 0) {
            printf("cd, pwd, ls, trash <file>, edit <file>, git <cmd>, exit\n");
        } else if (strcmp(argv[0], "pwd") == 0) {
            char cwd[PATH_MAX_LOCAL];
            if (getcwd(cwd, sizeof(cwd))) printf("%s\n", cwd);
        } else if (strcmp(argv[0], "cd") == 0) {
            const char *target = argc > 1 ? argv[1] : getenv("HOME");
            if (chdir(target) != 0) printf("cd: %s\n", strerror(errno));
        } else if (strcmp(argv[0], "ls") == 0) {
            cmd_ls(argc > 1 ? argv[1] : "");
        } else if (strcmp(argv[0], "trash") == 0 && argc > 1) {
            cmd_trash(argv[1]);
        } else if (strcmp(argv[0], "edit") == 0 && argc > 1) {
            editor_open(argv[1]);
        } else if (strcmp(argv[0], "git") == 0) {
            minigit_dispatch(argc - 1, argv + 1);
        } else {
            printf("commande inconnue : %s (tapez 'help')\n", argv[0]);
        }
    }
    return 0;
}
