import { fieldDescriptors } from '../fields/registry';
import type { FieldType } from '../types';

// Draggable palette of field types, grouped. Uses native HTML5 drag with a
// dataTransfer payload so the canvas can add the dropped type.
export function FieldPalette({ onAdd }: { onAdd: (type: FieldType) => void }) {
  const groups = ['Basic', 'Choice', 'Identity', 'Advanced', 'Layout'] as const;
  return (
    <div className="df-palette">
      {groups.map((group) => (
        <div key={group}>
          <div className="df-palette-group">{group}</div>
          {fieldDescriptors.filter((d) => d.group === group).map((d) => (
            <div
              key={d.type}
              className="df-palette-item"
              draggable
              onDragStart={(e) => e.dataTransfer.setData('text/field-type', d.type)}
              onClick={() => onAdd(d.type)}
              title="Drag onto the canvas or click to add"
            >
              <span>{d.icon}</span>
              <span>{d.label}</span>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
