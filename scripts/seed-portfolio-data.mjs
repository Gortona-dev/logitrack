const API = process.env.LOGITRACK_API_URL ?? "https://logitrack-api-etd1.onrender.com";
const ADMIN_EMAIL = process.env.LOGITRACK_ADMIN_EMAIL ?? "admin@logitrack.com";
const ADMIN_PASSWORD = process.env.LOGITRACK_ADMIN_PASSWORD ?? "admin123";
const seed = Date.now().toString().slice(-6);

async function request(path, options = {}, token) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 25_000);

  const response = await fetch(`${API}${path}`, {
    ...options,
    signal: controller.signal,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
  }).finally(() => clearTimeout(timeout));
  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok || !payload?.success) {
    throw new Error(`${options.method ?? "GET"} ${path} -> ${response.status}: ${payload?.message ?? text}`);
  }

  return payload.data;
}

function digits(base, index) {
  return `${base}${seed}${String(index).padStart(2, "0")}`.replace(/\D/g, "").slice(0, 14);
}

function phone(index) {
  return `1198${seed.slice(0, 3)}${String(index).padStart(4, "0")}`.slice(0, 11);
}

function plate(index) {
  const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const a = letters[(Number(seed[0]) + index) % 26];
  const b = letters[(Number(seed[1]) + index * 2) % 26];
  const c = letters[(Number(seed[2]) + index * 3) % 26];
  const number = (Number(seed.slice(-3)) + index * 37) % 10000;

  return `${a}${b}${c}${String(number).padStart(4, "0")}`;
}

async function createClients(token) {
  const clients = [];

  for (let index = 1; index <= 10; index++) {
    clients.push(await request("/api/v1/clients", {
      method: "POST",
      body: JSON.stringify({
        name: `Cliente Portfolio ${index}`,
        email: `cliente.portfolio.${seed}.${index}@logitrack.com`,
        document: digits("10", index),
        phone: phone(index),
      }),
    }, token));
  }

  return clients;
}

async function createDeliveryPersons(token) {
  const deliveryPersons = [];

  for (let index = 1; index <= 10; index++) {
    deliveryPersons.push(await request("/api/v1/delivery-persons", {
      method: "POST",
      body: JSON.stringify({
        name: `Entregador Portfolio ${index}`,
        email: `entregador.portfolio.${seed}.${index}@logitrack.com`,
        document: digits("20", index),
        phone: phone(index + 20),
      }),
    }, token));
  }

  return deliveryPersons;
}

async function createVehicles(token) {
  const brands = ["Fiat", "Volkswagen", "Renault", "Chevrolet", "Ford"];
  const models = ["Fiorino", "Saveiro", "Kangoo", "Montana", "Transit"];
  const vehicles = [];

  for (let index = 1; index <= 10; index++) {
    vehicles.push(await request("/api/v1/vehicles", {
      method: "POST",
      body: JSON.stringify({
        licensePlate: plate(index),
        brand: brands[(index - 1) % brands.length],
        model: `${models[(index - 1) % models.length]} ${index}`,
      }),
    }, token));
  }

  return vehicles;
}

