import { useEffect, useState } from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Badge,
  Box,
  Avatar,
  Tooltip,
} from "@mui/material";
import {
  Notifications as NotificationsIcon,
  AccountCircle,
  Logout as LogoutIcon,
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { DRAWER_WIDTH } from "./Sidebar";
import api from "../api/apiClient";

export default function Topbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    api
      .get("/notifications/unread?companyId=1")
      .then((res) => setUnreadCount(res.data?.length || 0))
      .catch(() => {});
  }, []);

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const initials = user?.fullName
    ? user.fullName
        .split(" ")
        .map((w) => w[0])
        .slice(0, 2)
        .join("")
        .toUpperCase()
    : "U";

  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        width: `calc(100% - ${DRAWER_WIDTH}px)`,
        ml: `${DRAWER_WIDTH}px`,
        backgroundColor: "#fff",
        borderBottom: "1px solid #e0e0e0",
        color: "#333",
      }}
    >
      <Toolbar sx={{ justifyContent: "space-between" }}>
        <Typography variant="h6" fontWeight={500} color="text.secondary">
          Painel Administrativo
        </Typography>

        <Box display="flex" alignItems="center" gap={1}>
          <Tooltip title="Notificações">
            <IconButton onClick={() => navigate("/notificacoes")}>
              <Badge badgeContent={unreadCount} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </Tooltip>

          <Box display="flex" alignItems="center" gap={1} ml={1}>
            <Avatar sx={{ width: 32, height: 32, bgcolor: "#1a237e", fontSize: 14 }}>
              {initials}
            </Avatar>
            <Typography variant="body2" fontWeight={500}>
              {user?.fullName || user?.username || "Usuário"}
            </Typography>
          </Box>

          <Tooltip title="Sair">
            <IconButton onClick={handleLogout} color="inherit">
              <LogoutIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
