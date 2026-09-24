#ifndef ISAX_MINIGIT_H
#define ISAX_MINIGIT_H

/* Dispatch les sous-commandes "git <cmd> [args...]" reçues par minishell.
 * argc/argv ici excluent déjà le mot "git" lui-même. */
void minigit_dispatch(int argc, char **argv);

#endif /* ISAX_MINIGIT_H */
