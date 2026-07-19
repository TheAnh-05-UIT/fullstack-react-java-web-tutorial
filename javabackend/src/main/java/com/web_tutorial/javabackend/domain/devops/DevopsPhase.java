package com.web_tutorial.javabackend.domain.devops;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "devops_phases")
public class DevopsPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Khóa định danh (Slug) duy nhất của giai đoạn, dùng trên URL và tra cứu từ
     * Frontend.
     * Ví dụ: "plan", "code", "build". Phải là chữ thường, không khoảng cách.
     */
    @Column(unique = true, nullable = false)
    @NotBlank
    private String phaseKey;

    /**
     * Tên hiển thị ngắn gọn của giai đoạn. Ví dụ: "Plan", "Code", "Build & Package"
     */
    @NotBlank
    private String title;

    /**
     * Tên đầy đủ trên bảng theo dõi (ví dụ: "Continuous Planning & Agile
     * Management")
     */
    private String name;

    /** Mô tả ngắn / câu slogan cho hero section (tagline) */
    @Column(columnDefinition = "TEXT")
    private String tagline;

    /** Bài tóm tắt chi tiết về giai đoạn (summary) */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /** Tiêu đề của khung code/YAML mẫu trên Banner (heroSnippetTitle) */
    private String heroSnippetTitle;

    /** Nội dung đoạn code/YAML mẫu trên Banner (heroSnippet) */
    @Column(columnDefinition = "TEXT")
    private String heroSnippet;

    /**
     * Tên icon từ thư viện lucide-react để hiển thị trên Frontend (ví dụ:
     * "clipboard-list", "code-2")
     */
    private String iconName;

    /** Màu sắc gradient chủ đạo của giai đoạn (VD: "from-blue-500 to-cyan-500") */
    private String colorGradient;

    /** Thứ tự hiển thị trong Timeline 8 giai đoạn (1=plan, 2=code, 3=build...) */
    @Column(nullable = false)
    private Integer displayOrder;

    /** Trạng thái hiển thị. Nếu false, giai đoạn sẽ bị ẩn khỏi trang học viên */
    private boolean active = true;

    /** Cấu hình giao diện màu sắc chi tiết (PhaseTheme JSON string) */
    @Column(columnDefinition = "LONGTEXT")
    private String themeJson;

    /** Danh sách bài học chương trình đào tạo (CurriculumItem[] JSON string) */
    @Column(columnDefinition = "LONGTEXT")
    private String curriculumJson;

    /** Danh sách công cụ chuẩn công nghiệp (ToolItem[] JSON string) */
    @Column(columnDefinition = "LONGTEXT")
    private String toolsJson;

    /** Lộ trình học tập từng bước (LearningStep[] JSON string) */
    @Column(columnDefinition = "LONGTEXT")
    private String learningPathJson;

    /**
     * Danh sách câu hỏi trắc nghiệm kiểm tra kiến thức (QuizQuestion[] JSON string)
     */
    @Column(columnDefinition = "LONGTEXT")
    private String quizJson;

    /**
     * Danh sách các bài thực hành thực tế Hands-on Labs & Code snippet
     * (PracticeLab[] JSON string)
     */
    @Column(columnDefinition = "LONGTEXT")
    private String handsOnLabsJson;

    // Tracking (Audit fields)
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhaseKey() {
        return phaseKey;
    }

    public void setPhaseKey(String phaseKey) {
        this.phaseKey = phaseKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getHeroSnippetTitle() {
        return heroSnippetTitle;
    }

    public void setHeroSnippetTitle(String heroSnippetTitle) {
        this.heroSnippetTitle = heroSnippetTitle;
    }

    public String getHeroSnippet() {
        return heroSnippet;
    }

    public void setHeroSnippet(String heroSnippet) {
        this.heroSnippet = heroSnippet;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(String colorGradient) {
        this.colorGradient = colorGradient;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getThemeJson() {
        return themeJson;
    }

    public void setThemeJson(String themeJson) {
        this.themeJson = themeJson;
    }

    public String getCurriculumJson() {
        return curriculumJson;
    }

    public void setCurriculumJson(String curriculumJson) {
        this.curriculumJson = curriculumJson;
    }

    public String getToolsJson() {
        return toolsJson;
    }

    public void setToolsJson(String toolsJson) {
        this.toolsJson = toolsJson;
    }

    public String getLearningPathJson() {
        return learningPathJson;
    }

    public void setLearningPathJson(String learningPathJson) {
        this.learningPathJson = learningPathJson;
    }

    public String getQuizJson() {
        return quizJson;
    }

    public void setQuizJson(String quizJson) {
        this.quizJson = quizJson;
    }

    public String getHandsOnLabsJson() {
        return handsOnLabsJson;
    }

    public void setHandsOnLabsJson(String handsOnLabsJson) {
        this.handsOnLabsJson = handsOnLabsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
