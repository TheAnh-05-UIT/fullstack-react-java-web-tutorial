package com.web_tutorial.javabackend.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.user.UserProjectProgress;

@Repository
public interface UserProjectProgressRepository extends JpaRepository<UserProjectProgress, Long> {
}
