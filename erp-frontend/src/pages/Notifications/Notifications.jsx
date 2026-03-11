import { useEffect, useState } from "react";
import {
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
  Paper,
  Chip,
  CircularProgress,
} from "@mui/material";
import {
  Inventory as StockIcon,
  ShoppingCart as OrderIcon,
  Receipt as InvoiceIcon,
  EmojiEvents as TargetIcon,
  MarkEmailRead as ReadIcon,
} from "@mui/icons-material";
import api from "../../api/apiClient";
import { useAuth } from "../../auth/AuthContext";

const typeConfig = {
  LOW_STOCK: { icon: <StockIcon />, color: "warning" },
  ORDER_CREATED: { icon: <OrderIcon />, color: "info" },
  INVOICE_OVERDUE: { icon: <InvoiceIcon />, color: "error" },
  SALES_TARGET_REACHED: { icon: <TargetIcon />, color: "success" },
};

export default function Notifications() {
  const { companyId } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get(`/notifications/unread?companyId=${companyId}`)
      .then((res) => setNotifications(res.data))
      .catch(() => setNotifications([]))
      .finally(() => setLoading(false));
  }, []);

  const markRead = (id) => {
    api.post(`/notifications/${id}/read`).then(() => {
      setNotifications((prev) => prev.filter((n) => n.id !== id));
    });
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" mt={10}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" mb={3}>
        Notificações
      </Typography>

      {notifications.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: "center", border: "1px solid #e0e0e0" }} elevation={0}>
          <Typography color="text.secondary">Nenhuma notificação pendente</Typography>
        </Paper>
      ) : (
        <Paper elevation={0} sx={{ border: "1px solid #e0e0e0" }}>
          <List>
            {notifications.map((n) => {
              const cfg = typeConfig[n.type] || { icon: <StockIcon />, color: "default" };
              return (
                <ListItem
                  key={n.id}
                  divider
                  secondaryAction={
                    <IconButton edge="end" onClick={() => markRead(n.id)} title="Marcar como lida">
                      <ReadIcon />
                    </IconButton>
                  }
                >
                  <ListItemIcon>{cfg.icon}</ListItemIcon>
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        {n.title}
                        <Chip label={n.type} color={cfg.color} size="small" />
                      </Box>
                    }
                    secondary={n.message}
                  />
                </ListItem>
              );
            })}
          </List>
        </Paper>
      )}
    </Box>
  );
}
