import { useLocation, useNavigate } from "react-router-dom";
import {
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Box,
  Divider,
} from "@mui/material";
import {
  Dashboard as DashboardIcon,
  ShoppingCart as OrdersIcon,
  Inventory as InventoryIcon,
  Warehouse as WarehouseIcon,
  PrecisionManufacturing as ManufacturingIcon,
  Receipt as InvoiceIcon,
  Payments as PaymentsIcon,
  Notifications as NotificationsIcon,
  BarChart as AnalyticsIcon,
  People as ClientsIcon,
  RequestQuote as QuotesIcon,
  Category as ProductsIcon,
  LocalShipping as DeliveriesIcon,
  Percent as CommissionsIcon,
  TrackChanges as TargetsIcon,
} from "@mui/icons-material";
import { useAuth } from "../auth/AuthContext";

const DRAWER_WIDTH = 260;

const menuItems = [
  { text: "Dashboard", icon: <DashboardIcon />, path: "/" },
  { text: "Clientes", icon: <ClientsIcon />, path: "/clientes" },
  { text: "Produtos", icon: <ProductsIcon />, path: "/produtos" },
  { text: "Orçamentos", icon: <QuotesIcon />, path: "/orcamentos" },
  { text: "Pedidos", icon: <OrdersIcon />, path: "/pedidos" },
  { text: "Estoque", icon: <InventoryIcon />, path: "/estoque" },
  { text: "Armazéns", icon: <WarehouseIcon />, path: "/armazens" },
  { text: "Produção", icon: <ManufacturingIcon />, path: "/producao" },
  { text: "Entregas", icon: <DeliveriesIcon />, path: "/entregas" },
  { text: "Faturamento", icon: <InvoiceIcon />, path: "/faturamento" },
  { text: "Pagamentos", icon: <PaymentsIcon />, path: "/pagamentos" },
  { text: "Comissões", icon: <CommissionsIcon />, path: "/comissoes" },
  { text: "Metas de Vendas", icon: <TargetsIcon />, path: "/metas" },
  { text: "Notificações", icon: <NotificationsIcon />, path: "/notificacoes" },
  { text: "Analytics", icon: <AnalyticsIcon />, path: "/analytics" },
];

export { DRAWER_WIDTH };

export default function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { checkRoute } = useAuth();

  const visibleItems = menuItems.filter((item) => checkRoute(item.path));

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: DRAWER_WIDTH,
          boxSizing: "border-box",
          background: "linear-gradient(180deg, #1a237e 0%, #0d1b3e 100%)",
          color: "#fff",
          borderRight: "none",
        },
      }}
    >
      <Toolbar sx={{ px: 3, py: 2 }}>
        <Box>
          <Typography variant="h6" fontWeight="bold" letterSpacing={1}>
            ERP Móveis
          </Typography>
          <Typography variant="caption" sx={{ opacity: 0.6 }}>
            Sistema de Gestão
          </Typography>
        </Box>
      </Toolbar>

      <Divider sx={{ borderColor: "rgba(255,255,255,0.1)" }} />

      <List sx={{ px: 1, mt: 1 }}>
        {visibleItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <ListItemButton
              key={item.text}
              onClick={() => navigate(item.path)}
              sx={{
                borderRadius: 2,
                mb: 0.5,
                backgroundColor: isActive ? "rgba(255,255,255,0.12)" : "transparent",
                "&:hover": { backgroundColor: "rgba(255,255,255,0.08)" },
              }}
            >
              <ListItemIcon sx={{ color: isActive ? "#90caf9" : "rgba(255,255,255,0.6)", minWidth: 40 }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.text}
                primaryTypographyProps={{
                  fontSize: 14,
                  fontWeight: isActive ? 600 : 400,
                  color: isActive ? "#fff" : "rgba(255,255,255,0.7)",
                }}
              />
            </ListItemButton>
          );
        })}
      </List>
    </Drawer>
  );
}
