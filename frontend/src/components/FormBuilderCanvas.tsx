import { useState } from 'react';
import {
  DndContext, PointerSensor, closestCenter, useSensor, useSensors,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext, arrayMove, useSortable, verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { descriptorFor } from '../fields/registry';
import type { FieldType, FormField } from '../types';

interface Props {
  fields: FormField[];
  selectedId?: string;
  onSelect: (id: string) => void;
  onAddType: (type: FieldType) => void;
  onReorder: (orderedIds: string[]) => void;
  onDelete: (id: string) => void;
}

function SortableField({ field, selected, onSelect, onDelete }: {
  field: FormField; selected: boolean; onSelect: () => void; onDelete: () => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: field.id });
  const style = { transform: CSS.Transform.toString(transform), transition };
  const d = descriptorFor(field.type);
  return (
    <div ref={setNodeRef} style={style}
      className={`df-canvas-field ${selected ? 'selected' : ''}`} onClick={onSelect}>
      <div className="df-fhead">
        <span>
          <span className="df-drag-handle" {...attributes} {...listeners}>⠿</span>
          <strong>{d.icon} {field.label}</strong>
          <span className="df-muted"> · {field.type.toLowerCase()}{field.required ? ' · required' : ''}</span>
        </span>
        <button className="df-btn ghost" onClick={(e) => { e.stopPropagation(); onDelete(); }}>✕</button>
      </div>
    </div>
  );
}

export function FormBuilderCanvas({ fields, selectedId, onSelect, onAddType, onReorder, onDelete }: Props) {
  const [over, setOver] = useState(false);
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 4 } }));

  const onDragEnd = (e: DragEndEvent) => {
    const { active, over: target } = e;
    if (target && active.id !== target.id) {
      const ids = fields.map((f) => f.id);
      const next = arrayMove(ids, ids.indexOf(String(active.id)), ids.indexOf(String(target.id)));
      onReorder(next);
    }
  };

  return (
    <div
      className={`df-canvas ${over ? 'dragover' : ''}`}
      onDragOver={(e) => { e.preventDefault(); setOver(true); }}
      onDragLeave={() => setOver(false)}
      onDrop={(e) => {
        setOver(false);
        const type = e.dataTransfer.getData('text/field-type') as FieldType;
        if (type) onAddType(type);
      }}
    >
      {fields.length === 0 && (
        <div className="df-muted" style={{ textAlign: 'center', padding: 40 }}>
          Drag fields here from the palette, or click a field type to add it.
        </div>
      )}
      <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={onDragEnd}>
        <SortableContext items={fields.map((f) => f.id)} strategy={verticalListSortingStrategy}>
          {fields.map((f) => (
            <SortableField key={f.id} field={f} selected={f.id === selectedId}
              onSelect={() => onSelect(f.id)} onDelete={() => onDelete(f.id)} />
          ))}
        </SortableContext>
      </DndContext>
    </div>
  );
}
