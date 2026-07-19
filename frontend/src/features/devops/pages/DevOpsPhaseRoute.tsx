import { useParams, Navigate } from 'react-router-dom';
import { DevOpsPhaseDetailPage } from './DevOpsPhaseDetailPage';

export function DevOpsPhaseRoute() {
  const { phaseKey } = useParams<{ phaseKey: string }>();
  
  if (!phaseKey) {
    return <Navigate to="/" replace />;
  }

  const normalizedKey = phaseKey.toLowerCase();

  return <DevOpsPhaseDetailPage phaseKey={normalizedKey} />;
}
