/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2014-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

#ifndef FLIGHTDEMO_ATOMICS_H
#define FLIGHTDEMO_ATOMICS_H

#include <sys/cdefs.h>
#include <sys/time.h>

__BEGIN_DECLS

#define __ATOMIC_INLINE__ static __inline__ __attribute__((always_inline))

__ATOMIC_INLINE__ int
__atomic_cmpxchg(int old, int _new, volatile int *ptr)
{
    /* We must return 0 on success */
    return __sync_val_compare_and_swap(ptr, old, _new) != old;
}

__ATOMIC_INLINE__ int
__atomic_swap(int _new, volatile int *ptr)
{
    int prev;
    do {
        prev = *ptr;
    } while (__sync_val_compare_and_swap(ptr, prev, _new) != prev);
    return prev;
}

__ATOMIC_INLINE__ int
__atomic_dec(volatile int *ptr)
{
  return __sync_fetch_and_sub (ptr, 1);
}

__ATOMIC_INLINE__ int
__atomic_inc(volatile int *ptr)
{
  return __sync_fetch_and_add (ptr, 1);
}

__ATOMIC_INLINE__ int
__atomic_add(int add, volatile int *ptr) {
	return __sync_fetch_and_add(ptr, add);
}

__ATOMIC_INLINE__ int
__atomic_sub(int sub, volatile int *ptr) {
	return __sync_fetch_and_sub(ptr, sub);
}

__ATOMIC_INLINE__ int
__atomic_or(int value, volatile int *ptr) {
/*	int32_t prev, tmp, status;
//	android_memory_barrier();
	do {
		__asm__ __volatile__ ("ldrex %0, [%4]\n"
							"orr %1, %0, %5\n"
							"strex %2, %1, [%4]"
							: "=&r" (prev), "=&r" (tmp),
							"=&r" (status), "+m" (*ptr)
							: "r" (ptr), "Ir" (value)
							: "cc");
	} while (__builtin_expect(status != 0, 0));
	return prev; */
	return __sync_fetch_and_or(ptr, value);
}

__END_DECLS

#endif //FLIGHTDEMO_ATOMICS_H
