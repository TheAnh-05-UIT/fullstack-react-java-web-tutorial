package com.web_tutorial.javabackend.service.tutorial.impl;

import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.repository.tutorial.CategoryRepository;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.tutorial.TutorialService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import com.web_tutorial.javabackend.service.security.SecurityService;

@Service
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;
    private final CategoryRepository categoryRepository;
    // Inject UserRepository vào Service thay vì Controller (đúng kiến trúc)
    private final UserRepository userRepository;

    public TutorialServiceImpl(TutorialRepository tutorialRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.tutorialRepository = tutorialRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // Chỉ trả về tutorial chưa bị soft-deleted
    @Override
    public List<Tutorial> getAllTutorials() {
        return this.tutorialRepository.findByIsDeletedFalse();
    }

    // Chỉ tìm tutorial chưa bị xóa
    @Override
    public Optional<Tutorial> getTutorialById(Long id) {
        return this.tutorialRepository.findByIdAndIsDeletedFalse(id);
    }

    // Chỉ tìm tutorial chưa bị xóa theo slug
    @Override
    public Optional<Tutorial> getTutorialBySlug(String slug) {
        return this.tutorialRepository.findBySlugAndIsDeletedFalse(slug);
    }

    @Override
    public Tutorial createTutorial(Tutorial tutorial) {
        String currentUser = SecurityService.getCurrentUserLogin().orElse("System");
        tutorial.setCreateBy(currentUser);
        tutorial.setCreatedAt(Instant.now());

        if (tutorial.getCategory() != null && tutorial.getCategory().getName() != null) {
            String catName = tutorial.getCategory().getName();
            Category category = categoryRepository.findByName(catName).orElseGet(() -> {
                Category newCat = new Category();
                newCat.setName(catName);
                newCat.setSlug(catName.toLowerCase().replace(" ", "-"));
                return categoryRepository.save(newCat);
            });
            tutorial.setCategory(category);
        }

        return this.tutorialRepository.save(tutorial);
    }

    @Override
    public Tutorial updateTutorial(Long id, Tutorial tutorialDetails) {
        // Dùng findByIdAndIsDeletedFalse để không xử lý tutorial đã xóa
        return this.tutorialRepository.findByIdAndIsDeletedFalse(id).map(tutorial -> {
            if (tutorialDetails.getTitle() != null)
                tutorial.setTitle(tutorialDetails.getTitle());
            if (tutorialDetails.getSlug() != null)
                tutorial.setSlug(tutorialDetails.getSlug());
            if (tutorialDetails.getDescription() != null)
                tutorial.setDescription(tutorialDetails.getDescription());
            if (tutorialDetails.getContent() != null)
                tutorial.setContent(tutorialDetails.getContent());
            if (tutorialDetails.getCoverImage() != null)
                tutorial.setCoverImage(tutorialDetails.getCoverImage());
            if (tutorialDetails.getStatus() != null)
                tutorial.setStatus(tutorialDetails.getStatus());

            // Cập nhật audit field
            String currentUser = SecurityService.getCurrentUserLogin().orElse("System");
            tutorial.setUpdateBy(currentUser);
            tutorial.setUpdatedAt(Instant.now());

            if (tutorialDetails.getCategory() != null && tutorialDetails.getCategory().getName() != null) {
                String catName = tutorialDetails.getCategory().getName();
                Category category = categoryRepository.findByName(catName).orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setName(catName);
                    newCat.setSlug(catName.toLowerCase().replace(" ", "-"));
                    return categoryRepository.save(newCat);
                });
                tutorial.setCategory(category);
            }

            return this.tutorialRepository.save(tutorial);
        }).orElseThrow(() -> new RuntimeException("Tutorial not found with id " + id));
    }

    // Soft delete thay vì xóa cứng khỏi DB
    @Override
    public void deleteTutorial(Long id) {
        this.tutorialRepository.findByIdAndIsDeletedFalse(id).ifPresent(tutorial -> {
            tutorial.setDeleted(true);
            tutorial.setUpdatedAt(Instant.now());
            this.tutorialRepository.save(tutorial);
        });
    }

    // Lookup author name trong Service, không để Controller truy cập Repository
    @Override
    public String getAuthorNameByEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email)
                .map(u -> u.getUsername())
                .orElse(email); // fallback về email nếu không tìm thấy user
    }
}

