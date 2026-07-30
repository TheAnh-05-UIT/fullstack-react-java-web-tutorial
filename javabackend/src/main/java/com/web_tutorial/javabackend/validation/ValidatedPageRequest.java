package com.web_tutorial.javabackend.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.web_tutorial.javabackend.exception.ApiInputValidationException;

public final class ValidatedPageRequest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private ValidatedPageRequest() {
    }

    public static Pageable of(Integer page, Integer size, String[] sortParameters, Set<String> allowedSortFields) {
        int validatedPage = page == null ? DEFAULT_PAGE : page;
        int validatedSize = size == null ? DEFAULT_SIZE : size;

        if (validatedPage < 0) {
            throw new ApiInputValidationException("Page must be at least 0.");
        }
        if (validatedSize < 1 || validatedSize > ApiInputConstraints.PAGE_SIZE_MAX) {
            throw new ApiInputValidationException(
                    "Size must be between 1 and " + ApiInputConstraints.PAGE_SIZE_MAX + ".");
        }

        Sort sort = parseSort(sortParameters, allowedSortFields);
        return PageRequest.of(validatedPage, validatedSize, sort);
    }

    private static Sort parseSort(String[] sortParameters, Set<String> allowedSortFields) {
        if (sortParameters == null || sortParameters.length == 0) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String parameter : sortParameters) {
            if (parameter == null || parameter.isBlank()) {
                throw new ApiInputValidationException("Sort must not be blank.");
            }
            String[] parts = parameter.split(",", -1);
            if (parts.length > 2 || parts[0].isBlank() || !allowedSortFields.contains(parts[0])) {
                throw new ApiInputValidationException("Sort field is not allowed.");
            }

            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length == 2) {
                try {
                    direction = Sort.Direction.fromString(parts[1].trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    throw new ApiInputValidationException("Sort direction must be asc or desc.");
                }
            }
            orders.add(new Sort.Order(direction, parts[0]));
        }
        return Sort.by(orders);
    }
}
