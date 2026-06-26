import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './layout/AppShell';
import { Dashboard } from './pages/Dashboard';
import { TemplateList } from './pages/TemplateList';
import { TemplateBuilder } from './pages/TemplateBuilder';
import { FormPreviewPage } from './pages/FormPreviewPage';
import { ConnectorSetup } from './pages/ConnectorSetup';
import { FieldMappingPage } from './pages/FieldMappingPage';
import { SubmissionsPage } from './pages/SubmissionsPage';
import { AdminSettings } from './pages/AdminSettings';

export default function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/templates" element={<TemplateList />} />
        <Route path="/templates/:id/build" element={<TemplateBuilder />} />
        <Route path="/templates/:id/preview" element={<FormPreviewPage />} />
        <Route path="/templates/:id/mappings" element={<FieldMappingPage />} />
        <Route path="/templates/:id/submissions" element={<SubmissionsPage />} />
        <Route path="/connectors" element={<ConnectorSetup />} />
        <Route path="/settings" element={<AdminSettings />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppShell>
  );
}
