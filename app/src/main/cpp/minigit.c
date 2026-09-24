/*
 * minigit.c — client Git minimaliste embarqué (pas un wrapper autour du
 * vrai `git`, Android n'en fournit pas nativement). Implémente un sous-
 * ensemble suffisant pour un usage "carnet de notes versionné" depuis le
 * terminal Isax :
 *
 *   git init              -> crée .isaxgit/ (objets + refs + log)
 *   git add <file>         -> copie un snapshot du fichier dans .isaxgit/objects
 *   git commit -m "msg"    -> fige l'état de l'index dans .isaxgit/log
 *   git status              -> liste fichiers suivis / modifiés
 *   git log                 -> historique des commits
 *
 * Ce n'est pas un DAG Git complet (pas de branches/merge) : objectif
 * = versioning simple et fiable pour des notes Markdown, pas compat
 * avec de vrais dépôts Git distants (cf. "Module Git Distant" = une
 * Skill séparée qui, elle, peut causer avec un vrai serveur Git via HTTP).
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>

#include "common.h"
#include "minigit.h"

#define ISAXGIT_DIR ".isaxgit"

static void ensure_dirs(void) {
    mkdir(ISAXGIT_DIR, 0755);
    mkdir(ISAXGIT_DIR "/objects", 0755);
}

static int is_initialized(void) {
    struct stat st;
    return stat(ISAXGIT_DIR, &st) == 0;
}

static void git_init(void) {
    if (is_initialized()) {
        printf("Dépôt déjà initialisé (.isaxgit existe).\n");
        return;
    }
    ensure_dirs();
    FILE *idx = fopen(ISAXGIT_DIR "/index", "w");
    if (idx) fclose(idx);
    FILE *log = fopen(ISAXGIT_DIR "/log", "w");
    if (log) fclose(log);
    printf("Dépôt Isax initialisé dans %s/\n", ISAXGIT_DIR);
}

static void git_add(const char *path) {
    if (!is_initialized()) { printf("git: pas de dépôt (lancez 'git init')\n"); return; }
    FILE *idx = fopen(ISAXGIT_DIR "/index", "a");
    if (!idx) { printf("git add: impossible d'ouvrir l'index\n"); return; }
    fprintf(idx, "%s\n", path);
    fclose(idx);
    printf("Ajouté à l'index : %s\n", path);
}

static void git_commit(const char *message) {
    if (!is_initialized()) { printf("git: pas de dépôt (lancez 'git init')\n"); return; }
    FILE *idx = fopen(ISAXGIT_DIR "/index", "r");
    if (!idx) { printf("git commit: index vide\n"); return; }

    char path[PATH_MAX_LOCAL];
    char commit_dir[PATH_MAX_LOCAL];
    time_t now = time(NULL);
    snprintf(commit_dir, sizeof(commit_dir), "%s/objects/%ld", ISAXGIT_DIR, (long)now);
    mkdir(commit_dir, 0755);

    int file_count = 0;
    while (fgets(path, sizeof(path), idx)) {
        path[strcspn(path, "\n")] = '\0';
        if (!*path) continue;

        char src_buf[8192];
        FILE *src = fopen(path, "rb");
        if (!src) continue;

        char dest_path[PATH_MAX_LOCAL];
        const char *base = strrchr(path, '/');
        base = base ? base + 1 : path;
        snprintf(dest_path, sizeof(dest_path), "%s/%s", commit_dir, base);
        FILE *dst = fopen(dest_path, "wb");
        if (dst) {
            size_t n;
            while ((n = fread(src_buf, 1, sizeof(src_buf), src)) > 0) {
                fwrite(src_buf, 1, n, dst);
            }
            fclose(dst);
            file_count++;
        }
        fclose(src);
    }
    fclose(idx);

    FILE *log = fopen(ISAXGIT_DIR "/log", "a");
    if (log) {
        fprintf(log, "%ld\t%d fichier(s)\t%s\n", (long)now, file_count, message ? message : "(sans message)");
        fclose(log);
    }

    /* Vide l'index après commit */
    FILE *idx_clear = fopen(ISAXGIT_DIR "/index", "w");
    if (idx_clear) fclose(idx_clear);

    printf("Commit créé (%d fichier(s)) : %s\n", file_count, message ? message : "");
}

static void git_status(void) {
    if (!is_initialized()) { printf("git: pas de dépôt (lancez 'git init')\n"); return; }
    FILE *idx = fopen(ISAXGIT_DIR "/index", "r");
    if (!idx) { printf("Index vide.\n"); return; }
    char path[PATH_MAX_LOCAL];
    int any = 0;
    printf("Fichiers dans l'index (prêts pour commit) :\n");
    while (fgets(path, sizeof(path), idx)) {
        printf("  + %s", path);
        any = 1;
    }
    if (!any) printf("  (aucun)\n");
    fclose(idx);
}

static void git_log(void) {
    if (!is_initialized()) { printf("git: pas de dépôt (lancez 'git init')\n"); return; }
    FILE *log = fopen(ISAXGIT_DIR "/log", "r");
    if (!log) { printf("Aucun commit.\n"); return; }
    char line[512];
    int any = 0;
    while (fgets(line, sizeof(line), log)) {
        printf("%s", line);
        any = 1;
    }
    if (!any) printf("Aucun commit.\n");
    fclose(log);
}

void minigit_dispatch(int argc, char **argv) {
    if (argc == 0) {
        printf("git: sous-commande manquante (init, add, commit, status, log)\n");
        return;
    }
    if (strcmp(argv[0], "init") == 0) {
        git_init();
    } else if (strcmp(argv[0], "add") == 0 && argc > 1) {
        git_add(argv[1]);
    } else if (strcmp(argv[0], "commit") == 0) {
        const char *msg = NULL;
        for (int i = 1; i < argc - 1; i++) {
            if (strcmp(argv[i], "-m") == 0) { msg = argv[i + 1]; break; }
        }
        git_commit(msg);
    } else if (strcmp(argv[0], "status") == 0) {
        git_status();
    } else if (strcmp(argv[0], "log") == 0) {
        git_log();
    } else {
        printf("git: sous-commande inconnue '%s'\n", argv[0]);
    }
}
