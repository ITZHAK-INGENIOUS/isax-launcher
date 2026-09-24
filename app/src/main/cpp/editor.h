#ifndef ISAX_EDITOR_H
#define ISAX_EDITOR_H

/* Ouvre un mini éditeur de texte en ligne de commande pour .txt/.md
 * (mode ligne : affiche le contenu numéroté, permet remplacement de ligne
 * via ":N nouveau texte", sauvegarde via ":w", quitte via ":q"). */
void editor_open(const char *path);

#endif /* ISAX_EDITOR_H */
