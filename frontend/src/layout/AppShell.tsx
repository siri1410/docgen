import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';

const nav = [
  { section: 'Build' },
  { to: '/', label: 'Dashboard', icon: '▦', end: true },
  { to: '/templates', label: 'Templates', icon: '🗂' },
  { section: 'Integrate' },
  { to: '/connectors', label: 'API Connectors', icon: '🔌' },
  { section: 'Data' },
  { to: '/settings', label: 'Settings', icon: '⚙' },
];

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="df-shell">
      <aside className="df-sidebar">
        <h1>doc<span className="df-brand-accent">gen</span></h1>
        {nav.map((item, i) =>
          'section' in item ? (
            <div key={`s-${i}`} className="df-section-label">{item.section}</div>
          ) : (
            <NavLink key={item.to} to={item.to!} end={item.end}
              className={({ isActive }) => `df-nav ${isActive ? 'active' : ''}`}>
              <span>{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ),
        )}
        <div className="df-spacer" />
        <div className="df-section-label">Field Library</div>
        <div className="df-muted" style={{ padding: '0 8px', fontSize: 12 }}>
          17 field types · registry-driven
        </div>
      </aside>
      <main className="df-main">{children}</main>
    </div>
  );
}
