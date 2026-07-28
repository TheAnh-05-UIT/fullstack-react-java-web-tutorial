package com.web_tutorial.javabackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.project.ProjectResponseDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.project.Project;
import com.web_tutorial.javabackend.domain.project.ProjectStatus;
import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.domain.tutorial.TutorialStatus;
import com.web_tutorial.javabackend.domain.user.User;
import com.web_tutorial.javabackend.repository.project.ProjectRepository;
import com.web_tutorial.javabackend.repository.tutorial.CategoryRepository;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.repository.user.UserRepository;
import com.web_tutorial.javabackend.service.project.impl.ProjectServiceImpl;
import com.web_tutorial.javabackend.service.tutorial.impl.TutorialServiceImpl;

@ExtendWith(MockitoExtension.class)
class TutorialAndProjectBatchQueryTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    private TutorialServiceImpl tutorialService;
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        tutorialService = new TutorialServiceImpl(tutorialRepository, categoryRepository, userRepository);
        projectService = new ProjectServiceImpl(projectRepository, categoryRepository, userRepository);
    }

    @Test
    void testTutorialBatchQuery_multipleItemsSameAuthor() {
        // Nhiều tutorial cùng tác giả "admin@example.com"
        Tutorial t1 = new Tutorial();
        t1.setId(1L);
        t1.setTitle("Tut 1");
        t1.setSlug("tut-1");
        t1.setCreateBy("admin@example.com");

        Tutorial t2 = new Tutorial();
        t2.setId(2L);
        t2.setTitle("Tut 2");
        t2.setSlug("tut-2");
        t2.setCreateBy("admin@example.com");

        Page<Tutorial> page = new PageImpl<>(Arrays.asList(t1, t2), PageRequest.of(0, 10), 2);
        when(tutorialRepository.findByStatusAndIsDeletedFalseOrderByIdDesc(
                eq(TutorialStatus.PUBLISHED), any(Pageable.class))).thenReturn(page);

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setUsername("Super Admin");
        when(userRepository.findAllByEmailIn(Set.of("admin@example.com"))).thenReturn(List.of(adminUser));

        ResultPaginationDTO result = tutorialService.getAllTutorials(PageRequest.of(0, 10));

        assertNotNull(result);
        List<?> items = (List<?>) result.getResult();
        assertEquals(2, items.size());
        
        TutorialResponseDTO dto1 = (TutorialResponseDTO) items.get(0);
        TutorialResponseDTO dto2 = (TutorialResponseDTO) items.get(1);
        assertEquals("Super Admin", dto1.getAuthorName());
        assertEquals("Super Admin", dto2.getAuthorName());

        // Kiểm tra batch query được gọi 1 lần duy nhất, findByEmail (N+1) không được gọi
        verify(userRepository, times(1)).findAllByEmailIn(Set.of("admin@example.com"));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testProjectBatchQuery_authorNotFoundFallbackToEmail() {
        // Tác giả không tồn tại trong bảng User -> fallback về email gốc
        Project p1 = new Project();
        p1.setId(10L);
        p1.setTitle("Project 1");
        p1.setSlug("project-1");
        p1.setCreateBy("unknown@example.com");

        Page<Project> page = new PageImpl<>(List.of(p1), PageRequest.of(0, 10), 1);
        when(projectRepository.findByStatusAndIsDeletedFalseOrderByIdDesc(
                eq(ProjectStatus.PUBLISHED), any(Pageable.class))).thenReturn(page);
        when(userRepository.findAllByEmailIn(Set.of("unknown@example.com"))).thenReturn(Collections.emptyList());

        ResultPaginationDTO result = projectService.getAllProjects(PageRequest.of(0, 10));

        List<?> items = (List<?>) result.getResult();
        ProjectResponseDTO dto1 = (ProjectResponseDTO) items.get(0);
        assertEquals("unknown@example.com", dto1.getAuthorName());

        verify(userRepository, times(1)).findAllByEmailIn(Set.of("unknown@example.com"));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testTutorialBatchQuery_emptyPageNoUserQuery() {
        // Trang rỗng -> không truy vấn bảng User
        Page<Tutorial> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(tutorialRepository.findByStatusAndIsDeletedFalseOrderByIdDesc(
                eq(TutorialStatus.PUBLISHED), any(Pageable.class))).thenReturn(emptyPage);

        ResultPaginationDTO result = tutorialService.getAllTutorials(PageRequest.of(0, 10));

        List<?> items = (List<?>) result.getResult();
        assertEquals(0, items.size());

        verify(userRepository, never()).findAllByEmailIn(anyCollection());
        verify(userRepository, never()).findByEmail(any());
    }
}
