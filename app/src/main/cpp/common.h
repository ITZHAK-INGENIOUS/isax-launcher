#ifndef ISAX_COMMON_H
#define ISAX_COMMON_H

#include <limits.h>

#ifndef PATH_MAX_LOCAL
#  ifdef PATH_MAX
#    define PATH_MAX_LOCAL PATH_MAX
#  else
#    define PATH_MAX_LOCAL 4096
#  endif
#endif

#endif /* ISAX_COMMON_H */
