import type { PhaseData } from '../types/devops.types';

export const testPhaseData: PhaseData = {
  id: 'test',
  name: 'Test',
  slug: 'test',
  stageNumber: 4,
  tagline: 'Validate every change against a comprehensive automated testing pyramid before it can ever reach production customers.',
  summary: 'You have mastered the Test stage—from writing unit tests with 90%+ coverage, integration tests against real databases, end-to-end browser automation, and performance load testing under simulated production traffic.',
  heroSnippetTitle: 'CucumberTestRunner.java · Selenium / JUnit / SonarQube',
  heroSnippet: `// JUnit 5 & Cucumber BDD Step Definition with Selenium WebDriver
@CucumberOptions(features = "src/test/resources/features", glue = "com.devops.stepdefs")
public class PaymentCheckoutTest {
    private WebDriver driver = new ChromeDriver(new SeleniumGridOptions());

    @Given("the user is on the checkout page with {int} items")
    public void navigateToCheckout(int itemCount) {
        driver.get("https://staging.devopsbuilder.com/checkout");
        Assertions.assertEquals(itemCount, Integer.parseInt(driver.findElement(By.id("cart-count")).getText()));
    }

    @When("the user submits a valid payment card")
    public void submitCard() {
        driver.findElement(By.id("submit-pay")).click();
    }

    @Then("SonarQube quality gate verifies 0 security hotspots and >90% coverage")
    public void verifyQualityGate() {
        // Automated assertion against SonarQube API before allowing merge
        SonarQubeClient.verifyGateStatus("payment-service", QualityGateStatus.PASSED);
    }
}`,
  theme: {
    gradient: 'from-fuchsia-900 via-purple-950 to-slate-900',
    iconBg: 'bg-purple-600',
    badgeBg: 'bg-purple-50 dark:bg-purple-950/60 border-purple-200 dark:border-purple-800',
    badgeText: 'text-purple-700 dark:text-purple-300',
    borderColor: 'border-purple-500',
    accentColor: 'text-purple-600 dark:text-purple-400',
    ctaBg: 'bg-gradient-to-r from-purple-600 to-fuchsia-600 hover:from-purple-700 hover:to-fuchsia-700',
    ctaText: 'text-purple-600 dark:text-purple-400'
  },
  curriculum: [
    {
      id: 'test-1',
      title: 'The Testing Pyramid: Unit, Integration & E2E Strategy',
      category: 'Core Fundamentals',
      duration: '3.5 Hours',
      level: 'Beginner',
      description: 'Understand the layered testing pyramid philosophy—why you need many fast unit tests at the base, fewer integration tests in the middle, and minimal expensive E2E tests at the peak.',
      tags: ['Testing Pyramid', 'Test Strategy', 'Unit Tests', 'Integration Tests', 'E2E'],
      objectives: [
        'Design test suite ratios following the 70/20/10 pyramid rule',
        'Distinguish between testing scope, speed, and confidence trade-offs',
        'Identify which test type catches which category of software defect'
      ]
    },
    {
      id: 'test-2',
      title: 'Unit Testing with Vitest/Jest & 90% Code Coverage',
      category: 'Core Fundamentals',
      duration: '5.0 Hours',
      level: 'Intermediate',
      description: 'Write deterministic, fast unit tests isolating individual functions and classes using mocking (vi.mock, jest.spyOn). Target 90% branch coverage across all service-layer code.',
      tags: ['Vitest', 'Jest', 'Test Coverage', 'Mocking', 'TDD'],
      objectives: [
        'Apply the AAA pattern (Arrange-Act-Assert) for every test case',
        'Mock external dependencies (HTTP clients, databases) using `vi.fn()` and `jest.spyOn()`',
        'Generate Istanbul HTML coverage reports and enforce thresholds via `vitest --coverage`'
      ]
    },
    {
      id: 'test-3',
      title: 'Integration Testing with TestContainers & Real Databases',
      category: 'Core Fundamentals',
      duration: '4.5 Hours',
      level: 'Intermediate',
      description: 'Run integration tests against real Docker-containerized PostgreSQL, Redis, and Kafka instances using TestContainers—eliminating fake in-memory mocks that miss real database quirks.',
      tags: ['TestContainers', 'PostgreSQL', 'Redis', 'JUnit 5', 'Integration Testing'],
      objectives: [
        'Spin up ephemeral real PostgreSQL containers within JUnit 5 test lifecycle',
        'Test database transaction rollback, optimistic locking, and constraint violations',
        'Verify Kafka producer/consumer message contracts with embedded brokers'
      ]
    },
    {
      id: 'test-4',
      title: 'End-to-End Browser Testing with Playwright',
      category: 'Core Fundamentals',
      duration: '4.0 Hours',
      level: 'Advanced',
      description: 'Automate critical user journeys (Login → Checkout → Order Confirmation) across Chromium, Firefox, and WebKit browsers using Playwright\'s modern async API.',
      tags: ['Playwright', 'E2E Testing', 'Browser Automation', 'Page Object Model'],
      objectives: [
        'Write resilient selectors using `getByRole`, `getByText`, `getByTestId` (never CSS classes)',
        'Implement Page Object Model (POM) pattern to eliminate test code duplication',
        'Configure `playwright.config.ts` for multi-browser and viewport matrix testing'
      ]
    },
    {
      id: 'test-5',
      title: 'Performance & Load Testing with k6 & Grafana',
      category: 'Advanced Practices',
      duration: '4.0 Hours',
      level: 'Advanced',
      description: 'Simulate production-scale traffic loads using k6 scripts—ramp up to 10,000 virtual users—and visualize live performance metrics on Grafana dashboards.',
      tags: ['k6', 'Load Testing', 'Performance Engineering', 'Grafana', 'Virtual Users'],
      objectives: [
        'Write k6 test scripts defining user scenarios, think times, and ramp-up patterns',
        'Assert P95 latency and error rate SLO thresholds within k6 `checks` and `thresholds`',
        'Stream k6 metrics to InfluxDB and visualize real-time on pre-built Grafana dashboards'
      ]
    },
    {
      id: 'test-6',
      title: 'Contract Testing with Pact & Consumer-Driven Contracts',
      category: 'Advanced Practices',
      duration: '4.0 Hours',
      level: 'Enterprise',
      description: 'Prevent API contract breakage in microservices architectures using Pact consumer-driven contract testing—ensuring providers never silently break their consumer APIs.',
      tags: ['Pact Framework', 'Contract Testing', 'Microservices', 'API Contracts', 'Pact Broker'],
      objectives: [
        'Write consumer Pact tests that generate contract files describing expected API shapes',
        'Verify provider implementations against consumer contracts in CI without needing deployed environments',
        'Publish and share contracts via Pact Broker with version tagging'
      ]
    }
  ],
  tools: [
    {
      name: 'Vitest & Jest',
      category: 'Testing & QA',
      description: 'Lightning-fast unit testing frameworks with built-in coverage, watch mode, and TypeScript support for JavaScript/TypeScript projects.',
      industryStandard: true,
      documentationUrl: 'https://vitest.dev/',
      internalLink: '/tutorials/vitest-testing-mastery'
    },
    {
      name: 'Playwright & Selenium',
      category: 'Testing & QA',
      description: 'Modern browser automation frameworks for E2E testing across real browser engines with parallel test execution.',
      industryStandard: true,
      documentationUrl: 'https://playwright.dev/'
    },
    {
      name: 'JUnit & Cucumber',
      category: 'Testing & QA',
      description: 'Industry standards for enterprise Java unit testing and Behavior-Driven Development (BDD) enabling natural-language specifications executable as tests.',
      industryStandard: true,
      documentationUrl: 'https://cucumber.io/'
    },
    {
      name: 'SonarQube Security & QA',
      category: 'Security',
      description: 'Continuous inspection platform that performs automated static code reviews to detect bugs, code smells, vulnerabilities, and security hotspots.',
      industryStandard: true,
      documentationUrl: 'https://www.sonarsource.com/products/sonarqube/'
    },
    {
      name: 'TestContainers',
      category: 'Testing & QA',
      description: 'Java/Python/Go library that spins up real Docker containers (PostgreSQL, Redis, Kafka) during test execution for realistic integration testing.',
      industryStandard: true,
      documentationUrl: 'https://testcontainers.com/'
    },
    {
      name: 'k6 & Grafana',
      category: 'Observability & SRE',
      description: 'Grafana k6 is the open-source load testing tool designed for DevOps teams—scriptable in JavaScript with native Grafana dashboard integration.',
      industryStandard: true,
      documentationUrl: 'https://k6.io/'
    }
  ],
  learningPath: [
    {
      stepNumber: 1,
      title: 'Set Up Vitest with 90% Coverage Threshold',
      duration: '1 Day',
      category: 'Core Fundamentals',
      description: 'Configure Vitest with Istanbul coverage and enforce minimum 90% branch coverage as a CI build gate.',
      keyTakeaway: 'Coverage without assertions is meaningless—every tested line must have a meaningful `expect()` assertion.'
    },
    {
      stepNumber: 2,
      title: 'Write Integration Tests with Real PostgreSQL via TestContainers',
      duration: '2 Days',
      category: 'Core Fundamentals',
      description: 'Replace in-memory H2 database with real containerized PostgreSQL using TestContainers for accurate database behavior testing.',
      keyTakeaway: 'In-memory databases like H2 silently ignore real PostgreSQL features like jsonb, pg_trgm, and row-level locks.'
    },
    {
      stepNumber: 3,
      title: 'Automate Critical User Journeys with Playwright',
      duration: '3 Days',
      category: 'Core Fundamentals',
      description: 'Write Page Object Model-based Playwright tests covering login, checkout, and profile update journeys in Chromium and Firefox.',
      keyTakeaway: 'E2E tests should only cover happy-path critical journeys—not every edge case (that is what unit tests are for).'
    },
    {
      stepNumber: 4,
      title: 'Execute k6 Load Tests at 500 Virtual Users',
      duration: '2 Days',
      category: 'Advanced Practices',
      description: 'Ramp up k6 load tests to 500 concurrent virtual users and assert P95 API response time stays under 300ms.',
      keyTakeaway: 'Performance regressions should be discovered in CI, not reported by angry customers.'
    },
    {
      stepNumber: 5,
      title: 'Implement Pact Contract Tests Between Services',
      duration: '2 Days',
      category: 'Advanced Practices',
      description: 'Write Pact consumer tests for payment-frontend → payment-api contract and publish contracts to Pact Broker.',
      keyTakeaway: 'Contract testing catches breaking API changes before they break other teams in a microservices architecture.'
    }
  ],
  quiz: [
    {
      question: 'According to the Testing Pyramid, why should there be significantly more unit tests than E2E tests?',
      options: [
        'E2E tests are too expensive to write, so teams avoid them entirely',
        'Unit tests run in milliseconds and test single functions in isolation, while E2E tests are slow, brittle, and require full deployed environments—making them expensive to maintain',
        'There is no difference; the ratio of unit to E2E tests does not matter',
        'E2E tests cannot catch logic bugs, so unit tests replace them entirely'
      ],
      correctIndex: 1,
      explanation: 'The Testing Pyramid reflects cost vs confidence trade-offs. Unit tests are fast, cheap, and catch logic bugs early. E2E tests are slow, flaky, and require full infrastructure—they are valuable but expensive to run and maintain at scale.',
      difficulty: 'Beginner'
    },
    {
      question: 'What critical problem does TestContainers solve that traditional in-memory database mocks cannot?',
      options: [
        'TestContainers generates test data automatically using machine learning',
        'TestContainers spins up real database Docker containers, exposing actual database behavior—including jsonb queries, full-text search, and transaction isolation levels—that H2 in-memory databases silently ignore',
        'TestContainers replaces unit tests entirely and is the only testing tool needed',
        'TestContainers automatically deploys tested applications to production Kubernetes'
      ],
      correctIndex: 1,
      explanation: 'H2 (in-memory Java DB) and SQLite do not support many PostgreSQL-specific features. TestContainers starts a real PostgreSQL Docker container, ensuring your SQL queries, constraints, and database-specific behaviors are actually tested.',
      difficulty: 'Intermediate'
    },
    {
      question: 'In Playwright tests, why should selectors use `getByRole`, `getByText`, or `getByTestId` instead of CSS class selectors like `.checkout-btn`?',
      options: [
        'CSS selectors are slower than role-based selectors by over 1000x',
        'CSS class names are implementation details that designers change frequently; role and semantic selectors reflect the user\'s actual experience and remain stable when visual redesigns happen',
        'CSS selectors are banned in Playwright v2.0 for security reasons',
        'getByRole selectors bypass authentication automatically in test environments'
      ],
      correctIndex: 1,
      explanation: 'Tests coupled to CSS class names break every time a developer renames a class during styling. User-centric selectors like `getByRole(\'button\', { name: \'Place Order\' })` reflect the actual user experience and survive UI redesigns.',
      difficulty: 'Intermediate'
    }
  ],
  handsOnLabs: [
    {
      id: 'test-lab-basic',
      title: 'Vitest Unit Test Suite with 90% Coverage Lab',
      tabTitle: '📌 Basic Lab: Unit Testing',
      level: 'Intermediate',
      duration: '1.5 Hours',
      difficulty: 'Intermediate Practical',
      prerequisites: 'Node.js installed and basic JavaScript/TypeScript familiarity.',
      desc: 'Write a complete Vitest test suite for a shopping cart service with 90%+ code coverage. Apply mocking for HTTP payment calls and use AAA pattern for all test cases.',
      objectives: [
        'Configure Vitest in `vite.config.ts` with Istanbul V8 coverage provider',
        'Write 8+ unit tests covering all branches of `CartService.calculateTotal()`',
        'Mock the Stripe API client using `vi.mock()` to isolate payment logic tests',
        'Achieve and verify 92% branch coverage via `vitest run --coverage`'
      ],
      snippetLabel: 'cart.service.spec.ts',
      codeSnippet: `import { describe, it, expect, vi, beforeEach } from 'vitest';
import { CartService } from './cart.service';

describe('CartService', () => {
  let cart: CartService;

  beforeEach(() => {
    cart = new CartService();
  });

  describe('calculateTotal', () => {
    it('should sum item prices correctly', () => {
      // Arrange
      cart.addItem({ id: '1', name: 'Widget', price: 29.99, qty: 2 });
      cart.addItem({ id: '2', name: 'Gadget', price: 49.99, qty: 1 });

      // Act
      const total = cart.calculateTotal();

      // Assert
      expect(total).toBeCloseTo(109.97);
    });

    it('should apply 10% discount when total exceeds 100', () => {
      cart.addItem({ id: '1', name: 'Premium', price: 150.00, qty: 1 });
      expect(cart.calculateTotal()).toBeCloseTo(135.00);
    });

    it('should throw error for empty cart checkout', () => {
      expect(() => cart.checkout()).toThrow('Cart is empty');
    });
  });
});`
    },
    {
      id: 'test-lab-advanced',
      title: 'Playwright E2E Test Automation with POM Lab',
      tabTitle: '⚡ Advanced Lab: E2E Playwright',
      level: 'Advanced',
      duration: '2.5 Hours',
      difficulty: 'Advanced E2E Engineering',
      prerequisites: 'Node.js, basic TypeScript, and access to the DevOpsBuilder demo app URL.',
      desc: 'Build a production-grade Playwright E2E test suite using the Page Object Model pattern. Test the complete checkout user journey across Chromium, Firefox, and Mobile Safari viewports.',
      objectives: [
        'Create `CheckoutPage` POM class with all checkout form locators as named properties',
        'Write 3 test scenarios: successful checkout, invalid card rejection, and session timeout',
        'Configure `playwright.config.ts` for 3-browser matrix: chromium, firefox, webkit',
        'Capture video recordings and screenshots only on test failure for debugging'
      ],
      snippetLabel: 'checkout.e2e.spec.ts',
      codeSnippet: `import { test, expect } from '@playwright/test';
import { CheckoutPage } from './pages/checkout.page';

test.describe('E2E: Complete Checkout Journey', () => {
  test('should complete purchase with valid Visa card', async ({ page }) => {
    const checkout = new CheckoutPage(page);

    await checkout.goto();
    await checkout.fillShipping({ name: 'Jane Doe', address: '123 Main St' });
    await checkout.fillPayment({ cardNumber: '4242424242424242', expiry: '12/28', cvv: '123' });
    await checkout.clickPlaceOrder();

    await expect(checkout.confirmationBanner).toContainText('Order Confirmed');
    await expect(checkout.orderIdBadge).toBeVisible();
  });

  test('should reject expired credit card gracefully', async ({ page }) => {
    const checkout = new CheckoutPage(page);
    await checkout.goto();
    await checkout.fillPayment({ cardNumber: '4000000000000069', expiry: '01/20', cvv: '111' });
    await checkout.clickPlaceOrder();

    await expect(checkout.errorAlert).toContainText('Card Declined: Expired card');
  });
});`
    }
  ],
  prevNav: { slug: 'build', label: 'Build Phase', sublabel: 'Stage 03 of 08' },
  nextNav: { slug: 'release', label: 'Release Phase', sublabel: 'Stage 05 of 08' }
};
