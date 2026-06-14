package com.web_tutorial.javabackend.repository.project;

import com.web_tutorial.javabackend.model.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

}
