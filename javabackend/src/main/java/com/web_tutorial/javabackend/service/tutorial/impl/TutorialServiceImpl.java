package com.web_tutorial.javabackend.service.tutorial.impl;

import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.repository.tutorial.CategoryRepository;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.tutorial.TutorialService;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import com.web_tutorial.javabackend.service.security.SecurityService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import org.springframework.transaction.annotation.Transactional;

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

    // Chỉ trả về tutorial chưa bị soft-deleted, ưu thiên hiển thị mới nhất trước
    @Override
    public List<Tutorial> getAllTutorials() {
        return this.tutorialRepository.findByIsDeletedFalseOrderByIdDesc();
    }

    @Override
    public ResultPaginationDTO getAllTutorials(Pageable pageable) {
        Page<Tutorial> page = this.tutorialRepository.findByIsDeletedFalseOrderByIdDesc(pageable);
        return MapperUtils.toResultPaginationDTO(page, tutorial -> {
            TutorialResponseDTO dto = MapperUtils.toTutorialResponseDTO(tutorial);
            if (dto.getCreateBy() != null) {
                dto.setAuthorName(this.getAuthorNameByEmail(dto.getCreateBy()));
            }
            return dto;
        });
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

        if (tutorial.getReadTime() == null || tutorial.getReadTime() <= 0 || tutorial.getReadTime() > 30) {
            tutorial.setReadTime(calculateCleanReadTime(tutorial.getContent(), tutorial.getDescription()));
        }
        if (tutorial.getViews() == null) {
            tutorial.setViews(0L);
        }

        return this.tutorialRepository.save(tutorial);
    }

    private int calculateCleanReadTime(String content, String description) {
        String rawText = (content != null && !content.isEmpty()) ? content : (description != null ? description : "");
        if (rawText.isEmpty()) return 5;
        String cleanText = rawText.replaceAll("data:image/[^;]+;base64,[a-zA-Z0-9+/=]+", "")
                                  .replaceAll("<[^>]+>", " ")
                                  .replaceAll("```[\\s\\S]*?```", " ")
                                  .replaceAll("[^a-zA-Z0-9À-ỹ\\s]", " ");
        int wordCount = cleanText.trim().split("\\s+").length;
        int minutes = (int) Math.ceil((double) wordCount / 250);
        return Math.max(3, Math.min(20, minutes));
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
            if (tutorialDetails.getReadTime() != null && tutorialDetails.getReadTime() > 0 && tutorialDetails.getReadTime() <= 30) {
                tutorial.setReadTime(tutorialDetails.getReadTime());
            } else if (tutorialDetails.getContent() != null || tutorial.getReadTime() == null || tutorial.getReadTime() <= 0 || tutorial.getReadTime() > 30) {
                tutorial.setReadTime(calculateCleanReadTime(tutorial.getContent(), tutorial.getDescription()));
            }

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
        }).orElseThrow(() -> new ResourceNotFoundException("Tutorial not found with id " + id));
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

    @Override
    public String getAuthorNameByEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email)
                .map(u -> u.getUsername())
                .orElse(email); // fallback về email nếu không tìm thấy user
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        this.tutorialRepository.incrementViews(id);
    }
}

