import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, type FieldInput, type TemplateInput } from '../api/client';
import type { FieldMapping } from '../types';

export function useTemplates() {
  return useQuery({ queryKey: ['templates'], queryFn: api.listTemplates });
}

export function useTemplate(id: string | undefined) {
  return useQuery({
    queryKey: ['template', id],
    queryFn: () => api.getTemplate(id!),
    enabled: !!id,
  });
}

export function useFieldTypes() {
  return useQuery({ queryKey: ['field-types'], queryFn: api.fieldTypes });
}

export function useConnectors() {
  return useQuery({ queryKey: ['connectors'], queryFn: api.listConnectors });
}

export function useMappings(templateId: string | undefined) {
  return useQuery({
    queryKey: ['mappings', templateId],
    queryFn: () => api.listMappings(templateId!),
    enabled: !!templateId,
  });
}

// Lazily fetches version history and toggles its visibility in the builder.
export function useTemplateVersionsToggle(templateId: string | undefined) {
  const [open, setOpen] = useState(false);
  const query = useQuery({
    queryKey: ['versions', templateId],
    queryFn: () => api.versions(templateId!),
    enabled: !!templateId && open,
  });
  return { open, toggle: () => setOpen((o) => !o), data: query.data };
}

export function useSubmissions(templateId: string | undefined) {
  return useQuery({
    queryKey: ['submissions', templateId],
    queryFn: () => api.listSubmissions(templateId!),
    enabled: !!templateId,
  });
}

export function useCreateTemplate() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: TemplateInput) => api.createTemplate(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['templates'] }),
  });
}

export function useTemplateActions(templateId: string | undefined) {
  const qc = useQueryClient();
  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['template', templateId] });
    qc.invalidateQueries({ queryKey: ['templates'] });
  };
  return {
    addField: useMutation({
      mutationFn: (body: FieldInput) => api.addField(templateId!, body),
      onSuccess: invalidate,
    }),
    updateField: useMutation({
      mutationFn: ({ fieldId, body }: { fieldId: string; body: FieldInput }) =>
        api.updateField(templateId!, fieldId, body),
      onSuccess: invalidate,
    }),
    deleteField: useMutation({
      mutationFn: (fieldId: string) => api.deleteField(templateId!, fieldId),
      onSuccess: invalidate,
    }),
    publish: useMutation({
      mutationFn: (notes?: string) => api.publishTemplate(templateId!, notes),
      onSuccess: invalidate,
    }),
    clone: useMutation({
      mutationFn: (name?: string) => api.cloneTemplate(templateId!, name),
      onSuccess: invalidate,
    }),
    saveMappings: useMutation({
      mutationFn: (mappings: FieldMapping[]) => api.saveMappings(templateId!, mappings),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['mappings', templateId] }),
    }),
  };
}
