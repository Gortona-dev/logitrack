import {
  BarChart3,
  Boxes,
  CircleHelp,
  ClipboardList,
  ChevronLeft,
  ChevronRight,
  LogOut,
  MapPinned,
  Moon,
  PackageCheck,
  RefreshCw,
  Route,
  Search,
  Settings,
  ShieldCheck,
  Sun,
  Truck,
  UserCog,
  UserRound,
  UsersRound,
  Warehouse,
  X,
} from "lucide-react";
import { FormEvent, ReactNode, useEffect, useMemo, useState } from "react";
import { api } from "./services/api";
import type {
  Client,
  DashboardOverview,
  DeliveryHistory,
  DeliveryPerson,
  DeliveryStatus,
  MeResponse,
  Order,
  Role,
  User,
  Vehicle,
} from "./types/api";

type Toast = { type: "success" | "error"; message: string } | null;
type Theme = "light" | "dark";
type TourStep = {
  title: string;
  text: string;
  route?: string;
};
type Session = {
  token: string;
  role: Role;
  name: string;
  email: string;
  document: string | null;
  phone: string | null;
  clientId: string | null;
  deliveryPersonId: string | null;
} | null;
type RouteConfig = { path: string; label: string; icon: ReactNode; roles: Role[] };

const statusLabels: Record<DeliveryStatus, string> = {
  PENDING: "Pendente",
  ASSIGNED: "Atribuído",
  PICKED_UP: "Coletado",
  IN_TRANSIT: "Em trânsito",
  DELIVERED: "Entregue",
  CANCELLED: "Cancelado",
};

const roleLabels: Record<Role, string> = {
  ADMIN: "Administrador",
  OPERADOR: "Operador",
  ENTREGADOR: "Entregador",
  CLIENTE: "Cliente",
};

const defaultRouteByRole: Record<Role, string> = {
  ADMIN: "/dashboard",
  OPERADOR: "/pedidos",
  ENTREGADOR: "/minhas-entregas",
  CLIENTE: "/rastreamento",
};

const routes: RouteConfig[] = [
  { path: "/dashboard", label: "Dashboard", icon: <BarChart3 size={18} />, roles: ["ADMIN"] },
  { path: "/clientes", label: "Clientes", icon: <UsersRound size={18} />, roles: ["ADMIN", "OPERADOR"] },
  { path: "/entregadores", label: "Entregadores", icon: <UserRound size={18} />, roles: ["ADMIN", "OPERADOR"] },
  { path: "/veiculos", label: "Veículos", icon: <Truck size={18} />, roles: ["ADMIN", "OPERADOR"] },
  { path: "/pedidos", label: "Pedidos", icon: <ClipboardList size={18} />, roles: ["ADMIN", "OPERADOR"] },
  { path: "/minhas-entregas", label: "Minhas entregas", icon: <Route size={18} />, roles: ["ENTREGADOR"] },
  { path: "/rastreamento", label: "Rastreamento", icon: <MapPinned size={18} />, roles: ["CLIENTE"] },
  { path: "/meus-pedidos", label: "Meus pedidos", icon: <PackageCheck size={18} />, roles: ["CLIENTE"] },
  { path: "/usuarios", label: "Usuários", icon: <UserCog size={18} />, roles: ["ADMIN"] },
  { path: "/relatorios", label: "Relatórios", icon: <BarChart3 size={18} />, roles: ["ADMIN"] },
  { path: "/configuracoes", label: "Configurações", icon: <Settings size={18} />, roles: ["ADMIN", "OPERADOR", "ENTREGADOR", "CLIENTE"] },
];

const onboardingStepsByRole: Record<Role, TourStep[]> = {
  ADMIN: [
    {
      title: "Visao geral da operacao",
      text: "O Dashboard resume pedidos, entregadores, veiculos e entregas recentes. Use esta tela para entender rapidamente a saude da operacao.",
      route: "/dashboard",
    },
    {
      title: "Usuarios e acessos",
      text: "Na aba Usuarios voce cadastra administradores, operadores, clientes e entregadores. Clientes e entregadores ganham vinculo operacional automaticamente.",
      route: "/usuarios",
    },
    {
      title: "Cadastro operacional",
      text: "Clientes, entregadores e veiculos ficam separados para facilitar filtros, edicao e manutencao dos dados usados nos pedidos.",
      route: "/clientes",
    },
    {
      title: "Pedidos e entregas",
      text: "Em Pedidos voce cria solicitacoes, atribui entregador e veiculo, acompanha status e consulta o historico completo da entrega.",
      route: "/pedidos",
    },
    {
      title: "Preferencias da conta",
      text: "Em Configuracoes voce atualiza seus dados e alterna entre tema claro e escuro. As preferencias ficam salvas neste navegador.",
      route: "/configuracoes",
    },
  ],
  OPERADOR: [
    {
      title: "Rotina operacional",
      text: "A aba Pedidos e o centro do operador: crie pedidos, filtre registros, atribua entregadores e avance o status da entrega.",
      route: "/pedidos",
    },
    {
      title: "Base de clientes",
      text: "Use Clientes para consultar e editar os dados de quem solicita entregas. A pesquisa filtra enquanto voce digita.",
      route: "/clientes",
    },
    {
      title: "Entregadores disponiveis",
      text: "Em Entregadores voce acompanha disponibilidade, telefone, documento e veiculo vinculado antes de atribuir uma entrega.",
      route: "/entregadores",
    },
    {
      title: "Frota em uso",
      text: "Em Veiculos voce identifica placas, modelos e status. Veiculos em uso ou manutencao nao devem entrar em novas atribuicoes.",
      route: "/veiculos",
    },
    {
      title: "Seu perfil",
      text: "Em Configuracoes ficam tema, nome, documento e telefone do usuario logado.",
      route: "/configuracoes",
    },
  ],
  ENTREGADOR: [
    {
      title: "Suas entregas",
      text: "Minhas entregas mostra somente os pedidos vinculados a voce. Selecione uma entrega para ver rota, status e historico.",
      route: "/minhas-entregas",
    },
    {
      title: "Atualizacao de status",
      text: "Quando uma entrega estiver liberada, avance as etapas como Coletado, Em transito e Entregue. Cada alteracao gera historico.",
      route: "/minhas-entregas",
    },
    {
      title: "Dados pessoais",
      text: "Em Configuracoes voce mantem telefone e documento atualizados para a equipe operacional.",
      route: "/configuracoes",
    },
  ],
  CLIENTE: [
    {
      title: "Rastreamento",
      text: "A aba Rastreamento mostra seus pedidos e o andamento da entrega sem expor dados internos da operacao.",
      route: "/rastreamento",
    },
    {
      title: "Meus pedidos",
      text: "Use Meus pedidos para revisar entregas, rota, status atual e historico de movimentacao.",
      route: "/meus-pedidos",
    },
    {
      title: "Perfil do cliente",
      text: "Em Configuracoes voce atualiza nome, documento, telefone e preferencia de tema.",
      route: "/configuracoes",
    },
  ],
};

const nextStatuses: Partial<Record<DeliveryStatus, DeliveryStatus[]>> = {
  PENDING: ["CANCELLED"],
  ASSIGNED: ["PICKED_UP", "CANCELLED"],
  PICKED_UP: ["IN_TRANSIT", "CANCELLED"],
  IN_TRANSIT: ["DELIVERED", "CANCELLED"],
};

