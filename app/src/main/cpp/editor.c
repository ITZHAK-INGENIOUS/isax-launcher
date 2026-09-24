/*
 * editor.c — éditeur minimaliste en mode "ligne" pour fichiers .txt/.md,
 * accessible depuis minishell via "edit <fichier>". Pensé pour un terminal
 * texte simple (pas de curses/ncurses pour rester léger et portable NDK).
 *
 * Commandes internes une fois le fichier ouvert :
 *   :p              affiche le contenu numéroté
 *   :N <texte>      remplace la ligne N par <texte>
 *   :a <texte>      ajoute une ligne à la fin
 *   :w              sauvegarde
 *   :q              quitte (sans sauvegarder si pas de :w avant)
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "editor.h"

#define MAX_LINES 2048
#define LINE_LEN 1024

void editor_open(const char *path) {
    char *lines[MAX_LINES];
    int count = 0;

    FILE *f = fopen(path, "r");
    if (f) {
        char buf[LINE_LEN];
        while (count < MAX_LINES && fgets(buf, sizeof(buf), f)) {
            buf[strcspn(buf, "\n")] = '\0';
            lines[count++] = strdup(buf);
        }
        fclose(f);
    } else {
        printf("(nouveau fichier) %s\n", path);
    }

    printf("--- édition de %s (%d lignes) — tapez :q pour quitter ---\n", path, count);

    char input[LINE_LEN];
    int dirty = 0;
    for (;;) {
        printf("edit> ");
        fflush(stdout);
        if (!fgets(input, sizeof(input), stdin)) break;
        input[strcspn(input, "\n")] = '\0';

        if (strcmp(input, ":q") == 0) {
            break;
        } else if (strcmp(input, ":p") == 0) {
            for (int i = 0; i < count; i++) printf("%3d| %s\n", i + 1, lines[i]);
        } else if (strcmp(input, ":w") == 0) {
            FILE *out = fopen(path, "w");
            if (out) {
                for (int i = 0; i < count; i++) fprintf(out, "%s\n", lines[i]);
                fclose(out);
                dirty = 0;
                printf("Sauvegardé (%d lignes) -> %s\n", count, path);
            } else {
                printf("Erreur d'écriture sur %s\n", path);
            }
        } else if (input[0] == ':' && strncmp(input, ":a ", 3) == 0) {
            if (count < MAX_LINES) {
                lines[count++] = strdup(input + 3);
                dirty = 1;
            }
        } else if (input[0] == ':') {
            int n = atoi(input + 1);
            char *space = strchr(input, ' ');
            if (n >= 1 && n <= count && space) {
                free(lines[n - 1]);
                lines[n - 1] = strdup(space + 1);
                dirty = 1;
            } else {
                printf("commande inconnue. Utilisez :p :N <texte> :a <texte> :w :q\n");
            }
        }
    }

    if (dirty) {
        printf("(modifications non sauvegardées perdues — tapez :w avant :q la prochaine fois)\n");
    }

    for (int i = 0; i < count; i++) free(lines[i]);
}
