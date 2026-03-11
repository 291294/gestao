import { useEffect, useState, useCallback } from "react";
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, IconButton, Chip, Tooltip,
} from "@mui/material";
import { DataGrid, GridToolbar } from "@mui/x-data-grid";
import { Add, Edit, Delete } from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";

export default function Inventory() {
  const { companyId } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ productId: "", warehouseLocation: "", quantityOnHand: 0, minStockLevel: "", maxStockLevel: "", unitCost: "" });
  const [saving, setSaving] = useState(false);

  const fetchData = useCallback(() => {
    setLoading(true);
    api.get(`/inventory/company/${companyId}`)
      .then((r) => setRows(r.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [companyId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleOpen = (row = null) => {
    if (row) {
      setEditing(row);
      setForm({
        productId: row.productId || "", warehouseLocation: row.warehouseLocation || "",
        quantityOnHand: row.quantityOnHand || 0, minStockLevel: row.minStockLevel || "",
        maxStockLevel: row.maxStockLevel || "", unitCost: row.unitCost || "",
      });
    } else {
      setEditing(null);
      setForm({ productId: "", warehouseLocation: "", quantityOnHand: 0, minStockLevel: "", maxStockLevel: "", unitCost: "" });
    }
    setOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = { companyId, productId: Number(form.productId), warehouseLocation: form.warehouseLocation, quantityOnHand: Number(form.quantityOnHand), minStockLevel: form.minStockLevel ? Number(form.minStockLevel) : null, maxStockLevel: form.maxStockLevel ? Number(form.maxStockLevel) : null, unitCost: form.unitCost ? Number(form.unitCost) : null };
      if (editing) await api.put(`/inventory/${editing.id}`, payload);
      else await api.post("/inventory", payload);
      setOpen(false);
      fetchData();
    } catch (err) { alert(err.response?.data?.message || "Erro ao salvar"); }
    finally { setSaving(false); }
  };

  const stockChip = (row) => {
    if (row.quantityOnHand === 0) return <Chip label="Sem Estoque" color="error" size="small" />;
    if (row.lowStock || (row.minStockLevel && row.quantityOnHand <= row.minStockLevel)) return <Chip label="Baixo" color="warning" size="small" />;
    return <Chip label="Normal" color="success" size="small" />;
  };

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "productName", headerName: "Produto", flex: 1, minWidth: 180 },
    { field: "warehouseLocation", headerName: "Localização", width: 150 },
    { field: "quantityOnHand", headerName: "Em Mãos", width: 100, type: "number" },
    { field: "quantityReserved", headerName: "Reservado", width: 100, type: "number" },
    { field: "quantityAvailable", headerName: "Disponível", width: 100, type: "number" },
    { field: "minStockLevel", headerName: "Mín.", width: 80, type: "number" },
    {
      field: "status", headerName: "Status", width: 130,
      renderCell: (p) => stockChip(p.row),
    },
    {
      field: "actions", headerName: "Ações", width: 100, sortable: false, filterable: false,
      renderCell: (p) => (
        <Box>
          <Tooltip title="Editar"><IconButton size="small" onClick={() => handleOpen(p.row)}><Edit fontSize="small" /></IconButton></Tooltip>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Estoque</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => handleOpen()}>Novo Item</Button>
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
        <DialogTitle>{editing ? "Editar Item de Estoque" : "Novo Item de Estoque"}</DialogTitle>
        <DialogContent dividers>
          <TextField label="Product ID" fullWidth margin="normal" type="number" value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })} required />
          <TextField label="Localização" fullWidth margin="normal" value={form.warehouseLocation} onChange={(e) => setForm({ ...form, warehouseLocation: e.target.value })} />
          <TextField label="Quantidade em Mãos" fullWidth margin="normal" type="number" value={form.quantityOnHand} onChange={(e) => setForm({ ...form, quantityOnHand: e.target.value })} />
          <Box display="flex" gap={2}>
            <TextField label="Estoque Mínimo" fullWidth margin="normal" type="number" value={form.minStockLevel} onChange={(e) => setForm({ ...form, minStockLevel: e.target.value })} />
            <TextField label="Estoque Máximo" fullWidth margin="normal" type="number" value={form.maxStockLevel} onChange={(e) => setForm({ ...form, maxStockLevel: e.target.value })} />
          </Box>
          <TextField label="Custo Unitário" fullWidth margin="normal" type="number" value={form.unitCost} onChange={(e) => setForm({ ...form, unitCost: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? "Salvando..." : "Salvar"}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
