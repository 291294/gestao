// Role-based permission definitions
// Maps roles to allowed route paths and permission strings
const ROLE_PERMISSIONS = {
  ADMIN: { routes: "*", permissions: "*" },
  GERENTE: {
    routes: [
      "/", "/clientes", "/produtos", "/orcamentos", "/pedidos", "/estoque",
      "/armazens", "/producao", "/entregas", "/faturamento", "/pagamentos",
      "/comissoes", "/metas", "/notificacoes", "/analytics",
    ],
    permissions: "*",
  },
  VENDEDOR: {
    routes: ["/", "/clientes", "/produtos", "/orcamentos", "/pedidos", "/comissoes", "/metas", "/notificacoes"],
    permissions: [
      "client.list", "client.view", "client.create", "client.update",
      "product.list", "product.view",
      "order.list", "order.view", "order.create", "order.update",
    ],
  },
  FINANCEIRO: {
    routes: ["/", "/faturamento", "/pagamentos", "/comissoes", "/analytics", "/notificacoes"],
    permissions: [
      "invoice.list", "invoice.view", "invoice.create", "invoice.generate",
      "payment.list", "payment.view", "payment.create", "payment.approve",
      "commission.list", "commission.view", "commission.approve",
    ],
  },
  LOGISTICA: {
    routes: ["/", "/estoque", "/armazens", "/entregas", "/notificacoes"],
    permissions: [
      "delivery.list", "delivery.view", "delivery.create", "delivery.schedule", "delivery.complete",
      "inventory.list", "inventory.view",
    ],
  },
  PRODUCAO: {
    routes: ["/", "/producao", "/produtos", "/estoque", "/armazens", "/notificacoes"],
    permissions: ["report.production", "product.list", "product.view"],
  },
};

export function hasRouteAccess(roles, path) {
  if (!roles || roles.length === 0) return false;
  return roles.some((role) => {
    const config = ROLE_PERMISSIONS[role];
    if (!config) return false;
    if (config.routes === "*") return true;
    return config.routes.includes(path);
  });
}

export function hasPermission(userPermissions, requiredPermission) {
  if (!userPermissions || userPermissions.length === 0) return false;
  return userPermissions.includes(requiredPermission);
}

export function canPerform(roles, userPermissions, resource, action) {
  if (!roles || roles.length === 0) return false;
  if (roles.includes("ADMIN")) return true;
  const perm = `${resource}.${action}`;
  return hasPermission(userPermissions, perm);
}