const emptyClient = { name: "", email: "", document: "", phone: "" };
const emptyDeliveryPerson = { name: "", email: "", document: "", phone: "" };
const emptyVehicle = { licensePlate: "", brand: "", model: "" };
const emptyOrder = { clientId: "", pickupAddress: "", deliveryAddress: "", description: "" };
const emptyUser = { name: "", email: "", password: "", role: "CLIENTE" as Role, document: "", phone: "" };

function App() {
  const [session, setSession] = useState<Session>(null);
  const [authChecked, setAuthChecked] = useState(false);
  const [theme, setTheme] = useState<Theme>(() => (window.localStorage.getItem("logitrack.theme") as Theme | null) ?? "light");
  const [path, setPath] = useState(() => window.location.pathname === "/" ? "/dashboard" : window.location.pathname);
  const [toast, setToast] = useState<Toast>(null);
  const [tourOpen, setTourOpen] = useState(false);

  useEffect(() => {
    const onPopState = () => setPath(window.location.pathname);
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem("logitrack.theme", theme);
  }, [theme]);

  useEffect(() => {
    const token = window.localStorage.getItem("logitrack.token");

    if (!token) {
      setAuthChecked(true);
      if (path !== "/login") navigate("/login");
      return;
    }

    api.me()
      .then((me) => {
        setSession(sessionFromMe(token, me));
      })
      .catch(() => {
        window.localStorage.removeItem("logitrack.token");
        window.localStorage.removeItem("logitrack.role");
        setSession(null);
        navigate("/login");
      })
      .finally(() => setAuthChecked(true));
  }, []);

  useEffect(() => {
    if (!authChecked || !session) return;
    const allowed = routes.some((route) => route.path === path && route.roles.includes(session.role));
    if (!allowed) {
      navigate(defaultRouteByRole[session.role]);
    }
  }, [authChecked, session, path]);

  useEffect(() => {
    if (!authChecked || !session) return;
    if (!window.localStorage.getItem(onboardingStorageKey(session.role))) {
      setTourOpen(true);
    }
  }, [authChecked, session?.role]);

  function navigate(nextPath: string) {
    window.history.pushState({}, "", nextPath);
    setPath(nextPath);
  }

  function showToast(type: "success" | "error", message: string) {
    setToast({ type, message });
    window.setTimeout(() => setToast(null), 4200);
  }

  async function login(token: string) {
    window.localStorage.setItem("logitrack.token", token);
    window.localStorage.removeItem("logitrack.role");
    const me = await api.me();
    const nextSession = sessionFromMe(token, me);
    setSession(nextSession);
    navigate(defaultRouteByRole[nextSession.role]);
  }

  function logout() {
    window.localStorage.removeItem("logitrack.token");
    window.localStorage.removeItem("logitrack.role");
    setSession(null);
    setTourOpen(false);
    navigate("/login");
  }

  function startTour() {
    setTourOpen(true);
  }

  function finishTour(markAsSeen = true) {
    if (session && markAsSeen) {
      window.localStorage.setItem(onboardingStorageKey(session.role), "true");
    }
    setTourOpen(false);
  }

  if (!authChecked) {
    return (
      <main className="login-shell">
        <section className="login-panel">
          <div className="brand login-brand">
            <div className="brand-mark"><Boxes size={22} /></div>
            <div>
              <strong>LogiTrack</strong>
              <span>Carregando sessão</span>
            </div>
          </div>
        </section>
      </main>
    );
  }

  if (!session) {
    return <LoginPage onLogin={login} showToast={showToast} toast={toast} />;
  }

  return (
    <div className="app-shell">
      <Sidebar session={session} path={path} navigate={navigate} logout={logout} startTour={startTour} />
      <main className="workspace">
        {toast && <div className={`toast ${toast.type}`}>{toast.message}</div>}
        <PageRouter path={path} role={session.role} session={session} setSession={setSession} theme={theme} setTheme={setTheme} showToast={showToast} />
      </main>
      {tourOpen && (
        <OnboardingTour
          role={session.role}
          currentPath={path}
          navigate={navigate}
          onClose={() => finishTour(false)}
          onFinish={() => finishTour(true)}
        />
      )}
    </div>
  );
}

function sessionFromMe(token: string, me: MeResponse): NonNullable<Session> {
  return {
    token,
    role: me.role,
    name: me.name,
    email: me.email,
    document: me.document,
    phone: me.phone,
    clientId: me.clientId,
    deliveryPersonId: me.deliveryPersonId,
  };
}

function LoginPage(props: {
  onLogin: (token: string) => Promise<void>;
  showToast: (type: "success" | "error", message: string) => void;
  toast: Toast;
}) {
  const [form, setForm] = useState({ email: "admin@logitrack.com", password: "admin123" });
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      const response = await api.login(form);
      await props.onLogin(response.token);
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-shell">
      {props.toast && <div className={`toast ${props.toast.type}`}>{props.toast.message}</div>}
      <section className="login-panel">
        <div className="brand login-brand">
          <div className="brand-mark"><Boxes size={22} /></div>
          <div>
            <strong>LogiTrack</strong>
            <span>SaaS de gestão logística</span>
          </div>
        </div>
        <div>
          <h1>Entrar no sistema</h1>
          <p>Use um perfil de teste para acessar as áreas protegidas da plataforma.</p>
        </div>
        <form className="stack-form" onSubmit={submit}>
          <label>
            Email
            <input value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
          </label>
          <label>
            Senha
            <input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
          </label>
          <button className="primary-button" disabled={loading} type="submit">
            <ShieldCheck size={16} /> {loading ? "Validando..." : "Entrar"}
          </button>
        </form>
        <div className="login-hints">
          <strong>Perfis para teste</strong>
          <span>admin@logitrack.com / admin123</span>
          <span>operador@logitrack.com / operador123</span>
          <span>entregador@logitrack.com / entregador123</span>
          <span>cliente@logitrack.com / cliente123</span>
        </div>
      </section>
    </main>
  );
}

function Sidebar(props: { session: Session; path: string; navigate: (path: string) => void; logout: () => void; startTour: () => void }) {
  const allowedRoutes = routes.filter((route) => props.session && route.roles.includes(props.session.role));

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark"><Boxes size={22} /></div>
        <div>
          <strong>LogiTrack</strong>
          <span>{props.session ? props.session.name : "Usuário"}</span>
        </div>
      </div>

      <nav className="nav">
        {allowedRoutes.map((route) => (
          <button
            className={props.path === route.path ? "active" : ""}
            key={route.path}
            onClick={() => props.navigate(route.path)}
            type="button"
          >
            {route.icon} {route.label}
          </button>
        ))}
      </nav>

      <button className="sidebar-note logout-button" onClick={props.logout} type="button">
        <span>Sessão segura</span>
        <strong><LogOut size={14} /> Sair</strong>
      </button>
      <button className="tour-entry-button" onClick={props.startTour} type="button">
        <CircleHelp size={16} />
        Guia da plataforma
      </button>
    </aside>
  );
}

