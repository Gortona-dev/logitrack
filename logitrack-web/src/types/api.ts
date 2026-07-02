export type DeliveryStatus =
  | "PENDING"
  | "ASSIGNED"
  | "PICKED_UP"
  | "IN_TRANSIT"
  | "DELIVERED"
  | "CANCELLED";

export type DeliveryPersonStatus = "AVAILABLE" | "UNAVAILABLE" | "ON_DELIVERY";

export type VehicleStatus = "AVAILABLE" | "IN_USE" | "MAINTENANCE";

export type Role = "ADMIN" | "OPERADOR" | "ENTREGADOR" | "CLIENTE";

export interface ApiResponse<T> {
  timestamp: string;
  success: boolean;
  message: string;
  data: T;
}

export interface ApiErrorResponse {
  timestamp: string;
  success: false;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: Array<{ field: string; message: string }>;
}

export interface Client {
  id: string;
  code: string;
  name: string;
  email: string;
  document: string;
  phone: string;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

export interface DeliveryPerson {
  id: string;
  code: string;
  name: string;
  email: string;
  document: string;
  phone: string;
  status: DeliveryPersonStatus;
  active: boolean;
  assignedVehicle: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Vehicle {
  id: string;
  code: string;
  licensePlate: string;
  brand: string;
  model: string;
  active: boolean;
  status: VehicleStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Order {
  id: string;
  trackingCode: string;
  clientId: string;
  clientCode: string;
  clientName: string;
  pickupAddress: string;
  deliveryAddress: string;
  description: string;
  deliveryId: string;
  status: DeliveryStatus;
  deliveryPersonId: string | null;
  deliveryPersonCode: string | null;
  vehicleId: string | null;
  vehicleCode: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Delivery {
  id: string;
  orderId: string;
  deliveryPersonId: string | null;
  vehicleId: string | null;
  status: DeliveryStatus;
  createdAt: string;
  updatedAt: string;
}

export interface DeliveryHistory {
  id: string;
  deliveryId: string;
  previousStatus: DeliveryStatus | null;
  newStatus: DeliveryStatus;
  notes: string | null;
  changedAt: string;
}

export interface LoginResponse {
  token: string;
  role: Role;
}

export interface MeResponse {
  id: string;
  name: string;
  email: string;
  role: Role;
  document: string | null;
  phone: string | null;
  clientId: string | null;
  deliveryPersonId: string | null;
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  document: string | null;
  phone: string | null;
  clientId: string | null;
  deliveryPersonId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardDelivery {
  orderId: string;
  clientName: string;
  deliveryAddress: string;
  status: DeliveryStatus;
  updatedAt: string;
}

export interface DashboardOverview {
  pending: number;
  assigned: number;
  inTransit: number;
  deliveredToday: number;
  cancelled: number;
  driversAvailable: number;
  driversBusy: number;
  activeVehicles: number;
  vehiclesInMaintenance: number;
  latestDeliveries: DashboardDelivery[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
