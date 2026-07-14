import type { PhaseData } from '../types/devops.types';

export const buildPhaseData: PhaseData = {
  id: 'build',
  name: 'Build',
  slug: 'build',
  stageNumber: 3,
  tagline: 'Automatically compile source code into deployable, immutable artifacts and container images that can be tested and released with confidence.',
  summary: 'You have mastered the Build stage—from Maven/Gradle compilation pipelines and Docker multi-stage image construction to dependency caching strategies and artifact registry publishing.',
  heroSnippetTitle: 'Jenkinsfile & pom.xml · Maven / Gradle Automated Build',
  heroSnippet: `pipeline {
  agent any
  tools {
    maven 'Apache-Maven-3.9'
    gradle 'Gradle-8.5'
  }
  stages {
    stage('Fetch Source Code & Dependencies') {
      steps {
        git branch: 'main', url: 'https://github.com/DevOpsBuilder/backend.git'
        sh 'mvn dependency:go-offline -B'
      }
    }
    stage('Compile & Package Functional Artifacts') {
      parallel {
        stage('Maven Package (Spring Boot Backend)') {
          steps { sh 'mvn clean package -DskipTests=false -Pproduction' }
        }
        stage('Gradle Build (Android / Kotlin Client)') {
          steps { sh 'gradle clean build --no-daemon -x test' }
        }
      }
    }
    stage('Docker Package Executable Image') {
      steps { sh 'docker build -t devopsbuilder/payment-service:\${BUILD_NUMBER} .' }
    }
  }
}`,
  theme: {
    gradient: 'from-violet-900 via-purple-950 to-slate-900',
    iconBg: 'bg-violet-600',
    badgeBg: 'bg-violet-50 dark:bg-violet-950/60 border-violet-200 dark:border-violet-800',
    badgeText: 'text-violet-700 dark:text-violet-300',
    borderColor: 'border-violet-500',
    accentColor: 'text-violet-600 dark:text-violet-400',
    ctaBg: 'bg-gradient-to-r from-violet-600 to-purple-600 hover:from-violet-700 hover:to-purple-700',
    ctaText: 'text-violet-600 dark:text-violet-400'
  },
  curriculum: [
    {
      id: 'build-1',
      title: 'Build Tools: Maven, Gradle & npm/pnpm Fundamentals',
      category: 'Core Fundamentals',
      duration: '4.0 Hours',
      level: 'Beginner',
      description: 'Understand the role of build tools—how Maven resolves dependency trees via `pom.xml`, how Gradle executes task DAGs, and how npm/pnpm manage JavaScript workspaces.',
      tags: ['Maven', 'Gradle', 'npm', 'pnpm', 'Build Lifecycle'],
      objectives: [
        'Navigate Maven build lifecycle phases: validate → compile → test → package → install → deploy',
        'Write Gradle build scripts defining custom tasks with dependency ordering',
        'Configure npm workspaces for monorepo frontend build isolation'
      ]
    },
    {
      id: 'build-2',
      title: 'Docker Multi-Stage Builds & Minimal Production Images',
      category: 'Core Fundamentals',
      duration: '5.0 Hours',
      level: 'Intermediate',
      description: 'Master Docker multi-stage builds to produce ultra-lean production images. Copy only compiled artifacts from builder stages and strip all development dependencies.',
      tags: ['Docker', 'Multi-Stage Build', 'Distroless Images', 'Layer Optimization'],
      objectives: [
        'Write multi-stage `Dockerfile` with separate `builder` and `production` stages',
        'Use Google Distroless base images to eliminate unnecessary OS packages',
        'Analyze and optimize Docker layer cache strategies for sub-30-second rebuilds'
      ]
    },
    {
      id: 'build-3',
      title: 'Dependency Vulnerability Scanning (Trivy & OWASP)',
      category: 'Core Fundamentals',
      duration: '3.0 Hours',
      level: 'Intermediate',
      description: 'Automate supply-chain security scanning during the build phase to detect CVEs in direct and transitive dependencies before they ship.',
      tags: ['Trivy', 'OWASP Dependency-Check', 'CVE Scanning', 'Supply Chain Security'],
      objectives: [
        'Integrate `trivy image` scanning into CI pipeline as a mandatory build gate',
        'Configure severity thresholds to fail builds containing CRITICAL/HIGH CVEs',
        'Generate SBOM (Software Bill of Materials) artifacts for regulatory audits'
      ]
    },
    {
      id: 'build-4',
      title: 'Artifact Registry & Semantic Version Tagging',
      category: 'Core Fundamentals',
      duration: '3.0 Hours',
      level: 'Intermediate',
      description: 'Publish immutable versioned build artifacts to professional artifact registries (Harbor, Artifactory, AWS ECR). Tag images with semantic versions linked to git commits.',
      tags: ['Artifact Registry', 'Harbor', 'Artifactory', 'ECR', 'Semantic Versioning'],
      objectives: [
        'Push Docker images to Harbor registry with SHA256-pinned tags',
        'Configure retention policies to prevent unbounded registry storage growth',
        'Implement image signing with Cosign for software supply chain integrity'
      ]
    },
    {
      id: 'build-5',
      title: 'CI/CD Build Pipeline Caching & Parallelization',
      category: 'Advanced Practices',
      duration: '4.0 Hours',
      level: 'Advanced',
      description: 'Dramatically reduce build times from 15 minutes to under 3 minutes using layer caching, dependency caching, and parallelized build matrix strategies.',
      tags: ['Build Caching', 'Pipeline Optimization', 'GitHub Actions Cache', 'Matrix Build'],
      objectives: [
        'Cache Maven `.m2` and npm `node_modules` directories between CI runs',
        'Configure Docker BuildKit layer caching with `cache-from/cache-to` registry mode',
        'Split large test suites across parallel matrix workers for horizontal scaling'
      ]
    },
    {
      id: 'build-6',
      title: 'Build Provenance & SLSA Supply Chain Security Framework',
      category: 'Advanced Practices',
      duration: '4.5 Hours',
      level: 'Enterprise',
      description: 'Comply with SLSA (Supply-chain Levels for Software Artifacts) framework requirements by generating cryptographically verifiable build provenance attestations.',
      tags: ['SLSA Framework', 'Build Provenance', 'Cosign', 'SBOM', 'Supply Chain'],
      objectives: [
        'Generate SLSA Level 3 provenance attestations linking artifacts to their exact source commit',
        'Sign and verify container images using keyless Sigstore/Cosign signing',
        'Publish SBOM artifacts in CycloneDX JSON format to artifact registry'
      ]
    }
  ],
  tools: [
    {
      name: 'Docker & BuildKit',
      category: 'Artifacts & Containers',
      description: 'The standard container image build system supporting multi-stage builds, build cache mount points, and remote BuildKit daemon for distributed builds.',
      industryStandard: true,
      documentationUrl: 'https://docs.docker.com/build/',
      internalLink: '/tutorials/docker-mastery'
    },
    {
      name: 'Maven & Gradle',
      category: 'CI/CD',
      description: 'Java ecosystem build orchestration tools handling dependency resolution, incremental compilation, multi-module project builds, and artifact packaging.',
      industryStandard: true,
      documentationUrl: 'https://maven.apache.org/'
    },
    {
      name: 'Jenkins',
      category: 'CI/CD',
      description: 'The world\'s leading open-source automation server enabling developers to reliably build, test, and package their software through automated declarative pipelines.',
      industryStandard: true,
      documentationUrl: 'https://www.jenkins.io/'
    },
    {
      name: 'Trivy & Cosign',
      category: 'Security',
      description: 'Aqua Security\'s open-source vulnerability scanner and the Sigstore keyless artifact signing tool for hardening software supply chains.',
      industryStandard: true,
      documentationUrl: 'https://trivy.dev/'
    },
    {
      name: 'Harbor Container Registry',
      category: 'Artifacts & Containers',
      description: 'CNCF-graduated open-source container registry with integrated vulnerability scanning, image signing, and multi-tenant access control.',
      industryStandard: true,
      documentationUrl: 'https://goharbor.io/'
    }
  ],
  learningPath: [
    {
      stepNumber: 1,
      title: 'Write Your First Optimized Multi-Stage Dockerfile',
      duration: '1 Day',
      category: 'Core Fundamentals',
      description: 'Create a production-grade multi-stage Dockerfile for a Spring Boot backend and React frontend, resulting in images under 50MB.',
      keyTakeaway: 'Never ship your development build tools (Node, JDK, Maven) inside a production Docker image.'
    },
    {
      stepNumber: 2,
      title: 'Configure Maven/Gradle Build with Dependency Caching',
      duration: '2 Days',
      category: 'Core Fundamentals',
      description: 'Integrate Maven or Gradle build configuration inside a GitHub Actions pipeline with `.m2` artifact caching to achieve 70% build time reduction.',
      keyTakeaway: 'Reproducible builds require exact dependency lock files (`package-lock.json`, `gradle.lockfile`).'
    },
    {
      stepNumber: 3,
      title: 'Integrate Trivy CVE Scanning as a Build Gate',
      duration: '1 Day',
      category: 'Core Fundamentals',
      description: 'Add a mandatory Trivy container scan step that blocks artifact publishing if any CRITICAL severity CVEs are detected.',
      keyTakeaway: 'Shift security left—a CVE discovered during build is 100x cheaper to fix than one found post-deployment.'
    },
    {
      stepNumber: 4,
      title: 'Publish Signed Images to Harbor with Semantic Tags',
      duration: '2 Days',
      category: 'Advanced Practices',
      description: 'Configure Docker image pushes to an internal Harbor registry with Cosign keyless signing and semantic version tags from Conventional Commits.',
      keyTakeaway: 'Immutable, signed, semantically-versioned images are the bedrock of reproducible deployments.'
    },
    {
      stepNumber: 5,
      title: 'Achieve SLSA Level 2 Provenance with GitHub Actions',
      duration: '2 Days',
      category: 'Advanced Practices',
      description: 'Use the official SLSA GitHub Actions generator to automatically produce cryptographic build provenance for all published artifacts.',
      keyTakeaway: 'SLSA provenance proves to auditors that your artifacts came from a trusted, unmodified source.'
    }
  ],
  quiz: [
    {
      question: 'What is the primary security benefit of using a Docker multi-stage build for a production image?',
      options: [
        'It allows developers to bypass SSL certificate validation in production',
        'The final production image contains only compiled artifacts—not development tools, compilers, or source code—dramatically reducing the attack surface',
        'Multi-stage builds automatically encrypt all environment variables',
        'It forces the image to run as the root user for maximum permissions'
      ],
      correctIndex: 1,
      explanation: 'Multi-stage builds create separate `FROM` stages. The `builder` stage contains your JDK/Node compiler and source code. The `production` stage copies only the compiled binary/dist folder, resulting in a minimal image without any build tools that attackers could exploit.',
      difficulty: 'Intermediate'
    },
    {
      question: 'Why is pinning exact dependency versions with lock files (`package-lock.json`, `pom.xml` with exact versions) critical for production builds?',
      options: [
        'Lock files make source code smaller and faster to download from GitHub',
        'Without exact version pinning, a dependency update pushed by a third-party NPM author could silently break or compromise your production build at any time',
        'Lock files are only used for local development and are ignored in CI/CD pipelines',
        'Dependency pinning is optional since package registries always maintain backward compatibility'
      ],
      correctIndex: 1,
      explanation: 'Version pinning ensures deterministic, reproducible builds. Without lock files, `npm install` might resolve to a newer patch/minor version that introduces breaking changes or security vulnerabilities—without any code change by your own team.',
      difficulty: 'Intermediate'
    },
    {
      question: 'What does SLSA (Supply-chain Levels for Software Artifacts) Level 2 specifically require compared to Level 1?',
      options: [
        'SLSA Level 2 requires all code to be written in memory-safe programming languages',
        'SLSA Level 2 requires that build provenance is generated by a hosted CI platform and cryptographically signed, ensuring it cannot be forged by an individual developer',
        'SLSA Level 2 bans the use of open-source dependencies entirely',
        'SLSA Level 2 mandates that all builds must complete within 5 minutes'
      ],
      correctIndex: 1,
      explanation: 'SLSA Level 1 just requires provenance to exist. Level 2 adds the requirement that the hosted CI/CD platform itself generates and signs the provenance, not an individual developer—preventing insider tampering of build records.',
      difficulty: 'Advanced'
    }
  ],
  handsOnLabs: [
    {
      id: 'build-lab-basic',
      title: 'Multi-Stage Docker Build Optimization Lab',
      tabTitle: '📌 Basic Lab: Docker Build',
      level: 'Intermediate',
      duration: '1.5 Hours',
      difficulty: 'Intermediate Practical',
      prerequisites: 'Docker Desktop installed and basic command-line familiarity.',
      desc: 'Build a production-grade multi-stage Docker image for a full-stack Spring Boot + React application. Measure image size reduction and verify the final image contains no build tools or source code.',
      objectives: [
        'Write a 3-stage Dockerfile: `deps`, `builder`, and `runtime` stages',
        'Verify final production image size is under 80MB using `docker images`',
        'Run `docker scan` or `trivy image` to check for CVEs before publishing',
        'Tag and push the verified image to Docker Hub with `v1.0.0` semantic tag'
      ],
      snippetLabel: 'Dockerfile.production',
      codeSnippet: `# Stage 1: Resolve dependencies (cached separately for fast rebuilds)
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Stage 2: Compile and package the JAR
FROM deps AS builder
COPY src ./src
RUN mvn package -DskipTests -q
# Output: target/app.jar (~35MB compiled fat-jar)

# Stage 3: Minimal JRE-only production runtime (no JDK/compiler)
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup --system appgrp && adduser --system --ingroup appgrp app
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
USER app
EXPOSE 8080
CMD ["java", "-Xmx512m", "-jar", "app.jar"]`
    },
    {
      id: 'build-lab-advanced',
      title: 'GitHub Actions Build Pipeline with Trivy & Harbor Push Lab',
      tabTitle: '⚡ Advanced Lab: CI Build Pipeline',
      level: 'Advanced',
      duration: '3.0 Hours',
      difficulty: 'Advanced CI/CD Engineering',
      prerequisites: 'GitHub account, Docker Hub or Harbor registry access, YAML syntax familiarity.',
      desc: 'Build a complete GitHub Actions CI pipeline that compiles, scans for CVEs with Trivy, signs images with Cosign, and publishes to Harbor. The pipeline fails automatically if any CRITICAL vulnerability is found.',
      objectives: [
        'Configure GitHub Actions `on: push` trigger for feature and main branches',
        'Add Maven dependency caching with `~/.m2` cache key based on `pom.xml` hash',
        'Run `aquasec/trivy-action` with `exit-code: 1` for CRITICAL/HIGH severity CVEs',
        'Sign published Docker image using `sigstore/cosign-installer` keyless signing'
      ],
      snippetLabel: '.github/workflows/build-and-publish.yml',
      codeSnippet: `name: "CI Build & Secure Publish"
on:
  push:
    branches: [main, "feat/**"]
  pull_request:
    branches: [main]
jobs:
  build-and-publish:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
      id-token: write # Required for Cosign keyless signing
    steps:
      - uses: actions/checkout@v4
      - name: Cache Maven dependencies
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: maven-\${{ hashFiles('**/pom.xml') }}
      - name: Build & package with Maven
        run: mvn package -DskipTests -q
      - name: Build Docker image
        run: |
          docker build -t harbor.internal/app:\${{ github.sha }} .
      - name: Scan for CVEs with Trivy
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: harbor.internal/app:\${{ github.sha }}
          severity: "CRITICAL,HIGH"
          exit-code: "1" # Block pipeline on vulnerabilities
      - name: Sign image with Cosign (keyless)
        uses: sigstore/cosign-installer@v3
        run: |
          cosign sign harbor.internal/app:\${{ github.sha }}`
    }
  ],
  prevNav: { slug: 'code', label: 'Code Phase', sublabel: 'Stage 02 of 08' },
  nextNav: { slug: 'test', label: 'Test Phase', sublabel: 'Stage 04 of 08' }
};