function OnboardingTour(props: {
  role: Role;
  currentPath: string;
  navigate: (path: string) => void;
  onClose: () => void;
  onFinish: () => void;
}) {
  const steps = onboardingStepsByRole[props.role];
  const [stepIndex, setStepIndex] = useState(0);
  const activeStep = steps[stepIndex];
  const isFirst = stepIndex === 0;
  const isLast = stepIndex === steps.length - 1;

  useEffect(() => {
    if (activeStep.route && activeStep.route !== props.currentPath) {
      props.navigate(activeStep.route);
    }
  }, [activeStep.route, props.currentPath]);

  function previousStep() {
    setStepIndex((current) => Math.max(0, current - 1));
  }

  function nextStep() {
    if (isLast) {
      props.onFinish();
      return;
    }
    setStepIndex((current) => Math.min(steps.length - 1, current + 1));
  }

  return (
    <div className="tour-layer" role="dialog" aria-modal="true" aria-labelledby="tour-title">
      <div className="tour-card">
        <button className="tour-close" type="button" aria-label="Fechar guia" onClick={props.onClose}>
          <X size={16} />
        </button>
        <div className="tour-kicker">Guia do {roleLabels[props.role]}</div>
        <h2 id="tour-title">{activeStep.title}</h2>
        <p>{activeStep.text}</p>
        {activeStep.route && (
          <span className="tour-route">Tela atual: {routes.find((route) => route.path === activeStep.route)?.label ?? activeStep.route}</span>
        )}
        <div className="tour-footer">
          <span>Passo {stepIndex + 1} de {steps.length}</span>
          <div className="tour-actions">
            <button className="ghost-button" type="button" onClick={previousStep} disabled={isFirst}>
              <ChevronLeft size={16} /> Anterior
            </button>
            <button className="primary-button" type="button" onClick={nextStep}>
              {isLast ? "Concluir" : "Proximo"} {!isLast && <ChevronRight size={16} />}
            </button>
          </div>
        </div>
        <button className="tour-skip" type="button" onClick={props.onFinish}>Nao mostrar novamente</button>
      </div>
    </div>
  );
}

function PageRouter(props: {
  path: string;
  role: Role;
  session: NonNullable<Session>;
  setSession: (session: Session) => void;
  theme: Theme;
  setTheme: (theme: Theme) => void;
  showToast: (type: "success" | "error", message: string) => void;
}) {
  if (props.path === "/dashboard") return <DashboardPage showToast={props.showToast} />;
  if (props.path === "/clientes") return <ClientsPage showToast={props.showToast} />;
  if (props.path === "/entregadores") return <DeliveryPersonsPage role={props.role} showToast={props.showToast} />;
  if (props.path === "/veiculos") return <VehiclesPage role={props.role} showToast={props.showToast} />;
  if (props.path === "/pedidos") return <OrdersPage showToast={props.showToast} />;
  if (props.path === "/minhas-entregas") return <OrdersPage showToast={props.showToast} mode="driver" />;
  if (props.path === "/rastreamento") return <TrackingPage showToast={props.showToast} />;
  if (props.path === "/meus-pedidos") return <OrdersPage showToast={props.showToast} mode="client" />;
  if (props.path === "/usuarios") return <UsersPage showToast={props.showToast} />;
  if (props.path === "/relatorios") return <PlaceholderPage title="Relatórios" text="Relatórios gerenciais ficam preparados para a próxima evolução da Sprint 2." />;
  return <SettingsPage session={props.session} setSession={props.setSession} theme={props.theme} setTheme={props.setTheme} showToast={props.showToast} />;
}

