package com.web_tutorial.javabackend.validation;

public final class ApiInputConstraints {

    public static final int VARCHAR_MAX = 255;
    public static final int EMAIL_MAX = 254;
    public static final int PASSWORD_MAX = 72;
    public static final int TEXT_MAX = 16_000;
    public static final int RICH_CONTENT_MAX = 1_000_000;
    public static final int CONTENT_KEY_MAX = 190;
    public static final int PAGE_SIZE_MAX = 100;
    public static final int TECH_STACK_MAX = 50;

    public static final String SLUG_PATTERN = "^[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*$";
    public static final String CONTENT_KEY_PATTERN = "^[A-Za-z0-9][A-Za-z0-9._-]*$";
    public static final String ROLE_PATTERN = "(?i)^(USER|ADMIN)$";

    private ApiInputConstraints() {
    }
}
