==-------------------------------------------==
==             SigmaProject v0.1             ==
==   https://github.com/k-zen/SigmaProject   ==
== Author: Andreas P. Koenzen <akc@apkc.net> ==
==-------------------------------------------==

Uses:
=====
sigmaproject [--monte-carlo] [-t=type] [i=1000] [-f=function] [-a=0] [-b=1]

Options:
========
    Available Functions:
    ====================
    --monte-carlo Uses the "Monte Carlo" procedure to estimate some value
                  defined by the parameter *-t*.

    Parameters:
    ===========
    -t The type of calculation to perform. Posible values are:

        Type #1: Numerical Integration
        ========
        Calculate the area under the curve defined by parameter *f*
        and between the interval (b - a).

    -i The amount of iterations to perform.
    -a The X coordinate of *a*. Must the smaller than *b*.
    -b The X coordinate of *b*. Must be bigger than *a*.

    Others:
    =======
    --plot Plot the aproximation using a scatter plot.

Flags:
======
    --help Shows this message.