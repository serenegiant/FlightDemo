//
// Created by saki on 16/02/21.
//

#include <stdlib.h>
#include <stdio.h>
#include "charutils.h"

bool endWidth(const char *text, const char *target) {
	bool result = false;
	const int text_len = text ? strlen(text) : 0;
	const int target_len = target ? strlen(target) : 0;
	if (text_len >= target_len) {
		result = true;
		for (int i = 1; i <= target_len; i++) {
			if (text[text_len - i] != target[target_len - i]) {
				result = false;
				break;
			}
		}
	}
	return result;
}