async function moveDelivery(deliveryId, status, notes, token) {
  return request(`/api/v1/deliveries/${deliveryId}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status, notes }),
  }, token);
}

async function createOrdersAndDeliveries(clients, deliveryPersons, vehicles, token) {
  const finalPlan = [
    ...Array(10).fill("DELIVERED"),
    ...Array(5).fill("CANCELLED"),
    ...Array(4).fill("IN_TRANSIT"),
    ...Array(3).fill("PICKED_UP"),
    ...Array(3).fill("ASSIGNED"),
  ];
  const orders = [];
  let reusableIndex = 0;
  let activeIndex = 0;

  for (let index = 1; index <= 25; index++) {
    const statusGoal = finalPlan[index - 1];
    const client = clients[(index - 1) % clients.length];
    const order = await request("/api/v1/orders", {
      method: "POST",
      body: JSON.stringify({
        clientId: client.id,
        pickupAddress: `Centro de Distribuicao ${((index - 1) % 5) + 1}, Sao Paulo - SP`,
        deliveryAddress: `Rua Logistica ${100 + index}, Bairro ${((index - 1) % 6) + 1}, Sao Paulo - SP`,
        description: `Pedido de portfolio ${seed}-${String(index).padStart(2, "0")}`,
      }),
    }, token);

    const person = statusGoal === "DELIVERED" || statusGoal === "CANCELLED"
      ? deliveryPersons[reusableIndex % 3]
      : deliveryPersons[activeIndex];
    const vehicle = statusGoal === "DELIVERED" || statusGoal === "CANCELLED"
      ? vehicles[reusableIndex % 3]
      : vehicles[activeIndex];

    if (statusGoal === "DELIVERED" || statusGoal === "CANCELLED") {
      reusableIndex++;
    } else {
      activeIndex++;
    }

    const delivery = await request(`/api/v1/deliveries/${order.id}/assign`, {
      method: "POST",
      body: JSON.stringify({ deliveryPersonId: person.id, vehicleId: vehicle.id }),
    }, token);

    if (["PICKED_UP", "IN_TRANSIT", "DELIVERED"].includes(statusGoal)) {
      await moveDelivery(delivery.id, "PICKED_UP", "Coleta realizada pelo seed de portfolio", token);
    }

    if (["IN_TRANSIT", "DELIVERED"].includes(statusGoal)) {
      await moveDelivery(delivery.id, "IN_TRANSIT", "Entrega em rota pelo seed de portfolio", token);
    }

    if (statusGoal === "DELIVERED") {
      await moveDelivery(delivery.id, "DELIVERED", "Entrega concluida pelo seed de portfolio", token);
    }

    if (statusGoal === "CANCELLED") {
      await moveDelivery(delivery.id, "CANCELLED", "Cancelamento simulado pelo seed de portfolio", token);
    }

    console.log(`[seed ${seed}] pedido ${index}/25 criado como ${statusGoal}`);
    orders.push({ trackingCode: order.trackingCode, statusGoal });
  }

  return orders;
}

function countByStatus(orders) {
  return orders.reduce((accumulator, order) => {
    accumulator[order.statusGoal] = (accumulator[order.statusGoal] ?? 0) + 1;
    return accumulator;
  }, {});
}

const login = await request("/api/v1/auth/login", {
  method: "POST",
  body: JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD }),
});

console.log(`[seed ${seed}] login OK`);
const clients = await createClients(login.token);
console.log(`[seed ${seed}] clientes criados: ${clients.length}`);
const deliveryPersons = await createDeliveryPersons(login.token);
console.log(`[seed ${seed}] entregadores criados: ${deliveryPersons.length}`);
const vehicles = await createVehicles(login.token);
console.log(`[seed ${seed}] veiculos criados: ${vehicles.length}`);
const orders = await createOrdersAndDeliveries(clients, deliveryPersons, vehicles, login.token);
console.log(`[seed ${seed}] pedidos e entregas criados: ${orders.length}`);
const dashboard = await request("/api/v1/dashboard", {}, login.token);

console.log(JSON.stringify({
  api: API,
  seed,
  created: {
    clients: clients.length,
    deliveryPersons: deliveryPersons.length,
    vehicles: vehicles.length,
    orders: orders.length,
  },
  deliveryPlan: countByStatus(orders),
  dashboard: {
    pending: dashboard.pending,
    assigned: dashboard.assigned,
    pickedUp: dashboard.pickedUp,
    inTransit: dashboard.inTransit,
    delivered: dashboard.delivered,
    deliveredToday: dashboard.deliveredToday,
    cancelled: dashboard.cancelled,
    driversAvailable: dashboard.driversAvailable,
    driversBusy: dashboard.driversBusy,
    activeVehicles: dashboard.activeVehicles,
    vehiclesAvailable: dashboard.vehiclesAvailable,
    vehiclesInUse: dashboard.vehiclesInUse,
  },
}, null, 2));
