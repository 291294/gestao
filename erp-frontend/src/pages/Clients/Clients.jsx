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

export default function Clients() {
  const { checkPermission } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: "", email: "", phone: "", profession: "", preferences: "" });
  const [saving, setSaving] = useState(false);
  const { showSuccess, showError } = useSnackbar();

  const canCreate = checkPermission("client", "create");
  const canEdit = checkPermission("client", "update");
  const canDelete = checkPermission("client", "delete");

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get("/clients").then((r) => setRows(r.data || [])).catch(() => setRows([])).finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleOpen = (row = null) => {
    if (row) {
      setEditing(row);
      setForm({ name: row.name || "", email: row.email || "", phone: row.phone || "", profession: row.profession || "", preferences: row.preferences || "" });
    } else {
      setEditing(null);
      setForm({ name: "", email: "", phone: "", profession: "", preferences: "" });
    }
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (editing) await api.put(`/clients/${editing.id}`, form);
      else await api.post("/clients", form);
      setOpen(false);
      showSuccess(editing ? "Cliente atualizado com sucesso!" : "Cliente criado com sucesso!");
      fetchData();
    } catch (err) { showError(err.response?.data?.message || "Erro ao salvar"); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Excluir este cliente?")) return;
    try { await api.delete(`/clients/${id}`); showSuccess("Cliente excluído com sucesso!"); fetchData(); } catch (err) { showError(err.response?.data?.message || "Erro"); }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "name", headerName: "Nome", flex: 1, minWidth: 200 },
    { field: "email", headerName: "Email", flex: 1, minWidth: 200 },
    { field: "phone", headerName: "Telefone", width: 150 },
    { field: "profession", headerName: "Profissão", width: 150 },
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
        <Typography variant="h4" fontWeight="bold">Clientes</Typography>
        {canCreate && <Button variant="contained" startIcon={<Add />} onClick={() => handleOpen()}>Novo Cliente</Button>}
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
        <DialogTitle>{editing ? "Editar Cliente" : "Novo Cliente"}</DialogTitle>
        <DialogContent dividers>
          <TextField label="Nome" fullWidth margin="normal" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <TextField label="Email" fullWidth margin="normal" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <TextField label="Telefone" fullWidth margin="normal" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <TextField label="Profissão" fullWidth margin="normal" value={form.profession} onChange={(e) => setForm({ ...form, profession: e.target.value })} />
          <TextField label="Preferências" fullWidth margin="normal" multiline rows={2} value={form.preferences} onChange={(e) => setForm({ ...form, preferences: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? "Salvando..." : "Salvar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
