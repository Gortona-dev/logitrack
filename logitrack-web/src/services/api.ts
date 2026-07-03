import type {
  ApiErrorResponse,
  ApiResponse,
  Client,
  Delivery,
  DeliveryHistory,
  DeliveryStatus,
  DeliveryPerson,
  DashboardOverview,
  LoginResponse,
  MeResponse,
  Order,
  PageResponse,
  Role,
  User,
  Vehicle,
} from "../types/api";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const fieldLabels: Record<string, string> = {
  brand: "marca",
  clientId: "cliente",
  deliveryAddress: "endereço de entrega",
  deliveryPersonId: "entregador",
  description: "descrição",
  document: "documento",
  email: "email",
  licensePlate: "placa",
  model: "modelo",
  name: "nome",
  notes: "observações",
  phone: "telefone",
  pickupAddress: "endereço de coleta",
  status: "status",
  vehicleId: "veículo",
};

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = window.localStorage.getItem("logitrack.token");
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  const text = await response.text();
  let payload: ApiResponse<T> | ApiErrorResponse | null = null;

  try {
    payload = text ? (JSON.parse(text) as ApiResponse<T> | ApiErrorResponse) : null;
  } catch {
    payload = null;
  }

  if (!response.ok || !payload?.success) {
    const errorPayload = payload as ApiErrorResponse;
    const fieldMessage = errorPayload?.fieldErrors
      ?.map((item) => `${fieldLabels[item.field] ?? item.field}: ${item.message}`)
      .join("; ");
    throw new Error(fieldMessage || errorPayload?.message || `Falha na requisição (${response.status})`);
  }

  return (payload as ApiResponse<T>).data;
}

export const api = {
  login: (body: { email: string; password: string }) =>
    request<LoginResponse>("/api/v1/auth/login", { method: "POST", body: JSON.stringify(body) }),
  me: () => request<MeResponse>("/api/v1/auth/me"),
  updateMe: (body: { name: string; document?: string; phone?: string }) =>
    request<MeResponse>("/api/v1/auth/me", { method: "PATCH", body: JSON.stringify(body) }),

  listUsers: () => request<User[]>("/api/v1/users"),
  createUser: (body: { name: string; email: string; password: string; role: Role; document?: string; phone?: string }) =>
    request<User>("/api/v1/users", { method: "POST", body: JSON.stringify(body) }),
  updateUser: (id: string, body: { name: string; email: string; password?: string; document?: string; phone?: string }) =>
    request<User>(`/api/v1/users/${id}`, { method: "PATCH", body: JSON.stringify(body) }),

  getDashboard: () => request<DashboardOverview>("/api/v1/dashboard"),

  listClients: () => request<Client[]>("/api/v1/clients"),
  listClientsPage: (filters: { search?: string; page?: number; size?: number }) => {
    const params = new URLSearchParams();
    if (filters.search) params.set("search", filters.search);
    params.set("page", String(filters.page ?? 0));
    params.set("size", String(filters.size ?? 10));
    return request<PageResponse<Client>>(`/api/v1/clients/page?${params.toString()}`);
  },
  createClient: (body: { name: string; email: string; document: string; phone: string }) =>
    request<Client>("/api/v1/clients", { method: "POST", body: JSON.stringify(body) }),
  updateClient: (id: string, body: { name: string; email: string; document: string; phone: string }) =>
    request<Client>(`/api/v1/clients/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
  deleteClient: (id: string) => request<void>(`/api/v1/clients/${id}`, { method: "DELETE" }),

  listDeliveryPersons: () => request<DeliveryPerson[]>("/api/v1/delivery-persons"),
  listDeliveryPersonsPage: (filters: { search?: string; page?: number; size?: number }) => {
    const params = new URLSearchParams();
    if (filters.search) params.set("search", filters.search);
    params.set("page", String(filters.page ?? 0));
    params.set("size", String(filters.size ?? 10));
    return request<PageResponse<DeliveryPerson>>(`/api/v1/delivery-persons/page?${params.toString()}`);
  },
  createDeliveryPerson: (body: { name: string; email: string; document: string; phone: string }) =>
    request<DeliveryPerson>("/api/v1/delivery-persons", { method: "POST", body: JSON.stringify(body) }),
  updateDeliveryPerson: (id: string, body: { name: string; email: string; document: string; phone: string }) =>
    request<DeliveryPerson>(`/api/v1/delivery-persons/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
  deleteDeliveryPerson: (id: string) => request<void>(`/api/v1/delivery-persons/${id}`, { method: "DELETE" }),

  listVehicles: () => request<Vehicle[]>("/api/v1/vehicles"),
  listVehiclesPage: (filters: { search?: string; page?: number; size?: number }) => {
    const params = new URLSearchParams();
    if (filters.search) params.set("search", filters.search);
    params.set("page", String(filters.page ?? 0));
    params.set("size", String(filters.size ?? 10));
    return request<PageResponse<Vehicle>>(`/api/v1/vehicles/page?${params.toString()}`);
  },
  createVehicle: (body: { licensePlate: string; brand: string; model: string }) =>
    request<Vehicle>("/api/v1/vehicles", { method: "POST", body: JSON.stringify(body) }),
  updateVehicle: (id: string, body: { licensePlate: string; brand: string; model: string }) =>
    request<Vehicle>(`/api/v1/vehicles/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
  deleteVehicle: (id: string) => request<void>(`/api/v1/vehicles/${id}`, { method: "DELETE" }),

  listOrders: (filters: { clientId?: string; deliveryPersonId?: string; status?: string }) => {
    const params = new URLSearchParams();
    if (filters.clientId) params.set("clientId", filters.clientId);
    if (filters.deliveryPersonId) params.set("deliveryPersonId", filters.deliveryPersonId);
    if (filters.status) params.set("status", filters.status);
    const query = params.toString();
    return request<Order[]>(`/api/v1/orders${query ? `?${query}` : ""}`);
  },
  createOrder: (body: {
    clientId: string;
    pickupAddress: string;
    deliveryAddress: string;
    description: string;
  }) => request<Order>("/api/v1/orders", { method: "POST", body: JSON.stringify(body) }),

  assignDelivery: (orderId: string, body: { deliveryPersonId: string; vehicleId: string }) =>
    request<Delivery>(`/api/v1/deliveries/${orderId}/assign`, { method: "POST", body: JSON.stringify(body) }),

  updateDeliveryStatus: (deliveryId: string, body: { status: DeliveryStatus; notes?: string }) =>
    request<Delivery>(`/api/v1/deliveries/${deliveryId}/status`, { method: "PATCH", body: JSON.stringify(body) }),

  getDeliveryHistory: (deliveryId: string) => request<DeliveryHistory[]>(`/api/v1/deliveries/${deliveryId}/history`),
};
