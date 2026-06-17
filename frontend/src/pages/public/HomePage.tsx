import { Hero, DevOpsLifecycle, FeaturedTutorials, FeaturedProjects, FeaturedRoadmaps } from '../../components/public';

export function HomePage() {
  return (
    <div>
      <Hero />
      <DevOpsLifecycle />
      <FeaturedTutorials />
      <FeaturedProjects />
      <FeaturedRoadmaps />
    </div>
  );
}
