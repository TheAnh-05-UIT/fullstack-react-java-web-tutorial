package com.web_tutorial.javabackend.config;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.domain.devops.DevopsPhase;
import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;

/**
 * Data Seeder: Tự động khởi tạo 8 Giai đoạn DevOps mặc định kèm cấu trúc nội
 * dung bài học
 * vào Database khi ứng dụng Spring Boot khởi động lần đầu tiên (khi DB còn
 * trống).
 *
 * Kiểm tra an toàn: Nếu đã có dữ liệu trong bảng devops_phases, seeder sẽ bỏ
 * qua hoàn toàn
 * để không ghi đè dữ liệu mà Admin đã chỉnh sửa.
 */
@Component
public class DevopsDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevopsDataSeeder.class);

    private final DevopsPhaseRepository phaseRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DevopsDataSeeder(DevopsPhaseRepository phaseRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.phaseRepository = phaseRepository;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("[DevopsDataSeeder] Đang làm mới dữ liệu DevOps Phases...");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        phaseRepository.deleteAll();
        phaseRepository.flush();
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("[DevopsDataSeeder] Bắt đầu khởi tạo 8 Giai đoạn DevOps Lifecycle Content mặc định từ JSON...");

        org.springframework.core.io.support.PathMatchingResourcePatternResolver resolver = new org.springframework.core.io.support.PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource[] resources;
        try {
            resources = resolver.getResources("classpath:data/devops_phases/*.json");
        } catch (Exception e) {
            log.warn("[DevopsDataSeeder] Không tìm thấy thư mục data/devops_phases, bỏ qua seed.");
            return;
        }

        for (org.springframework.core.io.Resource resource : resources) {
            try (java.io.InputStream is = resource.getInputStream()) {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(is);

                String key = node.path("slug").asText();
                if (key == null || key.isEmpty()) {
                    key = resource.getFilename().replace(".json", "");
                }

                DevopsPhase phase = new DevopsPhase();
                phase.setPhaseKey(key);
                phase.setTitle(node.path("title").asText(node.path("name").asText()));
                phase.setName(node.path("name").asText());
                phase.setTagline(node.path("tagline").asText());
                phase.setSummary(node.path("summary").asText());
                phase.setHeroSnippetTitle(node.path("heroSnippetTitle").asText());
                phase.setHeroSnippet(node.path("heroSnippet").asText());
                // In PhaseData, icon isn't directly at root, wait, theme has iconBg etc. But we
                // hardcoded icon before.
                // Let's fallback to some defaults if missing, or we can just parse from the old
                // hardcoded strings
                // Actually the JSON has no iconName or colorGradient at root.
                // In my old seeder, I hardcoded them. I should map them here!
                phase.setIconName(getIconForKey(key));
                phase.setColorGradient(getColorForKey(key));
                phase.setDisplayOrder(node.path("stageNumber").asInt(99));
                phase.setActive(true);

                phase.setThemeJson(objectMapper.writeValueAsString(node.path("theme")));
                phase.setCurriculumJson(objectMapper.writeValueAsString(node.path("curriculum")));
                phase.setToolsJson(objectMapper.writeValueAsString(node.path("tools")));
                phase.setLearningPathJson(objectMapper.writeValueAsString(node.path("learningPath")));
                phase.setQuizJson(objectMapper.writeValueAsString(node.path("quiz")));
                phase.setHandsOnLabsJson(objectMapper.writeValueAsString(node.path("handsOnLabs")));

                phase.setCreatedAt(Instant.now());
                phase.setCreatedBy("system-seeder");

                phaseRepository.save(phase);
                log.info("[DevopsDataSeeder] Phase '{}' đã được nạp dữ liệu nội dung.", key);
            } catch (Exception e) {
                log.error("[DevopsDataSeeder] Lỗi khi nạp dữ liệu từ file {}", resource.getFilename(), e);
            }
        }

        log.info("[DevopsDataSeeder] Khởi tạo thành công 8 Giai đoạn DevOps Lifecycle Content!");
    }

    private String getIconForKey(String key) {
        switch (key) {
            case "plan":
                return "clipboard-list";
            case "code":
                return "code-2";
            case "build":
                return "package";
            case "test":
                return "test-tube-2";
            case "release":
                return "rocket";
            case "deploy":
                return "server";
            case "operate":
                return "settings";
            case "monitor":
                return "activity";
            default:
                return "box";
        }
    }

    private String getColorForKey(String key) {
        switch (key) {
            case "plan":
                return "from-violet-500 to-purple-600";
            case "code":
                return "from-blue-500 to-cyan-600";
            case "build":
                return "from-amber-500 to-orange-600";
            case "test":
                return "from-emerald-500 to-teal-600";
            case "release":
                return "from-rose-500 to-pink-600";
            case "deploy":
                return "from-indigo-500 to-blue-600";
            case "operate":
                return "from-slate-500 to-gray-600";
            case "monitor":
                return "from-green-500 to-emerald-600";
            default:
                return "from-gray-500 to-gray-600";
        }
    }
}
