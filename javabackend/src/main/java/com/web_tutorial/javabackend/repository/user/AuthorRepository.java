package com.web_tutorial.javabackend.repository.user;

import com.web_tutorial.javabackend.model.user.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
