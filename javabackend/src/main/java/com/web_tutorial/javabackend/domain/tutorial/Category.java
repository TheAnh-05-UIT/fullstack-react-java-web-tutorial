package com.web_tutorial.javabackend.domain.tutorial;

import java.util.List;

import com.web_tutorial.javabackend.domain.project.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Model Category: Phân loại danh mục cho các bài viết.
 */
@Entity
@Table(name = "categores")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên và thông tin danh mục
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, unique = true)
    private String slug; // Đường dẫn SEO
    private String description;

    // Tracking
    private boolean isDeleted;

    // Danh sách bài viết thuộc danh mục này
    @OneToMany(mappedBy = "category")
    private List<Tutorial> listTutorials;

    @OneToMany(mappedBy = "category")
    private List<Project> projects;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<Tutorial> getListTutorials() {
        return listTutorials;
    }

    public void setListTutorials(List<Tutorial> listTutorials) {
        this.listTutorials = listTutorials;
    }

}