function UsersPage(props: { showToast: (type: "success" | "error", message: string) => void }) {
  const [users, setUsers] = useState<User[]>([]);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(emptyUser);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    try {
      setUsers(await api.listUsers());
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      if (editingId) {
        await api.updateUser(editingId, {
          name: form.name,
          email: form.email,
          password: form.password || undefined,
          document: form.document || undefined,
          phone: form.phone || undefined,
        });
        props.showToast("success", "Usuário atualizado");
      } else {
        await api.createUser({
          ...form,
          document: form.document || undefined,
          phone: form.phone || undefined,
        });
        props.showToast("success", "Usuário criado com acesso ao sistema");
      }
      setForm(emptyUser);
      setEditingId(null);
      await loadUsers();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  function startEditUser(user: User) {
    setEditingId(user.id);
    setForm({
      name: user.name,
      email: user.email,
      password: "",
      role: user.role,
      document: user.document ?? "",
      phone: user.phone ?? "",
    });
  }

  const operationalRole = form.role === "CLIENTE" || form.role === "ENTREGADOR";
  const filteredUsers = users.filter((user) =>
    normalized(`${user.name} ${user.email} ${user.role} ${user.document ?? ""} ${user.phone ?? ""}`).includes(normalized(search))
  );

  return (
    <>
      <PageHeader title="Usuários" description="Cadastro de acessos por perfil, com vínculo automático para clientes e entregadores." onRefresh={loadUsers} />
      <section className="content-grid">
        <Panel title="Acessos cadastrados" subtitle={`${filteredUsers.length} usuários`}>
          <SearchBox value={search} onChange={setSearch} placeholder="Pesquisar usuário" />
          <DataTable headers={["Nome", "Email", "Perfil", "CPF", "Telefone", "Vínculo"]}>
            {filteredUsers.map((user) => (
              <tr key={user.id} onClick={() => startEditUser(user)} className={editingId === user.id ? "selected" : ""}>
                <td>{user.name}</td>
                <td>{user.email}</td>
                <td>{roleLabels[user.role]}</td>
                <td>{user.document ?? "--"}</td>
                <td>{user.phone ?? "--"}</td>
                <td>{user.clientId ? "Cliente" : user.deliveryPersonId ? "Entregador" : "Interno"}</td>
              </tr>
            ))}
          </DataTable>
          {!filteredUsers.length && <EmptyState text="Nenhum usuário encontrado." />}
        </Panel>
        <Panel title={editingId ? "Editar usuário" : "Novo usuário"} subtitle="Somente administradores podem gerenciar acessos">
          <form className="stack-form" onSubmit={submit}>
            <input placeholder="Nome" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
            <input placeholder="Email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value.trim().toLowerCase() })} />
            <input type="password" placeholder={editingId ? "Nova senha opcional" : "Senha inicial"} value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
            <select value={form.role} disabled={Boolean(editingId)} onChange={(event) => setForm({ ...form, role: event.target.value as Role })}>
              <option value="CLIENTE">Cliente</option>
              <option value="ENTREGADOR">Entregador</option>
              <option value="OPERADOR">Operador</option>
              <option value="ADMIN">Administrador</option>
            </select>
            <input placeholder={operationalRole ? "CPF/documento obrigatório" : "CPF/documento"} value={form.document} onChange={(event) => setForm({ ...form, document: onlyDigits(event.target.value, 20) })} />
            <input placeholder={operationalRole ? "Telefone obrigatório" : "Telefone"} value={form.phone} onChange={(event) => setForm({ ...form, phone: maskPhone(event.target.value) })} />
            <button className="primary-button" disabled={loading} type="submit">{loading ? "Salvando..." : editingId ? "Salvar usuário" : "Criar usuário"}</button>
            {editingId && <button className="ghost-button" type="button" onClick={() => { setEditingId(null); setForm(emptyUser); }}>Cancelar edição</button>}
          </form>
        </Panel>
      </section>
    </>
  );
}

function SettingsPage(props: {
  session: NonNullable<Session>;
  setSession: (session: Session) => void;
  theme: Theme;
  setTheme: (theme: Theme) => void;
  showToast: (type: "success" | "error", message: string) => void;
}) {
  const [form, setForm] = useState({
    name: props.session.name,
    document: props.session.document ?? "",
    phone: props.session.phone ?? "",
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setForm({
      name: props.session.name,
      document: props.session.document ?? "",
      phone: props.session.phone ?? "",
    });
  }, [props.session.name, props.session.document, props.session.phone]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    try {
      const me = await api.updateMe({
        name: form.name,
        document: form.document || undefined,
        phone: form.phone || undefined,
      });
      const token = window.localStorage.getItem("logitrack.token");
      if (token) {
        props.setSession(sessionFromMe(token, me));
      }
      props.showToast("success", "Configurações salvas");
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <PageHeader title="Configurações" description="Preferências da conta, tema e dados pessoais do usuário logado." />
      <section className="content-grid">
        <Panel title="Meu perfil" subtitle={`${roleLabels[props.session.role]} · ${props.session.email}`}>
          <form className="stack-form" onSubmit={submit}>
            <input placeholder="Nome" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
            <input placeholder="CPF/documento" value={form.document} onChange={(event) => setForm({ ...form, document: onlyDigits(event.target.value, 20) })} />
            <input placeholder="Telefone" value={form.phone} onChange={(event) => setForm({ ...form, phone: maskPhone(event.target.value) })} />
            <button className="primary-button" disabled={saving} type="submit">{saving ? "Salvando..." : "Salvar perfil"}</button>
          </form>
          <DetailList items={[["Email", props.session.email], ["Perfil", roleLabels[props.session.role]], ["Vínculo", props.session.clientId ? "Cliente" : props.session.deliveryPersonId ? "Entregador" : "Usuário interno"]]} />
        </Panel>
        <Panel title="Aparência" subtitle="Preferência salva neste navegador">
          <div className="theme-options">
            <button className={props.theme === "light" ? "theme-option active" : "theme-option"} type="button" onClick={() => props.setTheme("light")}>
              <Sun size={18} />
              <span>Claro</span>
            </button>
            <button className={props.theme === "dark" ? "theme-option active" : "theme-option"} type="button" onClick={() => props.setTheme("dark")}>
              <Moon size={18} />
              <span>Escuro</span>
            </button>
          </div>
        </Panel>
      </section>
    </>
  );
}

function DashboardPage(props: { showToast: (type: "success" | "error", message: string) => void }) {
  const [dashboard, setDashboard] = useState<DashboardOverview | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  async function loadDashboard() {
    setLoading(true);
    try {
      setDashboard(await api.getDashboard());
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  const cards = dashboard ? [
    ["Pedidos totais", dashboard.totalOrders, <ClipboardList size={20} />],
    ["Pedidos pendentes", dashboard.pending, <ClipboardList size={20} />],
    ["Pedidos atribuídos", dashboard.assigned, <Route size={20} />],
    ["Em trânsito", dashboard.inTransit, <Truck size={20} />],
    ["Entregues", dashboard.delivered, <PackageCheck size={20} />],
    ["Entregues hoje", dashboard.deliveredToday, <PackageCheck size={20} />],
    ["Cancelados", dashboard.cancelled, <ClipboardList size={20} />],
    ["Entregadores disponíveis", dashboard.driversAvailable, <UserRound size={20} />],
    ["Entregadores ocupados", dashboard.driversBusy, <UserRound size={20} />],
    ["Veículos ativos", dashboard.activeVehicles, <Truck size={20} />],
    ["Veículos em manutenção", dashboard.vehiclesInMaintenance, <Warehouse size={20} />],
  ] as const : [];

  return (
    <>
      <PageHeader title="Dashboard" description="Visão geral operacional com indicadores reais da API." onRefresh={loadDashboard} loading={loading} />
      <section className="metric-grid wide">
        {cards.map(([label, value, icon]) => <MetricCard key={label} label={label} value={value} icon={icon} />)}
      </section>
      <section className="dashboard-grid">
        <Panel title="Entregas por dia" subtitle="Entregas concluídas nos últimos 7 dias">
          <DailyDeliveriesChart data={dashboard?.deliveredLastSevenDays ?? []} />
        </Panel>
        <Panel title="Distribuição por status" subtitle="Volume atual por etapa da entrega">
          <StatusDistributionChart data={dashboard?.statusDistribution ?? []} />
        </Panel>
        <Panel title="Capacidade operacional" subtitle="Disponibilidade da equipe e da frota">
          <div className="capacity-grid">
            <CapacityItem label="Entregadores disponíveis" value={dashboard?.driversAvailable ?? 0} total={(dashboard?.driversAvailable ?? 0) + (dashboard?.driversBusy ?? 0) + (dashboard?.driversInactive ?? 0)} tone="success" />
            <CapacityItem label="Entregadores em rota" value={dashboard?.driversBusy ?? 0} total={(dashboard?.driversAvailable ?? 0) + (dashboard?.driversBusy ?? 0) + (dashboard?.driversInactive ?? 0)} tone="warning" />
            <CapacityItem label="Veículos disponíveis" value={dashboard?.vehiclesAvailable ?? 0} total={dashboard?.activeVehicles ?? 0} tone="success" />
            <CapacityItem label="Veículos em uso" value={dashboard?.vehiclesInUse ?? 0} total={dashboard?.activeVehicles ?? 0} tone="warning" />
          </div>
        </Panel>
        <Panel title="Últimas entregas" subtitle="Atualizações mais recentes">
          <div className="timeline">
            {dashboard?.latestDeliveries.map((delivery) => (
              <div className="timeline-item" key={delivery.orderId}>
                <span />
                <div>
                  <strong>{delivery.clientName}</strong>
                  <p>{delivery.deliveryAddress} · {statusLabels[delivery.status]}</p>
                  <small>{formatDate(delivery.updatedAt)}</small>
                </div>
              </div>
            ))}
            {!dashboard?.latestDeliveries.length && <EmptyState text="Nenhuma entrega registrada ainda." />}
          </div>
        </Panel>
        <Panel title="Mapa operacional" subtitle="Preparado para rastreamento futuro">
          <div className="map-placeholder"><MapPinned size={34} /> Mapa em breve</div>
        </Panel>
      </section>
    </>
  );
}

function DailyDeliveriesChart(props: { data: DashboardOverview["deliveredLastSevenDays"] }) {
  const maxValue = Math.max(1, ...props.data.map((item) => item.delivered));

  return (
    <div className="bar-chart" aria-label="Entregas concluidas nos ultimos 7 dias">
      {props.data.map((item) => {
        const height = Math.max(8, Math.round((item.delivered / maxValue) * 100));

        return (
          <div className="bar-chart-item" key={item.date}>
            <div className="bar-track">
              <span style={{ height: `${height}%` }} />
            </div>
            <strong>{item.delivered}</strong>
            <small>{formatShortDate(item.date)}</small>
          </div>
        );
      })}
      {!props.data.length && <EmptyState text="Ainda não há entregas concluídas para montar o gráfico." />}
    </div>
  );
}

function StatusDistributionChart(props: { data: DashboardOverview["statusDistribution"] }) {
  const total = props.data.reduce((sum, item) => sum + item.total, 0);

  return (
    <div className="status-chart">
      {props.data.map((item) => {
        const percent = total ? Math.round((item.total / total) * 100) : 0;

        return (
          <div className="status-chart-row" key={item.status}>
            <div>
              <StatusPill status={item.status} />
              <strong>{item.total}</strong>
            </div>
            <div className="progress-track">
              <span className={item.status.toLowerCase().replace("_", "-")} style={{ width: `${percent}%` }} />
            </div>
            <small>{percent}%</small>
          </div>
        );
      })}
      {!props.data.length && <EmptyState text="Sem dados suficientes para a distribuição." />}
    </div>
  );
}

function CapacityItem(props: { label: string; value: number; total: number; tone: "success" | "warning" | "danger" }) {
  const percent = props.total ? Math.round((props.value / props.total) * 100) : 0;

  return (
    <div className="capacity-item">
      <div>
        <span>{props.label}</span>
        <strong>{props.value}</strong>
      </div>
      <div className="progress-track">
        <span className={props.tone} style={{ width: `${percent}%` }} />
      </div>
      <small>{percent}% do total</small>
    </div>
  );
}

function ClientsPage(props: { showToast: (type: "success" | "error", message: string) => void }) {
  const [clients, setClients] = useState<Client[]>([]);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [form, setForm] = useState(emptyClient);
  const [selected, setSelected] = useState<Client | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);

  useEffect(() => {
    loadClients();
  }, [page]);

  async function loadClients() {
    try {
      const data = await api.listClientsPage({ search, page, size: 8 });
      setClients(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setSelected((current) => data.content.find((client) => client.id === current?.id) ?? data.content[0] ?? null);
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      if (editingId) {
        await api.updateClient(editingId, form);
        props.showToast("success", "Cliente atualizado");
      } else {
        await api.createClient(form);
        props.showToast("success", "Cliente criado");
      }

      setForm(emptyClient);
      setEditingId(null);
      await loadClients();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  function applySearch(event: FormEvent) {
    event.preventDefault();
    setPage(0);
    loadClients();
  }

  function startEdit(client: Client) {
    setSelected(client);
    setEditingId(client.id);
    setForm({
      name: client.name,
      email: client.email,
      document: client.document,
      phone: client.phone,
    });
  }

  async function deleteClient(client: Client) {
    const confirmed = window.confirm(`Remover o cliente ${client.name}?`);
    if (!confirmed) return;

    try {
      await api.deleteClient(client.id);
      props.showToast("success", "Cliente removido");
      setForm(emptyClient);
      setEditingId(null);
      await loadClients();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  return (
    <>
      <PageHeader title="Clientes" description="Pesquisa, paginação, edição e exclusão lógica de clientes." onRefresh={loadClients} />
      <section className="content-grid">
        <Panel title="Base de clientes" subtitle={`${totalElements} registros encontrados`}>
          <form className="search-row" onSubmit={applySearch}>
            <Search size={16} />
            <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar cliente" />
            <button className="primary-button" type="submit">Buscar</button>
          </form>
          <DataTable headers={["Código", "Nome", "Email", "Documento", "Telefone", "Ações"]}>
            {clients.map((client) => (
              <tr key={client.id} onClick={() => setSelected(client)} className={selected?.id === client.id ? "selected" : ""}>
                <td>{client.code}</td>
                <td>{client.name}</td>
                <td>{client.email}</td>
                <td>{client.document}</td>
                <td>{client.phone}</td>
                <td>
                  <div className="button-row">
                    <button className="ghost-button" type="button" onClick={(event) => { event.stopPropagation(); startEdit(client); }}>Editar</button>
                    <button className="danger-button" type="button" onClick={(event) => { event.stopPropagation(); deleteClient(client); }}>Excluir</button>
                  </div>
                </td>
              </tr>
            ))}
          </DataTable>
          {!clients.length && <EmptyState text="Nenhum cliente encontrado." />}
          <div className="pagination-row">
            <button className="ghost-button" disabled={page === 0} onClick={() => setPage((current) => Math.max(current - 1, 0))} type="button">Anterior</button>
            <span>Página {page + 1} de {Math.max(totalPages, 1)}</span>
            <button className="ghost-button" disabled={page + 1 >= totalPages} onClick={() => setPage((current) => current + 1)} type="button">Próxima</button>
          </div>
        </Panel>
        <Panel title={editingId ? "Editar cliente" : "Novo cliente"} subtitle={selected ? `Detalhe: ${selected.name}` : "Cadastro rápido"}>
          <form className="stack-form" onSubmit={submit}>
            <input placeholder="Nome" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
            <input placeholder="Email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value.trim().toLowerCase() })} />
            <input placeholder="Documento" value={form.document} onChange={(event) => setForm({ ...form, document: onlyDigits(event.target.value, 20) })} />
            <input placeholder="Telefone" value={form.phone} onChange={(event) => setForm({ ...form, phone: maskPhone(event.target.value) })} />
            <button className="primary-button" type="submit">{editingId ? "Salvar alterações" : "Criar cliente"}</button>
            {editingId && <button className="ghost-button" type="button" onClick={() => { setEditingId(null); setForm(emptyClient); }}>Cancelar edição</button>}
          </form>
          {selected && <DetailList items={[["Email", selected.email], ["Documento", selected.document], ["Telefone", selected.phone]]} />}
        </Panel>
      </section>
    </>
  );
}

function LegacyClientsPage(props: { showToast: (type: "success" | "error", message: string) => void }) {
  const [clients, setClients] = useState<Client[]>([]);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(emptyClient);
  const [selected, setSelected] = useState<Client | null>(null);

  useEffect(() => {
    loadClients();
  }, []);

  async function loadClients() {
    try {
      const data = await api.listClients();
      setClients(data);
      setSelected(data[0] ?? null);
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      await api.createClient(form);
      setForm(emptyClient);
      props.showToast("success", "Cliente criado");
      await loadClients();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  const filtered = clients.filter((client) => normalized(client.name + client.email + client.document).includes(normalized(search)));

  return (
    <>
      <PageHeader title="Clientes" description="Pesquisa, cadastro e detalhes de clientes." onRefresh={loadClients} />
      <section className="content-grid">
        <Panel title="Base de clientes" subtitle={`${filtered.length} registros encontrados`}>
          <SearchBox value={search} onChange={setSearch} placeholder="Pesquisar cliente" />
          <DataTable headers={["Nome", "Email", "Documento", "Telefone"]}>
            {filtered.map((client) => (
              <tr key={client.id} onClick={() => setSelected(client)} className={selected?.id === client.id ? "selected" : ""}>
                <td>{client.name}</td>
                <td>{client.email}</td>
                <td>{client.document}</td>
                <td>{client.phone}</td>
              </tr>
            ))}
          </DataTable>
        </Panel>
        <Panel title="Novo cliente" subtitle={selected ? `Detalhe: ${selected.name}` : "Cadastro rápido"}>
          <QuickClientForm form={form} setForm={setForm} onSubmit={submit} />
          {selected && <DetailList items={[["Email", selected.email], ["Documento", selected.document], ["Telefone", selected.phone]]} />}
        </Panel>
      </section>
    </>
  );
}

function DeliveryPersonsPage(props: { role: Role; showToast: (type: "success" | "error", message: string) => void }) {
  const [people, setPeople] = useState<DeliveryPerson[]>([]);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(emptyDeliveryPerson);
  const [editingId, setEditingId] = useState<string | null>(null);

  useEffect(() => {
    loadPeople();
  }, []);

  async function loadPeople() {
    try {
      setPeople(await api.listDeliveryPersons());
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function deleteDeliveryPerson(person: DeliveryPerson) {
    const confirmed = window.confirm(`Remover o entregador ${person.name}?`);
    if (!confirmed) return;

    try {
      await api.deleteDeliveryPerson(person.id);
      props.showToast("success", "Entregador removido");
      await loadPeople();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  function startEditPerson(person: DeliveryPerson) {
    setEditingId(person.id);
    setForm({
      name: person.name,
      email: person.email,
      document: person.document,
      phone: person.phone,
    });
  }

  async function submitEdit(event: FormEvent) {
    event.preventDefault();
    if (!editingId) return;

    try {
      await api.updateDeliveryPerson(editingId, form);
      props.showToast("success", "Entregador atualizado");
      setEditingId(null);
      setForm(emptyDeliveryPerson);
      await loadPeople();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  const filteredPeople = people.filter((person) =>
    normalized(`${person.code} ${person.name} ${person.email} ${person.document} ${person.phone} ${person.assignedVehicle ?? ""}`).includes(normalized(search))
  );

  return (
    <>
      <PageHeader title="Entregadores" description="Equipe operacional, contatos e status de disponibilidade." onRefresh={loadPeople} />
      <section className="content-grid">
        <Panel title="Entregadores" subtitle={`${filteredPeople.length} registros`}>
          <SearchBox value={search} onChange={setSearch} placeholder="Pesquisar entregador" />
          <DataTable headers={props.role === "ADMIN" ? ["Código", "Nome", "Telefone", "CNH", "Veículo", "Status", "Ações"] : ["Código", "Nome", "Telefone", "CNH", "Veículo", "Status"]}>
            {filteredPeople.map((person) => (
              <tr key={person.id} onClick={() => startEditPerson(person)} className={editingId === person.id ? "selected" : ""}>
                <td>{person.code}</td>
                <td>{person.name}</td>
                <td>{person.phone}</td>
                <td>{person.document}</td>
                <td>{person.assignedVehicle ?? "Não atribuído"}</td>
                <td><AvailabilityPill tone={deliveryPersonStatusTone(person.status)}>{deliveryPersonStatusLabel(person.status)}</AvailabilityPill></td>
                {props.role === "ADMIN" && (
                  <td>
                    <button className="danger-button" type="button" onClick={(event) => { event.stopPropagation(); deleteDeliveryPerson(person); }}>Excluir</button>
                  </td>
                )}
              </tr>
            ))}
          </DataTable>
        </Panel>
        <Panel title={editingId ? "Editar entregador" : "Cadastro centralizado"} subtitle="Acesso e vínculo operacional">
          {editingId ? (
            <form className="stack-form" onSubmit={submitEdit}>
              <input placeholder="Nome" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
              <input placeholder="Email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value.trim().toLowerCase() })} />
              <input placeholder="CNH ou documento" value={form.document} onChange={(event) => setForm({ ...form, document: onlyDigits(event.target.value, 20) })} />
              <input placeholder="Telefone" value={form.phone} onChange={(event) => setForm({ ...form, phone: maskPhone(event.target.value) })} />
              <button className="primary-button" type="submit">Salvar entregador</button>
              <button className="ghost-button" type="button" onClick={() => { setEditingId(null); setForm(emptyDeliveryPerson); }}>Cancelar edição</button>
            </form>
          ) : (
            <EmptyState text="Novos entregadores devem ser criados na aba Usuários por um administrador." />
          )}
        </Panel>
      </section>
    </>
  );
}

function VehiclesPage(props: { role: Role; showToast: (type: "success" | "error", message: string) => void }) {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(emptyVehicle);
  const [editingId, setEditingId] = useState<string | null>(null);

  useEffect(() => {
    loadVehicles();
  }, []);

  async function loadVehicles() {
    try {
      setVehicles(await api.listVehicles());
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      if (editingId) {
        await api.updateVehicle(editingId, form);
        props.showToast("success", "Veículo atualizado");
      } else {
        await api.createVehicle(form);
        props.showToast("success", "Veículo criado");
      }
      setForm(emptyVehicle);
      setEditingId(null);
      await loadVehicles();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function deleteVehicle(vehicle: Vehicle) {
    const confirmed = window.confirm(`Remover o veículo ${vehicle.licensePlate}?`);
    if (!confirmed) return;

    try {
      await api.deleteVehicle(vehicle.id);
      props.showToast("success", "Veículo removido");
      await loadVehicles();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  function startEditVehicle(vehicle: Vehicle) {
    setEditingId(vehicle.id);
    setForm({
      licensePlate: vehicle.licensePlate,
      brand: vehicle.brand,
      model: vehicle.model,
    });
  }

  const filteredVehicles = vehicles.filter((vehicle) =>
    normalized(`${vehicle.code} ${vehicle.licensePlate} ${vehicle.brand} ${vehicle.model} ${vehicle.status}`).includes(normalized(search))
  );

  return (
    <>
      <PageHeader title="Veículos" description="Frota, disponibilidade e preparação para manutenção." onRefresh={loadVehicles} />
      <section className="content-grid">
        <Panel title="Frota" subtitle={`${filteredVehicles.length} veículos`}>
          <SearchBox value={search} onChange={setSearch} placeholder="Pesquisar veículo" />
          <DataTable headers={props.role === "ADMIN" ? ["Código", "Placa", "Marca", "Modelo", "Status", "Ações"] : ["Código", "Placa", "Marca", "Modelo", "Status"]}>
            {filteredVehicles.map((vehicle) => (
              <tr key={vehicle.id} onClick={() => startEditVehicle(vehicle)} className={editingId === vehicle.id ? "selected" : ""}>
                <td>{vehicle.code}</td>
                <td>{vehicle.licensePlate}</td>
                <td>{vehicle.brand}</td>
                <td>{vehicle.model}</td>
                <td><AvailabilityPill tone={vehicleStatusTone(vehicle)}>{vehicleStatusLabel(vehicle)}</AvailabilityPill></td>
                {props.role === "ADMIN" && (
                  <td>
                    <button className="danger-button" type="button" onClick={(event) => { event.stopPropagation(); deleteVehicle(vehicle); }}>Excluir</button>
                  </td>
                )}
              </tr>
            ))}
          </DataTable>
        </Panel>
        <Panel title={editingId ? "Editar veículo" : "Novo veículo"} subtitle="Cadastro rápido">
          <QuickVehicleForm form={form} setForm={setForm} onSubmit={submit} />
          {editingId && <button className="ghost-button" type="button" onClick={() => { setEditingId(null); setForm(emptyVehicle); }}>Cancelar edição</button>}
        </Panel>
      </section>
    </>
  );
}

function OrdersPage(props: { showToast: (type: "success" | "error", message: string) => void; mode?: "driver" | "client" }) {
  const [clients, setClients] = useState<Client[]>([]);
  const [deliveryPersons, setDeliveryPersons] = useState<DeliveryPerson[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [history, setHistory] = useState<DeliveryHistory[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState("");
  const [filters, setFilters] = useState({ clientId: "", deliveryPersonId: "", status: "" });
  const [orderForm, setOrderForm] = useState(emptyOrder);
  const [assignForm, setAssignForm] = useState({ deliveryPersonId: "", vehicleId: "" });
  const [statusNotes, setStatusNotes] = useState("");

  const selectedOrder = useMemo(() => orders.find((order) => order.id === selectedOrderId) ?? orders[0], [orders, selectedOrderId]);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (selectedOrder) loadHistory(selectedOrder.deliveryId);
  }, [selectedOrder?.deliveryId]);

  async function loadData(nextFilters = filters) {
    try {
      const [clientData, personData, vehicleData, orderData] = await Promise.all([
        api.listClients().catch(() => []),
        api.listDeliveryPersons().catch(() => []),
        api.listVehicles().catch(() => []),
        api.listOrders({
          clientId: nextFilters.clientId || undefined,
          deliveryPersonId: nextFilters.deliveryPersonId || undefined,
          status: nextFilters.status || undefined,
        }),
      ]);
      setClients(clientData);
      setDeliveryPersons(personData);
      setVehicles(vehicleData);
      setOrders(orderData);
      setSelectedOrderId((current) => current || orderData[0]?.id || "");
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function loadHistory(deliveryId: string) {
    try {
      setHistory(await api.getDeliveryHistory(deliveryId));
    } catch {
      setHistory([]);
    }
  }

  async function submitOrder(event: FormEvent) {
    event.preventDefault();
    try {
      const created = await api.createOrder(orderForm);
      setOrderForm(emptyOrder);
      setSelectedOrderId(created.id);
      props.showToast("success", "Pedido criado");
      await loadData();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function assignDelivery(event: FormEvent) {
    event.preventDefault();
    if (!selectedOrder) return;
    try {
      await api.assignDelivery(selectedOrder.id, assignForm);
      setAssignForm({ deliveryPersonId: "", vehicleId: "" });
      props.showToast("success", "Entrega atribuída");
      await loadData();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  async function updateStatus(status: DeliveryStatus) {
    if (!selectedOrder) return;
    try {
      await api.updateDeliveryStatus(selectedOrder.deliveryId, { status, notes: statusNotes });
      setStatusNotes("");
      props.showToast("success", `Status atualizado para ${statusLabels[status]}`);
      await loadData();
    } catch (error) {
      props.showToast("error", getErrorMessage(error));
    }
  }

  function applyFilters(event: FormEvent) {
    event.preventDefault();
    loadData(filters);
  }

  const title = props.mode === "driver" ? "Minhas entregas" : props.mode === "client" ? "Meus pedidos" : "Pedidos";

  return (
    <>
      <PageHeader title={title} description="Tela operacional com filtros, detalhes, timeline e atualização de status." onRefresh={() => loadData()} />
      <section className="content-grid">
        <Panel title="Lista de pedidos" subtitle={`${orders.length} registros carregados`}>
          <form className="filters" onSubmit={applyFilters}>
            <label>Cliente<select value={filters.clientId} onChange={(event) => setFilters({ ...filters, clientId: event.target.value })}><option value="">Todos</option>{clients.map((client) => <option value={client.id} key={client.id}>{client.name}</option>)}</select></label>
            <label>Entregador<select value={filters.deliveryPersonId} onChange={(event) => setFilters({ ...filters, deliveryPersonId: event.target.value })}><option value="">Todos</option>{deliveryPersons.map((person) => <option value={person.id} key={person.id}>{person.name}</option>)}</select></label>
            <label>Status<select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value })}><option value="">Todos</option>{Object.entries(statusLabels).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>
            <button className="primary-button" type="submit"><Search size={16} /> Filtrar</button>
          </form>
          <DataTable headers={["Pedido", "Cliente", "Status", "Rota", "Atualizado"]}>
            {orders.map((order) => (
              <tr key={order.id} className={selectedOrder?.id === order.id ? "selected" : ""} onClick={() => setSelectedOrderId(order.id)}>
                <td><code>{order.trackingCode}</code></td>
                <td>{order.clientName}</td>
                <td><StatusPill status={order.status} /></td>
                <td>{order.pickupAddress} → {order.deliveryAddress}</td>
                <td>{formatDate(order.updatedAt)}</td>
              </tr>
            ))}
          </DataTable>
          {!orders.length && <EmptyState text="Nenhum pedido encontrado." />}
        </Panel>
        <DeliveryDetail
          order={selectedOrder}
          history={history}
          deliveryPersons={deliveryPersons.filter((person) => person.status === "AVAILABLE")}
          vehicles={vehicles.filter((vehicle) => vehicle.active && vehicle.status === "AVAILABLE")}
          assignForm={assignForm}
          setAssignForm={setAssignForm}
          statusNotes={statusNotes}
          setStatusNotes={setStatusNotes}
          onAssign={assignDelivery}
          onStatus={updateStatus}
        />
      </section>
      {!props.mode && (
        <section className="forms-grid single">
          <Panel title="Novo pedido" subtitle="Cadastro operacional">
            <QuickOrderForm form={orderForm} setForm={setOrderForm} clients={clients} onSubmit={submitOrder} />
          </Panel>
        </section>
      )}
    </>
  );
}

function TrackingPage(props: { showToast: (type: "success" | "error", message: string) => void }) {
  return <OrdersPage showToast={props.showToast} mode="client" />;
}

function DeliveryDetail(props: {
  order?: Order;
  history: DeliveryHistory[];
  deliveryPersons: DeliveryPerson[];
  vehicles: Vehicle[];
  assignForm: { deliveryPersonId: string; vehicleId: string };
  setAssignForm: (value: { deliveryPersonId: string; vehicleId: string }) => void;
  statusNotes: string;
  setStatusNotes: (value: string) => void;
  onAssign: (event: FormEvent) => void;
  onStatus: (status: DeliveryStatus) => void;
}) {
  const availableNext = props.order ? nextStatuses[props.order.status] ?? [] : [];

  return (
    <Panel title="Detalhe da entrega" subtitle={props.order ? props.order.trackingCode : "Nenhum pedido selecionado"}>
      {props.order ? (
        <>
          <div className="detail-route">
            <StatusPill status={props.order.status} />
            <strong>{props.order.clientName}</strong>
            <span>{props.order.pickupAddress}</span>
            <span>{props.order.deliveryAddress}</span>
          </div>
          {props.order.status === "PENDING" && (
            <form className="stack-form" onSubmit={props.onAssign}>
              <select value={props.assignForm.deliveryPersonId} onChange={(event) => props.setAssignForm({ ...props.assignForm, deliveryPersonId: event.target.value })}>
                <option value="">Entregador disponível</option>
                {props.deliveryPersons.map((person) => <option value={person.id} key={person.id}>{person.name}</option>)}
              </select>
              <select value={props.assignForm.vehicleId} onChange={(event) => props.setAssignForm({ ...props.assignForm, vehicleId: event.target.value })}>
                <option value="">Veículo ativo</option>
                {props.vehicles.map((vehicle) => <option value={vehicle.id} key={vehicle.id}>{vehicle.licensePlate} - {vehicle.model}</option>)}
              </select>
              <button className="primary-button" type="submit">Atribuir</button>
            </form>
          )}
          <div className="status-actions">
            <input placeholder="Observações para o próximo status" value={props.statusNotes} onChange={(event) => props.setStatusNotes(event.target.value)} />
            <div className="button-row">
              {availableNext.map((status) => <button className="secondary-button" key={status} onClick={() => props.onStatus(status)}>{statusLabels[status]}</button>)}
              {!availableNext.length && <span className="muted">Nenhuma ação de status disponível.</span>}
            </div>
          </div>
          <div className="timeline">
            {props.history.map((item) => (
              <div className="timeline-item" key={item.id}>
                <span />
                <div>
                  <strong>{statusLabels[item.newStatus]}</strong>
                  <p>{item.notes ?? "Status alterado"}</p>
                  <small>{formatDate(item.changedAt)}</small>
                </div>
              </div>
            ))}
          </div>
        </>
      ) : <EmptyState text="Selecione um pedido para gerenciar a entrega." />}
    </Panel>
  );
}

function PageHeader(props: { title: string; description: string; onRefresh?: () => void; loading?: boolean }) {
  return (
    <header className="topbar">
      <div>
        <h1>{props.title}</h1>
        <p>{props.description}</p>
      </div>
      {props.onRefresh && (
        <button className="icon-button" onClick={props.onRefresh} aria-label="Atualizar dados">
          <RefreshCw className={props.loading ? "spin" : ""} size={18} />
        </button>
      )}
    </header>
  );
}

function Panel(props: { title: string; subtitle?: string; children: ReactNode }) {
  return (
    <section className="main-panel">
      <div className="panel-heading">
        <div>
          <h2>{props.title}</h2>
          {props.subtitle && <p>{props.subtitle}</p>}
        </div>
      </div>
      {props.children}
    </section>
  );
}

function MetricCard(props: { label: string; value: number; icon: ReactNode }) {
  return <article className="metric-card"><div className="metric-icon">{props.icon}</div><span>{props.label}</span><strong>{props.value}</strong></article>;
}

function SearchBox(props: { value: string; onChange: (value: string) => void; placeholder: string }) {
  return <div className="search-row"><Search size={16} /><input value={props.value} onChange={(event) => props.onChange(event.target.value)} placeholder={props.placeholder} /></div>;
}

function DataTable(props: { headers: string[]; children: ReactNode }) {
  return (
    <div className="table-wrap">
      <table>
        <thead><tr>{props.headers.map((header) => <th key={header}>{header}</th>)}</tr></thead>
        <tbody>{props.children}</tbody>
      </table>
    </div>
  );
}

function QuickClientForm(props: { form: typeof emptyClient; setForm: (value: typeof emptyClient) => void; onSubmit: (event: FormEvent) => void }) {
  return <form className="stack-form" onSubmit={props.onSubmit}><input placeholder="Nome" value={props.form.name} onChange={(event) => props.setForm({ ...props.form, name: event.target.value })} /><input placeholder="Email" value={props.form.email} onChange={(event) => props.setForm({ ...props.form, email: event.target.value.trim().toLowerCase() })} /><input placeholder="Documento" value={props.form.document} onChange={(event) => props.setForm({ ...props.form, document: onlyDigits(event.target.value, 20) })} /><input placeholder="Telefone" value={props.form.phone} onChange={(event) => props.setForm({ ...props.form, phone: maskPhone(event.target.value) })} /><button className="primary-button" type="submit">Criar cliente</button></form>;
}

function QuickDeliveryPersonForm(props: { form: typeof emptyDeliveryPerson; setForm: (value: typeof emptyDeliveryPerson) => void; onSubmit: (event: FormEvent) => void }) {
  return <form className="stack-form" onSubmit={props.onSubmit}><input placeholder="Nome" value={props.form.name} onChange={(event) => props.setForm({ ...props.form, name: event.target.value })} /><input placeholder="Email" value={props.form.email} onChange={(event) => props.setForm({ ...props.form, email: event.target.value.trim().toLowerCase() })} /><input placeholder="CNH ou documento" value={props.form.document} onChange={(event) => props.setForm({ ...props.form, document: onlyDigits(event.target.value, 20) })} /><input placeholder="Telefone" value={props.form.phone} onChange={(event) => props.setForm({ ...props.form, phone: maskPhone(event.target.value) })} /><button className="primary-button" type="submit">Criar entregador</button></form>;
}

function QuickVehicleForm(props: { form: typeof emptyVehicle; setForm: (value: typeof emptyVehicle) => void; onSubmit: (event: FormEvent) => void }) {
  return <form className="stack-form" onSubmit={props.onSubmit}><input placeholder="Placa" value={props.form.licensePlate} onChange={(event) => props.setForm({ ...props.form, licensePlate: maskPlate(event.target.value) })} /><input placeholder="Marca" value={props.form.brand} onChange={(event) => props.setForm({ ...props.form, brand: event.target.value })} /><input placeholder="Modelo" value={props.form.model} onChange={(event) => props.setForm({ ...props.form, model: event.target.value })} /><button className="primary-button" type="submit">Criar veículo</button></form>;
}

function QuickOrderForm(props: { form: typeof emptyOrder; setForm: (value: typeof emptyOrder) => void; clients: Client[]; onSubmit: (event: FormEvent) => void }) {
  return <form className="stack-form" onSubmit={props.onSubmit}><select value={props.form.clientId} onChange={(event) => props.setForm({ ...props.form, clientId: event.target.value })}><option value="">Selecione o cliente</option>{props.clients.map((client) => <option value={client.id} key={client.id}>{client.name}</option>)}</select><input placeholder="Endereço de coleta" value={props.form.pickupAddress} onChange={(event) => props.setForm({ ...props.form, pickupAddress: event.target.value })} /><input placeholder="Endereço de entrega" value={props.form.deliveryAddress} onChange={(event) => props.setForm({ ...props.form, deliveryAddress: event.target.value })} /><input placeholder="Descrição" value={props.form.description} onChange={(event) => props.setForm({ ...props.form, description: event.target.value })} /><button className="primary-button" type="submit">Criar pedido</button></form>;
}

function DetailList(props: { items: Array<[string, string]> }) {
  return <dl className="detail-list">{props.items.map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl>;
}

function PlaceholderPage(props: { title: string; text: string }) {
  return <><PageHeader title={props.title} description={props.text} /><Panel title="Em preparação" subtitle="Estrutura criada para evolução incremental"><EmptyState text="Esta página já respeita autenticação, autorização e navegação por perfil." /></Panel></>;
}

function EmptyState(props: { text: string }) {
  return <div className="empty-state">{props.text}</div>;
}

function StatusPill({ status }: { status: DeliveryStatus }) {
  return <span className={`status-pill ${status.toLowerCase().replace("_", "-")}`}>{statusLabels[status]}</span>;
}

function AvailabilityPill(props: { tone: "success" | "warning" | "danger"; children: ReactNode }) {
  return <span className={`availability-pill ${props.tone}`}>{props.children}</span>;
}

function shortId(id: string) {
  return id.slice(0, 8);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" }).format(new Date(value));
}

function formatShortDate(value: string) {
  return new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "2-digit" }).format(new Date(`${value}T00:00:00`));
}

function normalized(value: string) {
  return value.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

function onboardingStorageKey(role: Role) {
  return `logitrack.onboarding.${role}.done`;
}

function onlyDigits(value: string, maxLength = 20) {
  return value.replace(/\D/g, "").slice(0, maxLength);
}

function maskPhone(value: string) {
  const digits = onlyDigits(value, 11);
  if (digits.length <= 10) {
    return digits.replace(/^(\d{0,2})(\d{0,4})(\d{0,4}).*/, (_, ddd, first, last) =>
      [ddd && `(${ddd}`, ddd.length === 2 && ") ", first, last && `-${last}`].filter(Boolean).join("")
    );
  }
  return digits.replace(/^(\d{0,2})(\d{0,5})(\d{0,4}).*/, (_, ddd, first, last) =>
    [ddd && `(${ddd}`, ddd.length === 2 && ") ", first, last && `-${last}`].filter(Boolean).join("")
  );
}

function maskPlate(value: string) {
  return value.replace(/[^a-zA-Z0-9-]/g, "").toUpperCase().slice(0, 8);
}

function deliveryPersonStatusLabel(status: DeliveryPerson["status"]) {
  return status === "AVAILABLE" ? "Disponível" : status === "ON_DELIVERY" ? "Em rota" : "Inativo";
}

function deliveryPersonStatusTone(status: DeliveryPerson["status"]) {
  if (status === "AVAILABLE") return "success";
  if (status === "ON_DELIVERY") return "warning";
  return "danger";
}

function vehicleStatusLabel(vehicle: Vehicle) {
  if (!vehicle.active) return "Inativo";
  if (vehicle.status === "IN_USE") return "Em uso";
  if (vehicle.status === "MAINTENANCE") return "Manutenção";
  return "Disponível";
}

function vehicleStatusTone(vehicle: Vehicle) {
  if (!vehicle.active) return "danger";
  if (vehicle.status === "AVAILABLE") return "success";
  return "warning";
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Erro inesperado";
}

export default App;
