import type { PhaseData } from '../types/devops.types';

export const deployPhaseData: PhaseData = {
  id: 'deploy',
  name: 'Deploy',
  slug: 'deploy',
  stageNumber: 6,
  tagline: 'Ship software to production reliably, repeatably, and with zero downtime using Kubernetes, ArgoCD, and GitOps automation.',
  summary: 'You mastered the Deploy stage—from zero-downtime blue/green deployments and canary analysis to GitOps with ArgoCD, Helm chart packaging, and Kubernetes rolling update strategies.',
  heroSnippetTitle: 'k8s-production-deployment.yaml · Kubernetes / Docker / Ansible / Terraform',
  heroSnippet: `# Kubernetes Deployment Manifest · Zero-Downtime Rolling Update
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-production
  namespace: enterprise-prod
  annotations:
    provisioned_by: "Terraform-v1.6 · EKS Cluster"
    configured_by: "Ansible-Playbook · Rolling-Update-Task"
spec:
  replicas: 4
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%        # Spin up new Docker containers before terminating old ones
      maxUnavailable: 0    # Zero downtime during deployment cutover
  selector:
    matchLabels:
      app.kubernetes.io/name: payment-service
  template:
    metadata:
      labels:
        app.kubernetes.io/name: payment-service
    spec:
      containers:
        - name: payment-api
          image: devopsbuilder/payment-api:v2.4.0   # Docker Immutable Image
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet: { path: "/actuator/health/readiness", port: 8080 }
            initialDelaySeconds: 10`,
  theme: {
    gradient: 'from-orange-900 via-amber-950 to-slate-900',
    iconBg: 'bg-orange-500',
    badgeBg: 'bg-orange-50 dark:bg-orange-950/60 border-orange-200 dark:border-orange-800',
    badgeText: 'text-orange-700 dark:text-orange-300',
    borderColor: 'border-orange-500',
    accentColor: 'text-orange-600 dark:text-orange-400',
    ctaBg: 'bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600',
    ctaText: 'text-orange-600 dark:text-orange-400'
  },
  curriculum: [
    {
      id: 'deploy-1',
      title: 'Kubernetes Deployments: Pods, ReplicaSets & Rolling Updates',
      category: 'Core Fundamentals',
      duration: '5.0 Hours',
      level: 'Intermediate',
      description: 'Master Kubernetes core resource models—how Pods, ReplicaSets, and Deployments ensure desired-state reconciliation, rolling updates with zero downtime, and automatic Pod restart on failure.',
      tags: ['Kubernetes', 'Pods', 'Deployments', 'Rolling Updates', 'ReplicaSets'],
      objectives: [
        'Write Kubernetes `Deployment` YAML manifests with resource limits, readiness, and liveness probes',
        'Configure rolling update strategy with `maxSurge` and `maxUnavailable` parameters',
        'Execute `kubectl rollout undo` to instantly revert a broken deployment'
      ]
    },
    {
      id: 'deploy-2',
      title: 'Blue/Green & Canary Deployment Strategies',
      category: 'Core Fundamentals',
      duration: '4.5 Hours',
      level: 'Advanced',
      description: 'Implement zero-downtime production deployment strategies. Blue/Green for instant rollback capability, Canary for risk-validated progressive traffic shifts.',
      tags: ['Blue/Green Deployment', 'Canary Release', 'Traffic Splitting', 'Zero Downtime'],
      objectives: [
        'Implement Blue/Green deployments using two parallel Kubernetes Deployments and Service label switching',
        'Configure Istio VirtualService traffic weights for gradual canary traffic promotion (1% → 10% → 100%)',
        'Automate canary health analysis with Argo Rollouts and Prometheus metrics evaluation'
      ]
    },
    {
      id: 'deploy-3',
      title: 'GitOps with ArgoCD & Declarative Infrastructure',
      category: 'Core Fundamentals',
      duration: '5.0 Hours',
      level: 'Advanced',
      description: 'Manage Kubernetes deployments declaratively through Git. ArgoCD continuously reconciles cluster state with the GitOps repository, self-healing any manual drift.',
      tags: ['GitOps', 'ArgoCD', 'Declarative Config', 'Self-Healing', 'ApplicationSet'],
      objectives: [
        'Configure ArgoCD `Application` resources pointing to environment-specific Helm/Kustomize overlays',
        'Enable automated sync with `selfHeal: true` to revert unauthorized manual `kubectl apply` changes',
        'Implement multi-cluster GitOps promotion pipeline: dev → staging → production gates'
      ]
    },
    {
      id: 'deploy-4',
      title: 'Helm Charts: Packaging & Parameterizing Kubernetes Applications',
      category: 'Core Fundamentals',
      duration: '4.0 Hours',
      level: 'Intermediate',
      description: 'Package complex Kubernetes manifests into reusable Helm charts with environment-specific `values.yaml` overrides for dev, staging, and production configuration.',
      tags: ['Helm', 'Helm Charts', 'values.yaml', 'Templates', 'Chart Repository'],
      objectives: [
        'Structure a complete Helm chart with `Chart.yaml`, `values.yaml`, and `templates/` directory',
        'Use Helm template functions (`toJson`, `include`, `required`) for type-safe value rendering',
        'Publish Helm charts to an OCI-compliant registry (Harbor, Artifactory) with semantic versions'
      ]
    },
    {
      id: 'deploy-5',
      title: 'Progressive Delivery with Argo Rollouts & Analysis Templates',
      category: 'Advanced Practices',
      duration: '4.5 Hours',
      level: 'Advanced',
      description: 'Replace standard Kubernetes Deployments with Argo Rollouts for automated canary analysis using real-time Prometheus metrics to decide whether to promote or abort.',
      tags: ['Argo Rollouts', 'Progressive Delivery', 'Analysis Templates', 'Prometheus'],
      objectives: [
        'Write `Rollout` resources with canary steps (setWeight, pause, analysis)',
        'Define `AnalysisTemplate` querying Prometheus for error rate and P99 latency SLOs',
        'Configure automatic rollback when success rate drops below 99.5% during canary phase'
      ]
    }
  ],
  tools: [
    {
      name: 'ArgoCD',
      category: 'Orchestration & IaC',
      description: 'CNCF-graduated declarative GitOps continuous delivery controller for Kubernetes that continuously syncs cluster state with Git repository configurations.',
      industryStandard: true,
      documentationUrl: 'https://argo-cd.readthedocs.io/',
      internalLink: '/tutorials/argocd-gitops-mastery'
    },
    {
      name: 'Kubernetes & kubectl',
      category: 'Orchestration & IaC',
      description: 'The industry-standard container orchestration platform managing deployment scheduling, self-healing, autoscaling, and service networking.',
      industryStandard: true,
      documentationUrl: 'https://kubernetes.io/docs/'
    },
    {
      name: 'Helm',
      category: 'Orchestration & IaC',
      description: 'The Kubernetes package manager enabling reusable, versioned application packaging with environment-specific value overrides.',
      industryStandard: true,
      documentationUrl: 'https://helm.sh/docs/'
    },
    {
      name: 'Docker & Container Runtimes',
      category: 'Artifacts & Containers',
      description: 'The foundational containerization technology and runtime formats deploying software packages with isolated system libraries across any target host.',
      industryStandard: true,
      documentationUrl: 'https://docs.docker.com/'
    },
    {
      name: 'Ansible Automation',
      category: 'Orchestration & IaC',
      description: 'Agentless IT automation tool executing deployment playbooks, server configuration, and orchestrating rolling zero-downtime updates across production clusters.',
      industryStandard: true,
      documentationUrl: 'https://docs.ansible.com/'
    },
    {
      name: 'Terraform & Pulumi',
      category: 'Orchestration & IaC',
      description: 'Infrastructure-as-Code tools for provisioning cloud resources (EKS clusters, VPCs, RDS databases) in a declarative, version-controlled manner.',
      industryStandard: true,
      documentationUrl: 'https://developer.hashicorp.com/terraform/docs'
    }
  ],
  learningPath: [
    {
      stepNumber: 1,
      title: 'Deploy Your First App on Kubernetes with kubectl',
      duration: '2 Days',
      category: 'Core Fundamentals',
      description: 'Write a complete Kubernetes Deployment and Service manifest. Deploy a containerized Spring Boot app to a local `kind` cluster and expose it via NodePort.',
      keyTakeaway: 'A Deployment does not run containers—it manages ReplicaSets which manage Pods. Understanding this hierarchy is fundamental.'
    },
    {
      stepNumber: 2,
      title: 'Package the App as a Helm Chart with Multi-Env Values',
      duration: '2 Days',
      category: 'Core Fundamentals',
      description: 'Convert raw YAML manifests into a parameterized Helm chart with separate `values-dev.yaml` and `values-prod.yaml` files.',
      keyTakeaway: 'Helm charts transform configuration from duplication to parameterization—the difference between copy-paste and engineering.'
    },
    {
      stepNumber: 3,
      title: 'Set Up ArgoCD and Wire the GitOps Repository',
      duration: '2 Days',
      category: 'Core Fundamentals',
      description: 'Install ArgoCD into the cluster, create an Application pointing to your Helm chart in Git, and observe auto-sync and self-healing behavior.',
      keyTakeaway: 'In GitOps, the Git repository is the single source of truth—never `kubectl apply` directly to production.'
    },
    {
      stepNumber: 4,
      title: 'Implement Blue/Green Zero-Downtime Switch',
      duration: '2 Days',
      category: 'Advanced Practices',
      description: 'Deploy two parallel Deployment versions (v1-blue, v2-green) and switch the Service selector label to activate the new version with zero downtime.',
      keyTakeaway: 'Blue/Green provides instant, risk-free rollback in seconds—critical for high-stakes production migrations.'
    },
    {
      stepNumber: 5,
      title: 'Configure Canary with Argo Rollouts + Prometheus Analysis',
      duration: '3 Days',
      category: 'Advanced Practices',
      description: 'Replace the Blue/Green Deployment with an Argo Rollouts `Rollout` resource that automatically evaluates P99 latency SLOs during canary promotion.',
      keyTakeaway: 'Automated canary analysis removes human judgment from production promotion decisions—replacing gut feeling with real data.'
    }
  ],
  quiz: [
    {
      question: 'In Kubernetes, what is the relationship between a Deployment, ReplicaSet, and a Pod?',
      options: [
        'They are all identical objects with different names used in different cloud providers',
        'A Deployment manages ReplicaSets (ensuring desired replica count), and each ReplicaSet manages identical Pods (the actual running containers)',
        'A Pod contains multiple Deployments stacked inside each other for redundancy',
        'ReplicaSets are only used in production, while Deployments are only used in development'
      ],
      correctIndex: 1,
      explanation: 'The hierarchy is: Deployment → ReplicaSet → Pod. A Deployment handles rolling update strategy by creating new ReplicaSets during updates. ReplicaSets ensure the desired number of identical Pod replicas are always running.',
      difficulty: 'Intermediate'
    },
    {
      question: 'What is the fundamental principle behind GitOps that differentiates it from traditional push-based CI/CD?',
      options: [
        'GitOps requires all infrastructure to be running on a single cloud provider',
        'In GitOps, the desired system state is declared in Git, and a controller (ArgoCD) continuously compares live cluster state against Git—automatically reconciling any drift',
        'GitOps eliminates the need for any Kubernetes knowledge from development teams',
        'GitOps means developers directly SSH into production servers to deploy updates'
      ],
      correctIndex: 1,
      explanation: 'Traditional CI/CD pushes changes to the cluster imperatively. GitOps pulls changes declaratively—ArgoCD continuously watches the Git repository and reconciles the cluster to match, providing automatic drift detection and self-healing.',
      difficulty: 'Advanced'
    }
  ],
  handsOnLabs: [
    {
      id: 'deploy-lab-basic',
      title: 'First Kubernetes Deployment with Helm Chart Lab',
      tabTitle: '📌 Basic Lab: K8s + Helm',
      level: 'Intermediate',
      duration: '2.0 Hours',
      difficulty: 'Intermediate K8s Practical',
      prerequisites: '`kubectl` installed, `kind` or `minikube` cluster running locally.',
      desc: 'Deploy a Dockerized Spring Boot application to a local Kubernetes cluster using a custom Helm chart. Configure CPU/memory resource limits, liveness probes, and ClusterIP service exposure.',
      objectives: [
        'Write a complete `deployment.yaml` template with `resources.limits` and `readinessProbe`',
        'Create `values.yaml` with configurable `image.tag`, `replicaCount`, and `service.port`',
        'Execute `helm install my-app ./helm-chart --values values-dev.yaml` and verify pods are Running',
        'Test rolling update by changing `image.tag` and running `helm upgrade`'
      ],
      snippetLabel: 'helm-chart/templates/deployment.yaml',
      codeSnippet: `apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Release.Name }}-app
  labels:
    app.kubernetes.io/name: {{ .Chart.Name }}
    app.kubernetes.io/version: {{ .Values.image.tag }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      app: {{ .Release.Name }}-app
  template:
    spec:
      containers:
        - name: app
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          ports:
            - containerPort: {{ .Values.service.targetPort }}
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 10`
    },
    {
      id: 'deploy-lab-advanced',
      title: 'ArgoCD GitOps + Canary Rollout Lab',
      tabTitle: '⚡ Advanced Lab: ArgoCD GitOps',
      level: 'Advanced',
      duration: '3.0 Hours',
      difficulty: 'Advanced GitOps Engineering',
      prerequisites: 'Running Kubernetes cluster, ArgoCD installed, GitHub repository for GitOps configs.',
      desc: 'Set up a complete GitOps workflow where pushing Helm chart updates to a Git repository automatically triggers ArgoCD sync and a canary rollout with automated Prometheus health analysis.',
      objectives: [
        'Install ArgoCD and expose the UI via port-forward on the local cluster',
        'Configure an ArgoCD `Application` CRD pointing to `helm/overlays/production/` in the GitOps repository',
        'Convert the Helm `Deployment` to an Argo `Rollout` with 3-step canary: 25% → 50% → 100%',
        'Define a Prometheus `AnalysisTemplate` that automatically fails the rollout if error rate > 1%'
      ],
      snippetLabel: 'argo-rollout.yaml',
      codeSnippet: `apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: payment-service
spec:
  replicas: 10
  strategy:
    canary:
      steps:
        - setWeight: 10   # 10% of traffic → new version
        - pause: { duration: 2m }
        - analysis:
            templates:
              - templateName: prometheus-error-rate-check
        - setWeight: 50   # 50% traffic if analysis passed
        - pause: { duration: 3m }
        - setWeight: 100  # Full promotion
      analysis:
        startingStep: 2
---
apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata:
  name: prometheus-error-rate-check
spec:
  metrics:
    - name: error-rate
      successCondition: result[0] <= 0.01  # Max 1% error rate
      provider:
        prometheus:
          address: http://prometheus-server:9090
          query: rate(http_requests_total{status=~"5.."}[2m])`
    }
  ],
  prevNav: { slug: 'release', label: 'Release Phase', sublabel: 'Stage 05 of 08' },
  nextNav: { slug: 'operate', label: 'Operate Phase', sublabel: 'Stage 07 of 08' }
};
