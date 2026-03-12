import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, IconButton, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Edit, Delete } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";
import { useSnackbar } from "../../components/SnackbarProvider";

export default function Products() {
  const { checkPermission } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: "", material: "", color: "", basePrice: "" });
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const canCreate = checkPermission("product", "create");
  const canEdit = checkPermission("product", "update");
  const canDelete = checkPermission("product", "delete");

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get("/products").then((r) => setRows(r.data || [])).catch(() => setRows([])).finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleOpen = (row = null) => {
    if (row) {
      setEditing(row);
      setForm({ name: row.name || "", material: row.material || "", color: row.color || "", basePrice: row.basePrice || "" });
    } else {
      setEditing(null);
      setForm({ name: "", material: "", color: "", basePrice: "" });
    }
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = { ...form, basePrice: form.basePrice ? Number(form.basePrice) : null };
      if (editing) await api.put(`/products/${editing.id}`, payload);
      else await api.post("/products", payload);
      setOpen(false);
      showSuccess(editing ? "Produto atualizado com sucesso!" : "Produto criado com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao salvar"); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Excluir este produto?")) return;
    try { await api.delete(`/products/${id}`); showSuccess("Produto excluído com sucesso!"); fetchData(); } catch (err) { showError(err.response?.data?.message || "Erro"); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "name", headerName: "Nome", flex: 1, minWidth: 200 },
    { field: "material", headerName: "Material", width: 160 },
    { field: "color", headerName: "Cor", width: 120 },
    {
      field: "basePrice", headerName: "Preço Base", width: 140,
      valueFormatter: (v) => v != null ? `R$ ${Number(v).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}` : "—",
    },
    {
      field: "actions", headerName: "Ações", width: 120, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          {canEdit && <Tooltip title="Editar"><IconButton size="small" onClick={() => handleOpen(p.row)}><Edit fontSize="small" /></IconButton></Tooltip>}
          {canDelete && <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => handleDelete(p.row.id)}><Delete fontSize="small" /></IconButton></Tooltip>}
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Produtos</Typography>
        {canCreate && <Button variant="contained" startIcon={<Add />} onClick={() => handleOpen()}>Novo Produto</Button>}
      </Box>
      <Box sx={{ height: 600, bgcolor: "#fff", borderRadius: 2 }}>
        <DataGrid rows={rows} columns={columns} loading={loading}
          pageSizeOptions={[10, 25, 50]} initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick slots={{ toolbar: GridToolbar }}
          slotProps={{ toolbar: { showQuickFilter: true, quickFilterProps: { debounceMs: 300 } } }}
          sx={{ border: "1px solid #e0e0e0" }}
        />
      </Box>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? "Editar Produto" : "Novo Produto"}</DialogTitle>
        <DialogContent dividers>
          <TextField label="Nome" fullWidth margin="normal" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <TextField label="Material" fullWidth margin="normal" value={form.material} onChange={(e) => setForm({ ...form, material: e.target.value })} />
          <TextField label="Cor" fullWidth margin="normal" value={form.color} onChange={(e) => setForm({ ...form, color: e.target.value })} />
          <TextField label="Preço Base (R$)" fullWidth margin="normal" type="number" value={form.basePrice} onChange={(e) => setForm({ ...form, basePrice: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? "Salvando..." : "Salvar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
