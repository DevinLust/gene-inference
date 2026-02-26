package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

public record ParentsAtBirthFilter(
        Category category,
        Grade p1,
        Grade p2
) {}
