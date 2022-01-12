#define foo 0

#if 0
# define NEVER_DEFINED
#endif

// X is defined
#define X

#ifdef X
# define X_IS_DEFINED
# if 0
  // always disabled
# endif
# define X_IS_IT
#elif defined (Y)
# define Y_IS_DEFINED_BUT_NOT_X
# if 1
  // always enabled if outer branch enabled
# endif
#else
# define NEITHER_X_NOR_Y_IS_DEFINED
#endif

// X is not defined, Y is defined
#undef X
#define Y

#ifdef X
# define X_IS_DEFINED
# if 0
  // always disabled
# endif
# define X_IS_IT
#elif defined (Y)
# define Y_IS_DEFINED_BUT_NOT_X
# if 1
  // always enabled if outer branch enabled
# endif
#else
# define NEITHER_X_NOR_Y_IS_DEFINED
#endif

// X is not defined, Y is not defined
#undef X
#undef Y

#ifdef X
# define X_IS_DEFINED
# if 0
  // always disabled
# endif
# define X_IS_IT
#elif defined (Y)
# define Y_IS_DEFINED_BUT_NOT_X
# if 1
  // always enabled if outer branch enabled
# endif
#else
# define NEITHER_X_NOR_Y_IS_DEFINED
#endif

#ifndef F
#ifdef  // this gives an error
#error invalid ifdef
#endif

#if foo
//my code
#endif

#endif // unbalanced endif because of invalid ifdef above

#if foo // unterminated #if - http://bugs.eclipse.org/255018
// inactive code
// #endif
